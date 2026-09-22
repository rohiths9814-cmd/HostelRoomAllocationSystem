package com.hostel.main;

import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.Complaint;
import com.hostel.model.Student;
import com.hostel.service.ComplaintService;
import com.hostel.service.StudentService;
import com.hostel.util.InputUtil;

import java.sql.SQLException;
import java.util.ArrayList;

/** MODULE 9 - complaint management. */
public class ComplaintMenu {

    private final ComplaintService complaintService = new ComplaintService();
    private final StudentService studentService = new StudentService();

    public void show() throws SQLException {

        boolean stay = true;
        while (stay) {
            System.out.println("\n------------- COMPLAINT MANAGEMENT -------------");
            System.out.println("  1. Create a complaint");
            System.out.println("  2. View all complaints");
            System.out.println("  3. View PENDING complaints");
            System.out.println("  4. View IN_PROGRESS complaints");
            System.out.println("  5. View RESOLVED complaints");
            System.out.println("  6. View complaints of one student");
            System.out.println("  7. Update a complaint status");
            System.out.println("  8. Resolve a complaint");
            System.out.println("  9. Back to main menu");
            System.out.println("------------------------------------------------");

            int choice = InputUtil.readIntInRange("Enter choice (1-9): ", 1, 9);

            switch (choice) {
                case 1: createComplaint(); break;
                case 2: printList(complaintService.getAllComplaints(),
                                  "ALL COMPLAINTS"); break;
                case 3: printList(complaintService.getComplaintsByStatus(Complaint.PENDING),
                                  "PENDING COMPLAINTS"); break;
                case 4: printList(complaintService.getComplaintsByStatus(
                                  Complaint.IN_PROGRESS), "IN PROGRESS"); break;
                case 5: printList(complaintService.getComplaintsByStatus(Complaint.RESOLVED),
                                  "RESOLVED COMPLAINTS"); break;
                case 6: viewByStudent(); break;
                case 7: updateStatus(); break;
                case 8: resolveComplaint(); break;
                case 9: stay = false; break;
                default: break;
            }
        }
    }

    private void createComplaint() throws SQLException {

        System.out.println("\n----- NEW COMPLAINT -----");

        int studentId = InputUtil.readInt("Student ID : ");

        Student student;
        try {
            student = studentService.getStudentById(studentId);
        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
            InputUtil.pause();
            return;
        }

        System.out.println("Raising a complaint for: " + student.getName());
        System.out.println("\nCategories:");
        for (int i = 0; i < Complaint.CATEGORIES.length; i++) {
            System.out.println("   " + (i + 1) + ". " + Complaint.CATEGORIES[i]);
        }

        int categoryChoice = InputUtil.readIntInRange(
                "Choose a category (1-" + Complaint.CATEGORIES.length + "): ",
                1, Complaint.CATEGORIES.length);
        String category = Complaint.CATEGORIES[categoryChoice - 1];

        String description = InputUtil.readNonEmptyString("Description: ");

        try {
            int id = complaintService.createComplaint(studentId, category, description);
            if (id > 0) {
                System.out.println("\nComplaint registered. Complaint ID = " + id);
                System.out.println("Status: PENDING");
            }
        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (InvalidStudentDataException e) {
            System.out.println("[INVALID DATA] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void viewByStudent() throws SQLException {

        int studentId = InputUtil.readInt("\nEnter student ID: ");
        ArrayList<Complaint> complaints = complaintService.getComplaintsByStudent(studentId);
        printList(complaints, "COMPLAINTS OF STUDENT " + studentId);
    }

    private void updateStatus() throws SQLException {

        ArrayList<Complaint> open = complaintService.getAllComplaints();
        if (open.isEmpty()) {
            System.out.println("\nThere are no complaints yet.");
            InputUtil.pause();
            return;
        }

        printListNoPause(open, "ALL COMPLAINTS");

        int complaintId = InputUtil.readInt("\nEnter the complaint ID: ");

        System.out.println("New status:");
        System.out.println("   1. PENDING");
        System.out.println("   2. IN_PROGRESS");
        System.out.println("   3. RESOLVED");
        int choice = InputUtil.readIntInRange("Choose (1-3): ", 1, 3);

        String newStatus = Complaint.PENDING;
        if (choice == 2) {
            newStatus = Complaint.IN_PROGRESS;
        } else if (choice == 3) {
            newStatus = Complaint.RESOLVED;
        }

        try {
            if (complaintService.updateStatus(complaintId, newStatus)) {
                System.out.println("Status changed to " + newStatus + ".");
            }
        } catch (InvalidStudentDataException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void resolveComplaint() throws SQLException {

        ArrayList<Complaint> pending = complaintService.getComplaintsByStatus(
                Complaint.PENDING);
        ArrayList<Complaint> inProgress = complaintService.getComplaintsByStatus(
                Complaint.IN_PROGRESS);

        if (pending.isEmpty() && inProgress.isEmpty()) {
            System.out.println("\nThere is nothing left to resolve.");
            InputUtil.pause();
            return;
        }

        if (!pending.isEmpty()) {
            printListNoPause(pending, "PENDING");
        }
        if (!inProgress.isEmpty()) {
            printListNoPause(inProgress, "IN PROGRESS");
        }

        int complaintId = InputUtil.readInt("\nEnter the complaint ID to resolve: ");

        try {
            if (complaintService.resolveComplaint(complaintId)) {
                System.out.println("Complaint " + complaintId + " is now RESOLVED.");
            }
        } catch (InvalidStudentDataException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void printList(ArrayList<Complaint> complaints, String title) {
        printListNoPause(complaints, title);
        InputUtil.pause();
    }

    private void printListNoPause(ArrayList<Complaint> complaints, String title) {

        System.out.println("\n===== " + title + "  (" + complaints.size() + ") =====");
        if (complaints.isEmpty()) {
            System.out.println("Nothing to show.");
            return;
        }

        System.out.println(complaints.get(0).header());
        System.out.println("------------------------------------------------------"
                + "--------------------------------------");
        for (int i = 0; i < complaints.size(); i++) {
            complaints.get(i).display();
        }
    }
}
