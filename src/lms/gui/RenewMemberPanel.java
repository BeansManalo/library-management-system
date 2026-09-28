package lms.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDate;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import lms.core.Library;
import lms.core.Member;
import lms.core.ValidationException;

/**
 * Renews an expired membership -- reached from the RENEW button on the
 * "Add / Edit Members" list, which only appears for a member whose
 * membership has ended (see MemberListPanel.updateRenewButton()).
 *
 * Everything about the member stays as it is, including the books they
 * still have out. What changes is the membership itself: a new Member
 * ID (suggested from the old one, but editable), a new Join Date
 * (today unless changed) and a new End Date (a year after the Join Date
 * unless changed). The checks live in Library.renewMembership().
 *
 * If the person isn't coming back, DELETE INSTEAD hands over to the same
 * delete process as the Delete button (see MainFrame.showMemberDelete()).
 */
@SuppressWarnings("serial")
public class RenewMemberPanel extends JPanel {

    private static final int FIELD_WIDTH = 260;

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();

    private Member member;
    private JLabel lblName;
    private JLabel lblStatus;
    private JLabel lblKeeps;
    private JTextField txtNewId;
    private DatePickerField txtJoinDate;
    private DatePickerField txtEndDate;

    public RenewMemberPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        JLabel banner = Theme.banner("RENEW MEMBERSHIP");
        place(banner, 0, 0, 640, 30);
        add(banner);

        lblName = new JLabel();
        lblName.setFont(new Font("Arial", Font.BOLD, 14));
        lblName.setForeground(Theme.TEXT_PRIMARY);
        place(lblName, 20, 40, 600, 22);
        add(lblName);

        lblStatus = new JLabel();
        lblStatus.setFont(Theme.FONT_CARD_SUB_BOLD);
        lblStatus.setForeground(Theme.ALERT);
        place(lblStatus, 20, 62, 600, 16);
        add(lblStatus);

        lblKeeps = new JLabel();
        lblKeeps.setFont(Theme.FONT_CARD_SUB);
        lblKeeps.setForeground(Theme.TEXT_MUTED);
        place(lblKeeps, 20, 80, 600, 16);
        add(lblKeeps);

        txtNewId = textField("New Member ID:", 40, 112);

        JLabel lblIdHint = new JLabel("Suggested from the old ID -- change it if you like.");
        lblIdHint.setFont(Theme.FONT_CARD_SUB);
        lblIdHint.setForeground(Theme.TEXT_MUTED);
        place(lblIdHint, 340, 130, 260, 24);
        add(lblIdHint);

        txtJoinDate = dateField("New Join Date:", 40, 168);
        txtEndDate = dateField("New End Date:", 340, 168);

        // Same rule as the Add form: End Date follows Join Date (one year on)
        // until the person picks their own.
        txtJoinDate.setOnDateChanged(this::syncEndDate);

        JLabel lblDeleteHint = new JLabel("Not renewing? Delete the membership instead -- any books they still have out are marked returned or lost first.");
        lblDeleteHint.setFont(Theme.FONT_CARD_SUB);
        lblDeleteHint.setForeground(Theme.TEXT_MUTED);
        place(lblDeleteHint, 40, 250, 560, 16);
        add(lblDeleteHint);

        PillButton btnCancel = new PillButton("CANCEL", Theme.TEXT_MUTED,
            Theme.TEXT_MUTED.brighter(), Theme.TEXT_PRIMARY);
        place(btnCancel, 95, 300, 110, 28);
        btnCancel.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_MEMBER_LIST));
        add(btnCancel);

        PillButton btnDelete = new PillButton("DELETE INSTEAD", Theme.ALERT,
            new Color(0xC0, 0x45, 0x45), new Color(0x80, 0x24, 0x24));
        place(btnDelete, 220, 300, 160, 28);
        btnDelete.addActionListener(e -> mainFrame.showMemberDelete(member));
        add(btnDelete);

        PillButton btnRenew = new PillButton("RENEW");
        place(btnRenew, 395, 300, 150, 28);
        btnRenew.addActionListener(e -> renew());
        add(btnRenew);
    }

    /** Called by MainFrame right before showing this card -- see MainFrame.showMemberRenew(). */
    public void startFor(Member member) {
        this.member = member;
        LocalDate today = Library.today();

        lblName.setText(member.getName());
        lblStatus.setText("Membership ended " + member.getEndDate() + "   \u2022   Current ID: " + member.getMemberId());
        int out = member.getBooksBorrowed();
        lblKeeps.setText(out > 0
            ? "Everything else stays the same, including the " + out + " book(s) they still have out."
            : "Everything else about the member stays the same.");

        txtNewId.setText(mainFrame.getLibrary().suggestRenewedId(member, today));
        txtJoinDate.setMaxDate(today); // a membership can't start in the future
        txtJoinDate.setDate(today);
        syncEndDate();
    }

    /** End Date defaults to a year after the Join Date, but never earlier than the day after it (or today, if that has already passed). */
    private void syncEndDate() {
        LocalDate join = txtJoinDate.getDate();
        if (join == null) {
            txtEndDate.setMinDate(null);
            txtEndDate.setText("");
            return;
        }
        // Must come after the Join Date, and must not already be in the past
        // (or the renewed membership would be expired on arrival).
        LocalDate earliest = join.plusDays(1);
        LocalDate today = Library.today();
        if (earliest.isBefore(today)) {
            earliest = today;
        }
        txtEndDate.setMinDate(earliest);
        LocalDate end = join.plusYears(1);
        txtEndDate.setDate(end.isBefore(earliest) ? earliest : end);
    }

    private void renew() {
        try {
            mainFrame.getLibrary().renewMembership(member, txtNewId.getText(),
                txtJoinDate.getDate(), txtEndDate.getDate());
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Renew", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
            "Membership renewed for " + member.getName() + ".\n\n"
                + "New Member ID: " + member.getMemberId() + "\n"
                + "Valid " + member.getJoinDate() + " to " + member.getEndDate(),
            "Renew Membership", JOptionPane.INFORMATION_MESSAGE);
        mainFrame.showCard(MainFrame.CARD_MEMBER_LIST);
    }

    /** A label + text field pair at (x, y), FIELD_WIDTH wide. */
    private JTextField textField(String labelText, int x, int y) {
        addLabel(labelText, x, y);
        JTextField text = new JTextField();
        text.setFont(Theme.FONT_FIELD);
        text.setBorder(new CompoundBorder(new LineBorder(Theme.DIVIDER, 1), new EmptyBorder(2, 6, 2, 6)));
        place(text, x, y + 18, FIELD_WIDTH, 24);
        add(text);
        return text;
    }

    /** A label + calendar-popup pair at (x, y), FIELD_WIDTH wide. */
    private DatePickerField dateField(String labelText, int x, int y) {
        addLabel(labelText, x, y);
        DatePickerField picker = new DatePickerField(Member.DATE_FORMAT);
        place(picker, x, y + 18, FIELD_WIDTH, 24);
        add(picker);
        return picker;
    }

    private void addLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(Theme.TEXT_PRIMARY);
        place(label, x, y, FIELD_WIDTH, 16);
        add(label);
    }

    private void place(Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
