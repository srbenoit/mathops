package dev.mathops.app.exam;

/**
 * An interface to be implemented by all exam panels.
 */
interface ExamPanelInt {

    /**
     * Selects a problem to present in the current problem panel.
     *
     * @param sectionIndex the index of the section
     * @param problemIndex the index of the problem
     */
    void pickProblem(int sectionIndex, int problemIndex);

    /**
     * Performs a new layout the panel.
     */
    void revalidate();
}
