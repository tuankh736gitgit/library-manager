package hu.pte.mik.library.ui;

import hu.pte.mik.library.domain.Book;
import hu.pte.mik.library.domain.Dvd;
import hu.pte.mik.library.domain.LibraryItem;
import hu.pte.mik.library.domain.Loan;
import hu.pte.mik.library.domain.Magazine;
import hu.pte.mik.library.domain.Member;
import hu.pte.mik.library.exception.LibraryException;
import hu.pte.mik.library.exception.StorageException;
import hu.pte.mik.library.persistence.LibraryStorage;
import hu.pte.mik.library.policy.CappedFinePolicy;
import hu.pte.mik.library.policy.GracePeriodFinePolicy;
import hu.pte.mik.library.policy.StandardFinePolicy;
import hu.pte.mik.library.service.LibraryService;

import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Text menu: reads the user's choices and shows the results. Contains no library rules. */
public class ConsoleMenu {

    private final LibraryService service;
    private final LibraryStorage storage;
    private final Scanner in;
    private final PrintStream out;

    public ConsoleMenu(LibraryService service, LibraryStorage storage, Scanner in, PrintStream out) {
        this.service = service;
        this.storage = storage;
        this.in = in;
        this.out = out;
    }

    public void run() {
        out.println("=== Library Manager ===");
        loadData();
        try {
            boolean running = true;
            while (running) {
                printMenu();
                running = handle(readLine("Choose: "));
            }
        } catch (NoSuchElementException endOfInput) {
            out.println();
        }
        saveData();
        out.println("Goodbye!");
    }

    private void printMenu() {
        out.println();
        out.println(" 1. Add item              6. Borrow item");
        out.println(" 2. Add member            7. Return item");
        out.println(" 3. List items            8. Active loans");
        out.println(" 4. Search items          9. Overdue loans");
        out.println(" 5. List members         10. Change fine policy");
        out.println(" 0. Save and exit");
    }

    /** Runs one menu action; returns false when the user wants to leave. */
    private boolean handle(String choice) {
        try {
            switch (choice) {
                case "1" -> addItem();
                case "2" -> addMember();
                case "3" -> listItems();
                case "4" -> searchItems();
                case "5" -> printAll(service.getMembers(), "No members yet.");
                case "6" -> borrowItem();
                case "7" -> returnItem();
                case "8" -> printAll(service.getActiveLoans(), "Nothing is on loan.");
                case "9" -> listOverdue();
                case "10" -> changeFinePolicy();
                case "0" -> {
                    return false;
                }
                default -> out.println("Unknown option, please choose a number from the menu.");
            }
        } catch (LibraryException | IllegalArgumentException e) {
            out.println("Error: " + e.getMessage());
        }
        return true;
    }

    // ---------- actions ----------

    private void addItem() throws LibraryException {
        String type = readLine("Type (1 = book, 2 = magazine, 3 = DVD): ");
        if (!List.of("1", "2", "3").contains(type)) {
            throw new IllegalArgumentException("Unknown item type.");
        }
        String title = readLine("Title: ");
        int year = readInt("Year: ");
        int id = service.nextItemId();
        LibraryItem item = switch (type) {
            case "1" -> new Book(id, title, year, readLine("Author: "));
            case "2" -> new Magazine(id, title, year, readInt("Issue number: "));
            default -> new Dvd(id, title, year, readInt("Duration in minutes: "));
        };
        service.addItem(item);
        saveData();
        out.println("Added: " + item);
    }

    private void addMember() throws LibraryException {
        Member member = new Member(service.nextMemberId(), readLine("Name: "), readLine("Email: "));
        service.addMember(member);
        saveData();
        out.println("Added: " + member);
    }

    private void listItems() {
        String order = readLine("Sort by (1 = title, 2 = year, newest first, 3 = type): ");
        Comparator<LibraryItem> comparator = switch (order) {
            case "2" -> Comparator.comparingInt(LibraryItem::getYear).reversed()
                    .thenComparing(Comparator.naturalOrder());
            case "3" -> Comparator.comparing(LibraryItem::getTypeName)
                    .thenComparing(Comparator.naturalOrder());
            default -> Comparator.naturalOrder();
        };
        printAll(service.getItems(comparator), "The catalogue is empty.");
    }

    private void searchItems() {
        printAll(service.searchItemsByTitle(readLine("Text in the title: ")), "No matching items.");
    }

    private void borrowItem() throws LibraryException {
        int itemId = readInt("Item id: ");
        int memberId = readInt("Member id: ");
        Loan loan = service.borrowItem(itemId, memberId, LocalDate.now());
        saveData();
        out.println("Borrowed. Due back on " + loan.getDueDate() + ".");
    }

    private void returnItem() throws LibraryException {
        int itemId = readInt("Item id: ");
        LocalDate date = readDate("Return date (YYYY-MM-DD, Enter = today): ");
        long fine = service.returnItem(itemId, date);
        saveData();
        out.println(fine == 0 ? "Returned on time, no fine." : "Returned late. Fine: " + fine + " HUF.");
    }

    private void listOverdue() {
        LocalDate today = LocalDate.now();
        List<Loan> overdue = service.getOverdueLoans(today);
        if (overdue.isEmpty()) {
            out.println("No overdue loans.");
            return;
        }
        for (Loan loan : overdue) {
            out.printf("%s | %d days late | fine so far: %d HUF%n",
                    loan, loan.daysOverdue(today), service.calculateFine(loan, today));
        }
    }

    private void changeFinePolicy() {
        out.println("Current policy: " + service.getFinePolicy().getName());
        String choice = readLine("New policy (1 = standard, 2 = 3 day grace period, 3 = capped at 1000 HUF): ");
        switch (choice) {
            case "1" -> service.setFinePolicy(new StandardFinePolicy());
            case "2" -> service.setFinePolicy(new GracePeriodFinePolicy(3));
            case "3" -> service.setFinePolicy(new CappedFinePolicy(1000));
            default -> throw new IllegalArgumentException("Unknown policy.");
        }
        out.println("Policy is now: " + service.getFinePolicy().getName());
    }

    // ---------- storage ----------

    private void loadData() {
        try {
            service.importData(storage.load());
            out.printf("Loaded %d items and %d members.%n",
                    service.getItems().size(), service.getMembers().size());
        } catch (StorageException e) {
            out.println("Warning: " + e.getMessage());
            out.println("Starting with an empty library.");
        }
    }

    private void saveData() {
        try {
            storage.save(service.exportData());
        } catch (StorageException e) {
            out.println("Warning: " + e.getMessage());
        }
    }

    // ---------- input helpers ----------

    private String readLine(String prompt) {
        out.print(prompt);
        return in.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                out.println("Please enter a whole number.");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            String text = readLine(prompt);
            if (text.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(text);
            } catch (DateTimeParseException e) {
                out.println("Please use the format YYYY-MM-DD, for example 2026-11-29.");
            }
        }
    }

    private void printAll(List<?> rows, String emptyMessage) {
        if (rows.isEmpty()) {
            out.println(emptyMessage);
        } else {
            rows.forEach(out::println);
        }
    }
}
