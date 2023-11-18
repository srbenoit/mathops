package dev.mathops.core.parser.xml;

/**
 * A token that represents a comment, beginning with <!-- and ending with --> .
 */
final class TokComment extends AbstractXmlToken {

    /**
     * Constructs a new {@code TokComment}.
     *
     * @param theContent the content this token belongs to
     * @param theStart   the index of the '<' character
     * @param theEnd     the index after '>' character
     * @param theLineNumber the line number of the start of the span
     * @param theColumn     the column of the start of the span
     */
    TokComment(final XmlContent theContent, final int theStart, final int theEnd,
               final int theLineNumber, final int theColumn) {

        super(theContent, theStart, theEnd, theLineNumber, theColumn);
    }

    /**
     * Generates the string representation of the TokComment token.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return printContent("Comment: ").toString();
    }

    /**
     * Validates the contents of the TokComment token, which may store internal structure that can be used when the
     * token in assembled into a node.
     */
    @Override
    public void validate() {

        // No validation needed - comments can hold any content
    }

    /**
     * Tests whether the TokComment token generates one or more nodes (or part of a node) in the XML node structure.
     *
     * @return {@code true} if the token contributes to the XML node structure; {@code false} if not
     */
    @Override
    public boolean hasNodeStructure() {

        return false;
    }

    /**
     * Tests whether the TokComment token represents an XML element.
     *
     * @return {@code false} if the token represents an element; {@code true} if not
     */
    @Override
    public boolean isNonElement() {

        return false;
    }
}
