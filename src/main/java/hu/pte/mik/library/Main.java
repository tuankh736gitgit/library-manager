package hu.pte.mik.library;

import hu.pte.mik.library.persistence.LibraryStorage;
import hu.pte.mik.library.persistence.TextFileStorage;
import hu.pte.mik.library.policy.StandardFinePolicy;
import hu.pte.mik.library.service.LibraryService;
import hu.pte.mik.library.ui.ConsoleMenu;

import java.nio.file.Path;
import java.util.Scanner;

/** Entry point: wires the storage, the service and the console menu together. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Path dataDirectory = Path.of(args.length > 0 ? args[0] : "data");
        LibraryStorage storage = new TextFileStorage(dataDirectory);
        LibraryService service = new LibraryService(new StandardFinePolicy());

        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleMenu(service, storage, scanner, System.out).run();
        }
    }
}
