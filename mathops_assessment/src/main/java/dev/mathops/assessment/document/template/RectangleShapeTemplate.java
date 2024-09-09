package dev.mathops.assessment.document.template;

import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.EAttribute;
import dev.mathops.assessment.document.inst.RectangleShapeInst;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
final class RectangleShapeTemplate {

    /** The attributes that are present. */
    private final Map<EAttribute, NumberOrFormula> attributes;

    /**
     * Construct a new {@code RectangleShape}.
     */
    RectangleShapeTemplate() {

        this.attributes = new EnumMap<>(EAttribute.class);
    }

    /**
     * Gets the attributes map.
     *
     * @return the attributes map.
     */
    private Map<EAttribute, NumberOrFormula> getAttributes() {

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
    public void setBounds(final EAttribute x1Attribute, final NumberOrFormula x1Value,
                          final EAttribute x2Attribute, final NumberOrFormula x2Value,
                          final EAttribute y1Attribute, final NumberOrFormula y1Value,
                          final EAttribute y2Attribute, final NumberOrFormula y2Value) {

        if (x1Attribute == EAttribute.X || x1Attribute == EAttribute.X1 || x1Attribute == EAttribute.GX
            || x1Attribute == EAttribute.GX1) {
            if (x1Value == null) {
                throw new IllegalArgumentException("Value for X coordinate of first corner may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify X coordinate of first corner.");
        }

        if (x2Attribute == EAttribute.WIDTH || x2Attribute == EAttribute.X2 || x2Attribute == EAttribute.GWIDTH
            || x2Attribute == EAttribute.GX2) {
            if (x2Value == null) {
                throw new IllegalArgumentException("Value for X coordinate of second corner may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify X coordinate of second corner.");
        }

        if (y1Attribute == EAttribute.Y || y1Attribute == EAttribute.Y1 || y1Attribute == EAttribute.GY
            || y1Attribute == EAttribute.GY1) {
            if (y1Value == null) {
                throw new IllegalArgumentException("Value for Y coordinate of first corner may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify Y coordinate of first corner.");
        }

        if (y2Attribute == EAttribute.HEIGHT || y2Attribute == EAttribute.Y2 || y2Attribute == EAttribute.GHEIGHT
            || y2Attribute == EAttribute.GY2) {
            if (y2Value == null) {
                throw new IllegalArgumentException("Value for Y coordinate of second corner may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify Y coordinate of second corner.");
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
    public void setCenteredCircle(final EAttribute cxAttribute, final NumberOrFormula cxValue,
                                  final EAttribute cyAttribute, final NumberOrFormula cyValue,
                                  final EAttribute rAttribute, final NumberOrFormula rValue) {

        if (cxAttribute == EAttribute.CX || cxAttribute == EAttribute.GCX) {
            if (cxValue == null) {
                throw new IllegalArgumentException("Value for center X coordinate may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify center X coordinate.");
        }

        if (cyAttribute == EAttribute.CY || cyAttribute == EAttribute.GCY) {
            if (cyValue == null) {
                throw new IllegalArgumentException("Value for center Y coordinate may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify center Y coordinate.");
        }

        if (rAttribute == EAttribute.R || rAttribute == EAttribute.GR || rAttribute == EAttribute.WIDTH
            || rAttribute == EAttribute.GWIDTH || rAttribute == EAttribute.HEIGHT || rAttribute == EAttribute.GHEIGHT) {
            if (rValue == null) {
                throw new IllegalArgumentException("Value that defines radius may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify radius.");
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
    public void setCenteredEllipse(final EAttribute cxAttribute, final NumberOrFormula cxValue,
                                   final EAttribute cyAttribute, final NumberOrFormula cyValue,
                                   final EAttribute rxAttribute, final NumberOrFormula rxValue,
                                   final EAttribute ryAttribute, final NumberOrFormula ryValue) {

        if (cxAttribute == EAttribute.CX || cxAttribute == EAttribute.GCX) {
            if (cxValue == null) {
                throw new IllegalArgumentException("Value for center X coordinate may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify center X coordinate.");
        }

        if (cyAttribute == EAttribute.CY || cyAttribute == EAttribute.GCY) {
            if (cyValue == null) {
                throw new IllegalArgumentException("Value for center Y coordinate may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify center Y coordinate.");
        }

        if (rxAttribute == EAttribute.RX || rxAttribute == EAttribute.GRX || rxAttribute == EAttribute.WIDTH
            || rxAttribute == EAttribute.GWIDTH) {
            if (rxValue == null) {
                throw new IllegalArgumentException("Value that defines x-axis radius may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify x-axis radius.");
        }

        if (ryAttribute == EAttribute.RY || ryAttribute == EAttribute.GRY || ryAttribute == EAttribute.HEIGHT
            || ryAttribute == EAttribute.GHEIGHT) {
            if (ryValue == null) {
                throw new IllegalArgumentException("Value that defines y-axis radius may not be null.");
            }
        } else {
            throw new IllegalArgumentException("Unsupported attribute to specify y-axis radius.");
        }

        this.attributes.clear();
        this.attributes.put(cxAttribute, cxValue);
        this.attributes.put(cyAttribute, cyValue);
        this.attributes.put(rxAttribute, rxValue);
        this.attributes.put(ryAttribute, ryValue);
    }

    /**
     * Construct a copy of this object with a new owner.
     *
     * @return the copy
     */
    public RectangleShapeTemplate deepCopy() {

        final RectangleShapeTemplate copy = new RectangleShapeTemplate();

        copy.getAttributes().putAll(this.attributes);

        return copy;
    }

    /**
     * Generates an instance of this object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance primitive object; null if unable to create the instance
     */
    public RectangleShapeInst createInstance(final EvalContext evalContext) {

        RectangleShapeInst result = null;

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

        if (Objects.nonNull(x1) && Objects.nonNull(x2) && Objects.nonNull(y1) && Objects.nonNull(y2)) {

            final NumberOrFormula x1Spec = this.attributes.get(x1);
            final NumberOrFormula x2Spec = this.attributes.get(x2);
            final NumberOrFormula y1Spec = this.attributes.get(y1);
            final NumberOrFormula y2Spec = this.attributes.get(y2);

            final Object x1Val = x1Spec.evaluate(evalContext);
            final Object x2Val = x2Spec.evaluate(evalContext);
            final Object y1Val = y1Spec.evaluate(evalContext);
            final Object y2Val = y2Spec.evaluate(evalContext);

            if (x1Val instanceof final Number x1Nbr && x2Val instanceof final Number x2Nbr
                && y1Val instanceof final Number y1Nbr && y2Val instanceof final Number y2Nbr) {

                result = new RectangleShapeInst();
                result.setBounds(x1, x1Nbr.doubleValue(), x2, x2Nbr.doubleValue(), y1, y1Nbr.doubleValue(),
                        y2, y2Nbr.doubleValue());
            }
        } else {
            EAttribute cx = null;
            if (this.attributes.containsKey(EAttribute.CX)) {
                cx = EAttribute.CX;
            } else if (this.attributes.containsKey(EAttribute.GCX)) {
                cx = EAttribute.GCX;
            }

            EAttribute cy = null;
            if (this.attributes.containsKey(EAttribute.CY)) {
                cy = EAttribute.CY;
            } else if (this.attributes.containsKey(EAttribute.GCY)) {
                cy = EAttribute.GCY;
            }

            if (Objects.nonNull(cx) && Objects.nonNull(cy)) {

                final NumberOrFormula cxSpec = this.attributes.get(cx);
                final NumberOrFormula cySpec = this.attributes.get(cy);

                final Object cxVal = cxSpec.evaluate(evalContext);
                final Object cyVal = cySpec.evaluate(evalContext);

                if (cxVal instanceof final Number cxNbr && cyVal instanceof final Number cyNbr) {

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

                    if (Objects.nonNull(r)) {
                        final NumberOrFormula rSpec = this.attributes.get(r);
                        final Object rVal = rSpec.evaluate(evalContext);

                        if (rVal instanceof final Number rNbr) {

                            result = new RectangleShapeInst();
                            result.setCenteredCircle(cx, cxNbr.doubleValue(), cy, cyNbr.doubleValue(),
                                    r, rNbr.doubleValue());
                        } else {
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

                            if (Objects.nonNull(rx) && Objects.nonNull(ry)) {

                                final NumberOrFormula rxSpec = this.attributes.get(rx);
                                final NumberOrFormula rySpec = this.attributes.get(ry);

                                final Object rxVal = rxSpec.evaluate(evalContext);
                                final Object ryVal = rySpec.evaluate(evalContext);

                                if (rxVal instanceof final Number rxNbr && ryVal instanceof final Number ryNbr) {

                                    result = new RectangleShapeInst();
                                    result.setCenteredEllipse(cx, cxNbr.doubleValue(), cy, cyNbr.doubleValue(),
                                            rx, rxNbr.doubleValue(), ry, ryNbr.doubleValue());
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Appends XML attributes that represent this object's attribute values.  This should be called after the open tag
     * has been stated, such as by emitting ("&lt;rectangle").
     *
     * @param xml the {@code HtmlBuilder} to which to write the XML
     */
    public void addAttributes(final HtmlBuilder xml) {

        for (final Map.Entry<EAttribute, NumberOrFormula> entry : this.attributes.entrySet()) {
            final NumberOrFormula value = entry.getValue();

            final Number number = value.getNumber();
            if (number != null) {
                final String name = entry.getKey().label;
                xml.add(CoreConstants.SPC, name, "=\"", number, CoreConstants.QUOTE);
            }
        }
    }

    /**
     * Appends elements that represent this object's expression-based values.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the indentation level
     */
    public void addChildElements(final HtmlBuilder xml, final int indent) {

        for (final Map.Entry<EAttribute, NumberOrFormula> entry : this.attributes.entrySet()) {
            final NumberOrFormula value = entry.getValue();

            final Formula formula = value.getFormula();
            if (formula != null) {
                final String tag = entry.getKey().label;
                xml.openElement(indent, tag);
                xml.endOpenElement(false);
                formula.appendChildrenXml(xml);
                xml.endNonempty(indent, tag, true);
            }
        }
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "RectangleShapeTemplate";
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    public void accumulateParameterNames(final Collection<String> set) { // Do NOT change to "? super String"

        for (final Map.Entry<EAttribute, NumberOrFormula> entry : this.attributes.entrySet()) {
            final NumberOrFormula value = entry.getValue();
            final Formula formula = value.getFormula();

            if (formula != null) {
                final Set<String> formulaKeys = formula.params.keySet();
                set.addAll(formulaKeys);
            }
        }
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
        } else if (obj instanceof final RectangleShapeTemplate rect) {
            final Map<EAttribute, NumberOrFormula> rectAttributes = rect.getAttributes();
            equal = this.attributes.equals(rectAttributes);
        } else {
            equal = false;
        }

        return equal;
    }
}
