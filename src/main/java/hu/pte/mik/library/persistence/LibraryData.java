package hu.pte.mik.library.persistence;

import hu.pte.mik.library.domain.LibraryItem;
import hu.pte.mik.library.domain.Loan;
import hu.pte.mik.library.domain.Member;

import java.util.List;

/** Snapshot of everything the application has to remember between runs. */
public record LibraryData(List<LibraryItem> items, List<Member> members, List<Loan> loans) {

    public LibraryData {
        items = List.copyOf(items);
        members = List.copyOf(members);
        loans = List.copyOf(loans);
    }

    public static LibraryData empty() {
        return new LibraryData(List.of(), List.of(), List.of());
    }
}
