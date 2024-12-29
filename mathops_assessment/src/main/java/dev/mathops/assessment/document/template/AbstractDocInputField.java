package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.EFieldStyle;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.font.BundledFontManager;
import dev.mathops.text.builder.HtmlBuilder;

import java.awt.FontMetrics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A document object that supports the entry of a value into a field. Different field types are implemented as
 * subclasses, each with their own validation rules. Input fields allow definition of all document formatting
 * characteristics. Fields are drawn in a shaded outline, which highlights when the object is selected. Selecting the
 * field shows a text edit caret and enabled editing.
 */
abstract class AbstractDocInputField extends AbstractDocInput {

    /** The top inset. */
    static final int INSET_TOP = 2;

    /** The bottom inset. */
    static final int INSET_BOTTOM = 0;

    /** The left inset. */
    static final int INSET_LEFT = 5;

    /** The right inset. */
    private static final int INSET_RIGHT = 6;

    /** The top margin. */
    static final int MARGIN_TOP = 2;

    /** The bottom margin. */
    static final int MARGIN_BOTTOM = 2;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7064160897787530441L;

    /** The cut/paste clipboard. */
    private static Object clipboard;

    /** The actual entered string representation of the field contents. */
    private String text;

    /** The position at the right end of each character. */
    private int[] charPositions;

    /** The field style. */
    public EFieldStyle style;

    /** The current position of the edit caret. */
    int caret;

    /** The index of the selection start. */
    int selectStart = -1;

    /** The index of the selection end. */
    int selectEnd = -1;

    /** 0 = no drag, 1 = drag starting, 2 = drag continuing. */
    private int ownsDrag;

    /** History of values for undo/redo. */
    private final ArrayList<String> history;

    /** The position of the current value in the undo/redo history. */
    private int historyPos;

    /**
     * Construct a new {@code AbstractJDocInputField}.
     *
     * @param theName the name of the input's value in the parameter set
     */
    AbstractDocInputField(final String theName) {

        super(theName);

        this.history = new ArrayList<>(1);
        this.history.add(this.text);
        this.historyPos = 0;
        this.style = EFieldStyle.BOX;
        this.text = CoreConstants.EMPTY;
    }

    /**
     * Set the selected state of the input.
     *
     * @param isSelected true if the input is selected; false otherwise
     */
    @Override
    public final void setSelected(final boolean isSelected) {

        // Log.info(getClass().getName() + " setting selected = " + isSelected, new Exception());

        super.setSelected(isSelected);
        this.selectStart = -1;
        this.selectEnd = -1;
    }

    /**
     * Set the enabled formula for the input.
     *
     * @param theEnabledFormula the formula to evaluate to determine whether this input is enabled
     */
    @Override
    public final void setEnabledFormula(final Formula theEnabledFormula) {

        super.setEnabledFormula(theEnabledFormula);
        this.selectStart = -1;
        this.selectEnd = -1;
    }

    /**
     * Get the text contents of the field. This is retained in addition to the number value, since the user can enter
     * the same numeric value several ways ("100", "1E+2", "100.0", etc.)
     *
     * @return the text value of the field
     */
    final String getTextValue() {
        synchronized (this) {

            return this.text;
        }
    }

    /**
     * Set the value of the field to a particular text string, parsing it into either an integer or double value.
     *
     * @param theText the text to which to set the input's value
     * @return true if the value was a valid number; false otherwise
     */
    boolean setTextValue(final String theText) {
        synchronized (this) {

            this.charPositions = null;
            this.selectStart = -1;
            this.caret = 0;

            if (theText != null) {
                this.text = theText;
                this.caret = theText.length();
            } else {
                this.text = CoreConstants.EMPTY;
            }

            storeValue(this.text);

            if (getEvalContext() != null) {
                doLayout(getEvalContext(), ELayoutMode.TEXT);
            }

            return true;
        }
    }

    /**
     * Sets the text value.
     *
     * @param theText the text to which to set the input's value
     */
    final void innerSetTextValue(final String theText) {

        this.text = theText;
    }

    /**
     * Get the character positions.
     *
     * @return the character positions.
     */
    final int[] getCharPositions() {

        return this.charPositions;
    }

    /**
     * Set the character positions.
     *
     * @param theCharPositions the character positions.
     */
    final void setCharPositions(final int[] theCharPositions) {

        this.charPositions = theCharPositions;
    }

    /**
     * Recompute the size of the object's bounding box.
     *
     * @param context the evaluation context
     */
    @Override
    public final void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        final FontMetrics fm = BundledFontManager.getInstance().getFontMetrics(getFont());
        final int h = fm.getAscent() + fm.getDescent() + MARGIN_TOP + INSET_TOP + INSET_BOTTOM
                + MARGIN_BOTTOM;
        int w;

        final int minWidth = context.isPrintTarget() ? 40 : 20;

        final String actual = this.text.replace('-', '\u2212');

        w = Math.max(minWidth, fm.stringWidth(actual)) + INSET_LEFT + INSET_RIGHT;
        setBaseLine(MARGIN_TOP + INSET_TOP + fm.getAscent());
        setCenterLine(MARGIN_TOP + INSET_TOP + ((fm.getAscent() << 1) / 3));
        this.charPositions = new int[actual.length() + 1];
        this.charPositions[0] = 0;

        for (int i = 1; i <= this.text.length(); ++i) {
            this.charPositions[i] = fm.stringWidth(actual.substring(0, i));
        }

        if (isBoxed()) {
            w += 4; // Allow 2 pixels on either end for box
        }

        setWidth(w);
        setHeight(h);
    }

    /**
     * Generate a string representation of the input value (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public final String toString() {

        final HtmlBuilder builder = new HtmlBuilder(50);

        builder.add("{", getName(), "}=");
        builder.add(this.text);

        return builder.toString();
    }

    /**
     * Handler for mouse clicks. Mouse clicks are propagated to all children, with the coordinates being adjusted to the
     * child's frame. Input objects should gain/lose focus based on clicks, and other reactive objects can respond to
     * them as needed.
     *
     * @param xCoord     the X coordinate (in the object's coordinate system)
     * @param yCoord     the Y coordinate (in the object's coordinate system)
     * @param clickCount 1 for single click, 2 for double-click, and so on
     * @return true if a change requiring repaint occurred
     */
    @Override
    public final boolean processMouseClick(final int xCoord, final int yCoord, final int clickCount,
                                           final EvalContext context) {

        boolean repaint = false;

        // Handle double-clicks to select entire field.
        if (clickCount == 2 && isEnabled() && xCoord >= 0 && xCoord < getWidth() && yCoord >= 0
                && yCoord < getHeight()) {

            this.selectStart = 0;
            this.selectEnd = this.text.length();
            this.caret = this.selectEnd;
            repaint = true;
        }

        // Forward mouse click to all children
        if (super.processMouseClick(xCoord, yCoord, clickCount, context)) {
            repaint = true;
        }

        return repaint;
    }

    /**
     * Handler for mouse press actions. Mouse presses are propagated to all children, with the coordinates being
     * adjusted to the child's frame. This event is primarily used to detect the beginning of drag sequences.
     *
     * @param xCoord the X coordinate (in the object's coordinate system)
     * @param yCoord the Y coordinate (in the object's coordinate system)
     * @return true if a change requiring repaint occurred
     */
    @Override
    public final boolean processMousePress(final int xCoord, final int yCoord, final EvalContext context) {

        boolean repaint = false;
        int newCaret;
        int i;
        int mid;
        final int newX;

        if (isEnabled()) {
            if (xCoord >= 0 && xCoord < getWidth() && yCoord >= 0 && yCoord < getHeight()) {
                this.ownsDrag = 1;

                if (!isSelected()) {
                    setSelected(true);
                    this.caret = this.text.length();
                    repaint = true;
                } else {
                    // Already selected, so press can move caret
                    newCaret = this.text.length();
                    newX = xCoord - INSET_LEFT;

                    if (this.charPositions != null) {
                        final int len = this.charPositions.length;
                        for (i = 1; i < len; ++i) {
                            mid = (this.charPositions[i - 1] + this.charPositions[i]) / 2;

                            if (newX <= mid) {
                                newCaret = i - 1;

                                break;
                            }
                        }
                    }

                    if (newCaret != this.caret) {
                        this.caret = newCaret;
                        repaint = true;
                    }

                    this.selectStart = -1;
                    this.selectEnd = -1;
                }
            } else if (isSelected()) {
                this.ownsDrag = 0;
                setSelected(false);
                this.selectStart = -1;
                this.selectEnd = -1;
                repaint = true;
            } else {
                this.ownsDrag = 0;
            }
        } else {
            this.ownsDrag = 0;
        }

        // Forward mouse click to all children
        if (super.processMousePress(xCoord, yCoord, context)) {
            repaint = true;
        }

        return repaint;
    }

    /**
     * Handler for mouse release actions. Mouse releases are propagated to all children, with the coordinates being
     * adjusted to the child's frame. This event is primarily used to detect the end of drag sequences.
     *
     * @param xCoord the X coordinate (in the object's coordinate system)
     * @param yCoord the Y coordinate (in the object's coordinate system)
     * @return true if a change requiring repaint occurred
     */
    @Override
    public final boolean processMouseRelease(final int xCoord, final int yCoord, final EvalContext context) {

        boolean repaint = false;

        this.ownsDrag = 0;

        // Forward mouse click to all children
        if (super.processMouseRelease(xCoord, yCoord, context)) {
            repaint = true;
        }

        return repaint;
    }

    /**
     * Handler for key presses. Keys are propagated to all children, but only children who have focus should react to
     * them.
     *
     * @param keyChar   the key character
     * @param keyCode   the key code
     * @param modifiers mModifiers (CTRL, ALT, SHIFT, etc.) to the key press
     * @return true if a change requiring repaint occurred
     */
    @Override
    public boolean processKey(final char keyChar, final int keyCode, final int modifiers, final EvalContext context) {

        boolean repaint = false;
        final String txt;
        final int theCaret;
        final String valid = getValidCharacters();
        final boolean shift;
        final String clip;

        shift = (modifiers & InputEvent.SHIFT_DOWN_MASK) != 0;

        // React only if we are enabled and have focus.
        if (isEnabled() && isSelected()) {

            if (keyCode == KeyEvent.VK_LEFT) {

                if (shift) {

                    if ((this.caret > 0) && (this.selectStart == -1)) {
                        this.selectStart = this.caret - 1;
                        this.selectEnd = this.caret;
                        repaint = true;
                    } else if ((this.selectStart < this.caret) && (this.selectStart > 0)) {
                        this.selectStart--;
                        repaint = true;
                    } else if (this.selectEnd > this.caret) {
                        this.selectEnd--;

                        if (this.selectEnd == this.caret) {
                            this.selectStart = -1;
                            this.selectEnd = -1;
                        }

                        repaint = true;
                    }
                } else {

                    if (this.caret > 0) {
                        this.caret--;
                    }

                    this.selectStart = -1;
                    this.selectEnd = -1;
                    repaint = true;
                }
            } else if (keyCode == KeyEvent.VK_RIGHT) {

                if (shift) {

                    if ((this.caret < this.text.length()) && (this.selectStart == -1)) {
                        this.selectStart = this.caret;
                        this.selectEnd = this.caret + 1;
                        repaint = true;
                    } else if ((this.selectEnd > this.caret)
                            && (this.selectEnd < this.text.length())) {
                        this.selectEnd++;
                        repaint = true;
                    } else if (this.selectStart < this.caret) {
                        this.selectStart++;

                        if (this.selectStart == this.caret) {
                            this.selectStart = -1;
                            this.selectEnd = -1;
                        }

                        repaint = true;
                    }
                } else {

                    if (this.caret < this.text.length()) {
                        this.caret++;
                        this.selectStart = -1;
                        this.selectEnd = -1;
                        repaint = true;
                    }
                }
            } else if (keyCode == KeyEvent.VK_HOME) {

                if (shift) {

                    if (this.caret > 0) {
                        this.selectStart = 0;
                        this.selectEnd = this.caret;
                        repaint = true;
                    }
                } else {
                    this.caret = 0;
                    this.selectStart = -1;
                    this.selectEnd = -1;
                    repaint = true;
                }
            } else if (keyCode == KeyEvent.VK_END) {

                if (shift) {

                    if (this.caret < this.text.length()) {
                        this.selectStart = this.caret;
                        this.selectEnd = this.text.length();
                        repaint = true;
                    }
                } else {
                    this.caret = this.text.length();
                    this.selectStart = -1;
                    this.selectEnd = -1;
                    repaint = true;
                }
            } else if (keyChar == KeyEvent.VK_DELETE) {

                if ((this.selectStart != -1) && (this.selectEnd != -1)) {
                    txt = this.text.substring(0, this.selectStart) + this.text.substring(this.selectEnd);
                    theCaret = this.selectStart;
                    setTextValue(txt);
                    this.caret = theCaret;
                    repaint = true;

                    flushRedos();
                    addUndoCheckpoint();
                } else if (this.caret < this.text.length()) {
                    txt = this.text.substring(0, this.caret) + this.text.substring(this.caret + 1);
                    theCaret = this.caret;
                    setTextValue(txt);
                    this.caret = theCaret;
                    repaint = true;

                    flushRedos();
                    addUndoCheckpoint();
                }
            } else if (keyChar == KeyEvent.VK_BACK_SPACE) {

                if ((this.selectStart != -1) && (this.selectEnd != -1)) {
                    txt = this.text.substring(0, this.selectStart) + this.text.substring(this.selectEnd);
                    theCaret = this.selectStart;
                    setTextValue(txt);
                    this.caret = theCaret;
                    repaint = true;

                    flushRedos();
                    addUndoCheckpoint();
                } else if (this.caret > 0) {
                    txt = this.text.substring(0, this.caret - 1) + this.text.substring(this.caret);
                    theCaret = this.caret - 1;
                    setTextValue(txt);
                    this.caret = theCaret;
                    repaint = true;

                    flushRedos();
                    addUndoCheckpoint();
                }
            } else if (valid.indexOf(keyChar) != -1) {

                if (this.selectStart != -1 && this.selectEnd != -1) {
                    txt = this.text.substring(0, this.selectStart) + keyChar + this.text.substring(this.selectEnd);
                    theCaret = this.selectStart + 1;
                } else {
                    if (this.caret > this.text.length()) {
                        Log.warning("Caret was past end of text.");
                        this.caret = this.text.length();
                    }
                    if (this.caret == 0) {
                        if (this.caret == this.text.length()) {
                            txt = Character.toString(keyChar);
                        } else {
                            txt = keyChar + this.text.substring(this.caret);
                        }
                    } else if (this.caret == this.text.length()) {
                        txt = this.text + keyChar;
                    } else {
                        txt = this.text.substring(0, this.caret) + keyChar + this.text.substring(this.caret);
                    }

                    theCaret = this.caret + 1;

                }
                setTextValue(txt);
                this.caret = theCaret;
                flushRedos();
                addUndoCheckpoint();
                repaint = true;
            } else if (keyChar == 3) {

                // COPY
                if (this.text != null) {

                    if ((this.selectStart != -1) && (this.selectEnd != -1)) {
                        clipboard = this.text.substring(this.selectStart, this.selectEnd);
                    } else {
                        clipboard = this.text;
                    }
                }
            } else if (keyChar == 22) {

                // PASTE
                if (clipboard instanceof final String clipboardString) {
                    clip = clipboardString;

                    if ((this.selectStart != -1) && (this.selectEnd != -1)) {
                        txt = this.text.substring(0, this.selectStart) + clip + this.text.substring(this.selectEnd);
                        theCaret = this.selectStart + clip.length();
                    } else {
                        if (this.caret > this.text.length()) {
                            Log.warning("Caret was past end of text.");
                            this.caret = this.text.length();
                        }

                        if (this.caret == 0) {
                            if (this.caret == this.text.length()) {
                                txt = clip;
                            } else {
                                txt = clip + this.text.substring(this.caret);
                            }
                        } else if (this.caret == this.text.length()) {
                            txt = this.text.substring(0, this.caret) + clip;
                        } else {
                            txt = this.text.substring(0, this.caret) + clip + this.text.substring(this.caret);
                        }
                        theCaret = this.caret + clip.length();
                    }
                    setTextValue(txt);
                    this.caret = theCaret;
                    flushRedos();
                    addUndoCheckpoint();
                    repaint = true;
                }
            } else if (keyChar == 24) {

                // CUT
                if ((this.selectStart != -1) && (this.selectEnd != -1)) {
                    clipboard = this.text.substring(this.selectStart, this.selectEnd);

                    while (this.historyPos < (this.history.size() - 1)) {
                        this.history.remove(this.historyPos + 1);
                    }

                    txt = this.text.substring(0, this.selectStart)
                            + this.text.substring(this.selectEnd);
                    theCaret = this.selectStart;
                    setTextValue(txt);
                    this.caret = theCaret;
                    repaint = true;

                    this.history.add(this.text);
                    this.historyPos = this.history.size() - 1;
                }
            } else if (keyChar == 25) {

                // REDO
                if (this.historyPos < (this.history.size() - 1)) {
                    this.historyPos++;
                    setTextValue(this.history.get(this.historyPos));
                    repaint = true;
                }
            } else if (keyChar == 26) {

                // UNDO
                if (this.historyPos > 0) {
                    this.historyPos--;
                    setTextValue(this.history.get(this.historyPos));
                    repaint = true;
                }
            }
        }

        // Forward key to all children
        if (super.processKey(keyChar, keyCode, modifiers, context)) {
            repaint = true;
        }

        return repaint;
    }

    /**
     * Handler for mouse drag events. Mouse drags are propagated to all children, with the coordinates being adjusted to
     * the child's frame. Input objects should support selection of a range of objects.
     *
     * @param xCoord the X coordinate (in the object's coordinate system)
     * @param yCoord the Y coordinate (in the object's coordinate system)
     * @return true if a change requiring repaint occurred
     */
    @Override
    public final boolean processMouseDrag(final int xCoord, final int yCoord, final EvalContext context) {

        boolean repaint = false;
        int pos;
        int mid;
        final int newX;

        if (this.ownsDrag > 0) {

            if ((xCoord >= 0) && (xCoord < getWidth()) && (yCoord >= 0) && (yCoord < getHeight())) {

                // See what characters the cursor is between.
                pos = this.text.length();
                newX = xCoord - INSET_LEFT;

                if (this.charPositions != null) {
                    final int len = this.charPositions.length;
                    for (int i = 1; i < len; ++i) {
                        mid = (this.charPositions[i - 1] + this.charPositions[i]) / 2;

                        if (newX <= mid) {
                            pos = i - 1;

                            break;
                        }
                    }
                }

                // If the drag is just starting, set the caret to the start point
                if (this.ownsDrag == 1) {
                    this.caret = pos;
                    this.ownsDrag = 2;
                }

                // If pos = caret, no selection. If pos < caret, select from pos to caret. If pos >
                // caret, select from caret to pos.
                if (pos == this.caret) {
                    this.selectStart = -1;
                    this.selectEnd = -1;
                } else if (pos < this.caret) {
                    this.selectStart = pos;
                    this.selectEnd = this.caret;
                } else {
                    this.selectStart = this.caret;
                    this.selectEnd = pos;
                }

                repaint = true;
            }
        }

        if (super.processMouseDrag(xCoord, yCoord, context)) {
            repaint = true;
        }

        return repaint;
    }

    /**
     * Get the list of characters the field allows.
     *
     * @return a String made up of all valid characters
     */
    String getValidCharacters() {

        return CoreConstants.EMPTY;
    }

    /**
     * Flush forward redo events from the undo buffer. This is done when any new edit is made.
     */
    private void flushRedos() {

        while (this.historyPos < (this.history.size() - 1)) {
            this.history.remove(this.historyPos + 1);
        }
    }

    /**
     * Store the current state as a checkpoint to which undo may return.
     */
    private void addUndoCheckpoint() {

        this.history.add(this.text);
        this.historyPos = this.history.size() - 1;
    }

    /**
     * Write the LaTeX representation of the object to a string buffer.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file; the value should be updated if the method writes any files
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time (this method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL])
     * @param builder      the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  true to show answers in any inputs embedded in the document; false if answers should not be
     *                     shown
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex, final boolean[] overwriteAll, final HtmlBuilder builder,
                        final boolean showAnswers, final char[] mode, final EvalContext context) {

        // TODO: The input does not know its correctness
        builder.add(" \\framebox[.75in]{\\strut \\rule{0pt}{6pt}} ");
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    final int fieldInnerHashCode() {

        return inputInnerHashCode() + Objects.hashCode(this.text);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    final boolean fieldInnerEquals(final AbstractDocInputField obj) {

        return inputInnerEquals(obj) && Objects.equals(this.text, obj.text);
    }
}
