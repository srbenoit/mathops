package dev.mathops.core.parser.xml;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.parser.ParsingException;

import java.util.HashMap;
import java.util.Map;

/**
 * A token that represents an empty element, beginning with < and ending with />, with no < or > characters between.
 */
abstract class AbstractXmlTagToken extends AbstractXmlToken {

    /** Estimated number of attributes for map allocation. */
    private static final int EST_NUM_ATTRIBUTES = 10;

    /** The tag name. */
    private String name;

    /** The set of attributes. */
    private final Map<String, Attribute> attributes;

    /**
     * Constructs a new {@code AbstractXmlTagToken}.
     *
     * @param theContent       the content this token belongs to
     * @param theStart         the index of the '<' character
     * @param theEnd           the index after '>' character
     * @param theLineNumber the line number of the start of the span
     * @param theColumn     the column of the start of the span
     */
    AbstractXmlTagToken(final XmlContent theContent, final int theStart, final int theEnd, final int theLineNumber,
                        final int theColumn) {

        super(theContent, theStart, theEnd, theLineNumber, theColumn);

        this.attributes = new HashMap<>(EST_NUM_ATTRIBUTES);
    }

    /**
     * Gets the tag name.
     *
     * @return the name
     */
    public final String getName() {

        return this.name;
    }

    /**
     * Gets the map of attributes.
     *
     * @return the attributes
     */
    public final Map<String, Attribute> getAttributes() {

        return this.attributes;
    }

    /**
     * Tests whether the token generates one or more nodes (or part of a node) in the XML node structure.
     *
     * @return {@code true} if the token contributes to the XML node structure; {@code false} if not
     */
    @Override
    public final boolean hasNodeStructure() {

        return true;
    }

    /**
     * Tests whether the token represents an XML element.
     *
     * @return {@code false} if the token represents an element; {@code true} if not
     */
    @Override
    public final boolean isNonElement() {

        return false;
    }

    /**
     * Generates the string content of the token.
     *
     * @param prefix a prefix to print before the content
     * @return the string representation of the content
     */
    final String genString(final String prefix) {

        final HtmlBuilder htm = printContent(prefix);

        printValue(this.name, "Name", htm);

        if (!this.attributes.isEmpty()) {

            htm.add(" [Attributes:");

            for (final Map.Entry<String, Attribute> entry : this.attributes.entrySet()) {
                htm.add(CoreConstants.SPC, entry.getKey(), "=\"", entry.getValue(), CoreConstants.QUOTE);
            }

            htm.add(']');
        }

        return htm.toString();
    }

    /**
     * Validates the name, then extracts any attributes.
     *
     * @param endChars the number of end characters in the tag (1 or 2 for '>' or '/>', respectively)
     * @throws ParsingException if the name is invalid
     */
    final void extractNameAndAttributes(final int endChars) throws ParsingException {

        final XmlContent content = getContent();
        int pos = getStart() + 1;
        boolean valid = XmlChars.isNameStartChar(content.get(pos));
        ++pos;

        // Validate and store the name
        while (valid && pos < (getEnd() - endChars)) {

            if (XmlChars.isWhitespace(content.get(pos))) {
                break;
            }

            valid = XmlChars.isNameChar(content.get(pos));
            ++pos;
        }

        if (!valid) {
            throw new ParsingException(getStart(), pos, Res.fmt(Res.BAD_TAG,
                    content.substring(getStart(), getEnd()), content.substring(getStart() + 1, pos)));
        }

        this.name = content.substring(getStart() + 1, pos);

        extractAttributes(pos, getEnd() - endChars, this.attributes);
    }
}
