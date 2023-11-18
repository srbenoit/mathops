package dev.mathops.core.parser.xml;

import dev.mathops.core.parser.CharSpan;

/**
 * An attribute on an XML element.
 */
public final class Attribute extends CharSpan {

    /** The attribute name. */
    public final String name;

    /** The attribute value. */
    public final String value;

    /** The position of the start of the attribute name. */
    public final int nameStart;

    /** The position of the end of the attribute name. */
    public final int nameEnd;

    /**
     * Constructs a new {@code Attribute}.
     *
     * @param theName       the attribute name
     * @param theValue      the attribute value
     * @param theNameStart  the position of the start of the attribute name
     * @param theNameEnd    the position of the end of the attribute name
     * @param theStart the position of the start of the attribute value
     * @param theEnd   the position of the end of the attribute value
     * @param theLineNumber    the line number of the start of the attribute name
     * @param theColumn        the column of the start of the attribute name
     */
    Attribute(final String theName, final String theValue, final int theNameStart, final int theNameEnd,
              final int theStart, final int theEnd, final int theLineNumber, final int theColumn) {

        super(theStart, theEnd, theLineNumber, theColumn);

        this.name = theName;
        this.value = theValue;
        this.nameStart = theNameStart;
        this.nameEnd = theNameEnd;
    }
}
