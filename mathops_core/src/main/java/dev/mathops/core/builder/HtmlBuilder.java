package dev.mathops.core.builder;

import dev.mathops.core.CoreConstants;

/**
 * A simple implementation of the {@code HtmlBuilder} interface which can efficiently build HTML strings.
 */
public final class HtmlBuilder extends XmlBuilder {

    /** A common string. */
    private static final String TICK = "'";

    /** A common string. */
    private static final String OPEN_H = "<h";

    /** A common string. */
    private static final String OPEN_DIV = "<div";

    /** A common string. */
    private static final String OPEN_DIV_CLS = "<div class='";

    /** A common string. */
    private static final String TH = "<th>";

    /** A common string. */
    private static final String TD = "<td>";

    /**
     * Constructs a new {@code HtmlBuilder}.
     *
     * @param capacity the initial capacity to allocate for storage
     */
    public HtmlBuilder(final int capacity) {

        super(capacity);
    }

    /**
     * Tightens super-interface contract to return an {@code HtmlBuilder}.
     *
     * @param value the character value to append
     * @return a reference to this object
     */
    public HtmlBuilder add(final char value) {

        innerAddChar(value);
        return this;
    }

    /**
     * Tightens super-interface contract to return an {@code HtmlBuilder}.
     *
     * @param value the character value to append
     * @return a reference to this object
     */
    public HtmlBuilder add(final char[] value) {

        innerAddChars(value);
        return this;
    }

    /**
     * Tightens super-interface contract to return an {@code HtmlBuilder}.
     *
     * @param value the character value to append
     * @return a reference to this object
     */
    public HtmlBuilder add(final String value) {

        innerAddString(value);
        return this;
    }

    /**
     * Tightens super-interface contract to return an {@code HtmlBuilder}.
     *
     * @param value the boolean value to append
     * @return a reference to this object
     */
    public HtmlBuilder add(final boolean value) {

        innerAddBoolean(value);
        return this;
    }

    /**
     * Tightens super-interface contract to return an {@code HtmlBuilder}.
     *
     * @param value the int value to append
     * @return a reference to this object
     */
    public HtmlBuilder add(final int value) {

        innerAddInt(value);
        return this;
    }

    /**
     * Tightens super-interface contract to return an {@code HtmlBuilder}.
     *
     * @param value the long value to append
     * @return a reference to this object
     */
    public HtmlBuilder add(final long value) {

        innerAddLong(value);
        return this;
    }

    /**
     * Tightens super-interface contract to return an {@code HtmlBuilder}.
     *
     * @param value the float value to append
     * @return a reference to this object
     */
    public HtmlBuilder add(final float value) {

        innerAddFloat(value);
        return this;
    }

    /**
     * Tightens super-interface contract to return an {@code HtmlBuilder}.
     *
     * @param value the double value to append
     * @return a reference to this object
     */
    public HtmlBuilder add(final double value) {

        innerAddDouble(value);
        return this;
    }

    /**
     * Tightens super-interface contract to return an {@code HtmlBuilder}.
     *
     * @param value the objects to append
     * @return a reference to this object
     */
    public HtmlBuilder add(final Object value) {

        innerAddObject(value);
        return this;
    }

    /**
     * Tightens super-interface contract to return an {@code HtmlBuilder}.
     *
     * @param value the objects to append
     * @return a reference to this object
     */
    public HtmlBuilder add(final Object... value) {

        innerAddObjects(value);
        return this;
    }

    /**
     * Appends a newline to this sequence.
     *
     * @return a reference to this object
     */
    public HtmlBuilder addln() {

        innerAddCrlf();

        return this;
    }

    /**
     * Appends the string representation of the char argument followed by a newline to this sequence.
     *
     * @param value the character value to append
     * @return a reference to this object
     */
    public HtmlBuilder addln(final char value) {

        innerAddLnChar(value);

        return this;
    }

    /**
     * Appends the string representation of the char argument to this sequence. The argument is appended to the contents
     * of this sequence, whose length increases by the length of the argument plus 2 newline characters.
     * <p>
     * The effect is exactly as if the character were converted to a string by the method {@code String.valueOf(char)}
     * and then appended to this character sequence.
     *
     * @param value the character value to append
     * @return a reference to this object
     */
    public HtmlBuilder addln(final char... value) {

        innerAddLnChars(value);

        return this;
    }

    /**
     * Appends the string representation of the char argument followed by a newline to this sequence.
     *
     * @param value the character value to append
     * @return a reference to this object
     */
    public HtmlBuilder addln(final String value) {

        innerAddLnString(value);

        return this;
    }

    /**
     * Appends the string representation of the boolean argument followed by a newline to the sequence.
     *
     * @param value the boolean value to append
     * @return a reference to this object
     */
    public HtmlBuilder addln(final boolean value) {

        innerAddLnBoolean(value);

        return this;
    }

    /**
     * Appends the string representation of the float argument followed by a newline to this sequence.
     *
     * @param value the float value to append
     * @return a reference to this object
     */
    public HtmlBuilder addln(final float value) {

        innerAddLnFloat(value);

        return this;
    }

    /**
     * Appends the string representation of the double argument followed by a newline to this sequence.
     *
     * @param value the double value to append
     * @return a reference to this object
     */
    public HtmlBuilder addln(final double value) {

        innerAddLnDouble(value);

        return this;
    }

    /**
     * Appends the string representation of the int argument followed by a newline to this sequence.
     *
     * @param value the int value to append
     * @return a reference to this object
     */
    public HtmlBuilder addln(final int value) {

        innerAddLnInt(value);

        return this;
    }

    /**
     * Appends the string representation of the long argument followed by a newline to this sequence.
     *
     * @param value the long value to append
     * @return a reference to this object
     */
    public HtmlBuilder addln(final long value) {

        innerAddLnLong(value);

        return this;
    }

    /**
     * Appends the string representation of any number of Objects followed by a newline to this sequence.
     *
     * @param value the objects to append
     * @return a reference to this object
     */
    public HtmlBuilder addln(final Object value) {

        innerAddLnObjects(value);

        return this;
    }

    /**
     * Appends the string representation of any number of Objects followed by a newline to this sequence.
     *
     * @param value the objects to append
     * @return a reference to this object
     */
    public HtmlBuilder addln(final Object... value) {

        innerAddLnObjects(value);

        return this;
    }

    /**
     * Indents by appending a specified number of spaces.
     *
     * @param count the number of spaces to append
     */
    public void indent(final int count) {

        innerIndent(count);
    }

    /**
     * Escapes a {@code String} so that is suitable for use in an XML attribute, then appends the escaped
     * {@code String} to this sequence.
     *
     * @param value the {@code String} to escape and append
     * @return a reference to this object
     */
    public HtmlBuilder addEscaped(final String value) {

        innerAddEscaped(value);
        return this;
    }

    /**
     * Appends an XML attribute in the form {@code name='value'}, where {@code value} is escaped as needed to comply
     * with XML, and with a leading space before the name.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @param drop  if nonzero, drops to a new line and indents attribute this number of spaces
     * @return a reference to this object
     */
    public HtmlBuilder addAttribute(final String name, final Object value, final int drop) {

        innerAddAttribute(name, value, drop);
        return this;
    }

    /**
     * Appends the opening of an element tag, writing "&lt;tag".
     *
     * @param indent the number of spaces to indent the tag
     * @param tag    the tag being opened
     * @return a reference to this object
     */
    public HtmlBuilder openElement(final int indent, final String tag) {

        innerOpenElement(indent, tag);
        return this;
    }

    /**
     * Appends a start tag (without attributes) for an empty element tag, writing "&lt;tag&gt;".
     *
     * @param indent  the number of spaces to indent the tag
     * @param tag     the tag being opened
     * @param newline {@code true} to add a newline after the tag
     * @return a reference to this object
     */
    public HtmlBuilder startNonempty(final int indent, final String tag, final boolean newline) {

        innerStartNonempty(indent, tag, newline);
        return this;
    }

    /**
     * Appends an end tag for the opening of a nonempty element tag, writing "&lt;/tag&gt;".
     *
     * @param indent  the number of spaces to indent the tag
     * @param tag     the tag being opened
     * @param newline {@code true} to add a newline after the tag
     * @return a reference to this object
     */
    public HtmlBuilder endNonempty(final int indent, final String tag, final boolean newline) {

        innerEndNonempty(indent, tag, newline);
        return this;
    }

    /**
     * Appends the ending of the start tag of nonempty element tag, writing "&gt;[CRLF]".
     *
     * @param newline {@code true} to add a newline after the closure
     * @return a reference to this object
     */
    public HtmlBuilder endOpenElement(final boolean newline) {

        innerEndOpenElement(newline);
        return this;
    }

    /**
     * Appends the closure of an empty element tag, writing "/>[CRLF]".
     *
     * @param newline {@code true} to add a newline after the tag
     * @return a reference to this object
     */
    public HtmlBuilder closeEmptyElement(final boolean newline) {

        innerCloseEmptyElement(newline);
        return this;
    }

    /**
     * Emits the start of a "p" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sP() {

        add("<p>");

        return this;
    }

    /**
     * Emits the start of a "p" tag, with an optional CSS class.
     *
     * @param cls        the CSS class; {@code null} to omit
     * @param attributes additional attributes to add
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sP(final String cls, final String... attributes) {

        if (cls == null) {
            add("<p");
        } else {
            add("<p class='", cls, TICK);
        }

        for (final String attr : attributes) {
            add(CoreConstants.SPC_CHAR).add(attr);
        }

        add(XmlBuilder.GT_CHAR);

        return this;
    }

    /**
     * Emits the end of a "p" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder eP() {

        add("</p>");

        return this;
    }

    /**
     * Emits the start of a "span" tag, with an optional CSS class.
     *
     * @param cls        the CSS class; {@code null} to omit
     * @param attributes additional attributes to add
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sSpan(final String cls, final String... attributes) {

        if (cls == null) {
            add("<span");
        } else {
            add("<span class='", cls, TICK);
        }

        for (final String attr : attributes) {
            add(CoreConstants.SPC_CHAR).add(attr);
        }

        add(XmlBuilder.GT_CHAR);

        return this;
    }

    /**
     * Emits the end of a "span" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder eSpan() {

        add("</span>");

        return this;
    }

    /**
     * Emits the start of a "h?" tag, where ? is an integer from 1 to 6.
     *
     * @param level the heading level (1 to 6)
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sH(final int level) {

        add(OPEN_H).add(level).add(XmlBuilder.GT_CHAR);

        return this;
    }

    /**
     * Emits the start of a "h?" tag, where ? is an integer from 1 to 6.
     *
     * @param level the heading level (1 to 6)
     * @param cls   a class
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sH(final int level, final String cls) {

        add(OPEN_H).add(level);
        if (cls != null) {
            add(" class='").add(cls).add(XmlBuilder.TICK_CHAR);
        }
        add(XmlBuilder.GT_CHAR);

        return this;
    }

    /**
     * Emits the end of a "h?" tag, where ? is an integer from 1 to 6.
     *
     * @param level the heading level (1 to 6)
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder eH(final int level) {

        add("</h").add(level).add(XmlBuilder.GT_CHAR);

        return this;
    }

    /**
     * Emits an empty "br" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder br() {

        add("<br/>");

        return this;
    }

    /**
     * Emits an empty "hr" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder hr() {

        addln("<hr/>");

        return this;
    }

    /**
     * Emits an empty "hr" tag.
     *
     * @param cls the class name
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder hr(final String cls) {

        addln("<hr class='", cls, "'/>");

        return this;
    }

    /**
     * Emits the start of a "div" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sDiv() {

        add("<div>");

        return this;
    }

    /**
     * Emits the start of a "div" tag, with an optional CSS class.
     *
     * @param cls        the CSS class; {@code null} to omit
     * @param attributes additional attributes to add
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sDiv(final String cls, final String... attributes) {

        if (cls == null) {
            add(OPEN_DIV);
        } else {
            add(OPEN_DIV_CLS, cls, TICK);
        }

        for (final String attr : attributes) {
            add(CoreConstants.SPC_CHAR).add(attr);
        }

        add(XmlBuilder.GT_CHAR);

        return this;
    }

    /**
     * Emits the end of a "div" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder eDiv() {

        add("</div>");

        return this;
    }

    /**
     * Emits a start and end of a "div" tag, with no contents.
     *
     * @param cls        the CSS class; {@code null} to omit
     * @param attributes additional attributes to add
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder div(final String cls, final String... attributes) {

        if (cls == null) {
            add(OPEN_DIV);
        } else {
            add(OPEN_DIV_CLS, cls, TICK);
        }

        for (final String attr : attributes) {
            add(CoreConstants.SPC_CHAR).add(attr);
        }

        add("></div>");

        return this;
    }

    /**
     * Emits the start of a "tr" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sTr() {

        add("<tr>");

        return this;
    }

    /**
     * Emits the start of a "tr" tag.
     *
     * @param cls        the CSS class; {@code null} to omit
     * @param attributes additional attributes to add
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sTr(final String cls, final String... attributes) {

        if (cls == null) {
            add("<tr");
        } else {
            add("<tr class='", cls, TICK);
        }

        for (final String attr : attributes) {
            add(CoreConstants.SPC_CHAR).add(attr);
        }

        add(XmlBuilder.GT_CHAR);

        return this;
    }

    /**
     * Emits the end of a "tr" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder eTr() {

        add("</tr>");

        return this;
    }

    /**
     * Emits the start of a "table" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sTable() {

        add("<table>");

        return this;
    }

    /**
     * Emits the start of a "table" tag, with an optional CSS class.
     *
     * @param cls        the CSS class; {@code null} to omit
     * @param attributes additional attributes to add
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sTable(final String cls, final String... attributes) {

        if (cls == null) {
            add("<table");
        } else {
            add("<table class='", cls, TICK);
        }

        for (final String attr : attributes) {
            add(CoreConstants.SPC_CHAR).add(attr);
        }

        add(XmlBuilder.GT_CHAR);

        return this;
    }

    /**
     * Emits the end of a "table" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder eTable() {

        add("</table>");

        return this;
    }

    /**
     * Emits the start of a "th" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sTh() {

        add(TH);

        return this;
    }

    /**
     * Emits the start of a "th" tag.
     *
     * @param cls the CSS class; {@code null} to omit
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sTh(final String cls) {

        if (cls == null) {
            add(TH);
        } else {
            add("<th class='", cls, TICK_CLOSE);
        }

        return this;
    }

    /**
     * Emits the start of a "th" tag.
     *
     * @param cls        the CSS class; {@code null} to omit
     * @param attributes additional attributes to add
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sTh(final String cls, final String... attributes) {

        add("<th");
        if (cls != null) {
            add(" class='", cls, TICK);
        }

        for (final String attr : attributes) {
            add(CoreConstants.SPC_CHAR).add(attr);
        }

        add(XmlBuilder.GT_CHAR);

        return this;
    }

    /**
     * Emits the end of a "th" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder eTh() {

        add("</th>");

        return this;
    }

    /**
     * Emits the start of a "td" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sTd() {

        add(TD);

        return this;
    }

    /**
     * Emits the start of a "td" tag.
     *
     * @param cls the CSS class; {@code null} to omit
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sTd(final String cls) {

        if (cls == null) {
            add(TD);
        } else {
            add("<td class='", cls, TICK_CLOSE);
        }

        return this;
    }

    /**
     * Emits the start of a "td" tag.
     *
     * @param cls        the CSS class; {@code null} to omit
     * @param attributes additional attributes to add
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder sTd(final String cls, final String... attributes) {

        add("<td");
        if (cls != null) {
            add(" class='", cls, TICK);
        }

        for (final String attr : attributes) {
            add(CoreConstants.SPC_CHAR).add(attr);
        }

        add(XmlBuilder.GT_CHAR);

        return this;
    }

    /**
     * Emits the end of a "td" tag.
     *
     * @return this {@code HtmlBuilder}
     */
    public HtmlBuilder eTd() {

        add("</td>");

        return this;
    }
}
