package hu.pte.mik.library;

import hu.pte.mik.library.domain.Book;
import hu.pte.mik.library.domain.Loan;
import hu.pte.mik.library.domain.Member;
import hu.pte.mik.library.exception.LibraryException;
import hu.pte.mik.library.policy.CappedFinePolicy;
import hu.pte.mik.library.policy.FinePolicy;
import hu.pte.mik.library.policy.GracePeriodFinePolicy;
import hu.pte.mik.library.policy.StandardFinePolicy;
import hu.pte.mik.library.service.LibraryService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinePolicyTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    /** A book (50 HUF per day, 21 day loan) that is the given number of days late. */
    private static Loan lateBook() {
        return new Loan(new Book(1, "Dune", 1965, "Frank Herbert"),
                new Member(1, "Anna Kovacs", "anna@example.com"), DAY);
    }

    private static LocalDate daysLate(int days) {
        return DAY.plusDays(21 + days);
    }

    @Test
    void standardPolicyChargesEveryLateDay() {
        FinePolicy policy = new StandardFinePolicy();

        assertEquals(0, policy.calculateFine(lateBook(), daysLate(0)));
        assertEquals(500, policy.calculateFine(lateBook(), daysLate(10)));
    }

    @Test
    void gracePeriodPolicySkipsTheFirstDays() {
        FinePolicy policy = new GracePeriodFinePolicy(3);

        assertEquals(0, policy.calculateFine(lateBook(), daysLate(3)));
        assertEquals(350, policy.calculateFine(lateBook(), daysLate(10)));
    }

    @Test
    void cappedPolicyNeverExceedsTheMaximum() {
        FinePolicy policy = new CappedFinePolicy(1000);

        assertEquals(500, policy.calculateFine(lateBook(), daysLate(10)));
        assertEquals(1000, policy.calculateFine(lateBook(), daysLate(100)));
    }

    @Test
    void invalidPolicySettingsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new GracePeriodFinePolicy(-1));
        assertThrows(IllegalArgumentException.class, () -> new CappedFinePolicy(-1));
    }

    @Test
    void serviceUsesWhicheverPolicyIsCurrentlySet() throws LibraryException {
        LibraryService service = new LibraryService(new StandardFinePolicy());
        service.addItem(new Book(1, "Dune", 1965, "Frank Herbert"));
        service.addMember(new Member(1, "Anna Kovacs", "anna@example.com"));
        Loan loan = service.borrowItem(1, 1, DAY);

        assertEquals(5000, service.calculateFine(loan, daysLate(100)));

        service.setFinePolicy(new CappedFinePolicy(1000));
        assertEquals(1000, service.calculateFine(loan, daysLate(100)));

        service.setFinePolicy(new GracePeriodFinePolicy(3));
        assertEquals(4850, service.calculateFine(loan, daysLate(100)));
    }
}
