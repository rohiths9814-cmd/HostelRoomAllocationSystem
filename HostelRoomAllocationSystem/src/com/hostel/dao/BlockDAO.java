package com.hostel.dao;

import com.hostel.model.HostelBlock;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/** All SQL for the hostel_blocks table. */
public class BlockDAO {

    private HostelBlock buildBlock(ResultSet rs) throws SQLException {
        return new HostelBlock(
                rs.getInt("block_id"),
                rs.getString("block_name"),
                rs.getString("gender"),
                rs.getInt("floors"));
    }

    public int insert(HostelBlock block) throws SQLException {
        String sql = "INSERT INTO hostel_blocks (block_name, gender, floors) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, block.getBlockName());
            ps.setString(2, block.getGender());
            ps.setInt(3, block.getFloors());

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

    public HostelBlock findById(int blockId) throws SQLException {
        String sql = "SELECT * FROM hostel_blocks WHERE block_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, blockId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildBlock(rs);
                }
            }
        }
        return null;
    }

    public ArrayList<HostelBlock> findAll() throws SQLException {
        ArrayList<HostelBlock> blocks = new ArrayList<>();
        String sql = "SELECT * FROM hostel_blocks ORDER BY block_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                blocks.add(buildBlock(rs));
            }
        }
        return blocks;
    }

    /**
     * Every block, keyed by its id.
     *
     * HashMap is used because reports need to turn a block_id into a block
     * NAME again and again. Looking that up in a HashMap is O(1); searching
     * an ArrayList every time would be O(n) per lookup.
     *
     * Integer (the wrapper) is the key, not int, because a collection can
     * only hold objects. Writing map.get(3) works anyway thanks to
     * AUTOBOXING: the compiler quietly turns 3 into Integer.valueOf(3).
     */
    public HashMap<Integer, HostelBlock> findAllAsMap() throws SQLException {
        HashMap<Integer, HostelBlock> map = new HashMap<>();
        String sql = "SELECT * FROM hostel_blocks";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                HostelBlock block = buildBlock(rs);
                map.put(block.getBlockId(), block);   // int key -> autoboxed to Integer
            }
        }
        return map;
    }

    public boolean update(HostelBlock block) throws SQLException {
        String sql = "UPDATE hostel_blocks SET block_name = ?, gender = ?, floors = ? "
                   + "WHERE block_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, block.getBlockName());
            ps.setString(2, block.getGender());
            ps.setInt(3, block.getFloors());
            ps.setInt(4, block.getBlockId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int blockId) throws SQLException {
        String sql = "DELETE FROM hostel_blocks WHERE block_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, blockId);
            return ps.executeUpdate() > 0;
        }
    }

    /** How many rooms point at this block. A block with rooms cannot be deleted. */
    public int countRoomsInBlock(int blockId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE block_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, blockId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) FROM hostel_blocks";
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
