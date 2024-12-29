package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.CoordinateSystems;
import dev.mathops.assessment.document.EAttribute;
import dev.mathops.assessment.document.inst.RectangleShapeInst;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.XmlFormulaFactory;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.xml.IElement;
import dev.mathops.text.parser.xml.NonemptyElement;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
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
     * Extracts attributes and/or child elements from an XML element and attempts to construct a
     * {@code RectangleShapeTemplate}.  If this is successful, the constructed {@code RectangleShapeTemplate} is stored
     * in a provided primitive.
     *
     * @param evalContext the evaluation context
     * @param e           the element
     * @param primitive   the primitive to which to add the extracted shape
     * @param mode        the parser mode
     * @return true if successful; false if any error occurred
     */
    static boolean canExtract(final EvalContext evalContext, final IElement e,
                              final AbstractDocRectangleShape primitive, final EParserMode mode) {

        final List<IElement> children =
                e instanceof final NonemptyElement nonempty ? nonempty.getElementChildrenAsList() : null;

        final NumberOrFormula x = extractNumberOrFormula(evalContext, e, children, EAttribute.X, mode);
        final NumberOrFormula y = extractNumberOrFormula(evalContext, e, children, EAttribute.Y, mode);

        final NumberOrFormula x1 = extractNumberOrFormula(evalContext, e, children, EAttribute.X1, mode);
        final NumberOrFormula y1 = extractNumberOrFormula(evalContext, e, children, EAttribute.Y1, mode);

        final NumberOrFormula gx = extractNumberOrFormula(evalContext, e, children, EAttribute.GX, mode);
        final NumberOrFormula gy = extractNumberOrFormula(evalContext, e, children, EAttribute.GY, mode);

        final NumberOrFormula gx1 = extractNumberOrFormula(evalContext, e, children, EAttribute.GX1, mode);
        final NumberOrFormula gy1 = extractNumberOrFormula(evalContext, e, children, EAttribute.GY1, mode);

        final NumberOrFormula x2 = extractNumberOrFormula(evalContext, e, children, EAttribute.X2, mode);
        final NumberOrFormula y2 = extractNumberOrFormula(evalContext, e, children, EAttribute.Y2, mode);

        final NumberOrFormula gx2 = extractNumberOrFormula(evalContext, e, children, EAttribute.GX2, mode);
        final NumberOrFormula gy2 = extractNumberOrFormula(evalContext, e, children, EAttribute.GY2, mode);

        final NumberOrFormula width = extractNumberOrFormula(evalContext, e, children, EAttribute.WIDTH, mode);
        final NumberOrFormula height = extractNumberOrFormula(evalContext, e, children, EAttribute.HEIGHT, mode);

        final NumberOrFormula gwidth = extractNumberOrFormula(evalContext, e, children, EAttribute.GWIDTH, mode);
        final NumberOrFormula gheight = extractNumberOrFormula(evalContext, e, children, EAttribute.GHEIGHT, mode);

        EAttribute x1Attr = null;
        NumberOrFormula x1Value = null;
        if (Objects.nonNull(x)) {
            x1Attr = EAttribute.X;
            x1Value = x;
        } else if (Objects.nonNull(x1)) {
            x1Attr = EAttribute.X1;
            x1Value = x1;
        } else if (Objects.nonNull(gx)) {
            x1Attr = EAttribute.GX;
            x1Value = gx;
        } else if (Objects.nonNull(gx1)) {
            x1Attr = EAttribute.GX1;
            x1Value = gx1;
        }

        EAttribute x2Attr = null;
        NumberOrFormula x2Value = null;
        if (Objects.nonNull(width)) {
            x2Attr = EAttribute.WIDTH;
            x2Value = width;
        } else if (Objects.nonNull(x2)) {
            x2Attr = EAttribute.X2;
            x2Value = x2;
        } else if (Objects.nonNull(gwidth)) {
            x2Attr = EAttribute.GWIDTH;
            x2Value = gwidth;
        } else if (Objects.nonNull(gx2)) {
            x2Attr = EAttribute.GX2;
            x2Value = gx2;
        }

        EAttribute y1Attr = null;
        NumberOrFormula y1Value = null;
        if (Objects.nonNull(y)) {
            y1Attr = EAttribute.Y;
            y1Value = y;
        } else if (Objects.nonNull(y1)) {
            y1Attr = EAttribute.Y1;
            y1Value = y1;
        } else if (Objects.nonNull(gy)) {
            y1Attr = EAttribute.GY;
            y1Value = gy;
        } else if (Objects.nonNull(gy1)) {
            y1Attr = EAttribute.GY1;
            y1Value = gy1;
        }

        EAttribute y2Attr = null;
        NumberOrFormula y2Value = null;
        if (Objects.nonNull(height)) {
            y2Attr = EAttribute.HEIGHT;
            y2Value = height;
        } else if (Objects.nonNull(y2)) {
            y2Attr = EAttribute.Y2;
            y2Value = y2;
        } else if (Objects.nonNull(gheight)) {
            y2Attr = EAttribute.GHEIGHT;
            y2Value = gheight;
        } else if (Objects.nonNull(gy2)) {
            y2Attr = EAttribute.GY2;
            y2Value = gy2;
        }

        boolean ok = false;

        // If the shape has been completely specified by defining both corners in some way, look no further.

        if (Objects.nonNull(x1Attr) && Objects.nonNull(x2Attr) && Objects.nonNull(y1Attr) && Objects.nonNull(y2Attr)) {
            final RectangleShapeTemplate shape = new RectangleShapeTemplate();
            shape.setBounds(x1Attr, x1Value, x2Attr, x2Value, y1Attr, y1Value, y2Attr, y2Value);
            primitive.setShape(shape);
            ok = true;
        } else {
            // The shape is not completely defined in that way - look for definitions of center and radii

            final NumberOrFormula cx = extractNumberOrFormula(evalContext, e, children, EAttribute.CX, mode);
            final NumberOrFormula cy = extractNumberOrFormula(evalContext, e, children, EAttribute.CY, mode);

            final NumberOrFormula gcx = extractNumberOrFormula(evalContext, e, children, EAttribute.GCX, mode);
            final NumberOrFormula gcy = extractNumberOrFormula(evalContext, e, children, EAttribute.GCY, mode);

            EAttribute cxAttr = null;
            NumberOrFormula cxValue = null;
            if (Objects.nonNull(cx)) {
                cxAttr = EAttribute.CX;
                cxValue = cx;
            } else if (Objects.nonNull(gcx)) {
                cxAttr = EAttribute.GCX;
                cxValue = gcx;
            }

            EAttribute cyAttr = null;
            NumberOrFormula cyValue = null;
            if (Objects.nonNull(cy)) {
                cyAttr = EAttribute.CY;
                cyValue = cy;
            } else if (Objects.nonNull(gcy)) {
                cyAttr = EAttribute.GCY;
                cyValue = gcy;
            }

            if (Objects.nonNull(cxAttr) && Objects.nonNull(cyAttr)) {

                // We have a center - now check for either a single radius, or a pair of x and y radii.

                final NumberOrFormula rx = extractNumberOrFormula(evalContext, e, children, EAttribute.RX, mode);
                final NumberOrFormula grx = extractNumberOrFormula(evalContext, e, children, EAttribute.GRX, mode);

                final NumberOrFormula ry = extractNumberOrFormula(evalContext, e, children, EAttribute.RY, mode);
                final NumberOrFormula gry = extractNumberOrFormula(evalContext, e, children, EAttribute.GRY, mode);

                EAttribute rxAttr = null;
                NumberOrFormula rxValue = null;
                if (Objects.nonNull(rx)) {
                    rxAttr = EAttribute.RX;
                    rxValue = rx;
                } else if (Objects.nonNull(grx)) {
                    rxAttr = EAttribute.GRX;
                    rxValue = grx;
                } else if (Objects.nonNull(width)) {
                    rxAttr = EAttribute.WIDTH;
                    rxValue = width;
                } else if (Objects.nonNull(gwidth)) {
                    rxAttr = EAttribute.GWIDTH;
                    rxValue = gwidth;
                }

                EAttribute ryAttr = null;
                NumberOrFormula ryValue = null;
                if (Objects.nonNull(ry)) {
                    ryAttr = EAttribute.RY;
                    ryValue = ry;
                } else if (Objects.nonNull(gry)) {
                    ryAttr = EAttribute.GRY;
                    ryValue = gry;
                } else if (Objects.nonNull(height)) {
                    ryAttr = EAttribute.HEIGHT;
                    ryValue = height;
                } else if (Objects.nonNull(gheight)) {
                    ryAttr = EAttribute.GHEIGHT;
                    ryValue = gheight;
                }

                if (Objects.nonNull(rxAttr) && Objects.nonNull(ryAttr)) {
                    final RectangleShapeTemplate shape = new RectangleShapeTemplate();
                    shape.setCenteredEllipse(cxAttr, cxValue, cyAttr, cyValue, rxAttr, rxValue, ryAttr, ryValue);
                    primitive.setShape(shape);
                    ok = true;
                } else {
                    final NumberOrFormula r = extractNumberOrFormula(evalContext, e, children, EAttribute.R, mode);
                    final NumberOrFormula gr = extractNumberOrFormula(evalContext, e, children, EAttribute.GR, mode);

                    EAttribute rAttr = null;
                    NumberOrFormula rValue = null;
                    if (Objects.nonNull(r)) {
                        rAttr = EAttribute.R;
                        rValue = r;
                    } else if (Objects.nonNull(gr)) {
                        rAttr = EAttribute.GR;
                        rValue = gr;
                    }

                    if (Objects.nonNull(rAttr)) {
                        final RectangleShapeTemplate shape = new RectangleShapeTemplate();
                        shape.setCenteredCircle(cxAttr, cxValue, cyAttr, cyValue, rAttr, rValue);
                        primitive.setShape(shape);
                        ok = true;
                    }
                }
            }
        }

        return ok;
    }

    /**
     * Extracts a {@code NumberOrFormula} from an XML element.  The formula could be in an expression child element with
     * the attribute name as its tag name or in an attribute, as a constant, or in a (deprecated) text formula format.
     *
     * @param evalContext the evaluation context
     * @param e           the element from which to extract attributes
     * @param children    the list of children of the element (null if it has no children)
     * @param attribute   the attribute
     * @param mode        the parser mode
     * @return the parsed {@code NumberOrFormula}; {@code null} if unable to parse
     */
    private static NumberOrFormula extractNumberOrFormula(final EvalContext evalContext, final IElement e,
                                                          final Iterable<? extends IElement> children,
                                                          final EAttribute attribute, final EParserMode mode) {

        NumberOrFormula result = null;
        boolean ok = true;

        if (children != null) {
            for (final IElement child : children) {
                final String tagName = child.getTagName();
                if (attribute.label.equals(tagName)) {
                    if (child instanceof final NonemptyElement inner) {
                        final Formula theFormula = XmlFormulaFactory.extractFormula(evalContext, inner, mode);
                        if (theFormula == null) {
                            e.logError("Invalid '" + attribute.label + "' formula.");
                            ok = false;
                        } else {
                            result = new NumberOrFormula(theFormula);
                        }
                    }
                }
            }
        }

        if (ok && result == null) {
            // There was no (valid) child element with an expression - look for an attribute
            final String attr = e.getStringAttr(attribute.label);
            if (attr != null) {
                result = AbstractDocPrimitive.parseNumberOrFormula(attr, e, mode, attribute.label, "rectangle shape");
            }
        }

        return result;
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

        final EAttribute x1 = findX1Attribute();
        final EAttribute x2 = findX2Attribute();
        final EAttribute y1 = findY1Attribute();
        final EAttribute y2 = findY2Attribute();

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
            final EAttribute cx = findCXAttribute();
            final EAttribute cy = findCYAttribute();

            if (Objects.nonNull(cx) && Objects.nonNull(cy)) {

                final NumberOrFormula cxSpec = this.attributes.get(cx);
                final NumberOrFormula cySpec = this.attributes.get(cy);

                final Object cxVal = cxSpec.evaluate(evalContext);
                final Object cyVal = cySpec.evaluate(evalContext);

                if (cxVal instanceof final Number cxNbr && cyVal instanceof final Number cyNbr) {

                    final EAttribute r = findRAttribute();

                    if (Objects.nonNull(r)) {
                        final NumberOrFormula rSpec = this.attributes.get(r);
                        final Object rVal = rSpec.evaluate(evalContext);

                        if (rVal instanceof final Number rNbr) {

                            result = new RectangleShapeInst();
                            result.setCenteredCircle(cx, cxNbr.doubleValue(), cy, cyNbr.doubleValue(),
                                    r, rNbr.doubleValue());
                        } else {
                            final EAttribute rx = findRXAttribute();
                            final EAttribute ry = findRYAttribute();

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
     * Tests whether this object's attribute values are all constant.
     *
     * @return true if all attribute values are constants; false if at least one is given by a formula
     */
    public boolean isConstant() {

        boolean constant = true;

        for (final Map.Entry<EAttribute, NumberOrFormula> entry : this.attributes.entrySet()) {
            final NumberOrFormula value = entry.getValue();

            final Formula formula = value.getFormula();
            if (Objects.nonNull(formula)) {
                constant = false;
                break;
            }
        }

        return constant;
    }

    /**
     * Appends XML attributes that represent this object's attribute values.  This should be called after the open tag
     * has been stated, such as by emitting ("&lt;rectangle").
     *
     * @param xml the {@code HtmlBuilder} to which to write the XML
     */
    void addAttributes(final HtmlBuilder xml) {

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
    void addChildElements(final HtmlBuilder xml, final int indent) {

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
     * Determines the bounding rectangle (in pixel space) from attribute settings.
     *
     * @param context           the evaluation context
     * @param coordinateSystems the coordinate systems to use when resolving "graph space" coordinates
     * @return the bounding rectangle in pixel space; {@code null} if that rectangle could not be determined
     */
    Rectangle2D getBoundsRect(final EvalContext context, final CoordinateSystems coordinateSystems) {

        Rectangle2D result = null;

        final EAttribute x1 = findX1Attribute();
        final EAttribute x2 = findX2Attribute();
        final EAttribute y1 = findY1Attribute();
        final EAttribute y2 = findY2Attribute();

        if (Objects.nonNull(x1) && Objects.nonNull(x2) && Objects.nonNull(y1) && Objects.nonNull(y2)) {

            // This object is specified by defining two opposite corners

            final NumberOrFormula x1Spec = this.attributes.get(x1);
            final NumberOrFormula x2Spec = this.attributes.get(x2);
            final NumberOrFormula y1Spec = this.attributes.get(y1);
            final NumberOrFormula y2Spec = this.attributes.get(y2);

            final Object x1Val = x1Spec.evaluate(context);
            final Object x2Val = x2Spec.evaluate(context);
            final Object y1Val = y1Spec.evaluate(context);
            final Object y2Val = y2Spec.evaluate(context);

            if (x1Val instanceof final Number x1Nbr && x2Val instanceof final Number x2Nbr
                && y1Val instanceof final Number y1Nbr && y2Val instanceof final Number y2Nbr) {

                final double x1Dbl = x1Nbr.doubleValue();
                final double x2Dbl = x2Nbr.doubleValue();
                final double y1Dbl = y1Nbr.doubleValue();
                final double y2Dbl = y2Nbr.doubleValue();

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
            }
        } else {
            final EAttribute cx = findCXAttribute();
            final EAttribute cy = findCYAttribute();

            if (Objects.nonNull(cx) && Objects.nonNull(cy)) {

                final NumberOrFormula cxSpec = this.attributes.get(cx);
                final NumberOrFormula cySpec = this.attributes.get(cy);

                final Object cxVal = cxSpec.evaluate(context);
                final Object cyVal = cySpec.evaluate(context);

                if (cxVal instanceof final Number cxNbr && cyVal instanceof final Number cyNbr) {

                    final double cxDbl = cxNbr.doubleValue();
                    final double cyDbl = cyNbr.doubleValue();

                    final double centerX = cx == EAttribute.CX ? cxDbl : coordinateSystems.graphXToPixelX(cxDbl);
                    final double centerY = cy == EAttribute.CY ? cyDbl : coordinateSystems.graphYToPixelY(cyDbl);

                    final EAttribute rx = findRXAttribute();
                    final EAttribute ry = findRYAttribute();

                    if (Objects.nonNull(rx) && Objects.nonNull(ry)) {

                        final NumberOrFormula rxSpec = this.attributes.get(rx);
                        final NumberOrFormula rySpec = this.attributes.get(ry);

                        final Object rxVal = rxSpec.evaluate(context);
                        final Object ryVal = rySpec.evaluate(context);

                        if (rxVal instanceof final Number rxNbr && ryVal instanceof final Number ryNbr) {

                            final double rxDbl = rxNbr.doubleValue();
                            final double ryDbl = ryNbr.doubleValue();

                            // This object is specified by center and two radii

                            final double pRx = (rx == EAttribute.RX) ? rxDbl : (rx == EAttribute.WIDTH) ? rxDbl * 0.5 :
                                    (rx == EAttribute.GRX) ? coordinateSystems.graphXToPixelX(rxDbl) :
                                            coordinateSystems.graphWidthToPixelWidth(rxDbl);

                            final double pRy = (ry == EAttribute.RY) ? ryDbl : (ry == EAttribute.HEIGHT) ? ryDbl * 0.5 :
                                    (ry == EAttribute.GRY) ? coordinateSystems.graphXToPixelX(ryDbl) :
                                            coordinateSystems.graphHeightToPixelHeight(ryDbl);

                            result = new Rectangle2D.Double(centerX - pRx, centerY - pRy, pRx * 2.0, pRy * 2.0);
                        }
                    } else {

                        final EAttribute r = findRAttribute();

                        if (Objects.nonNull(r)) {
                            final NumberOrFormula rSpec = this.attributes.get(r);
                            final Object rVal = rSpec.evaluate(context);

                            if (rVal instanceof final Number rNbr) {

                                final double rDbl = rNbr.doubleValue();

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
