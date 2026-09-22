package com.hostel.model;

import java.time.LocalDate;

/**
 * One hostel fee record for one student.
 *
 * The amount is stored as double here because the syllabus asks for the
 * Double wrapper. In a real banking application you would use BigDecimal,
 * because double cannot represent money exactly (0.1 + 0.2 is not 0.3).
 * For a college project double is fine - but know the difference.
 */
public class HostelFee implements Displayable {

    public static final String PAID = "PAID";
    public static final String PENDING = "PENDING";

    private int feeId;
    private int studentId;
    private double amount;
    private LocalDate paymentDate;     // null while unpaid
    private String status;

    // filled by a JOIN for display only
    private String registerNumber = "";
    private String studentName = "";

    public HostelFee() {
        this.status = PENDING;
        this.amount = 0.0;
    }

    public HostelFee(int studentId, double amount) {
        this.studentId = studentId;
        this.amount = amount;
        this.paymentDate = null;
        this.status = PENDING;
    }

    public HostelFee(int feeId, int studentId, double amount,
                     LocalDate paymentDate, String status) {
        this.feeId = feeId;
        this.studentId = studentId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.status = status;
    }

    public boolean isPaid() {
        return PAID.equals(status);
    }

    /** Marks the fee paid today. */
    public void markPaid() {
        this.status = PAID;
        this.paymentDate = LocalDate.now();
    }

    public int getFeeId() {
        return feeId;
    }

    public void setFeeId(int feeId) {
        this.feeId = feeId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public String header() {
        return String.format("%-6s %-12s %-20s %-12s %-14s %-10s",
                "FEEID", "REG NO", "NAME", "AMOUNT", "PAID ON", "STATUS");
    }

    @Override
    public void display() {
        String paid = (paymentDate == null) ? "-" : paymentDate.toString();
        System.out.println(String.format("%-6d %-12s %-20s %-12.2f %-14s %-10s",
                feeId, registerNumber, studentName, amount, paid, status));
    }

    @Override
    public String toString() {
        return "HostelFee{id=" + feeId + ", amount=" + amount + ", status=" + status + "}";
    }
}
