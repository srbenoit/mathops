package dev.mathops.assessment.document.template;

import dev.mathops.assessment.Irrational;
import dev.mathops.assessment.document.AxisSpec;
import dev.mathops.assessment.document.AxisTicksSpec;
import dev.mathops.assessment.document.BoundingRect;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.document.GridSpec;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.assessment.document.inst.AbstractPrimitiveInst;
import dev.mathops.assessment.document.inst.DocGraphXYInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.xml.XmlEscaper;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.io.PrintStream;
import java.io.Serial;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A Graph of a 2D function. XML Format: &lt;graph-xy ...&gt; ... &lt;/graph-xy&gt>.
 */
public final class DocGraphXY extends AbstractDocPrimitiveContainer {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3300910122158935786L;

    /** The default tick label font size. */
    private static final int DEFAULT_AXIS_LABEL_SIZE = 20;

    /** The default tick label font size. */
    private static final int DEFAULT_TICK_LABEL_SIZE = 15;

    /** The default tick size. */
    private static final int DEFAULT_TICK_SIZE = 5;

    /** A formatter to truncate tick mark labels. */
    private static final NumberFormat format;

    /** The min x coordinate of the window that the graph shows. */
    private Number windowMinX;

    /** The max x coordinate of the window that the graph shows. */
    private Number windowMaxX;

    /** The min y coordinate of the window that the graph shows. */
    private Number windowMinY;

    /** The max y coordinate of the window that the graph shows. */
    private Number windowMaxY;

    /** The interval between X tick marks and grid lines (0 to hide). */
    Number xTickInterval;

    /** The interval between Y tick marks and grid lines (0 to hide). */
    Number yTickInterval;

    //
    //
    //

    /** The background color of the graph (null for transparent). */
    private String backgroundColorName;

    /** The background color of the graph (null for transparent). */
    private Color backgroundColor;

    /** The width of the graph border (0 to hide). */
    int borderWidth = 1;

    /** The color of the graph border. */
    private String borderColorName;

    /** The color of the graph border. */
    private Color borderColor = Color.BLACK;

    /** The width of the grid lines (set to 0 to hide). */
    int gridWidth = 1;

    /** The color to use when drawing the grid. */
    private String gridColorName;

    /** The color to use when drawing the grid. */
    private Color gridColor = new Color(200, 200, 255);

    /** The width of the tick marks (set to 0 to hide). */
    int tickWidth = 1;

    /** The color to use when drawing the tick marks. */
    private String tickColorName;

    /** The color to use when drawing the tick marks. */
    private Color tickColor = Color.black;

    /** Size of ticks in pixels (odd=centered, even=positive side, 0=hide). */
    int tickSize = DEFAULT_TICK_SIZE;

    /** The width of the axes (set to 0 to hide). */
    int axisWidth = 1;

    /** The color to use when drawing the axes. */
    private String axisColorName;

    /** The color to use when drawing axes. */
    private Color axisColor = Color.gray;

    /** Point size to use when drawing axis labels (0 to hide). */
    int axisLabelSize = DEFAULT_AXIS_LABEL_SIZE;

    /** Point size to use when drawing tick labels (0 to hide). */
    int tickLabelSize = DEFAULT_TICK_LABEL_SIZE;

    /** The label for the X axis. */
    String xAxisLabel = "x";

    /** The label for the Y axis. */
    String yAxisLabel = "y";

    static {
        format = new DecimalFormat();
        format.setMaximumFractionDigits(3);
    }

    /**
     * Construct a new {@code DocGraphXY}.
     *
     * @param width  the width of the object
     * @param height the height of the object
     * @param theAltText the alternative text for the generated image for accessibility
     */
    DocGraphXY(final int width, final int height, final String theAltText) {

        super(width, height, theAltText);

        // Set up a default window (-5 to 5 on both axes, like TI-83)
        this.windowMinX = Long.valueOf(-5L);
        this.windowMaxX = Long.valueOf(5L);
        this.windowMinY = Long.valueOf(-5L);
        this.windowMaxY = Long.valueOf(5L);
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocGraphXY deepCopy() {

        final String alt = getAltText();
        final DocGraphXY copy = new DocGraphXY(this.origWidth, this.origHeight, alt);

        copy.copyObjectFromContainer(this);

        copy.windowMinX = this.windowMinX;
        copy.windowMaxX = this.windowMaxX;
        copy.windowMinY = this.windowMinY;
        copy.windowMaxY = this.windowMaxY;
        copy.xTickInterval = this.xTickInterval;
        copy.yTickInterval = this.yTickInterval;

        copy.backgroundColorName = this.backgroundColorName;
        copy.backgroundColor = this.backgroundColor;
        copy.borderWidth = this.borderWidth;
        copy.borderColor = this.borderColor;
        copy.gridWidth = this.gridWidth;
        copy.gridColor = this.gridColor;
        copy.tickWidth = this.tickWidth;
        copy.tickColor = this.tickColor;
        copy.tickSize = this.tickSize;
        copy.axisWidth = this.axisWidth;
        copy.axisColor = this.axisColor;
        copy.axisLabelSize = this.axisLabelSize;
        copy.tickLabelSize = this.tickLabelSize;
        copy.xAxisLabel = this.xAxisLabel;
        copy.yAxisLabel = this.yAxisLabel;

        for (final AbstractDocPrimitive prim : getPrimitives()) {
            copy.addPrimitive(prim.deepCopy(copy));
        }

        return copy;
    }

//    /**
//     * Get the left edge of the window.
//     *
//     * @return the minimum X value of the window
//     */
//    public Number getMinX() {
//
//        return this.windowMinX;
//    }

//    /**
//     * Get the right edge of the window.
//     *
//     * @return the maximum X value of the window
//     */
//    public Number getMaxX() {
//
//        return this.windowMaxX;
//    }

//    /**
//     * Get the top edge of the window.
//     *
//     * @return the minimum Y value of the window
//     */
//    public Number getMinY() {
//
//        return this.windowMinY;
//    }

//    /**
//     * Get the bottom edge of the window.
//     *
//     * @return the maximum Y value of the window
//     */
//    public Number getMaxY() {
//
//        return this.windowMaxY;
//    }

    /**
     * Set the graph window.
     *
     * @param minX the left edge of the window
     * @param maxX the right edge of the window
     * @param minY the top edge of the window
     * @param maxY the bottom edge of the window
     */
    void setWindow(final Number minX, final Number maxX, final Number minY, final Number maxY) {

        if (minX == null || maxX == null || minY == null || maxY == null) {
            Log.warning("Missing window parameter - ignoring.");
        } else {
            this.windowMinX = minX;
            this.windowMaxX = maxX;
            this.windowMinY = minY;
            this.windowMaxY = maxY;
        }
    }

//    /**
//     * Gets the background color name.
//     *
//     * @return the color name
//     */
//    public String getBackgroundColorName() {
//
//        return this.backgroundColorName;
//    }

//    /**
//     * Get the background color.
//     *
//     * @return the background color
//     */
//    public Color getBackgroundColor() {
//
//        return this.backgroundColor;
//    }

    /**
     * Set the background color.
     *
     * @param name  the name of the color
     * @param color the background color
     */
    void setBackgroundColor(final String name, final Color color) {

        this.backgroundColorName = name;
        this.backgroundColor = color;
    }

//    /**
//     * Gets the border color name.
//     *
//     * @return the color name
//     */
//    public String getBorderColorName() {
//
//        return this.borderColorName;
//    }

//    /**
//     * Get the border color.
//     *
//     * @return the border color
//     */
//    public Color getBorderColor() {
//
//        return this.borderColor;
//    }

    /**
     * Set the border color.
     *
     * @param name  the name of the color
     * @param color the border color
     */
    void setBorderColor(final String name, final Color color) {

        this.borderColorName = name;
        this.borderColor = color;
    }

//    /**
//     * Get the grid color.
//     *
//     * @return the grid color
//     */
//    public Color getGridColor() {
//
//        return this.gridColor;
//    }

    /**
     * Set the grid color.
     *
     * @param name  the name of the color
     * @param color the grid color
     */
    void setGridColor(final String name, final Color color) {

        this.gridColorName = name;
        this.gridColor = color;
    }

//    /**
//     * Get the tick color.
//     *
//     * @return the tick color
//     */
//    public Color getTickColor() {
//
//        return this.tickColor;
//    }

    /**
     * Set the tick color.
     *
     * @param name  the name of the color
     * @param color the tick color
     */
    void setTickColor(final String name, final Color color) {

        this.tickColorName = name;
        this.tickColor = color;
    }

//    /**
//     * Get the axis color.
//     *
//     * @return the axis color
//     */
//    public Color getAxisColor() {
//
//        return this.axisColor;
//    }

    /**
     * Set the axis color.
     *
     * @param name  the name of the color
     * @param color the axis color
     */
    void setAxisColor(final String name, final Color color) {

        this.axisColorName = name;
        this.axisColor = color;
    }

    /**
     * Get the left alignment for the object.
     *
     * @return the object insets
     */
    @Override
    public int getLeftAlign() {

        return BASELINE;
    }

    /**
     * Generate the string label for a tick mark, given the value of the point along the axis, and the tick mark
     * interval. The string is chosen as the nearest multiple of the interval to the point.
     *
     * @param val      the value being labeled
     * @param interval the interval between tick marks
     * @return the formatted string
     */
    private static String tickLabel(final double val, final double interval) {

        final int mult;

        if (val > 0.0) {
            mult = (int) ((val + (interval / 2.0)) / interval);
        } else {
            mult = (int) ((val - (interval / 2.0)) / interval);
        }

        String lbl = format.format((double) mult * interval);

        if (lbl.charAt(0) == '-') {
            lbl = "\u2212" + lbl.substring(1);
        }

        return lbl;
    }

    /**
     * Draw the graph to an off-screen image.
     *
     * @param forceWhite true to force background rectangle to be white if it is the first primitive in the drawing
     *                   and it is filled
     * @param context    the evaluation context
     */
    @Override
    public void buildOffscreen(final boolean forceWhite, final EvalContext context) {

        if (context != null) {
            innerCreateOffscreen(getWidth(), getHeight());
            populateOffscreen(context);
        }
    }

    /**
     * Draw the graph to an off-screen image.
     *
     * @param context the evaluation context
     */
    private void populateOffscreen(final EvalContext context) {

        final Graphics2D g2d = (Graphics2D) (getOffscreen().getGraphics());

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Copy any incoming parameters into the new parameter set.
        for (final String name : context.getVariableNames()) {
            context.addVariable(context.getVariable(name));
        }

        // Compute edges of graph, consisting of bounds adjusted by insets.
        final Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());

        // Draw the background
        if (this.backgroundColor != null) {
            g2d.setColor(this.backgroundColor);
        } else {
            g2d.setColor(Color.white);
        }

        g2d.fillRect(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1);

        // Draw the border if configured
        g2d.setColor(this.borderColor);

        for (int i = 0; i < this.borderWidth; i++) {
            g2d.drawRect(bounds.x + i, bounds.y + i, bounds.width - (i << 1) - 1, bounds.height - (i << 1) - 1);
        }

        // Adjust bounding box based on border size
        bounds.x += this.borderWidth;
        bounds.width -= 2 * this.borderWidth;
        bounds.y += this.borderWidth;
        bounds.height -= 2 * this.borderWidth;

        g2d.setClip(bounds);

        // Locate the X and Y axes.
        final double minX = this.windowMinX.doubleValue();
        final double minY = this.windowMinY.doubleValue();
        final double maxX = this.windowMaxX.doubleValue();
        final double maxY = this.windowMaxY.doubleValue();

        int x = (int) ((double) bounds.width * (-minX) / (maxX - minX));
        final int axisX = ((x > 0) && (x < bounds.width)) ? x : -1;
        int y = (int) ((double) bounds.height * (-minY) / (maxY - minY));
        final int axisY = ((y > 0) && (y < bounds.height)) ? y : -1;

        double actualXTickInterval = 0.0;
        if (this.xTickInterval != null) {
            actualXTickInterval = this.xTickInterval.doubleValue();
        }

        // Draw the grid and tick marks if configured
        if (actualXTickInterval != 0.0 && (this.gridWidth > 0 || this.tickWidth > 0)) {

            if (this.tickLabelSize > 0) {
                Font theFont = getFont();
                theFont = theFont.deriveFont(theFont.getStyle() & (BOLD | ITALIC),
                        (float) this.tickLabelSize * getScale());
                g2d.setFont(theFont);
            }

            final FontMetrics fm = g2d.getFontMetrics();

            // Do the vertical grid lines, relative to the X axis
            double per = (maxX - minX) / (double) bounds.width;
            int prior = (int) ((minX - per) / actualXTickInterval);

            for (double dx = minX; dx < maxX; dx += per) {
                final int cur = (int) (dx / actualXTickInterval);

                if (cur != prior) {
                    prior = cur;

                    // Draw a vertical grid line at 'dx'
                    x = (int) ((double) bounds.width * (dx - minX) / (maxX - minX));
                    x -= this.gridWidth / 2;
                    g2d.setColor(this.gridColor);

                    for (int i = 0; i < this.gridWidth; i++) {

                        if (x > 0 && x < bounds.width) {
                            g2d.drawLine(bounds.x + x, bounds.y, bounds.x + x, bounds.y + bounds.height);
                        }

                        ++x;
                    }

                    if (axisY == -1) {
                        continue;
                    }

                    // Draw a vertical tick at 'dx'
                    g2d.setColor(this.tickColor);
                    x = (int) ((double) bounds.width * (dx - minX) / (maxX - minX));

                    // Draw tick mark label if configured
                    if (this.tickLabelSize > 0) {
                        if (this.xTickInterval instanceof final Irrational irr) {
                            final double factorValue = irr.getFactorValue();

                            // Fractions of factor value as tick labels
                            final double scale = dx / factorValue;
                            final int top = (int) Math.round(scale * (double) irr.denominator);
                            final BigInteger bigTop = new BigInteger(Integer.toString(top));
                            final BigInteger bigBot = new BigInteger(Long.toString(irr.denominator));
                            final int gcd = bigTop.gcd(bigBot).intValue();
                            final int reducedTop = top / gcd;
                            final int reducedBot = (int) (irr.denominator / (long) gcd);

                            final String upper;
                            if (reducedTop == 1) {
                                upper = irr.getFactorString();
                            } else if (reducedTop == -1) {
                                upper = "\u2212" + irr.getFactorString();
                            } else if (reducedTop > 0) {
                                upper = reducedTop + irr.getFactorString();
                            } else {
                                // Negative value: print using the proper minus sign
                                upper = "\u2212" + Math.abs(reducedTop) + irr.getFactorString();
                            }

                            if (reducedBot == 1) {
                                final int upperWidth = fm.stringWidth(upper);
                                final int topY = bounds.y + bounds.height - axisY + fm.getHeight();
                                final int leftx = bounds.x + x;

                                g2d.drawString(upper, leftx - (upperWidth / 2) + 1, topY);
                            } else {
                                final String lower = Integer.toString(reducedBot);

                                final int upperWidth = fm.stringWidth(upper);
                                final int lowerWidth = fm.stringWidth(lower);
                                final int maxWidth = Math.max(upperWidth, lowerWidth);
                                final int topY = bounds.y + bounds.height - axisY + fm.getHeight();
                                final int midY = topY + fm.getHeight();
                                final int leftx = bounds.x + x;
                                final int lineY = topY + 2;

                                g2d.drawString(upper, leftx - (upperWidth / 2) + 1, topY);
                                g2d.drawString(lower, leftx - (lowerWidth / 2) + 1, midY);
                                g2d.drawLine(leftx - (maxWidth / 2) + 1, lineY, leftx + (maxWidth / 2) + 1, lineY);
                            }
                        } else {
                            // Decimal tick labels
                            final String lbl = tickLabel(dx, actualXTickInterval);

                            if ((this.tickSize % 2) == 0) {
                                g2d.drawString(lbl, bounds.x + x - (fm.stringWidth(lbl) / 2) + 1,
                                        bounds.y + bounds.height - axisY + fm.getHeight());
                            } else {
                                g2d.drawString(lbl, bounds.x + x - (fm.stringWidth(lbl) / 2) + 1,
                                        bounds.y + bounds.height - axisY + fm.getHeight() + (this.tickSize / 2));
                            }
                        }
                    }

                    // Draw the tick mark
                    x -= this.tickWidth / 2;

                    for (int i = 0; i < this.tickWidth; i++) {

                        if (x > 0 && x < bounds.width) {

                            if ((this.tickSize % 2) == 0) {
                                g2d.drawLine(bounds.x + x, bounds.y + bounds.height - axisY - this.tickSize,
                                        bounds.x + x, bounds.y + bounds.height - axisY);
                            } else {
                                g2d.drawLine(bounds.x + x, bounds.y + bounds.height - axisY - (this.tickSize / 2),
                                        bounds.x + x, bounds.y + bounds.height - axisY + (this.tickSize / 2));
                            }
                        }

                        ++x;
                    }
                }
            }

            double actualYTickInterval = 0.0;
            if (this.yTickInterval != null) {
                actualYTickInterval = this.yTickInterval.doubleValue();
            }

            if (actualYTickInterval != 0.0 && (this.gridWidth > 0 || this.tickWidth > 0)) {

                // Do the horizontal grid lines, relative to the Y axis
                per = (maxY - minY) / (double) bounds.height;
                prior = (int) ((minY - per) / actualYTickInterval);

                for (double dy = minY; dy < maxY; dy += per) {
                    final int cur = (int) (dy / actualYTickInterval);

                    if (cur != prior) {
                        prior = cur;

                        // Draw a horizontal grid line at 'dy'
                        y = (int) ((double) bounds.height * (dy - minY) / (maxY - minY));
                        y -= this.gridWidth / 2;
                        g2d.setColor(this.gridColor);

                        for (int i = 0; i < this.gridWidth; i++) {
                            if (y > 0 && y < bounds.height) {
                                g2d.drawLine(bounds.x, bounds.y + bounds.height - y, bounds.x + bounds.width,
                                        bounds.y + bounds.height - y);
                            }

                            ++y;
                        }

                        if (axisX == -1) {
                            continue;
                        }

                        // Draw a horizontal tick at 'dy'
                        g2d.setColor(this.tickColor);
                        y = (int) ((double) bounds.height * (dy - minY) / (maxY - minY));

                        // Draw tick mark label if configured
                        if (this.tickLabelSize > 0) {
                            if (this.yTickInterval instanceof final Irrational irr) {
                                final double factorValue = irr.getFactorValue();

                                // Fractions of factor value as tick labels
                                final double scale = dy / factorValue;
                                final int top = (int) Math.round(scale * (double) irr.denominator);
                                final BigInteger bigTop = new BigInteger(Integer.toString(top));
                                final BigInteger bigBot = new BigInteger(Long.toString(irr.denominator));
                                final int gcd = bigTop.gcd(bigBot).intValue();
                                final int reducedTop = top / gcd;
                                final int reducedBot = (int) (irr.denominator / (long) gcd);

                                final String upper;
                                if (reducedTop == 1) {
                                    upper = irr.getFactorString();
                                } else if (reducedTop == -1) {
                                    upper = "\u2212" + irr.getFactorString();
                                } else if (reducedTop > 0) {
                                    upper = reducedTop + irr.getFactorString();
                                } else {
                                    // Negative value: print using the proper minus sign
                                    upper = "\u2212" + Math.abs(reducedTop) + irr.getFactorString();
                                }

                                if (reducedBot == 1) {
                                    final int upperWidth = fm.stringWidth(upper);
                                    final int topY = bounds.y + bounds.height - y + fm.getAscent() / 2 - 1;
                                    final int leftx = bounds.x + axisX - upperWidth - 2;

                                    g2d.drawString(upper, leftx - (upperWidth / 2) + 1, topY);
                                } else {
                                    final String lower = Integer.toString(reducedBot);

                                    final int upperWidth = fm.stringWidth(upper);
                                    final int lowerWidth = fm.stringWidth(lower);
                                    final int maxWidth = Math.max(upperWidth, lowerWidth);
                                    final int midY = bounds.y + bounds.height - y - 1;
                                    final int topY = midY - 2;
                                    final int botY = midY + fm.getAscent();
                                    final int leftx = bounds.x + axisX - maxWidth - 2;

                                    g2d.drawString(upper, leftx - (upperWidth / 2) + 1, topY);
                                    g2d.drawString(lower, leftx - (lowerWidth / 2) + 1, botY);
                                    g2d.drawLine(leftx - (maxWidth / 2) + 1, midY, leftx + (maxWidth / 2) + 1, midY);
                                }
                            } else {
                                // Decimal labels
                                final String lbl = tickLabel(dy, actualYTickInterval);

                                if ((this.tickSize % 2) == 0) {
                                    g2d.drawString(lbl, bounds.x + axisX - fm.stringWidth(lbl) - 2,
                                            bounds.y + bounds.height - y + (fm.getHeight() / 2) - 1);
                                } else {
                                    g2d.drawString(lbl,
                                            bounds.x + axisX - fm.stringWidth(lbl) - 2 - (this.tickSize / 2),
                                            bounds.y + bounds.height - y + (fm.getHeight() / 2) - 1);
                                }
                            }
                        }

                        y -= this.tickWidth / 2;

                        for (int i = 0; i < this.tickWidth; i++) {
                            if (y > 0 && y < bounds.height) {
                                if ((this.tickSize % 2) == 0) {
                                    g2d.drawLine(bounds.x + axisX, bounds.y + bounds.height - y,
                                            bounds.x + axisX + this.tickSize, bounds.y + bounds.height - y);
                                } else {
                                    g2d.drawLine(bounds.x + axisX - (this.tickSize / 2), bounds.y + bounds.height - y,
                                            bounds.x + axisX + (this.tickSize / 2), bounds.y + bounds.height - y);
                                }
                            }

                            ++y;
                        }
                    }
                }
            }
        }

        // Draw the axes if configured, and if visible
        g2d.setColor(this.axisColor);
        x = axisX - (this.axisWidth / 2);

        for (int i = 0; i < this.axisWidth; i++) {

            if (x > 0 && x < bounds.width) {
                g2d.drawLine(bounds.x + x, bounds.y, bounds.x + x, bounds.y + bounds.height);
            }

            ++x;
        }

        y = axisY - (this.axisWidth / 2);

        for (int i = 0; i < this.axisWidth; i++) {

            if (y > 0 && y < bounds.height) {
                g2d.drawLine(bounds.x, bounds.y + bounds.height - y, bounds.x + bounds.width,
                        bounds.y + bounds.height - y);
            }

            ++y;
        }

        // Label the axes if configured
        if (this.axisLabelSize > 0 && ((axisX != -1) || (axisY != -1))) {
            g2d.setColor(this.axisColor);
            Font theFont = getFont();
            theFont = theFont.deriveFont(theFont.getStyle() & (BOLD | ITALIC), (float) this.axisLabelSize * getScale());
            g2d.setFont(theFont);
            final FontMetrics fm = g2d.getFontMetrics();

            if (axisY != -1) {
                x = bounds.x + axisX + 4;
                y = bounds.y + fm.getAscent();
                g2d.drawString(this.yAxisLabel, x, y);
            }

            if (axisX != -1) {
                x = bounds.x + bounds.width - 2 - fm.stringWidth(this.xAxisLabel);
                y = bounds.y + bounds.height - axisY - fm.getDescent();
                g2d.drawString(this.xAxisLabel, x, y);
            }
        }

        // Draw the functions and drawing primitives
        for (final AbstractDocPrimitive primitive : getPrimitives()) {

            if (primitive instanceof final DocPrimitiveFormula formula) {
                formula.setWindow(this.windowMinX, this.windowMaxX, this.windowMinY, this.windowMaxY);
                formula.setBounds(bounds);
            }

            primitive.draw(g2d, context);
        }
    }

    /**
     * Generates an instance of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance document object; null if unable to create the instance
     */
    @Override
    public DocGraphXYInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        final StrokeStyle borderStyle = this.borderWidth == 0 ? null : new StrokeStyle((double) this.borderWidth,
                this.borderColorName, null, 1.0, EStrokeCap.BUTT, EStrokeJoin.MITER, 10.0f);

        final List<AbstractDocPrimitive> primitives = getPrimitives();
        final List<AbstractPrimitiveInst> primitivesInstList = new ArrayList<>(primitives.size());

        for (final AbstractDocPrimitive primitive : primitives) {
            primitivesInstList.add(primitive.createInstance(evalContext));
        }

        final double minX = this.windowMinX.doubleValue();
        final double minY = this.windowMinY.doubleValue();
        final double maxX = this.windowMaxX.doubleValue();
        final double maxY = this.windowMaxY.doubleValue();
        final BoundingRect bounds = new BoundingRect(minX, minY, maxX - minX, maxY - minY);

        final GridSpec grid = new GridSpec(this.gridWidth, this.gridColorName);

        final StrokeStyle axisStroke = new StrokeStyle((double) this.axisWidth, this.axisColorName, null, 1.0,
                EStrokeCap.BUTT, EStrokeJoin.MITER, 10.0f);

        final int tickPosLen;
        final int tickNegLen;
        if ((this.tickSize & 0x01) == 0x01) {
            tickPosLen = this.tickSize / 2;
            tickNegLen = tickPosLen;
        } else  {
            tickPosLen = this.tickSize;
            tickNegLen = 0;
        }

        final boolean hasTicks = this.tickWidth > 9 && this.tickSize > 0 && this.tickColorName != null;

        final AxisTicksSpec xTicks = hasTicks ?
                new AxisTicksSpec(this.tickWidth, tickPosLen, tickNegLen,
                this.tickColorName, this.xTickInterval, (float) this.tickLabelSize, "black") : null;

        final AxisTicksSpec yTicks = hasTicks ?
                new AxisTicksSpec(this.tickWidth, tickPosLen, tickNegLen,
                        this.tickColorName, this.yTickInterval, (float) this.tickLabelSize, "black") : null;

        final AxisSpec xAxis = new AxisSpec(axisStroke, this.xAxisLabel, (float) this.axisLabelSize, "black", xTicks);
        final AxisSpec yAxis = new AxisSpec(axisStroke, this.yAxisLabel, (float) this.axisLabelSize, "black", yTicks);

        final String alt = getAltText();
        final String actualAlt = alt == null ? null : generateStringContents(evalContext, alt);

        return new DocGraphXYInst(objStyle, this.backgroundColorName, getWidth(), getHeight(), actualAlt,
                borderStyle, primitivesInstList, bounds, grid, xAxis, yAxis);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add("<graphxy height='", Integer.toString(getHeight()), "' width='", Integer.toString(getWidth()), "'");

        printFormat(xml, 1.0f);

        xml.add(" minx='", this.windowMinX, "' miny='", this.windowMinY, "' maxx='", this.windowMaxX, "' maxy='",
                this.windowMaxY, "'");

        if (this.xTickInterval != null) {
            xml.add(" xtickinterval='", this.xTickInterval, "'");
        }

        if (this.yTickInterval != null) {
            xml.add(" ytickinterval='", this.yTickInterval, "'");
        }

        if (this.backgroundColorName != null) {
            xml.add(" bgcolor='", this.backgroundColorName, "'");
        }

        if (this.borderColorName != null) {
            xml.add(" bordercolor='", this.borderColorName, "'");
        }

        if (this.gridColorName != null) {
            xml.add(" gridcolor='", this.gridColorName, "'");
        }

        if (this.tickColorName != null) {
            xml.add(" tickcolor='", this.tickColorName, "'");
        }

        if (this.axisColorName != null) {
            xml.add(" axiscolor='", this.axisColorName, "'");
        }

        if (this.borderWidth != 1) {
            xml.add(" borderwidth='", Integer.toString(this.borderWidth), "'");
        }

        if (this.gridWidth != 1) {
            xml.add(" gridwidth='", Integer.toString(this.gridWidth), "'");
        }

        if (this.tickWidth != 1) {
            xml.add(" tickwidth='", Integer.toString(this.tickWidth), "'");
        }

        if (this.tickSize != DEFAULT_TICK_SIZE) {
            xml.add(" ticksize='", Integer.toString(this.tickSize), "'");
        }

        if (this.axisWidth != 1) {
            xml.add(" axiswidth='", Integer.toString(this.axisWidth), "'");
        }

        if (this.axisLabelSize != DEFAULT_AXIS_LABEL_SIZE) {
            xml.add(" axislabelfontsize='", Integer.toString(this.axisLabelSize), "'");
        }

        if (this.tickLabelSize != DEFAULT_TICK_LABEL_SIZE) {
            xml.add(" ticklabelfontsize='", Integer.toString(this.tickLabelSize), "'");
        }

        if (!"x".equals(this.xAxisLabel)) {
            xml.add(" xaxislabel='", this.xAxisLabel, "'");
        }

        if (!"y".equals(this.yAxisLabel)) {
            xml.add(" yaxislabel='", this.yAxisLabel, "'");
        }

        final String alt = getAltText();
        if (alt != null) {
            xml.add(" alt='", XmlEscaper.escape(alt), "'");
        }

        xml.add('>');

        appendPrimitivesXml(xml, indent + 1);

        xml.add("</graphxy>");
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>X-Y Graph");
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

        return "[X-Y GRAPH]";
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return primitiveContainerHashCode()
                + Objects.hashCode(this.windowMinX)
                + Objects.hashCode(this.windowMaxX)
                + Objects.hashCode(this.windowMinY)
                + Objects.hashCode(this.windowMaxY)
                + Objects.hashCode(this.xTickInterval)
                + Objects.hashCode(this.yTickInterval)
                + Objects.hashCode(this.backgroundColorName)
                + this.borderWidth
                + Objects.hashCode(this.borderColorName)
                + this.gridWidth
                + Objects.hashCode(this.gridColorName)
                + this.tickWidth
                + Objects.hashCode(this.tickColorName)
                + this.tickSize
                + this.axisWidth
                + Objects.hashCode(this.axisColorName)
                + this.axisLabelSize
                + this.tickLabelSize
                + Objects.hashCode(this.xAxisLabel)
                + Objects.hashCode(this.yAxisLabel)
                + Objects.hashCode(getPrimitives());
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
        } else if (obj instanceof final DocGraphXY graph) {
            equal = primitiveContainerEquals(graph)
                    && Objects.equals(this.windowMinX, graph.windowMinX)
                    && Objects.equals(this.windowMaxX, graph.windowMaxX)
                    && Objects.equals(this.windowMinY, graph.windowMinY)
                    && Objects.equals(this.windowMaxY, graph.windowMaxY)
                    && Objects.equals(this.xTickInterval, graph.xTickInterval)
                    && Objects.equals(this.yTickInterval, graph.yTickInterval)
                    && Objects.equals(this.backgroundColorName,
                    graph.backgroundColorName)
                    && this.borderWidth == graph.borderWidth
                    && Objects.equals(this.borderColorName, graph.borderColorName)
                    && this.gridWidth == graph.gridWidth
                    && Objects.equals(this.gridColorName, graph.gridColorName)
                    && this.tickWidth == graph.tickWidth
                    && Objects.equals(this.tickColorName, graph.tickColorName)
                    && this.tickSize == graph.tickSize
                    && this.axisWidth == graph.axisWidth
                    && Objects.equals(this.axisColorName, graph.axisColorName)
                    && this.axisLabelSize == graph.axisLabelSize
                    && this.tickLabelSize == graph.tickLabelSize
                    && Objects.equals(this.xAxisLabel, graph.xAxisLabel)
                    && Objects.equals(this.yAxisLabel, graph.yAxisLabel)
                    && Objects.equals(getPrimitives(), graph.getPrimitives());
        } else {
            equal = false;
        }

        return equal;
    }
}
