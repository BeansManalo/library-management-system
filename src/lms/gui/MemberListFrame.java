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
public class MemberListFrame extends JFrame {

    private JPanel contentPane;
    private JLabel lblListOfMembers;

    /**
     * Create the frame.
     */
    public MemberListFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPane = new JPanel();
        setContentPane(contentPane);

        lblListOfMembers = new JLabel("LIST OF MEMBERS");
        
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
        			.addGap(159)
        			.addComponent(lblListOfMembers)
        			.addContainerGap(145, Short.MAX_VALUE))
        		.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
        			.addContainerGap(171, Short.MAX_VALUE)
        			.addComponent(Return, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
        			.addGap(162))
        );
        gl_contentPane.setVerticalGroup(
        	gl_contentPane.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_contentPane.createSequentialGroup()
        			.addGap(116)
        			.addComponent(lblListOfMembers)
        			.addPreferredGap(ComponentPlacement.RELATED, 103, Short.MAX_VALUE)
        			.addComponent(Return)
        			.addGap(42))
        );
        contentPane.setLayout(gl_contentPane);
        pack();
    }
}
