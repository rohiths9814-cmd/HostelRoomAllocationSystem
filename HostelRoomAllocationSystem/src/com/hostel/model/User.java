package com.hostel.model;

/**
 * ABSTRACTION.
 *
 * "User" is a real idea in our system - both an Admin and a StudentUser are
 * users - but nobody is JUST a user. So the class is declared abstract:
 * it can be inherited from, but `new User()` is a compile error.
 *
 * It holds the state every user shares (id + username) and declares one
 * abstract method that every subclass is forced to answer for itself.
 */
public abstract class User {

    // protected = visible to this class AND to its subclasses,
    // but still hidden from unrelated classes.
    protected int userId;
    protected String username;

    /** Default constructor. */
    public User() {
        this.userId = 0;
        this.username = "guest";
    }

    /** Parameterised constructor - CONSTRUCTOR OVERLOADING. */
    public User(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    /**
     * ABSTRACT METHOD: no body here. Every concrete subclass must override it.
     * This is how the parent forces a rule on its children.
     */
    public abstract void displayRole();

    /** Concrete method - inherited as-is unless a subclass overrides it. */
    public void showBasicInfo() {
        System.out.println("User ID  : " + userId);
        System.out.println("Username : " + username);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
