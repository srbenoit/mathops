package dev.mathops.assessment.formula;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.PrintStream;

/**
 * The base class for all entities that exist in a formula.
 */
public abstract class AbstractFormulaObject {

    /**
     * Construct a new {@code AbstractFormulaObjectBase}.
     */
    AbstractFormulaObject() {

        // No action
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    public abstract AbstractFormulaObject deepCopy();

    /**
     * Print the object to an output stream. This default implementation does nothing. Subclasses should override to
     * print the object and any children.
     *
     * @param ps the {@code PrintStream} to print to
     */
    public final void print(final PrintStream ps) {

        ps.print(this);
    }

    /**
     * Gets the type this object generates.
     *
     * @param context the context under which to evaluate the formula
     * @return the type
     */
    protected abstract EType getType(EvalContext context);

    /**
     * Evaluates the object within the tree. Subclasses should override this to produce the correct value.
     *
     * @param context the context under which to evaluate the formula
     * @return a Long, Double, Boolean, or DocSimpleSpan value of the object, or a String with an error message if
     *         unable to compute
     */
    public abstract Object evaluate(EvalContext context);

    /**
     * Generate the string representation of the object.
     *
     * @return the string representation of the object
     */
    @Override
    public abstract String toString();

    /**
     * Create a string for a particular indentation level.
     *
     * @param indent the number of spaces to indent
     * @return a string with the requested number of spaces
     */
    static String makeIndent(final int indent) {

        final HtmlBuilder builder = new HtmlBuilder(indent);

        for (int i = 0; i < indent; ++i) {
            builder.add(' ');
        }

        return builder.toString();
    }

    /**
     * Tests whether this object is a simple constant value.
     *
     * @return true if a constant value
     */
    public abstract boolean isConstant();

    /**
     * Simplifies a formula by replacing all parameter references to constant values with the constant itself, and then
     * performing any constant-valued evaluations. For example, if a formula contained "3 * ({x} - 4)" and the parameter
     * {x} was a constant integer with value 7, this formula would be simplified to a single integer constant with value
     * 9.
     *
     * @param context the context under which to evaluate the formula
     * @return the simplified version of this object (returns this object itself if already simplified)
     */
    public abstract AbstractFormulaObject simplify(EvalContext context);

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public abstract int hashCode();

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param obj the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Appends an XML representation of the formula to an {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    protected abstract void appendXml(HtmlBuilder xml);

    /**
     * Appends a diagnostic representation of the formula.
     *
     * @param xml    the {@code HtmlBuilder} to which to append
     * @param indent the indent level
     */
    protected abstract void printDiagnostics(HtmlBuilder xml, int indent);

}
