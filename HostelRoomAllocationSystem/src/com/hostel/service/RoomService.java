package com.hostel.service;

import com.hostel.annotation.AdminOperation;
import com.hostel.dao.AllocationDAO;
import com.hostel.dao.BlockDAO;
import com.hostel.dao.RoomDAO;
import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.RoomNotFoundException;
import com.hostel.model.Room;
import com.hostel.util.Validation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/** Business rules for the room module. */
public class RoomService {

    private final RoomDAO roomDAO = new RoomDAO();
    private final BlockDAO blockDAO = new BlockDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();

    @AdminOperation("Add a room to a block")
    public int addRoom(Room room) throws InvalidStudentDataException, SQLException {

        assert room != null : "addRoom received a null Room";

        validateRoom(room);

        if (blockDAO.findById(room.getBlockId()) == null) {
            throw new InvalidStudentDataException(
                    "Block ID " + room.getBlockId() + " does not exist.");
        }
        if (roomDAO.findByRoomNumber(room.getRoomNumber()) != null) {
            throw new InvalidStudentDataException(
                    "Room number " + room.getRoomNumber() + " already exists.");
        }
        return roomDAO.insert(room);
    }

    public Room getRoomById(int roomId) throws RoomNotFoundException, SQLException {
        Room room = roomDAO.findById(roomId);
        if (room == null) {
            throw new RoomNotFoundException("No room found with ID " + roomId);
        }
        return room;
    }

    public Room getRoomByNumber(String roomNumber)
            throws RoomNotFoundException, SQLException {
        Room room = roomDAO.findByRoomNumber(roomNumber);
        if (room == null) {
            throw new RoomNotFoundException("No room found with number " + roomNumber);
        }
        return room;
    }

    public ArrayList<Room> getAllRooms() throws SQLException {
        return roomDAO.findAll();
    }

    public ArrayList<Room> getRoomsByStatus(String status) throws SQLException {
        return roomDAO.findByStatus(status);
    }

    public ArrayList<Room> getRoomsWithFreeBed() throws SQLException {
        return roomDAO.findRoomsWithFreeBed();
    }

    public ArrayList<Room> getRoomsWithFreeBedForGender(String gender) throws SQLException {
        return roomDAO.findRoomsWithFreeBedForGender(gender);
    }

    public HashMap<Integer, Room> getRoomMap() throws SQLException {
        return roomDAO.findAllAsMap();
    }

    /**
     * Updates room details.
     *
     * The capacity may not be lowered below the number of students already
     * living there - that would produce occupiedBeds > capacity, which is
     * exactly the state the assertions in Room forbid.
     */
    @AdminOperation("Update room details")
    public boolean updateRoom(Room room)
            throws InvalidStudentDataException, RoomNotFoundException, SQLException {

        Room existing = roomDAO.findById(room.getRoomId());
        if (existing == null) {
            throw new RoomNotFoundException(
                    "Cannot update: no room with ID " + room.getRoomId());
        }

        validateRoom(room);

        if (room.getCapacity() < existing.getOccupiedBeds()) {
            throw new InvalidStudentDataException(
                    "Capacity cannot be less than the " + existing.getOccupiedBeds()
                    + " student(s) already staying in this room.");
        }

        // Keep the real occupancy, then recompute the status from it.
        room.setOccupiedBeds(existing.getOccupiedBeds());
        if (!Room.MAINTENANCE.equals(room.getStatus())) {
            room.refreshStatus();
        }

        assert room.getOccupiedBeds() <= room.getCapacity()
                : "updateRoom produced occupiedBeds > capacity";

        return roomDAO.update(room);
    }

    /** A room with students in it cannot be deleted. */
    @AdminOperation("Delete a room")
    public boolean deleteRoom(int roomId) throws RoomNotFoundException, SQLException {

        Room room = roomDAO.findById(roomId);
        if (room == null) {
            throw new RoomNotFoundException("Cannot delete: no room with ID " + roomId);
        }
        if (room.getOccupiedBeds() > 0) {
            System.out.println("Room " + room.getRoomNumber() + " still has "
                    + room.getOccupiedBeds() + " student(s) in it.");
            System.out.println("Check those students out first.");
            return false;
        }
        if (!allocationDAO.findActiveByRoomId(roomId).isEmpty()) {
            System.out.println("This room still has active allocation records.");
            return false;
        }
        return roomDAO.delete(roomId);
    }

    /** Puts a room in or out of MAINTENANCE. */
    @AdminOperation("Change room status")
    public boolean setMaintenance(int roomId, boolean underMaintenance)
            throws RoomNotFoundException, SQLException {

        Room room = getRoomById(roomId);

        if (underMaintenance) {
            if (room.getOccupiedBeds() > 0) {
                System.out.println("Cannot start maintenance: students are still inside.");
                return false;
            }
            room.setStatus(Room.MAINTENANCE);
        } else {
            room.setStatus(Room.AVAILABLE);
            room.refreshStatus();
        }
        return roomDAO.update(room);
    }

    public int countRooms() throws SQLException {
        return roomDAO.countAll();
    }

    public int countRoomsByStatus(String status) throws SQLException {
        return roomDAO.countByStatus(status);
    }

    public int totalOccupiedBeds() throws SQLException {
        return roomDAO.totalOccupiedBeds();
    }

    public int totalBeds() throws SQLException {
        return roomDAO.totalBeds();
    }

    private void validateRoom(Room room) throws InvalidStudentDataException {

        if (!Validation.isValidRoomNumber(room.getRoomNumber())) {
            throw new InvalidStudentDataException(
                    "Room number must look like A-101 (letters, hyphen, 3 digits).");
        }
        if (!Validation.isValidCapacity(room.getCapacity())) {
            throw new InvalidStudentDataException("Capacity must be between 1 and 6 beds.");
        }
        if (room.getFloor() < 0 || room.getFloor() > 10) {
            throw new InvalidStudentDataException("Floor must be between 0 and 10.");
        }
    }
}
