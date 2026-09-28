package lms.core;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A registered library member.
 * Plain data holder (POJO), same shape as {@link Book} -- getters and
 * setters only, so it stays easy to swap for a database-backed row later.
 * The one exception: books borrowed and penalties aren't stored, they're
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

    /** Late returns so far: one penalty per loan that came back after its due date. */
    public int getPenalties() {
        int total = 0;
        for (Loan loan : loans) {
            if (loan.isPenalized()) {
                total++;
            }
        }
        return total;
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
