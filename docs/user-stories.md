# User stories

The user in every story is the librarian.

| # | Story | Acceptance criteria |
|---|-------|---------------------|
| 1 | As a librarian, I want to add books, magazines and DVDs to the catalogue so that I know what the library owns. | The item gets an id automatically. Empty titles and invalid years are rejected with a message. |
| 2 | As a librarian, I want to register members so that I know who borrows items. | A name and a valid email address are required. |
| 3 | As a librarian, I want to list the catalogue sorted by title, year or type so that I can find items quickly. | The list shows whether each item is available or on loan. |
| 4 | As a librarian, I want to search items by part of the title so that I do not have to read the whole list. | The search ignores upper and lower case. |
| 5 | As a librarian, I want to lend an item to a member so that the loan is recorded with its due date. | The due date depends on the item type. An item already on loan cannot be lent. A member cannot hold more than 3 items. |
| 6 | As a librarian, I want to take back an item so that it becomes available again and I see the late fee. | The fee is 0 when the item is returned on time. |
| 7 | As a librarian, I want to see all overdue loans so that I can remind the members. | The most overdue loan is listed first, with the days late and the fee so far. |
| 8 | As a librarian, I want to choose the late fee policy so that the program follows the current rules of the library. | The policy can be changed while the program is running and applies to the next calculation. |
| 9 | As a librarian, I want my data to be kept between runs so that I do not have to enter it again. | After restarting, the items, members and loans are the same as before. |
| 10 | As a librarian, I want clear messages when something goes wrong so that I know what to fix. | Wrong input, unknown ids and damaged data files produce a readable message and the program keeps running. |
