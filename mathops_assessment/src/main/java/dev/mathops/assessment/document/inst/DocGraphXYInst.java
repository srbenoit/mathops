package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.AxisSpec;
import dev.mathops.assessment.document.BoundingRect;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.assessment.document.GridSpec;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;

import java.util.List;
import java.util.Objects;

/**
 * An instance of a graph that supports with axes and 2D functions.
 */
public final class DocGraphXYInst extends AbstractPrimitiveContainerInst {

    /** The window that the graph shows. */
    private final BoundingRect window;

    /** The grid for the graph ({@code null} if there is no grid). */
    private final GridSpec grid;

    /** The specification for the x-axis; {@code null} if no x-axis to be drawn). */
    private final AxisSpec xAxis;

    /** The specification for the x-axis; {@code null} if no x-axis to be drawn). */
    private final AxisSpec yAxis;

    /**
     * Construct a new {@code DocGraphXYInst}.
     *
     * @param theStyle       the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theWidth       the width of the object
     * @param theHeight      the height of the object
     * @param theAltText     the alternative text for accessibility of generated images
     * @param theBorder      the border specification; {@code null} if there is no border
     * @param thePrimitives  the list of primitives
     * @param theWindow      the window the graph displays
     * @param theGrid        the grid specification; {@code null} if there is no grid
     * @param theXAxis       the specification of the x-axis
     * @param theYAxis       the specification of the y-axis
     */
    public DocGraphXYInst(final DocObjectInstStyle theStyle, final String theBgColorName, final int theWidth,
                          final int theHeight, final String theAltText, final StrokeStyle theBorder,
                          final List<? extends AbstractPrimitiveInst> thePrimitives, final BoundingRect theWindow,
                          final GridSpec theGrid, final AxisSpec theXAxis, final AxisSpec theYAxis) {

        super(theStyle, theBgColorName, theWidth, theHeight, theAltText, theBorder, thePrimitives);

        if (theWindow == null) {
            throw new IllegalArgumentException("Graph window may not be null");
        }

        this.window = theWindow;
        this.grid = theGrid;
        this.xAxis = theXAxis;
        this.yAxis = theYAxis;
    }

    /**
     * Gets the window that the graph shows.
     *
     * @return the window
     */
    public BoundingRect getWindow() {

        return this.window;
    }

    /**
     * Gets the grid specification.
     *
     * @return the grid specification; {@code null} if there is no grid
     */
    public GridSpec getGrid() {

        return this.grid;
    }

    /**
     * Gets the x-axis specification.
     *
     * @return the x-axis specification; {@code null} if the x-axis is not to be drawn
     */
    public AxisSpec getXAxis() {

        return this.xAxis;
    }

    /**
     * Gets the y-axis specification.
     *
     * @return the y-axis specification; {@code null} if the y-axis is not to be drawn
     */
    public AxisSpec getYAxis() {

        return this.yAxis;
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        final String ind = makeIndent(indent);

        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.add(ind);
        }
        xml.add("<graphxy");
        addPrimitiveContainerInstXmlAttributes(xml); // width, height, border, bgColor, style attributes

        final double minX = this.window.getX();
        final double minY = this.window.getY();
        final double maxX = minX + this.window.getWidth();
        final double maxY = minY + this.window.getHeight();

        xml.addAttribute("minx", Double.toString(minX), 0);
        xml.addAttribute("maxx", Double.toString(maxX), 0);
        xml.addAttribute("miny", Double.toString(minY), 0);
        xml.addAttribute("maxy", Double.toString(maxY), 0);
        if (this.grid != null) {
            this.grid.appendXmlAttributes(xml);
        }
        if (this.xAxis != null) {
            this.xAxis.appendXmlAttributes(xml, "x");
        }
        if (this.yAxis != null) {
            this.yAxis.appendXmlAttributes(xml, "y");
        }
        xml.add('>');
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln();
        }

        appendPrimitivesXml(xml, xmlStyle, indent + 1);

        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln();
            xml.add(ind);
        }
        xml.add("</graphxy>");
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln();
        }
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocGraphXY");
        appendPrimitiveContainerString(builder);
        builder.add(this.window.toString());
        if (this.grid != null) {
            builder.add(this.grid.toString());
        }
        if (this.xAxis != null) {
            builder.add("X-", this.xAxis.toString());
        }
        if (this.yAxis != null) {
            builder.add("Y-", this.yAxis.toString());
        }
        builder.add(':');

        boolean comma = false;
        for (final AbstractPrimitiveInst primitive : getPrimitives()) {
            if (comma) {
                builder.add(',');
            }
            builder.add(primitive.toString());
            comma = true;
        }

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return docPrimitiveContainerInstHashCode() + this.window.hashCode()
                + Objects.hashCode(this.grid)
                + Objects.hashCode(this.xAxis)
                + Objects.hashCode(this.yAxis);
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
        } else if (obj instanceof final DocGraphXYInst graph) {
            equal = checkDocPrimitiveContainerInstEquals(graph)
                    && this.window.equals(graph.window)
                    && Objects.equals(this.grid, graph.grid)
                    && Objects.equals(this.xAxis, graph.xAxis)
                    && Objects.equals(this.yAxis, graph.yAxis);
        } else {
            equal = false;
        }

        return equal;
    }
}
