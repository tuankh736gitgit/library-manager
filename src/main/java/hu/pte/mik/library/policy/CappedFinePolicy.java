package hu.pte.mik.library.policy;

import hu.pte.mik.library.domain.Loan;

import java.time.LocalDate;

/** Daily rate for every late day, but never more than a fixed maximum. */
public class CappedFinePolicy implements FinePolicy {

    private final long maximumFine;

    public CappedFinePolicy(long maximumFine) {
        if (maximumFine < 0) {
            throw new IllegalArgumentException("Maximum fine cannot be negative.");
        }
        this.maximumFine = maximumFine;
    }

    @Override
    public long calculateFine(Loan loan, LocalDate onDate) {
        long fine = loan.daysOverdue(onDate) * loan.getItem().getDailyFineRate();
        return Math.min(fine, maximumFine);
    }

    @Override
    public String getName() {
        return "Capped (at most " + maximumFine + " HUF per loan)";
    }
}
