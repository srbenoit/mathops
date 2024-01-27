package dev.mathops.assessment.formula;

import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A single case within a switch operation. Each case has an integer value to match, and a result value.
 */
public final class SwitchCase {

    /** The integer value to match. */
    final int toMatch;

    /** The value. */
    public final AbstractFormulaObject value;

    /**
     * Construct a new {@code SwitchCase}.
     *
     * @param theToMatch the integer value to match
     * @param theValue   the resulting value
     */
    SwitchCase(final int theToMatch, final AbstractFormulaObject theValue) {

        if (theValue == null) {
            throw new IllegalArgumentException("Value may not be null");
        }

        this.toMatch = theToMatch;
        this.value = theValue;
    }

    /**
     * Generate a deep copy of the object.
     *
     * @return the copy
     */
    public SwitchCase deepCopy() {

        return new SwitchCase(this.toMatch, this.value.deepCopy());
    }

    /**
     * Generate the string representation of the object.
     *
     * @param builder appends the string representation to a {@code HtmlBuilder}
     */
    void appendString(final HtmlBuilder builder) {

        builder.add(" case (" + this.toMatch + "): ", this.value.toString());
    }

    /**
     * Simplifies a formula by replacing all parameter references to constant values with the constant itself, and then
     * performing any constant-valued evaluations. For example, if a formula contained "3 * ({x} - 4)" and the parameter
     * {x} was a constant integer with value 7, this formula would be simplified to a single integer constant with value
     * 9.
     *
     * @param context the context under which to evaluate the formula
     * @return the simplified version of this object (returns this object itself if already simplified)
     */
    public SwitchCase simplify(final EvalContext context) {

        return new SwitchCase(this.toMatch, this.value.simplify(context));
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return this.toMatch + this.value.hashCode();
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param obj the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final SwitchCase sc) {
            equal = sc.toMatch == this.toMatch && sc.value.equals(this.value);
        } else {
            equal = false;
        }

        return equal;
    }

}
