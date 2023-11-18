package dev.mathops.core.builder;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.parser.xml.XmlEscaper;

/**
 * Supplements the methods of {@code LineBuilder} with convenience methods for creating XML (or HTML, etc.) files.
 */
class XmlBuilder extends LineBuilder {

    /** A commonly used string. */
    static final String TICK_CLOSE = "'/>";

    /** A commonly used string. */
    private static final char[] EQUAL_TICK = "='".toCharArray();

    /** A commonly used string. */
    private static final String LT = "<";

    /** A commonly used string. */
    private static final String LT_SLASH = "</";

    /** A commonly used string. */
    private static final String GT = ">";

    /** A commonly used string. */
    private static final String SLASH_GT = "/>";

    /** A commonly used character. */
    static final char TICK_CHAR = '\'';

    /** A commonly used character. */
    private static final char QUOTE_CHAR = '"';

    /** A commonly used character. */
    private static final char LT_CHAR = '<';

    /** A commonly used character. */
    static final char GT_CHAR = '>';

    /** A commonly used character. */
    private static final char AMP_CHAR = '&';

    /**
     * Constructs a new {@code XmlBuilder}.
     *
     * @param capacity the initial capacity to allocate for storage
     */
    XmlBuilder(final int capacity) {

        super(capacity);
    }

    /**
     * Escapes a {@code String} so that is suitable for use in an XML attribute, then appends the escaped
     * {@code String} to this sequence.
     *
     * @param value the {@code String} to escape and append
     */
    final void innerAddEscaped(final String value) {

        final char[] chars = value.toCharArray();

        for (final char chr : chars) {
            switch (chr) {
                case QUOTE_CHAR -> innerAddString(XmlEscaper.QUOT);
                case TICK_CHAR -> innerAddString(XmlEscaper.APOS);
                case LT_CHAR -> innerAddString(XmlEscaper.LEFT);
                case GT_CHAR -> innerAddString(XmlEscaper.RIGHT);
                case AMP_CHAR -> innerAddString(XmlEscaper.AMP);
                default -> innerAddChar(chr);
            }
        }
    }

    /**
     * Appends an XML attribute in the form {@code name='value'}, where {@code value} is escaped as needed to comply
     * with XML, and with a leading space before the name.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @param drop  if nonzero, drops to a new line and indents attribute this number of spaces
     */
    final void innerAddAttribute(final String name, final Object value, final int drop) {

        if (value != null) {
            if (drop == 0) {
                innerAddChar(CoreConstants.SPC_CHAR);
            } else {
                innerAddCrlf();
                innerIndent(drop);
            }

            innerAddObjects(name, EQUAL_TICK);
            final String str = value.toString();
            innerAddEscaped(str);
            innerAddChar(TICK_CHAR);
        }
    }

    /**
     * Appends the opening of an element tag, writing "&lt;tag".
     *
     * @param indent the number of spaces to indent the tag
     * @param tag    the tag being opened
     */
    final void innerOpenElement(final int indent, final String tag) {

        innerIndent(indent);
        innerAddObjects(LT, tag);
    }

    /**
     * Appends a start tag (without attributes) for an empty element tag, writing "&lt;tag&gt;".
     *
     * @param indent  the number of spaces to indent the tag
     * @param tag     the tag being opened
     * @param newline {@code true} to add a newline after the tag
     */
    final void innerStartNonempty(final int indent, final String tag, final boolean newline) {

        innerIndent(indent);
        innerAddObjects(LT, tag, GT);
        if (newline) {
            innerAddCrlf();
        }
    }

    /**
     * Appends an end tag for the opening of a nonempty element tag, writing "&lt;/tag&gt;".
     *
     * @param indent  the number of spaces to indent the tag
     * @param tag     the tag being opened
     * @param newline {@code true} to add a newline after the tag
     */
    final void innerEndNonempty(final int indent, final String tag, final boolean newline) {

        innerIndent(indent);
        innerAddObjects(LT_SLASH, tag, GT);
        if (newline) {
            innerAddCrlf();
        }
    }

    /**
     * Appends the ending of the start tag of nonempty element tag, writing "&gt;[CRLF]".
     *
     * @param newline {@code true} to add a newline after the closure
     */
    final void innerEndOpenElement(final boolean newline) {

        innerAddString(GT);

        if (newline) {
            innerAddCrlf();
        }
    }

    /**
     * Appends the closure of an empty element tag, writing "/>[CRLF]".
     *
     * @param newline {@code true} to add a newline after the tag
     */
    final void innerCloseEmptyElement(final boolean newline) {

        innerAddString(SLASH_GT);

        if (newline) {
            innerAddCrlf();
        }
    }

    /**
     * Appends some number of spaces.
     *
     * @param count the number of spaces to append
     */
    final void innerIndent(final int count) {

        for (int i = 0; i < count; ++i) {
            innerAddString(CoreConstants.SPC);
        }
    }
}
