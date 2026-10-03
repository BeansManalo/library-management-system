package lms.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * A JButton painted as a rounded, filled pill instead of the default
 * flat OS button, so the app doesn't lean on plain gray buttons for
 * every action. Text/icon painting is left to the normal look-and-feel
 * (via super.paintComponent) -- only the background is custom.
 */
@SuppressWarnings("serial")
public class PillButton extends JButton {

    private final Color fill;
    private final Color fillHover;
    private final Color fillPressed;

    public PillButton(String text) {
        this(text, Theme.NAVY, Theme.BLUE_ACCENT, Theme.NAVY_DARK);
    }

    public PillButton(String text, Color fill, Color fillHover, Color fillPressed) {
        super(text);
        this.fill = fill;
        this.fillHover = fillHover;
        this.fillPressed = fillPressed;

        setFont(Theme.FONT_BUTTON);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        Theme.handCursor(this);
    }

    public void setIcon(Icon icon, int gap) {
        setIcon(icon);
        setIconTextGap(gap);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color c = !isEnabled() ? Theme.DIVIDER
            : getModel().isPressed() ? fillPressed
            : getModel().isRollover() ? fillHover
            : fill;
        g2.setColor(c);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();

        super.paintComponent(g);
    }
}
