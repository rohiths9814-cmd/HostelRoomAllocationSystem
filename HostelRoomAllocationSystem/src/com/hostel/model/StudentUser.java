package com.hostel.model;

/**
 * The second child of User. It exists to prove POLYMORPHISM:
 *
 *     User u = new Admin(...);        u.displayRole();  -> ADMINISTRATOR
 *     User u = new StudentUser(...);  u.displayRole();  -> STUDENT
 *
 * Same reference type, same method call, different behaviour decided at
 * RUNTIME by the real object. That is dynamic method dispatch.
 */
public class StudentUser extends User {

    private String registerNumber;

    public StudentUser() {
        super();
        this.registerNumber = "NOT-SET";
    }

    public StudentUser(int userId, String username, String registerNumber) {
        super(userId, username);
        this.registerNumber = registerNumber;
    }

    @Override
    public void displayRole() {
        System.out.println("Role : STUDENT (can raise complaints and view own details)");
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    @Override
    public String toString() {
        return "StudentUser{id=" + userId + ", regNo='" + registerNumber + "'}";
    }
}
