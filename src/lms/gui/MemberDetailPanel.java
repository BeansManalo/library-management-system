package lms.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lms.core.Member;

/**
 * Read-only "library card" view of a single member: photo/avatar, name,
 * ID, membership dates, contact info, and a Code 39 barcode of the
 * member ID -- opened by double-clicking a row on the Member List,
 * since that screen is browse/search only now and no longer where
 * fields get edited (see MemberListPanel).
 */
@SuppressWarnings("serial")
public class MemberDetailPanel extends JPanel {

    private MainFrame mainFrame;
    private final ProportionalLayout layout = new ProportionalLayout();

    private JLabel nameLabel;
    private JLabel idLabel;
    private JLabel datesLabel;
    private JLabel emailLabel;
    private JLabel phoneLabel;
    private JLabel addressLabel;
    private JLabel iconLabel;
    private BarcodeLabel barcode;

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
        CardBackdrop cardBackdrop = new CardBackdrop();
        place(cardBackdrop, 90, 46, 460, 230);
        add(cardBackdrop);

        JLabel cardHeader = new JLabel("LIBRARY MEMBER CARD");
        cardHeader.setFont(Theme.FONT_LABEL);
        cardHeader.setForeground(Color.WHITE);
        cardHeader.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        place(cardHeader, 90, 46, 460, 24);
        add(cardHeader);

        iconLabel = new JLabel(RowIcons.person(64, Theme.NAVY));
        place(iconLabel, 112, 84, 64, 64);
        add(iconLabel);

        nameLabel = new JLabel();
        nameLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 17));
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        place(nameLabel, 200, 84, 330, 24);
        add(nameLabel);

        idLabel = new JLabel();
        idLabel.setFont(Theme.FONT_CARD_ITALIC);
        idLabel.setForeground(Theme.TEXT_MUTED);
        place(idLabel, 200, 108, 330, 18);
        add(idLabel);

        datesLabel = new JLabel();
        datesLabel.setFont(Theme.FONT_CARD_SUB);
        datesLabel.setForeground(Theme.TEXT_PRIMARY);
        place(datesLabel, 200, 128, 330, 18);
        add(datesLabel);

        emailLabel = new JLabel();
        emailLabel.setFont(Theme.FONT_CARD_SUB);
        emailLabel.setForeground(Theme.TEXT_MUTED);
        place(emailLabel, 112, 158, 420, 16);
        add(emailLabel);

        phoneLabel = new JLabel();
        phoneLabel.setFont(Theme.FONT_CARD_SUB);
        phoneLabel.setForeground(Theme.TEXT_MUTED);
        place(phoneLabel, 112, 176, 420, 16);
        add(phoneLabel);

        addressLabel = new JLabel();
        addressLabel.setFont(Theme.FONT_CARD_MUTED);
        addressLabel.setForeground(Theme.TEXT_MUTED);
        place(addressLabel, 112, 194, 420, 16);
        add(addressLabel);

        barcode = new BarcodeLabel();
        place(barcode, 112, 222, 410, 42);
        add(barcode);

        // Purely a viewer -- editing and deleting now only happen through
        // the "Add / Edit Members" screen (see MemberListPanel's manage
        // mode), so the only action here is leaving.
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
    }

    private void place(Component c, int x, int y, int w, int h) {
        layout.put(c, x, y, w, h);
    }

    /** Called by MainFrame right before switching to this card -- see MainFrame.showMemberDetail(). */
    public void loadMember(Member member) {
        this.member = member;
        if (member == null) {
            return;
        }

        nameLabel.setText(text(member.getName()));
        idLabel.setText(text(member.getMemberId()));
        datesLabel.setText(text(member.getJoinDate()) + "  \u2192  " + text(member.getEndDate()));
        emailLabel.setText(text(member.getEmail()));
        phoneLabel.setText(text(member.getContactNumber()));
        addressLabel.setText(text(member.getAddress()));
        barcode.setData(member.getMemberId());
    }

    private static String text(String value) {
        return (value == null || value.isEmpty()) ? "\u2014" : value;
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /** Plain white rounded-rect card face with a navy header strip. */
    private static class CardBackdrop extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.CARD_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.setColor(Theme.NAVY);
            g2.fillRoundRect(0, 0, getWidth(), 26, 14, 14);
            g2.fillRect(0, 13, getWidth(), 13);
            g2.setColor(Theme.DIVIDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
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
