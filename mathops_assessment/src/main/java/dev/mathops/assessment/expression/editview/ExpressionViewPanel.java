package dev.mathops.assessment.expression.editview;

import dev.mathops.assessment.expression.editmodel.ExpressionModel;
import dev.mathops.assessment.expression.editmodel.IExpressionModelListener;

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
public final class ExpressionViewPanel extends JPanel implements IExpressionModelListener {

    /** The model this panel will display. */
    private final ExpressionModel model;

    /** The minimum size for the panel. */
    private final Dimension minSize;

    /** The initial font size. */
    private final float initialFontSize;

    /**
     * Constructs a new {@code ExpressionViewPanel}.
     *
     * @param theModel           the model this panel will display
     * @param theMinSize         the minimum size for the panel
     * @param theInitialFontSize the initial font size
     */
    public ExpressionViewPanel(final ExpressionModel theModel, final Dimension theMinSize,
                               final float theInitialFontSize) {

        super();

        this.model = theModel;
        this.minSize = new Dimension(theMinSize);
        this.initialFontSize = theInitialFontSize;

        theModel.addListener(this);
    }

    /**
     * Called when the panel will no longer be used.
     */
    public void close() {

        this.model.removeListener(this);
    }

    /**
     * Called when the model changes.
     *
     * @param theModel the model that has changed
     */
    @Override
    public void modelChanged(final ExpressionModel theModel) {

        // TODO:
    }
}
