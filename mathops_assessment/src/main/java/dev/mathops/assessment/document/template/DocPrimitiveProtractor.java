package dev.mathops.assessment.document.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.EAngleUnits;
import dev.mathops.assessment.document.ETextAnchor;
import dev.mathops.assessment.document.inst.DocPrimitiveProtractorInst;
import dev.mathops.assessment.document.inst.RectangleShapeInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.xml.INode;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.util.Objects;
import java.util.Set;

/**
 * A protractor primitive.
 */
final class DocPrimitiveProtractor extends AbstractDocRectangleShape {

    /** The width of padding along the straight edge. */
    private static final double PADDING = 30;

    /** The thickness of the band. */
    private static final double THICKNESS = 40;

    /** A coefficient used to approximate circular arc with cubic. */
    private static final double CUBIC_APPROX = 0.55228474983079;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7079994790272139622L;

    /** The orientation angle, in degrees. */
    private NumberOrFormula orientation;

    /** The type of angle units to display. */
    private EAngleUnits angleUnits;

    /** The number of quadrants to display (2 for half-circle, 4 for full circle, etc.) */
    private Integer numQuadrants;

    /** The color name. */
    private String colorName;

    /** The color. */
    private Color color;

    /** The text color name. */
    private String textColorName;

    /** The text color. */
    private Color textColor;

    /** The alpha. */
    private Double alpha;

    /**
     * Construct a new {@code DocPrimitiveProtractor}.
     *
     * @param theOwner the object that owns this primitive
     */
    DocPrimitiveProtractor(final AbstractDocPrimitiveContainer theOwner) {

        super(theOwner);
    }

    /**
     * Sets the orientation angle, in degrees.
     *
     * @param theOrientation the orientation angle
     */
    void setOrientation(final NumberOrFormula theOrientation) {

        this.orientation = theOrientation;
    }

//    /**
//     * Gets the orientation angle, in degrees.
//     *
//     * @return the orientation angle
//     */
//    public NumberOrFormula getOrientation() {
//
//        return this.orientation;
//    }

    /**
     * Sets the angle units displayed.
     *
     * @param theAngleUnits the angle units
     */
    void setAngleUnits(final EAngleUnits theAngleUnits) {

        this.angleUnits = theAngleUnits;
    }

//    /**
//     * Gets the angle units displayed.
//     *
//     * @return the angle units
//     */
//    public EAngleUnits getAngleUnits() {
//
//        return this.orientationAngle;
//    }

    /**
     * Sets the number of quadrants to display.
     *
     * @param theNumQuadrants the number of quadrants
     */
    void setNumQuadrants(final Integer theNumQuadrants) {

        this.numQuadrants = theNumQuadrants;
    }

//    /**
//     * Gets the number of quadrants to display.
//     *
//     * @return the number of quadrants
//     */
//    public Integer getNumQuadrants() {
//
//        return this.numQuadrants;
//    }

    /**
     * Gets the color name.
     *
     * @return the color name
     */
    public String getColorName() {

        return this.colorName;
    }

    /**
     * Gets the text color name.
     *
     * @return the text color name
     */
    public String getTextColorName() {

        return this.textColorName;
    }

    /**
     * Gets the alpha value.
     *
     * @return the alpha value
     */
    public Double getAlpha() {

        return this.alpha;
    }

    /**
     * Construct a copy of this object with a new owner.
     *
     * @param theOwner the new owner
     * @return the copy
     */
    @Override
    public DocPrimitiveProtractor deepCopy(final AbstractDocPrimitiveContainer theOwner) {

        final DocPrimitiveProtractor copy = new DocPrimitiveProtractor(theOwner);

        final RectangleShapeTemplate myShape = getShape();
        if (myShape != null) {
            final RectangleShapeTemplate myShapeCopy = myShape.deepCopy();
            copy.setShape(myShapeCopy);
        }

        if (this.orientation != null) {
            copy.orientation = this.orientation.deepCopy();
        }

        copy.angleUnits = this.angleUnits;
        copy.numQuadrants = this.numQuadrants;
        copy.colorName = this.colorName;
        copy.color = this.color;
        copy.textColorName = this.textColorName;
        copy.textColor = this.textColor;
        copy.alpha = this.alpha;

        copy.scale = this.scale;

        return copy;
    }

    /**
     * Set an attribute value used in drawing.
     *
     * @param name     the name of the attribute
     * @param theValue the attribute value
     * @param elem     an element to which to log errors
     * @param mode     the parser mode
     * @return true if the attribute was valid; false otherwise
     */
    boolean setAttr(final String name, final String theValue, final INode elem, final EParserMode mode) {

        boolean ok = false;

        if (theValue == null) {
            ok = true;
        } else if ("orientation".equals(name)) {
            this.orientation = parseNumberOrFormula(theValue, elem, mode, "orientation", "protractor primitive");
            ok = this.orientation != null;
        } else if ("units".equals(name)) {
            if ("degrees".equalsIgnoreCase(theValue) || "deg".equalsIgnoreCase(theValue)) {
                this.angleUnits = EAngleUnits.DEGREES;
                ok = true;
            } else if ("radians".equalsIgnoreCase(theValue) || "rad".equalsIgnoreCase(theValue)) {
                this.angleUnits = EAngleUnits.RADIANS;
                ok = true;
            } else {
                elem.logError("Invalid 'units' value (" + theValue + ") on protractor primitive");
            }
        } else if ("quadrants".equals(name)) {
            try {
                final int count = Integer.parseInt(theValue);
                if (count < 1) {
                    this.numQuadrants = Integer.valueOf(1);
                } else if (count > 4) {
                    this.numQuadrants = Integer.valueOf(4);
                } else {
                    this.numQuadrants = Integer.valueOf(count);
                }
                ok = true;
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'quadrants' value (" + theValue + ") on protractor primitive");
            }
        } else if ("color".equals(name)) {
            if (ColorNames.isColorNameValid(theValue)) {
                this.color = ColorNames.getColor(theValue);
                this.colorName = theValue;
                ok = true;
            } else {
                elem.logError("Invalid 'color' value (" + theValue + ") on protractor primitive");
            }
        } else if ("text-color".equals(name)) {
            if (ColorNames.isColorNameValid(theValue)) {
                this.textColor = ColorNames.getColor(theValue);
                this.textColorName = theValue;
                ok = true;
            } else {
                elem.logError("Invalid 'text-color' value (" + theValue + ") on protractor primitive");
            }
        } else if ("alpha".equals(name)) {
            this.alpha = parseDouble(theValue, elem, name, "protractor primitive");
            ok = this.alpha != null;
        } else {
            elem.logError("Unsupported attribute '" + name + "' on protractor primitive");
        }

        return ok;
    }

    /**
     * Draw the primitive.
     *
     * @param grx     the graphics on which to draw
     * @param context the evaluation context
     */
    @Override
    public void draw(final Graphics2D grx, final EvalContext context) {

        final Rectangle2D bounds = getBoundsRect(context);

        if (bounds != null) {
            final Object result;

            double orient = 0.0;
            if (this.orientation != null) {
                result = this.orientation.evaluate(context);

                if (result instanceof final Number numResult) {
                    final double d = numResult.doubleValue();
                    if (d < 360.0) {
                        if (d < 0.0) {
                            if (d > -360.0) {
                                orient = d + 360.0;
                            }
                        } else {
                            orient = d;
                        }
                    }
                }
            }

            EAngleUnits units = EAngleUnits.DEGREES;
            if (this.angleUnits != null) {
                units = this.angleUnits;
            }

            int numQ = 2;
            if (this.numQuadrants != null) {
                final int n = this.numQuadrants.intValue();
                if (n >= 1 && n <= 4) {
                    numQ = n;
                }
            }

            Color c = Color.LIGHT_GRAY;
            if (this.color != null) {
                c = this.color;
            }

            Color tc = Color.BLACK;
            if (this.textColor != null) {
                tc = this.textColor;
            }

            float a = 1.0f;
            if (this.alpha != null) {
                a = Math.min(1.0f, this.alpha.floatValue());
            }

            final double x = bounds.getX();
            final double y = bounds.getY();
            final double width = bounds.getWidth();
            final double height = bounds.getHeight();
            final double r = Math.max(Math.abs(width), Math.abs(height)) * 0.5;

            if (a > 0.0f) {
                final double cxval = x + width * 0.5;
                final double cyval = y + height * 0.5;
                drawProtractor(grx, cxval, cyval, r, orient, units, numQ, c, tc, a);
            }
        }
    }

    /**
     * Draw the protractor shape.
     *
     * @param grx    the graphics on which to draw
     * @param cx     the center x coordinate
     * @param cy     the center y coordinate
     * @param r      the radius
     * @param orient the orientation angle, in degrees
     * @param units  the type of units to draw
     * @param numQ   the number of quadrants to draw
     * @param c      the main color for the protractor
     * @param tc     the text color
     * @param a      the alpha
     */
    private void drawProtractor(final Graphics2D grx, final double cx, final double cy, final double r,
                                final double orient, final EAngleUnits units, final int numQ, final Color c,
                                final Color tc, final float a) {

        final double orientRad = Math.toRadians(orient);
        final double cosOrient = Math.cos(orientRad);
        final double sinOrient = Math.sin(orientRad);
        final double x1 = cx + r * cosOrient;
        final double y1 = cy - r * sinOrient;
        final double x2 = cx - r * sinOrient;
        final double y2 = cy - r * cosOrient;
        final double x3 = cx - r * cosOrient;
        final double y3 = cy + r * sinOrient;
        final double x4 = cx + r * sinOrient;
        final double y4 = cy + r * cosOrient;
        final double scaledR = r * CUBIC_APPROX;

        final double eastX = cosOrient;
        final double eastY = -sinOrient;
        final double westX = -cosOrient;
        final double westY = sinOrient;
        final double southX = sinOrient;
        final double southY = cosOrient;
        final double northX = sinOrient;
        final double northY = -cosOrient;

        final double eastPadX = PADDING * eastX;
        final double eastPadY = PADDING * eastY;
        final double westPadX = PADDING * westX;
        final double westPadY = PADDING * westY;
        final double southPadX = PADDING * southX;
        final double southPadY = PADDING * southY;

        // Draw and outline the protractor shape
        final double scl = this.scale;

        final Path2D outline = new Path2D.Double();
        if (numQ == 1) {
            outline.moveTo((x1 + southPadX) * scl, (y1 + southPadY) * scl);
            outline.lineTo(x1 * scl, y1 * scl);
            final double cp1ax = x1 - scaledR * sinOrient;
            final double cp1ay = y1 - scaledR * cosOrient;
            final double cp1bx = x2 + scaledR * cosOrient;
            final double cp1by = y2 - scaledR * sinOrient;
            outline.curveTo(cp1ax * scl, cp1ay * scl, cp1bx * scl, cp1by * scl, x2 * scl, y2 * scl);
            outline.lineTo((x2 + westPadX) * scl, (y2 + westPadY) * scl);
            outline.lineTo((cx + southPadX + westPadX) * scl, (cy + southPadY + westPadY) * scl);
            outline.closePath();
        } else if (numQ == 2) {
            outline.moveTo((x1 + southPadX) * scl, (y1 + southPadY) * scl);
            outline.lineTo(x1 * scl, y1 * scl);
            final double cp1ax = x1 - scaledR * sinOrient;
            final double cp1ay = y1 - scaledR * cosOrient;
            final double cp1bx = x2 + scaledR * cosOrient;
            final double cp1by = y2 - scaledR * sinOrient;
            outline.curveTo(cp1ax * scl, cp1ay * scl, cp1bx * scl, cp1by * scl, x2 * scl, y2 * scl);
            final double cp2ax = x2 - scaledR * cosOrient;
            final double cp2ay = y2 + scaledR * sinOrient;
            final double cp2bx = x3 - scaledR * sinOrient;
            final double cp2by = y3 - scaledR * cosOrient;
            outline.curveTo(cp2ax * scl, cp2ay * scl, cp2bx * scl, cp2by * scl, x3 * scl, y3 * scl);
            outline.lineTo((x3 + southPadX) * scl, (y3 + southPadY) * scl);
            outline.closePath();
        } else if (numQ == 3) {
            outline.moveTo((x1 + southPadX) * scl, (y1 + southPadY) * scl);
            outline.lineTo(x1 * scl, y1 * scl);
            final double cp1ax = x1 - scaledR * sinOrient;
            final double cp1ay = y1 - scaledR * cosOrient;
            final double cp1bx = x2 + scaledR * cosOrient;
            final double cp1by = y2 - scaledR * sinOrient;
            outline.curveTo(cp1ax * scl, cp1ay * scl, cp1bx * scl, cp1by * scl, x2 * scl, y2 * scl);
            final double cp2ax = x2 - scaledR * cosOrient;
            final double cp2ay = y2 + scaledR * sinOrient;
            final double cp2bx = x3 - scaledR * sinOrient;
            final double cp2by = y3 - scaledR * cosOrient;
            outline.curveTo(cp2ax * scl, cp2ay * scl, cp2bx * scl, cp2by * scl, x3 * scl, y3 * scl);
            final double cp3ax = x3 + scaledR * sinOrient;
            final double cp3ay = y3 + scaledR * cosOrient;
            final double cp3bx = x4 - scaledR * cosOrient;
            final double cp3by = y4 + scaledR * sinOrient;
            outline.curveTo(cp3ax * scl, cp3ay * scl, cp3bx * scl, cp3by * scl, x4 * scl, y4 * scl);
            outline.lineTo((x4 + eastPadX) * scl, (y4 + eastPadY) * scl);
            outline.lineTo((cx + southPadX + eastPadX) * scl, (cy + southPadY + eastPadY) * scl);
            outline.closePath();
        } else {
            outline.moveTo(x1 * scl, y1 * scl);
            final double cp1ax = x1 - scaledR * sinOrient;
            final double cp1ay = y1 - scaledR * cosOrient;
            final double cp1bx = x2 + scaledR * cosOrient;
            final double cp1by = y2 - scaledR * sinOrient;
            outline.curveTo(cp1ax * scl, cp1ay * scl, cp1bx * scl, cp1by * scl, x2 * scl, y2 * scl);
            final double cp2ax = x2 - scaledR * cosOrient;
            final double cp2ay = y2 + scaledR * sinOrient;
            final double cp2bx = x3 - scaledR * sinOrient;
            final double cp2by = y3 - scaledR * cosOrient;
            outline.curveTo(cp2ax * scl, cp2ay * scl, cp2bx * scl, cp2by * scl, x3 * scl, y3 * scl);
            final double cp3ax = x3 + scaledR * sinOrient;
            final double cp3ay = y3 + scaledR * cosOrient;
            final double cp3bx = x4 - scaledR * cosOrient;
            final double cp3by = y4 + scaledR * sinOrient;
            outline.curveTo(cp3ax * scl, cp3ay * scl, cp3bx * scl, cp3by * scl, x4 * scl, y4 * scl);
            final double cp4ax = x4 + scaledR * cosOrient;
            final double cp4ay = y4 - scaledR * sinOrient;
            final double cp4bx = x1 + scaledR * sinOrient;
            final double cp4by = y1 + scaledR * cosOrient;
            outline.curveTo(cp4ax * scl, cp4ay * scl, cp4bx * scl, cp4by * scl, x1 * scl, y1 * scl);
            outline.closePath();
        }

        Composite origComp = null;
        if (a < 0.99f) {
            origComp = grx.getComposite();
            grx.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        }

        grx.setColor(c);
        grx.fill(outline);
        grx.setColor(c.darker());
        grx.draw(outline);

        // Alpha is not applied to markings at this time (we might add a "text-alpha" later)
        if (origComp != null) {
            grx.setComposite(origComp);
        }

        // Draw the markings on the protractor
        grx.setColor(tc);
        grx.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.round(12.0f * this.scale)));

        if (numQ == 1) {
            final Line2D hLine = new Line2D.Double((cx + westPadX) * scl, (cy + westPadY) * scl, x1 * scl, y1 * scl);
            grx.draw(hLine);
            final Line2D vLine = new Line2D.Double((cx + southPadX) * scl, (cy + southPadY) * scl, x2 * scl, y2 * scl);
            grx.draw(vLine);

            drawText(grx, "0", (x1 + 3.0 * southX + 3.0 * westX) * scl, (y1 + 3.0 * southY + 3.0 * westY) * scl,
                    orient, ETextAnchor.NE);

            if (units == EAngleUnits.DEGREES) {
                drawText(grx, "90", (x2 + 3.0 * southX + 4.0 * westX) * scl, (y2 + 3.0 * southY + 4.0 * westY) * scl,
                        orient, ETextAnchor.NE);
                drawDegreeTicks(grx, cx * scl, cy * scl, r * scl, orient, 1, 90);
            } else {
                drawFraction(grx, "\u03c0", "2", (x2 + 3.0 * southX + 4.0 * westX) * scl,
                        (y2 + 3.0 * southY + 4.0 * westY) * scl, orient, ETextAnchor.NE);
                drawRadiansTicks(grx, cx * scl, cy * scl, r * scl, orient, 1, 48);
            }

        } else if (numQ == 2) {
            final Line2D hLine = new Line2D.Double(x3 * scl, y3 * scl, x1 * scl, y1 * scl);
            grx.draw(hLine);

            drawText(grx, "0", (x1 + 3.0 * southX + 3.0 * westX) * scl, (y1 + 3.0 * southY + 3.0 * westY) * scl,
                    orient, ETextAnchor.NE);

            if (units == EAngleUnits.DEGREES) {
                drawText(grx, "180", (x3 + 3.0 * southX + 3.0 * eastX) * scl, (y3 + 3.0 * southY + 3.0 * eastY) * scl,
                        orient, ETextAnchor.NW);
                drawDegreeTicks(grx, cx * scl, cy * scl, r * scl, orient, 1, 180);
            } else {
                drawText(grx, "\u03c0", (x3 + 3.0 * southX + 3.0 * eastX) * scl,
                        (y3 + 3.0 * southY + 3.0 * eastY) * scl, orient, ETextAnchor.NW);
                drawRadiansTicks(grx, cx * scl, cy * scl, r * scl, orient, 1, 96);
            }

        } else if (numQ == 3) {
            final Line2D hLine = new Line2D.Double(cx * scl, cy * scl, x1 * scl, y1 * scl);
            grx.draw(hLine);
            final Line2D vLine = new Line2D.Double(cx * scl, cy * scl, x4 * scl, y4 * scl);
            grx.draw(vLine);

            drawText(grx, "0", (x1 + 3.0 * southX + 3.0 * westX) * scl, (y1 + 3.0 * southY + 3.0 * westY) * scl,
                    orient, ETextAnchor.NE);

            if (units == EAngleUnits.DEGREES) {
                drawText(grx, "270", (x4 + 3.0 * northX + 3.0 * eastX) * scl, (y4 + 3.0 * northY + 3.0 * eastY) * scl,
                        orient, ETextAnchor.SW);
                drawDegreeTicks(grx, cx * scl, cy * scl, r * scl, orient, 1, 270);
            } else {
                drawFraction(grx, "3\u03c0", "2", (x4 + 3.0 * northX + 3.0 * eastX) * scl,
                        (y4 + 3.0 * northY + 3.0 * eastY) * scl, orient, ETextAnchor.SW);
                drawRadiansTicks(grx, cx * scl, cy * scl, r * scl, orient, 1, 144);
            }
        } else if (numQ == 4) {
            if (units == EAngleUnits.DEGREES) {
                drawDegreeTicks(grx, cx * scl, cy * scl, r * scl, orient, 0, 360);
            } else {
                drawRadiansTicks(grx, cx * scl, cy * scl, r * scl, orient, 0, 192);
            }
        }
    }

    /**
     * Draws degree tick marks.
     *
     * @param grx    the graphics on which to draw
     * @param cx     the center x coordinate
     * @param cy     the center y coordinate
     * @param r      the radius
     * @param orient the orientation angle, in degrees
     * @param start  the first degree whose tick mark to draw
     * @param end    the degree after the last tick mark to draw
     */
    private void drawDegreeTicks(final Graphics2D grx, final double cx, final double cy,
                                 final double r, final double orient, final int start, final int end) {

        final double scl = this.scale;
        final double innerRadLong = r - THICKNESS * 0.5 * scl;
        final double innerRadMed = r - THICKNESS * 0.3 * scl;
        final double innerRadShort = r - THICKNESS * 0.15 * scl;

        for (int angleDeg = start; angleDeg < end; ++angleDeg) {
            final double anchorRad = innerRadLong - 12.0 * scl;

            final double innerRad = (angleDeg % 10) == 0 ? innerRadLong :
                    ((angleDeg % 5) == 0 ? innerRadMed : innerRadShort);

            final double angleRad = Math.toRadians((double) angleDeg + orient);
            final double cosAngleRad = Math.cos(angleRad);
            final double sinAngleRad = Math.sin(angleRad);

            final Shape tick = new Line2D.Double(cx + innerRad * cosAngleRad, cy - innerRad * sinAngleRad,
                    cx + r * cosAngleRad, cy - r * sinAngleRad);
            grx.draw(tick);

            if ((angleDeg % 10) == 0) {
                final double anchorX = cx + anchorRad * cosAngleRad;
                final double anchorY = cy - anchorRad * sinAngleRad;
                drawText(grx, Integer.toString(angleDeg), anchorX, anchorY, orient, ETextAnchor.C);
            }
        }
    }

    /**
     * Draws radian tick marks.
     *
     * @param grx    the graphics on which to draw
     * @param cx     the center x coordinate
     * @param cy     the center y coordinate
     * @param r      the radius
     * @param orient the orientation angle, in degrees
     * @param start  the first index whose tick mark to draw
     * @param end    the index after the last tick mark to draw (48 per quarter-turn)
     */
    private void drawRadiansTicks(final Graphics2D grx, final double cx, final double cy,
                                  final double r, final double orient, final int start, final int end) {

        final double scl = this.scale;
        final double innerRadLong = r - THICKNESS * 0.5 * scl;
        final double innerRadMed = r - THICKNESS * 0.3 * scl;
        final double innerRadShort = r - THICKNESS * 0.15 * scl;

        final double anchorRad = innerRadLong - 12.0 * scl;
        final double anchorRad2 = innerRadLong - 18.0 * scl;
        final double orientRad = Math.toRadians(orient);

        // Draw long lines every pi/12, shorter every pi/24, and short every pi/96
        for (int step = start; step < end; ++step) {

            final double innerRad = (step % 8) == 0 ? innerRadLong :
                    ((step % 4) == 0 ? innerRadMed : innerRadShort);

            final double angleRad = (double) step * Math.PI / 96.0 + orientRad;
            final double cosAngleRad = Math.cos(angleRad);
            final double sinAngleRad = Math.sin(angleRad);

            final Shape tick = new Line2D.Double(cx + innerRad * cosAngleRad, cy - innerRad * sinAngleRad,
                    cx + r * cosAngleRad, cy - r * sinAngleRad);
            grx.draw(tick);

            if ((step % 8) == 0) {
                final int twelfths = step / 8;

                final String num;
                String den = CoreConstants.EMPTY;

                double rad = anchorRad;
                if (twelfths == 0) {
                    num = "0";
                } else if (twelfths == 1) {
                    rad = anchorRad2;
                    num = "\u03c0";
                    den = "12";
                } else if (twelfths == 2) {
                    rad = anchorRad2;
                    num = "\u03c0";
                    den = "6";
                } else if (twelfths == 3) {
                    rad = anchorRad2;
                    num = "\u03c0";
                    den = "4";
                } else if (twelfths == 4) {
                    rad = anchorRad2;
                    num = "\u03c0";
                    den = "3";
                } else if (twelfths == 5) {
                    rad = anchorRad2;
                    num = "5\u03c0";
                    den = "12";
                } else if (twelfths == 6) {
                    rad = anchorRad2;
                    num = "\u03c0";
                    den = "2";
                } else if (twelfths == 7) {
                    rad = anchorRad2;
                    num = "7\u03c0";
                    den = "12";
                } else if (twelfths == 8) {
                    rad = anchorRad2;
                    num = "2\u03c0";
                    den = "3";
                } else if (twelfths == 9) {
                    rad = anchorRad2;
                    num = "3\u03c0";
                    den = "4";
                } else if (twelfths == 10) {
                    rad = anchorRad2;
                    num = "5\u03c0";
                    den = "6";
                } else if (twelfths == 11) {
                    rad = anchorRad2;
                    num = "11\u03c0";
                    den = "12";
                } else if (twelfths == 12) {
                    num = "\u03c0";
                } else if (twelfths == 13) {
                    rad = anchorRad2;
                    num = "13\u03c0";
                    den = "12";
                } else if (twelfths == 14) {
                    rad = anchorRad2;
                    num = "7\u03c0";
                    den = "6";
                } else if (twelfths == 15) {
                    rad = anchorRad2;
                    num = "15\u03c0";
                    den = "12";
                } else if (twelfths == 16) {
                    rad = anchorRad2;
                    num = "4\u03c0";
                    den = "3";
                } else if (twelfths == 17) {
                    rad = anchorRad2;
                    num = "17\u03c0";
                    den = "12";
                } else if (twelfths == 18) {
                    rad = anchorRad2;
                    num = "3\u03c0";
                    den = "2";
                } else if (twelfths == 19) {
                    rad = anchorRad2;
                    num = "19\u03c0";
                    den = "12";
                } else if (twelfths == 20) {
                    rad = anchorRad2;
                    num = "5\u03c0";
                    den = "3";
                } else if (twelfths == 21) {
                    rad = anchorRad2;
                    num = "21\u03c0";
                    den = "12";
                } else if (twelfths == 22) {
                    rad = anchorRad2;
                    num = "11\u03c0";
                    den = "6";
                } else {
                    rad = anchorRad2;
                    num = "23\u03c0";
                    den = "12";
                }
                final double anchorX = cx + rad * cosAngleRad;
                final double anchorY = cy - rad * sinAngleRad;
                if (den.isEmpty()) {
                    drawText(grx, num, anchorX, anchorY, orient, ETextAnchor.C);
                } else {
                    drawFraction(grx, num, den, anchorX, anchorY, orient, ETextAnchor.C);
                }
            }
        }
    }

    /**
     * Draws a text string using the current font.
     *
     * @param grx      the graphics on which to draw
     * @param str      the string to draw
     * @param x        the X position of the anchor point
     * @param y        the Y position of the anchor point
     * @param rotation the rotation about the anchor point, in degrees
     * @param anchor   the anchor point
     */
    private static void drawText(final Graphics2D grx, final String str, final double x, final double y,
                                 final double rotation, final ETextAnchor anchor) {

        final AffineTransform origXform = grx.getTransform();

        final boolean doRotate = Math.abs(rotation) > 0.5;
        if (doRotate) {
            grx.translate(x, y);
            grx.rotate(Math.toRadians(-rotation));
            grx.translate(-x, -y);
        }

        final FontRenderContext frc = grx.getFontRenderContext();
        final GlyphVector vect = grx.getFont().createGlyphVector(frc, str);
        final Rectangle2D bounds = vect.getVisualBounds();

        double actualX = 0.0;
        double actualY = 0.0;
        if (anchor == null || anchor == ETextAnchor.SW) {
            actualX = x;
            actualY = y;
        } else if (anchor == ETextAnchor.NW) {
            actualX = x;
            actualY = y + bounds.getHeight();
        } else if (anchor == ETextAnchor.W) {
            actualX = x;
            actualY = y + bounds.getHeight() * 0.5;
        } else if (anchor == ETextAnchor.N) {
            actualX = x - bounds.getWidth() * 0.5;
            actualY = y + bounds.getHeight();
        } else if (anchor == ETextAnchor.C) {
            actualX = x - bounds.getWidth() * 0.5;
            actualY = y + bounds.getHeight() * 0.5;
        } else if (anchor == ETextAnchor.S) {
            actualX = x - bounds.getWidth() * 0.5;
            actualY = y;
        } else if (anchor == ETextAnchor.NE) {
            actualX = x - bounds.getWidth();
            actualY = y + bounds.getHeight();
        } else if (anchor == ETextAnchor.E) {
            actualX = x - bounds.getWidth();
            actualY = y + bounds.getHeight() * 0.5;
        } else if (anchor == ETextAnchor.SE) {
            actualX = x - bounds.getWidth();
            actualY = y;
        }

        grx.drawString(str, (float) actualX, (float) actualY);

        grx.setTransform(origXform);
    }

    /**
     * Draws a fraction using the current font.
     *
     * @param grx      the graphics on which to draw
     * @param numer    the numerator to draw
     * @param denom    the denominator to draw
     * @param x        the X position of the anchor point
     * @param y        the Y position of the anchor point
     * @param rotation the rotation about the anchor point, in degrees
     * @param anchor   the anchor point
     */
    private static void drawFraction(final Graphics2D grx, final String numer, final String denom, final double x,
                                     final double y, final double rotation, final ETextAnchor anchor) {

        final AffineTransform origXform = grx.getTransform();

        final boolean doRotate = Math.abs(rotation) > 0.5;
        if (doRotate) {
            grx.translate(x, y);
            grx.rotate(Math.toRadians(-rotation));
            grx.translate(-x, -y);
        }

        final FontRenderContext frc = grx.getFontRenderContext();
        final GlyphVector numVect = grx.getFont().createGlyphVector(frc, numer);
        final GlyphVector denVect = grx.getFont().createGlyphVector(frc, denom);
        final Rectangle2D numBounds = numVect.getVisualBounds();
        final Rectangle2D denBounds = denVect.getVisualBounds();
        final Rectangle2D bounds = new Rectangle2D.Double(0.0, 0.0,
                Math.max(numBounds.getWidth(), denBounds.getWidth()),
                numBounds.getHeight() + denBounds.getHeight() + 5.0);

        double actualX = 0.0;
        double actualY = 0.0;
        if (anchor == null || anchor == ETextAnchor.SW) {
            actualX = x;
            actualY = y;
        } else if (anchor == ETextAnchor.NW) {
            actualX = x;
            actualY = y + bounds.getHeight();
        } else if (anchor == ETextAnchor.W) {
            actualX = x;
            actualY = y + bounds.getHeight() * 0.5;
        } else if (anchor == ETextAnchor.N) {
            actualX = x - bounds.getWidth() * 0.5;
            actualY = y + bounds.getHeight();
        } else if (anchor == ETextAnchor.C) {
            actualX = x - bounds.getWidth() * 0.5;
            actualY = y + bounds.getHeight() * 0.5;
        } else if (anchor == ETextAnchor.S) {
            actualX = x - bounds.getWidth() * 0.5;
            actualY = y;
        } else if (anchor == ETextAnchor.NE) {
            actualX = x - bounds.getWidth();
            actualY = y + bounds.getHeight();
        } else if (anchor == ETextAnchor.E) {
            actualX = x - bounds.getWidth();
            actualY = y + bounds.getHeight() * 0.5;
        } else if (anchor == ETextAnchor.SE) {
            actualX = x - bounds.getWidth();
            actualY = y;
        }

        final double numX = actualX + (bounds.getWidth() - numBounds.getWidth()) * 0.5;
        final double numY = actualY - 5.0 - denBounds.getHeight();
        grx.drawString(numer, (float) numX, (float) numY);

        final double denX = actualX + (bounds.getWidth() - denBounds.getWidth()) * 0.5;
        grx.drawString(denom, (float) denX, (float) actualY);

        final double sepY = actualY - denBounds.getHeight() - 2.5;
        final Line2D sep = new Line2D.Double(actualX, sepY, actualX + bounds.getWidth(), sepY);
        grx.draw(sep);

        grx.setTransform(origXform);
    }

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context the evaluation context
     */
    @Override
    public void doLayout(final EvalContext context) {

        // No action
    }

    /**
     * Generates an instance of this primitive based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance primitive object; null if unable to create the instance
     */
    @Override
    public DocPrimitiveProtractorInst createInstance(final EvalContext evalContext) {

        final RectangleShapeTemplate shape = getShape();
        DocPrimitiveProtractorInst result = null;

        if (Objects.nonNull(shape)) {
            final Object orientVal = this.orientation == null ? null : this.orientation.evaluate(evalContext);
            final Color colorVal = this.color == null ? Color.lightGray : this.color;
            final Color textColorVal = this.textColor == null ? Color.black : this.textColor;

            if (orientVal instanceof final Number oNbr) {

                final double alphaValue = this.alpha == null ? 1.0 : this.alpha.doubleValue();

                final RectangleShapeInst shapeInst = getShape().createInstance(evalContext);
                result = new DocPrimitiveProtractorInst(shapeInst,
                        oNbr.doubleValue(), this.angleUnits, this.numQuadrants.intValue(), colorVal, textColorVal,
                        alphaValue);
            }
        }

        return result;
    }

    /**
     * Write the XML representation of the object to a {@code v}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent);
        final String ind2 = makeIndent(indent + 1);

        xml.add(ind, "<protractor");

        final RectangleShapeTemplate shape = getShape();
        if (shape != null) {
            shape.addAttributes(xml);
        }

        if (this.orientation != null && this.orientation.getNumber() != null) {
            xml.add(" orientation=\"", this.orientation.getNumber(), CoreConstants.QUOTE);
        }

        if (this.angleUnits == EAngleUnits.DEGREES) {
            xml.add(" units=\"deg\"");
        } else if (this.angleUnits == EAngleUnits.RADIANS) {
            xml.add(" units=\"rad\"");
        }

        if (this.numQuadrants != null) {
            xml.add(" quadrants=\"", this.numQuadrants, CoreConstants.QUOTE);
        }

        if (this.colorName != null) {
            xml.add(" color=\"", this.colorName, CoreConstants.QUOTE);
        }

        if (this.textColorName != null) {
            xml.add(" text-color=\"", this.textColorName, CoreConstants.QUOTE);
        }

        if (this.alpha != null) {
            xml.add(" alpha=\"", this.alpha.toString(), CoreConstants.QUOTE);
        }

        if ((shape == null || shape.isConstant())
            && (this.orientation == null || this.orientation.getFormula() == null)) {
            xml.addln("/>");
        } else {
            xml.addln(">");

            if (!(shape == null || shape.isConstant())) {
                shape.addChildElements(xml, indent + 1);
            }

            if (this.orientation != null && this.orientation.getFormula() != null) {
                xml.add(ind2, "<orientation>");
                this.orientation.getFormula().appendChildrenXml(xml);
                xml.addln("</orientation>");
            }

            xml.addln(ind, "</protractor>");
        }
    }

    /**
     * Generate a String representation, which is just the type as a String.
     *
     * @return the primitive type string
     */
    @Override
    public String toString() {

        return "Protractor";
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) { // Do NOT change to "? super String"

        final RectangleShapeTemplate shape = getShape();
        if (shape != null) {
            shape.accumulateParameterNames(set);
        }

        if (this.orientation != null && this.orientation.getFormula() != null) {
            set.addAll(this.orientation.getFormula().params.keySet());
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(getShape())
               + Objects.hashCode(this.orientation)
               + Objects.hashCode(this.angleUnits)
               + Objects.hashCode(this.numQuadrants)
               + Objects.hashCode(this.colorName)
               + Objects.hashCode(this.color)
               + Objects.hashCode(this.textColorName)
               + Objects.hashCode(this.textColor)
               + Objects.hashCode(this.alpha);
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
        } else if (obj instanceof final DocPrimitiveProtractor prot) {
            equal = Objects.equals(getShape(), prot.getShape())
                    && Objects.equals(this.orientation, prot.orientation)
                    && this.angleUnits == prot.angleUnits
                    && Objects.equals(this.numQuadrants, prot.numQuadrants)
                    && Objects.equals(this.colorName, prot.colorName)
                    && Objects.equals(this.color, prot.color)
                    && Objects.equals(this.textColorName, prot.textColorName)
                    && Objects.equals(this.textColor, prot.textColor)
                    && Objects.equals(this.alpha, prot.alpha);
        } else {
            equal = false;
        }

        return equal;
    }
}
