package com.hostel.dao;

import com.hostel.model.Room;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * All SQL for the rooms table.
 *
 * Notice that some methods take a Connection as their first parameter.
 * Those are the ones used INSIDE a transaction: the service layer opens one
 * connection, turns auto-commit off, and every step must run on that SAME
 * connection or it would not be part of the same transaction.
 */
public class RoomDAO {

    private Room buildRoom(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_id"),
                rs.getString("room_number"),
                rs.getInt("block_id"),
                rs.getInt("floor"),
                rs.getInt("capacity"),
                rs.getInt("occupied_beds"),
                rs.getString("status"));
    }

    public int insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms "
                + "(room_number, block_id, floor, capacity, occupied_beds, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, room.getBlockId());
            ps.setInt(3, room.getFloor());
            ps.setInt(4, room.getCapacity());
            ps.setInt(5, room.getOccupiedBeds());
            ps.setString(6, room.getStatus());

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

    public Room findById(int roomId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return findById(con, roomId);
        }
    }

    /** The transaction-safe version: reuses a connection given by the caller. */
    public Room findById(Connection con, int roomId) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildRoom(rs);
                }
            }
        }
        return null;
    }

    public Room findByRoomNumber(String roomNumber) throws SQLException {
        String sql = "SELECT * FROM rooms WHERE room_number = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildRoom(rs);
                }
            }
        }
        return null;
    }

    public ArrayList<Room> findAll() throws SQLException {
        ArrayList<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY block_id, room_number";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(buildRoom(rs));
            }
        }
        return rooms;
    }

    /** Rooms filtered by status: AVAILABLE / PARTIALLY_OCCUPIED / FULL / MAINTENANCE. */
    public ArrayList<Room> findByStatus(String status) throws SQLException {
        ArrayList<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE status = ? ORDER BY block_id, room_number";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(buildRoom(rs));
                }
            }
        }
        return rooms;
    }

    /** Rooms that still have a free bed and are not under maintenance. */
    public ArrayList<Room> findRoomsWithFreeBed() throws SQLException {
        ArrayList<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE occupied_beds < capacity "
                   + "AND status <> ? ORDER BY block_id, room_number";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, Room.MAINTENANCE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(buildRoom(rs));
                }
            }
        }
        return rooms;
    }

    /**
     * Free rooms that belong to a block reserved for this gender.
     * Boys must not be put into a girls block, so the allocation menu
     * uses this instead of findRoomsWithFreeBed().
     */
    public ArrayList<Room> findRoomsWithFreeBedForGender(String gender) throws SQLException {
        ArrayList<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.* FROM rooms r "
                   + "JOIN hostel_blocks b ON r.block_id = b.block_id "
                   + "WHERE r.occupied_beds < r.capacity AND r.status <> ? "
                   + "AND b.gender = ? ORDER BY r.block_id, r.room_number";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, Room.MAINTENANCE);
            ps.setString(2, gender);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(buildRoom(rs));
                }
            }
        }
        return rooms;
    }

    /**
     * Every room keyed by room_id, so the reports can answer
     * "which room is id 5?" instantly instead of searching a list.
     */
    public HashMap<Integer, Room> findAllAsMap() throws SQLException {
        HashMap<Integer, Room> map = new HashMap<>();
        String sql = "SELECT * FROM rooms";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Room room = buildRoom(rs);
                map.put(room.getRoomId(), room);
            }
        }
        return map;
    }

    public boolean update(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_number = ?, block_id = ?, floor = ?, "
                   + "capacity = ?, status = ? WHERE room_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, room.getBlockId());
            ps.setInt(3, room.getFloor());
            ps.setInt(4, room.getCapacity());
            ps.setString(5, room.getStatus());
            ps.setInt(6, room.getRoomId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Writes the new bed count and status. Used inside allocation,
     * transfer and checkout transactions, so it takes the shared Connection.
     */
    public boolean updateOccupancy(Connection con, int roomId, int occupiedBeds, String status)
            throws SQLException {
        String sql = "UPDATE rooms SET occupied_beds = ?, status = ? WHERE room_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, occupiedBeds);
            ps.setString(2, status);
            ps.setInt(3, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countAll() throws SQLException {
        return countWhere("SELECT COUNT(*) FROM rooms", null);
    }

    public int countByStatus(String status) throws SQLException {
        return countWhere("SELECT COUNT(*) FROM rooms WHERE status = ?", status);
    }

    /** Total beds currently occupied across the whole hostel. */
    public int totalOccupiedBeds() throws SQLException {
        String sql = "SELECT SUM(occupied_beds) FROM rooms";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /** Total beds that exist across the whole hostel. */
    public int totalBeds() throws SQLException {
        String sql = "SELECT SUM(capacity) FROM rooms";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /** Small helper so the four counting methods do not repeat the same code. */
    private int countWhere(String sql, String parameter) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (parameter != null) {
                ps.setString(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
