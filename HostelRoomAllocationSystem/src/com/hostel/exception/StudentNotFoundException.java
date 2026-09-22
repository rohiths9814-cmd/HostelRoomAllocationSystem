package com.hostel.exception;

/** Thrown when a student id / register number does not exist in the database. */
public class StudentNotFoundException extends Exception {

    public StudentNotFoundException(String message) {
        super(message);
    }
}
