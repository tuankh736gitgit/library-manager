package hu.pte.mik.library;

import hu.pte.mik.library.domain.Book;
import hu.pte.mik.library.domain.Dvd;
import hu.pte.mik.library.domain.Loan;
import hu.pte.mik.library.domain.Magazine;
import hu.pte.mik.library.domain.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainValidationTest {

    @Test
    void itemsRejectInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new Book(0, "Title", 2020, "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book(1, "  ", 2020, "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book(1, "Title", 3020, "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Book(1, "Title", 2020, ""));
        assertThrows(IllegalArgumentException.class, () -> new Book(1, "Bad|Title", 2020, "Author"));
        assertThrows(IllegalArgumentException.class, () -> new Magazine(1, "Title", 2020, 0));
        assertThrows(IllegalArgumentException.class, () -> new Dvd(1, "Title", 2020, -5));
    }

    @Test
    void membersNeedANameAndAValidEmail() {
        assertThrows(IllegalArgumentException.class, () -> new Member(1, "", "a@b.hu"));
        assertThrows(IllegalArgumentException.class, () -> new Member(1, "Anna", "not-an-email"));
        assertEquals("Anna", new Member(1, "  Anna ", "a@b.hu").getName());
    }

    @Test
    void loanKnowsWhenItIsOverdue() {
        LocalDate day = LocalDate.of(2026, 11, 2);
        Loan loan = new Loan(new Dvd(1, "Arrival", 2016, 116), new Member(1, "Anna", "a@b.hu"), day);

        assertFalse(loan.isOverdue(day.plusDays(3)));
        assertTrue(loan.isOverdue(day.plusDays(4)));
        assertEquals(4, loan.daysOverdue(day.plusDays(7)));

        loan.close(day.plusDays(5));
        assertFalse(loan.isActive());
        assertEquals(2, loan.daysOverdue(day.plusDays(30)));
        assertThrows(IllegalStateException.class, () -> loan.close(day.plusDays(6)));
    }
}
