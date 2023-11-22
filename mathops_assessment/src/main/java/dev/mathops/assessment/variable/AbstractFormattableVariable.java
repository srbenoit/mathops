package dev.mathops.assessment.variable;

import dev.mathops.assessment.EType;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * A variable that supports a format string and decimal format.
 */
public abstract class AbstractFormattableVariable extends AbstractVariable {

    /** The pattern for the decimal formatter. */
    private String formatPattern;

    /** The formatter to use when printing the value, if numeric. */
    private DecimalFormat decimalFormat;

    /**
     * Constructs a new {@code AbstractFormattableVariable}.
     *
     * @param theName the variable name
     * @param theType the type of value this variable can store
     */
    AbstractFormattableVariable(final String theName, final EType theType) {

        super(theName, theType);
    }

    /**
     * Gets the format pattern.
     *
     * @return the format pattern
     */
    public final String getFormatPattern() {

        return this.formatPattern;
    }

    /**
     * Creates a copy of the parameter. This is a deep copy that creates new copies of all mutable members. However,
     * generated values are discarded in the copy, and parameter change listeners are not carried over to the copy.
     *
     * @return a copy of the original object
     */
    @Override
    abstract AbstractFormattableVariable deepCopy();

    /**
     * Populates the fields of a copy of this variable.
     *
     * @param copy the copy
     */
    final void populateCopyFromVariable(final AbstractFormattableVariable copy) {

        populateCopy(copy);

        copy.setFormatPattern(this.formatPattern);
    }

    /**
     * Sets the format pattern to use when displaying this parameter's numeric value. This causes the
     * {@code DecimalFormat} to be re-generated if the supplied format is valid. If the supplied format is not valid, it
     * is not stored.
     *
     * @param theFormat the decimal format string (see java.text.DecimalFormat)
     */
    final void setFormatPattern(final String theFormat) {

        this.formatPattern = theFormat;

        if (theFormat == null) {
            this.decimalFormat = null;
        } else {
            try {
                this.decimalFormat = new DecimalFormat(theFormat);
            } catch (final IllegalArgumentException e) {
                this.formatPattern = null;
                this.decimalFormat = null;
            }
        }
    }

    /**
     * Gets the format to use when displaying this parameter's numeric value.
     *
     * @return the decimal format string (see java.text.DecimalFormat)
     */
    public final DecimalFormat getDecimalFormat() {

        return this.decimalFormat;
    }

    /**
     * Retrieves a string representation of this parameter's value.
     *
     * @return the parameter value string
     */
    @Override
    public final String valueAsString() {

        final Object value = getValue();
        String str;

        if (value instanceof final Long longValue) {

            if (this.decimalFormat != null) {
                str = this.decimalFormat.format(longValue.longValue());
            } else {
                final DecimalFormat fmt = new DecimalFormat();
                str = fmt.format(longValue.longValue());
            }
            str = str.replace('-', '\u2212');
        } else if (value instanceof final Integer intValue) {

            if (this.decimalFormat != null) {
                str = this.decimalFormat.format(intValue.longValue());
            } else {
                final DecimalFormat fmt = new DecimalFormat();
                str = fmt.format(intValue.longValue());
            }
            str = str.replace('-', '\u2212');
        } else if (value instanceof final Double doubleValue) {

            if (this.decimalFormat != null) {
                str = this.decimalFormat.format(doubleValue.doubleValue());
            } else {
                // By default, we truncate reals at 8 decimal points
                final DecimalFormat fmt = new DecimalFormat();
                fmt.setMaximumFractionDigits(8);
                str = fmt.format(doubleValue.doubleValue());
            }
            str = str.replace('-', '\u2212');
        } else if (value instanceof Boolean) {
            str = ((Boolean) value).toString().toUpperCase(Locale.ROOT);
        } else if (value instanceof String) {
            str = (String) value;
        } else if (value != null) {
            str = value.toString();
        } else {
            str = "null";
        }

        return str;
    }

    /**
     * Starts the XML representation, emitting attributes for members in this base class.
     *
     * @param xml       the {@code HtmlBuilder} to which to write the XML
     * @param indent    the number of spaces to indent the printout
     * @param typeLabel the type (the value of the 'type' attribute)
     */
    @Override
    protected final void startXml(final HtmlBuilder xml, final int indent,
                                  final String typeLabel) {

        super.startXml(xml, indent, typeLabel);
        writeAttribute(xml, "decimal-format", this.formatPattern);
    }

    /**
     * Gets the hash code of the fields in the base class.
     *
     * @return the hash code
     */
    @Override
    protected final int innerHashCode() {

        return super.innerHashCode() + EqualityTests.objectHashCode(this.formatPattern);
    }

    /**
     * Tests whether this object is equal to another object. To be equal, the other object must be a {@code Parameter}
     * and be in exactly the same state.
     *
     * @param obj the object to test for equality
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    final boolean innerEqualsVariable(final AbstractFormattableVariable obj) {

        return innerEquals(obj) && Objects.equals(this.formatPattern, obj.formatPattern);
    }

    /**
     * Logs messages to indicate why this object is not equal to another.
     *
     * @param obj    the other object
     * @param indent the indent level
     */
    final void innerWhyNotEqualVariable(final AbstractFormattableVariable obj, final int indent) {

        innerWhyNotEqual(obj, indent);

        if (!Objects.equals(this.formatPattern, obj.formatPattern)) {
            Log.info(makeIndent(indent), "UNEQUAL ", getClass().getSimpleName(),
                    " (formatPattern: " + this.formatPattern + "!=" + obj.formatPattern + ")");
        }
    }
}
