package dev.mathops.core.ui;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * A set of utilities for user interface construction.
 */
public enum UIUtilities {
    ;

    /** Default horizontal position. */
    private static final double DEFULT_X = 0.5;

    /** Default vertical position. */
    private static final double DEFULT_Y = 0.3;

    /**
     * Packs a window (including JFrame and JDialog), centers it within the desktop, and makes it visible.
     *
     * @param window the window to position
     */
    public static void packAndCenter(final Window window) {

        packAndShow(window, DEFULT_X, DEFULT_Y);
    }

    /**
     * Packs a window (including JFrame and JDialog), places it at a particular position within the desktop, and makes
     * it visible.
     *
     * @param window the window to position
     * @param xPos   the x position (0.0 is left, 1.0 is right, 0.5 is centered)
     * @param yPos   the y position (0.0 is top, 1.0 is bottom, 0.5 is centered)
     */
    private static void packAndShow(final Window window, final double xPos, final double yPos) {

        window.pack();

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = window.getSize();

        final int xPixel = (int) ((double) (screen.width - size.width) * xPos);
        final int yPixel = (int) ((double) (screen.height - size.height) * yPos);

        window.setLocation(xPixel, yPixel);
        window.setVisible(true);
    }

    /**
     * Given an array of labels, makes all of them right-aligned, and with the same preferred size.
     *
     * @param labels the array of labels to process
     */
    public static void makeLabelsSameSizeRightAligned(final JLabel[] labels) {

        int maxW = 0;
        int maxH = 0;
        for (final JLabel lbl : labels) {
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            final Dimension size = lbl.getPreferredSize();
            maxW = Math.max(maxW, size.width);
            maxH = Math.max(maxH, size.height);
        }

        final Dimension newSize = new Dimension(maxW, maxH);
        for (final JLabel lbl1 : labels) {
            lbl1.setPreferredSize(newSize);
        }
    }
}
