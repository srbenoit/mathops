package dev.mathops.app.exam;

import javax.swing.JFrame;
import java.awt.event.ActionListener;

/**
 * An interface to be implemented by all exam containers.
 */
public interface ExamContainerInt extends ActionListener {

    /**
     * Called when a timer expires.
     */
    void timerExpired();

    /**
     * Selects a problem to present in the current problem panel.
     *
     * @param sectionIndex the index of the section
     * @param problemIndex the index of the problem
     */
    void pickProblem(int sectionIndex, int problemIndex);

    /**
     * Writes the current exam state to disk.
     */
    void doCacheExamState();

    /**
     * Gets the {@code JFrame}.
     *
     * @return the frame
     */
    JFrame getFrame();
}
