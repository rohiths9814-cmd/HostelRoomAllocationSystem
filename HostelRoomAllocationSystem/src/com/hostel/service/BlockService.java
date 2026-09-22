package com.hostel.service;

import com.hostel.annotation.AdminOperation;
import com.hostel.dao.BlockDAO;
import com.hostel.exception.InvalidStudentDataException;
import com.hostel.model.HostelBlock;
import com.hostel.util.Validation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/** Business rules for hostel blocks (Block A, Block B, Block C...). */
public class BlockService {

    private final BlockDAO blockDAO = new BlockDAO();

    @AdminOperation("Add a hostel block")
    public int addBlock(HostelBlock block) throws InvalidStudentDataException, SQLException {

        assert block != null : "addBlock received a null HostelBlock";

        validateBlock(block);
        return blockDAO.insert(block);
    }

    public HostelBlock getBlockById(int blockId) throws SQLException {
        return blockDAO.findById(blockId);
    }

    public ArrayList<HostelBlock> getAllBlocks() throws SQLException {
        return blockDAO.findAll();
    }

    /** Used by reports to print a block NAME next to a room. */
    public HashMap<Integer, HostelBlock> getBlockMap() throws SQLException {
        return blockDAO.findAllAsMap();
    }

    @AdminOperation("Update a hostel block")
    public boolean updateBlock(HostelBlock block)
            throws InvalidStudentDataException, SQLException {

        validateBlock(block);
        return blockDAO.update(block);
    }

    /**
     * A block that still has rooms cannot be deleted, because
     * rooms.block_id is a FOREIGN KEY pointing at it.
     */
    @AdminOperation("Delete a hostel block")
    public boolean deleteBlock(int blockId) throws SQLException {

        int rooms = blockDAO.countRoomsInBlock(blockId);
        if (rooms > 0) {
            System.out.println("This block still has " + rooms + " room(s).");
            System.out.println("Delete or move those rooms first.");
            return false;
        }
        return blockDAO.delete(blockId);
    }

    public int countBlocks() throws SQLException {
        return blockDAO.countAll();
    }

    /** Shared validation, so add and update cannot drift apart. */
    private void validateBlock(HostelBlock block) throws InvalidStudentDataException {

        if (!Validation.isValidBlockName(block.getBlockName())) {
            throw new InvalidStudentDataException(
                    "Block name must start with a letter, 2-30 characters. Example: Block A");
        }
        if (!Validation.isValidGender(block.getGender())) {
            throw new InvalidStudentDataException("Block gender must be MALE or FEMALE.");
        }
        if (block.getFloors() < 1 || block.getFloors() > 10) {
            throw new InvalidStudentDataException("Number of floors must be between 1 and 10.");
        }
    }
}
