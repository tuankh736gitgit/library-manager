package hu.pte.mik.library;

import hu.pte.mik.library.domain.Book;
import hu.pte.mik.library.domain.Dvd;
import hu.pte.mik.library.domain.LibraryItem;
import hu.pte.mik.library.domain.Loan;
import hu.pte.mik.library.domain.Magazine;
import hu.pte.mik.library.domain.Member;
import hu.pte.mik.library.exception.DuplicateEntityException;
import hu.pte.mik.library.exception.EntityNotFoundException;
import hu.pte.mik.library.exception.ItemNotAvailableException;
import hu.pte.mik.library.exception.LibraryException;
import hu.pte.mik.library.exception.LoanLimitExceededException;
import hu.pte.mik.library.policy.StandardFinePolicy;
import hu.pte.mik.library.service.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryServiceTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    private LibraryService service;

    @BeforeEach
    void setUp() throws LibraryException {
        service = new LibraryService(new StandardFinePolicy());
        service.addItem(new Book(1, "Effective Java", 2018, "Joshua Bloch"));
        service.addItem(new Magazine(2, "Wired", 2026, 11));
        service.addItem(new Dvd(3, "Arrival", 2016, 116));
        service.addItem(new Book(4, "Clean Code", 2008, "Robert C. Martin"));
        service.addMember(new Member(1, "Anna Kovacs", "anna@example.com"));
        service.addMember(new Member(2, "Ben Nagy", "ben@example.com"));
    }

    @Test
    void dueDateDependsOnTheItemType() throws LibraryException {
        assertEquals(DAY.plusDays(21), service.borrowItem(1, 1, DAY).getDueDate());
        assertEquals(DAY.plusDays(7), service.borrowItem(2, 1, DAY).getDueDate());
        assertEquals(DAY.plusDays(3), service.borrowItem(3, 1, DAY).getDueDate());
    }

    @Test
    void borrowedItemCannotBeBorrowedAgain() throws LibraryException {
        service.borrowItem(1, 1, DAY);

        assertFalse(service.findItem(1).isAvailable());
        assertThrows(ItemNotAvailableException.class, () -> service.borrowItem(1, 2, DAY));
    }

    @Test
    void memberCannotExceedTheLoanLimit() throws LibraryException {
        service.borrowItem(1, 1, DAY);
        service.borrowItem(2, 1, DAY);
        service.borrowItem(3, 1, DAY);

        assertThrows(LoanLimitExceededException.class, () -> service.borrowItem(4, 1, DAY));
        assertTrue(service.findItem(4).isAvailable());
    }

    @Test
    void returningOnTimeCostsNothingAndFreesTheItem() throws LibraryException {
        service.borrowItem(1, 1, DAY);

        assertEquals(0, service.returnItem(1, DAY.plusDays(21)));
        assertTrue(service.findItem(1).isAvailable());
        assertTrue(service.getActiveLoans().isEmpty());
    }

    @Test
    void returningLateIsFinedWithTheItemsDailyRate() throws LibraryException {
        service.borrowItem(3, 1, DAY);

        assertEquals(500, service.returnItem(3, DAY.plusDays(3 + 5)));
    }

    @Test
    void returningAnItemThatIsNotOnLoanFails() {
        assertThrows(LibraryException.class, () -> service.returnItem(1, DAY));
    }

    @Test
    void unknownIdsAreReported() {
        assertThrows(EntityNotFoundException.class, () -> service.borrowItem(99, 1, DAY));
        assertThrows(EntityNotFoundException.class, () -> service.borrowItem(1, 99, DAY));
    }

    @Test
    void duplicateIdsAreRejected() {
        assertThrows(DuplicateEntityException.class,
                () -> service.addItem(new Book(1, "Another", 2020, "Someone")));
        assertThrows(DuplicateEntityException.class,
                () -> service.addMember(new Member(1, "Copy", "copy@example.com")));
    }

    @Test
    void itemsAreSortedByTitleByDefaultAndByComparatorOnRequest() {
        List<String> byTitle = service.getItems().stream().map(LibraryItem::getTitle).toList();
        assertEquals(List.of("Arrival", "Clean Code", "Effective Java", "Wired"), byTitle);

        List<Integer> newestFirst = service.getItems(Comparator.comparingInt(LibraryItem::getYear).reversed())
                .stream().map(LibraryItem::getYear).toList();
        assertEquals(List.of(2026, 2018, 2016, 2008), newestFirst);
    }

    @Test
    void searchIgnoresCase() {
        assertEquals(1, service.searchItemsByTitle("JAVA").size());
        assertTrue(service.searchItemsByTitle("python").isEmpty());
    }

    @Test
    void overdueLoansAreListedMostLateFirst() throws LibraryException {
        service.borrowItem(1, 1, DAY);   // due after 21 days
        service.borrowItem(3, 2, DAY);   // due after 3 days
        service.borrowItem(2, 2, DAY);   // due after 7 days

        List<Loan> overdue = service.getOverdueLoans(DAY.plusDays(10));

        assertEquals(List.of("Arrival", "Wired"),
                overdue.stream().map(loan -> loan.getItem().getTitle()).toList());
    }

    @Test
    void nextIdsContinueAfterTheHighestExistingId() {
        assertEquals(5, service.nextItemId());
        assertEquals(3, service.nextMemberId());
    }
}
