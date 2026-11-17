package hu.pte.mik.library.exception;

/** Data could not be read from or written to storage. */
public class StorageException extends LibraryException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
