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

    /** Reference nHD (640x360) size every screen's layout is designed against. */
    public static final Dimension NHD_SIZE = new Dimension(640, 360);

    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_BOOK_LIST = "BOOK_LIST";
    public static final String CARD_MEMBER_LIST = "MEMBER_LIST";
    public static final String CARD_ADD_EDIT_BOOK = "ADD_EDIT_BOOK";
    public static final String CARD_ADD_EDIT_MEMBER = "ADD_EDIT_MEMBER";
    public static final String CARD_BOOK_DETAIL = "BOOK_DETAIL";
    public static final String CARD_MEMBER_DETAIL = "MEMBER_DETAIL";

    private JPanel contentPane;
    private CardLayout cardLayout;

    // The single shared data store every panel reads from and writes to.
    private final Library library = new Library();

    // Kept as fields (not just constructor-local) so showCard() and the
    // showXxxForm()/showXxxDetail() helpers below can reach them after
    // construction.
    private BookListPanel bookListPanel;
    private MemberListPanel memberListPanel;
    private AddEditBookPanel addEditBookPanel;
    private AddEditMemberPanel addEditMemberPanel;
    private BookDetailPanel bookDetailPanel;
    private MemberDetailPanel memberDetailPanel;

    /**
     * Create the frame.
     */
    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Library Management System");
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        // Resizable (including maximize/full screen) -- every screen
        // uses ProportionalLayout instead of fixed null-layout bounds,
        // so the whole design scales with the window instead of
        // clipping or leaving dead space at larger sizes.
        setResizable(true);
        setMinimumSize(new Dimension(480, 270));

        cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);
        contentPane.setBackground(Theme.APP_BG);
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

        bookDetailPanel = new BookDetailPanel();
        bookDetailPanel.setMainFrame(this);
        contentPane.add(bookDetailPanel, CARD_BOOK_DETAIL);

        memberDetailPanel = new MemberDetailPanel();
        memberDetailPanel.setMainFrame(this);
        contentPane.add(memberDetailPanel, CARD_MEMBER_DETAIL);

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
            bookListPanel.refresh();
        } else if (cardName.equals(CARD_MEMBER_LIST)) {
            memberListPanel.refresh();
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

    /** Opens the Book List in its plain, read-only browse/search role. */
    public void showBookList() {
        bookListPanel.setManageMode(false);
        showCard(CARD_BOOK_LIST);
    }

    /** Opens the same Book List screen in its "Add / Edit Books" management role. */
    public void showBookManage() {
        bookListPanel.setManageMode(true);
        showCard(CARD_BOOK_LIST);
    }

    /** Opens the Member List in its plain, read-only browse/search role. */
    public void showMemberList() {
        memberListPanel.setManageMode(false);
        showCard(CARD_MEMBER_LIST);
    }

    /** Opens the same Member List screen in its "Add / Edit Members" management role. */
    public void showMemberManage() {
        memberListPanel.setManageMode(true);
        showCard(CARD_MEMBER_LIST);
    }

    /** Opens the read-only, in-depth Book Details screen for the given book. */
    public void showBookDetail(Book book) {
        bookDetailPanel.loadBook(book);
        showCard(CARD_BOOK_DETAIL);
    }

    /** Opens the read-only library-card-style Member Details screen for the given member. */
    public void showMemberDetail(Member member) {
        memberDetailPanel.loadMember(member);
        showCard(CARD_MEMBER_DETAIL);
    }
}
