package com.hostel.exception;

/** Thrown when a room id / room number does not exist in the database. */
public class RoomNotFoundException extends Exception {

    public RoomNotFoundException(String message) {
        super(message);
    }
}
