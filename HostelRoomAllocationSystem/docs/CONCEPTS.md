# Where every syllabus concept lives in this project

For each topic: **what it means → where we used it → why → alternatives →
common mistakes.**

---

## A. OOP

### Class and Object

* **Where:** `model/Student.java` is the class. `new Student(...)` in
  `StudentMenu.addStudent()` creates the object.
* **Meaning:** a class is a blueprint (it describes state and behaviour);
  an object is one real instance of that blueprint, stored in the heap.
* **Common mistake:** saying "class and object are the same". A class is
  compiled code; an object exists only while the program runs.

### Encapsulation

* **Where:** every field in every model class is `private`, with public
  getters and setters.
* **Why:** outside code can never put a `Room` into an impossible state by
  writing `room.occupiedBeds = 99`. It has to go through a method we control.
* **Alternative:** public fields — rejected, because then no rule can ever
  be enforced.
* **Common mistake:** writing getters and setters that do nothing and calling
  that encapsulation. Encapsulation is about *control*, and you can see real
  control in `Room.occupyOneBed()` and `Student.validate()`.

### Constructor and constructor overloading

* **Where:** `Student` has three constructors:
  * `Student()` — defaults
  * `Student(8 args)` — used when ADDING a new student (no id yet, MySQL
    generates it)
  * `Student(9 args)` — used when READING from the database (id known)
* **Why three:** the three situations genuinely have different information
  available.
* **`this(...)`:** the 9-argument constructor calls the 8-argument one so the
  shared assignments are written once. `this(...)` must be the first statement.
* **Common mistakes:**
  * giving a constructor a return type — that silently makes it an ordinary
    method, not a constructor
  * thinking overloading depends on the return type. It does not. Only the
    **parameter list** counts
  * forgetting that writing any constructor removes the free default one

### Inheritance, `super`, method overriding

* **Where:** `User` (abstract) → `Admin` and `StudentUser`.
* **`extends`:** `Admin extends User` — Admin gets `userId`, `username`,
  `showBasicInfo()` for free and adds `password`.
* **`super(...)`:** `Admin`'s constructor calls `super(adminId, username)`
  to build the parent part first. It must be the first statement, because
  the parent part of the object has to exist before the child part.
* **`super.method()`:** `Admin.showBasicInfo()` calls
  `super.showBasicInfo()` and then adds a line of its own.
* **Overriding rules:** same name, same parameters, same (or narrower)
  return type, and the access modifier cannot be made more restrictive.
* **Common mistake:** confusing overriding with overloading.
  *Overriding* = a child replacing a parent method (runtime, inheritance).
  *Overloading* = same name, different parameters, in one class (compile time).

### Polymorphism

* **Where:** `AdminService.demonstratePolymorphism()` and
  `ConceptDemo.demoOop()`:

```java
User[] users = { new Admin(...), new StudentUser(...) };
for (int i = 0; i < users.length; i++) {
    users[i].displayRole();    // different output each time
}
```

* **Why it matters:** the reference type is `User`, but Java looks at the
  **real object at runtime** and runs that class's version. This is called
  dynamic method dispatch.
* **Also used by:** the `Displayable` interface — reports call `display()`
  without caring whether the object is a Student, a Room or a Complaint.

### Abstraction

* **Where:** `abstract class User` with `public abstract void displayRole();`
* **Why abstract:** every user has an id and a username, but "just a User"
  is not a real thing in this system. Making the class abstract means
  `new User()` is a **compile error**, and every subclass is forced to
  answer `displayRole()` for itself.

### Interface

* **Where:** `model/Displayable.java`, implemented by `Student`, `Room`,
  `HostelBlock`, `Allocation`, `Complaint`, `HostelFee` and `WaitingEntry`.
* **Interface vs abstract class:**

| | Abstract class | Interface |
|---|---|---|
| Instance fields | yes | no |
| Constructor | yes | no |
| How many can you have | extend **one** | implement **many** |
| Meaning | "is a kind of" | "can do this" |

* `WaitingEntry` also implements the built-in `Comparable` interface, which
  is what lets a `PriorityQueue` sort it.

### Access modifiers

| Modifier | Visible to | Used in this project for |
|---|---|---|
| `private` | only this class | every model field, every DAO helper |
| *(default)* | same package only | nothing (deliberately) |
| `protected` | same package + subclasses | `User.userId`, `User.username` |
| `public` | everywhere | getters, setters, service methods |

`protected` is exactly right for `User`'s fields: `Admin` and `StudentUser`
need them, unrelated classes must not have them.

---

## B. Collection Framework

| Collection | Where | Why that one |
|---|---|---|
| `ArrayList<Student>` | `StudentDAO.findAll()` | we add at the end and then read in order; index access is O(1) and iteration is the cheapest of any list |
| `HashSet<String>` | `StudentDAO.findAllRegisterNumbers()` | we ask exactly one question — "is this register number already taken?" — and `contains()` is O(1). An ArrayList would scan every element |
| `HashMap<Integer, Room>` | `RoomDAO.findAllAsMap()` | reports must turn a `room_id` into a room over and over; a map lookup is O(1) |
| `HashMap<String, Integer>` | `ComplaintDAO.countByStatus()` | SQL `GROUP BY` already returns label → count pairs; a map stores that shape directly |
| `LinkedList` (as `Queue`) | `WaitingListService.getFifoQueue()` | strict first-come-first-served; removing the head is O(1), while `ArrayList.remove(0)` shifts every remaining element |
| `PriorityQueue` | `WaitingListService.getPriorityQueue()` | some students must be served before others; order comes from `WaitingEntry.compareTo()` |
| `TreeMap` | `ReportService.printRoomWiseStudents()` | same as HashMap but keeps keys **sorted**, so rooms print in order |

### The important trade-offs

* **ArrayList vs LinkedList** — ArrayList wins for reading by index and for
  iteration (contiguous memory). LinkedList wins for adding/removing at the
  front. We use both, each where it fits.
* **HashSet vs ArrayList for duplicate checks** — HashSet is O(1),
  ArrayList is O(n). With 1000 students that is 1 comparison instead of 1000.
* **HashMap vs TreeMap** — HashMap is faster but gives **no order guarantee**.
  TreeMap is slightly slower but sorted. We use TreeMap only where the output
  order matters.
* **PriorityQueue warning:** only `poll()` returns elements in order.
  Printing the queue directly shows internal heap order, which looks
  scrambled. This is a favourite exam trick.

---

## C. Regular Expressions

All in `util/Validation.java`, using `Pattern` and `Matcher`.

```java
private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[6-9][0-9]{9}$");

public static boolean isValidPhone(String phone) {
    Matcher matcher = PHONE_PATTERN.matcher(phone.trim());
    return matcher.matches();
}
```

* **`Pattern`** = the compiled rule. **`Matcher`** = the engine that tests
  one piece of text against it.
* Patterns are `static final` because `Pattern.compile()` is the expensive
  part. Compile once, match a million times.

| Field | Pattern | Reads as |
|---|---|---|
| Name | `^[A-Za-z][A-Za-z. ]{1,49}$` | a letter, then 1-49 letters/dots/spaces |
| Register number | `^[0-9]{2}[A-Z]{2,4}[0-9]{3}$` | `26CSE001` |
| Phone | `^[6-9][0-9]{9}$` | 10 digits starting 6/7/8/9 |
| Email | `^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+[.][A-Za-z]{2,}$` | text @ text . text |
| Password | `^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9@#$_.]{6,20}$` | 6-20 chars, at least one letter and one digit |
| Room number | `^[A-Z]{1,2}-[0-9]{3}$` | `A-101` |

**Symbols to know:**

| Symbol | Meaning |
|---|---|
| `^` | start of the text |
| `$` | end of the text |
| `[abc]` | any one of a, b, c |
| `[^abc]` | any character *except* a, b, c |
| `{2,4}` | between 2 and 4 times |
| `+` | one or more |
| `*` | zero or more |
| `?` | zero or one |
| `(?=...)` | lookahead: must be followed by this, but consume nothing |
| `[.]` | a literal dot (a dot inside a class loses its "any character" meaning) |

**Common mistakes:**

* Using `matches()` vs `find()`. `matches()` requires the **whole** string to
  fit — that is what validation needs. `find()` only needs a match somewhere,
  so `"abc12345xyz"` would pass a phone check.
* Forgetting `^` and `$`, which has the same effect.
* Writing `\.` in a Java string. In Java source you must write `"\\."`
  because the backslash itself has to be escaped. We used `[.]` instead,
  which sidesteps the problem entirely.
* Using regex to check number **ranges**. Year 1-4 is an `if`, not a regex.

---

## D. Wrapper Classes

A wrapper turns a primitive into an object, so it can go into a collection
and can carry methods.

| Primitive | Wrapper |
|---|---|
| `int` | `Integer` |
| `double` | `Double` |
| `boolean` | `Boolean` |
| `char` | `Character` |

### Where we used them

* `util/InputUtil.java` — `Integer.parseInt()`, `Double.valueOf()`,
  `Boolean.TRUE/FALSE`, `Character.toUpperCase()`
* `HashMap<Integer, Room>` — a collection cannot hold `int`, only `Integer`
* `service/ReportService.printCount()` — reading an `Integer` out of a map

### parseInt vs valueOf

```java
int   a = Integer.parseInt("42");   // returns a PRIMITIVE int
Integer b = Integer.valueOf("42");  // returns an Integer OBJECT
```

### Autoboxing and unboxing

```java
Integer boxed = 10;    // AUTOBOXING: compiler writes Integer.valueOf(10)
int plain = boxed;     // UNBOXING:   compiler writes boxed.intValue()
```

### Two traps that appear in exams

**1. `NullPointerException` from unboxing `null`**

```java
Integer count = map.get("WIFI");   // returns null if the key is missing
int value = count;                 // NullPointerException!
```

`ReportService.printCount()` checks for null first. This is real defensive
code, not a textbook example.

**2. The Integer cache**

```java
Integer x = 127, y = 127;   x == y  ->  true   (cached, same object)
Integer p = 128, q = 128;   p == q  ->  false  (two objects)
                            p.equals(q) -> true
```

Java caches `Integer` objects from -128 to 127. **Always compare objects
with `equals()`, never `==`.** Run `ConceptDemo` to see it happen.

---

## E. Assertions

An assertion states something the programmer believes is **always true**.
If it is false, the program has a bug.

```java
assert occupiedBeds <= capacity
     : "Occupied beds (" + occupiedBeds + ") exceeded capacity";
```

* **Where:** `model/Room.java` (bed-count consistency) and
  `service/AllocationService.java` (arguments are not null, invariants hold
  after the transaction).
* **They are OFF by default.** Enable with `-ea`:

```bash
java -ea -cp "bin;lib\*" com.hostel.main.Main
```

* **Assertions vs input validation — the key distinction:**

| | Assertion | Validation |
|---|---|---|
| Checks | our own code's assumptions | what the user typed |
| Who is at fault | the programmer | the user |
| Runs in the final build | **no** | yes, always |
| On failure | `AssertionError` (crash) | a polite message, ask again |

A year of `8` is **not** an assertion failure — users type wrong numbers all
the time. That belongs in `Validation.isValidYear()`. But
`occupiedBeds > capacity` can only happen if our own code is broken, so that
is an assertion.

* **Common mistake:** putting side effects inside an assert, e.g.
  `assert list.remove(x);`. Without `-ea` the line never runs and the
  program behaves differently.
  (The one deliberate exception is the `assert enabled = true;` trick in
  `Main.printBanner()`, which exists only to *detect* whether `-ea` was given.)

---

## F. Annotations

An **annotation is metadata** — information attached to code that does not
change what the code does, but can be read by the compiler or by a tool.

### Built-in: `@Override`

Used on every overridden method in this project. It asks the compiler to
verify that the method really does override something. Mis-spell
`displayRole` as `displayRoll` and you get a **compile error** instead of a
silent bug where your method is simply never called.

### Custom: `@AdminOperation`

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminOperation {
    String value() default "Admin only operation";
    boolean modifiesData() default true;
}
```

Used on the service methods only an administrator may call:

```java
@AdminOperation("Delete a student record")
public boolean deleteStudent(int studentId) { ... }
```

* `@interface` (not `interface`) declares an annotation type.
* `@Target(METHOD)` — it can only be placed on methods.
* `@Retention(RUNTIME)` — the information survives into the running program,
  so a tool could read it with reflection. `SOURCE` would discard it after
  compiling; `CLASS` keeps it in the .class file but not at runtime.
* `value()` is special: when an annotation has only `value`, you may write
  `@AdminOperation("text")` instead of `@AdminOperation(value = "text")`.

**Other built-in annotations to name in a viva:** `@Deprecated`,
`@SuppressWarnings`, `@FunctionalInterface`, `@SafeVarargs`.

---

## G. Exception Handling

### The six custom exceptions

| Exception | Thrown when |
|---|---|
| `InvalidStudentDataException` | regex validation fails |
| `DuplicateRegisterNumberException` | the register number already exists |
| `StudentNotFoundException` | no student with that id / register number |
| `RoomNotFoundException` | no room with that id / number |
| `RoomFullException` | the room has no free bed, or is under maintenance |
| `AllocationException` | an allocation rule is broken (already has a room, wrong gender block, not checked in) |

All extend `Exception`, so they are **checked**: the compiler forces the
caller to handle them. That is deliberate — these are all situations the
program must recover from.

### Checked vs unchecked

| | Checked | Unchecked |
|---|---|---|
| Extends | `Exception` | `RuntimeException` |
| Compiler forces handling | yes | no |
| Means | an expected, recoverable situation | a programming bug |
| Examples here | our six + `SQLException` | `NumberFormatException`, `NullPointerException` |

### try / catch / finally / throw / throws

`StudentMenu.addStudent()` shows all of them:

```java
try {
    int id = studentService.addStudent(student);   // may throw
} catch (InvalidStudentDataException e) {
    System.out.println("[INVALID DATA] " + e.getMessage());
} catch (DuplicateRegisterNumberException e) {
    System.out.println("[DUPLICATE] " + e.getMessage());
} finally {
    InputUtil.pause();       // always runs
}
```

* `throw` = actually throwing one (`throw new RoomFullException(...)`)
* `throws` = declaring that a method might throw one
* `finally` runs after success, after an exception, and even after a
  `return`. It is where cleanup belongs.
* **Catch order matters:** a subclass must be caught before its superclass,
  otherwise the code is unreachable and will not compile.

### try-with-resources

Every DAO uses it:

```java
try (Connection con = DBConnection.getConnection();
     PreparedStatement ps = con.prepareStatement(sql)) {
    ...
}   // both are closed automatically, even if an exception is thrown
```

Anything implementing `AutoCloseable` can go in the brackets. Before Java 7
you had to close in a `finally` block and nest another try/catch inside it.

---

## H. JDBC

### The five steps

1. **Load the driver** — `Class.forName("com.mysql.cj.jdbc.Driver")`
   (in the `static` block of `DBConnection`, so it runs once)
2. **Open a connection** — `DriverManager.getConnection(url, user, password)`
3. **Create a statement** — `con.prepareStatement(sql)`
4. **Execute** — `executeQuery()` for SELECT, `executeUpdate()` for
   INSERT/UPDATE/DELETE
5. **Close** — handled automatically by try-with-resources

### Statement vs PreparedStatement

| | `Statement` | `PreparedStatement` |
|---|---|---|
| SQL built by | joining strings | `?` placeholders |
| SQL injection | **vulnerable** | safe |
| Compiled | every time | once, then reused |
| Used here | `DBConnection.testConnection()` only (fixed SQL, no user input) | everywhere else |

**Why it is safe:** the SQL text and the values travel to MySQL separately.
A value can never be re-read as a command. With a plain `Statement`, a user
typing `' OR '1'='1` as a password would log in as anybody.

### executeQuery vs executeUpdate

* `executeQuery()` → returns a `ResultSet` (rows). Use for SELECT.
* `executeUpdate()` → returns an `int` (rows affected). Use for
  INSERT/UPDATE/DELETE. That is why our DAOs write `return ps.executeUpdate() > 0;`

### ResultSet

A cursor sitting *before* the first row. `rs.next()` moves forward and
returns `false` when there are no more rows — which is why every read is
`if (rs.next())` or `while (rs.next())`.

Columns are read by name (`rs.getString("name")`) or by 1-based index
(`rs.getInt(1)`). **JDBC indexes start at 1, not 0.**

### Getting the generated id back

```java
con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
...
ResultSet keys = ps.getGeneratedKeys();
if (keys.next()) { return keys.getInt(1); }
```

This is how we learn the `AUTO_INCREMENT` id MySQL just assigned.

### NULL handling

`checkout_date` is NULL while the student is still staying:

```java
ps.setNull(4, java.sql.Types.DATE);        // writing a NULL
```

and when reading, `rs.getDate()` returns `null`, so calling
`.toLocalDate()` on it directly would throw `NullPointerException`. That is
why every DAO has a small `toLocalDate(Date)` helper.

---

## I. Transactions

### Why they are needed

Allocating a room is **two** changes to the database:

1. insert a row into `allocations`
2. increase `rooms.occupied_beds`

If step 1 succeeds and the program dies before step 2, the database now
claims a student lives in a room that says it is empty — and that bed will
be given to somebody else. The data is corrupt and nothing will fix it
automatically.

A transaction makes both changes **atomic**: all of them, or none of them.

### The pattern (in `AllocationService`)

```java
Connection con = null;
try {
    con = DBConnection.getConnection();
    con.setAutoCommit(false);        // ---- BEGIN TRANSACTION ----

    ...all the steps, all on THIS connection...

    con.commit();                    // ---- COMMIT ----
} catch (SQLException e) {
    con.rollback();                  // ---- ROLLBACK: undo everything ----
    throw e;
} finally {
    con.setAutoCommit(true);
    con.close();
}
```

**By default JDBC is in auto-commit mode**, where every single statement
commits itself. `setAutoCommit(false)` is what groups several statements
into one unit of work.

**Critically, every step must use the SAME `Connection`.** That is why
methods like `RoomDAO.updateOccupancy(Connection con, ...)` take a
connection parameter instead of opening their own — a second connection
would be a separate transaction and would not roll back with the first.

### The three transactions in this project

| Operation | Steps inside one transaction |
|---|---|
| **Allocate** | insert allocation + increase occupied_beds + remove from waiting list |
| **Transfer** | close old allocation + free old room bed + fill new room bed + insert new allocation |
| **Checkout** | close allocation (CHECKED_OUT + date) + free the room bed |

### ACID

| Letter | Meaning | Here |
|---|---|---|
| **A**tomicity | all or nothing | commit / rollback |
| **C**onsistency | rules are never violated | foreign keys, UNIQUE, and `occupied_beds <= capacity` |
| **I**solation | concurrent transactions do not see each other's half-done work | MySQL InnoDB default: REPEATABLE READ |
| **D**urability | committed data survives a crash | InnoDB writes to disk |

**Note:** transactions only work on **InnoDB** tables (MySQL's default
engine). The older MyISAM engine ignores them silently — a good viva detail.
