package com.hostel.service;

import com.hostel.annotation.AdminOperation;
import com.hostel.dao.AllocationDAO;
import com.hostel.dao.StudentDAO;
import com.hostel.dao.WaitingListDAO;
import com.hostel.exception.AllocationException;
import com.hostel.exception.StudentNotFoundException;
import com.hostel.model.Student;
import com.hostel.model.WaitingEntry;
import com.hostel.util.Validation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * MODULE 8 - the waiting list.
 *
 * This module exists mainly to show TWO different queue collections side
 * by side, and why the choice between them matters:
 *
 * LinkedList (used as a Queue)
 *   - strict FIRST COME, FIRST SERVED
 *   - removing the head is O(1) because a linked list only has to move one
 *     pointer, while ArrayList.remove(0) shifts every remaining element
 *   - use it when fairness by arrival time is the rule
 *
 * PriorityQueue
 *   - serves the SMALLEST element first, not the oldest
 *   - "smallest" is decided by WaitingEntry.compareTo(): priority 1 before
 *     priority 5, and when priorities tie, the earlier request wins
 *   - use it when some students must be served first (final year, medical
 *     grounds, distance from home)
 *   - WARNING for the viva: only poll() comes out in order. Printing a
 *     PriorityQueue directly shows heap order, which looks scrambled.
 */
public class WaitingListService {

    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();

    @AdminOperation("Add a student to the waiting list")
    public int addToWaitingList(int studentId, int priority)
            throws StudentNotFoundException, AllocationException, SQLException {

        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new StudentNotFoundException("No student with ID " + studentId);
        }
        if (!Validation.isValidPriority(priority)) {
            throw new AllocationException("Priority must be between 1 (highest) and 5.");
        }
        if (allocationDAO.findActiveByStudentId(studentId) != null) {
            throw new AllocationException(
                    student.getName() + " already has a room, so they cannot wait for one.");
        }
        if (waitingListDAO.findWaitingByStudentId(studentId) != null) {
            throw new AllocationException(
                    student.getName() + " is already on the waiting list.");
        }

        WaitingEntry entry = new WaitingEntry(studentId, priority);
        return waitingListDAO.insert(entry);
    }

    /** The raw list, already sorted by SQL. Used for plain printing. */
    public ArrayList<WaitingEntry> getWaitingList() throws SQLException {
        return waitingListDAO.findWaiting();
    }

    /**
     * The waiting list as a FIFO queue built on LinkedList.
     *
     * Queue is the INTERFACE, LinkedList is the implementation. Coding to
     * the interface means the implementation could be swapped later
     * without changing the rest of the program.
     */
    public Queue<WaitingEntry> getFifoQueue() throws SQLException {
        Queue<WaitingEntry> queue = new LinkedList<>();
        ArrayList<WaitingEntry> entries = waitingListDAO.findWaiting();

        for (int i = 0; i < entries.size(); i++) {
            queue.offer(entries.get(i));      // offer() adds at the TAIL
        }
        return queue;
    }

    /** The same students, but ordered by priority instead of arrival. */
    public PriorityQueue<WaitingEntry> getPriorityQueue() throws SQLException {
        PriorityQueue<WaitingEntry> queue = new PriorityQueue<>();
        ArrayList<WaitingEntry> entries = waitingListDAO.findWaiting();

        for (int i = 0; i < entries.size(); i++) {
            queue.offer(entries.get(i));      // position decided by compareTo()
        }
        return queue;
    }

    /**
     * Who should get the next free bed, according to priority.
     * peek() looks at the head WITHOUT removing it, poll() removes it.
     */
    public WaitingEntry getNextStudentToAllocate() throws SQLException {
        PriorityQueue<WaitingEntry> queue = getPriorityQueue();
        return queue.peek();
    }

    @AdminOperation("Remove a student from the waiting list")
    public boolean removeFromWaitingList(int waitingId) throws SQLException {
        return waitingListDAO.updateStatus(waitingId, WaitingEntry.REMOVED);
    }

    public WaitingEntry getEntryById(int waitingId) throws SQLException {
        return waitingListDAO.findById(waitingId);
    }

    public int countWaiting() throws SQLException {
        return waitingListDAO.countWaiting();
    }

    /**
     * Prints both orderings next to each other so the difference between
     * a LinkedList queue and a PriorityQueue is visible on screen.
     */
    public void compareQueueOrders() throws SQLException {

        Queue<WaitingEntry> fifo = getFifoQueue();
        PriorityQueue<WaitingEntry> priority = getPriorityQueue();

        if (fifo.isEmpty()) {
            System.out.println("The waiting list is empty.");
            return;
        }

        System.out.println("\n1) LinkedList as a Queue - FIRST COME, FIRST SERVED");
        System.out.println("   " + String.format("%-4s %-12s %-20s %-10s",
                "POS", "REG NO", "NAME", "PRIORITY"));
        int position = 1;
        while (!fifo.isEmpty()) {
            WaitingEntry entry = fifo.poll();     // poll() removes from the HEAD
            System.out.println("   " + String.format("%-4d %-12s %-20s %-10d",
                    position, entry.getRegisterNumber(), entry.getStudentName(),
                    entry.getPriority()));
            position++;
        }

        System.out.println("\n2) PriorityQueue - HIGHEST PRIORITY FIRST");
        System.out.println("   " + String.format("%-4s %-12s %-20s %-10s",
                "POS", "REG NO", "NAME", "PRIORITY"));
        position = 1;
        while (!priority.isEmpty()) {
            WaitingEntry entry = priority.poll();
            System.out.println("   " + String.format("%-4d %-12s %-20s %-10d",
                    position, entry.getRegisterNumber(), entry.getStudentName(),
                    entry.getPriority()));
            position++;
        }
    }
}
