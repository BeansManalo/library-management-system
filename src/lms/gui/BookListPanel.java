package lms.gui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lms.core.Book;

@SuppressWarnings("serial")
public class BookListPanel extends JPanel {

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();
    private JLabel banner;
    private JTextField txtSearch;
    private CardListPanel cardListPanel;
    private PillButton btnView;
    private PillButton btnAddNew;
    private PillButton btnDelete;

    // True when this screen is playing the "Add / Edit Books" role (Add
    // New + Delete visible, double-click opens the editable form)
    // instead of the plain read-only "Book List" role (double-click
    // opens the read-only Book Details viewer instead, nothing here can
    // change a book). Both roles share this one screen/card -- see
    // MainFrame.showBookList() / showBookManage() -- the same way
    // AddEditBookPanel already reuses one screen for both Add and Edit.
    private boolean manageMode;

    // The rows currently shown (i.e. after the search filter is applied),
    // in the same order as the card list. cardPanels is the on-screen
    // component for each entry in displayedBooks, kept in step with it so
    // a click can flip one card's selected look without rebuilding the
    // whole list.
    private List<Book> displayedBooks = new ArrayList<>();
    private List<BookCardPanel> cardPanels = new ArrayList<>();
    private int selectedIndex = -1;

    /**
     * Create the panel.
     */
    public BookListPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        // Catches clicks on the blank margins around the table and
        // buttons -- everywhere that isn't a specific control. The
        // table's own blank space (below the last row) is handled
        // separately below, since the scroll pane's viewport sits in
        // front of this panel there.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectRow(-1);
            }
        });

        banner = Theme.banner("LIST OF BOOKS");
        place(banner, 0, 0, 640, 30);
        add(banner);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(Theme.FONT_LABEL);
        place(lblSearch, 20, 40, 60, 22);
        add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setFont(Theme.FONT_FIELD);
        txtSearch.setBorder(new javax.swing.border.CompoundBorder(
            new LineBorder(Theme.DIVIDER, 1), new EmptyBorder(2, 6, 2, 6)));
        place(txtSearch, 85, 40, 220, 24);
        add(txtSearch);
        // Live filter: re-run the search on every keystroke instead of
        // waiting for a button press.
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refresh();
            }
        });

        cardListPanel = new CardListPanel();
        JScrollPane scrollPane = new JScrollPane(cardListPanel);
        scrollPane.setBorder(new LineBorder(Theme.DIVIDER, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        scrollPane.getViewport().setBackground(Theme.CARD_BG);
        place(scrollPane, 20, 72, 430, 212);
        add(scrollPane);
        CardListPanel.makeBackgroundDeselectable(scrollPane, cardListPanel, () -> selectRow(-1));

        // Visible in both roles, at the top of the button column so
        // there's no dead space above it when Add New / Delete are
        // hidden -- see setManageMode() for the VIEW/EDIT label swap.
        btnView = new PillButton("VIEW");
        place(btnView, 460, 72, 150, 30);
        btnView.addActionListener(e -> {
            Book selected = getSelectedBook();
            if (selected != null) {
                openBook(selected);
            }
        });
        add(btnView);

        // Add New / Delete are the management role's buttons only --
        // see setManageMode() -- hidden by default so this screen opens
        // as the plain read-only list.
        btnAddNew = new PillButton("ADD NEW");
        place(btnAddNew, 460, 110, 150, 30);
        btnAddNew.addActionListener(e -> mainFrame.showBookForm(null));
        add(btnAddNew);

        btnDelete = new PillButton("DELETE", new Color(0xA5, 0x33, 0x33),
            new Color(0xC0, 0x45, 0x45), new Color(0x80, 0x24, 0x24));
        place(btnDelete, 460, 148, 150, 30);
        btnDelete.addActionListener(e -> deleteSelectedBook());
        add(btnDelete);

        PillButton btnBack = new PillButton("BACK");
        place(btnBack, 270, 300, 100, 28);
        btnBack.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_DASHBOARD));
        add(btnBack);

        setManageMode(false);
    }

    private void place(java.awt.Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    /**
     * Switches this screen between the plain read-only "Book List" role
     * and the "Add / Edit Books" management role: only the management
     * role shows Add New / Delete, and only it opens the editable form
     * on double-click (the plain list opens the read-only viewer
     * instead). Called by MainFrame right before showing this card --
     * see MainFrame.showBookList() / showBookManage().
     */
    public void setManageMode(boolean manageMode) {
        this.manageMode = manageMode;
        banner.setText(manageMode ? "ADD / EDIT BOOKS" : "LIST OF BOOKS");
        btnView.setText(manageMode ? "EDIT" : "VIEW");
        btnAddNew.setVisible(manageMode);
        btnDelete.setVisible(manageMode);
    }

    /**
     * Reloads the card list from the shared Library, applying the current
     * search text as a filter. Called on every keystroke in the search
     * box, and by MainFrame every time this screen is shown (see
     * MainFrame.showCard) so the list never shows stale data.
     */
    public void refresh() {
        String query = txtSearch.getText().trim().toLowerCase();

        displayedBooks = new ArrayList<>();
        for (Book book : mainFrame.getLibrary().getBooks()) {
            if (query.isEmpty() || matches(book, query)) {
                displayedBooks.add(book);
            }
        }

        selectedIndex = -1;
        cardPanels = new ArrayList<>();
        cardListPanel.removeAll();
        for (int i = 0; i < displayedBooks.size(); i++) {
            Book book = displayedBooks.get(i);
            int rowIndex = i;
            BookCardPanel card = new BookCardPanel(book,
                () -> selectRow(rowIndex),
                () -> openBook(book));
            cardPanels.add(card);
            cardListPanel.add(card);
        }
        cardListPanel.revalidate();
        cardListPanel.repaint();
    }

    /** Double-click target: the editable form in manage mode, the read-only viewer otherwise. */
    private void openBook(Book book) {
        if (manageMode) {
            mainFrame.showBookForm(book);
        } else {
            mainFrame.showBookDetail(book);
        }
    }

    private void selectRow(int index) {
        selectedIndex = index;
        for (int i = 0; i < cardPanels.size(); i++) {
            cardPanels.get(i).setSelected(i == index);
        }
    }

    /** True if any of the book's fields (including its tags) contain the search text. */
    private boolean matches(Book book, String query) {
        return contains(book.getTitle(), query)
            || contains(book.getAuthor(), query)
            || contains(book.getGenre(), query)
            || contains(book.getIsbn(), query)
            || contains(book.getPublisher(), query)
            || contains(book.getTags(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    /** Removes the selected row from the library, after confirming with the user. */
    private void deleteSelectedBook() {
        Book selected = getSelectedBook();
        if (selected == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "Delete \"" + selected.getTitle() + "\"?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            mainFrame.getLibrary().getBooks().remove(selected);
            refresh();
        }
    }

    /**
     * Returns the Book behind the selected row, or null (with a message
     * dialog instead of an exception) if nothing is selected.
     */
    private Book getSelectedBook() {
        if (selectedIndex < 0 || selectedIndex >= displayedBooks.size()) {
            JOptionPane.showMessageDialog(this,
                "Please select a book first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return displayedBooks.get(selectedIndex);
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
