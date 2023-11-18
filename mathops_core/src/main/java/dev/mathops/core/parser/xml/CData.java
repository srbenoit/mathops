package dev.mathops.core.parser.xml;

import dev.mathops.core.parser.CharSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * A CDATA node, used to represent any characters that are not part of tags. Whitespace is retained in these nodes
 * exactly as supplied in the source XML.
 */
public final class CData extends CharSpan implements IContentNode {

    /** The content. */
    public final String content;

    /** Errors associated with the element. */
    private List<XmlContentError> errors;

    /**
     * Constructs a new {@code CData} with content taken directly from the source XML.
     *
     * @param xml           the source XML from which this node was parsed
     * @param theStart      the start position of the block
     * @param theEnd        the end position of the block
     * @param theLineNumber the line number of the start of the span
     * @param theColumn     the column of the start of the span
     */
    CData(final XmlContent xml, final int theStart, final int theEnd, final int theLineNumber, final int theColumn) {

        super(theStart, theEnd, theLineNumber, theColumn);

        this.content = xml.substring(theStart, theEnd);
    }

    /**
     * Constructs a new {@code CData}.
     *
     * @param xml           the source XML from which this node was parsed
     * @param theStart      the start position of the block
     * @param theEnd        the end position of the block
     * @param theLineNumber the line number of the start of the span
     * @param theColumn     the column of the start of the span
     * @param theContent    the content, if already parsed
     */
    CData(final XmlContent xml, final int theStart, final int theEnd, final int theLineNumber,
          final int theColumn, final String theContent) {

        super(theStart, theEnd, theLineNumber, theColumn);

        this.content = theContent;
    }

    /**
     * Gets the content.
     *
     * @return the content
     */
    @Override
    public String getContent() {

        return this.content;
    }

    /**
     * Generates the print representation of the node.
     *
     * @param indent the number of spaces to indent the element
     * @return the print representation
     */
    @Override
    public String print(final int indent) {

        return this.content;
    }

    /**
     * Logs an error message to the element.
     *
     * @param msg the message
     */
    @Override
    public void logError(final String msg) {

        if (this.errors == null) {
            this.errors = new ArrayList<>(2);
        }
        this.errors.add(new XmlContentError(this, msg));
    }

    /**
     * Gets the list of errors.
     *
     * @return the list of errors; {@code null} if no errors have been logged
     */
    @Override
    public List<XmlContentError> getErrors() {

        return this.errors;
    }

    /**
     * Recursively accumulates all errors in this node and its descendants into a list. Errors are accumulated by
     * pre-order depth-first traversal.
     *
     * @param allErrors the list into which to accumulate errors
     */
    @Override
    public void accumulateErrors(final List<? super XmlContentError> allErrors) {

        if (this.errors != null) {
            allErrors.addAll(this.errors);
        }
    }
}
