package dev.mathops.assessment.expression.editmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * An expression (or sub-expression) modeled as a sequence of expression objects.
 */
public final class Expr extends AbstractExprObject {

    /** The list of objects in the sequence. */
    private final List<AbstractExprObject> glyphs;

    /**
     * Constructs a new {@code Expr}.
     */
    Expr() {

        super();

        this.glyphs = new ArrayList<>(10);
    }

    /**
     * Processes an action represented by an integer.  If the action code is 0xFFFF or smaller, it is interpreted
     * as a Unicode character;  otherwise, it is interpreted as an enumerated code.
     *
     * <p>
     * If there is a selection and an action is performed that would result in the deletion of that selection, the
     * deletion is done before this method is called.  Actions on objects are called only when there is no selection
     * region.
     *
     * @param action the action code
     * @param cursorPosition the cursor position
     */
    void processAction(final int action, final int cursorPosition) {

    }
}
