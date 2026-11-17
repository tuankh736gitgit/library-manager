package hu.pte.mik.library.policy;

import hu.pte.mik.library.domain.Loan;

import java.time.LocalDate;

/**
 * Strategy interface: decides how much a late loan costs.
 * The library service only knows this interface, never a concrete policy.
 */
public interface FinePolicy {

    /** Fine in HUF for the loan as of the given day. */
    long calculateFine(Loan loan, LocalDate onDate);

    String getName();
}
