package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EJustification;
import dev.mathops.assessment.document.ETableSizing;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.assessment.document.Padding;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.Arrays;
import java.util.Objects;

/**
 * An object that presents a grid of data in a table form. There are two forms of data the table can support. In the
 * first, the table displays simple strings, using the table's formatting parameters for each. In the second, each entry
 * in the table is a {@code DocObject}, carrying its own formatting and computing its own bounds.<br>
 * <br>
 * There are two settings for table layout: UNIFORM will cause all columns and rows to be the same size (columns will be
 * the width of the widest entry, and rows will be the height of the tallest entry), and NONUNIFORM will make each row
 * and column just large enough to contain its entries.
 * <br>
 * <br>
 * Other settings control grid lines, margins applied to each entry, the color scheme, and whether the top row is a
 * header.
 */
public final class DocTableInst extends AbstractDocObjectInst {

    /** The array of non-wrapping spans to display, with [row][column] organization. */
    private final DocNonwrappingSpanInst[][] cells;

    /** Column sizing strategy. */
    private final ETableSizing columnSizing;

    /** Row sizing strategy. */
    private final ETableSizing rowSizing;

    /** Default justification for cells (a cell can override). */
    private final EJustification justification;

    /** Padding to add around every cell; {@code null} if no extra padding. */
    private final Padding cellPadding;

    /** A border to surround the table; {@code null} if no border. */
    public final StrokeStyle border;

    /** The style for horizontal lines between rows; {@code null} if no lines between rows. */
    private final StrokeStyle hLines;

    /** The style for vertical lines between columns; {@code null} if no lines between columns. */
    private final StrokeStyle vLines;

    /**
     * Construct a new {@code DocTableInst} object.
     *
     * @param theStyle         the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName   the background color name ({@code null} if transparent)
     * @param theCells         the array of non-wrapping spans to display, with [row][column] organization
     * @param theCellPadding   the padding to add around every cell; {@code null} if no extra padding
     * @param theColumnSizing  the column sizing strategy
     * @param theRowSizing     the row sizing strategy
     * @param theJustification the default justification for cells (a cell can override)
     * @param theBorder        the border to surround the table; {@code null} if no border
     * @param theHLines        the style for horizontal lines between rows; {@code null} if no lines between rows
     * @param theVLines        the style for vertical lines between columns; {@code null} if no lines between columns
     */
    public DocTableInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                        final DocNonwrappingSpanInst[][] theCells, final ETableSizing theColumnSizing,
                        final ETableSizing theRowSizing, final EJustification theJustification,
                        final Padding theCellPadding, final StrokeStyle theBorder, final StrokeStyle theHLines,
                        final StrokeStyle theVLines) {

        super(theStyle, theBgColorName);

        if (theCells == null) {
            throw new IllegalArgumentException("Cells array may not be null");
        }
        if (theColumnSizing == null) {
            throw new IllegalArgumentException("Column sizing method may not be null");
        }
        if (theRowSizing == null) {
            throw new IllegalArgumentException("Row sizing method may not be null");
        }
        if (theJustification == null) {
            throw new IllegalArgumentException("Justification may not be null");
        }

        final int numRows = theCells.length;
        if (numRows > 0) {
            final int numCols = theCells[0].length;
            for (int i = 1; i < numRows; ++i) {
                if (theCells[i].length != numCols) {
                    throw new IllegalArgumentException("Cells array must have same number of items per row");
                }
            }
            for (final DocNonwrappingSpanInst[] theCell : theCells) {
                for (int j = 0; j < numCols; ++j) {
                    if (theCell[j] == null) {
                        throw new IllegalArgumentException("Cells array may not contain nulls");
                    }
                }
            }
        }

        this.cells = new DocNonwrappingSpanInst[numRows][];
        for (int i = 0; i < numRows; ++i) {
            this.cells[i] = theCells[i].clone();
        }

        this.columnSizing = theColumnSizing;
        this.rowSizing = theRowSizing;
        this.justification = theJustification;
        this.cellPadding = theCellPadding;
        this.border = theBorder;
        this.hLines = theHLines;
        this.vLines = theVLines;
    }

    /**
     * Gets the array of non-wrapping spans to display, with [row][column] organization.
     *
     * @return a copy of the cells array
     */
    public DocNonwrappingSpanInst[][] getCells() {

        final int numRows = this.cells.length;
        final DocNonwrappingSpanInst[][] result= new DocNonwrappingSpanInst[numRows][];
        for (int i = 0; i < numRows; ++i) {
            result[i] = this.cells[i].clone();
        }

        return result;
    }

    /**
     * Gets the column sizing method.
     *
     * @return the column sizing method
     */
    public ETableSizing getColumnSizing() {

        return this.columnSizing;
    }

    /**
     * Gets the row sizing method.
     *
     * @return the row sizing method
     */
    public ETableSizing getRowSizing() {

        return this.rowSizing;
    }

    /**
     * Gets the default justification for cells (a cell can override)
     *
     * @return the default justification
     */
    public EJustification getJustification() {

        return this.justification;
    }

    /**
     * Gets the padding to add around every cell.
     *
     * @return the cell padding; {@code null} if no extra padding
     */
    public Padding getCellPadding() {

        return this.cellPadding;
    }

    /**
     * Gets the table border style.
     *
     * @return the border style; {@code null} if no border
     */
    public StrokeStyle getBorder() {

        return this.border;
    }

    /**
     * Gets the style for horizontal lines between rows.
     *
     * @return the horizontal line style; {@code null} if no lines between rows
     */
    public StrokeStyle getHLines() {

        return this.hLines;
    }

    /**
     * Gets the style for vertical lines between columns.
     *
     * @return the vertical line style; {@code null} if no lines between columns
     */
    public StrokeStyle getVLines() {

        return this.vLines;
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml      the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent   the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        final String ind = makeIndent(indent);
        final String ind1 = makeIndent(indent + 1);
        final String ind2 = makeIndent(indent + 2);
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.add(ind);
        }

        xml.add("<table");
        addDocObjectInstXmlAttributes(xml);
        xml.addAttribute("col-sizing", this.columnSizing, 0);
        xml.addAttribute("row-sizing", this.rowSizing, 0);
        xml.addAttribute("justification", this.justification, 0);
        if (this.cellPadding != null) {
            this.cellPadding.appendXmlAttributes(xml, "padding");
        }
        if (this.border != null) {
            this.border.appendXmlAttributes(xml, "border");
        }
        if (this.hLines != null) {
            this.hLines.appendXmlAttributes(xml, "hlines");
        }
        if (this.vLines != null) {
            this.vLines.appendXmlAttributes(xml, "vlines");
        }
        xml.add(">");

        for (final DocNonwrappingSpanInst[] row : this.cells) {
            if (xmlStyle == EXmlStyle.INDENTED) {
                xml.addln();
                xml.add(ind1);
            }
            xml.add("<tr>");

            final int numCells = row.length;
            for (int cell = 0; cell < numCells; ++cell) {
                if (xmlStyle == EXmlStyle.INDENTED) {
                    xml.addln();
                    xml.add(ind2);
                }
                xml.add("<tc>");
                // Spacing matters - go to inline
                row[cell].toXml(xml, EXmlStyle.INLINE, 0);
                xml.add("</tc>");
                if (xmlStyle == EXmlStyle.INDENTED) {
                    xml.addln();
                    xml.add(ind);
                }
            }

            xml.add("</tr>");
        }
        if (xmlStyle == EXmlStyle.INDENTED) {
            xml.addln();
            xml.add(ind);
        }
        xml.add("</table>");
    }

    /**
     * Generate a {@code String} representation of the fraction (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocTableInst");
        appendStyleString(builder);
        builder.add("{col-sizing=", this.columnSizing, ",row-sizing=", this.rowSizing, ",justification=",
                this.justification);

        if (this.cellPadding != null) {
            builder.add(",padding=", this.cellPadding);
        }
        if (this.border != null) {
            builder.add(",border=", this.border);
        }
        if (this.hLines != null) {
            builder.add(",hlines=", this.hLines);
        }
        if (this.vLines != null) {
            builder.add(",vlines=", this.vLines);
        }
        builder.add("cells=");
        for (final DocNonwrappingSpanInst[] row : this.cells) {
            builder.add('[');
            for (final DocNonwrappingSpanInst col : row) {
                builder.add('[');
                builder.add(col);
                builder.add(']');
            }
            builder.add(']');
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

        return docObjectInstHashCode() + Arrays.deepHashCode(this.cells) + this.columnSizing.hashCode()
                + this.rowSizing.hashCode() + this.justification.hashCode() + Objects.hashCode(this.cellPadding)
                + Objects.hashCode(this.border) + Objects.hashCode(this.hLines) + Objects.hashCode(this.vLines);
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
        } else if (obj instanceof final DocTableInst table) {
            equal = checkDocObjectInstEquals(table)
                    && this.columnSizing == table.getColumnSizing()
                    && this.rowSizing == table.getRowSizing()
                    && this.justification == table.getJustification()
                    && Arrays.deepEquals(this.cells, table.cells)
                    && Objects.equals(this.cellPadding, table.getCellPadding())
                    && Objects.equals(this.border, table.border)
                    && Objects.equals(this.hLines, table.getHLines())
                    && Objects.equals(this.vLines, table.getVLines());
        } else {
            equal = false;
        }

        return equal;
    }
}
