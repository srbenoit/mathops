package dev.mathops.assessment.document.template;

import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.EJustification;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.EVAlign;
import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocParagraphInst;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.font.BundledFontManager;
import dev.mathops.text.builder.HtmlBuilder;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.font.GlyphVector;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * A paragraph is a collection of document objects which flow naturally in a left-to-right, top-down ordering. Each
 * object has a property that controls its vertical alignment within the lines of the paragraph. Each flowed line will
 * define a baseline. Each object has a top, bottom, centerline and baseline. The default is to align all baselines of
 * objects, but each object can be set regarding the alignment point for the next object. A paragraph may contain any
 * other descendant of {@code DocObject} except other paragraphs.
 */
public final class DocParagraph extends AbstractDocSpanBase {

    /** Code for right justification. */
    public static final int RIGHT = 2;

    /** Code for center justification. */
    public static final int CENTER = 3;

    /** Code for left justification. */
    static final int LEFT = 1;

    /** Code for full justification. */
    static final int FULL = 4;

    /** Code for left hanging justification. */
    public static final int LEFT_HANG = 5;

    /** Code for no spacing. */
    public static final int NONE = 11;

    /** Code for small spacing. */
    public static final int SMALL = 12;

    /** Code for normal spacing. */
    public static final int NORMAL = 13;

    /** Code for large spacing. */
    public static final int LARGE = 14;

    /** The top inset. */
    private static final int INSET_TOP_NONE = 1;

    /** The top inset. */
    private static final int INSET_TOP_NORMAL = 7;

    /** The top inset. */
    private static final int INSET_TOP_SMALL = 3;

    /** The top inset. */
    private static final int INSET_TOP_LARGE = 11;

    /** The right inset. */
    private static final int INSET_RIGHT = 4;

    /** The bottom inset. */
    private static final int INSET_BOTTOM_NONE = 1;

    /** The bottom inset. */
    private static final int INSET_BOTTOM_SMALL = 3;

    /** The bottom inset. */
    private static final int INSET_BOTTOM_NORMAL = 7;

    /** The bottom inset. */
    private static final int INSET_BOTTOM_LARGE = 11;

    /** The left inset. */
    private static final int INSET_LEFT = 4;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7749655278678810830L;

    /** The paragraph justification. */
    private int justification = LEFT;

    /** The paragraph spacing. */
    private int spacing = NORMAL;

    /** The indentation level. */
    private int indent = 0;

    /**
     * Construct a new {@code DocParagraph} object.
     */
    public DocParagraph() {

        super();
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocParagraph deepCopy() {

        final DocParagraph copy = new DocParagraph();

        copy.copyObjectFromContainer(this);
        copy.justification = this.justification;
        copy.spacing = this.spacing;
        copy.indent = this.indent;

        for (final AbstractDocObjectTemplate child : getChildren()) {
            copy.add(child.deepCopy());
        }

        return copy;
    }

    /**
     * Get the paragraph justification.
     *
     * @return the justification setting. One of LEFT, RIGHT, CENTER or FULL
     */
    public int getJustification() {

        return this.justification;
    }

    /**
     * Set the paragraph justification.
     *
     * @param theJustification the justification setting. One of LEFT, RIGHT, CENTER or FULL
     */
    public void setJustification(final int theJustification) {

        if (theJustification == LEFT || theJustification == RIGHT || theJustification == CENTER
                || theJustification == FULL || theJustification == LEFT_HANG) {
            this.justification = theJustification;
        } else {
            throw new IllegalArgumentException("Invalid justification setting");
        }
    }

    /**
     * Get the paragraph spacing.
     *
     * @return the spacing setting. One of NONE, SMALL, NORMAL, or LARGE
     */
    public int getSpacing() {

        return this.spacing;
    }

    /**
     * Set the paragraph spacing.
     *
     * @param theSpacing the spacing setting. One of NONE, SMALL, NORMAL, or LARGE
     */
    public void setSpacing(final int theSpacing) {

        if ((theSpacing != NONE) && (theSpacing != SMALL)
                && (theSpacing != NORMAL) && (theSpacing != LARGE)) {
            throw new IllegalArgumentException("Invalid spacing setting");
        }

        this.spacing = theSpacing;
    }

    /**
     * Get the indentation level.
     *
     * @return the indentation level, as a number of widths of a decimal "0" digit"
     */
    public int getIndent() {

        return this.indent;
    }

    /**
     * Set the indentation level.
     *
     * @param theIndent the indentation level (negative numbers clamped to zero)
     */
    public void setIndent(final int theIndent) {

        this.indent = Math.max(0, theIndent);
    }

    /**
     * Reposition all objects based on object dimensions and object flow settings.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        final int digitWidth = calculateDigitWidth();

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

        // Skip any leading whitespace in the paragraph (a space or newline after the <p> tag should not trigger
        // indentation).
        final int count = objects.size();
        int i;
        for (i = 0; i < count; i++) {
            if (!(objects.get(i) instanceof DocWhitespace)) {
                break;
            }
        }

        int topInset;
        int bottomInset;

        switch (this.spacing) {
            case NONE:
                topInset = INSET_TOP_NONE;
                bottomInset = INSET_BOTTOM_NONE;
                break;

            case SMALL:
                topInset = INSET_TOP_SMALL;
                bottomInset = INSET_BOTTOM_SMALL;
                break;

            case LARGE:
                topInset = INSET_TOP_LARGE;
                bottomInset = INSET_BOTTOM_LARGE;
                break;

            case NORMAL:
            default:
                topInset = INSET_TOP_NORMAL;
                bottomInset = INSET_BOTTOM_NORMAL;
                break;
        }

        int leftInset = INSET_LEFT;
        if (this.indent > 0) {
            leftInset += (int) Math.round((double) digitWidth * this.indent);
        }

        // Now, lay out the accumulated list of flowable objects into lines, and, using the
        // computed baseline and centerline for each line, position each object within the line.
        int x = leftInset;
        int y = topInset;
        final int height;

        int hanging = 0;
        int first = 0;
        for (; i < count; i++) {
            final AbstractDocObjectTemplate obj = objects.get(i);

            if (obj instanceof DocAlignmentMark) {
                if (this.justification == LEFT_HANG) {
                    hanging = x;
                }
            } else if (obj instanceof final DocHAlign hAlign) {

                final NumberOrFormula pos = hAlign.getPosition();
                Number posC = pos.getNumber();
                if (posC == null) {
                    final Formula posF = pos.getFormula();
                    if (posF.evaluate(context) instanceof final Number numberValue) {
                        posC = numberValue;
                    }
                }

                if (posC != null) {
                    final double absolute = posC.doubleValue() * (double) digitWidth;
                    if (Double.isFinite(absolute) && absolute > 0.0) {
                        final int intAbs = INSET_LEFT + (int)absolute;
                        if (intAbs > (getWidth() - INSET_RIGHT)) {
                            // Tab is past end of line - do a line wrap
                            if (first < i) {
                                y = arrangeSingleLine(objects, first, i - 1, y);
                                first = i;
                            }

                            obj.setX(x);
                            x = hanging > 0 ? hanging : leftInset;
                        } else if (intAbs > x) {
                            // Move to the tab stop
                            obj.setX(intAbs);
                            x = intAbs;
                        }
                    }
                }
            } else {
                final int objWidth = obj.getWidth();

                final int objX;
                if ((x + objWidth) > (getWidth() - INSET_RIGHT)) {

                    // Object won't fit on current line, so do vertical arrangement for the line we just finished, if
                    // any.
                    if (first < i) {
                        y = arrangeSingleLine(objects, first, i - 1, y);
                        first = i;
                    }

                    // Wrap the line
                    objX = hanging > 0 ? hanging : leftInset;

                    if (obj instanceof DocWhitespace) {
                        // If we wrap and leave whitespace at the start, don't advance for the whitespace
                        x = objX;
                    } else {
                        x = objX + objWidth;
                    }
                } else {
                    // Add object to current line
                    objX = x;
                    x += objWidth;
                }

                obj.setX(objX);
                obj.setY(0);
            }
        }

        // Arrange remaining items on last line.
        if (first < count) {
            y = arrangeSingleLine(objects, first, count - 1, y);
        }

        height = y + bottomInset;

        setHeight(height);
    }

    /**
     * Set vertical position of each object within a line, based on their alignment parameters.
     *
     * @param objects the list of objects being laid out
     * @param first   the array index of the first object in the line
     * @param last    the array index of the last object in the line
     * @param yPos    the Y position to place the top of the line
     * @return the Y position of the bottom of the arranged line
     */
    private int arrangeSingleLine(final List<? extends AbstractDocObjectTemplate> objects, final int first,
                                  final int last, final int yPos) {

        int newY = yPos;
        int oY = 0;
        int maxCenter = 0;
        int height = 0;
        int maxHeight = 0;

        // For all baseline objects, find max center height above baseline (this is the "true center-line" as distance
        // above baseline)
        for (int i = first; i <= last; i++) {
            final AbstractDocObjectTemplate obj = objects.get(i);

            if (obj instanceof DocDrawing || obj instanceof DocGraphXY || obj instanceof DocImage
                    || obj instanceof DocTable) {
                continue;
            }

            if (obj.getLeftAlign() == EVAlign.BASELINE) {
                final int center = obj.getBaseLine() - obj.getCenterLine();

                if (center > maxCenter) {
                    maxCenter = center;
                }
            }
        }

        if (maxCenter == 0) {
            // There are no baseline-aligned things - use our font to find a center line.
            final BundledFontManager bfm = BundledFontManager.getInstance();
            final Font font = getFont();
            final FontMetrics fm = bfm.getFontMetrics(font);
            final GlyphVector gv = font.createGlyphVector(fm.getFontRenderContext(), "My");
            maxCenter = (int) Math.round(-gv.getGlyphOutline(0).getBounds2D().getMinY() * 0.5);
        }

        // Compute maximum height of any object - this will become the new baseline height for the whole span.
        for (int i = first; i <= last; i++) {
            final AbstractDocObjectTemplate obj = objects.get(i);

            if (obj.getLeftAlign() == EVAlign.BASELINE) {
                height = obj.getBaseLine();
            } else if (obj.getLeftAlign() == EVAlign.CENTER) {
                height = maxCenter + obj.getCenterLine();
            }

            if (height > maxHeight) {
                maxHeight = height;
            }
        }

        // Generate the correct Y values for all objects, tracking the bottom-most point to use as bounds of this object
        for (int i = first; i <= last; i++) {
            final AbstractDocObjectTemplate obj = objects.get(i);

            final int oHeight = obj.getHeight();

            if (obj.getLeftAlign() == EVAlign.BASELINE) {
                oY = yPos + maxHeight - obj.getBaseLine();
            } else if (obj.getLeftAlign() == EVAlign.CENTER) {
                oY = yPos + maxHeight - maxCenter - obj.getCenterLine();
            }

            if ((oY + oHeight) > newY) {
                newY = oY + oHeight;
            }

            obj.setY(oY);
        }

        final AbstractDocObjectTemplate lastObj = objects.get(last);
        final int maxX = lastObj.getX() + lastObj.getWidth();
        final int dx = getWidth() - maxX;

        if (dx > 0) {

            switch (this.justification) {

                case CENTER:
                    for (int i = first; i <= last; i++) {
                        final AbstractDocObjectTemplate obj = objects.get(i);
                        obj.setX(obj.getX() + (dx / 2));
                    }
                    break;

                case RIGHT:
                    for (int i = first; i <= last; i++) {
                        final AbstractDocObjectTemplate obj = objects.get(i);
                        obj.setX(obj.getX() + dx);
                    }
                    break;

                case FULL:
                    if ((dx << 2) < getWidth()) {
                        for (int i = first; i <= last; i++) {
                            final AbstractDocObjectTemplate obj = objects.get(i);
                            obj.setX(obj.getX() + (dx * (i - first) / (last - first)));
                        }
                    }
                    break;

                default:
                    break;
            }
        }

        return newY;
    }

    /**
     * Draw the image.
     *
     * @param grx the {@code Graphics} object to which to draw the image
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        prePaint(grx);

        innerPaintComponent(grx);

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.paintComponent(grx, mathMode);
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
    public DocParagraphInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float) getFontSize(),
                getFontStyle());

        final List<AbstractDocObjectTemplate> children = getChildren();
        final List<AbstractDocObjectInst> childrenInstList = new ArrayList<>(children.size());

        for (final AbstractDocObjectTemplate child : children) {
            childrenInstList.add(child.createInstance(evalContext));
        }

        final EJustification just;

        if (this.justification == CENTER) {
            just = EJustification.CENTER;
        } else if (this.justification == RIGHT) {
            just = EJustification.RIGHT;
        } else if (this.justification == FULL) {
            just = EJustification.FULL;
        } else {
            just = EJustification.LEFT;
        }

        return new DocParagraphInst(objStyle, null, childrenInstList, just);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent);

        xml.add(ind, "<p");
        printFormat(xml, 1.0f);

        if (this.justification != LEFT) {
            xml.add(" justification=\"");

            switch (this.justification) {

                case RIGHT:
                    xml.add("right");
                    break;

                case CENTER:
                    xml.add("center");
                    break;

                case FULL:
                    xml.add("full");
                    break;

                case LEFT_HANG:
                    xml.add("left-hang");
                    break;

                default:
                    break;
            }

            xml.add('"');
        }

        if (this.spacing != NORMAL) {
            xml.add(" spacing=\"");

            switch (this.spacing) {

                case NONE:
                    xml.add("none");
                    break;

                case SMALL:
                    xml.add("small");
                    break;

                case LARGE:
                    xml.add("large");
                    break;

                default:
                    break;
            }

            xml.add('"');
        }

        if (this.indent > 0) {
            xml.add(" indent=\"");
            xml.add(this.indent);
            xml.add('"');
        }

        xml.add('>');

        for (final AbstractDocObjectTemplate child : getChildren()) {

            if (child instanceof DocText) {
                child.toXml(xml, 0);
            } else {
                child.toXml(xml, indent + 1);
            }
        }

        xml.addln("</p>");
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
    public void toLaTeX(final File dir, final int[] fileIndex, final boolean[] overwriteAll, final HtmlBuilder builder,
                        final boolean showAnswers, final char[] mode, final EvalContext context) {

        if (getChildren().isEmpty()) {
            return;
        }

        switch (this.justification) {

            case RIGHT:
                builder.addln("\\begin{raggedleft}");
                break;

            case CENTER:
                builder.addln("\\begin{center}");
                break;

            case LEFT:
            case LEFT_HANG:
                builder.addln("\\begin{raggedright}");
                break;

            default:
                break;
        }

        if (mode[0] == '$') {
            builder.add('$');
        } else if (mode[0] == 'M') {
            builder.add("\\]");
        }

        mode[0] = 'T';

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
        }

        if (mode[0] == '$') {
            builder.add('$');
        } else if (mode[0] == 'M') {
            builder.add("\\]");
        }

        mode[0] = 'T';

        switch (this.justification) {

            case LEFT:
            case LEFT_HANG:
                builder.addln("\\end{raggedright}~\\\\*[6pt]");
                break;

            case RIGHT:
                builder.addln("\\end{raggedleft}~\\\\*[6pt]");
                break;

            case CENTER:
                builder.addln("\\end{center}~\\\\*[6pt]");
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

        ps.print("<li>Paragraph");

        if (this.tag != null) {
            ps.print(" (");
            ps.print(this.tag);
            ps.print(')');
        }

        switch (this.justification) {

            case LEFT:
                ps.println(" [LEFT]");
                break;

            case RIGHT:
                ps.println(" [RIGHT]");
                break;

            case CENTER:
                ps.println(" [CENTER]");
                break;

            case FULL:
                ps.println(" [FULL]");
                break;

            case LEFT_HANG:
                ps.println(" [LEFT-HANG]");
                break;

            default:
                break;
        }

        printTreeContents(ps);

        ps.print("</li>");
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return innerHashCode() + this.justification + this.spacing + this.indent;
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
        } else if (obj instanceof final DocParagraph paragraph) {
            equal = innerEquals(paragraph) && this.justification == paragraph.justification
                    && this.spacing == paragraph.spacing
                    && this.indent == paragraph.indent;
        } else {
            equal = false;
        }

        return equal;
    }
}
