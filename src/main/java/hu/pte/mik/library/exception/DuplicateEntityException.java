package hu.pte.mik.library.exception;

public class DuplicateEntityException extends LibraryException {

    public DuplicateEntityException(String what, int id) {
        super(what + " with id " + id + " already exists.");
    }
}
