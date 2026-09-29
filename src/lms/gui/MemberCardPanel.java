package lms.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lms.core.Member;

/**
 * One row of the Member List screen: avatar glyph, name/email/phone, the
 * member ID alone in the top-right corner, join/end dates on the left,
 * and the date of birth in the bottom-left, facing the books-borrowed/
 * overdue-books pair in the bottom-right -- out of the way of the contact
 * details, and a labelled field like the ones opposite it.
 *
 * Shares its structure with BookCardPanel through RowCardPanel and, like
 * it, is built once and reused as the JList's ListCellRenderer for every
 * row instead of being rebuilt per member (see BookCardPanel's class
 * comment for why).
 */
@SuppressWarnings("serial")
public class MemberCardPanel extends RowCardPanel<Member> {

    private final JLabel bornLabel = new JLabel();
    private final JLabel borrowedLabel = new JLabel();
    private final JLabel overdueLabel = new JLabel();
    private final JLabel joinValue = new JLabel();
    private final JLabel endValue = new JLabel();

    public MemberCardPanel() {
        super(RowIcons.person(30, Color.BLACK));
        assemble(CardListPanel.statColumn("Join Date", joinValue, "End Date", endValue), buildBottomRow());
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

    @Override
    protected void update(Member member) {
        idLabel.setText(text(member.getMemberId()));
        titleLabel.setText(text(member.getName()));
        subtitleLabel.setText(text(member.getEmail()));
        detailLabel.setText(text(member.getContactNumber()));
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
}
