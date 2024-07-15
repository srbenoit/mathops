package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.EFenceType;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.EPrimaryBaseline;
import dev.mathops.assessment.document.EVAlign;
import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.document.inst.DocFenceInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
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

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2013064330513943343L;

    /** The type of fence this is. */
    public int type = PARENTHESES;

    /** The set of outlines of the object to draw. */
    private int outLines = 0xFFFF;

    /** The opening fence text. */
    DocText openFence;

    /** The closing fence text. */
    DocText closeFence;

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

        if (this.openFence != null) {
            copy.openFence = this.openFence.deepCopy();
            copy.add(copy.openFence);
        }

        for (final AbstractDocObjectTemplate child : getChildren()) {
            if (child == this.openFence || child == this.closeFence) {
                continue;
            }
            copy.add(child.deepCopy());
        }

        if (this.closeFence != null) {
            copy.closeFence = this.closeFence.deepCopy();
            copy.add(copy.closeFence);
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

        // NOTE: Fences are laid out only for width - their height will be adjusted as needed
        // during rendering

        if (this.openFence != null) {
            this.openFence.doLayout(context, mathMode);
        }
        if (this.closeFence != null) {
            this.closeFence.doLayout(context, mathMode);
        }

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

        // For all baseline objects, find max center height above baseline (this is the
        // "true centerline" as distance above baseline)
        int maxCenter = 0;
        for (final AbstractDocObjectTemplate obj : objects) {
            if (obj.getLeftAlign() == EVAlign.BASELINE) {
                final int center = obj.getBaseLine() - obj.getCenterLine();
                maxCenter = Math.max(maxCenter, center);
            }
        }

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
        int x = 0; // this.openFence.getWidth();
        int y = 0;

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

        x += this.closeFence.getWidth();

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

        Graphics2D g2d = null;
        if (grx instanceof Graphics2D) {
            g2d = (Graphics2D) grx;
        } else {
            Log.warning("FENCE: NULL GRAPHICS");
        }

        // FIXME: We can't print these to a Graphics!!!

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

        // Paint all children

        prePaint(grx);
        innerPaintComponent(grx);

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.paintComponent(grx, mathMode);
        }

        // Now paint the fences
        final int w = this.openFence.getWidth();
        double thick = (double) maxy / 24.0;

        if (thick < 2.0) {
            thick = 2.0;
        }

        if (g2d != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }

        Rectangle2D.Double bounds;
        Arc2D.Double arc;
        GeneralPath path;
        final int maxH = Math.max(getHeight(), maxy - miny);

        final Color color = ColorNames.getColor(getColorName());

        switch (this.type) {

            case PARENTHESES:
                final BufferedImage img = new BufferedImage(maxx - minx, maxH, BufferedImage.TYPE_INT_ARGB);
                final Graphics2D ig = (Graphics2D) (img.getGraphics());
                ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ig.setColor(color);

                final Path2D.Double parenPath = new Path2D.Double();
                double cx = (double) w;
                final double cy = (double) maxH / 2.0;
                parenPath.moveTo(cx, 0.0);
                for (int i = 1; i <= 30; ++i) {
                    final double angle = Math.toRadians(270.0 - (double) (i * 180) / 30.0);
                    parenPath.lineTo(cx + (double) (w - 2) * StrictMath.cos(angle), cy + cy * StrictMath.sin(angle));
                }
                for (int i = 1; i <= 30; ++i) {
                    final double angle = Math.toRadians(90.0 + (double) (i * 180) / 30.0);
                    parenPath.lineTo(cx + ((double) w - 2.0 - thick) * StrictMath.cos(angle), cy
                            + cy * StrictMath.sin(angle));
                }
                parenPath.closePath();

                cx = (double) (maxx - minx - w);
                parenPath.moveTo(cx, 0.0);
                for (int i = 1; i <= 30; ++i) {
                    final double angle = Math.toRadians(270.0 + (double) (i * 180) / 30.0);
                    parenPath.lineTo(cx + (double) (w - 2) * StrictMath.cos(angle), cy + cy * StrictMath.sin(angle));
                }
                for (int i = 1; i <= 30; ++i) {
                    final double angle = Math.toRadians(90.0 - (double) (i * 180) / 30.0);
                    parenPath.lineTo(cx + ((double) w - 2.0 - thick) * StrictMath.cos(angle), cy
                            + cy * StrictMath.sin(angle));
                }
                parenPath.closePath();

                ig.fill(parenPath);

                grx.drawImage(img, minx, miny, minx + w, miny + maxH, 0, 0, w, maxH, null);
                grx.drawImage(img, maxx - w, 0, maxx, miny + maxH, maxx - minx - w, 0, maxx - minx, maxH, null);
                break;

            case BARS:
                grx.drawLine(minx + (w / 2), miny, minx + (w / 2), miny + maxH);
                grx.drawLine(maxx - (w / 2), miny, maxx - (w / 2), miny + maxH);
                break;

            case BRACKETS:
                grx.fillRect(minx + (w / 2) - 2, miny, 2, maxH);
                grx.drawLine(minx + (w / 2) - 2, miny, minx + (w / 2) + 4, miny);
                grx.drawLine(minx + (w / 2) - 2, miny + 1, minx + (w / 2) + 4, miny);
                grx.drawLine(minx + (w / 2) - 2, miny + maxH, minx + (w / 2) + 4, miny + maxH);
                grx.drawLine(minx + (w / 2) - 2, miny + maxH - 1, minx + (w / 2) + 4, miny + maxH);

                grx.fillRect(maxx - (w / 2), miny, 2, maxH);
                grx.drawLine(maxx - (w / 2) + 1, miny, maxx - (w / 2) - 5, miny);
                grx.drawLine(maxx - (w / 2) + 1, miny + 1, maxx - (w / 2) - 5, miny);
                grx.drawLine(maxx - (w / 2) + 1, miny + maxH, maxx - (w / 2) - 5, miny + maxH);
                grx.drawLine(maxx - (w / 2) + 1, miny + maxH - 1, maxx - (w / 2) - 5, miny + maxH);
                break;

            case BRACES:
                // Top point down to vertical, outer edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) - (thick / 2.0), (double) miny,
                        (double) w + thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 90.0, 90.0, Arc2D.OPEN);
                path = new GeneralPath(arc);

                // Vertical down to center point, outer edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) + (thick / 2.0), (double) miny,
                        (double) w - thick, ((double) maxH / 2.0) - 1.0);

                arc = new Arc2D.Double(bounds, (double) 0, -90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Center point up to vertical, inner edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) - (thick / 2.0), (double) miny,
                        (double) w + thick, ((double) maxH/ 2.0) + 1.0);

                arc = new Arc2D.Double(bounds, -90.0, 90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Vertical up to top point, inner edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) + (thick / 2.0), (double) miny,
                        (double) w - thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 180.0, -90.0, Arc2D.OPEN);
                path.append(arc, true);

                if (g2d != null) {
                    g2d.fill(path);
                }

                // Center point down to vertical, outer edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) + (thick / 2.0),
                        (double) (miny + (maxH / 2) + 1), (double) w - thick,
                        ((double) maxH / 2.0) - 1.0);

                arc = new Arc2D.Double(bounds, 90.0, -90.0, Arc2D.OPEN);
                path = new GeneralPath(arc);

                // Vertical down to bottom point, outer edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) - (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0), (double) w + thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 180.0, 90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Bottom point up to vertical, inner edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) + (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0), (double) w - thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 270.0, -90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Vertical up to center point, inner edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) - (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0) - 1.0, (double) w + thick, ((double) maxH / 2.0) + 1.0);

                arc = new Arc2D.Double(bounds, (double) 0, 90.0, Arc2D.OPEN);
                path.append(arc, true);
                if (g2d != null) {
                    g2d.fill(path);
                }

                // Adjust for right-hand brace
                minx = maxx - w;

                // Top point down to vertical, outer edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) - (thick / 2.0), (double) miny,
                        (double) w + thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 90.0, -90.0, Arc2D.OPEN);
                path = new GeneralPath(arc);

                // Vertical down to center point, outer edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) + (thick / 2.0), (double) miny,
                        (double) w - thick, ((double) maxH / 2.0) - 1.0);

                arc = new Arc2D.Double(bounds, 180.0, 90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Center point up to vertical, inner edge
                bounds = new Rectangle2D.Double((double) (minx + (w / 2)) - (thick / 2.0), (double) miny,
                        (double) w + thick, ((double) maxH / 2.0) + 1.0);

                arc = new Arc2D.Double(bounds, 270.0, -90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Vertical up to top point, inner edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) + (thick / 2.0), (double) miny,
                        (double) w - thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 0.0, 90.0, Arc2D.OPEN);
                path.append(arc, true);

                if (g2d != null) {
                    g2d.fill(path);
                }

                // Center point down to vertical, outer edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) + (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0) + 1.0, (double) w - thick, ((double) maxH / 2.0) - 1.0);

                arc = new Arc2D.Double(bounds, 90.0, 90.0, Arc2D.OPEN);
                path = new GeneralPath(arc);

                // Vertical down to bottom point, outer edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) - (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0), (double) w + thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, (double) 0, -90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Bottom point up to vertical, inner edge
                bounds = new Rectangle2D.Double((double) (minx - (w / 2)) + (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0), (double) w - thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 270.0, 90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Vertical up to center point, inner edge
                bounds = new Rectangle2D.Double((double) (minx + (w / 2)) - (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0) - 1.0, (double) w + thick,
                        ((double) maxH / 2.0) + 1.0);

                arc = new Arc2D.Double(bounds, 180.0, -90.0, Arc2D.OPEN);
                path.append(arc, true);
                if (g2d != null) {
                    g2d.fill(path);
                }
                break;

            case LBRACE:
                // Top point down to vertical, outer edge
                bounds = new Rectangle2D.Double((double) (minx + (w / 2)) - (thick / 2.0), (double) miny,
                        (double) w + thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 90.0, 90.0, Arc2D.OPEN);
                path = new GeneralPath(arc);

                // Vertical down to center point, outer edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) + (thick / 2.0), (double) miny,
                        (double) w - thick, ((double) maxH / 2.0) - 1.0);

                arc = new Arc2D.Double(bounds, 0.0, -90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Center point up to vertical, inner edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) - (thick / 2.0), (double) miny,
                        (double) w + thick, ((double) (maxy - miny) / 2.0) + 1.0);

                arc = new Arc2D.Double(bounds, -90.0, 90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Vertical up to top point, inner edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) + (thick / 2.0), (double) miny,
                        (double) w - thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 180.0, -90.0, Arc2D.OPEN);
                path.append(arc, true);

                if (g2d != null) {
                    g2d.fill(path);
                }

                // Center point down to vertical, outer edge
                bounds = new Rectangle2D.Double((double) (minx - (w / 2)) + (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0) + 1.0, (double) w - thick, ((double) maxH / 2.0) - 1.0);

                arc = new Arc2D.Double(bounds, 90.0, -90.0, Arc2D.OPEN);
                path = new GeneralPath(arc);

                // Vertical down to bottom point, outer edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) - (thick / 2.0),
                        (double) (miny + (maxH / 2)), (double) w + thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 180.0, 90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Bottom point up to vertical, inner edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) + (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0), (double) w - thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 270.0, -90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Vertical up to center point, inner edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) - (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0) - 1.0, (double) w + thick, ((double) maxH / 2.0) + 1.0);

                arc = new Arc2D.Double(bounds, 0.0, 90.0, Arc2D.OPEN);
                path.append(arc, true);
                if (g2d != null) {
                    g2d.fill(path);
                }

                // Adjust for right-hand brace
                minx = maxx - w;

                // Top point down to vertical, outer edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) - (thick / 2.0), (double) miny,
                        (double) w + thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 90.0, -90.0, Arc2D.OPEN);
                path = new GeneralPath(arc);

                // Vertical down to center point, outer edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) + (thick / 2.0), (double) miny,
                        (double) w - thick, ((double) maxH / 2.0) - 1.0);

                arc = new Arc2D.Double(bounds, 180.0, 90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Center point up to vertical, inner edge
                bounds = new Rectangle2D.Double((double) (minx + (w / 2)) - (thick / 2.0), (double) miny,
                        (double) w + thick, ((double) maxH / 2.0) + 1.0);

                arc = new Arc2D.Double(bounds, 270.0, -90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Vertical up to top point, inner edge
                bounds = new Rectangle2D.Double((double) (minx - (w / 2)) + (thick / 2.0), (double) miny,
                        (double) w - thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, (double) 0, 90.0, Arc2D.OPEN);
                path.append(arc, true);
                //
                // if (g2d != null) {
                // g2d.fill(path);
                // }

                // Center point down to vertical, outer edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) + (thick / 2.0),
                        ((double) miny + ((double) maxH / 2.0) + 1.0), (double) w - thick, ((double) maxH / 2.0) - 1.0);

                arc = new Arc2D.Double(bounds, 90.0, 90.0, Arc2D.OPEN);
                path = new GeneralPath(arc);

                // Vertical down to bottom point, outer edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) - (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0), (double) w + thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 0.0, -90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Bottom point up to vertical, inner edge
                bounds = new Rectangle2D.Double((double) minx - ((double) w / 2.0) + (thick / 2.0),
                        (double) miny + ((double) maxH / 2.0), (double) w - thick, (double) maxH / 2.0);

                arc = new Arc2D.Double(bounds, 270.0, 90.0, Arc2D.OPEN);
                path.append(arc, true);

                // Vertical up to center point, inner edge
                bounds = new Rectangle2D.Double((double) minx + ((double) w / 2.0) - (thick / 2.0),
                        (double) (miny + (maxH / 2) - 1), (double) w + thick, ((double) maxH / 2.0) + 1.0);

                arc = new Arc2D.Double(bounds, 180.0, -90.0, Arc2D.OPEN);
                path.append(arc, true);
                // if (g2d != null) {
                // g2d.fill(path);
                // }

                break;

            default:
                break;
        }

        postPaint(grx);
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

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
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
        }else if (this.type == BARS) {
            fenceType = EFenceType.BARS;
        }else if (this.type == BRACES) {
            fenceType = EFenceType.BRACES;
        }else if (this.type == LBRACE) {
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
            ++count;

            if ((count != 1) && (count != size)) {

                if (child instanceof DocText) {
                    child.toXml(xml, 0);
                } else {
                    child.toXml(xml, indent + 1);
                }
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

        final HtmlBuilder buf = new HtmlBuilder(50);

        buf.add(this.openFence.toString());
        buf.add(super.toString());
        buf.add(this.closeFence.toString());

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
