package com.hostel.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All REGEX validation of the project lives here in one place.
 *
 * Pattern  = the compiled rule ("what a valid phone number looks like").
 * Matcher  = the engine that tests one piece of text against that rule.
 *
 * The patterns are compiled ONCE into static final fields, because
 * Pattern.compile() is the slow part. Matching afterwards is cheap.
 */
public class Validation {

    // ----------------------------------------------------------
    // THE PATTERNS
    // ----------------------------------------------------------

    /** Name: starts with a letter, then letters / spaces / dots. 2-50 chars. */
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z. ]{1,49}$");

    /** Register number: 2 digits + 2-4 capital letters + 3 digits.  26CSE001 */
    private static final Pattern REGISTER_PATTERN =
            Pattern.compile("^[0-9]{2}[A-Z]{2,4}[0-9]{3}$");

    /** Indian mobile: starts 6/7/8/9, exactly 10 digits total. */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[6-9][0-9]{9}$");

    /**
     * Email: something@something.something
     * [.] is a character class holding one dot - it matches a LITERAL dot.
     * The other way is an escaped dot, but inside a Java String the escape
     * character must itself be escaped, which is easy to get wrong.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+[.][A-Za-z]{2,}$");

    /** Username: starts with a letter, then letters/digits/underscore, 4-20 chars. */
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z0-9_]{3,19}$");

    /**
     * Password: 6-20 characters, must contain at least one letter and one digit.
     * (?=.*[A-Za-z]) is a "lookahead": peek ahead and make sure a letter exists
     * somewhere, without consuming any characters.
     */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z0-9@#$_.]{6,20}$");

    /** Room number: 1-2 letters, a hyphen, 3 digits.   A-101 */
    private static final Pattern ROOM_PATTERN =
            Pattern.compile("^[A-Z]{1,2}-[0-9]{3}$");

    /** Department: letters and spaces only, 2-30 chars.  CSE / MECH / CIVIL */
    private static final Pattern DEPARTMENT_PATTERN =
            Pattern.compile("^[A-Za-z ]{2,30}$");

    /** Block name: letters, digits and spaces.  "Block A" */
    private static final Pattern BLOCK_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z0-9 ]{1,29}$");

    /** Amount: up to 8 digits, optionally 1-2 decimal places.  45000  45000.50 */
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("^[0-9]{1,8}([.][0-9]{1,2})?$");

    // ----------------------------------------------------------
    // THE CHECK METHODS
    // Every one returns a boolean (a primitive), and uses Matcher.
    // ----------------------------------------------------------

    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        Matcher matcher = NAME_PATTERN.matcher(name.trim());
        return matcher.matches();       // matches() = the WHOLE text must fit
    }

    public static boolean isValidRegisterNumber(String registerNumber) {
        if (registerNumber == null) {
            return false;
        }
        Matcher matcher = REGISTER_PATTERN.matcher(registerNumber.trim().toUpperCase());
        return matcher.matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        Matcher matcher = PHONE_PATTERN.matcher(phone.trim());
        return matcher.matches();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email.trim());
        return matcher.matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidRoomNumber(String roomNumber) {
        if (roomNumber == null) {
            return false;
        }
        return ROOM_PATTERN.matcher(roomNumber.trim().toUpperCase()).matches();
    }

    public static boolean isValidDepartment(String department) {
        if (department == null) {
            return false;
        }
        return DEPARTMENT_PATTERN.matcher(department.trim()).matches();
    }

    public static boolean isValidBlockName(String blockName) {
        if (blockName == null) {
            return false;
        }
        return BLOCK_PATTERN.matcher(blockName.trim()).matches();
    }

    public static boolean isValidAmount(String amount) {
        if (amount == null) {
            return false;
        }
        return AMOUNT_PATTERN.matcher(amount.trim()).matches();
    }

    // ----------------------------------------------------------
    // Checks that are simpler with plain if/else than with regex.
    // Regex is for PATTERNS in text, not for number ranges.
    // ----------------------------------------------------------

    /** Year of study must be 1, 2, 3 or 4. */
    public static boolean isValidYear(int year) {
        return year >= 1 && year <= 4;
    }

    /** Gender must be MALE or FEMALE (case does not matter). */
    public static boolean isValidGender(String gender) {
        if (gender == null) {
            return false;
        }
        String g = gender.trim().toUpperCase();
        return g.equals("MALE") || g.equals("FEMALE");
    }

    /** Room capacity: at least 1 bed, at most 6 beds. */
    public static boolean isValidCapacity(int capacity) {
        return capacity >= 1 && capacity <= 6;
    }

    /** Waiting-list priority: 1 (highest) to 5 (lowest). */
    public static boolean isValidPriority(int priority) {
        return priority >= 1 && priority <= 5;
    }
}
