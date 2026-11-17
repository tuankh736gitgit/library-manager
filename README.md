# Library Manager

A console application for a small library, written in Java for the Programming 2 project assignment
(University of Pécs, Faculty of Engineering and Information Technology).

A librarian can register books, magazines and DVDs, register members, lend and take back items,
see what is overdue, and choose how late fees are calculated. Everything is saved to text files,
so the library is still there the next time the program starts.

## Prerequisites

- JDK 17 or newer
- Apache Maven 3.8 or newer

## Build, test and run

```bash
mvn clean package                       # compiles, runs all tests, builds the jar
java -jar target/library-manager-1.0.0.jar
```

Run only the tests:

```bash
mvn test
```

Data is written to a `data/` folder in the directory where the program is started.
A different folder can be given as the first argument:

```bash
java -jar target/library-manager-1.0.0.jar my-library
```

## Sample usage

```text
=== Library Manager ===
Loaded 0 items and 0 members.

 1. Add item              6. Borrow item
 2. Add member            7. Return item
 3. List items            8. Active loans
 4. Search items          9. Overdue loans
 5. List members         10. Change fine policy
 0. Save and exit
Choose: 1
Type (1 = book, 2 = magazine, 3 = DVD): 3
Title: Arrival
Year: 2016
Duration in minutes: 116
Added: #1 [DVD] Arrival (2016) - 116 min - available

Choose: 2
Name: Anna Kovacs
Email: anna@example.com
Added: #1 Anna Kovacs <anna@example.com>

Choose: 6
Item id: 1
Member id: 1
Borrowed. Due back on 2026-11-20.

Choose: 6
Item id: 1
Member id: 1
Error: "Arrival" is already on loan.

Choose: 7
Item id: 1
Return date (YYYY-MM-DD, Enter = today): 2026-11-25
Returned late. Fine: 500 HUF.
```

Tip for evaluating fines: option 7 accepts any return date, so a late return can be tried
without waiting. Option 10 switches the fine policy while the program is running.

## Library rules

| Item type | Loan period | Fine per late day |
|-----------|-------------|-------------------|
| Book      | 21 days     | 50 HUF            |
| Magazine  | 7 days      | 20 HUF            |
| DVD       | 3 days      | 100 HUF           |

- An item that is on loan cannot be borrowed again until it is returned.
- A member can hold at most 3 items at the same time.

## Implemented requirements

| Requirement | Where to find it |
|-------------|------------------|
| Encapsulation | All fields are `private`; constructors validate their arguments (`LibraryItem`, `Member`, `Loan`); lists are only exposed as copies. |
| Abstraction | Abstract class `LibraryItem`; interfaces `FinePolicy` and `LibraryStorage`. |
| Inheritance and polymorphism | `Book`, `Magazine` and `Dvd` extend `LibraryItem` and override `getLoanPeriodDays()`, `getDailyFineRate()`, `getTypeName()` and `getDetails()`. `Loan` and the fine policies call these through the parent type, so the due date and the fine depend on the real type at runtime. |
| Relationships | `LibraryService` aggregates many `LibraryItem`s and `Member`s and owns many `Loan`s; each `Loan` refers to exactly one item and one member. |
| Collections and generics | `ArrayList<LibraryItem>`, `ArrayList<Member>`, `ArrayList<Loan>`. Sorting with `Comparable<LibraryItem>` (by title), `Comparator` (by year, by type, members by name) and a lambda (overdue loans, most late first). |
| Exceptions | Checked `LibraryException` with domain subclasses `ItemNotAvailableException`, `LoanLimitExceededException`, `EntityNotFoundException`, `DuplicateEntityException` and `StorageException`. Console input is validated and re-asked. |
| Persistence | `TextFileStorage` reads and writes `items.txt`, `members.txt` and `loans.txt` with try-with-resources. Corrupt files are reported with file name and line number. |
| Testing | JUnit 5 tests for the service rules, the fine policies, domain validation and storage. |
| Build | Standard Maven project; `mvn clean package` works from a clean checkout. |

## Design pattern: Strategy

**Problem.** Libraries change their late fee rules (a grace period, a maximum fee, a campaign).
If the calculation were written inside `LibraryService`, every new rule would mean editing
that class and adding more `if` branches.

**Solution.** The calculation is a separate object behind the `FinePolicy` interface.

| Role | Class |
|------|-------|
| Strategy | `policy.FinePolicy` |
| Concrete strategies | `policy.StandardFinePolicy`, `policy.GracePeriodFinePolicy`, `policy.CappedFinePolicy` |
| Context | `service.LibraryService` (holds the current policy, `setFinePolicy` replaces it) |
| Client | `ui.ConsoleMenu` (menu option 10 chooses the policy) |

**Benefit.** The policy can be swapped while the program runs, a new rule is one new class with
no change to `LibraryService`, and each policy is tested on its own (`FinePolicyTest`).

## Project structure

```text
src/main/java/hu/pte/mik/library/
  Main.java          starts the program
  domain/            LibraryItem, Book, Magazine, Dvd, Member, Loan
  policy/            FinePolicy and its three implementations (Strategy)
  service/           LibraryService: the library rules
  persistence/       LibraryStorage, TextFileStorage, LibraryData
  exception/         LibraryException and its subclasses
  ui/                ConsoleMenu
src/test/java/hu/pte/mik/library/   JUnit tests
docs/                UML class diagram, design notes, user stories
```

## Documentation

- [Design notes](docs/design.md): problem statement, design decisions, known limitations
- [UML class diagram](docs/class-diagram.png) ([editable Mermaid source](docs/class-diagram.mmd))
- [User stories](docs/user-stories.md)

## Contributors

- Nguyen Duong Tuan Khoa (Neptun: PPWPOX)

## License

[MIT](LICENSE)
