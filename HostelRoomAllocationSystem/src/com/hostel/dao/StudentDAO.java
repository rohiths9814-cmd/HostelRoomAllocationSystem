package com.hostel.dao;

import com.hostel.model.Allocation;
import com.hostel.model.Student;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

/** All SQL for the students table. */
public class StudentDAO {

    /**
     * Turns the CURRENT row of a ResultSet into a Student object.
     * Written once and reused by every select method below.
     */
    private Student buildStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("student_id"),
                rs.getString("register_number"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getInt("year"),
                rs.getString("gender"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("address"));
    }

    // ==========================================================
    // INSERT
    // ==========================================================

    /**
     * Inserts a student and returns the AUTO_INCREMENT id MySQL generated.
     * RETURN_GENERATED_KEYS asks the driver to hand that id back to us.
     */
    public int insert(Student student) throws SQLException {
        String sql = "INSERT INTO students "
                + "(register_number, name, department, year, gender, phone, email, address) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getRegisterNumber());
            ps.setString(2, student.getName());
            ps.setString(3, student.getDepartment());
            ps.setInt(4, student.getYear());
            ps.setString(5, student.getGender());
            ps.setString(6, student.getPhone());
            ps.setString(7, student.getEmail());
            ps.setString(8, student.getAddress());

            int rowsInserted = ps.executeUpdate();   // executeUpdate for INSERT/UPDATE/DELETE
            if (rowsInserted == 0) {
                return 0;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return 0;
    }

    // ==========================================================
    // SELECT
    // ==========================================================

    public Student findById(int studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildStudent(rs);
                }
            }
        }
        return null;
    }

    public Student findByRegisterNumber(String registerNumber) throws SQLException {
        String sql = "SELECT * FROM students WHERE register_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, registerNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildStudent(rs);
                }
            }
        }
        return null;
    }

    /**
     * Every student, ordered by id.
     *
     * ArrayList is the right collection here: we only ever add at the end
     * and then walk through the list in order to print it. ArrayList gives
     * O(1) access by index and is the cheapest list to iterate.
     */
    public ArrayList<Student> findAll() throws SQLException {
        ArrayList<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                students.add(buildStudent(rs));
            }
        }
        return students;
    }

    /** Partial search on name or register number. LIKE needs % around the text. */
    public ArrayList<Student> searchByNameOrRegister(String keyword) throws SQLException {
        ArrayList<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students "
                   + "WHERE name LIKE ? OR register_number LIKE ? ORDER BY name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(buildStudent(rs));
                }
            }
        }
        return students;
    }

    /**
     * Every register number already in use.
     *
     * HashSet is chosen because we ask it only one question: is this
     * register number already present?  HashSet.contains() is O(1) on
     * average, while ArrayList.contains() has to scan every element.
     * A HashSet also refuses to store the same value twice by definition.
     */
    public HashSet<String> findAllRegisterNumbers() throws SQLException {
        HashSet<String> registerNumbers = new HashSet<>();
        String sql = "SELECT register_number FROM students";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                registerNumbers.add(rs.getString("register_number"));
            }
        }
        return registerNumbers;
    }

    /** Students who do not currently hold an ACTIVE allocation. */
    public ArrayList<Student> findStudentsWithoutRoom() throws SQLException {
        ArrayList<Student> students = new ArrayList<>();
        String sql = "SELECT s.* FROM students s "
                   + "WHERE s.student_id NOT IN "
                   + "(SELECT a.student_id FROM allocations a WHERE a.status = ?) "
                   + "ORDER BY s.register_number";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, Allocation.ACTIVE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(buildStudent(rs));
                }
            }
        }
        return students;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // ==========================================================
    // UPDATE and DELETE
    // ==========================================================

    public boolean update(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, department = ?, year = ?, "
                   + "gender = ?, phone = ?, email = ?, address = ? "
                   + "WHERE student_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, student.getName());
            ps.setString(2, student.getDepartment());
            ps.setInt(3, student.getYear());
            ps.setString(4, student.getGender());
            ps.setString(5, student.getPhone());
            ps.setString(6, student.getEmail());
            ps.setString(7, student.getAddress());
            ps.setInt(8, student.getStudentId());

            return ps.executeUpdate() > 0;   // rows affected
        }
    }

    public boolean delete(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }
}
