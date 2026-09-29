package lms.gui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
    private JList<Member> memberList;
    private PillButton btnView;
    private PillButton btnAddNew;
    private PillButton btnDelete;
    private PillButton btnRenew;

    /**
     * The three roles this one screen plays: LIST is the plain read-only
     * browse/search role (double-click opens the Member Details viewer),
     * MANAGE is "Add / Edit Members" (Add New + Delete visible, opens the
     * editable form, and Renew appears for a member whose membership has
     * expired), PICK is the first step of borrowing a book -- the
     * very same list, but choosing a row hands that member to the borrow
     * screen -- and RETURN is the same for returning, where only members
     * who have books out are listed. See MainFrame.showMemberList() /
     * showMemberManage() / showBorrowMemberSelect() / showReturnMemberSelect().
     */
    public enum Mode {
        LIST("LIST OF MEMBERS", "VIEW") {
            @Override
            void open(MainFrame frame, Member member) {
                frame.showMemberDetail(member);
            }
        },
        MANAGE("ADD / EDIT MEMBERS", "EDIT") {
            @Override
            void open(MainFrame frame, Member member) {
                frame.showMemberForm(member);
            }

            @Override
            boolean managing() {
                return true;
            }
        },
        PICK("SELECT MEMBER", "SELECT") {
            @Override
            void open(MainFrame frame, Member member) {
                frame.showBorrowBook(member);
            }
        },
        RETURN("SELECT MEMBER", "SELECT") {
            @Override
            void open(MainFrame frame, Member member) {
                frame.showReturnBook(member);
            }

            @Override
            boolean accepts(Member member) {
                return member.getBooksBorrowed() > 0;
            }
        };

        private final String title;
        private final String actionLabel;

        Mode(String title, String actionLabel) {
            this.title = title;
            this.actionLabel = actionLabel;
        }

        /** What double-clicking a row (or the VIEW/EDIT/SELECT button) does with that member. */
        abstract void open(MainFrame frame, Member member);

        /** True for the role that offers Add New, Delete and Renew. */
        boolean managing() {
            return false;
        }

        /** True if this role lists the given member at all. */
        boolean accepts(Member member) {
            return true;
        }
    }

    private Mode mode = Mode.LIST;

    // Same idea as BookListPanel: displayedMembers is the filtered list
    // memberList's model reads from -- memberList.getSelectedValue() /
    // getSelectedIndex() is enough to know which one is selected.
    private List<Member> displayedMembers = new ArrayList<>();

    /**
     * Create the panel.
     */
    public MemberListPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        // Catches clicks on the blank margins around the table and
        // buttons -- everywhere that isn't a specific control. The
        // list's own blank space (below the last row) is handled
        // separately below, on the scroll pane's viewport, since it
        // sits in front of this panel there.
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                memberList.clearSelection();
            }
        });

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

        // A JList instead of a scrollable panel of always-live row
        // components: it only ever renders the rows actually on screen,
        // reusing the one MemberCardPanel instance below as its cell
        // renderer, so filtering/scrolling stays fast regardless of how
        // many members there are (see BookCardPanel's class comment,
        // which this mirrors).
        memberList = new JList<>();
        memberList.setCellRenderer(new MemberCardPanel());
        // No setFixedCellHeight(): each row is sized from the renderer's
        // own preferred height instead of a hardcoded guess, so it stays
        // correct even if a row's content (and so its natural height)
        // changes later.
        memberList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberList.setBackground(Theme.CARD_BG);
        // JList doesn't select anything on a click that misses every row
        // (e.g. in the leftover space below the last one), so a plain
        // click listener -- rather than a selection listener -- is what's
        // needed to also clear the selection on that kind of click.
        memberList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = memberList.locationToIndex(e.getPoint());
                if (index >= 0 && memberList.getCellBounds(index, index).contains(e.getPoint())) {
                    memberList.setSelectedIndex(index);
                    if (e.getClickCount() == 2) {
                        openMember(displayedMembers.get(index));
                    }
                } else {
                    memberList.clearSelection();
                }
            }
        });

        // RENEW only shows for an expired member, so it follows the selection.
        memberList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateRenewButton();
            }
        });

        JScrollPane scrollPane = new JScrollPane(memberList);
        scrollPane.setBorder(new LineBorder(Theme.DIVIDER, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        scrollPane.getViewport().setBackground(Theme.CARD_BG);
        place(scrollPane, 20, 72, 430, 212);
        add(scrollPane);
        // Clicking the scroll pane's own background -- the gap below the
        // last row when the list doesn't fill the visible height --
        // deselects the current row, the same way clicking empty space
        // around a file list normally does.
        scrollPane.getViewport().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                memberList.clearSelection();
            }
        });

        // Visible in both roles, at the top of the button column so
        // there's no dead space above it when Add New / Delete are
        // hidden -- see setMode() for the VIEW/EDIT label swap.
        btnView = new PillButton("VIEW");
        place(btnView, 460, 72, 150, 30);
        btnView.addActionListener(e -> {
            Member selected = getSelectedMember();
            if (selected != null) {
                openMember(selected);
            }
        });
        add(btnView);

        // Add New / Delete are the management role's buttons only --
        // see setMode() -- hidden by default so this screen opens
        // as the plain read-only list.
        btnAddNew = new PillButton("ADD NEW");
        place(btnAddNew, 460, 110, 150, 30);
        btnAddNew.addActionListener(e -> mainFrame.showMemberForm(null));
        add(btnAddNew);

        btnDelete = new PillButton("DELETE", new Color(0xA5, 0x33, 0x33),
            new Color(0xC0, 0x45, 0x45), new Color(0x80, 0x24, 0x24));
        place(btnDelete, 460, 148, 150, 30);
        btnDelete.addActionListener(e -> deleteSelectedMember());
        add(btnDelete);

        // Only ever visible for a member whose membership has ended -- see updateRenewButton().
        btnRenew = new PillButton("RENEW");
        place(btnRenew, 460, 186, 150, 30);
        btnRenew.addActionListener(e -> {
            Member selected = getSelectedMember();
            if (selected != null) {
                mainFrame.showMemberRenew(selected);
            }
        });
        add(btnRenew);

        PillButton btnBack = new PillButton("BACK");
        place(btnBack, 270, 300, 100, 28);
        btnBack.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_DASHBOARD));
        add(btnBack);

        setMode(Mode.LIST);
    }

    private void place(java.awt.Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    /** Switches which of the three roles (see {@link Mode}) this screen plays. */
    public void setMode(Mode mode) {
        this.mode = mode;
        banner.setText(mode.title);
        btnView.setText(mode.actionLabel);
        btnAddNew.setVisible(mode.managing());
        btnDelete.setVisible(mode.managing());
        updateRenewButton();
    }

    /** RENEW is offered only in the management role, and only for a selected member whose membership has expired. */
    private void updateRenewButton() {
        Member selected = memberList.getSelectedValue();
        btnRenew.setVisible(mode.managing() && selected != null && selected.isExpired());
    }

    /**
     * Reloads the list from the shared Library, applying the current
     * search text as a filter. Called on every keystroke in the search
     * box, and by MainFrame every time this screen is shown (see
     * MainFrame.showCard) so the list never shows stale data.
     */
    public void refresh() {
        String query = txtSearch.getText().trim().toLowerCase();

        displayedMembers = new ArrayList<>();
        for (Member member : mainFrame.getLibrary().getMembers()) {
            if (mode.accepts(member) && (query.isEmpty() || matches(member, query))) {
                displayedMembers.add(member);
            }
        }

        // Just hands the JList plain Member references, not components --
        // the actual row visuals only get built when a row scrolls into
        // view (see the cell renderer set up above).
        memberList.setListData(displayedMembers.toArray(new Member[0]));
        memberList.clearSelection();
        updateRenewButton();
    }

    /** Double-click target: depends on the current role (see {@link Mode}). */
    private void openMember(Member member) {
        mode.open(mainFrame, member);
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

    /**
     * Deletes the selected member. A member with nothing out is just
     * confirmed and removed; one who still has books out goes through the
     * returned-or-lost process first -- see MainFrame.showMemberDelete().
     */
    private void deleteSelectedMember() {
        Member selected = getSelectedMember();
        if (selected != null) {
            mainFrame.showMemberDelete(selected);
        }
    }

    /**
     * Returns the Member behind the selected row, or null (with a message
     * dialog instead of an exception) if nothing is selected.
     */
    private Member getSelectedMember() {
        Member selected = memberList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a member first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return selected;
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
