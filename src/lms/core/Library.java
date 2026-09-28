package lms.core;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Dev mode only: when set, this stands in for the real date everywhere
    // the app asks "what day is it?" (overdue and membership-expiry checks
    // included). Null means use the system clock.
    private static LocalDate overrideToday;

    /** "Today" as the library sees it: the real date, unless dev mode has set another one. */
    public static LocalDate today() {
        return overrideToday != null ? overrideToday : LocalDate.now();
    }

    /** Dev mode: pretend today is {@code date}. Null goes back to the real date. */
    public static void setToday(LocalDate date) {
        overrideToday = date;
    }

    /** Some of the copies on one outstanding loan, coming back. */
    public record Return(Loan loan, int quantity) { }

    /**
     * How one outstanding loan ends when its member is deleted: how many
     * copies come back to the shelf and how many are gone for good.
     */
    public record Settlement(Loan loan, int returned, int lost) { }

    public List<Book> getBooks() {
        return books;
    }

    public List<Member> getMembers() {
        return members;
    }

    /** Replaces everything in this library with what {@code other} holds. */
    public void replaceWith(Library other) {
        books.clear();
        books.addAll(other.books);
        members.clear();
        members.addAll(other.members);
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
        LocalDate today = Library.today();
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
        if (returnDate.isAfter(Library.today())) {
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

    /**
     * Renews an expired membership: the member keeps every detail and every
     * book they still have out, but gets a new ID/code and a new join and
     * end date. Only an expired membership can be renewed. Same
     * all-or-nothing checking as borrowing: if anything is invalid,
     * nothing changes and the message says why.
     */
    public void renewMembership(Member member, String newId, LocalDate joinDate, LocalDate endDate)
            throws ValidationException {
        LocalDate today = Library.today();
        if (!member.isExpired()) {
            throw new ValidationException("Only an expired membership can be renewed.");
        }
        String id = (newId == null) ? "" : newId.trim();
        if (id.isEmpty()) {
            throw new ValidationException("The new Member ID is required.");
        }
        if (id.equalsIgnoreCase(member.getMemberId())) {
            throw new ValidationException("A renewed membership needs a new Member ID, different from the old one.");
        }
        for (Member other : members) {
            if (other != member && id.equalsIgnoreCase(other.getMemberId())) {
                throw new ValidationException("Member ID \"" + id + "\" already belongs to " + other.getName() + ".");
            }
        }
        if (joinDate == null) {
            throw new ValidationException("The new Join Date is required.");
        }
        if (joinDate.isAfter(today)) {
            throw new ValidationException("The new Join Date can't be in the future.");
        }
        if (endDate == null) {
            throw new ValidationException("The new End Date is required.");
        }
        if (!endDate.isAfter(joinDate)) {
            throw new ValidationException("The End Date must be after the Join Date.");
        }
        if (endDate.isBefore(today)) {
            throw new ValidationException("The new End Date can't already be in the past -- the membership would still be expired.");
        }

        member.setMemberId(id);
        member.setJoinDate(Member.DATE_FORMAT.format(joinDate));
        member.setEndDate(Member.DATE_FORMAT.format(endDate));
        // Loans belong to the Member object, so the books they still have out carry over untouched.
    }

    /**
     * Suggests an unused ID for a renewal, in the same style as the current
     * one: "LIB-2026-0042" becomes "LIB-2026-0043" (with the year swapped to
     * the new join year when the ID has one). Just a starting point -- the
     * renewal screen lets staff type a different code.
     */
    public String suggestRenewedId(Member member, LocalDate joinDate) {
        String old = (member.getMemberId() == null) ? "" : member.getMemberId().trim();
        String prefix;
        int width;
        Matcher trailing = Pattern.compile("^(.*?)(\\d+)$").matcher(old);
        if (trailing.matches()) {
            prefix = trailing.group(1);
            width = trailing.group(2).length();
        } else if (old.isEmpty()) {
            prefix = "LIB-" + joinDate.getYear() + "-";
            width = 4;
        } else {
            prefix = old + "-";
            width = 1;
        }
        Matcher year = Pattern.compile("^(.*-)\\d{4}-$").matcher(prefix);
        if (year.matches()) {
            prefix = year.group(1) + joinDate.getYear() + "-";
        }

        int next = 1;
        for (Member other : members) {
            String id = other.getMemberId();
            if (id != null && id.startsWith(prefix)) {
                String rest = id.substring(prefix.length());
                if (rest.matches("\\d{1,9}")) {
                    next = Math.max(next, Integer.parseInt(rest) + 1);
                }
            }
        }
        return prefix + String.format("%0" + width + "d", next);
    }

    /**
     * Permanently deletes a member. Every book they still have out must be
     * settled first: each copy either came back (it goes back on the
     * shelf) or is permanently lost (it leaves the library's owned count
     * for good). All-or-nothing: if any loan isn't fully accounted for,
     * nothing changes and the message says why.
     */
    public void deleteMember(Member member, List<Settlement> settlements) throws ValidationException {
        if (!members.contains(member)) {
            throw new ValidationException("That member isn't in the library.");
        }
        Map<Loan, Integer> accounted = new LinkedHashMap<>();
        for (Settlement s : settlements) {
            if (s.returned() < 0 || s.lost() < 0) {
                throw new ValidationException("Copy counts can't be negative.");
            }
            accounted.merge(s.loan(), s.returned() + s.lost(), Integer::sum);
        }
        for (Loan loan : member.getOutstandingLoans()) {
            Integer covered = accounted.remove(loan);
            if (covered == null || covered.intValue() != loan.getQuantity()) {
                throw new ValidationException("Every borrowed copy of \"" + loan.getBook().getTitle()
                    + "\" must be marked as returned or lost first.");
            }
        }
        if (!accounted.isEmpty()) {
            throw new ValidationException("Some of those books aren't on this member's loans.");
        }

        for (Settlement s : settlements) {
            Book book = s.loan().getBook();
            if (s.lost() > 0) {
                // Gone for good: these copies leave the owned count. They were
                // already off the shelf, so Available stays where it is.
                int owned = Math.max(0, book.getTotalCopies() - s.lost());
                book.setTotalCopies(owned);
                book.setAvailableCopies(Math.min(book.getAvailableCopies(), owned));
            }
            if (s.returned() > 0) {
                book.setAvailableCopies(book.getAvailableCopies() + s.returned());
            }
        }
        // The member goes right after this, and their loan history with them,
        // so the Loan objects themselves don't need updating.
        members.remove(member);
    }
}
