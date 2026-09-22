package com.hostel.dao;

import com.hostel.model.Complaint;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

/** All SQL for the complaints table. */
public class ComplaintDAO {

    private LocalDate toLocalDate(Date sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        return sqlDate.toLocalDate();
    }

    private Complaint buildComplaint(ResultSet rs) throws SQLException {
        Complaint complaint = new Complaint(
                rs.getInt("complaint_id"),
                rs.getInt("student_id"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("status"),
                toLocalDate(rs.getDate("created_date")),
                toLocalDate(rs.getDate("resolved_date")));
        complaint.setRegisterNumber(rs.getString("register_number"));
        complaint.setStudentName(rs.getString("name"));
        return complaint;
    }

    private String detailSelect() {
        return "SELECT c.*, s.register_number, s.name FROM complaints c "
             + "JOIN students s ON c.student_id = s.student_id ";
    }

    public int insert(Complaint complaint) throws SQLException {
        String sql = "INSERT INTO complaints "
                + "(student_id, category, description, status, created_date, resolved_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, complaint.getStudentId());
            ps.setString(2, complaint.getCategory());
            ps.setString(3, complaint.getDescription());
            ps.setString(4, complaint.getStatus());
            ps.setDate(5, Date.valueOf(complaint.getCreatedDate()));
            ps.setNull(6, java.sql.Types.DATE);       // not resolved yet

            if (ps.executeUpdate() == 0) {
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

    public Complaint findById(int complaintId) throws SQLException {
        String sql = detailSelect() + "WHERE c.complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildComplaint(rs);
                }
            }
        }
        return null;
    }

    public ArrayList<Complaint> findAll() throws SQLException {
        String sql = detailSelect() + "ORDER BY c.complaint_id DESC";
        return runQuery(sql, null);
    }

    public ArrayList<Complaint> findByStatus(String status) throws SQLException {
        String sql = detailSelect() + "WHERE c.status = ? ORDER BY c.complaint_id DESC";
        return runQuery(sql, status);
    }

    public ArrayList<Complaint> findByStudentId(int studentId) throws SQLException {
        ArrayList<Complaint> list = new ArrayList<>();
        String sql = detailSelect() + "WHERE c.student_id = ? ORDER BY c.complaint_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildComplaint(rs));
                }
            }
        }
        return list;
    }

    /**
     * Changes the status. When it becomes RESOLVED we also stamp the date,
     * otherwise we clear it again (in case an admin moves it back).
     */
    public boolean updateStatus(int complaintId, String newStatus) throws SQLException {
        String sql = "UPDATE complaints SET status = ?, resolved_date = ? "
                   + "WHERE complaint_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            if (Complaint.RESOLVED.equals(newStatus)) {
                ps.setDate(2, Date.valueOf(LocalDate.now()));
            } else {
                ps.setNull(2, java.sql.Types.DATE);
            }
            ps.setInt(3, complaintId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int complaintId) throws SQLException {
        String sql = "DELETE FROM complaints WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, complaintId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * How many complaints of each status, and of each category.
     *
     * A HashMap is perfect here: the label is the key, the count is the
     * value, and GROUP BY in SQL already produced exactly those pairs.
     * The Integer values are autoboxed from the int returned by getInt().
     */
    public HashMap<String, Integer> countByStatus() throws SQLException {
        return groupCount("SELECT status, COUNT(*) FROM complaints GROUP BY status");
    }

    public HashMap<String, Integer> countByCategory() throws SQLException {
        return groupCount("SELECT category, COUNT(*) FROM complaints GROUP BY category");
    }

    private HashMap<String, Integer> groupCount(String sql) throws SQLException {
        HashMap<String, Integer> counts = new HashMap<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString(1), rs.getInt(2));
            }
        }
        return counts;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM complaints";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private ArrayList<Complaint> runQuery(String sql, String parameter) throws SQLException {
        ArrayList<Complaint> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (parameter != null) {
                ps.setString(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildComplaint(rs));
                }
            }
        }
        return list;
    }
}
