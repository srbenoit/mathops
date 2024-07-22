package dev.mathops.assessment.formula;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.parser.xml.XmlContentError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class constructs {@code Formula} objects from text representations.
 */
public enum FormulaFactory {
    ;

    /**
     * Parse a formula from a text string representation. This format supports the basic "calculator syntax" in which
     * expressions are entered in a linear fashion using parentheses to manage order of operations.<br>
     * <br>
     * If the formula parses correctly, a Formula object is returned. If not, null is returned <br>
     * <br>
     * Parsing takes place in two phases. In the first phase, the entire expression is converted into a sequence of
     * tokens. Each token is one of:
     * <ul>
     * <li>A numeric value (or predefined constant)</li>
     * <li>A parameter</li>
     * <li>A sub-formula (interior of parentheses or function argument)</li>
     * <li>A function
     * <li>An operator</li>
     * </ul>
     * Note that during the first phase, when a sub-formula is encountered, the entire sub-formula
     * is parsed independently (through both phases), and the resulting {@code Formula} object
     * is used as the token for the higher level pass.<br>
     * <br>
     * In the second phase, the tokens are arranged in a tree structure based on operator
     * precedence. The result is a tree that can be evaluated by simple recursion.<br>
     * <br>
     * As parsing is occurring, a set of references to the named parameters used in the formula is
     * maintained, so the list of parameter names can be queried, and parameter values can be set
     * prior to evaluation.
     *
     * @param evalContext the evaluation context
     * @param formulaText the text to parse
     * @param mode        the parser mode
     * @return a {@code Formula} object if successful, null if unsuccessful
     */
    public static Formula parseFormulaString(final EvalContext evalContext, final String formulaText,
                                             final EParserMode mode) {

        // Log.info("Parsing formula from '" + formulaText + "'");

        final FormulaSource src = new FormulaSource();
        src.setXml(formulaText);

        final int[] pos = {0, 0};
        pos[1] = formulaText.length();

        return parseFormulaString(evalContext, src, pos, mode);
    }

    /**
     * Parse a formula from an XML source containing a string representation.
     *
     * @param evalContext the evaluation context
     * @param source      a {@code XmlSource} containing the formula text to parse
     * @param pos         a 2-element integer array in which element [0] is the index of the start of the formula, and
     *                    [1] is the index of its end
     * @param mode        the parser mode
     * @return a {@code Formula} object if successful, null if unsuccessful
     */
    private static Formula parseFormulaString(final EvalContext evalContext, final FormulaSource source,
                                              final int[] pos, final EParserMode mode) {

        Formula formula = null;

        // Phase 1: tokenize the formula text
        final List<AbstractFormulaObject> tokens = new ArrayList<>(20);

        // Phase 2: Change operators to unary as appropriate
        // Phase 3: Convert things like 4{x} into 4*{x}
        if ((buildTokens(evalContext, source, pos, tokens, mode)
                && identifyUnaryOperators(source, pos, tokens)) && insertMultiplications(tokens)) {
            // printTokens(tokens);

            // Phase 4: Build the formula
            formula = buildFormula(source, pos, tokens);
        }

        return formula;
    }

    /**
     * Convert a text formula into a series of tokens.
     *
     * @param evalContext the evaluation context
     * @param source      a {@code XmlSource} containing the formula text to parse
     * @param pos         a 2-element integer array in which element [0] is the index of the start of the formula, and
     *                    [1] is the index of its end
     * @param tokens      an {@code ArrayList} to which tokens will be added
     * @param mode        the parser mode
     * @return true if tokenization was successful; false otherwise
     */
    private static boolean buildTokens(final EvalContext evalContext, final FormulaSource source,
                                       final int[] pos, final List<? super AbstractFormulaObject> tokens,
                                       final EParserMode mode) {

        final String xml = source.getXml();
        int end;

        final int[] inner = {pos[0], pos[1]};

        while (inner[0] < inner[1]) {
            final char ch = xml.charAt(inner[0]);

            if (Character.isWhitespace(ch)) {
                // Whitespace (ignore)
                end = inner[0] + 1;
            } else if (ch == '{') {
                // Start of a parameter
                end = parseParameter(source, inner, tokens);
            } else if (ch == '\\') {
                // Start of Unicode escape
                end = parseUnicodeEscape(source, inner, tokens);
            } else if ("+-*/^%<>\u2264\u2265=\u2260|&".indexOf(ch) != -1) {
                // Operator
                end = parseOperator(source, inner, tokens);
            } else if (ch == '(') {
                // Grouping operation
                end = parseGroupingOperation(evalContext, source, inner, tokens, mode);
            } else if (ch == '[') {
                // Vector
                end = parseVector(evalContext, source, inner, tokens, mode);
            } else if (ch == '\'' || ch == '\"') {
                // Span
                end = parseSpan(evalContext, source, inner, tokens, mode);
            } else if (Character.isDigit(ch) || ch == '.') {
                // Number
                end = parseNumber(source, inner, tokens);
            } else if (Character.isUpperCase(ch)) {
                // Constant
                end = parseConstant(source, inner, tokens);
            } else if (Character.isLowerCase(ch)) {
                // Function
                end = parseFunction(evalContext, source, inner, tokens, mode);
            } else {
                final String msg = "Unrecognized symbol in formula: '" + ch + "'";
                source.parseError(msg, pos[0], pos[1]);
                end = -1;
            }

            if (end == -1) {
                // Error (message will have been added already)
                return false;
            }

            inner[0] = end;
        }

        if (tokens.isEmpty()) {
            final String msg = "Formula empty.";
            source.parseError(msg, pos[0], pos[1]);

            return false;
        }

        // for (final AbstractFormulaObject tok : tokens) {
        // Log.info(" TOKEN: " + tok);
        // }

        return true;
    }

    /**
     * Parse a parameter name from the formula string. Parameter names are surrounded with curly braces {}, and there
     * can be nothing inside these braces but the parameter name. Note that parameter names may include any Unicode
     * character (including spaces) except "{" and " }".
     *
     * @param source a {@code XmlSource} containing the formula text to parse
     * @param pos    a 2-element integer array in which element [0] is the index of the start of the formula, and [1] is
     *               the index of its end
     * @param tokens an {@code ArrayList} to which tokens will be added
     * @return the position of the character after the closing brace of the parameter name, or -1 if an error occurred
     */
    private static int parseParameter(final FormulaSource source, final int[] pos,
                                      final Collection<? super AbstractFormulaObject> tokens) {

        final int[] range = {pos[0], source.indexOf('}', pos[0] + 1)};

        if (range[1] == -1) {
            final String msg = "Opening parameter brace with no matching close brace.";
            source.parseError(msg, pos[0], pos[0]);
            return -1;
        }

        final String xml = source.getXml();
        Integer index = null;

        // See if there's an index included.
        int open = source.indexOf('[', pos[0] + 1);

        if (open != -1 && open < range[1]) {
            final int close = source.indexOf(']', open);

            if (close != -1 && close < range[1]) {

                // Found index, so extract it
                try {
                    index = Integer.valueOf(xml.substring(open + 1, close));
                } catch (final NumberFormatException ex) {
                    final String msg = "Parameter index is not a valid number.";
                    source.parseError(msg, open, close);

                    return -1;
                }

                if (index.intValue() < 1) {
                    final String msg = "Parameter index must be 1 or greater.";
                    source.parseError(msg, open, close);

                    return -1;
                }
            } else {
                final String msg = "Opening parameter index with no matching close bracket.";
                source.parseError(msg, open, open);

                return -1;
            }
        } else {
            open = range[1]; // We use 'open' as the end of the name.
        }

        final String name = xml.substring(pos[0] + 1, open);
        final int pos2 = name.indexOf('{') + 1;

        if (pos2 != 0) {
            final String fullName = xml.substring(range[0], range[1]);
            final String msg = "Parameter names (" + fullName + ") may not contain curly braces.";
            source.parseError(msg, pos2, pos2);

            return -1;
        }

        final VariableRef ref = new VariableRef(name);
        ref.index = index;
        tokens.add(ref);

        return range[1] + 1;
    }

    /**
     * Parse a Unicode escape by converting it into a normal character, then calling {@code parseExplicitOperator}.
     *
     * @param source a {@code XmlSource} containing the formula text to parse
     * @param pos    a 2-element integer array in which element [0] is the index of the start of the escape, and [1] is
     *               the index of its end
     * @param tokens an {@code ArrayList} to which tokens will be added
     * @return the position of the character after the operation, or -1 if an error occurred
     */
    private static int parseUnicodeEscape(final FormulaSource source, final int[] pos,
                                          final Collection<? super AbstractFormulaObject> tokens) {

        final String xml = source.getXml();
        final String valid = "0123456789abcdefABCDEF";

        if (xml.length() < pos[0] + 5) {
            final String msg = "Unicode escape extends beyond end of source XML.";
            source.parseError(msg, pos[0], pos[0]);

            return -1;
        }

        if (xml.charAt(pos[0] + 1) != 'u') {
            final String msg = "Invalid Unicode escape (must be '\\u' followed by 4 hexadecimal digits): "
                    + source.getOrigin();
            source.parseError(msg, pos[0], pos[0] + 5);

            return -1;
        }

        if (valid.indexOf(xml.charAt(pos[0] + 2)) == -1 || valid.indexOf(xml.charAt(pos[0] + 3)) == -1
                || valid.indexOf(xml.charAt(pos[0] + 4)) == -1 || valid.indexOf(xml.charAt(pos[0] + 5)) == -1) {
            final String msg = "Invalid Unicode escape (must be '\\u' followed by 4 hexadecimal digits): "
                    + source.getOrigin();
            source.parseError(msg, pos[0], pos[0] + 5);

            return -1;
        }

        final int value;
        try {
            value = Integer.parseInt(xml.substring(pos[0] + 2, pos[0] + 6), 16);
        } catch (final NumberFormatException ex) {
            final String msg = "Unable to convert Unicode escape into numeric value";
            source.parseError(msg, pos[0], pos[0] + 6);

            return -1;
        }

        pos[0] += 5;

        switch (value) {

            case 0x0026:
                return parseExplicitOperator('&', pos, tokens);

            case 0x003c:
                return parseExplicitOperator('<', pos, tokens);

            case 0x003e:
                return parseExplicitOperator('>', pos, tokens);

            case 0x2260:
                return parseExplicitOperator('\u2260', pos, tokens);

            case 0x2264:
                return parseExplicitOperator('\u2264', pos, tokens);

            case 0x2265:
                return parseExplicitOperator('\u2265', pos, tokens);

            case 0x221e:
                final ConstRealValue real1 = new ConstRealValue(Double.POSITIVE_INFINITY);
                tokens.add(real1);

                return pos[0] + 1;

            case 0x03c0:
                final ConstRealValue real2 = new ConstRealValue(Math.PI);
                tokens.add(real2);

                return pos[0] + 1;

            case 0x0435:
                final ConstRealValue real3 = new ConstRealValue(Math.E);
                tokens.add(real3);

                return pos[0] + 1;

            default:
                break;
        }

        final String msg = "Invalid Unicode escape value.";
        source.parseError(msg, pos[0] - 6, pos[0] - 1);

        return -1;
    }

    /**
     * Parse a binary operation name from the formula string. Binary operations include +. -, *, /, ^ and %.
     *
     * @param source a {@code XmlSource} containing the formula text to parse
     * @param pos    a 2-element integer array in which element [0] is the index of the start of the formula, and [1] is
     *               the index of its end
     * @param tokens an {@code ArrayList} to which tokens will be added
     * @return the position of the character after the operation, or -1 if an error occurred
     */
    private static int parseOperator(final FormulaSource source, final int[] pos,
                                     final Collection<? super AbstractFormulaObject> tokens) {

        return parseExplicitOperator(source.getXml().charAt(pos[0]), pos, tokens);
    }

    /**
     * Parse an explicit binary operation name from the formula string. Binary operations include +. -, *, /, ^, %, <,
     * >, \u2264, \u2265, =, \u2260, &, and |.
     *
     * @param operator the explicit operator
     * @param pos      a 2-element integer array in which element [0] is the index of the start of the formula, and [1]
     *                 is the index of its end
     * @param tokens   an {@code ArrayList} to which tokens will be added
     * @return the position of the character after the operation, or -1 if an error occurred
     */
    private static int parseExplicitOperator(final char operator, final int[] pos,
                                             final Collection<? super AbstractFormulaObject> tokens) {

        final EBinaryOp oper = EBinaryOp.forOp(operator);
        final BinaryOper op = new BinaryOper(oper);

        tokens.add(op);

        return pos[0] + 1;
    }

    /**
     * Parse a grouping operation, such as parentheses or square brackets. This is done by locating the matching symbol,
     * then taking everything inside the symbols and parsing it as a separate formula. Once this is done, the resulting
     * root node is added to the current formula.
     *
     * @param evalContext the evaluation context
     * @param source      a {@code XmlSource} containing the formula text to parse
     * @param pos         a 2-element integer array in which element [0] is the index of the start of the formula, and
     *                    [1] is the index of its end
     * @param tokens      an {@code ArrayList} to which tokens will be added
     * @param mode        the parser mode
     * @return the position of the character after the operation, or -1 if an error occurred
     */
    private static int parseGroupingOperation(final EvalContext evalContext, final FormulaSource source,
                                              final int[] pos, final Collection<? super AbstractFormulaObject> tokens,
                                              final EParserMode mode) {

        final int[] range = {pos[0], 0};

        // Locate the matching closing symbol
        final String xml = source.getXml();
        int count = 1;
        for (range[1] = pos[0] + 1; count > 0 && range[1] < pos[1]; range[1]++) {

            if (xml.charAt(range[1]) == '(') {
                count++;
            } else if (xml.charAt(range[1]) == ')') {
                count--;
            }
        }

        if (count > 0) {
            final String msg = "No matching ')' found.";
            source.parseError(msg, pos[0], pos[0]);
            return -1;
        }

        // Move back to point to the last closing symbol.
        range[1]--;

        // Parse what's inside the parentheses as a separate formula
        final int[] pos2 = {range[0] + 1, range[1]};
        final Formula inner = parseFormulaString(evalContext, source, pos2, mode);

        if (inner == null) {
            return -1;
        }

        // Construct the grouping operation object and add its token
        final GroupingOper group = new GroupingOper();
        final int numChildren = inner.numChildren();
        for (int i = 0; i < numChildren; ++i) {
            group.addChild(inner.getChild(i));
        }
        tokens.add(group);

        return range[1] + 1;
    }

    /**
     * Parse a grouping operation, such as parentheses or square brackets. This is done by locating the matching symbol,
     * then taking everything inside the symbols and parsing it as a separate formula. Once this is done, the resulting
     * root node is added to the current formula.
     *
     * @param evalContext the evaluation context
     * @param source      a {@code XmlSource} containing the formula text to parse
     * @param pos         a 2-element integer array in which element [0] is the index of the start of the formula, and
     *                    [1] is the index of its end
     * @param tokens      an {@code ArrayList} to which tokens will be added
     * @param mode        the parser mode
     * @return the position of the character after the operation, or -1 if an error occurred
     */
    private static int parseVector(final EvalContext evalContext, final FormulaSource source,
                                   final int[] pos, final Collection<? super AbstractFormulaObject> tokens,
                                   final EParserMode mode) {

        final int[] range = {pos[0], 0};

        // Locate the matching closing symbol
        final String xml = source.getXml();
        int count = 1;
        for (range[1] = pos[0] + 1; count > 0 && range[1] < pos[1]; range[1]++) {

            if (xml.charAt(range[1]) == '[') {
                count++;
            } else if (xml.charAt(range[1]) == ']') {
                count--;
            }
        }

        if (count > 0) {
            final String msg = "No matching ']' found.";
            source.parseError(msg, pos[0], pos[0]);
            return -1;
        }

        // Move back to point to the last closing symbol.
        range[1]--;

        // Parse what's inside the brackets as a list of components, separated by commas.
        final int[] pos2 = {range[0] + 1, range[1]};

        final Collection<AbstractFormulaObject> entries = new ArrayList<>(5);

        while (pos2[0] < pos2[1]) {

            // Locate the next comma, ignoring nested vectors
            range[0] = pos2[0];
            count = 0;

            for (range[1] = range[0]; range[1] < pos2[1]; range[1]++) {

                if (xml.charAt(range[1]) == CoreConstants.COMMA_CHAR && count == 0) {
                    break;
                }

                if (xml.charAt(range[1]) == '[') {
                    count++;
                } else if (xml.charAt(range[1]) == ']') {
                    count--;
                }
            }

            final Formula inner = parseFormulaString(evalContext, source, range, mode);

            if (inner == null) {
                return -1;
            }

            final AbstractFormulaObject entry = inner.numChildren() == 1 ? inner.getChild(0) : inner;
            entries.add(entry);

            pos2[0] = range[1] + 1;
        }

        // Construct the vector object so we can add its contents
        final RealFormulaVector vector = new RealFormulaVector();
        for (final AbstractFormulaObject child : entries) {
            vector.addChild(child);
        }

        // Add the vector to the tokens list.
        tokens.add(vector);

        return pos2[1] + 1;
    }

    /**
     * Parse an if-then-else test. The contents of the test are contained in a "()" group. The test consists of three
     * separate formulae, separated by "?" and ":" like the corresponding Java ternary operation. However, since there
     * may be nested tests, this function searches like it does with grouping operations, matching '?' and ':' symbols
     * to be certain the result matches the author's intent.
     *
     * @param evalContext the evaluation context
     * @param source      a {@code XmlSource} containing the formula text to parse
     * @param pos         a 2-element integer array in which element [0] is the index of the start of the formula, and
     *                    [1] is the index of its end
     * @param tokens      an {@code ArrayList} to which tokens will be added
     * @param mode        the parser mode
     * @return the position of the character after the operation, or -1 if an error occurred
     */
    private static int parseTest(final EvalContext evalContext, final FormulaSource source,
                                 final int[] pos, final Collection<? super AbstractFormulaObject> tokens,
                                 final EParserMode mode) {

        final int[] range = {pos[0], 0};

        // Step 1: Locate the matching closing symbol
        final int[] inner = {pos[0] + 1, 0};

        final String xml = source.getXml();
        int count = 1;
        char quote = 0;
        for (range[1] = inner[0]; count > 0 && range[1] < pos[1]; range[1]++) {

            if (quote != 0) {
                if (xml.charAt(range[1]) == quote) {
                    quote = 0;
                }
            } else if (xml.charAt(range[1]) == '\'' || xml.charAt(range[1]) == '\"') {
                quote = xml.charAt(range[1]);
            } else if (xml.charAt(range[1]) == '(') {
                count++;
            } else if (xml.charAt(range[1]) == ')') {
                count--;
            }
        }

        if (count > 0) {
            final String msg = "No matching ')' found.";
            source.parseError(msg, pos[0], pos[0]);
            return -1;
        }

        // Step 2: Extract the IF clause in the test (we have to ignore '?'
        // characters in possible nested tests).
        boolean searching = true;
        count = 1;

        for (inner[1] = inner[0]; count > 0 && inner[1] < range[1] - 1; inner[1]++) {

            if (quote == 0) {
                switch (xml.charAt(inner[1])) {
                    case '\'':
                    case '\"':
                        quote = xml.charAt(inner[1]);
                        break;

                    case '(':
                        count++;
                        break;

                    case ')':
                        count--;
                        break;

                    case '?':
                        if (count == 1) {
                            searching = false;
                            count = 0; // Stop searching.
                        }

                        break;

                    default:
                        break;
                }
            } else {
                if (xml.charAt(inner[1]) == quote) {
                    quote = 0;
                }
            }
        }

        if (searching) {
            final String msg = "No '?' found.";
            source.parseError(msg, pos[0], range[1]);

            return -1;
        }

        inner[1]--; // Back up to position of '?'
        final Formula ifClause =
                parseFormulaString(evalContext, source, inner, mode);

        if (ifClause == null) {
            return -1;
        }

        // Step 3: Extract the THEN clause in the test
        inner[0] = inner[1] + 1; // Step to position just after '?'
        searching = true;
        count = 1;

        for (inner[1] = inner[0]; count > 0 && inner[1] < range[1] - 1; inner[1]++) {

            if (quote == 0) {
                switch (xml.charAt(inner[1])) {
                    case '\'':
                    case '\"':
                        quote = xml.charAt(inner[1]);
                        break;

                    case '(':
                        count++;
                        break;

                    case ')':
                        count--;
                        break;

                    case ':':
                        if (count == 1) {
                            searching = false;
                            count = 0; // Stop searching.
                        }
                        break;

                    default:
                        break;
                }
            } else {
                if (xml.charAt(inner[1]) == quote) {
                    quote = 0;
                }
            }
        }

        if (searching) {
            final String msg = "No ':' found.";
            source.parseError(msg, pos[0], pos[1]);

            return -1;
        }

        inner[1]--; // Back up to position of ':'
        final Formula thenClause =
                parseFormulaString(evalContext, source, inner, mode);

        if (thenClause == null) {
            return -1;
        }

        inner[0] = inner[1] + 1; // Step to position just after ':'
        inner[1] = range[1] - 1;
        final Formula elseClause =
                parseFormulaString(evalContext, source, inner, mode);

        if (elseClause == null) {
            return -1;
        }

        // Construct the test operation object and add its token
        final TestOper test = new TestOper();

        if (ifClause.numChildren() == 1) {
            test.addChild(ifClause.getChild(0));
        } else {
            test.addChild(ifClause);
        }

        if (thenClause.numChildren() == 1) {
            test.addChild(thenClause.getChild(0));
        } else {
            test.addChild(thenClause);
        }

        if (elseClause.numChildren() == 1) {
            test.addChild(elseClause.getChild(0));
        } else {
            test.addChild(elseClause);
        }

        tokens.add(test);

        return range[1] + 1;
    }

    /**
     * Parse a span value. The value is parsed by searching forward for the closing quote and taking everything between
     * as span contents. This allows a span to contain any of the operators or special characters that would otherwise
     * be parsed as part of a formula.
     *
     * @param evalContext the evaluation context
     * @param source      a {@code XmlSource} containing the formula text to parse
     * @param pos         a 2-element integer array in which element [0] is the index of the start of the formula, and
     *                    [1] is the index of its end
     * @param tokens      an {@code ArrayList} to which tokens will be added
     * @param mode        the parser mode
     * @return the position of the character after the operation, or -1 if an error occurred
     */
    private static int parseSpan(final EvalContext evalContext, final FormulaSource source, final int[] pos,
                                 final Collection<? super AbstractFormulaObject> tokens, final EParserMode mode) {

        final int[] range = {pos[0] + 1, 0};

        // Look for the closing ' or "
        final String xml = source.getXml();
        boolean searching = true;
        for (range[1] = range[0]; range[1] < pos[1]; range[1]++) {
            if (xml.charAt(range[1]) == xml.charAt(pos[0])) {
                searching = false;
                break;
            }
        }

        if (searching) {
            final String msg = "Missing closing quotation mark.";
            source.parseError(msg, range[0], range[0]);
            return -1;
        }

        final String spanSource = "<X>" + source.getXml().substring(range[0], range[1]) + "</X>";

        try {
            final XmlContent content = new XmlContent(spanSource, false, false);
            final IElement top = content.getToplevel();
            if (top instanceof final NonemptyElement nonempty) {

                final DocSimpleSpan span = DocFactory.parseSpan(evalContext, nonempty, mode);

                if (span == null) {
                    Log.warning("Failed to parse span XML within formula");

                    final List<XmlContentError> errors = content.getAllErrors();
                    for (final XmlContentError error : errors) {
                        Log.warning("    ", error);
                    }
                    return -1;
                }

                tokens.add(new ConstSpanValue(span));
            } else {
                Log.warning("Failed to parse span XML within formula");
                return -1;
            }
        } catch (final ParsingException ex) {
            Log.warning("Failed to parse span XML within formula", ex);
            return -1;
        }

        return range[1] + 1;
    }

    /**
     * Parse a numeric value. The value is parsed by searching forward for all numeric characters or decimal point
     * symbols. If no decimal point was found, or if it was the last symbol found, the value is parsed as an integer.
     * Otherwise, the value is parsed as a real.
     *
     * @param source a {@code XmlSource} containing the formula text to parse
     * @param pos    a 2-element integer array in which element [0] is the index of the start of the formula, and [1] is
     *               the index of its end
     * @param tokens an {@code ArrayList} to which tokens will be added
     * @return the position of the character after the operation, or -1 if an error occurred
     */
    private static int parseNumber(final FormulaSource source, final int[] pos,
                                   final Collection<? super AbstractFormulaObject> tokens) {

        final int[] range = {pos[0], 0};

        // Look for the end of the numeric value
        final String xml = source.getXml();
        boolean scient = false;
        int decimal = -1;
        for (range[1] = pos[0]; range[1] < pos[1]; range[1]++) {
            final char ch = xml.charAt(range[1]);

            if (Character.isDigit(ch)) {
                continue;
            }

            if (ch == 'E' || ch == 'e') {

                if (scient) {
                    // Second 'E' - error.
                    final String msg = "Invalid scientific notation";
                    source.parseError(msg, range[1], range[1]);
                    return -1;
                }

                scient = true;

                // If the next char is a '-', skip it.
                if (range[1] < pos[1] - 1) {
                    if (xml.charAt(range[1] + 1) == '-') {
                        range[1]++;
                    }
                } else {
                    // Ends with the 'E', not valid.
                    final String msg = "Invalid scientific notation";
                    source.parseError(msg, range[1], range[1]);
                    return -1;
                }
            } else if (ch == '.') {

                if (decimal != -1) {
                    // Second decimal point - error.
                    final String msg = "Two decimal points in numeric value.";
                    source.parseError(msg, range[1], range[1]);
                    return -1;
                }

                decimal = range[1];
            } else {
                break;
            }
        }

        if (!scient && (decimal == -1 || decimal == range[1] - 1)) {

            // Parse it as an integer
            final String sub;
            if (decimal == -1) {
                sub = xml.substring(range[0], range[1]);
            } else {
                sub = xml.substring(range[0], range[1] - 1);
            }

            try {
                final long intval = Long.parseLong(sub);
                final AbstractFormulaObject obj = new ConstIntegerValue(intval);
                tokens.add(obj);
            } catch (final NumberFormatException ex) {
                final String msg = "Unable to parse integer value.";
                source.parseError(msg, range[0], range[1]);

                return -1;
            }
        } else {
            // Parse it as a real value
            final String sub = xml.substring(pos[0], range[1]);

            try {
                final double realval = Double.parseDouble(sub);
                final AbstractFormulaObject obj = new ConstRealValue(realval);
                tokens.add(obj);
            } catch (final NumberFormatException ex) {
                final String msg = "Unable to parse real value.";
                source.parseError(msg, range[0], range[1]);
                return -1;
            }
        }

        return range[1];
    }

    /**
     * Parse a predefined constant. The only constants currently recognized are 'E', 'PI', 'TRUE' and 'FALSE'.
     *
     * @param source a {@code XmlSource} containing the formula text to parse
     * @param pos    a 2-element integer array in which element [0] is the index of the start of the formula, and [1] is
     *               the index of its end
     * @param tokens an {@code ArrayList} to which tokens will be added
     * @return the position of the character after the operation, or -1 if an error occurred
     */
    private static int parseConstant(final FormulaSource source, final int[] pos,
                                     final Collection<? super AbstractFormulaObject> tokens) {

        final String xml = source.getXml();

        final int[] range = {pos[0], 0};
        final char ch = xml.charAt(pos[0]);

        if (ch == 'E') {
            range[1] = pos[0];
            final AbstractFormulaObject obj = new ConstRealValue(Math.E);
            tokens.add(obj);

            return range[1] + 1;
        }

        if (ch == 'P' && pos[1] > pos[0] + 1 && xml.charAt(pos[0] + 1) == 'I') {
            range[1] = pos[0] + 1;
            final AbstractFormulaObject obj = new ConstRealValue(Math.PI);
            tokens.add(obj);

            return range[1] + 1;
        }

        if (ch == 'T' && pos[1] > pos[0] + 3 && xml.charAt(pos[0] + 1) == 'R'
                && xml.charAt(pos[0] + 2) == 'U' && xml.charAt(pos[0] + 3) == 'E') {
            range[1] = pos[0] + 3;
            final AbstractFormulaObject obj = new ConstBooleanValue(true);
            tokens.add(obj);

            return range[1] + 1;
        }

        if (ch == 'F' && pos[1] > pos[0] + 4 && xml.charAt(pos[0] + 1) == 'A'
                && xml.charAt(pos[0] + 2) == 'L' && xml.charAt(pos[0] + 3) == 'S'
                && xml.charAt(pos[0] + 4) == 'E') {
            range[1] = pos[0] + 4;
            final AbstractFormulaObject obj = new ConstBooleanValue(false);
            tokens.add(obj);

            return range[1] + 1;
        }

        final String msg = "Unrecognized constant beginning with " + ch;
        int end = pos[0];
        while (end < pos[1] && Character.isUpperCase(xml.charAt(end))) {
            ++end;
        }

        source.parseError(msg, range[0], end - 1);

        return -1;
    }

    /**
     * Parse a function.
     *
     * @param evalContext the evaluation context
     * @param source      a {@code XmlSource} containing the formula text to parse
     * @param pos         a 2-element integer array in which element [0] is the index of the start of the formula, and
     *                    [1] is the index of its end
     * @param tokens      an {@code ArrayList} to which tokens will be added
     * @param mode        the parser mode
     * @return the position of the character after the operation, or -1 if an error occurred
     */
    private static int parseFunction(final EvalContext evalContext, final FormulaSource source,
                                     final int[] pos, final List<? super AbstractFormulaObject> tokens,
                                     final EParserMode mode) {

        final String xml = source.getXml();
        String sub = xml.substring(pos[0], pos[1]);
        final int[] range = {pos[0], 0};

        // Special case: "test" function to do if-then-else
        if (sub.startsWith("test(")) {
            final int[] inner = {pos[0] + 4, pos[1]};

            return parseTest(evalContext, source, inner, tokens, mode);
        }

        boolean searching = true;

        // Three-letter functions with one argument
        if (sub.startsWith("abs(")
                || sub.startsWith("cos(")
                || sub.startsWith("sin(")
                || sub.startsWith("tan(")
                || sub.startsWith("exp(")
                || sub.startsWith("log(")
                || sub.startsWith("gcd(")) {
            final int[] inner = {pos[0] + 3, pos[1]};
            searching = false;
            range[1] = parseGroupingOperation(evalContext, source, inner, tokens, mode) - 1;
            sub = sub.substring(0, 3);
        }

        // Four letter functions with one argument
        if (sub.startsWith("acos(")
                || sub.startsWith("asin(")
                || sub.startsWith("atan(")
                || sub.startsWith("ceil(")
                || sub.startsWith("sqrt(")
                || sub.startsWith("cbrt(")) {
            final int[] inner = {pos[0] + 4, pos[1]};
            searching = false;
            range[1] = parseGroupingOperation(evalContext, source, inner, tokens, mode) - 1;
            sub = sub.substring(0, 4);
        }

        // Five-letter functions with one argument
        if (sub.startsWith("floor(")
                || sub.startsWith("round(")
                || sub.startsWith("toDeg(")
                || sub.startsWith("toRad(")
                || sub.startsWith("srad2(")
                || sub.startsWith("srad3(")) {
            final int[] inner = {pos[0] + 5, pos[1]};
            searching = false;
            range[1] = parseGroupingOperation(evalContext, source, inner, tokens, mode) - 1;
            sub = sub.substring(0, 5);
        }

        if (range[1] > 0) {
            final GroupingOper group = (GroupingOper) tokens.getLast();

            final EFunction which = EFunction.forName(sub);
            if (which == null) {
                final String msg = "Unrecognized function name.";

                range[1] = pos[0];
                while (range[1] < pos[1] && Character.isLowerCase(xml.charAt(range[1]))) {
                    ++range[1];
                }

                source.parseError(msg, pos[0], range[1] - 1);
            } else {
                final Function func = new Function(which);
                final int count = group.numChildren();
                for (int i = 0; i < count; ++i) {
                    func.addChild(group.getChild(i));
                }
                tokens.removeLast();
                tokens.add(func);
            }
        } else if (searching) {

            final int firstChar = sub.isEmpty() ? 0 : sub.charAt(0);

            // Treat x, y, z, t as special cases - independent variables
            if (firstChar == 'x') {
                final VariableRef ref = new VariableRef("x");
                tokens.add(ref);
            } else if (firstChar == 'y') {
                final VariableRef ref = new VariableRef("y");
                tokens.add(ref);
            } else if (firstChar == 'z') {
                final VariableRef ref = new VariableRef("z");
                tokens.add(ref);
            } else if (firstChar == 't') {
                final VariableRef ref = new VariableRef("t");
                tokens.add(ref);
            } else {
                final String msg = "Unrecognized function name.";

                range[1] = pos[0];
                while (range[1] < pos[1] && Character.isLowerCase(xml.charAt(range[1]))) {
                    ++range[1];
                }

                source.parseError(msg, pos[0], range[1] - 1);
            }
        }

        return range[1] + 1;
    }

    /**
     * Search for operators that should be treated as unary and convert them from their current status as BinaryOperator
     * objects.
     *
     * @param source a {@code XmlSource} containing the formula text to parse
     * @param pos    a 2-element integer array in which element [0] is the index of the start of the formula, and [1] is
     *               the index of its end
     * @param tokens the ordered list of tokens
     * @return true on success, false on any failure
     */
    private static boolean identifyUnaryOperators(final FormulaSource source, final int[] pos,
                                                  final List<AbstractFormulaObject> tokens) {

        // See if the first token is an operator, in which case it must be treated as unary if
        // possible.
        AbstractFormulaObject obj = tokens.getFirst();

        if (obj instanceof final BinaryOper oper) {

            if (oper.op == EBinaryOp.SUBTRACT) {
                final UnaryOper unary = new UnaryOper(EUnaryOp.MINUS);
                tokens.set(0, unary);
            } else if (oper.op == EBinaryOp.ADD) {
                final UnaryOper unary = new UnaryOper(EUnaryOp.PLUS);
                tokens.set(0, unary);
            } else {
                final String msg = "'" + oper.op + "' is not valid at the start of a formula.";
                source.parseError(msg, pos[0], pos[1] - 1);

                return false;
            }
        }

        // Now look for operators that immediately follow other operators. In these cases, we treat
        // the first as binary, and all subsequent as unary. This allows things like "4 * --5"
        // which is the same as "4 * 5" since the two negation operators will cancel out.
        int prior = -10;

        final int numTokens = tokens.size();
        for (int i = 0; i < numTokens; ++i) {
            obj = tokens.get(i);

            if (obj instanceof final BinaryOper oper) {

                if (prior == i - 1) {
                    if (oper.op == EBinaryOp.SUBTRACT) {
                        final UnaryOper unary = new UnaryOper(EUnaryOp.MINUS);
                        tokens.set(i, unary);
                    } else if (oper.op == EBinaryOp.ADD) {
                        final UnaryOper unary = new UnaryOper(EUnaryOp.PLUS);
                        tokens.set(i, unary);
                    } else {
                        obj = tokens.get(prior);
                        final String msg = "'" + oper.op + "' is not valid following '" + obj + "'";
                        source.parseError(msg, pos[0], pos[1]);

                        return false;
                    }
                }

                prior = i;
            }
        }

        // Finally, we can work right to left, taking anything preceded by a unary operator and
        // making it a child of that operator, removing it from the array list. (We work right to
        // left so the case shown above of multiple adjacent unary operators will result in proper
        // nesting.)
        int inx = tokens.size() - 2;

        while (inx >= 0) {
            obj = tokens.get(inx);

            if (obj instanceof final UnaryOper oper) {
                obj = tokens.get(inx + 1);
                oper.addChild(obj);
                tokens.remove(inx + 1);
            }

            --inx;
        }

        return true;
    }

    /**
     * Search for consecutive tokens representing numbers, parameter references, grouping operations, functions or test
     * operations, and insert an implied multiplication between them.
     *
     * @param tokens the ordered list of tokens
     * @return true on success, false on any failure
     */
    private static boolean insertMultiplications(final List<AbstractFormulaObject> tokens) {

        // Scan all tokens looking for two consecutive that evaluate to a
        // number. Note it is possible that one or more actually evaluate
        // to a boolean - this error won't be apparent until evaluation time.
        int inx = 0;
        final int numTokens = tokens.size();
        while (inx < numTokens - 1) {
            final AbstractFormulaObject left = tokens.get(inx);
            final AbstractFormulaObject right = tokens.get(inx + 1);

            final boolean leftHit = left instanceof TestOper || left instanceof GroupingOper
                    || left instanceof VariableRef || left instanceof ConstIntegerValue
                    || left instanceof ConstRealValue;

            final boolean rightHit = right instanceof TestOper || right instanceof GroupingOper
                    || right instanceof VariableRef || right instanceof ConstIntegerValue
                    || right instanceof ConstRealValue || right instanceof Function;

            if (leftHit && rightHit) {
                final BinaryOper multiply = new BinaryOper(EBinaryOp.MULTIPLY);
                tokens.add(inx + 1, multiply);
                ++inx; // Skip over the new operator
            }

            ++inx; // Move to the next token
        }

        return true;
    }

    /**
     * Construct the formula by building a tree from the tokens, and scanning for all parameters used anywhere in the
     * formula.
     *
     * @param source a {@code XmlSource} containing the formula text to parse
     * @param pos    a 2-element integer array in which element [0] is the index of the start of the formula, and [1] is
     *               the index of its end
     * @param tokens the ordered list of tokens
     * @return a {@code Formula} object on success, null any failure
     */
    private static Formula buildFormula(final FormulaSource source, final int[] pos,
                                        final List<? extends AbstractFormulaObject> tokens) {

        final EBinaryOp[][] precedence =
                {{EBinaryOp.MULTIPLY, EBinaryOp.DIVIDE, EBinaryOp.POWER, EBinaryOp.REMAINDER},
                        {EBinaryOp.ADD, EBinaryOp.SUBTRACT},
                        {EBinaryOp.LT, EBinaryOp.GT, EBinaryOp.LE, EBinaryOp.GE, EBinaryOp.EQ, EBinaryOp.APPROX,
                                EBinaryOp.NE},
                        {EBinaryOp.AND}, {EBinaryOp.OR}};

        // First, we pass through the token list from left to right, assigning arguments to the binary operators of
        // high precedence until all such operators have been processed, then we do the same for lower precedence
        // operators, continuing down to the lowest precedence. At the end of this process, there should be only one
        // item remaining in the tokens list.

        for (final EBinaryOp[] eBinaryOps : precedence) {
            boolean scanning = true;

            while (scanning) {
                scanning = false;

                final int numTokens = tokens.size();
                for (int i = 0; i < numTokens; i++) {
                    final AbstractFormulaObject obj = tokens.get(i);

                    if (obj instanceof final BinaryOper oper) {

                        if (oper.numChildren() == 2) {
                            // Don't continue to combine operators we've already processed.
                            continue;
                        }

                        // Can't handle binary operators on either end
                        if (i == 0 || i == numTokens - 1) {

                            if (oper.numChildren() == 0) {
                                final String msg = "Unable to find arguments for '" + oper.op + "'";
                                source.parseError(msg, pos[0], pos[1]);
                                return null;
                            }

                            continue;
                        }

                        // See if the operator is of interest on this pass
                        if (isInPrecedence(eBinaryOps, oper.op)) {

                            // Take tokens to the left and right and make them this operator's children
                            final AbstractFormulaObject left = tokens.get(i - 1);
                            final AbstractFormulaObject right = tokens.get(i + 1);

                            oper.addChild(left);
                            oper.addChild(right);

                            tokens.remove(i + 1);
                            tokens.remove(i - 1);

                            // Restart for loop since array list was modified
                            scanning = true;
                            break;
                        }
                    }
                }
            }
        }

        Formula result = null;

        // Safety check to make sure we have parsed correctly
        if (tokens.size() == 1) {
            // Now, we have the tree, so we can construct the Formula.
            final AbstractFormulaObject obj = tokens.getFirst();
            result = new Formula(obj);
        } else {
            for (final AbstractFormulaObject t : tokens) {
                Log.info(t);
            }
            source.parseError("Failed to parse formula.", pos[0], pos[1]);
            // printTokens(tokens);
        }

        return result;
    }

    /**
     * Tests whether an operator is within a list of operators.
     *
     * @param precedence the precedence list of operators
     * @param op         the operator to test
     * @return true if the test operator is in the precedence list; false if not
     */
    private static boolean isInPrecedence(final EBinaryOp[] precedence, final EBinaryOp op) {

        boolean result = false;

        for (final EBinaryOp test : precedence) {
            if (test == op) {
                result = true;
                break;
            }
        }

        return result;
    }

//    /**
//     * Utility function to print the tokens list.
//     *
//     * @param tokens the list of tokens
//     */
//     private static void printTokens(final List<FormulaObjectBase> tokens) {
//
//     int inx = 0;
//     FormulaObjectBase obj = null;
//     LOG.fine("--- Begin Current Tokens List ---");
//     for (inx = 0; inx < tokens.size(); inx++) {
//     obj = tokens.get(inx);
//     LOG.finest("> ");
//     LOG.finest(obj.toString());
//     LOG.fine(CoreConstants.EMPTY);
//     }
//     LOG.fine("--- End Current Tokens List ---");
//     }

    /**
     * Main method for testing.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final String str =
                "test({simplify1}=1?'':test({simplify2}=1?' = {simplify1}':' = {simplify1}&lt;radical&gt;&lt;base&gt;" +
                        "{simplify2}&lt;/base&gt;&lt;/radical&gt;'))";

        final Formula form =
                parseFormulaString(new EvalContext(), str, EParserMode.NORMAL);

        Log.info(form.toString());

        final HtmlBuilder diag = new HtmlBuilder(100);
        form.printDiagnostics(diag, 0);
        Log.info(diag.toString());

        final HtmlBuilder xml = new HtmlBuilder(100);
        form.appendXml(xml);
        Log.info(xml.toString());
    }
}
