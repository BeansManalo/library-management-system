package lms.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.InputStream;
import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * One place for the colors and fonts every panel shares, so the app
 * reads as one piece of software instead of five separately-styled
 * screens. Navy palette with a lemon accent; Comfortaa Bold for titles and
 * buttons, Arial for everything that has to stay small and readable.
 */
final class Theme {

    private Theme() {
    }

    // -- Palette -------------------------------------------------------
    // The same in light and dark mode:
    static final Color BLUE_ACCENT = new Color(0x3E, 0x7C, 0xB1);
    // The app's namesake, used sparingly: the underline of a screen title.
    static final Color LEMON = new Color(0xF2, 0xC2, 0x30);
    // Overdue books, expired memberships, lost copies -- anything that needs attention.
    // The fill of red buttons (white text on it); for red text use ALERT_TEXT.
    static final Color ALERT = new Color(0xA5, 0x33, 0x33);

    // These change with the mode (see setDark()). NAVY, ALERT and TEXT_MUTED also fill buttons
    // that carry white text, so they stay dark enough for that; where one is used as ink on the
    // page instead, the app uses HEADING, ALERT_TEXT or INK, which go light in dark mode.
    static Color NAVY_DARK;
    static Color NAVY;
    static Color HEADING;
    static Color ALERT_TEXT;
    static Color INK;
    // The desk the page card (see MainFrame) sits on.
    static Color APP_BG;
    static Color CARD_BG;
    static Color CARD_SELECTED;
    static Color DIVIDER;
    // Outline of fields, lists and tables: a step darker than DIVIDER so they read on the page.
    static Color FIELD_BORDER;
    static Color CHIP_BG;
    static Color TABLE_HEAD;
    static Color ROW_ALT;
    static Color TEXT_PRIMARY;
    static Color TEXT_MUTED;
    // The pressed fill of the grey CANCEL buttons.
    static Color CANCEL_PRESSED;

    private static boolean dark;

    static {
        setDark(false);
    }

    static boolean isDark() {
        return dark;
    }

    /**
     * Switches the changing colors to their light or dark value. Components copy a color when
     * they are built, so screens that already exist keep the old ones until they are rebuilt.
     */
    static void setDark(boolean on) {
        dark = on;
        //                   light     dark
        NAVY_DARK      = pick(0x0F1F33, 0x1F4571);
        NAVY           = pick(0x1B3A5C, 0x2A5A8E);
        HEADING        = pick(0x1B3A5C, 0x8EB8E6);
        ALERT_TEXT     = pick(0xA53333, 0xE87B7B);
        INK            = pick(0x000000, 0xE4EAF1);
        APP_BG         = pick(0xE3E9F1, 0x10171F);
        CARD_BG        = pick(0xFFFFFF, 0x1B2531);
        CARD_SELECTED  = pick(0xDBE9F7, 0x2A4361);
        DIVIDER        = pick(0xD3DDE8, 0x303E4E);
        FIELD_BORDER   = pick(0xBCCAD9, 0x465970);
        CHIP_BG        = pick(0xE7EDF4, 0x263444);
        TABLE_HEAD     = pick(0xE9EFF6, 0x243245);
        ROW_ALT        = pick(0xF6F9FC, 0x202B38);
        TEXT_PRIMARY   = pick(0x1B2A3A, 0xE4EAF1);
        TEXT_MUTED     = pick(0x637283, 0x718399);
        CANCEL_PRESSED = pick(0x1B2A3A, 0x4E5E72);
    }

    private static Color pick(int light, int darkMode) {
        return new Color(dark ? darkMode : light);
    }

    // -- Fonts ---------------------------------------------------------
    private static final String BODY = "Arial";
    private static final Font DISPLAY = loadDisplay();

    static final Font FONT_HEADING = display(14);
    static final Font FONT_LABEL = new Font(BODY, Font.PLAIN, 12);
    static final Font FONT_FIELD = new Font(BODY, Font.PLAIN, 12);
    static final Font FONT_BUTTON = display(11);
    static final Font FONT_CARD_TITLE = new Font(BODY, Font.BOLD, 12);
    // The reference mockups italicize the "personal detail" fields
    // (author/email, the ID in the corner, the left-column stat values)
    // and keep the more categorical fields upright -- see FONT_CARD_SUB
    // (bottom-right, book-style) vs FONT_CARD_ITALIC (everything else).
    static final Font FONT_CARD_ITALIC = new Font(BODY, Font.ITALIC, 10);
    static final Font FONT_CARD_SUB = new Font(BODY, Font.PLAIN, 10);
    static final Font FONT_CARD_SUB_BOLD = new Font(BODY, Font.BOLD, 10);
    static final Font FONT_CARD_MUTED = new Font(BODY, Font.PLAIN, 8);
    static final Font FONT_CARD_CAPTION = new Font(BODY, Font.BOLD, 8);
    static final Font FONT_CARD_STAT = new Font(BODY, Font.ITALIC, 10);

    /** Comfortaa Bold (bundled in lms/fonts) at the given size; plain Arial Bold if the file is missing. */
    static Font display(float size) {
        return DISPLAY.deriveFont(size);
    }

    private static Font loadDisplay() {
        try (InputStream in = Theme.class.getResourceAsStream("/lms/fonts/Comfortaa-Bold.ttf")) {
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (Exception e) {
            return new Font(BODY, Font.BOLD, 12);
        }
    }

    /**
     * App-wide look-and-feel defaults. Call before the first component exists:
     * Arial instead of the platform's dialog font (message boxes, spinners,
     * tooltips included), and slim flat scroll bars instead of the stock ones.
     */
    static void install() {
        for (Object key : Collections.list(UIManager.getDefaults().keys())) {
            if (key instanceof String s && s.endsWith(".font")) {
                UIManager.put(key, new FontUIResource(BODY, Font.PLAIN, 12));
            }
        }
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("ScrollBarUI", FlatScrollBarUI.class.getName());

        // Message / confirm / input / option boxes (see ThemedOptionPaneUI for the rest).
        UIManager.put("OptionPaneUI", ThemedOptionPaneUI.class.getName());
        UIManager.put("OptionPane.background", CARD_BG);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont", new FontUIResource(BODY, Font.PLAIN, 12));
        UIManager.put("OptionPane.border", new EmptyBorder(16, 18, 14, 18));
        UIManager.put("OptionPane.messageAreaBorder", new EmptyBorder(0, 0, 4, 0));
        UIManager.put("OptionPane.buttonAreaBorder", new EmptyBorder(10, 0, 0, 0));
        UIManager.put("OptionPane.informationIcon", new MessageIcon(NAVY, Color.WHITE, "i"));
        UIManager.put("OptionPane.questionIcon", new MessageIcon(BLUE_ACCENT, Color.WHITE, "?"));
        UIManager.put("OptionPane.warningIcon", new MessageIcon(LEMON, NAVY, "!"));
        UIManager.put("OptionPane.errorIcon", new MessageIcon(ALERT, Color.WHITE, "\u00d7"));

        // Text boxes and drop-downs inside those dialogs match the app's fields.
        UIManager.put("TextField.border", fieldBorder());
        UIManager.put("ComboBox.background", CARD_BG);
        UIManager.put("ComboBox.selectionBackground", CARD_SELECTED);
        UIManager.put("ComboBox.selectionForeground", TEXT_PRIMARY);

        // The L&F's own text fields, drop-downs and spinners are black on white. In dark mode they
        // follow the page; in light mode the override is removed (null) so the L&F's colors show.
        for (String field : new String[] {"TextField", "FormattedTextField"}) {
            ifDark(field + ".background", CARD_BG);
            ifDark(field + ".inactiveBackground", ROW_ALT);
            ifDark(field + ".disabledBackground", ROW_ALT);
            ifDark(field + ".foreground", TEXT_PRIMARY);
            ifDark(field + ".inactiveForeground", TEXT_MUTED);
            ifDark(field + ".caretForeground", TEXT_PRIMARY);
            ifDark(field + ".selectionBackground", CARD_SELECTED);
            ifDark(field + ".selectionForeground", TEXT_PRIMARY);
        }
        ifDark("ComboBox.foreground", TEXT_PRIMARY);
        // Metal draws a drop-down's arrow in a fixed black; the basic UI colors it from these.
        ifDark("ComboBoxUI", "javax.swing.plaf.basic.BasicComboBoxUI");
        ifDark("ComboBox.buttonBackground", CARD_BG);
        ifDark("ComboBox.buttonShadow", FIELD_BORDER);
        ifDark("ComboBox.buttonDarkShadow", TEXT_MUTED);
        ifDark("ComboBox.buttonHighlight", FIELD_BORDER);
        ifDark("ComboBox.border", new LineBorder(FIELD_BORDER, 1));
        ifDark("Spinner.background", CARD_BG);
        ifDark("Spinner.foreground", TEXT_PRIMARY);
        ifDark("Spinner.border", new LineBorder(FIELD_BORDER, 1));
    }

    private static void ifDark(String key, Object value) {
        UIManager.put(key, dark ? value : null);
    }

    /** A flat round badge with one character in it, in place of the platform's dialog icons. */
    private static final class MessageIcon implements javax.swing.Icon {
        private final Color fill;
        private final Color mark;
        private final String text;

        MessageIcon(Color fill, Color mark, String text) {
            this.fill = fill;
            this.mark = mark;
            this.text = text;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillOval(x, y, 30, 30);
            g2.setColor(mark);
            g2.setFont(display(18));
            java.awt.FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, x + (30 - fm.stringWidth(text)) / 2, y + (30 - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 30;
        }

        @Override
        public int getIconHeight() {
            return 30;
        }
    }

    /**
     * A screen's title: left-aligned, with a thin rule under it and a lemon
     * tab at its start. Caller only needs to position it with setBounds().
     */
    static JLabel title(String text) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(DIVIDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
                g.setColor(LEMON);
                g.fillRect(0, getHeight() - 3, 36, 3);
            }
        };
        label.setFont(FONT_HEADING);
        label.setForeground(HEADING);
        return label;
    }

    /** The border of every text field: a rounded outline with a little room around the text. */
    static Border fieldBorder() {
        return new CompoundBorder(outline(), new EmptyBorder(2, 7, 2, 7));
    }

    /** Just the rounded outline, for small controls that bring their own padding. */
    static Border outline() {
        return new RoundBorder();
    }

    private static final class RoundBorder extends AbstractBorder {
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(FIELD_BORDER);
            g2.drawRoundRect(x, y, w - 1, h - 1, 8, 8);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }

    /**
     * The pointing hand over a clickable control -- but only while it is enabled, since the hand
     * promises a click that does something. Follows the control as it is enabled and disabled.
     */
    static void handCursor(JComponent control) {
        Runnable update = () -> control.setCursor(
            new Cursor(control.isEnabled() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        update.run();
        control.addPropertyChangeListener("enabled", e -> update.run());
    }

    /** A small rounded, filled pill for a single tag/keyword (non-interactive). */
    static JLabel chip(String text) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CHIP_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setFont(FONT_CARD_MUTED);
        label.setForeground(TEXT_PRIMARY);
        label.setBorder(new EmptyBorder(3, 9, 3, 9));
        return label;
    }

    /** A compact, read-only-looking table in the app's style; widths are relative column weights. */
    static JTable table(TableModel model, int... widths) {
        JTable t = new JTable(model) {
            // Alternate rows are tinted, and cells get a little room either side.
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : ROW_ALT);
                }
                if (c instanceof JComponent j) {
                    j.setBorder(new EmptyBorder(0, 6, 0, 6));
                }
                return c;
            }
        };
        t.setFont(FONT_CARD_SUB);
        t.setForeground(TEXT_PRIMARY);
        t.setBackground(CARD_BG);
        t.setRowHeight(22);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(CARD_SELECTED);
        t.setSelectionForeground(TEXT_PRIMARY);
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Numbers (quantity, copies free) centered so they don't run into the next column.
        DefaultTableCellRenderer centered = new DefaultTableCellRenderer();
        centered.setHorizontalAlignment(SwingConstants.CENTER);
        t.setDefaultRenderer(Integer.class, centered);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            {
                setBackground(TABLE_HEAD);
                setForeground(HEADING);
                setBorder(new CompoundBorder(new MatteBorder(0, 0, 2, 0, BLUE_ACCENT), new EmptyBorder(0, 6, 0, 6)));
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                    boolean focus, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focus, row, column);
                setFont(FONT_CARD_SUB_BOLD);
                setHorizontalAlignment(table.getColumnClass(column) == Integer.class
                    ? SwingConstants.CENTER : SwingConstants.LEFT);
                return this;
            }
        });
        t.getTableHeader().setPreferredSize(new Dimension(t.getTableHeader().getPreferredSize().width, 24));
        for (int i = 0; i < widths.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        return t;
    }

    /** Wraps a table in a scroll pane with the app's border and white background. */
    static JScrollPane scroll(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(new LineBorder(FIELD_BORDER, 1));
        pane.getViewport().setBackground(CARD_BG);
        return pane;
    }

    /** Slim, flat scroll bar: just a rounded thumb, no arrow buttons. Installed by install(). */
    public static final class FlatScrollBarUI extends BasicScrollBarUI {

        public static ComponentUI createUI(JComponent c) {
            return new FlatScrollBarUI();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return new Dimension(10, 10);
        }

        @Override
        protected javax.swing.JButton createDecreaseButton(int orientation) {
            return noButton();
        }

        @Override
        protected javax.swing.JButton createIncreaseButton(int orientation) {
            return noButton();
        }

        private static javax.swing.JButton noButton() {
            javax.swing.JButton b = new javax.swing.JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle r) {
            g.setColor(ROW_ALT);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() || isDragging ? BLUE_ACCENT : FIELD_BORDER);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }
    }
}
