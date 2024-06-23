package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.Serial;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;

/**
 * A panel that allows the user to edit a formula.
 */
public final class FormulaEditorPanel extends JPanel
        implements KeyListener, MouseListener, MouseMotionListener, FocusListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -6130745458112651264L;

    /** The maximum number of undo levels to store. */
    private static final int MAX_UNDO_LEVELS = 100;

    /** The outer color for a focused border. */
    private static final Color NORMAL_BORDER_COLOR = new Color(194, 194, 194);

    /** The outer color for a focused border. */
    private static final Color FOCUSED_OUTER_COLOR = new Color(137, 176, 212);

    /** The inner color for a focused border. */
    private static final Color FOCUSED_INNER_COLOR = new Color(204, 225, 245);

    /** The font size. */
    private final int fontSize;

    /** The minimum width. */
    private int minWidth;

    /** The insets. */
    private final Insets insets;

    /** The formula data model container. */
    private FEFormula root;

    /** The cursor. */
    private FECursor cursor;

    /** The undo stack. */
    private final Deque<UndoRedoState> undo;

    /** The redo stack. */
    private final Deque<UndoRedoState> redo;

    /**
     * Constructs a new {@code FormulaEditorPanel}.
     *
     * @param theFontSize the font size
     * @param theInsets   the insets for the contained formula UI object
     * @param theTypes    the types of formula this panel will edit; null to allow all types
     */
    public FormulaEditorPanel(final int theFontSize, final Insets theInsets,
                              final EType... theTypes) {

        super();

        this.minWidth = 30;

        final Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        setBorder(emptyBorder);

        setBackground(Color.WHITE);
        this.cursor = new FECursor();
        this.fontSize = theFontSize;
        this.insets = theInsets;

        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        addFocusListener(this);

        this.undo = new ArrayDeque<>(10);
        this.redo = new ArrayDeque<>(10);

        this.root = new FEFormula(theFontSize, this, theInsets);

        final Dimension size = this.root.layoutFormula();
        if (size.width < this.minWidth) {
            size.width = this.minWidth;
        }
        setPreferredSize(size);

        final EnumSet<EType> specified = EnumSet.noneOf(EType.class);
        if (theTypes == null || theTypes.length == 0) {
            specified.add(EType.BOOLEAN);
            specified.add(EType.INTEGER);
            specified.add(EType.REAL);
            specified.add(EType.INTEGER_VECTOR);
            specified.add(EType.REAL_VECTOR);
            specified.add(EType.STRING);
            specified.add(EType.SPAN);
        } else {
            final List<EType> typesList = Arrays.asList(theTypes);
            specified.addAll(typesList);
        }

        final EnumSet<EType> allowed = this.root.getAllowedTypes();
        final EnumSet<EType> possible = this.root.getPossibleTypes();
        allowed.addAll(specified);
        possible.addAll(specified);
    }

    /**
     * Constructs a new {@code FormulaEditorPanel}.
     *
     * @param theFontSize  the font size for the generated object
     * @param theInsets    the insets for the contained formula UI object
     * @param initialValue the initial formula to display
     * @param evalContext  an evaluation context with variable names from which to select
     * @param theTypes     the types of formula this panel will edit; null to allow all types
     */
    public FormulaEditorPanel(final int theFontSize, final Insets theInsets,
                              final Formula initialValue, final EvalContext evalContext, final EType... theTypes) {

        this(theFontSize, theInsets, theTypes);

        setFormula(initialValue == null ? null : new NumberOrFormula(initialValue));
    }

    /**
     * Sets the minimum width.
     *
     * @param theMinWidth the new minimum width
     */
    public void setMinWidth(final int theMinWidth) {

        this.minWidth = theMinWidth;

        boolean change = false;

        final Dimension pref = getPreferredSize();
        if (pref.width < this.minWidth) {
            pref.width = this.minWidth;
            setPreferredSize(pref);
            change = true;
        }

        final Dimension size = getSize();
        if (size.width < this.minWidth) {
            size.width = this.minWidth;
            setSize(size);
            change = true;
        }

        if (change) {
            revalidate();
            repaint();
        }
    }

    /**
     * Gets the formula data model container.
     *
     * @return the root container
     */
    public FEFormula getRoot() {

        return this.root;
    }

    /**
     * Sets the formula. This clears and recomputes the formula edit tree.
     *
     * @param theFormula the formula
     */
    public void setFormula(final NumberOrFormula theFormula) {

        // Root is never null - we're about to replace it, so preserve its allowed/possible lists
        final EnumSet<EType> allowed = this.root.getAllowedTypes();
        final EnumSet<EType> possible = this.root.getPossibleTypes();

        if (theFormula == null) {
            this.root = new FEFormula(this.fontSize, this, this.insets);
            this.root.getAllowedTypes().addAll(allowed);
            this.root.getPossibleTypes().addAll(possible);
        } else if (theFormula.getFormula() != null) {
            this.root = theFormula.getFormula().generateFEFormula(this.fontSize, this, this.insets);
            this.root.getAllowedTypes().addAll(allowed);
            final EnumSet<EType> newPossible = this.root.getPossibleTypes();
            newPossible.addAll(possible);

            final EType rootType = this.root.getCurrentType();

            final boolean mismatch;
            if (rootType == null) {
                final EnumSet<EType> filtered = EType.filter(allowed, newPossible);
                mismatch = filtered.isEmpty();
            } else {
                mismatch = !allowed.contains(rootType);
            }

            if (mismatch) {
                throw new IllegalArgumentException("Specified type(s) not compatible with type (" + rootType +
                        ") of given formula");
            }
        }

        final Dimension size = this.root.layoutFormula();
        if (size.width < this.minWidth) {
            size.width = this.minWidth;
        }

        setPreferredSize(size);
        setSize(size);
        revalidate();
        repaint();
    }

    /**
     * Sets the enabled state of the panel.
     *
     * @param enabled {@code true} to enable; {@code false} to disable
     */
    @Override
    public void setEnabled(final boolean enabled) {

        super.setEnabled(enabled);

        setBackground(enabled ? FEFormula.ENABLED_BG : FEFormula.DISABLED_BG);
    }

    /**
     * Adds a listener.
     *
     * @param theListener the listener
     */
    public void addListener(final IFormulaEditorListener theListener) {

        this.root.addListener(theListener);
    }

    /**
     * Paints the component.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        final boolean hasFocus = hasFocus();

        final Color bg = isEnabled() ? getBackground() : FEFormula.DISABLED_BG;
        this.root.render(this.cursor, hasFocus, bg);
        this.root.paint(g);

        final Dimension size = getSize();

        if (hasFocus) {
            g.setColor(FOCUSED_INNER_COLOR);
            g.drawRoundRect(1, 1, size.width - 3, size.height - 3, 2, 2);
            g.setColor(FOCUSED_OUTER_COLOR);
        } else {
            g.setColor(NORMAL_BORDER_COLOR);
        }
        g.drawRoundRect(0, 0, size.width - 1, size.height - 1, 2, 2);
    }

    /**
     * Called when the mouse is dragged in the panel.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseDragged(final MouseEvent e) {

        // TODO Auto-generated method stub
    }

    /**
     * Called when the mouse is moved in the panel.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseMoved(final MouseEvent e) {

        // TODO Auto-generated method stub
    }

    /**
     * Called when the mouse is clicked in the panel.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        // TODO Auto-generated method stub
    }

    /**
     * Called when the mouse is pressed in the panel.
     *
     * @param e the mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        requestFocus();
    }

    /**
     * Called when the mouse is released in the panel.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        // TODO Auto-generated method stub
    }

    /**
     * Called when the mouse enters the panel.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        // TODO Auto-generated method stub
    }

    /**
     * Called when the mouse exits the panel.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        // TODO Auto-generated method stub
    }

    /**
     * Called when a key is typed when the panel has focus.
     *
     * @param e the key event
     */
    @Override
    public void keyTyped(final KeyEvent e) {

        final int modifiers = e.getModifiersEx();
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) == 0) {
            final char ch = e.getKeyChar();
            this.root.processChar(this.cursor, ch);
            e.consume();
        }
    }

    /**
     * Called when a key is pressed when the panel has focus.
     *
     * @param e the key event
     */
    @Override
    public void keyPressed(final KeyEvent e) {

        final int modifiers = e.getModifiersEx();
        final int code = e.getKeyCode();
        final int numSteps = this.root.getNumCursorSteps();

        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK) {
            if (code == KeyEvent.VK_LEFT) {
                // Add the prior character to the selection
                if (this.cursor.cursorPosition > 0) {
                    if (this.cursor.selectionStart == -1) {
                        this.cursor.selectionStart = this.cursor.cursorPosition;
                    }
                    --this.cursor.cursorPosition;
                    this.root.update(false);
                    e.consume();
                }
            } else if (code == KeyEvent.VK_RIGHT) {
                // Add the prior character to the selection
                if (this.cursor.cursorPosition < numSteps) {
                    if (this.cursor.selectionStart == -1) {
                        this.cursor.selectionStart = this.cursor.cursorPosition;
                    }
                    ++this.cursor.cursorPosition;
                    this.root.update(false);
                    e.consume();
                }
            } else if (code == KeyEvent.VK_HOME) {
                // Add the from current position to start of text to the selection
                if (this.cursor.cursorPosition > 0) {
                    if (this.cursor.selectionStart == -1) {
                        this.cursor.selectionStart = this.cursor.cursorPosition;
                    }
                    this.cursor.cursorPosition = 0;
                    this.root.update(false);
                    e.consume();
                }
            } else // Add the from current position to end of text to the selection
                if ((code == KeyEvent.VK_END) && (this.cursor.cursorPosition < numSteps)) {
                    if (this.cursor.selectionStart == -1) {
                        this.cursor.selectionStart = this.cursor.cursorPosition;
                    }
                    this.cursor.cursorPosition = numSteps;
                    this.root.update(false);
                    e.consume();
                }
        } else if ((modifiers & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {

            if (code == KeyEvent.VK_Z) {
                // Undo
                undo();
                e.consume();
            } else if (code == KeyEvent.VK_Y) {
                // Redo
                redo();
                e.consume();
            } else if (code == KeyEvent.VK_C) {
                // Copy
                copy();
                e.consume();
            }

        } else if (code == KeyEvent.VK_LEFT) {
            // Move cursor left
            this.cursor.selectionStart = -1;
            if (this.cursor.cursorPosition > 0) {
                --this.cursor.cursorPosition;
            }
            this.root.update(false);
            e.consume();
        } else if (code == KeyEvent.VK_RIGHT) {
            // Move cursor right
            this.cursor.selectionStart = -1;
            if (this.cursor.cursorPosition < numSteps) {
                ++this.cursor.cursorPosition;
            }
            this.root.update(false);
            e.consume();
        } else if (code == KeyEvent.VK_HOME) {
            // Move cursor to start
            this.cursor.selectionStart = -1;
            if (this.cursor.cursorPosition > 0) {
                this.cursor.cursorPosition = 0;
            }
            this.root.update(false);
            e.consume();
        } else if (code == KeyEvent.VK_END) {
            // Move cursor to end
            this.cursor.selectionStart = -1;
            if (this.cursor.cursorPosition < numSteps) {
                this.cursor.cursorPosition = numSteps;
            }
            this.root.update(false);
            e.consume();
        }

        // TODO: Add handling of "Up" and "Down" arrows in contexts where that makes sense
    }

    /**
     * Called when a key is released when the panel has focus.
     *
     * @param e the key event
     */
    @Override
    public void keyReleased(final KeyEvent e) {

        // TODO Auto-generated method stub
    }

    /**
     * Called when focus is gained.
     *
     * @param e the focus event
     */
    @Override
    public void focusGained(final FocusEvent e) {

        repaint();
    }

    /**
     * Called when focus is lost.
     *
     * @param e the focus event
     */
    @Override
    public void focusLost(final FocusEvent e) {

        repaint();
    }

    /**
     * Simulates the typing of a character. Used to allow a palette to provide buttons for characters that don't appear
     * on standard keyboards, like the Pi symbol, or to type the character representations for built-in functions.
     *
     * @param ch the character to type
     */
    public void typeChar(final char ch) {

        this.root.processChar(this.cursor, ch);
    }

    /**
     * Stores state for an "undo" operation.
     */
    public void storeUndo() {

        final UndoRedoState state = new UndoRedoState(this.root, this.cursor);

        // final HtmlBuilder htm = new HtmlBuilder(100);
        // this.root.emitDiagnostics(htm, 0);
        // Log.info("Storing UNDO state: " + htm.toString());

        if (this.undo.size() >= MAX_UNDO_LEVELS) {
            this.undo.removeLast();
        }
        this.undo.push(state);

        // Storing a new undo purges redo steps
        this.redo.clear();
    }

    /**
     * Performs an "undo" operation.
     */
    private void undo() {

        // The top-most entry on the "undo" stack is the most recent state, so move it to the redo
        // stack before we do anything

        Log.info("Undo");

        // The first entry is always our initial state - don't delete that
        if (this.undo.size() > 1) {
            final UndoRedoState curState = this.undo.poll();
            if (curState != null) {
                this.redo.push(curState);

                final HtmlBuilder htm1 = new HtmlBuilder(100);
                curState.formula.emitDiagnostics(htm1, 0);
                Log.info("State moved to REDO: ", htm1);

                final UndoRedoState priorState = this.undo.peek();
                if (priorState != null) {

                    final HtmlBuilder htm2 = new HtmlBuilder(100);
                    priorState.formula.emitDiagnostics(htm2, 0);
                    Log.info("PEEKED UNDO state: ", htm2);

                    this.root = priorState.formula.duplicate();
                    this.cursor = priorState.cursor.duplicate();

                    final HtmlBuilder htm3 = new HtmlBuilder(100);
                    this.root.emitDiagnostics(htm3, 0);
                    Log.info("Undo restored state: ", htm3);

                    this.root.update(false);
                }
            }
        }
    }

    /**
     * Performs a "redo" operation.
     */
    private void redo() {

        Log.info("Redo");

        final UndoRedoState toRestore = this.redo.poll();
        if (toRestore != null) {
            this.undo.push(toRestore);
            this.root = toRestore.formula.duplicate();
            this.cursor = toRestore.cursor.duplicate();
            this.root.update(false);
        }
    }

    /**
     * Performs a "copy" operation.
     */
    private void copy() {

        final String text = this.root.getText();

        final Transferable stringSelection = new StringSelection(text);
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }
}
