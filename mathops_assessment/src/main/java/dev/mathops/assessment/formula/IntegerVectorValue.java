package dev.mathops.assessment.formula;

import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Collection;

/**
 * An immutable vector of long integer values.
 */
public final class IntegerVectorValue {

    /** The vector elements. */
    private final long[] elements;

    /**
     * Constructs a new {@code IntegerVectorValue} with all elements set to zero.
     *
     * @param theElements the elements
     * @throws IllegalArgumentException if {@code theElements} is null or zero length
     */
    public IntegerVectorValue(final long... theElements) {

        if (theElements == null || theElements.length == 0) {
            throw new IllegalArgumentException("A vector must have at least one element");
        }

        this.elements = theElements.clone();
    }

    /**
     * Constructs a new {@code IntegerVectorValue} with all elements set to zero.
     *
     * @param theElements the elements
     * @throws IllegalArgumentException if {@code theElements} is null or zero length
     */
    public IntegerVectorValue(final Collection<Long> theElements) {

        if (theElements == null || theElements.isEmpty()) {
            throw new IllegalArgumentException("A vector must have at least one element");
        }

        this.elements = new long[theElements.size()];
        int index = 0;
        for (final Long value : theElements) {
            this.elements[index] = value.longValue();
            ++index;
        }
    }

    /**
     * Attempts to parse a {@code IntegerVectorValue} from a string.
     *
     * @param toParse the string to parse
     * @return the parsed value
     * @throws NumberFormatException if the string cannot be parsed
     */
    public static IntegerVectorValue parse(final String toParse) throws NumberFormatException {

        if (toParse == null) {
            throw new NumberFormatException("Integer vector value string may not be null");
        }

        final String inner = extractVectorContent(toParse);

        final String[] split = inner.split(CoreConstants.COMMA);
        final int count = split.length;

        final long[] elements = new long[split.length];
        for (int i = 0; i < count; ++i) {
            final String trimmedEntry = split[i].trim();
            elements[i] = Long.parseLong(trimmedEntry);
        }

        return new IntegerVectorValue(elements);
    }

    /**
     * Given an integer vector string, which may or may not have surrounding square brackets, extract the interior
     * comma-separated list of integer values.
     *
     * @param toParse the string to parse
     * @throws NumberFormatException if the string cannot be parsed
     */
    private static String extractVectorContent(final String toParse) {

        final String trimmed = toParse.trim();
        if (trimmed.isEmpty()) {
            throw new NumberFormatException("Integer vector value may not be empty.");
        }

        final int len = trimmed.length();

        final boolean hasLeadingBracket = (int) trimmed.charAt(0) == '[';
        final boolean hasTrailingBracket = (int) trimmed.charAt(len - 1) == ']';

        final String inner;
        if (hasLeadingBracket && hasTrailingBracket) {
            inner = trimmed.substring(1, len - 1);
        } else if (hasLeadingBracket || hasTrailingBracket) {
            throw new NumberFormatException("Mismatched brackets in integer vector value.");
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
    long getElement(final int index) {

        return this.elements[index];
    }

    /**
     * Returns a hash code value for the {@code IntegerVectorValue}.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {

        final int count = getNumElements();
        int hash = count;

        for (int row = 0; row < count; ++row) {
            final long element = getElement(row);
            hash += Double.hashCode((double) element);
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

        if (obj instanceof final IntegerVectorValue other) {
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
