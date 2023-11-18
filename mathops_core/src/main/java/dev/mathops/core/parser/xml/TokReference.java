package dev.mathops.core.parser.xml;

/**
 * A token that represents a reference, of the form &amp;Name;, &amp;#Decimal;, or &amp;#xHex;.
 */
public final class TokReference extends AbstractXmlToken {

    /**
     * the string this reference represents (using {@code String} rather than character allows for Unicode values in the
     * range above 0xFFFF).
     */
    private String value;

    /**
     * Constructs a new {@code TokReference}.
     *
     * @param theContent the content this token belongs to
     * @param theStart   the index of the '&' character
     * @param theEnd     the index after the ';' character
     * @param theLineNumber the line number of the start of the span
     * @param theColumn     the column of the start of the span
     */
    TokReference(final XmlContent theContent, final int theStart, final int theEnd,
                 final int theLineNumber, final int theColumn) {

        super(theContent, theStart, theEnd, theLineNumber, theColumn);
    }

    /**
     * Generates the string representation of the TokReference token.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "Reference: '" + this.value + "'";
    }

    /**
     * Gets the parsed value of a reference.
     *
     * @return the parsed value
     */
    public String getValue() {

        return this.value;
    }

    /**
     * Validates the contents of the TokReference token, which may store internal structure that can be used when the
     * token in assembled into a node.
     */
    @Override
    public void validate() {

        this.value = XmlEscaper.unescape(getContent().substring(getStart(), getEnd()));
    }

    /**
     * Tests whether the TokReference token generates one or more nodes (or part of a node) in the XML node structure.
     *
     * @return {@code true} if the token contributes to the XML node structure; {@code false} if not
     */
    @Override
    public boolean hasNodeStructure() {

        return true;
    }

    /**
     * Tests whether the TokReference token represents an XML element.
     *
     * @return {@code false} if the token represents an element; {@code true} if not
     */
    @Override
    public boolean isNonElement() {

        return true;
    }
}
