package lms.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import lms.core.Book;

@SuppressWarnings("serial")
public class BookListPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblListOfBooks;
    private JTable table;
    private JTextField txtSearch;
    private DefaultTableModel tableModel;

    // The rows currently shown in the table (i.e. after the search filter
    // is applied), in the same order as the table. Row index in the table
    // maps 1:1 to index in this list, so a click on row N always refers
    // to displayedBooks.get(N) -- no ID lookup needed.
    private List<Book> displayedBooks = new ArrayList<>();

    /**
     * Create the panel.
     */
    public BookListPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setLayout(null);

        lblListOfBooks = new JLabel("LIST OF BOOKS");
        lblListOfBooks.setHorizontalAlignment(SwingConstants.CENTER);
        lblListOfBooks.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblListOfBooks.setBounds(0, 8, 640, 20);
        add(lblListOfBooks);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setBounds(20, 36, 60, 20);
        add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setBounds(85, 36, 220, 22);
        add(txtSearch);
        // Live filter: re-run the search on every keystroke instead of
        // waiting for a button press.
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshTable();
            }
        });

        // Column headers match Book's real fields (no more "New column"
        // placeholders). isCellEditable is overridden so the table is
        // display-only -- edits always go through the Add/Edit screen,
        // never by typing straight into a cell.
        tableModel = new DefaultTableModel(
            new String[] { "Book ID", "Title", "Author", "Genre", "Availability" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setBorder(new LineBorder(new Color(0, 0, 0)));
        table.setBounds(20, 66, 430, 210);
        add(table);

        // Column widths only need to be set once -- refreshTable() below
        // reuses the same tableModel/columns and only swaps the row data,
        // so these widths stick around across refreshes.
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(135);
        table.getColumnModel().getColumn(2).setPreferredWidth(105);
        table.getColumnModel().getColumn(3).setPreferredWidth(65);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);

        // Double-click a row as a shortcut for Edit.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedBook();
                }
            }
        });

        JButton btnAddNew = new JButton("ADD NEW");
        btnAddNew.setBounds(460, 66, 150, 28);
        btnAddNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showBookForm(null);
            }
        });
        add(btnAddNew);

        JButton btnEdit = new JButton("EDIT");
        btnEdit.setBounds(460, 100, 150, 28);
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedBook();
            }
        });
        add(btnEdit);

        JButton btnDelete = new JButton("DELETE");
        btnDelete.setBounds(460, 134, 150, 28);
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedBook();
            }
        });
        add(btnDelete);

        JButton btnBack = new JButton("BACK");
        btnBack.setBounds(270, 300, 100, 28);
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainFrame.showCard(MainFrame.CARD_DASHBOARD);
            }
        });
        add(btnBack);
    }

    /**
     * Reloads the table from the shared Library, applying the current
     * search text as a filter. Called on every keystroke in the search
     * box, and by MainFrame every time this screen is shown (see
     * MainFrame.showCard) so the table never shows stale data.
     */
    public void refreshTable() {
        String query = txtSearch.getText().trim().toLowerCase();

        displayedBooks = new ArrayList<>();
        for (Book book : mainFrame.getLibrary().getBooks()) {
            if (query.isEmpty() || matches(book, query)) {
                displayedBooks.add(book);
            }
        }

        tableModel.setRowCount(0);
        for (Book book : displayedBooks) {
            tableModel.addRow(new Object[] {
                book.getBookId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.isAvailable() ? "Available" : "Borrowed"
            });
        }
    }

    /** True if any of the book's fields contain the search text. */
    private boolean matches(Book book, String query) {
        return contains(book.getBookId(), query)
            || contains(book.getTitle(), query)
            || contains(book.getAuthor(), query)
            || contains(book.getGenre(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    /** Opens the Add/Edit screen pre-filled with the selected row, if any. */
    private void editSelectedBook() {
        Book selected = getSelectedBook();
        if (selected != null) {
            mainFrame.showBookForm(selected);
        }
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
            refreshTable();
        }
    }

    /**
     * Returns the Book behind the selected row, or null (with a message
     * dialog instead of an exception) if nothing is selected.
     */
    private Book getSelectedBook() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a book first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return displayedBooks.get(row);
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
