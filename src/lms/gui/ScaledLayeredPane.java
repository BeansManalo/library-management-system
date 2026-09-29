package lms.gui;

import java.awt.AWTEvent;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.JViewport;
import javax.swing.RepaintManager;
import javax.swing.UIManager;

/**
 * Makes a bigger window (see MainFrame.openSettings) look exactly like the normal one,
 * only bigger. Every screen stays laid out at the 640x360 it was designed for; this pane (which holds
 * the content and any popups) paints all of that scaled up to fit the window, centered,
 * and the mouse events are mapped back so clicks land on what is drawn. Fonts, borders,
 * icons and buttons therefore all grow by the same factor, and the shape never changes.
 * At the normal window size nothing is scaled and nothing is different.
 */
@SuppressWarnings("serial")
final class ScaledLayeredPane extends JLayeredPane {

    private static final int W = MainFrame.NHD_SIZE.width;
    private static final int H = MainFrame.NHD_SIZE.height;

    /** Call before the frame's content pane is set. */
    static void install(JFrame frame) {
        UIManager.put("LabelUI", ScaledLabelUI.class.getName()); // before any label exists
        ScaledLayeredPane pane = new ScaledLayeredPane();
        frame.setLayeredPane(pane);

        // Swing repaints just the changed rectangle, in unscaled coordinates; repainting the
        // whole window instead keeps every paint going through the scaled paint() below.
        RepaintManager.setCurrentManager(new RepaintManager() {
            @Override
            public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
                JRootPane root = c.getRootPane();
                if (root != null && root == frame.getRootPane()) {
                    super.addDirtyRegion(root, 0, 0, root.getWidth(), root.getHeight());
                } else {
                    super.addDirtyRegion(c, x, y, w, h);
                }
            }
        });

        // Mouse events arrive in window pixels; the components live in 640x360 space.
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueue() {
            @Override
            protected void dispatchEvent(AWTEvent e) {
                super.dispatchEvent(e instanceof MouseEvent m && m.getSource() == frame ? pane.toDesign(m) : e);
            }
        });
    }

    private double scale() {
        return Math.min(getWidth() / (double) W, getHeight() / (double) H);
    }

    private int offsetX() {
        return (int) Math.round((getWidth() - W * scale()) / 2);
    }

    private int offsetY() {
        return (int) Math.round((getHeight() - H * scale()) / 2);
    }

    private boolean isScaled() {
        return scale() != 1 || offsetX() != 0 || offsetY() != 0;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        // Scrolling by copying pixels works in window space; plain repaints work in ours.
        simpleScrolling(this);
    }

    private static void simpleScrolling(Container c) {
        for (java.awt.Component child : c.getComponents()) {
            if (child instanceof JViewport v) {
                v.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            }
            if (child instanceof Container inner) {
                simpleScrolling(inner);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        if (!isScaled()) {
            super.paint(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Theme.APP_BG);
        g2.fillRect(0, 0, getWidth(), getHeight());
        double s = scale();
        g2.translate(offsetX(), offsetY());
        g2.scale(s, s);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        super.paint(g2);
        g2.dispose();
    }

    /** Window pixels -> 640x360 design space. */
    private MouseEvent toDesign(MouseEvent e) {
        if (!isScaled()) {
            return e;
        }
        double s = scale();
        // Mouse coordinates are the frame's, which start at the window's outer corner (title bar and
        // borders included), while this pane starts at the root pane. Scale only the part inside the
        // root pane, then put the frame's offset back on. Floor at the pixel's centre, not round, so
        // a click lands on the design pixel actually drawn under it.
        int ox = getRootPane().getX();
        int oy = getRootPane().getY();
        int x = (int) Math.floor((e.getX() - ox - offsetX() + 0.5) / s) + ox;
        int y = (int) Math.floor((e.getY() - oy - offsetY() + 0.5) / s) + oy;
        if (e instanceof MouseWheelEvent w) {
            return new MouseWheelEvent(w.getComponent(), w.getID(), w.getWhen(), w.getModifiersEx(), x, y,
                w.getXOnScreen(), w.getYOnScreen(), w.getClickCount(), w.isPopupTrigger(), w.getScrollType(),
                w.getScrollAmount(), w.getWheelRotation(), w.getPreciseWheelRotation());
        }
        return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), x, y,
            e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
    }
}
