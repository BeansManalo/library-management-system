package lms.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class DashboardPanel extends JPanel {

    private static final Color ACCENT_HOVER = Theme.BLUE_ACCENT.brighter();
    private static final Color ACCENT_PRESSED = Theme.BLUE_ACCENT.darker();

    private final ProportionalLayout layout = new ProportionalLayout();
    private MainFrame mainFrame;
    private JPanel menuOverlay;
    private final PillButton[] tiles = new PillButton[6];

    /**
     * Create the panel.
     */
    public DashboardPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setOpaque(false);
        setLayout(layout);

        // Hamburger button in the top-left corner.
        PillButton btnMenu = new PillButton("");
        btnMenu.setIcon(RowIcons.menu(16, Color.WHITE));
        btnMenu.addActionListener(e -> setMenuOpen(!menuOverlay.isVisible()));
        place(btnMenu, 14, 12, 34, 28);
        add(btnMenu);

        // The menu: a translucent layer over the whole screen that dims it, swallows
        // clicks so the buttons underneath can't be pressed, and closes the menu when
        // clicked anywhere that isn't an option. The hamburger sits above it, so it stays live.
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
        place(menuOverlay, 0, 0, 640, 360);
        add(menuOverlay);
        buildMenu();

        JLabel title = new JLabel("LeMon.S v0.1", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.LEMON);
                g2.fillRoundRect((getWidth() - 56) / 2, getHeight() - 4, 56, 4, 4, 4);
                g2.dispose();
            }
        };
        title.setFont(Theme.display(34));
        title.setForeground(Theme.HEADING);
        place(title, 0, 50, 640, 56);
        add(title);

        // Two columns, three rows: books, members, then the two things done at the desk.
        tiles[0] = tile("Book List", RowIcons.book(20, Color.WHITE), false, e -> mainFrame.showBookList());
        tiles[1] = tile("Add / Edit Books", RowIcons.book(20, Color.WHITE), false, e -> mainFrame.showBookManage());
        tiles[2] = tile("Member List", RowIcons.person(20, Color.WHITE), false, e -> mainFrame.showMemberList());
        tiles[3] = tile("Add / Edit Members", RowIcons.person(20, Color.WHITE), false, e -> mainFrame.showMemberManage());
        // Borrowing starts by choosing a member, then the books -- see MainFrame.showBorrowMemberSelect().
        tiles[4] = tile("Borrow Book", RowIcons.book(20, Color.WHITE), true, e -> mainFrame.showBorrowMemberSelect());
        // Same steps, but the member list only shows members with books out -- see
        // MainFrame.showReturnMemberSelect(). Kept as its own button rather than one
        // combined "Borrow / Return" screen, since the two have different forms.
        tiles[5] = tile("Return Book", RowIcons.book(20, Color.WHITE), true, e -> mainFrame.showReturnMemberSelect());
        for (int i = 0; i < tiles.length; i++) {
            place(tiles[i], 63 + (i % 2) * 264, 130 + (i / 2) * 62, 250, 48);
            add(tiles[i]);
        }
    }

    private PillButton tile(String text, Icon icon, boolean accent, java.awt.event.ActionListener action) {
        PillButton b = accent ? new PillButton(text, Theme.BLUE_ACCENT, ACCENT_HOVER, ACCENT_PRESSED) : new PillButton(text);
        b.setFont(Theme.display(12));
        b.setIcon(icon, 12);
        b.addActionListener(action);
        return b;
    }

    /** The options sit on a navy panel that hangs from the hamburger; lighter blue buttons stand out on it. */
    private void buildMenu() {
        JPanel platform = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.NAVY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        platform.setOpaque(false);
        platform.addMouseListener(new MouseAdapter() { }); // clicks on it don't reach the overlay

        PillButton btnSettings = menuButton("Settings", Theme.BLUE_ACCENT);
        PillButton btnExit = menuButton("Exit", Theme.BLUE_ACCENT);
        PillButton btnSaveLibrary = menuButton("Save Library", Theme.BLUE_ACCENT);
        PillButton btnLoadLibrary = menuButton("Load Library", Theme.BLUE_ACCENT);
        PillButton btnDeleteLibrary = menuButton("Delete Library", Theme.ALERT);
        btnSettings.addActionListener(e -> {
            setMenuOpen(false);
            mainFrame.openSettings();
        });
        btnExit.addActionListener(e -> {
            setMenuOpen(false);
            mainFrame.confirmExit();
        });
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
                    .addComponent(btnSettings, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSaveLibrary, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLoadLibrary, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteLibrary, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExit, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
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
                .addComponent(btnSettings, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(btnExit, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
                .addGap(8)
        );
        platform.setLayout(gl_menu);

        ProportionalLayout overlayLayout = new ProportionalLayout();
        menuOverlay.setLayout(overlayLayout);
        overlayLayout.put(platform, 14, 46, 166, 296);
        menuOverlay.add(platform);
    }

    private PillButton menuButton(String text, Color fill) {
        return new PillButton(text, fill, fill.brighter(), fill.darker());
    }

    private void place(Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    /**
     * Opens or closes the hamburger menu. The overlay already blocks the
     * mouse; taking the dashboard buttons out of the focus order as well
     * keeps them from being pressed with Tab + Space/Enter.
     */
    private void setMenuOpen(boolean open) {
        menuOverlay.setVisible(open);
        for (Component c : tiles) {
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
