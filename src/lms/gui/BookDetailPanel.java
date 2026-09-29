package lms.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import lms.core.Book;

/**
 * Read-only, in-depth look at a single book: full details, a tag list,
 * and a per-copy tracker (one square per physical copy, colored by
 * whether it's presently available) -- opened by double-clicking a row
 * on the Book List, since that screen is browse/search only now and no
 * longer where fields get edited (see BookListPanel).
 */
@SuppressWarnings("serial")
public class BookDetailPanel extends JPanel {

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();

    private JLabel banner;
    private static final int TITLE_W = 420;

    private FitLabel titleLabel;
    private JLabel authorLabel;
    private JLabel genreLabel;
    private JLabel isbnValue;
    private JLabel publisherValue;
    private JLabel dateValue;
    private JLabel copiesValue;
    private JPanel tagsRow;
    private CopyGrid copiesGrid;

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

        titleLabel = new FitLabel().shrink(6);
        titleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        // Tall enough for two lines and bottom-aligned, so a one-line title sits
        // where it always did (see FitLabel.setWrappedText).
        titleLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        titleLabel.setBorder(new EmptyBorder(0, 0, 2, 0));
        place(titleLabel, 100, 36, TITLE_W, 34);
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

        // A single custom-painted component instead of one real JPanel
        // "swatch" per physical copy: the old version could hang for a
        // moment on a book with a very large copy count, since it had
        // to build, lay out, and register a tooltip on that many live
        // Swing components every time this screen opened. Drawing NxM
        // rectangles in one paintComponent() call, the same way this
        // app's barcode is drawn (see MemberDetailPanel.BarcodeLabel),
        // costs a fraction of that regardless of how large the count
        // gets, while keeping the same look and the same per-square
        // "Available"/"Checked out" tooltip.
        copiesGrid = new CopyGrid();
        place(copiesGrid, 30, 254, 570, 40);
        add(copiesGrid);

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

        titleLabel.setWrappedText(book.getTitle(), TITLE_W);
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

        copiesGrid.setCopies(book.getAvailableCopies(), book.getTotalCopies());
    }

    private static String text(String value) {
        return (value == null || value.isEmpty()) ? "\u2014" : value;
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * Draws the copy tracker as a grid of small squares in one
     * paintComponent() call instead of one real component per copy.
     * When there isn't room to draw every copy in the space given, the
     * grid fills what fits and folds the rest into a "+N" label rather
     * than silently overflowing past its own bounds (which is what a
     * FlowLayout of that many components would otherwise do here).
     */
    private static class CopyGrid extends JComponent {
        private static final int SIZE = 16;
        private static final int GAP = 3;
        private static final int STEP = SIZE + GAP;

        private int available;
        private int total;

        CopyGrid() {
            ToolTipManager.sharedInstance().registerComponent(this);
        }

        void setCopies(int available, int total) {
            this.total = Math.max(0, total);
            this.available = Math.max(0, Math.min(available, this.total));
            repaint();
        }

        private int columns() {
            return Math.max(1, (getWidth() + GAP) / STEP);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (total == 0) {
                g2.setFont(Theme.FONT_CARD_MUTED);
                g2.setColor(Theme.TEXT_MUTED);
                g2.drawString("No copies on record", 0, SIZE - 3);
                g2.dispose();
                return;
            }

            int cols = columns();
            int rows = Math.max(1, (getHeight() + GAP) / STEP);
            int capacity = cols * rows;
            boolean overflow = total > capacity;
            // Leaves the last cell free for the "+N" label below.
            int shown = overflow ? Math.max(0, capacity - 1) : total;

            for (int i = 0; i < shown; i++) {
                g2.setColor(i < available ? Theme.BLUE_ACCENT : new Color(0xA5, 0x33, 0x33));
                g2.fillRect((i % cols) * STEP, (i / cols) * STEP, SIZE, SIZE);
            }
            if (overflow) {
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(Theme.FONT_CARD_CAPTION);
                g2.drawString("+" + (total - shown), (shown % cols) * STEP, (shown / cols) * STEP + SIZE - 4);
            }
            g2.dispose();
        }

        /** Location-sensitive tooltip: which copy square (if any) is under the cursor. */
        @Override
        public String getToolTipText(MouseEvent event) {
            if (total == 0) {
                return null;
            }
            int cols = columns();
            int col = event.getX() / STEP;
            int row = event.getY() / STEP;
            if (col < 0 || col >= cols) {
                return null;
            }
            int index = row * cols + col;
            if (index < 0 || index >= total) {
                return null;
            }
            return index < available ? "Available" : "Checked out";
        }
    }
}
