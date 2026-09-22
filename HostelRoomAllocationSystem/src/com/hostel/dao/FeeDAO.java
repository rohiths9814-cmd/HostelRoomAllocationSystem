package com.hostel.dao;

import com.hostel.model.HostelFee;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

/** All SQL for the hostel_fees table. */
public class FeeDAO {

    private LocalDate toLocalDate(Date sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        return sqlDate.toLocalDate();
    }

    private HostelFee buildFee(ResultSet rs) throws SQLException {
        HostelFee fee = new HostelFee(
                rs.getInt("fee_id"),
                rs.getInt("student_id"),
                rs.getDouble("amount"),
                toLocalDate(rs.getDate("payment_date")),
                rs.getString("status"));
        fee.setRegisterNumber(rs.getString("register_number"));
        fee.setStudentName(rs.getString("name"));
        return fee;
    }

    private String detailSelect() {
        return "SELECT f.*, s.register_number, s.name FROM hostel_fees f "
             + "JOIN students s ON f.student_id = s.student_id ";
    }

    public int insert(HostelFee fee) throws SQLException {
        String sql = "INSERT INTO hostel_fees (student_id, amount, payment_date, status) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, fee.getStudentId());
            ps.setDouble(2, fee.getAmount());

            if (fee.getPaymentDate() == null) {
                ps.setNull(3, java.sql.Types.DATE);
            } else {
                ps.setDate(3, Date.valueOf(fee.getPaymentDate()));
            }
            ps.setString(4, fee.getStatus());

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

    public HostelFee findById(int feeId) throws SQLException {
        String sql = detailSelect() + "WHERE f.fee_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, feeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildFee(rs);
                }
            }
        }
        return null;
    }

    public ArrayList<HostelFee> findAll() throws SQLException {
        String sql = detailSelect() + "ORDER BY f.fee_id";
        return runQuery(sql, 0, null);
    }

    public ArrayList<HostelFee> findByStatus(String status) throws SQLException {
        String sql = detailSelect() + "WHERE f.status = ? ORDER BY f.fee_id";
        return runQuery(sql, 0, status);
    }

    /** Payment history of one student. */
    public ArrayList<HostelFee> findByStudentId(int studentId) throws SQLException {
        String sql = detailSelect() + "WHERE f.student_id = ? ORDER BY f.fee_id";
        return runQuery(sql, studentId, null);
    }

    /** Marks a fee as PAID and stamps today as the payment date. */
    public boolean markPaid(int feeId) throws SQLException {
        String sql = "UPDATE hostel_fees SET status = ?, payment_date = ? "
                   + "WHERE fee_id = ? AND status = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, HostelFee.PAID);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setInt(3, feeId);
            ps.setString(4, HostelFee.PENDING);    // refuse to pay an already paid fee

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int feeId) throws SQLException {
        String sql = "DELETE FROM hostel_fees WHERE fee_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, feeId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Sum of every unpaid fee, for the reports module. */
    public double totalPendingAmount() throws SQLException {
        String sql = "SELECT SUM(amount) FROM hostel_fees WHERE status = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, HostelFee.PENDING);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public double totalCollectedAmount() throws SQLException {
        String sql = "SELECT SUM(amount) FROM hostel_fees WHERE status = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, HostelFee.PAID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public int countByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM hostel_fees WHERE status = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * One helper for three queries. If intParam is greater than zero it is
     * bound as parameter 1, otherwise stringParam is bound (or nothing).
     */
    private ArrayList<HostelFee> runQuery(String sql, int intParam, String stringParam)
            throws SQLException {
        ArrayList<HostelFee> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (intParam > 0) {
                ps.setInt(1, intParam);
            } else if (stringParam != null) {
                ps.setString(1, stringParam);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildFee(rs));
                }
            }
        }
        return list;
    }
}
