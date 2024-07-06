package dev.mathops.assessment.expression.editview;

import dev.mathops.assessment.expression.editmodel.Expr;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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
    private final Expr expr;

    /** The minimum size for the panel. */
    private final Dimension minSize;

    /** The initial font size. */
    private final float initialFontSize;

    /** The minimum font size. */
    private final float minFontSize;

    /** The laid out expression. */
    private ExprBox laidOutExpression;

    /**
     * Constructs a new {@code ExpressionViewPanel}.
     *
     * @param theExpr            the model this panel will display
     * @param theMinSize         the minimum size for the panel
     * @param theInitialFontSize the initial font size
     */
    public ExpressionViewPanel(final Expr theExpr, final Dimension theMinSize, final float theInitialFontSize,
                               final float theMinFontSize) {

        super();

        this.expr = theExpr;

        this.minSize = new Dimension(theMinSize);
        setPreferredSize(theMinSize);

        this.initialFontSize = theInitialFontSize;
        this.minFontSize = theMinFontSize;

        updateExpression();
    }

    /**
     * Updates the expression.
     */
    public void updateExpression() {

        this.laidOutExpression = new ExprBox(this.expr, this.initialFontSize, this.minFontSize);
        this.laidOutExpression.x = 0;
        this.laidOutExpression.y = this.laidOutExpression.top;
        repaint();
    }

    /**
     * Paints the component.
     *
     * @param g the {@code Graphics} to which to draw
     */
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        if (g instanceof final Graphics2D g2d && this.laidOutExpression != null) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

            this.laidOutExpression.paint(g2d);
        }
    }
}
