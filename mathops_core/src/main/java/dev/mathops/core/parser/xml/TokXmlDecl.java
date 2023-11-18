package dev.mathops.core.parser.xml;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;

/**
 * A token that represents the XML declaration, beginning with <? and ending with ?>.
 */
final class TokXmlDecl extends AbstractXmlToken {

    /** The length of the start of the tag, &lt;?xml. */
    private static final int START_LEN = 5;

    /** The offset of the minor version in the version number. */
    private static final int MINOR_VER_OFFSET = 3;

    /** The string representing 'yes'. */
    private static final String YES_STR = "yes";

    /** The string representing 'no'. */
    private static final String NO_STR = "no";

    /** The version number of the XML declaration. */
    private String versionNum;

    /** The specified encoding. */
    private String encoding;

    /** The specified standalone setting. */
    private Boolean standalone;

    /**
     * Constructs a new {@code TokXmlDecl}.
     *
     * @param theContent the content this token belongs to
     * @param theStart   the index of the '<' character
     * @param theEnd     the index after '>' character
     * @param theLineNumber the line number of the start of the span
     * @param theColumn     the column of the start of the span
     */
    TokXmlDecl(final XmlContent theContent, final int theStart, final int theEnd,
               final int theLineNumber, final int theColumn) {

        super(theContent, theStart, theEnd, theLineNumber, theColumn);

        this.versionNum = null;
        this.encoding = null;
        this.standalone = null;
    }

    /**
     * Gets the version number parsed from the XML specification.
     *
     * @return the version number
     */
    public String getVersionNum() {

        return this.versionNum;
    }

    /**
     * Gets the encoding parsed from the XML specification.
     *
     * @return the encoding
     */
    public String getEncoding() {

        return this.encoding;
    }

    /**
     * Gets the standalone setting parsed from the XML specification.
     *
     * @return the standalone setting
     */
    public Boolean getStandalone() {

        return this.standalone;
    }

    /**
     * Generates the string representation of the TokXmlDecl token.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder str = printContent("XmlDecl: ");

        printValue(this.versionNum, "Version", str);
        printValue(this.encoding, "Encoding", str);
        printValue(this.standalone, "Standalone", str);

        return str.toString();
    }

    /**
     * Validates the contents of the TokXmlDecl token, which may store internal structure that can be used when the
     * token in assembled into a node.
     *
     * @throws ParsingException if the contents are invalid
     */
    @Override
    public void validate() throws ParsingException {

        final XmlContent content = getContent();

        this.versionNum = null;

        validateString(content, "xml ", 2, false);

        int pos = parseVersion();

        if (pos < (getEnd() - 2) && content.get(pos) == 'e') {
            pos = skipWhitespace(parseEncoding(pos));
        }

        if (pos < (getEnd() - 2) && content.get(pos) == 's') {
            pos = skipWhitespace(parseStandalone(pos));
        }

        if (pos != getEnd() - 2) {
            throw new ParsingException(getStart(), getStart() + START_LEN,
                    Res.get(Res.BAD_XML_DECL));
        }
    }

    /**
     * Tests whether the TokXmlDecl token generates one or more nodes (or part of a node) in the XML node structure.
     *
     * @return {@code true} if the token contributes to the XML node structure; {@code false} if not
     */
    @Override
    public boolean hasNodeStructure() {

        return false;
    }

    /**
     * Parse the version number from the XML declaration.
     *
     * @return the position of the first non-whitespace character after the version number
     * @throws ParsingException if the version could not be parsed
     */
    private int parseVersion() throws ParsingException {

        final XmlContent content = getContent();

        int pos = validateString(content, "version",
                skipWhitespace(getStart() + START_LEN + 1), true);

        final char quot = content.get(pos);
        final int match = indexOf(quot, pos + 1, getEnd());

        if (XmlChars.isQuote(quot) && match != -1 && XmlChars.isDigit(content.get(pos + 1))
                && content.get(pos + 2) == '.') {

            for (int i = pos + MINOR_VER_OFFSET; i < match; ++i) {
                final char test = content.get(i);

                if (!XmlChars.isDigit(test)) {
                    throw new ParsingException(this, Res.get(Res.BAD_XML_DECL));
                }
            }

            this.versionNum = content.substring(pos + 1, match);

            pos = skipWhitespace(match + 1);
        } else {
            throw new ParsingException(this, Res.get(Res.BAD_XML_DECL));
        }

        return pos;
    }

    /**
     * Parses the encoding specification.
     *
     * @param start the position at which the specification begins
     * @return the position of the first non-whitespace character after the encoding
     * @throws ParsingException if the encoding could not be parsed
     */
    private int parseEncoding(final int start) throws ParsingException {

        final XmlContent content = getContent();
        final int pos = validateString(content, "encoding", start, true);

        final char quot = content.get(pos);
        final int match = indexOf(quot, pos + 1, getEnd());

        if (match == -1 || !XmlChars.isQuote(quot)) {
            throw new ParsingException(this, Res.get(Res.BAD_XML_DECL));
        }

        boolean valid = true;

        if (XmlChars.isEncodingChar1(content.get(pos + 1))) {

            for (int i = pos + 2; i < match; ++i) {
                final char test = content.get(i);

                if (!XmlChars.isEncodingChar2(test)) {
                    Log.warning(Res.fmt(Res.BAD_ENCCHAR, Character.toString(content.get(i))));
                    valid = false;

                    break;
                }
            }
        } else {
            Log.warning(Res.fmt(Res.BAD_ENCCHAR1, Character.toString(content.get(quot + 1))));
            valid = false;
        }

        if (valid) {
            this.encoding = content.substring(pos + 1, match);
        } else {
            throw new ParsingException(this, Res.get(Res.BAD_XML_DECL));
        }

        return skipWhitespace(match + 1);
    }

    /**
     * Parses the encoding specification.
     *
     * @param start the position at which the specification begins
     * @return the position of the first non-whitespace character after the encoding
     * @throws ParsingException if the encoding could not be parsed
     */
    private int parseStandalone(final int start) throws ParsingException {

        final XmlContent content = getContent();
        int pos = validateString(content, "standalone", start, true);

        final char quot = content.get(pos);
        final int match = indexOf(quot, pos + 1, getEnd());

        if (XmlChars.isQuote(quot) && match != -1) {
            final String sub = content.substring(pos + 1, match);

            if (YES_STR.equals(sub)) {
                this.standalone = Boolean.TRUE;
            } else if (NO_STR.equals(sub)) {
                this.standalone = Boolean.FALSE;
            } else {
                throw new ParsingException(this, Res.get(Res.BAD_XML_DECL));
            }

            pos = skipWhitespace(match + 1);
        } else {
            throw new ParsingException(this, Res.get(Res.BAD_XML_DECL));
        }

        return pos;
    }

    /**
     * Verifies that the content of a {@code XmlContent} beginning at a specified position matches a specified array of
     * characters.
     *
     * @param content the {@code XmlContent}
     * @param test    the array of characters to test for
     * @param start   the position at which to begin testing
     * @param equals  {@code true} to expect and search for an '=' symbol after the string, with optional whitespace
     *                before and after the '='
     * @return the position of the first non-whitespace character after the validated string, or after the '=', if
     *         called for
     * @throws ParsingException if the string could not be validated
     */
    private int validateString(final XmlContent content, final CharSequence test, final int start,
                               final boolean equals) throws ParsingException {

        for (int i = 0; i < test.length(); ++i) {
            if (content.get(start + i) != test.charAt(i)) {
                throw new ParsingException(this, Res.get(Res.BAD_XML_DECL));
            }
        }

        int pos = skipWhitespace(start + test.length());

        if (equals) {
            if (content.get(pos) == '=') {
                pos = skipWhitespace(pos + 1);
            } else {
                throw new ParsingException(this, Res.get(Res.BAD_XML_DECL));
            }
        }

        return pos;
    }

    /**
     * Tests whether the XmlContent token represents an XML element.
     *
     * @return {@code false} if the token represents an element; {@code true} if not
     */
    @Override
    public boolean isNonElement() {

        return false;
    }
}
