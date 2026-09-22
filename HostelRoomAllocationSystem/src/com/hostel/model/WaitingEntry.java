package com.hostel.model;

import java.time.LocalDate;

/**
 * One student waiting for a room.
 *
 * It implements Comparable so a PriorityQueue knows how to order entries:
 * priority 1 is served before priority 5, and if two students have the same
 * priority the one who applied earlier wins.
 */
public class WaitingEntry implements Displayable, Comparable<WaitingEntry> {

    public static final String WAITING = "WAITING";
    public static final String ALLOCATED = "ALLOCATED";
    public static final String REMOVED = "REMOVED";

    private int waitingId;
    private int studentId;
    private LocalDate requestDate;
    private int priority;              // 1 = highest, 5 = lowest
    private String status;

    // filled by a JOIN for display only
    private String registerNumber = "";
    private String studentName = "";

    public WaitingEntry() {
        this.priority = 5;
        this.status = WAITING;
        this.requestDate = LocalDate.now();
    }

    public WaitingEntry(int studentId, int priority) {
        this.studentId = studentId;
        this.priority = priority;
        this.requestDate = LocalDate.now();
        this.status = WAITING;
    }

    public WaitingEntry(int waitingId, int studentId, LocalDate requestDate,
                        int priority, String status) {
        this.waitingId = waitingId;
        this.studentId = studentId;
        this.requestDate = requestDate;
        this.priority = priority;
        this.status = status;
    }

    /**
     * Tells a PriorityQueue how to compare two waiting students.
     * Return a negative number if THIS entry should come out first.
     */
    @Override
    public int compareTo(WaitingEntry other) {
        if (this.priority != other.priority) {
            return this.priority - other.priority;      // smaller priority first
        }
        return this.requestDate.compareTo(other.requestDate);  // then older first
    }

    public int getWaitingId() {
        return waitingId;
    }

    public void setWaitingId(int waitingId) {
        this.waitingId = waitingId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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
        return String.format("%-6s %-12s %-20s %-14s %-10s %-12s",
                "WAITID", "REG NO", "NAME", "REQUESTED ON", "PRIORITY", "STATUS");
    }

    @Override
    public void display() {
        System.out.println(String.format("%-6d %-12s %-20s %-14s %-10d %-12s",
                waitingId, registerNumber, studentName, requestDate, priority, status));
    }

    @Override
    public String toString() {
        return "WaitingEntry{id=" + waitingId + ", studentId=" + studentId
                + ", priority=" + priority + "}";
    }
}
