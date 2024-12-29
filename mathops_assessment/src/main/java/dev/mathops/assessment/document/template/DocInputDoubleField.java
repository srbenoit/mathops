package dev.mathops.assessment.document.template;

import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.EFieldStyle;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.inst.DocInputDoubleFieldInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableInputReal;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.PrintStream;
import java.io.Serial;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A document object that supports the entry of a real number value. Real numbers may be entered using normal or
 * scientific notation. The input control allows definition of all document formatting characteristics. Real number
 * fields are drawn in a shaded outline, which highlights when the object is selected. When selected, a text edit caret
 * is shown and editing is supported.
 */
public final class DocInputDoubleField extends AbstractDocInputField {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 329971597754305390L;

    /** A pre-compiled regular expression pattern. */
    private static final Pattern PATTERN = Pattern.compile(CoreConstants.COMMA);

    /** The real value. */
    private Double value;

    /** The width. */
    public Integer width;

    /** The integer default value. */
    Double defaultValue;

    /** The value to substitute when the user has entered only "-". */
    Double minusAs;

    /**
     * Construct a new {@code DocInputDoubleField}.
     *
     * @param theName the name of the input's value in the parameter set
     */
    DocInputDoubleField(final String theName) {

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

        AbstractVariable var = theContext.getVariable(getName());

        if (var == null) {
            var = new VariableInputReal(getName());
            theContext.addVariable(var);
        } else if (!(var instanceof VariableInputReal)) {
            throw new IllegalArgumentException(Res.fmt(Res.INCONSISTENT_TYPE, var.name,
                    var.getClass().getSimpleName(), VariableInputReal.class.getSimpleName()));
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
    public DocInputDoubleField deepCopy() {

        final DocInputDoubleField copy = new DocInputDoubleField(getName());
        copy.copyObjectFromInput(this);

        copy.innerSetTextValue(getTextValue());
        copy.style = this.style;
        copy.value = this.value;
        copy.width = this.width;
        copy.defaultValue = this.defaultValue;
        copy.minusAs = this.minusAs;

        for (final AbstractDocObjectTemplate child : getChildren()) {
            copy.add(child.deepCopy());
        }

        return copy;
    }

    /**
     * Set the value of the field to a particular text string, parsing it into either an integer or double value.
     *
     * @param theText the text to which to set the input's value
     * @return true if the value was a valid number; false otherwise
     */
    @Override
    public synchronized boolean setTextValue(final String theText) {

        final String oldVal = getTextValue();

//        Log.info("setTextValue called on '", getName(), "' with ", theText, ", oldVal = ", oldVal,
//                ", value = ", this.value);

        final String oldCleaned = cleanValue(oldVal);
        final String cleaned = cleanValue(theText);

        boolean ok = false;

        // If no change, do nothing.
        if (cleaned == null && oldVal == null) {
            if (this.defaultValue == null) {
                ok = true;
            }
        } else if (cleaned != null && cleaned.equals(oldCleaned)) {
            ok = true;
        }

        if (!ok) {
            ok = true;
            this.value = null;

            setCharPositions(null);
            this.selectStart = -1;
            this.selectEnd = -1;
            this.caret = 0;

            if (cleaned == null || "null".equals(cleaned)) {
                innerSetTextValue(CoreConstants.EMPTY);
                this.caret = 0;
                this.value = this.defaultValue;
            } else {
                innerSetTextValue(cleaned);
                this.caret = cleaned.length();

                if (CoreConstants.DASH.equals(cleaned)) {
                    if (this.minusAs == null) {
                        ok = false;
                    } else {
                        this.value = this.minusAs;
                    }
                } else {
                    this.caret = cleaned.length();

                    // Allowed formats:
                    // 123.456
                    // -123.456
                    // 123.45/67.89 (with minus sign allowed on either numerator or denominator)
                    // Any number can be followed by a "PI" or "E" symbol, like 360/2PI or 5PI/6

                    try {
                        final Number parsedNumber = NumberParser.parse(cleaned);
                        final double d = parsedNumber.doubleValue();
                        this.value = Double.valueOf(d);
                    } catch (final NumberFormatException ex) {
                        Log.warning("Unable tp parse '", cleaned, "' as a number");
                        ok = false;
                    }
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
     * Cleans a string value.
     *
     * @param theText the string to be cleaned
     * @return the cleaned string (null if the text was effectively empty)
     */
    private static String cleanValue(final String theText) {

        String cleaned;
        if (theText == null) {
            cleaned = null;
        } else {
            cleaned = theText.trim().replace(CoreConstants.COMMA, CoreConstants.EMPTY);
            if (!cleaned.isEmpty()) {
                if (cleaned.charAt(0) == '(' && cleaned.charAt(cleaned.length() - 1) == ')') {
                    cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
                }
                if (cleaned.startsWith("-(") && cleaned.charAt(cleaned.length() - 1) == ')') {
                    cleaned = "-" + cleaned.substring(2, cleaned.length() - 1).trim();
                }
                if (!cleaned.isEmpty() && cleaned.charAt(0) == '$') {
                    cleaned = cleaned.substring(1);
                }
                if (!cleaned.isEmpty() && cleaned.charAt(cleaned.length() - 1) == '%') {
                    cleaned = cleaned.substring(0, cleaned.length() - 1);
                }
            }
            cleaned = PATTERN.matcher(cleaned).replaceAll(CoreConstants.EMPTY);
            if (cleaned.isEmpty()) {
                cleaned = null;
            }
        }

        return cleaned;
    }

    /**
     * Attempts to parse a numeric string that could be a number, or a number coefficient preceding the PI symbol or the
     * letter "e".
     *
     * @param str the string to parse
     * @return the result; null if unable to parse
     */
    private Double parseNumericString(final String str) {

        Double result = null;

        if (!str.isEmpty()) {
            switch (str) {
                case "\u03c0" -> result = Double.valueOf(Math.PI);
                case "-\u03c0" -> result = Double.valueOf(-Math.PI);
                case CoreConstants.DASH -> result = this.minusAs;
                case "e" -> result = Double.valueOf(Math.E);
                case "-e" -> result = Double.valueOf(-Math.E);
                default -> {
                    final int lastChar = str.charAt(str.length() - 1);
                    if (lastChar == '\u03c0' || lastChar == 'e') {
                        try {
                            final double coef = Double.parseDouble(str.substring(0, str.length() - 1));
                            result = Double.valueOf(coef * (lastChar == '\u03c0' ? Math.PI : Math.E));
                        } catch (final NumberFormatException e) {
                            Log.warning(Res.get(Res.INVALID_NUMBER));
                        }
                    } else {
                        try {
                            result = Double.valueOf(str);
                        } catch (final NumberFormatException e) {
                            Log.warning(Res.get(Res.INVALID_NUMBER), ": ", str);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Get the real value of the field, if the value is valid as a real number.
     *
     * @return the number value as a Double, or null if the value is not a valid number
     */
    public Double getDoubleValue() {

        return this.value;
    }

    /**
     * Set the value of the field to a real number.
     *
     * @param theValue the value to which to set the field
     */
    void setOnlyDoubleValue(final Double theValue) {

        this.value = theValue;
    }

    /**
     * Set the value of the field to a real number.
     *
     * @param theValue the value to which to set the field. The field text is set to the canonical representation of the
     *                 number
     */
    public void setDoubleValue(final Double theValue) {

        setCharPositions(null);
        this.selectStart = -1;
        this.selectEnd = -1;
        this.caret = 0;

        if (theValue == null) {
            this.value = null;
            innerSetTextValue(CoreConstants.EMPTY);
            this.caret = 0;
        } else {
            this.value = theValue;

            // Only change text if current text does not equal value.
            if (getTextValue() != null) {

                try {
                    if (!theValue.equals(Double.valueOf(getTextValue()))) {
                        innerSetTextValue(theValue.toString());
                    }
                } catch (final NumberFormatException e) {
                    innerSetTextValue(theValue.toString());
                }
            } else {
                innerSetTextValue(theValue.toString());
            }

            this.caret = getTextValue().length();
        }

        storeValue(this.value);

        if (getEvalContext() != null) {
            doLayout(getEvalContext(), ELayoutMode.INLINE_MATH);
        }

        notifyChangeListeners();
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
        boolean drawBg = this.style == EFieldStyle.BOX;
        final Color fg;
        if (!isEnabled() && (grx instanceof Graphics2D)) {
            grx.setColor(DISABLED_BG_COLOR);
            fg = DISABLED_FG_COLOR;
        } else if (!isSelected()) {
            grx.setColor(ENABLED_BG_COLOR);
            fg = ENABLED_FG_COLOR;
        } else {
            // If we have a text value, but no numeric value, there must be an error in the text value format.
            final String text = getTextValue();
            if (text == null || text.isEmpty()) {
                grx.setColor(SELECTED_BG_COLOR);
            } else if (this.value == null) {
                grx.setColor(ERROR_BG_COLOR);
                drawBg = true;
            } else {
                grx.setColor(SELECTED_BG_COLOR);
            }

            fg = SELECTED_FG_COLOR;
        }

        if (drawBg) {
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
        Object origHints = null;
        if (grx instanceof Graphics2D) {
            g2d = (Graphics2D) grx;
            origHints = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
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
            if (isSelected() && (grx instanceof Graphics2D)) {

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
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, origHints);
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
    public DocInputDoubleFieldInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float) getFontSize(),
                getFontStyle());

        return new DocInputDoubleFieldInst(objStyle, null, getName(), getEnabledVarName(), getEnabledVarValue(),
                this.style, this.width.intValue(), this.defaultValue, this.minusAs);
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        // Since the text value should never contain any characters that would be invalid in XML
        // (", ', <, >, \, /), we can just write out the text.
        xml.add("<input type='real'");
        addXmlAttributes(xml); // Add name, enabled, visible, maxLength

        if (this.width != null) {
            xml.add(" width='", this.width, "'");
        }

        if (this.defaultValue != null) {
            xml.add(" default='", this.defaultValue, "'");
        }

        if (this.style == EFieldStyle.UNDERLINE) {
            xml.add(" style='underline'");
        }

        if (this.minusAs != null) {
            xml.add(" treat-minus-as='", this.minusAs, "'");
        }

        if (getTextValue() != null) {
            xml.add(" textValue='", getTextValue(), "'");
        }

        if (this.value != null) {
            xml.add(" value='", this.value, "'");
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

        ps.print("<li>Input (Real) '");
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

        return "-1234567890.e/\u03c0";
    }

    /**
     * Clear the input's value.
     */
    @Override
    public void clear() {

        setTextValue(null);
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

        final boolean result;

        if (theValue.startsWith(theTag)) {
            result = setTextValue(theValue.substring(theTag.length()));
        } else {
            Log.warning(Res.fmt(Res.BAD_ATTEMPT_TO_SET, getName(), theValue));
            result = false;
        }

        return result;
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return fieldInnerHashCode()
                + Objects.hashCode(this.value)
                + Objects.hashCode(this.defaultValue)
                + Objects.hashCode(this.minusAs);
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
        } else if (obj instanceof final DocInputDoubleField field) {
            equal = fieldInnerEquals(field)
                    && Objects.equals(this.value, field.value)
                    && Objects.equals(this.width, field.width)
                    && Objects.equals(this.defaultValue, field.defaultValue)
                    && Objects.equals(this.minusAs, field.minusAs);
        } else {
            equal = false;
        }

        return equal;
    }
}
