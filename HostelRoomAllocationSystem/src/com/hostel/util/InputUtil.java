package com.hostel.util;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Safe console input.
 *
 * The problem this class solves: if the user types "abc" where a number is
 * expected, Integer.parseInt() throws NumberFormatException and the whole
 * program crashes. Here we catch that, print a friendly message and ask again.
 *
 * This class is also where WRAPPER CLASSES show up:
 *   Integer.parseInt(...)  String -> int       (parsing)
 *   Double.parseDouble(..) String -> double
 *   Integer.valueOf(...)   String -> Integer   (an object, not a primitive)
 *   Boolean.valueOf(...)   String -> Boolean
 *   Character.toUpperCase  char helper
 */
public class InputUtil {

    /** One Scanner for the whole program. Opening several on System.in causes bugs. */
    private static final Scanner scanner = new Scanner(System.in);

    /** Reads a line of text exactly as typed. */
    public static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /** Reads a line of text and refuses to accept an empty one. */
    public static String readNonEmptyString(String prompt) {
        while (true) {
            String value = readString(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("  !! This field cannot be left blank. Try again.");
        }
    }

    /** Reads a whole number, re-asking until the text really is a number. */
    public static int readInt(String prompt) {
        while (true) {
            String text = readString(prompt);
            try {
                // parseInt returns the primitive int
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("  !! '" + text + "' is not a whole number. Try again.");
            }
        }
    }

    /** Reads a whole number and also forces it into a range (both ends included). */
    public static int readIntInRange(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt);
            // Autoboxing: the int 'value' becomes an Integer object here,
            // and Integer.valueOf() gives us access to object methods.
            Integer boxed = Integer.valueOf(value);
            if (boxed >= min && boxed <= max) {   // unboxing back to int for the compare
                return boxed;                     // unboxing again on return
            }
            System.out.println("  !! Enter a number between " + min + " and " + max + ".");
        }
    }

    /** Reads a decimal number (used for fee amounts). */
    public static double readDouble(String prompt) {
        while (true) {
            String text = readString(prompt);
            try {
                Double boxed = Double.valueOf(text);   // String -> Double object
                double amount = boxed;                 // unboxing -> primitive
                if (amount < 0) {
                    System.out.println("  !! Amount cannot be negative.");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("  !! '" + text + "' is not a valid amount. Try again.");
            }
        }
    }

    /** Asks a yes/no question. Demonstrates Character and Boolean wrappers. */
    public static boolean readYesNo(String prompt) {
        while (true) {
            String text = readString(prompt + " (y/n): ");
            if (text.isEmpty()) {
                System.out.println("  !! Please type y or n.");
                continue;
            }
            Character first = Character.toUpperCase(text.charAt(0));  // autoboxed char
            if (first == 'Y') {
                return Boolean.TRUE;      // Boolean wrapper constant -> unboxed to true
            }
            if (first == 'N') {
                return Boolean.FALSE;
            }
            System.out.println("  !! Please type y or n.");
        }
    }

    /** "Press ENTER to continue" so report output does not scroll away. */
    public static void pause() {
        System.out.print("\nPress ENTER to continue...");
        try {
            scanner.nextLine();
        } catch (InputMismatchException e) {
            // Nothing to do - we only wanted to wait.
        }
    }

    /** Closes the Scanner when the program exits. */
    public static void close() {
        scanner.close();
    }
}
