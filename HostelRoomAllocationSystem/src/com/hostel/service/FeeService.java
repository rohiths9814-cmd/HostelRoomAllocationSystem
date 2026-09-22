package com.hostel.service;

import com.hostel.annotation.AdminOperation;
import com.hostel.dao.FeeDAO;
import com.hostel.dao.StudentDAO;
import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.HostelFee;
import com.hostel.model.Student;

import java.sql.SQLException;
import java.util.ArrayList;

/** MODULE 10 - hostel fees. */
public class FeeService {

    private final FeeDAO feeDAO = new FeeDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    /** Fees are capped at a sensible range so a typo cannot create a 5 crore fee. */
    private static final double MIN_AMOUNT = 1000.0;
    private static final double MAX_AMOUNT = 200000.0;

    @AdminOperation("Add a hostel fee record for a student")
    public int addFee(int studentId, double amount)
            throws StudentNotFoundException, InvalidStudentDataException, SQLException {

        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException("No student with ID " + studentId);
        }

        // Autoboxing: the primitive double becomes a Double object here,
        // which lets us call the wrapper methods on it.
        Double boxedAmount = Double.valueOf(amount);
        if (boxedAmount.isNaN() || boxedAmount.isInfinite()) {
            throw new InvalidStudentDataException("That is not a usable amount.");
        }
        if (amount < MIN_AMOUNT || amount > MAX_AMOUNT) {
            throw new InvalidStudentDataException(
                    "Fee amount must be between " + MIN_AMOUNT + " and " + MAX_AMOUNT + ".");
        }

        HostelFee fee = new HostelFee(studentId, amount);
        return feeDAO.insert(fee);
    }

    public HostelFee getFeeById(int feeId) throws SQLException {
        return feeDAO.findById(feeId);
    }

    public ArrayList<HostelFee> getAllFees() throws SQLException {
        return feeDAO.findAll();
    }

    public ArrayList<HostelFee> getPendingFees() throws SQLException {
        return feeDAO.findByStatus(HostelFee.PENDING);
    }

    public ArrayList<HostelFee> getPaidFees() throws SQLException {
        return feeDAO.findByStatus(HostelFee.PAID);
    }

    /** Payment history of a single student. */
    public ArrayList<HostelFee> getFeesByStudent(int studentId)
            throws StudentNotFoundException, SQLException {

        if (studentDAO.findById(studentId) == null) {
            throw new StudentNotFoundException("No student with ID " + studentId);
        }
        return feeDAO.findByStudentId(studentId);
    }

    /**
     * Records a payment.
     * The SQL itself contains "AND status = PENDING", so paying twice is
     * impossible even if two admins click at the same moment.
     */
    @AdminOperation("Record a fee payment")
    public boolean markAsPaid(int feeId) throws InvalidStudentDataException, SQLException {

        HostelFee fee = feeDAO.findById(feeId);
        if (fee == null) {
            throw new InvalidStudentDataException("No fee record with ID " + feeId);
        }
        if (fee.isPaid()) {
            System.out.println("This fee was already paid on " + fee.getPaymentDate() + ".");
            return false;
        }
        return feeDAO.markPaid(feeId);
    }

    @AdminOperation("Delete a fee record")
    public boolean deleteFee(int feeId) throws SQLException {
        return feeDAO.delete(feeId);
    }

    public double getTotalPending() throws SQLException {
        return feeDAO.totalPendingAmount();
    }

    public double getTotalCollected() throws SQLException {
        return feeDAO.totalCollectedAmount();
    }

    public int countPending() throws SQLException {
        return feeDAO.countByStatus(HostelFee.PENDING);
    }

    public int countPaid() throws SQLException {
        return feeDAO.countByStatus(HostelFee.PAID);
    }
}
