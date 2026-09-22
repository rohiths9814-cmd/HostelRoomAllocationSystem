package com.hostel.dao;

import com.hostel.model.Allocation;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * All SQL for the allocations table.
 *
 * Several methods take a Connection parameter. Those run inside the
 * allocation / transfer / checkout TRANSACTIONS, which must use one single
 * connection from BEGIN to COMMIT.
 */
public class AllocationDAO {

    /**
     * SQL DATE columns can be NULL (checkout_date is empty while the student
     * is still living there). rs.getDate() returns null in that case, and
     * calling .toLocalDate() on null would throw NullPointerException,
     * so every date read goes through this helper.
     */
    private LocalDate toLocalDate(Date sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        return sqlDate.toLocalDate();
    }

    private Allocation buildAllocation(ResultSet rs) throws SQLException {
        return new Allocation(
                rs.getInt("allocation_id"),
                rs.getInt("student_id"),
                rs.getInt("room_id"),
                toLocalDate(rs.getDate("allocation_date")),
                toLocalDate(rs.getDate("checkout_date")),
                rs.getString("status"));
    }

    /** Same as above, plus the joined student and room columns for display. */
    private Allocation buildDetailedAllocation(ResultSet rs) throws SQLException {
        Allocation allocation = buildAllocation(rs);
        allocation.setRegisterNumber(rs.getString("register_number"));
        allocation.setStudentName(rs.getString("name"));
        allocation.setRoomNumber(rs.getString("room_number"));
        allocation.setBlockName(rs.getString("block_name"));
        return allocation;
    }

    /** The SELECT used by every "with details" method. */
    private String detailSelect() {
        return "SELECT a.*, s.register_number, s.name, r.room_number, b.block_name "
             + "FROM allocations a "
             + "JOIN students s ON a.student_id = s.student_id "
             + "JOIN rooms r ON a.room_id = r.room_id "
             + "JOIN hostel_blocks b ON r.block_id = b.block_id ";
    }

    // ==========================================================
    // INSERT  (transaction version)
    // ==========================================================

    public int insert(Connection con, Allocation allocation) throws SQLException {
        String sql = "INSERT INTO allocations "
                + "(student_id, room_id, allocation_date, checkout_date, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, allocation.getStudentId());
            ps.setInt(2, allocation.getRoomId());
            ps.setDate(3, Date.valueOf(allocation.getAllocationDate()));

            if (allocation.getCheckoutDate() == null) {
                ps.setNull(4, java.sql.Types.DATE);       // how to store a real NULL
            } else {
                ps.setDate(4, Date.valueOf(allocation.getCheckoutDate()));
            }
            ps.setString(5, allocation.getStatus());

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

    // ==========================================================
    // UPDATE  (transaction versions)
    // ==========================================================

    /** Ends an allocation: status becomes CHECKED_OUT or TRANSFERRED. */
    public boolean closeAllocation(Connection con, int allocationId,
                                   String newStatus, LocalDate endDate) throws SQLException {
        String sql = "UPDATE allocations SET status = ?, checkout_date = ? "
                   + "WHERE allocation_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setDate(2, Date.valueOf(endDate));
            ps.setInt(3, allocationId);
            return ps.executeUpdate() > 0;
        }
    }

    // ==========================================================
    // SELECT
    // ==========================================================

    /** The one ACTIVE allocation of a student, or null if they have no room. */
    public Allocation findActiveByStudentId(int studentId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return findActiveByStudentId(con, studentId);
        }
    }

    public Allocation findActiveByStudentId(Connection con, int studentId)
            throws SQLException {
        String sql = "SELECT * FROM allocations WHERE student_id = ? AND status = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, Allocation.ACTIVE);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildAllocation(rs);
                }
            }
        }
        return null;
    }

    public Allocation findById(int allocationId) throws SQLException {
        String sql = detailSelect() + "WHERE a.allocation_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, allocationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildDetailedAllocation(rs);
                }
            }
        }
        return null;
    }

    /** Every allocation ever made, newest first. This is the stay history. */
    public ArrayList<Allocation> findAll() throws SQLException {
        String sql = detailSelect() + "ORDER BY a.allocation_id DESC";
        return runDetailQuery(sql, null);
    }

    /** Only the students currently living in the hostel. */
    public ArrayList<Allocation> findAllActive() throws SQLException {
        String sql = detailSelect() + "WHERE a.status = ? ORDER BY r.room_number";
        return runDetailQuery(sql, Allocation.ACTIVE);
    }

    /** The full history of one student, including transfers and checkouts. */
    public ArrayList<Allocation> findByStudentId(int studentId) throws SQLException {
        ArrayList<Allocation> list = new ArrayList<>();
        String sql = detailSelect() + "WHERE a.student_id = ? ORDER BY a.allocation_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildDetailedAllocation(rs));
                }
            }
        }
        return list;
    }

    /** Who is living in one particular room right now. */
    public ArrayList<Allocation> findActiveByRoomId(int roomId) throws SQLException {
        ArrayList<Allocation> list = new ArrayList<>();
        String sql = detailSelect() + "WHERE a.room_id = ? AND a.status = ? "
                   + "ORDER BY s.register_number";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setString(2, Allocation.ACTIVE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildDetailedAllocation(rs));
                }
            }
        }
        return list;
    }

    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM allocations WHERE status = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, Allocation.ACTIVE);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /** Shared code for the detail queries that take zero or one parameter. */
    private ArrayList<Allocation> runDetailQuery(String sql, String parameter)
            throws SQLException {
        ArrayList<Allocation> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (parameter != null) {
                ps.setString(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildDetailedAllocation(rs));
                }
            }
        }
        return list;
    }
}
