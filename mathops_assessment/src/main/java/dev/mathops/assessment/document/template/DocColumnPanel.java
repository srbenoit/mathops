package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.log.Log;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serial;

/**
 * A panel designed to host a single {@code DocColumn} object.
 */
public final class DocColumnPanel extends JPanel implements ComponentListener, MouseListener, MouseMotionListener,
        KeyListener, InputChangeListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3889595181186103186L;

    /** The {@code DocColumn} this panel is to render. */
    public final DocColumn column;

    /** The evaluation context. */
    private final EvalContext context;

    /**
     * Construct a new {@code DocColumnPanel}.
     *
     * @param theColumn  the {@code DocColumn} this panel is to render
     * @param theContext the evaluation context
     */
    public DocColumnPanel(final DocColumn theColumn, final EvalContext theContext) {

        super();

        this.column = theColumn;
        this.context = theContext;
        setBackground(Color.WHITE);

        setLayout(new DocColumnLayout(theColumn, theContext));

        addComponentListener(this);
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);

        setFocusCycleRoot(true);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setRequestFocusEnabled(true);

        // Register the panel as the recipient of input change events.
        theColumn.addInputChangeListener(this);
    }

    /**
     * Repaint the window.
     *
     * @param g the {@code Graphics} object to paint to
     */
    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        if (this.column != null) {
            this.column.paintComponent(g, ELayoutMode.TEXT);
        }
    }

    /**
     * Implementation of {@code ComponentListener}.
     *
     * @param e the event
     */
    @Override
    public void componentResized(final ComponentEvent e) {

        if (this.column != null) {
            this.column.setColumnWidth(getWidth());
            this.column.doLayout(this.context, ELayoutMode.TEXT);
            repaint();
        }
    }

    /**
     * Implementation of {@code ComponentListener}.
     *
     * @param e the component event
     */
    @Override
    public void componentHidden(final ComponentEvent e) {

        // No action
    }

    /**
     * Implementation of {@code ComponentListener}.
     *
     * @param e the component event
     */
    @Override
    public void componentShown(final ComponentEvent e) {

        // No action
    }

    /**
     * Implementation of {@code ComponentListener}.
     *
     * @param e the component event
     */
    @Override
    public void componentMoved(final ComponentEvent e) {

        // No action
    }

    /**
     * Implementation of {@code MouseListener}.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        // No action
    }

    /**
     * Implementation of {@code MouseListener}.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        // No action
    }

    /**
     * Implementation of {@code MouseListener}.
     *
     * @param e the mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        if (!this.isEnabled()) {
            return;
        }

        if (this.column.processMousePress(e.getX(), e.getY(), this.context)) {
            repaint();
        }
    }

    /**
     * Implementation of {@code MouseListener}.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        if (!this.isEnabled()) {
            return;
        }

        if (this.column.processMouseRelease(e.getX(), e.getY(), this.context)) {
            repaint();
        }
    }

    /**
     * Implementation of {@code MouseListener}.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        if (!this.isEnabled()) {
            return;
        }

        requestFocusInWindow();

        if (this.column.processMouseClick(e.getX(), e.getY(), e.getClickCount(),
                this.context)) {
            repaint();
        }
    }

    /**
     * Implementation of {@code KeyListener}.
     *
     * @param e the key event
     */
    @Override
    public void keyPressed(final KeyEvent e) {

        if (!this.isEnabled()) {
            return;
        }

        switch (e.getKeyCode()) {

            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_HOME:
            case KeyEvent.VK_END:
            case KeyEvent.VK_TAB:
                if (this.column.processKey(KeyEvent.CHAR_UNDEFINED, e.getKeyCode(),
                        e.getModifiersEx(), this.context)) {
                    repaint();
                }

                break;

            default:
                break;
        }
    }

    /**
     * Implementation of {@code KeyListener}.
     *
     * @param e the key event
     */
    @Override
    public void keyReleased(final KeyEvent e) {

        // No action
    }

    /**
     * Implementation of {@code KeyListener}.
     *
     * @param e the key event
     */
    @Override
    public void keyTyped(final KeyEvent e) {

        if (!this.isEnabled()) {
            return;
        }

        if (this.column.processKey(e.getKeyChar(), e.getKeyCode(), e.getModifiersEx(),
                this.context)) {
            repaint();
        }
    }

    /**
     * Implementation of {@code MouseMotionListener}.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseMoved(final MouseEvent e) {

        // No action
    }

    /**
     * Implementation of {@code MouseMotionListener}.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseDragged(final MouseEvent e) {

        if (!this.isEnabled()) {
            return;
        }

        if (this.column.processMouseDrag(e.getX(), e.getY(), this.context)) {
            repaint();
        }
    }

    /**
     * Indication that an input's value has changed.
     *
     * @param source the input whose value has changed
     */
    @Override
    public void inputChanged(final AbstractDocInput source) {

        this.column.refreshInputs(this.context, false);
        revalidate();
        repaint();
    }
}
