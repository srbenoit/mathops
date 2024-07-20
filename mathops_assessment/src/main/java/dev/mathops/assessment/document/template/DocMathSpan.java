package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.DocObjectStyle;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.EVAlign;
import dev.mathops.assessment.document.inst.AbstractDocObjectInst;
import dev.mathops.assessment.document.inst.DocMathSpanInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.font.BundledFontManager;

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
 * A series of document objects that will be rendered without line wrap. Objects will be laid out using mathematical
 * formatting. Some characters will automatically be rendered in italics.
 */
public final class DocMathSpan extends AbstractDocSpanBase {

    /** The color to use for math spans. */
    static final String MATH_COLOR_NAME = "0070C0";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2510534184856000376L;

    /** The set of outlines of the object to draw. */
    private int outLines = 0xFFFF;

    /**
     * Construct a new {@code DocMathSpan}.
     */
    DocMathSpan() {

        super();
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocMathSpan deepCopy() {

        final DocMathSpan copy = new DocMathSpan();

        copy.copyObjectFrom(this);

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
    private void copyObjectFrom(final DocMathSpan source) {

        copyObjectFromContainer(source);
        this.outLines = source.outLines;
    }

    /**
     * Recompute the size of the bounding box of the object.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        // Gather all flow objects contained, and perform layout on them. For parameter reference children, this will
        // re-generate the referenced content

        final List<AbstractDocObjectTemplate> objects = new ArrayList<>(10);

        for (final AbstractDocObjectTemplate child : getChildren()) {

            // Non-wrap spans, Math spans and Fences all lay out without line breaks - they all become a single
            // "flow object".

            // Simple spans (which include wrapping spans), emit their child objects (they are "transparent"). Parameter
            // references will emit either a DocText with the parameter value or the contents of a Span parameter, but
            // are also "transparent" with span values.

            // Allow the child to lay out its own contents. For transparent spans, this lays out child objects
            // recursively. Parameter references will use this method to build a list of resolved layout objects.

            child.doLayout(context, mathMode);

            if (child instanceof final DocSimpleSpan childSpan) {
                childSpan.accumulateFlowObjects(objects);
            } else if (child instanceof final DocParameterReference childParamRef) {
                childParamRef.getLaidOutContents().accumulateFlowObjects(objects);
            } else {
                objects.add(child);
            }
        }

        // For all baseline objects, find max center height above baseline (this is the "true center line" as distance
        // above baseline)
        int maxCenter = 0;
        for (final AbstractDocObjectTemplate obj : objects) {
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
        int maxHeight = 0;
        for (final AbstractDocObjectTemplate obj : objects) {

            int height = 0;
            if (obj.getLeftAlign() == EVAlign.BASELINE) {
                height = obj.getBaseLine();
            } else if (obj.getLeftAlign() == EVAlign.CENTER) {
                height = maxCenter + obj.getCenterLine();
            }

            if (height > maxHeight) {
                maxHeight = height;
            }
        }

        // Store the baseline and center line offsets
        setBaseLine(maxHeight);
        setCenterLine(maxHeight - maxCenter);

        // Generate the correct Y values for all objects, tracking the bottom-most point to use as bounds of this object
        int x = 0;
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

            final int h = obj.getHeight();
            if ((objY + h) > y) {
                y = objY + h;
            }

            x += obj.getWidth();
        }

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

        prePaint(grx);

        innerPaintComponent(grx);

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.paintComponent(grx, ELayoutMode.INLINE_MATH);
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
    public DocMathSpanInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float) getFontSize(),
                getFontStyle());

        final List<AbstractDocObjectTemplate> children = getChildren();
        final List<AbstractDocObjectInst> childrenInstList = new ArrayList<>(children.size());

        for (final AbstractDocObjectTemplate child : children) {
            childrenInstList.add(child.createInstance(evalContext));
        }

        return new DocMathSpanInst(objStyle, null, childrenInstList);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code v} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add("<math");
        printFormat(xml, 1.0f);
        xml.add('>');

        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.toXml(xml, 0);
        }

        xml.add("</math>");
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

        // FIXME: Make this non-wrapping
        for (final AbstractDocObjectTemplate child : getChildren()) {
            child.toLaTeX(dir, fileIndex, overwriteAll, builder, showAnswers, mode, context);
        }
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>MathSpan");

        if (this.tag != null) {
            ps.print(" (");
            ps.print(this.tag);
            ps.print(')');
        }

        printTreeContents(ps);

        ps.print("</li>");
    }

    /**
     * Print the format attributes to an {@code HtmlBuilder}, in XML format. The format text will include a leading
     * space.
     *
     * @param builder          the {@code HtmlBuilder} to which to write the information
     * @param defaultFontScale the default font scale (if the font scale is not this value, it will be emitted as an
     *                         attribute)
     */
    @Override
    public void printFormat(final HtmlBuilder builder, final float defaultFontScale) {

        final DocObjectStyle style = getStyle();

        if (style != null) {
            if (style.colorName != null && !style.colorName.equals(MATH_COLOR_NAME)) {
                builder.add(" color='", style.colorName, "'");
            }

            if (style.fontName != null) {
                builder.add(" fontname='", style.fontName, "'");
            }

            if (style.fontSize != 0.0f) {
                builder.add(" fontsize='", Float.toString(style.fontSize), "'");
            } else if (style.fontScale != defaultFontScale) {
                builder.add(" fontsize='", Integer.toString((int) (style.fontScale * 100.0f)), "%'");
            }

            if (style.fontStyle != null) {
                builder.add(" fontstyle='", makeStyleString(), "'");
            }
        }

        boolean comma = false;

        if (this.outLines != 0xFFFF) {
            builder.add(" lines='");

            if ((this.outLines & DocTable.TOPLINE) != 0) {
                builder.add("top");
                comma = true;
            }

            if ((this.outLines & DocTable.LEFTLINE) != 0) {
                if (comma) {
                    builder.add(CoreConstants.COMMA_CHAR);
                }
                builder.add("left");
                comma = true;
            }

            if ((this.outLines & DocTable.BOTTOMLINE) != 0) {
                if (comma) {
                    builder.add(CoreConstants.COMMA_CHAR);
                }
                builder.add("bottom");
                comma = true;
            }

            if ((this.outLines & DocTable.RIGHTLINE) != 0) {
                if (comma) {
                    builder.add(CoreConstants.COMMA_CHAR);
                }
                builder.add("right");
            }

            builder.add('\'');
        }
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return innerHashCode();
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
        } else if (obj instanceof final DocMathSpan span) {
            equal = innerEquals(span);
        } else {
            equal = false;
        }

        return equal;
    }
}
