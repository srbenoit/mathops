package dev.mathops.assessment;

import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;

import java.util.Locale;

/**
 * A base class for object that will serialize themselves into XML streams.
 */
public abstract class AbstractXmlObject {

    /** Pre-created indent strings for fast indenting. */
    private static final String[] INDENTS = {CoreConstants.EMPTY,
            "  ",
            "    ",
            "      ",
            "        ",
            "          ",
            "            ",
            "              ",
            "                ",
            "                  ",
            "                    ",
            "                      ",
            "                        ",
            "                          ",
            "                            "};

    /**
     * Constructs a new {@code AbstractXmlObject}.
     */
    protected AbstractXmlObject() {

        // No action
    }

    /**
     * Generates the XML representation of the object as a String.
     *
     * @param indent the number of spaces to indent the printout
     * @return the XML representation
     */
    public final String toXmlString(final int indent) {

        final HtmlBuilder xml = new HtmlBuilder(512);

        appendXml(xml, indent);

        return xml.toString();
    }

    /**
     * Appends the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    public abstract void appendXml(HtmlBuilder xml, int indent);

    /**
     * Writes an attribute value to an XML output stream. The attribute will include a space, the attribute name, an '='
     * sign, then the value, surrounded in quotes.
     *
     * @param xml   the {@code HtmlBuilder} to which to write the XML
     * @param name  the name of the attribute
     * @param value the attribute value
     */
    public static void writeAttribute(final HtmlBuilder xml, final String name, final Object value) {

        if (value != null) {
            xml.add(CoreConstants.SPC, name, "=\"");

            if (value instanceof Boolean) {
                xml.add(((Boolean) value).toString().toUpperCase(Locale.ROOT));
            } else {
                xml.add(escape(value.toString()));
            }

            xml.add('\"');
        }
    }

    /**
     * Creates a string for a particular indentation level.
     *
     * @param indent the number of spaces to indent
     * @return a string with the requested number of spaces
     */
    public static String makeIndent(final int indent) {

        final String str;

        if (indent < INDENTS.length) {
            str = INDENTS[indent];
        } else {
            str = INDENTS[INDENTS.length - 1];
        }

        return str;
    }

    /**
     * Replaces XML special characters with Unicode escapes.
     *
     * @param text the string to be escaped
     * @return the escaped string
     */
    protected static String escape(final CharSequence text) {

        final int len = text.length();
        final HtmlBuilder builder = new HtmlBuilder(len + (len / 20));

        for (int i = 0; i < len; ++i) {
            final char chr = text.charAt(i);

            if (chr == '<') {
                builder.add("&lt;");
            } else if (chr == '>') {
                builder.add("&gt;");
            } else if (chr == '&') {
                builder.add("&amp;");
            } else if (chr == '\u2264') {
                builder.add("\\u2264");
            } else if (chr == '\u2265') {
                builder.add("\\u2265");
            } else {
                builder.add(chr);
            }
        }

        return builder.toString();
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public abstract int hashCode();

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param obj the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public abstract boolean equals(Object obj);
}
