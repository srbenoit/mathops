package dev.mathops.core.parser.xml;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.parser.ICharSpan;

/**
 * An error message along with the span object with which the error is associated.
 */
public class XmlContentError {

    /** The span. */
    public final ICharSpan span;

    /** The error message. */
    public final String msg;

    /**
     * Constructs a new {@code XmlContentError}.
     *
     * @param theSpan the span
     * @param theMsg  the error message
     */
    public XmlContentError(final ICharSpan theSpan, final String theMsg) {

        this.span = theSpan;
        this.msg = theMsg;
    }

    /**
     * Generates a string representation of the error.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat(this.msg, " at line ",
                Integer.toString(this.span.getLineNumber()), ", column ",
                Integer.toString(this.span.getColumn()), ", [",
                Integer.toString(this.span.getStart()), ":",
                Integer.toString(this.span.getEnd()), "]");
    }
}
