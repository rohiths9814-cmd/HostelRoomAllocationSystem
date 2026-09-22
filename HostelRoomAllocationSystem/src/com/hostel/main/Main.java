package com.hostel.main;

import com.hostel.service.AdminService;
import com.hostel.util.DBConnection;
import com.hostel.util.InputUtil;

import java.sql.SQLException;

/**
 * The entry point of the whole program.
 *
 * Main is only allowed to do three things:
 *      1. print menus
 *      2. read what the user typed
 *      3. call a service
 *
 * There is NO SQL here and NO business rule here. That separation is what
 * makes the project easy to explain in a viva:
 *
 *      Main  ->  Service  ->  DAO  ->  MySQL
 *      (menu)    (rules)      (SQL)
 */
public class Main {

    private static final AdminService adminService = new AdminService();

    private static final StudentMenu studentMenu = new StudentMenu();
    private static final HostelRoomMenu hostelRoomMenu = new HostelRoomMenu();
    private static final AllocationMenu allocationMenu = new AllocationMenu();
    private static final ComplaintMenu complaintMenu = new ComplaintMenu();
    private static final FeeMenu feeMenu = new FeeMenu();
    private static final ReportMenu reportMenu = new ReportMenu();

    public static void main(String[] args) {

        printBanner();

        boolean running = true;
        while (running) {
            try {
                if (adminService.isLoggedIn()) {
                    running = showMainMenu();
                } else {
                    running = showLoginMenu();
                }
            } catch (SQLException e) {
                // One safety net around everything. A database problem
                // prints a readable line instead of a stack trace.
                System.out.println("\n[DATABASE ERROR] " + e.getMessage());
                System.out.println("Check that MySQL is running and that the settings");
                System.out.println("in DBConnection.java are correct.");
                InputUtil.pause();
            }
        }

        InputUtil.close();
        System.out.println("\nThank you for using the Hostel Management System. Goodbye.");
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("=========================================================");
        System.out.println("      HOSTEL ROOM ALLOCATION AND MANAGEMENT SYSTEM       ");
        System.out.println("                  Console Application                    ");
        System.out.println("=========================================================");

        // Assertions are OFF unless the program is started with -ea.
        // This little trick detects that and tells the user.
        boolean assertionsOn = false;
        assert assertionsOn = true;        // only runs when -ea is given
        if (assertionsOn) {
            System.out.println("  [assertions are ENABLED - development mode]");
        } else {
            System.out.println("  [assertions are OFF - start with -ea to enable them]");
        }
    }

    // ==========================================================
    // LOGIN MENU
    // ==========================================================

    /** Returns false when the user chooses to exit the program. */
    private static boolean showLoginMenu() throws SQLException {

        System.out.println("\n---------------------------------------------------------");
        System.out.println("  1. Admin Login");
        System.out.println("  2. Test database connection");
        System.out.println("  3. Exit");
        System.out.println("---------------------------------------------------------");

        int choice = InputUtil.readIntInRange("Enter choice (1-3): ", 1, 3);

        switch (choice) {
            case 1:
                doLogin();
                return true;
            case 2:
                System.out.println();
                DBConnection.testConnection();
                InputUtil.pause();
                return true;
            case 3:
                return false;
            default:
                return true;
        }
    }

    private static void doLogin() throws SQLException {

        System.out.println("\n----------------- ADMIN LOGIN -----------------");
        String username = InputUtil.readNonEmptyString("Username : ");
        String password = InputUtil.readNonEmptyString("Password : ");

        if (adminService.login(username, password)) {
            System.out.println("\nLogin successful. Welcome, " + adminService.getLoggedInName() + ".");
            adminService.showCurrentUser();
        }
        InputUtil.pause();
    }

    // ==========================================================
    // MAIN MENU
    // ==========================================================

    private static boolean showMainMenu() throws SQLException {

        System.out.println("\n=========================================================");
        System.out.println("  HOSTEL ROOM ALLOCATION SYSTEM      user: "
                + adminService.getLoggedInName());
        System.out.println("=========================================================");
        System.out.println("   1. Student Management");
        System.out.println("   2. Hostel Block Management");
        System.out.println("   3. Room Management");
        System.out.println("   4. Room Allocation");
        System.out.println("   5. Room Transfer");
        System.out.println("   6. Student Checkout");
        System.out.println("   7. Waiting List");
        System.out.println("   8. Complaint Management");
        System.out.println("   9. Fee Management");
        System.out.println("  10. Reports");
        System.out.println("  11. Logout");
        System.out.println("  12. Exit");
        System.out.println("=========================================================");

        int choice = InputUtil.readIntInRange("Enter choice (1-12): ", 1, 12);

        switch (choice) {
            case 1:
                studentMenu.show();
                break;
            case 2:
                hostelRoomMenu.showBlockMenu();
                break;
            case 3:
                hostelRoomMenu.showRoomMenu();
                break;
            case 4:
                allocationMenu.allocateRoom();
                break;
            case 5:
                allocationMenu.transferStudent();
                break;
            case 6:
                allocationMenu.checkoutStudent();
                break;
            case 7:
                allocationMenu.showWaitingListMenu();
                break;
            case 8:
                complaintMenu.show();
                break;
            case 9:
                feeMenu.show();
                break;
            case 10:
                reportMenu.show();
                break;
            case 11:
                adminService.logout();
                System.out.println("\nYou have been logged out.");
                InputUtil.pause();
                break;
            case 12:
                return false;
            default:
                System.out.println("Unknown choice.");
        }
        return true;
    }
}
