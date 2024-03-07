/**
 * Classes that represent the "model" of an expression within the editor.
 *
 * <p>
 * A top-level "expression" model object is a container for a sequence of expression objects:
 * <ul>
 *     <li>Operators</li>
 *     <li>Decimal digits, decimal points, and the Engineering "E" symbol</li>
 *     <li>Symbolic constants like PI, E, and I</li>
 *     <li>Boolean constants</li>
 *     <li>String constants</li>
 *     <li>Variable names</li>
 *     <li>Fractions (with expressions as numerator and denominator)</li>
 *     <li>Base/Exponent constructions (with expressions as base and exponent)</li>
 *     <li>Radical constructions (with an expression under a radical)</li>
 *     <li>N-th Root Constructions (with an integer root and an expression under a radical)</li>
 *     <li>Parenthesized expressions</li>
 *     <li>Vectors with expression components</li>
 *     <li>Matrices with expression entries</li>
 *     <li>Function invocations, with one or more expressions as arguments</li>
 *     <li>IF-THEN constructions</li>
 *     <li>IF-THEN-ELSE constructions</li>
 *     <li>SWITCH constructions</li>
 * </ul>
 *
 * <p>
 * Every object is either a "leaf", which represents a single "cursor step" in an expression, or is a "branch", or
 * compound object with subordinate content (either sub-expressions or integers or strings), where one cursor step
 * moves into the object, one cursor step moves from one child to the next, and one cursor step moves out of the object.
 *
 * <p>
 * Expression models support the following types of action:
 * <ul>
 *     <li>Insert one of the above things at a specified position</li>
 *     <li>Delete the object at a specified position</li>
 * </ul>
 */
package dev.mathops.assessment.expression.editmodel;
