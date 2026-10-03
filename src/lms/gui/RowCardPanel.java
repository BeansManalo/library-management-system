package lms.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * What every row card in a list screen has in common (BookCardPanel and
 * MemberCardPanel extend this): the background and selected look, the ID in
 * the top-right corner, the icon with its three lines of text, and the
 * ListCellRenderer plumbing that turns a list item into a drawn row.
 *
 * A subclass only says what is specific to its own row: which icon, how the
 * labels are filled in from its item (update), and the stat column and bottom
 * row it puts around them (assemble). A future card for another kind of item
 * (a loan, say) extends this the same way.
 *
 * Like its subclasses, one instance is built once per screen and reused for
 * every row -- update() changes the existing labels' text, never rebuilds them.
 */
@SuppressWarnings("serial")
abstract class RowCardPanel<T> extends JPanel implements ListCellRenderer<T> {

    private boolean selected;
    private final JPanel rightSide = new JPanel(new BorderLayout());

    /** Top-right corner: the ISBN or the Member ID. */
    protected final JLabel idLabel = new JLabel();
    /** The three lines of text beside the icon, top to bottom. */
    protected final JLabel titleLabel = new FitLabel().shrink(3);
    protected final JLabel subtitleLabel = new FitLabel();
    protected final JLabel detailLabel = new FitLabel();

    protected RowCardPanel(Icon icon) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(3, 8, 3, 8));

        rightSide.setOpaque(false);
        rightSide.add(buildTopRow(), BorderLayout.NORTH);
        rightSide.add(buildBodyRow(icon), BorderLayout.CENTER);
    }

    /**
     * Finishes the card with the two parts only the subclass can build (its
     * labels don't exist yet while this class's constructor runs, so it calls
     * this from its own constructor). The stat column sits in BorderLayout.WEST
     * of the *whole* card, so its divider border runs the full card height.
     */
    protected final void assemble(JPanel statColumn, JPanel bottomRow) {
        rightSide.add(bottomRow, BorderLayout.SOUTH);
        add(statColumn, BorderLayout.WEST);
        add(rightSide, BorderLayout.CENTER);
    }

    /** Refreshes every label from {@code item} -- no new components. */
    protected abstract void update(T item);

    private JPanel buildTopRow() {
        idLabel.setFont(Theme.FONT_CARD_ITALIC);
        idLabel.setForeground(Theme.TEXT_PRIMARY);
        idLabel.setBorder(new MatteBorder(0, 0, 1, 0, Theme.TEXT_MUTED));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(idLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel buildBodyRow(Icon icon) {
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(new EmptyBorder(0, 6, 0, 8));
        iconLabel.setVerticalAlignment(SwingConstants.TOP);

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));

        titleLabel.setFont(Theme.FONT_CARD_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        subtitleLabel.setFont(Theme.FONT_CARD_ITALIC);
        subtitleLabel.setForeground(Theme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        detailLabel.setFont(Theme.FONT_CARD_MUTED);
        detailLabel.setForeground(Theme.TEXT_MUTED);
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textStack.add(titleLabel);
        textStack.add(subtitleLabel);
        textStack.add(detailLabel);

        JPanel iconText = new JPanel(new BorderLayout());
        iconText.setOpaque(false);
        iconText.add(iconLabel, BorderLayout.WEST);
        iconText.add(textStack, BorderLayout.CENTER);
        return iconText;
    }

    /** A dash stands in for a blank field. */
    protected static String text(String value) {
        return (value == null || value.isEmpty()) ? "\u2014" : value;
    }

    @Override
    public final Component getListCellRendererComponent(JList<? extends T> list, T value,
            int index, boolean isSelected, boolean cellHasFocus) {
        selected = isSelected;
        update(value);
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(selected ? Theme.CARD_SELECTED : Theme.CARD_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (selected) {
            g2.setColor(Theme.HEADING);
            g2.fillRect(0, 0, 3, getHeight());
        }
        g2.setColor(Theme.DIVIDER);
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        g2.dispose();
        super.paintComponent(g);
    }
}
