package com.hostel.model;

/**
 * An INTERFACE is a contract: "any class that implements me MUST have
 * these methods". It contains no data and (here) no method bodies.
 *
 * Student, Room, HostelBlock, Complaint and HostelFee all implement it,
 * so a report can hold a mixed list and call display() on every item
 * without caring what type it really is. That is POLYMORPHISM.
 *
 * Interface vs abstract class (classic viva question):
 *   - a class can implement MANY interfaces, but extend only ONE class
 *   - an interface has no constructor and no instance fields
 *   - use an interface for "can do this", a class for "is a kind of that"
 */
public interface Displayable {

    /** Print this object as one neat console row. */
    void display();

    /** The column header that matches display(). */
    String header();
}
