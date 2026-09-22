package com.hostel.service;

import com.hostel.annotation.AdminOperation;
import com.hostel.dao.AllocationDAO;
import com.hostel.dao.BlockDAO;
import com.hostel.dao.RoomDAO;
import com.hostel.dao.StudentDAO;
import com.hostel.dao.WaitingListDAO;
import com.hostel.exception.AllocationException;
import com.hostel.exception.RoomFullException;
import com.hostel.exception.RoomNotFoundException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.Allocation;
import com.hostel.model.HostelBlock;
import com.hostel.model.Room;
import com.hostel.model.Student;
import com.hostel.model.WaitingEntry;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * THE MAIN MODULE - and the reason this project needs TRANSACTIONS.
 *
 * Allocating a room is not one change, it is TWO:
 *      1. insert a row into allocations
 *      2. increase rooms.occupied_beds
 *
 * If step 1 works and the program crashes before step 2, the database is
 * left lying: a student is recorded in a room whose bed count says it is
 * empty. Later, that bed gets given to somebody else.
 *
 * A transaction makes the two changes ATOMIC - all of them happen, or none
 * of them do. The pattern used in every method below is always the same:
 *
 *      con.setAutoCommit(false);     // start the transaction
 *      ... do all the steps ...
 *      con.commit();                 // make it permanent
 *   catch:
 *      con.rollback();               // undo everything
 *   finally:
 *      con.setAutoCommit(true);
 *      con.close();
 *
 * By default JDBC runs in auto-commit mode, where every single statement
 * commits itself immediately. setAutoCommit(false) is what turns several
 * statements into one unit of work.
 */
public class AllocationService {

    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final BlockDAO blockDAO = new BlockDAO();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();

    // ==============================================================
    // MODULE 5 - ALLOCATE A ROOM
    // ==============================================================

    @AdminOperation("Allocate a room to a student")
    public Allocation allocateRoom(int studentId, int roomId)
            throws StudentNotFoundException, RoomNotFoundException,
                   RoomFullException, AllocationException, SQLException {

        // ---------- checks that need no transaction ----------
        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException("No student with ID " + studentId);
        }

        assert student != null : "student became null after the lookup";

        if (allocationDAO.findActiveByStudentId(studentId) != null) {
            throw new AllocationException(
                    student.getName() + " already has an active room allocation. "
                    + "Use Room Transfer instead.");
        }

        // ---------- the transaction ----------
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);              // ---- BEGIN TRANSACTION ----

            Room room = roomDAO.findById(con, roomId);
            if (room == null) {
                throw new RoomNotFoundException("No room with ID " + roomId);
            }

            assert room.getOccupiedBeds() <= room.getCapacity()
                    : "Database already holds a corrupt room: " + room.getRoomNumber();

            if (Room.MAINTENANCE.equals(room.getStatus())) {
                throw new RoomFullException(
                        "Room " + room.getRoomNumber() + " is under maintenance.");
            }
            if (!room.isAvailable()) {
                throw new RoomFullException(
                        "Room " + room.getRoomNumber() + " is FULL ("
                        + room.getOccupiedBeds() + "/" + room.getCapacity()
                        + " beds used). Choose another room or use the waiting list.");
            }

            checkGenderMatches(student, room);

            // STEP 1 - one more bed is taken
            room.occupyOneBed();                   // also refreshes the status
            roomDAO.updateOccupancy(con, room.getRoomId(),
                    room.getOccupiedBeds(), room.getStatus());

            // STEP 2 - record the allocation
            Allocation allocation = new Allocation(studentId, roomId);
            int allocationId = allocationDAO.insert(con, allocation);
            allocation.setAllocationId(allocationId);

            // STEP 3 - if the student was queued, take them off the waiting list
            WaitingEntry waiting = waitingListDAO.findWaitingByStudentId(studentId);
            if (waiting != null) {
                waitingListDAO.updateStatus(con, waiting.getWaitingId(),
                        WaitingEntry.ALLOCATED);
            }

            con.commit();                          // ---- COMMIT ----

            allocation.setRegisterNumber(student.getRegisterNumber());
            allocation.setStudentName(student.getName());
            allocation.setRoomNumber(room.getRoomNumber());
            return allocation;

        } catch (SQLException e) {
            rollbackQuietly(con);
            throw e;
        } catch (RoomFullException | RoomNotFoundException | AllocationException e) {
            rollbackQuietly(con);
            throw e;
        } finally {
            closeQuietly(con);
        }
    }

    // ==============================================================
    // MODULE 6 - TRANSFER A STUDENT TO ANOTHER ROOM
    // ==============================================================

    /**
     * Four changes, all inside one transaction:
     *   1. old allocation becomes TRANSFERRED
     *   2. old room frees one bed
     *   3. new room takes one bed
     *   4. a new ACTIVE allocation row is inserted
     *
     * The old row is kept, so the allocations table is also the transfer
     * history of every student.
     */
    @AdminOperation("Transfer a student to a different room")
    public Allocation transferStudent(int studentId, int newRoomId)
            throws StudentNotFoundException, RoomNotFoundException,
                   RoomFullException, AllocationException, SQLException {

        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException("No student with ID " + studentId);
        }

        Allocation current = allocationDAO.findActiveByStudentId(studentId);
        if (current == null) {
            throw new AllocationException(
                    student.getName() + " does not have a room yet. Allocate one first.");
        }
        if (current.getRoomId() == newRoomId) {
            throw new AllocationException("The student is already in that room.");
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);              // ---- BEGIN TRANSACTION ----

            Room oldRoom = roomDAO.findById(con, current.getRoomId());
            Room newRoom = roomDAO.findById(con, newRoomId);

            if (newRoom == null) {
                throw new RoomNotFoundException("No room with ID " + newRoomId);
            }
            if (oldRoom == null) {
                throw new RoomNotFoundException(
                        "The current room record is missing. Database is inconsistent.");
            }
            if (!newRoom.isAvailable()) {
                throw new RoomFullException(
                        "Room " + newRoom.getRoomNumber() + " is not available ("
                        + newRoom.getStatus() + ").");
            }

            checkGenderMatches(student, newRoom);

            // 1. close the old allocation
            allocationDAO.closeAllocation(con, current.getAllocationId(),
                    Allocation.TRANSFERRED, LocalDate.now());

            // 2. free the bed in the old room
            oldRoom.freeOneBed();
            roomDAO.updateOccupancy(con, oldRoom.getRoomId(),
                    oldRoom.getOccupiedBeds(), oldRoom.getStatus());

            // 3. take a bed in the new room
            newRoom.occupyOneBed();
            roomDAO.updateOccupancy(con, newRoom.getRoomId(),
                    newRoom.getOccupiedBeds(), newRoom.getStatus());

            // 4. new allocation row
            Allocation fresh = new Allocation(studentId, newRoomId);
            fresh.setAllocationId(allocationDAO.insert(con, fresh));

            assert oldRoom.getOccupiedBeds() >= 0 : "old room went negative";
            assert newRoom.getOccupiedBeds() <= newRoom.getCapacity()
                    : "new room overflowed";

            con.commit();                          // ---- COMMIT ----

            fresh.setRegisterNumber(student.getRegisterNumber());
            fresh.setStudentName(student.getName());
            fresh.setRoomNumber(newRoom.getRoomNumber());
            System.out.println("Moved out of : " + oldRoom.getRoomNumber()
                    + "  (now " + oldRoom.getOccupiedBeds() + "/"
                    + oldRoom.getCapacity() + ")");
            return fresh;

        } catch (SQLException e) {
            rollbackQuietly(con);
            throw e;
        } catch (RoomFullException | RoomNotFoundException | AllocationException e) {
            rollbackQuietly(con);
            throw e;
        } finally {
            closeQuietly(con);
        }
    }

    // ==============================================================
    // MODULE 7 - CHECKOUT
    // ==============================================================

    /**
     * Two changes inside one transaction:
     *   1. the allocation becomes CHECKED_OUT with today as checkout_date
     *   2. the room frees one bed and its status is recalculated
     */
    @AdminOperation("Check a student out of the hostel")
    public boolean checkoutStudent(int studentId)
            throws StudentNotFoundException, AllocationException, SQLException {

        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException("No student with ID " + studentId);
        }

        Allocation current = allocationDAO.findActiveByStudentId(studentId);
        if (current == null) {
            throw new AllocationException(
                    student.getName() + " is not currently allocated to any room.");
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);              // ---- BEGIN TRANSACTION ----

            Room room = roomDAO.findById(con, current.getRoomId());
            if (room == null) {
                throw new AllocationException("The room record is missing.");
            }

            // 1. close the allocation
            allocationDAO.closeAllocation(con, current.getAllocationId(),
                    Allocation.CHECKED_OUT, LocalDate.now());

            // 2. free the bed
            room.freeOneBed();
            roomDAO.updateOccupancy(con, room.getRoomId(),
                    room.getOccupiedBeds(), room.getStatus());

            assert room.getOccupiedBeds() >= 0
                    : "checkout drove occupied beds below zero in " + room.getRoomNumber();

            con.commit();                          // ---- COMMIT ----

            System.out.println("Room " + room.getRoomNumber() + " now has "
                    + room.getAvailableBeds() + " free bed(s). Status: "
                    + room.getStatus());
            return true;

        } catch (SQLException e) {
            rollbackQuietly(con);
            throw e;
        } catch (AllocationException e) {
            rollbackQuietly(con);
            throw e;
        } finally {
            closeQuietly(con);
        }
    }

    // ==============================================================
    // READ-ONLY HELPERS
    // ==============================================================

    public ArrayList<Allocation> getActiveAllocations() throws SQLException {
        return allocationDAO.findAllActive();
    }

    public ArrayList<Allocation> getAllAllocations() throws SQLException {
        return allocationDAO.findAll();
    }

    public ArrayList<Allocation> getStudentHistory(int studentId) throws SQLException {
        return allocationDAO.findByStudentId(studentId);
    }

    public ArrayList<Allocation> getRoomOccupants(int roomId) throws SQLException {
        return allocationDAO.findActiveByRoomId(roomId);
    }

    public int countActiveAllocations() throws SQLException {
        return allocationDAO.countActive();
    }

    // ==============================================================
    // PRIVATE HELPERS
    // ==============================================================

    /** A boy may not be put into a girls block and vice versa. */
    private void checkGenderMatches(Student student, Room room)
            throws AllocationException, SQLException {

        HostelBlock block = blockDAO.findById(room.getBlockId());
        if (block == null) {
            return;                  // no block information, skip the rule
        }
        if (!block.getGender().equalsIgnoreCase(student.getGender())) {
            throw new AllocationException(
                    "Room " + room.getRoomNumber() + " is in " + block.getBlockName()
                    + ", which is reserved for " + block.getGender() + " students. "
                    + student.getName() + " is " + student.getGender() + ".");
        }
    }

    /**
     * Undoes everything done since setAutoCommit(false).
     * It is wrapped in its own try/catch because rollback() can itself
     * fail (for example if the connection already dropped), and we must
     * not lose the ORIGINAL exception because of a second one.
     */
    private void rollbackQuietly(Connection con) {
        if (con == null) {
            return;
        }
        try {
            con.rollback();
            System.out.println("[TRANSACTION ROLLED BACK - no changes were saved]");
        } catch (SQLException e) {
            System.out.println("Rollback failed: " + e.getMessage());
        }
    }

    /** Restores auto-commit and closes. Runs in finally, so it ALWAYS runs. */
    private void closeQuietly(Connection con) {
        if (con == null) {
            return;
        }
        try {
            con.setAutoCommit(true);
            con.close();
        } catch (SQLException e) {
            System.out.println("Could not close the connection: " + e.getMessage());
        }
    }
}
