/**
 * Classes that represent the "View" portion of an expression editor.
 *
 * <p>
 * An expression establishes a typographic baseline, and calculates an initial typographic and mathematical center
 * height using glyphs from the initial font.
 *
 * <p>
 * Leaf nodes (Boolean constants, decimal points, digits, the Engineering E, operators, and symbolic constants) are all
 * laid out with their baselines on the expression baseline.
 *
 * <p>
 * A "base with exponent" construction is laid out with the base's baseline on the expression baseline.  The exponent is
 * a sub-expression which is laid out by itself, then is positioned with its baseline 60% up the base's ascent, or so
 * its bottom edge lies above the typographic midline of the base, whichever is higher. The exponent's font size is the
 * greatest of 74% of the base's font size or the minimum font size.  If the base's sub-expression or the exponent
 * sub-expression is empty, they are represented as an empty dashed box.
 *
 * <p>
 * A "fraction" construction is laid out with its fraction bar on the mathematical center of the preceding object.  The
 * numerator and denominator are subexpressions that are placed with bottom and top edges (respectively) a set distance
 * above or below the fraction bar (and centered horizontally).  If the numerator sub-expression or the denominator
 * sub-expression is empty, they are represented as an empty dashed box.
 *
 * <p>
 * A "function" construction is laid out with the function name and opening parenthesis on the typographic baseline,
 * then the argument sub-expressions, separated by commas, all lined up on the same typographic baseline, then the
 * closing parenthesis on the same baseline.  The parentheses size automatically based on the highest and lowest points
 * in the argument expressions.  Any empty argument sub-expressions are represented by an empty dashed box.  Inserting a
 * "comma" character after the last argument creates a new argument in a n-ary function.
 *
 * <p>
 * A "matrix" or "vector" construction shows opening and closing brackets, centered on the typographic center line of
 * the object to its left, sized to contain all entries.  Empty entries are represented by dashed boxes.  Otherwise,
 * entry sub-expressions are laid out.  Entries in each row are aligned with consistent mathematical center.  Entries in
 * each column are aligned horizontally according to the justification setting.
 *
 * <p>
 * A "radical" or "radical with root" construction is laid out the base sub-expression placed on the current baseline,
 * surrounded by a radical (surd plus overline) sized to contain the base.  The root (an integer or variable name, not a
 * full sub-expression) is laid with font size the greatest of 74% of the base's font size or the minimum font size, so
 * its bottom edge sits some distance above the top of the start of the surd.
 *
 * <p>
 * A "String" construction is laid out with its contents on the baseline, surrounded by double-quotes;
 *
 * <p>
 * A "variable name" construction is laid out with its name on the baseline, any accents drawn above, and any subscript
 * drawn with font size the greatest of 74% of the variable name's font size or the minimum font size, with its
 * typographic center on the baseline of the variable name.
 *
 * <p>
 * An "If-then-else" construction can be laid out as a single line or as a multi-line construction.  When laid out as a
 * single line, the construction has "IF (" followed by the condition expression, followed by ") THEN {", the THEN
 * clause sub-expression, and "} ELSE {", then the ELSE sub-expression, then "}", where parentheses and braces are
 * automatically sized to contain the sub-expression.  For each nested else clause that is also an If-Then-Else
 * construction, the representation is condensed with " ELSE IF (", the condition, ") THEN {", the THEN clause, and "}",
 * and for the final else. In this case, all parts are laid out on the current expression baseline. In multi-line
 * layout, the "IF (" and condition expression, followed by ") THEN {" are laid out on the first line, the THEN clause
 * sub-expression on the second line (indented), and "} ELSE {" or "} ELSE IF (" on the third line, and so on, with each
 * THEN or ELSE clause indented.  The first line's baseline and centers are used as the object's baseline and centers.
 *
 * <p>
 * A "switch" construction is laid out as a multi-line construction, with "SWITCH (", the condition sub-expression, ")
 * {" on the first line, then indented lines for each case and the default value, then a closing "}" on its own line.
 * Each case has "CASE #: {", the case sub-expression, then "}".  The default value has "DEFAULT: {", the default
 * sub-expression, then "}".  Cases and defaults are aligned so their colons align vertically, and so the leftmost edge
 * is indented some distance from the SWITCH.  The first line's baseline and centers are used as the object's baseline
 * and centers.
 */
package dev.mathops.assessment.expression.editview;
