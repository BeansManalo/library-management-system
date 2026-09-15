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
public class BookListPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel lblListOfBooks;

    /**
     * Create the panel.
     */
    public BookListPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);

        lblListOfBooks = new JLabel("LIST OF BOOKS");
        lblListOfBooks.setBounds(283, 172, 75, 14);

        JButton Return = new JButton("BACK");
        Return.setBounds(294, 257, 59, 23);
        Return.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainFrame.showCard(MainFrame.CARD_DASHBOARD);
            }
        });
        setLayout(null);
        add(lblListOfBooks);
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
