package lms.core;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * One book (in some quantity) lent to a member: when it was borrowed,
 * when it's due back, and when it actually came back. Each loan has its own due date, so books
 * borrowed together can still be due on different days.
 */
public class Loan {

    /** Loan period offered when nothing else is chosen: about a week. */
    public static final int DEFAULT_LOAN_DAYS = 7;

    public enum Status { BORROWED, OVERDUE, RETURNED, RETURNED_LATE }

    private final Book book;
    private int quantity; // only ever shrinks, by splitOff()
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnDate; // null until the copies come back

    public Loan(Book book, int quantity, LocalDate borrowDate, LocalDate dueDate) {
        this.book = book;
        this.quantity = quantity;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    public Book getBook() {
        return book;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    /**
     * For a partial return: takes {@code count} copies out of this loan
     * and hands them back as their own loan (same dates), so the returned
     * copies and the ones still out can be tracked separately.
     */
    Loan splitOff(int count) {
        quantity -= count;
        return new Loan(book, count, borrowDate, dueDate);
    }

    /** For the return process: records the day the copies came back. */
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    /** True if the copies came back after the due date (a fact about a finished loan). */
    public boolean isReturnedLate() {
        return returnDate != null && returnDate.isAfter(dueDate);
    }

    /** True if the copies are still out and {@code today} is past the due date. */
    public boolean isOverdue(LocalDate today) {
        return returnDate == null && today.isAfter(dueDate);
    }

    /** How many days past the due date the copies are as of {@code today} (0 if they aren't overdue). */
    public long getDaysOverdue(LocalDate today) {
        return isOverdue(today) ? ChronoUnit.DAYS.between(dueDate, today) : 0;
    }

    public Status getStatus(LocalDate today) {
        if (returnDate != null) {
            return isReturnedLate() ? Status.RETURNED_LATE : Status.RETURNED;
        }
        return isOverdue(today) ? Status.OVERDUE : Status.BORROWED;
    }
}
