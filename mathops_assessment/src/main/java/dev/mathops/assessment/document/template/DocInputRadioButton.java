package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocInputRadioButtonInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableInputInteger;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.font.BundledFontManager;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;

/**
 * A document object that supports a boolean choice. The choice will be presented to the student as a radio button,
 * which can either be checked or unchecked, or can be in an unanswered state.
 */
public final class DocInputRadioButton extends AbstractDocInput {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8535240309962107214L;

    /** The top inset. */
    private static final int INSET_TOP = 2;

    /** The right inset. */
    private static final int INSET_RIGHT = 9;

    /** The bottom inset. */
    private static final int INSET_BOTTOM = 0;

    /** The left inset. */
    private static final int INSET_LEFT = 5;

    /** The state of the radio button. */
    public final int value;

    /**
     * Construct a new {@code DocInputRadioButton}.
     *
     * @param theName     the name of the input's value in the parameter set
     * @param theValue the value corresponding to this choice
     */
    DocInputRadioButton(final String theName, final int theValue) {

        super(theName);

        this.value = theValue;
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
            theContext.addVariable(new VariableInputInteger(getName()));
        } else if (!(var instanceof VariableInputInteger)) {
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
    public DocInputRadioButton deepCopy() {

        final DocInputRadioButton copy = new DocInputRadioButton(getName(), this.value);

        copy.copyObjectFromInput(this);

        for (final AbstractDocObjectTemplate child : getChildren()) {
            copy.add(child.deepCopy());
        }

        return copy;
    }

    /**
     * Determine whether this choice is currently selected. The choice is selected if the named integer parameter is set
     * to this choice's value.
     *
     * @return true if selected; false if not
     */
    public boolean isChoiceSelected() {

        synchronized (this) {
            boolean isSelected = false;

            final EvalContext ctx = getEvalContext();
            if (ctx != null) {
                final AbstractVariable param = ctx.getVariable(getName());

                if (param != null) {
                    final Object theValue = param.getValue();

                    if (theValue instanceof Long) {
                        isSelected = ((Long) theValue).longValue() == (long) this.value;
                    } else if (theValue instanceof Integer) {
                        isSelected = ((Integer) theValue).intValue() == this.value;
                    }
                }
            }

            return isSelected;
        }
    }

    /**
     * Set this choice as the selected choice in the parameter.
     */
    void selectChoice() {

        synchronized (this) {
            // This could get called during deserialization before input is bound - trust
            // deserialization to restore state without requiring this action

            if (getEvalContext() != null) {
                final AbstractVariable param = getEvalContext().getVariable(getName());

                // If value is not changing, do nothing.
                if (param instanceof VariableInputInteger && (param.getValue() instanceof Long)
                        && (param.getValue().equals(Long.valueOf((long) this.value)))) {

                    return;
                }

                storeValue(Long.valueOf((long) this.value));
                doLayout(getEvalContext(), ELayoutMode.TEXT);
                notifyChangeListeners();
            }
        }
    }

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context the evaluation context
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        final FontMetrics fm = BundledFontManager.getInstance().getFontMetrics(getFont());
        final int w = fm.getAscent() + INSET_LEFT + INSET_RIGHT;

        setBaseLine(INSET_TOP + fm.getAscent());
        setCenterLine(INSET_TOP + ((fm.getAscent() << 1) / 3));

        setWidth(w);
        final int h = fm.getAscent() + fm.getDescent() + INSET_TOP + INSET_BOTTOM;
        setHeight(h);
    }

    /**
     * Generate a string representation of the input value (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(50);

        if (isChoiceSelected()) {
            builder.add("{", getName(), "}=", Integer.toString(this.value));

            return builder.toString();
        }

        return "null";
    }

    /**
     * Handler for mouse press actions. Mouse presses are propagated to all children, with the coordinates being
     * adjusted to the child's frame. This event is primarily used to detect the beginning of drag sequences.
     *
     * @param xCoord the X coordinate (in the object's coordinate system)
     * @param yCoord the Y coordinate (in the object's coordinate system)
     * @return true if a change requiring repaint occurred
     */
    @Override
    public boolean processMousePress(final int xCoord, final int yCoord, final EvalContext context) {

        boolean repaint = false;

        if (isEnabled()) {
            if (xCoord >= 0 && xCoord < getWidth() && yCoord >= 0 && yCoord < getHeight()) {
                selectChoice();
                setSelected(true);
                repaint = true;
            } else if (isSelected()) {
                setSelected(false);
                repaint = true;
            }
        }

        // Forward mouse click to all children
        if (super.processMousePress(xCoord, yCoord, context)) {
            repaint = true;
        }

        return repaint;
    }

    /**
     * Handler for key presses. Keys are propagated to all children, but only children who have focus should react to
     * them.
     *
     * @param keyChar   the key character
     * @param keyCode   the key code
     * @param modifiers modifiers (CTRL, ALT, SHIFT, etc.) to the key press
     * @return true if a change requiring repaint occurred
     */
    @Override
    public boolean processKey(final char keyChar, final int keyCode, final int modifiers,
                              final EvalContext context) {

        boolean repaint = false;

        // React only if we are enabled and have focus.
        if (isEnabled() && isSelected() && keyChar == ' ') {
            selectChoice();
            repaint = true;
        }

        // Forward key to all children
        if (super.processKey(keyChar, keyCode, modifiers, context)) {
            repaint = true;
        }

        return repaint;
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

        Graphics2D g2d = null;
        Object origHints = null;
        if (grx instanceof Graphics2D) {
            g2d = (Graphics2D) grx;
            origHints = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }

        final int size = getWidth() - INSET_LEFT - INSET_RIGHT;

        // Draw a drop shadow
        if ((SHADOW_COLOR != null) && (grx instanceof Graphics2D)) {
            grx.setColor(SHADOW_COLOR);
            grx.drawOval(INSET_LEFT + 1, INSET_TOP + 1, size, size);
        }

        // Draw the field background, with color based on whether the field is selected and/or enabled.
        final Color fg;
        if (!isEnabled() && (grx instanceof Graphics2D)) {
            grx.setColor(DISABLED_BG_COLOR);
            fg = DISABLED_FG_COLOR;
        } else if (!isSelected() && (grx instanceof Graphics2D)) {
            grx.setColor(ENABLED_BG_COLOR);
            fg = ENABLED_FG_COLOR;
        } else {
            grx.setColor(SELECTED_BG_COLOR);
            fg = SELECTED_FG_COLOR;
        }

        grx.fillOval(INSET_LEFT, INSET_TOP, size, size);

        // Draw the field outline
        grx.setColor(fg);
        grx.drawOval(INSET_LEFT, INSET_TOP, size, size);

        // If selected, draw a selection outline in the caret color
        if (isSelected() && (grx instanceof Graphics2D)) {
            grx.setColor(CARET_COLOR);
            grx.drawOval(INSET_LEFT + 1, INSET_TOP + 1, size - 2, size - 2);
        }

        // Draw the check if value is TRUE
        if (isChoiceSelected()) {
            grx.setColor(fg);
            final int x = INSET_LEFT + 5;
            final int y = INSET_TOP + 5;
            grx.fillOval(x, y, size - 9, size - 9);
        }

        // Restore state of Graphics
        if (g2d != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, origHints);
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
     * @return the instance document object; null if unable to create the instance
     */
    @Override
    public DocInputRadioButtonInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float) getFontSize(),
                getFontStyle());

        return new DocInputRadioButtonInst(objStyle, null, getName(), getEnabledVarName(), getEnabledVarValue(),
                (long) this.value);
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
        xml.add("<input type='radio-button'");
        addXmlAttributes(xml); // Add name, enabled, visible.

        xml.add(" value='", Integer.toString(this.value), "' selected='", Boolean.toString(isChoiceSelected()), "'>");

        final Formula enabled = getEnabledFormula();
        if (enabled != null) {
            xml.add("<enabled>");
            enabled.appendChildrenXml(xml);
            xml.add("</enabled>");
        }

        xml.add("</input>");
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>Input (Radio) '");
        ps.print(this.value);
        ps.println('\'');
        printTreeContents(ps);
        ps.print("</li>");
    }

    /**
     * Clear the input's value.
     */
    @Override
    public void clear() {

        storeValue(null);
        if (getEvalContext() != null) {
            doLayout(getEvalContext(), ELayoutMode.TEXT);
        }
    }

    /**
     * Set the value of the input based on a String.
     *
     * @param theValue the String representation of the value to set
     * @return true if the value was set; false otherwise
     */
    @Override
    public boolean setValue(final String theValue) {

        boolean result;

        final String theTag = "{" + getName() + "}=";

        if (theValue.startsWith(theTag)) {
            final String toParse = theValue.substring(theTag.length());
            if (toParse.isBlank()) {
                result = false;
            } else {
                final int valueToSet;
                try {
                    valueToSet = Long.valueOf(theValue.substring(theTag.length())).intValue();

                    if (valueToSet == this.value) {
                        selectChoice();
                    }

                    result = true;
                } catch (final NumberFormatException e) {
                    Log.warning(Res.fmt(Res.UNABLE_TO_PARSE, theValue));
                    result = false;
                }
            }

        } else {
            Log.info(Res.fmt(Res.BAD_ATTEMPT_TO_SET, getName(), theValue));
            result = false;
        }

        return result;
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

        // TODO: The input does not know its correctness
        builder.add("\\mbox{ \\xy<3pt,3pt>*\\cir<6pt>{}\\endxy } ");
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return inputInnerHashCode() + this.value;
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
        } else if (obj instanceof final DocInputRadioButton button) {
            equal = inputInnerEquals(button) && this.value == button.value;
        } else {
            equal = false;
        }

        return equal;
    }
}
