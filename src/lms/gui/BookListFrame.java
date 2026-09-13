package lms.gui;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class BookListFrame extends JFrame {

    private JPanel contentPane;
    private JLabel lblListOfBooks;

    /**
     * Create the frame.
     */
    public BookListFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPane = new JPanel();
        setContentPane(contentPane);

        lblListOfBooks = new JLabel("LIST OF BOOKS");
        
        JButton Return = new JButton("BACK");
        Return.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent e) {
        		new DashboardFrame().setVisible(true);
                dispose();
        	}
        });

        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
        	gl_contentPane.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_contentPane.createSequentialGroup()
        			.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
        				.addGroup(gl_contentPane.createSequentialGroup()
        					.addGap(159)
        					.addComponent(lblListOfBooks))
        				.addGroup(gl_contentPane.createSequentialGroup()
        					.addGap(167)
        					.addComponent(Return, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)))
        			.addContainerGap(161, Short.MAX_VALUE))
        );
        gl_contentPane.setVerticalGroup(
        	gl_contentPane.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_contentPane.createSequentialGroup()
        			.addGap(116)
        			.addComponent(lblListOfBooks)
        			.addPreferredGap(ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
        			.addComponent(Return)
        			.addGap(47))
        );
        contentPane.setLayout(gl_contentPane);
        pack();
    }
}
