package lms.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import lms.core.Loan;
import lms.core.Member;

/**
 * Read-only "library card" view of a single member -- opened by
 * double-clicking a row on the Member List, since that screen is
 * browse/search only now and no longer where fields get edited (see
 * MemberListPanel).
 *
 * The card has two faces, toggled with the FLIP button: the FRONT shows
 * the everyday identifying details (photo/avatar, name, member ID, date
 * of birth, contact info), and the BACK shows the Code 39 barcode and
 * the membership period. Both faces share the same card backdrop and
 * header -- only the fields underneath swap out (via setVisible(), not
 * two separate sub-panels), so every field stays registered directly
 * with this screen's own ProportionalLayout and keeps scaling correctly
 * on window resize (a nested panel with its own layout wouldn't).
 *
 * A second toggle (BOOKS / ID) swaps the whole card for a scrollable
 * record of what this member has borrowed: the same details at the top,
 * then every loan grouped under the day it was borrowed, each with its
 * own return date and status (including a penalty flag for late returns).
 * The card is compact on its two ID faces and grows taller for that list.
 */
@SuppressWarnings("serial")
public class MemberDetailPanel extends JPanel {

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();

    // The card and the button column beside it, centred as a group on the
    // 640x360 design. The ID faces are compact; the borrowed-books list
    // gets the same card, just taller.
    private static final int CARD_X = 68;
    private static final int CARD_Y = 58;
    private static final int CARD_W = 400;
    private static final int ID_H = 190;
    private static final int RECORDS_H = 232;
    private static final int BTN_X = 480;
    private static final int BTN_W = 92;
    private static final int HEADER_H = 26;
    private static final int RULE_Y = 116; // divider between the two halves of each face

    private final List<Component> frontFace = new ArrayList<>();
    private final List<Component> backFace = new ArrayList<>();

    private CardBackdrop cardBackdrop;

    // -- Front face --
    private JLabel nameLabel;
    private JLabel idLabel;
    private JLabel birthLabel;
    private JLabel emailLabel;
    private JLabel phoneLabel;
    private JLabel addressLabel;

    // -- Back face --
    private JLabel joinDateLabel;
    private JLabel endDateLabel;
    private BarcodeLabel barcode;

    // -- Borrowed-books view (replaces the whole card while showing) --
    private JScrollPane recordsScroll;
    private JList<Object> recordsList;
    private JLabel cardHeader;

    private PillButton btnFlip;
    private PillButton btnRecords;
    private boolean showingBack;
    private boolean showingRecords;

    private Member member;

    public MemberDetailPanel() {
        setPreferredSize(MainFrame.NHD_SIZE);
        setBackground(Theme.APP_BG);
        setLayout(layout);

        JLabel banner = Theme.banner("MEMBER DETAILS");
        place(banner, 0, 0, 640, 30);
        add(banner);

        // The library-card visual is just a backdrop drawn behind the
        // rest of this panel's own components (all sharing this same
        // panel's coordinate space) -- not a separate container --
        // which keeps a single ProportionalLayout in charge of the
        // whole screen instead of nesting a second one inside it.
        // Its size is set in showFace(), since it changes with the view.
        cardBackdrop = new CardBackdrop();
        add(cardBackdrop);

        // Shared by both faces -- the card's header strip never toggles.
        cardHeader = new JLabel("LIBRARY MEMBER CARD");
        cardHeader.setFont(Theme.FONT_LABEL);
        cardHeader.setForeground(Color.WHITE);
        cardHeader.setHorizontalAlignment(SwingConstants.CENTER);
        place(cardHeader, CARD_X, CARD_Y, CARD_W, HEADER_H);
        add(cardHeader);

        buildFrontFace();
        buildBackFace();
        buildRecordsView();

        btnFlip = new PillButton("FLIP");
        place(btnFlip, BTN_X, 120, BTN_W, 28);
        btnFlip.addActionListener(e -> setShowingBack(!showingBack));
        add(btnFlip);

        btnRecords = new PillButton("BOOKS");
        place(btnRecords, BTN_X, 158, BTN_W, 28);
        btnRecords.addActionListener(e -> setShowingRecords(!showingRecords));
        add(btnRecords);

        // Purely a viewer -- editing and deleting now only happen through
        // the "Add / Edit Members" screen (see MemberListPanel's manage
        // mode), so the only other action here is leaving.
        PillButton btnBack = new PillButton("BACK");
        place(btnBack, 270, 300, 100, 32);
        btnBack.addActionListener(e -> mainFrame.showCard(MainFrame.CARD_MEMBER_LIST));
        add(btnBack);

        // Swing paints components added earlier ON TOP of ones added
        // later (the opposite of the usual assumption) -- the backdrop
        // was added first, at the front of the Z-order, hiding
        // everything drawn "on" the card. Push it to the very back now
        // that every other component exists.
        setComponentZOrder(cardBackdrop, getComponentCount() - 1);

        showFace();
    }

    /** Name, member ID, date of birth, and contact details. */
    private void buildFrontFace() {
        // The person glyph on a soft circle with an accent ring.
        JLabel avatar = new JLabel(RowIcons.person(48, Theme.NAVY), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int d = Math.min(getWidth(), getHeight()) - 2; // stays round when the window isn't 16:9
                int x = (getWidth() - d) / 2;
                int y = (getHeight() - d) / 2;
                g2.setColor(Theme.CHIP_BG);
                g2.fillOval(x, y, d, d);
                g2.setColor(Theme.BLUE_ACCENT);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(x, y, d, d);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        place(avatar, CARD_X + 18, CARD_Y + 42, 64, 64);
        add(avatar);
        frontFace.add(avatar);

        int textX = CARD_X + 96;
        int textW = CARD_W - 96 - 18;
        nameLabel = faceLabel(frontFace, new Font("Arial", Font.BOLD, 17), Theme.TEXT_PRIMARY,
            SwingConstants.LEFT, textX, CARD_Y + 46, textW, 24);
        idLabel = faceLabel(frontFace, Theme.FONT_CARD_SUB_BOLD, Theme.BLUE_ACCENT,
            SwingConstants.LEFT, textX, CARD_Y + 72, textW, 16);
        birthLabel = faceLabel(frontFace, Theme.FONT_CARD_SUB, Theme.TEXT_PRIMARY,
            SwingConstants.LEFT, textX, CARD_Y + 90, textW, 16);

        emailLabel = contactRow("EMAIL", 0);
        phoneLabel = contactRow("PHONE", 1);
        addressLabel = contactRow("ADDRESS", 2);
    }

    /** One captioned line of contact details under the divider. */
    private JLabel contactRow(String caption, int row) {
        int y = CARD_Y + RULE_Y + 10 + row * 18;
        faceLabel(frontFace, Theme.FONT_CARD_CAPTION, Theme.TEXT_MUTED,
            SwingConstants.LEFT, CARD_X + 18, y, 56, 16).setText(caption);
        return faceLabel(frontFace, Theme.FONT_CARD_SUB, Theme.TEXT_PRIMARY,
            SwingConstants.LEFT, CARD_X + 76, y, CARD_W - 94, 16);
    }

    /** Membership period and the scannable barcode. */
    private void buildBackFace() {
        int half = (CARD_W - 36) / 2;
        Font date = new Font("Arial", Font.BOLD, 14);

        faceLabel(backFace, Theme.FONT_CARD_CAPTION, Theme.TEXT_MUTED,
            SwingConstants.CENTER, CARD_X + 18, CARD_Y + 42, CARD_W - 36, 14).setText("MEMBERSHIP PERIOD");

        faceLabel(backFace, Theme.FONT_CARD_CAPTION, Theme.TEXT_MUTED,
            SwingConstants.CENTER, CARD_X + 18, CARD_Y + 64, half, 14).setText("JOIN DATE");
        joinDateLabel = faceLabel(backFace, date, Theme.TEXT_PRIMARY,
            SwingConstants.CENTER, CARD_X + 18, CARD_Y + 78, half, 22);

        faceLabel(backFace, Theme.FONT_CARD_CAPTION, Theme.TEXT_MUTED,
            SwingConstants.CENTER, CARD_X + 18 + half, CARD_Y + 64, half, 14).setText("END DATE");
        endDateLabel = faceLabel(backFace, date, Theme.TEXT_PRIMARY,
            SwingConstants.CENTER, CARD_X + 18 + half, CARD_Y + 78, half, 22);

        barcode = new BarcodeLabel();
        place(barcode, CARD_X + 30, CARD_Y + RULE_Y + 8, CARD_W - 60, 56);
        add(barcode);
        backFace.add(barcode);
    }

    /** A card label on the given face -- placed, added, and registered so showFace() can toggle it. */
    private JLabel faceLabel(List<Component> face, Font font, Color color, int align, int x, int y, int w, int h) {
        JLabel label = new JLabel("", align);
        label.setFont(font);
        label.setForeground(color);
        place(label, x, y, w, h);
        add(label);
        face.add(label);
        return label;
    }

    /** The scrollable borrowed-books list that stands in for the card while BOOKS is toggled on. */
    private void buildRecordsView() {
        recordsList = new JList<>();
        recordsList.setCellRenderer(new RecordsRenderer());
        recordsList.setBackground(Theme.CARD_BG);
        recordsList.setFocusable(false);
        // Read-only: rows are never "selected".
        recordsList.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
            }
        });

        recordsScroll = new JScrollPane(recordsList);
        recordsScroll.setBorder(null);
        recordsScroll.getViewport().setBackground(Theme.CARD_BG);
        recordsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        recordsScroll.getVerticalScrollBar().setUnitIncrement(16);
        place(recordsScroll, CARD_X + 6, CARD_Y + HEADER_H + 6, CARD_W - 12, RECORDS_H - HEADER_H - 12);
        add(recordsScroll);
    }

    private void place(Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    /** Called by MainFrame right before switching to this card -- see MainFrame.showMemberDetail(). */
    public void loadMember(Member member) {
        this.member = member;
        showingBack = false;
        showingRecords = false;
        showFace();
        if (member == null) {
            return;
        }

        nameLabel.setText(text(member.getName()));
        idLabel.setText("MEMBER ID: " + text(member.getMemberId()));
        birthLabel.setText("DATE OF BIRTH: " + text(member.getBirthDate()));
        emailLabel.setText(text(member.getEmail()));
        phoneLabel.setText(text(member.getContactNumber()));
        addressLabel.setText(text(member.getAddress()));

        joinDateLabel.setText(text(member.getJoinDate()));
        endDateLabel.setText(text(member.getEndDate()));
        barcode.setData(member.getMemberId());

        loadRecords(member);
    }

    /** Rebuilds the borrowed-books list: details first, then loans newest-first, grouped by borrow date. */
    private void loadRecords(Member member) {
        List<Loan> loans = new ArrayList<>(member.getLoans());
        loans.sort(Comparator.comparing(Loan::getBorrowDate, Comparator.reverseOrder()));
        Map<LocalDate, Integer> booksPerDay = new LinkedHashMap<>();
        for (Loan loan : loans) {
            booksPerDay.merge(loan.getBorrowDate(), loan.getQuantity(), Integer::sum);
        }

        List<Object> items = new ArrayList<>();
        items.add(member); // shown as the details block at the top
        if (loans.isEmpty()) {
            items.add("No books borrowed yet.");
        }
        LocalDate day = null;
        for (Loan loan : loans) {
            if (!loan.getBorrowDate().equals(day)) {
                day = loan.getBorrowDate();
                items.add("Borrowed " + Member.DATE_FORMAT.format(day) + "   \u2022   " + booksPerDay.get(day) + " book(s)");
            }
            items.add(loan);
        }
        recordsList.setListData(items.toArray());
        recordsScroll.getVerticalScrollBar().setValue(0);
    }

    /** Toggles which of the two card faces is showing. */
    private void setShowingBack(boolean back) {
        showingBack = back;
        showFace();
    }

    /** Toggles between the ID card and the borrowed-books record. */
    private void setShowingRecords(boolean records) {
        showingRecords = records;
        showFace();
    }

    /** Shows exactly one of: card front, card back, or the borrowed-books record. */
    private void showFace() {
        for (Component c : frontFace) {
            c.setVisible(!showingRecords && !showingBack);
        }
        for (Component c : backFace) {
            c.setVisible(!showingRecords && showingBack);
        }

        recordsScroll.setVisible(showingRecords);
        btnFlip.setVisible(!showingRecords);
        cardHeader.setText(showingRecords ? "BORROWED BOOKS" : "LIBRARY MEMBER CARD");
        btnRecords.setText(showingRecords ? "ID" : "BOOKS");

        int height = showingRecords ? RECORDS_H : ID_H;
        place(cardBackdrop, CARD_X, CARD_Y, CARD_W, height);
        cardBackdrop.setShape(height, !showingRecords);
        revalidate();
        repaint();
    }

    private static String text(String value) {
        return (value == null || value.isEmpty()) ? "\u2014" : value;
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * Draws the borrowed-books list. One instance is reused for every
     * row (like BookCardPanel/MemberCardPanel), and each row is one of
     * three kinds: the member's details (first row), a text heading for
     * a borrow date, or a single loan.
     */
    private static class RecordsRenderer implements ListCellRenderer<Object> {
        private static final Color LATE = new Color(0xA5, 0x33, 0x33);

        private final JLabel dName = new JLabel();
        private final JLabel dId = new JLabel();
        private final JLabel dContact = new JLabel();
        private final JLabel dAddress = new JLabel();
        private final JPanel detailsRow = flat(new BorderLayout(10, 0));

        private final JLabel heading = new JLabel();
        private final JPanel headingRow = flat(new BorderLayout());

        private final JLabel lTitle = new JLabel();
        private final JLabel lSub = new JLabel();
        private final JLabel lStatus = new JLabel();
        private final JPanel loanRow = flat(new BorderLayout(8, 0));

        RecordsRenderer() {
            dName.setFont(new Font("Arial", Font.BOLD, 14));
            dName.setForeground(Theme.TEXT_PRIMARY);
            dId.setFont(Theme.FONT_CARD_ITALIC);
            dId.setForeground(Theme.TEXT_MUTED);
            dContact.setFont(Theme.FONT_CARD_SUB);
            dContact.setForeground(Theme.TEXT_MUTED);
            dAddress.setFont(Theme.FONT_CARD_SUB);
            dAddress.setForeground(Theme.TEXT_MUTED);
            JPanel details = flat(new GridLayout(0, 1));
            details.add(dName);
            details.add(dId);
            details.add(dContact);
            details.add(dAddress);
            detailsRow.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.DIVIDER), new EmptyBorder(8, 12, 8, 12)));
            detailsRow.add(new JLabel(RowIcons.person(44, Theme.NAVY)), BorderLayout.WEST);
            detailsRow.add(details, BorderLayout.CENTER);

            heading.setFont(Theme.FONT_CARD_SUB_BOLD);
            heading.setForeground(Theme.TEXT_PRIMARY);
            headingRow.setBackground(Theme.CHIP_BG);
            headingRow.setBorder(new EmptyBorder(4, 12, 4, 12));
            headingRow.add(heading, BorderLayout.CENTER);

            lTitle.setFont(Theme.FONT_CARD_TITLE);
            lTitle.setForeground(Theme.TEXT_PRIMARY);
            lSub.setFont(Theme.FONT_CARD_SUB);
            lSub.setForeground(Theme.TEXT_MUTED);
            lStatus.setFont(Theme.FONT_CARD_SUB_BOLD);
            lStatus.setHorizontalAlignment(SwingConstants.RIGHT);
            JPanel text = flat(new GridLayout(2, 1));
            text.add(lTitle);
            text.add(lSub);
            loanRow.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Theme.DIVIDER), new EmptyBorder(4, 24, 4, 12)));
            loanRow.add(text, BorderLayout.CENTER);
            loanRow.add(lStatus, BorderLayout.EAST);
        }

        /**
         * A white row panel that reports zero preferred width, so the list
         * always fits the viewport (long titles are clipped instead of
         * forcing a sideways scrollbar) -- only its height is measured.
         */
        private static JPanel flat(LayoutManager layout) {
            JPanel p = new JPanel(layout) {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(0, super.getPreferredSize().height);
                }
            };
            p.setBackground(Theme.CARD_BG);
            return p;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof Member m) {
                dName.setText(text(m.getName()));
                dId.setText("MEMBER ID: " + text(m.getMemberId()) + "     DATE OF BIRTH: " + text(m.getBirthDate()));
                dContact.setText(text(m.getEmail()) + "   \u2022   " + text(m.getContactNumber()));
                dAddress.setText(text(m.getAddress()));
                return detailsRow;
            }
            if (value instanceof Loan loan) {
                lTitle.setText(text(loan.getBook().getTitle()) + "   \u00D7" + loan.getQuantity());
                Loan.Status status = loan.getStatus(LocalDate.now());
                String due = Member.DATE_FORMAT.format(loan.getDueDate());
                lSub.setText(loan.isReturned()
                    ? "Returned " + Member.DATE_FORMAT.format(loan.getReturnDate()) + "  (due " + due + ")"
                    : "Return by " + due);
                lStatus.setText(switch (status) {
                    case BORROWED -> "BORROWED";
                    case OVERDUE -> "OVERDUE";
                    case RETURNED -> "RETURNED";
                    case RETURNED_LATE -> "RETURNED LATE \u2022 PENALTY";
                });
                lStatus.setForeground(switch (status) {
                    case BORROWED -> Theme.BLUE_ACCENT;
                    case RETURNED -> Theme.TEXT_MUTED;
                    default -> LATE;
                });
                return loanRow;
            }
            heading.setText(String.valueOf(value));
            return headingRow;
        }
    }

    /**
     * The card face: white fading to a pale blue, a navy header strip with
     * an accent line under it, and (on the ID faces) a divider. Measured
     * against the layout's design height so it scales with the labels on it.
     */
    private static class CardBackdrop extends JComponent {
        private int designHeight = ID_H;
        private boolean ruled;

        void setShape(int designHeight, boolean ruled) {
            this.designHeight = designHeight;
            this.ruled = ruled;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            float k = h / (float) designHeight;
            int head = Math.round(HEADER_H * k);

            g2.setPaint(new GradientPaint(0, 0, Theme.CARD_BG, 0, h, Theme.APP_BG));
            g2.fillRoundRect(0, 0, w, h, 14, 14);
            g2.setColor(Theme.NAVY);
            g2.fillRoundRect(0, 0, w, head, 14, 14);
            g2.fillRect(0, head / 2, w, head - head / 2);
            g2.setColor(Theme.BLUE_ACCENT);
            g2.fillRect(0, head, w, Math.max(2, Math.round(3 * k)));
            if (ruled) {
                int margin = Math.round(18f * w / CARD_W);
                g2.setColor(Theme.DIVIDER);
                g2.drawLine(margin, Math.round(RULE_Y * k), w - margin, Math.round(RULE_Y * k));
            }
            g2.setColor(Theme.DIVIDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
            g2.dispose();
        }
    }

    /** Draws a Code 39 barcode of whatever data it's given, scaled to fill its own width. */
    private static class BarcodeLabel extends JComponent {
        private String data = "";

        void setData(String value) {
            this.data = (value == null) ? "" : value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (data.isEmpty() || getWidth() <= 0) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            int unitsAtNarrow1 = Barcode.width(data, 1);
            int narrow = Math.max(1, getWidth() / Math.max(1, unitsAtNarrow1));
            int totalWidth = Barcode.width(data, narrow);
            int x = Math.max(0, (getWidth() - totalWidth) / 2);
            Barcode.draw(g2, data, x, 0, narrow, Math.max(10, getHeight() - 14), Color.BLACK);

            g2.setFont(Theme.FONT_CARD_MUTED);
            g2.setColor(Theme.TEXT_MUTED);
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(data)) / 2;
            g2.drawString(data, Math.max(0, textX), getHeight() - 2);
            g2.dispose();
        }
    }
}
