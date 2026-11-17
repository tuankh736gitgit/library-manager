package hu.pte.mik.library.exception;

import hu.pte.mik.library.domain.Member;

/** Domain rule: a member may only hold a limited number of items at once. */
public class LoanLimitExceededException extends LibraryException {

    public LoanLimitExceededException(Member member, int limit) {
        super(member.getName() + " already has " + limit + " items on loan (the maximum).");
    }
}
