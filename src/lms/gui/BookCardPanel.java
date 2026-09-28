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
import lms.core.Book;

/**
 * One row of the Book List screen: cover glyph, title/author/genre, the
 * ISBN in the top-right corner, copy counts on the left, publisher/date
 * in the bottom-right.
 *
 * One instance is built once per screen and reused as the JList's
 * ListCellRenderer for every row (see BookListPanel) -- update() just
 * changes the existing labels' text instead of rebuilding this whole
 * panel tree per book, and JList itself only ever calls the renderer
 * for rows that are actually on screen. That combination is what keeps
 * the list responsive on every keystroke of the search box no matter
 * how many books there are, unlike the old design where every row was
 * its own always-live JPanel sitting in a plain scrollable panel.
 *
 * The stat column sits in BorderLayout.WEST of the *whole* card (not
 * just alongside the icon row), so its divider border runs the full
 * card height instead of stopping partway.
 */
@SuppressWarnings("serial")
public class BookCardPanel extends JPanel implements ListCellRenderer<Book> {

    private boolean selected;

    private final JLabel isbnLabel = new JLabel();
    private final JLabel titleLabel = new FitLabel().shrink(3);
    private final JLabel authorLabel = new FitLabel();
    private final JLabel genreLabel = new FitLabel();
    private final JLabel publisherLabel = new FitLabel();
    private final JLabel dateLabel = new JLabel();
    private final JLabel ownedValue = new JLabel();
    private final JLabel availableValue = new JLabel();

    public BookCardPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(3, 8, 3, 8));

        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);
        rightSide.add(buildTopRow(), BorderLayout.NORTH);
        rightSide.add(buildBodyRow(), BorderLayout.CENTER);
        rightSide.add(buildBottomRow(), BorderLayout.SOUTH);

        add(CardListPanel.statColumn("Currently Owned", ownedValue, "Available", availableValue), BorderLayout.WEST);
        add(rightSide, BorderLayout.CENTER);
    }

    private JPanel buildTopRow() {
        isbnLabel.setFont(Theme.FONT_CARD_ITALIC);
        isbnLabel.setForeground(Theme.TEXT_PRIMARY);
        isbnLabel.setBorder(new MatteBorder(0, 0, 1, 0, Theme.TEXT_MUTED));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(isbnLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel buildBodyRow() {
        JLabel icon = new JLabel(RowIcons.book(30, Color.BLACK));
        icon.setBorder(new EmptyBorder(0, 6, 0, 8));
        icon.setVerticalAlignment(SwingConstants.TOP);

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));

        titleLabel.setFont(Theme.FONT_CARD_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        authorLabel.setFont(Theme.FONT_CARD_ITALIC);
        authorLabel.setForeground(Theme.TEXT_MUTED);
        authorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        genreLabel.setFont(Theme.FONT_CARD_MUTED);
        genreLabel.setForeground(Theme.TEXT_MUTED);
        genreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textStack.add(titleLabel);
        textStack.add(authorLabel);
        textStack.add(genreLabel);

        JPanel iconText = new JPanel(new BorderLayout());
        iconText.setOpaque(false);
        iconText.add(icon, BorderLayout.WEST);
        iconText.add(textStack, BorderLayout.CENTER);
        return iconText;
    }

    private JPanel buildBottomRow() {
        publisherLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        publisherLabel.setFont(Theme.FONT_CARD_SUB_BOLD);
        publisherLabel.setForeground(Theme.TEXT_PRIMARY);

        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dateLabel.setFont(Theme.FONT_CARD_SUB);
        dateLabel.setForeground(Theme.TEXT_MUTED);

        JPanel stack = new JPanel(new GridLayout(2, 1));
        stack.setOpaque(false);
        stack.add(publisherLabel);
        stack.add(dateLabel);

        // Centered (not EAST) so a long publisher is cut short instead of sprawling
        // left; the inset keeps it out from under the cover glyph and stat column.
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 100, 0, 0));
        row.add(stack, BorderLayout.CENTER);
        return row;
    }

    /** Refreshes every label from {@code book} and this row's selected look -- no new components. */
    private void update(Book book, boolean selected) {
        this.selected = selected;
        isbnLabel.setText(text(book.getIsbn()));
        titleLabel.setText(text(book.getTitle()));
        authorLabel.setText(text(book.getAuthor()));
        genreLabel.setText(text(book.getGenre()));
        publisherLabel.setText(text(book.getPublisher()));
        dateLabel.setText(text(book.getPublicationDate()));
        ownedValue.setText(String.valueOf(book.getTotalCopies()));
        availableValue.setText(String.valueOf(book.getAvailableCopies()));
    }

    private static String text(String value) {
        return (value == null || value.isEmpty()) ? "\u2014" : value;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Book> list, Book value,
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
