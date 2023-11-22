/**
 * Expressions.
 * <p>
 * Every expression generates a value of one of the following types:
 * <ul>
 * <li>Long
 * <li>Double
 * <li>Boolean
 * <li>DocSimpleSpan
 * <li>IntegerVectorValue
 * <li>RealVectorValue
 * <li>ErrorValue
 * </ul>
 *
 * <pre>
 * AbstractFormulaObject
 *   BooleanValue [Boolean value]
 *   ErrorValue [String error]
 *   IntegerValue [Long value]
 *   RealValue [Double value]
 *   SpanValue [DocSimpleSpan value]
 *   VariableRef [String name, Integer index]
 *   AbstractFormulaContainer [List&lt;AbstractFormulaObject&gt; children]
 *     Formula [Map&lt;String,List&lt;VariableRef&gt;&gt; params]
 *     Function [String function]
 *     GroupingOper
 *     TestOperation
 *     VectorValue
 *     AbstractOper [char op]
 *       BinaryOper
 *       UnaryOper
 *
 * FormulaFactory
 * XmlFormulaFactory
 * </pre>
 */
package dev.mathops.assessment.formula;
