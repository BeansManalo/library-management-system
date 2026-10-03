package lms.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import lms.core.Library.Settlement;
import lms.core.Library;
import lms.core.Loan;
import lms.core.Member;
import lms.core.ValidationException;

/**
 * Deleting a member who still has books out -- reached through
 * MainFrame.showMemberDelete(), from the Delete button or from "Delete
 * instead" on the Renew screen. A member with nothing out never gets here.
 *
 * Every borrowed copy gets its own row, and each one has to be settled as
 * either RETURNED (it goes back on the shelf) or LOST (it's gone for good, so
 * it comes off that book's Currently Owned count). Pick rows -- Shift or
 * Ctrl-click for several -- and press MARK RETURNED / MARK LOST. DELETE MEMBER
 * stays disabled until every copy has an outcome, and asks once more before
 * anything is changed. The actual bookkeeping is Library.deleteMember().
 */
@SuppressWarnings("serial")
public class DeleteMemberPanel extends JPanel {

    private enum Outcome { UNDECIDED, RETURNED, LOST }

    /** One physical copy from one loan, and what's been decided for it so far. */
    private static final class Row {
        final Loan loan;
        final int copy;
        Outcome outcome = Outcome.UNDECIDED;

        Row(Loan loan, int copy) {
            this.loan = loan;
            this.copy = copy;
        }
    }

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();

    private Member member;
    private final List<Row> rows = new ArrayList<>();

    private JLabel lblName;
    private JLabel lblDetails;
    private JLabel lblSummary;
    private JTable table;
    private PillButton btnDelete;

    private final AbstractTableModel model = new AbstractTableModel() {
        private final String[] names = {"Book", "Copy", "Due", "Outcome"};

        @Override
        public int getRowCount() {
            return rows.size();
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
        public Object getValueAt(int row, int column) {
            Row r = rows.get(row);
            Loan loan = r.loan;
            return switch (column) {
                case 0 -> loan.getBook().getTitle();
                case 1 -> loan.getQuantity() > 1 ? r.copy + " of " + loan.getQuantity() : "";
                case 2 -> Member.DATE_FORMAT.format(loan.getDueDate())
                    + (loan.isOverdue(Library.today()) ? "  (overdue)" : "");
                default -> switch (r.outcome) {
                    case UNDECIDED -> "\u2014";
                    case RETURNED -> "Returned";
                    case LOST -> "Lost";
                };
            };
        }
    };

    public DeleteMemberPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setOpaque(false);
        setLayout(layout);

        JLabel heading = Theme.title("DELETE MEMBER");
        place(heading, 20, 7, 600, 24);
        add(heading);

        lblName = new FitLabel().shrink(4);
        lblName.setFont(new Font("Arial", Font.BOLD, 14));
        lblName.setForeground(Theme.TEXT_PRIMARY);
        place(lblName, 20, 38, 600, 22);
        add(lblName);

        lblDetails = new JLabel();
        lblDetails.setFont(Theme.FONT_CARD_SUB_BOLD);
        lblDetails.setForeground(Theme.ALERT_TEXT);
        place(lblDetails, 20, 60, 600, 16);
        add(lblDetails);

        JLabel lblHint = new JLabel("Returned copies go back on the shelf. Lost copies are removed from the book's Currently Owned count for good.");
        lblHint.setFont(Theme.FONT_CARD_SUB);
        lblHint.setForeground(Theme.TEXT_MUTED);
        place(lblHint, 20, 76, 600, 16);
        add(lblHint);

        table = Theme.table(model, 280, 50, 120, 80);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                    boolean focus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, column);
                Outcome outcome = rows.get(row).outcome;
                c.setForeground(outcome == Outcome.LOST ? Theme.ALERT_TEXT
                    : outcome == Outcome.RETURNED ? Theme.BLUE_ACCENT : Theme.TEXT_MUTED);
                c.setFont(Theme.FONT_CARD_SUB_BOLD);
                return c;
            }
        });
        JScrollPane scroll = Theme.scroll(table);
        place(scroll, 20, 98, 600, 140);
        add(scroll);

        PillButton btnSelectAll = new PillButton("SELECT ALL");
        place(btnSelectAll, 20, 244, 120, 26);
        btnSelectAll.addActionListener(e -> table.selectAll());
        add(btnSelectAll);

        PillButton btnReturned = new PillButton("MARK RETURNED");
        place(btnReturned, 150, 244, 160, 26);
        btnReturned.addActionListener(e -> markSelected(Outcome.RETURNED));
        add(btnReturned);

        PillButton btnLost = new PillButton("MARK LOST", Theme.ALERT,
            new Color(0xC0, 0x45, 0x45), new Color(0x80, 0x24, 0x24));
        place(btnLost, 320, 244, 120, 26);
        btnLost.addActionListener(e -> markSelected(Outcome.LOST));
        add(btnLost);

        lblSummary = new JLabel();
        lblSummary.setFont(Theme.FONT_CARD_SUB);
        lblSummary.setForeground(Theme.TEXT_MUTED);
        place(lblSummary, 20, 274, 600, 16);
        add(lblSummary);

        PillButton btnCancel = new PillButton("CANCEL", Theme.TEXT_MUTED,
            Theme.TEXT_MUTED.brighter(), Theme.CANCEL_PRESSED);
        place(btnCancel, 180, 300, 110, 28);
        btnCancel.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_MEMBER_LIST));
        add(btnCancel);

        btnDelete = new PillButton("DELETE MEMBER", Theme.ALERT,
            new Color(0xC0, 0x45, 0x45), new Color(0x80, 0x24, 0x24));
        place(btnDelete, 310, 300, 160, 28);
        btnDelete.addActionListener(e -> confirmAndDelete());
        add(btnDelete);
    }

    /** Called by MainFrame right before showing this card -- see MainFrame.showMemberDelete(). */
    public void startFor(Member member) {
        this.member = member;
        rows.clear();
        for (Loan loan : member.getOutstandingLoans()) {
            for (int copy = 1; copy <= loan.getQuantity(); copy++) {
                rows.add(new Row(loan, copy));
            }
        }

        lblName.setText(member.getName() + "  (" + member.getMemberId() + ")");
        lblDetails.setText("Still has " + rows.size() + " borrowed book(s) out. Mark each one as returned or lost before deleting.");
        model.fireTableDataChanged();
        updateSummary();
    }

    private void markSelected(Outcome outcome) {
        int[] selected = table.getSelectedRows();
        if (selected.length == 0) {
            JOptionPane.showMessageDialog(this, "Select one or more books first.",
                "Delete Member", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (int row : selected) {
            rows.get(row).outcome = outcome;
        }
        model.fireTableRowsUpdated(0, rows.size() - 1); // keeps the selection
        updateSummary();
    }

    private int count(Outcome outcome) {
        int n = 0;
        for (Row row : rows) {
            if (row.outcome == outcome) {
                n++;
            }
        }
        return n;
    }

    /** Refreshes the "x of y decided" line, and lets DELETE MEMBER through only once every copy has an outcome. */
    private void updateSummary() {
        int returned = count(Outcome.RETURNED);
        int lost = count(Outcome.LOST);
        int left = rows.size() - returned - lost;
        lblSummary.setText((rows.size() - left) + " of " + rows.size() + " decided   \u2022   "
            + returned + " returned   \u2022   " + lost + " lost"
            + (left > 0 ? "   \u2022   " + left + " still to decide" : ""));
        btnDelete.setEnabled(left == 0);
    }

    private static String copies(int n) {
        return n + " cop" + (n == 1 ? "y" : "ies");
    }

    private void confirmAndDelete() {
        int returned = count(Outcome.RETURNED);
        int lost = count(Outcome.LOST);
        StringBuilder message = new StringBuilder("Permanently delete " + member.getName()
            + " (" + member.getMemberId() + ")?\n\n");
        if (returned > 0) {
            message.append("\u2022 ").append(copies(returned)).append(" will go back on the shelf.\n");
        }
        if (lost > 0) {
            message.append("\u2022 ").append(copies(lost)).append(" will be removed from the library's owned count for good.\n");
        }
        message.append("\nThis can't be undone.");
        int choice = JOptionPane.showConfirmDialog(this, message.toString(),
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // One settlement per loan: how many of its copies came back and how many were lost.
        Map<Loan, int[]> perLoan = new LinkedHashMap<>();
        for (Row row : rows) {
            int[] counts = perLoan.computeIfAbsent(row.loan, l -> new int[2]);
            counts[row.outcome == Outcome.LOST ? 1 : 0]++;
        }
        List<Settlement> settlements = new ArrayList<>();
        perLoan.forEach((loan, counts) -> settlements.add(new Settlement(loan, counts[0], counts[1])));

        try {
            mainFrame.getLibrary().deleteMember(member, settlements);
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
            return;
        }
        mainFrame.saveLibrary();
        mainFrame.showCard(MainFrame.CARD_MEMBER_LIST);
    }

    private void place(Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
