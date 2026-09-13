package lms.gui;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class AddEditBookFrame extends JFrame {

    private JPanel contentPane;
    private JLabel lblAddAndEditBooks;

    /**
     * Create the frame.
     */
    public AddEditBookFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPane = new JPanel();
        setContentPane(contentPane);

        lblAddAndEditBooks = new JLabel("ADD AND EDIT BOOKS");
        
        JButton Return = new JButton("BACK");
        Return.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		new DashboardFrame().setVisible(true);
                dispose();
        	}
        });
        Return.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        	}
        });

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
        	gl_contentPane.createParallelGroup(Alignment.TRAILING)
        		.addGroup(gl_contentPane.createSequentialGroup()
        			.addContainerGap(143, Short.MAX_VALUE)
        			.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_contentPane.createSequentialGroup()
        					.addComponent(lblAddAndEditBooks)
        					.addGap(138))
        				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
        					.addComponent(Return)
        					.addGap(163))))
        );
        gl_contentPane.setVerticalGroup(
        	gl_contentPane.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_contentPane.createSequentialGroup()
        			.addGap(115)
        			.addComponent(lblAddAndEditBooks)
        			.addPreferredGap(ComponentPlacement.RELATED, 89, Short.MAX_VALUE)
        			.addComponent(Return)
        			.addGap(57))
        );
        contentPane.setLayout(gl_contentPane);
        pack();
    }
}
