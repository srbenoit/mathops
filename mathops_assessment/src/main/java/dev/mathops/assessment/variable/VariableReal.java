package dev.mathops.assessment.variable;

import dev.mathops.assessment.EType;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.PrintStream;

/**
 * An integer-valued variable.
 */
public final class VariableReal extends AbstractFormattableVariable {

    /** XML type tag. */
    static final String TYPE_TAG = "real";

    /**
     * Constructs a new {@code VariableReal}.
     *
     * @param theName the variable name
     */
    public VariableReal(final String theName) {

        super(theName, EType.REAL);
    }

    /**
     * Gets the variable type.
     *
     * @return the variable type
     */
    @Override
    public EVariableType getVariableType() {

        return EVariableType.REAL;
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
    VariableReal deepCopy() {

        final VariableReal copy = new VariableReal(this.name);

        populateCopyFromVariable(copy);

        return copy;
    }

    /**
     * Gets the hash code of the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return innerHashCode();
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
        } else if (obj instanceof final VariableReal variable) {
            equal = innerEqualsVariable(variable);
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

        // No action
    }

    /**
     * Generates a string representation of the parameter, including any assigned value. The format of the generated
     * string is: name=value (type)
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        final Object value = getValue();
        htm.add(this.name, " = ", value, LPAREN, TYPE_TAG, RPAREN);

        return htm.toString();
    }

    /**
     * Appends the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void appendXml(final HtmlBuilder xml, final int indent) {

        startXml(xml, indent, TYPE_TAG);
        final Object value = getValue();
        writeAttribute(xml, "value", value);
        xml.addln("/>");
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
        ps.print(" value=");
        final Object value = getValue();
        ps.print(value);
        ps.println(RPAREN);
    }
}
