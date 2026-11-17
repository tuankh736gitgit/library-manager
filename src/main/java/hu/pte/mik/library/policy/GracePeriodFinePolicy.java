package hu.pte.mik.library.policy;

import hu.pte.mik.library.domain.Loan;

import java.time.LocalDate;

/** The first few late days are free; only the days after that are charged. */
public class GracePeriodFinePolicy implements FinePolicy {

    private final int graceDays;

    public GracePeriodFinePolicy(int graceDays) {
        if (graceDays < 0) {
            throw new IllegalArgumentException("Grace days cannot be negative.");
        }
        this.graceDays = graceDays;
    }

    @Override
    public long calculateFine(Loan loan, LocalDate onDate) {
        long chargedDays = Math.max(0, loan.daysOverdue(onDate) - graceDays);
        return chargedDays * loan.getItem().getDailyFineRate();
    }

    @Override
    public String getName() {
        return "Grace period (first " + graceDays + " late days are free)";
    }
}
