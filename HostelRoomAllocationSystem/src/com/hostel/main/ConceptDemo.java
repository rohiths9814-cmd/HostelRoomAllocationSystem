package com.hostel.main;

import com.hostel.exception.InvalidStudentDataException;
import com.hostel.model.Admin;
import com.hostel.model.Room;
import com.hostel.model.Student;
import com.hostel.model.StudentUser;
import com.hostel.model.User;
import com.hostel.model.WaitingEntry;
import com.hostel.util.Validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A SECOND main method that needs NO database.
 *
 * Run it to see OOP, collections, regex, wrapper classes, exceptions and
 * assertions working on their own. It is the easiest way to test the Java
 * side of the project before MySQL is installed, and it is a good thing to
 * demonstrate in the viva.
 *
 *      java -ea -cp bin com.hostel.main.ConceptDemo
 */
public class ConceptDemo {

    public static void main(String[] args) {

        System.out.println("=========================================================");
        System.out.println("   JAVA CONCEPT DEMONSTRATION  (no database needed)      ");
        System.out.println("=========================================================");

        demoOop();
        demoRegex();
        demoWrapperClasses();
        demoCollections();
        demoExceptions();
        demoAssertions();

        System.out.println("\n=========================================================");
        System.out.println("  All demonstrations finished.");
        System.out.println("=========================================================");
    }

    // ==========================================================
    // 1. OOP
    // ==========================================================
    private static void demoOop() {

        System.out.println("\n--- 1. OOP: inheritance, overriding, polymorphism ---");

        // The reference type is User (the abstract parent).
        // The objects are Admin and StudentUser (the concrete children).
        User[] users = {
            new Admin(1, "admin", "admin123"),
            new StudentUser(2, "rohith", "26CSE001")
        };

        for (int i = 0; i < users.length; i++) {
            users[i].showBasicInfo();      // inherited from User
            users[i].displayRole();        // overridden - Java picks at RUNTIME
            System.out.println();
        }

        System.out.println("Same loop, same method name, two different outputs.");
        System.out.println("That is polymorphism / dynamic method dispatch.");

        // Constructor overloading: three ways to build a Student
        Student a = new Student();
        Student b = new Student("26CSE001", "Rohith", "CSE", 2,
                "MALE", "9876543210", "rohith@gmail.com", "Salem");
        Student c = new Student(7, "26CSE009", "Meena", "IT", 3,
                "FEMALE", "9876500000", "meena@gmail.com", "Erode");

        System.out.println("\nConstructor overloading:");
        System.out.println("  no-arg     -> " + a);
        System.out.println("  8 argument -> " + b);
        System.out.println("  9 argument -> " + c);
    }

    // ==========================================================
    // 2. REGEX
    // ==========================================================
    private static void demoRegex() {

        System.out.println("\n--- 2. REGEX validation ---");

        String[] registerNumbers = {"26CSE001", "26cse001", "CSE001", "26C001", "26CSE0011"};
        System.out.println("Register number rule: 2 digits, 2-4 capitals, 3 digits");
        System.out.println("(the validator upper-cases the text first, so 26cse001 passes)");
        for (int i = 0; i < registerNumbers.length; i++) {
            report(registerNumbers[i], Validation.isValidRegisterNumber(registerNumbers[i]));
        }

        String[] phones = {"9876543210", "12345", "1234567890", "98765432101"};
        System.out.println("\nPhone rule: 10 digits starting with 6, 7, 8 or 9");
        for (int i = 0; i < phones.length; i++) {
            report(phones[i], Validation.isValidPhone(phones[i]));
        }

        String[] emails = {"abc@gmail.com", "abc", "abc@gmail", "a.b-c@mail.co.in"};
        System.out.println("\nEmail rule: text @ text . text");
        for (int i = 0; i < emails.length; i++) {
            report(emails[i], Validation.isValidEmail(emails[i]));
        }

        String[] rooms = {"A-101", "AB-202", "A101", "A-12", "ABC-101"};
        System.out.println("\nRoom number rule: 1-2 capitals, hyphen, 3 digits");
        for (int i = 0; i < rooms.length; i++) {
            report(rooms[i], Validation.isValidRoomNumber(rooms[i]));
        }
    }

    private static void report(String value, boolean valid) {
        System.out.println("   " + String.format("%-18s", value)
                + (valid ? "VALID" : "INVALID"));
    }

    // ==========================================================
    // 3. WRAPPER CLASSES
    // ==========================================================
    private static void demoWrapperClasses() {

        System.out.println("\n--- 3. WRAPPER CLASSES ---");

        // parsing: String -> primitive
        int year = Integer.parseInt("3");
        double amount = Double.parseDouble("45000.50");
        System.out.println("Integer.parseInt(\"3\")          = " + year
                + "   (primitive int)");
        System.out.println("Double.parseDouble(\"45000.50\") = " + amount
                + "   (primitive double)");

        // valueOf: String or primitive -> wrapper OBJECT
        Integer boxedYear = Integer.valueOf("4");
        Boolean flag = Boolean.valueOf("true");
        Character grade = Character.valueOf('A');
        System.out.println("Integer.valueOf(\"4\")           = " + boxedYear
                + "   (Integer object)");
        System.out.println("Boolean.valueOf(\"true\")        = " + flag);
        System.out.println("Character.valueOf('A')         = " + grade);

        // autoboxing and unboxing
        Integer boxed = 10;          // AUTOBOXING   int -> Integer
        int plain = boxed;           // UNBOXING     Integer -> int
        System.out.println("\nAutoboxing  int 10 -> Integer : " + boxed);
        System.out.println("Unboxing    Integer -> int    : " + plain);

        // useful constants and helpers
        System.out.println("\nInteger.MAX_VALUE = " + Integer.MAX_VALUE);
        System.out.println("Integer.MIN_VALUE = " + Integer.MIN_VALUE);

        // The classic trap
        Integer x = 127;
        Integer y = 127;
        Integer p = 128;
        Integer q = 128;
        System.out.println("\nThe Integer cache trap (values -128..127 are cached):");
        System.out.println("   127 == 127 -> " + (x == y) + "    (same cached object)");
        System.out.println("   128 == 128 -> " + (p == q) + "   (two different objects)");
        System.out.println("   128.equals(128) -> " + p.equals(q)
                + "   <-- always compare objects with equals()");
    }

    // ==========================================================
    // 4. COLLECTIONS
    // ==========================================================
    private static void demoCollections() {

        System.out.println("\n--- 4. COLLECTION FRAMEWORK ---");

        // ArrayList - ordered, allows duplicates, fast to read by index
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Student("26CSE001", "Rohith", "CSE", 2,
                "MALE", "9876543210", "rohith@gmail.com", "Salem"));
        students.add(new Student("26CSE002", "Kumar", "CSE", 3,
                "MALE", "9876543211", "kumar@gmail.com", "Erode"));
        System.out.println("ArrayList  (a list of students, keeps insertion order)");
        System.out.println("   size = " + students.size()
                + ", first = " + students.get(0).getName());

        // HashSet - no duplicates, O(1) contains()
        HashSet<String> registerNumbers = new HashSet<>();
        registerNumbers.add("26CSE001");
        registerNumbers.add("26CSE002");
        boolean addedAgain = registerNumbers.add("26CSE001");   // rejected
        System.out.println("\nHashSet   (stops duplicate register numbers)");
        System.out.println("   adding 26CSE001 a second time returned " + addedAgain);
        System.out.println("   size is still " + registerNumbers.size());
        System.out.println("   contains(26CSE002) -> "
                + registerNumbers.contains("26CSE002"));

        // HashMap - key -> value, O(1) lookup
        HashMap<Integer, Room> roomMap = new HashMap<>();
        roomMap.put(1, new Room("A-101", 1, 1, 4));
        roomMap.put(2, new Room("A-102", 1, 1, 4));
        System.out.println("\nHashMap   (find a room instantly by its id)");
        System.out.println("   roomMap.get(1) -> " + roomMap.get(1));
        System.out.println("   roomMap.get(9) -> " + roomMap.get(9)
                + "   (missing key gives null, never an error)");

        // LinkedList as a FIFO queue
        Queue<String> fifo = new LinkedList<>();
        fifo.offer("Rohith");
        fifo.offer("Kumar");
        fifo.offer("Priya");
        System.out.println("\nLinkedList as a Queue  (first come, first served)");
        System.out.print("   order out: ");
        while (!fifo.isEmpty()) {
            System.out.print(fifo.poll() + "  ");
        }

        // PriorityQueue - smallest first, decided by compareTo()
        PriorityQueue<WaitingEntry> priorityQueue = new PriorityQueue<>();
        WaitingEntry e1 = new WaitingEntry(1, 5);
        e1.setStudentName("Rohith");
        WaitingEntry e2 = new WaitingEntry(2, 1);
        e2.setStudentName("Kumar");
        WaitingEntry e3 = new WaitingEntry(3, 3);
        e3.setStudentName("Priya");
        priorityQueue.offer(e1);
        priorityQueue.offer(e2);
        priorityQueue.offer(e3);

        System.out.println("\n\nPriorityQueue  (highest priority first, 1 beats 5)");
        System.out.println("   added in order: Rohith(5), Kumar(1), Priya(3)");
        System.out.print("   comes out as : ");
        while (!priorityQueue.isEmpty()) {
            WaitingEntry next = priorityQueue.poll();
            System.out.print(next.getStudentName() + "(" + next.getPriority() + ")  ");
        }
        System.out.println();
    }

    // ==========================================================
    // 5. EXCEPTION HANDLING
    // ==========================================================
    private static void demoExceptions() {

        System.out.println("\n--- 5. EXCEPTION HANDLING ---");

        Student bad = new Student("BAD-REG", "R0hith99", "CSE", 9,
                "MALE", "12345", "not-an-email", "Salem");

        try {
            System.out.println("Validating a deliberately broken student...");
            bad.validate();
            System.out.println("   (this line is never reached)");

        } catch (InvalidStudentDataException e) {
            System.out.println("   CAUGHT InvalidStudentDataException:");
            System.out.println("   -> " + e.getMessage());

        } finally {
            System.out.println("   finally always runs - this is where cleanup goes.");
        }

        // A built-in unchecked exception
        try {
            System.out.println("\nParsing the text \"abc\" as a number...");
            int broken = Integer.parseInt("abc");
            System.out.println(broken);
        } catch (NumberFormatException e) {
            System.out.println("   CAUGHT NumberFormatException: " + e.getMessage());
            System.out.println("   This is why InputUtil.readInt() re-asks instead");
            System.out.println("   of letting the program crash.");
        }
    }

    // ==========================================================
    // 6. ASSERTIONS
    // ==========================================================
    private static void demoAssertions() {

        System.out.println("\n--- 6. ASSERTIONS ---");

        boolean enabled = false;
        assert enabled = true;          // this line only runs when -ea is given

        if (!enabled) {
            System.out.println("Assertions are currently OFF.");
            System.out.println("Restart with:  java -ea -cp bin com.hostel.main.ConceptDemo");
            return;
        }

        System.out.println("Assertions are ON.");

        Room room = new Room("A-101", 1, 1, 2);    // capacity 2
        room.occupyOneBed();
        room.occupyOneBed();
        System.out.println("Room A-101 filled legally: " + room);

        System.out.println("\nNow forcing an illegal state (occupied 5 of 2 beds)");
        System.out.println("and calling getAvailableBeds()...");

        room.setOccupiedBeds(5);                   // corrupt the object on purpose

        try {
            room.getAvailableBeds();
            System.out.println("   No error - assertions must be disabled.");
        } catch (AssertionError e) {
            System.out.println("   CAUGHT AssertionError:");
            System.out.println("   -> " + e.getMessage());
            System.out.println("\n   Note: AssertionError is an Error, not an Exception.");
            System.out.println("   Normally you do NOT catch it - it means the code has");
            System.out.println("   a bug. It is caught here only to show the message.");
        }
    }
}
