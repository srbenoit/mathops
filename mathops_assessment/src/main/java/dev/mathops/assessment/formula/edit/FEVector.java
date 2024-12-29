package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.ConstIntegerValue;
import dev.mathops.assessment.formula.ConstRealValue;
import dev.mathops.assessment.formula.EFunction;
import dev.mathops.assessment.formula.EUnaryOp;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.IntegerFormulaVector;
import dev.mathops.assessment.formula.RealFormulaVector;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.EnumSet;
import java.util.List;

/**
 * A container for a Vector that can generate an {@code IntegerVectorValue} or {@code RealVectorValue}.
 */
public final class FEVector extends AbstractFEObject {

    /** The opening bracket box. */
    private RenderedBox openBracket;

    /** The closing bracket box. */
    private RenderedBox closeBracket;

    /** The array of entries (can be empty, never {@code null}). */
    private AbstractFEObject[] entries;

    /** The rendered commas between vector entries (one less than the number of entries). */
    private RenderedBox[] commas;

    /**
     * Constructs a new {@code FEVector}.
     *
     * @param theFontSize the font size for the component
     */
    public FEVector(final int theFontSize) {

        super(theFontSize);

        this.entries = new AbstractFEObject[1];

        final EnumSet<EType> allowed = getAllowedTypes();
        allowed.add(EType.INTEGER_VECTOR);
        allowed.add(EType.REAL_VECTOR);

        final EnumSet<EType> possible = getPossibleTypes();
        possible.add(EType.INTEGER_VECTOR);
        possible.add(EType.REAL_VECTOR);
    }

    /**
     * Sets the number of entries. If longer than the current number of entries, existing entries are retained and the
     * vector is lengthened. If less than the current number of entries, the vector is truncated and entries beyond the
     * new length are discarded.
     *
     * @param newNumEntries the new number of entries (values less than 1 are treated as 1)
     * @param storeUndo     true to store an undo state; false to skip
     */
    public void setNumEntries(final int newNumEntries, final boolean storeUndo) {

        final int newSize = Math.max(1, newNumEntries);
        final int toCopy = Math.min(this.entries.length, newSize);

        final AbstractFEObject[] newEntries = new AbstractFEObject[newSize];
        if (toCopy > 0) {
            System.arraycopy(this.entries, 0, newEntries, 0, toCopy);
        }
        this.entries = newEntries;

        recomputeCurrentType();
        update(storeUndo);
    }

    /**
     * Gets the number of entries in the vector.
     *
     * @return the number of entries
     */
    public int getNumEntries() {

        return this.entries.length;
    }

    /**
     * Sets the entry at an index.
     *
     * @param index     the index (if this is negative or beyond the end of the vector, no action is taken)
     * @param newEntry  the new entry (may be {@code null})
     * @param storeUndo true to store an undo state; false to skip
     */
    public void setEntry(final int index, final AbstractFEObject newEntry,
                         final boolean storeUndo) {

        if (newEntry == null) {
            if (this.entries[index] != null) {
                this.entries[index].setParent(null);
            }
            this.entries[index] = null;
            recomputeCurrentType();
            update(storeUndo);
        } else {
            final EnumSet<EType> allowed = getAllowedEntryTypes();
            final EType childType = newEntry.getCurrentType();
            final boolean isAllowed;

            if (childType == null) {
                final EnumSet<EType> filtered = EType.filter(allowed, newEntry.getAllowedTypes());
                isAllowed = !filtered.isEmpty();
            } else {
                isAllowed = allowed.contains(childType);
            }

            if (isAllowed) {
                if (this.entries[index] != null) {
                    this.entries[index].setParent(null);
                }
                this.entries[index] = newEntry;
                newEntry.setParent(this);
                recomputeCurrentType();
                update(storeUndo);
            }
        }

    }

    /**
     * Gets the value at an index.
     *
     * @param index the index (if this is negative or beyond the end of the vector, {@code null} is returned)
     * @return the value (can be {@code null})
     */
    public AbstractFEObject getEntry(final int index) {

        AbstractFEObject result = null;

        if (index >= 0 && index < this.entries.length) {
            result = this.entries[index];
        }

        return result;
    }

    /**
     * Gets the total number of "cursor steps" in the object and its descendants. The cursor steps over an opening "<",
     * over a comma between each entry, and over a closing ">", plus the steps within each entry.
     *
     * @return the number of cursor steps
     */
    @Override
    public int getNumCursorSteps() {

        int total;

        if (this.entries.length == 0) {
            total = 2;
        } else {
            total = 1 + this.entries.length; // Number of brackets + commas

            for (final AbstractFEObject entry : this.entries) {
                if (entry != null) {
                    total += entry.getNumCursorSteps();
                }
            }
        }

        return total;
    }

    /**
     * Tests whether this object is in a valid state.
     *
     * @return true if valid (a formula can be generated); false if not
     */
    @Override
    public boolean isValid() {

        boolean valid = this.entries.length > 0;

        for (final AbstractFEObject entry : this.entries) {
            if (entry == null) {
                valid = false;
            } else {
                valid = entry.isValid();
            }
            if (!valid) {
                break;
            }
        }

        return valid;
    }

    /**
     * Attempts to replace one child with another. For example, replacing an integer constant with a real constant when
     * the user types a '.' character while entering a number.
     *
     * @param currentChild the current child
     * @param newChild     the new (replacement) child
     * @return true if the replacement was allowed (and performed); false if it is not allowed
     */
    @Override
    public boolean replaceChild(final AbstractFEObject currentChild,
                                final AbstractFEObject newChild) {

        int index = -1;

        final int count = this.entries.length;
        for (int i = 0; i < count; ++i) {
            if (this.entries[i] == currentChild) {
                index = i;
                break;
            }
        }

        boolean result;

        if (index == -1) {
            Log.warning("Attempt to replace child that was not actually a child");
            result = false;
        } else if (newChild == null) {
            this.entries[index] = null;
            recomputeCurrentType();
            update(true);
            result = true;
        } else {
            final EnumSet<EType> allowed = getAllowedTypes();
            final boolean realAllowed = allowed.contains(EType.REAL_VECTOR);
            final EType newType = newChild.getCurrentType();

            if (newType == EType.INTEGER) {
                setEntry(index, newChild, true);
                result = true;
            } else if (newType == EType.REAL) {
                if (realAllowed) {
                    setEntry(index, newChild, true);
                    result = true;
                } else {
                    Log.warning("Attempt to add real to integer vector");
                    result = false;
                }
            } else if (newType == null) {
                result = false;
                final EnumSet<EType> possible = newChild.getAllowedTypes();
                final EnumSet<EType> filtered = EnumSet.noneOf(EType.class);

                if (possible.contains(EType.INTEGER)) {
                    filtered.add(EType.INTEGER);
                    result = true;
                }
                if (realAllowed && possible.contains(EType.REAL)) {
                    filtered.add(EType.REAL);
                    result = true;
                }

                if (result) {
                    possible.clear();
                    possible.addAll(filtered);
                    setEntry(index, newChild, true);
                }
            } else {
                Log.warning("Attempt to add ", newType, " type child to vector");
                result = false;
            }
        }

        return result;
    }

    /**
     * Determines the entry types allowed based on the type of vector.
     *
     * @return the allowed argument types
     */
    private EnumSet<EType> getAllowedEntryTypes() {

        final EnumSet<EType> allowed = EnumSet.noneOf(EType.class);

        allowed.add(EType.INTEGER);

        if (getAllowedTypes().contains(EType.REAL_VECTOR)) {
            allowed.add(EType.REAL);
        }

        return allowed;
    }

    /**
     * Recomputes the current type (does nothing for fixed-type constant values).
     */
    @Override
    public void recomputeCurrentType() {

        boolean allInt = true;
        boolean allPresent = true;
        for (final AbstractFEObject obj : this.entries) {
            if (obj == null) {
                allPresent = false;
                break;
            }

            final EType type = obj.getCurrentType();
            if (type == null) {
                allPresent = false;
                break;
            }
            if (type != EType.INTEGER) {
                allInt = false;
            }
        }

        final EnumSet<EType> possible = getPossibleTypes();
        possible.clear();

        if (allPresent) {
            setCurrentType(allInt ? EType.INTEGER_VECTOR : EType.REAL_VECTOR);
            possible.add(getCurrentType());
        } else {
            setCurrentType(null);
            possible.add(EType.INTEGER_VECTOR);
            possible.add(EType.REAL_VECTOR);
        }
    }

    /**
     * Recomputes all cursor positions within the object.
     *
     * @param startPos the start position of this object
     */
    @Override
    public void recomputeCursorPositions(final int startPos) {

        setFirstCursorPosition(startPos);

        int pos = startPos + 1; // Skip opening "["

        for (final AbstractFEObject obj : this.entries) {
            if (obj != null) {
                obj.recomputeCursorPositions(pos);
                pos += obj.getNumCursorSteps();
            }
            ++pos; // Skip comma
        }
    }

    /**
     * Generates an {@code IntegerVectorValue} or {@code RealVectorValue} object.
     *
     * @return the object; {@code null} if this object is invalid
     */
    @Override
    public AbstractFormulaObject generate() {

        AbstractFormulaObject result = null;

        final int count = this.entries.length;

        if (count > 0) {
            final AbstractFormulaObject[] objects = new AbstractFormulaObject[count];
            boolean valid = true;
            boolean allIntegers = true;

            for (int i = 0; valid && i < count; ++i) {
                final AbstractFEObject entry = this.entries[i];

                if (entry == null) {
                    valid = false;
                } else {
                    objects[i] = entry.generate();
                    if (objects[i] == null) {
                        valid = false;
                    } else if (objects[i] instanceof ErrorValue) {
                        valid = false;
                        result = objects[i];
                    } else if (objects[i] instanceof ConstRealValue) {
                        allIntegers = false;
                    } else if (!(objects[i] instanceof ConstIntegerValue)) {
                        Log.warning("Element in vector generated ", objects[i].getClass().getName());
                        valid = false;
                    }
                }
            }

            if (valid) {
                if (allIntegers) {
                    final IntegerFormulaVector intVec = new IntegerFormulaVector();
                    for (final AbstractFormulaObject obj : objects) {
                        intVec.addChild(obj);
                    }
                    result = intVec;
                } else {
                    final boolean realAllowed = getAllowedTypes().contains(EType.REAL_VECTOR);

                    if (realAllowed) {
                        final RealFormulaVector realVec = new RealFormulaVector();
                        for (final AbstractFormulaObject obj : objects) {
                            realVec.addChild(obj);
                        }
                        result = realVec;
                    } else {
                        final String msg = "Integer vector had non-integer values";
                        Log.warning(msg);
                        result = new ErrorValue(msg);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Asks the object what modifications are valid for a specified cursor position or selection range.
     *
     * @param fECursor               cursor position information
     * @param allowedModifications a set that will be populated with the set of allowed modifications at the specified
     *                             position
     */
    @Override
    public void indicateValidModifications(final FECursor fECursor, final EnumSet<EModification> allowedModifications) {

        final int cursorPos = fECursor.cursorPosition;

        final int start = getFirstCursorPosition();
        final int end = start + getNumCursorSteps();
        final boolean realAllowed = getAllowedTypes().contains(EType.REAL_VECTOR);

        if (cursorPos > start && cursorPos < end - 1) {
            int pos = start + 1;
            for (final AbstractFEObject entry : this.entries) {
                if (cursorPos == pos) {
                    // At start of entry "i"
                    if (entry == null) {
                        allowedModifications.add(EModification.TYPE);
                        allowedModifications.add(EModification.INSERT_INTEGER);
                        if (realAllowed) {
                            allowedModifications.add(EModification.INSERT_REAL);
                        }
                    } else {
                        entry.indicateValidModifications(fECursor, allowedModifications);
                    }
                    break;
                }

                if (entry != null) {
                    pos += entry.getNumCursorSteps();
                    if (cursorPos < pos) {
                        entry.indicateValidModifications(fECursor, allowedModifications);
                        break;
                    }
                }

                // Skip over comma
                ++pos;
            }
        }
    }

    /**
     * Processes a typed character.
     *
     * @param fECursor the cursor position and selection range
     * @param ch     the character typed
     */
    @Override
    public void processChar(final FECursor fECursor, final char ch) {

        final int cursorPos = fECursor.cursorPosition;

        final int start = getFirstCursorPosition();
        final int end = start + getNumCursorSteps();
        final boolean realAllowed = getAllowedTypes().contains(EType.REAL_VECTOR);

        // Log.info("Vector processing '", Character.toString(ch), "' start=" + start + ", end=" + end + ",
        // pos=" + cursorPos);

        if (cursorPos > start && cursorPos < end) {
            final int fontSize = getFontSize();

            if ((int) ch == (int) CoreConstants.COMMA_CHAR) {
                // Create a new entry - perhaps split a number if the cursor is within a number
                int pos = start + 1;
                final int count = this.entries.length;
                for (int i = 0; i < count; ++i) {
                    if (cursorPos == pos) {
                        // At start of entry "i" - create a new entry and push entries "i" and higher forward
                        final AbstractFEObject[] newEntries = new AbstractFEObject[count + 1];
                        if (i > 0) {
                            System.arraycopy(this.entries, 0, newEntries, 0, i - 1);
                        }
                        System.arraycopy(this.entries, i, newEntries, i, count - i);
                        this.entries = newEntries;
                        ++fECursor.cursorPosition;
                        update(true);
                    } else {
                        final AbstractFEObject entry = this.entries[i];
                        if (entry != null) {
                            final int entryEnd = entry.getFirstCursorPosition() + entry.getNumCursorSteps();

                            if (cursorPos == entryEnd) {
                                // At end of entry "i" - create a new entry and push entries greater than "i" forward

                                final AbstractFEObject[] newEntries = new AbstractFEObject[count + 1];

                                System.arraycopy(this.entries, 0, newEntries, 0, i + 1);
                                if (i + 1 < count) {
                                    System.arraycopy(this.entries, i + 1, newEntries, i + 2, count - i - 1);
                                }
                                this.entries = newEntries;
                                ++fECursor.cursorPosition;
                                update(true);
                            } else if (cursorPos < entryEnd) {
                                // We are within an entry - if it is a simple Integer or Real number, the comma can
                                // split it into two entries. If it is anything more complex, we do not attempt to
                                // split. For example "123+456" going to "123+4,56" is not supported

                                if (entry instanceof final FEConstantInteger intEntry) {
                                    final String text = intEntry.getText();
                                    final int offset = fECursor.cursorPosition - entry.getNumCursorSteps();

                                    final FEConstantInteger preInt = new FEConstantInteger(fontSize);
                                    final String firstPart = text.substring(0, offset);
                                    preInt.setText(firstPart, false);

                                    final FEConstantInteger postInt = new FEConstantInteger(fontSize);
                                    final String lastPart = text.substring(offset);
                                    postInt.setText(lastPart, false);

                                    final AbstractFEObject[] newEntries = new AbstractFEObject[count + 1];
                                    if (i > 0) {
                                        System.arraycopy(this.entries, 0, newEntries, 0, i - 1);
                                    }
                                    if (i + 1 < count) {
                                        System.arraycopy(this.entries, i + 1, newEntries, i + 1, count - i);
                                    }
                                    setEntry(i, preInt, false);
                                    setEntry(i + 1, postInt, false);
                                    this.entries = newEntries;
                                    ++fECursor.cursorPosition;
                                    update(true);

                                } else if (entry instanceof final FEConstantReal realEntry) {

                                    // TODO:

                                } else {
                                    entry.processChar(fECursor, CoreConstants.COMMA_CHAR);
                                }
                            }

                            pos = entryEnd;
                        }
                    }

                    // Skip over comma
                    ++pos;
                }

            } else {
                int pos = start + 1;
                final int numEntries = this.entries.length;
                for (int i = 0; i < numEntries; ++i) {
                    if (cursorPos == pos) {
                        // At start of entry "i"
                        if (this.entries[i] == null) {

                            if ((int) ch >= '0' && (int) ch <= '9') {
                                ++fECursor.cursorPosition;
                                final FEConstantInteger constInt = new FEConstantInteger(fontSize);
                                final String txt = Character.toString(ch);
                                constInt.setText(txt, false);
                                setEntry(i, constInt, true);
                            } else if ((int) ch == '\u03c0' || (int) ch == '\u0435' || (int) ch == '.') {
                                if (realAllowed) {
                                    ++fECursor.cursorPosition;
                                    final FEConstantReal constReal = new FEConstantReal(fontSize);
                                    final String txt = Character.toString(ch);
                                    constReal.setText(txt, false);
                                    setEntry(i, constReal, true);
                                }
                            } else if ((int) ch == '+' || ch == '-') {
                                ++fECursor.cursorPosition;
                                final FEUnaryOper unary = new FEUnaryOper(fontSize,
                                        (int) ch == '+' ? EUnaryOp.PLUS : EUnaryOp.MINUS);
                                if (!realAllowed) {
                                    unary.getAllowedTypes().remove(EType.REAL);
                                }
                                setEntry(i, unary, true);
                            } else if ((int) ch == '{') {
                                ++fECursor.cursorPosition;
                                final FEVarRef varRef = new FEVarRef(fontSize);
                                varRef.getAllowedTypes().clear();
                                varRef.getAllowedTypes().add(EType.INTEGER);
                                if (!realAllowed) {
                                    varRef.getAllowedTypes().remove(EType.REAL);
                                }
                                setEntry(i, varRef, true);
                            } else if ((int) ch == '(') {
                                ++fECursor.cursorPosition;
                                final FEGrouping grouping = new FEGrouping(fontSize);
                                grouping.getAllowedTypes().clear();
                                grouping.getAllowedTypes().add(EType.INTEGER);
                                if (!realAllowed) {
                                    grouping.getAllowedTypes().remove(EType.REAL);
                                }
                                setEntry(i, grouping, true);
                            } else if ((int) ch >= '\u2720' && (int) ch <= '\u274F') {
                                final EFunction f = EFunction.forChar(ch);
                                if (f != null) {
                                    if (realAllowed) {
                                        if (f != EFunction.NOT) {
                                            ++fECursor.cursorPosition;
                                            final FEFunction function = new FEFunction(fontSize, f);
                                            function.getAllowedTypes().clear();
                                            function.getAllowedTypes().add(EType.INTEGER);
                                            function.getAllowedTypes().remove(EType.REAL);
                                            setEntry(i, function, true);
                                        }
                                    } else if (f == EFunction.ABS || f == EFunction.CEIL || f == EFunction.FLOOR
                                            || f == EFunction.ROUND || f == EFunction.GCD || f == EFunction.SRAD2
                                            || f == EFunction.SRAD3) {
                                        ++fECursor.cursorPosition;
                                        final FEFunction function = new FEFunction(fontSize, f);
                                        function.getAllowedTypes().clear();
                                        function.getAllowedTypes().add(EType.INTEGER);
                                        setEntry(i, function, true);
                                    }
                                }
                            } else if (ch == '<') {
                                ++fECursor.cursorPosition;
                                final FETest test = new FETest(fontSize);
                                test.getAllowedTypes().clear();
                                test.getAllowedTypes().add(EType.INTEGER);
                                if (!realAllowed) {
                                    test.getAllowedTypes().remove(EType.REAL);
                                }
                                setEntry(i, test, true);
                            }
                        } else {
                            this.entries[i].processChar(fECursor, ch);
                        }
                        break;
                    }

                    if (this.entries[i] != null) {
                        pos += this.entries[i].getNumCursorSteps();
                        Log.info("VEC[" + i + "]: Cursor = " + cursorPos + ", entry end=" + pos);
                        if (cursorPos <= pos) {
                            this.entries[i].processChar(fECursor, ch);
                            break;
                        }
                    }

                    // Skip over comma
                    ++pos;
                }
            }
        } else if (end == 2) {
            if ((int) ch == 0x08 && cursorPos == 2) {
                // Backspace at end of vector when empty
                --fECursor.cursorPosition;
                getParent().replaceChild(this, null);
            } else if ((int) ch == 0x7f && cursorPos == 0) {
                // Delete at start of vector when empty
                getParent().replaceChild(this, null);
            }
        }
    }

    /**
     * Processes an insert.
     *
     * @param fECursor   the cursor position and selection range
     * @param toInsert the object to insert (never {@code null})
     * @return {@code null} on success; an error message on failure
     */
    @Override
    public String processInsert(final FECursor fECursor, final AbstractFEObject toInsert) {

        String error = null;

        final int cursorPos = fECursor.cursorPosition;

        final int start = getFirstCursorPosition();
        final int end = start + getNumCursorSteps();
        final boolean realAllowed = getAllowedTypes().contains(EType.REAL_VECTOR);

        if (cursorPos > start && cursorPos < end - 1) {
            int pos = start + 1;
            final int numEntries = this.entries.length;
            for (int i = 0; i < numEntries; ++i) {
                if (cursorPos == pos) {
                    // At start of entry "i"
                    if (this.entries[i] == null) {
                        final EType newType = toInsert.getCurrentType();

                        // Insert at entry [i]
                        if ((newType == EType.INTEGER) || (realAllowed && newType == EType.REAL)) {
                            setEntry(i, toInsert, true);
                        } else if (newType == null) {
                            boolean result = false;
                            final EnumSet<EType> possible = toInsert.getAllowedTypes();
                            final EnumSet<EType> filtered = EnumSet.noneOf(EType.class);

                            if (possible.contains(EType.INTEGER)) {
                                filtered.add(EType.INTEGER);
                                result = true;
                            }

                            if (realAllowed && possible.contains(EType.REAL)) {
                                filtered.add(EType.REAL);
                                result = true;
                            }

                            if (result) {
                                possible.clear();
                                possible.addAll(filtered);
                                setEntry(i, toInsert, true);
                            }
                        }
                    } else {
                        error = this.entries[i].processInsert(fECursor, toInsert);
                    }
                    break;
                }

                if (this.entries[i] != null) {
                    pos += this.entries[i].getNumCursorSteps();
                    if (cursorPos < pos) {
                        error = this.entries[i].processInsert(fECursor, toInsert);
                        break;
                    }
                }

                // Skip over comma
                ++pos;
            }
        }

        return error;
    }

    /**
     * Performs layout when something changes. This method clears and re-generates the sequence of rendered boxes that
     * this component represents. This is part of the processing when the root object receives an "update"
     * notification.
     *
     * <p>
     * This method should lay out all child objects and rendered boxes relative to its own origin, but this method does
     * not
     *
     * @param g2d the {@code Graphics2D} from which a font render context can be obtained
     */
    @Override
    public void layout(final Graphics2D g2d) {

        final int fontSize = getFontSize();

        this.openBracket = new RenderedBox("[");
        this.openBracket.setFontSize(fontSize);
        this.openBracket.layout(g2d);

        this.closeBracket = new RenderedBox("]");
        this.closeBracket.setFontSize(fontSize);
        this.closeBracket.layout(g2d);

        final int numEntries = this.entries.length;

        this.commas = new RenderedBox[numEntries - 1];
        final int numCommas = this.commas.length;
        for (int i = 0; i < numCommas; ++i) {
            this.commas[i] = new RenderedBox(",");
            this.commas[i].setFontSize(fontSize);
            this.commas[i].layout(g2d);
        }

        final FontRenderContext frc = g2d.getFontRenderContext();
        final Font font = getFont();
        final LineMetrics lineMetrics = font.getLineMetrics("0", frc);

        int x = this.openBracket.getAdvance();
        int topY = 0;
        int botY = 0;

        final Rectangle openBracketBounds = this.openBracket.getBounds();
        topY = Math.min(topY, openBracketBounds.y);
        botY = Math.max(botY, openBracketBounds.y + openBracketBounds.height);

        if (this.entries[0] != null) {
            this.entries[0].layout(g2d);
            this.entries[0].translate(x, 0);
            x += this.entries[0].getAdvance();

            final Rectangle entryBounds = this.entries[0].getBounds();
            topY = Math.min(topY, entryBounds.y);
            botY = Math.max(botY, entryBounds.y + entryBounds.height);
        }

        for (int i = 1; i < numEntries; ++i) {
            this.commas[i - 1].translate(x, 0);
            x += this.commas[i - 1].getAdvance();

            final Rectangle commaBounds = this.commas[i - 1].getBounds();
            topY = Math.min(topY, commaBounds.y);
            botY = Math.max(botY, commaBounds.y + commaBounds.height);

            if (this.entries[i] != null) {
                this.entries[i].layout(g2d);
                this.entries[i].translate(x, 0);
                x += this.entries[i].getAdvance();

                final Rectangle entryBounds = this.entries[i].getBounds();
                topY = Math.min(topY, entryBounds.y);
                botY = Math.max(botY, entryBounds.y + entryBounds.height);
            }
        }

        this.closeBracket.getOrigin().setLocation(x, 0);

        final Rectangle closeBRacketBounds = this.closeBracket.getBounds();
        topY = Math.min(topY, closeBRacketBounds.y);
        botY = Math.max(botY, closeBRacketBounds.y + closeBRacketBounds.height);

        x += this.closeBracket.getAdvance();

        final float[] lineBaselines = lineMetrics.getBaselineOffsets();
        final int center = Math.round(lineBaselines[Font.CENTER_BASELINE]);

        setAdvance(x);
        setCenterAscent(center);
        getOrigin().setLocation(0, 0);
        getBounds().setBounds(0, topY, x, botY - topY);
    }

    /**
     * Moves this object and all subordinate objects. Used during layout.
     *
     * @param dx the x offset
     * @param dy the y offset
     */
    @Override
    public void translate(final int dx, final int dy) {

        if (this.openBracket != null) {
            this.openBracket.translate(dx, dy);
        }
        if (this.closeBracket != null) {
            this.closeBracket.translate(dx, dy);
        }
        if (this.commas != null) {
            for (final RenderedBox box : this.commas) {
                box.translate(dx, dy);
            }
        }
        for (final AbstractFEObject entry : this.entries) {
            if (entry != null) {
                entry.translate(dx, dy);
            }
        }

        getOrigin().translate(dx, dy);
    }

    /**
     * Renders the component. This renders all rendered boxes in this component or its descendants.
     *
     * @param g2d    the {@code Graphics2D} to which to render
     * @param cursor the cursor position and selection range
     */
    @Override
    public void render(final Graphics2D g2d, final FECursor cursor) {

        if (this.openBracket != null) {
            this.openBracket.render(g2d, false);
        }

        if (this.commas != null) {
            for (final RenderedBox box : this.commas) {
                box.render(g2d, false);
            }
        }

        for (final AbstractFEObject entry : this.entries) {
            if (entry != null) {
                entry.render(g2d, cursor);
            }
        }

        if (this.closeBracket != null) {
            this.closeBracket.render(g2d, false);
        }
    }

    /**
     * Accumulates the ordered set of rendered boxes that make up this object.
     *
     * @param target the list to which to add rendered boxes
     */
    @Override
    public void gatherRenderedBoxes(final List<? super RenderedBox> target) {

        if (this.openBracket != null) {
            target.add(this.openBracket);
        }

        if (this.entries[0] != null) {
            this.entries[0].gatherRenderedBoxes(target);
        }

        final int numEntries = this.entries.length;
        for (int i = 1; i < numEntries; ++i) {
            if (this.commas != null) {
                target.add(this.commas[i - 1]);
            }
            if (this.entries[i] != null) {
                this.entries[i].gatherRenderedBoxes(target);
            }
        }

        if (this.closeBracket != null) {
            target.add(this.closeBracket);
        }
    }

    /**
     * Emits a diagnostic representation of this object.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     * @param indent  the indentation level
     */
    @Override
    public void emitDiagnostics(final HtmlBuilder builder, final int indent) {

        indent(builder, indent);
        final AbstractFEObject parent = getParent();
        builder.addln(parent == null ? "Vector*:" : "Vector:");

        final int numEntries = this.entries.length;
        for (int i = 0; i < numEntries; ++i) {
            if (this.entries[i] == null) {
                indent(builder, indent + 1);
                final String iStr = Integer.toString(i);
                builder.addln("(No [", iStr, "] entry)");
            } else {
                this.entries[i].emitDiagnostics(builder, indent + 1);
            }
        }
    }

    /**
     * Creates a duplicate of this object.
     *
     * @return the duplicate
     */
    @Override
    public FEVector duplicate() {

        final int fontSize = getFontSize();
        final FEVector dup = new FEVector(fontSize);

        dup.getAllowedTypes().clear();

        final EnumSet<EType> allowedTypes = getAllowedTypes();
        dup.getAllowedTypes().addAll(allowedTypes);

        final EType currentType = getCurrentType();
        dup.setCurrentType(currentType);

        final int numEntries = this.entries.length;
        dup.setNumEntries(numEntries, false);
        for (int i = 0; i < numEntries; ++i) {
            if (this.entries[i] != null) {
                // The following sets the parent on the duplicate entry
                final AbstractFEObject dupEntry = this.entries[i].duplicate();
                dup.setEntry(i, dupEntry, false);
            }
        }

        return dup;
    }
}
