package dev.mathops.db.old.logic.mathplan.data;

import java.util.Arrays;
import java.util.List;

/**
 * An academic major in which students could express interest for determining potential math requirements. Programs may
 * be "bare" majors or majors with a concentration.
 *
 * <p>
 * They may be presented to users as a list of distinct majors, with concentrations only shown if a student expresses an
 * interest in the major.
 */
public final class Major implements Comparable<Major> {

    /** The profile question numbers that map to this major ("Canonical" number first). */
    public final int[] questionNumbers;

    /** All program codes from CIM that map to the major ("Canonical" code first). */
    public final List<String> programCodes;

    /** The major name. */
    public final String majorName;

    /** The catalog URL. */
    public final String catalogUrl;

    /**
     * Constructs a new {@code Program} when the program code is of the form "MAJR-XX", where "XX" is the degree type
     * ("BS", "BA", etc.).
     *
     * @param theQuestionNumbers the profile question numbers that map to this major (canonical number first)
     * @param theProgramCodes    the program codes that map to this major (canonical code first)
     * @param theMajorName       the name of the major
     * @param theCatalogUrl      the catalog URL
     */
    public Major(final int[] theQuestionNumbers, final String[] theProgramCodes, final String theMajorName,
                 final String theCatalogUrl) {

        this.questionNumbers = theQuestionNumbers.clone();
        this.programCodes = Arrays.asList(theProgramCodes);
        this.majorName = theMajorName;
        this.catalogUrl = theCatalogUrl;
    }

    /**
     * Generates the hash code of the major. Hash code is based only on program code, since that is what {@code equals}
     * uses.
     */
    @Override
    public int hashCode() {

        return this.programCodes.hashCode();
    }

    /**
     * Tests whether this object is equal to another. To be equal, the other object must be a {@code Major} and must
     * have the same program code.
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final Major major) {
            equal = this.programCodes.equals(major.programCodes);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return this.majorName;
    }

    /**
     * Compares this major to another for order. Order is based first on the major name (compared as strings), then by
     * concentration name (as strings).
     *
     * @param other the other object against which to compare
     * @return 0 if {@code other} is equal to this major; a value less than 0 if this major is lexicographically less
     *         than {@code other}; and a value greater than 0 if this major is lexicographically greater than
     *         {@code other}
     */
    @Override
    public int compareTo(final Major other) {

        int result = this.majorName.compareTo(other.majorName);

        if (result == 0) {
            final String first = this.programCodes.getFirst();
            final String otherFirst = other.programCodes.getFirst();
            result = first.compareTo(otherFirst);
        }

        return result;
    }
}
