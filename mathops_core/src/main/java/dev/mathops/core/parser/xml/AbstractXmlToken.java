package dev.mathops.core.parser.xml;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.parser.CharSpan;
import dev.mathops.core.parser.ParsingException;

import java.util.Map;

/**
 * The base class for a token that maps to some range of characters in a source XML character array.
 */
abstract class AbstractXmlToken extends CharSpan implements IXmlToken {

    /** The content this token belongs to. */
    private final XmlContent content;

    /**
     * Constructs a new {@code AbstractXmlToken}.
     *
     * @param theContent    the content this token belongs to
     * @param theStart      the index of the first character of the token in the XML source character array
     * @param theEnd        the index after the last character in the token in the XML source character array
     * @param theLineNumber the line number of the start of the span (the first line in the file is line 1)
     * @param theColumn     the column of the start of the span (the first character in the line is column 1)
     */
    AbstractXmlToken(final XmlContent theContent, final int theStart, final int theEnd,
                     final int theLineNumber, final int theColumn) {

        super(theStart, theEnd, theLineNumber, theColumn);

        this.content = theContent;
    }

    /**
     * Gets the {@code XmlContent} on which this token is based.
     *
     * @return the {@code XmlContent}
     */
    @Override
    public final XmlContent getContent() {

        return this.content;
    }

    /**
     * Returns the position of the first non-whitespace character starting at a specified position in the content.
     *
     * @param theStart the position at which to start searching
     * @return the position of the first non-whitespace character found
     */
    final int skipWhitespace(final int theStart) {

        int pos = theStart;

        while (XmlChars.isWhitespace(this.content.get(pos))) {
            ++pos;
        }

        return pos;
    }

    /**
     * Returns the first index of a particular character in the content XML.
     *
     * @param theChar  the character for which to search
     * @param theStart the position at which to start searching
     * @param theEnd   the position at which to stop searching
     * @return the first position at which the character was found, -1 if not found in the specified search range
     */
    final int indexOf(final char theChar, final int theStart, final int theEnd) {

        int pos = theStart;
        int found = -1;

        while (pos < theEnd) {

            if (this.content.get(pos) == theChar) {
                found = pos;
                break;
            }

            ++pos;
        }

        return found;
    }

    /**
     * Extracts the attributes from the tag and stores them in a map from attribute name to attribute value.
     *
     * @param first      the position at which to start searching
     * @param last       the position after the last position to search
     * @param attributes a map to which to add the attribute name/value pairs
     * @throws ParsingException if the attributes cannot be parsed
     */
    final void extractAttributes(final int first, final int last,
                                 final Map<? super String, ? super Attribute> attributes) throws ParsingException {

        int pos = skipWhitespace(first);

        while (pos < last) {
            pos = extractAttribute(pos, last, attributes);
        }
    }

    /**
     * Extracts a single attribute from the tag.
     *
     * @param nameStart  the file position of the start of the attribute name
     * @param last       the position after the last position to search
     * @param attributes a map to which to add the attribute name/value pairs
     * @return the position after the whitespace following the end of the attribute
     * @throws ParsingException if the attribute cannot be parsed
     */
    private int extractAttribute(final int nameStart, final int last,
                                 final Map<? super String, ? super Attribute> attributes) throws ParsingException {

        final int nameEnd = validateName(nameStart, last);
        int pos = skipWhitespace(nameEnd);

        if (this.content.get(pos) != '=') {
            throw new ParsingException(getStart(), pos,
                    Res.fmt(Res.MISS_EQ, this.content.substring(getStart(), getEnd())));
        }

        pos = skipWhitespace(pos + 1);

        final char quot = this.content.get(pos);
        final int match = indexOf(quot, pos + 1, getEnd());

        if ((quot == '\'' || quot == '\"') && match != -1) {
            final int valueStart = pos + 1;
            final String name = this.content.substring(nameStart, nameEnd);

            final String valueStr = this.content.substring(valueStart, match);
            if (valueStr.indexOf('<') != -1 || valueStr.indexOf('>') != -1) {
                this.content.logError(this, "Suspicious '" + name + "' attribute value: " + valueStr);
            }

            attributes.put(name, new Attribute(name, XmlEscaper.unescape(valueStr), nameStart,
                    nameEnd, valueStart, match, getLineNumber(), getColumn()));
            pos = skipWhitespace(match + 1);
        } else {
            final int start = Math.max(0, getStart() - 20);
            throw new ParsingException(getStart(), pos,
                    Res.fmt(Res.BAD_ATSPEC, this.content.substring(nameStart, nameEnd),
                            this.content.substring(start, getEnd())));
        }

        return pos;
    }

    /**
     * Scans and validates the attribute name. A valid name has a first character that satisfies
     * {@code XmlParser.isNameStartChar}, and all remaining characters must satisfy {@code XmlParser.isNameChar}.
     *
     * @param nameStart the position of the start of the name
     * @param last      the position after the last position to search
     * @return the index after the first '=' or whitespace character after the name
     * @throws ParsingException if the name is invalid
     */
    private int validateName(final int nameStart, final int last) throws ParsingException {

        boolean valid = XmlChars.isNameStartChar(this.content.get(nameStart));
        int pos = nameStart + 1;

        // Validate and store the name
        while (valid && pos < (last - 1)) {
            final char chr = this.content.get(pos);

            if (XmlChars.isWhitespace(chr) || chr == '=') {
                break;
            }

            valid = XmlChars.isNameChar(chr);
            ++pos;
        }

        if (!valid) {
            throw new ParsingException(getStart(), pos,
                    Res.fmt(Res.BAD_ATNAME, this.content.substring(getStart(), getEnd()),
                            this.content.substring(nameStart, pos)));
        }

        return pos;
    }

    /**
     * Creates a new {@code HtmlBuilder} and appends the XMl content associated with the token to that
     * {@code HtmlBuilder} with a prefix.
     *
     * @param prefix the prefix
     * @return the generated {@code HtmlBuilder}
     */
    final HtmlBuilder printContent(final String prefix) {

        final HtmlBuilder xml = new HtmlBuilder(50);

        xml.add(prefix, this.content.substring(getStart(), getEnd()));

        return xml;
    }

    /**
     * Prints a value in the format " [Label=value]" to a {@code HtmlBuilder}. If the value is {@code null}, nothing is
     * appended.
     *
     * @param value the value
     * @param label the label
     * @param xml   the {@code HtmlBuilder} to which to append
     */
    static void printValue(final Object value, final String label,
                           final HtmlBuilder xml) {

        if (value != null) {
            xml.add(" [", label, "=", value, "]");
        }
    }
}
