package lms.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;

/**
 * Makes every JOptionPane (message, confirm, input and option boxes alike) look like
 * the rest of the app: white page, Arial text, pill buttons. Installed by Theme.install(),
 * so the dialogs need no changes where they are shown.
 */
public class ThemedOptionPaneUI extends BasicOptionPaneUI {

    public static ComponentUI createUI(JComponent c) {
        return new ThemedOptionPaneUI();
    }

    @Override
    protected void installComponents() {
        super.installComponents();
        restyle(optionPane);
    }

    // The pane builds its message and button panels itself, in the platform grey; paint them
    // the app's colors, and turn its plain buttons into pills. "No" and "Cancel" are the quiet
    // choice (grey, like every CANCEL button in the app); everything else is navy.
    private static void restyle(Container c) {
        boolean buttons = "OptionPane.buttonArea".equals(c.getName());
        for (Component child : c.getComponents()) {
            // Only the pane's own buttons -- not a drop-down's arrow or a date picker's calendar button.
            if (buttons && child instanceof JButton b) {
                b.setUI(new PillUI());
                b.setFont(Theme.FONT_BUTTON);
                b.setForeground(java.awt.Color.WHITE);
                b.setBorder(new EmptyBorder(6, 18, 6, 18));
                b.setFocusPainted(false);
                b.setDisplayedMnemonicIndex(-1); // keep Alt+Y / Alt+N, drop the underline
                Theme.handCursor(b);
                b.putClientProperty("quiet", "No".equals(b.getText()) || "Cancel".equals(b.getText()));
            } else if (child instanceof JPanel p) {
                p.setBackground(Theme.CARD_BG);
            }
            if (child instanceof Container inner) {
                restyle(inner);
            }
        }
    }

    /** The same rounded fill PillButton paints, for buttons the pane creates itself. */
    private static final class PillUI extends BasicButtonUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            boolean quiet = Boolean.TRUE.equals(b.getClientProperty("quiet"));
            java.awt.Color base = quiet ? Theme.TEXT_MUTED : Theme.NAVY;
            java.awt.Color fill = b.getModel().isPressed() ? (quiet ? Theme.CANCEL_PRESSED : Theme.NAVY_DARK)
                : b.getModel().isRollover() ? (quiet ? Theme.TEXT_MUTED.brighter() : Theme.BLUE_ACCENT) : base;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 10, 10);
            g2.dispose();
            super.paint(g, c);
        }

        @Override
        public void installDefaults(AbstractButton b) {
            super.installDefaults(b);
            b.setOpaque(false);
            b.setRolloverEnabled(true);
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            Dimension d = super.getPreferredSize(c);
            return new Dimension(Math.max(d.width, 84), d.height);
        }
    }
}
