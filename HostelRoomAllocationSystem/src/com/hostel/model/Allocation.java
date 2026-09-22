package com.hostel.model;

import java.time.LocalDate;

/**
 * One row of the allocations table: which student lives in which room.
 *
 * Old rows are never deleted. When a student is transferred the old row is
 * marked TRANSFERRED and a new row is inserted, so this table is also the
 * full stay history and transfer history of every student.
 */
public class Allocation implements Displayable {

    public static final String ACTIVE = "ACTIVE";
    public static final String CHECKED_OUT = "CHECKED_OUT";
    public static final String TRANSFERRED = "TRANSFERRED";

    private int allocationId;
    private int studentId;
    private int roomId;
    private LocalDate allocationDate;
    private LocalDate checkoutDate;    // null while the student is still staying
    private String status;

    // Extra read-only fields filled by SQL JOINs, so reports can print
    // names instead of raw id numbers. They are not stored in the table.
    private String registerNumber = "";
    private String studentName = "";
    private String roomNumber = "";
    private String blockName = "";

    public Allocation() {
        this.status = ACTIVE;
        this.allocationDate = LocalDate.now();
    }

    public Allocation(int studentId, int roomId) {
        this.studentId = studentId;
        this.roomId = roomId;
        this.allocationDate = LocalDate.now();
        this.checkoutDate = null;
        this.status = ACTIVE;
    }

    public Allocation(int allocationId, int studentId, int roomId,
                      LocalDate allocationDate, LocalDate checkoutDate, String status) {
        this.allocationId = allocationId;
        this.studentId = studentId;
        this.roomId = roomId;
        this.allocationDate = allocationDate;
        this.checkoutDate = checkoutDate;
        this.status = status;
    }

    public boolean isActive() {
        return ACTIVE.equals(status);
    }

    public int getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(int allocationId) {
        this.allocationId = allocationId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public LocalDate getAllocationDate() {
        return allocationDate;
    }

    public void setAllocationDate(LocalDate allocationDate) {
        this.allocationDate = allocationDate;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(LocalDate checkoutDate) {
        this.checkoutDate = checkoutDate;
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

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    @Override
    public String header() {
        return String.format("%-6s %-12s %-18s %-10s %-14s %-12s %-12s %-12s",
                "ALLOC", "REG NO", "NAME", "ROOM", "BLOCK", "ALLOC DATE",
                "CHECKOUT", "STATUS");
    }

    @Override
    public void display() {
        String out = (checkoutDate == null) ? "-" : checkoutDate.toString();
        System.out.println(String.format("%-6d %-12s %-18s %-10s %-14s %-12s %-12s %-12s",
                allocationId, registerNumber, studentName, roomNumber, blockName,
                allocationDate, out, status));
    }

    @Override
    public String toString() {
        return "Allocation{id=" + allocationId + ", studentId=" + studentId
                + ", roomId=" + roomId + ", status=" + status + "}";
    }
}
