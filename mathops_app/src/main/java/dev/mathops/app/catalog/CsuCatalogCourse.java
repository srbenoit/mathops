package dev.mathops.app.catalog;

/**
 * A factory that can create prerequisite trees or parse them from the text representation stored in the university
 * catalog.
 *
 * <p>
 * Last reviewed October 22, 2023.
 */
enum CsuCatalogCourse {

    /** A course. */
    AEAD8310("AEAD", "8310", 0, 0),

    /** A course. */
    AEAD8312("AEAD", "8312", 0, 0),

    /** A course. */
    AEAD8313("AEAD", "8313", 0, 0),

    /** A course. */
    AEAD8355("AEAD", "8355", 0, 0),

    /** A course. */
    AEAD8510("AEAD", "8510", 0, 0),

    /** A course. */
    AEAD8512("AEAD", "8512", 0, 0),

    /** A course. */
    AEAD8513("AEAD", "8513", 0, 0),

    /** A course. */
    AEAD8555("AEAD", "8555", 0, 0),

    /** A course. */
    AEBA8110("AEBA", "8110", 0, 0),

    /** A course. */
    AEBA8112("AEBA", "8112", 0, 0),

    /** A course. */
    AEBA8113("AEBA", "8113", 0, 0),

    /** A course. */
    AEBA8155("AEBA", "8155", 0, 0),

    /** A course. */
    AEEPxxxx("AEEP", "xxxx", 0, 0);

    /** The prefix. */
    private final String prefix;

    /** The number. */
    private final String number;

    /** The minimum number of credits. */
    private final int minCredits;

    /** The maximum number of credits. */
    private final int maxCredits;

    /**
     * Constructs a new {@code CsuCatalogCourse}.
     *
     * @param thePrefix     the prefix
     * @param theNumber     the number
     * @param theMinCredits the minimum number of credits
     * @param theMaxCredits the maximum number of credits
     */
    CsuCatalogCourse(final String thePrefix, final String theNumber, final int theMinCredits, final int theMaxCredits) {

        this.prefix = thePrefix;
        this.number = theNumber;
        this.minCredits = theMinCredits;
        this.maxCredits = theMaxCredits;
    }
}
