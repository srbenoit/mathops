package dev.mathops.assessment.document.inst;

import dev.mathops.assessment.document.EPaletteSymbol;
import dev.mathops.assessment.document.EXmlStyle;
import dev.mathops.commons.builder.HtmlBuilder;

import java.util.Arrays;

/**
 * A palette that allows the user to click buttons to enter symbols, such as "pi", "theta", etc. This control can be
 * embedded in a paragraph or span. It does not appear in a "print" version of a document, and should behave as if a key
 * was pressed in terms of the currently focused input and insertion caret.
 */
public final class DocSymbolPaletteInst extends AbstractDocObjectInst {

    /** The symbols. */
    public final EPaletteSymbol[] symbols;

    /**
     * Construct a new {@code DocSymbolPaletteInst} object.
     *
     * @param theStyle    the style object ({@code null} to inherit the parent object's style)
     * @param theBgColorName the background color name ({@code null} if transparent)
     * @param theSymbols the symbols the palette will support
     */
    public DocSymbolPaletteInst(final DocObjectInstStyle theStyle, final String theBgColorName,
                                final EPaletteSymbol... theSymbols) {

        super(theStyle, theBgColorName);

        if (theSymbols == null || theSymbols.length == 0) {
            throw new IllegalArgumentException("Symbols list may not be null or empty");
        }

        this.symbols = theSymbols.clone();
    }

    /**
     * Gets the symbols.
     *
     * @return a copy of the symbols list
     */
    public EPaletteSymbol[] getSymbols() {

        return this.symbols.clone();
    }

    /**
     * Write the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml      the {@code HtmlBuilder} to which to write the XML
     * @param xmlStyle the style to use when emitting XML
     * @param indent   the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final EXmlStyle xmlStyle, final int indent) {

        xml.add("<symbol-palette");
        addDocObjectInstXmlAttributes(xml);

        final HtmlBuilder symlist = new HtmlBuilder(50);
        symlist.add(this.symbols[0]);
        final int count = this.symbols.length;
        for (int i = 1; i < count; ++i) {
            symlist.add(',');
            symlist.add(this.symbols[i]);
        }
        xml.addAttribute("symbols", symlist.toString(), 0);
        xml.add("/>");
    }

    /**
     * Generate a {@code String} representation of the fraction (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(500);

        builder.add("DocRadicalInst");
        appendStyleString(builder);
        builder.add(",symbols=");
        builder.add(this.symbols[0]);
        final int count = this.symbols.length;
        for (int i = 1; i < count; ++i) {
            builder.add(',');
            builder.add(this.symbols[i]);
        }

        return builder.toString();
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return docObjectInstHashCode() + Arrays.hashCode(this.symbols);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocSymbolPaletteInst palette) {
            equal = checkDocObjectInstEquals(palette)
                    && Arrays.equals(this.symbols, palette.symbols);
        } else {
            equal = false;
        }

        return equal;
    }
}
