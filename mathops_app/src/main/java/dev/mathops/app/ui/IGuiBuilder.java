package dev.mathops.app.ui;

import javax.swing.JFrame;

/**
 * An interface implemented by classes that need to construct a GUI in the AWT event thread.
 */
@FunctionalInterface
public interface IGuiBuilder {

    /**
     * Constructs the GUI, to be called in the AWT event thread.
     *
     * @param frame the frame to which to add menus if needed
     */
    void buildUI(JFrame frame);
}
