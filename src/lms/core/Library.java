package lms.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory home for every Book and Member the app knows about.
 *
 * MainFrame owns the single instance of this class and every panel
 * reaches it through mainFrame.getLibrary(), so there is exactly one
 * copy of the data no matter which screen is showing. When the
 * planned database module is added later, the two lists below are
 * the only thing that needs to change (e.g. load/save through a DAO
 * instead of keeping everything in memory) -- none of the GUI code
 * has to change.
 */
public class Library {

    private final List<Book> books = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();

    /** Some of the copies on one outstanding loan, coming back. */
    public record Return(Loan loan, int quantity) { }

    public List<Book> getBooks() {
        return books;
    }

    public List<Member> getMembers() {
        return members;
    }

    /**
     * Records the given loans against a member and takes the copies off
     * the shelf. Everything is checked first, so it's all-or-nothing: if
     * any loan is invalid, nothing changes and the message says why.
     */
    public void borrowBooks(Member member, List<Loan> loans) throws ValidationException {
        if (loans.isEmpty()) {
            throw new ValidationException("Add at least one book to borrow.");
        }
        LocalDate today = LocalDate.now();
        // The same book can be on more than one line (different return
        // dates), so availability is checked against the combined total.
        Map<Book, Integer> requested = new HashMap<>();
        for (Loan loan : loans) {
            if (loan.getQuantity() < 1) {
                throw new ValidationException("Each book needs a quantity of at least 1.");
            }
            if (loan.getBorrowDate().isAfter(today)) {
                throw new ValidationException("A book can't be borrowed on a future date.");
            }
            if (loan.getDueDate().isBefore(today)) {
                throw new ValidationException("The return date can't be in the past.");
            }
            requested.merge(loan.getBook(), loan.getQuantity(), Integer::sum);
        }
        for (Map.Entry<Book, Integer> entry : requested.entrySet()) {
            Book book = entry.getKey();
            int available = book.getAvailableCopies();
            if (available < 1) {
                throw new ValidationException("No copies of \"" + book.getTitle() + "\" are available.");
            }
            if (entry.getValue() > available) {
                throw new ValidationException("Only " + available + " cop" + (available == 1 ? "y" : "ies")
                    + " of \"" + book.getTitle() + "\" available.");
            }
        }

        for (Loan loan : loans) {
            Book book = loan.getBook();
            book.setAvailableCopies(book.getAvailableCopies() - loan.getQuantity());
            member.addLoan(loan);
        }
    }

    /**
     * Marks the given copies as returned on {@code returnDate} and puts
     * them back on the shelf. Same all-or-nothing checking as borrowing.
     * Returning only part of a loan splits it: the returned copies become
     * their own (returned) loan and the rest stay out on the original.
     */
    public void returnBooks(Member member, List<Return> returns, LocalDate returnDate) throws ValidationException {
        if (returns.isEmpty()) {
            throw new ValidationException("Add at least one book to return.");
        }
        if (returnDate.isAfter(LocalDate.now())) {
            throw new ValidationException("A book can't be returned on a future date.");
        }
        Map<Loan, Integer> requested = new LinkedHashMap<>();
        for (Return r : returns) {
            Loan loan = r.loan();
            if (r.quantity() < 1) {
                throw new ValidationException("Each book needs a quantity of at least 1.");
            }
            if (returnDate.isBefore(loan.getBorrowDate())) {
                throw new ValidationException("\"" + loan.getBook().getTitle() + "\" can't be returned before it was borrowed ("
                    + Member.DATE_FORMAT.format(loan.getBorrowDate()) + ").");
            }
            requested.merge(loan, r.quantity(), Integer::sum);
        }
        for (Map.Entry<Loan, Integer> entry : requested.entrySet()) {
            Loan loan = entry.getKey();
            if (loan.isReturned() || entry.getValue() > loan.getQuantity()) {
                throw new ValidationException("More copies of \"" + loan.getBook().getTitle() + "\" than are out on loan.");
            }
        }

        for (Map.Entry<Loan, Integer> entry : requested.entrySet()) {
            Loan loan = entry.getKey();
            int count = entry.getValue();
            Loan returned = loan;
            if (count < loan.getQuantity()) {
                returned = loan.splitOff(count);
                member.addLoan(returned);
            }
            returned.setReturnDate(returnDate);
            Book book = loan.getBook();
            book.setAvailableCopies(book.getAvailableCopies() + count);
        }
    }
}
