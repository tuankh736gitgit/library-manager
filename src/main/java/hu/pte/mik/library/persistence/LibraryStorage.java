package hu.pte.mik.library.persistence;

import hu.pte.mik.library.exception.StorageException;

/** Contract for saving and restoring the library, independent of the file format. */
public interface LibraryStorage {

    void save(LibraryData data) throws StorageException;

    /** Returns empty data when nothing has been saved yet. */
    LibraryData load() throws StorageException;
}
