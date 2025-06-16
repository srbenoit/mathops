package dev.mathops.assessment.formula;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.CData;
import dev.mathops.text.parser.xml.EmptyElement;
import dev.mathops.text.parser.xml.IElement;
import dev.mathops.text.parser.xml.INode;
import dev.mathops.text.parser.xml.NonemptyElement;

import java.util.List;
import java.util.Locale;

/**
 * This class constructs {@code Formula} objects from XML representations.
 */
public enum XmlFormulaFactory {
    ;

    /** A commonly-used String. */
    private static final String BOOLEAN = "boolean";

    /** A commonly-used String. */
    private static final String INTEGER = "integer";

    /** A commonly-used String. */
    private static final String REAL = "real";

    /** A commonly-used String. */
    private static final String STRING = "string";

    /** A commonly-used String. */
    private static final String INT_VECTOR = "int-vector";

    /** A commonly-used String. */
    private static final String REAL_VECTOR = "real-vector";

    /** A commonly-used String. */
    private static final String VECTOR = "vector";

    /** A commonly-used String. */
    private static final String VARREF = "varref";

    /** A commonly-used String. */
    private static final String SPAN = "span";

    /** A commonly-used String. */
    private static final String ERROR = "error";

    /** A commonly-used String. */
    private static final String BINARY = "binary";

    /** A commonly-used String. */
    private static final String UNARY = "unary";

    /** A commonly-used String. */
    private static final String FORMULA = "formula";

    /** A commonly-used String. */
    private static final String EXPR = "expr";

    /** A commonly-used String. */
    private static final String FUNCTION = "function";

    /** A commonly-used String. */
    private static final String GROUPING = "grouping";

    /** A commonly-used String. */
    private static final String TEST = "test";

    /** A commonly-used String. */
    private static final String SWITCH = "switch";

    /** A commonly-used String. */
    private static final String IS_EXACT = "is-exact";

    /** A commonly-used String. */
    private static final String VALUE = "value";

    /** A commonly-used String. */
    private static final String TRUE = "TRUE";

    /** A commonly-used String. */
    private static final String FALSE = "FALSE";

    /** A commonly-used String. */
    private static final String INDEX = "index";

    /** A commonly-used String. */
    private static final String OP = "op";

    /** A commonly-used String. */
    private static final String NAME = "name";

    /** A commonly-used String. */
    private static final String CONDITION = "condition";

    /** A commonly-used String. */
    private static final String CASE = "case";

    /** A commonly-used String. */
    private static final String DEFAULT = "default";

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

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                // Realized formulas serialized will have this
                if (VALUE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10000)";
                element.logError(msg);
            }
        }

        final List<IElement> children = element.getElementChildrenAsList();

        if (children.size() == 1) {
            final IElement child = children.getFirst();
            final String childTag = child.getTagName().toLowerCase(Locale.ROOT);

            AbstractFormulaObject root = null;

            if (child instanceof final EmptyElement empty) {
                switch (childTag) {
                    case BOOLEAN -> root = extractBoolean(empty, mode);
                    case INTEGER -> root = extractInteger(empty, mode);
                    case REAL -> root = extractReal(empty, mode);
                    case STRING -> root = extractString(empty, mode);
                    case INT_VECTOR -> root = extractConstIntegerVector(empty, mode);
                    case REAL_VECTOR -> root = extractConstRealVector(empty, mode);
                    case VARREF -> root = extractVarRef(empty, mode);
                    default -> {
                        if (mode.reportAny) {
                            final String msg = Res.fmt(Res.BAD_EMPTY_ELEM, childTag) + " (10001)";
                            element.logError(msg);
                        }
                    }
                }
            } else if (child instanceof final NonemptyElement nonempty) {

                switch (childTag) {
                    case INT_VECTOR -> root = extractIntegerVector(evalContext, nonempty, mode);
                    case REAL_VECTOR -> root = extractRealVector(evalContext, nonempty, mode);
                    case VECTOR -> {
                        if (mode.reportDeprecated) {
                            final String msg = Res.fmt(Res.DEPRECATED_TAG, VECTOR) + " (10002)";
                            element.logError(msg);
                        }
                        root = extractIntegerVector(evalContext, nonempty, mode);
                    }
                    case SPAN -> root = extractSpan(evalContext, nonempty, mode);
                    case ERROR -> root = extractError(nonempty, mode);
                    case BINARY -> root = extractBinaryOp(evalContext, nonempty, mode);
                    case UNARY -> root = extractUnaryOp(evalContext, nonempty, mode);
                    case FORMULA, EXPR -> root = extractFormula(evalContext, nonempty, mode);
                    case FUNCTION -> root = extractFunction(evalContext, nonempty, mode);
                    case GROUPING -> {
                        if (mode.reportDeprecated) {
                            final String msg = Res.fmt(Res.DEPRECATED_TAG, GROUPING) + " (10003)";
                            element.logError(msg);
                        }
                        root = extractGrouping(evalContext, nonempty, mode);
                    }
                    case TEST -> root = extractTest(evalContext, nonempty, mode);
                    case SWITCH -> root = extractSwitch(evalContext, nonempty, mode);
                    case IS_EXACT -> root = extractIsExact(evalContext, nonempty, mode);
                    default -> {
                        if (mode.reportAny) {
                            final String msg = Res.fmt(Res.BAD_NONEMPTY_ELEM, childTag) + " (10004)";
                            element.logError(msg);
                        }
                    }
                }
            }

            if (mode.reportAny && root instanceof Formula) {
                final String msg = Res.get(Res.EXPR_WITH_EXPR_CHILD) + " (10005)";
                element.logError(msg);
            }

            result = root == null ? null : new Formula(root);

        } else if (mode.reportAny) {
            final String tagName = element.getTagName();
            final String msg = Res.fmt(Res.ELEM_MUST_HAVE_ONE_CHILD, tagName) + " (10006)";
            element.logError(msg);
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
     * @param mode    the parser mode
     * @return a {@code ConstBooleanValue} object if successful, {@code null} if unsuccessful
     */
    private static ConstBooleanValue extractBoolean(final EmptyElement element, final EParserMode mode) {

        ConstBooleanValue result = null;

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                if (VALUE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10010)";
                element.logError(msg);
            }
        }

        final String str = element.getStringAttr(VALUE).toUpperCase(Locale.ROOT);

        if (TRUE.equals(str)) {
            result = new ConstBooleanValue(true);
        } else if (FALSE.equals(str)) {
            result = new ConstBooleanValue(false);
        } else if (mode.reportAny) {
            final String msg = Res.fmt(Res.BAD_CONST_BOOLEAN, str) + " (10011)";
            element.logError(msg);
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
     * @param mode    the parser mode
     * @return a {@code ConstIntegerValue} object if successful, {@code null} if unsuccessful
     */
    private static ConstIntegerValue extractInteger(final EmptyElement element, final EParserMode mode) {

        ConstIntegerValue result = null;

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                if (VALUE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10020)";
                element.logError(msg);
            }
        }

        final String str = element.getStringAttr(VALUE);

        try {
            final long parsed = Long.parseLong(str);
            result = new ConstIntegerValue(parsed);
        } catch (final NumberFormatException ex) {
            if (mode.reportAny) {
                final String msg = Res.fmt(Res.BAD_CONST_INT, str) + " (10021)";
                element.logError(msg);
            }
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
     * @param mode    the parser mode
     * @return a {@code ConstRealValue} object if successful, {@code null} if unsuccessful
     */
    private static ConstRealValue extractReal(final EmptyElement element, final EParserMode mode) {

        ConstRealValue result = null;

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                if (VALUE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10030)";
                element.logError(msg);
            }
        }

        final String str = element.getStringAttr(VALUE);

        try {
            final Number parsed = NumberParser.parse(str);
            result = new ConstRealValue(parsed);
        } catch (final NumberFormatException ex) {
            if (mode.reportAny) {
                final String msg = Res.fmt(Res.BAD_CONST_REAL, str) + " (10031)";
                element.logError(msg);
            }
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
     * @param mode    the parser mode
     * @return a {@code ConstStringValue} object if successful, {@code null} if unsuccessful
     */
    private static ConstStringValue extractString(final IElement element, final EParserMode mode) {

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                if (VALUE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10040)";
                element.logError(msg);
            }
        }

        final String str = element.getStringAttr(VALUE);

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
     * @param mode    the parser mode
     * @return a {@code ConstIntegerVector} object if successful, {@code null} if unsuccessful
     */
    private static ConstIntegerVector extractConstIntegerVector(final EmptyElement element, final EParserMode mode) {

        ConstIntegerVector result = null;

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                if (VALUE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10050)";
                element.logError(msg);
            }
        }

        final String str = element.getStringAttr(VALUE);
        try {
            final IntegerVectorValue parsed = IntegerVectorValue.parse(str);
            result = new ConstIntegerVector(parsed);
        } catch (final NumberFormatException ex) {
            if (mode.reportAny) {
                final String msg = Res.fmt(Res.BAD_CONST_INT_VECTOR, str) + " (10051)";
                element.logError(msg);
            }
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
     * @param mode    the parser mode
     * @return a {@code ConstRealVector} object if successful, {@code null} if unsuccessful
     */
    private static ConstRealVector extractConstRealVector(final EmptyElement element, final EParserMode mode) {

        ConstRealVector result = null;

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                if (VALUE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10060)";
                element.logError(msg);
            }
        }

        final String str = element.getStringAttr(VALUE);
        try {
            final RealVectorValue parsed = RealVectorValue.parse(str);
            result = new ConstRealVector(parsed);
        } catch (final NumberFormatException ex) {
            if (mode.reportAny) {
                final String msg = Res.fmt(Res.BAD_CONST_REAL_VECTOR, str) + " (10061)";
                element.logError(msg);
            }
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

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10070)";
                element.logError(msg);
            }
        }

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

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10071)";
                element.logError(msg);
            }
        }

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

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10080)";
                element.logError(msg);
            }
        }

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
     * @param mode    the parser mode
     * @return a {@code ErrorValue} object if successful, {@code null} if unsuccessful
     */
    private static ErrorValue extractError(final NonemptyElement element, final EParserMode mode) {

        ErrorValue result = null;

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10090)";
                element.logError(msg);
            }
        }

        if (element.getNumChildren() == 1) {
            final INode child = element.getChild(0);
            if (child instanceof final CData cdata) {
                result = new ErrorValue(cdata.content);
            } else if (mode.reportAny) {
                final String msg = Res.get(Res.ERROR_ELEM_TEXT_ONLY) + " (10091)";
                element.logError(msg);
            }
        } else if (mode.reportAny) {
            final String msg = Res.get(Res.ERROR_ELEM_TEXT_ONLY) + " (10092)";
            element.logError(msg);
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
     * @param mode    the parser mode
     * @return a {@code VariableRef} object if successful, {@code null} if unsuccessful
     */
    private static VariableRef extractVarRef(final EmptyElement element, final EParserMode mode) {

        VariableRef result = null;

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                if (NAME.equals(attrName) || INDEX.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10100)";
                element.logError(msg);
            }
        }

        final String name = element.getStringAttr(NAME);
        if (name == null || name.isEmpty()) {
            if (mode.reportAny) {
                final String msg = Res.get(Res.VARREF_MISSING_NAME) + " (10101)";
                element.logError(msg);
            }
        } else {
            result = new VariableRef(name);

            if (element.hasAttribute(INDEX)) {
                try {
                    result.index = element.getIntegerAttr(INDEX, null);
                } catch (final ParsingException ex) {
                    if (mode.reportAny) {
                        final String msg = Res.get(Res.BAD_VAR_INDEX) + " (10102)";
                        element.logError(msg);
                    }
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

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                if (OP.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10110)";
                element.logError(msg);
            }
        }

        final String oper = element.getStringAttr(OP);

        switch (oper) {
            case "+" -> result = new BinaryOper(EBinaryOp.ADD);
            case "-" -> result = new BinaryOper(EBinaryOp.SUBTRACT);
            case "*" -> result = new BinaryOper(EBinaryOp.MULTIPLY);
            case "/" -> result = new BinaryOper(EBinaryOp.DIVIDE);
            case "^" -> result = new BinaryOper(EBinaryOp.POWER);
            case "%" -> result = new BinaryOper(EBinaryOp.REMAINDER);
            case "<", "LT" -> result = new BinaryOper(EBinaryOp.LT);
            case ">", "GT" -> result = new BinaryOper(EBinaryOp.GT);
            case "\u2264", "LE", "LEQ" -> result = new BinaryOper(EBinaryOp.LE);
            case "\u2265", "GE", "GEQ" -> result = new BinaryOper(EBinaryOp.GE);
            case "=" -> result = new BinaryOper(EBinaryOp.EQ);
            case "~" -> result = new BinaryOper(EBinaryOp.APPROX);
            case "\u2260", "NE", "NEQ" -> result = new BinaryOper(EBinaryOp.NE);
            case "&", "AND" -> result = new BinaryOper(EBinaryOp.AND);
            case "|", "OR" -> result = new BinaryOper(EBinaryOp.OR);
            case null, default -> {
                final String msg = Res.fmt(Res.BAD_BINARY_OP, oper) + " (10111)";
                element.logError(msg);
            }
        }

        if (result != null) {
            extractChildren(evalContext, element, result, mode);

            if (mode.reportAny) {
                // Check for opportunities to streamline n-ary operators
                if (result.op == EBinaryOp.AND) {
                    final int count = result.numChildren();
                    for (int i = 0; i < count; ++i) {
                        if (result.getChild(i) instanceof final BinaryOper child && child.op == EBinaryOp.AND) {
                            element.logError("Nested AND operators - simplify? (10112)");
                        }
                    }
                } else if (result.op == EBinaryOp.OR) {
                    final int count = result.numChildren();
                    for (int i = 0; i < count; ++i) {
                        if (result.getChild(i) instanceof final BinaryOper child && child.op == EBinaryOp.OR) {
                            element.logError("Nested OR operators - simplify? (10113)");
                        }
                    }
                } else if (result.op == EBinaryOp.ADD) {
                    final int count = result.numChildren();
                    for (int i = 0; i < count; ++i) {
                        if (result.getChild(i) instanceof final BinaryOper child && child.op == EBinaryOp.ADD) {
                            element.logError("Nested ADD operators - simplify? (10114)");
                        }
                    }
                } else if (result.op == EBinaryOp.MULTIPLY) {
                    final int count = result.numChildren();
                    for (int i = 0; i < count; ++i) {
                        if (result.getChild(i) instanceof final BinaryOper child && child.op == EBinaryOp.MULTIPLY) {
                            element.logError("Nested MULTIPLY operators - simplify? (10115)");
                        }
                    }
                }
            }
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

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                if (OP.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10120)";
                element.logError(msg);
            }
        }

        final String oper = element.getStringAttr(OP);

        if ("+".equals(oper)) {
            result = new UnaryOper(EUnaryOp.PLUS);
            if (mode.reportAny) {
                element.logError("Useless unary '+' operator. (10121)");
            }
        } else if ("-".equals(oper)) {
            result = new UnaryOper(EUnaryOp.MINUS);
        } else {
            final String msg = Res.fmt(Res.BAD_UNARY_OP, oper) + " (10122)";
            element.logError(msg);
        }

        if (result != null) {
            extractChildren(evalContext, element, result, mode);

            if (mode.reportAny && result.numChildren() == 1) {
                final AbstractFormulaObject child = result.getChild(0);
                if (child != null && child.isConstant()) {
                    element.logError("Unary '-' operator applied to constant - simplify?. (10123)");
                }
            }
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

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                if (NAME.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10130)";
                element.logError(msg);
            }
        }

        final String name = element.getStringAttr(NAME);
        if (name == null || name.isEmpty()) {
            if (mode.reportAny) {
                final String msg = Res.get(Res.FUNCTION_MISSING_NAME) + " (10131)";
                element.logError(msg);
            }
        } else {
            final EFunction which = EFunction.forName(name);

            if (which == null) {
                if (mode.reportAny) {
                    final String msg = Res.fmt(Res.BAD_FUNCTION_NAME, name + " (10132)");
                    element.logError(msg);
                }
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

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10150)";
                element.logError(msg);
            }
        }

        extractChildren(evalContext, element, result, mode);

        if (mode.reportAny && result.numChildren() != 3) {
            element.logError("'test' element should have exactly three children (10151)");
        }

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

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10160)";
                element.logError(msg);
            }
        }

        final List<IElement> children = element.getElementChildrenAsList();
        for (final IElement child : children) {
            if (child instanceof final NonemptyElement nonempty) {
                final String childTag = child.getTagName();

                if (CONDITION.equals(childTag)) {
                    if (mode.reportAny) {
                        for (final String attrName : nonempty.attributeNames()) {
                            final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10161)";
                            nonempty.logError(msg);
                        }
                    }

                    final Formula form = extractFormula(evalContext, nonempty, mode);
                    if (form.numChildren() == 1) {
                        result.condition = form.getChild(0);
                    } else {
                        result.condition = form;
                    }
                } else if (CASE.equals(childTag)) {
                    if (mode.reportAny) {
                        for (final String attrName : element.attributeNames()) {
                            if (VALUE.equals(attrName)) {
                                continue;
                            }
                            final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10162)";
                            element.logError(msg);
                        }
                    }

                    try {
                        final Integer toMatch = child.getIntegerAttr(VALUE, null);

                        if (toMatch == null) {
                            if (mode.reportAny) {
                                final String msg = Res.get(Res.MISSING_CASE_VALUE) + " (10163)";
                                child.logError(msg);
                            }
                            break;
                        }

                        final Formula form = extractFormula(evalContext, nonempty, mode);
                        if (form == null) {
                            if (mode.reportAny) {
                                final String msg = Res.get(Res.MISSING_CASE_FORMULA) + " (10164)";
                                child.logError(msg);
                            }
                        } else {
                            final int valueToMatch = toMatch.intValue();
                            if (form.numChildren() == 1) {
                                final AbstractFormulaObject child1 = form.getChild(0);
                                result.cases.add(new SwitchCase(valueToMatch, child1));
                            } else {
                                result.cases.add(new SwitchCase(valueToMatch, form));
                            }
                        }
                    } catch (final ParsingException ex) {
                        if (mode.reportAny) {
                            final String msg = Res.get(Res.BAD_CASE_VALUE) + " (10165)";
                            child.logError(msg);
                        }
                    }
                } else if (DEFAULT.equals(childTag)) {
                    final Formula form = extractFormula(evalContext, nonempty, mode);
                    if (form.numChildren() == 1) {
                        result.defaultValue = form.getChild(0);
                    } else {
                        result.defaultValue = form;
                    }
                }
            } else {
                if (mode.reportAny) {
                    child.logError("Unexpected empty element child of 'case' element (10166)");
                }
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

        if (mode.reportAny) {
            for (final String attrName : element.attributeNames()) {
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (10170)";
                element.logError(msg);
            }
        }

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
                switch (childTag) {
                    case BOOLEAN -> extracted = extractBoolean(empty, mode);
                    case INTEGER -> extracted = extractInteger(empty, mode);
                    case REAL -> extracted = extractReal(empty, mode);
                    case STRING -> extracted = extractString(empty, mode);
                    case INT_VECTOR -> extracted = extractConstIntegerVector(empty, mode);
                    case REAL_VECTOR -> extracted = extractConstRealVector(empty, mode);
                    case VARREF -> extracted = extractVarRef(empty, mode);
                    case null, default -> {
                        if (mode.reportAny) {
                            final String msg = Res.fmt(Res.BAD_EMPTY_ELEM, childTag) + " (10180)";
                            element.logError(msg);
                        }
                    }
                }

                if (extracted != null) {
                    container.addChild(extracted);
                }
            } else if (child instanceof final NonemptyElement nonempty) {

                switch (childTag) {
                    case INT_VECTOR -> extracted = extractIntegerVector(evalContext, nonempty, mode);
                    case REAL_VECTOR -> extracted = extractRealVector(evalContext, nonempty, mode);
                    case VECTOR -> {
                        if (mode.reportDeprecated) {
                            final String msg = Res.fmt(Res.DEPRECATED_TAG, VECTOR) + " (10181)";
                            element.logError(msg);
                        }
                        extracted = extractIntegerVector(evalContext, nonempty, mode);
                    }
                    case SPAN -> extracted = extractSpan(evalContext, nonempty, mode);
                    case ERROR -> extracted = extractError(nonempty, mode);
                    case BINARY -> extracted = extractBinaryOp(evalContext, nonempty, mode);
                    case UNARY -> extracted = extractUnaryOp(evalContext, nonempty, mode);
                    case FORMULA, EXPR -> extracted = extractFormula(evalContext, nonempty, mode);
                    case FUNCTION -> extracted = extractFunction(evalContext, nonempty, mode);
                    case GROUPING -> {
                        if (mode.reportDeprecated) {
                            final String msg = Res.fmt(Res.DEPRECATED_TAG, GROUPING) + " (10182)";
                            element.logError(msg);
                        }
                        extracted = extractGrouping(evalContext, nonempty, mode);
                    }
                    case TEST -> extracted = extractTest(evalContext, nonempty, mode);
                    case SWITCH -> extracted = extractSwitch(evalContext, nonempty, mode);
                    case IS_EXACT -> extracted = extractIsExact(evalContext, nonempty, mode);
                    case null, default -> {
                        if (mode.reportAny) {
                            final String msg = Res.fmt(Res.BAD_NONEMPTY_ELEM, childTag) + " (10183)";
                            element.logError(msg);
                        }
                    }
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
