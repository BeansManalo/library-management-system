package lms.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLabelUI;

/**
 * Text drawn at a scaled-up size can come out a pixel or two wider than the
 * unscaled measurements the layout was built from, which would clip the last
 * character of a label that is exactly as wide as its text. When the window is
 * scaled, labels therefore paint a few pixels past their edges (see
 * ScaledLayeredPane, which installs this). At the normal size nothing changes.
 */
public class ScaledLabelUI extends MetalLabelUI {

    private static final ScaledLabelUI INSTANCE = new ScaledLabelUI();

    public static ComponentUI createUI(JComponent c) {
        return INSTANCE;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g.create();
        java.awt.Rectangle clip = g2.getClipBounds();
        if (clip != null && g2.getTransform().getScaleX() != 1) {
            g2.setClip(clip.x - 4, clip.y, clip.width + 8, clip.height); // sideways only
        }
        super.paint(g2, c);
        g2.dispose();
    }
}
