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
import lms.core.Book;

/**
 * One row of the Book List screen: cover glyph, title/author/genre, the
 * ISBN in the top-right corner, copy counts on the left, publisher/date
 * in the bottom-right -- laid out with real layout managers (not fixed
 * bounds) so the row stretches cleanly to the scroll pane's width.
 *
 * The stat column sits in BorderLayout.WEST of the *whole* card (not
 * just alongside the icon row), so its divider border runs the full
 * card height instead of stopping partway.
 */
@SuppressWarnings("serial")
public class BookCardPanel extends JPanel {

    private boolean selected;

    public BookCardPanel(Book book, Runnable onSelect, Runnable onOpen) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(3, 8, 3, 8));

        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);
        rightSide.add(buildTopRow(book), BorderLayout.NORTH);
        rightSide.add(buildBodyRow(book), BorderLayout.CENTER);
        rightSide.add(buildBottomRow(book), BorderLayout.SOUTH);

        add(CardListPanel.statColumn(
            "Currently Owned", String.valueOf(book.getTotalCopies()),
            "Available", String.valueOf(book.getAvailableCopies())), BorderLayout.WEST);
        add(rightSide, BorderLayout.CENTER);

        CardListPanel.makeClickable(this, onSelect, onOpen);
    }

    private JPanel buildTopRow(Book book) {
        JLabel isbn = new JLabel(text(book.getIsbn()));
        isbn.setFont(Theme.FONT_CARD_ITALIC);
        isbn.setForeground(Theme.TEXT_PRIMARY);
        isbn.setBorder(new MatteBorder(0, 0, 1, 0, Theme.TEXT_MUTED));

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(isbn, BorderLayout.EAST);
        return row;
    }

    private JPanel buildBodyRow(Book book) {
        JLabel icon = new JLabel(RowIcons.book(30, Color.BLACK));
        icon.setBorder(new EmptyBorder(0, 6, 0, 8));
        icon.setVerticalAlignment(SwingConstants.TOP);

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(text(book.getTitle()));
        title.setFont(Theme.FONT_CARD_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel author = new JLabel(text(book.getAuthor()));
        author.setFont(Theme.FONT_CARD_ITALIC);
        author.setForeground(Theme.TEXT_MUTED);
        author.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel genre = new JLabel(text(book.getGenre()));
        genre.setFont(Theme.FONT_CARD_MUTED);
        genre.setForeground(Theme.TEXT_MUTED);
        genre.setAlignmentX(Component.LEFT_ALIGNMENT);

        textStack.add(title);
        textStack.add(author);
        textStack.add(genre);

        JPanel iconText = new JPanel(new BorderLayout());
        iconText.setOpaque(false);
        iconText.add(icon, BorderLayout.WEST);
        iconText.add(textStack, BorderLayout.CENTER);
        return iconText;
    }

    private JPanel buildBottomRow(Book book) {
        JLabel publisher = new JLabel(text(book.getPublisher()), SwingConstants.RIGHT);
        publisher.setFont(Theme.FONT_CARD_SUB_BOLD);
        publisher.setForeground(Theme.TEXT_PRIMARY);

        JLabel date = new JLabel(text(book.getPublicationDate()), SwingConstants.RIGHT);
        date.setFont(Theme.FONT_CARD_SUB);
        date.setForeground(Theme.TEXT_MUTED);

        JPanel stack = new JPanel(new GridLayout(2, 1));
        stack.setOpaque(false);
        stack.add(publisher);
        stack.add(date);

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
