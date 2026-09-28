package lms.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.OverlayLayout;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class DashboardPanel extends JPanel {

    private MainFrame mainFrame;
    private JPanel buttonArea;
    private JPanel menuOverlay;
    private PillButton btnBookList;
    private PillButton btnAddEditBooks;
    private PillButton btnMemberList;
    private PillButton btnAddEditMembers;
    private PillButton btnBorrowBook;
    private PillButton btnReturnBook;

    /**
     * Create the panel.
     */
    public DashboardPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(new BorderLayout());

        JLabel banner = Theme.banner("LIBRARY MANAGEMENT SYSTEM");
        banner.setPreferredSize(new java.awt.Dimension(640, 34));
        add(banner, BorderLayout.NORTH);

        // Hamburger button on the banner's left edge. The border insets it
        // from the edge; it's symmetric so the title stays centered.
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(4, 6, 4, 6));
        PillButton btnMenu = new PillButton("", Theme.NAVY, Theme.BLUE_ACCENT, Theme.NAVY_DARK);
        btnMenu.setIcon(RowIcons.menu(16, Color.WHITE));
        btnMenu.setPreferredSize(new Dimension(30, 26));
        btnMenu.addActionListener(e -> setMenuOpen(!menuOverlay.isVisible()));
        banner.add(btnMenu, BorderLayout.WEST);

        buttonArea = new JPanel();
        buttonArea.setBackground(Theme.APP_BG);

        btnBookList = new PillButton("Book List");
        btnBookList.setIcon(RowIcons.book(18, java.awt.Color.WHITE), 10);
        btnBookList.addActionListener(e -> mainFrame.showBookList());

        btnAddEditBooks = new PillButton("Add / Edit Books");
        btnAddEditBooks.setIcon(RowIcons.book(18, java.awt.Color.WHITE), 10);
        btnAddEditBooks.addActionListener(e -> mainFrame.showBookManage());
        // Opens the same list screen as "Book List", but in its
        // management role: Add New / Delete are visible, and
        // double-clicking a row opens it for editing instead of just
        // viewing it -- see BookListPanel.setManageMode().

        btnMemberList = new PillButton("Member List");
        btnMemberList.setIcon(RowIcons.person(18, java.awt.Color.WHITE), 10);
        btnMemberList.addActionListener(e -> mainFrame.showMemberList());

        btnAddEditMembers = new PillButton("Add / Edit Members");
        btnAddEditMembers.setIcon(RowIcons.person(18, java.awt.Color.WHITE), 10);
        btnAddEditMembers.addActionListener(e -> mainFrame.showMemberManage());
        // Same reasoning as btnAddEditBooks above.

        btnBorrowBook = new PillButton("Borrow Book");
        btnBorrowBook.setIcon(RowIcons.book(18, java.awt.Color.WHITE), 10);
        btnBorrowBook.addActionListener(e -> mainFrame.showBorrowMemberSelect());
        // Borrowing starts by choosing a member, then the books -- see
        // MainFrame.showBorrowMemberSelect().

        btnReturnBook = new PillButton("Return Book");
        btnReturnBook.setIcon(RowIcons.book(18, java.awt.Color.WHITE), 10);
        btnReturnBook.addActionListener(e -> mainFrame.showReturnMemberSelect());
        // Same steps as btnBorrowBook above, but the member list only shows
        // members with books out -- see MainFrame.showReturnMemberSelect().
        // Kept as a separate button/flow rather than one combined
        // "Borrow / Return" screen, since the two have different forms.

        GroupLayout gl_this = new GroupLayout(buttonArea);
        gl_this.setHorizontalGroup(
            gl_this.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(gl_this.createParallelGroup(Alignment.CENTER)
                    .addComponent(btnBookList, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddEditBooks, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMemberList, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddEditMembers, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBorrowBook, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReturnBook, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE)
        );
        gl_this.setVerticalGroup(
            gl_this.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnBookList, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(btnAddEditBooks, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(btnMemberList, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(btnAddEditMembers, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(btnBorrowBook, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(btnReturnBook, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE)
        );
        buttonArea.setLayout(gl_this);

        // The hamburger menu: a translucent layer over the button area that
        // dims it, swallows clicks so the buttons underneath can't be
        // pressed, and closes the menu when clicked anywhere that isn't an
        // option. The banner sits outside it, so the hamburger stays live.
        menuOverlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 90));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        menuOverlay.setOpaque(false);
        menuOverlay.setVisible(false);
        menuOverlay.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setMenuOpen(false);
            }
        });

        // The options sit on a banner-colored side platform that hangs from the
        // banner down to the bottom of the window, so they don't float. Navy
        // buttons would vanish into it, hence the lighter blue fill.
        JPanel platform = new JPanel();
        platform.setBackground(Theme.NAVY);
        platform.addMouseListener(new MouseAdapter() { }); // clicks on it don't reach the overlay

        PillButton btnSaveLibrary = new PillButton("Save Library",
            Theme.BLUE_ACCENT, Theme.BLUE_ACCENT.brighter(), Theme.BLUE_ACCENT.darker());
        PillButton btnLoadLibrary = new PillButton("Load Library",
            Theme.BLUE_ACCENT, Theme.BLUE_ACCENT.brighter(), Theme.BLUE_ACCENT.darker());
        PillButton btnDeleteLibrary = new PillButton("Delete Library",
            Theme.ALERT, Theme.ALERT.brighter(), Theme.ALERT.darker());
        btnSaveLibrary.addActionListener(e -> {
            setMenuOpen(false);
            mainFrame.exportLibrary();
        });
        btnLoadLibrary.addActionListener(e -> {
            setMenuOpen(false);
            mainFrame.importLibrary();
        });
        btnDeleteLibrary.addActionListener(e -> {
            setMenuOpen(false);
            mainFrame.deleteLibrary();
        });

        GroupLayout gl_menu = new GroupLayout(platform);
        gl_menu.setHorizontalGroup(
            gl_menu.createSequentialGroup()
                .addGap(8)
                .addGroup(gl_menu.createParallelGroup(Alignment.LEADING)
                    .addComponent(btnSaveLibrary, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLoadLibrary, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteLibrary, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
                .addGap(8)
        );
        gl_menu.setVerticalGroup(
            gl_menu.createSequentialGroup()
                .addGap(8)
                .addComponent(btnSaveLibrary, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(btnLoadLibrary, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(btnDeleteLibrary, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, Short.MAX_VALUE)
        );
        platform.setLayout(gl_menu);
        menuOverlay.setLayout(new BorderLayout());
        menuOverlay.add(platform, BorderLayout.WEST); // full height, just wide enough for the buttons

        // OverlayLayout stacks its children on the same spot; the first one
        // added is drawn on top.
        JPanel body = new JPanel();
        body.setLayout(new OverlayLayout(body));
        body.add(menuOverlay);
        body.add(buttonArea);
        add(body, BorderLayout.CENTER);
    }

    /**
     * Opens or closes the hamburger menu. The overlay already blocks the
     * mouse; taking the dashboard buttons out of the focus order as well
     * keeps them from being pressed with Tab + Space/Enter.
     */
    private void setMenuOpen(boolean open) {
        menuOverlay.setVisible(open);
        for (Component c : buttonArea.getComponents()) {
            c.setFocusable(!open);
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
