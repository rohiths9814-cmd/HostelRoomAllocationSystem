package com.hostel.service;

import com.hostel.annotation.AdminOperation;
import com.hostel.dao.ComplaintDAO;
import com.hostel.dao.StudentDAO;
import com.hostel.exception.InvalidStudentDataException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.Complaint;
import com.hostel.model.Student;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/** MODULE 9 - complaints raised by students. */
public class ComplaintService {

    private final ComplaintDAO complaintDAO = new ComplaintDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    public int createComplaint(int studentId, String category, String description)
            throws StudentNotFoundException, InvalidStudentDataException, SQLException {

        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException("No student with ID " + studentId);
        }
        if (!Complaint.isValidCategory(category)) {
            throw new InvalidStudentDataException(
                    "Category must be one of: ELECTRICITY, WATER, CLEANING, "
                    + "WIFI, FURNITURE, OTHER.");
        }
        if (description == null || description.trim().length() < 5) {
            throw new InvalidStudentDataException(
                    "Description must be at least 5 characters long.");
        }
        if (description.length() > 255) {
            throw new InvalidStudentDataException(
                    "Description is too long (maximum 255 characters).");
        }

        Complaint complaint = new Complaint(studentId,
                category.trim().toUpperCase(), description.trim());
        return complaintDAO.insert(complaint);
    }

    public Complaint getComplaintById(int complaintId) throws SQLException {
        return complaintDAO.findById(complaintId);
    }

    public ArrayList<Complaint> getAllComplaints() throws SQLException {
        return complaintDAO.findAll();
    }

    public ArrayList<Complaint> getComplaintsByStatus(String status) throws SQLException {
        return complaintDAO.findByStatus(status);
    }

    public ArrayList<Complaint> getComplaintsByStudent(int studentId) throws SQLException {
        return complaintDAO.findByStudentId(studentId);
    }

    /**
     * Moves a complaint along: PENDING -> IN_PROGRESS -> RESOLVED.
     * A complaint that is already RESOLVED is not reopened by accident.
     */
    @AdminOperation("Change the status of a complaint")
    public boolean updateStatus(int complaintId, String newStatus)
            throws InvalidStudentDataException, SQLException {

        Complaint complaint = complaintDAO.findById(complaintId);
        if (complaint == null) {
            throw new InvalidStudentDataException(
                    "No complaint found with ID " + complaintId);
        }

        if (!Complaint.PENDING.equals(newStatus)
                && !Complaint.IN_PROGRESS.equals(newStatus)
                && !Complaint.RESOLVED.equals(newStatus)) {
            throw new InvalidStudentDataException(
                    "Status must be PENDING, IN_PROGRESS or RESOLVED.");
        }

        if (Complaint.RESOLVED.equals(complaint.getStatus())
                && !Complaint.RESOLVED.equals(newStatus)) {
            System.out.println("This complaint was already resolved on "
                    + complaint.getResolvedDate() + ".");
            System.out.println("Reopening it is not allowed.");
            return false;
        }

        return complaintDAO.updateStatus(complaintId, newStatus);
    }

    @AdminOperation("Mark a complaint as resolved")
    public boolean resolveComplaint(int complaintId)
            throws InvalidStudentDataException, SQLException {
        return updateStatus(complaintId, Complaint.RESOLVED);
    }

    @AdminOperation("Delete a complaint")
    public boolean deleteComplaint(int complaintId) throws SQLException {
        return complaintDAO.delete(complaintId);
    }

    /** For the reports module. */
    public HashMap<String, Integer> getStatusStatistics() throws SQLException {
        return complaintDAO.countByStatus();
    }

    public HashMap<String, Integer> getCategoryStatistics() throws SQLException {
        return complaintDAO.countByCategory();
    }

    public int countAll() throws SQLException {
        return complaintDAO.countAll();
    }
}
