package dev.mathops.assessment.formula.edit;

import dev.mathops.commons.log.Log;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * The base class for all objects that have a bounding rectangle, origin point, advance width, font, and preferred
 * alignment baseline.
 */
abstract class AbstractFEDrawable {

    /** The font size to use for the component. */
    private int fontSize = 0;

    /** True for the Stix font; false for OpenSans. */
    private boolean isStix;

    /** The current font. */
    private Font font = null;

    /** The origin-relative bounding box. */
    private final Rectangle bounds;

    /** The origin of this object as a unit. */
    private final Point origin;

    /** The advance of this object as a unit. */
    private int advance = 0;

    /** The ascent of the center baseline above the typographic baseline. */
    private int centerAscent = 0;

    /** True to use the center line as the alignment point; false for baseline. */
    private boolean alignToCenter = false;

    /**
     * Constructs a new {@code AbstractFEDrawable}.
     */
    AbstractFEDrawable() {

        this.bounds = new Rectangle();
        this.origin = new Point();
        this.isStix = true;
    }

    /**
     * Make the box use the OpenSans font.
     */
    final void useSans() {

        this.isStix = false;
    }

    /**
     * Sets the font size for the component. This populates the font and font metrics.
     *
     * @param theFontSize the new font size
     */
    final void setFontSize(final int theFontSize) {

        this.fontSize = theFontSize;

        final Fonts fonts = Fonts.getInstance();

        this.font =
                this.isStix ? fonts.getSizedStix(theFontSize) : fonts.getSizedOpenSans(theFontSize);

        if (this.font == null) {
            Log.warning("Warning: Using fallback font.");
            this.font = this.isStix ? new Font(Font.SERIF, Font.PLAIN, theFontSize)
                    : new Font(Font.SANS_SERIF, Font.PLAIN, theFontSize);
        }
    }

    /**
     * Gets the font size for the component.
     *
     * @return the font size
     */
    final int getFontSize() {

        return this.fontSize;
    }

    /**
     * Gets the font.
     *
     * @return the font
     */
    final Font getFont() {

        return this.font;
    }

    /**
     * Gets the bounding rectangle for this box.
     *
     * @return the origin-relative bounding rectangle
     */
    final Rectangle getBounds() {

        return this.bounds;
    }

    /**
     * Gets the origin at which to draw the box.
     *
     * @return the origin
     */
    final Point getOrigin() {

        return this.origin;
    }

    /**
     * Sets the advance width.
     *
     * @param theAdvance the new advance width
     */
    final void setAdvance(final int theAdvance) {

        this.advance = theAdvance;
    }

    /**
     * Gets the advance width.
     *
     * @return the advance width
     */
    final int getAdvance() {

        return this.advance;
    }

    /**
     * Sets the ascent of the center line.
     *
     * @param theCenterAscent the new ascent of the center line
     */
    final void setCenterAscent(final int theCenterAscent) {

        this.centerAscent = theCenterAscent;
    }

    /**
     * Gets the ascent of the center line.
     *
     * @return the ascent of the center line
     */
    final int getCenterAscent() {

        return this.centerAscent;
    }

    /**
     * Sets the vertical alignment method for the object.
     *
     * @param isAlignToCenter {@code true} if object should align to the center line rather than baseline; {@code false}
     *                        to align to the baseline
     */
    public final void setAlignToCenter(final boolean isAlignToCenter) {

        this.alignToCenter = isAlignToCenter;
    }

    /**
     * Tests the vertical alignment method for the object.
     *
     * @return {@code true} if object should align to the center line rather than baseline; {@code false} to align to
     *         the baseline
     */
    public final boolean isAlignToCenter() {

        return this.alignToCenter;
    }
}
