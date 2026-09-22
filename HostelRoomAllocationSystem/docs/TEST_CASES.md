# Test Cases

Run these in order after loading `database/schema.sql`. Paste this table
into your project report — the "Actual Result" column is for you to fill in
while testing, and the screenshots you take here are your evidence.

Start the program with assertions on:

```bash
java -ea -cp "bin;lib\*" com.hostel.main.Main
```

---

## A. Login (Module 1)

| # | Test | Input | Expected Result | Actual |
|---|---|---|---|---|
| A1 | Valid login | `admin` / `admin123` | Login successful, main menu appears | |
| A2 | Wrong password | `admin` / `wrong123` | "Login failed. Username or password is wrong." | |
| A3 | Username too short (regex) | `ad` / `admin123` | "Invalid username format." — no database query is made | |
| A4 | Password with no digit (regex) | `admin` / `password` | "Invalid password format." | |
| A5 | SQL injection attempt | `admin` / `x' OR '1'='1` | Login fails. PreparedStatement treats it as a literal password | |
| A6 | Logout | menu option 11 | Returns to the login menu | |

---

## B. Student Management (Module 2)

| # | Test | Input | Expected Result | Actual |
|---|---|---|---|---|
| B1 | Add a valid student | `26CSE010`, `Arun Kumar`, `CSE`, 2, M, `9876543299`, `arun2@gmail.com` | "Student added successfully. New student ID = 6" | |
| B2 | **Duplicate register number** | register `26CSE001` | `[DUPLICATE] Register number 26CSE001 already exists.` | |
| B3 | Invalid register number | `CSE001` | `[INVALID DATA] Register number must look like 26CSE001...` | |
| B4 | Invalid phone (too short) | `12345` | `[INVALID DATA] Phone must be 10 digits and start with 6, 7, 8 or 9.` | |
| B5 | Invalid phone (wrong first digit) | `1234567890` | Same message as B4 | |
| B6 | Invalid email | `abc` | `[INVALID DATA] Email must look like name@example.com` | |
| B7 | **Invalid year** | `8` | "Enter a number between 1 and 4." and it asks again | |
| B8 | Non-numeric year | `abc` | "'abc' is not a whole number. Try again." — no crash | |
| B9 | Blank name | just press ENTER | "This field cannot be left blank. Try again." | |
| B10 | View all students | menu 2 | Table with all students, aligned columns | |
| B11 | View one student | ID `1` | Full details of Rohith | |
| B12 | View non-existent student | ID `999` | `[NOT FOUND] No student found with ID 999` | |
| B13 | Search by partial name | `roh` | Finds Rohith | |
| B14 | Search by register number | `26CSE` | Finds all CSE students | |
| B15 | Search with no match | `zzzz` | "No students to show." (0 results, no error) | |
| B16 | Update a student | change phone | "Student updated successfully." | |
| B17 | Update with invalid data | phone `123` | `[INVALID DATA]` and "The student was NOT updated." | |
| B18 | Delete a student with no room | the one from B1 | "Student deleted." | |
| B19 | **Delete a student who has a room** | a student allocated in D1 | "This student is still living in a room. Check the student out first." | |

---

## C. Block and Room Management (Modules 3, 4)

| # | Test | Input | Expected Result | Actual |
|---|---|---|---|---|
| C1 | Add a block | `Block D`, F, 2 | "Block added. New block ID = 4" | |
| C2 | Invalid block name | `@@@` | `[INVALID DATA] Block name must start with a letter...` | |
| C3 | Delete an empty block | Block D | "Block deleted." | |
| C4 | **Delete a block that has rooms** | Block A | "This block still has 3 room(s). Delete or move those rooms first." | |
| C5 | Add a room | `A-301`, block 1, floor 3, capacity 4 | "Room added." | |
| C6 | Invalid room number | `A101` (no hyphen) | `[INVALID DATA] Room number must look like A-101...` | |
| C7 | **Duplicate room number** | `A-101` | `[INVALID DATA] Room number A-101 already exists.` | |
| C8 | Non-existent block id | block `99` | `[INVALID DATA] Block ID 99 does not exist.` | |
| C9 | Capacity out of range | `9` | "Enter a number between 1 and 6." | |
| C10 | Search a room | `A-101` | Full details, free beds calculated correctly | |
| C11 | View AVAILABLE rooms | menu 6 | Only rooms with status AVAILABLE | |
| C12 | View FULL rooms | menu 8 | Only rooms with status FULL | |
| C13 | Put a room in maintenance | empty room | Status becomes MAINTENANCE | |
| C14 | **Maintenance on an occupied room** | a room with students | "Cannot start maintenance: students are still inside." | |
| C15 | **Lower capacity below occupancy** | room with 2 students, set capacity 1 | `[INVALID DATA] Capacity cannot be less than the 2 student(s) already staying...` | |
| C16 | Delete an occupied room | a room with students | "Room A-101 still has 1 student(s) in it." | |

---

## D. Allocation (Module 5) — the core tests

| # | Test | Input | Expected Result | Actual |
|---|---|---|---|---|
| D1 | **Allocate a room** | student 1 (Rohith) → room 1 (A-101) | ALLOCATION SUCCESSFUL. Capacity 4, Occupied 1, Available 3, status PARTIALLY_OCCUPIED | |
| D2 | Verify in MySQL | `SELECT * FROM rooms WHERE room_id=1;` | `occupied_beds` = 1, `status` = PARTIALLY_OCCUPIED | |
| D3 | Verify the allocation row | `SELECT * FROM allocations;` | One row, status ACTIVE, today's date, `checkout_date` NULL | |
| D4 | **Duplicate allocation** | student 1 again | `[NOT ALLOWED] Rohith already has an active room allocation. Use Room Transfer instead.` | |
| D5 | Fill a room completely | allocate 2 students to B-101 (capacity 2) | After the second, status becomes FULL | |
| D6 | **Allocate to a full room** | a third student → B-101 | `[ROOM FULL] Room B-101 is FULL (2/2 beds used)...` then offers the waiting list | |
| D7 | **Wrong gender block** | Priya (FEMALE) → A-101 (Block A is MALE) | `[NOT ALLOWED] Room A-101 is in Block A, which is reserved for MALE students.` | |
| D8 | Maintenance room | any student → B-102 | Room does not appear in the free list at all | |
| D9 | Non-existent room | room id `999` | `[NOT FOUND] No room with ID 999` | |
| D10 | Non-existent student | student id `999` | `[NOT FOUND] No student with ID 999` | |

---

## E. Transfer (Module 6)

| # | Test | Input | Expected Result | Actual |
|---|---|---|---|---|
| E1 | **Transfer a student** | Rohith A-101 → A-102 | TRANSFER SUCCESSFUL, "Moved out of: A-101 (now 0/4)" | |
| E2 | Old room freed | `SELECT * FROM rooms WHERE room_number='A-101';` | `occupied_beds` back to 0, status AVAILABLE | |
| E3 | New room filled | `SELECT * FROM rooms WHERE room_number='A-102';` | `occupied_beds` = 1, status PARTIALLY_OCCUPIED | |
| E4 | **History kept** | `SELECT * FROM allocations WHERE student_id=1;` | TWO rows: the old one TRANSFERRED with a checkout date, the new one ACTIVE | |
| E5 | Transfer to the same room | Rohith → A-102 again | `[NOT ALLOWED] The student is already in that room.` | |
| E6 | Transfer to a full room | → B-101 (full) | `[ROOM FULL] Room B-101 is not available (FULL).` | |
| E7 | Transfer a student with no room | a student with no allocation | `[NOT ALLOWED] ... does not have a room yet. Allocate one first.` | |

---

## F. Checkout (Module 7)

| # | Test | Input | Expected Result | Actual |
|---|---|---|---|---|
| F1 | **Check out a student** | Rohith | "Room A-102 now has 4 free bed(s). Status: AVAILABLE" | |
| F2 | Allocation closed | `SELECT * FROM allocations WHERE student_id=1;` | The ACTIVE row is now CHECKED_OUT with today as `checkout_date` | |
| F3 | Bed freed | `SELECT * FROM rooms WHERE room_number='A-102';` | `occupied_beds` decreased by 1, status recalculated | |
| F4 | **Checkout twice** | Rohith again | He no longer appears in the active list; by id: `[NOT ALLOWED] ... is not currently allocated to any room.` | |
| F5 | Bed is reusable | allocate another student to A-102 | Succeeds | |
| F6 | Cancel a checkout | answer `n` | "Cancelled." Nothing changes in the database | |

---

## G. Waiting List (Module 8)

| # | Test | Input | Expected Result | Actual |
|---|---|---|---|---|
| G1 | Add to waiting list | a student, priority 3 | "Added to the waiting list. Waiting ID = 1" | |
| G2 | **Duplicate entry** | same student again | `[NOT ALLOWED] ... is already on the waiting list.` | |
| G3 | Student who already has a room | an allocated student | `[NOT ALLOWED] ... already has a room, so they cannot wait for one.` | |
| G4 | Invalid priority | `9` | "Enter a number between 1 and 5." | |
| G5 | **FIFO vs PRIORITY** | add three students with priorities 5, 1, 3 then menu 3 | List 1 is in the order added; list 2 is priority 1, 3, 5 | |
| G6 | Who is next | menu 4 | The priority-1 student | |
| G7 | **Allocate the next student** | menu 5 | Allocation succeeds AND the waiting entry becomes ALLOCATED in the same transaction | |
| G8 | Verify de-queue | `SELECT * FROM waiting_list;` | That row shows status ALLOCATED, not WAITING | |
| G9 | Remove from waiting list | menu 6 | Status becomes REMOVED | |
| G10 | Empty waiting list | remove everyone, then menu 2 | "Nobody is waiting for a room." | |

---

## H. Complaints (Module 9)

| # | Test | Input | Expected Result | Actual |
|---|---|---|---|---|
| H1 | Create a complaint | student 1, WIFI, "No internet in the room" | "Complaint registered. Complaint ID = 1. Status: PENDING" | |
| H2 | Description too short | `ab` | `[INVALID DATA] Description must be at least 5 characters long.` | |
| H3 | Non-existent student | id `999` | `[NOT FOUND] No student with ID 999` | |
| H4 | PENDING → IN_PROGRESS | menu 7 | "Status changed to IN_PROGRESS." | |
| H5 | Resolve a complaint | menu 8 | Status RESOLVED, `resolved_date` set to today | |
| H6 | **Reopen a resolved complaint** | set it back to PENDING | "This complaint was already resolved on ... Reopening it is not allowed." | |
| H7 | Filter by status | menus 3, 4, 5 | Each shows only complaints with that status | |
| H8 | Complaints of one student | menu 6 | Only that student's complaints | |

---

## I. Fees (Module 10)

| # | Test | Input | Expected Result | Actual |
|---|---|---|---|---|
| I1 | Add a fee | student 1, `45000` | "Fee record created. Status: PENDING" | |
| I2 | Non-numeric amount | `abc` | "'abc' is not a valid amount. Try again." — no crash | |
| I3 | Amount too small | `50` | `[INVALID DATA] Fee amount must be between 1000.0 and 200000.0.` | |
| I4 | Negative amount | `-500` | "Amount cannot be negative." | |
| I5 | **Mark as paid** | the fee from I1 | "Payment recorded. Status is now PAID." | |
| I6 | **Pay twice** | the same fee id | "This fee was already paid on ..." | |
| I7 | Pending fees list | menu 3 | Only PENDING rows, with the total at the bottom | |
| I8 | Payment history | menu 5, student 1 | All their fees plus total paid / total pending | |
| I9 | Fee summary | menu 7 | Counts and amounts match the lists | |

---

## J. Reports (Module 11)

| # | Test | Expected Result | Actual |
|---|---|---|---|
| J1 | Hostel summary | All counts match the database; occupancy % is correct | |
| J2 | Students without a room | Matches `students` minus ACTIVE allocations | |
| J3 | Student-wise allocation | One row per student currently in the hostel | |
| J4 | Room-wise student list | Grouped by room, **rooms in sorted order** (TreeMap) | |
| J5 | Block-wise vacancy | Beds, occupied and free add up per block | |
| J6 | Available / partial / full / maintenance | Each list matches the room statuses | |
| J7 | Pending fees | Matches the fee module | |
| J8 | Complaint statistics | Counts by status and category; categories with 0 show `0`, not an error | |
| J9 | Full allocation history | Shows ACTIVE, TRANSFERRED and CHECKED_OUT rows | |
| J10 | Reports on an empty database | Every report prints "nothing to show" instead of crashing | |

---

## K. Assertions (run with `-ea`)

| # | Test | How | Expected Result | Actual |
|---|---|---|---|---|
| K1 | Assertions detected | start the program | Banner says "[assertions are ENABLED]" | |
| K2 | Assertions off | run without `-ea` | Banner says "[assertions are OFF]" | |
| K3 | **Assertion fires** | `java -ea -cp bin com.hostel.main.ConceptDemo` | Section 6 catches `AssertionError: Occupied beds (5) exceeded capacity (2)` | |
| K4 | Corrupt the database by hand | `UPDATE rooms SET occupied_beds=99 WHERE room_id=1;` then view rooms with `-ea` | AssertionError is raised — the assertion caught data our code could not have produced | |

*(Remember to set `occupied_beds` back to the correct value after K4.)*

---

## L. Transaction rollback — the most important test

This is what proves the transaction actually works. Do it in front of the
examiner if you can.

### L1 — Rollback on a database error

1. In MySQL, temporarily break the allocations table so the INSERT will fail
   *after* the room update has already happened:

```sql
ALTER TABLE allocations MODIFY allocation_date DATE NOT NULL;
ALTER TABLE allocations ADD CONSTRAINT chk_fail CHECK (allocation_id < 0);
```

2. Note the current bed count:

```sql
SELECT room_number, occupied_beds, status FROM rooms WHERE room_id = 1;
```

3. Try to allocate a student to room 1 in the program.

**Expected:** the program prints
`[TRANSACTION ROLLED BACK - no changes were saved]` followed by a
`[DATABASE ERROR]` message.

4. Check the bed count again — **it must be unchanged**. The room update was
   undone even though it had already executed, because it was never
   committed.

5. Remove the constraint afterwards:

```sql
ALTER TABLE allocations DROP CONSTRAINT chk_fail;
```

### L2 — Rollback on a business rule

1. Note `occupied_beds` for a room in a MALE block.
2. Try to allocate a FEMALE student to it.
3. **Expected:** `[NOT ALLOWED] ... reserved for MALE students.`
4. Check `occupied_beds` — unchanged. The exception was thrown *before*
   `commit()`, so `rollback()` ran.

### L3 — Kill the program mid-transaction

1. Note the bed count of a room.
2. Start an allocation, and close the terminal window while it is running.
3. Restart and check the database — the bed count is unchanged. An
   uncommitted transaction dies with the connection.

---

## M. Database error handling

| # | Test | How | Expected Result | Actual |
|---|---|---|---|---|
| M1 | MySQL not running | stop the MySQL service, run the program | `[DATABASE ERROR] Communications link failure` + a hint. No stack trace | |
| M2 | Wrong password | change PASSWORD in `DBConnection.java` | "Access denied for user 'root'@'localhost'" | |
| M3 | Missing driver | run with `-cp bin` only | "FATAL: MySQL JDBC driver not found on the classpath." | |
| M4 | Wrong database name | change the URL | "Unknown database 'xyz'" | |

---

## Summary for your report

| Module | Test cases | Passed | Failed |
|---|---|---|---|
| Login | 6 | | |
| Student Management | 19 | | |
| Block & Room | 16 | | |
| Allocation | 10 | | |
| Transfer | 7 | | |
| Checkout | 6 | | |
| Waiting List | 10 | | |
| Complaints | 8 | | |
| Fees | 9 | | |
| Reports | 10 | | |
| Assertions | 4 | | |
| Transactions | 3 | | |
| Database errors | 4 | | |
| **Total** | **112** | | |
