package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An expression object that represents a Boolean constant.
 */
public final class ExprLeafBoolean extends ExprObjectLeaf {

    /** The boolean value. */
    private final boolean value;

    /**
     * Constructs a new {@code ExprLeafBoolean}.
     *
     * @param theValue the boolean value
     */
    public ExprLeafBoolean(final boolean theValue) {

        super();

        this.value = theValue;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public boolean getValue() {

        return this.value;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("ExprLeafBoolean{value='");
        htm.add(this.value);
        htm.add("}");

        return htm.toString();
    }
}
