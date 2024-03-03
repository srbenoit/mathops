/**
 * Classes that represent the "model" of an expression within the editor.
 *
 * <p>
 * Every object that can flow in a linear sequence in an expression is a "glyph".  This includes constructions like
 * fractions, radicals, vectors and matrices, strings, etc.  A sequence of these glyphs are arranged in a line, and
 * a cursor can navigate along that line.
 *
 * <p>
 * When a glyph may contain children, the cursor can "step into" the glyph from either end, and can then "step out of"
 * and back to its container's sequence of cursor positions.  The cursor object has an "in sub-expression" flag that
 * indicates it is within a sub-expression of the child to the right of the cursor.  When this is true, the cursor
 * position tracked by the sub-expression governs.
 */
package dev.mathops.assessment.expression.editmodel;
