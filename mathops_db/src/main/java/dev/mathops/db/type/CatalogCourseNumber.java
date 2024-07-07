package dev.mathops.db.type;

import dev.mathops.commons.CoreConstants;

import java.util.Objects;

/**
 * A combination of a prefix, like "MATH" and a course number, like "126".
 */
public final class CatalogCourseNumber implements Comparable<CatalogCourseNumber> {

    /** The course prefix, like "MATH". */
    private final String prefix;

    /** The course number, like "126" (String since it could include letter suffix, like "569A"). */
    private final String number;

    /**
     * Constructs a new {@code CatalogCourseNumber}.
     *
     * @param thePrefix the course prefix, like "MATH"
     * @param theNumber the course number, like "126"
     * @throws IllegalArgumentException if either prefix or number is null or blank
     */
    public CatalogCourseNumber(final String thePrefix, final String theNumber) {

        if (thePrefix == null || thePrefix.isBlank()) {
            throw new IllegalArgumentException("Prefix may not be null or blank");
        }
        if (theNumber == null || theNumber.isBlank()) {
            throw new IllegalArgumentException("Course number may not be null or blank");
        }

        this.prefix = thePrefix.trim();
        this.number = theNumber.trim();
    }

    /**
     * Attempts to parse a catalog course number from its string representation.
     *
     * @param str the string representation
     * @return the parsed {@code CatalogCourseNumber}
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    public static CatalogCourseNumber parse(final String str) {

        final CatalogCourseNumber result;

        final int spc = str.indexOf(' ');
        if (spc == -1) {
            final int spc2 = str.indexOf('\u00A0');
            if (spc2 == -1) {
                final String fixed = str.replace("&#160;", CoreConstants.SPC);

                final int fixedSpc = fixed.indexOf(' ');
                if (fixedSpc == -1) {
                    throw new IllegalArgumentException("Unable to parse course number: " + str);
                } else {
                    final String prefix = fixed.substring(0, fixedSpc);
                    final String number = fixed.substring(fixedSpc + 1);

                    result = new CatalogCourseNumber(prefix, number);
                }
            } else {
                final String prefix = str.substring(0, spc2);
                final String number = str.substring(spc2 + 1);

                result = new CatalogCourseNumber(prefix, number);
            }
        } else {
            final String prefix = str.substring(0, spc);
            final String number = str.substring(spc + 1);

            result = new CatalogCourseNumber(prefix, number);
        }

        return result;
    }

    /**
     * Gets the prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {

        return this.prefix;
    }

    /**
     * Gets the number.
     *
     * @return the number
     */
    public String getNumber() {

        return this.number;
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        return this.prefix + CoreConstants.SPC + this.number;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final CatalogCourseNumber o) {

        int result = this.prefix.compareTo(o.prefix);

        if (result == 0) {
            result = this.number.compareTo(o.number);
        }

        return result;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.prefix) + Objects.hashCode(this.number);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final CatalogCourseNumber rec) {
            equal = Objects.equals(this.prefix, rec.prefix)
                    && Objects.equals(this.number, rec.number);
        } else {
            equal = false;
        }

        return equal;
    }
}
