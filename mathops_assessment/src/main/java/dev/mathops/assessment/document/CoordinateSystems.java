package dev.mathops.assessment.document;

import dev.mathops.commons.builder.SimpleBuilder;

import java.util.Objects;

/**
 * An object that holds information on the coordinate systems relative to which shapes can be specified.  This includes
 * the "pixel space" coordinate system (defined by a width and height), and a "graph space" system (defined by a set of
 * {@code NumberBounds} and a border width (which must be in pixel space).  The drawing size, minus the border width on
 * each side, is mapped to the graph space bounds to resolve graph coordinates.
 */
public final class CoordinateSystems {

    /** The width of the region in "pixel space". */
    private final Number pixelSpaceWidth;

    /** The height of the region in "pixel space". */
    private final Number pixelSpaceHeight;

    /** The border width, in pixels (may be null if there is no border). */
    private final Number borderWidth;

    /** The bounds of the "graph space". */
    private final NumberBounds graphSpaceBounds;

    /**
     * Constructs a new {@code CoordinateSystems}.
     *
     * @param thePixelSpaceWidth  the width of the region in "pixel space"
     * @param thePixelSpaceHeight the height of the region in "pixel space"
     * @param theBorderWidth      the border width, in pixels (can be null if there is no border)
     * @param theGraphSpaceBounds the bounds of the "graph space"
     */
    public CoordinateSystems(final Number thePixelSpaceWidth, final Number thePixelSpaceHeight,
                             final Number theBorderWidth, final NumberBounds theGraphSpaceBounds) {

        if (thePixelSpaceWidth == null || thePixelSpaceHeight == null) {
            throw new IllegalArgumentException("Pixel space dimensions may not be null");
        }
        if (theGraphSpaceBounds == null) {
            throw new IllegalArgumentException("Bounds may not be null");
        }

        final double w = thePixelSpaceWidth.doubleValue();
        final double h = thePixelSpaceHeight.doubleValue();
        if (Double.isFinite(w) && w >= 0.0 && Double.isFinite(h) && h >= 0.0) {

            if (theBorderWidth != null) {
                final double bw = theBorderWidth.doubleValue();
                if (!(Double.isFinite(bw) && bw >= 0.0)) {
                    throw new IllegalArgumentException("Border width must be finite and non-negative");
                }
            }

            this.pixelSpaceWidth = thePixelSpaceWidth;
            this.pixelSpaceHeight = thePixelSpaceHeight;
            this.borderWidth = theBorderWidth;
            this.graphSpaceBounds = theGraphSpaceBounds;
        } else {
            throw new IllegalArgumentException("Pixel space width and height must be finite and non-negative");
        }
    }

    /**
     * Constructs a new {@code CoordinateSystems} for a drawing with no graph space (pixel space and graph space are the
     * same).
     *
     * @param thePixelSpaceWidth  the width of the region in "pixel space"
     * @param thePixelSpaceHeight the height of the region in "pixel space"
     * @param theBorderWidth      the border width, in pixels (can be null if there is no border)
     */
    public CoordinateSystems(final Number thePixelSpaceWidth, final Number thePixelSpaceHeight,
                             final Number theBorderWidth) {

        if (thePixelSpaceWidth == null || thePixelSpaceHeight == null) {
            throw new IllegalArgumentException("Pixel space dimensions may not be null");
        }

        final double w = thePixelSpaceWidth.doubleValue();
        final double h = thePixelSpaceHeight.doubleValue();
        if (Double.isFinite(w) && w >= 0.0 && Double.isFinite(h) && h >= 0.0) {

            final double bw;
            if (theBorderWidth == null) {
                bw = 0.0;
            } else {
                bw = theBorderWidth.doubleValue();
                if (!(Double.isFinite(bw) && bw >= 0.0)) {
                    throw new IllegalArgumentException("Border width must be finite and non-negative");
                }
            }

            this.pixelSpaceWidth = thePixelSpaceWidth;
            this.pixelSpaceHeight = thePixelSpaceHeight;
            this.borderWidth = theBorderWidth;

            final double insideWidth = w - bw * 2.0;
            final double insideHeight = h - bw * 2.0;

            final Double leftTop = Double.valueOf(bw);
            final Double right = Double.valueOf(w - bw);
            final Double bottom = Double.valueOf(h - bw);

            this.graphSpaceBounds = new NumberBounds(leftTop, right, bottom, leftTop);
        } else {
            throw new IllegalArgumentException("Pixel space width and height must be finite and non-negative");
        }
    }

    /**
     * Gets the width of the region in "pixel space".
     *
     * @return the width, in pixels (never null)
     */
    public Number getPixelSpaceWidth() {

        return this.pixelSpaceWidth;
    }

    /**
     * Gets the height of the region in "pixel space".
     *
     * @return the height, in pixels (never null)
     */
    public Number getPixelSpaceHeight() {

        return this.pixelSpaceHeight;
    }

    /**
     * Gets the border width, in "pixel space".
     *
     * @return the border width, in pixels (can be null if there is no border)
     */
    public Number getBorderWidth() {

        return this.borderWidth;
    }

    /**
     * Gets the bounds of the graph in "graph space".
     *
     * @return the graph space bounds
     */
    public NumberBounds getGraphSpaceBounds() {

        return this.graphSpaceBounds;
    }

    /**
     * Calculates the pixel-space X coordinate for a given graph-space X coordinate.
     *
     * @param graphSpaceX the graph-space X coordinate
     * @return the pixel-space X coordinate
     */
    public double graphXToPixelX(final double graphSpaceX) {

        final double border = this.borderWidth == null ? 0.0 : this.borderWidth.doubleValue();
        final double pixWidth = this.pixelSpaceWidth.doubleValue() - border * 2.0;

        final double result;

        if (pixWidth <= 0.0) {
            // Borders are so fat there is no remaining space - use the inside of the left border.
            result = border;
        } else {
            final double graphLeft = this.graphSpaceBounds.getLeftX().doubleValue();
            final double graphRight = this.graphSpaceBounds.getRightX().doubleValue();
            final double deltaX = graphRight - graphLeft;

            if (deltaX == 0.0) {
                // No change in x coordinate from left to right of graph - center everything
                result = border + pixWidth / 2.0;
            } else {
                result = border + pixWidth * (graphSpaceX - graphLeft) / deltaX;
            }
        }

        return result;
    }

    /**
     * Calculates the pixel-space Y coordinate for a given graph-space Y coordinate.
     *
     * @param graphSpaceY the graph-space Y coordinate
     * @return the pixel-space Y coordinate
     */
    public double graphYToPixelY(final double graphSpaceY) {

        final double border = this.borderWidth == null ? 0.0 : this.borderWidth.doubleValue();
        final double pixHeight = this.pixelSpaceHeight.doubleValue() - border * 2.0;

        final double result;

        if (pixHeight <= 0.0) {
            // Borders are so fat there is no remaining space - use the inside of the left border.
            result = border;
        } else {
            final double graphBottom = this.graphSpaceBounds.getBottomY().doubleValue();
            final double graphTop = this.graphSpaceBounds.getTopY().doubleValue();
            final double deltaY = graphBottom - graphTop;

            if (deltaY == 0.0) {
                // No change in y coordinate from bottom to top of graph - center everything
                result = border + pixHeight / 2.0;
            } else {
                result = border + pixHeight * (graphSpaceY - graphTop) / deltaY;
            }
        }

        return result;
    }

    /**
     * Calculates the pixel-space width for a given graph-space width.
     *
     * @param graphSpaceWidth the graph-space width (change in X coordinate in graph space)
     * @return the pixel-space width (change in X coordinate in pixel space)
     */
    public double graphWidthToPixelWidth(final double graphSpaceWidth) {

        final double border = this.borderWidth == null ? 0.0 : this.borderWidth.doubleValue();
        final double pixWidth = this.pixelSpaceWidth.doubleValue() - border * 2.0;

        final double result;

        if (pixWidth <= 0.0) {
            // Borders are so fat there is no remaining space, effective pixel width of everything is zero
            result = 0.0;
        } else {
            final double graphWidth =
                    this.graphSpaceBounds.getRightX().doubleValue() - this.graphSpaceBounds.getLeftX().doubleValue();

            result = graphSpaceWidth * pixWidth / graphWidth;
        }

        return result;
    }

    /**
     * Calculates the pixel-space height for a given graph-space height.  Note that for a typical graph, where positive
     * Y is upward, and where pixel Y coordinates increase downward, the pixel height and graph-space height will have
     * opposite sign.
     *
     * @param graphSpaceHeight the graph-space height (change in Y coordinate in graph space)
     * @return the pixel-space height (change in Y coordinate in pixel space)
     */
    public double graphHeightToPixelHeight(final double graphSpaceHeight) {

        final double border = this.borderWidth == null ? 0.0 : this.borderWidth.doubleValue();
        final double pixHeight = this.pixelSpaceHeight.doubleValue() - border * 2.0;

        final double result;

        if (pixHeight <= 0.0) {
            // Borders are so fat there is no remaining space, effective pixel height of everything is zero
            result = 0.0;
        } else {
            final double graphHeight =
                    this.graphSpaceBounds.getBottomY().doubleValue() - this.graphSpaceBounds.getTopY().doubleValue();

            result = graphSpaceHeight * pixHeight / graphHeight;
        }

        return result;
    }

    /**
     * Calculates the graph-space X coordinate for a given pixel-space X coordinate.
     *
     * @param pixelSpaceX the pixel-space X coordinate
     * @return the graph-space X coordinate
     */
    public double pixelXToGraphX(final double pixelSpaceX) {

        final double border = this.borderWidth == null ? 0.0 : this.borderWidth.doubleValue();
        final double pixWidth = this.pixelSpaceWidth.doubleValue() - border * 2.0;

        final double graphLeft = this.graphSpaceBounds.getLeftX().doubleValue();
        final double graphRight = this.graphSpaceBounds.getRightX().doubleValue();

        final double result;

        if (pixWidth <= 0.0) {
            // Borders are so fat there is no remaining space - use the center of graph coordinates.
            result = (graphLeft + graphRight) * 0.5;
        } else {
            final double fraction = (pixelSpaceX - border) / pixWidth;
            result = graphLeft + fraction * (graphRight - graphLeft);
        }

        return result;
    }

    /**
     * Calculates the graph-space Y coordinate for a given pixel-space Y coordinate.
     *
     * @param pixelSpaceY the pixel-space Y coordinate
     * @return the graph-space Y coordinate
     */
    public double pixelYToGraphY(final double pixelSpaceY) {

        final double border = this.borderWidth == null ? 0.0 : this.borderWidth.doubleValue();
        final double pixHeight = this.pixelSpaceHeight.doubleValue() - border * 2.0;

        final double graphBottom = this.graphSpaceBounds.getBottomY().doubleValue();
        final double graphTop = this.graphSpaceBounds.getTopY().doubleValue();

        final double result;

        if (pixHeight <= 0.0) {
            // Borders are so fat there is no remaining space - use the center of graph coordinates.
            result = (graphTop + graphBottom) * 0.5;
        } else {
            final double fraction = (pixelSpaceY - border) / pixHeight;
            result = graphTop + fraction * (graphBottom - graphTop);
        }

        return result;
    }

    /**
     * Generate a diagnostic {@code String} representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.borderWidth == null ?
                SimpleBuilder.concat("CoordinateSystems:width=", this.pixelSpaceWidth, ",height=",
                        this.pixelSpaceHeight, ",graphSpaceBounds=", this.graphSpaceBounds) :
                SimpleBuilder.concat("CoordinateSystems:width=", this.pixelSpaceWidth, ",height=",
                        this.pixelSpaceHeight, ",border=", this.borderWidth, ",graphSpaceBounds=",
                        this.graphSpaceBounds);
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.pixelSpaceWidth.hashCode() + this.pixelSpaceHeight.hashCode()
               + Objects.hashCode(this.borderWidth) + this.graphSpaceBounds.hashCode();
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
        } else if (obj instanceof final CoordinateSystems system) {
            equal = this.pixelSpaceWidth.equals(system.pixelSpaceWidth)
                    && this.pixelSpaceHeight.equals(system.pixelSpaceHeight)
                    && Objects.equals(this.borderWidth, system.borderWidth)
                    && this.graphSpaceBounds.equals(system.graphSpaceBounds);
        } else {
            equal = false;
        }

        return equal;
    }
}
