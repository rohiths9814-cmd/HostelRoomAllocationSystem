package com.hostel.model;

import java.time.LocalDate;

/** A complaint raised by a student about the hostel. */
public class Complaint implements Displayable {

    public static final String PENDING = "PENDING";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String RESOLVED = "RESOLVED";

    /** The only categories the menu accepts. */
    public static final String[] CATEGORIES = {
        "ELECTRICITY", "WATER", "CLEANING", "WIFI", "FURNITURE", "OTHER"
    };

    private int complaintId;
    private int studentId;
    private String category;
    private String description;
    private String status;
    private LocalDate createdDate;
    private LocalDate resolvedDate;

    // filled by a JOIN for display only
    private String registerNumber = "";
    private String studentName = "";

    public Complaint() {
        this.status = PENDING;
        this.createdDate = LocalDate.now();
    }

    public Complaint(int studentId, String category, String description) {
        this.studentId = studentId;
        this.category = category;
        this.description = description;
        this.status = PENDING;
        this.createdDate = LocalDate.now();
        this.resolvedDate = null;
    }

    public Complaint(int complaintId, int studentId, String category, String description,
                     String status, LocalDate createdDate, LocalDate resolvedDate) {
        this.complaintId = complaintId;
        this.studentId = studentId;
        this.category = category;
        this.description = description;
        this.status = status;
        this.createdDate = createdDate;
        this.resolvedDate = resolvedDate;
    }

    /** Checks a typed category against the allowed list. */
    public static boolean isValidCategory(String category) {
        if (category == null) {
            return false;
        }
        String upper = category.trim().toUpperCase();
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(upper)) {
                return true;
            }
        }
        return false;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        this.complaintId = complaintId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDate resolvedDate) {
        this.resolvedDate = resolvedDate;
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
        return String.format("%-5s %-12s %-16s %-13s %-30s %-13s %-12s",
                "ID", "REG NO", "NAME", "CATEGORY", "DESCRIPTION", "STATUS", "RAISED ON");
    }

    @Override
    public void display() {
        String shortDesc = description;
        if (shortDesc != null && shortDesc.length() > 28) {
            shortDesc = shortDesc.substring(0, 27) + "~";
        }
        System.out.println(String.format("%-5d %-12s %-16s %-13s %-30s %-13s %-12s",
                complaintId, registerNumber, studentName, category, shortDesc,
                status, createdDate));
    }

    @Override
    public String toString() {
        return "Complaint{id=" + complaintId + ", category=" + category
                + ", status=" + status + "}";
    }
}
