package dev.mathops.web.websocket.help.conversation;

/**
 * Possible states of a conversation message.
 */
public enum EMessageState {

    /** Deleted. */
    DELETED("D"),

    /** Unread by student (authored by staff). */
    UNREAD_BY_STUDENT("U"),

    /** Unread by staff (authored by student). */
    UNREAD_BY_STAFF("u"),

    /** Read by student (authored by staff). */
    READ_BY_STUDENT("R"),

    /** Read by staff (authored by student). */
    READ_BY_STAFF("r");

    /** The code used to store the state in a database. */
    final String code;

    /**
     * Constructs a new {@code EMessageState}.
     *
     * @param theCode the code used to store the state in a database
     */
    EMessageState(final String theCode) {

        this.code = theCode;
    }

    /**
     * Retrieves the {@code EMessageState} corresponding to a specified code.
     *
     * @param theCode the code
     * @return the corresponding {@code EMessageState}; null if none matches the code
     */
    static EMessageState forCode(final String theCode) {

        EMessageState result = null;

        for (final EMessageState test : values()) {
            if (test.code.equals(theCode)) {
                result = test;
                break;
            }
        }

        return result;
    }
}
