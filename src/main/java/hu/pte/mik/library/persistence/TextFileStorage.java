package hu.pte.mik.library.persistence;

import hu.pte.mik.library.domain.Book;
import hu.pte.mik.library.domain.Dvd;
import hu.pte.mik.library.domain.LibraryItem;
import hu.pte.mik.library.domain.Loan;
import hu.pte.mik.library.domain.Magazine;
import hu.pte.mik.library.domain.Member;
import hu.pte.mik.library.exception.StorageException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the library in three plain text files, one record per line,
 * with fields separated by the '|' character.
 */
public class TextFileStorage implements LibraryStorage {

    private static final String SEPARATOR = "|";
    private static final String SPLIT_PATTERN = "\\|";

    private final Path itemsFile;
    private final Path membersFile;
    private final Path loansFile;
    private final Path directory;

    public TextFileStorage(Path directory) {
        this.directory = directory;
        this.itemsFile = directory.resolve("items.txt");
        this.membersFile = directory.resolve("members.txt");
        this.loansFile = directory.resolve("loans.txt");
    }

    @Override
    public void save(LibraryData data) throws StorageException {
        try {
            Files.createDirectories(directory);
            writeLines(itemsFile, data.items().stream().map(this::formatItem).toList());
            writeLines(membersFile, data.members().stream().map(this::formatMember).toList());
            writeLines(loansFile, data.loans().stream().map(this::formatLoan).toList());
        } catch (IOException e) {
            throw new StorageException("Could not save data to " + directory.toAbsolutePath(), e);
        }
    }

    @Override
    public LibraryData load() throws StorageException {
        Map<Integer, LibraryItem> items = new HashMap<>();
        Map<Integer, Member> members = new HashMap<>();
        List<LibraryItem> itemList = new ArrayList<>();
        List<Member> memberList = new ArrayList<>();
        List<Loan> loans = new ArrayList<>();

        for (Line line : readLines(itemsFile)) {
            LibraryItem item = line.parse(this::parseItem);
            items.put(item.getId(), item);
            itemList.add(item);
        }
        for (Line line : readLines(membersFile)) {
            Member member = line.parse(this::parseMember);
            members.put(member.getId(), member);
            memberList.add(member);
        }
        for (Line line : readLines(loansFile)) {
            Loan loan = line.parse(fields -> parseLoan(fields, items, members));
            if (loan.isActive()) {
                loan.getItem().markBorrowed();
            }
            loans.add(loan);
        }
        return new LibraryData(itemList, memberList, loans);
    }

    // ---------- writing ----------

    private void writeLines(Path file, List<String> lines) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private String formatItem(LibraryItem item) {
        String extra;
        if (item instanceof Book book) {
            extra = book.getAuthor();
        } else if (item instanceof Magazine magazine) {
            extra = String.valueOf(magazine.getIssueNumber());
        } else if (item instanceof Dvd dvd) {
            extra = String.valueOf(dvd.getDurationMinutes());
        } else {
            throw new IllegalArgumentException("Unknown item type: " + item.getClass().getSimpleName());
        }
        return String.join(SEPARATOR, item.getTypeName(), String.valueOf(item.getId()),
                item.getTitle(), String.valueOf(item.getYear()), extra);
    }

    private String formatMember(Member member) {
        return String.join(SEPARATOR, String.valueOf(member.getId()), member.getName(), member.getEmail());
    }

    private String formatLoan(Loan loan) {
        return String.join(SEPARATOR,
                String.valueOf(loan.getItem().getId()),
                String.valueOf(loan.getMember().getId()),
                loan.getLoanDate().toString(),
                loan.getDueDate().toString(),
                loan.isActive() ? "-" : loan.getReturnDate().toString());
    }

    // ---------- reading ----------

    private List<Line> readLines(Path file) throws StorageException {
        List<Line> lines = new ArrayList<>();
        if (!Files.exists(file)) {
            return lines;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String text;
            int number = 0;
            while ((text = reader.readLine()) != null) {
                number++;
                if (!text.isBlank()) {
                    lines.add(new Line(file, number, text));
                }
            }
        } catch (IOException e) {
            throw new StorageException("Could not read " + file.toAbsolutePath(), e);
        }
        return lines;
    }

    private LibraryItem parseItem(String[] f) {
        requireFields(f, 5);
        int id = Integer.parseInt(f[1]);
        int year = Integer.parseInt(f[3]);
        return switch (f[0]) {
            case "BOOK" -> new Book(id, f[2], year, f[4]);
            case "MAGAZINE" -> new Magazine(id, f[2], year, Integer.parseInt(f[4]));
            case "DVD" -> new Dvd(id, f[2], year, Integer.parseInt(f[4]));
            default -> throw new IllegalArgumentException("unknown item type '" + f[0] + "'");
        };
    }

    private Member parseMember(String[] f) {
        requireFields(f, 3);
        return new Member(Integer.parseInt(f[0]), f[1], f[2]);
    }

    private Loan parseLoan(String[] f, Map<Integer, LibraryItem> items, Map<Integer, Member> members) {
        requireFields(f, 5);
        LibraryItem item = items.get(Integer.parseInt(f[0]));
        Member member = members.get(Integer.parseInt(f[1]));
        if (item == null || member == null) {
            throw new IllegalArgumentException("loan refers to a missing item or member");
        }
        LocalDate returned = f[4].equals("-") ? null : LocalDate.parse(f[4]);
        return new Loan(item, member, LocalDate.parse(f[2]), LocalDate.parse(f[3]), returned);
    }

    private void requireFields(String[] fields, int expected) {
        if (fields.length != expected) {
            throw new IllegalArgumentException("expected " + expected + " fields but found " + fields.length);
        }
    }

    @FunctionalInterface
    private interface LineParser<T> {
        T parse(String[] fields);
    }

    /** One line of a data file; turns any parsing problem into a StorageException with its location. */
    private record Line(Path file, int number, String text) {

        <T> T parse(LineParser<T> parser) throws StorageException {
            try {
                return parser.parse(text.split(SPLIT_PATTERN, -1));
            } catch (RuntimeException e) {
                throw new StorageException("Corrupt data in " + file.getFileName()
                        + " line " + number + ": " + e.getMessage(), e);
            }
        }
    }
}
