package com.hostel.exception;

/**
 * Thrown when student data fails validation
 * (bad name, bad register number, bad phone, bad email, bad year...).
 *
 * It extends Exception (not RuntimeException), so it is a CHECKED exception:
 * the compiler forces the caller to either catch it or declare "throws".
 * That is exactly what we want for user input problems - they must be handled.
 */
public class InvalidStudentDataException extends Exception {

    public InvalidStudentDataException(String message) {
        super(message);          // super() passes the message up to Exception
    }
}
