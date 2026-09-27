package lms.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lms.core.Book;

/**
 * Read-only, in-depth look at a single book: full details, a tag list,
 * and a per-copy tracker (one chip per physical copy, colored by
 * whether it's presently available) -- opened by double-clicking a row
 * on the Book List, since that screen is browse/search only now and no
 * longer where fields get edited (see BookListPanel).
 */
@SuppressWarnings("serial")
public class BookDetailPanel extends JPanel {

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();

    private JLabel banner;
    private JLabel titleLabel;
    private JLabel authorLabel;
    private JLabel genreLabel;
    private JLabel isbnValue;
    private JLabel publisherValue;
    private JLabel dateValue;
    private JLabel copiesValue;
    private JPanel tagsRow;
    private JPanel copiesRow;

    private Book book;

    public BookDetailPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        banner = Theme.banner("BOOK DETAILS");
        place(banner, 0, 0, 640, 30);
        add(banner);

        JLabel icon = new JLabel(RowIcons.book(56, Color.BLACK));
        place(icon, 30, 44, 56, 56);
        add(icon);

        titleLabel = new JLabel();
        titleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        place(titleLabel, 100, 44, 420, 26);
        add(titleLabel);

        authorLabel = new JLabel();
        authorLabel.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 13));
        authorLabel.setForeground(Theme.TEXT_MUTED);
        place(authorLabel, 100, 70, 420, 18);
        add(authorLabel);

        genreLabel = new JLabel();
        genreLabel.setFont(Theme.FONT_LABEL);
        genreLabel.setForeground(Theme.TEXT_MUTED);
        place(genreLabel, 100, 90, 420, 18);
        add(genreLabel);

        isbnValue = detailRow("ISBN", 30, 118);
        publisherValue = detailRow("Publisher", 30, 158);
        dateValue = detailRow("Published", 30, 198);
        copiesValue = detailRow("Copies (available / total)", 340, 118);

        JLabel tagsCaption = new JLabel("Tags");
        tagsCaption.setFont(Theme.FONT_CARD_CAPTION);
        tagsCaption.setForeground(Theme.TEXT_MUTED);
        place(tagsCaption, 340, 158, 260, 14);
        add(tagsCaption);

        tagsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        tagsRow.setOpaque(false);
        place(tagsRow, 340, 174, 260, 46);
        add(tagsRow);

        JLabel copiesCaption = new JLabel("Copy tracker (each square is one physical copy)");
        copiesCaption.setFont(Theme.FONT_CARD_CAPTION);
        copiesCaption.setForeground(Theme.TEXT_MUTED);
        place(copiesCaption, 30, 238, 400, 14);
        add(copiesCaption);

        copiesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        copiesRow.setOpaque(false);
        place(copiesRow, 30, 254, 570, 40);
        add(copiesRow);

        // Purely a viewer -- editing and deleting now only happen through
        // the "Add / Edit Books" screen (see BookListPanel's manage
        // mode), so the only action here is leaving.
        PillButton btnBack = new PillButton("BACK");
        place(btnBack, 270, 300, 100, 32);
        btnBack.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_BOOK_LIST));
        add(btnBack);
    }

    private void place(Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    /** A caption + value pair, stacked, matching the card's read-only "info line" look. */
    private JLabel detailRow(String caption, int x, int y) {
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(Theme.FONT_CARD_CAPTION);
        captionLabel.setForeground(Theme.TEXT_MUTED);
        place(captionLabel, x, y, 260, 14);
        add(captionLabel);

        JLabel valueLabel = new JLabel();
        valueLabel.setFont(Theme.FONT_CARD_TITLE);
        valueLabel.setForeground(Theme.TEXT_PRIMARY);
        place(valueLabel, x, y + 15, 260, 20);
        add(valueLabel);
        return valueLabel;
    }

    /** Called by MainFrame right before switching to this card -- see MainFrame.showBookDetail(). */
    public void loadBook(Book book) {
        this.book = book;
        if (book == null) {
            return;
        }

        titleLabel.setText(book.getTitle());
        authorLabel.setText(book.getAuthor());
        genreLabel.setText(book.getGenre());
        isbnValue.setText(text(book.getIsbn()));
        publisherValue.setText(text(book.getPublisher()));
        dateValue.setText(text(book.getPublicationDate()));
        copiesValue.setText(book.getAvailableCopies() + " / " + book.getTotalCopies());

        tagsRow.removeAll();
        String tags = book.getTags();
        if (tags != null && !tags.trim().isEmpty()) {
            for (String tag : tags.split(",")) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    tagsRow.add(Theme.chip(trimmed));
                }
            }
        } else {
            JLabel none = new JLabel("No tags");
            none.setFont(Theme.FONT_CARD_MUTED);
            none.setForeground(Theme.TEXT_MUTED);
            tagsRow.add(none);
        }
        tagsRow.revalidate();
        tagsRow.repaint();

        copiesRow.removeAll();
        int total = Math.max(0, book.getTotalCopies());
        int available = Math.max(0, Math.min(book.getAvailableCopies(), total));
        for (int i = 0; i < total; i++) {
            JPanel swatch = new JPanel();
            swatch.setPreferredSize(new java.awt.Dimension(16, 16));
            swatch.setBackground(i < available ? Theme.BLUE_ACCENT : new Color(0xA5, 0x33, 0x33));
            swatch.setToolTipText(i < available ? "Available" : "Checked out");
            copiesRow.add(swatch);
        }
        if (total == 0) {
            JLabel none = new JLabel("No copies on record");
            none.setFont(Theme.FONT_CARD_MUTED);
            none.setForeground(Theme.TEXT_MUTED);
            copiesRow.add(none);
        }
        copiesRow.revalidate();
        copiesRow.repaint();
    }

    private static String text(String value) {
        return (value == null || value.isEmpty()) ? "\u2014" : value;
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
