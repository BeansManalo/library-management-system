package lms.gui;

import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import lms.core.Book;
import lms.core.Library;
import lms.core.Member;

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

    // The single shared data store every panel reads from and writes to.
    private final Library library = new Library();

    // Kept as fields (not just constructor-local) so showCard() and the
    // showXxxForm() helpers below can reach them after construction.
    private BookListPanel bookListPanel;
    private MemberListPanel memberListPanel;
    private AddEditBookPanel addEditBookPanel;
    private AddEditMemberPanel addEditMemberPanel;

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

        bookListPanel = new BookListPanel();
        bookListPanel.setMainFrame(this);
        contentPane.add(bookListPanel, CARD_BOOK_LIST);

        memberListPanel = new MemberListPanel();
        memberListPanel.setMainFrame(this);
        contentPane.add(memberListPanel, CARD_MEMBER_LIST);

        addEditBookPanel = new AddEditBookPanel();
        addEditBookPanel.setMainFrame(this);
        contentPane.add(addEditBookPanel, CARD_ADD_EDIT_BOOK);

        addEditMemberPanel = new AddEditMemberPanel();
        addEditMemberPanel.setMainFrame(this);
        contentPane.add(addEditMemberPanel, CARD_ADD_EDIT_MEMBER);

        cardLayout.show(contentPane, CARD_DASHBOARD);

        getAccessibleContext().setAccessibleDescription("");
        pack();
    }

    /** The one shared Book/Member store for the whole app. */
    public Library getLibrary() {
        return library;
    }

    /**
     * Swaps the frame's visible screen without opening a new window.
     */
    public void showCard(String cardName) {
        cardLayout.show(contentPane, cardName);

        // CardLayout panels are created once in the constructor above and
        // reused for the life of the app -- they are never rebuilt when
        // shown again. So list screens have to reload their table data
        // here every time they become visible, or they'd keep showing
        // whatever was on screen the last time the user looked at them.
        if (cardName.equals(CARD_BOOK_LIST)) {
            bookListPanel.refreshTable();
        } else if (cardName.equals(CARD_MEMBER_LIST)) {
            memberListPanel.refreshTable();
        }
        // Add another "else if" here for any future list screen that needs
        // the same reload-on-show treatment.
    }

    /**
     * Opens the Add/Edit Book screen. Pass null to start a blank "Add New"
     * form, or an existing Book to open it pre-filled for editing.
     */
    public void showBookForm(Book bookToEdit) {
        addEditBookPanel.loadBook(bookToEdit);
        showCard(CARD_ADD_EDIT_BOOK);
    }

    /**
     * Opens the Add/Edit Member screen. Pass null to start a blank "Add
     * New" form, or an existing Member to open it pre-filled for editing.
     */
    public void showMemberForm(Member memberToEdit) {
        addEditMemberPanel.loadMember(memberToEdit);
        showCard(CARD_ADD_EDIT_MEMBER);
    }
}
