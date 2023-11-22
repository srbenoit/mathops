package dev.mathops.font;

import java.awt.Font;

/**
 * A specification for a font that can be obtained from the {@code BundledFontManager}.
 */
public final class FontSpec {

    /** The name of the font, or one of "SANS", "SERIF" or "MONOSPACE". */
    public String fontName;

    /** The point size of the font. */
    public double fontSize;

    /** The style of the font. */
    public int fontStyle;

    /**
     * Constructs a new {@code FontSpec}.
     */
    public FontSpec() {

        this.fontName = null;
        this.fontSize = 1.0;
        this.fontStyle = Font.PLAIN;
    }
}
