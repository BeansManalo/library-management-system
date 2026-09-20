package lms.core;

/**
 * A registered library member.
 * Plain data holder (POJO), same shape as {@link Book} -- getters and
 * setters only, so it stays easy to swap for a database-backed row later.
 */
public class Member {

    private String memberId;
    private String name;
    private String contactNumber;
    private String email;
    private String address;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
