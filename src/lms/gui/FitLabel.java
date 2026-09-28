package lms.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JLabel;

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

    /** Lets the font shrink by up to {@code points} to fit before "..." kicks in. */
    FitLabel shrink(int points) {
        shrink = points;
        return this;
    }

    // Everything paints through getFont(), so the fitted size never touches
    // the real font (no setFont, so no relayout/repaint loop).
    @Override
    public Font getFont() {
        Font f = super.getFont();
        String text = getText();
        Insets in = getInsets();
        int room = getWidth() - in.left - in.right;
        if (f == null || text == null || room <= 0) {
            return f;
        }
        float min = f.getSize2D() - shrink;
        while (f.getSize2D() > min && getFontMetrics(f).stringWidth(text) > room) {
            f = f.deriveFont(f.getSize2D() - 1);
        }
        return f;
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
