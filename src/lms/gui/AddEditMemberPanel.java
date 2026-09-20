package lms.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import lms.core.Member;
import lms.core.ValidationException;

@SuppressWarnings("serial")
public class AddEditMemberPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblAddAndEditMembers;
    private JTextField txtMemberId;
    private JTextField txtName;
    private JTextField txtContactNumber;
    private JTextField txtEmail;
    private JTextField txtAddress;

    // The member currently being edited, or null while adding a new one.
    // Set by loadMember(), which MainFrame calls right before switching to
    // this card -- see MainFrame.showMemberForm().
    private Member editingMember;

    /**
     * Create the panel.
     */
    public AddEditMemberPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setLayout(null);

        lblAddAndEditMembers = new JLabel("ADD MEMBER");
        lblAddAndEditMembers.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblAddAndEditMembers.setHorizontalAlignment(SwingConstants.CENTER);
        lblAddAndEditMembers.setBounds(0, 10, 640, 24);
        add(lblAddAndEditMembers);

        JLabel lblMemberId = new JLabel("Member ID:");
        lblMemberId.setBounds(150, 55, 90, 20);
        add(lblMemberId);

        txtMemberId = new JTextField();
        txtMemberId.setBounds(250, 54, 220, 22);
        add(txtMemberId);

        JLabel lblName = new JLabel("Name:");
        lblName.setBounds(150, 90, 90, 20);
        add(lblName);

        txtName = new JTextField();
        txtName.setBounds(250, 89, 220, 22);
        add(txtName);

        JLabel lblContactNumber = new JLabel("Contact Number:");
        lblContactNumber.setBounds(150, 125, 90, 20);
        add(lblContactNumber);

        txtContactNumber = new JTextField();
        txtContactNumber.setBounds(250, 124, 220, 22);
        add(txtContactNumber);

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setBounds(150, 160, 90, 20);
        add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBounds(250, 159, 220, 22);
        add(txtEmail);

        JLabel lblAddress = new JLabel("Address:");
        lblAddress.setBounds(150, 195, 90, 20);
        add(lblAddress);

        txtAddress = new JTextField();
        txtAddress.setBounds(250, 194, 220, 22);
        add(txtAddress);

        JButton btnSave = new JButton("SAVE");
        btnSave.setBounds(215, 240, 100, 28);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        add(btnSave);

        JButton btnCancel = new JButton("CANCEL");
        btnCancel.setBounds(325, 240, 100, 28);
        btnCancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Nothing has been written to editingMember at this point
                // (see save() below), so just navigating away is enough
                // to discard whatever the user typed.
                mainFrame.showCard(MainFrame.CARD_DASHBOARD);
            }
        });
        add(btnCancel);
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

        if (member == null) {
            lblAddAndEditMembers.setText("ADD MEMBER");
            txtMemberId.setText("");
            txtName.setText("");
            txtContactNumber.setText("");
            txtEmail.setText("");
            txtAddress.setText("");
        } else {
            lblAddAndEditMembers.setText("EDIT MEMBER");
            txtMemberId.setText(member.getMemberId());
            txtName.setText(member.getName());
            txtContactNumber.setText(member.getContactNumber());
            txtEmail.setText(member.getEmail());
            txtAddress.setText(member.getAddress());
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
            validate(txtMemberId.getText(), txtName.getText(), txtContactNumber.getText(),
                txtEmail.getText(), txtAddress.getText());
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isNew = (editingMember == null);
        Member member = isNew ? new Member() : editingMember;

        member.setMemberId(txtMemberId.getText().trim());
        member.setName(txtName.getText().trim());
        member.setContactNumber(txtContactNumber.getText().trim());
        member.setEmail(txtEmail.getText().trim());
        member.setAddress(txtAddress.getText().trim());

        if (isNew) {
            mainFrame.getLibrary().getMembers().add(member);
        }
        // When editing, "member" is the same object reference stored in the
        // shared list, so the setters above already updated it in place --
        // no separate "replace in list" step is needed.

        mainFrame.showCard(MainFrame.CARD_MEMBER_LIST);
    }

    /** Throws ValidationException naming the first required field left blank. */
    private void validate(String memberId, String name, String contactNumber, String email, String address)
            throws ValidationException {
        if (memberId.trim().isEmpty()) {
            throw new ValidationException("Member ID is required.");
        }
        if (name.trim().isEmpty()) {
            throw new ValidationException("Name is required.");
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
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
