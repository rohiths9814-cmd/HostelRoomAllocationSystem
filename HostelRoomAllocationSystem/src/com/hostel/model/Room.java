package com.hostel.model;

/**
 * A hostel room.
 *
 * This class is where ASSERTIONS earn their place. The rule
 * "occupiedBeds can never be more than capacity, and never below zero"
 * is an INTERNAL rule of our own program. If it is ever broken, our code
 * has a bug - the user did not cause it.
 *
 * Assertions are checked only when you run java with -ea, so they cost
 * nothing in the finished program. They are a development safety net,
 * NOT a replacement for validating user input.
 */
public class Room implements Displayable {

    // Status values as constants. Constants stop spelling mistakes.
    public static final String AVAILABLE = "AVAILABLE";
    public static final String PARTIALLY_OCCUPIED = "PARTIALLY_OCCUPIED";
    public static final String FULL = "FULL";
    public static final String MAINTENANCE = "MAINTENANCE";

    private int roomId;
    private String roomNumber;     // A-101
    private int blockId;           // foreign key -> hostel_blocks
    private int floor;
    private int capacity;          // total beds
    private int occupiedBeds;      // beds currently used
    private String status;

    public Room() {
        this.roomId = 0;
        this.roomNumber = "";
        this.blockId = 0;
        this.floor = 1;
        this.capacity = 1;
        this.occupiedBeds = 0;
        this.status = AVAILABLE;
    }

    public Room(String roomNumber, int blockId, int floor, int capacity) {
        this.roomNumber = roomNumber;
        this.blockId = blockId;
        this.floor = floor;
        this.capacity = capacity;
        this.occupiedBeds = 0;
        this.status = AVAILABLE;
    }

    public Room(int roomId, String roomNumber, int blockId, int floor,
                int capacity, int occupiedBeds, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.blockId = blockId;
        this.floor = floor;
        this.capacity = capacity;
        this.occupiedBeds = occupiedBeds;
        this.status = status;
    }

    // ==========================================================
    // BUSINESS BEHAVIOUR
    // A model may hold logic about its own data - that is the
    // whole point of objects.
    // ==========================================================

    /** How many beds are still free. */
    public int getAvailableBeds() {
        // INTERNAL ASSUMPTIONS - enabled only when you run java -ea
        assert capacity > 0 : "Room capacity must be positive: " + roomNumber;
        assert occupiedBeds >= 0 : "Occupied beds went negative: " + roomNumber;
        assert occupiedBeds <= capacity
                : "Occupied beds (" + occupiedBeds + ") exceeded capacity ("
                  + capacity + ") in room " + roomNumber;

        return capacity - occupiedBeds;
    }

    /** True only if a bed is free AND the room is not under maintenance. */
    public boolean isAvailable() {
        if (MAINTENANCE.equals(status)) {
            return false;
        }
        return getAvailableBeds() > 0;
    }

    /** Adds one occupant. Called only after the service layer checked for space. */
    public void occupyOneBed() {
        assert occupiedBeds < capacity : "occupyOneBed() called on a full room " + roomNumber;
        occupiedBeds = occupiedBeds + 1;
        refreshStatus();
    }

    /** Removes one occupant (checkout or transfer out). */
    public void freeOneBed() {
        assert occupiedBeds > 0 : "freeOneBed() called on an empty room " + roomNumber;
        occupiedBeds = occupiedBeds - 1;
        refreshStatus();
    }

    /** Recalculates the status from the bed counts. */
    public void refreshStatus() {
        if (MAINTENANCE.equals(status)) {
            return;                       // maintenance is set by hand, keep it
        }
        if (occupiedBeds == 0) {
            status = AVAILABLE;
        } else if (occupiedBeds >= capacity) {
            status = FULL;
        } else {
            status = PARTIALLY_OCCUPIED;
        }
    }

    // ==========================================================
    // GETTERS AND SETTERS
    // ==========================================================

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getBlockId() {
        return blockId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(int occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String header() {
        return String.format("%-6s %-10s %-8s %-6s %-9s %-9s %-6s %-20s",
                "ROOMID", "ROOM NO", "BLOCKID", "FLOOR", "CAPACITY", "OCCUPIED",
                "FREE", "STATUS");
    }

    @Override
    public void display() {
        System.out.println(String.format("%-6d %-10s %-8d %-6d %-9d %-9d %-6d %-20s",
                roomId, roomNumber, blockId, floor, capacity, occupiedBeds,
                capacity - occupiedBeds, status));
    }

    @Override
    public String toString() {
        return "Room{" + roomNumber + ", beds " + occupiedBeds + "/" + capacity
                + ", " + status + "}";
    }
}
