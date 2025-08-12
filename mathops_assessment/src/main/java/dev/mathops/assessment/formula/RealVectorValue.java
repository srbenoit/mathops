package dev.mathops.assessment.formula;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.number.NumberParser;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable vector of double values.
 */
public final class RealVectorValue {

    /** The vector elements. */
    private final Number[] elements;

    /**
     * Constructs a new {@code RealVectorValue}.
     *
     * @param theElements the elements
     * @throws IllegalArgumentException if {@code theElements} is null or zero length
     */
    public RealVectorValue(final double... theElements) {

        if (theElements == null || theElements.length == 0) {
            throw new IllegalArgumentException("A real vector must have at least one element");
        }
        final int len = theElements.length;

        this.elements = new Number[len];
        for (int i = 0; i < len; ++i) {
            this.elements[i] = Double.valueOf(theElements[i]);
        }
    }

    /**
     * Constructs a new {@code RealVectorValue}.
     *
     * @param theElements the elements
     * @throws IllegalArgumentException if {@code theElements} is null or zero length
     */
    public RealVectorValue(final Number... theElements) {

        if (theElements == null || theElements.length == 0) {
            throw new IllegalArgumentException("A real vector must have at least one element");
        }

        for (final Number theElement : theElements) {
            if (theElement == null) {
                throw new IllegalArgumentException("Vector elements may not be null");
            }
        }

        this.elements = theElements.clone();
    }

    /**
     * Attempts to parse a {@code RealVectorValue} from a string.
     *
     * @param toParse the string to parse
     * @return the parsed value
     * @throws NumberFormatException if the string cannot be parsed
     */
    public static RealVectorValue parse(final String toParse) {

        if (toParse == null) {
            throw new NumberFormatException("Real vector value string may not be null");
        }

        final String inner = extractVectorContent(toParse);

        final String[] split = inner.split(CoreConstants.COMMA);
        final int count = split.length;

        final Number[] elements = new Number[split.length];
        for (int i = 0; i < count; ++i) {
            final String trimmedElement = split[i].trim();
            elements[i] = NumberParser.parse(trimmedElement);
        }

        return new RealVectorValue(elements);
    }

    /**
     * Given a real vector string, which may or may not have surrounding square brackets, extract the interior
     * comma-separated list of real values.
     *
     * @param toParse the string to parse
     * @throws NumberFormatException if the string cannot be parsed
     */
    private static String extractVectorContent(final String toParse) {

        final String trimmed = toParse.trim();
        if (trimmed.isEmpty()) {
            throw new NumberFormatException("Real vector value may not be empty.");
        }

        final int len = trimmed.length();

        final boolean hasLeadingBracket = (int) trimmed.charAt(0) == '[';
        final boolean hasTrailingBracket = (int) trimmed.charAt(len - 1) == ']';

        final String inner;
        if (hasLeadingBracket && hasTrailingBracket) {
            inner = trimmed.substring(1, len - 1);
        } else if (hasLeadingBracket || hasTrailingBracket) {
            throw new NumberFormatException("Mismatched brackets in real vector value.");
        } else {
            inner = trimmed;
        }

        return inner;
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
    Number getElement(final int index) {

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
            hash += getElement(row).hashCode();
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
                final Number otherElement = other.getElement(row);
                equal = getElement(row).equals(otherElement);
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
            final Number element = getElement(row);
            xml.add(element);
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

        final int numElements = getNumElements();
        final HtmlBuilder xml = new HtmlBuilder(30 * numElements);

        appendString(xml);

        return xml.toString();
    }
}
