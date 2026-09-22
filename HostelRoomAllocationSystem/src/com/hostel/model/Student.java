package com.hostel.model;

import com.hostel.exception.InvalidStudentDataException;
import com.hostel.util.Validation;

/**
 * ENCAPSULATION in one class:
 *   - every field is private  -> nothing outside can touch the data directly
 *   - public getters/setters  -> access happens only through methods we control
 *
 * It also implements Displayable, so a Student can be printed by any code
 * that only knows about the Displayable interface.
 */
public class Student implements Displayable {

    private int studentId;            // AUTO_INCREMENT primary key from MySQL
    private String registerNumber;    // UNIQUE - 26CSE001
    private String name;
    private String department;
    private int year;                 // 1 to 4
    private String gender;            // MALE / FEMALE
    private String phone;
    private String email;
    private String address;

    // ==========================================================
    // CONSTRUCTORS  (three of them = CONSTRUCTOR OVERLOADING)
    // ==========================================================

    /** 1. No-arg constructor - sensible defaults, no printing. */
    public Student() {
        this.studentId = 0;
        this.registerNumber = "";
        this.name = "Unknown";
        this.department = "";
        this.year = 1;
        this.gender = "MALE";
        this.phone = "";
        this.email = "";
        this.address = "";
    }

    /** 2. Everything except the id (used when ADDING a new student). */
    public Student(String registerNumber, String name, String department, int year,
                   String gender, String phone, String email, String address) {
        this.registerNumber = registerNumber;
        this.name = name;
        this.department = department;
        this.year = year;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    /**
     * 3. Full constructor including the id (used when READING from MySQL).
     *
     * this(...) calls the other constructor of the SAME class, so the eight
     * common assignments are written only once. It must be the first line.
     */
    public Student(int studentId, String registerNumber, String name, String department,
                   int year, String gender, String phone, String email, String address) {
        this(registerNumber, name, department, year, gender, phone, email, address);
        this.studentId = studentId;
    }

    // ==========================================================
    // VALIDATION
    // ==========================================================

    /**
     * Checks every field with the REGEX rules in Validation.
     * Throws instead of printing, so the caller cannot ignore the problem.
     *
     * This is NOT an assertion: bad user input is expected and must always
     * be handled, even in the final released program.
     */
    public void validate() throws InvalidStudentDataException {

        if (!Validation.isValidRegisterNumber(registerNumber)) {
            throw new InvalidStudentDataException(
                    "Register number must look like 26CSE001 (2 digits, 2-4 capitals, 3 digits).");
        }
        if (!Validation.isValidName(name)) {
            throw new InvalidStudentDataException(
                    "Name must start with a letter and contain only letters, spaces or dots.");
        }
        if (!Validation.isValidDepartment(department)) {
            throw new InvalidStudentDataException(
                    "Department must contain letters only (2-30 characters).");
        }
        if (!Validation.isValidYear(year)) {
            throw new InvalidStudentDataException(
                    "Invalid year. Please enter a year between 1 and 4.");
        }
        if (!Validation.isValidGender(gender)) {
            throw new InvalidStudentDataException("Gender must be MALE or FEMALE.");
        }
        if (!Validation.isValidPhone(phone)) {
            throw new InvalidStudentDataException(
                    "Phone must be 10 digits and start with 6, 7, 8 or 9.");
        }
        if (!Validation.isValidEmail(email)) {
            throw new InvalidStudentDataException(
                    "Email must look like name@example.com");
        }
    }

    // ==========================================================
    // GETTERS AND SETTERS
    // ==========================================================

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // ==========================================================
    // Displayable + Object overrides
    // ==========================================================

    @Override
    public String header() {
        return String.format("%-5s %-12s %-20s %-8s %-5s %-8s %-12s %-25s",
                "ID", "REG NO", "NAME", "DEPT", "YEAR", "GENDER", "PHONE", "EMAIL");
    }

    @Override
    public void display() {
        System.out.println(String.format("%-5d %-12s %-20s %-8s %-5d %-8s %-12s %-25s",
                studentId, registerNumber, name, department, year, gender, phone, email));
    }

    /** Full detail view for "View student". */
    public void displayFull() {
        System.out.println("  Student ID      : " + studentId);
        System.out.println("  Register Number : " + registerNumber);
        System.out.println("  Name            : " + name);
        System.out.println("  Department      : " + department);
        System.out.println("  Year            : " + year);
        System.out.println("  Gender          : " + gender);
        System.out.println("  Phone           : " + phone);
        System.out.println("  Email           : " + email);
        System.out.println("  Address         : " + address);
    }

    /**
     * Overriding Object.toString(). Without this, printing a Student gives
     * something like com.hostel.model.Student@1b6d3586 which helps nobody.
     */
    @Override
    public String toString() {
        return "Student{id=" + studentId + ", regNo='" + registerNumber
                + "', name='" + name + "', dept='" + department
                + "', year=" + year + "}";
    }
}
