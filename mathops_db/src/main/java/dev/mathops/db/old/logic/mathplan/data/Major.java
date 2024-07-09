package dev.mathops.db.old.logic.mathplan.data;

/**
 * An academic major in which students could express interest for determining potential math requirements. Programs may
 * be "bare" majors or majors with a concentration.
 *
 * <p>
 * They may be presented to users as a list of distinct majors, with concentrations only shown if a student expresses an
 * interest in the major.
 */
public final class Major implements Comparable<Major> {

    /** The profile question number. */
    public final Integer questionNumber;

    /** The complete program code from CIM. */
    public final String programCode;

    /** True if concentration should be auto-checked when user checks major. */
    public Boolean autoCheck = null;

    /**
     * True if major or concentration can be selected by itself (FALSE for majors in which the student MUST choose a
     * concentration).
     */
    public Boolean checkable = null;

    /** The major name. */
    public final String majorName;

    /** The concentration name. */
    public String concentrationName = null;

    /** The catalog URL. */
    public final String catalogUrl;

    /** Indicates the major is bogus and cannot be selected in the Math Plan. */
    public final boolean bogus;

    /**
     * Constructs a new {@code Program} when the program code is of the form "MAJR-XX", where "XX" is the degree type
     * ("BS", "BA", etc.).
     *
     * @param theQuestionNumber the profile question number
     * @param theProgramCode    the program code
     * @param theCheckable      true if the major or concentration can be selected by itself (FALSE for majors in which
     *                          the student MUST choose a concentration)
     * @param theMajorName      the name of the major
     * @param theCatalogUrl     the catalog URL
     * @throws IllegalArgumentException if the program code is not in a valid format
     */
    public Major(final int theQuestionNumber, final String theProgramCode, final Boolean theCheckable,
                 final String theMajorName, final String theCatalogUrl)
            throws IllegalArgumentException {

        this(theQuestionNumber, theProgramCode, theCheckable, theMajorName, theCatalogUrl, false);
    }

    /**
     * Constructs a new {@code Program} when the program code is of the form "MAJR-XX", where "XX" is the degree type
     * ("BS", "BA", etc.).
     *
     * @param theQuestionNumber the profile question number
     * @param theProgramCode    the program code
     * @param theCheckable      true if the major or concentration can be selected by itself (FALSE for majors in which
     *                          the student MUST choose a concentration)
     * @param theMajorName      the name of the major
     * @param theCatalogUrl     the catalog URL
     * @param isBogus           true if the major is bogus and cannot be selected in the Math Plan
     * @throws IllegalArgumentException if the program code is not in a valid format
     */
    public Major(final int theQuestionNumber, final String theProgramCode, final Boolean theCheckable,
                 final String theMajorName, final String theCatalogUrl, final boolean isBogus)
            throws IllegalArgumentException {

        this.questionNumber = Integer.valueOf(theQuestionNumber);
        this.programCode = theProgramCode;
        this.checkable = theCheckable;
        this.majorName = theMajorName;
        this.catalogUrl = theCatalogUrl;
        this.bogus = isBogus;
    }

    /**
     * Constructs a new {@code Program} when the program code is of the form "MAJR-CONC-XX", where "XX" is the degree
     * type ("BS", "BA", etc.).
     *
     * @param theQuestionNumber    the profile question number
     * @param theProgramCode       the program code
     * @param theAutoCheck         true if the concentration should be automatically checked when the major is checked
     * @param theMajorName         the name of the major
     * @param theConcentrationName the concentration name
     * @param theCatalogUrl        the catalog URL
     * @throws IllegalArgumentException if the program code is not in a valid format
     */
    public Major(final int theQuestionNumber, final String theProgramCode,
                 final Boolean theAutoCheck, final String theMajorName, final String theConcentrationName,
                 final String theCatalogUrl) throws IllegalArgumentException {

        this(theQuestionNumber, theProgramCode, theAutoCheck, theMajorName, theConcentrationName, theCatalogUrl, false);
    }

    /**
     * Constructs a new {@code Program} when the program code is of the form "MAJR-CONC-XX", where "XX" is the degree
     * type ("BS", "BA", etc.).
     *
     * @param theQuestionNumber    the profile question number
     * @param theProgramCode       the program code
     * @param theAutoCheck         true if the concentration should be automatically checked when the major is checked
     * @param theMajorName         the name of the major
     * @param theConcentrationName the concentration name
     * @param theCatalogUrl        the catalog URL
     * @param isBogus              true if the major is bogus and cannot be selected in the Math Plan
     * @throws IllegalArgumentException if the program code is not in a valid format
     */
    public Major(final int theQuestionNumber, final String theProgramCode,
                 final Boolean theAutoCheck, final String theMajorName, final String theConcentrationName,
                 final String theCatalogUrl, final boolean isBogus) throws IllegalArgumentException {

        this.questionNumber = Integer.valueOf(theQuestionNumber);
        this.programCode = theProgramCode;
        this.autoCheck = theAutoCheck;
        this.majorName = theMajorName;
        this.concentrationName = theConcentrationName;
        this.catalogUrl = theCatalogUrl;
        this.bogus = isBogus;
    }

    /**
     * Generates the hash code of the major. Hash code is based only on program code, since that is what {@code equals}
     * uses.
     */
    @Override
    public int hashCode() {

        return this.programCode.hashCode();
    }

    /**
     * Tests whether this object is equal to another. To be equal, the other object must be a {@code Major} and must
     * have the same program code.
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final Major major) {
            equal = this.programCode.equals(major.programCode);
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

        return (this.concentrationName == null) ? this.majorName
                : (this.majorName + ": " + this.concentrationName);
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
            if (this.concentrationName == null) {
                result = other.concentrationName == null ? 0 : -1;
            } else if (other.concentrationName == null) {
                result = 1;
            } else {
                result = this.concentrationName.compareTo(other.concentrationName);
            }

            if (result == 0) {
                result = this.programCode.compareTo(other.programCode);
            }
        }

        return result;
    }
}
