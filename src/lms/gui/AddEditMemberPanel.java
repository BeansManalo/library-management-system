package lms.gui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lms.core.Library;
import lms.core.Member;
import lms.core.ValidationException;

@SuppressWarnings("serial")
public class AddEditMemberPanel extends JPanel {

    // Shared column width for every field except the First/Last Name
    // row, which splits it between two fields -- keeping both columns
    // this wide (instead of the original 160) closes the dead gap that
    // used to sit between them and balances the margin on both sides.
    private static final int FIELD_WIDTH = 260;

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();
    private JLabel lblAddAndEditMembers;
    private JTextField txtMemberId;
    private JTextField txtFirstName;
    private JTextField txtLastName;
    private DatePickerField txtBirthDate;
    private JTextField txtContactNumber;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private DatePickerField txtJoinDate;
    private DatePickerField txtEndDate;

    // The member currently being edited, or null while adding a new one.
    // Set by loadMember(), which MainFrame calls right before switching to
    // this card -- see MainFrame.showMemberForm().
    private Member editingMember;

    // Shown/parsed as e.g. "09/25/2026", matching how the member card
    // displays it.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Create the panel.
     */
    public AddEditMemberPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        lblAddAndEditMembers = Theme.banner("ADD MEMBER");
        place(lblAddAndEditMembers, 0, 0, 640, 30);
        add(lblAddAndEditMembers);

        // Left column: who the member is. First/Last Name share one row
        // (kept together as one visual "Name" unit) with Birth Date
        // directly beneath it.
        txtMemberId = field("Member ID:", 40, 42);
        txtFirstName = field("First Name:", 40, 92, 120);
        txtLastName = field("Last Name:", 170, 92, 130);
        txtBirthDate = dateField("Birth Date:", 40, 142, DATE_FORMAT);
        txtContactNumber = field("Contact Number:", 40, 192);

        // Right column: how to reach them and their membership window.
        // Books Borrowed / Overdue Books are intentionally not here -- those
        // will belong to a future borrowing system, not something typed
        // in by hand; the list/detail views still show them read-only.
        txtEmail = field("Email:", 340, 42);
        txtAddress = field("Address:", 340, 92);
        txtJoinDate = dateField("Join Date:", 340, 142, DATE_FORMAT);
        txtEndDate = dateField("End Date:", 340, 192, DATE_FORMAT);

        // End Date defaults to a year after Join Date, and only opens up
        // for manual editing once there's a Join Date to measure from --
        // recomputed every time Join Date changes, so it always reflects
        // "one year after" until the person overrides it themselves.
        txtEndDate.setEnabled(false);
        txtJoinDate.setOnDateChanged(() -> {
            LocalDate join = txtJoinDate.getDate();
            txtEndDate.setMinDate(join);
            if (join != null) {
                txtEndDate.setDate(join.plusYears(1));
                txtEndDate.setEnabled(true);
            } else {
                txtEndDate.setText("");
                txtEndDate.setEnabled(false);
            }
        });

        PillButton btnSave = new PillButton("SAVE");
        place(btnSave, 215, 260, 100, 32);
        btnSave.addActionListener(e -> save());
        add(btnSave);

        PillButton btnCancel = new PillButton("CANCEL", Theme.TEXT_MUTED,
            Theme.TEXT_MUTED.brighter(), Theme.TEXT_PRIMARY);
        place(btnCancel, 325, 260, 100, 32);
        // Nothing has been written to editingMember at this point (see
        // save() below), so just navigating away is enough to discard
        // whatever the user typed. This screen is only ever reached from
        // the "Add / Edit Members" list (see MemberListPanel's manage
        // mode), so that's where Cancel returns to.
        btnCancel.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_MEMBER_LIST));
        add(btnCancel);
    }

    /** Registers a component's design-time bounds with the resizable layout. */
    private void place(java.awt.Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    /** Adds a label + text field pair at (x, y), FIELD_WIDTH wide, and returns the field. */
    private JTextField field(String labelText, int x, int y) {
        return field(labelText, x, y, FIELD_WIDTH);
    }

    /** Same as field(labelText, x, y), but with an explicit field width -- for fields sharing a row. */
    private JTextField field(String labelText, int x, int y, int width) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(Theme.TEXT_PRIMARY);
        place(label, x, y, width, 16);
        add(label);

        JTextField text = new JTextField();
        text.setFont(Theme.FONT_FIELD);
        text.setBorder(new javax.swing.border.CompoundBorder(
            new javax.swing.border.LineBorder(Theme.DIVIDER, 1),
            new javax.swing.border.EmptyBorder(2, 6, 2, 6)));
        place(text, x, y + 18, width, 24);
        add(text);
        return text;
    }

    /** Same as field(), but with a calendar popup instead of a plain text box. */
    private DatePickerField dateField(String labelText, int x, int y, DateTimeFormatter format) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(Theme.TEXT_PRIMARY);
        place(label, x, y, FIELD_WIDTH, 16);
        add(label);

        DatePickerField picker = new DatePickerField(format);
        place(picker, x, y + 18, FIELD_WIDTH, 24);
        add(picker);
        return picker;
    }

    /**
     * Prepares this screen for the given member. Called by MainFrame right
     * before it switches to this card (see MainFrame.showMemberForm), since
     * with CardLayout this panel object is reused rather than recreated
     * on every visit -- without this, a second visit would still be
     * showing whatever was typed in during the first one.
     *
     * @param member the member to edit, or null to start a blank "Add New" form
     */
    public void loadMember(Member member) {
        editingMember = member;
        txtBirthDate.setMaxDate(Library.today());
        txtJoinDate.setMaxDate(Library.today());

        if (member == null) {
            lblAddAndEditMembers.setText("ADD MEMBER");
            txtMemberId.setText("");
            txtFirstName.setText("");
            txtLastName.setText("");
            txtBirthDate.setText("");
            txtContactNumber.setText("");
            txtEmail.setText("");
            txtAddress.setText("");
            txtJoinDate.setText("");
            txtEndDate.setText("");
            txtEndDate.setEnabled(false);
            txtEndDate.setMinDate(null);
        } else {
            lblAddAndEditMembers.setText("EDIT MEMBER");
            txtMemberId.setText(member.getMemberId());
            txtFirstName.setText(member.getFirstName());
            txtLastName.setText(member.getLastName());
            txtBirthDate.setText(member.getBirthDate());
            txtContactNumber.setText(member.getContactNumber());
            txtEmail.setText(member.getEmail());
            txtAddress.setText(member.getAddress());
            txtJoinDate.setText(member.getJoinDate());
            txtEndDate.setText(member.getEndDate());
            txtEndDate.setEnabled(member.getJoinDate() != null && !member.getJoinDate().isEmpty());
            txtEndDate.setMinDate(txtJoinDate.getDate());
        }
    }

    /**
     * Validates the form, then either creates a new Member and adds it to
     * the shared list, or updates the member already being edited in
     * place. Either way, control returns to the Member List screen
     * afterwards.
     */
    private void save() {
        try {
            validateRequired(txtMemberId.getText(), txtFirstName.getText(), txtLastName.getText(),
                txtBirthDate.getText(), txtContactNumber.getText(), txtEmail.getText(), txtAddress.getText(),
                txtJoinDate.getText(), txtEndDate.getText());
            mainFrame.getLibrary().checkMemberIdFree(txtMemberId.getText(), editingMember);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isNew = (editingMember == null);
        Member member = isNew ? new Member() : editingMember;

        member.setMemberId(txtMemberId.getText().trim());
        member.setFirstName(txtFirstName.getText().trim());
        member.setLastName(txtLastName.getText().trim());
        member.setBirthDate(txtBirthDate.getText().trim());
        member.setContactNumber(txtContactNumber.getText().trim());
        member.setEmail(txtEmail.getText().trim());
        member.setAddress(txtAddress.getText().trim());
        member.setJoinDate(txtJoinDate.getText().trim());
        member.setEndDate(txtEndDate.getText().trim());
        // Books Borrowed / Overdue Books aren't form fields -- Member works
        // them out from its loans, so a new member starts at zero and an
        // edited one keeps its history.

        if (isNew) {
            mainFrame.getLibrary().getMembers().add(member);
        }
        // When editing, "member" is the same object reference stored in the
        // shared list, so the setters above already updated it in place --
        // no separate "replace in list" step is needed.

        mainFrame.saveLibrary();
        mainFrame.showCard(MainFrame.CARD_MEMBER_LIST);
    }

    /** Throws ValidationException naming the first required field left blank. */
    private void validateRequired(String memberId, String firstName, String lastName, String birthDate,
            String contactNumber, String email, String address, String joinDate, String endDate)
            throws ValidationException {
        if (memberId.trim().isEmpty()) {
            throw new ValidationException("Member ID is required.");
        }
        if (firstName.trim().isEmpty()) {
            throw new ValidationException("First Name is required.");
        }
        if (lastName.trim().isEmpty()) {
            throw new ValidationException("Last Name is required.");
        }
        if (birthDate.trim().isEmpty()) {
            throw new ValidationException("Birth Date is required.");
        }
        if (contactNumber.trim().isEmpty()) {
            throw new ValidationException("Contact Number is required.");
        }
        if (email.trim().isEmpty()) {
            throw new ValidationException("Email is required.");
        }
        if (address.trim().isEmpty()) {
            throw new ValidationException("Address is required.");
        }
        if (joinDate.trim().isEmpty()) {
            throw new ValidationException("Join Date is required.");
        }
        if (endDate.trim().isEmpty()) {
            throw new ValidationException("End Date is required.");
        }
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
