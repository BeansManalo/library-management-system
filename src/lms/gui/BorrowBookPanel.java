package lms.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import lms.core.Book;
import lms.core.Library;
import lms.core.Loan;
import lms.core.Member;

/**
 * Step 2 of borrowing (step 1 is picking the member -- see
 * MemberListPanel.Mode.PICK): build the list of books this member is
 * taking. Laid out like the checkout screens in other library systems
 * (BiblioteQ, Koha, Evergreen): the catalogue on the left, the
 * "books to borrow" list on the right, and per-book quantity and
 * return date chosen at the moment a book is added -- so books borrowed
 * together can still be due back on different days.
 *
 * Date rules: the borrow date can't be in the future (no reservations)
 * or before the member joined; a return date can't be in the past.
 * "Review" hands the result to BorrowConfirmPanel.
 */
@SuppressWarnings("serial")
public class BorrowBookPanel extends JPanel {

    /** One row of "books to borrow"; becomes a Loan once the borrow date is settled at review time. */
    private record Line(Book book, int quantity, LocalDate dueDate) { }

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();

    private Member member;
    private JLabel lblMember;
    private DatePickerField txtBorrowDate;
    private DatePickerField txtReturnDate;
    private JTextField txtSearch;
    private JTable bookTable;
    private final SpinnerNumberModel qtyModel = new SpinnerNumberModel(1, 1, 1, 1);

    private List<Book> displayedBooks = new ArrayList<>();
    private final List<Line> basket = new ArrayList<>();

    private final AbstractTableModel bookModel = new AbstractTableModel() {
        private final String[] names = {"Title", "Author", "Avail."};

        @Override
        public int getRowCount() {
            return displayedBooks.size();
        }

        @Override
        public int getColumnCount() {
            return names.length;
        }

        @Override
        public String getColumnName(int column) {
            return names[column];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 2 ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int column) {
            Book book = displayedBooks.get(row);
            return switch (column) {
                case 0 -> book.getTitle();
                case 1 -> book.getAuthor();
                default -> remaining(book);
            };
        }
    };

    private final AbstractTableModel basketModel = new AbstractTableModel() {
        private final String[] names = {"Title", "Qty", "Return by"};

        @Override
        public int getRowCount() {
            return basket.size();
        }

        @Override
        public int getColumnCount() {
            return names.length;
        }

        @Override
        public String getColumnName(int column) {
            return names[column];
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return column == 1 ? Integer.class : String.class;
        }

        @Override
        public Object getValueAt(int row, int column) {
            Line line = basket.get(row);
            return switch (column) {
                case 0 -> line.book().getTitle();
                case 1 -> line.quantity();
                default -> Member.DATE_FORMAT.format(line.dueDate());
            };
        }
    };

    private JTable basketTable;

    public BorrowBookPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        JLabel banner = Theme.banner("BORROW BOOK");
        place(banner, 0, 0, 640, 30);
        add(banner);

        lblMember = new JLabel();
        lblMember.setFont(Theme.FONT_CARD_TITLE);
        lblMember.setForeground(Theme.TEXT_PRIMARY);
        place(lblMember, 20, 36, 350, 22);
        add(lblMember);

        JLabel lblBorrowDate = label("Borrow date:", SwingConstants.RIGHT);
        place(lblBorrowDate, 380, 36, 78, 22);
        add(lblBorrowDate);

        txtBorrowDate = new DatePickerField(Member.DATE_FORMAT);
        // A new borrow date changes what "about a week from now" means.
        txtBorrowDate.setOnDateChanged(() -> txtReturnDate.setDate(defaultReturnDate()));
        place(txtBorrowDate, 462, 36, 148, 22);
        add(txtBorrowDate);

        JLabel lblSearch = label("Search:", SwingConstants.LEFT);
        place(lblSearch, 20, 64, 50, 22);
        add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setFont(Theme.FONT_FIELD);
        txtSearch.setBorder(new CompoundBorder(new LineBorder(Theme.DIVIDER, 1), new EmptyBorder(2, 6, 2, 6)));
        place(txtSearch, 72, 64, 248, 22);
        add(txtSearch);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshBooks();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshBooks();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshBooks();
            }
        });

        JLabel lblBasket = new JLabel("Books to borrow");
        lblBasket.setFont(Theme.FONT_CARD_TITLE);
        lblBasket.setForeground(Theme.TEXT_PRIMARY);
        place(lblBasket, 332, 64, 200, 22);
        add(lblBasket);

        bookTable = Theme.table(bookModel, 170, 110, 40);
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                syncQuantityLimit();
            }
        });
        bookTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedBook();
                }
            }
        });
        JScrollPane bookScroll = Theme.scroll(bookTable);
        place(bookScroll, 20, 90, 300, 160);
        add(bookScroll);

        basketTable = Theme.table(basketModel, 150, 30, 80);
        JScrollPane basketScroll = Theme.scroll(basketTable);
        place(basketScroll, 332, 90, 288, 160);
        add(basketScroll);

        JLabel lblQty = label("Qty:", SwingConstants.LEFT);
        place(lblQty, 20, 258, 28, 22);
        add(lblQty);

        JSpinner spnQuantity = new JSpinner(qtyModel);
        spnQuantity.setFont(Theme.FONT_FIELD);
        place(spnQuantity, 50, 258, 54, 22);
        add(spnQuantity);

        JLabel lblReturn = label("Return by:", SwingConstants.LEFT);
        place(lblReturn, 114, 258, 60, 22);
        add(lblReturn);

        txtReturnDate = new DatePickerField(Member.DATE_FORMAT);
        place(txtReturnDate, 176, 258, 130, 22);
        add(txtReturnDate);

        PillButton btnAdd = new PillButton("ADD");
        place(btnAdd, 318, 256, 80, 26);
        btnAdd.addActionListener(e -> addSelectedBook());
        add(btnAdd);

        PillButton btnRemove = new PillButton("REMOVE", new Color(0xA5, 0x33, 0x33),
            new Color(0xC0, 0x45, 0x45), new Color(0x80, 0x24, 0x24));
        place(btnRemove, 520, 256, 100, 26);
        btnRemove.addActionListener(e -> removeSelectedLine());
        add(btnRemove);

        PillButton btnBack = new PillButton("BACK");
        place(btnBack, 190, 300, 110, 28);
        btnBack.addActionListener(e -> goBack());
        add(btnBack);

        PillButton btnReview = new PillButton("REVIEW");
        place(btnReview, 320, 300, 130, 28);
        btnReview.addActionListener(e -> review());
        add(btnReview);
    }

    private JLabel label(String text, int alignment) {
        JLabel l = new JLabel(text, alignment);
        l.setFont(Theme.FONT_LABEL);
        return l;
    }

    private void place(Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    /**
     * Called by MainFrame when a member has been picked. Starts a fresh
     * list for them and returns true, or -- if their membership has
     * already ended -- explains why and returns false so the screen
     * doesn't open.
     */
    public boolean startFor(Member member) {
        LocalDate today = Library.today();
        if (member.isExpired()) {
            JOptionPane.showMessageDialog(this,
                member.getName() + "'s membership ended on " + member.getEndDate() + ".\n"
                    + "Renew it under Add / Edit Members before borrowing.",
                "Membership Ended", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        this.member = member;
        basket.clear();
        lblMember.setText("Member: " + member.getName() + "  (" + member.getMemberId() + ")");

        txtBorrowDate.setMinDate(member.getJoinLocalDate()); // can't borrow before joining
        txtBorrowDate.setMaxDate(today);                     // ...or from the future
        txtBorrowDate.setDate(today);
        txtReturnDate.setMinDate(today);                     // can't return from the past
        txtReturnDate.setDate(defaultReturnDate());

        txtSearch.setText("");
        refreshBooks();
        basketModel.fireTableDataChanged();
        return true;
    }

    /** About a week after the borrow date, but never earlier than today. */
    private LocalDate defaultReturnDate() {
        LocalDate today = Library.today();
        LocalDate borrowed = txtBorrowDate.getDate();
        LocalDate due = (borrowed == null ? today : borrowed).plusDays(Loan.DEFAULT_LOAN_DAYS);
        return due.isBefore(today) ? today : due;
    }

    /** Copies of this book still free to add: what's on the shelf minus what's already in the list. */
    private int remaining(Book book) {
        int inBasket = 0;
        for (Line line : basket) {
            if (line.book() == book) {
                inBasket += line.quantity();
            }
        }
        return book.getAvailableCopies() - inBasket;
    }

    private void refreshBooks() {
        String query = txtSearch.getText().trim().toLowerCase();
        displayedBooks = new ArrayList<>();
        for (Book book : mainFrame.getLibrary().getBooks()) {
            if (query.isEmpty() || book.matches(query)) {
                displayedBooks.add(book);
            }
        }
        bookModel.fireTableDataChanged();
        syncQuantityLimit();
    }

    /** The quantity spinner can never go past the copies still free for the selected book. */
    private void syncQuantityLimit() {
        int row = bookTable.getSelectedRow();
        int max = row < 0 ? 1 : Math.max(1, remaining(displayedBooks.get(row)));
        qtyModel.setMaximum(max);
        if (qtyModel.getNumber().intValue() > max) {
            qtyModel.setValue(max);
        }
    }

    private void addSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row < 0) {
            warn("Please select a book first.");
            return;
        }
        Book book = displayedBooks.get(row);
        int free = remaining(book);
        if (free < 1) {
            warn("No copies of \"" + book.getTitle() + "\" are available.");
            return;
        }
        int quantity = Math.min(qtyModel.getNumber().intValue(), free);
        LocalDate due = txtReturnDate.getDate();

        // Same book with the same return date just adds to that line.
        boolean merged = false;
        for (int i = 0; i < basket.size() && !merged; i++) {
            Line line = basket.get(i);
            if (line.book() == book && line.dueDate().equals(due)) {
                basket.set(i, new Line(book, line.quantity() + quantity, due));
                merged = true;
            }
        }
        if (!merged) {
            basket.add(new Line(book, quantity, due));
        }

        basketModel.fireTableDataChanged();
        bookModel.fireTableRowsUpdated(0, displayedBooks.size() - 1); // keeps the selection
        syncQuantityLimit();
    }

    private void removeSelectedLine() {
        int row = basketTable.getSelectedRow();
        if (row < 0) {
            warn("Please select a book in the list to remove.");
            return;
        }
        basket.remove(row);
        basketModel.fireTableDataChanged();
        if (!displayedBooks.isEmpty()) {
            bookModel.fireTableRowsUpdated(0, displayedBooks.size() - 1);
        }
        syncQuantityLimit();
    }

    private void review() {
        if (basket.isEmpty()) {
            warn("Add at least one book first.");
            return;
        }
        LocalDate borrowDate = txtBorrowDate.getDate();
        List<Loan> loans = new ArrayList<>();
        for (Line line : basket) {
            loans.add(new Loan(line.book(), line.quantity(), borrowDate, line.dueDate()));
        }
        mainFrame.showBorrowConfirm(member, loans);
    }

    private void goBack() {
        if (!basket.isEmpty() && JOptionPane.showConfirmDialog(this,
                "Discard the books you've added?", "Leave Borrowing",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        mainFrame.showBorrowMemberSelect();
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Borrow Book", JOptionPane.WARNING_MESSAGE);
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
