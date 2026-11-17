package hu.pte.mik.library.domain;

public class Magazine extends LibraryItem {

    private final int issueNumber;

    public Magazine(int id, String title, int year, int issueNumber) {
        super(id, title, year);
        if (issueNumber <= 0) {
            throw new IllegalArgumentException("Issue number must be positive.");
        }
        this.issueNumber = issueNumber;
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    @Override
    public int getLoanPeriodDays() {
        return 7;
    }

    @Override
    public int getDailyFineRate() {
        return 20;
    }

    @Override
    public String getTypeName() {
        return "MAGAZINE";
    }

    @Override
    public String getDetails() {
        return "issue " + issueNumber;
    }
}
