package hu.pte.mik.library.policy;

import hu.pte.mik.library.domain.Loan;

import java.time.LocalDate;

/** Every late day costs the item's daily rate. */
public class StandardFinePolicy implements FinePolicy {

    @Override
    public long calculateFine(Loan loan, LocalDate onDate) {
        return loan.daysOverdue(onDate) * loan.getItem().getDailyFineRate();
    }

    @Override
    public String getName() {
        return "Standard (daily rate for every late day)";
    }
}
