package dev.mathops.assessment.document.inst;

import dev.mathops.commons.builder.HtmlBuilder;

import java.awt.Font;

/**
 * An immutable container for color and font style information.
 */
public final class DocObjectInstStyle {

    /** Plain font style (default). */
    public static final int PLAIN = Font.PLAIN;

    /** Bold font style. */
    public static final int BOLD = Font.BOLD;

    /** Italic font style. */
    public static final int ITALIC = Font.ITALIC;

    /** Underline font style. */
    public static final int UNDERLINE = 0x04;

    /** Over-line font style. */
    public static final int OVERLINE = 0x08;

    /** Strike through font style. */
    public static final int STRIKETHROUGH = 0x10;

    /** Boxed font style. */
    public static final int BOXED = 0x20;

    /** Hidden font style. */
    public static final int HIDDEN = 0x40;

    /** The foreground color name to use for any text using this format. */
    public final String colorName;

    /** The name of the font to use (default is SERIF). */
    public final String fontName;

    /** The point size of font to use. */
    public final float fontSize;

    /** The font style - some bitwise combination of style values from this class. */
    public final int fontStyle;

    /**
     * Construct a new {@code DocObjectIterationStyle}.
     *
     * @param theColorName the foreground color name (may not be {@code null})
     * @param theFontName  the font family name (may not be {@code null})
     * @param theFontSize  the font point size
     * @param theFontStyle the font style
     */
    public DocObjectInstStyle(final String theColorName, final String theFontName, final float theFontSize,
                              final int theFontStyle) {

        if (theColorName == null) {
            throw new IllegalArgumentException("Color name may not be null");
        }

        this.colorName = theColorName;
        this.fontName = theFontName;
        this.fontSize = theFontSize;
        this.fontStyle = theFontStyle;
    }

    /**
     * Test whether the font is boldface.
     *
     * @return {@code true} if boldface, {@code false} otherwise
     */
    public boolean isBold() {

        return (this.fontStyle & BOLD) == BOLD;
    }

    /**
     * Test whether the font is italics.
     *
     * @return {@code true} if italics, {@code false} otherwise
     */
    public boolean isItalic() {

        return (this.fontStyle & ITALIC) == ITALIC;
    }

    /**
     * Test whether the font is underlined.
     *
     * @return {@code true} if underlined, {@code false} otherwise
     */
    public boolean isUnderline() {

        return (this.fontStyle & UNDERLINE) == UNDERLINE;
    }

    /**
     * Test whether the font is overlined.
     *
     * @return {@code true} if overlined, {@code false} otherwise
     */
    public boolean isOverline() {

        return (this.fontStyle & OVERLINE) == OVERLINE;
    }

    /**
     * Test whether the font is strike-through.
     *
     * @return {@code true} if strike-through, {@code false} otherwise
     */
    public boolean isStrikethrough() {

        return (this.fontStyle & STRIKETHROUGH) == STRIKETHROUGH;
    }

    /**
     * Test whether the font is boxed.
     *
     * @return {@code true} if boxed, {@code false} otherwise
     */
    public boolean isBoxed() {

        return (this.fontStyle & BOXED) == BOXED;
    }

    /**
     * Test whether the font is hidden.
     *
     * @return {@code true} if hidden, {@code false} otherwise
     */
    public boolean isHidden() {

        return (this.fontStyle & HIDDEN) == HIDDEN;
    }

    /**
     * Appends XML attributes for style properties.
     * @param xml the {@code HtmlBuilder} to which to append
     */
    public void appendXmlAttributes(final HtmlBuilder xml) {

        xml.addAttribute("color", this.colorName, 0);
        xml.addAttribute("fontname", this.fontName, 0);
        xml.addAttribute("fontsize", Float.toString(this.fontSize), 0);
        if (this.fontStyle != 0) {
            xml.addAttribute("fontstyle", Integer.toString(this.fontStyle), 0);
        }
    }

    /**
     * Generate a diagnostic {@code String} representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(70);

        builder.add("DocObjectIterationStyle:colorName=", this.colorName, ",fontName=", this.fontName, ",fontSize");
        builder.add(this.fontSize);
        builder.add(",fontStyle=0x", Integer.toHexString(this.fontStyle));

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.colorName.hashCode() + this.fontName.hashCode() + Float.hashCode(this.fontSize) + this.fontStyle;
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocObjectInstStyle style) {
            equal = this.colorName.equals(style.colorName)
                    && this.fontName.equals(style.fontName)
                    && Math.abs(this.fontSize - style.fontSize) < 0.00001f
                    && this.fontStyle == style.fontStyle;
        } else {
            equal = false;
        }

        return equal;
    }
}
