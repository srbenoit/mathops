package dev.mathops.assessment.expression.editmodel;

import java.util.ArrayList;
import java.util.List;

/**
 *  An expression model which consists of a top-level {@code ExprGlyphSequence} and a cursor position, and an optional
 *  selection anchor.
 */
public final class ExpressionModel {

    /** The top-level glyph sequence. */
    private final Expr sequence;

    /** Listeners to notify when the model changes. */
    private final List<IExpressionModelListener> listeners;

    /**
     * Constructs a new {@code ExpressionModel}.
     */
    public ExpressionModel() {

        this.sequence = new Expr();

        this.listeners = new ArrayList<>(3);
    }

    /**
     * Adds a listener that will be notified when the model changes.
     *
     * @param theListener the listener to add
     */
    public void addListener(final IExpressionModelListener theListener) {

        this.listeners.add(theListener);
    }

    /**
     * Removes a listener that was registered previously with {@code addListener}.
     *
     * @param theListener the listener to remove
     */
    public void removeListener(final IExpressionModelListener theListener) {

        this.listeners.remove(theListener);
    }

    /**
     * Sends a change notification to all registered listeners.
     */
    public void fireChange() {

        for (final IExpressionModelListener listener : this.listeners) {
            listener.modelChanged(this);
        }
    }
}
