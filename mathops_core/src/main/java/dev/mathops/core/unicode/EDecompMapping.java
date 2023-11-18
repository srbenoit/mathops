package dev.mathops.core.unicode;

/**
 * The supported tags indicating decomposition mapping.
 */
public enum EDecompMapping {

    /** A font variant (e.g. a blackletter form). */
    FONT("<font>",
            "A font variant (e.g. a blackletter form)."),

    /** A no-break version of a space or hyphen. */
    NOBREAK("<noBreak>",
            "A no-break version of a space or hyphen."),

    /** An initial presentation form (Arabic). */
    INITIAL("<initial>",
            "An initial presentation form (Arabic)."),

    /** A medial presentation form (Arabic). */
    MEDIAL("<medial>",
            "A medial presentation form (Arabic)."),

    /** A final presentation form (Arabic). */
    FINAL("<final>",
            "A final presentation form (Arabic)."),

    /** An isolated presentation form (Arabic). */
    ISOLATED("<isolated>",
            "An isolated presentation form (Arabic)."),

    /** An encircled form. */
    CIRCLE("<circle>",
            "An encircled form."),

    /** A superscript form. */
    SUPER("<super>",
            "A superscript form."),

    /** A subscript form. */
    SUB("<sub>",
            "A subscript form."),

    /** A vertical layout presentation form. */
    VERTICAL("<vertical>",
            "A vertical layout presentation form."),

    /** A wide (or zenkaku) compatibility character. */
    WIDE("<wide>",
            "A wide (or zenkaku) compatibility character."),

    /** A narrow (or hankaku) compatibility character. */
    NARROW("<narrow>",
            "A narrow (or hankaku) compatibility character."),

    /** A small variant form (CNS compatibility). */
    SMALL("<small>",
            "A small variant form (CNS compatibility)."),

    /** A CJK squared font variant. */
    SQUARE("<square>",
            "A CJK squared font variant."),

    /** A vulgar fraction form. */
    FRACTION("<fraction>",
            "A vulgar fraction form."),

    /** Otherwise unspecified compatibility character. */
    COMPAT("<compat>",
            "Otherwise unspecified compatibility character.");

    /** The tag. */
    public final String tag;

    /** The tag's description. */
    public final String description;

    /**
     * Constructs a new {@code EDecompMapping}.
     *
     * @param theTag         the tag
     * @param theDescription the description
     */
    EDecompMapping(final String theTag, final String theDescription) {

        this.tag = theTag;
        this.description = theDescription;
    }

    /**
     * Gets the {@code EDecompMapping} corresponding to a particular tag.
     *
     * @param theTag the tag
     * @return the corresponding {@code EDecompMapping}
     */
    static EDecompMapping forTag(final String theTag) {

        EDecompMapping result = null;

        for (final EDecompMapping test : values()) {
            if (test.tag.equals(theTag)) {
                result = test;
                break;
            }
        }

        return result;
    }
}
