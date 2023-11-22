package dev.mathops.assessment.htmlgen;

/**
 * An active style.
 */
public final class Style {

    /** The point size. */
    private final float size;

    /** The current color name. */
    private final String colorName;

    /**
     * Constructs a new {@code Style}.
     *
     * @param theSize      the point size
     * @param theColorName the color name
     */
    public Style(final float theSize, final String theColorName) {

        this.size = theSize;
        this.colorName = theColorName;
    }

    /**
     * Gets the point size.
     *
     * @return the point size
     */
    public float getSize() {

        return this.size;
    }

    /**
     * Gets the current color name.
     *
     * @return the current color name
     */
    public String getColorName() {

        return this.colorName;
    }
}
