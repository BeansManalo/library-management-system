package lms.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingConstants;
import java.awt.Font;

@SuppressWarnings("serial")
public class MemberListPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblListOfMembers;
    private JTable table;

    /**
     * Create the panel.
     */
    public MemberListPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);

        lblListOfMembers = new JLabel("LIST OF MEMBERS");
        lblListOfMembers.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblListOfMembers.setHorizontalAlignment(SwingConstants.CENTER);
        lblListOfMembers.setBounds(279, 29, 105, 14);

        JButton Return = new JButton("BACK");
        Return.setBounds(302, 252, 59, 23);
        Return.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainFrame.showCard(MainFrame.CARD_DASHBOARD);
            }
        });
        setLayout(null);
        add(lblListOfMembers);
        add(Return);
        
        table = new JTable();
        table.setModel(new DefaultTableModel(
        	new Object[][] {
        		{null, null, null, null, null},
        		{null, null, null, null, null},
        		{null, null, null, null, null},
        		{null, null, null, null, null},
        		{null, null, null, null, null},
        		{null, null, null, null, null},
        		{null, null, null, null, null},
        		{null, null, null, null, null},
        		{null, null, null, null, null},
        		{null, null, null, null, null},
        	},
        	new String[] {
        		"New column", "New column", "New column", "New column", "New column"
        	}
        ));
        table.setBorder(new LineBorder(new Color(0, 0, 0)));
        table.setBounds(143, 56, 375, 160);
        add(table);
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
