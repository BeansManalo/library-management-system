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
import lms.core.Member;

@SuppressWarnings("serial")
public class MemberListPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblListOfMembers;
    private JTable table;
    private JTextField txtSearch;
    private DefaultTableModel tableModel;

    // Same idea as BookListPanel.displayedMembers: row index in the table
    // maps 1:1 to index in this list (the currently filtered rows), so a
    // click on row N always refers to displayedMembers.get(N).
    private List<Member> displayedMembers = new ArrayList<>();

    /**
     * Create the panel.
     */
    public MemberListPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setLayout(null);

        lblListOfMembers = new JLabel("LIST OF MEMBERS");
        lblListOfMembers.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblListOfMembers.setHorizontalAlignment(SwingConstants.CENTER);
        lblListOfMembers.setBounds(0, 8, 640, 20);
        add(lblListOfMembers);

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

        // Column headers match Member's real fields (no more "New column"
        // placeholders). isCellEditable is overridden so the table is
        // display-only -- edits always go through the Add/Edit screen.
        tableModel = new DefaultTableModel(
            new String[] { "Member ID", "Name", "Contact Number", "Email", "Address" }, 0) {
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
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(50);

        // Double-click a row as a shortcut for Edit.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedMember();
                }
            }
        });

        JButton btnAddNew = new JButton("ADD NEW");
        btnAddNew.setBounds(460, 66, 150, 28);
        btnAddNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showMemberForm(null);
            }
        });
        add(btnAddNew);

        JButton btnEdit = new JButton("EDIT");
        btnEdit.setBounds(460, 100, 150, 28);
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedMember();
            }
        });
        add(btnEdit);

        JButton btnDelete = new JButton("DELETE");
        btnDelete.setBounds(460, 134, 150, 28);
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedMember();
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

        displayedMembers = new ArrayList<>();
        for (Member member : mainFrame.getLibrary().getMembers()) {
            if (query.isEmpty() || matches(member, query)) {
                displayedMembers.add(member);
            }
        }

        tableModel.setRowCount(0);
        for (Member member : displayedMembers) {
            tableModel.addRow(new Object[] {
                member.getMemberId(),
                member.getName(),
                member.getContactNumber(),
                member.getEmail(),
                member.getAddress()
            });
        }
    }

    /** True if any of the member's fields contain the search text. */
    private boolean matches(Member member, String query) {
        return contains(member.getMemberId(), query)
            || contains(member.getName(), query)
            || contains(member.getContactNumber(), query)
            || contains(member.getEmail(), query)
            || contains(member.getAddress(), query);
    }

    private boolean contains(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    /** Opens the Add/Edit screen pre-filled with the selected row, if any. */
    private void editSelectedMember() {
        Member selected = getSelectedMember();
        if (selected != null) {
            mainFrame.showMemberForm(selected);
        }
    }

    /** Removes the selected row from the library, after confirming with the user. */
    private void deleteSelectedMember() {
        Member selected = getSelectedMember();
        if (selected == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
            "Delete \"" + selected.getName() + "\"?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            mainFrame.getLibrary().getMembers().remove(selected);
            refreshTable();
        }
    }

    /**
     * Returns the Member behind the selected row, or null (with a message
     * dialog instead of an exception) if nothing is selected.
     */
    private Member getSelectedMember() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a member first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return displayedMembers.get(row);
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
