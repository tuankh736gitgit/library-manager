package hu.pte.mik.library.domain;

import java.util.Objects;

/**
 * Common parent of everything the library can lend.
 * Subclasses decide how long they may be borrowed and how expensive a late day is.
 */
public abstract class LibraryItem implements Comparable<LibraryItem> {

    private final int id;
    private final String title;
    private final int year;
    private boolean available = true;

    protected LibraryItem(int id, String title, int year) {
        if (id <= 0) {
            throw new IllegalArgumentException("Item id must be positive.");
        }
        if (year < 1000 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 1000 and 2100.");
        }
        this.id = id;
        this.title = Texts.requireText(title, "Title");
        this.year = year;
    }

    /** Number of days this kind of item may be kept. */
    public abstract int getLoanPeriodDays();

    /** Fine in HUF for each day the item is late. */
    public abstract int getDailyFineRate();

    /** Short type name used on screen and in the data file. */
    public abstract String getTypeName();

    /** Type specific information, for example the author of a book. */
    public abstract String getDetails();

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public boolean isAvailable() {
        return available;
    }

    public void markBorrowed() {
        available = false;
    }

    public void markReturned() {
        available = true;
    }

    /** Natural order: alphabetical by title, then by id. */
    @Override
    public int compareTo(LibraryItem other) {
        int byTitle = title.compareToIgnoreCase(other.title);
        return byTitle != 0 ? byTitle : Integer.compare(id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LibraryItem other && id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("#%d [%s] %s (%d) - %s - %s",
                id, getTypeName(), title, year, getDetails(),
                available ? "available" : "on loan");
    }
}
