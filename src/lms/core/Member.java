package lms.core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A registered library member.
 * Plain data holder (POJO), same shape as {@link Book} -- getters and
 * setters only, so it stays easy to swap for a database-backed row later.
 * The one exception: books borrowed and overdue books aren't stored, they're
 * worked out from the member's loans so they can never drift out of sync.
 */
public class Member {

    /** The format every member date (and every loan date shown for them) is written in. */
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private String memberId;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private String email;
    private String address;
    private String birthDate;
    private String joinDate;
    private String endDate;
    private final List<Loan> loans = new ArrayList<>();

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /** Full display name, "First Last" -- used everywhere a single name string is needed. */
    public String getName() {
        String first = (firstName == null) ? "" : firstName;
        String last = (lastName == null) ? "" : lastName;
        return (first + " " + last).trim();
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<Loan> getLoans() {
        return Collections.unmodifiableList(loans);
    }

    /** Only Library.borrowBooks() should call this, after checking availability. */
    void addLoan(Loan loan) {
        loans.add(loan);
    }

    /** Copies this member currently has out (not yet returned), across every loan. */
    public int getBooksBorrowed() {
        int total = 0;
        for (Loan loan : loans) {
            if (!loan.isReturned()) {
                total += loan.getQuantity();
            }
        }
        return total;
    }

    /** Loans that haven't come back yet, in the order they were recorded. */
    public List<Loan> getOutstandingLoans() {
        List<Loan> outstanding = new ArrayList<>();
        for (Loan loan : loans) {
            if (!loan.isReturned()) {
                outstanding.add(loan);
            }
        }
        return outstanding;
    }

    /** The loans this member still has out that are past their due date, longest overdue first. */
    public List<Loan> getOverdueLoans() {
        LocalDate today = Library.today();
        List<Loan> overdue = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isOverdue(today)) {
                overdue.add(loan);
            }
        }
        overdue.sort(Comparator.comparing(Loan::getDueDate));
        return overdue;
    }

    /** "Overdue Books": copies this member currently has out that should already have been returned. */
    public int getOverdueBooks() {
        int total = 0;
        for (Loan loan : getOverdueLoans()) {
            total += loan.getQuantity();
        }
        return total;
    }

    /**
     * True once the membership's end date has passed. A membership is still
     * good on its end date itself. A missing or unreadable end date never
     * counts as expired.
     */
    public boolean isExpired() {
        LocalDate end = getEndLocalDate();
        return end != null && end.isBefore(Library.today());
    }

    /** The membership start date as a LocalDate, or null if it isn't set/valid. */
    public LocalDate getJoinLocalDate() {
        return parse(joinDate);
    }

    /** The membership end date as a LocalDate, or null if it isn't set/valid. */
    public LocalDate getEndLocalDate() {
        return parse(endDate);
    }

    private static LocalDate parse(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }
}
