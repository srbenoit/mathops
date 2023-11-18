package dev.mathops.core.parser.xml;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * A utility class to escape strings for use in XML (as an attribute value, for example) and recover the original
 * strings from such escaped string values.
 */
public enum XmlEscaper {
    ;

    /** The quote escape. */
    public static final String QUOT = "&quot;";

    /** The apostrophe escape. */
    public static final String APOS = "&apos;";

    /** The less-than escape. */
    public static final String LEFT = "&lt;";

    /** The greater-than escape. */
    public static final String RIGHT = "&gt;";

    /** The ampersand escape. */
    public static final String AMP = "&amp;";

    /** The initial size of the escaper buffer. */
    private static final int INIT_BUFFER_SIZE = 100;

    /** Offset of a hex value in the escape "&#x12ab". */
    private static final int HEX_VALUE_OFFSET = 3;

    /** Offset of a hex value in the escape "&#1234". */
    private static final int DEC_VALUE_OFFSET = 2;

    /** Base of hex numbers. */
    private static final int HEX_BASE = 16;

    /** A string builder used to construct output strings. */
    private static final HtmlBuilder STR;

    static {
        STR = new HtmlBuilder(INIT_BUFFER_SIZE);
    }

    /**
     * Escapes a string for use in an XML attribute.
     *
     * @param orig the original string to escape
     * @return the escaped string
     */
    public static String escape(final String orig) {

        final char[] chars = orig.toCharArray();

        synchronized (STR) {
            STR.reset();

            for (final char chr : chars) {
                if (chr == '\"') {
                    STR.add(QUOT);
                } else if (chr == '\'') {
                    STR.add(APOS);
                } else if (chr == '<') {
                    STR.add(LEFT);
                } else if (chr == '>') {
                    STR.add(RIGHT);
                } else if (chr == '&') {
                    STR.add(AMP);
                } else {
                    STR.add(chr);
                }
            }

            return STR.toString();
        }
    }

    /**
     * Unescapes a string that was processed by {@code escape}.
     *
     * @param escaped the string to unescape
     * @return the original (unescaped) string
     */
    public static String unescape(final String escaped) {

        synchronized (STR) {
            STR.reset();

            int pos = 0;

            final int len = escaped.length();
            while (pos < len) {
                final char chr = escaped.charAt(pos);

                if (chr == '&') {

                    if (isEscape(escaped, pos, QUOT)) {
                        STR.add('\"');
                        pos += QUOT.length();
                    } else if (isEscape(escaped, pos, APOS)) {
                        STR.add('\'');
                        pos += APOS.length();
                    } else if (isEscape(escaped, pos, LEFT)) {
                        STR.add('<');
                        pos += LEFT.length();
                    } else if (isEscape(escaped, pos, RIGHT)) {
                        STR.add('>');
                        pos += RIGHT.length();
                    } else if (isEscape(escaped, pos, AMP)) {
                        STR.add('&');
                        pos += AMP.length();
                    } else {
                        pos = unescapeSingle(escaped, pos);
                    }
                } else {
                    STR.add(chr);
                    ++pos;
                }
            }

            return STR.toString();
        }
    }

    /**
     * Processes a single escape sequence. This method must be called from within a block that is synchronized on
     * {@code STR}.
     *
     * @param escaped the string to unescape
     * @param start   the position of the '&' at the start of the escape
     * @return the position after the escape
     */
    private static int unescapeSingle(final String escaped, final int start) {

        int pos = start;
        final int semicolon = escaped.indexOf(';', pos + 1);

        if (semicolon == -1 || escaped.charAt(pos + 1) != '#') {
            STR.add(escaped.charAt(pos));
            ++pos;
        } else if (escaped.charAt(pos + 2) == 'x') {
            boolean valid = true;

            for (int i = pos + HEX_VALUE_OFFSET; valid && i < semicolon; ++i) {
                valid = XmlChars.isHex(escaped.charAt(i));
            }

            if (valid) {
                STR.add(Character.toChars(Integer.parseInt(escaped.substring(pos + HEX_VALUE_OFFSET, semicolon),
                        HEX_BASE)));
            }

            pos = semicolon + 1;
        } else {
            boolean valid = true;

            for (int i = pos + DEC_VALUE_OFFSET; valid && i < semicolon; ++i) {
                valid = XmlChars.isDigit(escaped.charAt(i));
            }

            if (valid) {
                STR.add(Character.toChars(Integer.parseInt(escaped.substring(pos + DEC_VALUE_OFFSET, semicolon))));
            }

            pos = semicolon + 1;
        }

        return pos;
    }

    /**
     * Tests whether a character sequence has a given escape sequence at a specified position.
     *
     * @param escaped the character sequence
     * @param pos     the position
     * @param esc     the escape sequence to test for
     * @return {@code true} if the code sequence does contain the escape at the given position; {@code false}
     *         otherwise
     */
    private static boolean isEscape(final CharSequence escaped, final int pos, final CharSequence esc) {

        final int len = esc.length();
        boolean hit = escaped.length() >= (pos + len);

        for (int i = 1; hit && i < len; ++i) {
            hit = escaped.charAt(pos + i) == esc.charAt(i);
        }

        return hit;
    }
}
