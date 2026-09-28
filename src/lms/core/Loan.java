package lms.core;

import java.time.LocalDate;

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

    /** A penalty applies when the copies came back after the due date. */
    public boolean isPenalized() {
        return returnDate != null && returnDate.isAfter(dueDate);
    }

    public Status getStatus(LocalDate today) {
        if (returnDate != null) {
            return isPenalized() ? Status.RETURNED_LATE : Status.RETURNED;
        }
        return today.isAfter(dueDate) ? Status.OVERDUE : Status.BORROWED;
    }
}
