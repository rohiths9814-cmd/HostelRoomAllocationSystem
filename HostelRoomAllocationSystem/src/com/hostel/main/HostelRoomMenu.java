package com.hostel.main;

import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.RoomNotFoundException;
import com.hostel.model.HostelBlock;
import com.hostel.model.Room;
import com.hostel.service.BlockService;
import com.hostel.service.RoomService;
import com.hostel.util.InputUtil;

import java.sql.SQLException;
import java.util.ArrayList;

/** MODULE 3 (hostel blocks) and MODULE 4 (rooms). */
public class HostelRoomMenu {

    private final BlockService blockService = new BlockService();
    private final RoomService roomService = new RoomService();

    // ==========================================================
    // BLOCK MENU
    // ==========================================================
    public void showBlockMenu() throws SQLException {

        boolean stay = true;
        while (stay) {
            System.out.println("\n------------ HOSTEL BLOCK MANAGEMENT ------------");
            System.out.println("  1. Add block");
            System.out.println("  2. View all blocks");
            System.out.println("  3. Update block");
            System.out.println("  4. Delete block");
            System.out.println("  5. Back to main menu");
            System.out.println("-------------------------------------------------");

            int choice = InputUtil.readIntInRange("Enter choice (1-5): ", 1, 5);

            switch (choice) {
                case 1: addBlock(); break;
                case 2: viewBlocks(); break;
                case 3: updateBlock(); break;
                case 4: deleteBlock(); break;
                case 5: stay = false; break;
                default: break;
            }
        }
    }

    private void addBlock() throws SQLException {

        System.out.println("\n----- ADD HOSTEL BLOCK -----");
        String name = InputUtil.readNonEmptyString("Block name (e.g. Block D): ");
        String gender = readGender("Reserved for (M/F)      : ");
        int floors = InputUtil.readIntInRange("Number of floors (1-10) : ", 1, 10);

        try {
            int id = blockService.addBlock(new HostelBlock(name, gender, floors));
            if (id > 0) {
                System.out.println("\nBlock added. New block ID = " + id);
            } else {
                System.out.println("\nThe block could not be saved.");
            }
        } catch (InvalidStudentDataException e) {
            System.out.println("\n[INVALID DATA] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void viewBlocks() throws SQLException {

        ArrayList<HostelBlock> blocks = blockService.getAllBlocks();

        System.out.println("\n===== HOSTEL BLOCKS  (" + blocks.size() + ") =====");
        if (blocks.isEmpty()) {
            System.out.println("No blocks yet. Add one first.");
            InputUtil.pause();
            return;
        }

        System.out.println(blocks.get(0).header());
        System.out.println("---------------------------------------------");
        for (int i = 0; i < blocks.size(); i++) {
            blocks.get(i).display();
        }
        InputUtil.pause();
    }

    private void updateBlock() throws SQLException {

        int id = InputUtil.readInt("\nEnter the block ID to update: ");
        HostelBlock block = blockService.getBlockById(id);

        if (block == null) {
            System.out.println("[NOT FOUND] No block with ID " + id);
            InputUtil.pause();
            return;
        }

        System.out.println("Current: " + block);
        System.out.println("Press ENTER to keep a value.");

        String name = InputUtil.readString("Block name [" + block.getBlockName() + "]: ");
        if (!name.isEmpty()) {
            block.setBlockName(name);
        }

        String gender = InputUtil.readString("Gender [" + block.getGender() + "]: ");
        if (!gender.isEmpty()) {
            block.setGender(gender.toUpperCase().startsWith("F") ? "FEMALE" : "MALE");
        }

        String floors = InputUtil.readString("Floors [" + block.getFloors() + "]: ");
        if (!floors.isEmpty()) {
            try {
                block.setFloors(Integer.parseInt(floors));
            } catch (NumberFormatException e) {
                System.out.println("  (not a number - keeping " + block.getFloors() + ")");
            }
        }

        try {
            if (blockService.updateBlock(block)) {
                System.out.println("Block updated.");
            } else {
                System.out.println("Nothing was changed.");
            }
        } catch (InvalidStudentDataException e) {
            System.out.println("[INVALID DATA] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void deleteBlock() throws SQLException {

        int id = InputUtil.readInt("\nEnter the block ID to delete: ");
        HostelBlock block = blockService.getBlockById(id);

        if (block == null) {
            System.out.println("[NOT FOUND] No block with ID " + id);
            InputUtil.pause();
            return;
        }

        System.out.println("About to delete: " + block);
        if (InputUtil.readYesNo("Are you sure")) {
            if (blockService.deleteBlock(id)) {
                System.out.println("Block deleted.");
            }
        } else {
            System.out.println("Cancelled.");
        }
        InputUtil.pause();
    }

    // ==========================================================
    // ROOM MENU
    // ==========================================================
    public void showRoomMenu() throws SQLException {

        boolean stay = true;
        while (stay) {
            System.out.println("\n--------------- ROOM MANAGEMENT ---------------");
            System.out.println("  1. Add room");
            System.out.println("  2. View all rooms");
            System.out.println("  3. Search room (by room number)");
            System.out.println("  4. Update room");
            System.out.println("  5. Delete room");
            System.out.println("  6. View AVAILABLE rooms");
            System.out.println("  7. View PARTIALLY OCCUPIED rooms");
            System.out.println("  8. View FULL rooms");
            System.out.println("  9. Put a room in / out of MAINTENANCE");
            System.out.println(" 10. Back to main menu");
            System.out.println("-----------------------------------------------");

            int choice = InputUtil.readIntInRange("Enter choice (1-10): ", 1, 10);

            switch (choice) {
                case 1: addRoom(); break;
                case 2: viewRooms(roomService.getAllRooms(), "ALL ROOMS"); break;
                case 3: searchRoom(); break;
                case 4: updateRoom(); break;
                case 5: deleteRoom(); break;
                case 6: viewRooms(roomService.getRoomsByStatus(Room.AVAILABLE),
                                  "AVAILABLE ROOMS"); break;
                case 7: viewRooms(roomService.getRoomsByStatus(Room.PARTIALLY_OCCUPIED),
                                  "PARTIALLY OCCUPIED ROOMS"); break;
                case 8: viewRooms(roomService.getRoomsByStatus(Room.FULL),
                                  "FULL ROOMS"); break;
                case 9: toggleMaintenance(); break;
                case 10: stay = false; break;
                default: break;
            }
        }
    }

    private void addRoom() throws SQLException {

        ArrayList<HostelBlock> blocks = blockService.getAllBlocks();
        if (blocks.isEmpty()) {
            System.out.println("\nAdd a hostel block first - a room must belong to one.");
            InputUtil.pause();
            return;
        }

        System.out.println("\n----- ADD ROOM -----");
        System.out.println("Available blocks:");
        for (int i = 0; i < blocks.size(); i++) {
            HostelBlock block = blocks.get(i);
            System.out.println("   " + block.getBlockId() + " = " + block.getBlockName()
                    + " (" + block.getGender() + ", " + block.getFloors() + " floors)");
        }

        String number = InputUtil.readNonEmptyString(
                "Room number (e.g. A-101): ").toUpperCase();
        int blockId = InputUtil.readInt("Block ID                : ");
        int floor = InputUtil.readIntInRange("Floor (0-10)            : ", 0, 10);
        int capacity = InputUtil.readIntInRange("Capacity / beds (1-6)   : ", 1, 6);

        try {
            int id = roomService.addRoom(new Room(number, blockId, floor, capacity));
            if (id > 0) {
                System.out.println("\nRoom added. New room ID = " + id);
            } else {
                System.out.println("\nThe room could not be saved.");
            }
        } catch (InvalidStudentDataException e) {
            System.out.println("\n[INVALID DATA] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void viewRooms(ArrayList<Room> rooms, String title) {

        System.out.println("\n===== " + title + "  (" + rooms.size() + ") =====");
        if (rooms.isEmpty()) {
            System.out.println("No rooms to show.");
            InputUtil.pause();
            return;
        }

        System.out.println(rooms.get(0).header());
        System.out.println("-----------------------------------------------------"
                + "---------------------------");
        for (int i = 0; i < rooms.size(); i++) {
            rooms.get(i).display();
        }
        InputUtil.pause();
    }

    private void searchRoom() throws SQLException {

        String number = InputUtil.readNonEmptyString("\nEnter room number: ").toUpperCase();
        try {
            Room room = roomService.getRoomByNumber(number);
            System.out.println("\n----- ROOM DETAILS -----");
            System.out.println("  Room ID       : " + room.getRoomId());
            System.out.println("  Room number   : " + room.getRoomNumber());
            System.out.println("  Block ID      : " + room.getBlockId());
            System.out.println("  Floor         : " + room.getFloor());
            System.out.println("  Capacity      : " + room.getCapacity());
            System.out.println("  Occupied beds : " + room.getOccupiedBeds());
            System.out.println("  Free beds     : " + room.getAvailableBeds());
            System.out.println("  Status        : " + room.getStatus());
        } catch (RoomNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void updateRoom() throws SQLException {

        int id = InputUtil.readInt("\nEnter the room ID to update: ");

        try {
            Room room = roomService.getRoomById(id);
            System.out.println("Current: " + room);
            System.out.println("Press ENTER to keep a value.");

            String number = InputUtil.readString("Room number [" + room.getRoomNumber() + "]: ");
            if (!number.isEmpty()) {
                room.setRoomNumber(number.toUpperCase());
            }

            String floor = InputUtil.readString("Floor [" + room.getFloor() + "]: ");
            if (!floor.isEmpty()) {
                try {
                    room.setFloor(Integer.parseInt(floor));
                } catch (NumberFormatException e) {
                    System.out.println("  (not a number - keeping " + room.getFloor() + ")");
                }
            }

            String capacity = InputUtil.readString("Capacity [" + room.getCapacity() + "]: ");
            if (!capacity.isEmpty()) {
                try {
                    room.setCapacity(Integer.parseInt(capacity));
                } catch (NumberFormatException e) {
                    System.out.println("  (not a number - keeping " + room.getCapacity() + ")");
                }
            }

            if (roomService.updateRoom(room)) {
                System.out.println("Room updated.");
            } else {
                System.out.println("Nothing was changed.");
            }

        } catch (RoomNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (InvalidStudentDataException e) {
            System.out.println("[INVALID DATA] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void deleteRoom() throws SQLException {

        int id = InputUtil.readInt("\nEnter the room ID to delete: ");
        try {
            Room room = roomService.getRoomById(id);
            System.out.println("About to delete: " + room);

            if (InputUtil.readYesNo("Are you sure")) {
                if (roomService.deleteRoom(id)) {
                    System.out.println("Room deleted.");
                }
            } else {
                System.out.println("Cancelled.");
            }
        } catch (RoomNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void toggleMaintenance() throws SQLException {

        int id = InputUtil.readInt("\nEnter the room ID: ");
        try {
            Room room = roomService.getRoomById(id);
            System.out.println("Current status: " + room.getStatus());

            boolean turnOn = !Room.MAINTENANCE.equals(room.getStatus());
            String question = turnOn
                    ? "Put this room UNDER maintenance"
                    : "Take this room OUT of maintenance";

            if (InputUtil.readYesNo(question)) {
                if (roomService.setMaintenance(id, turnOn)) {
                    System.out.println("Status changed.");
                }
            } else {
                System.out.println("Cancelled.");
            }
        } catch (RoomNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private String readGender(String prompt) {
        while (true) {
            String gender = InputUtil.readNonEmptyString(prompt).toUpperCase();
            if (gender.startsWith("M")) {
                return "MALE";
            }
            if (gender.startsWith("F")) {
                return "FEMALE";
            }
            System.out.println("  !! Type M for male or F for female.");
        }
    }
}
