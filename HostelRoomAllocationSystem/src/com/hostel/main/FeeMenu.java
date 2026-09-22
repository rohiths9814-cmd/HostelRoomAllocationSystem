package com.hostel.main;

import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.HostelFee;
import com.hostel.model.Student;
import com.hostel.service.FeeService;
import com.hostel.service.StudentService;
import com.hostel.util.InputUtil;

import java.sql.SQLException;
import java.util.ArrayList;

/** MODULE 10 - hostel fee management. */
public class FeeMenu {

    private final FeeService feeService = new FeeService();
    private final StudentService studentService = new StudentService();

    public void show() throws SQLException {

        boolean stay = true;
        while (stay) {
            System.out.println("\n---------------- FEE MANAGEMENT ----------------");
            System.out.println("  1. Add a fee record");
            System.out.println("  2. View all fees");
            System.out.println("  3. View PENDING fees");
            System.out.println("  4. View PAID fees");
            System.out.println("  5. Payment history of one student");
            System.out.println("  6. Mark a fee as PAID");
            System.out.println("  7. Fee summary");
            System.out.println("  8. Back to main menu");
            System.out.println("------------------------------------------------");

            int choice = InputUtil.readIntInRange("Enter choice (1-8): ", 1, 8);

            switch (choice) {
                case 1: addFee(); break;
                case 2: printList(feeService.getAllFees(), "ALL FEE RECORDS"); break;
                case 3: printList(feeService.getPendingFees(), "PENDING FEES"); break;
                case 4: printList(feeService.getPaidFees(), "PAID FEES"); break;
                case 5: viewStudentHistory(); break;
                case 6: markPaid(); break;
                case 7: printSummary(); break;
                case 8: stay = false; break;
                default: break;
            }
        }
    }

    private void addFee() throws SQLException {

        System.out.println("\n----- ADD FEE RECORD -----");

        int studentId = InputUtil.readInt("Student ID : ");

        Student student;
        try {
            student = studentService.getStudentById(studentId);
        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
            InputUtil.pause();
            return;
        }

        System.out.println("Adding a fee for: " + student.getName());

        // readDouble() uses Double.valueOf() and catches NumberFormatException,
        // so typing "abc" here just re-asks instead of crashing the program.
        double amount = InputUtil.readDouble("Amount     : ");

        try {
            int id = feeService.addFee(studentId, amount);
            if (id > 0) {
                System.out.println("\nFee record created. Fee ID = " + id);
                System.out.println("Status: PENDING");
            }
        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (InvalidStudentDataException e) {
            System.out.println("[INVALID DATA] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void viewStudentHistory() throws SQLException {

        int studentId = InputUtil.readInt("\nEnter student ID: ");

        try {
            ArrayList<HostelFee> fees = feeService.getFeesByStudent(studentId);
            printListNoPause(fees, "PAYMENT HISTORY OF STUDENT " + studentId);

            double paid = 0.0;
            double pending = 0.0;
            for (int i = 0; i < fees.size(); i++) {
                HostelFee fee = fees.get(i);
                if (fee.isPaid()) {
                    paid = paid + fee.getAmount();
                } else {
                    pending = pending + fee.getAmount();
                }
            }

            System.out.println("\n  Total paid    : " + String.format("%.2f", paid));
            System.out.println("  Total pending : " + String.format("%.2f", pending));

        } catch (StudentNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void markPaid() throws SQLException {

        ArrayList<HostelFee> pending = feeService.getPendingFees();
        if (pending.isEmpty()) {
            System.out.println("\nThere are no pending fees.");
            InputUtil.pause();
            return;
        }

        printListNoPause(pending, "PENDING FEES");

        int feeId = InputUtil.readInt("\nEnter the fee ID that was paid: ");

        try {
            if (feeService.markAsPaid(feeId)) {
                System.out.println("Payment recorded. Status is now PAID.");
            }
        } catch (InvalidStudentDataException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
        InputUtil.pause();
    }

    private void printSummary() throws SQLException {

        System.out.println("\n============== FEE SUMMARY ==============");
        System.out.println("  Paid records    : " + feeService.countPaid());
        System.out.println("  Pending records : " + feeService.countPending());
        System.out.println("  Amount collected: "
                + String.format("%.2f", feeService.getTotalCollected()));
        System.out.println("  Amount pending  : "
                + String.format("%.2f", feeService.getTotalPending()));
        System.out.println("=========================================");
        InputUtil.pause();
    }

    private void printList(ArrayList<HostelFee> fees, String title) {
        printListNoPause(fees, title);
        InputUtil.pause();
    }

    private void printListNoPause(ArrayList<HostelFee> fees, String title) {

        System.out.println("\n===== " + title + "  (" + fees.size() + ") =====");
        if (fees.isEmpty()) {
            System.out.println("Nothing to show.");
            return;
        }

        System.out.println(fees.get(0).header());
        System.out.println("------------------------------------------------------"
                + "--------------------");
        for (int i = 0; i < fees.size(); i++) {
            fees.get(i).display();
        }
    }
}
