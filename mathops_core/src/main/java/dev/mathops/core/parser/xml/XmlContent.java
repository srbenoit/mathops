package dev.mathops.core.parser.xml;

import dev.mathops.core.parser.ICharSpan;
import dev.mathops.core.parser.ParsingException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A container for a character array of XML content, the tokenization of the content, and the parsed structure. Updating
 * the content clears the tokenization and parsed structure and triggers parsing of the new content, which may generate
 * errors if the content is invalid.
 *
 * <p>
 * This class supports read and write locks. Any process that accesses the XML content should first acquire a read lock,
 * releasing it once all XML content has been accessed. Any process that will update the XML data or parsed structure
 * must first obtain a write lock, releasing it after changes have been made. The write lock can be held by only one
 * thread and only when no read locks are held, but when the write lock is not held, multiple threads may obtain read
 * locks.
 *
 * <p>
 * If it can be guaranteed that only one thread will access the {@code XmlContent}, locks are not necessary.
 */
public final class XmlContent extends LockedContent {

    /** The recognized tokens in the source XML. */
    private List<IXmlToken> tokens;

    /** The parsed node structure. */
    private List<INode> nodes;

    /** Errors associated with the element. */
    private List<XmlContentError> errors;

    /**
     * Constructs a new, empty, {@code XmlContent}.
     */
    public XmlContent() {

        super();

        this.tokens = null;
        this.nodes = null;
    }

    /**
     * Constructs a new {@code XmlContent} with some initial XML.
     *
     * @param content         the new XML content, as a character array
     * @param elementsOnly    {@code true} to omit any character and whitespace found between elements from the
     *                        constructed parsed structure (simplifies parsing of XML content where only elements are
     *                        significant)
     * @param includeComments true to include comments
     * @throws ParsingException if the XML could not be parsed (the XML content in this object is set regardless, but
     *                          parsed structure will be empty on a parsing error)
     */
    public XmlContent(final String content, final boolean elementsOnly,
                      final boolean includeComments) throws ParsingException {

        super(content.toCharArray());

        this.tokens = null;
        this.nodes = null;
        XmlParser.parse(this, elementsOnly, includeComments);
    }

    /**
     * Constructs a new {@code XmlContent} with some initial XML.
     *
     * @param theContent         the new XML content, as a character array
     * @param elementsOnly    {@code true} to omit any character and whitespace found between elements from the
     *                        constructed parsed structure (simplifies parsing of XML content where only elements are
     *                        significant)
     * @param includeComments true to include comments
     * @throws ParsingException if the XML could not be parsed (the XML content in this object is set regardless, but
     *                          parsed structure will be empty on a parsing error)
     */
    public XmlContent(final char[] theContent, final boolean elementsOnly,
                      final boolean includeComments) throws ParsingException {

        super(theContent);

        this.tokens = null;
        this.nodes = null;
        XmlParser.parse(this, elementsOnly, includeComments);
    }

    /**
     * Sets the list of tokens from the XML content.
     *
     * @param theTokens the list of tokens
     */
    public void setTokens(final List<IXmlToken> theTokens) {

        this.tokens = theTokens;
    }

    /**
     * Gets the list of tokens from the XML content.
     *
     * @return the (unmodifiable) list of tokens
     */
    public List<IXmlToken> getTokens() {

        return (this.tokens == null) ? null : Collections.unmodifiableList(this.tokens);
    }

    /**
     * Sets the list of nodes parsed from the XML content.
     *
     * @param theNodes the list of parsed nodes
     */
    public void setNodes(final List<INode> theNodes) {

        this.nodes = theNodes;
    }

    /**
     * Gets the list of nodes parsed from the XML content.
     *
     * @return the (unmodifiable) list of parsed nodes
     */
    public List<INode> getNodes() {

        return (this.nodes == null) ? null : Collections.unmodifiableList(this.nodes);
    }

    /**
     * Gets the first top-level element node.
     *
     * @return the top-level element
     */
    public IElement getToplevel() {

        IElement result = null;

        for (final INode next : this.nodes) {
            if (next instanceof IElement) {
                result = (IElement) next;
                break;
            }
        }

        return result;
    }

    /**
     * Logs an error message to the element.
     *
     * @param source the source span
     * @param msg    the message
     */
    public void logError(final ICharSpan source, final String msg) {

        if (this.errors == null) {
            this.errors = new ArrayList<>(2);
        }
        this.errors.add(new XmlContentError(source, msg));
    }

    /**
     * Gets the list of errors.
     *
     * @return the list of errors; {@code null} if no errors have been logged
     */
    public List<XmlContentError> getErrors() {

        return this.errors;
    }

    /**
     * Gets the list of all errors, including errors from the element tree.
     *
     * @return the list of errors; may be empty but never {@code null}
     */
    public List<XmlContentError> getAllErrors() {

        final List<XmlContentError> allErrors = new ArrayList<>(10);

        if (this.errors != null) {
            allErrors.addAll(this.errors);
        }

        for (final INode node : this.nodes) {
            node.accumulateErrors(allErrors);
        }

        return allErrors;
    }
}
