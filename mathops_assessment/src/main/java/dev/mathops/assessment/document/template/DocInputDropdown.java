package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocInputDropdownInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableInputInteger;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.font.BundledFontManager;
import dev.mathops.text.builder.HtmlBuilder;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.PrintStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A document object that supports the entry of an integer value through the selection of an item from a dropdown box.
 */
public final class DocInputDropdown extends AbstractDocInput {

    /** The top inset. */
    static final int INSET_TOP = 2;

    /** The bottom inset. */
    static final int INSET_BOTTOM = 0;

    /** The left inset. */
    static final int INSET_LEFT = 5;

    /** The right inset. */
    private static final int INSET_RIGHT = 5;

    /** The top margin. */
    static final int MARGIN_TOP = 2;

    /** The bottom margin. */
    static final int MARGIN_BOTTOM = 2;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 8014953887713429659L;

    /** The value of the current selection (not its index in the list of selections). */
    private Long value;

    /** The default value. */
    Long defaultValue;

    /** The laid out field height (not including the popup). */
    private int fieldHeight;

    /** The expanded state of the dropdown. */
    private boolean expanded = false;

    /** The list of options. */
    private final List<DocInputDropdownOption> options;

    /**
     * Construct a new {@code DocInputDropdown}.
     *
     * @param theName the name of the input's value in the parameter set
     */
    DocInputDropdown(final String theName) {

        super(theName);

        this.options = new ArrayList<>(10);
    }

    /**
     * Adds an option.
     *
     * @param toAdd the option to add
     */
    void addOption(final DocInputDropdownOption toAdd) {

        this.options.add(toAdd);
    }

    /**
     * Gets the number of options.
     *
     * @return the number of options
     */
    public int getNumOptions() {

        return this.options.size();
    }

    /**
     * Gets the option at a specified index.
     *
     * @param index the index
     * @return the option
     */
    public DocInputDropdownOption getOption(final int index) {

        return this.options.get(index);
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

        AbstractVariable var = theContext.getVariable(getName());

        if (var == null) {
            var = new VariableInputInteger(getName());
            theContext.addVariable(var);
        } else if (!(var instanceof VariableInputInteger)) {
            throw new IllegalArgumentException(Res.fmt(Res.INCONSISTENT_TYPE, var.name,
                    var.getClass().getSimpleName(), VariableInputInteger.class.getSimpleName()));
        }

        if (this.defaultValue != null) {
            var.setValue(this.defaultValue);
        }
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocInputDropdown deepCopy() {

        final DocInputDropdown copy = new DocInputDropdown(getName());
        copy.copyObjectFromInput(this);

        copy.value = this.value;
        copy.defaultValue = this.defaultValue;

        for (final AbstractDocObjectTemplate child : getChildren()) {
            copy.add(child.deepCopy());
        }

        for (final DocInputDropdownOption option : this.options) {
            copy.addOption(new DocInputDropdownOption(option.getText(), option.getValue()));
        }

        return copy;
    }

    /**
     * Get the long integer value associated with the current selection.
     *
     * @return the selection value; null if there is no selection
     */
    public Long getValue() {

        return this.value;
    }

    /**
     * Sets the selection value.
     *
     * @param theValue the value to which to set the field
     */
    void setValue(final Long theValue) {

        Log.info("Set value to ", theValue);

        this.value = theValue;
        storeValue(this.value);

        notifyChangeListeners();
    }

    /**
     * Set the value of the field to a particular text string, parsing it into either an integer or double value.
     *
     * @param theText the text to which to set the input's value
     * @return true if the value was a valid number; false otherwise
     */
    private boolean setTextValue(final String theText) {

        final String oldVal = this.value == null ? null : this.value.toString();
        final String cleaned = theText == null || theText.isBlank() ? null : theText.trim();

        boolean ok = false;

        // If no change, do nothing.
        if (cleaned == null && oldVal == null) {
            if (this.defaultValue == null) {
                ok = true;
            }
        } else if (cleaned != null && cleaned.equals(oldVal)) {
            ok = true;
        }

        if (!ok) {
            ok = true;
            this.value = null;

            if (cleaned == null || "null".equals(cleaned)) {
                this.value = this.defaultValue;
            } else {
                try {
                    this.value = Long.valueOf(cleaned);
                } catch (final NumberFormatException ex) {
                    Log.warning("Unable tp parse '", cleaned, "' as a number");
                    ok = false;
                }
            }

            storeValue(this.value);

            if (getEvalContext() != null) {
                doLayout(getEvalContext(), ELayoutMode.INLINE_MATH);
            }

            notifyChangeListeners();
        }

        return ok;
    }

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context the evaluation context
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        final Font font = getFont();
        final FontMetrics fm = BundledFontManager.getInstance().getFontMetrics(font);

        int maxTextWidth = 0;
        for (final DocInputDropdownOption option : this.options) {
            final int textWidth = fm.stringWidth(option.getText());
            maxTextWidth = Math.max(textWidth, maxTextWidth);
        }

        final int w = maxTextWidth + fm.getHeight() + INSET_LEFT + INSET_RIGHT;

        setBaseLine(INSET_TOP + fm.getAscent());
        setCenterLine(INSET_TOP + ((fm.getAscent() << 1) / 3));

        setWidth(w);

        this.fieldHeight = fm.getAscent() + fm.getDescent() + INSET_TOP + INSET_BOTTOM;
        final int h = this.fieldHeight + getNumOptions() * fm.getHeight();
        setHeight(h);
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

        grx.setFont(getFont());

        final FontMetrics fm = grx.getFontMetrics();

        // Draw a drop shadow
        if (grx instanceof Graphics2D) {
            grx.setColor(SHADOW_COLOR);
            grx.drawRect(1, MARGIN_TOP + 1, w, this.fieldHeight - MARGIN_TOP - MARGIN_BOTTOM);
        }

        // Draw the field background, with color based on whether the field is selected and/or enabled.
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

        grx.fillRect(0, MARGIN_TOP, w, this.fieldHeight - MARGIN_TOP - MARGIN_BOTTOM);

        // Draw the field outline
        grx.setColor(fg);
        grx.drawRect(0, MARGIN_TOP, w, this.fieldHeight - MARGIN_TOP - MARGIN_BOTTOM);

        Graphics2D g2d = null;
        Object origHint = null;
        if (grx instanceof Graphics2D) {
            g2d = (Graphics2D) grx;
            origHint = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        String actual = CoreConstants.EMPTY;
        if (this.value != null) {
            for (final DocInputDropdownOption option : this.options) {
                if (option.getValue().equals(this.value)) {
                    actual = option.getText();
                    break;
                }
            }
        }

        final int lwidth = fm.stringWidth(actual);

        int x = INSET_LEFT;
        int y = getBaseLine();

        // For boxed, bounds are 4 pixels wider to accommodate box
        if (isBoxed()) {
            x += 2;
        }

        if (isVisible()) {
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

            grx.setColor(fg);
            final int arrowH = this.fieldHeight / 4;
            final int arrowW = arrowH * 2;
            int arrowX = w - INSET_RIGHT - arrowW;
            final int arrowTop = (this.fieldHeight - arrowH) / 2;
            final int arrowBot = arrowTop + arrowH;

            grx.drawLine(arrowX, arrowTop, arrowX + arrowW / 2, arrowBot);
            grx.drawLine(arrowX + arrowW / 2, arrowBot, arrowX + arrowW, arrowTop);

            grx.drawLine(arrowX, arrowTop + 1, arrowX + arrowW / 2, arrowBot + 1);
            grx.drawLine(arrowX + arrowW / 2, arrowBot + 1, arrowX + arrowW, arrowTop + 1);

            grx.drawLine(arrowX, arrowTop + 2, arrowX + arrowW / 2, arrowBot + 2);
            grx.drawLine(arrowX + arrowW / 2, arrowBot + 2, arrowX + arrowW, arrowTop + 2);

            if (this.expanded) {
                final int numOptions = getNumOptions();
                final int boxHeight = INSET_TOP + INSET_BOTTOM + fm.getHeight() * numOptions;

                // Popup shadow
                grx.setColor(SHADOW_COLOR);
                grx.drawRect(1, this.fieldHeight + 1, w, boxHeight);

                // Popup background
                grx.setColor(ENABLED_BG_COLOR);
                grx.fillRect(0, this.fieldHeight, w, boxHeight);

                // popup outline
                grx.setColor(ENABLED_FG_COLOR);
                grx.drawRect(0, this.fieldHeight, w, boxHeight);

                int xx = INSET_LEFT;
                int yy = this.fieldHeight + INSET_TOP + fm.getAscent() + fm.getLeading() / 2;

                for (final DocInputDropdownOption option : this.options) {
                    grx.drawString(option.getText(), xx, yy);
                    yy += fm.getHeight();
                }
            }
        }

        // Restore state of Graphics
        if (g2d != null) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, origHint);
        }

        postPaint(grx);
    }

    /**
     * Handler for mouse press actions. Mouse presses are propagated to all children, with the coordinates being
     * adjusted to the child's frame. This event is primarily used to detect the beginning of drag sequences.
     *
     * @param xCoord  the X coordinate (in the object's coordinate system)
     * @param yCoord  the Y coordinate (in the object's coordinate system)
     * @param context the evaluation context
     * @return {@code true} if a change requiring repaint occurred
     */
    public boolean processMousePress(final int xCoord, final int yCoord, final EvalContext context) {

        // Superclass handles selection/focus
        boolean repaint = super.processMousePress(xCoord, yCoord, context);

        final int w = getWidth();
        final int h = getHeight();

        if (isEnabled() && xCoord >= 0 && xCoord < w && yCoord >= 0 && yCoord < h) {
            final int left = w - h;

            if (this.expanded && yCoord > this.fieldHeight) {
                // The user is selecting an option
                final int heightPerOption = (getHeight() - this.fieldHeight) / getNumOptions();
                final int which = (yCoord - this.fieldHeight) / heightPerOption;
                final DocInputDropdownOption selected = this.options.get(which);
                setValue(selected.getValue());
                repaint = true;
            }

            if (xCoord > left) {
                this.expanded = !this.expanded;
                repaint = true;
            }
        }

        return repaint;
    }

    /**
     * Generate a string representation of the input value (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(50);

        if (this.value != null) {
            builder.add("{", getName(), "}=", this.value);
            return builder.toString();
        }

        return "null";
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
    public DocInputDropdownInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float) getFontSize(),
                getFontStyle());

        return new DocInputDropdownInst(objStyle, null, getName(), getEnabledVarName(), getEnabledVarValue(),
                this.defaultValue);
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
        xml.add("<input type='dropdown'");
        addXmlAttributes(xml); // Add name, enabled, visible, maxLength

        if (this.defaultValue != null) {
            xml.add(" default='", this.defaultValue, "'");
        }
        if (this.value != null) {
            xml.add(" value='", this.value, "'");
        }
        xml.add(">");

        final Formula enabled = getEnabledFormula();
        if (enabled != null) {
            xml.add("<enabled>");
            enabled.appendChildrenXml(xml);
            xml.add("</enabled>");
        }

        for (final DocInputDropdownOption option : this.options) {
            option.toXml(xml, 0);
        }

        xml.add("</input>");
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

        // In LaTeX, we emit as a set of radio buttons with all the options.

        for (final DocInputDropdownOption option : this.options) {
            builder.add("\\mbox{ \\xy<3pt,3pt>*\\cir<6pt>{}\\endxy } ");
            builder.addln(option.getText());
        }
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>Input (Dropdown) '");
        ps.print(getValue());
        ps.println('\'');
        printTreeContents(ps);
        ps.print("</li>");
    }

    /**
     * Clear the input's value.
     */
    @Override
    public void clear() {

        this.value = null;
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

        Log.info(Res.fmt(Res.BAD_ATTEMPT_TO_SET, getName(), this.value));

        return false;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return inputInnerHashCode() + Objects.hashCode(this.value)
               + Objects.hashCode(this.defaultValue);
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
        } else if (obj instanceof final DocInputDropdown field) {
            equal = inputInnerEquals(field)
                    && Objects.equals(this.value, field.value)
                    && Objects.equals(this.defaultValue, field.defaultValue);
        } else {
            equal = false;
        }

        return equal;
    }
}
