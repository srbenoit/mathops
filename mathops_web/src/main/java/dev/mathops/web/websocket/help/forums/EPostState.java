package dev.mathops.web.websocket.help.forums;

/**
 * Possible states of a forum post.
 */
public enum EPostState {

    /** Deleted. */
    DELETED('D'),

    /** Unread. */
    UNREAD('U'),

    /** READ. */
    READ('R'),

    /** STARRED. */
    STARRED('S');

    /** The code used to store the state in a database. */
    private final char code;

    /**
     * Constructs a new {@code EPostState}.
     *
     * @param theCode the code used to store the state in a database
     */
    EPostState(final char theCode) {

        this.code = theCode;
    }

    /**
     * Retrieves the {@code EPostState} corresponding to a specified code.
     *
     * @param theCode the code
     * @return the corresponding {@code EPostState}; null if none matches the code
     */
    public static EPostState forCode(final char theCode) {

        EPostState result = null;

        for (final EPostState test : values()) {
            if (test.code == theCode) {
                result = test;
                break;
            }
        }

        return result;
    }
}
