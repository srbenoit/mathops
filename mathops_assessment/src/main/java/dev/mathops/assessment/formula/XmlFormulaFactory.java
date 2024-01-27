package dev.mathops.assessment.formula;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.CData;
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.NonemptyElement;

import java.util.List;
import java.util.Locale;

/**
 * This class constructs {@code Formula} objects from XML representations.
 */
public enum XmlFormulaFactory {
    ;

    /**
     * Generate a {@code Formula} object from a parsed XML element (this method allows this element to have any tag, not
     * just "formula").
     *
     * @param evalContext the evaluation context
     * @param element     the parsed XML element
     * @param mode        the parser mode
     * @return a {@code Formula} object if successful, {@code null} if unsuccessful
     */
    public static Formula extractFormula(final EvalContext evalContext, final NonemptyElement element,
                                         final EParserMode mode) {

        Formula result = null;

        final List<IElement> children = element.getElementChildrenAsList();

        if (children.size() == 1) {
            final IElement child = children.get(0);
            final String childTag = child.getTagName().toLowerCase(Locale.ROOT);

            AbstractFormulaObject root = null;

            if (child instanceof final EmptyElement empty) {
                switch (childTag) {
                    case "boolean" -> root = extractBoolean(empty);
                    case "integer" -> root = extractInteger(empty);
                    case "real" -> root = extractReal(empty);
                    case "string" -> root = extractString(empty);
                    case "int-vector" -> root = extractConstIntegerVector(empty);
                    case "real-vector" -> root = extractConstRealVector(empty);
                    case "varref" -> root = extractVarRef(empty);
                    default -> element.logError("An empty '" + childTag + "' element is not valid in this context.");
                }
            } else if (child instanceof final NonemptyElement nonempty) {

                switch (childTag) {
                    case "int-vector" -> root = extractIntegerVector(evalContext, nonempty, mode);
                    case "real-vector" -> root = extractRealVector(evalContext, nonempty, mode);
                    case "vector" -> {
                        if (mode == EParserMode.NORMAL) {
                            element.logError("Deprecated 'vector' tag");
                        }
                        root = extractIntegerVector(evalContext, nonempty, mode);
                    }
                    case "span" -> root = extractSpan(evalContext, nonempty, mode);
                    case "error" -> root = extractError(nonempty);
                    case "binary" -> root = extractBinaryOp(evalContext, nonempty, mode);
                    case "unary" -> root = extractUnaryOp(evalContext, nonempty, mode);
                    case "formula" -> root = extractFormula(evalContext, nonempty, mode);
                    case "function" -> root = extractFunction(evalContext, nonempty, mode);
                    case "grouping" -> {
                        if (mode == EParserMode.NORMAL) {
                            element.logError("Deprecated 'grouping' tag");
                        }
                        root = extractGrouping(evalContext, nonempty, mode);
                    }
                    case "test" -> root = extractTest(evalContext, nonempty, mode);
                    case "switch" -> root = extractSwitch(evalContext, nonempty, mode);
                    case "is-exact" -> root = extractIsExact(evalContext, nonempty, mode);
                    default -> element.logError("A nonempty '" + childTag + "' element is not valid in this context.");
                }
            }
            result = root == null ? null : new Formula(root);
        } else {
            element.logError("'formula' element must have exactly one child");
        }

        return result;
    }

    /**
     * Generate a {@code ConstBooleanValue} object from XML source.
     *
     * <pre>
     * &lt;boolean value='true|false'/&gt;
     * </pre>
     *
     * @param element the element from which to extract the object
     * @return a {@code ConstBooleanValue} object if successful, {@code null} if unsuccessful
     */
    private static ConstBooleanValue extractBoolean(final EmptyElement element) {

        ConstBooleanValue result = null;

        final String str = element.getStringAttr("value").toLowerCase(Locale.ROOT);

        if ("true".equals(str)) {
            result = new ConstBooleanValue(true);
        } else if ("false".equals(str)) {
            result = new ConstBooleanValue(false);
        } else {
            element.logError("Invalid constant boolean value: " + str);
        }

        return result;
    }

    /**
     * Generate a {@code ConstIntegerValue} object from XML source.
     *
     * <pre>
     * &lt;integer value='...'/&gt;
     * </pre>
     *
     * @param element the element from which to extract the object
     * @return a {@code ConstIntegerValue} object if successful, {@code null} if unsuccessful
     */
    private static ConstIntegerValue extractInteger(final EmptyElement element) {

        ConstIntegerValue result = null;

        final String str = element.getStringAttr("value");

        try {
            result = new ConstIntegerValue(Long.parseLong(str));
        } catch (final NumberFormatException ex) {
            element.logError("Invalid constant integer value: " + str);
        }

        return result;
    }

    /**
     * Generate a {@code ConstRealValue} object from XML source.
     *
     * <pre>
     * &lt;real value='...'/&gt;
     * </pre>
     *
     * @param element the element from which to extract the object
     * @return a {@code ConstRealValue} object if successful, {@code null} if unsuccessful
     */
    private static ConstRealValue extractReal(final EmptyElement element) {

        ConstRealValue result = null;

        final String str = element.getStringAttr("value");

        try {
            final Number parsed = NumberParser.parse(str);
            result = new ConstRealValue(parsed);
        } catch (final NumberFormatException ex) {
            element.logError("Invalid constant real value: " + str);
        }

        return result;
    }

    /**
     * Generate a {@code ConstStringValue} object from XML source.
     *
     * <pre>
     * &lt;string value='...'/&gt;
     * </pre>
     *
     * @param element the element from which to extract the object
     * @return a {@code ConstStringValue} object if successful, {@code null} if unsuccessful
     */
    private static ConstStringValue extractString(final IElement element) {

        final String str = element.getStringAttr("value");

        return new ConstStringValue(str);
    }

    /**
     * Generate a {@code ConstIntegerVector} object from XML source.
     *
     * <pre>
     * &lt;int-vector value='...'/&gt;
     * </pre>
     *
     * @param element the element from which to extract the object
     * @return a {@code ConstIntegerVector} object if successful, {@code null} if unsuccessful
     */
    private static ConstIntegerVector extractConstIntegerVector(final EmptyElement element) {

        ConstIntegerVector result = null;

        final String str = element.getStringAttr("value");
        try {
            result = new ConstIntegerVector(IntegerVectorValue.parse(str));
        } catch (final NumberFormatException ex) {
            element.logError("Invalid constant integer vector value: " + str);
        }

        return result;
    }

    /**
     * Generate a {@code ConstRealVector} object from XML source.
     *
     * <pre>
     * &lt;vector&gt; ...children... &lt;/vector&gt;
     * </pre>
     *
     * @param element the element from which to extract the object
     * @return a {@code ConstRealVector} object if successful, {@code null} if unsuccessful
     */
    private static ConstRealVector extractConstRealVector(final EmptyElement element) {

        ConstRealVector result = null;

        final String str = element.getStringAttr("value");
        try {
            result = new ConstRealVector(RealVectorValue.parse(str));
        } catch (final NumberFormatException ex) {
            element.logError("Invalid constant real vector value: " + str);
        }

        return result;
    }

    /**
     * Generate a {@code IntegerFormulaVector} object from XML source.
     *
     * <pre>
     * &lt;int-vector&gt; ...children... &lt;/int-vector&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param element     the element from which to extract the object
     * @param mode        the parser mode
     * @return a {@code IntegerFormulaVector} object if successful, {@code null} if unsuccessful
     */
    private static IntegerFormulaVector extractIntegerVector(final EvalContext evalContext,
                                                             final NonemptyElement element, final EParserMode mode) {

        final IntegerFormulaVector result = new IntegerFormulaVector();

        extractChildren(evalContext, element, result, mode);

        return result;
    }

    /**
     * Generate a {@code RealFormulaVector} object from XML source.
     *
     * <pre>
     * &lt;vector&gt; ...children... &lt;/vector&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param element     the element from which to extract the object
     * @param mode        the parser mode
     * @return a {@code RealFormulaVector} object if successful, {@code null} if unsuccessful
     */
    private static RealFormulaVector extractRealVector(final EvalContext evalContext, final NonemptyElement element,
                                                       final EParserMode mode) {

        final RealFormulaVector result = new RealFormulaVector();

        extractChildren(evalContext, element, result, mode);

        return result;
    }

    /**
     * Generate a {@code ConstSpanValue} object from XML source.
     *
     * <pre>
     * &lt;span&gt; ...simple span... &lt;/span&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param element     the element from which to extract the object
     * @param mode        the parser mode
     * @return a {@code ConstSpanValue} object if successful, {@code null} if unsuccessful
     */
    private static ConstSpanValue extractSpan(final EvalContext evalContext, final NonemptyElement element,
                                              final EParserMode mode) {

        ConstSpanValue result = null;

        final DocSimpleSpan span = DocFactory.parseSpan(evalContext, element, mode);

        if (span != null) {
            result = new ConstSpanValue(span);
        }

        return result;
    }

    /**
     * Generate an {@code ErrorValue} object from XML source.
     *
     * <pre>
     * &lt;error/&gt; ...message... &lt;/error&gt;
     * </pre>
     *
     * @param element the element from which to extract the object
     * @return a {@code ErrorValue} object if successful, {@code null} if unsuccessful
     */
    private static ErrorValue extractError(final NonemptyElement element) {

        ErrorValue result = null;

        if (element.getNumChildren() == 1) {
            final INode child = element.getChild(0);
            if (child instanceof final CData cdata) {
                result = new ErrorValue(cdata.content);
            } else {
                element.logError("'error' element must have simple text content");
            }
        } else {
            element.logError("'error' element must have simple text content");
        }

        return result;
    }

    /**
     * Generate a {@code VariableRef} object from XML source.
     *
     * <pre>
     * &lt;varref name='...' index='...'/&gt;
     * </pre>
     *
     * @param element the element from which to extract the object
     * @return a {@code VariableRef} object if successful, {@code null} if unsuccessful
     */
    private static VariableRef extractVarRef(final EmptyElement element) {

        VariableRef result = null;

        final String name = element.getStringAttr("name");
        if (name == null || name.isEmpty()) {
            element.logError("'varref' element must have nonempty 'name' attribute");
        } else {
            result = new VariableRef(name);

            if (element.hasAttribute("index")) {
                try {
                    result.index = element.getIntegerAttr("index", null);
                } catch (final ParsingException ex) {
                    element.logError("Invalid variable index");
                }
            }
        }

        return result;
    }

    /**
     * Generate a {@code BinaryOper} object from XML source.
     *
     * <pre>
     * &lt;binary op='...'&gt; ...children... &lt;/binary&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param element     the element from which to extract the object
     * @param mode        the parser mode
     * @return a {@code BinaryOper} object if successful, {@code null} if unsuccessful
     */
    private static BinaryOper extractBinaryOp(final EvalContext evalContext, final NonemptyElement element,
                                              final EParserMode mode) {

        BinaryOper result = null;

        final String oper = element.getStringAttr("op");

        if (oper.length() == 1) {
            final char opChar = oper.charAt(0);
            final EBinaryOp op = EBinaryOp.forOp(opChar);

            if (op == null) {
                element.logError("Invalid binary operator: " + oper + " (\\u" + Integer.toHexString(opChar) + ").");
            } else {
                result = new BinaryOper(op);
                extractChildren(evalContext, element, result, mode);
            }
        } else {
            element.logError("invalid binary operator: " + oper);
        }

        return result;
    }

    /**
     * Generate a {@code UnaryOper} object from XML source.
     *
     * <pre>
     * &lt;unary op='...'&gt; ...children... &lt;/unary&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param element     the element from which to extract the object
     * @param mode        the parser mode
     * @return a {@code UnaryOper} object if successful, {@code null} if unsuccessful
     */
    private static UnaryOper extractUnaryOp(final EvalContext evalContext, final NonemptyElement element,
                                            final EParserMode mode) {

        UnaryOper result = null;

        final String oper = element.getStringAttr("op");

        if (oper.length() == 1) {
            final char opChar = oper.charAt(0);
            final EUnaryOp op = EUnaryOp.forOp(opChar);

            if (op == null) {
                element.logError("invalid unary operator: " + oper);
            } else {
                result = new UnaryOper(op);
                extractChildren(evalContext, element, result, mode);
            }
        } else {
            element.logError("invalid unary operator: " + oper);
        }

        return result;
    }

    /**
     * Generate a {@code Function} object from XML source.
     *
     * <pre>
     * &lt;function name='...'&gt; ...children... &lt;/function&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param element     the element from which to extract the object
     * @param mode        the parser mode
     * @return a {@code Function} object if successful, {@code null} if unsuccessful
     */
    private static Function extractFunction(final EvalContext evalContext, final NonemptyElement element,
                                            final EParserMode mode) {

        Function result = null;

        final String name = element.getStringAttr("name");
        if (name == null || name.isEmpty()) {
            element.logError("'function' element must have nonempty 'name' attribute");
        } else {
            final EFunction which = EFunction.forName(name);

            if (which == null) {
                element.logError("Invalid function name: " + name);
            } else {
                result = new Function(which);
                extractChildren(evalContext, element, result, mode);
            }
        }

        return result;
    }

    /**
     * Generate a {@code GroupingOper} object from XML source.
     *
     * <pre>
     * &lt;grouping&gt; ...children... &lt;/grouping&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param element     the element from which to extract the object
     * @param mode        the parser mode
     * @return a {@code GroupingOper} object if successful, {@code null} if unsuccessful
     */
    private static GroupingOper extractGrouping(final EvalContext evalContext, final NonemptyElement element,
                                                final EParserMode mode) {

        final GroupingOper result = new GroupingOper();
        extractChildren(evalContext, element, result, mode);

        return result;
    }

    /**
     * Generate a {@code TestOper} object from XML source.
     *
     * <pre>
     * &lt;test&gt; ...children... &lt;/test&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param element     the element from which to extract the object
     * @param mode        the parser mode
     * @return a {@code TestOper} object if successful, {@code null} if unsuccessful
     */
    private static TestOper extractTest(final EvalContext evalContext, final NonemptyElement element,
                                        final EParserMode mode) {

        final TestOper result = new TestOper();
        extractChildren(evalContext, element, result, mode);

        return result;
    }

    /**
     * Generate a {@code SwitchOper} object from XML source.
     *
     * <pre>
     * &lt;switch&gt;
     *    &lt;condition&gt; ... &lt;/condition&gt;
     *    &lt;case value='M'&gt; ... &lt;/case&gt;
     *    &lt;default&gt; ... &lt;/default&gt;
     * &lt;/switch&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param element     the element from which to extract the object
     * @param mode        the parser mode
     * @return a {@code SwitchOper} object if successful, {@code null} if unsuccessful
     */
    private static SwitchOper extractSwitch(final EvalContext evalContext, final NonemptyElement element,
                                            final EParserMode mode) {

        final SwitchOper result = new SwitchOper();

        final List<IElement> children = element.getElementChildrenAsList();
        for (final IElement child : children) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = child.getTagName();

                if ("condition".equals(childTag)) {
                    final Formula form = extractFormula(evalContext, nonempty, mode);
                    if (form.numChildren() == 1) {
                        result.condition = form.getChild(0);
                    } else {
                        result.condition = form;
                    }
                } else if ("case".equals(childTag)) {
                    try {
                        final Integer toMatch = child.getIntegerAttr("value", null);

                        if (toMatch == null) {
                            child.logError("'case' element is missing 'value' attribute.");
                            break;
                        }

                        final Formula form = extractFormula(evalContext, nonempty, mode);
                        if (form == null) {
                            child.logError("Missing formula in 'case' element.");
                        } else if (form.numChildren() == 1) {
                            result.cases.add(new SwitchCase(toMatch.intValue(), form.getChild(0)));
                        } else {
                            result.cases.add(new SwitchCase(toMatch.intValue(), form));
                        }
                    } catch (final ParsingException ex) {
                        child.logError("'case' element has invalid 'value' attribute.");
                    }
                } else if ("default".equals(childTag)) {
                    final Formula form = extractFormula(evalContext, nonempty, mode);
                    if (form.numChildren() == 1) {
                        result.defaultValue = form.getChild(0);
                    } else {
                        result.defaultValue = form;
                    }
                }
            } else {
                child.logError("Unexpected empty element child of 'case' element");
                break;
            }
        }

        return result;
    }

    /**
     * Generate a {@code IsExactOper} object from XML source.
     *
     * <pre>
     * &lt;is-exact&gt; ...children... &lt;/is-exact&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param element     the element from which to extract the object
     * @param mode        the parser mode
     * @return a {@code IsExactOper} object if successful, {@code null} if unsuccessful
     */
    private static IsExactOper extractIsExact(final EvalContext evalContext, final NonemptyElement element,
                                              final EParserMode mode) {

        final IsExactOper result = new IsExactOper();
        extractChildren(evalContext, element, result, mode);

        return result;
    }

    /**
     * Given a nonempty element, extracts all child elements as formula objects and adds them to a specified container.
     *
     * @param evalContext the evaluation context
     * @param element     the element to parse
     * @param container   the container to which to add objects parsed from child elements
     * @param mode        the parser mode
     */
    private static void extractChildren(final EvalContext evalContext, final NonemptyElement element,
                                        final AbstractFormulaContainer container, final EParserMode mode) {

        final List<IElement> children = element.getElementChildrenAsList();
        for (final IElement child : children) {
            final String childTag = child.getTagName();
            AbstractFormulaObject extracted = null;

            if (child instanceof final EmptyElement empty) {
                if ("boolean".equals(childTag)) {
                    extracted = extractBoolean(empty);
                } else if ("integer".equals(childTag)) {
                    extracted = extractInteger(empty);
                } else if ("real".equals(childTag)) {
                    extracted = extractReal(empty);
                } else if ("string".equals(childTag)) {
                    extracted = extractString(empty);
                } else if ("int-vector".equals(childTag)) {
                    extracted = extractConstIntegerVector(empty);
                } else if ("real-vector".equals(childTag)) {
                    extracted = extractConstRealVector(empty);
                } else if ("varref".equals(childTag)) {
                    extracted = extractVarRef(empty);
                } else {
                    element.logError("An empty '" + childTag + "' element is not valid in this context:"
                            + element.print(0));
                }

                if (extracted != null) {
                    container.addChild(extracted);
                }
            } else if (child instanceof final NonemptyElement nonempty) {

                if ("int-vector".equals(childTag)) {
                    extracted = extractIntegerVector(evalContext, nonempty, mode);
                } else if ("real-vector".equals(childTag)) {
                    extracted = extractRealVector(evalContext, nonempty, mode);
                } else if ("vector".equals(childTag)) {
                    if (mode == EParserMode.NORMAL) {
                        element.logError("Deprecated 'vector' tag");
                    }
                    extracted = extractIntegerVector(evalContext, nonempty, mode);
                } else if ("span".equals(childTag)) {
                    extracted = extractSpan(evalContext, nonempty, mode);
                } else if ("error".equals(childTag)) {
                    extracted = extractError(nonempty);
                } else if ("binary".equals(childTag)) {
                    extracted = extractBinaryOp(evalContext, nonempty, mode);
                } else if ("unary".equals(childTag)) {
                    extracted = extractUnaryOp(evalContext, nonempty, mode);
                } else if ("formula".equals(childTag)) {
                    extracted = extractFormula(evalContext, nonempty, mode);
                } else if ("function".equals(childTag)) {
                    extracted = extractFunction(evalContext, nonempty, mode);
                } else if ("grouping".equals(childTag)) {
                    if (mode == EParserMode.NORMAL) {
                        element.logError("Deprecated 'grouping' tag");
                    }
                    extracted = extractGrouping(evalContext, nonempty, mode);
                } else if ("test".equals(childTag)) {
                    extracted = extractTest(evalContext, nonempty, mode);
                } else if ("switch".equals(childTag)) {
                    extracted = extractSwitch(evalContext, nonempty, mode);
                } else if ("is-exact".equals(childTag)) {
                    extracted = extractIsExact(evalContext, nonempty, mode);
                } else {
                    element.logError("A nonempty '" + childTag + "' element is not valid in this context:"
                            + element.print(0));
                }

                if (extracted != null) {
                    container.addChild(extracted);
                }
            }
        }
    }

    ///**
    // * Main method for testing.
    // *
    // * @param args command-line arguments
    // */
    // public static void main(final String... args) {
    //
    // final String xml = SimpleBuilder.concat(//
    // "<formula>",
    // " <binary op='='>",
    // " <string value='x'/>",
    // " <function name='lcase'><varref name='c'/></function>",
    // " </binary>",
    // "</formula>");
    //
    // try {
    // final XmlContent content = new XmlContent(xml, true, false);
    // final IElement top = content.getToplevel();
    //
    // final List<XmlContentError> errors = content.getAllErrors();
    // for (final XmlContentError error : errors) {
    // Log.warning(error);
    // }
    //
    // if (top instanceof NonemptyElement nonempt) {
    // final Formula formula = extractFormula(nonempt, EParserMode.NORMAL);
    //
    // final HtmlBuilder builder = new HtmlBuilder(100);
    // formula.appendXml(builder);
    // Log.fine(builder.toString());
    // }
    //
    // } catch (final ParsingException ex) {
    // Log.warning(ex);
    // }
    // }
}
