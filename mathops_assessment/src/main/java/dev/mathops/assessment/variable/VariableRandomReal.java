package dev.mathops.assessment.variable;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.PrintStream;
import java.util.Objects;

/**
 * A random real-valued variable.
 */
public final class VariableRandomReal extends AbstractFormattableVariable implements IRangedVariable {

    /** XML type tag. */
    static final String TYPE_TAG = "random-real";

    /** The minimum random integer/real value. */
    private NumberOrFormula min;

    /** The maximum random integer/real value. */
    private NumberOrFormula max;

    /**
     * Constructs a new {@code VariableRandomReal}.
     *
     * @param theName the variable name
     */
    public VariableRandomReal(final String theName) {

        super(theName, EType.REAL);
    }

    /**
     * Gets the variable type.
     *
     * @return the variable type
     */
    @Override
    public EVariableType getVariableType() {

        return EVariableType.RANDOM_REAL;
    }

    /**
     * Tests whether this is an input variable.
     *
     * @return true if an input variable (false for objects of this class)
     */
    @Override
    public boolean isInput() {

        return false;
    }

    /**
     * Creates a copy of the variable. This is a deep copy that creates new copies of all mutable members. However,
     * generated values are discarded in the copy, and parameter change listeners are not carried over to the copy.
     *
     * @return a copy of the original object
     */
    @Override
    VariableRandomReal deepCopy() {

        final VariableRandomReal copy = new VariableRandomReal(this.name);

        populateCopyFromVariable(copy);

        if (this.min != null) {
            copy.min = this.min.deepCopy();
        }
        if (this.max != null) {
            copy.max = this.max.deepCopy();
        }

        return copy;
    }

    /**
     * Sets the minimum value.
     *
     * @param theMin the minimum value
     */
    @Override
    public void setMin(final NumberOrFormula theMin) {

        this.min = theMin;
    }

    /**
     * Gets the minimum value.
     *
     * @return the minimum value
     */
    @Override
    public NumberOrFormula getMin() {

        return this.min;
    }

    /**
     * Sets the maximum value.
     *
     * @param theMax the maximum value
     */
    @Override
    public void setMax(final NumberOrFormula theMax) {

        this.max = theMax;
    }

    /**
     * Gets the maximum value.
     *
     * @return the maximum value
     */
    @Override
    public NumberOrFormula getMax() {

        return this.max;
    }

    /**
     * Gets the hash code of the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return innerHashCode() + Objects.hashCode(this.min) + Objects.hashCode(this.max);
    }

    /**
     * Tests whether this object is equal to another object. To be equal, the other object must be a {@code Parameter}
     * and be in exactly the same state.
     *
     * @param obj the object to test for equality
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final VariableRandomReal var) {
            equal = innerEqualsVariable(var) && Objects.equals(this.min, var.min)
                    && Objects.equals(this.max, var.max);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Clears the cached derived value.
     */
    @Override
    public void clearDerivedValues() {

        setValue(null);
    }

    /**
     * Generates a string representation of the parameter, including any assigned value. The format of the generated
     * string is: name=value (type)
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add(this.name, " = ", getValue(), LPAREN, TYPE_TAG, " between ", this.min, " and ", this.max, RPAREN);

        return builder.toString();
    }

    /**
     * Appends the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void appendXml(final HtmlBuilder xml, final int indent) {

        final String ind0 = makeIndent(indent);
        final String ind1 = makeIndent(indent + 1);

        startXml(xml, indent, TYPE_TAG);
        writeAttribute(xml, "value", getValue());
        if (this.min != null) {
            writeAttribute(xml, "min", this.min.getNumber());
        }
        if (this.max != null) {
            writeAttribute(xml, "max", this.max.getNumber());
        }

        if ((this.min != null && this.min.getFormula() != null)
                || (this.max != null && this.max.getFormula() != null)) {

            xml.addln('>');

            if (this.min != null && this.min.getFormula() != null) {
                xml.add(ind1, "<min>");
                this.min.getFormula().appendChildrenXml(xml);
                xml.addln("</min>");
            }
            if (this.max != null && this.max.getFormula() != null) {
                xml.add(ind1, "<max>");
                this.max.getFormula().appendChildrenXml(xml);
                xml.addln("</max>");
            }

            xml.addln(ind0, "</var>");
        } else {
            xml.addln("/>");
        }
    }

    /**
     * Prints diagnostic information about a parameter, in HTML format, to a stream. The data will be embedded as a list
     * item in an unordered list.
     *
     * @param ps          the stream to print to
     * @param includeTree {@code true} to include a dump of the entire span tree structures
     */
    @Override
    public void printDiagnostics(final PrintStream ps, final boolean includeTree) {

        startDiagnostics(ps);

        ps.print(TYPE_TAG);
        if (this.min != null) {
            ps.print(" min=");
            ps.print(this.min);
        }
        if (this.max != null) {
            ps.print(" max=");
            ps.print(this.max);
        }

        if (hasValue()) {
            ps.print(" generated=");
            ps.print(getValue());
        }

        ps.println(RPAREN);
    }
}
