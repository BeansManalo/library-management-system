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
        lblAddAndEditBooks.setBounds(267, 163, 108, 14);

        JButton Return = new JButton("BACK");
        Return.setBounds(284, 250, 75, 23);
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
        setLayout(null);
        add(lblAddAndEditBooks);
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
