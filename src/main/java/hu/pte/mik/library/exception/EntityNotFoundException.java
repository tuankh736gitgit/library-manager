package hu.pte.mik.library.exception;

public class EntityNotFoundException extends LibraryException {

    public EntityNotFoundException(String what, int id) {
        super(what + " with id " + id + " does not exist.");
    }
}
