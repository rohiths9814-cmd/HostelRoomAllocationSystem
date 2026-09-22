package com.hostel.main;

import com.hostel.exception.DuplicateRegisterNumberException;
import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.Student;
import com.hostel.service.StudentService;
import com.hostel.util.InputUtil;

import java.sql.SQLException;
import java.util.ArrayList;

/** MODULE 2 - the student management screens. */
public class StudentMenu {

    private final StudentService studentService = new StudentService();

    public void show() throws SQLException {

        boolean stay = true;
        while (stay) {
            System.out.println("\n--------------- STUDENT MANAGEMENT ---------------");
            System.out.println("  1. Add student");
            System.out.println("  2. View all students");
            System.out.println("  3. View one student (by ID)");
            System.out.println("  4. Search student (name or register number)");
            System.out.println("  5. Update student");
            System.out.println("  6. Delete student");
            System.out.println("  7. Students without a room");
            System.out.println("  8. Back to main menu");
            System.out.println("--------------------------------------------------");

            int choice = InputUtil.readIntInRange("Enter choice (1-8): ", 1, 8);

            switch (choice) {
                case 1: addStudent(); break;
                case 2: viewAllStudents(); break;
                case 3: viewOneStudent(); break;
                case 4: searchStudents(); break;
                case 5: updateStudent(); break;
                case 6: deleteStudent(); break;
                case 7: viewStudentsWithoutRoom(); break;
                case 8: stay = false; break;
                default: break;
            }
        }
    }

    // ==========================================================
    // 1. ADD
    // ==========================================================
    private void addStudent() throws SQLException {

        System.out.println("\n----- ADD STUDENT -----");
        System.out.println("(register number format: 26CSE001)");

        Student student = new Student();
        student.setRegisterNumber(
                InputUtil.readNonEmptyString("Register Number : ").toUpperCase());
        student.setName(InputUtil.readNonEmptyString("Name            : "));
        student.setDepartment(
                InputUtil.readNonEmptyString("Department      : ").toUpperCase());
        student.setYear(InputUtil.readIntInRange("Year (1-4)      : ", 1, 4));
        student.setGender(readGender());
        student.setPhone(InputUtil.readNonEmptyString("Phone (10 digit): "));
        student.setEmail(InputUtil.readNonEmptyString("Email           : "));
        student.setAddress(InputUtil.readString("Address         : "));

        /*
         * Three different problems, three different catch blocks, three
         * different messages. That is the whole point of writing custom
         * exception classes instead of returning true/false.
         */
        try {
            int id = studentService.addStudent(student);
            if (id > 0) {
                System.out.println("\nStudent added successfully. New student ID = " + id);
            } else {
                System.out.println("\nThe student could not be saved.");
            }
        } catch (InvalidStudentDataException e) {
            System.out.println("\n[INVALID DATA] " + e.getMessage());
        } catch (DuplicateRegisterNumberException e) {
            System.out.println("\n[DUPLICATE] " + e.getMessage());
        } finally {
            // finally always runs - after success, after an exception, even
            // after a return statement. It is where cleanup belongs.
            InputUtil.pause();
        }
    }

    // ==========================================================
    // 2. VIEW ALL
    // ==========================================================
    private void viewAllStudents() throws SQLException {

        ArrayList<Student> students = studentService.getAllStudents();
        printStudentTable(students, "ALL STUDENTS");
        InputUtil.pause();
    }

    // ==========================================================
    // 3. VIEW ONE
    // ==========================================================
    private void viewOneStudent() throws SQLException {

        int id = InputUtil.readInt("\nEnter student ID: ");
        try {
            Student student = studentService.getStudentById(id);
            System.out.println("\n----- STUDENT DETAILS -----");
            student.displayFull();
        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        }
        InputUtil.pause();
    }

    // ==========================================================
    // 4. SEARCH
    // ==========================================================
    private void searchStudents() throws SQLException {

        String keyword = InputUtil.readNonEmptyString("\nEnter name or register number: ");
        ArrayList<Student> results = studentService.searchStudents(keyword);
        printStudentTable(results, "SEARCH RESULTS FOR: " + keyword);
        InputUtil.pause();
    }

    // ==========================================================
    // 5. UPDATE
    // ==========================================================
    private void updateStudent() throws SQLException {

        int id = InputUtil.readInt("\nEnter the student ID to update: ");

        try {
            Student student = studentService.getStudentById(id);

            System.out.println("\nCurrent details:");
            student.displayFull();
            System.out.println("\nPress ENTER to keep the current value.");

            student.setName(readOrKeep("Name       ", student.getName()));
            student.setDepartment(readOrKeep("Department ", student.getDepartment()));

            String yearText = InputUtil.readString("Year (1-4) [" + student.getYear() + "]: ");
            if (!yearText.isEmpty()) {
                try {
                    student.setYear(Integer.parseInt(yearText));
                } catch (NumberFormatException e) {
                    System.out.println("  (not a number - keeping " + student.getYear() + ")");
                }
            }

            student.setGender(readOrKeep("Gender     ", student.getGender()).toUpperCase());
            student.setPhone(readOrKeep("Phone      ", student.getPhone()));
            student.setEmail(readOrKeep("Email      ", student.getEmail()));
            student.setAddress(readOrKeep("Address    ", student.getAddress()));

            if (studentService.updateStudent(student)) {
                System.out.println("\nStudent updated successfully.");
            } else {
                System.out.println("\nNothing was changed.");
            }

        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (InvalidStudentDataException e) {
            System.out.println("[INVALID DATA] " + e.getMessage());
            System.out.println("The student was NOT updated.");
        }
        InputUtil.pause();
    }

    // ==========================================================
    // 6. DELETE
    // ==========================================================
    private void deleteStudent() throws SQLException {

        int id = InputUtil.readInt("\nEnter the student ID to delete: ");

        try {
            Student student = studentService.getStudentById(id);
            System.out.println("\nAbout to delete:");
            student.displayFull();

            if (!InputUtil.readYesNo("\nAre you sure")) {
                System.out.println("Cancelled. Nothing was deleted.");
                InputUtil.pause();
                return;
            }

            if (studentService.deleteStudent(id)) {
                System.out.println("Student deleted.");
            } else {
                System.out.println("The student was not deleted.");
            }

        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        }
        InputUtil.pause();
    }

    // ==========================================================
    // 7. STUDENTS WITHOUT A ROOM
    // ==========================================================
    private void viewStudentsWithoutRoom() throws SQLException {

        ArrayList<Student> students = studentService.getStudentsWithoutRoom();
        printStudentTable(students, "STUDENTS WITHOUT A ROOM");
        InputUtil.pause();
    }

    // ==========================================================
    // SMALL HELPERS
    // ==========================================================

    /** Prints any list of students as a table, or says it is empty. */
    private void printStudentTable(ArrayList<Student> students, String title) {

        System.out.println("\n===== " + title + "  (" + students.size() + ") =====");

        if (students.isEmpty()) {
            System.out.println("No students to show.");
            return;
        }

        System.out.println(students.get(0).header());
        System.out.println("--------------------------------------------------"
                + "--------------------------------------");

        for (int i = 0; i < students.size(); i++) {
            students.get(i).display();
        }
    }

    /** Shows the old value in brackets and keeps it when ENTER is pressed. */
    private String readOrKeep(String label, String currentValue) {
        String typed = InputUtil.readString(label + " [" + currentValue + "]: ");
        if (typed.isEmpty()) {
            return currentValue;
        }
        return typed;
    }

    private String readGender() {
        while (true) {
            String gender = InputUtil.readNonEmptyString("Gender (M/F)    : ").toUpperCase();
            if (gender.startsWith("M")) {
                return "MALE";
            }
            if (gender.startsWith("F")) {
                return "FEMALE";
            }
            System.out.println("  !! Type M for male or F for female.");
        }
    }
}
