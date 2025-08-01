package dev.mathops.assessment.variable;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.EType;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.formula.IntegerVectorValue;
import dev.mathops.assessment.formula.XmlFormulaFactory;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.number.NumberParser;
import dev.mathops.text.parser.xml.CData;
import dev.mathops.text.parser.xml.Comment;
import dev.mathops.text.parser.xml.EmptyElement;
import dev.mathops.text.parser.xml.IElement;
import dev.mathops.text.parser.xml.INode;
import dev.mathops.text.parser.xml.NonemptyElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * A factory class to parse XML representations of parameters.
 */
public enum VariableFactory {
    ;

    /** The value emitted by {@code toString} for a true value. */
    private static final String TRUE_STRING = "true";

    /** The value emitted by {@code toString} for a false value. */
    private static final String FALSE_STRING = "false";

    /** A commonly used string. */
    private static final String PARAM = "param";

    /** A commonly used string. */
    private static final String VAR = "var";

    /** A commonly used string. */
    private static final String NAME = "name";

    /** A commonly used string. */
    private static final String TYPE = "type";

    /** A commonly used string. */
    private static final String VALUE_TYPE = "value-type";

    /** A commonly used string. */
    private static final String EXCLUDE = "exclude";

    /** A commonly used string. */
    private static final String MIN = "min";

    /** A commonly used string. */
    private static final String MAX = "max";

    /** A commonly used string. */
    private static final String FORMULA = "formula";

    /** A commonly used string. */
    private static final String EXPR = "expr";

    /** A commonly used string. */
    private static final String SPAN = "span";

    /** A commonly used string. */
    private static final String FORMAT = "format";

    /** A commonly used string. */
    private static final String DECIMAL_FORMAT = "decimal-format";

    /** A commonly used string. */
    private static final String CHOOSE_FROM = "choose-from";

    /** A commonly used string. */
    private static final String VALUE = "value";

    /** A commonly used string. */
    private static final String LONG = "long";

    /** A commonly used string. */
    private static final String DOUBLE = "double";

    /** A commonly used string. */
    private static final String BOOLEAN = "boolean";

    /** A commonly used string. */
    private static final String IRRATIONAL = "irrational";

    /** A commonly used string. */
    private static final String STRING = "string";

    /** A commonly used string. */
    private static final String INT_VECTOR = "int-vector";

    /** A commonly used string. */
    private static final String MAX_DENOM = "max-denom";

    /** A commonly used string. */
    private static final String GENERATED_INTEGER = "generated-integer";

    /** A commonly used string. */
    private static final String GENERATED_REAL = "generated-real";

    /** A commonly used string. */
    private static final String GENERATED_BOOLEAN = "generated-boolean";

    /** A commonly used string. */
    private static final String GENERATED_STRING = "generated-string";

    /** A commonly used character. */
    private static final int L_BRACE = '{';

    /** A commonly used character. */
    private static final int R_BRACE = '}';

    /** Characters that are interpreted as TRUE in input strings, the first is used in output. */
    private static final String TRUE_CHARS = "YyTt1!";

    /** Characters that are interpreted as FALSE in input strings, the first is used in output. */
    private static final String FALSE_CHARS = "NnFf0.";

    /** An empty array used to create other arrays of formulas. */
    private static final Formula[] ZERO_LEN_FORMULA_ARR = new Formula[0];

    /**
     * Parses all 'param' and 'var' elements from a nonempty XML element. Any errors encountered are logged in the
     * element.
     *
     * @param elem        the element
     * @param evalContext the evaluation context to which to add the loaded parameters
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    public static boolean parseVars(final EvalContext evalContext, final NonemptyElement elem, final EParserMode mode) {

        // "<param .../>" is old style tag with formulas in attributes
        // "<var> ... </var>" is new style tag with formulas as child objects
        // We do one pass extracting "param" elements and a second pass for "var" elements.

        // If there are one or more comments that directly precedes a variable, we associate those comments with the
        // variable.

        // Log.info("Parsing Vars in a problem...");

        boolean result = true;
        final Collection<Comment> comments = new ArrayList<>(3);

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final IElement childElem) {

                final String tag = childElem.getTagName();

                if (child instanceof final EmptyElement empty) {
                    if (PARAM.equals(tag)) {
                        result = result && processEmptyParamElement(evalContext, empty, comments, mode);
                    } else if (VAR.equals(tag)) {
                        result = result && processEmptyVarElement(evalContext, empty, comments, mode);
                    }
                } else if (child instanceof final NonemptyElement nonempty) {
                    if (PARAM.equals(tag)) {
                        result = result && processNonemptyParamElement(evalContext, nonempty, comments, mode);
                    } else if (VAR.equals(tag)) {
                        result = result && processNonemptyVarElement(evalContext, nonempty, comments, mode);
                    }
                }
                comments.clear();
            } else if (child instanceof final Comment childComment) {
                comments.add(childComment);
            } else if (!(child instanceof CData)) {
                comments.clear();
            }
        }

        // for (final String varName : evalCtx.getVariableNames()) {
        // final AbstractVariable var = evalCtx.getVariable(varName);
        // Log.info(" ", varName, " = ", var.getValue());
        // }

        return result;
    }

    /**
     * Processes an old-style "&lt;param .../&gt;" element. These elements are problematic since a formula could have an
     * embedded span, which cannot live in an XML attribute reliably. However, the current problem bank contains these
     * elements, so they must be parsable.
     *
     * @param elem        the element
     * @param evalContext the evaluation context to which to add the parsed variable
     * @param comments    comments to associate with the variable
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean processEmptyParamElement(final EvalContext evalContext, final EmptyElement elem,
                                                    final Iterable<Comment> comments, final EParserMode mode) {

        boolean valid = true;

        if (mode.reportDeprecated) {
            elem.logError("Deprecated &lt;param&gt; element (20010)");
        }

        final String parameterName = elem.getStringAttr(NAME);
        final String parameterType = elem.getStringAttr(TYPE);
        final String valueTypeStr = elem.getStringAttr(VALUE_TYPE);

        if (parameterName == null) {
            if (mode.reportAny) {
                elem.logError("<param> element is missing 'name' attribute. (20011)");
            }
            valid = false;
        } else if (parameterName.indexOf(L_BRACE) != -1 || parameterName.indexOf(R_BRACE) != -1) {
            if (mode.reportAny) {
                elem.logError("Parameter names may not contain '{' or '}' (20012)");
            }
            valid = false;
        }

        if (parameterType == null) {
            if (mode.reportAny) {
                elem.logError("&lt;param&gt; element is missing 'type' attribute. (20013)");
            }
            valid = false;
        }

        EType type = EType.ERROR;
        if (valueTypeStr != null) {
            type = EType.forLabel(valueTypeStr);
        }

        // Create the variable object, and begin populating it.
        AbstractVariable variable = null;

        if (VariableInteger.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableInteger(parameterName);
        } else if (VariableReal.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableReal(parameterName);
        } else if (VariableBoolean.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableBoolean(parameterName);
        } else if (VariableSpan.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableSpan(parameterName);
        } else if (VariableRandomInteger.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableRandomInteger(parameterName);
        } else if (VariableRandomReal.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableRandomReal(parameterName);
        } else if (VariableRandomBoolean.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableRandomBoolean(parameterName);
        } else if (VariableRandomChoice.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableRandomChoice(parameterName, type);
        } else if (VariableDerived.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableDerived(parameterName, type);
        } else {
            if (mode.reportAny) {
                elem.logError("Unrecognized parameter type: " + parameterType + " (20014)");
            }
            valid = false;
        }

        if (variable != null) {
            if (variable.type == null) {
                variable.type = type;
            }

            // PARSE min/max for variables that need it...

            final String minStr = elem.getStringAttr(MIN);
            final String maxStr = elem.getStringAttr(MAX);

            switch (variable) {
                case final VariableRandomInteger vRInt -> {
                    if (minStr != null) {
                        final Formula minFormula = FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                        if (minFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'min' formula. (20015)");
                            }
                            valid = false;
                        } else {
                            vRInt.setMin(new NumberOrFormula(minFormula));
                        }
                    }
                    if (maxStr != null) {
                        final Formula maxFormula = FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                        if (maxFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'max' formula. (20016)");
                            }
                            valid = false;
                        } else {
                            vRInt.setMax(new NumberOrFormula(maxFormula));
                        }
                    }

                    if (vRInt.getMin() == null || vRInt.getMax() == null) {
                        if (mode.reportAny) {
                            elem.logError("'random-int' parameters require 'min' and 'max' attributes. (20017)");
                        }
                        valid = false;
                    }
                }
                case final VariableRandomReal vRReal -> {
                    if (minStr != null) {
                        final Formula minFormula = FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                        if (minFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'min' formula. (20018)");
                            }
                            valid = false;
                        } else {
                            vRReal.setMin(new NumberOrFormula(minFormula));
                        }
                    }
                    if (maxStr != null) {
                        final Formula maxFormula = FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                        if (maxFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'max' formula. (20019)");
                            }
                            valid = false;
                        } else {
                            vRReal.setMax(new NumberOrFormula(maxFormula));
                        }
                    }

                    if (vRReal.getMin() == null || vRReal.getMax() == null) {
                        if (mode.reportAny) {
                            elem.logError("'random-real' parameters require 'min' and 'max' attributes. (20020)");
                        }
                        valid = false;
                    }
                }
                case final VariableDerived vDer -> {
                    if (minStr != null) {
                        final Formula minFormula = FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                        if (minFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'min' formula. (20021)");
                            }
                            valid = false;
                        } else {
                            vDer.setMin(new NumberOrFormula(minFormula));
                        }
                    }
                    if (maxStr != null) {
                        final Formula maxFormula = FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                        if (maxFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'max' formula. (20022)");
                            }
                            valid = false;
                        } else {
                            vDer.setMax(new NumberOrFormula(maxFormula));
                        }
                    }
                }
                default -> {
                    // All other types may not have min or max values
                    if (minStr != null) {
                        if (mode.reportAny) {
                            elem.logError("Only random numeric or derived parameters can have minimum. (20023)");
                        }
                        valid = false;
                    }
                    if (maxStr != null) {
                        if (mode.reportAny) {
                            elem.logError("Only random numeric or derived parameters can have maximum. (20024)");
                        }
                        valid = false;
                    }
                }
            }

            final String excludeStr = elem.getStringAttr(EXCLUDE);

            if (excludeStr != null) {
                if (variable instanceof final IExcludableVariable excludeVar) {
                    final String[] split = excludeStr.split(CoreConstants.COMMA);
                    final List<Formula> formulas = new ArrayList<>(split.length);

                    for (final String toParse : split) {
                        final Formula formula = FormulaFactory.parseFormulaString(evalContext, toParse, mode);

                        if (formula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'exclude' formula. (20025)");
                            }
                            valid = false;
                        } else {
                            formulas.add(formula);
                        }
                    }

                    if (formulas.isEmpty()) {
                        if (mode.reportAny) {
                            elem.logError("Empty exclude value in parameter. (20026)");
                        }
                        valid = false;
                    } else {
                        final Formula[] excludeFormulae = formulas.toArray(ZERO_LEN_FORMULA_ARR);
                        excludeVar.setExcludes(excludeFormulae);
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only random or derived integer or choice parameter can have excludes. (20027)");
                    }
                    valid = false;
                }
            }

            final String chooseFromStr = elem.getStringAttr(CHOOSE_FROM);

            if (chooseFromStr != null) {
                if (variable instanceof final VariableRandomChoice rcvar) {

                    final String[] split = chooseFromStr.split(CoreConstants.COMMA);
                    final List<Formula> formulas = new ArrayList<>(split.length);

                    for (final String toParse : split) {
                        final Formula formula = FormulaFactory.parseFormulaString(evalContext, toParse, mode);

                        if (formula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'choose-from' formula. (20028)");
                            }
                            valid = false;
                        } else {
                            if (type == null) {
                                final EType formulaType = formula.getType(evalContext);

                                if (formulaType != null && formulaType != EType.ERROR) {
                                    type = formulaType;
                                    variable.type = formulaType;
                                }
                            }
                            formulas.add(formula);
                        }
                    }

                    if (formulas.isEmpty()) {
                        if (mode.reportAny) {
                            elem.logError("Empty choose-from value in parameter. (20029)");
                        }
                        valid = false;
                    } else {
                        final Formula[] chooseFormulae = formulas.toArray(ZERO_LEN_FORMULA_ARR);
                        rcvar.setChooseFromList(chooseFormulae);
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only random-choice parameter can have choose-from list. (20030)");
                    }
                    valid = false;
                }
            }

            final String formulaStr = elem.getStringAttr(FORMULA);

            if (formulaStr != null) {
                if (variable instanceof final VariableDerived vDer) {

                    final Formula formula = FormulaFactory.parseFormulaString(evalContext, formulaStr, mode);
                    if (formula == null) {
                        if (mode.reportAny) {
                            elem.logError("Unable to parse 'formula' formula. (20031)");
                        }
                        valid = false;
                    } else {
                        vDer.setFormula(formula);

                        if (type == null) {
                            final EType formulaType = formula.getType(evalContext);

                            if (formulaType != null && formulaType != EType.ERROR) {
                                variable.type = formulaType;
                            }
                        }
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only derived parameter can have formula list. (20032)");
                    }
                    valid = false;
                }
            }

            final String valueStr = elem.getStringAttr(VALUE);

            if (valueStr == null) {
                if (variable instanceof VariableInteger || variable instanceof VariableReal
                    || variable instanceof VariableBoolean) {
                    if (mode.reportAny) {
                        elem.logError("Constant parameter with no value. (20033)");
                    }
                    valid = false;
                }
            } else if (variable instanceof VariableInteger || variable instanceof VariableReal
                       || variable instanceof VariableBoolean) {

                final Formula formula = FormulaFactory.parseFormulaString(evalContext, valueStr, mode);

                if (formula == null) {
                    if (mode.reportAny) {
                        elem.logError("Constant parameter with invalid value. (20034)");
                    }
                    valid = false;
                } else if (formula.parameterNames().length > 0) {
                    if (mode.reportAny) {
                        elem.logError("Constant value may not reference other parameters. (20035)");
                    }
                    valid = false;
                } else {
                    final Object evaluationResult = formula.evaluate(evalContext);

                    if (evaluationResult instanceof ErrorValue) {
                        if (mode.reportAny) {
                            elem.logError("Can't evaluate constant value:" + evaluationResult + " (20036)");
                        }
                        valid = false;
                    } else if (variable instanceof VariableInteger) {
                        if (evaluationResult instanceof Long) {
                            variable.setValue(evaluationResult);
                        } else {
                            if (mode.reportAny) {
                                elem.logError("Integer parameter value is not integer. (20037)");
                            }
                            valid = false;
                        }
                    } else if (variable instanceof VariableReal) {
                        if (evaluationResult instanceof final Long longVal) {
                            final double dblVal = longVal.doubleValue();
                            final Double dblObject = Double.valueOf(dblVal);
                            variable.setValue(dblObject);
                        } else if (evaluationResult instanceof Double) {
                            variable.setValue(evaluationResult);
                        } else {
                            if (mode.reportAny) {
                                elem.logError("Real parameter value is not real. (20038)");
                            }
                            valid = false;
                        }
                    } else if (evaluationResult instanceof Boolean) {
                        variable.setValue(evaluationResult);
                    } else {
                        if (mode.reportAny) {
                            elem.logError("Boolean parameter value is not boolean. (20039)");
                        }
                        valid = false;
                    }
                }
            } else {
                if (mode.reportAny) {
                    elem.logError("Only constant types may have value specifications. (20040)");
                }
                valid = false;
            }

            final String generatedIntegerStr = elem.getStringAttr(GENERATED_INTEGER);

            if (generatedIntegerStr != null) {
                if (variable instanceof VariableRandomInteger || variable instanceof VariableRandomReal
                    || variable instanceof VariableRandomChoice || variable instanceof VariableDerived) {

                    try {
                        final Long parsedLong = Long.valueOf(generatedIntegerStr);
                        variable.setValue(parsedLong);
                    } catch (final NumberFormatException e) {
                        if (mode.reportAny) {
                            elem.logError("Invalid 'generated-integer' value: " + generatedIntegerStr + "  (20041)");
                        }
                        valid = false;
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only random integer, random real, choice or derived parameter can have " +
                                      "generated integer value. (20042)");
                    }
                    valid = false;
                }
            }

            final String generatedRealStr = elem.getStringAttr(GENERATED_REAL);

            if (generatedRealStr != null) {
                if (variable instanceof VariableRandomReal || variable instanceof VariableRandomChoice
                    || variable instanceof VariableDerived) {

                    try {
                        final Double parsedDbl = Double.valueOf(generatedRealStr);
                        variable.setValue(parsedDbl);
                    } catch (final NumberFormatException e) {
                        if (mode.reportAny) {
                            elem.logError("Invalid 'generated-real' value: " + generatedRealStr + " (20043)");
                        }
                        valid = false;
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError(
                                "Only random real, choice or derived parameter can have generated real value. (20044)");
                    }
                    valid = false;
                }
            }

            final String generatedBooleanStr = elem.getStringAttr(GENERATED_BOOLEAN);

            if (generatedBooleanStr != null) {
                if (variable instanceof VariableRandomBoolean || variable instanceof VariableRandomChoice
                    || variable instanceof VariableDerived) {

                    try {
                        final Boolean parsedBoolean = parseBooleanValue(generatedBooleanStr);
                        variable.setValue(parsedBoolean);
                    } catch (final IllegalArgumentException ex) {
                        if (mode.reportAny) {
                            elem.logError("Invalid 'generated-boolean' value: " + generatedBooleanStr + " (20045)");
                        }
                        valid = false;
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError(
                                "Only random boolean, choice or derived parameter can have generated boolean value. " +
                                "(20046)");
                    }
                    valid = false;
                }
            }

            final String generatedString = elem.getStringAttr(GENERATED_STRING);

            if (generatedString != null) {
                if (variable instanceof VariableDerived) {
                    try {
                        variable.setValue(generatedString);
                    } catch (final NumberFormatException e) {
                        if (mode.reportAny) {
                            elem.logError("Invalid 'generated-string' value: " + generatedString + " (20047)");
                        }
                        valid = false;
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only derived parameter can have generated string value (20048)");
                    }
                    valid = false;
                }
            }

            final String decimalFormatStr = elem.getStringAttr(DECIMAL_FORMAT);

            if (decimalFormatStr != null) {
                if (variable instanceof final AbstractFormattableVariable fmt) {
                    fmt.setFormatPattern(decimalFormatStr);
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only formattable variables may have decimal-format. (20049)");
                    }
                    valid = false;
                }
            }

            if (variable instanceof VariableSpan) {
                if (mode.reportAny) {
                    elem.logError("Span parameter must contain span elements. (20050)");
                }
                valid = false;
            }

            if (valid) {
                if (evalContext.getVariable(variable.name) == null) {
                    for (final Comment comment : comments) {
                        final String trimmed = comment.getContent().trim();
                        variable.addComment(trimmed);
                    }
                    evalContext.addVariable(variable);
                } else {
                    if (mode.reportAny) {
                        elem.logError("Parameter '" + variable.name + "' is duplicated. (20051)");
                    }
                    valid = false;
                }
            }
        }

        return valid;
    }

    /**
     * Processes an old-style "&lt;param .../&gt;" element. These elements are problematic since a formula could have an
     * embedded span, which cannot live in an XML attribute reliably. However, the current problem bank contains these
     * elements, so they must be parsable.
     *
     * @param elem        the element
     * @param evalContext the evaluation context to which to add the parsed variable
     * @param comments    comments to associate with the variable
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean processNonemptyParamElement(final EvalContext evalContext, final NonemptyElement elem,
                                                       final Iterable<Comment> comments, final EParserMode mode) {

        boolean valid = true;

        if (mode.reportDeprecated) {
            elem.logError("Deprecated &lt;param&gt; element (20060)");
        }

        final String parameterName = elem.getStringAttr(NAME);
        final String parameterType = elem.getStringAttr(TYPE);
        final String valueTypeStr = elem.getStringAttr(VALUE_TYPE);

        if (parameterName == null) {
            if (mode.reportAny) {
                elem.logError("<param> element is missing 'name' attribute. (20061)");
            }
            valid = false;
        } else if (parameterName.indexOf(L_BRACE) != -1 || parameterName.indexOf(R_BRACE) != -1) {
            if (mode.reportAny) {
                elem.logError("Parameter names may not contain '{' or '}' (20062)");
            }
            valid = false;
        }

        if (parameterType == null) {
            if (mode.reportAny) {
                elem.logError("<param> element is missing 'type' attribute. (20063)");
            }
            valid = false;
        }

        EType type = null;
        if (valueTypeStr != null) {
            type = EType.forLabel(valueTypeStr);
        }

        // Create the variable object, and begin populating it.
        AbstractVariable variable = null;

        if (VariableInteger.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableInteger(parameterName);
        } else if (VariableReal.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableReal(parameterName);
        } else if (VariableBoolean.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableBoolean(parameterName);
        } else if (VariableSpan.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableSpan(parameterName);
        } else if (VariableRandomInteger.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableRandomInteger(parameterName);
        } else if (VariableRandomReal.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableRandomReal(parameterName);
        } else if (VariableRandomBoolean.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableRandomBoolean(parameterName);
        } else if (VariableRandomChoice.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            // We make some bogus assumption about type until we learn more
            variable = new VariableRandomChoice(parameterName, EType.SPAN);
        } else if (VariableDerived.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            // We make some bogus assumption about type until we learn more
            variable = new VariableDerived(parameterName, EType.SPAN);
        } else if (VariableInputInteger.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableInputInteger(parameterName);
        } else if (VariableInputReal.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            variable = new VariableInputReal(parameterName);
        } else {
            if (mode.reportAny) {
                elem.logError("Unrecognized parameter type: " + parameterType + " (20064)");
            }
            valid = false;
        }

        if (variable != null) {
            if (variable.type == null) {
                variable.type = type;
            }

            // PARSE min/max for variables that need it...

            final String minStr = elem.getStringAttr(MIN);
            final String maxStr = elem.getStringAttr(MAX);

            switch (variable) {
                case final VariableRandomInteger vRInt -> {
                    if (minStr != null) {
                        final Formula minFormula = FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                        if (minFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'min' formula. (20065)");
                            }
                            valid = false;
                        } else {
                            vRInt.setMin(new NumberOrFormula(minFormula));
                        }
                    }
                    if (maxStr != null) {
                        final Formula maxFormula = FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                        if (maxFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'max' formula. (20066)");
                            }
                            valid = false;
                        } else {
                            vRInt.setMax(new NumberOrFormula(maxFormula));
                        }
                    }

                    if (vRInt.getMin() == null || vRInt.getMax() == null) {
                        if (mode.reportAny) {
                            elem.logError("'random-int' parameters require 'min' and 'max' attributes. (20067)");
                        }
                        valid = false;
                    }
                }
                case final VariableRandomReal vRReal -> {
                    if (minStr != null) {
                        final Formula minFormula = FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                        if (minFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'min' formula. (20068)");
                            }
                            valid = false;
                        } else {
                            vRReal.setMin(new NumberOrFormula(minFormula));
                        }
                    }
                    if (maxStr != null) {
                        final Formula maxFormula = FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                        if (maxFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'max' formula. (20069)");
                            }
                            valid = false;
                        } else {
                            vRReal.setMax(new NumberOrFormula(maxFormula));
                        }
                    }

                    if (vRReal.getMin() == null || vRReal.getMax() == null) {
                        if (mode.reportAny) {
                            elem.logError("'random-real' parameters require 'min' and 'max' attributes. (20070)");
                        }
                        valid = false;
                    }
                }
                case final VariableDerived vDer -> {
                    if (minStr != null) {
                        final Formula minFormula = FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                        if (minFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'min' formula. (20071)");
                            }
                            valid = false;
                        } else {
                            vDer.setMin(new NumberOrFormula(minFormula));
                        }
                    }
                    if (maxStr != null) {
                        final Formula maxFormula = FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                        if (maxFormula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'max' formula. (20072)");
                            }
                            valid = false;
                        } else {
                            vDer.setMax(new NumberOrFormula(maxFormula));
                        }
                    }
                }
                default -> {
                    // All other types may not have min or max values
                    if (minStr != null) {
                        if (mode.reportAny) {
                            elem.logError("Only random numeric or derived parameters can have minimum. (20073)");
                        }
                        valid = false;
                    }
                    if (maxStr != null) {
                        if (mode.reportAny) {
                            elem.logError("Only random numeric or derived parameters can have maximum. (20074)");
                        }
                        valid = false;
                    }
                }
            }

            final String excludeStr = elem.getStringAttr(EXCLUDE);

            if (excludeStr != null) {
                if (variable instanceof final IExcludableVariable excludeVar) {
                    final String[] split = excludeStr.split(CoreConstants.COMMA);
                    final List<Formula> formulas = new ArrayList<>(split.length);

                    for (final String toParse : split) {
                        final Formula formula = FormulaFactory.parseFormulaString(evalContext, toParse, mode);

                        if (formula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'exclude' formula. (20075)");
                            }
                            valid = false;
                        } else {
                            formulas.add(formula);
                        }
                    }

                    if (formulas.isEmpty()) {
                        if (mode.reportAny) {
                            elem.logError("Empty exclude value in parameter. (20076)");
                        }
                        valid = false;
                    } else {
                        final Formula[] excludeFormulae = formulas.toArray(ZERO_LEN_FORMULA_ARR);
                        excludeVar.setExcludes(excludeFormulae);
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only random or derived integer or choice parameter can have excludes. (20077)");
                    }
                    valid = false;
                }
            }

            final String chooseFromStr = elem.getStringAttr(CHOOSE_FROM);

            if (chooseFromStr != null) {
                if (variable instanceof final VariableRandomChoice rcvar) {

                    final String[] split = chooseFromStr.split(CoreConstants.COMMA);
                    final List<Formula> formulas = new ArrayList<>(split.length);

                    for (final String toParse : split) {
                        final Formula formula = FormulaFactory.parseFormulaString(evalContext, toParse, mode);

                        if (formula == null) {
                            if (mode.reportAny) {
                                elem.logError("Unable to parse 'choose-from' formula. (20078)");
                            }
                            valid = false;
                        } else {
                            if (type == null) {
                                final EType formulaType = formula.getType(evalContext);

                                if (formulaType != null && formulaType != EType.ERROR) {
                                    type = formulaType;
                                    variable.type = formulaType;
                                }
                            }
                            formulas.add(formula);
                        }
                    }

                    if (formulas.isEmpty()) {
                        if (mode.reportAny) {
                            elem.logError("Empty choose-from value in parameter. (20079)");
                        }
                        valid = false;
                    } else {
                        final Formula[] chooseFormulae = formulas.toArray(ZERO_LEN_FORMULA_ARR);
                        rcvar.setChooseFromList(chooseFormulae);

                        if (type != null) {
                            rcvar.setType(type);
                        }
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only random-choice parameter can have choose-from list. (20080)");
                    }
                    valid = false;
                }
            }

            final String formulaStr = elem.getStringAttr(FORMULA);

            if (formulaStr != null) {
                if (variable instanceof final VariableDerived vDer) {
                    final Formula formula = FormulaFactory.parseFormulaString(evalContext, formulaStr, mode);

                    if (formula == null) {
                        if (mode.reportAny) {
                            elem.logError("Unable to parse 'formula' formula. (20081)");
                        }
                        valid = false;
                    } else {
                        vDer.setFormula(formula);

                        if (type == null) {
                            final EType formulaType = formula.getType(evalContext);

                            if (formulaType != null && formulaType != EType.ERROR) {
                                variable.type = formulaType;
                            }
                        }
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only derived parameter can have formula list. (20082)");
                    }
                    valid = false;
                }
            }

            final String valueStr = elem.getStringAttr(VALUE);

            if (valueStr == null) {
                if (variable instanceof VariableInteger || variable instanceof VariableReal
                    || variable instanceof VariableBoolean) {
                    if (mode.reportAny) {
                        elem.logError("Constant parameter with no value. (20083)");
                    }
                    valid = false;
                }
            } else if (variable instanceof VariableInteger || variable instanceof VariableReal
                       || variable instanceof VariableBoolean || variable instanceof VariableInputInteger
                       || variable instanceof VariableInputReal) {

                final Formula formula =
                        FormulaFactory.parseFormulaString(evalContext, valueStr, mode);

                if (formula == null) {
                    if (mode.reportAny) {
                        elem.logError("Constant parameter with invalid value. (20084)");
                    }
                    valid = false;
                } else if (formula.parameterNames().length > 0) {
                    if (mode.reportAny) {
                        elem.logError("Constant value may not reference other parameters. (20085)");
                    }
                    valid = false;
                } else {
                    final Object evaluationResult = formula.evaluate(evalContext);

                    if (evaluationResult instanceof ErrorValue) {
                        if (mode.reportAny) {
                            elem.logError("Can't evaluate constant value: " + evaluationResult + " (20086)");
                        }
                        valid = false;
                    } else if (variable instanceof VariableInteger || variable instanceof VariableInputInteger) {
                        if (evaluationResult instanceof Long) {
                            variable.setValue(evaluationResult);
                        } else {
                            if (mode.reportAny) {
                                elem.logError("Integer parameter value is not integer. (20087)");
                            }
                            valid = false;
                        }
                    } else if (variable instanceof VariableReal || variable instanceof VariableInputReal) {
                        if (evaluationResult instanceof final Long longVal) {
                            final double dblVal = longVal.doubleValue();
                            final Double dblObject = Double.valueOf(dblVal);
                            variable.setValue(dblObject);
                        } else if (evaluationResult instanceof Double) {
                            variable.setValue(evaluationResult);
                        } else {
                            if (mode.reportAny) {
                                elem.logError("Real parameter value is not real. (20088)");
                            }
                            valid = false;
                        }
                    } else if (evaluationResult instanceof Boolean) {
                        variable.setValue(evaluationResult);
                    } else {
                        if (mode.reportAny) {
                            elem.logError("Boolean parameter value is not boolean. (20089)");
                        }
                        valid = false;
                    }
                }
            } else {
                if (mode.reportAny) {
                    elem.logError("Only constant types may have value specifications. (20090)");
                }
                valid = false;
            }

            final String generatedIntegerStr = elem.getStringAttr(GENERATED_INTEGER);

            if (generatedIntegerStr != null) {
                if (variable instanceof VariableRandomInteger || variable instanceof VariableRandomReal
                    || variable instanceof VariableRandomChoice || variable instanceof VariableDerived) {

                    try {
                        final Long parsedLong = Long.valueOf(generatedIntegerStr);
                        variable.setValue(parsedLong);
                    } catch (final NumberFormatException e) {
                        if (mode.reportAny) {
                            elem.logError("Invalid 'generated-integer' value: " + generatedIntegerStr + " (20091)");
                        }
                        valid = false;
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only random integer, random real, choice or derived parameter can have " +
                                      "generated integer value. (20092)");
                    }
                    valid = false;
                }
            }

            final String generatedRealStr = elem.getStringAttr(GENERATED_REAL);

            if (generatedRealStr != null) {
                if (variable instanceof VariableRandomReal || variable instanceof VariableRandomChoice
                    || variable instanceof VariableDerived) {

                    try {
                        final Double parsedDbl = Double.valueOf(generatedRealStr);
                        variable.setValue(parsedDbl);
                    } catch (final NumberFormatException e) {
                        if (mode.reportAny) {
                            elem.logError("Invalid 'generated-real' value: " + generatedRealStr + " (20093)");
                        }
                        valid = false;
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError(
                                "Only random real, choice or derived parameter can have generated real value. (20094)");
                    }
                    valid = false;
                }
            }

            final String generatedBooleanStr = elem.getStringAttr(GENERATED_BOOLEAN);

            if (generatedBooleanStr != null) {
                if (variable instanceof VariableRandomBoolean || variable instanceof VariableRandomChoice
                    || variable instanceof VariableDerived) {

                    try {
                        final Boolean parsedBoolean = parseBooleanValue(generatedBooleanStr);
                        variable.setValue(parsedBoolean);
                    } catch (final IllegalArgumentException ex) {
                        if (mode.reportAny) {
                            elem.logError("Invalid 'generated-boolean' value: " + generatedBooleanStr + " (20095)");
                        }
                        valid = false;
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError(
                                "Only random boolean, choice or derived parameter can have generated boolean value. " +
                                "(20096)");
                    }
                    valid = false;
                }
            }

            final String generatedString = elem.getStringAttr(GENERATED_STRING);

            if (generatedString != null) {
                if (variable instanceof VariableDerived) {
                    try {
                        variable.setValue(generatedString);
                    } catch (final NumberFormatException e) {
                        if (mode.reportAny) {
                            elem.logError("Invalid 'generated-string' value: " + generatedString + " (20097)");
                        }
                        valid = false;
                    }
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only derived parameter can have generated string value. (20098)");
                    }
                    valid = false;
                }
            }

            final String decimalFormatStr = elem.getStringAttr(DECIMAL_FORMAT);

            if (decimalFormatStr != null) {
                if (variable instanceof final AbstractFormattableVariable fmt) {
                    fmt.setFormatPattern(decimalFormatStr);
                } else {
                    if (mode.reportAny) {
                        elem.logError("Only formattable variables may have decimal-format. (20099)");
                    }
                    valid = false;
                }
            }

            if (variable instanceof VariableSpan) {
                final DocSimpleSpan span = DocFactory.parseSpan(evalContext, elem, mode);
                if (span != null && span.numChildren() > 0) {
                    variable.setValue(span);
                }
            } else if ((variable instanceof VariableDerived || variable instanceof VariableRandomChoice)) {
                DocSimpleSpan span = DocFactory.parseSpan(evalContext, elem, mode);
                if (span == null) {
                    span = new DocSimpleSpan();
                }
                variable.setValue(span);
            }

            if (valid) {
                if (evalContext.getVariable(variable.name) == null) {
                    for (final Comment comment : comments) {
                        final String trimmed = comment.getContent().trim();
                        variable.addComment(trimmed);
                    }
                    evalContext.addVariable(variable);
                } else {
                    if (mode.reportAny) {
                        elem.logError("Parameter '" + variable.name + "' is duplicated. (20100)");
                    }
                    valid = false;
                }
            }
        }

        return valid;
    }

    /**
     * Processes an empty "var" element (valid for 'boolean', 'input-int', 'input-real', 'int', 'random-boolean', and
     * 'real' types).
     *
     * <pre>
     * &lt;var name='...' type='...' format='...' value='...'/&gt;
     * </pre>
     *
     * @param elem        the element
     * @param evalContext the evaluation context to which to add the parsed variable
     * @param comments    comments to associate with the variable
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean processEmptyVarElement(final EvalContext evalContext, final EmptyElement elem,
                                                  final Iterable<Comment> comments, final EParserMode mode) {

        boolean valid = false;

        final String varName = elem.getStringAttr(NAME);
        final String typeStr = elem.getStringAttr(TYPE);

        if (varName == null) {
            if (mode.reportAny) {
                elem.logError("Missing 'name' attribute on <var> element (20200)");
            }
        } else if (typeStr == null) {
            if (mode.reportAny) {
                elem.logError("Missing 'type' attribute on <var> element (20201)");
            }
        } else if (varName.indexOf(L_BRACE) != -1 || varName.indexOf(R_BRACE) != -1) {
            if (mode.reportAny) {
                elem.logError("Variable names may not contain '{' or '}' (20202)");
            }
        } else {
            AbstractVariable variable = null;

            if (VariableBoolean.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableBoolean(elem, varName, mode);
            } else if (VariableInteger.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableInteger(elem, varName, mode);
            } else if (VariableReal.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableReal(elem, varName, mode);
            } else if (VariableRandomBoolean.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomBoolean(elem, varName, mode);
            } else if (VariableRandomInteger.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomInteger(elem, varName, mode);
            } else if (VariableRandomReal.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomReal(elem, varName, mode);
            } else if (VariableRandomPermutation.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomPermutation(elem, varName, mode);
            } else if (VariableRandomSimpleAngle.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomSimpleAngle(elem, varName, mode);

            } else if (VariableInputInteger.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableInputInteger(varName);
            } else if (VariableInputIntegerVector.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableInputIntegerVector(varName);
            } else if (VariableInputReal.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableInputReal(varName);
            } else if (VariableInputString.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableInputString(varName);
            } else if (mode.reportAny) {
                elem.logError("Unrecognized empty-element variable type. (20203)");
            }

            if (variable != null) {
                if (evalContext.getVariable(variable.name) == null) {
                    for (final Comment comment : comments) {
                        final String trimmed = comment.getContent().trim();
                        variable.addComment(trimmed);
                    }
                    evalContext.addVariable(variable);
                    valid = true;
                } else if (mode.reportAny) {
                    elem.logError("Variable '" + variable.name + "' is duplicated. (20204)");
                }
            }
        }

        return valid;
    }

    /**
     * Processes a nonempty "var" element (valid for 'derived', 'random-choice', 'random-imt', 'random-real', and 'span'
     * types).
     *
     * <pre>
     * &lt;var name='...' type='...' long='#' double='#' boolean='...' format='...' value='#'&gt;
     *   &lt;min&gt; ...min value formula... &lt;/min&gt;
     *   &lt;max&gt; ...min value formula... &lt;/max&gt;
     *   &lt;exclude&gt; ...list of excluded values formulas... &lt;/exclude&gt;
     *   &lt;choose-from&gt; ...list of formulas of values to choose from... &lt;/choose-from&gt;
     *   (Sequence of AbstractDocObjects for 'span' type)
     * &lt;/var&gt;
     * </pre>
     *
     * @param elem        the element
     * @param evalContext the evaluation context to which to add the parsed variable
     * @param comments    comments to associate with the variable
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean processNonemptyVarElement(final EvalContext evalContext, final NonemptyElement elem,
                                                     final Iterable<Comment> comments, final EParserMode mode) {

        boolean valid = false;

        final String varName = elem.getStringAttr(NAME);
        final String typeStr = elem.getStringAttr(TYPE);

        if (varName == null) {
            if (mode.reportAny) {
                elem.logError("Missing 'name' attribute on <var> element (20210)");
            }
        } else if (typeStr == null) {
            if (mode.reportAny) {
                elem.logError("Missing 'type' attribute on <var> element (20211)");
            }
        } else if (varName.indexOf(L_BRACE) != -1 || varName.indexOf(R_BRACE) != -1) {
            if (mode.reportAny) {
                elem.logError("Variable names may not contain '{' or '}' (20212)");
            }
        } else {
            if (mode.reportAny && elem.getElementChildrenAsList().isEmpty() &&
                !VariableSpan.TYPE_TAG.contentEquals(typeStr)) {
                elem.logError("Variable could be in '&lt;var ... /&gt;' format. (20213)");
            }

            AbstractVariable variable = null;

            if (VariableBoolean.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableBoolean(elem, varName, mode);
            } else if (VariableInteger.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableInteger(elem, varName, mode);
            } else if (VariableReal.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableReal(elem, varName, mode);
            } else if (VariableSpan.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableSpan(evalContext, elem, varName, mode);
            } else if (VariableRandomBoolean.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomBoolean(elem, varName, mode);
            } else if (VariableRandomInteger.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomInteger(evalContext, elem, varName, mode);
            } else if (VariableRandomReal.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomReal(evalContext, elem, varName, mode);
            } else if (VariableRandomPermutation.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomPermutation(evalContext, elem, varName, mode);
            } else if (VariableRandomChoice.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomChoice(evalContext, elem, varName, mode);
            } else if (VariableRandomSimpleAngle.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableRandomSimpleAngle(evalContext, elem, varName, mode);
            } else if (VariableDerived.TYPE_TAG.contentEquals(typeStr)) {
                variable = extractVariableDerived(evalContext, elem, varName, mode);
            } else if (mode.reportAny) {
                elem.logError("Unrecognized nonempty-element variable type. (20214)");
            }

            if (variable != null) {
                if (evalContext.getVariable(variable.name) == null) {
                    for (final Comment comment : comments) {
                        final String trimmed = comment.getContent().trim();
                        variable.addComment(trimmed);
                    }
                    evalContext.addVariable(variable);
                    valid = true;
                } else if (mode.reportAny) {
                    elem.logError("Variable '" + variable.name + "' is duplicated. (20215)");
                }
            }
        }

        return valid;
    }

    /**
     * Extracts a {@code VariableBoolean} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='boolean' value='...'/&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return the parsed variable on success; null on failure
     */
    private static VariableBoolean extractVariableBoolean(final EmptyElement elem, final String varName,
                                                          final EParserMode mode) {

        VariableBoolean result = null;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || VALUE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20220)";
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);

        if (valueStr == null) {
            if (mode.reportAny) {
                elem.logError("Boolean <var> element missing required 'value' attribute in {" + varName + "} (20221)");
            }
        } else if (TRUE_STRING.equalsIgnoreCase(valueStr)) {
            result = new VariableBoolean(varName);
            result.setValue(Boolean.TRUE);
        } else if (FALSE_STRING.equalsIgnoreCase(valueStr)) {
            result = new VariableBoolean(varName);
            result.setValue(Boolean.FALSE);
        } else if (mode.reportAny) {
            elem.logError("Invalid boolean value: '" + valueStr + "' in {" + varName + "} (20222)");
        }

        return result;
    }

    /**
     * Extracts a {@code VariableBoolean} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='boolean' value='true|false'/&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return the parsed variable on success; null on failure
     */
    private static VariableBoolean extractVariableBoolean(final NonemptyElement elem, final String varName,
                                                          final EParserMode mode) {

        VariableBoolean result = null;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || VALUE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20230)";
                elem.logError(msg);
            }
        }

        final List<IElement> children = elem.getElementChildrenAsList();
        if (children.isEmpty()) {
            final String valueStr = elem.getStringAttr(VALUE);

            if (valueStr == null) {
                if (mode.reportAny) {
                    elem.logError("Boolean <var> element missing required 'value' attribute in {" + varName
                                  + "} (20231)");
                }
            } else if (TRUE_STRING.equalsIgnoreCase(valueStr)) {
                result = new VariableBoolean(varName);
                result.setValue(Boolean.TRUE);
            } else if (FALSE_STRING.equalsIgnoreCase(valueStr)) {
                result = new VariableBoolean(varName);
                result.setValue(Boolean.FALSE);
            } else if (mode.reportAny) {
                elem.logError("Invalid boolean value: '" + valueStr + "' in {" + varName + "} (20232)");
            }
        } else if (mode.reportAny) {
            elem.logError("Boolean variable may not contain child elements. (20233)");
        }

        return result;
    }

    /**
     * Extracts a {@code VariableInteger} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='int' value='...' format='...'/&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return the parsed variable on success; null on failure
     */
    private static VariableInteger extractVariableInteger(final EmptyElement elem, final String varName,
                                                          final EParserMode mode) {

        VariableInteger result = null;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || VALUE.equals(attrName) || FORMAT.equals(
                        attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20240)";
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);

        if (valueStr == null) {
            if (mode.reportAny) {
                elem.logError("Integer <var> element missing required 'value' attribute in {" + varName + "} (20241)");
            }
        } else {
            String formatStr = elem.getStringAttr(FORMAT);
            if (formatStr == null) {
                formatStr = elem.getStringAttr(DECIMAL_FORMAT);
                if (mode.reportDeprecated && formatStr != null) {
                    elem.logError("'decimal-format' is deprecated, use 'format' instead. (20242)");
                }
            }

            if (mode.reportAny && formatStr != null && formatStr.startsWith("##")) {
                elem.logError("Perhaps format string in integer variable '" + varName
                              + "' could be simplified? (20243)");
            }

            try {
                final Long longVal = Long.valueOf(valueStr);
                result = new VariableInteger(varName);
                result.setValue(longVal);
                result.setFormatPattern(formatStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "} (20244)");
                }
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableInteger} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='int' format='...'&gt;
     *     &lt;formula&gt; ... &lt;/formula&gt;
     * &lt;/var&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return the parsed variable on success; null on failure
     */
    private static VariableInteger extractVariableInteger(final NonemptyElement elem, final String varName,
                                                          final EParserMode mode) {

        VariableInteger result = null;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || VALUE.equals(attrName) || FORMAT.equals(
                        attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20250)";
                elem.logError(msg);
            }
        }

        String formatStr = elem.getStringAttr(FORMAT);
        if (formatStr == null) {
            formatStr = elem.getStringAttr(DECIMAL_FORMAT);
            if (mode.reportDeprecated && formatStr != null) {
                elem.logError("'decimal-format' is deprecated, use 'format' instead. (20251)");
            }
        }

        if (mode.reportAny && formatStr != null && formatStr.startsWith("##")) {
            elem.logError("Perhaps format string in integer variable '" + varName + "' could be simplified? (20252)");
        }

        final List<IElement> children = elem.getElementChildrenAsList();
        if (children.isEmpty()) {
            final String valueStr = elem.getStringAttr(VALUE);
            if (valueStr == null) {
                if (mode.reportAny) {
                    elem.logError("Integer <var> element missing required 'value' attribute in {" + varName
                                  + "} (20253)");
                }
            } else {
                try {
                    final Long longVal = Long.valueOf(valueStr);
                    result = new VariableInteger(varName);
                    result.setValue(longVal);
                    result.setFormatPattern(formatStr);
                } catch (final NumberFormatException ex) {
                    if (mode.reportAny) {
                        elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "} (20254)");
                    }
                }
            }
        } else if (mode.reportAny) {
            elem.logError("Integer variable may not contain child elements. (20255)");
        }

        return result;
    }

    /**
     * Extracts a {@code VariableReal} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='real' value='...' format='...'/&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return the parsed variable on success; null on failure
     */
    private static VariableReal extractVariableReal(final EmptyElement elem, final String varName,
                                                    final EParserMode mode) {

        VariableReal result = null;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || VALUE.equals(attrName) || FORMAT.equals(
                        attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20260)";
                elem.logError(msg);
            }
        }

        String formatStr = elem.getStringAttr(FORMAT);
        if (formatStr == null) {
            formatStr = elem.getStringAttr(DECIMAL_FORMAT);
            if (mode.reportDeprecated && formatStr != null) {
                elem.logError("'decimal-format' is deprecated, use 'format' instead. (20261)");
            }
        }

        if (mode.reportAny && formatStr != null && formatStr.startsWith("##")) {
            elem.logError("Perhaps format string in real variable '" + varName + "' could be simplified? (20262)");
        }

        final String valueStr = elem.getStringAttr(VALUE);
        if (valueStr == null) {
            if (mode.reportAny) {
                elem.logError("Real <var> element missing required 'value' attribute in {" + varName + "} (20263)");
            }
        } else {
            try {
                final Number numberVal = NumberParser.parse(valueStr);
                result = new VariableReal(varName);
                result.setValue(numberVal);
                result.setFormatPattern(formatStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid real value: '" + valueStr + "' in {" + varName + "} (20264)");
                }
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableReal} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='real' value='...' format='...'&gt;
     *     &lt;formula&gt; ... &lt;/formula&gt;
     * &lt;/var&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return the parsed variable on success; null on failure
     */
    private static VariableReal extractVariableReal(final NonemptyElement elem, final String varName,
                                                    final EParserMode mode) {

        VariableReal result = null;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || VALUE.equals(attrName) || FORMAT.equals(
                        attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20270)";
                elem.logError(msg);
            }
        }

        String formatStr = elem.getStringAttr(FORMAT);
        if (formatStr == null) {
            formatStr = elem.getStringAttr(DECIMAL_FORMAT);
            if (mode.reportDeprecated && formatStr != null) {
                elem.logError("'decimal-format' is deprecated, use 'format' instead. (20271)");
            }
        }

        if (mode.reportAny && formatStr != null && formatStr.startsWith("##")) {
            elem.logError("Perhaps format string in real variable '" + varName + "' could be simplified? (20272)");
        }

        final List<IElement> children = elem.getElementChildrenAsList();
        if (children.isEmpty()) {
            final String valueStr = elem.getStringAttr(VALUE);

            if (valueStr == null) {
                if (mode.reportAny) {
                    elem.logError("Real <var> element missing required 'value' attribute in {" + varName + "} (20273)");
                }
            } else {
                try {
                    final Number numberVal = NumberParser.parse(valueStr);
                    result = new VariableReal(varName);
                    result.setValue(numberVal);
                    result.setFormatPattern(formatStr);
                } catch (final NumberFormatException ex) {
                    if (mode.reportAny) {
                        elem.logError("Invalid real value: '" + valueStr + "' in {" + varName + "} (20274)");
                    }
                }
            }
        } else if (mode.reportAny) {
            elem.logError("Real variable may not contain child elements. (20275)");
        }

        return result;
    }

    /**
     * Extracts a {@code VariableSpan} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='span'&gt;
     *   ...DocSimpleSpan children XML...
     * &lt;/var&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param varName     the variable name
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableSpan extractVariableSpan(final EvalContext evalContext, final NonemptyElement elem,
                                                    final String varName, final EParserMode mode) {

        final VariableSpan result;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20280)";
                elem.logError(msg);
            }
        }

        final DocSimpleSpan span = DocFactory.parseSpan(evalContext, elem, mode);

        if (span == null) {
            result = null;
        } else {
            result = new VariableSpan(varName);
            result.setValue(span);
        }

        return result;
    }

    /**
     * Extracts a {@code VariableRandomBoolean} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-boolean' value='...'/&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomBoolean extractVariableRandomBoolean(final EmptyElement elem, final String varName,
                                                                      final EParserMode mode) {

        VariableRandomBoolean result = null;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20290)";
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);

        if (valueStr == null) {
            result = new VariableRandomBoolean(varName);
        } else if (TRUE_STRING.equalsIgnoreCase(valueStr)) {
            result = new VariableRandomBoolean(varName);
            result.setValue(Boolean.TRUE);
        } else if (FALSE_STRING.equalsIgnoreCase(valueStr)) {
            result = new VariableRandomBoolean(varName);
            result.setValue(Boolean.FALSE);
        } else if (mode.reportAny) {
            elem.logError("Invalid boolean value: '" + valueStr + "' in {" + varName + "} (20291)");
        }

        return result;
    }

    /**
     * Extracts a {@code VariableRandomBoolean} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-boolean' value='...'/&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomBoolean extractVariableRandomBoolean(final NonemptyElement elem,
                                                                      final String varName,
                                                                      final EParserMode mode) {

        VariableRandomBoolean result = null;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20300)";
                elem.logError(msg);
            }
        }

        final List<IElement> children = elem.getElementChildrenAsList();
        if (children.isEmpty()) {
            final String valueStr = elem.getStringAttr(VALUE);

            if (valueStr == null) {
                result = new VariableRandomBoolean(varName);
            } else if (TRUE_STRING.equalsIgnoreCase(valueStr)) {
                result = new VariableRandomBoolean(varName);
                result.setValue(Boolean.TRUE);
            } else if (FALSE_STRING.equalsIgnoreCase(valueStr)) {
                result = new VariableRandomBoolean(varName);
                result.setValue(Boolean.FALSE);
            } else if (mode.reportAny) {
                elem.logError("Invalid random boolean value: '" + valueStr + "' in {" + varName + "} (20301)");
            }
        } else if (mode.reportAny) {
            elem.logError("Random boolean variable may not contain child elements. (20302)");
        }

        return result;
    }

    /**
     * Extracts a {@code VariableRandomInteger} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-int' value='...' min='...' max='...' format='...'/&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomInteger extractVariableRandomInteger(final EmptyElement elem, final String varName,
                                                                      final EParserMode mode) {

        boolean valid = true;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || MIN.equals(attrName) || MAX.equals(attrName)
                    || EXCLUDE.equals(attrName) || FORMAT.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20310)";
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);
        Long value = null;

        if (valueStr != null) {
            try {
                value = Long.valueOf(valueStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "} (20311)");
                }
                valid = false;
            }
        }

        VariableRandomInteger result = null;

        final String minStr = elem.getStringAttr(MIN);
        final String maxStr = elem.getStringAttr(MAX);

        if (minStr == null || maxStr == null) {
            if (mode.reportAny) {
                elem.logError("Missing required min/max value in random integer var {" + varName + "} (20312)");
            }
        } else {
            NumberOrFormula min = null;
            try {
                final Long parsedMin = Long.valueOf(minStr);
                min = new NumberOrFormula(parsedMin);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "} (20313)");
                }
                valid = false;
            }

            NumberOrFormula max = null;
            try {
                final Long parsedMax = Long.valueOf(maxStr);
                max = new NumberOrFormula(parsedMax);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "} (20314)");
                }
                valid = false;
            }

            if (valid && mode.reportAny) {
                final Number minNumber = min.getNumber();
                final Number maxNumber = max.getNumber();

                if (minNumber.longValue() == maxNumber.longValue()) {
                    elem.logError("Random integer with min and max the same (20315)");
                }
            }

            String formatStr = elem.getStringAttr(FORMAT);
            if (formatStr == null) {
                formatStr = elem.getStringAttr(DECIMAL_FORMAT);
                if (mode.reportDeprecated && formatStr != null) {
                    elem.logError("'decimal-format' is deprecated, use 'format' instead. (20316)");
                }
            }

            if (mode.reportAny && formatStr != null && formatStr.startsWith("##")) {
                elem.logError("Perhaps format string in integer variable '" + varName
                              + "' could be simplified? (20317)");
            }

            if (valid) {
                result = new VariableRandomInteger(varName);
                result.setValue(value);
                result.setFormatPattern(formatStr);
                result.setMin(min);
                result.setMax(max);
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableRandomInteger} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-int' value='...'&gt;
     *   &lt;min&gt; ... formula contents ... &lt;/min&gt;
     *   &lt;max&gt; ... formula contents ... &lt;/max&gt;
     *   &lt;exclude&gt; ... formula contents ... &lt;/exclude&gt;
     *   &lt;exclude&gt; ... formula contents ... &lt;/exclude&gt;
     *   ...
     * &lt;/var&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param varName     the variable name
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomInteger extractVariableRandomInteger(final EvalContext evalContext,
                                                                      final NonemptyElement elem,
                                                                      final String varName, final EParserMode mode) {

        boolean valid = true;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || MIN.equals(attrName) || MAX.equals(attrName)
                    || EXCLUDE.equals(attrName) || FORMAT.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName + " (20320)");
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);
        Long value = null;

        if (valueStr != null) {
            try {
                value = Long.valueOf(valueStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "} (20321)");
                }
                valid = false;
            }
        }

        VariableRandomInteger result = null;

        NumberOrFormula min = null;
        NumberOrFormula max = null;

        final String minStr = elem.getStringAttr(MIN);
        if (minStr != null) {
            try {
                final Long parsedMin = Long.valueOf(minStr);
                min = new NumberOrFormula(parsedMin);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "} (20322)");
                }
                valid = false;
            }
        }

        final String maxStr = elem.getStringAttr(MAX);
        if (maxStr != null) {
            try {
                final Long parsedMax = Long.valueOf(maxStr);
                max = new NumberOrFormula(parsedMax);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "} (20323)");
                }
                valid = false;
            }
        }

        if (valid && min != null && max != null && mode.reportAny) {
            final Number minNumber = min.getNumber();
            final Number maxNumber = max.getNumber();

            if (minNumber != null && maxNumber != null && minNumber.longValue() == maxNumber.longValue()) {
                elem.logError("Random integer with min and max the same (20324)");
            }
        }

        if (valid) {
            final Collection<Formula> excludes = new ArrayList<>(5);

            label:
            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    switch (tag) {
                        case MIN -> {
                            if (min == null) {
                                final Formula minFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty,
                                        mode);
                                if (minFormula == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid 'min' formula in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20325)");
                                    }
                                    valid = false;
                                } else {
                                    if (mode.reportAny && minFormula.isConstant()) {
                                        elem.logError("Constant 'min' in {" + varName
                                                      + "} could be specified in attribute? (20326)");
                                    }
                                    min = new NumberOrFormula(minFormula);
                                }
                            } else {
                                if (mode.reportAny) {
                                    elem.logError("Multiple 'min' values in {" + varName + "} (20327)");
                                }
                                break label;
                            }
                        }
                        case MAX -> {
                            if (max == null) {
                                final Formula maxFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty,
                                        mode);
                                if (maxFormula == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid 'max' formula in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20328)");
                                    }
                                    valid = false;
                                } else {
                                    if (mode.reportAny && maxFormula.isConstant()) {
                                        elem.logError("Constant 'max' in {" + varName
                                                      + "} could be specified in attribute? (20328)");
                                    }
                                    max = new NumberOrFormula(maxFormula);
                                }
                            } else {
                                if (mode.reportAny) {
                                    elem.logError("Multiple 'max' values in {" + varName + "} (20330)");
                                }
                                break label;
                            }
                        }
                        case EXCLUDE -> {
                            final Formula exclude = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (exclude == null) {
                                if (mode.reportAny) {
                                    elem.logError("Invalid 'exclude' formula in {" + varName + "}: "
                                                  + nonempty.print(0) + " (20331)");
                                }
                                valid = false;
                            } else {
                                excludes.add(exclude);
                            }
                        }
                        case null, default -> {
                            if (mode.reportAny) {
                                elem.logError("Unsupported '" + tag + "' element in in {" + varName + "} (20332)");
                            }
                            valid = false;
                        }
                    }
                }
            }

            String formatStr = elem.getStringAttr(FORMAT);
            if (formatStr == null) {
                formatStr = elem.getStringAttr(DECIMAL_FORMAT);
                if (mode.reportDeprecated && formatStr != null) {
                    elem.logError("'decimal-format' is deprecated, use 'format' instead. (20333)");
                }
            }

            if (mode.reportAny && formatStr != null && formatStr.startsWith("##")) {
                elem.logError("Perhaps format string in integer variable '" + varName
                              + "' could be simplified? (20334)");
            }

            if (valid) {
                result = new VariableRandomInteger(varName);
                result.setValue(value);
                result.setFormatPattern(formatStr);
                result.setMin(min);
                result.setMax(max);
                result.setExcludes(excludes);
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableRandomReal} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-rea;' value='...' min='...' max='...' format='...'/&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomReal extractVariableRandomReal(final EmptyElement elem, final String varName,
                                                                final EParserMode mode) {

        boolean valid = true;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || MIN.equals(attrName) || MAX.equals(attrName)
                    || EXCLUDE.equals(attrName) || FORMAT.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20340)";
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);
        Number value = null;

        if (valueStr != null) {
            try {
                value = NumberParser.parse(valueStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid real value: '" + valueStr + "' in {" + varName + "} (20341)");
                }
                valid = false;
            }
        }

        VariableRandomReal result = null;

        final String minStr = elem.getStringAttr(MIN);
        final String maxStr = elem.getStringAttr(MAX);

        if (minStr == null || maxStr == null) {
            if (mode.reportAny) {
                elem.logError("Missing required min/max value in random real var {" + varName + "} (20342)");
            }
        } else {
            NumberOrFormula min = null;
            try {
                final Number parsedMin = NumberParser.parse(minStr);
                min = new NumberOrFormula(parsedMin);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "} (20343)");
                }
                valid = false;
            }

            NumberOrFormula max = null;
            try {
                final Number parsedMax = NumberParser.parse(maxStr);
                max = new NumberOrFormula(parsedMax);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "} (20344)");
                }
                valid = false;
            }

            String formatStr = elem.getStringAttr(FORMAT);
            if (formatStr == null) {
                formatStr = elem.getStringAttr(DECIMAL_FORMAT);
                if (mode.reportDeprecated && formatStr != null) {
                    elem.logError("'decimal-format' is deprecated, use 'format' instead. (20345)");
                }
            }

            if (mode.reportAny && formatStr != null && formatStr.startsWith("##")) {
                elem.logError("Perhaps format string in real variable '" + varName + "' could be simplified? (20346)");
            }

            if (valid) {

                result = new VariableRandomReal(varName);
                result.setValue(value);
                result.setFormatPattern(formatStr);
                result.setMin(min);
                result.setMax(max);
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableRandomReal} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-real' format='...' value='...'&gt;
     *   &lt;min&gt; ... formula contents ... &lt;/min&gt;
     *   &lt;max&gt; ... formula contents ... &lt;/max&gt;
     * &lt;/var&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param varName     the variable name
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomReal extractVariableRandomReal(final EvalContext evalContext,
                                                                final NonemptyElement elem, final String varName,
                                                                final EParserMode mode) {

        boolean valid = true;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || MIN.equals(attrName) || MAX.equals(attrName)
                    || EXCLUDE.equals(attrName) || FORMAT.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20350)";
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);
        Double value = null;

        if (valueStr != null) {
            try {
                value = Double.valueOf(valueStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid real value: '" + valueStr + "' in {" + varName + "} (20351)");
                }
                valid = false;
            }
        }

        NumberOrFormula min = null;
        NumberOrFormula max = null;

        final String minStr = elem.getStringAttr(MIN);
        if (minStr != null) {
            try {
                final Number parsedMin = NumberParser.parse(minStr);
                min = new NumberOrFormula(parsedMin);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    if (mode.reportAny) {
                        elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "} (20352)");
                    }
                }
                valid = false;
            }
        }

        final String maxStr = elem.getStringAttr(MAX);
        if (maxStr != null) {
            try {
                final Number parsedMax = NumberParser.parse(maxStr);
                max = new NumberOrFormula(parsedMax);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "} (20353)");
                }
                valid = false;
            }
        }

        VariableRandomReal result = null;

        if (valid) {
            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    if (MIN.equals(tag)) {
                        if (min == null) {
                            final Formula minFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (minFormula == null) {
                                if (mode.reportAny) {
                                    elem.logError("Invalid 'min' formula in {" + varName + "}: " + nonempty.print(0)
                                                  + " (20354)");
                                }
                                valid = false;
                            } else {
                                if (mode.reportAny && minFormula.isConstant()) {
                                    elem.logError("Constant 'min' in {" + varName
                                                  + "} could be specified in attribute? (20355)");
                                }
                                min = new NumberOrFormula(minFormula);
                            }
                        } else {
                            if (mode.reportAny) {
                                elem.logError("Multiple 'min' values in {" + varName + "} (20356)");
                            }
                            break;
                        }
                    } else if (MAX.equals(tag)) {
                        if (max == null) {
                            final Formula maxFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (maxFormula == null) {
                                if (mode.reportAny) {
                                    elem.logError("Invalid 'max' formula in {" + varName + "}: " + nonempty.print(0)
                                                  + " (20357)");
                                }
                                valid = false;
                            } else {
                                if (mode.reportAny && maxFormula.isConstant()) {
                                    elem.logError("Constant 'max' in {" + varName
                                                  + "} could be specified in attribute? (20358)");
                                }
                                max = new NumberOrFormula(maxFormula);
                            }
                        } else {
                            if (mode.reportAny) {
                                elem.logError("Multiple 'max' values in {" + varName + "} (20359)");
                            }
                            break;
                        }
                    } else {
                        if (mode.reportAny) {
                            elem.logError("Unsupported '" + tag + "' element in {" + varName + "} (20360)");
                        }
                        valid = false;
                    }
                }
            }

            String formatStr = elem.getStringAttr(FORMAT);
            if (formatStr == null) {
                formatStr = elem.getStringAttr(DECIMAL_FORMAT);
                if (mode.reportDeprecated && formatStr != null) {
                    elem.logError("'decimal-format' is deprecated, use 'format' instead. (20361)");
                }
            }

            if (mode.reportAny && formatStr != null && formatStr.startsWith("##")) {
                elem.logError("Perhaps format string in real variable '" + varName + "' could be simplified? (20362)");
            }

            if (valid) {
                result = new VariableRandomReal(varName);
                result.setValue(value);
                result.setFormatPattern(formatStr);
                result.setMin(min);
                result.setMax(max);
            }
        }

        return result;

    }

    /**
     * Extracts a {@code VariableRandomPermutation} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-int' value='...' min='...' max='...' format='...'/&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomPermutation extractVariableRandomPermutation(final EmptyElement elem,
                                                                              final String varName,
                                                                              final EParserMode mode) {

        boolean valid = true;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || MIN.equals(attrName) || MAX.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20370)";
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);
        IntegerVectorValue value = null;

        if (valueStr != null) {
            try {
                value = IntegerVectorValue.parse(valueStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid permutation value: '" + valueStr + "' in {" + varName + "} (20371)");
                }
                valid = false;
            }
        }

        VariableRandomPermutation result = null;

        final String minStr = elem.getStringAttr(MIN);
        final String maxStr = elem.getStringAttr(MAX);

        if (minStr == null || maxStr == null) {
            if (mode.reportAny) {
                elem.logError("Missing required min/max value in random permutation var {" + varName + "} (20372)");
            }
        } else {
            NumberOrFormula min = null;
            try {
                final Long parsedMin = Long.valueOf(minStr);
                min = new NumberOrFormula(parsedMin);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "} (20373)");
                }
                valid = false;
            }

            NumberOrFormula max = null;
            try {
                final Long parsedMax = Long.valueOf(maxStr);
                max = new NumberOrFormula(parsedMax);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "} (20374)");
                }
                valid = false;
            }

            if (valid) {
                result = new VariableRandomPermutation(varName);
                result.setValue(value);
                result.setMin(min);
                result.setMax(max);
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableRandomPermutation} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-permutation' min='...' max='...' value='...'/&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param varName     the variable name
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomPermutation extractVariableRandomPermutation(final EvalContext evalContext,
                                                                              final NonemptyElement elem,
                                                                              final String varName,
                                                                              final EParserMode mode) {

        boolean valid = true;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || MIN.equals(attrName) || MAX.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20380)";
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);
        IntegerVectorValue value = null;

        if (valueStr != null) {
            try {
                value = IntegerVectorValue.parse(valueStr);
            } catch (final IllegalArgumentException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid permutation value: '" + valueStr + "' in {" + varName + "} (20381)");
                }
                valid = false;
            }
        }

        NumberOrFormula min = null;
        NumberOrFormula max = null;

        final String minStr = elem.getStringAttr(MIN);
        if (minStr != null) {
            try {
                final Long parsedMin = Long.valueOf(minStr);
                min = new NumberOrFormula(parsedMin);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "} (20382)");
                }
                valid = false;
            }
        }

        final String maxStr = elem.getStringAttr(MAX);
        if (maxStr != null) {
            try {
                final Long parsedMax = Long.valueOf(maxStr);
                max = new NumberOrFormula(parsedMax);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "} (20383)");
                }
                valid = false;
            }
        }

        VariableRandomPermutation result = null;

        if (valid) {
            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    if (MIN.equals(tag)) {
                        if (min == null) {
                            final Formula minFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (minFormula == null) {
                                if (mode.reportAny) {
                                    elem.logError("Invalid 'min' formula in {" + varName + "}: " + nonempty.print(0)
                                                  + " (20384)");
                                }
                                valid = false;
                            } else {
                                if (mode.reportAny && minFormula.isConstant()) {
                                    elem.logError("Constant 'min' in {" + varName
                                                  + "} could be specified in attribute? (20385)");
                                }
                                min = new NumberOrFormula(minFormula);
                            }
                        } else {
                            if (mode.reportAny) {
                                elem.logError("Multiple 'min' values in {" + varName + "} (20386)");
                            }
                            break;
                        }
                    } else if (MAX.equals(tag)) {
                        if (max == null) {
                            final Formula maxFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (maxFormula == null) {
                                if (mode.reportAny) {
                                    elem.logError("Invalid 'max' formula in {" + varName + "}: " + nonempty.print(0)
                                                  + " (20387)");
                                }
                                valid = false;
                            } else {
                                if (mode.reportAny && maxFormula.isConstant()) {
                                    elem.logError("Constant 'max' in {" + varName
                                                  + "} could be specified in attribute? (20388)");
                                }
                                max = new NumberOrFormula(maxFormula);
                            }
                        } else {
                            if (mode.reportAny) {
                                elem.logError("Multiple 'max' values in {" + varName + "} (20389)");
                            }
                            break;
                        }
                    } else {
                        if (mode.reportAny) {
                            elem.logError("Unsupported '" + tag + "' element in in {" + varName + "} (20390)");
                        }
                        valid = false;
                    }
                }
            }

            if (valid) {
                result = new VariableRandomPermutation(varName);
                result.setValue(value);
                result.setMin(min);
                result.setMax(max);
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableRandomChoice} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-choice' format='...' long='..' double='..' boolean='..'
     *   value-type='...'&gt;
     *   &lt;span&gt; ... span value XML ... &lt;/span&gt;
     *   &lt;choose-from&gt; ... formula contents ... &lt;/choose-from&gt;
     *   &lt;choose-from&gt; ... formula contents ... &lt;/choose-from&gt;
     *   ...
     *   &lt;exclude&gt; ... formula contents ... &lt;/exclude&gt;
     *   &lt;exclude&gt; ... formula contents ... &lt;/exclude&gt;
     *   ...
     * &lt;/var&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param varName     the variable name
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomChoice extractVariableRandomChoice(final EvalContext evalContext,
                                                                    final NonemptyElement elem, final String varName,
                                                                    final EParserMode mode) {

        boolean valid = true;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || FORMAT.equals(attrName)
                    || VALUE_TYPE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20400)";
                elem.logError(msg);
            }
        }

        Object value = null;

        EType type = EType.ERROR;
        final String valueTypeStr = elem.getStringAttr(VALUE_TYPE);
        if (valueTypeStr != null) {
            type = EType.forLabel(valueTypeStr);
        }

        final String longStr = elem.getStringAttr(LONG);
        if (longStr == null) {
            final String doubleStr = elem.getStringAttr(DOUBLE);
            if (doubleStr == null) {
                final String booleanStr = elem.getStringAttr(BOOLEAN);
                if (booleanStr == null) {
                    final String irrationalStr = elem.getStringAttr(IRRATIONAL);
                    if (irrationalStr == null) {
                        final String stringStr = elem.getStringAttr(STRING);
                        if (stringStr == null) {

                            final String intVectorStr = elem.getStringAttr(INT_VECTOR);
                            if (intVectorStr != null) {
                                if (type == EType.ERROR) {
                                    type = EType.INTEGER_VECTOR;
                                }
                                try {
                                    value = IntegerVectorValue.parse(intVectorStr);
                                } catch (final NumberFormatException ex) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid integer vector value: '" + intVectorStr + "' in {"
                                                      + varName + "} (20401)");
                                    }
                                    valid = false;
                                }
                            }
                        } else {
                            if (type == EType.ERROR) {
                                type = EType.STRING;
                            }
                            value = stringStr;
                        }
                    } else {
                        if (type == EType.ERROR || type == EType.INTEGER) {
                            type = EType.REAL;
                        }
                        try {
                            value = NumberParser.parse(irrationalStr);
                        } catch (final NumberFormatException ex) {
                            if (mode.reportAny) {
                                elem.logError("Invalid irrational value: '" + irrationalStr + "' in {" + varName
                                              + "} (20402)");
                            }
                            valid = false;
                        }
                    }
                } else {
                    if (type == EType.ERROR) {
                        type = EType.BOOLEAN;
                    }
                    if (TRUE_STRING.equalsIgnoreCase(booleanStr)) {
                        value = Boolean.TRUE;
                    } else if (FALSE_STRING.equalsIgnoreCase(booleanStr)) {
                        value = Boolean.FALSE;
                    } else {
                        if (mode.reportAny) {
                            elem.logError("Invalid boolean value: '" + booleanStr + "' in {" + varName + "} (20403)");
                        }
                        valid = false;
                    }
                }
            } else {
                if (type == EType.ERROR || type == EType.INTEGER) {
                    type = EType.REAL;
                }
                try {
                    value = Double.valueOf(doubleStr);
                } catch (final NumberFormatException ex) {
                    if (mode.reportAny) {
                        elem.logError("Invalid real value: '" + doubleStr + "' in {" + varName + "} (20404)");
                    }
                    valid = false;
                }
            }
        } else {
            if (type == EType.ERROR) {
                type = EType.INTEGER;
            }
            try {
                value = Long.valueOf(longStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid long value: '" + longStr + "' in {" + varName + "} (20405)");
                }
                valid = false;
            }
        }

        VariableRandomChoice result = null;

        if (valid) {
            final Collection<Formula> excludes = new ArrayList<>(5);
            final Collection<Formula> choices = new ArrayList<>(5);
            DocSimpleSpan span = null;

            label:
            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    switch (tag) {
                        case EXCLUDE -> {
                            final Formula exclude = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (exclude == null) {
                                if (mode.reportAny) {
                                    elem.logError("Invalid 'exclude' formula in {" + varName + "}: "
                                                  + nonempty.print(0) + " (20406)");
                                }
                                valid = false;
                            } else {
                                excludes.add(exclude);
                            }
                        }
                        case CHOOSE_FROM -> {
                            final Formula choice = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (choice == null) {
                                if (mode.reportAny) {
                                    elem.logError("Invalid 'choose-from' formula in {" + varName + "}: "
                                                  + nonempty.print(0) + " (20407)");
                                }
                                valid = false;
                            } else {
                                choices.add(choice);
                                final EType choiceType = choice.getType(evalContext);
                                if (type == null && choiceType != null) {
                                    type = choiceType;
                                } else if (choiceType != type) {
                                    if ((type == EType.INTEGER && choiceType == EType.REAL)
                                        || (type == EType.REAL && choiceType == EType.INTEGER)) {
                                        type = EType.REAL;
                                    } else {
                                        if (mode.reportAny) {
                                            elem.logError("Inconsistent types in 'choose-from' formulas in {"
                                                          + varName + "} (20408)");
                                        }
                                        valid = false;
                                    }
                                }
                            }
                        }
                        case SPAN -> {
                            if (type == EType.ERROR) {
                                type = EType.SPAN;
                            }
                            if (span == null) {
                                span = DocFactory.parseSpan(evalContext, nonempty, mode);
                                if (span == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid <span> content in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20409)");
                                    }
                                    valid = false;
                                }
                            } else {
                                if (mode.reportAny) {
                                    elem.logError("Multiple 'span' values not allowed in {" + varName + "} (20410)");
                                }
                                break label;
                            }
                        }
                        case null, default -> {
                            if (mode.reportAny) {
                                elem.logError("Unsupported '" + tag + "' element in {" + varName + "} (20411)");
                            }
                            valid = false;
                        }
                    }
                }
            }

            if (value != null && span != null) {
                if (mode.reportAny) {
                    elem.logError("Cannot have both attribute-specified value and span value in {" + varName
                                  + "} (20412)");
                }
                valid = false;
            }

            String formatStr = elem.getStringAttr(FORMAT);
            if (formatStr == null) {
                formatStr = elem.getStringAttr(DECIMAL_FORMAT);
                if (mode.reportDeprecated && formatStr != null) {
                    elem.logError("'decimal-format' is deprecated, use 'format' instead. (20413)");
                }
            }

            if (mode.reportAny && formatStr != null && formatStr.startsWith("##")) {
                elem.logError("Perhaps format string in choice variable '" + varName
                              + "' could be simplified? (20414)");
            }

            if (valid) {
                result = new VariableRandomChoice(varName, type);
                result.setValue(value == null ? span : value);
                result.setFormatPattern(formatStr);
                result.setChooseFromList(choices);
                result.setExcludes(excludes);
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableRandomSimpleAngle} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-simple-angle' value='...' min='...' max='...'
     *         max-denom='...'/&gt;
     * </pre>
     *
     * @param elem    the element
     * @param varName the variable name
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomSimpleAngle extractVariableRandomSimpleAngle(final EmptyElement elem,
                                                                              final String varName,
                                                                              final EParserMode mode) {

        boolean valid = true;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || MIN.equals(attrName) || MAX.equals(attrName)
                    || MAX_DENOM.equals(attrName) || EXCLUDE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20420)";
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);
        Long value = null;

        if (valueStr != null) {
            try {
                value = Long.valueOf(valueStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "} (20421)");
                }
                valid = false;
            }
        }

        VariableRandomSimpleAngle result = null;

        final String minStr = elem.getStringAttr(MIN);
        final String maxStr = elem.getStringAttr(MAX);

        if (minStr == null || maxStr == null) {
            if (mode.reportAny) {
                elem.logError("Missing required min/max value in random integer var {" + varName + "} (20422)");
            }
        } else {
            NumberOrFormula min = null;
            try {
                final Long parsedMin = Long.valueOf(minStr);
                min = new NumberOrFormula(parsedMin);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "} (20423)");
                }
                valid = false;
            }

            NumberOrFormula max = null;
            try {
                final Long parsedMax = Long.valueOf(maxStr);
                max = new NumberOrFormula(parsedMax);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "} (20424)");
                }
                valid = false;
            }

            final String maxDenomStr = elem.getStringAttr(MAX_DENOM);
            NumberOrFormula maxDenom = null;
            try {
                final Long parsedMaxDenom = Long.valueOf(maxDenomStr);
                maxDenom = new NumberOrFormula(parsedMaxDenom);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "} (20425)");
                }
                valid = false;
            }

            if (valid) {
                result = new VariableRandomSimpleAngle(varName);
                result.setValue(value);
                result.setMin(min);
                result.setMax(max);
                result.setMaxDenom(maxDenom);
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableRandomSimpleAngle} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='random-simple-angle' value='...'&gt;
     *   &lt;min&gt; ... formula contents ... &lt;/min&gt;
     *   &lt;max&gt; ... formula contents ... &lt;/max&gt;
     *   &lt;max-denom&gt; ... formula contents ... &lt;/max-denom&gt;
     *   &lt;exclude&gt; ... formula contents ... &lt;/exclude&gt;
     *   &lt;exclude&gt; ... formula contents ... &lt;/exclude&gt;
     *   ...
     * &lt;/var&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param varName     the variable name
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomSimpleAngle extractVariableRandomSimpleAngle(final EvalContext evalContext,
                                                                              final NonemptyElement elem,
                                                                              final String varName,
                                                                              final EParserMode mode) {

        boolean valid = true;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || MIN.equals(attrName) || MAX.equals(attrName)
                    || MAX_DENOM.equals(attrName) || EXCLUDE.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20430)";
                elem.logError(msg);
            }
        }

        final String valueStr = elem.getStringAttr(VALUE);
        Long value = null;

        if (valueStr != null) {
            try {
                value = Long.valueOf(valueStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "} (20431)");
                }
                valid = false;
            }
        }

        NumberOrFormula min = null;
        NumberOrFormula max = null;
        NumberOrFormula maxDenom = null;

        final String minStr = elem.getStringAttr(MIN);
        if (minStr != null) {
            try {
                final Long parsedMin = Long.valueOf(minStr);
                min = new NumberOrFormula(parsedMin);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "} (20432)");
                }
                valid = false;
            }
        }

        final String maxStr = elem.getStringAttr(MAX);
        if (maxStr != null) {
            try {
                final Long parsedMax = Long.valueOf(maxStr);
                max = new NumberOrFormula(parsedMax);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "} (20433)");
                }
                valid = false;
            }
        }

        final String maxDenomStr = elem.getStringAttr(MAX_DENOM);
        if (maxDenomStr != null) {
            try {
                final Long parsedMaxDenom = Long.valueOf(maxDenomStr);
                maxDenom = new NumberOrFormula(parsedMaxDenom);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max-denom value: '" + maxDenomStr + "' in {" + varName + "} (20434)");
                }
                valid = false;
            }
        }

        VariableRandomSimpleAngle result = null;

        if (valid) {
            final Collection<Formula> excludes = new ArrayList<>(5);

            label:
            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    switch (tag) {
                        case MIN -> {
                            if (min == null) {
                                final Formula minFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty,
                                        mode);
                                if (minFormula == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid 'min' formula in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20435)");
                                    }
                                    valid = false;
                                } else {
                                    if (mode.reportAny && minFormula.isConstant()) {
                                        elem.logError("Constant 'min' in {" + varName
                                                      + "} could be specified in attribute? (20436)");
                                    }
                                    min = new NumberOrFormula(minFormula);
                                }
                            } else {
                                if (mode.reportAny) {
                                    elem.logError("Multiple 'min' values in {" + varName + "} (20437)");
                                }
                                break label;
                            }
                        }
                        case MAX -> {
                            if (max == null) {
                                final Formula maxFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty,
                                        mode);
                                if (maxFormula == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid 'max' formula in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20438)");
                                    }
                                    valid = false;
                                } else {
                                    if (mode.reportAny && maxFormula.isConstant()) {
                                        elem.logError("Constant 'max' in {" + varName
                                                      + "} could be specified in attribute? (20439)");
                                    }
                                    max = new NumberOrFormula(maxFormula);
                                }
                            } else {
                                elem.logError("Multiple 'max' values in {" + varName + "} (20440)");
                                break label;
                            }
                        }
                        case MAX_DENOM -> {
                            if (maxDenom == null) {
                                final Formula maxDenomFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty,
                                        mode);
                                if (maxDenomFormula == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid 'max-denom' formula in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20441)");
                                    }
                                    valid = false;
                                } else {
                                    if (mode.reportAny && maxDenomFormula.isConstant()) {
                                        elem.logError("Constant 'max-denom' in {" + varName
                                                      + "} could be specified in attribute? (20442)");
                                    }
                                    maxDenom = new NumberOrFormula(maxDenomFormula);
                                }
                            } else {
                                if (mode.reportAny) {
                                    elem.logError("Multiple 'max-denom' values in {" + varName + "} (20443)");
                                }
                                break label;
                            }
                        }
                        case EXCLUDE -> {
                            final Formula exclude = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (exclude == null) {
                                if (mode.reportAny) {
                                    elem.logError("Invalid 'exclude' formula in {" + varName + "}: "
                                                  + nonempty.print(0) + " (20444)");
                                }
                                valid = false;
                            } else {
                                excludes.add(exclude);
                            }
                        }
                        case null, default -> {
                            if (mode.reportAny) {
                                elem.logError("Unsupported '" + tag + "' element in in {" + varName + "} (20445)");
                            }
                            valid = false;
                        }
                    }
                }
            }

            if (valid) {
                result = new VariableRandomSimpleAngle(varName);
                result.setValue(value);
                result.setMin(min);
                result.setMax(max);
                result.setMaxDenom(maxDenom);
                result.setExcludes(excludes);
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableDerived} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='derived' format='...' long='...' double='...' boolean='...'
     *   value-type='...'&gt;
     *   &lt;span&gt; ... span value XML ... &lt;/span&gt;
     *   &lt;min&gt; ... formula contents ... &lt;/min&gt;
     *   &lt;max&gt; ... formula contents ... &lt;/max&gt;
     *   &lt;formula&gt; ... formula contents ... &lt;/formula&gt;
     *   &lt;exclude&gt; ... formula contents ... &lt;/exclude&gt;
     *   &lt;exclude&gt; ... formula contents ... &lt;/exclude&gt;
     *   ...
     * &lt;/var&gt;
     * </pre>
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param varName     the variable name
     * @param mode        the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableDerived extractVariableDerived(final EvalContext evalContext, final NonemptyElement elem,
                                                          final String varName, final EParserMode mode) {

        boolean valid = true;

        if (mode.reportAny) {
            for (final String attrName : elem.attributeNames()) {
                if (NAME.equals(attrName) || TYPE.equals(attrName) || VALUE_TYPE.equals(attrName) || MIN.equals(
                        attrName)
                    || MAX.equals(attrName) || EXCLUDE.equals(attrName) || FORMAT.equals(attrName)) {
                    continue;
                }
                final String msg = Res.fmt(Res.UNEXPECTED_ATTR, attrName) + " (20450)";
                elem.logError(msg);
            }
        }

        Object value = null;

        EType type = EType.ERROR;
        final String valueTypeStr = elem.getStringAttr(VALUE_TYPE);
        if (valueTypeStr == null) {
            if (mode.reportAny) {
                elem.logError("Derived variable {" + varName + "} does not specify value type (20451)");
            }
        } else {
            type = EType.forLabel(valueTypeStr);
            if (type == null) {
                if (mode.reportAny) {
                    elem.logError("Invalid value-type: '" + valueTypeStr + "' in {" + varName + "} (20452)");
                }
                valid = false;
            }
        }

        final String longStr = elem.getStringAttr(LONG);
        if (longStr == null) {
            final String doubleStr = elem.getStringAttr(DOUBLE);
            if (doubleStr == null) {
                final String booleanStr = elem.getStringAttr(BOOLEAN);
                if (booleanStr == null) {
                    final String irrationalStr = elem.getStringAttr(IRRATIONAL);
                    if (irrationalStr == null) {
                        final String stringStr = elem.getStringAttr(STRING);
                        if (stringStr == null) {
                            final String intVectorStr = elem.getStringAttr(INT_VECTOR);
                            if (intVectorStr != null) {
                                if (type == EType.ERROR) {
                                    type = EType.INTEGER_VECTOR;
                                }
                                try {
                                    value = IntegerVectorValue.parse(intVectorStr);
                                } catch (final NumberFormatException ex) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid integer vector value: '" + intVectorStr + "' in {"
                                                      + varName + "} (20453)");
                                    }
                                    valid = false;
                                }
                            }
                        } else {
                            if (type == EType.ERROR) {
                                type = EType.STRING;
                            }
                            value = stringStr;
                        }
                    } else {
                        if (type == EType.ERROR || type == EType.INTEGER) {
                            type = EType.REAL;
                        }
                        try {
                            value = NumberParser.parse(irrationalStr);
                        } catch (final NumberFormatException ex) {
                            if (mode.reportAny) {
                                elem.logError("Invalid irrational value: '" + irrationalStr + "' in {" + varName
                                              + "} (20455)");
                            }
                            valid = false;
                        }
                    }
                } else {
                    if (type == EType.ERROR) {
                        type = EType.BOOLEAN;
                    }
                    if (TRUE_STRING.equalsIgnoreCase(booleanStr)) {
                        value = Boolean.TRUE;
                    } else if (FALSE_STRING.equalsIgnoreCase(booleanStr)) {
                        value = Boolean.FALSE;
                    } else {
                        if (mode.reportAny) {
                            elem.logError("Invalid boolean value: '" + booleanStr + "' in {" + varName + "} (20455)");
                        }
                        valid = false;
                    }
                }
            } else {
                if (type == EType.ERROR || type == EType.INTEGER) {
                    type = EType.REAL;
                }
                try {
                    value = Double.valueOf(doubleStr);
                } catch (final NumberFormatException ex) {
                    if (mode.reportAny) {
                        elem.logError("Invalid real value: '" + doubleStr + "' in {" + varName + "} (20456)");
                    }
                    valid = false;
                }
            }
        } else {
            if (type == EType.ERROR) {
                type = EType.INTEGER;
            }
            try {
                value = Long.valueOf(longStr);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid long value: '" + longStr + "' in {" + varName + "} (20457)");
                }
                valid = false;
            }
        }

        VariableDerived result = null;

        NumberOrFormula min = null;
        NumberOrFormula max = null;

        final String minStr = elem.getStringAttr(MIN);
        if (minStr != null) {
            try {
                final Number parsedMin = NumberParser.parse(minStr);
                min = new NumberOrFormula(parsedMin);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "} (20458)");
                }
                valid = false;
            }
        }

        final String maxStr = elem.getStringAttr(MAX);
        if (maxStr != null) {
            try {
                final Number parsedMax = NumberParser.parse(maxStr);
                max = new NumberOrFormula(parsedMax);
            } catch (final NumberFormatException ex) {
                if (mode.reportAny) {
                    elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "} (20459)");
                }
                valid = false;
            }
        }

        if (valid) {
            final Collection<Formula> excludes = new ArrayList<>(5);
            DocSimpleSpan span = null;
            Formula formula = null;

            label:
            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    switch (tag) {
                        case MIN -> {
                            if (min == null) {
                                final Formula minFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty,
                                        mode);
                                if (minFormula == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid 'min' formula in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20460)");
                                    }
                                    valid = false;
                                } else {
                                    if (mode.reportAny && minFormula.isConstant()) {
                                        elem.logError("Constant 'min' in {" + varName
                                                      + "} could be specified in attribute? (20461)");
                                    }
                                    min = new NumberOrFormula(minFormula);
                                }
                            } else {
                                if (mode.reportAny) {
                                    elem.logError("Multiple 'min' values in {" + varName + "} (20462)");
                                }
                                break label;
                            }
                        }
                        case MAX -> {
                            if (max == null) {
                                final Formula maxFormula = XmlFormulaFactory.extractFormula(evalContext, nonempty,
                                        mode);
                                if (maxFormula == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid 'max' formula in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20463)");
                                    }
                                    valid = false;
                                } else {
                                    if (mode.reportAny && maxFormula.isConstant()) {
                                        elem.logError("Constant 'max' in {" + varName
                                                      + "} could be specified in attribute? (20464)");
                                    }
                                    max = new NumberOrFormula(maxFormula);
                                }
                            } else {
                                if (mode.reportAny) {
                                    elem.logError("Multiple 'max' values in {" + varName + "} (20465)");
                                }
                                break label;
                            }
                        }
                        case EXCLUDE -> {
                            final Formula exclude = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (exclude == null) {
                                if (mode.reportAny) {
                                    elem.logError("Invalid 'exclude' formula in {" + varName + "}: "
                                                  + nonempty.print(0) + " (20466)");
                                }
                                valid = false;
                            } else {
                                excludes.add(exclude);
                            }
                        }
                        case SPAN -> {
                            if (type == EType.ERROR) {
                                type = EType.SPAN;
                            }
                            if (span == null) {
                                span = DocFactory.parseSpan(evalContext, nonempty, mode);
                                if (span == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid <span> content in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20467)");
                                    }
                                    valid = false;
                                }
                            } else {
                                if (mode.reportAny) {
                                    elem.logError("Multiple 'span' values in {" + varName + "} (20468)");
                                }
                                break label;
                            }
                        }
                        case EXPR -> {
                            if (formula == null) {
                                formula = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                                if (formula == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid 'expr' formula in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20469)");
                                    }
                                    valid = false;
                                }
                            } else {
                                if (mode.reportAny) {
                                    elem.logError("Multiple 'expr/formula' values in {" + varName + "} (20470)");
                                }
                                break label;
                            }
                        }
                        case FORMULA -> {
                            if (mode.reportDeprecated) {
                                elem.logError("&lt;formula&gt; is deprecated, use &lt;expr&gt; instead. (20471)");
                            }
                            if (formula == null) {
                                formula = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                                if (formula == null) {
                                    if (mode.reportAny) {
                                        elem.logError("Invalid 'formula' formula in {" + varName + "}: "
                                                      + nonempty.print(0) + " (20472)");
                                    }
                                    valid = false;
                                }
                            } else {
                                if (mode.reportAny) {
                                    elem.logError("Multiple 'expr/formula' values in {" + varName + "} (20473)");
                                }
                                break label;
                            }
                        }
                        case null, default -> {
                            if (mode.reportAny) {
                                elem.logError("Unsupported '" + tag + "' element in {" + varName + "} (20474)");
                            }
                            valid = false;
                        }
                    }
                }
            }

            if (valid && formula == null) {
                if (mode.reportAny) {
                    elem.logError("Derived value does not include formula. (20475)");
                }
                valid = false;
            } else if (value != null && span != null) {
                if (mode.reportAny) {
                    elem.logError("Cannot have both attribute-specified value and span value. (20476)");
                }
                valid = false;
            }

            String formatStr = elem.getStringAttr(FORMAT);
            if (formatStr == null) {
                formatStr = elem.getStringAttr(DECIMAL_FORMAT);
                if (mode.reportDeprecated && formatStr != null) {
                    elem.logError("'decimal-format' is deprecated, use 'format' instead. (20477)");
                }
            }

            if (mode.reportAny && formatStr != null && formatStr.startsWith("##")) {
                elem.logError("Perhaps format string in derived variable '" + varName
                              + "' could be simplified? (20478)");
            }

            if (valid) {
                result = new VariableDerived(varName, type);
                result.setValue(value == null ? span : value);
                result.setFormatPattern(formatStr);
                result.setMin(min);
                result.setMax(max);
                result.setFormula(formula);
                result.setExcludes(excludes);
            }
        }

        return result;
    }

    /**
     * Returns a {@code Boolean} whose value is found by parsing a {@code String}.
     *
     * @param str the string value (
     * @return the parsed {@code Boolean}
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    public static Boolean parseBooleanValue(final String str) throws IllegalArgumentException {

        final int len = str.length();
        final Boolean result;

        if (len == 1) {
            final char chr = str.charAt(0);
            if (TRUE_CHARS.indexOf(chr) == -1) {
                if (FALSE_CHARS.indexOf(chr) == -1) {
                    throw new IllegalArgumentException("Invalid boolean format '" + str + "' (20480)");
                }
                result = Boolean.FALSE;
            } else {
                result = Boolean.TRUE;
            }
        } else {
            final String lower = str.toLowerCase(Locale.US);
            if (TRUE_STRING.equals(lower)) {
                result = Boolean.TRUE;
            } else if (FALSE_STRING.equals(lower)) {
                result = Boolean.FALSE;
            } else {
                throw new IllegalArgumentException("Invalid boolean format '" + str + "' (20481)");
            }
        }

        return result;
    }

    /**
     * Extracts a {@code VariableInputInteger} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='input-int'/&gt;
     * </pre>
     *
     * @param varName the variable name
     * @return the parsed variable on success; null on failure
     */
    private static VariableInputInteger extractVariableInputInteger(final String varName) {

        return new VariableInputInteger(varName);
    }

    /**
     * Extracts a {@code VariableInputIntegerVector} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='input-int-vector'/&gt;
     * </pre>
     *
     * @param varName the variable name
     * @return the parsed variable on success; null on failure
     */
    private static VariableInputIntegerVector
    extractVariableInputIntegerVector(final String varName) {

        return new VariableInputIntegerVector(varName);
    }

    /**
     * Extracts a {@code VariableInputReal} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='input-real'/&gt;
     * </pre>
     *
     * @param varName the variable name
     * @return the parsed variable on success; null on failure
     */
    private static VariableInputReal extractVariableInputReal(final String varName) {

        return new VariableInputReal(varName);
    }

    /**
     * Extracts a {@code VariableInputString} from a "var" element.
     *
     * <pre>
     * &lt;var name='...' type='input-string'/&gt;
     * </pre>
     *
     * @param varName the variable name
     * @return the parsed variable on success; null on failure
     */
    private static VariableInputString extractVariableInputString(final String varName) {

        return new VariableInputString(varName);
    }
}
