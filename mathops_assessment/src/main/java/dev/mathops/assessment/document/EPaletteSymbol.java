package dev.mathops.assessment.document;

/**
 * Symbols that a symbol entry palette can support.
 */
public enum EPaletteSymbol {

    /** The "pi" symbol. */
    PI("pi", '\u03c0'),

    /** The "e" symbol. */
    E("e", 'e');

    /** The label. */
    public final String label;

    /** The character the symbol represents (the pi symbol, for example). */
    public final char character;

    /**
     * Constructs a new {@code EPaletteSymbol}.
     *
     * @param theLabel     the label
     * @param theCharacter the character the symbol represents
     */
    EPaletteSymbol(final String theLabel, final char theCharacter) {

        this.label = theLabel;
        this.character = theCharacter;
    }

    /**
     * Returns the {@code EPaletteSymbol} that has a specified label.
     *
     * @param theLabel the label
     * @return the corresponding {@code EPaletteSymbol}; null if none has the label
     */
    public static EPaletteSymbol forLabel(final String theLabel) {

        EPaletteSymbol result = null;

        for (final EPaletteSymbol test : values()) {
            if (test.label.equals(theLabel)) {
                result = test;
            }
        }

        return result;
    }
}
