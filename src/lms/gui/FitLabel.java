package lms.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * A JLabel for text of unpredictable length (book titles, publishers,
 * member names...). It never asks its layout for the width of its text,
 * only for whatever the layout has spare, so a long value can't shove its
 * neighbours aside or widen a list row. Text that doesn't fit is first
 * shrunk by up to {@link #shrink(int)} points (none by default), and
 * whatever still doesn't fit is cut short with "..." -- JLabel's own
 * behaviour once it is given less room than its text needs.
 */
@SuppressWarnings("serial")
class FitLabel extends JLabel {

    private int shrink;
    private Font base; // the label's own font, before setWrappedText last changed it

    /** Lets the font shrink by up to {@code points} to fit before "..." kicks in. */
    FitLabel shrink(int points) {
        shrink = points;
        return this;
    }

    // Everything paints through getFont(), so the fitted size never touches
    // the real font (no setFont, so no relayout/repaint loop). Two-line (HTML)
    // text is left alone: Swing's HTML renderer fixes its font when the text is set.
    @Override
    public Font getFont() {
        Font f = super.getFont();
        String text = getText();
        Insets in = getInsets();
        int room = getWidth() - in.left - in.right;
        if (f == null || text == null || room <= 0 || text.startsWith("<html>")) {
            return f;
        }
        float min = f.getSize2D() - shrink;
        while (f.getSize2D() > min && getFontMetrics(f).stringWidth(text) > room) {
            f = f.deriveFont(f.getSize2D() - 1);
        }
        return f;
    }

    /**
     * Like setText, for a title that may need two lines: text that still doesn't fit
     * on one line after shrinking is broken at the space nearest its middle and shown
     * at the smallest size the shrink allows (smaller still, down to 9, if a half is
     * still too wide); a half that still doesn't fit is cut short with "...".
     * {@code width} is the width the label will be given. Needs a label tall enough
     * for two lines.
     */
    void setWrappedText(String text, int width) {
        if (base == null) {
            base = super.getFont();
        }
        setFont(base); // undo the size a previous two-line title left behind
        Insets in = getInsets();
        int room = width - in.left - in.right - 4;
        Font f = base.deriveFont(base.getSize2D() - shrink);
        int mid = text.length() / 2;
        int a = text.lastIndexOf(' ', mid);
        int b = text.indexOf(' ', mid);
        int cut = a < 0 ? b : b < 0 ? a : mid - a <= b - mid ? a : b;
        FontMetrics fm = getFontMetrics(f);
        if (cut <= 0 || fm.stringWidth(text) <= room) {
            setText(text); // one line (or no space to break at): getFont() shrinks / cuts it
            return;
        }
        String top = text.substring(0, cut);
        String bottom = text.substring(cut + 1);
        while (f.getSize() > 9 && Math.max(fm.stringWidth(top), fm.stringWidth(bottom)) > room) {
            f = f.deriveFont(f.getSize2D() - 1);
            fm = getFontMetrics(f);
        }
        setFont(f);
        setText("<html>" + html(clip(top, fm, room)) + "<br>" + html(clip(bottom, fm, room)) + "</html>");
    }

    // Cuts a line short with "..." the way JLabel itself does.
    private static String clip(String s, FontMetrics fm, int room) {
        return SwingUtilities.layoutCompoundLabel(fm, s, null, SwingConstants.CENTER, SwingConstants.LEFT,
            SwingConstants.CENTER, SwingConstants.RIGHT, new Rectangle(room, 100), new Rectangle(), new Rectangle(), 0);
    }

    private static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;");
    }

    // Height always comes from the unshrunk font, so rows stay one size.
    @Override
    public Dimension getPreferredSize() {
        Insets in = getInsets();
        return new Dimension(0, getFontMetrics(super.getFont()).getHeight() + in.top + in.bottom);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, getPreferredSize().height);
    }
}
