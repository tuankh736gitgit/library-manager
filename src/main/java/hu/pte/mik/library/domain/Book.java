package hu.pte.mik.library.domain;

public class Book extends LibraryItem {

    private final String author;

    public Book(int id, String title, int year, String author) {
        super(id, title, year);
        this.author = Texts.requireText(author, "Author");
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public int getLoanPeriodDays() {
        return 21;
    }

    @Override
    public int getDailyFineRate() {
        return 50;
    }

    @Override
    public String getTypeName() {
        return "BOOK";
    }

    @Override
    public String getDetails() {
        return "by " + author;
    }
}
