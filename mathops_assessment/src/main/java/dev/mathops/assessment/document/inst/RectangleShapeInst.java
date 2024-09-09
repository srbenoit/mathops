package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EAttribute;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.EnumMap;
import java.util.Map;

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
    private final Map<EAttribute, Double> attributes;

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
    private Map<EAttribute, Double> getAttributes() {

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
    public void setBounds(final EAttribute x1Attribute, final double x1Value,
                          final EAttribute x2Attribute, final double x2Value,
                          final EAttribute y1Attribute, final double y1Value,
                          final EAttribute y2Attribute, final double y2Value) {

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

        final Double x1Obj = Double.valueOf(x1Value);
        final Double x2Obj = Double.valueOf(x2Value);
        final Double y1Obj = Double.valueOf(y1Value);
        final Double y2Obj = Double.valueOf(y2Value);

        this.attributes.clear();
        this.attributes.put(x1Attribute, x1Obj);
        this.attributes.put(x2Attribute, x2Obj);
        this.attributes.put(y1Attribute, y1Obj);
        this.attributes.put(y2Attribute, y2Obj);
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
    public void setCenteredCircle(final EAttribute cxAttribute, final double cxValue,
                                  final EAttribute cyAttribute, final double cyValue,
                                  final EAttribute rAttribute, final double rValue) {

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

        final Double cxObj = Double.valueOf(cxValue);
        final Double cyObj = Double.valueOf(cyValue);
        final Double rObj = Double.valueOf(rValue);

        this.attributes.clear();
        this.attributes.put(cxAttribute, cxObj);
        this.attributes.put(cyAttribute, cyObj);
        this.attributes.put(rAttribute, rObj);
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
    public void setCenteredEllipse(final EAttribute cxAttribute, final double cxValue,
                                   final EAttribute cyAttribute, final double cyValue,
                                   final EAttribute rxAttribute, final double rxValue,
                                   final EAttribute ryAttribute, final double ryValue) {

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

        final Double csObj = Double.valueOf(cxValue);
        final Double cyObj = Double.valueOf(cyValue);
        final Double rxObj = Double.valueOf(rxValue);
        final Double ryObj = Double.valueOf(ryValue);

        this.attributes.clear();
        this.attributes.put(cxAttribute, csObj);
        this.attributes.put(cyAttribute, cyObj);
        this.attributes.put(rxAttribute, rxObj);
        this.attributes.put(ryAttribute, ryObj);
    }

    /**
     * Appends XML attributes that represent this object's attribute values.  This should be called after the open tag
     * has been stated, such as by emitting ("&lt;rectangle").
     *
     * @param xml the {@code HtmlBuilder} to which to write the XML
     */
    public void addAttributes(final HtmlBuilder xml) {

        for (final Map.Entry<EAttribute, Double> entry : this.attributes.entrySet()) {
            final String name = entry.getKey().label;
            final Double value = entry.getValue();

            xml.add(CoreConstants.SPC, name, "=\"", value, CoreConstants.QUOTE);
        }
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
            final Map<EAttribute, Double> rectAttributes = rect.getAttributes();
            equal = this.attributes.equals(rectAttributes);
        } else {
            equal = false;
        }

        return equal;
    }
}
