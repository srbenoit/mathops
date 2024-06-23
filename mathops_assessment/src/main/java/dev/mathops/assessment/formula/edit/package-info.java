/**
 * Classes that support editing and generation of formulas.
 *
 * <p>
 * An editable formula tree is a tree of typed objects with specific rules about child types. Each object can be in a
 * valid or invalid state, and (when valid) can generate corresponding formula objects. These objects store enough
 * information to allow run-time editing of formulas.
 *
 * <p>
 * Every object contains a sequence of atomic "steps" over which a cursor can move, so that a complete formula can be
 * represented as a linear sequence of these steps, numbered from 0. A cursor position N, also numbered from 0, means
 * the cursor is positioned before step N, and a selection range from M to N covers steps M to N - 1.
 *
 * <pre>
 * FEBinaryOper            - A binary operator with two Integer, Real, or Boolean arguments
 * FEConstantBoolean       - A fixed Boolean constant
 * FEConstantError         - A fixed Error constant
 * FEConstantInteger       - A fixed Integer constant
 * FEConstantIntegerVector - A fixed Integer vector constant
 * FEConstantReal          - A fixed Real constant
 * FEConstantRealVector    - A fixed Real vector constant
 * FEConstantSpan          - A fixed Span constant
 * FEConstantString        - A fixed String constant
 * FECursor                - The current cursor
 * FEFormula               - Holds a single argument, generates a formula
 * FEFunction              - A function of a single argument
 * FEGrouping              - A container with a single Integer or Real argument
 * FEIsExact               - An exactness test
 * FESwitch                - A switch operation
 * FETest                  - A test (IF/THEN/ELSE) operation
 * FEUnaryOper             - A unary operator with a single Integer or Real argument
 * FEVarRef                - A reference to a variable with optional index
 * FEVector                - A vector that can generate integer or real vector values
 * </pre>
 */
package dev.mathops.assessment.formula.edit;
