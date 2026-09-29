package lms.gui;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import lms.core.Book;
import lms.core.Library;
import lms.core.ValidationException;

@SuppressWarnings("serial")
public class AddEditBookPanel extends JPanel {

    // Was 160 -- widening to fill the column closes the dead gap that
    // used to sit between the two columns and balances the margin on
    // both sides of the form.
    private static final int FIELD_WIDTH = 260;

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();
    private JLabel lblAddAndEditBooks;
    private JTextField txtIsbn;
    private JTextField txtTitle;
    private JTextField txtAuthor;
    private JTextField txtGenre;
    private JTextField txtPublisher;
    private DatePickerField txtPublicationDate;
    private JTextField txtTotalCopies;
    private JTextField txtTags;

    // The book currently being edited, or null while adding a new one.
    // Set by loadBook(), which MainFrame calls right before switching to
    // this card -- see MainFrame.showBookForm().
    private Book editingBook;

    // Shown/parsed as e.g. "February 22, 2022", matching how the book
    // card displays it.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US);

    /**
     * Create the panel.
     */
    public AddEditBookPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        lblAddAndEditBooks = Theme.banner("ADD BOOK");
        place(lblAddAndEditBooks, 0, 0, 640, 30);
        add(lblAddAndEditBooks);

        // Left column: what the book is. ISBN anchors it (there's no
        // separate Book ID -- the ISBN already uniquely identifies the
        // title, so a second internal ID would just be one more field to
        // keep in sync with nothing extra to show for it).
        txtIsbn = field("ISBN:", 40, 42);
        txtTitle = field("Title:", 40, 92);
        txtAuthor = field("Author:", 40, 142);
        txtGenre = field("Genre:", 40, 192);

        // Right column: catalog/inventory details. No "Available Copies"
        // field here -- see save() below for why.
        txtPublisher = field("Publisher:", 340, 42);
        txtPublicationDate = dateField("Publication Date:", 340, 92, DATE_FORMAT);
        txtTotalCopies = field("Total Copies:", 340, 142);
        txtTags = field("Tags:", 340, 192);
        txtTags.setToolTipText("Comma-separated, e.g. cozy, mystery, sequel");

        JLabel tagsHint = new JLabel("Separate multiple tags with a comma");
        tagsHint.setFont(Theme.FONT_CARD_MUTED);
        tagsHint.setForeground(Theme.TEXT_MUTED);
        place(tagsHint, 340, 236, FIELD_WIDTH, 14);
        add(tagsHint);

        PillButton btnSave = new PillButton("SAVE");
        place(btnSave, 215, 260, 100, 32);
        btnSave.addActionListener(e -> save());
        add(btnSave);

        PillButton btnCancel = new PillButton("CANCEL", Theme.TEXT_MUTED,
            Theme.TEXT_MUTED.brighter(), Theme.TEXT_PRIMARY);
        place(btnCancel, 325, 260, 100, 32);
        // Nothing has been written to editingBook at this point (see
        // save() below), so just navigating away is enough to discard
        // whatever the user typed. This screen is only ever reached from
        // the "Add / Edit Books" list (see BookListPanel's manage mode),
        // so that's where Cancel returns to.
        btnCancel.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_BOOK_LIST));
        add(btnCancel);
    }

    /** Registers a component's design-time bounds with the resizable layout. */
    private void place(java.awt.Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    /** Adds a label + text field pair at (x, y) and returns the field. */
    private JTextField field(String labelText, int x, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(Theme.TEXT_PRIMARY);
        place(label, x, y, 150, 16);
        add(label);

        JTextField text = new JTextField();
        text.setFont(Theme.FONT_FIELD);
        text.setBorder(new javax.swing.border.CompoundBorder(
            new javax.swing.border.LineBorder(Theme.DIVIDER, 1),
            new javax.swing.border.EmptyBorder(2, 6, 2, 6)));
        place(text, x, y + 18, FIELD_WIDTH, 24);
        add(text);
        return text;
    }

    /** Same as field(), but with a calendar popup instead of a plain text box. */
    private DatePickerField dateField(String labelText, int x, int y, DateTimeFormatter format) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(Theme.TEXT_PRIMARY);
        place(label, x, y, 150, 16);
        add(label);

        DatePickerField picker = new DatePickerField(format);
        place(picker, x, y + 18, FIELD_WIDTH, 24);
        add(picker);
        return picker;
    }

    /**
     * Prepares this screen for the given book. Called by MainFrame right
     * before it switches to this card (see MainFrame.showBookForm), since
     * with CardLayout this panel object is reused rather than recreated
     * on every visit -- without this, a second visit would still be
     * showing whatever was typed in during the first one.
     *
     * @param book the book to edit, or null to start a blank "Add New" form
     */
    public void loadBook(Book book) {
        editingBook = book;
        txtPublicationDate.setMaxDate(Library.today());

        if (book == null) {
            lblAddAndEditBooks.setText("ADD BOOK");
            txtIsbn.setText("");
            txtTitle.setText("");
            txtAuthor.setText("");
            txtGenre.setText("");
            txtPublisher.setText("");
            txtPublicationDate.setText("");
            txtTotalCopies.setText("");
            txtTags.setText("");
        } else {
            lblAddAndEditBooks.setText("EDIT BOOK");
            txtIsbn.setText(book.getIsbn());
            txtTitle.setText(book.getTitle());
            txtAuthor.setText(book.getAuthor());
            txtGenre.setText(book.getGenre());
            txtPublisher.setText(book.getPublisher());
            txtPublicationDate.setText(book.getPublicationDate());
            txtTotalCopies.setText(String.valueOf(book.getTotalCopies()));
            txtTags.setText(book.getTags());
        }
    }

    /**
     * Validates the form, then either creates a new Book and adds it to
     * the shared list, or updates the book already being edited in place.
     * Either way, control returns to the Book List screen afterwards.
     */
    private void save() {
        int totalCopies;
        try {
            validateRequired(txtIsbn.getText(), txtTitle.getText(), txtAuthor.getText(),
                txtGenre.getText(), txtPublisher.getText(), txtPublicationDate.getText());
            totalCopies = parseNonNegative(txtTotalCopies.getText(), "Total Copies");
            mainFrame.getLibrary().checkIsbnFree(txtIsbn.getText(), editingBook);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isNew = (editingBook == null);
        Book book = isNew ? new Book() : editingBook;

        // How many copies are currently checked out (0 for a brand-new
        // book) -- captured before setTotalCopies() below overwrites the
        // old total, so it can be carried over onto the new total.
        int checkedOut = book.getTotalCopies() - book.getAvailableCopies();

        book.setIsbn(txtIsbn.getText().trim());
        book.setTitle(txtTitle.getText().trim());
        book.setAuthor(txtAuthor.getText().trim());
        book.setGenre(txtGenre.getText().trim());
        book.setPublisher(txtPublisher.getText().trim());
        book.setPublicationDate(txtPublicationDate.getText().trim());
        book.setTotalCopies(totalCopies);
        book.setTags(txtTags.getText().trim());

        // Available Copies isn't a form field -- how many of a book are
        // actually on the shelf right now is what a borrowing system
        // would own, not something to hand-type here. Instead, whatever
        // was already checked out carries over onto the new total, so
        // raising Total Copies raises Available Copies to match (the new
        // copies start on the shelf) and lowering it lowers Available
        // Copies too, never below 0.
        book.setAvailableCopies(Math.max(0, totalCopies - checkedOut));

        if (isNew) {
            mainFrame.getLibrary().getBooks().add(book);
        }
        // When editing, "book" is the same object reference stored in the
        // shared list, so the setters above already updated it in place --
        // no separate "replace in list" step is needed.

        mainFrame.saveLibrary();
        mainFrame.showCard(MainFrame.CARD_BOOK_LIST);
    }

    /** Throws ValidationException naming the first required field left blank. */
    private void validateRequired(String isbn, String title, String author, String genre,
            String publisher, String publicationDate) throws ValidationException {
        if (isbn.trim().isEmpty()) {
            throw new ValidationException("ISBN is required.");
        }
        if (title.trim().isEmpty()) {
            throw new ValidationException("Title is required.");
        }
        if (author.trim().isEmpty()) {
            throw new ValidationException("Author is required.");
        }
        if (genre.trim().isEmpty()) {
            throw new ValidationException("Genre is required.");
        }
        if (publisher.trim().isEmpty()) {
            throw new ValidationException("Publisher is required.");
        }
        if (publicationDate.trim().isEmpty()) {
            throw new ValidationException("Publication Date is required.");
        }
    }

    /** Parses a whole number >= 0, or throws ValidationException naming the field. */
    private int parseNonNegative(String value, String fieldName) throws ValidationException {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                throw new ValidationException(fieldName + " can't be negative.");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new ValidationException(fieldName + " must be a whole number.");
        }
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
