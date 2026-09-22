# Hostel Room Allocation and Management System

A mini project in **Core Java + JDBC + MySQL**.

No Spring, no Hibernate, no Maven, no ORM. Every concept in the syllabus is
demonstrated in real working code.

### The project is three programs

| Part | What it is | Port | Run with |
|---|---|---|---|
| **1. Console app** | `com.hostel.main.Main` — the full menu system | – | `run.bat` |
| **2. REST API** | `com.hostel.server.ApiServer` — serves JSON | 8080 | `run-api.bat` |
| **3. React frontend** | `hostel-frontend/` — web interface | 5173 | `run-frontend.bat` |

**Part 1 runs completely on its own** and is all you need for a console
demo. Parts 2 and 3 are the web version of the same system — the API reuses
exactly the same `service` and `dao` classes, so the business rules and
transactions are shared, not duplicated.

> **Moving to another computer?** Follow
> **[docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md)** instead of this file — it is
> the complete, tested, step-by-step version.

---

## 1. What you need

| Tool | Version | Needed for |
|---|---|---|
| JDK | 17+ (built and tested on 21) | all parts |
| MySQL Server | 8.0 | all parts |
| MySQL Connector/J | bundled in `lib/` | all parts |
| Node.js | 20.19+ or 22.12+ | the React frontend only |
| VS Code | Extension Pack for Java | editing |

---

## 0. The scripts

| Script | What it does |
|---|---|
| `setup-database.bat` | creates the `hostel_management` database from `schema.sql` |
| `compile.bat` | compiles every `.java` file into `bin/` |
| `run.bat` | starts the console application |
| `run-api.bat` | starts the REST API server on port 8080 |
| `run-frontend.bat` | installs deps if needed, starts the React dev server |
| `run-demo.bat` | the no-database concept demonstration |

---

## 2. Setup (do this once)

### Step 1 — Install MySQL

Download **MySQL Community Server** from
<https://dev.mysql.com/downloads/mysql/> and install it.
During setup you will be asked for a **root password** — write it down.

### Step 2 — Download the JDBC driver

Java cannot talk to MySQL on its own. It needs a **driver** — a `.jar` file
that translates JDBC calls into the MySQL network protocol.

Download **Connector/J (Platform Independent)** from
<https://dev.mysql.com/downloads/connector/j/>, unzip it, and copy
`mysql-connector-j-x.x.x.jar` into the `lib/` folder of this project.

### Step 3 — Create the database

Open MySQL Command Line Client (or MySQL Workbench) and run:

```bash
mysql -u root -p < database/schema.sql
```

That creates the `hostel_management` database, all 8 tables, and sample data
(3 blocks, 7 rooms, 5 students, 2 admins).

### Step 4 — Put your password in the code

Open `src/com/hostel/util/DBConnection.java` and change these lines to match
your MySQL installation:

```java
private static final String USERNAME = "root";
private static final String PASSWORD = "root";   // <-- your root password
```

---

## 3. How to compile and run

From the project root (`HostelRoomAllocationSystem`):

### Windows (Command Prompt / PowerShell)

Compile:

```bash
javac -d bin -sourcepath src src\com\hostel\main\Main.java src\com\hostel\main\ConceptDemo.java
```

Run the full application:

```bash
java -ea -cp "bin;lib\*" com.hostel.main.Main
```

Or just double-click **`compile.bat`** then **`run.bat`**.

### What the flags mean — a guaranteed viva question

| Flag | Meaning |
|---|---|
| `-d bin` | write the `.class` files into `bin/`, recreating the package folders |
| `-sourcepath src` | where to find the other `.java` files it needs |
| `-cp "bin;lib\*"` | **classpath**: look for classes in `bin`, and for the driver in `lib` |
| `-ea` | **enable assertions** (they are switched OFF by default) |
| `com.hostel.main.Main` | the fully qualified class name — not a filename |

On Linux or Mac the classpath separator is `:` instead of `;`.

### Login

```
username : admin
password : admin123
```

(The second seeded account is `warden` / `warden123`.)

---

## 4. Run the concept demo without MySQL

Not ready to install MySQL yet? This second `main` method needs no database
at all and prints OOP, regex, wrapper classes, collections, exceptions and
assertions working live:

```bash
java -ea -cp bin com.hostel.main.ConceptDemo
```

This is also the easiest thing to show at the start of a viva.

---

## 5. Project structure

```
HostelRoomAllocationSystem/
├── database/
│   └── schema.sql              all 8 tables + sample data
├── lib/                        mysql-connector-j-26.7.0.jar (the driver)
├── bin/                        compiled .class files (ignored by git)
├── docs/
│   ├── SETUP_GUIDE.md          how to run this on another computer
│   ├── CONCEPTS.md             where every syllabus topic is used
│   ├── VIVA_QUESTIONS.md       questions and answers
│   └── TEST_CASES.md           the test table for your report
├── hostel-frontend/            the React web interface (optional part)
│   ├── src/pages/              one page per module
│   ├── src/services/api.js     every fetch() call lives here
│   └── .env                    VITE_API_BASE_URL
└── src/com/hostel/
    ├── model/         the data: what a Student, Room, Allocation IS
    ├── dao/           the SQL: how it is saved and loaded
    ├── service/       the rules: what is allowed and what is not
    ├── util/          DB connection, regex validation, safe input
    ├── exception/     custom exception classes
    ├── annotation/    the custom @AdminOperation annotation
    ├── main/          the console menus
    └── server/        the REST API handlers (HttpServer, no framework)
```

### Why the layers exist

```
                 Main / Menus  ─┐
   (console screens)            ├─>  Service  ->  DAO  ->  MySQL
React  ->  server/ handlers  ─┘       (rules)     (SQL)
             (JSON over HTTP)
```

* **Menus** only print and read input. No SQL, no rules.
* **Server handlers** only parse JSON and write JSON. No SQL, no rules.
* **Services** decide what is allowed (is the room full? is the register
  number a duplicate?) and own the transactions.
* **DAOs** contain SQL and nothing else.
* **Models** hold the data and the small rules about their own fields.

Notice that the console menus and the REST handlers sit at the *same* level
— they are two different front doors into one shared `service` layer. That
is why adding the web version needed no change at all to the business rules
or the transactions.

Each layer only talks to the one below it. That is why the SQL could be
swapped for file storage without touching a single menu.

---

## 6. The 11 modules

| # | Module | Where |
|---|---|---|
| 1 | Admin login / logout | `AdminService`, `AdminDAO` |
| 2 | Student management (CRUD) | `StudentService`, `StudentMenu` |
| 3 | Hostel block management | `BlockService`, `HostelRoomMenu` |
| 4 | Room management | `RoomService`, `HostelRoomMenu` |
| 5 | Room allocation (**transaction**) | `AllocationService.allocateRoom()` |
| 6 | Room transfer (**transaction**) | `AllocationService.transferStudent()` |
| 7 | Checkout (**transaction**) | `AllocationService.checkoutStudent()` |
| 8 | Waiting list (LinkedList + PriorityQueue) | `WaitingListService` |
| 9 | Complaint management | `ComplaintService`, `ComplaintMenu` |
| 10 | Fee management | `FeeService`, `FeeMenu` |
| 11 | Reports | `ReportService`, `ReportMenu` |

---

## 7. Database design

Eight tables with primary keys, foreign keys and unique constraints.

```
hostel_blocks 1 ──< rooms 1 ──< allocations >── 1 students
                                                    │
                                    ┌───────────────┼───────────────┐
                                    │               │               │
                               complaints     hostel_fees     waiting_list
```

Key constraints and why they exist:

| Constraint | Table | Why |
|---|---|---|
| `register_number UNIQUE` | students | two students can never share a register number, even if the Java check is bypassed |
| `UNIQUE (block_id, room_number)` | rooms | a room number only has to be unique *inside* its block |
| `FOREIGN KEY block_id` | rooms | a room cannot point at a block that does not exist |
| `FOREIGN KEY student_id, room_id` | allocations | an allocation cannot reference a deleted student or room |

**Why allocations are never deleted:** a transfer marks the old row
`TRANSFERRED` and inserts a new one; a checkout marks it `CHECKED_OUT`.
The table therefore doubles as the complete stay and transfer history,
which is why no separate history table is needed.

---

## 8. Where each syllabus concept lives

Short version — the full explanation is in [docs/CONCEPTS.md](docs/CONCEPTS.md).

| Concept | File |
|---|---|
| Abstract class, inheritance, polymorphism | `model/User.java`, `Admin.java`, `StudentUser.java` |
| Interface | `model/Displayable.java` |
| Encapsulation, constructor overloading | `model/Student.java` |
| Regex (`Pattern` + `Matcher`) | `util/Validation.java` |
| Wrapper classes, autoboxing | `util/InputUtil.java`, `service/ReportService.java` |
| Assertions | `model/Room.java`, `service/AllocationService.java` |
| Custom annotation | `annotation/AdminOperation.java` |
| Custom exceptions | `exception/` (6 classes) |
| JDBC `PreparedStatement` | every class in `dao/` |
| **Transactions (commit / rollback)** | `service/AllocationService.java` |
| ArrayList, HashSet, HashMap | `dao/StudentDAO.java`, `dao/BlockDAO.java` |
| LinkedList, Queue, PriorityQueue | `service/WaitingListService.java` |
| TreeMap (sorted grouping) | `service/ReportService.java` |

---

## 9. Troubleshooting

Full list in [docs/SETUP_GUIDE.md](docs/SETUP_GUIDE.md) §9.

**`Public Key Retrieval is not allowed`**
MySQL 8 uses `caching_sha2_password`. The first time an account connects
after the MySQL service starts, the driver must fetch the server's RSA
public key, and it refuses to by default. The JDBC URL in
`DBConnection.java` must include `allowPublicKeyRetrieval=true` — it
already does. This one is sneaky: it can work for days and then fail after
a reboot, because restarting MySQL clears its password cache.

**`No suitable driver found for jdbc:mysql://...`**
The Connector/J jar is missing from the classpath. Check that the jar is in
`lib/` and that you ran with `-cp "bin;lib\*"`.

**`Access denied for user 'root'@'localhost'`**
The password in `DBConnection.java` does not match your MySQL password.

**`Unknown database 'hostel_management'`**
You have not run `database/schema.sql` yet.

**`Communications link failure`**
The MySQL service is not running. Start it from Windows Services
(`services.msc` → MySQL80 → Start).

**Assertions do not fire**
You forgot `-ea`. Without it, every `assert` line is skipped entirely.
