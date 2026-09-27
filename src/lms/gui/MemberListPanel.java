package lms.gui;

import java.awt.Color;
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
import lms.core.Member;

@SuppressWarnings("serial")
public class MemberListPanel extends JPanel {

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();
    private JLabel banner;
    private JTextField txtSearch;
    private CardListPanel cardListPanel;
    private PillButton btnAddNew;
    private PillButton btnDelete;

    // True when this screen is playing the "Add / Edit Members" role
    // (Add New + Delete visible, double-click opens the editable form)
    // instead of the plain read-only "Member List" role (double-click
    // opens the read-only Member Details viewer instead, nothing here
    // can change a member). Both roles share this one screen/card --
    // see MainFrame.showMemberList() / showMemberManage() -- the same
    // way AddEditMemberPanel already reuses one screen for both Add and
    // Edit.
    private boolean manageMode;

    // Same idea as BookListPanel: cardPanels mirrors displayedMembers 1:1
    // so a click can flip one card's selected look without rebuilding
    // the whole list.
    private List<Member> displayedMembers = new ArrayList<>();
    private List<MemberCardPanel> cardPanels = new ArrayList<>();
    private int selectedIndex = -1;

    /**
     * Create the panel.
     */
    public MemberListPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        banner = Theme.banner("LIST OF MEMBERS");
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

        // Add New / Delete are the management role's buttons only --
        // see setManageMode() -- hidden by default so this screen opens
        // as the plain read-only list.
        btnAddNew = new PillButton("ADD NEW");
        place(btnAddNew, 460, 72, 150, 30);
        btnAddNew.addActionListener(e -> mainFrame.showMemberForm(null));
        add(btnAddNew);

        btnDelete = new PillButton("DELETE", new Color(0xA5, 0x33, 0x33),
            new Color(0xC0, 0x45, 0x45), new Color(0x80, 0x24, 0x24));
        place(btnDelete, 460, 110, 150, 30);
        btnDelete.addActionListener(e -> deleteSelectedMember());
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
     * Switches this screen between the plain read-only "Member List"
     * role and the "Add / Edit Members" management role: only the
     * management role shows Add New / Delete, and only it opens the
     * editable form on double-click (the plain list opens the read-only
     * viewer instead). Called by MainFrame right before showing this
     * card -- see MainFrame.showMemberList() / showMemberManage().
     */
    public void setManageMode(boolean manageMode) {
        this.manageMode = manageMode;
        banner.setText(manageMode ? "ADD / EDIT MEMBERS" : "LIST OF MEMBERS");
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

        displayedMembers = new ArrayList<>();
        for (Member member : mainFrame.getLibrary().getMembers()) {
            if (query.isEmpty() || matches(member, query)) {
                displayedMembers.add(member);
            }
        }

        selectedIndex = -1;
        cardPanels = new ArrayList<>();
        cardListPanel.removeAll();
        for (int i = 0; i < displayedMembers.size(); i++) {
            Member member = displayedMembers.get(i);
            int rowIndex = i;
            MemberCardPanel card = new MemberCardPanel(member,
                () -> selectRow(rowIndex),
                () -> openMember(member));
            cardPanels.add(card);
            cardListPanel.add(card);
        }
        cardListPanel.revalidate();
        cardListPanel.repaint();
    }

    /** Double-click target: the editable form in manage mode, the read-only viewer otherwise. */
    private void openMember(Member member) {
        if (manageMode) {
            mainFrame.showMemberForm(member);
        } else {
            mainFrame.showMemberDetail(member);
        }
    }

    private void selectRow(int index) {
        selectedIndex = index;
        for (int i = 0; i < cardPanels.size(); i++) {
            cardPanels.get(i).setSelected(i == index);
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
            refresh();
        }
    }

    /**
     * Returns the Member behind the selected row, or null (with a message
     * dialog instead of an exception) if nothing is selected.
     */
    private Member getSelectedMember() {
        if (selectedIndex < 0 || selectedIndex >= displayedMembers.size()) {
            JOptionPane.showMessageDialog(this,
                "Please select a member first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return displayedMembers.get(selectedIndex);
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
