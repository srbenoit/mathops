package dev.mathops.db.enums;

/**
 * Term names (2-letter) with associated full names.
 */
public enum EOfferingTermName {

    /** Spring term. */
    EVERY_SPRING("Spring"),

    /** Summer term. */
    EVERY_SUMMER("Summer"),

    /** Fall term. */
    EVERY_FALL("Fall"),

    /** Spring term, even years only. */
    SPRING_EVEN_YEARS("Spring (even years)"),

    /** Summer term, even years only. */
    SUMMER_EVEN_YEARS("Summer (even years)"),

    /** Fall term, even years only. */
    FALL_EVEN_YEARS("Fall (even years)"),

    /** Spring term, odd years only. */
    SPRING_ODD_YEARS("Spring (odd years)"),

    /** Summer term, odd years only. */
    SUMMER_ODD_YEARS("Summer (odd years)"),

    /** Fall term, odd years only. */
    FALL_ODD_YEARS("Fall (odd years)"),

    /** Spring term, every third year. */
    SPRING_THIRD_YEARS("Spring (every third year)"),

    /** Summer term, every third year. */
    SUMMER_THIRD_YEARS("Summer (every third year)"),

    /** Fall term, every third year. */
    FALL_THIRD_YEARS("Fall (every third year)"),

    /** Spring term, as needed. */
    SPRING_AS_NEEDED("Spring (as needed)"),

    /** Summer term, as needed. */
    SUMMER_AS_NEEDED("Summer (as needed)"),

    /** Fall term, as needed. */
    FALL_AS_NEEDED("Fall (as needed)");

    /** The label. */
    public final String label;

    /**
     * Constructs a new {@code EOfferingTermName}.
     *
     * @param theLabel the label
     */
    EOfferingTermName(final String theLabel) {

        this.label = theLabel;
    }
}
