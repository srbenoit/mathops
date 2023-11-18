package dev.mathops.core.parser.xml;

/**
 * A token that represents one or more whitespace characters.
 */
public final class TokWhitespace extends AbstractXmlToken {

    /**
     * Constructs a new {@code TokWhitespace}.
     *
     * @param theContent the content this token belongs to
     * @param theStart   the index of the first whitespace in XML source
     * @param theEnd     the index after the last whitespace in XML source
     * @param theLineNumber the line number of the start of the span
     * @param theColumn     the column of the start of the span
     */
    TokWhitespace(final XmlContent theContent, final int theStart, final int theEnd,
                  final int theLineNumber, final int theColumn) {

        super(theContent, theStart, theEnd, theLineNumber, theColumn);
    }

    /**
     * Generates the string representation of the TokWhitespace token.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "Whitespace";
    }

    /**
     * Validates the contents of the TokWhitespace token, which may store internal structure that can be used when the
     * token in assembled into a node.
     */
    @Override
    public void validate() {

        // No validation needed
    }

    /**
     * Tests whether the TokWhitespace token generates one or more nodes (or part of a node) in the XML node structure.
     *
     * @return {@code true} if the token contributes to the XML node structure; {@code false} if not
     */
    @Override
    public boolean hasNodeStructure() {

        return true;
    }

    /**
     * Tests whether the TokWhitespace token represents an XML element.
     *
     * @return {@code false} if the token represents an element; {@code true} if not
     */
    @Override
    public boolean isNonElement() {

        return true;
    }
}
