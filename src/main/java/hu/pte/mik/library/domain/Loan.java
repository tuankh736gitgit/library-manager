package hu.pte.mik.library.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Connects one member with one item for a period of time. */
public class Loan {

    private final LibraryItem item;
    private final Member member;
    private final LocalDate loanDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;

    /** Creates a new loan; the due date comes from the item's own loan period. */
    public Loan(LibraryItem item, Member member, LocalDate loanDate) {
        this(item, member, loanDate,
                Objects.requireNonNull(loanDate, "loanDate").plusDays(item.getLoanPeriodDays()), null);
    }

    /** Recreates a loan that was read back from storage. */
    public Loan(LibraryItem item, Member member, LocalDate loanDate, LocalDate dueDate, LocalDate returnDate) {
        this.item = Objects.requireNonNull(item, "item");
        this.member = Objects.requireNonNull(member, "member");
        this.loanDate = Objects.requireNonNull(loanDate, "loanDate");
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate");
        if (dueDate.isBefore(loanDate)) {
            throw new IllegalArgumentException("Due date cannot be before the loan date.");
        }
        if (returnDate != null && returnDate.isBefore(loanDate)) {
            throw new IllegalArgumentException("Return date cannot be before the loan date.");
        }
        this.returnDate = returnDate;
    }

    public LibraryItem getItem() {
        return item;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isActive() {
        return returnDate == null;
    }

    /** Whole days past the due date on the given day; 0 when not late. */
    public long daysOverdue(LocalDate onDate) {
        LocalDate end = isActive() ? onDate : returnDate;
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, end));
    }

    public boolean isOverdue(LocalDate onDate) {
        return isActive() && daysOverdue(onDate) > 0;
    }

    public void close(LocalDate date) {
        if (!isActive()) {
            throw new IllegalStateException("Loan is already closed.");
        }
        if (date.isBefore(loanDate)) {
            throw new IllegalArgumentException("Return date cannot be before the loan date.");
        }
        this.returnDate = date;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s | borrowed %s, due %s%s",
                item.getTitle(), member.getName(), loanDate, dueDate,
                isActive() ? "" : ", returned " + returnDate);
    }
}
