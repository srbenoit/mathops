package dev.mathops.app.eos;

import java.time.LocalDateTime;

/**
 * State associated with a single task in the checklist.
 */
final class TaskState {

    /** The indentation level. */
    final int indent;

    /** The task label. */
    final String label;

    /** The date/time the task was marked as completed. */
    LocalDateTime whenCompleted = null;

    /** The details text. */
    String details = null;

    /** The notes text. */
    String notes = null;

    /**
     * Constructs a new {@code TaskState}.
     *
     * @param theIndent the indentation level
     * @param theLabel  the task label
     */
    TaskState(final int theIndent, final String theLabel) {

        this.indent = theIndent;
        this.label = theLabel;
    }
}
