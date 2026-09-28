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
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import lms.core.Library.Return;
import lms.core.Library;
import lms.core.Loan;
import lms.core.Member;

/**
 * Step 2 of returning (step 1 is picking the member -- see
 * MemberListPanel.Mode.RETURN, which only lists members with books out):
 * the mirror image of BorrowBookPanel. The member's outstanding loans are
 * on the left, the "books to return" list on the right, and the quantity
 * is chosen as each is added -- so a member can bring back only some of
 * the copies they borrowed. One return date covers the whole list.
 *
 * Date rules: a book can't be returned on a future date or before it was
 * borrowed. "Review" hands the result to ReturnConfirmPanel.
 */
@SuppressWarnings("serial")
public class ReturnBookPanel extends JPanel {

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();

    private Member member;
    private JLabel lblMember;
    private DatePickerField txtReturnDate;
    private JTable loanTable;
    private JTable basketTable;
    private final SpinnerNumberModel qtyModel = new SpinnerNumberModel(1, 1, 1, 1);

    private List<Loan> outstanding = new ArrayList<>();
    private final List<Return> basket = new ArrayList<>();

    private final AbstractTableModel loanModel = new AbstractTableModel() {
        private final String[] names = {"Title", "Qty", "Due"};

        @Override
        public int getRowCount() {
            return outstanding.size();
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
            Loan loan = outstanding.get(row);
            return switch (column) {
                case 0 -> loan.getBook().getTitle();
                case 1 -> remaining(loan);
                default -> Member.DATE_FORMAT.format(loan.getDueDate())
                    + (loan.getStatus(Library.today()) == Loan.Status.OVERDUE ? "  (overdue)" : "");
            };
        }
    };

    private final AbstractTableModel basketModel = new AbstractTableModel() {
        private final String[] names = {"Title", "Qty", "Due"};

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
            Return line = basket.get(row);
            return switch (column) {
                case 0 -> line.loan().getBook().getTitle();
                case 1 -> line.quantity();
                default -> Member.DATE_FORMAT.format(line.loan().getDueDate());
            };
        }
    };

    public ReturnBookPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        JLabel banner = Theme.banner("RETURN BOOK");
        place(banner, 0, 0, 640, 30);
        add(banner);

        lblMember = new JLabel();
        lblMember.setFont(Theme.FONT_CARD_TITLE);
        lblMember.setForeground(Theme.TEXT_PRIMARY);
        place(lblMember, 20, 36, 350, 22);
        add(lblMember);

        JLabel lblReturnDate = label("Return date:", SwingConstants.RIGHT);
        place(lblReturnDate, 380, 36, 78, 22);
        add(lblReturnDate);

        txtReturnDate = new DatePickerField(Member.DATE_FORMAT);
        place(txtReturnDate, 462, 36, 148, 22);
        add(txtReturnDate);

        JLabel lblLoans = new JLabel("Borrowed books");
        lblLoans.setFont(Theme.FONT_CARD_TITLE);
        lblLoans.setForeground(Theme.TEXT_PRIMARY);
        place(lblLoans, 20, 64, 200, 22);
        add(lblLoans);

        JLabel lblBasket = new JLabel("Books to return");
        lblBasket.setFont(Theme.FONT_CARD_TITLE);
        lblBasket.setForeground(Theme.TEXT_PRIMARY);
        place(lblBasket, 332, 64, 200, 22);
        add(lblBasket);

        loanTable = Theme.table(loanModel, 150, 40, 110);
        loanTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                syncQuantityLimit();
            }
        });
        loanTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addSelectedLoan();
                }
            }
        });
        JScrollPane loanScroll = Theme.scroll(loanTable);
        place(loanScroll, 20, 90, 300, 160);
        add(loanScroll);

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

        PillButton btnAdd = new PillButton("ADD");
        place(btnAdd, 114, 256, 80, 26);
        btnAdd.addActionListener(e -> addSelectedLoan());
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
     * list for them, loaded with everything they still have out. (No
     * membership check, unlike borrowing: books can always come back.)
     */
    public void startFor(Member member) {
        LocalDate today = Library.today();
        this.member = member;
        basket.clear();
        outstanding = member.getLoans().stream().filter(loan -> !loan.isReturned()).toList();
        lblMember.setText("Member: " + member.getName() + "  (" + member.getMemberId() + ")");

        txtReturnDate.setMinDate(outstanding.stream().map(Loan::getBorrowDate).min(LocalDate::compareTo).orElse(null));
        txtReturnDate.setMaxDate(today); // can't return on a future date
        txtReturnDate.setDate(today);

        loanModel.fireTableDataChanged();
        basketModel.fireTableDataChanged();
        syncQuantityLimit();
    }

    /** Copies on this loan not yet in the list: what's out minus what's already added. */
    private int remaining(Loan loan) {
        int inBasket = 0;
        for (Return line : basket) {
            if (line.loan() == loan) {
                inBasket += line.quantity();
            }
        }
        return loan.getQuantity() - inBasket;
    }

    /** The quantity spinner can never go past the copies still out on the selected loan. */
    private void syncQuantityLimit() {
        int row = loanTable.getSelectedRow();
        int max = row < 0 ? 1 : Math.max(1, remaining(outstanding.get(row)));
        qtyModel.setMaximum(max);
        if (qtyModel.getNumber().intValue() > max) {
            qtyModel.setValue(max);
        }
    }

    private void addSelectedLoan() {
        int row = loanTable.getSelectedRow();
        if (row < 0) {
            warn("Please select a book first.");
            return;
        }
        Loan loan = outstanding.get(row);
        int free = remaining(loan);
        if (free < 1) {
            warn("All copies of \"" + loan.getBook().getTitle() + "\" are already in the list.");
            return;
        }
        int quantity = Math.min(qtyModel.getNumber().intValue(), free);

        // Same loan again just adds to that line.
        boolean merged = false;
        for (int i = 0; i < basket.size() && !merged; i++) {
            Return line = basket.get(i);
            if (line.loan() == loan) {
                basket.set(i, new Return(loan, line.quantity() + quantity));
                merged = true;
            }
        }
        if (!merged) {
            basket.add(new Return(loan, quantity));
        }
        refreshTables();
    }

    private void removeSelectedLine() {
        int row = basketTable.getSelectedRow();
        if (row < 0) {
            warn("Please select a book in the list to remove.");
            return;
        }
        basket.remove(row);
        refreshTables();
    }

    private void refreshTables() {
        basketModel.fireTableDataChanged();
        if (!outstanding.isEmpty()) {
            loanModel.fireTableRowsUpdated(0, outstanding.size() - 1); // keeps the selection
        }
        syncQuantityLimit();
    }

    private void review() {
        if (basket.isEmpty()) {
            warn("Add at least one book first.");
            return;
        }
        mainFrame.showReturnConfirm(member, List.copyOf(basket), txtReturnDate.getDate());
    }

    private void goBack() {
        if (!basket.isEmpty() && JOptionPane.showConfirmDialog(this,
                "Discard the books you've added?", "Leave Returning",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        mainFrame.showReturnMemberSelect();
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(this, message, "Return Book", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
