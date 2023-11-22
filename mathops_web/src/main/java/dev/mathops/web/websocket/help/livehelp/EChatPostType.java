package dev.mathops.web.websocket.help.livehelp;

/**
 * Types of chat post.
 */
enum EChatPostType {

    /** Text. */
    TEXT("T"),

    /** Math. */
    MATH("M"),

    /** HTML. */
    HTML("H"),

    /** Image. */
    IMAGE("I");

    /** The type code. */
    private final String code;

    /**
     * Constructs a new {@code EChatPostType}.
     *
     * @param theCode the type code
     */
    EChatPostType(final String theCode) {

        this.code = theCode;
    }

    /**
     * Retrieves the {@code EChatPostType} that corresponds to a given code.
     *
     * @param code the code
     * @return the corresponding {@code EChatPostType}; null if none corresponds
     */
    static EChatPostType forCode(final String code) {

        EChatPostType result = null;

        for (final EChatPostType test : values()) {
            if (test.code.equals(code)) {
                result = test;
                break;
            }
        }

        return result;

    }
}
