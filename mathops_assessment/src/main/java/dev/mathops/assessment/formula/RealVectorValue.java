package dev.mathops.assessment.formula;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;

/**
 * An immutable vector of double values.
 */
public final class RealVectorValue {

    /** The vector elements. */
    private final double[] elements;

    /**
     * Constructs a new {@code RealVectorValue} with all elements set to zero.
     *
     * @param theElements the elements
     * @throws IllegalArgumentException if {@code theElements} is null or zero length
     */
    public RealVectorValue(final double... theElements) {

        if (theElements == null || theElements.length == 0) {
            throw new IllegalArgumentException("A vector must have at least one element");
        }

        this.elements = theElements.clone();
    }

    /**
     * Attempts to parse a {@code RealVectorValue} from a string.
     *
     * @param toParse the string to parse
     * @return the parsed value
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    public static RealVectorValue parse(final String toParse) throws IllegalArgumentException {

        if (toParse == null || toParse.isBlank()) {
            throw new IllegalArgumentException("Vector value string may not be null or blank");
        }

        final RealVectorValue result;

        final String trimmed = toParse.trim();
        if (!trimmed.isEmpty() && trimmed.charAt(0) == '[' && trimmed.charAt(trimmed.length() - 1) == ']') {
            final String inner = trimmed.substring(1, trimmed.length() - 1);
            final String[] split = inner.split(",");
            final int count = split.length;

            final double[] elements = new double[split.length];
            for (int i = 0; i < count; ++i) {
                elements[i] = Double.parseDouble(split[i].trim());
            }

            result = new RealVectorValue(elements);
        } else {
            throw new IllegalArgumentException("Vector value must be enclosed in [] brackets.");
        }

        return result;
    }

    /**
     * Gets the number of elements in the vector.
     *
     * @return the number of elements
     */
    int getNumElements() {

        return this.elements.length;
    }

    /**
     * Retrieves an element from the vector.
     *
     * @param index the index of the element to retrieve
     * @return the value
     */
    double getElement(final int index) {

        return this.elements[index];
    }

    /**
     * Returns a hash code value for the {@code RealVectorValue}.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {

        final int count = getNumElements();
        int hash = count;

        for (int row = 0; row < count; ++row) {
            final long bits = Double.doubleToLongBits(getElement(row));
            hash += (int) (bits ^ (bits >>> Integer.SIZE));
        }

        return hash;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * To be equal, {@code obj} must also be an {@code IColumnVector}, and must have the same number of elements and
     * same value in each element.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if this object is the same as {@code obj}; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        boolean equal;

        if (obj instanceof final RealVectorValue other) {
            final int count = getNumElements();
            equal = count == other.getNumElements();
            for (int row = 0; equal && row < count; ++row) {
                equal = getElement(row) == other.getElement(row);
            }
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates a string representation of the value suitable for use in an XML attribute, and appends it to an
     * {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    private void appendString(final HtmlBuilder xml) {

        final int count = getNumElements();

        xml.add('[');
        for (int row = 0; row < count; ++row) {
            if (row > 0) {
                xml.add(CoreConstants.COMMA_CHAR);
            }
            xml.add(getElement(row));
        }
        xml.add(']');
    }

    /**
     * Generates a string representation of the value.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder xml = new HtmlBuilder(30 * getNumElements());

        appendString(xml);

        return xml.toString();
    }
}
