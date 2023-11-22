package dev.mathops.app.problem;

import jwabbit.gui.CalculatorPanel;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JPanel;
import javax.swing.TransferHandler;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The base class for panels that present a problem to a student and gather the student's response. This base class
 * manages a list of answer listeners that will be notified when the student's answer changes.
 */
public abstract class AbstractProblemPanelBase extends JPanel
        implements AnswerListener, ComponentListener, MouseListener, MouseMotionListener {

    /** A version code for serialization compatibility checking. */
    @Serial
    private static final long serialVersionUID = 574609642721628216L;

    /** The panel showing the calculator. */
    final CalculatorPanel calculator;

    /** True to display the answers, false otherwise. */
    boolean showAnswers;

    /** True to display the solution, false otherwise. */
    boolean showSolutions;

    /** Listeners for changes to the answer. */
    private final List<AnswerListener> answerListeners;

    /** The first mouse event in recognizing a drag gesture. */
    private MouseEvent firstMouseEvent;

    /**
     * Construct a new {@code ProblemPanelBase}.
     *
     * @param theCalculator the panel showing the calculator
     * @param showAnswer    true to display the answers, false otherwise
     * @param showSolution  true to display the solution, false otherwise
     */
    AbstractProblemPanelBase(final CalculatorPanel theCalculator, final boolean showAnswer,
                             final boolean showSolution) {

        super();

        this.calculator = theCalculator;
        this.showAnswers = showAnswer;
        this.showSolutions = showSolution;

        this.answerListeners = new ArrayList<>(5);

        addComponentListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        setTransferHandler(new ProblemPanelTransferHandler());
    }

    /**
     * Add an answer listener that will be notified when a student's answer is changed or cleared.
     *
     * @param listener the {@code AnswerListener} to add
     */
    public final void addAnswerListener(final AnswerListener listener) {

        this.answerListeners.add(listener);
    }

    /**
     * Removes an answer listener.
     *
     * @param listener the {@code AnswerListener} to remove
     */
    public final void removeAnswerListener(final AnswerListener listener) {

        this.answerListeners.remove(listener);
    }

    /**
     * Record a student's answer.
     *
     * @param answer a list of answer objects, whose type depends on the type of problem for which the answer is being
     *               submitted; the answers will be passed directly into the {@code PresentedProblem} object
     */
    @Override
    public final void recordAnswer(final Object[] answer) {

        // Forward the request to all registered listeners
        for (final AnswerListener listener : this.answerListeners) {
            listener.recordAnswer(answer);
        }
    }

    /**
     * Clear a student's answer.
     */
    @Override
    public final void clearAnswer() {

        // Forward the request to all registered listeners
        for (final AnswerListener listener : this.answerListeners) {
            listener.clearAnswer();
        }
    }

    /**
     * Set the visibility of the entry blocks for students. This is intended to be used as part of the image export
     * feature.
     *
     * @param visible true to make the choices/answer entry box visible, false to hide them
     */
    public abstract void setEntryVisibility(boolean visible);

    /**
     * Set the visibility of the answers.
     *
     * @param visible true to make the answers visible, false to hide them
     */
    public abstract void setAnswerVisibility(boolean visible);

    /**
     * Set the visibility of the solutions.
     *
     * @param visible true to make the solutions visible, false to hide them
     */
    public abstract void setSolutionVisibility(boolean visible);

    /**
     * Export the image as a JPEG file.
     *
     * @param target File the file to write to
     */
    public final void export(final File target) {

        final Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");

        if (iter.hasNext()) {
            final ImageWriter writer = iter.next();

            try (final FileImageOutputStream fios = new FileImageOutputStream(target)) {
                writer.setOutput(fios);

                final BufferedImage img =
                        new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                this.paint(img.getGraphics());

                writer.write(img);
            } catch (final IOException ex) {
                // No action
            }
        }
    }

    /**
     * Get an image of the screen.
     *
     * @return the screen image
     */
    public final BufferedImage getImage() {

        // Build an image of the screen
        final int w = getWidth();
        final int h = getHeight();

        final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        paint(img.getGraphics());

        return img;
    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentHidden(final ComponentEvent e) {

        // No action
    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentMoved(final ComponentEvent e) {

        // No action
    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentResized(final ComponentEvent e) {

        // No action
    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentShown(final ComponentEvent e) {

        // No action
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
     * Implementation of the mouse listener interface.
     *
     * @param e the mouse event
     */
    @Override
    public final void mousePressed(final MouseEvent e) {

        this.firstMouseEvent = e;
    }

    /**
     * Implementation of the mouse listener interface.
     *
     * @param e the mouse event
     */
    @Override
    public final void mouseReleased(final MouseEvent e) {

        this.firstMouseEvent = null;
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
     * Implementation of the mouse motion listener interface.
     *
     * @param e the mouse event
     */
    @Override
    public final void mouseDragged(final MouseEvent e) {

        final int dx;
        final int dy;

        if (this.firstMouseEvent != null) {
            dx = Math.abs(e.getX() - this.firstMouseEvent.getX());
            dy = Math.abs(e.getY() - this.firstMouseEvent.getY());

            // Arbitrarily define a 5-pixel shift as the beginning of a drag.
            if ((dx > 5) || (dy > 5)) {

                // Tell the transfer handler to initiate the drag.
                final TransferHandler handler = getTransferHandler();
                handler.exportAsDrag(this, this.firstMouseEvent, TransferHandler.COPY);
                this.firstMouseEvent = null;

                e.consume();
            }
        }
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

    /**
     * Sets the relative size.
     *
     * @param relSize the size, from -3 to +5.
     */
    public abstract void setRelativeSize(int relSize);
}
