package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.EJustification;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.document.ETableSizing;
import dev.mathops.assessment.document.EVAlign;
import dev.mathops.assessment.document.Padding;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.assessment.document.inst.DocNonwrappingSpanInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocTableInst;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.ui.ColorNames;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

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
public final class DocTable extends AbstractDocContainer {

    /** Setting to force row and column sizes to be consistent. */
    public static final int UNIFORM = 1;

    /** Setting to allow cell content to dictate row/column sizes. */
    static final int NONUNIFORM = 2;

    /** Code for left justification. */
    public static final int LEFT = 1;

    /** Code for right justification. */
    public static final int RIGHT = 2;

    /** Code for center justification. */
    public static final int CENTER = 3;

    /** Code to draw the line to the left of the cell. */
    static final int LEFTLINE = 0x01;

    /** Code to draw the line to the right of the cell. */
    static final int RIGHTLINE = 0x02;

    /** Code to draw the line to the top of the cell. */
    static final int TOPLINE = 0x04;

    /** Code to draw the line to the bottom of the cell. */
    static final int BOTTOMLINE = 0x08;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2226196206521951036L;

    /** A grid of DocNonwrappingSpan to display, with [row][column] organization. */
    public final DocNonwrappingSpan[][] objectData;

    /** Insets to apply to each cell. */
    public Insets cellInsets;

    /** Setting for row/column spacing (UNIFORM or NONUNIFORM). */
    private int spacing = UNIFORM;

    /** Setting for justification within cells (LEFT, RIGHT, CENTER). */
    private int justification = CENTER;

    /** The Y positions of the bottom edge of each row. */
    private int[] rowY;

    /** The Y positions of the baseline of each row. */
    private int[] rowBase;

    /** The X positions of the right edge of each column. */
    private int[] colX;

    /** The width (in pixels) of the box to surround the table. */
    public int boxWidth = 1;

    /** The width (in pixels) of interior horizontal lines in the table. */
    public int hLineWidth = 1;

    /** The width (in pixels) of interior vertical lines in the table. */
    public int vLineWidth = 1;

    /** The background color of the graph (null for transparent). */
    public String backgroundColorName;

    /** The background color of the graph (null for transparent). */
    private Color backgroundColor;

    /**
     * Construct a {@code DocTable} to display a grid of {@code DocObject} values.
     *
     * @param data the data to display
     */
    DocTable(final DocNonwrappingSpan[][] data) {

        super();

        this.objectData = data;

        // Set parentage of each object, so they can inherit table style
        for (final DocNonwrappingSpan[] datum : data) {

            if (datum == null) {
                continue;
            }

            for (final DocNonwrappingSpan docNonwrappingSpan : datum) {

                if (docNonwrappingSpan != null) {
                    add(docNonwrappingSpan);
                }
            }
        }

        setLeftAlign(EVAlign.CENTER);
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocTable deepCopy() {

        final DocNonwrappingSpan[][] data = new DocNonwrappingSpan[this.objectData.length][];

        final int dataLen = data.length;
        for (int i = 0; i < dataLen; i++) {
            data[i] = new DocNonwrappingSpan[this.objectData[i].length];

            final int innerLen = data[i].length;
            for (int j = 0; j < innerLen; j++) {
                if (this.objectData[i][j] != null) {
                    data[i][j] = this.objectData[i][j].deepCopy();
                }
            }
        }

        final DocTable copy = new DocTable(data);
        copy.copyObjectFromContainer(this);

        copy.cellInsets = this.cellInsets;
        copy.spacing = this.spacing;
        copy.justification = this.justification;
        copy.rowY = this.rowY;
        copy.rowBase = this.rowBase;
        copy.colX = this.colX;
        copy.boxWidth = this.boxWidth;
        copy.hLineWidth = this.hLineWidth;
        copy.vLineWidth = this.vLineWidth;
        copy.backgroundColorName = this.backgroundColorName;
        copy.backgroundColor = this.backgroundColor;

        return copy;
    }

    /**
     * Get the table spacing setting.
     *
     * @return UNIFORM or NONUNIFORM
     */
    public int getSpacing() {

        return this.spacing;
    }

    /**
     * Set the table spacing property.
     *
     * @param theSpacing UNIFORM to make all rows the same width and all columns the same height, or NONUNIFORM to make
     *                   all columns just as wide as they need to be, and all rows just as high as they need to be
     */
    public void setSpacing(final int theSpacing) {

        if ((theSpacing == UNIFORM) || (theSpacing == NONUNIFORM)) {
            this.spacing = theSpacing;
        }
    }

    /**
     * Get the table justification setting.
     *
     * @return LEFT, RIGHT or CENTER
     */
    public int getJustification() {

        return this.justification;
    }

    /**
     * Set the table justification property, which controls how all cells are justified.
     *
     * @param theJustification LEFT, RIGHT or CENTER
     */
    public void setJustification(final int theJustification) {

        if ((theJustification == LEFT) || (theJustification == RIGHT)
                || (theJustification == CENTER)) {
            this.justification = theJustification;
        }
    }

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

    /**
     * Gets the number of rows.
     *
     * @return the number of rows
     */
    public int getNumRows() {

        return this.objectData.length;
    }

    /**
     * Recompute the bounding box of the object. The upper left corner of the box will not move, but the width and
     * height will be recomputed based on current image size.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        if (this.objectData != null) {
            doObjectLayout(context, mathMode);

            setWidth(this.colX[this.colX.length - 1] + this.boxWidth - this.vLineWidth);
            setHeight(this.rowY[this.rowY.length - 1] + this.boxWidth - this.hLineWidth);

            setBaseLine(this.rowBase[this.rowY.length - 1]);
            setCenterLine((this.rowY[this.rowY.length - 1] + this.rowY[0]) / 2);
        } else {
            // Null data, so disappear
            setWidth(0);
            setHeight(0);
        }
    }

    /**
     * Lay out the table using the {@code DocObject} data.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    private void doObjectLayout(final EvalContext context, final ELayoutMode mathMode) {

        int lIns = 0;
        int rIns = 0;
        int tIns = 0;
        int bIns = 0;
        int cnt = 0;

        // Get cell insets
        if (this.cellInsets != null) {
            lIns = this.cellInsets.left;
            rIns = this.cellInsets.right;
            tIns = this.cellInsets.top;
            bIns = this.cellInsets.bottom;
        }

        // First, lay out each child
        for (final DocNonwrappingSpan[] objectDatum : this.objectData) {

            if (objectDatum == null) {
                continue;
            }

            for (final DocNonwrappingSpan docNonwrappingSpan : objectDatum) {
                if (docNonwrappingSpan != null) {
                    docNonwrappingSpan.doLayout(context, mathMode);
                }
            }
        }

        // Count the columns
        for (final DocNonwrappingSpan[] objectDatum : this.objectData) {
            if ((objectDatum != null) && (objectDatum.length > cnt)) {
                cnt = objectDatum.length;
            }
        }

        // Allocate storage for X/Y position data, testing, so we avoid expensive object creation if not needed
        if ((this.rowY == null) || (this.rowY.length != (this.objectData.length + 1))) {
            this.rowY = new int[this.objectData.length + 1];
        }

        if ((this.rowBase == null) || (this.rowBase.length != (this.objectData.length + 1))) {
            this.rowBase = new int[this.objectData.length + 1];
        }

        if ((this.colX == null) || (this.colX.length != (cnt + 1))) {
            this.colX = new int[cnt + 1];
        }

        final int rowYLen = this.rowY.length;
        final int colXLen = this.colX.length;
        final int objDataLen = this.objectData.length;

        // Zero out the arrays
        for (int i = 0; i < rowYLen; i++) {
            this.rowY[i] = 0;
            this.rowBase[i] = 0;
        }

        Arrays.fill(this.colX, 0);

        if (this.spacing == UNIFORM) {

            // Determine maximum width and height of data
            int wMax = 0;

            for (int i = 0; i < objDataLen; i++) {

                if (this.objectData[i] == null) {
                    continue;
                }

                int aMax = 0;
                int dMax = 0;

                final int innerLen = this.objectData[i].length;
                for (int j = 0; j < innerLen; j++) {

                    if (this.objectData[i][j] != null) {
                        final int asc = this.objectData[i][j].getBaseLine();
                        final int desc = this.objectData[i][j].getHeight() - asc;
                        final int wid = this.objectData[i][j].getWidth();

                        if (asc > aMax) {
                            aMax = asc;
                        }

                        if (desc > dMax) {
                            dMax = desc;
                        }

                        if (wid > wMax) {
                            wMax = wid;
                        }
                    }
                }

                aMax += tIns;
                dMax += bIns;

                this.rowBase[i] = aMax + tIns;
                this.rowY[i + 1] += aMax + dMax;
            }

            // Adjust height and width for insets
            wMax += lIns + rIns;

            // Now set ALL rows and columns to consistent sizes
            this.rowY[0] = this.boxWidth;
            this.rowBase[0] += this.rowY[0];

            for (int i = 1; i < rowYLen; i++) {
                this.rowY[i] += this.rowY[i - 1] + this.hLineWidth;
                this.rowBase[i] += this.rowY[i];
            }

            this.colX[0] = this.boxWidth;

            for (int i = 1; i < colXLen; i++) {
                this.colX[i] = this.colX[i - 1] + wMax + this.vLineWidth;
            }
        } else {
            // Find height of each row, storing in mRowY for now, and build max width of each column in mColX.
            for (int i = 0; i < objDataLen; i++) {

                if (this.objectData[i] == null) {
                    this.rowBase[i] = 0;
                    this.rowY[i + 1] = tIns + bIns;

                    continue;
                }

                int aMax = 0;
                int dMax = 0;

                final int innerLen = this.objectData[i].length;
                for (int j = 0; j < innerLen; j++) {

                    if (this.objectData[i][j] != null) {
                        final int asc = this.objectData[i][j].getBaseLine();
                        final int desc = this.objectData[i][j].getHeight() - asc;

                        if (asc > aMax) {
                            aMax = asc;
                        }

                        if (desc > dMax) {
                            dMax = desc;
                        }

                        final int w = this.objectData[i][j].getWidth() + lIns + rIns;

                        if (w > this.colX[j + 1]) {
                            this.colX[j + 1] = w;
                        }
                    }
                }

                aMax += tIns;
                dMax += bIns;
                this.rowBase[i] = aMax;
                this.rowY[i + 1] += aMax + dMax;
            }

            // Now set the positions relative to each other
            this.rowY[0] = this.boxWidth;
            this.rowBase[0] += this.rowY[0];

            for (int i = 1; i < rowYLen; i++) {
                this.rowY[i] += this.rowY[i - 1] + this.hLineWidth;
                this.rowBase[i] += this.rowY[i];
            }

            this.colX[0] = this.boxWidth;

            for (int i = 1; i < colXLen; i++) {
                this.colX[i] += this.colX[i - 1] + this.vLineWidth;
            }
        }

        // Set the location of each child object relative to row and
        // column positions, centering each child in each cell.
        for (int i = 0; i < objDataLen; i++) {

            if (this.objectData[i] == null) {
                continue;
            }

            final int innerLen = this.objectData[i].length;
            for (int j = 0; j < innerLen; j++) {
                final AbstractDocObjectTemplate obj = this.objectData[i][j];

                if (obj != null) {

                    // Set object horizontal position based on justification
                    final int x;
                    if (this.justification == LEFT) {
                        x = lIns + this.colX[j];
                    } else if (this.justification == RIGHT) {
                        x = this.colX[j + 1] - rIns - this.vLineWidth - obj.getWidth();
                    } else {
                        x = (this.colX[j] + this.colX[j + 1] - this.vLineWidth - obj.getWidth()) / 2;
                    }

                    obj.setX(x);
                    obj.setY(this.rowBase[i] - obj.getBaseLine());
                }
            }
        }
    }

    /**
     * Draw the table.
     *
     * @param grx the {@code Graphics} object to which to draw the table
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        if (this.objectData == null) {
            return; // No drawing if data is all null.
        }

        prePaint(grx);

        innerPaintComponent(grx);

        final int top = 0;
        final int left = 0;
        final int bottom = getHeight();
        final int right = getWidth();

        // Draw the background
        if (this.backgroundColor != null) {
            grx.setColor(this.backgroundColor);
            grx.fillRect(left, top, right - left, bottom - top);
        }

        final Color lineColor = ColorNames.getColor(getColorName());
        grx.setColor(lineColor);

        // Draw the box
        for (int i = 0; i < this.boxWidth; i++) {
            grx.drawRect(left + i, top + i, right - left - 1 - (i << 1), bottom - top - 1 - (i << 1));
        }

        // Now paint the children
        final int dataLen = this.objectData.length;
        for (int i = 0; i < dataLen; i++) {

            if (this.objectData[i] == null) {
                continue;
            }

            final int innerLen = this.objectData[i].length;
            for (int j = 0; j < innerLen; j++) {
                final DocNonwrappingSpan obj = this.objectData[i][j];

                if (obj != null) {
                    if (obj.backgroundColor != null) {
                        grx.setColor(obj.backgroundColor);
                        grx.fillRect(this.colX[j], this.rowY[i], this.colX[j + 1] - this.colX[j],
                                this.rowY[i + 1] - this.rowY[i]);
                    }

                    obj.paintComponent(grx, mathMode);

                    final int outlines = obj.outlines;

                    grx.setColor(lineColor);

                    if ((outlines & LEFTLINE) != 0) {

                        for (int k = 0; k < this.vLineWidth; k++) {
                            grx.drawLine(this.colX[j] - this.vLineWidth + k,
                                    this.rowY[i] - this.hLineWidth, this.colX[j] - this.vLineWidth + k,
                                    this.rowY[i + 1] - this.hLineWidth);
                        }
                    }

                    if ((outlines & RIGHTLINE) != 0) {

                        for (int k = 0; k < this.vLineWidth; k++) {
                            grx.drawLine(this.colX[j + 1] - this.vLineWidth + k,
                                    this.rowY[i] - this.hLineWidth,
                                    this.colX[j + 1] - this.vLineWidth + k,
                                    this.rowY[i + 1] - this.hLineWidth);
                        }
                    }

                    if ((outlines & TOPLINE) != 0) {

                        for (int k = 0; k < this.hLineWidth; k++) {
                            grx.drawLine(this.colX[j] - this.vLineWidth,
                                    this.rowY[i] - this.hLineWidth + k,
                                    this.colX[j + 1] - this.vLineWidth,
                                    this.rowY[i] - this.hLineWidth + k);
                        }
                    }

                    if ((outlines & BOTTOMLINE) != 0) {

                        for (int k = 0; k < this.hLineWidth; k++) {
                            grx.drawLine(this.colX[j] - this.vLineWidth,
                                    this.rowY[i + 1] - this.hLineWidth + k,
                                    this.colX[j + 1] - this.vLineWidth,
                                    this.rowY[i + 1] - this.hLineWidth + k);
                        }
                    }
                }
            }
        }

        postPaint(grx);
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) {

        if (this.objectData != null) {
            for (final DocNonwrappingSpan[] objectDatum : this.objectData) {
                if (objectDatum != null) {
                    for (final AbstractDocObjectTemplate obj : objectDatum) {
                        if (obj != null) {
                            obj.accumulateParameterNames(set);
                        }
                    }
                }
            }
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
     * @return the instance document object
     */
    @Override
    public DocTableInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float) getFontSize(),
                getFontStyle());

        final int numRows = this.objectData.length;
        final DocNonwrappingSpanInst[][] cells = new DocNonwrappingSpanInst[numRows][];
        for (int r = 0; r < numRows; ++r) {
            final DocNonwrappingSpan[] row = this.objectData[r];
            final int numCols = row.length;
            cells[r] = new DocNonwrappingSpanInst[numCols];
            for (int c = 0; c < numCols; ++c) {
                cells[r][c] = row[c].createInstance(evalContext);
            }
        }

        final ETableSizing colSizing = this.spacing == UNIFORM ? ETableSizing.UNIFORM : ETableSizing.NONUNIFORM;
        final EJustification just;
        if (this.justification == CENTER) {
            just = EJustification.CENTER;
        } else if (this.justification == RIGHT) {
            just = EJustification.RIGHT;
        } else {
            just = EJustification.LEFT;
        }

        final Padding padding = this.cellInsets == null ? new Padding(0.0, 0.0, 0.0, 0.0)
                : new Padding((double) this.cellInsets.left, (double) this.cellInsets.top,
                (double) this.cellInsets.right, (double) this.cellInsets.bottom);

        final StrokeStyle border = this.boxWidth == 0 ? null :
                new StrokeStyle((double) this.boxWidth, "black", null, 1.0, EStrokeCap.BUTT, EStrokeJoin.MITER, 10.0f);

        final StrokeStyle hlines = this.hLineWidth == 0 ? null :
                new StrokeStyle((double) this.hLineWidth, "black", null, 1.0, EStrokeCap.BUTT, EStrokeJoin.MITER,
                        10.0f);

        final StrokeStyle vlines = this.vLineWidth == 0 ? null :
                new StrokeStyle((double) this.vLineWidth, "black", null, 1.0, EStrokeCap.BUTT, EStrokeJoin.MITER,
                        10.0f);

        return new DocTableInst(objStyle, this.backgroundColorName, cells, colSizing, ETableSizing.NONUNIFORM, just,
                padding, border, hlines, vlines);
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add("<table");

        if (this.boxWidth != 1) {
            xml.add(" box-width=\"", Integer.toString(this.boxWidth), CoreConstants.QUOTE);
        }

        if (this.vLineWidth != 1) {
            xml.add(" v-line-width=\"", Integer.toString(this.vLineWidth), CoreConstants.QUOTE);
        }

        if (this.hLineWidth != 1) {
            xml.add(" h-line-width=\"", Integer.toString(this.hLineWidth), CoreConstants.QUOTE);
        }

        if (this.spacing == NONUNIFORM) {
            xml.add(" column-width=\"nonuniform\"");
        }

        if (this.justification != CENTER) {

            switch (this.justification) {

                case LEFT:
                    xml.add(" justification=\"left\"");
                    break;

                case RIGHT:
                    xml.add(" justification=\"right\"");
                    break;

                default:
                    break;
            }
        }

        if (this.backgroundColorName != null) {
            xml.add(" bgcolor=\"", this.backgroundColorName, CoreConstants.QUOTE);
        }

        if (this.cellInsets != null) {
            xml.add(" cell-margins=\"");

            if ((this.cellInsets.left == this.cellInsets.top)
                    && (this.cellInsets.left == this.cellInsets.right)
                    && (this.cellInsets.left == this.cellInsets.bottom)) {
                xml.add(this.cellInsets.left);
            } else {
                xml.add(Integer.toString(this.cellInsets.top), CoreConstants.COMMA,
                        Integer.toString(this.cellInsets.left), CoreConstants.COMMA,
                        Integer.toString(this.cellInsets.bottom), CoreConstants.COMMA,
                        Integer.toString(this.cellInsets.right));
            }

            xml.add('\"');
        }

//        if (my alignment == BASELINE) {
//            xml.add(" valign='baseline'");
//        }

        printFormat(xml, 1.0f);
        xml.addln(">");

        if (this.objectData != null) {

            for (final DocNonwrappingSpan[] objectDatum : this.objectData) {
                xml.add("<tr>");

                if (objectDatum != null) {
                    for (final DocNonwrappingSpan docNonwrappingSpan : objectDatum) {
                        if (docNonwrappingSpan != null) {
                            docNonwrappingSpan.toXml(xml, 0);
                        }
                    }
                }
                xml.add("</tr>");
            }
        }
        xml.add("</table>");
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

        final char oldMode = mode[1];

        builder.add("\\begin{center} \\begin{tabular}{");
        mode[1] = 'T';

        if (this.vLineWidth != 0) {
            builder.add('|');
        }

        if ((this.objectData != null) && (this.objectData[0] != null)) {

            final int innerLen = this.objectData[0].length;
            for (int i = 0; i < innerLen; i++) {

                switch (this.justification) {

                    case CENTER:
                        builder.add('c');
                        break;

                    case LEFT:
                        builder.add('l');
                        break;

                    case RIGHT:
                        builder.add('r');
                        break;

                    default:
                        break;
                }

                if (this.vLineWidth != 0) {
                    builder.add('|');
                }
            }
        }

        builder.addln("}");

        if (this.objectData != null) {

            if (this.hLineWidth != 0) {
                builder.addln("\\hline");
            }

            for (final DocNonwrappingSpan[] objectDatum : this.objectData) {

                if (objectDatum != null) {

                    final int len = objectDatum.length;
                    for (int j = 0; j < len; j++) {
                        builder.add('~');

                        if (j > 0) {
                            builder.add('&');
                        }

                        if (objectDatum[j] != null) {
                            objectDatum[j].toLaTeX(dir, fileIndex, overwriteAll, builder,
                                    showAnswers, mode, context);
                        }
                    }
                }

                builder.add("\\\\");

                if (this.hLineWidth != 0) {
                    builder.add("\\hline");
                }

                builder.addln();
            }
        }

        mode[1] = oldMode;
        builder.addln("\\end{tabular}\\end{center}");
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>Table");
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

        return "[TABLE]";
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return innerHashCode() + Objects.hashCode(this.objectData) + this.spacing << 5
                + this.justification + Objects.hashCode(this.backgroundColorName);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocTable table) {
            equal = innerEquals(table) && this.spacing == table.spacing && this.justification == table.justification
                    && Objects.equals(this.backgroundColorName, table.backgroundColorName);

            if (equal) {
                if (this.objectData == null) {
                    equal = table.objectData == null;
                } else if ((table.objectData == null) || (this.objectData.length != table.objectData.length)) {
                    equal = false;
                } else {
                    final int dataLen = this.objectData.length;
                    for (int i = 0; i < dataLen; ++i) {
                        if (!Arrays.equals(this.objectData[i], table.objectData[i])) {
                            equal = false;
                            break;
                        }
                    }
                }
            }
        } else {
            equal = false;
        }

        return equal;
    }
}
