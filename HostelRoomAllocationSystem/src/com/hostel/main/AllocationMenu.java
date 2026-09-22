package com.hostel.main;

import com.hostel.exception.AllocationException;
import com.hostel.exception.RoomFullException;
import com.hostel.exception.RoomNotFoundException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.Allocation;
import com.hostel.model.Room;
import com.hostel.model.Student;
import com.hostel.model.WaitingEntry;
import com.hostel.service.AllocationService;
import com.hostel.service.RoomService;
import com.hostel.service.StudentService;
import com.hostel.service.WaitingListService;
import com.hostel.util.InputUtil;

import java.sql.SQLException;
import java.util.ArrayList;

/** MODULES 5, 6, 7 and 8 - allocation, transfer, checkout and the waiting list. */
public class AllocationMenu {

    private final AllocationService allocationService = new AllocationService();
    private final StudentService studentService = new StudentService();
    private final RoomService roomService = new RoomService();
    private final WaitingListService waitingListService = new WaitingListService();

    // ==========================================================
    // MODULE 5 - ALLOCATE
    // ==========================================================
    public void allocateRoom() throws SQLException {

        System.out.println("\n================ ROOM ALLOCATION ================");

        ArrayList<Student> waitingForRoom = studentService.getStudentsWithoutRoom();
        if (waitingForRoom.isEmpty()) {
            System.out.println("Every student already has a room.");
            InputUtil.pause();
            return;
        }

        System.out.println("\nStudents who still need a room:");
        System.out.println(waitingForRoom.get(0).header());
        for (int i = 0; i < waitingForRoom.size(); i++) {
            waitingForRoom.get(i).display();
        }

        int studentId = InputUtil.readInt("\nEnter student ID: ");

        Student student;
        try {
            student = studentService.getStudentById(studentId);
        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
            InputUtil.pause();
            return;
        }

        // Only show rooms in a block that matches this gender.
        ArrayList<Room> freeRooms =
                roomService.getRoomsWithFreeBedForGender(student.getGender());

        if (freeRooms.isEmpty()) {
            System.out.println("\nThere is no free bed in any " + student.getGender()
                    + " block right now.");
            if (InputUtil.readYesNo("Add this student to the waiting list instead")) {
                addToWaitingList(studentId);
            }
            InputUtil.pause();
            return;
        }

        System.out.println("\nRooms with a free bed for " + student.getGender()
                + " students:");
        System.out.println(freeRooms.get(0).header());
        for (int i = 0; i < freeRooms.size(); i++) {
            freeRooms.get(i).display();
        }

        int roomId = InputUtil.readInt("\nEnter room ID: ");

        /*
         * Four different things can go wrong, and each one deserves its own
         * message. The service either COMMITS everything or ROLLS BACK
         * everything, so the database is never left half-updated.
         */
        try {
            Allocation allocation = allocationService.allocateRoom(studentId, roomId);
            Room room = roomService.getRoomById(roomId);

            System.out.println("\n---------------- ALLOCATION SUCCESSFUL ----------------");
            System.out.println("  Student   : " + student.getRegisterNumber()
                    + " - " + student.getName());
            System.out.println("  Room      : " + room.getRoomNumber());
            System.out.println("  Date      : " + allocation.getAllocationDate());
            System.out.println("  Capacity  : " + room.getCapacity());
            System.out.println("  Occupied  : " + room.getOccupiedBeds());
            System.out.println("  Available : " + room.getAvailableBeds());
            System.out.println("  Status    : " + room.getStatus());
            System.out.println("-------------------------------------------------------");

        } catch (RoomFullException e) {
            System.out.println("\n[ROOM FULL] " + e.getMessage());
            if (InputUtil.readYesNo("Add this student to the waiting list")) {
                addToWaitingList(studentId);
            }
        } catch (RoomNotFoundException e) {
            System.out.println("\n[NOT FOUND] " + e.getMessage());
        } catch (StudentNotFoundException e) {
            System.out.println("\n[NOT FOUND] " + e.getMessage());
        } catch (AllocationException e) {
            System.out.println("\n[NOT ALLOWED] " + e.getMessage());
        }
        InputUtil.pause();
    }

    // ==========================================================
    // MODULE 6 - TRANSFER
    // ==========================================================
    public void transferStudent() throws SQLException {

        System.out.println("\n================= ROOM TRANSFER =================");

        ArrayList<Allocation> active = allocationService.getActiveAllocations();
        if (active.isEmpty()) {
            System.out.println("No student is currently allocated to a room.");
            InputUtil.pause();
            return;
        }

        System.out.println("\nStudents currently in a room:");
        System.out.println(active.get(0).header());
        for (int i = 0; i < active.size(); i++) {
            active.get(i).display();
        }

        int studentId = InputUtil.readInt("\nEnter the student ID to transfer: ");

        Student student;
        try {
            student = studentService.getStudentById(studentId);
        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
            InputUtil.pause();
            return;
        }

        ArrayList<Room> freeRooms =
                roomService.getRoomsWithFreeBedForGender(student.getGender());

        if (freeRooms.isEmpty()) {
            System.out.println("\nThere is no other room with a free bed.");
            InputUtil.pause();
            return;
        }

        System.out.println("\nRooms with a free bed:");
        System.out.println(freeRooms.get(0).header());
        for (int i = 0; i < freeRooms.size(); i++) {
            freeRooms.get(i).display();
        }

        int newRoomId = InputUtil.readInt("\nEnter the NEW room ID: ");

        try {
            Allocation fresh = allocationService.transferStudent(studentId, newRoomId);
            Room newRoom = roomService.getRoomById(newRoomId);

            System.out.println("\n---------------- TRANSFER SUCCESSFUL ----------------");
            System.out.println("  Student   : " + student.getRegisterNumber()
                    + " - " + student.getName());
            System.out.println("  Moved to  : " + newRoom.getRoomNumber()
                    + "  (now " + newRoom.getOccupiedBeds() + "/"
                    + newRoom.getCapacity() + ")");
            System.out.println("  Date      : " + fresh.getAllocationDate());
            System.out.println("  The old allocation is kept with status TRANSFERRED,");
            System.out.println("  so the transfer history is never lost.");
            System.out.println("-----------------------------------------------------");

        } catch (RoomFullException e) {
            System.out.println("\n[ROOM FULL] " + e.getMessage());
        } catch (RoomNotFoundException e) {
            System.out.println("\n[NOT FOUND] " + e.getMessage());
        } catch (StudentNotFoundException e) {
            System.out.println("\n[NOT FOUND] " + e.getMessage());
        } catch (AllocationException e) {
            System.out.println("\n[NOT ALLOWED] " + e.getMessage());
        }
        InputUtil.pause();
    }

    // ==========================================================
    // MODULE 7 - CHECKOUT
    // ==========================================================
    public void checkoutStudent() throws SQLException {

        System.out.println("\n================ STUDENT CHECKOUT ================");

        ArrayList<Allocation> active = allocationService.getActiveAllocations();
        if (active.isEmpty()) {
            System.out.println("Nobody is currently staying in the hostel.");
            InputUtil.pause();
            return;
        }

        System.out.println("\nStudents currently in the hostel:");
        System.out.println(active.get(0).header());
        for (int i = 0; i < active.size(); i++) {
            active.get(i).display();
        }

        int studentId = InputUtil.readInt("\nEnter the student ID to check out: ");

        try {
            Student student = studentService.getStudentById(studentId);

            if (!InputUtil.readYesNo("Check out " + student.getName())) {
                System.out.println("Cancelled.");
                InputUtil.pause();
                return;
            }

            if (allocationService.checkoutStudent(studentId)) {
                System.out.println("\n" + student.getName()
                        + " has been checked out successfully.");
                System.out.println("The bed is available again.");
            }

        } catch (StudentNotFoundException e) {
            System.out.println("\n[NOT FOUND] " + e.getMessage());
        } catch (AllocationException e) {
            System.out.println("\n[NOT ALLOWED] " + e.getMessage());
        }
        InputUtil.pause();
    }

    // ==========================================================
    // MODULE 8 - WAITING LIST
    // ==========================================================
    public void showWaitingListMenu() throws SQLException {

        boolean stay = true;
        while (stay) {
            System.out.println("\n---------------- WAITING LIST ----------------");
            System.out.println("  1. Add a student to the waiting list");
            System.out.println("  2. View the waiting list");
            System.out.println("  3. Compare FIFO order and PRIORITY order");
            System.out.println("  4. Who is next (highest priority)");
            System.out.println("  5. Allocate a room to the next student");
            System.out.println("  6. Remove a student from the waiting list");
            System.out.println("  7. Back to main menu");
            System.out.println("----------------------------------------------");

            int choice = InputUtil.readIntInRange("Enter choice (1-7): ", 1, 7);

            switch (choice) {
                case 1: askAndAddToWaitingList(); break;
                case 2: viewWaitingList(); break;
                case 3: waitingListService.compareQueueOrders(); InputUtil.pause(); break;
                case 4: showNextStudent(); break;
                case 5: allocateNextWaitingStudent(); break;
                case 6: removeFromWaitingList(); break;
                case 7: stay = false; break;
                default: break;
            }
        }
    }

    private void askAndAddToWaitingList() throws SQLException {

        ArrayList<Student> withoutRoom = studentService.getStudentsWithoutRoom();
        if (withoutRoom.isEmpty()) {
            System.out.println("\nEvery student already has a room.");
            InputUtil.pause();
            return;
        }

        System.out.println("\nStudents without a room:");
        System.out.println(withoutRoom.get(0).header());
        for (int i = 0; i < withoutRoom.size(); i++) {
            withoutRoom.get(i).display();
        }

        int studentId = InputUtil.readInt("\nEnter student ID: ");
        addToWaitingList(studentId);
        InputUtil.pause();
    }

    /** Shared by the allocation screen and the waiting-list screen. */
    private void addToWaitingList(int studentId) throws SQLException {

        System.out.println("\nPriority:  1 = highest (final year, medical)  5 = lowest");
        int priority = InputUtil.readIntInRange("Priority (1-5): ", 1, 5);

        try {
            int id = waitingListService.addToWaitingList(studentId, priority);
            if (id > 0) {
                System.out.println("Added to the waiting list. Waiting ID = " + id);
            }
        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (AllocationException e) {
            System.out.println("[NOT ALLOWED] " + e.getMessage());
        }
    }

    private void viewWaitingList() throws SQLException {

        ArrayList<WaitingEntry> entries = waitingListService.getWaitingList();

        System.out.println("\n===== WAITING LIST  (" + entries.size() + ") =====");
        if (entries.isEmpty()) {
            System.out.println("Nobody is waiting for a room.");
            InputUtil.pause();
            return;
        }

        System.out.println(entries.get(0).header());
        System.out.println("---------------------------------------------------------------");
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).display();
        }
        InputUtil.pause();
    }

    private void showNextStudent() throws SQLException {

        WaitingEntry next = waitingListService.getNextStudentToAllocate();

        if (next == null) {
            System.out.println("\nThe waiting list is empty.");
        } else {
            System.out.println("\nNext student to be given a room:");
            System.out.println("   " + next.getRegisterNumber() + " - "
                    + next.getStudentName());
            System.out.println("   priority " + next.getPriority()
                    + ", waiting since " + next.getRequestDate());
        }
        InputUtil.pause();
    }

    private void allocateNextWaitingStudent() throws SQLException {

        WaitingEntry next = waitingListService.getNextStudentToAllocate();
        if (next == null) {
            System.out.println("\nThe waiting list is empty.");
            InputUtil.pause();
            return;
        }

        System.out.println("\nNext in the queue: " + next.getRegisterNumber()
                + " - " + next.getStudentName() + " (priority " + next.getPriority() + ")");

        Student student;
        try {
            student = studentService.getStudentById(next.getStudentId());
        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
            InputUtil.pause();
            return;
        }

        ArrayList<Room> freeRooms =
                roomService.getRoomsWithFreeBedForGender(student.getGender());

        if (freeRooms.isEmpty()) {
            System.out.println("There is still no free bed. The student stays in the queue.");
            InputUtil.pause();
            return;
        }

        System.out.println("\nRooms with a free bed:");
        System.out.println(freeRooms.get(0).header());
        for (int i = 0; i < freeRooms.size(); i++) {
            freeRooms.get(i).display();
        }

        int roomId = InputUtil.readInt("\nEnter room ID: ");

        try {
            allocationService.allocateRoom(next.getStudentId(), roomId);
            System.out.println("\nAllocated. The student was removed from the waiting list");
            System.out.println("in the SAME transaction, so the two can never disagree.");
        } catch (RoomFullException e) {
            System.out.println("\n[ROOM FULL] " + e.getMessage());
        } catch (RoomNotFoundException e) {
            System.out.println("\n[NOT FOUND] " + e.getMessage());
        } catch (StudentNotFoundException e) {
            System.out.println("\n[NOT FOUND] " + e.getMessage());
        } catch (AllocationException e) {
            System.out.println("\n[NOT ALLOWED] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void removeFromWaitingList() throws SQLException {

        ArrayList<WaitingEntry> entries = waitingListService.getWaitingList();
        if (entries.isEmpty()) {
            System.out.println("\nThe waiting list is empty.");
            InputUtil.pause();
            return;
        }

        System.out.println(entries.get(0).header());
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).display();
        }

        int waitingId = InputUtil.readInt("\nEnter the waiting ID to remove: ");

        if (waitingListService.getEntryById(waitingId) == null) {
            System.out.println("[NOT FOUND] No waiting entry with ID " + waitingId);
        } else if (waitingListService.removeFromWaitingList(waitingId)) {
            System.out.println("Removed from the waiting list.");
        } else {
            System.out.println("Nothing was changed.");
        }
        InputUtil.pause();
    }
}
