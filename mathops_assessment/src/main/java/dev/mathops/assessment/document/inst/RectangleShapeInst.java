package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.CoordinateSystems;
import dev.mathops.assessment.document.EAttribute;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * A container for the various sets of attributes and/or expressions that can determine a rectangular shape for a
 * primitive.  A rectangle is defined by two opposing corners as points.  This is used to position the following
 * primitives:
 * <ul>
 *     <li>Line (the line connects the two opposing corners)</li>
 *     <li>Rectangle</li>
 *     <li>Oval (that inscribed in the bounding rectangle)</li>
 *     <li>Arc (a portion of the Oval, with optional decorations)</li>
 *     <li>Raster (the raster is placed in the rectangle)</li>
 *     <li>Protractor (within the oval, scaled to maintain aspect ratio)</li>
 *     <li>Ruler (positioned the same way as a Line)</li>
 * </ul>
 *
 * <p>
 * There are several attributes that contribute to rectangle position.  The attributes (or corresponding child
 * expression elements) are described here.  The allowed combinations that can specify a rectangle are described
 * below.
 * <ul>
 *     <li>'x', 'cx', 'x1', and 'x2' represent x coordinates in "pixel space", where the left edge of the graphic is
 *         zero.</li>
 *     <li>'gx', 'gcx', 'gx1', amd 'gx2' represent x coordinates in "graph space", or the x positions of that
 *         coordinate on the x-axis.</li>
 *     <li>'y', 'cy', 'y1', and 'y2' represent a y coordinate in "pixel space", where the top edge of the graphic is
 *         zero.</li>
 *     <li>'gy', 'gcy', 'gy1', and 'gy2' represent y coordinates in "graph space", or the y position of that
 *         coordinate on the y-axis.</li>
 *     <li>'width' represents an x distance in "pixel space".</li>
 *     <li>'gwidth' represents an x distance in "graph space".</li>
 *     <li>'height' represents a y distance in "pixel space".</li>
 *     <li>'gheight' represents a y distance in "graph space".</li>
 *     <li>'r' represents the radius of a circle in "pixel space".</li>
 *     <li>'gr' represents the radius of a circle in "graph space" (which may not appears as a circle if the graph's
 *          axes have different scales).</li>
 *     <li>'rx' and 'ry' represents the x- and y-radii of an axis-aligned ellipse in "pixel space".</li>
 *     <li>'grx' and 'gry' represents the x- and y-radii of an axis-aligned ellipse in "graph space".</li>
 * </ul>
 *
 * <p>
 * A rectangle can be specified by any (but only one) of the following combinations (where an attribute with constant
 * value or an expression child element are considered the same for this purpose).
 * <ul>
 *     <li>Two points that define opposite corners, with all of the following:
 *     <ul>
 *         <li>'x' or 'x1' or 'gx' or 'gx1' (the x coordinate of the first corner)</li>
 *         <li>'y' or 'y1' or 'gy' or 'gy1' (the y coordinate of the first corner)</li>
 *         <li>'width' or 'x2' or 'gwidth' or 'gx2' (the width or the x coordinate of the second corner)</li>
 *         <li>'height' or 'y2' or 'gheight' or 'gy2' (the height or the y coordinate of the second corner)</li>
 *     </ul></li>
 *     <li>A center point and circle radii, with all of the following:
 *     <ul>
 *         <li>'cx' or 'gcx' (the x coordinate of the center)</li>
 *         <li>'cy' or 'gcy' (the y coordinate of the center)</li>
 *         <li>'r' or 'gr' (the radius) or 'width' or 'gwidth' or 'height' or 'gheight' (the diameter)</li>
 *     </ul></li>
 *     <li>A center point and axis-aligned ellipse radii, with all of the following:
 *     <ul>
 *         <li>'cx' or 'gcx' (the x coordinate of the center)</li>
 *         <li>'cy' or 'gcy' (the y coordinate of the center)</li>
 *         <li>'rx' or 'grx' (the radius in the x direction) or 'width' or 'gwidth' (the x diameter)</li>
 *         <li>'ry' or 'gry' (the radius in the y direction) or 'height' or 'gheight' (the y diameter)</li>
 *     </ul></li>
 * </ul>
 */
public final class RectangleShapeInst {

    /** The attributes that are present. */
    private final Map<EAttribute, Number> attributes;

    /**
     * Construct a new {@code RectangleShapeInst}.
     */
    public RectangleShapeInst() {

        this.attributes = new EnumMap<>(EAttribute.class);
    }

    /**
     * Gets the attributes map.
     *
     * @return the attributes map.
     */
    private Map<EAttribute, Number> getAttributes() {

        return this.attributes;
    }

    /**
     * Sets the bounds.
     *
     * @param x1Attribute the attribute used to specify the first corner's x coordinate (X, X1, GX, or GX1)
     * @param x1Value     the value of the x1 attribute
     * @param x2Attribute the attribute used to specify the second corner's x coordinate (WIDTH, X2, GWID TH, or GX2)
     * @param x2Value     the value of the x2 attribute
     * @param y1Attribute the attribute used to specify the first corner's Y coordinate (Y, Y1, GY, or GY1)
     * @param y1Value     the value of the y1 attribute
     * @param y2Attribute the attribute used to specify the second corner's y coordinate (HEIGHT, Y2, GHEIGHT, or GY2)
     * @param y2Value     the value of the y2 attribute
     */
    public void setBounds(final EAttribute x1Attribute, final Number x1Value,
                          final EAttribute x2Attribute, final Number x2Value,
                          final EAttribute y1Attribute, final Number y1Value,
                          final EAttribute y2Attribute, final Number y2Value) {

        if (x1Attribute == EAttribute.X || x1Attribute == EAttribute.X1 || x1Attribute == EAttribute.GX
            || x1Attribute == EAttribute.GX1) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify X coordinate of first corner.");
        }

        if (x2Attribute == EAttribute.WIDTH || x2Attribute == EAttribute.X2 || x2Attribute == EAttribute.GWIDTH
            || x2Attribute == EAttribute.GX2) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify X coordinate of second corner.");
        }

        if (y1Attribute == EAttribute.Y || y1Attribute == EAttribute.Y1 || y1Attribute == EAttribute.GY
            || y1Attribute == EAttribute.GY1) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify Y coordinate of first corner.");
        }

        if (y2Attribute == EAttribute.HEIGHT || y2Attribute == EAttribute.Y2 || y2Attribute == EAttribute.GHEIGHT
            || y2Attribute == EAttribute.GY2) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify Y coordinate of second corner.");
        }

        if (x1Value == null || x2Value == null || y1Value == null || y2Value == null) {
            throw new IllegalArgumentException("Values may not be null");
        }

        this.attributes.clear();
        this.attributes.put(x1Attribute, x1Value);
        this.attributes.put(x2Attribute, x2Value);
        this.attributes.put(y1Attribute, y1Value);
        this.attributes.put(y2Attribute, y2Value);
    }

    /**
     * Sets the shape based on a centered circle.
     *
     * @param cxAttribute the attribute used to specify the center x coordinate (X, X1, GX, or GX1)
     * @param cxValue     the value of the cx attribute
     * @param cyAttribute the attribute used to specify the center Y coordinate (Y, Y1, GY, or GY1)
     * @param cyValue     the value of the cy attribute
     * @param rAttribute  the attribute used to specify the radius (R, GR, WIDTH, GWIDTH, HEIGHT, or GHEIGHT)
     * @param rValue      the value of the r attribute
     */
    public void setCenteredCircle(final EAttribute cxAttribute, final Number cxValue,
                                  final EAttribute cyAttribute, final Number cyValue,
                                  final EAttribute rAttribute, final Number rValue) {

        if (cxAttribute == EAttribute.X || cxAttribute == EAttribute.X1 || cxAttribute == EAttribute.GX
            || cxAttribute == EAttribute.GX1) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify center X coordinate.");
        }

        if (cyAttribute == EAttribute.Y || cyAttribute == EAttribute.Y1 || cyAttribute == EAttribute.GY
            || cyAttribute == EAttribute.GY1) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify center Y coordinate.");
        }

        if (rAttribute == EAttribute.R || rAttribute == EAttribute.GR || rAttribute == EAttribute.WIDTH
            || rAttribute == EAttribute.GWIDTH || rAttribute == EAttribute.HEIGHT || rAttribute == EAttribute.GHEIGHT) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify radius.");
        }

        if (cxValue == null || cyValue == null || rValue == null) {
            throw new IllegalArgumentException("Values may not be null");
        }

        this.attributes.clear();
        this.attributes.put(cxAttribute, cxValue);
        this.attributes.put(cyAttribute, cyValue);
        this.attributes.put(rAttribute, rValue);
    }

    /**
     * Sets the shape based on a centered ellipse.
     *
     * @param cxAttribute the attribute used to specify the center x coordinate (X, X1, GX, or GX1)
     * @param cxValue     the value of the cx attribute
     * @param cyAttribute the attribute used to specify the center Y coordinate (Y, Y1, GY, or GY1)
     * @param cyValue     the value of the cy attribute
     * @param rxAttribute the attribute used to specify the x-axis radius (RX, GRX, WIDTH, or GWIDTH)
     * @param rxValue     the value of the rx attribute
     * @param ryAttribute the attribute used to specify the y-axis radius (RY, GRY, HEIGHT, or GHEIGHT)
     * @param ryValue     the value of the ry attribute
     */
    public void setCenteredEllipse(final EAttribute cxAttribute, final Number cxValue,
                                   final EAttribute cyAttribute, final Number cyValue,
                                   final EAttribute rxAttribute, final Number rxValue,
                                   final EAttribute ryAttribute, final Number ryValue) {

        if (cxAttribute == EAttribute.X || cxAttribute == EAttribute.X1 || cxAttribute == EAttribute.GX
            || cxAttribute == EAttribute.GX1) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify center X coordinate.");
        }

        if (cyAttribute == EAttribute.Y || cyAttribute == EAttribute.Y1 || cyAttribute == EAttribute.GY
            || cyAttribute == EAttribute.GY1) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify center Y coordinate.");
        }

        if (rxAttribute == EAttribute.RX || rxAttribute == EAttribute.GRX || rxAttribute == EAttribute.WIDTH
            || rxAttribute == EAttribute.GWIDTH) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify x-axis radius.");
        }

        if (ryAttribute == EAttribute.RY || ryAttribute == EAttribute.GRY || ryAttribute == EAttribute.HEIGHT
            || ryAttribute == EAttribute.GHEIGHT) {
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify y-axis radius.");
        }

        if (cxValue == null || cyValue == null || rxValue == null || ryValue == null) {
            throw new IllegalArgumentException("Values may not be null");
        }

        this.attributes.clear();
        this.attributes.put(cxAttribute, cxValue);
        this.attributes.put(cyAttribute, cyValue);
        this.attributes.put(rxAttribute, rxValue);
        this.attributes.put(ryAttribute, ryValue);
    }

    /**
     * Appends XML attributes that represent this object's attribute values.  This should be called after the open tag
     * has been stated, such as by emitting ("&lt;rectangle").
     *
     * @param xml the {@code HtmlBuilder} to which to write the XML
     */
    public void addAttributes(final HtmlBuilder xml) {

        for (final Map.Entry<EAttribute, Number> entry : this.attributes.entrySet()) {
            final String name = entry.getKey().label;
            final Number value = entry.getValue();

            xml.add(CoreConstants.SPC, name, "=\"", value, CoreConstants.QUOTE);
        }
    }

    /**
     * Determines the bounding rectangle (in pixel space) from attribute settings.
     *
     * @param coordinateSystems the coordinate systems to use when resolving "graph space" coordinates
     * @return the bounding rectangle in pixel space; {@code null} if that rectangle could not be determined
     */
    public Rectangle2D getBoundsRect(final CoordinateSystems coordinateSystems) {

        Rectangle2D result = null;

        final EAttribute x1 = findX1Attribute();
        final EAttribute x2 = findX2Attribute();
        final EAttribute y1 = findY1Attribute();
        final EAttribute y2 = findY2Attribute();

        if (Objects.nonNull(x1) && Objects.nonNull(x2) && Objects.nonNull(y1) && Objects.nonNull(y2)) {

            // This object is specified by defining two opposite corners

            final double x1Dbl = this.attributes.get(x1).doubleValue();
            final double x2Dbl = this.attributes.get(x2).doubleValue();
            final double y1Dbl = this.attributes.get(y1).doubleValue();
            final double y2Dbl = this.attributes.get(y2).doubleValue();

            // interpret and compute bounding rect
            final double p1X = (x1 == EAttribute.X || x1 == EAttribute.X1) ? x1Dbl :
                    coordinateSystems.graphXToPixelX(x1Dbl);

            final double p2X = (x2 == EAttribute.WIDTH) ? p1X + x2Dbl : (x2 == EAttribute.X2) ? x2Dbl :
                    (x2 == EAttribute.GX2) ? coordinateSystems.graphXToPixelX(x2Dbl) :
                            p1X + coordinateSystems.graphWidthToPixelWidth(x2Dbl);

            final double p1Y = (y1 == EAttribute.Y || y1 == EAttribute.Y1) ? y1Dbl :
                    coordinateSystems.graphYToPixelY(y1Dbl);

            final double p2Y = (y2 == EAttribute.HEIGHT) ? p1Y + y2Dbl : (y2 == EAttribute.Y2) ? y2Dbl :
                    (y2 == EAttribute.GY2) ? coordinateSystems.graphYToPixelY(y2Dbl) :
                            p1Y + coordinateSystems.graphHeightToPixelHeight(y2Dbl);

            result = new Rectangle2D.Double(p1X, p1Y, p2X - p1X, p2Y - p1Y);
        } else {
            final EAttribute cx = findCXAttribute();
            final EAttribute cy = findCYAttribute();

            if (Objects.nonNull(cx) && Objects.nonNull(cy)) {

                final double cxDbl = this.attributes.get(cx).doubleValue();
                final double cyDbl = this.attributes.get(cy).doubleValue();

                final double centerX = cx == EAttribute.CX ? cxDbl : coordinateSystems.graphXToPixelX(cxDbl);
                final double centerY = cy == EAttribute.CY ? cyDbl : coordinateSystems.graphXToPixelX(cyDbl);

                final EAttribute rx = findRXAttribute();
                final EAttribute ry = findRYAttribute();

                if (Objects.nonNull(rx) && Objects.nonNull(ry)) {

                    final double rxDbl = this.attributes.get(rx).doubleValue();
                    final double ryDbl = this.attributes.get(ry).doubleValue();

                    // This object is specified by center and two radii

                    final double pRx = (rx == EAttribute.RX) ? rxDbl : (rx == EAttribute.WIDTH) ? rxDbl * 0.5 :
                            (rx == EAttribute.GRX) ? coordinateSystems.graphXToPixelX(rxDbl) :
                                    coordinateSystems.graphWidthToPixelWidth(rxDbl);

                    final double pRy = (ry == EAttribute.RY) ? ryDbl : (ry == EAttribute.HEIGHT) ? ryDbl * 0.5 :
                            (ry == EAttribute.GRY) ? coordinateSystems.graphXToPixelX(ryDbl) :
                                    coordinateSystems.graphHeightToPixelHeight(ryDbl);

                    result = new Rectangle2D.Double(centerX - pRx, centerY - pRy, pRx * 2.0, pRy * 2.0);
                } else {
                    final EAttribute r = findRAttribute();

                    if (Objects.nonNull(r)) {

                        final double rDbl = this.attributes.get(r).doubleValue();

                        // This object is specified by center and a single radius

                        // Compute Rx and Ry separately since graph scale in each dimension might be different
                        final double pRx;
                        final double pRy;

                        if (r == EAttribute.R) {
                            pRx = rDbl;
                            pRy = rDbl;
                        } else if (r == EAttribute.GR) {
                            pRx = coordinateSystems.graphWidthToPixelWidth(rDbl);
                            pRy = coordinateSystems.graphHeightToPixelHeight(rDbl);
                        } else if (r == EAttribute.WIDTH || r == EAttribute.HEIGHT) {
                            pRx = rDbl * 0.5;
                            pRy = pRx;
                        } else {
                            pRx = coordinateSystems.graphWidthToPixelWidth(rDbl) * 0.5;
                            pRy = coordinateSystems.graphHeightToPixelHeight(rDbl) * 0.5;
                        }

                        result = new Rectangle2D.Double(centerX - pRx, centerY - pRy, pRx * 2.0, pRy * 2.0);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Searches for an attribute whose value is present and that can define the "x1" corner (this can be the "X", "X1",
     * "GX", or "GX1" attribute).
     *
     * @return the attribute, if one was found; null if not
     */
    private EAttribute findX1Attribute() {

        EAttribute x1 = null;

        if (this.attributes.containsKey(EAttribute.X)) {
            x1 = EAttribute.X;
        } else if (this.attributes.containsKey(EAttribute.X1)) {
            x1 = EAttribute.X1;
        } else if (this.attributes.containsKey(EAttribute.GX)) {
            x1 = EAttribute.GX;
        } else if (this.attributes.containsKey(EAttribute.GX1)) {
            x1 = EAttribute.GX1;
        }

        return x1;
    }

    /**
     * Searches for an attribute whose value is present and that can define the "x2" corner (this can be the "WIDTH",
     * "X2", "GWIDTH", or "GX2" attribute).
     *
     * @return the attribute, if one was found; null if not
     */
    private EAttribute findX2Attribute() {

        EAttribute x2 = null;

        if (this.attributes.containsKey(EAttribute.WIDTH)) {
            x2 = EAttribute.WIDTH;
        } else if (this.attributes.containsKey(EAttribute.X2)) {
            x2 = EAttribute.X2;
        } else if (this.attributes.containsKey(EAttribute.GWIDTH)) {
            x2 = EAttribute.GWIDTH;
        } else if (this.attributes.containsKey(EAttribute.GX2)) {
            x2 = EAttribute.GX2;
        }

        return x2;
    }

    /**
     * Searches for an attribute whose value is present and that can define the "y1" corner (this can be the "Y", "Y1",
     * "GY", or "GY1" attribute).
     *
     * @return the attribute, if one was found; null if not
     */
    private EAttribute findY1Attribute() {

        EAttribute y1 = null;

        if (this.attributes.containsKey(EAttribute.Y)) {
            y1 = EAttribute.Y;
        } else if (this.attributes.containsKey(EAttribute.Y1)) {
            y1 = EAttribute.Y1;
        } else if (this.attributes.containsKey(EAttribute.GY)) {
            y1 = EAttribute.GY;
        } else if (this.attributes.containsKey(EAttribute.GY1)) {
            y1 = EAttribute.GY1;
        }

        return y1;
    }

    /**
     * Searches for an attribute whose value is present and that can define the "y2" corner (this can be the "HEIGHT",
     * "Y2", "GHEIGHT", or "GY2" attribute).
     *
     * @return the attribute, if one was found; null if not
     */
    private EAttribute findY2Attribute() {

        EAttribute y2 = null;

        if (this.attributes.containsKey(EAttribute.HEIGHT)) {
            y2 = EAttribute.HEIGHT;
        } else if (this.attributes.containsKey(EAttribute.Y2)) {
            y2 = EAttribute.Y2;
        } else if (this.attributes.containsKey(EAttribute.GHEIGHT)) {
            y2 = EAttribute.GHEIGHT;
        } else if (this.attributes.containsKey(EAttribute.GY2)) {
            y2 = EAttribute.GY2;
        }

        return y2;
    }

    /**
     * Searches for an attribute whose value is present and that can define the X coordinate of the center of the shape
     * (this can be the "CX" or "GCX" attribute).
     *
     * @return the attribute, if one was found; null if not
     */
    private EAttribute findCXAttribute() {

        EAttribute cx = null;

        if (this.attributes.containsKey(EAttribute.CX)) {
            cx = EAttribute.CX;
        } else if (this.attributes.containsKey(EAttribute.GCX)) {
            cx = EAttribute.GCX;
        }

        return cx;
    }

    /**
     * Searches for an attribute whose value is present and that can define the Y coordinate of the center of the shape
     * (this can be the "CY" or "GCY" attribute).
     *
     * @return the attribute, if one was found; null if not
     */
    private EAttribute findCYAttribute() {

        EAttribute cy = null;

        if (this.attributes.containsKey(EAttribute.CY)) {
            cy = EAttribute.CY;
        } else if (this.attributes.containsKey(EAttribute.GCY)) {
            cy = EAttribute.GCY;
        }

        return cy;
    }

    /**
     * Searches for an attribute whose value is present and that can define the constant radius of the shape (this can
     * be the "R", "GR", "WIDTH", "GWIDTH", "HEIGHT", or "GHEIGHT" attribute).
     *
     * @return the attribute, if one was found; null if not
     */
    private EAttribute findRAttribute() {

        EAttribute r = null;

        if (this.attributes.containsKey(EAttribute.R)) {
            r = EAttribute.R;
        } else if (this.attributes.containsKey(EAttribute.GR)) {
            r = EAttribute.GR;
        } else if (this.attributes.containsKey(EAttribute.WIDTH)) {
            r = EAttribute.WIDTH;
        } else if (this.attributes.containsKey(EAttribute.GWIDTH)) {
            r = EAttribute.GWIDTH;
        } else if (this.attributes.containsKey(EAttribute.HEIGHT)) {
            r = EAttribute.HEIGHT;
        } else if (this.attributes.containsKey(EAttribute.GHEIGHT)) {
            r = EAttribute.GHEIGHT;
        }

        return r;
    }

    /**
     * Searches for an attribute whose value is present and that can define the x-axis radius of the shape (this can be
     * the "RX", "GRX", "WIDTH", or "GWIDTH" attribute).
     *
     * @return the attribute, if one was found; null if not
     */
    private EAttribute findRXAttribute() {

        EAttribute rx = null;

        if (this.attributes.containsKey(EAttribute.RX)) {
            rx = EAttribute.RX;
        } else if (this.attributes.containsKey(EAttribute.GRX)) {
            rx = EAttribute.GRX;
        } else if (this.attributes.containsKey(EAttribute.WIDTH)) {
            rx = EAttribute.WIDTH;
        } else if (this.attributes.containsKey(EAttribute.GWIDTH)) {
            rx = EAttribute.GWIDTH;
        }

        return rx;
    }

    /**
     * Searches for an attribute whose value is present and that can define the y-axis radius of the shape (this can be
     * the "RY", "GRY", "HEIGHT", or "GHEIGHT" attribute).
     *
     * @return the attribute, if one was found; null if not
     */
    private EAttribute findRYAttribute() {

        EAttribute ry = null;

        if (this.attributes.containsKey(EAttribute.RY)) {
            ry = EAttribute.RY;
        } else if (this.attributes.containsKey(EAttribute.GRY)) {
            ry = EAttribute.GRY;
        } else if (this.attributes.containsKey(EAttribute.HEIGHT)) {
            ry = EAttribute.HEIGHT;
        } else if (this.attributes.containsKey(EAttribute.GHEIGHT)) {
            ry = EAttribute.GHEIGHT;
        }

        return ry;
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "RectangleShapeInst";
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return this.attributes.hashCode();
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final RectangleShapeInst rect) {
            final Map<EAttribute, Number> rectAttributes = rect.getAttributes();
            equal = this.attributes.equals(rectAttributes);
        } else {
            equal = false;
        }

        return equal;
    }
}
