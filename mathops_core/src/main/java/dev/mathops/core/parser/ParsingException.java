package dev.mathops.core.parser;

import dev.mathops.core.CoreConstants;

import java.io.Serial;

/**
 * An exception while parsing a text format.
 */
public final class ParsingException extends Exception {

    /** The left bracket. */
    private static final String LEFT = " [";

    /** The right bracket. */
    private static final String RIGHT = "]";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5058873263271687373L;

    /**
     * Constructs a new {@code ParsingException} with the specified detail message.
     *
     * @param span    the span of XML that caused the exception
     * @param message the detail message
     */
    public ParsingException(final ICharSpan span, final String message) {

        super(message + LEFT + span.getStart() + CoreConstants.COMMA + span.getEnd() + RIGHT);
    }

    /**
     * Constructs a new {@code ParsingException} with the specified detail message.
     *
     * @param theStartOffset the starting offset of the XML that caused the exception
     * @param theEndOffset   the ending offset of the XML that caused the exception
     * @param message        the detail message
     */
    public ParsingException(final int theStartOffset, final int theEndOffset,
                            final String message) {

        super(message + LEFT + theStartOffset + CoreConstants.COMMA + theEndOffset + RIGHT);
    }

    /**
     * Constructs a new {@code ParsingException} with the specified detail message and cause.
     *
     * @param span    the span of XML that caused the exception
     * @param message the detail message
     * @param cause   the cause
     */
    public ParsingException(final ICharSpan span, final String message, final Throwable cause) {

        super(message + LEFT + span.getStart() + CoreConstants.COMMA + span.getEnd() + RIGHT,
                cause);
    }

    /**
     * Constructs a new {@code ParsingException} with the specified detail message and cause.
     *
     * @param theStartOffset the starting offset of the XML that caused the exception
     * @param theEndOffset   the ending offset of the XML that caused the exception
     * @param message        the detail message
     * @param cause          the cause
     */
    public ParsingException(final int theStartOffset, final int theEndOffset, final String message,
                            final Throwable cause) {

        super(message + LEFT + theStartOffset + CoreConstants.COMMA + theEndOffset + RIGHT, cause);
    }

    /**
     * Constructs a new {@code ParsingException} with the specified cause and a detail message of
     * {@code (cause==null ? null : cause.toString())} (which typically contains the class and detail message of
     * {@code cause} ).
     *
     * @param cause the cause
     */
    public ParsingException(final Throwable cause) {

        super(cause);
    }
}
