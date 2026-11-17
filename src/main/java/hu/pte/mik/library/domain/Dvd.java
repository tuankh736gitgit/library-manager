package hu.pte.mik.library.domain;

public class Dvd extends LibraryItem {

    private final int durationMinutes;

    public Dvd(int id, String title, int year, int durationMinutes) {
        super(id, title, year);
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
        this.durationMinutes = durationMinutes;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    @Override
    public int getLoanPeriodDays() {
        return 3;
    }

    @Override
    public int getDailyFineRate() {
        return 100;
    }

    @Override
    public String getTypeName() {
        return "DVD";
    }

    @Override
    public String getDetails() {
        return durationMinutes + " min";
    }
}
