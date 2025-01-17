package dev.mathops.app.ui;

import dev.mathops.commons.log.Log;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * A runnable object whose run method brings a particular frame to the front of the window stacking order.
 */
public class FrameToFront implements Runnable {

    /** The frame that is to be brought to the front. */
    private final JFrame target;

    /** The last time the application repainted its screen. */
    private long nextRepaint;

    /**
     * Constructs a new {@code FrameToFront} object.
     *
     * @param theTarget the frame that is to be brought to the front
     */
    public FrameToFront(final JFrame theTarget) {

        this.target = theTarget;
        this.nextRepaint = System.currentTimeMillis();
    }

    /**
     * Brings the target frame to the front of the stacking order.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            final String msg = Res.get(Res.NOT_AWT_THREAD);
            Log.warning(msg);
        }

        // this.target.toFront();

        if (System.currentTimeMillis() >= this.nextRepaint) {
            this.target.repaint();
            this.nextRepaint = System.currentTimeMillis() + 2000L;
        }
    }
}
