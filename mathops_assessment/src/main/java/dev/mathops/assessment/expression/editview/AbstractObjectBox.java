package dev.mathops.assessment.expression.editview;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

/**
 * A box that bounds an object in a presented expression.
 */
abstract class AbstractObjectBox {

    /** Commonly used value. */
    static final double HALF = 0.5;

    /** The X position of the left edge of the box. */
    int x;

    /** The Y position of the typographic baseline (positive Y is downward). */
    int y;

    /** The width of the box. */
    int width;

    /** The (positive) offset relative to the baseline of the top of the box. */
    int top;

    /** The (negative) offset relative to the baseline of the bottom of the box (height = top - bottom). */
    int bottom;

    /**
     * The (positive) offset relative to the baseline of the typographic center-line of the box (this is the midpoint
     * between the top and bottom of full-height uppercase Latin letters like M, N, H, and X in the font).
     */
    int typoCenter;

    /**
     * The (positive) offset relative to the baseline of the mathematical center-line of the box (this is the midpoint
     * between the top and bottom of mathematical operators like + and - in the font).
     */
    int mathAxis;

    /**
     * Constructs a new {@code ObjectBox}.
     */
    AbstractObjectBox() {

        // No action
    }

    /**
     * Sets the mathematical axis and typographic center-line based on glyphs in the font.
     *
     * @param font the font
     */
    final void setAxisCenter(final Font font) {

        final GlyphVector plusVector = font.createGlyphVector(Fonts.frc, "+");
        final Rectangle2D plusBounds = plusVector.getVisualBounds();
        final double plusMinY = plusBounds.getMinY();
        final double plusMaxY = plusBounds.getMaxY();
        this.mathAxis = (int) (Math.round((plusMinY + plusMaxY) * HALF));

        final GlyphVector eVector = font.createGlyphVector(Fonts.frc, "E");
        final Rectangle2D eBounds = eVector.getVisualBounds();
        final double eMinY = eBounds.getMinY();
        final double eMaxY = eBounds.getMaxY();
        this.typoCenter = (int) (Math.round((eMinY + eMaxY) * HALF));
    }

    /**
     * Sets the x coordinate at which to draw the left edge of the box.
     *
     * @param theX the new x coordinate
     */
    final void setX(final int theX) {

        this.x = theX;
    }

    /**
     * Gets the x position at which to place the left edge of this box.
     *
     * @return the x position
     */
    public final int getX() {

        return this.x;
    }

    /**
     * Sets the y coordinate at which to draw the baseline.
     *
     * @param theY the new y coordinate
     */
    final void setY(final int theY) {

        this.y = theY;
    }

    /**
     * Gets the y position at which to place the baseline.
     *
     * @return the y position
     */
    public final int getY() {

        return this.y;
    }

    /**
     * Sets the box width.
     *
     * @param theWidth the new width
     */
    final void setWidth(final int theWidth) {

        this.width = theWidth;
    }

    /**
     * Gets the width of this box.
     *
     * @return the width
     */
    public final int getWidth() {

        return this.width;
    }

    /**
     * Sets the top and bottom offsets.
     *
     * @param theTop    the new top offset
     * @param theBottom the new bottom offset
     */
    final void setTopBottom(final int theTop, final int theBottom) {

        this.top = theTop;
        this.bottom = theBottom;
    }

    /**
     * Gets the (positive) offset relative to the baseline of the top of the box.
     *
     * @return the top offset
     */
    public final int getTop() {

        return this.top;
    }

    /**
     * Gets the (negative) offset relative to the baseline of the bottom of the box (height = top - bottom).
     *
     * @return the bottom offset
     */
    public final int getBottom() {

        return this.bottom;
    }

    /**
     * Gets the total height of the box.
     *
     * @return the height
     */
    final int getHeight() {

        return this.top - this.bottom;
    }

    /**
     * Gets the (positive) offset relative to the baseline of the typographic center-line of the box (this is the
     * midpoint between the top and bottom of full-height uppercase Latin letters like M, N, H, and X in the font).
     *
     * @return the typographic center offset
     */
    public final int getTypoCenter() {

        return this.typoCenter;
    }

    /**
     * Gets the (positive) offset relative to the baseline of the mathematical center-line of the box (this is the
     * midpoint between the top and bottom of mathematical operators like + and - in the font).
     *
     * @return the mathematical axis offset
     */
    public final int getMathAxis() {

        return this.mathAxis;
    }

    /**
     * Paints the contents of the box
     *
     * @param g2d the {@code Graphics2D} to which to draw
     */
    public abstract void paint(Graphics2D g2d);
}
