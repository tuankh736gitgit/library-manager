package hu.pte.mik.library.service;

import hu.pte.mik.library.domain.LibraryItem;
import hu.pte.mik.library.domain.Loan;
import hu.pte.mik.library.domain.Member;
import hu.pte.mik.library.exception.DuplicateEntityException;
import hu.pte.mik.library.exception.EntityNotFoundException;
import hu.pte.mik.library.exception.ItemNotAvailableException;
import hu.pte.mik.library.exception.LibraryException;
import hu.pte.mik.library.exception.LoanLimitExceededException;
import hu.pte.mik.library.persistence.LibraryData;
import hu.pte.mik.library.policy.FinePolicy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Holds the catalogue, the members and the loans, and enforces the library rules. */
public class LibraryService {

    public static final int MAX_ACTIVE_LOANS_PER_MEMBER = 3;

    private final List<LibraryItem> items = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();
    private FinePolicy finePolicy;

    public LibraryService(FinePolicy finePolicy) {
        this.finePolicy = Objects.requireNonNull(finePolicy, "finePolicy");
    }

    // ---------- fine policy (Strategy) ----------

    public FinePolicy getFinePolicy() {
        return finePolicy;
    }

    public void setFinePolicy(FinePolicy finePolicy) {
        this.finePolicy = Objects.requireNonNull(finePolicy, "finePolicy");
    }

    public long calculateFine(Loan loan, LocalDate onDate) {
        return finePolicy.calculateFine(loan, onDate);
    }

    // ---------- catalogue ----------

    public int nextItemId() {
        return items.stream().mapToInt(LibraryItem::getId).max().orElse(0) + 1;
    }

    public void addItem(LibraryItem item) throws DuplicateEntityException {
        Objects.requireNonNull(item, "item");
        if (items.contains(item)) {
            throw new DuplicateEntityException("Item", item.getId());
        }
        items.add(item);
    }

    public LibraryItem findItem(int id) throws EntityNotFoundException {
        return items.stream()
                .filter(item -> item.getId() == id)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
    }

    /** Items in their natural order (by title). */
    public List<LibraryItem> getItems() {
        return getItems(Comparator.naturalOrder());
    }

    public List<LibraryItem> getItems(Comparator<LibraryItem> order) {
        List<LibraryItem> sorted = new ArrayList<>(items);
        sorted.sort(order);
        return sorted;
    }

    public List<LibraryItem> searchItemsByTitle(String text) {
        String needle = text.toLowerCase(Locale.ROOT).trim();
        return items.stream()
                .filter(item -> item.getTitle().toLowerCase(Locale.ROOT).contains(needle))
                .sorted()
                .toList();
    }

    // ---------- members ----------

    public int nextMemberId() {
        return members.stream().mapToInt(Member::getId).max().orElse(0) + 1;
    }

    public void addMember(Member member) throws DuplicateEntityException {
        Objects.requireNonNull(member, "member");
        if (members.contains(member)) {
            throw new DuplicateEntityException("Member", member.getId());
        }
        members.add(member);
    }

    public Member findMember(int id) throws EntityNotFoundException {
        return members.stream()
                .filter(member -> member.getId() == id)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Member", id));
    }

    public List<Member> getMembers() {
        List<Member> sorted = new ArrayList<>(members);
        sorted.sort(Comparator.comparing(Member::getName, String.CASE_INSENSITIVE_ORDER));
        return sorted;
    }

    // ---------- loans ----------

    public Loan borrowItem(int itemId, int memberId, LocalDate date) throws LibraryException {
        LibraryItem item = findItem(itemId);
        Member member = findMember(memberId);
        if (!item.isAvailable()) {
            throw new ItemNotAvailableException(item);
        }
        if (getActiveLoansOf(member).size() >= MAX_ACTIVE_LOANS_PER_MEMBER) {
            throw new LoanLimitExceededException(member, MAX_ACTIVE_LOANS_PER_MEMBER);
        }
        Loan loan = new Loan(item, member, date);
        item.markBorrowed();
        loans.add(loan);
        return loan;
    }

    /** Closes the active loan of the item and returns the fine in HUF. */
    public long returnItem(int itemId, LocalDate date) throws LibraryException {
        LibraryItem item = findItem(itemId);
        Loan loan = loans.stream()
                .filter(l -> l.isActive() && l.getItem().equals(item))
                .findFirst()
                .orElseThrow(() -> new LibraryException("\"" + item.getTitle() + "\" is not on loan."));
        if (date.isBefore(loan.getLoanDate())) {
            throw new LibraryException("Return date cannot be before the loan date (" + loan.getLoanDate() + ").");
        }
        loan.close(date);
        item.markReturned();
        return finePolicy.calculateFine(loan, date);
    }

    public List<Loan> getActiveLoans() {
        return loans.stream()
                .filter(Loan::isActive)
                .sorted(Comparator.comparing(Loan::getDueDate))
                .toList();
    }

    public List<Loan> getActiveLoansOf(Member member) {
        return loans.stream()
                .filter(loan -> loan.isActive() && loan.getMember().equals(member))
                .toList();
    }

    /** Late loans, the most overdue first. */
    public List<Loan> getOverdueLoans(LocalDate onDate) {
        return loans.stream()
                .filter(loan -> loan.isOverdue(onDate))
                .sorted((a, b) -> Long.compare(b.daysOverdue(onDate), a.daysOverdue(onDate)))
                .toList();
    }

    // ---------- persistence support ----------

    public LibraryData exportData() {
        return new LibraryData(items, members, loans);
    }

    public void importData(LibraryData data) {
        items.clear();
        members.clear();
        loans.clear();
        items.addAll(data.items());
        members.addAll(data.members());
        loans.addAll(data.loans());
    }
}
