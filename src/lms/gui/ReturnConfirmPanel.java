package lms.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import lms.core.Library.Return;
import lms.core.Member;
import lms.core.ValidationException;

/**
 * Step 3 of returning, mirroring BorrowConfirmPanel: a summary of who is
 * returning what, and which copies are coming back late. Nothing is
 * recorded until ACCEPT, and both ACCEPT and
 * DECLINE ask once more before doing anything. BACK returns to the
 * return screen with everything still in it.
 */
@SuppressWarnings("serial")
public class ReturnConfirmPanel extends JPanel {

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();

    private Member member;
    private List<Return> returns = new ArrayList<>();
    private LocalDate returnDate;

    private JLabel lblName;
    private JLabel lblDetails;
    private JLabel lblDate;
    private JLabel lblTotals;

    private final AbstractTableModel model = new AbstractTableModel() {
        private final String[] names = {"Title", "Qty", "Due", "Status"};

        @Override
        public int getRowCount() {
            return returns.size();
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
            Return line = returns.get(row);
            return switch (column) {
                case 0 -> line.loan().getBook().getTitle();
                case 1 -> line.quantity();
                case 2 -> Member.DATE_FORMAT.format(line.loan().getDueDate());
                default -> isLate(line) ? "Late" : "On time";
            };
        }
    };

    public ReturnConfirmPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        JLabel banner = Theme.banner("CONFIRM RETURN");
        place(banner, 0, 0, 640, 30);
        add(banner);

        lblName = new FitLabel().shrink(4);
        lblName.setFont(new Font("Arial", Font.BOLD, 14));
        lblName.setForeground(Theme.TEXT_PRIMARY);
        place(lblName, 20, 38, 600, 22);
        add(lblName);

        lblDetails = new JLabel();
        lblDetails.setFont(Theme.FONT_CARD_SUB);
        lblDetails.setForeground(Theme.TEXT_MUTED);
        place(lblDetails, 20, 60, 600, 16);
        add(lblDetails);

        lblDate = new JLabel();
        lblDate.setFont(Theme.FONT_CARD_SUB_BOLD);
        lblDate.setForeground(Theme.TEXT_PRIMARY);
        place(lblDate, 20, 78, 600, 16);
        add(lblDate);

        JScrollPane scroll = Theme.scroll(Theme.table(model, 300, 50, 100, 120));
        place(scroll, 20, 102, 600, 156);
        add(scroll);

        lblTotals = new JLabel();
        lblTotals.setFont(Theme.FONT_CARD_SUB);
        lblTotals.setForeground(Theme.TEXT_MUTED);
        place(lblTotals, 20, 264, 600, 16);
        add(lblTotals);

        PillButton btnBack = new PillButton("BACK");
        place(btnBack, 140, 300, 110, 28);
        btnBack.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_RETURN_BOOK));
        add(btnBack);

        PillButton btnDecline = new PillButton("DECLINE", new Color(0xA5, 0x33, 0x33),
            new Color(0xC0, 0x45, 0x45), new Color(0x80, 0x24, 0x24));
        place(btnDecline, 265, 300, 110, 28);
        btnDecline.addActionListener(e -> decline());
        add(btnDecline);

        PillButton btnAccept = new PillButton("ACCEPT");
        place(btnAccept, 390, 300, 110, 28);
        btnAccept.addActionListener(e -> accept());
        add(btnAccept);
    }

    /** Called by MainFrame right before showing this card -- see MainFrame.showReturnConfirm(). */
    public void load(Member member, List<Return> returns, LocalDate returnDate) {
        this.member = member;
        this.returns = returns;
        this.returnDate = returnDate;

        int books = 0;
        int late = 0;
        for (Return line : returns) {
            books += line.quantity();
            if (isLate(line)) {
                late++;
            }
        }
        long titles = returns.stream().map(line -> line.loan().getBook()).distinct().count();

        lblName.setText(member.getName());
        lblDetails.setText("Member ID: " + member.getMemberId() + "   \u2022   "
            + (member.getContactNumber() == null || member.getContactNumber().isEmpty() ? "\u2014" : member.getContactNumber()));
        lblDate.setText("Return date: " + Member.DATE_FORMAT.format(returnDate));
        lblTotals.setText(books + " book(s)  \u2022  " + titles + " title(s)"
            + (late > 0 ? "  \u2022  " + late + " late" : ""));
        model.fireTableDataChanged();
    }

    private boolean isLate(Return line) {
        return returnDate.isAfter(line.loan().getDueDate());
    }

    private void accept() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Record these books as returned by " + member.getName() + "?",
            "Confirm Return", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            mainFrame.getLibrary().returnBooks(member, returns, returnDate);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Return", JOptionPane.ERROR_MESSAGE);
            return;
        }
        mainFrame.saveLibrary();
        JOptionPane.showMessageDialog(this, "Return recorded.", "Return Book", JOptionPane.INFORMATION_MESSAGE);
        mainFrame.showCard(MainFrame.CARD_DASHBOARD);
    }

    private void decline() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Decline this return? Nothing will be recorded.",
            "Confirm Decline", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            mainFrame.showCard(MainFrame.CARD_DASHBOARD);
        }
    }

    private void place(Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
