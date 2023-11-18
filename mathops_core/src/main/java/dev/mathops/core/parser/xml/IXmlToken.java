package dev.mathops.core.parser.xml;

import dev.mathops.core.parser.ICharSpan;
import dev.mathops.core.parser.ParsingException;

/**
 * The interface implemented by XML tokens.
 */
public interface IXmlToken extends ICharSpan {

    /**
     * Gets the {@code XmlContent} on which this token is based.
     *
     * @return the {@code XmlContent}
     */
    XmlContent getContent();

    /**
     * Validates the contents of the token, which may store internal structure that can be used when the token in
     * assembled into a node.
     *
     * @throws ParsingException if the contents are invalid
     */
    void validate() throws ParsingException;

    /**
     * Tests whether the token generates one or more nodes (or part of a node) in the XML node structure.
     *
     * @return {@code true} if the token contributes to the XML node structure; {@code false} if not
     */
    boolean hasNodeStructure();

    /**
     * Tests whether the token represents an XML element.
     *
     * @return {@code false} if the token represents an element; {@code true} if not
     */
    boolean isNonElement();
}
