package lms.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lms.core.Member;

/**
 * One row of the Member List screen: avatar glyph, name/email/phone, the
 * member ID in the top-right corner, join/end dates on the left, and
 * books-borrowed/penalties in the bottom-right. Mirrors BookCardPanel's
 * structure (including the WEST-spanning stat column, so its divider
 * runs the full card height) so the two lists read as one family.
 */
@SuppressWarnings("serial")
public class MemberCardPanel extends JPanel {

    private boolean selected;

    public MemberCardPanel(Member member, Runnable onSelect, Runnable onOpen) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(3, 8, 3, 8));

        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);
        rightSide.add(buildTopRow(member), BorderLayout.NORTH);
        rightSide.add(buildBodyRow(member), BorderLayout.CENTER);
        rightSide.add(buildBottomRow(member), BorderLayout.SOUTH);

        add(CardListPanel.statColumn(
            "Join Date", text(member.getJoinDate()),
            "End Date", text(member.getEndDate())), BorderLayout.WEST);
        add(rightSide, BorderLayout.CENTER);

        CardListPanel.makeClickable(this, onSelect, onOpen);
    }

    private JPanel buildTopRow(Member member) {
        JLabel memberId = new JLabel(text(member.getMemberId()));
        memberId.setFont(Theme.FONT_CARD_ITALIC);
        memberId.setForeground(Theme.TEXT_PRIMARY);
        memberId.setBorder(new MatteBorder(0, 0, 1, 0, Theme.TEXT_MUTED));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(memberId, BorderLayout.EAST);
        return row;
    }

    private JPanel buildBodyRow(Member member) {
        JLabel icon = new JLabel(RowIcons.person(30, Color.BLACK));
        icon.setBorder(new EmptyBorder(0, 6, 0, 8));
        icon.setVerticalAlignment(SwingConstants.TOP);

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(text(member.getName()));
        name.setFont(Theme.FONT_CARD_TITLE);
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel email = new JLabel(text(member.getEmail()));
        email.setFont(Theme.FONT_CARD_ITALIC);
        email.setForeground(Theme.TEXT_MUTED);
        email.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel phone = new JLabel(text(member.getContactNumber()));
        phone.setFont(Theme.FONT_CARD_MUTED);
        phone.setForeground(Theme.TEXT_MUTED);
        phone.setAlignmentX(Component.LEFT_ALIGNMENT);

        textStack.add(name);
        textStack.add(email);
        textStack.add(phone);

        JPanel iconText = new JPanel(new BorderLayout());
        iconText.setOpaque(false);
        iconText.add(icon, BorderLayout.WEST);
        iconText.add(textStack, BorderLayout.CENTER);
        return iconText;
    }

    private JPanel buildBottomRow(Member member) {
        JLabel borrowed = new JLabel("Books Borrowed: " + member.getBooksBorrowed(), SwingConstants.RIGHT);
        borrowed.setFont(Theme.FONT_CARD_ITALIC);
        borrowed.setForeground(Theme.TEXT_PRIMARY);

        JLabel penalties = new JLabel("Penalties: " + member.getPenalties(), SwingConstants.RIGHT);
        penalties.setFont(Theme.FONT_CARD_ITALIC);
        penalties.setForeground(member.getPenalties() > 0 ? Theme.NAVY : Theme.TEXT_MUTED);

        JPanel stack = new JPanel(new GridLayout(2, 1));
        stack.setOpaque(false);
        stack.add(borrowed);
        stack.add(penalties);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(stack, BorderLayout.EAST);
        return row;
    }

    private static String text(String value) {
        return (value == null || value.isEmpty()) ? "\u2014" : value;
    }

    void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
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
