package hu.pte.mik.library;

import hu.pte.mik.library.domain.Book;
import hu.pte.mik.library.domain.Dvd;
import hu.pte.mik.library.domain.LibraryItem;
import hu.pte.mik.library.domain.Magazine;
import hu.pte.mik.library.domain.Member;
import hu.pte.mik.library.exception.LibraryException;
import hu.pte.mik.library.exception.StorageException;
import hu.pte.mik.library.persistence.LibraryData;
import hu.pte.mik.library.persistence.TextFileStorage;
import hu.pte.mik.library.policy.StandardFinePolicy;
import hu.pte.mik.library.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextFileStorageTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    @TempDir
    Path directory;

    @Test
    void loadingBeforeAnythingWasSavedGivesAnEmptyLibrary() throws StorageException {
        LibraryData data = new TextFileStorage(directory.resolve("missing")).load();

        assertTrue(data.items().isEmpty());
        assertTrue(data.members().isEmpty());
        assertTrue(data.loans().isEmpty());
    }

    @Test
    void savedLibraryIsRestoredExactly() throws LibraryException {
        LibraryService original = new LibraryService(new StandardFinePolicy());
        original.addItem(new Book(1, "Effective Java", 2018, "Joshua Bloch"));
        original.addItem(new Magazine(2, "Wired", 2026, 11));
        original.addItem(new Dvd(3, "Arrival", 2016, 116));
        original.addMember(new Member(1, "Anna Kovacs", "anna@example.com"));
        original.borrowItem(1, 1, DAY);
        original.borrowItem(3, 1, DAY);
        original.returnItem(3, DAY.plusDays(2));

        TextFileStorage storage = new TextFileStorage(directory);
        storage.save(original.exportData());

        LibraryService restored = new LibraryService(new StandardFinePolicy());
        restored.importData(storage.load());

        assertEquals(3, restored.getItems().size());
        assertEquals(1, restored.getMembers().size());
        assertEquals(2, restored.exportData().loans().size());

        LibraryItem book = restored.findItem(1);
        assertInstanceOf(Book.class, book);
        assertEquals("Joshua Bloch", ((Book) book).getAuthor());
        assertFalse(book.isAvailable());
        assertEquals(11, ((Magazine) restored.findItem(2)).getIssueNumber());
        assertTrue(restored.findItem(3).isAvailable());

        assertEquals(1, restored.getActiveLoans().size());
        assertEquals(DAY.plusDays(21), restored.getActiveLoans().get(0).getDueDate());
        assertEquals("Anna Kovacs", restored.findMember(1).getName());
    }

    @Test
    void corruptFileIsReportedWithItsLocation() throws IOException {
        Files.writeString(directory.resolve("items.txt"), "BOOK|1|Dune|1965|Frank Herbert\nBOOK|x|Broken\n");

        StorageException e = assertThrows(StorageException.class, () -> new TextFileStorage(directory).load());

        assertTrue(e.getMessage().contains("items.txt"));
        assertTrue(e.getMessage().contains("line 2"));
    }

    @Test
    void loanPointingToAMissingItemIsRejected() throws IOException {
        Files.writeString(directory.resolve("loans.txt"), "7|1|2026-11-02|2026-11-23|-\n");

        assertThrows(StorageException.class, () -> new TextFileStorage(directory).load());
    }
}
