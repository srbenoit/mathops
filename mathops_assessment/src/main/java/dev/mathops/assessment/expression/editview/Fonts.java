package dev.mathops.assessment.expression.editview;

import dev.mathops.font.BundledFontManager;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;

/**
 * A container for one-point versions of all fonts needed.
 */
public enum Fonts {
    ;

    /** A {@code Graphics2D} font render context used to generate glyph vectors and font metrics. */
    public static final Graphics2D g2d;

    /** A font render context used to generate glyph vectors and font metrics. */
    public static final FontRenderContext frc;

    /** A sans-serif font used for strings. */
    public static final Font sans;

    /** The math font. */
    public static final Font math;

    /** The text font. */
    public static final Font text;

    static {
        final BundledFontManager fontMgr = BundledFontManager.getInstance();

        sans = fontMgr.getOnePointFont("Arial");
        math = fontMgr.getOnePointFont("STIX Two Math Regular");
        text = fontMgr.getOnePointFont("STIX Two Text Regular");

        final BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        g2d = img.createGraphics();
        frc = g2d.getFontRenderContext();
    }
}
