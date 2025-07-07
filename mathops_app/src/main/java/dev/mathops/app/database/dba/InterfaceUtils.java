package dev.mathops.app.database.dba;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Font;

/**
 * User interface utilities.
 */
enum InterfaceUtils {
    ;

    /** The threshold for the sum R+B+G of the background color that is considered "light mode". */
    private static final int BRIGHT_MODE_THRESHOLD = 384;

    /**
     * Tests whether a background color should be considered "light mode" or "dark mode".
     *
     * @param bg the background color
     * @return the highlight color
     */
    static boolean isLight(final Color bg) {

        final int brightness = bg.getRed() + bg.getGreen() + bg.getBlue();

        return brightness > BRIGHT_MODE_THRESHOLD;
    }

    /**
     * Generates a highlight color based on whether this the background indicates "light mode" or "dark mode".
     *
     * @param isLight true if "light mode"; false if "dark mode"
     * @return the highlight color
     */
    static Color createHighlightColor(final boolean isLight) {

        return isLight ? new Color(50, 200, 180) : new Color(100, 160, 140);
    }

    /**
     * Generates an accent color based on whether this the background indicates "light mode" or "dark mode".
     *
     * @param isLight true if "light mode"; false if "dark mode"
     * @return the highlight color
     */
    static Color createAccentColor(final Color bg, final boolean isLight) {

        final int bgR = bg.getRed();
        final int bgG = bg.getGreen();
        final int bgB = bg.getBlue();

        final int r;
        final int g;
        final int b;

        if (isLight) {
            final Color darker = bg.darker();
            r = (darker.getRed() + bgR * 2) / 3;
            g = (darker.getGreen() + bgG * 2) / 3;
            b = (darker.getBlue() + bgB * 2) / 3;
        } else {
            final Color brighter = bg.brighter();
            r = (brighter.getRed() + bgR) / 2;
            g = (brighter.getGreen() + bgG) / 2;
            b = (brighter.getBlue() + bgB) / 2;
        }

        return new Color(r, g, b);
    }

    /**
     * Given a component, retrieves its font then generates a new font that is a scaled version of the original and sets
     * the component's font to the scaled font.
     *
     * @param component the component
     * @param factor    the scaling factor
     */
    static void resizeFont(final JComponent component, final double factor) {

        final Font origFont = component.getFont();
        final int size = origFont.getSize();
        final float scale = (float) ((double) size * factor);

        final Font scaledFont = origFont.deriveFont(scale);
        component.setFont(scaledFont);
    }
}
