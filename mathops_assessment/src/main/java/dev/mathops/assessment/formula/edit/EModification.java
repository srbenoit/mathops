package dev.mathops.assessment.formula.edit;

/**
 * The set of possible modifications that can be made, based on the cursor position and existing content, within a
 * formula edit structure. Many of these actions are available when the cursor it at a specific location with no
 * selection active, or when there is a section that would be deleted by one of these actions like an insertion and that
 * insertion would be valid.
 *
 * <p>
 * A user can always arrow left or right or click on a UI to change the cursor position, can create a selection region
 * by SHIFT-arrow actions or dragging on a UI, and can (to the extent supported by the UI) perform UNDO/REDO actions.
 */
public enum EModification {

    /** User can insert (or paste) a Boolean-valued object at the current position. */
    INSERT_BOOLEAN,

    /** User can insert (or paste) an Integer-valued object at the current position. */
    INSERT_INTEGER,

    /** User can insert (or paste) a Real-valued object at the current position. */
    INSERT_REAL,

    /** User can insert (or paste) an IntegerVector-valued object at the current position. */
    INSERT_INTEGER_VECTOR,

    /** User can insert (or paste) a RealVector-valued object at the current position. */
    INSERT_REAL_VECTOR,

    /** User can insert (or paste) a String-valued object at the current position. */
    INSERT_STRING,

    /** User can insert (or paste) a Span-valued object at the current position. */
    INSERT_SPAN,

    /** User can type (or paste) text - allowed characters may be filtered. */
    TYPE
}
