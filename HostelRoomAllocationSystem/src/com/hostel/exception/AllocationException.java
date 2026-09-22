package com.hostel.exception;

/**
 * Thrown for allocation rule violations, for example:
 *  - the student already has an active room
 *  - the student has no active allocation but tries to check out
 *  - boy being allocated to a girls' block
 */
public class AllocationException extends Exception {

    public AllocationException(String message) {
        super(message);
    }
}
