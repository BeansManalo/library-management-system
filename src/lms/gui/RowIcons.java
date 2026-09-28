package lms.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Small glyphs for the list cards (and, in white, the dashboard's dark
 * buttons), painted directly with Graphics2D instead of bundling image
 * files -- a plain stand-in for a book cover / member photo.
 */
final class RowIcons {

    private RowIcons() {
    }

    /**
     * A closed book seen at a slight angle: a narrow spine, a wider front
     * cover beside it, a bottom edge bar under both, and a small bookmark
     * ribbon -- matching the reference mockup's icon rather than a plain
     * outlined square.
     */
    static Icon book(int size, Color color) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = begin(g, x, y);

                // One solid silhouette (spine+cover as a single shape)
                // rather than two separate rounded rects -- two pieces
                // with a gap between them tends to round both inner
                // corners and read as a notch cut into a block instead
                // of a spine beside a cover, at these small sizes.
                int totalW = Math.round(size * 0.60f);
                int bodyH = Math.round(size * 0.60f);
                int barH = Math.max(2, Math.round(size * 0.14f));
                int startX = (size - totalW) / 2;
                int startY = Math.round(size * 0.10f);
                int arc = Math.max(1, size / 14);

                g2.setColor(color);
                g2.fillRoundRect(startX, startY, totalW, bodyH, arc, arc);
                g2.fillRoundRect(startX, startY + bodyH - arc, totalW, barH, arc, arc);

                // Spine/cover crease -- a thin line in whatever the icon
                // sits on, so it reads as a crease notched into the cover
                // rather than a stripe of some unrelated third color.
                g2.setStroke(new java.awt.BasicStroke(Math.max(1f, size / 22f)));
                g2.setColor(blendWithBackdrop(color));
                int spineW = Math.round(totalW * 0.34f);
                g2.drawLine(startX + spineW, startY + Math.max(1, size / 14),
                    startX + spineW, startY + bodyH - Math.max(1, size / 14));

                // Bookmark ribbon, hanging from the bottom bar -- skipped
                // at the smallest sizes (e.g. the dashboard buttons),
                // where this level of detail just reads as noise.
                if (size >= 24) {
                    int ribbonW = Math.max(3, Math.round(size * 0.16f));
                    int ribbonH = Math.max(3, Math.round(size * 0.22f));
                    int ribbonX = startX + (totalW - ribbonW) / 2;
                    int ribbonY = startY + bodyH - arc + barH - Math.round(barH * 0.25f);

                    Path2D.Float ribbon = new Path2D.Float();
                    ribbon.moveTo(ribbonX, ribbonY);
                    ribbon.lineTo(ribbonX + ribbonW, ribbonY);
                    ribbon.lineTo(ribbonX + ribbonW, ribbonY + ribbonH);
                    ribbon.lineTo(ribbonX + ribbonW / 2f, ribbonY + ribbonH * 0.6f);
                    ribbon.lineTo(ribbonX, ribbonY + ribbonH);
                    ribbon.closePath();

                    g2.setColor(Theme.DIVIDER);
                    g2.fill(ribbon);
                }

                g2.dispose();
            }
        };
    }

    /** A simple head-and-shoulders silhouette, clipped to a circular frame. */
    static Icon person(int size, Color color) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = begin(g, x, y);
                g2.clip(new Ellipse2D.Float(0, 0, size, size));

                g2.setColor(color);
                int headD = size * 2 / 5;
                g2.fillOval((size - headD) / 2, size / 7, headD, headD);
                g2.fillArc(size / 8, size / 2, size * 3 / 4, size * 3 / 4, 0, 180);
                g2.dispose();
            }
        };
    }

    /**
     * The spine crease needs to read as a notch cut into the cover, so
     * it's painted in whatever the icon is sitting on (card = white,
     * dark button = navy) rather than a fixed third color.
     */
    private static Color blendWithBackdrop(Color iconColor) {
        return iconColor == Color.WHITE ? Theme.NAVY : Theme.CARD_BG;
    }

    /** A plain outlined calendar page, for the date-picker button. */
    static Icon calendar(int size, Color color) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = begin(g, x, y);
                int pad = Math.max(1, size / 10);
                int bodyY = Math.round(size * 0.24f);
                float stroke = Math.max(1f, size / 13f);

                g2.setStroke(new java.awt.BasicStroke(stroke));
                g2.setColor(color);
                g2.drawRoundRect(pad, bodyY, size - 2 * pad, size - bodyY - pad, 3, 3);
                g2.drawLine(pad, bodyY + Math.round(size * 0.2f), size - pad, bodyY + Math.round(size * 0.2f));

                int ringW = Math.max(1, size / 9);
                int ringH = Math.round(size * 0.2f);
                g2.fillRect(pad + Math.round(size * 0.12f), Math.round(size * 0.05f), ringW, ringH);
                g2.fillRect(size - pad - Math.round(size * 0.12f) - ringW, Math.round(size * 0.05f), ringW, ringH);

                g2.dispose();
            }
        };
    }

    /** A small solid triangle, for the date picker's prev/next month buttons. */
    static Icon triangle(int size, boolean pointRight, Color color) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = begin(g, x, y);
                g2.setColor(color);
                int[] xs, ys;
                if (pointRight) {
                    xs = new int[] {Math.round(size * 0.25f), Math.round(size * 0.25f), Math.round(size * 0.8f)};
                    ys = new int[] {0, size, size / 2};
                } else {
                    xs = new int[] {Math.round(size * 0.75f), Math.round(size * 0.75f), Math.round(size * 0.2f)};
                    ys = new int[] {0, size, size / 2};
                }
                g2.fillPolygon(xs, ys, 3);
                g2.dispose();
            }
        };
    }

    /** Three stacked bars (a "hamburger"), for the dashboard's menu button. */
    static Icon menu(int size, Color color) {
        return new Icon() {
            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = begin(g, x, y);
                g2.setColor(color);
                int barH = Math.max(2, size / 8);
                for (int i = 0; i < 3; i++) {
                    g2.fillRoundRect(0, i * (size - barH) / 2, size, barH, barH, barH);
                }
                g2.dispose();
            }
        };
    }

    /** Shared setup: a fresh Graphics2D translated to the icon's origin, antialiased. */
    private static Graphics2D begin(Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        return g2;
    }
}
