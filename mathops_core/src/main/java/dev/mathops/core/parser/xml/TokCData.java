package dev.mathops.core.parser.xml;

/**
 * A token that represents a CDATA block declaration, beginning with <![CDATA[ and ending with ]]>, which may contain
 * any characters except the sequence ']]>'.
 */
public final class TokCData extends AbstractXmlToken {

    /**
     * Constructs a new {@code TokCData}.
     *
     * @param theContent the content this token belongs to
     * @param theStart   the index of the '<' character
     * @param theEnd     the index after '>' character
     * @param theLineNumber the line number of the start of the span
     * @param theColumn     the column of the start of the span
     */
    TokCData(final XmlContent theContent, final int theStart, final int theEnd,
             final int theLineNumber, final int theColumn) {

        super(theContent, theStart, theEnd, theLineNumber, theColumn);
    }

    /**
     * Generates the string representation of the TokCData token.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return printContent("CData: ").toString();
    }

    /**
     * Validates the contents of the TokCData token, which may store internal structure that can be used when the token
     * in assembled into a node.
     */
    @Override
    public void validate() {

        // No validation needed - CData can hold any content
    }

    /**
     * Tests whether the TokCData token generates one or more nodes (or part of a node) in the XML node structure.
     *
     * @return {@code true} if the token contributes to the XML node structure; {@code false} if not
     */
    @Override
    public boolean hasNodeStructure() {

        return true;
    }

    /**
     * Tests whether the TokCData token represents an XML element.
     *
     * @return {@code false} if the token represents an element; {@code true} if not
     */
    @Override
    public boolean isNonElement() {

        return true;
    }
}
