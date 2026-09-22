package com.hostel.service;

import com.hostel.annotation.AdminOperation;
import com.hostel.dao.AllocationDAO;
import com.hostel.dao.StudentDAO;
import com.hostel.exception.DuplicateRegisterNumberException;
import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.Student;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

/** Business rules for the student module. */
public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();

    /**
     * Adds a student.
     *
     * Order of the checks matters:
     *   1. validate()  - regex, cheap, no database needed
     *   2. duplicate   - one query, using a HashSet for O(1) lookups
     *   3. insert      - only now do we touch the table
     *
     * The register_number column is ALSO declared UNIQUE in MySQL. That is
     * deliberate: the Java check is for a friendly message, the database
     * constraint is the guarantee that can never be bypassed.
     */
    @AdminOperation("Add a new student record")
    public int addStudent(Student student)
            throws InvalidStudentDataException, DuplicateRegisterNumberException,
                   SQLException {

        assert student != null : "addStudent received a null Student";

        student.validate();

        HashSet<String> existing = studentDAO.findAllRegisterNumbers();
        if (existing.contains(student.getRegisterNumber())) {
            throw new DuplicateRegisterNumberException(
                    "Register number " + student.getRegisterNumber()
                    + " already exists. Every student must have a unique one.");
        }

        int newId = studentDAO.insert(student);
        student.setStudentId(newId);
        return newId;
    }

    /** Throws instead of returning null, so the caller cannot forget to check. */
    public Student getStudentById(int studentId)
            throws StudentNotFoundException, SQLException {

        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException("No student found with ID " + studentId);
        }
        return student;
    }

    public Student getStudentByRegisterNumber(String registerNumber)
            throws StudentNotFoundException, SQLException {

        Student student = studentDAO.findByRegisterNumber(registerNumber);
        if (student == null) {
            throw new StudentNotFoundException(
                    "No student found with register number " + registerNumber);
        }
        return student;
    }

    public ArrayList<Student> getAllStudents() throws SQLException {
        return studentDAO.findAll();
    }

    public ArrayList<Student> searchStudents(String keyword) throws SQLException {
        return studentDAO.searchByNameOrRegister(keyword);
    }

    public ArrayList<Student> getStudentsWithoutRoom() throws SQLException {
        return studentDAO.findStudentsWithoutRoom();
    }

    @AdminOperation("Update an existing student record")
    public boolean updateStudent(Student student)
            throws InvalidStudentDataException, StudentNotFoundException, SQLException {

        assert student != null : "updateStudent received a null Student";

        if (studentDAO.findById(student.getStudentId()) == null) {
            throw new StudentNotFoundException(
                    "Cannot update: no student with ID " + student.getStudentId());
        }

        student.validate();
        return studentDAO.update(student);
    }

    /**
     * Deletes a student.
     *
     * A student who still holds an ACTIVE room may not be deleted - the
     * allocations row points at them through a FOREIGN KEY, and deleting
     * the parent row would break that link. MySQL would refuse anyway;
     * we check first so the user gets a sentence instead of a stack trace.
     */
    @AdminOperation("Delete a student record")
    public boolean deleteStudent(int studentId)
            throws StudentNotFoundException, SQLException {

        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException(
                    "Cannot delete: no student with ID " + studentId);
        }

        if (allocationDAO.findActiveByStudentId(studentId) != null) {
            System.out.println("This student is still living in a room.");
            System.out.println("Check the student out first, then delete the record.");
            return false;
        }

        return studentDAO.delete(studentId);
    }

    public int countStudents() throws SQLException {
        return studentDAO.countAll();
    }
}
