package lms.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

@SuppressWarnings("serial")
public class AddEditBookPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblAddAndEditBooks;

    /**
     * Create the panel.
     */
    public AddEditBookPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);

        lblAddAndEditBooks = new JLabel("ADD AND EDIT BOOKS");

        JButton Return = new JButton("BACK");
        Return.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainFrame.showCard(MainFrame.CARD_DASHBOARD);
            }
        });
        Return.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });

        GroupLayout gl_this = new GroupLayout(this);
        gl_this.setHorizontalGroup(
        	gl_this.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_this.createSequentialGroup()
        			.addContainerGap(272, Short.MAX_VALUE)
        			.addGroup(gl_this.createParallelGroup(Alignment.LEADING)
        				.addGroup(Alignment.TRAILING, gl_this.createSequentialGroup()
        					.addComponent(Return)
        					.addGap(284))
        				.addGroup(Alignment.TRAILING, gl_this.createSequentialGroup()
        					.addComponent(lblAddAndEditBooks)
        					.addGap(260))))
        );
        gl_this.setVerticalGroup(
        	gl_this.createParallelGroup(Alignment.LEADING)
        		.addGroup(Alignment.TRAILING, gl_this.createSequentialGroup()
        			.addContainerGap(165, Short.MAX_VALUE)
        			.addComponent(lblAddAndEditBooks)
        			.addGap(60)
        			.addComponent(Return)
        			.addGap(98))
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
