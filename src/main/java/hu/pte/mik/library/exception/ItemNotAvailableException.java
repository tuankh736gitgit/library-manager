package hu.pte.mik.library.exception;

import hu.pte.mik.library.domain.LibraryItem;

/** Domain rule: an item that is already on loan cannot be borrowed again. */
public class ItemNotAvailableException extends LibraryException {

    public ItemNotAvailableException(LibraryItem item) {
        super("\"" + item.getTitle() + "\" is already on loan.");
    }
}
