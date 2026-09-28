package lms.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lms.core.Member;

/**
 * One row of the Member List screen: avatar glyph, name/email/phone, the
 * member ID alone in the top-right corner, join/end dates on the left,
 * and the date of birth in the bottom-left, facing the books-borrowed/
 * overdue-books pair in the bottom-right -- out of the way of the contact
 * details, and a labelled field like the ones opposite it.
 *
 * Mirrors BookCardPanel's structure and, like it, is built once and
 * reused as the JList's ListCellRenderer for every row instead of being
 * rebuilt per member (see BookCardPanel's class comment for why).
 */
@SuppressWarnings("serial")
public class MemberCardPanel extends JPanel implements ListCellRenderer<Member> {

    private boolean selected;

    private final JLabel memberIdLabel = new JLabel();
    private final JLabel nameLabel = new FitLabel().shrink(3);
    private final JLabel emailLabel = new FitLabel();
    private final JLabel phoneLabel = new FitLabel();
    private final JLabel bornLabel = new JLabel();
    private final JLabel borrowedLabel = new JLabel();
    private final JLabel overdueLabel = new JLabel();
    private final JLabel joinValue = new JLabel();
    private final JLabel endValue = new JLabel();

    public MemberCardPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(3, 8, 3, 8));

        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);
        rightSide.add(buildTopRow(), BorderLayout.NORTH);
        rightSide.add(buildBodyRow(), BorderLayout.CENTER);
        rightSide.add(buildBottomRow(), BorderLayout.SOUTH);

        add(CardListPanel.statColumn("Join Date", joinValue, "End Date", endValue), BorderLayout.WEST);
        add(rightSide, BorderLayout.CENTER);
    }

    private JPanel buildTopRow() {
        memberIdLabel.setFont(Theme.FONT_CARD_ITALIC);
        memberIdLabel.setForeground(Theme.TEXT_PRIMARY);
        memberIdLabel.setBorder(new MatteBorder(0, 0, 1, 0, Theme.TEXT_MUTED));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(memberIdLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel buildBodyRow() {
        JLabel icon = new JLabel(RowIcons.person(30, Color.BLACK));
        icon.setBorder(new EmptyBorder(0, 6, 0, 8));
        icon.setVerticalAlignment(SwingConstants.TOP);

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));

        nameLabel.setFont(Theme.FONT_CARD_TITLE);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        emailLabel.setFont(Theme.FONT_CARD_ITALIC);
        emailLabel.setForeground(Theme.TEXT_MUTED);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        phoneLabel.setFont(Theme.FONT_CARD_MUTED);
        phoneLabel.setForeground(Theme.TEXT_MUTED);
        phoneLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textStack.add(nameLabel);
        textStack.add(emailLabel);
        textStack.add(phoneLabel);

        JPanel iconText = new JPanel(new BorderLayout());
        iconText.setOpaque(false);
        iconText.add(icon, BorderLayout.WEST);
        iconText.add(textStack, BorderLayout.CENTER);
        return iconText;
    }

    private JPanel buildBottomRow() {
        borrowedLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        borrowedLabel.setFont(Theme.FONT_CARD_ITALIC);
        borrowedLabel.setForeground(Theme.TEXT_PRIMARY);

        overdueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        overdueLabel.setFont(Theme.FONT_CARD_ITALIC);

        JPanel stack = new JPanel(new GridLayout(2, 1));
        stack.setOpaque(false);
        stack.add(borrowedLabel);
        stack.add(overdueLabel);

        // Bottom-aligned so it sits level with the last line opposite it,
        // and inset to line up under the avatar above.
        bornLabel.setFont(Theme.FONT_CARD_ITALIC);
        bornLabel.setForeground(Theme.TEXT_MUTED);
        bornLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        bornLabel.setBorder(new EmptyBorder(0, 6, 0, 0));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(bornLabel, BorderLayout.WEST);
        row.add(stack, BorderLayout.EAST);
        return row;
    }

    /** Refreshes every label from {@code member} and this row's selected look -- no new components. */
    private void update(Member member, boolean selected) {
        this.selected = selected;
        memberIdLabel.setText(text(member.getMemberId()));
        nameLabel.setText(text(member.getName()));
        emailLabel.setText(text(member.getEmail()));
        phoneLabel.setText(text(member.getContactNumber()));
        bornLabel.setText("Date of Birth: " + text(member.getBirthDate()));
        borrowedLabel.setText("Books Borrowed: " + member.getBooksBorrowed());
        int overdue = member.getOverdueBooks();
        overdueLabel.setText("Overdue Books: " + overdue);
        overdueLabel.setForeground(overdue > 0 ? Theme.ALERT : Theme.TEXT_MUTED);
        joinValue.setText(text(member.getJoinDate()));
        endValue.setText(text(member.getEndDate()));
        // An expired membership shows its end date in red (this one label is reused for every row).
        endValue.setForeground(member.isExpired() ? Theme.ALERT : Theme.TEXT_PRIMARY);
    }

    private static String text(String value) {
        return (value == null || value.isEmpty()) ? "\u2014" : value;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Member> list, Member value,
            int index, boolean isSelected, boolean cellHasFocus) {
        update(value, isSelected);
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(selected ? Theme.CARD_SELECTED : Theme.CARD_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (selected) {
            g2.setColor(Theme.NAVY);
            g2.fillRect(0, 0, 3, getHeight());
        }
        g2.setColor(Theme.DIVIDER);
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        g2.dispose();
        super.paintComponent(g);
    }
}
