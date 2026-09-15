package lms.gui;

import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {

    /** Consistent nHD (640x360) size shared by every screen in the app. */
    public static final Dimension NHD_SIZE = new Dimension(640, 360);

    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_BOOK_LIST = "BOOK_LIST";
    public static final String CARD_MEMBER_LIST = "MEMBER_LIST";
    public static final String CARD_ADD_EDIT_BOOK = "ADD_EDIT_BOOK";
    public static final String CARD_ADD_EDIT_MEMBER = "ADD_EDIT_MEMBER";

    private JPanel contentPane;
    private CardLayout cardLayout;

    /**
     * Create the frame.
     */
    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Library Management System");
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setResizable(false);

        cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);
        setContentPane(contentPane);

        DashboardPanel dashboardPanel = new DashboardPanel();
        dashboardPanel.setMainFrame(this);
        contentPane.add(dashboardPanel, CARD_DASHBOARD);

        BookListPanel bookListPanel = new BookListPanel();
        bookListPanel.setMainFrame(this);
        contentPane.add(bookListPanel, CARD_BOOK_LIST);

        MemberListPanel memberListPanel = new MemberListPanel();
        memberListPanel.setMainFrame(this);
        contentPane.add(memberListPanel, CARD_MEMBER_LIST);

        AddEditBookPanel addEditBookPanel = new AddEditBookPanel();
        addEditBookPanel.setMainFrame(this);
        contentPane.add(addEditBookPanel, CARD_ADD_EDIT_BOOK);

        AddEditMemberPanel addEditMemberPanel = new AddEditMemberPanel();
        addEditMemberPanel.setMainFrame(this);
        contentPane.add(addEditMemberPanel, CARD_ADD_EDIT_MEMBER);

        cardLayout.show(contentPane, CARD_DASHBOARD);

        getAccessibleContext().setAccessibleDescription("");
        pack();
    }

    /**
     * Swaps the frame's visible screen without opening a new window.
     */
    public void showCard(String cardName) {
        cardLayout.show(contentPane, cardName);
    }
}
