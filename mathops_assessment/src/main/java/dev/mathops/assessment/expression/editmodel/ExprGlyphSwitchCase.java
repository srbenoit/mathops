package dev.mathops.assessment.expression.editmodel;

/**
 * A single case within a switch operation.
 */
public class ExprGlyphSwitchCase extends AbstractExprGlyph {

    /** The integer case value. */
    public final int caseValue;

    /** The resulting value. */
    public final ExprGlyphSequence result;

    /**
     * Constructs a new {@code ExprGlyphSwitchCase}.
     *
     * @param theCaseValue the case value
     */
    public ExprGlyphSwitchCase(final int theCaseValue) {

        super();

        this.caseValue = theCaseValue;
        this.result = new ExprGlyphSequence();
    }
}
