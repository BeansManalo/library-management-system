package lms.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

@SuppressWarnings("serial")
public class DashboardFrame extends JFrame {

    private JPanel contentPane;
    private JButton btnBookList;
    private JButton btnAddEditBooks;
    private JButton btnMemberList;
    private JButton btnAddEditMembers;

    /**
     * Create the frame.
     */
    public DashboardFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Library Management System");
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setResizable(false);

        contentPane = new JPanel();
        setContentPane(contentPane);

        btnBookList = new JButton("Book List");
        btnBookList.setFont(new Font("Arial", Font.BOLD, 12));
        btnBookList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new BookListFrame().setVisible(true);
                dispose();
            }
        });

        btnAddEditBooks = new JButton("Add / Edit Books");
        btnAddEditBooks.setFont(new Font("Arial", Font.BOLD, 12));
        btnAddEditBooks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new AddEditBookFrame().setVisible(true);
                dispose();
            }
        });

        btnMemberList = new JButton("Member List");
        btnMemberList.setFont(new Font("Arial", Font.BOLD, 12));
        btnMemberList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new MemberListFrame().setVisible(true);
                dispose();
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
                new AddEditMemberFrame().setVisible(true);
                dispose();
            }
        });

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
            gl_contentPane.createParallelGroup(Alignment.TRAILING)
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addGap(151, 151, 151)
                    .addGroup(gl_contentPane.createParallelGroup(Alignment.CENTER)
                        .addComponent(btnBookList, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnAddEditBooks, GroupLayout.PREFERRED_SIZE, 136, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnMemberList, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnAddEditMembers))
                    .addGap(151, 151, 151))
        );
        gl_contentPane.linkSize(SwingConstants.HORIZONTAL,
            btnAddEditBooks, btnAddEditMembers, btnBookList, btnMemberList);
        gl_contentPane.setVerticalGroup(
            gl_contentPane.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_contentPane.createSequentialGroup()
                    .addGap(105, 105, 105)
                    .addComponent(btnBookList)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnAddEditBooks)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnMemberList)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(btnAddEditMembers)
                    .addContainerGap(105, Short.MAX_VALUE))
        );
        contentPane.setLayout(gl_contentPane);

        getAccessibleContext().setAccessibleDescription("");
        pack();
    }
}
