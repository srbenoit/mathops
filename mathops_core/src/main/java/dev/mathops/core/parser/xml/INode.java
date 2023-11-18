package dev.mathops.core.parser.xml;

import dev.mathops.core.parser.ICharSpan;

import java.util.List;

/**
 * The interface implemented by elements, empty elements, and CData spans.
 */
public interface INode extends ICharSpan {

    /**
     * Generates the print representation of the node.
     *
     * @param indent the number of spaces to indent the element
     * @return the print representation
     */
    String print(int indent);

    /**
     * Logs an error message to the element.
     *
     * @param msg the message
     */
    void logError(String msg);

    /**
     * Gets the list of errors.
     *
     * @return the list of errors; {@code null} if no errors have been logged
     */
    List<XmlContentError> getErrors();

    /**
     * Recursively accumulates all errors in this node and its descendants into a list. Errors are accumulated by
     * pre-order depth-first traversal.
     *
     * @param allErrors the list into which to accumulate errors
     */
    void accumulateErrors(List<? super XmlContentError> allErrors);
}
