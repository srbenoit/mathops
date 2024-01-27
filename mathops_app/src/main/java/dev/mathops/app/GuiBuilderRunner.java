package dev.mathops.app;

import dev.mathops.commons.log.Log;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

/**
 * A class to execute a {@code GuiBuilder} from the AWT event thread.
 */
public final class GuiBuilderRunner implements Runnable {

    /** The {@code GuiBuilder} whose build method is to be called. */
    private final IGuiBuilder owner;

    /** The frame to which to add menus if needed. */
    private JFrame frame;

    /**
     * Constructs a new {@code GuiBuilderRunner}.
     *
     * @param theOwner the {@code GuiBuilder} whose build method is to be called
     */
    public GuiBuilderRunner(final IGuiBuilder theOwner) {

        this.owner = theOwner;
    }

    /**
     * Execute the {@code buildUI} method on the owner object in the AWT event thread, waiting for that to complete
     * before returning.
     *
     * @param menuFrame the frame to which to add menus if needed
     */
    public void buildUI(final JFrame menuFrame) {

        this.frame = menuFrame;

        if (SwingUtilities.isEventDispatchThread()) {
            run();
        } else {

            try {
                SwingUtilities.invokeAndWait(this);
            } catch (final InvocationTargetException ex1) {
                Log.warning(ex1);
            } catch (final InterruptedException ex2) {
                Log.warning(ex2);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Runnable method to call the owner's {@code buildUI} method from within the AWT event dispatcher thread.
     */
    @Override
    public void run() {

        this.owner.buildUI(this.frame);
    }
}
