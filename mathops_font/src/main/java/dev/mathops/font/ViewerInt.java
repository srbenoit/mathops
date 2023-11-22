package dev.mathops.font;

/**
 * An interface for viewers that have a scroll pane.
 */
@FunctionalInterface
interface ViewerInt {

    /**
     * Tells the scroll pane that something inside it has changed.
     *
     * @param jump the vertical size of boxes
     */
    void updateScroller(int jump);
}
