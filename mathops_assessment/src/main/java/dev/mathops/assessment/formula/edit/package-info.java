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
 * FEFormulaContainer - holds a single argument, generates a formula
 * FEConstantBool     - A fixed Boolean constant
 * FEConstantInt      - A fixed Integer constant
 * FEConstantReal     - A fixed Real constant
 * FEConstantSpan     - A fixed Span constant
 * FEVector           - A vector of N Integer or Real arguments
 * FEVarRef           - A reference to a variable with optional index
 * FEUnaryOper        - A unary operator with a single Integer or Real argument
 * FEBinaryOper       - A binary operator with two Integer, Real, or Boolean arguments
 * FEGrouping         - A container with a single Integer or Real argument
 * FEFunction         - A function of a single argument
 * FETest             - A Boolean-valued condition with two additional (compatible) arguments
 * </pre>
 */
package dev.mathops.assessment.formula.edit;
