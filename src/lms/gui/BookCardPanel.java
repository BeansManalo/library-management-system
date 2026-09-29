package lms.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lms.core.Book;

/**
 * One row of the Book List screen: cover glyph, title/author/genre, the
 * ISBN in the top-right corner, copy counts on the left, publisher/date
 * in the bottom-right. The parts every row card shares (painting, ISBN
 * corner, glyph and text lines) live in RowCardPanel; this class only
 * adds what is specific to a book.
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
public class BookCardPanel extends RowCardPanel<Book> {

    private final JLabel publisherLabel = new FitLabel();
    private final JLabel dateLabel = new JLabel();
    private final JLabel ownedValue = new JLabel();
    private final JLabel availableValue = new JLabel();

    public BookCardPanel() {
        super(RowIcons.book(30, Color.BLACK));
        assemble(CardListPanel.statColumn("Currently Owned", ownedValue, "Available", availableValue), buildBottomRow());
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

    @Override
    protected void update(Book book) {
        idLabel.setText(text(book.getIsbn()));
        titleLabel.setText(text(book.getTitle()));
        subtitleLabel.setText(text(book.getAuthor()));
        detailLabel.setText(text(book.getGenre()));
        publisherLabel.setText(text(book.getPublisher()));
        dateLabel.setText(text(book.getPublicationDate()));
        ownedValue.setText(String.valueOf(book.getTotalCopies()));
        availableValue.setText(String.valueOf(book.getAvailableCopies()));
    }
}
