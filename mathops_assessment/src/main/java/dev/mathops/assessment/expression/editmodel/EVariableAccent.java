package dev.mathops.assessment.expression.editmodel;

/**
 * Possible accents that can be added to a variable name.
 */
public enum EVariableAccent {

    /** A grave accent over the variable name. */
    GRAVE('\u0300'),

    /** Aan acute accent over the variable name. */
    ACUTE('\u0301'),

    /** A hat (circumflex) over the variable name. */
    HAT('\u0302'),

    /** A tilde over the variable name. */
    TILDE('\u0303'),

    /** A bar (overline) over the variable name. */
    BAR('\u0305'),

    /** A breve (smile) over the variable name. */
    BREVE('\u0306'),

    /** A dot over the variable name. */
    DOT('\u0307'),

    /** A double dot (diaeresis) over the variable name. */
    DOT2('\u0308'),

    /** A ring over the variable name. */
    RING('\u030A'),

    /** A caron (inverted hat) over the variable name. */
    CARON('\u030C'),

    /** A double bar (overline) over the variable name. */
    BAR2('\u033F'),

    /** A prime attached to the variable name. */
    PRIME('\u2032'),

    /** A double-prime attached to the variable name. */
    PRIME2('\u2033'),

    /** A triple-prime attached to the variable name. */
    PRIME3('\u2034'),

    /** A right-pointing arrow over the variable name. */
    R_ARROW('\u2192'),

    /** A left-pointing arrow over the variable name. */
    L_ARROW('\u2190'),

    /** A left/right-pointing arrow over the variable name. */
    LR_ARROW('\u2194'),

    /** A right-pointing harpoon over the variable name. */
    R_HARPOON('\u21C0'),

    /** A left-pointing harpoon over the variable name. */
    L_HARPOON('\u21BC'),

    /** A left/right-pointing harpoon over the variable name. */
    LR_HARPOON('\u21CB'),

    /** A right/left-pointing harpoon over the variable name. */
    RL_HARPOON('\u21CC');

    /** The character. */
    public final char character;

    /**
     * Constructs a new {@code EVariableAccent}.
     *
     * @param theCharacter the character
     */
    EVariableAccent(final char theCharacter) {

        this.character = theCharacter;
    }
}
