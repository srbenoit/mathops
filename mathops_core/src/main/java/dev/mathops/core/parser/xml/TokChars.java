package dev.mathops.core.parser.xml;

/**
 * A run of characters not containing '&' or '<' or whitespace.
 */
public final class TokChars extends AbstractXmlToken {

    /**
     * Constructs a new {@code TokChars}.
     *
     * @param theContent the content this token belongs to
     * @param theStart   the index of the first character in XML source
     * @param theEnd     the index after the last character in XML source
     * @param theLineNumber the line number of the start of the span
     * @param theColumn     the column of the start of the span
     */
    TokChars(final XmlContent theContent, final int theStart, final int theEnd,
             final int theLineNumber, final int theColumn) {

        super(theContent, theStart, theEnd, theLineNumber, theColumn);
    }

    /**
     * Generates the string representation of the TokChars token.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return printContent("Chars: ").toString();
    }

    /**
     * Validates the contents of the TokChars token, which may store internal structure that can be used when the token
     * in assembled into a node.
     */
    @Override
    public void validate() {

        // No validation needed - Chars can hold any content
    }

    /**
     * Tests whether the TokChars token generates one or more nodes (or part of a node) in the XML node structure.
     *
     * @return {@code true} if the token contributes to the XML node structure; {@code false} if not
     */
    @Override
    public boolean hasNodeStructure() {

        return true;
    }

    /**
     * Tests whether the TokChars token represents an XML element.
     *
     * @return {@code false} if the token represents an element; {@code true} if not
     */
    @Override
    public boolean isNonElement() {

        return true;
    }
}
