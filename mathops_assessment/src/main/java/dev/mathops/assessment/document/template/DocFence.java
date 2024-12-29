package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.EFenceType;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.EPrimaryBaseline;
import dev.mathops.assessment.document.EVAlign;
import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.document.inst.DocFenceInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;
import dev.mathops.text.builder.HtmlBuilder;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * A fence, such as a parenthesis or bracket, which will be laid out centered on its line of text.
 */
public final class DocFence extends AbstractDocSpanBase {

    /** A constant to indicate parentheses. */
    public static final int PARENTHESES = 1;

    /** A constant to indicate square brackets. */
    public static final int BRACKETS = 2;

    /** A constant to indicate vertical bars. */
    public static final int BARS = 3;

    /** A constant to indicate braces (curly brackets). */
    public static final int BRACES = 4;

    /** A constant to indicate left braces (curly bracket). */
    public static final int LBRACE = 5;

    /** The width of a parenthesis. */
    private static final int PAREN_WIDTH = 6;

    /** The width of a brace. */
    private static final int BRACE_WIDTH = 6;

    /** The width of a bracket. */
    private static final int BRACKET_WIDTH = 5;

    /** The width of vertical bars. */
    private static final int BARS_WIDTH = 4;

    /** The padding on the left and right of a fenced construction. */
    private static final int FENCE_PAD = 2;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2013064330513943343L;

    /** The type of fence this is. */
    public int type = PARENTHESES;

    /** The set of outlines of the object to draw. */
    private int outLines = 0xFFFF;

    /**
     * Construct a new {@code DocFence} object.
     */
    DocFence() {

        super();

        setLeftAlign(EVAlign.CENTER);
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocFence deepCopy() {

        final DocFence copy = new DocFence();

        copy.copyObjectFrom(this);
        copy.type = this.type;

        for (final AbstractDocObjectTemplate child : getChildren()) {
            copy.add(child.deepCopy());
        }

        return copy;
    }

    /**
     * Copy information from a source {@code DocObject} object, including all underlying {@code DocFormattable}
     * information.
     *
     * @param source the {@code DocObject} from which to copy data
     */
    private void copyObjectFrom(final DocFence source) {

        copyObjectFromContainer(source);

        this.outLines = source.outLines;
    }

    /**
     * Recompute the size of the object's bounding box, and those of its children. This base class method simply calls
     * {@code doLayout} on all children. It will be up to overriding subclasses to set the locations of the children
     * relative to each other.
     *
     * @param context the evaluation context
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        // Gather all flow objects contained, and perform layout on them. For parameter reference
        // children, this will re-generate the referenced content

        final List<AbstractDocObjectTemplate> objects = new ArrayList<>(10);

        for (final AbstractDocObjectTemplate child : getChildren()) {

            // Nonwrap spans, Math spans and Fences all lay out without line breaks - they all
            // become a single "flow object".

            // Simple spans (which include wrapping spans), emit their child objects (they are
            // "transparent"). Parameter references will emit either a DocText with the parameter value
            // or the contents of a Span parameter, but are also "transparent" with span values.

            // Allow the child to lay out its own contents. For transparent spans, this lays out
            // child objects recursively. Parameter references will use this method to build a
            // list of resolved layout objects.

            child.doLayout(context, mathMode);

            if (child instanceof final DocSimpleSpan childSpan) {
                childSpan.accumulateFlowObjects(objects);
            } else if (child instanceof final DocParameterReference childParamRef) {
                childParamRef.getLaidOutContents().accumulateFlowObjects(objects);
            } else {
                objects.add(child);
            }
        }

        final BundledFontManager bfm = BundledFontManager.getInstance();
        final Font font = getFont();
        final FontMetrics fm = bfm.getFontMetrics(font);
        final GlyphVector gv = font.createGlyphVector(fm.getFontRenderContext(), "My");
        final int maxCenter = (int) Math.round(-gv.getGlyphOutline(0).getBounds2D().getMinY() * 0.5);

        // Compute the maximum height of any object; this will become the new baseline height for the whole span.
        int maxHeight = 0;
        for (final AbstractDocObjectTemplate obj : objects) {

            int height = 0;
            if (obj.getLeftAlign() == EVAlign.BASELINE) {
                height = obj.getBaseLine();
            } else if (obj.getLeftAlign() == EVAlign.CENTER) {
                height = maxCenter + obj.getCenterLine();
            }

            maxHeight = Math.max(maxHeight, height);
        }

        // Store the baseline and centerline offsets
        setBaseLine(maxHeight);
        setCenterLine(maxHeight - maxCenter);

        // Generate the correct Y values for all objects, tracking the bottom-most point to use as
        // bounds of this object.
        int x = FENCE_PAD;
        int y = 0;

        // Add opening fence width
        switch (this.type) {
            case PARENTHESES -> x += PAREN_WIDTH;
            case BRACKETS -> x += BRACKET_WIDTH;
            case BARS -> x += BARS_WIDTH;
            case BRACES, LBRACE -> x += BRACE_WIDTH;
        }

        for (final AbstractDocObjectTemplate obj : objects) {
            int objY = 0;
            if (obj.getLeftAlign() == EVAlign.BASELINE) {
                objY = getBaseLine() - obj.getBaseLine();
            } else if (obj.getLeftAlign() == EVAlign.CENTER) {
                objY = getCenterLine() - obj.getCenterLine();
            }

            obj.setX(x);
            obj.setY(objY);

            y = Math.max(y, objY + obj.getHeight());
            x += obj.getWidth();
        }

        // Add closing fence width
        switch (this.type) {
            case PARENTHESES -> x += PAREN_WIDTH;
            case BRACKETS -> x += BRACKET_WIDTH;
            case BARS -> x += BARS_WIDTH;
            case BRACES -> x += BRACE_WIDTH;
        }

        x += FENCE_PAD;

        setWidth(x);
        setHeight(y);
    }

    /**
     * Draw the image.
     *
     * @param grx the {@code Graphics} object to which to draw the image
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        int minx = 10000;
        int maxx = 0;
        int miny = 10000;
        int maxy = 0;

        // Determine the bounding box for all contained children
        for (final AbstractDocObjectTemplate obj : getChildren()) {
            final int x = obj.getX();
            minx = Math.min(minx, x);

            final int width = obj.getWidth();
            maxx = Math.max(maxx, x + width);

            final int y = obj.getY();
            miny = Math.min(miny, y);

            final int height = obj.getHeight();
            maxy = Math.max(maxy, y + height);
        }

        prePaint(grx);
        innerPaintComponent(grx);

        final Graphics2D g2d;
        BufferedImage offscreen = null;
        if (grx instanceof Graphics2D) {
            g2d = (Graphics2D) grx;
        } else {
            offscreen = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            g2d = offscreen.createGraphics();
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int w = getWidth();
        final int h = getHeight();
        final Rectangle2D.Double bounds = new Rectangle2D.Double(0, miny, w, h);

        final Color color = ColorNames.getColor(getColorName());
        g2d.setColor(color);

        switch (this.type) {

            case PARENTHESES:
                drawLeftParen(g2d, bounds);
                drawRightParen(g2d, bounds);
                break;

            case BARS:
                final double height = bounds.getHeight();
                final double top = bounds.getMinY();
                final double left = bounds.getMinX() + (double) FENCE_PAD;
                final double right = bounds.getMaxX() - (double) FENCE_PAD;
                final Shape rect1 = new Rectangle2D.Double(left, top, 2.0, height);
                g2d.fill(rect1);
                final Shape rect2 = new Rectangle2D.Double(right - 2.0, top, 2.0, height);
                g2d.fill(rect2);
                break;

            case BRACKETS:
                drawLeftBracket(g2d, bounds);
                drawRightBracket(g2d, bounds);
                break;

            case BRACES:
                drawLeftBrace(g2d, bounds);
                drawRightBrace(g2d, bounds);
                break;

            case LBRACE:
                drawLeftBrace(g2d, bounds);
                break;

            default:
                break;
        }

        if (offscreen != null) {
            grx.drawImage(offscreen, 0, 0, null);
            g2d.dispose();
        }

        // Paint all children
        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.paintComponent(grx, mathMode);
        }

        postPaint(grx);
    }

    /**
     * Draws a left parenthesis at the left end of a bounding box.
     *
     * @param g2d    the {@code Graphics2D} object to which to draw
     * @param bounds the bounding box for the fenced construction
     */
    private void drawLeftParen(final Graphics2D g2d, final Rectangle2D bounds) {

        final double height = bounds.getHeight();

        final double left = bounds.getMinX() + (double) FENCE_PAD;
        final double x1 = left + (double) PAREN_WIDTH;
        final double x2 = left;
        final double x3 = x2 + 2.0;

        final double top = bounds.getMinY();
        final double bottom = top + height;
        final double curveHeight = Math.min(20.0, height * 0.4);

        final double y1 = top;
        final double y2 = top + 1.0;
        final double y3 = top + curveHeight;
        final double y4 = bottom - curveHeight;
        final double y5 = bottom - 1.0;
        final double y6 = bottom;

        // Recall that positive Y is down, and that positive arc angle is counterclockwise

        // Top outer curve
        final double xrad1 = x1 - x2;
        final double yrad1 = y3 - y1;
        final Rectangle2D arc1Bounds = new Rectangle2D.Double(x2, y1, xrad1 * 2.0, yrad1 * 2.0);
        final Shape seg1 = new Arc2D.Double(arc1Bounds, 90.0, 90.0, Arc2D.OPEN);
        final GeneralPath path = new GeneralPath(seg1);

        // Left straight edge
        final Shape line2 = new Line2D.Double(x2, y3, x2, y4);
        path.append(line2, true);

        // Bottom outer curve
        final double xrad3 = x1 - x2;
        final double yrad3 = y6 - y4;
        final Rectangle2D arc3Bounds = new Rectangle2D.Double(x2, y4 - yrad3, xrad3 * 2.0, yrad3 * 2.0);
        final Shape seg3 = new Arc2D.Double(arc3Bounds, 180.0, 90.0, Arc2D.OPEN);
        path.append(seg3, true);

        // Vertical
        final Shape line4 = new Line2D.Double(x1, y6, x1, y5);
        path.append(line4, true);

        // Bottom inner curve
        final double xrad5 = x1 - x3;
        final double yrad5 = y5 - y4;
        final Rectangle2D arc5Bounds = new Rectangle2D.Double(x3, y4 - yrad5, xrad5 * 2.0, yrad5 * 2.0);
        final Shape seg5 = new Arc2D.Double(arc5Bounds, 270.0, -90.0, Arc2D.OPEN);
        path.append(seg5, true);

        // inner straight edge
        final Shape line6 = new Line2D.Double(x3, y4, x3, y3);
        path.append(line6, true);

        // Top inner curve
        final double xrad7 = x1 - x3;
        final double yrad7 = y3 - y2;
        final Rectangle2D arc7Bounds = new Rectangle2D.Double(x3, y2, xrad7 * 2.0, yrad7 * 2.0);
        final Shape seg7 = new Arc2D.Double(arc7Bounds, 180.0, -90.0, Arc2D.OPEN);
        path.append(seg7, true);

        // Top vertical
        path.closePath();

        g2d.fill(path);
    }

    /**
     * Draws a right parenthesis at the left end of a bounding box.
     *
     * @param g2d    the {@code Graphics2D} object to which to draw
     * @param bounds the bounding box for the fenced construction
     */
    private void drawRightParen(final Graphics2D g2d, final Rectangle2D bounds) {

        final double height = bounds.getHeight();
        final double right = bounds.getMaxX() - (double) FENCE_PAD;

        final double x1 = right - (double) PAREN_WIDTH;
        final double x2 = right;
        final double x3 = x2 - 2.0;

        final double top = bounds.getMinY();
        final double bottom = top + height;
        final double curveHeight = Math.min(20.0, height * 0.4);

        final double y1 = top;
        final double y2 = top + 1.0;
        final double y3 = top + curveHeight;
        final double y4 = bottom - curveHeight;
        final double y5 = bottom - 1.0;
        final double y6 = bottom;

        // Recall that positive Y is down, and that positive arc angle is counterclockwise

        // Top outer curve
        final double xrad1 = x2 - x1;
        final double yrad1 = y3 - y1;
        final Rectangle2D arc1Bounds = new Rectangle2D.Double(x1 - xrad1, y1, xrad1 * 2.0, yrad1 * 2.0);
        final Shape seg1 = new Arc2D.Double(arc1Bounds, 90.0, -90.0, Arc2D.OPEN);
        final GeneralPath path = new GeneralPath(seg1);

        // Left straight edge
        final Shape line2 = new Line2D.Double(x2, y3, x2, y4);
        path.append(line2, true);

        // Bottom outer curve
        final double xrad3 = x2 - x1;
        final double yrad3 = y6 - y4;
        final Rectangle2D arc3Bounds = new Rectangle2D.Double(x1 - xrad3, y4 - yrad3, xrad3 * 2.0, yrad3 * 2.0);
        final Shape seg3 = new Arc2D.Double(arc3Bounds, 0.0, -90.0, Arc2D.OPEN);
        path.append(seg3, true);

        // Vertical
        final Shape line4 = new Line2D.Double(x1, y6, x1, y5);
        path.append(line4, true);

        // Bottom inner curve
        final double xrad5 = x3 - x1;
        final double yrad5 = y5 - y4;
        final Rectangle2D arc5Bounds = new Rectangle2D.Double(x1 - xrad5, y4 - yrad5, xrad5 * 2.0, yrad5 * 2.0);
        final Shape seg5 = new Arc2D.Double(arc5Bounds, 270.0, 90.0, Arc2D.OPEN);
        path.append(seg5, true);

        // inner straight edge
        final Shape line6 = new Line2D.Double(x3, y4, x3, y3);
        path.append(line6, true);

        // Top inner curve
        final double xrad7 = x3 - x1;
        final double yrad7 = y3 - y2;
        final Rectangle2D arc7Bounds = new Rectangle2D.Double(x1 - xrad7, y2, xrad7 * 2.0, yrad7 * 2.0);
        final Shape seg7 = new Arc2D.Double(arc7Bounds, 0.0, 90.0, Arc2D.OPEN);
        path.append(seg7, true);

        // Top vertical
        path.closePath();

        g2d.fill(path);
    }

    /**
     * Draws a left bracket at the left end of a bounding box.
     *
     * @param g2d    the {@code Graphics2D} object to which to draw
     * @param bounds the bounding box for the fenced construction
     */
    private void drawLeftBracket(final Graphics2D g2d, final Rectangle2D bounds) {

        final double height = bounds.getHeight();

        final double left = bounds.getMinX() + (double) FENCE_PAD;
        final double x1 = left + (double) BRACKET_WIDTH;
        final double x2 = left;
        final double x3 = x2 + 2.0;

        final double top = bounds.getMinY();
        final double bottom = top + height;

        final double y1 = top;
        final double y2 = top + 1.5;
        final double y3 = bottom - 1.5;
        final double y4 = bottom;

        // Recall that positive Y is down

        final Path2D path = new Path2D.Double();
        path.moveTo(x1, y1);
        path.lineTo(x2, y1);
        path.lineTo(x2, y4);
        path.lineTo(x1, y4);
        path.lineTo(x1, y3);
        path.lineTo(x3, y3);
        path.lineTo(x3, y2);
        path.lineTo(x1, y2);
        path.closePath();

        g2d.fill(path);
    }

    /**
     * Draws a right bracket at the left end of a bounding box.
     *
     * @param g2d    the {@code Graphics2D} object to which to draw
     * @param bounds the bounding box for the fenced construction
     */
    private void drawRightBracket(final Graphics2D g2d, final Rectangle2D bounds) {

        final double height = bounds.getHeight();
        final double right = bounds.getMaxX() - (double) FENCE_PAD;

        final double x1 = right - (double) BRACKET_WIDTH;
        final double x2 = right;
        final double x3 = x2 - 2.0;

        final double top = bounds.getMinY();
        final double bottom = top + height;

        final double y1 = top;
        final double y2 = top + 1.5;
        final double y3 = bottom - 1.5;
        final double y4 = bottom;

        // Recall that positive Y is down

        final Path2D path = new Path2D.Double();
        path.moveTo(x1, y1);
        path.lineTo(x2, y1);
        path.lineTo(x2, y4);
        path.lineTo(x1, y4);
        path.lineTo(x1, y3);
        path.lineTo(x3, y3);
        path.lineTo(x3, y2);
        path.lineTo(x1, y2);
        path.closePath();

        g2d.fill(path);
    }

    /**
     * Draws a left brace at the left end of a bounding box.
     *
     * @param g2d    the {@code Graphics2D} object to which to draw
     * @param bounds the bounding box for the fenced construction
     */
    private void drawLeftBrace(final Graphics2D g2d, final Rectangle2D bounds) {

        final double height = bounds.getHeight();

        final double left = bounds.getMinX() + (double) FENCE_PAD;
        final double x1 = left + (double) BRACE_WIDTH;
        final double x2 = left + (double) BRACE_WIDTH * 0.2;
        final double x3 = left + (double) BRACE_WIDTH * 0.33;
        final double x4 = left;
        final double x5 = left + 2.0 + (double) BRACE_WIDTH * 0.2;
        final double x6 = left + (double) BRACE_WIDTH * 0.66;
        final double x7 = (x4 + x6) * 0.5;

        final double top = bounds.getMinY();
        final double bottom = top + height;
        final double center = (top + bottom) * 0.5;

        final double heightOver3 = height / 3.0;
        final double heightOver6 = height / 6.0;
        final double heightOver9 = height / 9.0;

        final double y1 = top;
        final double y2 = top + 1.0;
        final double y3 = top + heightOver6;
        final double y4 = top + heightOver3;
        final double y5 = y4;
        final double y6 = center - heightOver9;
        final double y7 = center - 1.0;
        final double y8 = center;
        final double y15 = bottom;
        final double y14 = bottom - 1.0;
        final double y13 = bottom - heightOver6;
        final double y12 = bottom - heightOver3;
        final double y11 = y12;
        final double y10 = center + heightOver9;
        final double y9 = center + 1.0;

        // Recall that positive Y is down, and that positive arc angle is counterclockwise

        // Top outer curve
        final double xrad1 = x1 - x2;
        final double yrad1 = y3 - y1;
        final Rectangle2D arc1Bounds = new Rectangle2D.Double(x2, y1, xrad1 * 2.0, yrad1 * 2.0);
        final Shape seg1 = new Arc2D.Double(arc1Bounds, 90.0, 90.0, Arc2D.OPEN);
        final GeneralPath path = new GeneralPath(seg1);

        // Path down to center structure
        final Shape seg2 = new CubicCurve2D.Double(x2, y3, x2, y4, x3, y5, x3, y6);
        path.append(seg2, true);

        // Upper outside arc
        final double xrad35 = x3 - x4;
        final double yrad3 = y7 - y6;
        final Rectangle2D arc3Bounds = new Rectangle2D.Double(x4 - xrad35, y6 - yrad3, xrad35 * 2.0, yrad3 * 2.0);
        final Shape seg3 = new Arc2D.Double(arc3Bounds, 0.0, -90.0, Arc2D.OPEN);
        path.append(seg3, true);

        // Left straight edge
        final Shape line4 = new Line2D.Double(x4, y7, x4, y9);
        path.append(line4, true);

        // Lower outside arc
        final double yrad5 = y10 - y9;
        final Rectangle2D arc5Bounds = new Rectangle2D.Double(x4 - xrad35, y9, xrad35 * 2.0, yrad5 * 2.0);
        final Shape seg5 = new Arc2D.Double(arc5Bounds, 90, -90.0, Arc2D.OPEN);
        path.append(seg5, true);

        // Path down to bottom structure
        final Shape seg6 = new CubicCurve2D.Double(x3, y10, x3, y11, x2, y12, x2, y13);
        path.append(seg6, true);

        // Bottom outer curve
        final double xrad7 = x1 - x2;
        final double yrad7 = y15 - y13;
        final Rectangle2D arc7Bounds = new Rectangle2D.Double(x2, y13 - yrad7, xrad7 * 2.0, yrad7 * 2.0);
        final Shape seg7 = new Arc2D.Double(arc7Bounds, 180.0, 90.0, Arc2D.OPEN);
        path.append(seg7, true);

        // Vertical
        final Shape line8 = new Line2D.Double(x1, y15, x1, y14);
        path.append(line8, true);

        // Bottom inner curve
        final double xrad9 = x1 - x5;
        final double yrad9 = y14 - y13;
        final Rectangle2D arc9Bounds = new Rectangle2D.Double(x5, y13 - yrad9, xrad9 * 2.0, yrad9 * 2.0);
        final Shape seg9 = new Arc2D.Double(arc9Bounds, 270.0, -90.0, Arc2D.OPEN);
        path.append(seg9, true);

        // Path up to center structure
        final Shape seg10 = new CubicCurve2D.Double(x5, y13, x5, y12, x6, y11, x6, y10);
        path.append(seg10, true);

        // Lower inside center arc
        final double xrad1112 = x6 - x7;
        final double yrad11 = y10 - y8;
        final Rectangle2D arc11Bounds = new Rectangle2D.Double(x7 - xrad1112, y8, xrad1112 * 2.0, yrad11 * 2.0);
        final Shape seg11 = new Arc2D.Double(arc11Bounds, 0.0, 90.0, Arc2D.OPEN);
        path.append(seg11, true);

        // Upper inside center arc
        final double yrad12 = y8 - y6;
        final Rectangle2D arc12Bounds = new Rectangle2D.Double(x7 - xrad1112, y6 - yrad12, xrad1112 * 2.0,
                yrad12 * 2.0);
        final Shape seg12 = new Arc2D.Double(arc12Bounds, 270.0, 90.0, Arc2D.OPEN);
        path.append(seg12, true);

        // Path up to top structure
        final Shape seg13 = new CubicCurve2D.Double(x6, y6, x6, y5, x5, y4, x5, y3);
        path.append(seg13, true);

        // Top inner curve
        final double xrad15 = x1 - x5;
        final double yrad15 = y3 - y2;
        final Rectangle2D arc14Bounds = new Rectangle2D.Double(x5, y2, xrad15 * 2.0, yrad15 * 2.0);
        final Shape seg14 = new Arc2D.Double(arc14Bounds, 180.0, -90.0, Arc2D.OPEN);
        path.append(seg14, true);

        // Top vertical
        path.closePath();

        g2d.fill(path);
    }

    /**
     * Draws a right brace at the right end of a bounding box.
     *
     * @param g2d    the {@code Graphics2D} object to which to draw
     * @param bounds the bounding box for the fenced construction
     */
    private void drawRightBrace(final Graphics2D g2d, final Rectangle2D bounds) {

        final double height = bounds.getHeight();

        final double right = bounds.getMaxX() - (double) FENCE_PAD;
        final double x1 = right - (double) BRACE_WIDTH;
        final double x2 = right - (double) BRACE_WIDTH * 0.2;
        final double x3 = right - (double) BRACE_WIDTH * 0.33;
        final double x4 = right;
        final double x5 = right - 2.0 - (double) BRACE_WIDTH * 0.2;
        final double x6 = right - (double) BRACE_WIDTH * 0.66;
        final double x7 = (x4 + x6) * 0.5;

        final double top = bounds.getMinY();
        final double bottom = top + height;
        final double center = (top + bottom) * 0.5;

        final double heightOver3 = height / 3.0;
        final double heightOver6 = height / 6.0;
        final double heightOver9 = height / 9.0;

        final double y1 = top;
        final double y2 = top + 1.0;
        final double y3 = top + heightOver6;
        final double y4 = top + heightOver3;
        final double y5 = y4;
        final double y6 = center - heightOver9;
        final double y7 = center - 1.0;
        final double y8 = center;
        final double y15 = bottom;
        final double y14 = bottom - 1.0;
        final double y13 = bottom - heightOver6;
        final double y12 = bottom - heightOver3;
        final double y11 = y12;
        final double y10 = center + heightOver9;
        final double y9 = center + 1.0;

        // Recall that positive Y is down, and that positive arc angle is counterclockwise

        // Top outer curve
        final double xrad1 = x2 - x1;
        final double yrad1 = y3 - y1;
        final Rectangle2D arc1Bounds = new Rectangle2D.Double(x1 - xrad1, y1, xrad1 * 2.0, yrad1 * 2.0);
        final Shape seg1 = new Arc2D.Double(arc1Bounds, 90.0, -90.0, Arc2D.OPEN);
        final GeneralPath path = new GeneralPath(seg1);

        // Path down to center structure
        final Shape seg2 = new CubicCurve2D.Double(x2, y3, x2, y4, x3, y5, x3, y6);
        path.append(seg2, true);

        // Upper outside arc
        final double xrad35 = x4 - x3;
        final double yrad3 = y7 - y6;
        final Rectangle2D arc3Bounds = new Rectangle2D.Double(x3, y6 - yrad3, xrad35 * 2.0, yrad3 * 2.0);
        final Shape seg3 = new Arc2D.Double(arc3Bounds, 180.0, 90.0, Arc2D.OPEN);
        path.append(seg3, true);

        // Left straight edge
        final Shape line4 = new Line2D.Double(x4, y7, x4, y9);
        path.append(line4, true);

        // Lower outside arc
        final double yrad5 = y10 - y9;
        final Rectangle2D arc5Bounds = new Rectangle2D.Double(x3, y9, xrad35 * 2.0, yrad5 * 2.0);
        final Shape seg5 = new Arc2D.Double(arc5Bounds, 90, 90.0, Arc2D.OPEN);
        path.append(seg5, true);

        // Path down to bottom structure
        final Shape seg6 = new CubicCurve2D.Double(x3, y10, x3, y11, x2, y12, x2, y13);
        path.append(seg6, true);

        // Bottom outer curve
        final double xrad7 = x2 - x1;
        final double yrad7 = y15 - y13;
        final Rectangle2D arc7Bounds = new Rectangle2D.Double(x1 - xrad7, y13 - yrad7, xrad7 * 2.0, yrad7 * 2.0);
        final Shape seg7 = new Arc2D.Double(arc7Bounds, 0.0, -90.0, Arc2D.OPEN);
        path.append(seg7, true);

        // Vertical
        final Shape line8 = new Line2D.Double(x1, y15, x1, y14);
        path.append(line8, true);

        // Bottom inner curve
        final double xrad9 = x5 - x1;
        final double yrad9 = y14 - y13;
        final Rectangle2D arc9Bounds = new Rectangle2D.Double(x1 - xrad9, y13 - yrad9, xrad9 * 2.0, yrad9 * 2.0);
        final Shape seg9 = new Arc2D.Double(arc9Bounds, 270.0, 90.0, Arc2D.OPEN);
        path.append(seg9, true);

        // Path up to center structure
        final Shape seg10 = new CubicCurve2D.Double(x5, y13, x5, y12, x6, y11, x6, y10);
        path.append(seg10, true);

        // Lower inside center arc
        final double xrad1112 = x7 - x6;
        final double yrad11 = y10 - y8;
        final Rectangle2D arc11Bounds = new Rectangle2D.Double(x6, y8, xrad1112 * 2.0, yrad11 * 2.0);
        final Shape seg11 = new Arc2D.Double(arc11Bounds, 180.0, -90.0, Arc2D.OPEN);
        path.append(seg11, true);

        // Upper inside center arc
        final double yrad12 = y8 - y6;
        final Rectangle2D arc12Bounds = new Rectangle2D.Double(x6, y6 - yrad12, xrad1112 * 2.0, yrad12 * 2.0);
        final Shape seg12 = new Arc2D.Double(arc12Bounds, 270.0, -90.0, Arc2D.OPEN);
        path.append(seg12, true);

        // Path up to top structure
        final Shape seg13 = new CubicCurve2D.Double(x6, y6, x6, y5, x5, y4, x5, y3);
        path.append(seg13, true);

        // Top inner curve
        final double xrad15 = x5 - x1;
        final double yrad15 = y3 - y2;
        final Rectangle2D arc14Bounds = new Rectangle2D.Double(x1 - xrad15, y2, xrad15 * 2.0, yrad15 * 2.0);
        final Shape seg14 = new Arc2D.Double(arc14Bounds, 0.0, 90.0, Arc2D.OPEN);
        path.append(seg14, true);

        // Top vertical
        path.closePath();

        g2d.fill(path);
    }

    /**
     * Generates an instance of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance document object
     */
    @Override
    public DocFenceInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float) getFontSize(),
                getFontStyle());

        final List<AbstractDocObjectTemplate> children = getChildren();
        final List<AbstractDocObjectInst> childrenInstList = new ArrayList<>(children.size());

        for (final AbstractDocObjectTemplate child : getChildren()) {
            childrenInstList.add(child.createInstance(evalContext));
        }

        final EFenceType fenceType;
        if (this.type == PARENTHESES) {
            fenceType = EFenceType.PARENTHESES;
        } else if (this.type == BRACKETS) {
            fenceType = EFenceType.BRACKETS;
        } else if (this.type == BARS) {
            fenceType = EFenceType.BARS;
        } else if (this.type == BRACES) {
            fenceType = EFenceType.BRACES;
        } else if (this.type == LBRACE) {
            fenceType = EFenceType.LBRACE;
        } else {
            fenceType = null;
        }

        return new DocFenceInst(objStyle, null, childrenInstList, fenceType, EPrimaryBaseline.CENTERLINE);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        int count = 0;

        xml.add("<fence");
        printFormat(xml, 1.0f);

        switch (this.type) {
            case BRACKETS:
                xml.add(" type='brackets'");
                break;
            case BARS:
                xml.add(" type='bars'");
                break;
            case BRACES:
                xml.add(" type='braces'");
                break;
            case LBRACE:
                xml.add(" type='lbrace'");
                break;
            default:
                break;
        }

        if (getLeftAlign() == EVAlign.BASELINE) {
            xml.add(" valign='baseline'");
        }

        xml.add('>');

        final int size = getChildren().size();

        for (final AbstractDocObjectTemplate child : getChildren()) {
            if (child instanceof DocText) {
                child.toXml(xml, 0);
            } else {
                child.toXml(xml, indent + 1);
            }
        }

        xml.add("</fence>");
    }

    /**
     * Write the LaTeX representation of the object to a string buffer.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file; the value should be updated if the method writes any files
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time (this method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL])
     * @param builder      the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  true to show answers in any inputs embedded in the document; false if answers should not be
     *                     shown
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex,
                        final boolean[] overwriteAll, final HtmlBuilder builder, final boolean showAnswers,
                        final char[] mode, final EvalContext context) {

        int count = 0;

        switch (this.type) {

            case PARENTHESES:
                builder.add("\\left(");
                break;

            case BRACKETS:
                builder.add("\\left[");
                break;

            case BARS:
                builder.add("\\left|");
                break;

            case BRACES:
            case LBRACE:
                builder.add("\\left\\{");
                break;

            default:
                break;
        }

        final int size = getChildren().size();

        for (final AbstractDocObjectTemplate child : getChildren()) {
            count++;

            if ((count != 1) && (count != size)) {
                child.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
            }
        }

        switch (this.type) {

            case PARENTHESES:
                builder.add("\\right)");
                break;

            case BRACKETS:
                builder.add("\\right]");
                break;

            case BARS:
                builder.add("\\right|");
                break;

            case BRACES:
                builder.add("\\right\\}");
                break;

            case LBRACE:
                builder.add("\\right.");
                break;

            default:
                break;
        }
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>Fence");

        printTreeContents(ps);

        ps.print("</li>");
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String innerString = super.toString();
        final int len = innerString.length();

        final HtmlBuilder buf = new HtmlBuilder(len + 2);

        switch (this.type) {
            case PARENTHESES -> buf.add('(');
            case BRACKETS -> buf.add('[');
            case BARS -> buf.add('|');
            case BRACES, LBRACE -> buf.add('{');
        }
        buf.add(innerString);
        switch (this.type) {
            case PARENTHESES -> buf.add(')');
            case BRACKETS -> buf.add(']');
            case BARS -> buf.add('|');
            case BRACES -> buf.add('}');
        }

        return buf.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return innerHashCode() + this.type;
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
        } else if (obj instanceof final DocFence fence) {
            equal = innerEquals(fence) && this.type == fence.type;
        } else {
            equal = false;
        }

        return equal;
    }
}
