package com.hostel.exception;

/**
 * Thrown when we try to allocate a student to a room that has no free bed,
 * or a room that is under MAINTENANCE.
 */
public class RoomFullException extends Exception {

    public RoomFullException(String message) {
        super(message);
    }
}
