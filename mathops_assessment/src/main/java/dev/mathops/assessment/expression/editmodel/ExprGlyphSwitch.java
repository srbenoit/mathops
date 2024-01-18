package dev.mathops.assessment.expression.editmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * A switch operation.
 */
public class ExprGlyphSwitch extends AbstractExprGlyph {

    /** The resulting value. */
    public final ExprGlyphSequence condition;

    /** The cases. */
    public final List<ExprGlyphSwitchCase> cases;

    /** The default result if no cases match. */
    public final ExprGlyphSequence defaultResult;

    /**
     * Constructs a new {@code ExprGlyphSwitch}.
     */
    public ExprGlyphSwitch() {

        super();

        this.condition = new ExprGlyphSequence();
        this.cases = new ArrayList<> (10);
        this.defaultResult = new ExprGlyphSequence();
    }
}
