package dev.mathops.assessment.expression.editview;

import dev.mathops.assessment.expression.editmodel.Expr;

import javax.swing.JPanel;
import java.awt.Dimension;

/**
 * A panel that displays an expression and updates itself as the expression is changed.  This class displays the
 * expression as well as the cursor position and selection, and can accept keyboard and mouse input and map mouse
 * locations to glyphs, and passes those inputs to the controller.
 *
 * <p>
 * This panel will dynamically update its preferred size based on the rendered expression, and on some minimum size
 * provided to its constructor.  It will invalidate itself if the rendered expression requires a new size.  If the panel
 * size is smaller than the expression requires, the expression will be elided with an ellipsis where clipped, but the
 * cursor position will always be visible.
 */
public final class ExpressionViewPanel extends JPanel {

    /** The expression this panel will display. */
    private Expr expr;

    /** The minimum size for the panel. */
    private final Dimension minSize;

    /** The initial font size. */
    private final float initialFontSize;

    /** The laid out view. */
    private ExprObjectView view;

    /**
     * Constructs a new {@code ExpressionViewPanel}.
     *
     * @param theExpr            the model this panel will display
     * @param theMinSize         the minimum size for the panel
     * @param theInitialFontSize the initial font size
     */
    public ExpressionViewPanel(final Expr theExpr, final Dimension theMinSize, final float theInitialFontSize) {

        super();

        this.minSize = new Dimension(theMinSize);
        this.initialFontSize = theInitialFontSize;

        updateExpression(theExpr);
    }

    /**
     * Updates the expression.
     *
     * @param theExpr the expression
     */
    void updateExpression(final Expr theExpr) {

        this.expr = theExpr;

        final float minFontSize = Math.max(7.0f, this.initialFontSize * 25.0f / 100.0f);

        this.view = LayoutEngine.layoutExpr(this.initialFontSize, minFontSize, this.expr);
    }
}
