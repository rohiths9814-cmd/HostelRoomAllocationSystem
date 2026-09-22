package com.hostel.model;

/**
 * INHERITANCE: Admin IS-A User.
 * "extends User" means Admin automatically gets userId, username,
 * showBasicInfo() and the getters/setters, and adds a password of its own.
 */
public class Admin extends User {

    private String password;

    /** No-arg constructor. super() calls User's no-arg constructor first. */
    public Admin() {
        super();
        this.password = "";
    }

    /**
     * super(...) MUST be the first statement: the parent part of the object
     * has to be built before the child part.
     */
    public Admin(int adminId, String username, String password) {
        super(adminId, username);      // builds the User part
        this.password = password;      // builds the Admin part
    }

    /**
     * METHOD OVERRIDING: same name, same parameters, same return type
     * as the abstract method in User - but with a body.
     *
     * @Override is a built-in annotation. It asks the compiler to check that
     * a method really is overriding something. If you mis-spell the name,
     * you get a compile error instead of a silent bug. Always use it.
     */
    @Override
    public void displayRole() {
        System.out.println("Role : ADMINISTRATOR (full access to all modules)");
    }

    /** Overriding a CONCRETE parent method, and calling the parent version too. */
    @Override
    public void showBasicInfo() {
        super.showBasicInfo();         // run User's version first...
        System.out.println("Access   : ALL MODULES");
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Admin{id=" + userId + ", username='" + username + "'}";
    }
}
