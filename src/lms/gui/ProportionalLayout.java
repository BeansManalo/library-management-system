package lms.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;

/**
 * A drop-in replacement for {@code setLayout(null)} that keeps every
 * screen resizable (including maximizing / full screen) without
 * redoing any of their hand-placed layouts: each component is still
 * registered with the same pixel bounds used when this app was designed
 * at a fixed 640x360, but those bounds are stored as fractions of that
 * reference size and re-applied, scaled to whatever size the container
 * actually is, on every resize. Real bounds are set on the real
 * components (not just painted at a different scale), so mouse clicks
 * land exactly where things are drawn at any window size.
 *
 * Font sizes are not scaled by this -- only position and size -- so
 * text stays a fixed, legible point size while the space around it
 * grows.
 */
class ProportionalLayout implements LayoutManager {

    private static final float REF_W = MainFrame.NHD_SIZE.width;
    private static final float REF_H = MainFrame.NHD_SIZE.height;

    private final Map<Component, float[]> fractions = new HashMap<>();

    /** Registers a component using pixel bounds from the original 640x360 design. */
    void put(Component c, int x, int y, int w, int h) {
        fractions.put(c, new float[] {x / REF_W, y / REF_H, w / REF_W, h / REF_H});
    }

    @Override
    public void layoutContainer(Container parent) {
        int cw = parent.getWidth();
        int ch = parent.getHeight();
        for (Map.Entry<Component, float[]> entry : fractions.entrySet()) {
            float[] f = entry.getValue();
            entry.getKey().setBounds(Math.round(f[0] * cw), Math.round(f[1] * ch),
                Math.round(f[2] * cw), Math.round(f[3] * ch));
        }
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return MainFrame.NHD_SIZE;
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(Math.round(REF_W * 0.6f), Math.round(REF_H * 0.6f));
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }
}
