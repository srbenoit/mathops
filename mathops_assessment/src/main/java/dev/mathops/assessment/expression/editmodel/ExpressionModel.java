package dev.mathops.assessment.expression.editmodel;

import java.util.ArrayList;
import java.util.List;

/**
 *  An expression model which consists of a top-level {@code ExprGlyphSequence} and a cursor position, and an optional
 *  selection anchor.
 */
public final class ExpressionModel {

    /** The top-level glyph sequence. */
    private final ExprGlyphSequence sequence;

    /** The cursor position. */
    private int cursorPos;

    /** The selection anchor (equal to the cursor position if no glyphs are selected). */
    private int selectionAnchor;

    /** Listeners to notify when the model changes. */
    private final List<IExpressionModelListener> listeners;

    /**
     * Constructs a new {@code ExpressionModel}.
     */
    public ExpressionModel() {

        this.sequence = new ExprGlyphSequence();
        this.cursorPos = 0;
        this.selectionAnchor = 0;

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
