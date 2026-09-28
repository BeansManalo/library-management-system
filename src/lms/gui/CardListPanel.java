package lms.gui;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Shared layout helper for the narrow two-stat column on the left edge
 * of every row card (BookCardPanel / MemberCardPanel).
 *
 * The Book List and Member List used to be plain scrollable panels
 * holding one always-live BookCardPanel/MemberCardPanel per row, which
 * got rebuilt from scratch on every keystroke of the search box -- fine
 * for a handful of rows, but it noticeably lagged once the list got
 * long. Both screens now use a JList instead (see BookListPanel /
 * MemberListPanel), which only ever renders the rows actually on
 * screen, reusing a single BookCardPanel/MemberCardPanel instance as
 * its ListCellRenderer. This class no longer needs to host the list
 * itself -- just the bit of row layout the two card classes share.
 * Because that one card instance is now reused instead of rebuilt, the
 * two value labels are created by the caller and handed in here (rather
 * than built fresh from a String) so the caller can keep a reference
 * and update the text in place on every re-render.
 */
final class CardListPanel {

    private CardListPanel() {
    }

    /** The narrow two-stat column on the left edge of every card. */
    static JPanel statColumn(String captionA, JLabel valueA, String captionB, JLabel valueB) {
        JPanel col = new JPanel(new java.awt.GridLayout(2, 1));
        col.setOpaque(false);
        col.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 0, 1, Theme.DIVIDER),
            new EmptyBorder(0, 3, 0, 6)));
        // Wide enough for the longest caption ("Currently Owned") to sit
        // on one line at FONT_CARD_CAPTION -- measured at 82px, so this
        // leaves a clean margin either side rather than trimming it close.
        col.setPreferredSize(new Dimension(100, 10));
        col.add(statCell(captionA, valueA, false));
        col.add(statCell(captionB, valueB, true));
        return col;
    }

    /**
     * A caption directly over its value, grouped tightly as one block and
     * centered (via the glue on each side) within whichever half of the
     * card this cell is given -- rather than each label centering
     * independently in its own half of that space, which spreads the two
     * apart instead of pairing them.
     */
    private static JPanel statCell(String caption, JLabel valueLabel, boolean topDivider) {
        JPanel cell = new JPanel();
        cell.setOpaque(false);
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        if (topDivider) {
            cell.setBorder(new MatteBorder(1, 0, 0, 0, Theme.DIVIDER));
        }
        JLabel captionLabel = new JLabel(caption, SwingConstants.CENTER);
        captionLabel.setFont(Theme.FONT_CARD_CAPTION);
        captionLabel.setForeground(Theme.TEXT_PRIMARY);
        captionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(Theme.FONT_CARD_STAT);
        valueLabel.setForeground(Theme.TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cell.add(Box.createVerticalGlue());
        cell.add(captionLabel);
        cell.add(valueLabel);
        cell.add(Box.createVerticalGlue());
        return cell;
    }
}
