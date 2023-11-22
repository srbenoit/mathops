package dev.mathops.assessment.variable;

import dev.mathops.assessment.EType;
import dev.mathops.core.builder.HtmlBuilder;

import java.io.PrintStream;

/**
 * An integer-vector-valued input variable.
 */
public final class VariableInputIntegerVector extends AbstractVariable {

    /** XML type tag. */
    static final String TYPE_TAG = "input-int-vector";

    /**
     * Constructs a new {@code VariableInputIntegerVector}.
     *
     * @param theName the variable name
     */
    VariableInputIntegerVector(final String theName) {

        super(theName, EType.INTEGER_VECTOR);
    }

    /**
     * Gets the variable type.
     *
     * @return the variable type
     */
    @Override
    public EVariableType getVariableType() {

        return EVariableType.INPUT_INTEGER_VECTOR;
    }

    /**
     * Tests whether this is an input variable.
     *
     * @return true if an input variable (true for objects of this class)
     */
    @Override
    public boolean isInput() {

        return true;
    }

    /**
     * Creates a copy of the variable. This is a deep copy that creates new copies of all mutable members. However,
     * generated values are discarded in the copy, and parameter change listeners are not carried over to the copy.
     *
     * @return a copy of the original object
     */
    @Override
    VariableInputIntegerVector deepCopy() {

        final VariableInputIntegerVector copy = new VariableInputIntegerVector(this.name);

        populateCopy(copy);

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
        } else if (obj instanceof final VariableInputIntegerVector var) {
            equal = innerEquals(var);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Logs messages to indicate why this object is not equal to another.
     *
     * @param obj    the other object
     * @param indent the indent level
     */
    @Override
    public void whyNotEqual(final AbstractVariable obj, final int indent) {

        innerWhyNotEqual(obj, indent);
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

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add(this.name, " = ", getValue(), LPAREN, TYPE_TAG, RPAREN);

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

        startXml(xml, indent, TYPE_TAG);
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
        ps.print(getValue());
        ps.println(RPAREN);
    }
}
