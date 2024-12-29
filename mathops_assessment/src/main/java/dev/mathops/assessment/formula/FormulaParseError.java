package dev.mathops.assessment.formula;

import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;
/**
 * A log record that indicates an error in parsing. It includes the range in the source data where the parse error
 * occurred.
 */
final class FormulaParseError implements MessageInt {

    /** The error message. */
    private final String errorMsg;

    /** The start position. */
    private final int start;

    /** The end position. */
    private final int end;

    /**
     * Constructs a new {@code FormulaParseError}.
     *
     * @param msg the message
     * @param pos the error position
     */
    FormulaParseError(final String msg, final int pos) {

        this.errorMsg = msg;
        this.start = pos;
        this.end = pos;
    }

    /**
     * Constructs a new {@code FormulaParseError}.
     *
     * @param msg      the message
     * @param startPos the start position
     * @param endPos   the end position
     */
    FormulaParseError(final String msg, final int startPos, final int endPos) {

        this.errorMsg = msg;
        this.start = startPos;
        this.end = endPos;
    }

    /**
     * Generates the string representation of the message.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(200);

        builder.add(this.errorMsg, "(", Integer.toString(this.start));

        if (this.end != this.start) {
            builder.add(CoreConstants.DASH, Integer.toString(this.end));
        }

        builder.add(")");

        return builder.toString();
    }
}
