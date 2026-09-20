package lms.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class DashboardPanel extends JPanel {

    private MainFrame mainFrame;
    private JButton btnBookList;
    private JButton btnAddEditBooks;
    private JButton btnMemberList;
    private JButton btnAddEditMembers;

    /**
     * Create the panel.
     */
    public DashboardPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);

        btnBookList = new JButton("Book List");
        btnBookList.setFont(new Font("Arial", Font.BOLD, 12));
        btnBookList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainFrame.showCard(MainFrame.CARD_BOOK_LIST);
            }
        });

        btnAddEditBooks = new JButton("Add / Edit Books");
        btnAddEditBooks.setFont(new Font("Arial", Font.BOLD, 12));
        btnAddEditBooks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Go through showBookForm(null) rather than showCard()
                // directly, so this shortcut always opens a blank "Add"
                // form instead of possibly reusing whatever book was
                // last being edited (the Add/Edit panel is reused, not
                // recreated, between visits).
                mainFrame.showBookForm(null);
            }
        });

        btnMemberList = new JButton("Member List");
        btnMemberList.setFont(new Font("Arial", Font.BOLD, 12));
        btnMemberList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainFrame.showCard(MainFrame.CARD_MEMBER_LIST);
            }
        });
        btnMemberList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO add your handling code here:
            }
        });

        btnAddEditMembers = new JButton("Add / Edit Members");
        btnAddEditMembers.setFont(new Font("Arial", Font.BOLD, 12));
        btnAddEditMembers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Same reasoning as btnAddEditBooks above: always start blank.
                mainFrame.showMemberForm(null);
            }
        });

        GroupLayout gl_this = new GroupLayout(this);
        gl_this.setHorizontalGroup(
            gl_this.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(gl_this.createParallelGroup(Alignment.CENTER)
                    .addComponent(btnBookList, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddEditBooks, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMemberList, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAddEditMembers))
                .addGap(0, 0, Short.MAX_VALUE)
        );
        gl_this.linkSize(SwingConstants.HORIZONTAL,
            btnAddEditBooks, btnAddEditMembers, btnBookList, btnMemberList);
        gl_this.setVerticalGroup(
            gl_this.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnBookList)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(btnAddEditBooks)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(btnMemberList)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(btnAddEditMembers)
                .addGap(0, 0, Short.MAX_VALUE)
        );
        setLayout(gl_this);
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
