package dev.mathops.db.enums;

/**
 * Term names (2-letter) with associated full names.
 */
public enum ETermName {

    /** Spring term. */
    SPRING("SP", "Spring"),

    /** Summer term. */
    SUMMER("SM", "Summer"),

    /** Fall term. */
    FALL("FA", "Fall");

    /** The 2-letter term name. */
    public final String termName;

    /** The full term name. */
    public final String fullName;

    /**
     * Constructs a new {@code ETermName}.
     *
     * @param theTermName the 2-letter term name
     * @param theFullName the full term name
     */
    ETermName(final String theTermName, final String theFullName) {

        this.termName = theTermName;
        this.fullName = theFullName;
    }

    /**
     * Gets the {@code ETermName} that has a specified term name.
     *
     * @param theName the 2-letter term name for which to search
     * @return the matching {@code ETermName}; {@code null} if none match
     */
    public static ETermName forName(final String theName) {

        ETermName result = null;

        for (final ETermName test : values()) {
            if (test.termName.equals(theName)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Gets the {@code ETermName} that has a specified full name.
     *
     * @param theName the full name for which to search
     * @return the matching {@code ETermName}; {@code null} if none match
     */
    public static ETermName forFullName(final String theName) {

        ETermName result = null;

        for (final ETermName test : values()) {
            if (test.fullName.equals(theName)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Returns the string representation of the object.
     *
     * @return the string representation (the term name)
     */
    @Override
    public String toString() {

        return this.termName;
    }
}
