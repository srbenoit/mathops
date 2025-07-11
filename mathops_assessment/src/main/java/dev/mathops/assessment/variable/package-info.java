/**
 * Objects related to parametrized variables for random item generation.
 *
 * <pre>
 * AbstractXmlObject
 *   AbstractVariable [String name, Object value]
 *     VariableBoolean
 *     VariableRandomBoolean
 *     VariableSpan
 *     AbstractFormattableVariable {String formatPattern, DecimalFormat format]
 *       VariableDerived {IRangedVariable, IExcludableVariable} [Formula min, Formula max, Formula[] exclude,
 *           Formula formula]
 *       VariableInputInt
 *       VariableInputReal
 *       VariableInt
 *       VariableRandomChoice [Formula[] exclude, Formula[] chooseFrom]
 *       VariableRandomInt [Formula min, Formula max, Formula[] exclude]
 *       VariableRandomReal [Formula min, Formula max]
 *       VariableReal
 *
 * VariableFactory (parses from XML)
 *
 * EvalContext [long seed, Map<String,Var> vars, Random rand, boolean retry, boolean printTarget]
 * </pre>
 */
package dev.mathops.assessment.variable;
