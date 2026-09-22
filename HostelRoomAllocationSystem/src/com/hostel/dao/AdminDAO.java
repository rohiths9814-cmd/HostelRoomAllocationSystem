package com.hostel.dao;

import com.hostel.model.Admin;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database access for the admins table.
 *
 * A DAO (Data Access Object) contains SQL and NOTHING ELSE.
 * No menus, no Scanner, no business rules. That separation means the SQL
 * can be changed without touching the rest of the program.
 */
public class AdminDAO {

    /**
     * Looks for an admin with this username AND password.
     * Returns the Admin object, or null if the credentials are wrong.
     *
     * Note the "?" placeholders. With a plain Statement we would build the
     * SQL by joining strings, and a user could type
     *        anything' OR '1'='1
     * as the password and log in. PreparedStatement sends the values
     * SEPARATELY from the SQL text, so they can never become SQL commands.
     * That is how SQL INJECTION is prevented.
     */
    public Admin findByUsernameAndPassword(String username, String password)
            throws SQLException {

        String sql = "SELECT admin_id, username, password FROM admins "
                   + "WHERE username = ? AND password = ?";

        // try-with-resources: everything opened in the ( ) is closed
        // automatically at the end of the block, even if an exception is thrown.
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);     // index starts at 1, not 0
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {           // next() moves to the first row
                    return new Admin(
                            rs.getInt("admin_id"),
                            rs.getString("username"),
                            rs.getString("password"));
                }
            }
        }
        return null;                       // no matching row = login failed
    }

    /** Used to show how many admin accounts exist. */
    public int countAdmins() throws SQLException {
        String sql = "SELECT COUNT(*) FROM admins";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
