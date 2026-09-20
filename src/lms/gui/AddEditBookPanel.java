package lms.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import lms.core.Book;
import lms.core.ValidationException;

@SuppressWarnings("serial")
public class AddEditBookPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblAddAndEditBooks;
    private JTextField txtBookId;
    private JTextField txtTitle;
    private JTextField txtAuthor;
    private JTextField txtGenre;
    private JCheckBox chkAvailable;

    // The book currently being edited, or null while adding a new one.
    // Set by loadBook(), which MainFrame calls right before switching to
    // this card -- see MainFrame.showBookForm().
    private Book editingBook;

    /**
     * Create the panel.
     */
    public AddEditBookPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setLayout(null);

        lblAddAndEditBooks = new JLabel("ADD BOOK");
        lblAddAndEditBooks.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblAddAndEditBooks.setHorizontalAlignment(SwingConstants.CENTER);
        lblAddAndEditBooks.setBounds(0, 10, 640, 24);
        add(lblAddAndEditBooks);

        JLabel lblBookId = new JLabel("Book ID:");
        lblBookId.setBounds(150, 55, 90, 20);
        add(lblBookId);

        txtBookId = new JTextField();
        txtBookId.setBounds(250, 54, 220, 22);
        add(txtBookId);

        JLabel lblTitle = new JLabel("Title:");
        lblTitle.setBounds(150, 90, 90, 20);
        add(lblTitle);

        txtTitle = new JTextField();
        txtTitle.setBounds(250, 89, 220, 22);
        add(txtTitle);

        JLabel lblAuthor = new JLabel("Author:");
        lblAuthor.setBounds(150, 125, 90, 20);
        add(lblAuthor);

        txtAuthor = new JTextField();
        txtAuthor.setBounds(250, 124, 220, 22);
        add(txtAuthor);

        JLabel lblGenre = new JLabel("Genre:");
        lblGenre.setBounds(150, 160, 90, 20);
        add(lblGenre);

        txtGenre = new JTextField();
        txtGenre.setBounds(250, 159, 220, 22);
        add(txtGenre);

        JLabel lblAvailable = new JLabel("Available:");
        lblAvailable.setBounds(150, 195, 90, 20);
        add(lblAvailable);

        chkAvailable = new JCheckBox();
        chkAvailable.setBounds(250, 194, 20, 20);
        add(chkAvailable);

        JButton btnSave = new JButton("SAVE");
        btnSave.setBounds(215, 240, 100, 28);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        add(btnSave);

        JButton btnCancel = new JButton("CANCEL");
        btnCancel.setBounds(325, 240, 100, 28);
        btnCancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Nothing has been written to editingBook at this point
                // (see save() below), so just navigating away is enough
                // to discard whatever the user typed.
                mainFrame.showCard(MainFrame.CARD_DASHBOARD);
            }
        });
        add(btnCancel);
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

        if (book == null) {
            lblAddAndEditBooks.setText("ADD BOOK");
            txtBookId.setText("");
            txtTitle.setText("");
            txtAuthor.setText("");
            txtGenre.setText("");
            chkAvailable.setSelected(true);
        } else {
            lblAddAndEditBooks.setText("EDIT BOOK");
            txtBookId.setText(book.getBookId());
            txtTitle.setText(book.getTitle());
            txtAuthor.setText(book.getAuthor());
            txtGenre.setText(book.getGenre());
            chkAvailable.setSelected(book.isAvailable());
        }
    }

    /**
     * Validates the form, then either creates a new Book and adds it to
     * the shared list, or updates the book already being edited in place.
     * Either way, control returns to the Book List screen afterwards.
     */
    private void save() {
        try {
            validate(txtBookId.getText(), txtTitle.getText(), txtAuthor.getText(), txtGenre.getText());
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isNew = (editingBook == null);
        Book book = isNew ? new Book() : editingBook;

        book.setBookId(txtBookId.getText().trim());
        book.setTitle(txtTitle.getText().trim());
        book.setAuthor(txtAuthor.getText().trim());
        book.setGenre(txtGenre.getText().trim());
        book.setAvailable(chkAvailable.isSelected());

        if (isNew) {
            mainFrame.getLibrary().getBooks().add(book);
        }
        // When editing, "book" is the same object reference stored in the
        // shared list, so the setters above already updated it in place --
        // no separate "replace in list" step is needed.

        mainFrame.showCard(MainFrame.CARD_BOOK_LIST);
    }

    /** Throws ValidationException naming the first required field left blank. */
    private void validate(String bookId, String title, String author, String genre) throws ValidationException {
        if (bookId.trim().isEmpty()) {
            throw new ValidationException("Book ID is required.");
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
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
