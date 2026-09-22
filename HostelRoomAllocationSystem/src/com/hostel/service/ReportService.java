package com.hostel.service;

import com.hostel.dao.AllocationDAO;
import com.hostel.dao.BlockDAO;
import com.hostel.dao.ComplaintDAO;
import com.hostel.dao.FeeDAO;
import com.hostel.dao.RoomDAO;
import com.hostel.dao.StudentDAO;
import com.hostel.dao.WaitingListDAO;
import com.hostel.model.Allocation;
import com.hostel.model.Complaint;
import com.hostel.model.HostelBlock;
import com.hostel.model.HostelFee;
import com.hostel.model.Room;
import com.hostel.model.Student;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * MODULE 11 - all the console reports.
 *
 * This class is the best place in the project to see WHY different
 * collections exist. Read the comment above each report.
 */
public class ReportService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final BlockDAO blockDAO = new BlockDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final FeeDAO feeDAO = new FeeDAO();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();

    private static final String LINE =
            "------------------------------------------------------------"
          + "------------------------";

    // ==========================================================
    // REPORT 1 - the summary dashboard
    // ==========================================================
    public void printSummary() throws SQLException {

        int totalStudents = studentDAO.countAll();
        int totalBlocks = blockDAO.countAll();
        int totalRooms = roomDAO.countAll();
        int available = roomDAO.countByStatus(Room.AVAILABLE);
        int partial = roomDAO.countByStatus(Room.PARTIALLY_OCCUPIED);
        int full = roomDAO.countByStatus(Room.FULL);
        int maintenance = roomDAO.countByStatus(Room.MAINTENANCE);
        int totalBeds = roomDAO.totalBeds();
        int occupiedBeds = roomDAO.totalOccupiedBeds();
        int allocated = allocationDAO.countActive();
        int waiting = waitingListDAO.countWaiting();

        System.out.println("\n" + LINE);
        System.out.println("                        HOSTEL SUMMARY REPORT");
        System.out.println(LINE);
        System.out.println("  Total students registered   : " + totalStudents);
        System.out.println("  Students with a room        : " + allocated);
        System.out.println("  Students without a room     : " + (totalStudents - allocated));
        System.out.println("  Students on the waiting list: " + waiting);
        System.out.println(LINE);
        System.out.println("  Total hostel blocks         : " + totalBlocks);
        System.out.println("  Total rooms                 : " + totalRooms);
        System.out.println("     available (empty)        : " + available);
        System.out.println("     partially occupied       : " + partial);
        System.out.println("     full                     : " + full);
        System.out.println("     under maintenance        : " + maintenance);
        System.out.println(LINE);
        System.out.println("  Total beds in the hostel    : " + totalBeds);
        System.out.println("  Beds occupied               : " + occupiedBeds);
        System.out.println("  Beds free                   : " + (totalBeds - occupiedBeds));

        if (totalBeds > 0) {
            // (occupied * 100.0) forces DOUBLE division.
            // Writing occupied * 100 / totalBeds would do INTEGER division
            // and silently throw the decimals away - a classic Java trap.
            double percent = (occupiedBeds * 100.0) / totalBeds;
            System.out.println("  Occupancy                   : "
                    + String.format("%.1f", percent) + " %");
        }
        System.out.println(LINE);
    }

    // ==========================================================
    // REPORT 2 - students with no room
    // ==========================================================
    public void printStudentsWithoutRoom() throws SQLException {

        ArrayList<Student> students = studentDAO.findStudentsWithoutRoom();

        System.out.println("\n" + LINE);
        System.out.println("  STUDENTS WITHOUT A ROOM  (" + students.size() + ")");
        System.out.println(LINE);

        if (students.isEmpty()) {
            System.out.println("  Every registered student has a room.");
            return;
        }

        System.out.println(students.get(0).header());
        for (int i = 0; i < students.size(); i++) {
            students.get(i).display();
        }
    }

    // ==========================================================
    // REPORT 3 - student-wise allocation
    // ==========================================================
    public void printStudentWiseAllocation() throws SQLException {

        ArrayList<Allocation> allocations = allocationDAO.findAllActive();

        System.out.println("\n" + LINE);
        System.out.println("  STUDENT-WISE ALLOCATION  (" + allocations.size() + " active)");
        System.out.println(LINE);

        if (allocations.isEmpty()) {
            System.out.println("  Nobody is allocated to a room yet.");
            return;
        }

        System.out.println(allocations.get(0).header());
        for (int i = 0; i < allocations.size(); i++) {
            allocations.get(i).display();
        }
    }

    // ==========================================================
    // REPORT 4 - room-wise students
    // ==========================================================

    /**
     * Groups the students by the room they live in.
     *
     * The database gives us a flat list of allocations. To print it room by
     * room we need a lookup structure, and a HashMap of
     * roomId -> list of students is exactly that.
     *
     * TreeMap is used instead of HashMap for the OUTPUT because a TreeMap
     * keeps its keys sorted, so the rooms print in order. A HashMap gives
     * no ordering guarantee at all - that is a common exam question.
     */
    public void printRoomWiseStudents() throws SQLException {

        ArrayList<Allocation> allocations = allocationDAO.findAllActive();
        HashMap<Integer, Room> roomMap = roomDAO.findAllAsMap();
        HashMap<Integer, HostelBlock> blockMap = blockDAO.findAllAsMap();

        // group: room id -> everybody living in it
        TreeMap<Integer, ArrayList<Allocation>> grouped = new TreeMap<>();

        for (int i = 0; i < allocations.size(); i++) {
            Allocation allocation = allocations.get(i);
            Integer key = allocation.getRoomId();

            if (!grouped.containsKey(key)) {
                grouped.put(key, new ArrayList<Allocation>());
            }
            grouped.get(key).add(allocation);
        }

        System.out.println("\n" + LINE);
        System.out.println("  ROOM-WISE STUDENT LIST");
        System.out.println(LINE);

        if (grouped.isEmpty()) {
            System.out.println("  No room is occupied yet.");
            return;
        }

        // Walking a Map with entrySet() - the standard way to read every pair
        for (Map.Entry<Integer, ArrayList<Allocation>> entry : grouped.entrySet()) {

            Integer roomId = entry.getKey();
            ArrayList<Allocation> occupants = entry.getValue();

            Room room = roomMap.get(roomId);           // O(1) lookup
            String roomLabel = (room == null) ? ("room id " + roomId) : room.getRoomNumber();
            String blockLabel = "";

            if (room != null) {
                HostelBlock block = blockMap.get(room.getBlockId());
                if (block != null) {
                    blockLabel = " (" + block.getBlockName() + ")";
                }
            }

            System.out.println("\n  ROOM " + roomLabel + blockLabel
                    + "   beds used: " + occupants.size()
                    + (room == null ? "" : "/" + room.getCapacity()));

            for (int i = 0; i < occupants.size(); i++) {
                Allocation occupant = occupants.get(i);
                System.out.println("      - " + occupant.getRegisterNumber()
                        + "  " + occupant.getStudentName()
                        + "   since " + occupant.getAllocationDate());
            }
        }
    }

    // ==========================================================
    // REPORT 5 - vacancy by block
    // ==========================================================
    public void printBlockWiseVacancy() throws SQLException {

        ArrayList<Room> rooms = roomDAO.findAll();
        HashMap<Integer, HostelBlock> blockMap = blockDAO.findAllAsMap();

        TreeMap<Integer, int[]> totals = new TreeMap<>();   // blockId -> {beds, used}

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            Integer blockId = room.getBlockId();

            if (!totals.containsKey(blockId)) {
                totals.put(blockId, new int[]{0, 0});
            }
            int[] counters = totals.get(blockId);
            counters[0] = counters[0] + room.getCapacity();
            counters[1] = counters[1] + room.getOccupiedBeds();
        }

        System.out.println("\n" + LINE);
        System.out.println("  BLOCK-WISE VACANCY");
        System.out.println(LINE);
        System.out.println(String.format("  %-20s %-10s %-10s %-10s %-10s",
                "BLOCK", "GENDER", "BEDS", "OCCUPIED", "FREE"));

        for (Map.Entry<Integer, int[]> entry : totals.entrySet()) {
            HostelBlock block = blockMap.get(entry.getKey());
            int[] counters = entry.getValue();

            String name = (block == null) ? ("block " + entry.getKey()) : block.getBlockName();
            String gender = (block == null) ? "-" : block.getGender();

            System.out.println(String.format("  %-20s %-10s %-10d %-10d %-10d",
                    name, gender, counters[0], counters[1], counters[0] - counters[1]));
        }
    }

    // ==========================================================
    // REPORT 6 - pending fees
    // ==========================================================
    public void printPendingFees() throws SQLException {

        ArrayList<HostelFee> pending = feeDAO.findByStatus(HostelFee.PENDING);
        double total = feeDAO.totalPendingAmount();
        double collected = feeDAO.totalCollectedAmount();

        System.out.println("\n" + LINE);
        System.out.println("  PENDING FEES  (" + pending.size() + " record(s))");
        System.out.println(LINE);

        if (pending.isEmpty()) {
            System.out.println("  Every fee has been paid.");
        } else {
            System.out.println(pending.get(0).header());
            for (int i = 0; i < pending.size(); i++) {
                pending.get(i).display();
            }
        }

        System.out.println(LINE);
        System.out.println("  Amount still to collect : " + String.format("%.2f", total));
        System.out.println("  Amount already collected: " + String.format("%.2f", collected));
        System.out.println(LINE);
    }

    // ==========================================================
    // REPORT 7 - complaint statistics
    // ==========================================================

    /**
     * SQL did the counting with GROUP BY, and handed back pairs of
     * label -> count. A HashMap stores exactly that shape.
     * The Integer values are AUTOBOXED from the int that getInt() returns.
     */
    public void printComplaintStatistics() throws SQLException {

        HashMap<String, Integer> byStatus = complaintDAO.countByStatus();
        HashMap<String, Integer> byCategory = complaintDAO.countByCategory();
        int total = complaintDAO.countAll();

        System.out.println("\n" + LINE);
        System.out.println("  COMPLAINT STATISTICS  (total " + total + ")");
        System.out.println(LINE);

        System.out.println("  By status:");
        printCount(byStatus, Complaint.PENDING);
        printCount(byStatus, Complaint.IN_PROGRESS);
        printCount(byStatus, Complaint.RESOLVED);

        System.out.println("\n  By category:");
        for (int i = 0; i < Complaint.CATEGORIES.length; i++) {
            printCount(byCategory, Complaint.CATEGORIES[i]);
        }
        System.out.println(LINE);
    }

    /**
     * map.get(key) returns an Integer OBJECT, and it returns null when the
     * key is missing. Assigning that straight into an int would UNBOX null
     * and throw NullPointerException - one of the most common autoboxing
     * bugs in Java. So we check for null first.
     */
    private void printCount(HashMap<String, Integer> map, String key) {
        Integer count = map.get(key);
        int value = (count == null) ? 0 : count.intValue();
        System.out.println(String.format("     %-16s : %d", key, value));
    }

    // ==========================================================
    // REPORT 8 - room status lists
    // ==========================================================
    public void printRoomsByStatus(String status) throws SQLException {

        ArrayList<Room> rooms = roomDAO.findByStatus(status);

        System.out.println("\n" + LINE);
        System.out.println("  ROOMS WITH STATUS " + status + "  (" + rooms.size() + ")");
        System.out.println(LINE);

        if (rooms.isEmpty()) {
            System.out.println("  No room currently has this status.");
            return;
        }

        System.out.println(rooms.get(0).header());
        for (int i = 0; i < rooms.size(); i++) {
            rooms.get(i).display();
        }
    }
}
