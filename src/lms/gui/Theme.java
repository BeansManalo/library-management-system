package lms.gui;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * One place for the colors and fonts every panel shares, so the app
 * reads as one piece of software instead of five separately-styled
 * screens. Dark-blue palette, Arial throughout, per the requested look.
 */
final class Theme {

    private Theme() {
    }

    // -- Palette -------------------------------------------------------
    static final Color NAVY_DARK = new Color(0x0F, 0x1F, 0x33);
    static final Color NAVY = new Color(0x1B, 0x3A, 0x5C);
    static final Color BLUE_ACCENT = new Color(0x3E, 0x7C, 0xB1);
    static final Color APP_BG = new Color(0xEE, 0xF2, 0xF7);
    static final Color CARD_BG = Color.WHITE;
    static final Color CARD_SELECTED = new Color(0xDB, 0xE9, 0xF7);
    static final Color DIVIDER = new Color(0xD3, 0xDD, 0xE8);
    static final Color CHIP_BG = new Color(0xE7, 0xED, 0xF4);
    static final Color TEXT_PRIMARY = new Color(0x1B, 0x2A, 0x3A);
    static final Color TEXT_MUTED = new Color(0x63, 0x72, 0x83);

    // -- Fonts (Arial everywhere, per the request) ----------------------
    static final Font FONT_BANNER = new Font("Arial", Font.BOLD, 14);
    static final Font FONT_LABEL = new Font("Arial", Font.PLAIN, 12);
    static final Font FONT_FIELD = new Font("Arial", Font.PLAIN, 12);
    static final Font FONT_BUTTON = new Font("Arial", Font.BOLD, 12);
    static final Font FONT_CARD_TITLE = new Font("Arial", Font.BOLD, 12);
    // The reference mockups italicize the "personal detail" fields
    // (author/email, the ID in the corner, the left-column stat values)
    // and keep the more categorical fields upright -- see FONT_CARD_SUB
    // (bottom-right, book-style) vs FONT_CARD_ITALIC (everything else).
    static final Font FONT_CARD_ITALIC = new Font("Arial", Font.ITALIC, 10);
    static final Font FONT_CARD_SUB = new Font("Arial", Font.PLAIN, 10);
    static final Font FONT_CARD_SUB_BOLD = new Font("Arial", Font.BOLD, 10);
    static final Font FONT_CARD_MUTED = new Font("Arial", Font.PLAIN, 8);
    static final Font FONT_CARD_CAPTION = new Font("Arial", Font.BOLD, 8);
    static final Font FONT_CARD_STAT = new Font("Arial", Font.ITALIC, 10);

    /**
     * The dark banner every screen opens with (e.g. "LIST OF BOOKS").
     * Caller only needs to position it with setBounds().
     */
    static JLabel banner(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(NAVY);
        label.setForeground(Color.WHITE);
        label.setFont(FONT_BANNER);
        label.setBorder(new EmptyBorder(0, 0, 0, 0));
        return label;
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
        JTable t = new JTable(model);
        t.setFont(FONT_CARD_SUB);
        t.setForeground(TEXT_PRIMARY);
        t.setRowHeight(20);
        t.setGridColor(DIVIDER);
        t.setShowVerticalLines(false);
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
        t.getTableHeader().setFont(FONT_CARD_SUB_BOLD);
        for (int i = 0; i < widths.length; i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        return t;
    }

    /** Wraps a table in a scroll pane with the app's border and white background. */
    static JScrollPane scroll(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(new LineBorder(DIVIDER, 1));
        pane.getViewport().setBackground(CARD_BG);
        return pane;
    }
}
