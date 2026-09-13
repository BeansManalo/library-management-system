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
public class AddEditMemberFrame extends JFrame {

    private JPanel contentPane;
    private JLabel lblAddAndEditMembers;

    /**
     * Create the frame.
     */
    public AddEditMemberFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPane = new JPanel();
        setContentPane(contentPane);

        lblAddAndEditMembers = new JLabel("ADD AND EDIT MEMBERS");
        
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
        	gl_contentPane.createParallelGroup(Alignment.TRAILING)
        		.addGroup(gl_contentPane.createSequentialGroup()
        			.addContainerGap(127, Short.MAX_VALUE)
        			.addComponent(lblAddAndEditMembers)
        			.addGap(138))
        		.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
        			.addGap(158)
        			.addComponent(Return, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
        			.addContainerGap(169, Short.MAX_VALUE))
        );
        gl_contentPane.setVerticalGroup(
        	gl_contentPane.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_contentPane.createSequentialGroup()
        			.addGap(115)
        			.addComponent(lblAddAndEditMembers)
        			.addPreferredGap(ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
        			.addComponent(Return)
        			.addGap(52))
        );
        contentPane.setLayout(gl_contentPane);
        pack();
    }
}
