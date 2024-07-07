package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.EFieldStyle;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocInputExpressionFieldInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableInputInteger;
import dev.mathops.assessment.variable.VariableInputString;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.xml.XmlEscaper;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.PrintStream;
import java.io.Serial;
import java.util.Objects;

/**
 * A document object that supports the entry of a general expression. The input control allows definition of all
 * document formatting characteristics. Expression fields are drawn in a shaded outline, which highlights when the
 * object is selected. When selected, an edit caret is shown and editing is supported.
 *
 * <p>
 * Expression fields support entry of digits, decimal points, variables, arithmetic operators, fractions, exponents and
 * roots, functions, and paired parentheses.  Attributes control which of these are allowed for a given input.
 *
 * <p>
 * As the user exits the expression, the input simultaneously builds a plain-text representation.  The text
 * representation is the content that is submitted as the input value.  The input can attempt to parse the text into a
 * valid (evaluable) expression, and if this fails, the input's background is set to an error indicator color.
 */
public final class DocInputExpressionField extends AbstractDocInputField {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1523313633615080761L;

    /** The width. */
    public Integer width;

    /**
     * Construct a new {@code DocInputExpressionField}.
     *
     * @param theName the name of the input's value in the parameter set
     */
    DocInputExpressionField(final String theName) {

        super(theName);
    }

    /**
     * Binds the input to the corresponding variable in an evaluation context, creating that variable if not already
     * present.
     *
     * @param theContext the evaluation context
     */
    @Override
    public void bind(final EvalContext theContext) {

        super.bind(theContext);

        final AbstractVariable var = theContext.getVariable(getName());

        if (var == null) {
            final VariableInputString newVar = new VariableInputString(getName());
            newVar.setValue(CoreConstants.EMPTY);
            theContext.addVariable(newVar);
        } else if (!(var instanceof VariableInputString)) {
            throw new IllegalArgumentException(Res.fmt(Res.INCONSISTENT_TYPE, var.name,
                    var.getClass().getSimpleName(), VariableInputInteger.class.getSimpleName()));
        }
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocInputExpressionField deepCopy() {

        final DocInputExpressionField copy = new DocInputExpressionField(getName());
        copy.copyObjectFromInput(this);

        copy.innerSetTextValue(getTextValue());
        copy.style = this.style;
        copy.width = this.width;

        for (final AbstractDocObjectTemplate child : getChildren()) {
            copy.add(child.deepCopy());
        }

        return copy;
    }

    /**
     * Set the value of the field to a particular text string. The input string is filtered to only printable ASCII
     * characters between 0x32 and 0x7E, inclusive.
     *
     * @param theText the text to which to set the input's value
     * @return true if the value was a valid number; false otherwise
     */
    @Override
    public synchronized boolean setTextValue(final String theText) {

        final String cleaned;

        if (theText == null || theText.isEmpty()) {
            cleaned = CoreConstants.EMPTY;
        } else {
            final StringBuilder builder = new StringBuilder(theText.length());

            for (final char ch : theText.toCharArray()) {
                if (ch >= 0x20 && ch <= 0x7E) {
                    builder.append(ch);
                }
            }
            cleaned = builder.toString();
        }

        final String oldVal = getTextValue();

        boolean ok = cleaned.equals(oldVal);

        // If no change, do nothing.

        if (!ok) {
            ok = true;

            setCharPositions(null);
            this.selectStart = -1;
            this.selectEnd = -1;
            this.caret = 0;

            innerSetTextValue(cleaned);

            this.caret = cleaned.length();

            storeValue(getTextValue());

            if (getEvalContext() != null) {
                doLayout(getEvalContext(), ELayoutMode.INLINE_MATH);
            }

            notifyChangeListeners();
        }

        return ok;
    }

    /**
     * Draw the object.
     *
     * @param grx the {@code Graphics} to draw to
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        prePaint(grx);

        innerPaintComponent(grx);

        final int w = getWidth();
        final int h = getHeight();

        // Draw a drop shadow (box style only)
        if (this.style == EFieldStyle.BOX && SHADOW_COLOR != null && grx instanceof Graphics2D) {
            grx.setColor(SHADOW_COLOR);
            grx.drawRect(1, MARGIN_TOP + 1, w, h - MARGIN_TOP - MARGIN_BOTTOM);
        }

        // Draw the field background, with color based on whether the field is selected and/or enabled.
        final boolean draw = this.style == EFieldStyle.BOX;
        final Color fg;
        if (!isEnabled() && grx instanceof Graphics2D) {
            grx.setColor(DISABLED_BG_COLOR);
            fg = DISABLED_FG_COLOR;
        } else if (!isSelected()) {
            grx.setColor(ENABLED_BG_COLOR);
            fg = ENABLED_FG_COLOR;
        } else {
            grx.setColor(SELECTED_BG_COLOR);
            fg = SELECTED_FG_COLOR;
        }

        if (draw) {
            grx.fillRect(0, MARGIN_TOP, w, h - MARGIN_TOP - MARGIN_BOTTOM);
        }

        // Draw the field outline
        grx.setColor(fg);
        if (this.style == EFieldStyle.BOX) {
            grx.drawRect(0, MARGIN_TOP, w, h - MARGIN_TOP - MARGIN_BOTTOM);
        } else if (this.style == EFieldStyle.UNDERLINE) {
            grx.drawLine(0, h - MARGIN_BOTTOM, w, h - MARGIN_BOTTOM);
        }

        Graphics2D g2d = null;
        Object origHint = null;
        if (grx instanceof Graphics2D) {
            g2d = (Graphics2D) grx;
            origHint = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        grx.setFont(getFont());
        final FontMetrics fm = grx.getFontMetrics();

        final String tmp;
        String actual;
        if (getTextValue() != null) {
            tmp = getTextValue();
            actual = tmp.replace('-', '\u2212');

            while (actual.endsWith(CoreConstants.SPC)) {
                actual = actual.substring(0, tmp.length() - 1);
            }
        } else {
            actual = CoreConstants.EMPTY;
        }

        int lwidth = fm.stringWidth(actual);

        if (!(grx instanceof Graphics2D)) {
            lwidth = fm.stringWidth("99999999");
        }

        int x = INSET_LEFT;
        int y = getBaseLine();

        // For boxed, bounds are 4 pixels wider to accommodate box
        if (isBoxed()) {
            x += 2;
        }

        if (isVisible()) {
            final int[] charPositions = getCharPositions();

            if (charPositions != null && this.selectStart != -1 && this.selectEnd != -1) {
                // Draw selection background
                grx.setColor(HIGHLIGHT_COLOR);
                grx.fillRect(x + charPositions[this.selectStart], MARGIN_TOP + INSET_TOP,
                        charPositions[this.selectEnd] - charPositions[this.selectStart],
                        getHeight() - MARGIN_TOP - MARGIN_BOTTOM - INSET_BOTTOM - INSET_TOP - 2);
            }

            grx.setColor(fg);
            grx.drawString(actual, x, y);

            if (isUnderline()) {
                x = INSET_LEFT;
                y = getBaseLine() + 1;
                grx.drawLine(x, y, x + lwidth, y);
            }

            if (isOverline()) {
                x = INSET_LEFT;
                y = INSET_TOP + 1;
                grx.drawLine(x, y, x + lwidth, y);
            }

            if (isStrikethrough()) {
                x = INSET_LEFT;
                y = getCenterLine();
                grx.drawLine(x, y, x + lwidth, y);
            }

            if (isBoxed()) {
                x = INSET_LEFT;
                y = MARGIN_TOP + INSET_TOP;
                final int y2 = getHeight() - MARGIN_BOTTOM - INSET_BOTTOM - 1;
                grx.drawLine(x, y, x + w - 1, y);
                grx.drawLine(x, y2, x + w - 1, y2);
                grx.drawLine(x, y2, x + w - 1, y2);
                grx.drawLine(x, y, x, y2);
                grx.drawLine(x + w - 1, y, x + w - 1, y2);
            }

            // If selected, draw the caret.
            if (isSelected() && grx instanceof Graphics2D) {

                if (charPositions != null) {
                    if (!actual.isEmpty()) {
                        lwidth = charPositions[this.caret];
                    }
                }

                grx.setColor(CARET_COLOR);
                x += lwidth;
                grx.drawLine(x, y, x, y - fm.getAscent() + 2);
                grx.drawLine(x - 2, y + 2, x, y);
                grx.drawLine(x + 2, y + 2, x, y);
                grx.drawLine(x - 2, y - fm.getAscent(), x, y - fm.getAscent() + 2);
                grx.drawLine(x + 2, y - fm.getAscent(), x, y - fm.getAscent() + 2);
                x++;
                grx.drawLine(x, y, x, y - fm.getAscent() + 2);
                grx.drawLine(x - 2, y + 2, x, y);
                grx.drawLine(x + 2, y + 2, x, y);
                grx.drawLine(x - 2, y - fm.getAscent(), x, y - fm.getAscent() + 2);
                grx.drawLine(x + 2, y - fm.getAscent(), x, y - fm.getAscent() + 2);
            }
        }

        // Restore state of Graphics
        if (g2d != null) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, origHint);
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
    public DocInputExpressionFieldInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float) getFontSize(),
                getFontStyle());

        return new DocInputExpressionFieldInst(objStyle, null, getName(), getEnabledVarName(), getEnabledVarValue(),
                this.style, this.width.intValue());
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        // Since the text value should never contain any characters that would be invalid in XML
        // (", ', <, >, \, /), we can just write out the text.
        xml.add("<input type='expression'");
        addXmlAttributes(xml); // Add name, enabled, visible, maxLength

        if (this.width != null) {
            xml.add(" width='", this.width, "'");
        }

        if (this.style == EFieldStyle.UNDERLINE) {
            xml.add(" style='underline'");
        }

        final String txt = getTextValue();
        if (txt != null) {
            xml.add(" value='", XmlEscaper.escape(txt), "'");
        }

        final Formula enabled = getEnabledFormula();
        if (enabled != null) {
            xml.add(">");
            xml.add("<enabled>");
            enabled.appendChildrenXml(xml);
            xml.add("</enabled>");
            xml.add("</input>");
        } else {
            xml.add("/>");
        }
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>Input (Expression) '");
        ps.print(getTextValue());
        ps.println('\'');
        printTreeContents(ps);
        ps.print("</li>");
    }

    /**
     * Get the list of characters the field allows.
     *
     * @return a String made up of all valid characters
     */
    @Override
    protected String getValidCharacters() {

        return " !\"#$%&'()*+,-./01234569789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    }

    /**
     * Clear the input's value.
     */
    @Override
    public void clear() {

        innerSetTextValue(CoreConstants.EMPTY);
    }

    /**
     * Set the value of the input based on a String.
     *
     * @param theValue the String representation of the value to set
     * @return true if the value was set; false otherwise
     */
    @Override
    public boolean setValue(final String theValue) {

        final String theTag = "{" + getName() + "}=";

        if (theValue.startsWith(theTag)) {
            return setTextValue(theValue.substring(theTag.length()));
        }

        Log.info(Res.fmt(Res.BAD_ATTEMPT_TO_SET, getName(), theValue));

        return false;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return fieldInnerHashCode();
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
        } else if (obj instanceof final DocInputExpressionField field) {
            equal = fieldInnerEquals(field) && Objects.equals(this.width, field.width);
        } else {
            equal = false;
        }

        return equal;
    }
}
