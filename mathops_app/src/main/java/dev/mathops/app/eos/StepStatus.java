package dev.mathops.app.eos;

/**
 * A container for status from a Swing worker executing a single step in the procedure.
 *
 * @param percentComplete the percentage complete (from 0 to 100)
 * @param currentTask     the current task
 */
public record StepStatus(int percentComplete, String currentTask) {
}
