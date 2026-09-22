package com.hostel.exception;

/** Thrown when a register number that already exists is entered again. */
public class DuplicateRegisterNumberException extends Exception {

    public DuplicateRegisterNumberException(String message) {
        super(message);
    }
}
