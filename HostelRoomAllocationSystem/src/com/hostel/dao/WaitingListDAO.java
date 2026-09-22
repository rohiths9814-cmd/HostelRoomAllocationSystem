package com.hostel.dao;

import com.hostel.model.WaitingEntry;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/** All SQL for the waiting_list table. */
public class WaitingListDAO {

    private WaitingEntry buildEntry(ResultSet rs) throws SQLException {
        WaitingEntry entry = new WaitingEntry(
                rs.getInt("waiting_id"),
                rs.getInt("student_id"),
                rs.getDate("request_date").toLocalDate(),
                rs.getInt("priority"),
                rs.getString("status"));
        entry.setRegisterNumber(rs.getString("register_number"));
        entry.setStudentName(rs.getString("name"));
        return entry;
    }

    private String detailSelect() {
        return "SELECT w.*, s.register_number, s.name FROM waiting_list w "
             + "JOIN students s ON w.student_id = s.student_id ";
    }

    public int insert(WaitingEntry entry) throws SQLException {
        String sql = "INSERT INTO waiting_list (student_id, request_date, priority, status) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, entry.getStudentId());
            ps.setDate(2, Date.valueOf(entry.getRequestDate()));
            ps.setInt(3, entry.getPriority());
            ps.setString(4, entry.getStatus());

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

    /**
     * Everybody still WAITING, already ordered by priority then by date.
     * The service layer copies this into a LinkedList and a PriorityQueue
     * so the two collections can be compared in the demo.
     */
    public ArrayList<WaitingEntry> findWaiting() throws SQLException {
        ArrayList<WaitingEntry> list = new ArrayList<>();
        String sql = detailSelect() + "WHERE w.status = ? "
                   + "ORDER BY w.priority ASC, w.request_date ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, WaitingEntry.WAITING);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildEntry(rs));
                }
            }
        }
        return list;
    }

    public WaitingEntry findById(int waitingId) throws SQLException {
        String sql = detailSelect() + "WHERE w.waiting_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, waitingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildEntry(rs);
                }
            }
        }
        return null;
    }

    /** Is this student already queued and still waiting? */
    public WaitingEntry findWaitingByStudentId(int studentId) throws SQLException {
        String sql = detailSelect() + "WHERE w.student_id = ? AND w.status = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setString(2, WaitingEntry.WAITING);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildEntry(rs);
                }
            }
        }
        return null;
    }

    public boolean updateStatus(int waitingId, String newStatus) throws SQLException {
        String sql = "UPDATE waiting_list SET status = ? WHERE waiting_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, waitingId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Transaction version: used when allocation and de-queue must succeed together. */
    public boolean updateStatus(Connection con, int waitingId, String newStatus)
            throws SQLException {
        String sql = "UPDATE waiting_list SET status = ? WHERE waiting_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, waitingId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int waitingId) throws SQLException {
        String sql = "DELETE FROM waiting_list WHERE waiting_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, waitingId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countWaiting() throws SQLException {
        String sql = "SELECT COUNT(*) FROM waiting_list WHERE status = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, WaitingEntry.WAITING);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
