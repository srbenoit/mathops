package jwabbit.log;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import javax.swing.JPanel;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serial;

/**
 * A panel that includes a static logger to which diagnostic messages can be logged, and which provides default empty
 * implementations of {@code MouseListener} and {@code MouseMotionListener}. to reduce code overhead in subclasses that
 * require those functions.
 */
public class LoggedPanel extends JPanel implements MouseListener, MouseMotionListener {

    /** A log to which to write diagnostic messages. */
    protected static final ObjLogger LOG;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -9154069273727941925L;

    static {
        LOG = new ObjLogger();
    }

    /**
     * Constructs a new {@code LoggedPanel}.
     */
    protected LoggedPanel() {

        super();
    }

    /**
     * Constructs a new {@code LoggedPanel} with the specified layout manager.
     *
     * @param layout the {@code LayoutManager} to use
     */
    protected LoggedPanel(final LayoutManager layout) {

        super(layout);
    }

    /**
     * Invoked when the mouse button has been clicked (pressed and released) on a component.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        // No action
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e the mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        // No action
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        // No action
    }

    /**
     * Invoked when the mouse enters a component.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        // No action
    }

    /**
     * Invoked when the mouse exits a component.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        // No action
    }

    /**
     * Invoked when a mouse button is pressed on a component and then dragged. {@code MOUSE_DRAGGED} events will
     * continue to be delivered to the component where the drag originated until the mouse button is released
     * (regardless of whether the mouse position is within the bounds of the component).
     *
     * <p>
     * Due to platform-dependent Drag&Drop implementations, {@code MOUSE_DRAGGED} events may not be delivered during a
     * native Drag&Drop operation.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseDragged(final MouseEvent e) {

        // No action
    }

    /**
     * Invoked when the mouse cursor has been moved onto a component but no buttons have been pushed.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseMoved(final MouseEvent e) {

        // No action
    }
}
