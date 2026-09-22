# Viva Questions and Answers

Answers are written the way you should *say* them: short first, then one
supporting sentence, then the place in your own project where it happens.
Pointing at your own code is what turns a memorised answer into a real one.

---

## 1. About the project

**Q: Explain your project in two minutes.**
It is a console-based Hostel Room Allocation and Management System written
in Core Java with JDBC and MySQL. An administrator logs in and can manage
students, hostel blocks and rooms, allocate rooms, transfer students between
rooms, check them out, maintain a waiting list, handle complaints, track
hostel fees, and generate reports. It has eleven modules, eight database
tables, and a four-layer architecture: menu → service → DAO → MySQL.

**Q: Why did you choose this project?**
Room allocation has a genuine correctness problem in it — two database
tables must change together or the data goes wrong. That let me demonstrate
transactions, which most CRUD projects never need.

**Q: Explain your architecture.**
Four layers. `main` holds the menus and only prints and reads input.
`service` holds the business rules and owns the transactions. `dao` holds
SQL and nothing else. `model` holds the data. Each layer only talks to the
one below it, so the SQL could be replaced with file storage without
touching a single menu.

**Q: Why separate DAO and Service?**
Different reasons to change. The DAO changes when the database changes; the
service changes when a hostel rule changes. Keeping them apart means one
change never breaks the other. It also makes the SQL easy to find — it is
all in one package.

**Q: What would you improve if you had more time?**
Password hashing instead of plain text, a login for students as well as
admins (the `StudentUser` class is already there), and `SELECT ... FOR UPDATE`
row locking so two admins cannot allocate the last bed at the same moment.

---

## 2. Core Java and OOP

**Q: What are the four pillars of OOP?**
Encapsulation, inheritance, polymorphism, abstraction. In my project:
`private` fields with getters/setters, `Admin extends User`, `displayRole()`
overridden in two subclasses, and the `abstract class User`.

**Q: Difference between overloading and overriding?**

| Overloading | Overriding |
|---|---|
| Same class | Parent and child class |
| Different parameter list | Identical signature |
| Decided at **compile** time | Decided at **runtime** |
| `Student` has 3 constructors | `Admin.displayRole()` replaces `User.displayRole()` |

**Q: Can you override a `static` method?**
No. Static methods belong to the class, not the object, so there is no
object to dispatch on. If a child declares the same static method it is
*hiding*, not overriding.

**Q: Can you override a `private` method?**
No — a private method is not visible to the subclass at all, so there is
nothing to override.

**Q: Why is `User` abstract?**
Because "just a user" does not exist in this system — everybody is either an
Admin or a StudentUser. Making it abstract means `new User()` is a compile
error, and forces every subclass to implement `displayRole()`.

**Q: Abstract class vs interface?**
An abstract class can have fields, constructors and normal methods, and you
can extend only one. An interface has no instance fields or constructor,
and a class can implement many. Rule of thumb: abstract class for "is a kind
of", interface for "can do this". I have both — `User` is abstract,
`Displayable` is an interface.

**Q: What is `this` and what is `super`?**
`this` refers to the current object; I use it to tell a field apart from a
constructor parameter with the same name. `super` refers to the parent part;
`super(...)` calls the parent constructor and `super.method()` calls the
parent version of an overridden method. Both `this(...)` and `super(...)`
must be the first statement in a constructor.

**Q: What happens if you do not write any constructor?**
Java supplies a no-argument default constructor. As soon as you write any
constructor yourself, that free one disappears.

**Q: What is dynamic method dispatch?**
When a method is called on a reference, Java picks the version based on the
**actual object** at runtime, not the reference type. In `ConceptDemo` I have
a `User[]` holding an `Admin` and a `StudentUser`; the same
`users[i].displayRole()` prints two different things.

**Q: What does `final` do?**
On a variable it means the value cannot change after assignment. On a method
it cannot be overridden. On a class it cannot be extended. My DAO references
in the services are `final` because they should never be reassigned.

**Q: `String` vs `StringBuilder`?**
`String` is immutable — every concatenation creates a new object.
`StringBuilder` is mutable and cheaper inside loops. My SQL strings are
built once at method level, so `String` is fine there.

**Q: `==` vs `equals()`?**
`==` compares references (are these the same object?). `equals()` compares
contents. This is exactly the Integer cache trap: `Integer 128 == 128` is
`false` but `.equals()` is `true`. `ConceptDemo` prints it live.

---

## 3. Collections

**Q: What is the Collection Framework?**
A set of ready-made data structures and the interfaces over them —
`List`, `Set`, `Queue` and `Map`. (`Map` is technically not a `Collection`,
which is a common trick question.)

**Q: ArrayList vs LinkedList?**
ArrayList is backed by a resizable array: O(1) access by index, fast
iteration, but inserting or removing at the front shifts everything.
LinkedList is a chain of nodes: O(1) insert/remove at the ends, but O(n) to
reach an index. I use ArrayList for student lists and LinkedList for the
waiting-list queue.

**Q: Why HashSet for register numbers?**
Because I ask it only one question — "does this register number already
exist?" — and `HashSet.contains()` is O(1) on average, versus O(n) for a
list. A Set also refuses duplicates by definition.

**Q: How does HashSet know something is a duplicate?**
It calls `hashCode()` to find the bucket, then `equals()` to compare inside
it. For `String` both are already implemented correctly, which is why I store
register numbers as Strings.

**Q: Why HashMap for rooms?**
Reports repeatedly turn a `room_id` into a room. A map does that in O(1);
searching a list would be O(n) every single time.

**Q: HashMap vs TreeMap vs LinkedHashMap?**
HashMap: fastest, **no order guarantee**. TreeMap: sorted by key, O(log n).
LinkedHashMap: insertion order preserved. I use TreeMap in the room-wise
report so the rooms come out in order.

**Q: HashMap vs Hashtable?**
Hashtable is the old synchronised version and does not allow null keys or
values. HashMap is unsynchronised and faster. For thread safety today you
would use `ConcurrentHashMap`.

**Q: What is a PriorityQueue and how does it know the order?**
It always serves the "smallest" element first. The order comes from
`Comparable.compareTo()` — my `WaitingEntry.compareTo()` puts priority 1
before priority 5, and breaks ties with the earlier request date.
**Important:** only `poll()` returns elements in order; printing the queue
shows raw heap order.

**Q: `Comparable` vs `Comparator`?**
`Comparable` is implemented by the class itself (`compareTo`) and gives one
natural ordering. `Comparator` is a separate object (`compare`) and lets you
have several different orderings. `WaitingEntry` uses `Comparable`.

**Q: What is the difference between `offer()`/`add()` and `poll()`/`remove()`?**
`add()` and `remove()` throw an exception on failure; `offer()` and `poll()`
return `false`/`null` instead. I use `offer()` and `poll()` because an empty
waiting list is normal, not an error.

---

## 4. Regex

**Q: What are Pattern and Matcher?**
`Pattern` is the compiled rule; `Matcher` is the engine that tests one piece
of text against it. `Pattern.compile()` is expensive, so I compile every
pattern once into a `static final` field in `Validation`.

**Q: `matches()` vs `find()`?**
`matches()` requires the **entire** string to fit the pattern. `find()` looks
for the pattern anywhere inside. Validation must use `matches()` — otherwise
`"abc9876543210xyz"` would pass as a phone number.

**Q: Explain your phone pattern `^[6-9][0-9]{9}$`.**
`^` start, `[6-9]` first digit must be 6, 7, 8 or 9, `[0-9]{9}` nine more
digits, `$` end. Total exactly ten digits.

**Q: Explain the password pattern.**
`^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9@#$_.]{6,20}$`. The two `(?=...)`
groups are **lookaheads** — they check that a letter exists and a digit
exists anywhere ahead, without consuming characters. Then the real pattern
allows 6 to 20 characters from the permitted set.

**Q: Why not use regex for the year 1-4?**
Regex matches text patterns, not numeric ranges. A simple
`year >= 1 && year <= 4` is clearer and correct. Using regex there is a
common mistake.

---

## 5. Wrapper Classes

**Q: What is a wrapper class and why is it needed?**
A class that wraps a primitive so it becomes an object — `int`→`Integer`,
`double`→`Double`, `boolean`→`Boolean`, `char`→`Character`. Needed because
collections can only hold objects: `HashMap<Integer, Room>` is legal,
`HashMap<int, Room>` is not.

**Q: Autoboxing and unboxing?**
Autoboxing is the compiler automatically converting a primitive to its
wrapper (`Integer x = 10;` becomes `Integer.valueOf(10)`). Unboxing is the
reverse (`int y = x;` becomes `x.intValue()`).

**Q: `parseInt()` vs `valueOf()`?**
`Integer.parseInt("42")` returns a primitive `int`.
`Integer.valueOf("42")` returns an `Integer` object.

**Q: What is the danger with unboxing?**
If the wrapper is `null`, unboxing throws `NullPointerException`.
`map.get(key)` returns `null` for a missing key, so
`int count = map.get("WIFI");` can crash. My `ReportService.printCount()`
checks for null first.

**Q: Explain the Integer cache.**
Java caches `Integer` objects from -128 to 127, so `Integer 127 == 127` is
`true` but `Integer 128 == 128` is `false`. This is why objects must always
be compared with `equals()`.

---

## 6. Exception Handling

**Q: Exception vs Error?**
An `Exception` is a problem the program can reasonably recover from.
An `Error` (like `OutOfMemoryError` or `AssertionError`) signals something
you are not expected to handle. Both extend `Throwable`.

**Q: Checked vs unchecked?**
Checked extends `Exception` and the compiler forces you to catch it or
declare `throws` — used for expected situations like `SQLException` and my
six custom exceptions. Unchecked extends `RuntimeException` and is not
enforced — it usually signals a bug, like `NullPointerException`.

**Q: Why did you create custom exceptions?**
So the caller can react differently to different problems.
`RoomFullException` should offer the waiting list;
`StudentNotFoundException` should ask for the id again. A boolean return
value could not tell those two apart.

**Q: `throw` vs `throws`?**
`throw` actually throws an object: `throw new RoomFullException(...)`.
`throws` is part of a method signature, declaring what it might throw.

**Q: Will `finally` always execute?**
Yes — after success, after an exception, and even after a `return`. The only
exceptions are `System.exit()` or the JVM being killed.

**Q: What is try-with-resources?**
Resources declared in `try (...)` are closed automatically at the end of the
block, even if an exception is thrown. Every DAO in my project uses it for
`Connection`, `PreparedStatement` and `ResultSet`.

**Q: Why must a subclass exception be caught before its superclass?**
Because catch blocks are tested in order. If `catch (Exception e)` came
first it would swallow everything, and the compiler rejects the later blocks
as unreachable code.

---

## 7. Assertions

**Q: What is an assertion?**
A statement of something the programmer believes is always true. If it is
false, an `AssertionError` is thrown. `assert condition : message;`

**Q: How do you enable them?**
With the `-ea` flag: `java -ea -cp "bin;lib\*" com.hostel.main.Main`.
They are **disabled by default** — without `-ea` the JVM skips those lines
entirely, so they cost nothing in production.

**Q: Difference between an assertion and validation?**
An assertion checks **our own code's** assumptions and indicates a
programmer bug. Validation checks **what the user typed** and is a normal,
expected situation. A wrong year is validation; `occupied_beds > capacity`
is an assertion, because only broken code could produce it.

**Q: Where did you use assertions?**
In `Room.getAvailableBeds()`, `occupyOneBed()` and `freeOneBed()` to enforce
`0 <= occupiedBeds <= capacity`, and in `AllocationService` to check that
arguments are not null and that the invariants still hold after a
transaction.

**Q: Why should you not put side effects in an assert?**
Because without `-ea` the line never runs, so the program behaves
differently in development and production — the worst kind of bug.

---

## 8. Annotations

**Q: What is an annotation?**
Metadata attached to code. It does not change what the code does; it
describes the code so the compiler or a tool can react to it.

**Q: What does `@Override` actually do?**
It tells the compiler to verify that the method really overrides a
superclass or interface method. If you misspell the name you get a compile
error instead of a silent bug where your method is simply never called.

**Q: Explain your custom annotation.**
`@AdminOperation` marks service methods that only an administrator may call.
It is declared with `@interface`, targeted at methods with
`@Target(ElementType.METHOD)`, and kept at runtime with
`@Retention(RetentionPolicy.RUNTIME)` so a tool could read it via reflection.
It has a `value()` for the description and a `modifiesData()` flag.

**Q: What are the retention policies?**
`SOURCE` — discarded after compiling (e.g. `@Override`).
`CLASS` — stored in the `.class` file but not available at runtime (default).
`RUNTIME` — available at runtime through reflection. Mine is `RUNTIME`.

**Q: Name some built-in annotations.**
`@Override`, `@Deprecated`, `@SuppressWarnings`, `@FunctionalInterface`,
`@SafeVarargs`.

---

## 9. JDBC

**Q: What is JDBC?**
Java Database Connectivity — a standard API that lets Java talk to any
relational database. The database-specific part lives in a driver, so the
same Java code works with MySQL, Oracle or PostgreSQL by swapping the jar.

**Q: What are the steps to connect?**
Load the driver, get a `Connection` from `DriverManager`, create a
`PreparedStatement`, execute it, process the `ResultSet`, close everything.

**Q: Why `Class.forName()`?**
It loads the driver class, whose static initialiser registers it with
`DriverManager`. Since JDBC 4.0 this is automatic, but it is still written
explicitly in most college projects and makes the step visible.

**Q: Statement vs PreparedStatement — and which did you use?**
`PreparedStatement` almost everywhere. It uses `?` placeholders, so the
values travel separately from the SQL text and can never be interpreted as
commands — that is what prevents SQL injection. It is also pre-compiled, so
repeated execution is faster. I use a plain `Statement` in exactly one place,
`DBConnection.testConnection()`, where the SQL is fixed and has no user input.

**Q: Show me an SQL injection example.**
With string concatenation, a user typing `' OR '1'='1` as the password makes
the WHERE clause always true and they log in as anybody. With
`PreparedStatement` that text is treated as a literal password value and
simply does not match.

**Q: `executeQuery()` vs `executeUpdate()` vs `execute()`?**
`executeQuery()` returns a `ResultSet` — for SELECT.
`executeUpdate()` returns the number of rows affected — for
INSERT/UPDATE/DELETE. `execute()` returns a boolean and is used when you do
not know which kind of statement it is.

**Q: What is a ResultSet?**
A cursor over the rows returned by a query. It starts *before* the first
row, and `next()` moves forward, returning `false` when there are no more
rows. Columns are read by name or by **1-based** index.

**Q: How do you get the auto-generated id back?**
Pass `Statement.RETURN_GENERATED_KEYS` to `prepareStatement()`, then read
`ps.getGeneratedKeys()`. I use this in every `insert()` method.

**Q: How do you handle NULL from the database?**
`rs.getDate()` returns `null` for a NULL column, so I never call
`.toLocalDate()` on it directly — each DAO has a small `toLocalDate(Date)`
helper that checks for null first. To *write* a NULL I use
`ps.setNull(index, java.sql.Types.DATE)`.

**Q: What is connection pooling and did you use it?**
Reusing a set of open connections instead of opening a new one each time,
because opening a connection is expensive. I did not use it — it needs an
external library, and for a single-user console project it is unnecessary.
I would add HikariCP in a real application.

---

## 10. Transactions

**Q: What is a transaction?**
A group of database operations treated as one unit — either all of them
succeed, or none of them do.

**Q: Why does your project need one?**
Allocating a room changes two tables: it inserts into `allocations` and
increases `rooms.occupied_beds`. If the first succeeded and the second
failed, the database would say a student lives in a room that claims to be
empty, and that bed would be given away again. The transaction makes both
changes atomic.

**Q: How do you write a transaction in JDBC?**
`con.setAutoCommit(false)` to begin, then all the statements, then
`con.commit()`. On any exception, `con.rollback()`. Restore
`setAutoCommit(true)` and close in `finally`.

**Q: What is auto-commit?**
JDBC's default mode, where every single statement commits itself
immediately. Turning it off is what lets several statements form one unit.

**Q: Why do your DAO methods take a `Connection` parameter?**
Because every statement in a transaction must run on the **same**
connection. If `RoomDAO` opened its own, it would be a separate transaction
and would not roll back with the first one.

**Q: Explain ACID.**
Atomicity — all or nothing. Consistency — constraints are never violated.
Isolation — concurrent transactions do not see each other's half-finished
work. Durability — once committed, the data survives a crash.

**Q: Which of your operations are transactional?**
Three: allocate, transfer and checkout. Transfer is the biggest — four
changes in one unit: close the old allocation, free the old bed, fill the
new bed, insert the new allocation.

**Q: What if the transaction fails halfway?**
`rollback()` undoes everything since `setAutoCommit(false)`. My code prints
`[TRANSACTION ROLLED BACK - no changes were saved]` so you can see it happen.

**Q: Do transactions work on any MySQL table?**
No — only on **InnoDB** tables. The older MyISAM engine ignores
commit/rollback silently. InnoDB is the default in MySQL 5.5 and later, so
my tables are InnoDB.

---

## 11. Database / MySQL

**Q: Explain your database design.**
Eight tables. `hostel_blocks` has many `rooms`; `students` and `rooms` are
linked by `allocations`; `complaints`, `hostel_fees` and `waiting_list` each
belong to a student. All relationships are enforced with foreign keys.

**Q: Primary key vs unique key vs foreign key?**
A primary key uniquely identifies a row and cannot be NULL — one per table.
A unique key also forbids duplicates but allows a NULL and you can have
several. A foreign key points at another table's primary key and stops you
referencing a row that does not exist.

**Q: Where did you use a UNIQUE constraint?**
`students.register_number` is UNIQUE. Also `rooms` has
`UNIQUE (block_id, room_number)` — a composite unique key, because a room
number only has to be unique *inside* its block.

**Q: You check duplicates in Java with a HashSet. Why also in the database?**
Defence in depth. The Java check gives a friendly message; the database
constraint is the guarantee. If someone wrote another program against the
same database, or two admins acted at the same moment, only the constraint
would save the data.

**Q: What is normalisation? Which form are you in?**
Organising tables to remove redundancy. My schema is in 3NF: every column
depends on the whole primary key and nothing else. For example a room stores
`block_id`, not the block name — so renaming a block changes one row, not
hundreds.

**Q: Why do you not delete allocation rows?**
Because they are the history. A transfer marks the old row `TRANSFERRED`
and inserts a new one; a checkout marks it `CHECKED_OUT`. Deleting would
destroy the record of who stayed where — and it also means I do not need a
separate transfer-history table.

**Q: What is a JOIN? Where did you use one?**
A JOIN combines rows from two tables on a matching column. My
`AllocationDAO` joins `allocations` with `students`, `rooms` and
`hostel_blocks` in one query so a report can print names and room numbers
instead of raw id numbers.

**Q: `DELETE` vs `TRUNCATE` vs `DROP`?**
`DELETE` removes rows (can have a WHERE, can be rolled back).
`TRUNCATE` removes all rows quickly and resets AUTO_INCREMENT.
`DROP` removes the table itself.

**Q: What does AUTO_INCREMENT do?**
MySQL generates the next number automatically for that column, so I never
have to invent ids. I read the generated value back with
`getGeneratedKeys()`.

---

## 12. Questions about your own code

**Q: Walk me through what happens when you allocate a room.**
The menu reads a student id and a room id and calls
`AllocationService.allocateRoom()`. The service checks the student exists and
does not already have a room. It opens a connection and calls
`setAutoCommit(false)`. It loads the room on that connection, refuses if it
is under maintenance or full, and checks the block gender matches the
student. Then it calls `room.occupyOneBed()`, writes the new bed count,
inserts the allocation row, and removes the student from the waiting list if
they were on it. Then `commit()`. Any exception triggers `rollback()`, and
`finally` closes the connection.

**Q: Show me where you prevented `occupiedBeds > capacity`.**
Three places, deliberately. `Room.isAvailable()` refuses when there is no
free bed. `RoomService.updateRoom()` refuses to lower the capacity below the
current occupancy. And assertions inside `Room` catch it if our own code
ever gets it wrong anyway.

**Q: What happens if two admins allocate the last bed at the same time?**
With my current code both could read "1 bed free" before either writes, and
one bed would be over-allocated. The correct fix is
`SELECT ... FOR UPDATE`, which locks the room row until the transaction
ends. I know the limitation and where to fix it.

**Q: Why is `Main.java` so thin?**
Because a menu class should only print and read input. If business rules
crept into `Main`, they could not be reused or tested, and I would end up
copying the same check into several menu options.

**Q: What was the hardest part?**
Getting the transactions right — specifically realising that every DAO
method inside a transaction has to receive the *same* `Connection`. My first
version had each DAO opening its own connection, which meant the rollback
silently did nothing.
