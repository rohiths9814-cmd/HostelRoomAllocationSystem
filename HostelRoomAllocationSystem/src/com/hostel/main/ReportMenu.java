package com.hostel.main;

import com.hostel.model.Allocation;
import com.hostel.model.Room;
import com.hostel.service.AllocationService;
import com.hostel.service.ReportService;
import com.hostel.service.WaitingListService;
import com.hostel.util.InputUtil;

import java.sql.SQLException;
import java.util.ArrayList;

/** MODULE 11 - reports. */
public class ReportMenu {

    private final ReportService reportService = new ReportService();
    private final AllocationService allocationService = new AllocationService();
    private final WaitingListService waitingListService = new WaitingListService();

    public void show() throws SQLException {

        boolean stay = true;
        while (stay) {
            System.out.println("\n------------------- REPORTS -------------------");
            System.out.println("   1. Hostel summary (counts and occupancy)");
            System.out.println("   2. Students without a room");
            System.out.println("   3. Student-wise allocation");
            System.out.println("   4. Room-wise student list");
            System.out.println("   5. Block-wise vacancy");
            System.out.println("   6. Available rooms");
            System.out.println("   7. Partially occupied rooms");
            System.out.println("   8. Full rooms");
            System.out.println("   9. Rooms under maintenance");
            System.out.println("  10. Pending fees");
            System.out.println("  11. Complaint statistics");
            System.out.println("  12. Full allocation history");
            System.out.println("  13. Waiting list order (FIFO vs priority)");
            System.out.println("  14. Back to main menu");
            System.out.println("-----------------------------------------------");

            int choice = InputUtil.readIntInRange("Enter choice (1-14): ", 1, 14);

            switch (choice) {
                case 1: reportService.printSummary(); InputUtil.pause(); break;
                case 2: reportService.printStudentsWithoutRoom(); InputUtil.pause(); break;
                case 3: reportService.printStudentWiseAllocation(); InputUtil.pause(); break;
                case 4: reportService.printRoomWiseStudents(); InputUtil.pause(); break;
                case 5: reportService.printBlockWiseVacancy(); InputUtil.pause(); break;
                case 6: reportService.printRoomsByStatus(Room.AVAILABLE);
                        InputUtil.pause(); break;
                case 7: reportService.printRoomsByStatus(Room.PARTIALLY_OCCUPIED);
                        InputUtil.pause(); break;
                case 8: reportService.printRoomsByStatus(Room.FULL);
                        InputUtil.pause(); break;
                case 9: reportService.printRoomsByStatus(Room.MAINTENANCE);
                        InputUtil.pause(); break;
                case 10: reportService.printPendingFees(); InputUtil.pause(); break;
                case 11: reportService.printComplaintStatistics(); InputUtil.pause(); break;
                case 12: printAllocationHistory(); break;
                case 13: waitingListService.compareQueueOrders(); InputUtil.pause(); break;
                case 14: stay = false; break;
                default: break;
            }
        }
    }

    /**
     * Every allocation ever made - active, transferred and checked out.
     * Nothing is deleted from the allocations table, so this is the
     * complete stay history of the hostel.
     */
    private void printAllocationHistory() throws SQLException {

        ArrayList<Allocation> all = allocationService.getAllAllocations();

        System.out.println("\n===== FULL ALLOCATION HISTORY  (" + all.size() + ") =====");
        if (all.isEmpty()) {
            System.out.println("No allocation has been made yet.");
            InputUtil.pause();
            return;
        }

        System.out.println(all.get(0).header());
        System.out.println("------------------------------------------------------"
                + "--------------------------------------");
        for (int i = 0; i < all.size(); i++) {
            all.get(i).display();
        }

        System.out.println("\nACTIVE      = the student is living there now");
        System.out.println("TRANSFERRED = moved to another room on the checkout date");
        System.out.println("CHECKED_OUT = left the hostel on the checkout date");
        InputUtil.pause();
    }
}
