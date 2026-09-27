package lms.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Vertical stack of row cards (BookCardPanel / MemberCardPanel) meant to
 * sit inside a JScrollPane. Implementing Scrollable just to track the
 * viewport's width is what lets each row stretch edge-to-edge instead of
 * leaving a gap next to the scrollbar, and gives the wheel a sane
 * per-row scroll amount instead of Swing's tiny default.
 */
@SuppressWarnings("serial")
public class CardListPanel extends JPanel implements Scrollable {

    /** Roughly one card's height -- see BookCardPanel/MemberCardPanel. */
    private static final int UNIT = 64;

    public CardListPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.CARD_BG);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return UNIT;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * A click anywhere on a card -- including on one of its labels, which
     * would otherwise swallow the event before the card panel ever saw it
     * -- selects the row; a double-click also opens it. Installed once,
     * recursively, over the card's whole component tree.
     */
    static void makeClickable(Component root, Runnable onSelect, Runnable onOpen) {
        root.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onSelect.run();
                if (e.getClickCount() == 2) {
                    onOpen.run();
                }
            }
        });
        if (root instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) root).getComponents()) {
                makeClickable(child, onSelect, onOpen);
            }
        }
    }

    /** The narrow two-stat column on the left edge of every card. */
    static JPanel statColumn(String captionA, String valueA, String captionB, String valueB) {
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
    private static JPanel statCell(String caption, String value, boolean topDivider) {
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
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(Theme.FONT_CARD_STAT);
        valueLabel.setForeground(Theme.TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cell.add(javax.swing.Box.createVerticalGlue());
        cell.add(captionLabel);
        cell.add(valueLabel);
        cell.add(javax.swing.Box.createVerticalGlue());
        return cell;
    }
}
