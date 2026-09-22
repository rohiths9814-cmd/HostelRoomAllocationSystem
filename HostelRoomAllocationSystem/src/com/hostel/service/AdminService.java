package com.hostel.service;

import com.hostel.annotation.AdminOperation;
import com.hostel.dao.AdminDAO;
import com.hostel.model.Admin;
import com.hostel.model.StudentUser;
import com.hostel.model.User;
import com.hostel.util.Validation;

import java.sql.SQLException;

/**
 * Login / logout and the "who is using the program right now" state.
 *
 * A SERVICE class holds BUSINESS RULES. It decides what is allowed.
 * It never writes SQL itself - it asks a DAO to do that.
 */
public class AdminService {

    private final AdminDAO adminDAO = new AdminDAO();

    /** null when nobody is logged in. */
    private Admin loggedInAdmin = null;

    /**
     * Checks the username and password.
     *
     * Step 1 - REGEX validation (is the text even shaped like a username?)
     * Step 2 - database check (does that account exist?)
     *
     * Doing step 1 first means obviously wrong input never reaches MySQL.
     */
    public boolean login(String username, String password) throws SQLException {

        if (!Validation.isValidUsername(username)) {
            System.out.println("Invalid username format.");
            System.out.println("It must start with a letter and be 4-20 characters.");
            return false;
        }

        if (!Validation.isValidPassword(password)) {
            System.out.println("Invalid password format.");
            System.out.println("It must be 6-20 characters with at least one letter "
                    + "and one digit.");
            return false;
        }

        Admin admin = adminDAO.findByUsernameAndPassword(username, password);
        if (admin == null) {
            System.out.println("Login failed. Username or password is wrong.");
            return false;
        }

        this.loggedInAdmin = admin;
        return true;
    }

    public void logout() {
        this.loggedInAdmin = null;
    }

    public boolean isLoggedIn() {
        return loggedInAdmin != null;
    }

    public Admin getLoggedInAdmin() {
        return loggedInAdmin;
    }

    public String getLoggedInName() {
        if (loggedInAdmin == null) {
            return "guest";
        }
        return loggedInAdmin.getUsername();
    }

    /**
     * Prints who is logged in.
     *
     * The variable is declared as User but holds an Admin. When
     * displayRole() is called, Java looks at the REAL object at RUNTIME and
     * runs the Admin version. That is POLYMORPHISM / dynamic dispatch.
     */
    @AdminOperation(value = "Show the current session", modifiesData = false)
    public void showCurrentUser() {
        if (loggedInAdmin == null) {
            System.out.println("Nobody is logged in.");
            return;
        }
        User user = loggedInAdmin;      // upcasting: Admin reference -> User reference
        user.showBasicInfo();
        user.displayRole();
    }

    /**
     * A tiny demonstration for the viva: the SAME loop, the SAME method
     * call, two different outputs, chosen by the real object type.
     */
    public void demonstratePolymorphism() {
        User[] users = {
            new Admin(1, "admin", "admin123"),
            new StudentUser(2, "rohith", "26CSE001")
        };

        System.out.println("\n--- Polymorphism demo (same call, different behaviour) ---");
        for (int i = 0; i < users.length; i++) {
            users[i].showBasicInfo();      // inherited from User
            users[i].displayRole();        // overridden in each subclass
            System.out.println();
        }
    }
}
