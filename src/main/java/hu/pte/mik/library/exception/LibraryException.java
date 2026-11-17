package hu.pte.mik.library.exception;

/** Checked base class for every broken library rule. */
public class LibraryException extends Exception {

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
