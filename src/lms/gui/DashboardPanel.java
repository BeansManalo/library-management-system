package lms.gui;

import java.awt.BorderLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

@SuppressWarnings("serial")
public class DashboardPanel extends JPanel {

    private MainFrame mainFrame;
    private PillButton btnBookList;
    private PillButton btnAddEditBooks;
    private PillButton btnMemberList;
    private PillButton btnAddEditMembers;

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

        JPanel buttonArea = new JPanel();
        buttonArea.setBackground(Theme.APP_BG);
        add(buttonArea, BorderLayout.CENTER);

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

        GroupLayout gl_this = new GroupLayout(buttonArea);
        gl_this.setHorizontalGroup(
            gl_this.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(gl_this.createParallelGroup(Alignment.CENTER)
                    .addComponent(btnBookList, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddEditBooks, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMemberList, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddEditMembers, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE))
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
                .addGap(0, 0, Short.MAX_VALUE)
        );
        buttonArea.setLayout(gl_this);
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
