package lms.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

@SuppressWarnings("serial")
public class AddEditMemberPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblAddAndEditMembers;

    /**
     * Create the panel.
     */
    public AddEditMemberPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);

        lblAddAndEditMembers = new JLabel("ADD AND EDIT MEMBERS");
        lblAddAndEditMembers.setBounds(262, 162, 121, 14);

        JButton Return = new JButton("BACK");
        Return.setBounds(284, 232, 75, 23);
        Return.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainFrame.showCard(MainFrame.CARD_DASHBOARD);
            }
        });
        setLayout(null);
        add(lblAddAndEditMembers);
        add(Return);
    }

    /**
     * Wires this panel to the frame hosting it, so its buttons can switch
     * cards instead of opening a new window.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
}
