package dev.mathops.assessment.variable;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.EType;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.formula.IntegerVectorValue;
import dev.mathops.assessment.formula.XmlFormulaFactory;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.parser.xml.CData;
import dev.mathops.core.parser.xml.Comment;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.core.parser.xml.IElement;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;

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
    public static boolean parseVars(final EvalContext evalContext, final NonemptyElement elem,
                                    final EParserMode mode) {

        // "<param .../>" is old style tag with formulas in attributes
        // "<var> ... </var>" is new style tag with formulas as child objects
        // We do one pass extracting "param" elements and a second pass for "var" elements.

        // If there are one or more comments that directly precedes a variable, we associate those
        // comments with the variable.

        // Log.info("Parsing Vars in a problem...");

        boolean result = true;
        final Collection<Comment> comments = new ArrayList<>(3);

        for (final INode child : elem.getChildrenAsList()) {
            if (child instanceof final IElement childElem) {

                final String tag = childElem.getTagName();

                if (child instanceof final EmptyElement empty) {
                    if ("param".equals(tag)) {
                        result = result && processEmptyParamElement(evalContext, empty, comments, mode);
                    } else if ("var".equals(tag)) {
                        result = result && processEmptyVarElement(evalContext, empty, comments);
                    }
                } else if (child instanceof final NonemptyElement nonempty) {
                    if ("param".equals(tag)) {
                        result = result && processNonemptyParamElement(evalContext, nonempty, comments, mode);
                    } else if ("var".equals(tag)) {
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
    private static boolean processEmptyParamElement(final EvalContext evalContext,
                                                    final EmptyElement elem, final Iterable<Comment> comments,
                                                    final EParserMode mode) {

        boolean valid = true;

        if (mode == EParserMode.NORMAL) {
            elem.logError("Deprecated '<param>' element");
        }

        final String parameterName = elem.getStringAttr("name");
        final String parameterType = elem.getStringAttr("type");
        final String valueTypeStr = elem.getStringAttr("value-type");

        if (parameterName == null) {
            elem.logError("<param> element is missing 'name' attribute.");
            valid = false;
        } else if (parameterName.indexOf('{') != -1 || parameterName.indexOf('}') != -1) {
            elem.logError("Parameter names may not contain '{' or '}'");
            valid = false;
        }

        if (parameterType == null) {
            elem.logError("<param> element is missing 'type' attribute.");
            valid = false;
        }

        EType type = EType.ERROR;
        if (valueTypeStr != null) {
            type = EType.forLabel(valueTypeStr);
        }

        // Create the variable object, and begin populating it.
        AbstractVariable var = null;

        if (VariableInteger.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableInteger(parameterName);
        } else if (VariableReal.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableReal(parameterName);
        } else if (VariableBoolean.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableBoolean(parameterName);
        } else if (VariableSpan.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableSpan(parameterName);
        } else if (VariableRandomInteger.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableRandomInteger(parameterName);
        } else if (VariableRandomReal.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableRandomReal(parameterName);
        } else if (VariableRandomBoolean.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableRandomBoolean(parameterName);
        } else if (VariableRandomChoice.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableRandomChoice(parameterName, type);
        } else if (VariableDerived.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableDerived(parameterName, type);
        } else {
            elem.logError("Unrecognized parameter type: " + parameterType);
            valid = false;
        }

        if (var != null) {
            if (var.type == null) {
                var.type = type;
            }

            // PARSE min/max for variables that need it...

            final String minStr = elem.getStringAttr("min");
            final String maxStr = elem.getStringAttr("max");

            if (var instanceof final VariableRandomInteger vRInt) {
                if (minStr != null) {
                    final Formula minFormula =
                            FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                    if (minFormula == null) {
                        elem.logError("Unable to parse 'min' formula.");
                        valid = false;
                    } else {
                        vRInt.setMin(new NumberOrFormula(minFormula));
                    }
                }
                if (maxStr != null) {
                    final Formula maxFormula =
                            FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                    if (maxFormula == null) {
                        elem.logError("Unable to parse 'max' formula.");
                        valid = false;
                    } else {
                        vRInt.setMax(new NumberOrFormula(maxFormula));
                    }
                }

                if (vRInt.getMin() == null || vRInt.getMax() == null) {
                    elem.logError("'random-int' parameters require 'min' and 'max' attributes.");
                    valid = false;
                }
            } else if (var instanceof final VariableRandomReal vRReal) {
                if (minStr != null) {
                    final Formula minFormula =
                            FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                    if (minFormula == null) {
                        elem.logError("Unable to parse 'min' formula.");
                        valid = false;
                    } else {
                        vRReal.setMin(new NumberOrFormula(minFormula));
                    }
                }
                if (maxStr != null) {
                    final Formula maxFormula =
                            FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                    if (maxFormula == null) {
                        elem.logError("Unable to parse 'max' formula.");
                        valid = false;
                    } else {
                        vRReal.setMax(new NumberOrFormula(maxFormula));
                    }
                }

                if (vRReal.getMin() == null || vRReal.getMax() == null) {
                    elem.logError("'random-real' parameters require 'min' and 'max' attributes.");
                    valid = false;
                }
            } else if (var instanceof final VariableDerived vDer) {
                if (minStr != null) {
                    final Formula minFormula =
                            FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                    if (minFormula == null) {
                        elem.logError("Unable to parse 'min' formula.");
                        valid = false;
                    } else {
                        vDer.setMin(new NumberOrFormula(minFormula));
                    }
                }
                if (maxStr != null) {
                    final Formula maxFormula =
                            FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                    if (maxFormula == null) {
                        elem.logError("Unable to parse 'max' formula.");
                        valid = false;
                    } else {
                        vDer.setMax(new NumberOrFormula(maxFormula));
                    }
                }
            } else {
                // All other types may not have min or max values
                if (minStr != null) {
                    elem.logError("Only random numeric or derived parameters can have minimum.");
                    valid = false;
                }
                if (maxStr != null) {
                    elem.logError("Only random numeric or derived parameters can have maximum.");
                    valid = false;
                }
            }

            final String excludeStr = elem.getStringAttr("exclude");

            if (excludeStr != null) {
                if (var instanceof final IExcludableVariable excludeVar) {
                    final String[] split = excludeStr.split(CoreConstants.COMMA);
                    final List<Formula> formulas = new ArrayList<>(split.length);

                    for (final String toParse : split) {
                        final Formula formula =
                                FormulaFactory.parseFormulaString(evalContext, toParse, mode);

                        if (formula == null) {
                            elem.logError("Unable to parse 'exclude' formula.");
                            valid = false;
                        } else {
                            formulas.add(formula);
                        }
                    }

                    if (formulas.isEmpty()) {
                        elem.logError("Empty exclude value in parameter.");
                        valid = false;
                    } else {
                        final Formula[] excludeFormulae = formulas.toArray(ZERO_LEN_FORMULA_ARR);
                        excludeVar.setExcludes(excludeFormulae);
                    }
                } else {
                    elem.logError(
                            "Only random or derived integer or choice parameter can have excludes.");
                    valid = false;
                }
            }

            final String chooseFromStr = elem.getStringAttr("choose-from");

            if (chooseFromStr != null) {
                if (var instanceof final VariableRandomChoice rcvar) {

                    final String[] split = chooseFromStr.split(CoreConstants.COMMA);
                    final List<Formula> formulas = new ArrayList<>(split.length);

                    for (final String toParse : split) {
                        final Formula formula =
                                FormulaFactory.parseFormulaString(evalContext, toParse, mode);

                        if (formula == null) {
                            elem.logError("Unable to parse 'choose-from' formula.");
                            valid = false;
                        } else {
                            if (type == null) {
                                final EType formulaType = formula.getType(evalContext);

                                if (formulaType != null && formulaType != EType.ERROR) {
                                    type = formulaType;
                                    var.type = formulaType;
                                }
                            }
                            formulas.add(formula);
                        }
                    }

                    if (formulas.isEmpty()) {
                        elem.logError("Empty choose-from value in parameter.");
                        valid = false;
                    } else {
                        final Formula[] chooseFormulae = formulas.toArray(ZERO_LEN_FORMULA_ARR);
                        rcvar.setChooseFromList(chooseFormulae);
                    }
                } else {
                    elem.logError("Only random-choice parameter can have choose-from list.");
                    valid = false;
                }
            }

            final String formulaStr = elem.getStringAttr("formula");

            if (formulaStr != null) {
                if (var instanceof final VariableDerived vDer) {

                    final Formula formula =
                            FormulaFactory.parseFormulaString(evalContext, formulaStr, mode);
                    if (formula == null) {
                        elem.logError("Unable to parse 'formula' formula.");
                        valid = false;
                    } else {
                        vDer.setFormula(formula);

                        if (type == null) {
                            final EType formulaType = formula.getType(evalContext);

                            if (formulaType != null && formulaType != EType.ERROR) {
                                var.type = formulaType;
                            }
                        }
                    }
                } else {
                    elem.logError("Only derived parameter can have formula list.");
                    valid = false;
                }
            }

            final String valueStr = elem.getStringAttr("value");

            if (valueStr == null) {
                if (var instanceof VariableInteger || var instanceof VariableReal
                        || var instanceof VariableBoolean) {
                    elem.logError("Constant parameter with no value.");
                    valid = false;
                }
            } else if (var instanceof VariableInteger || var instanceof VariableReal
                    || var instanceof VariableBoolean) {

                final Formula formula = FormulaFactory.parseFormulaString(evalContext, valueStr, mode);

                if (formula == null) {
                    elem.logError("Constant parameter with invalid value.");
                    valid = false;
                } else if (formula.parameterNames().length > 0) {
                    elem.logError("Constant value may not reference other parameters.");
                    valid = false;
                } else {
                    final Object evaluationResult = formula.evaluate(evalContext);

                    if (evaluationResult instanceof ErrorValue) {
                        elem.logError(
                                "Can't evaluate constant value.\n" + evaluationResult);
                        valid = false;
                    } else if (var instanceof VariableInteger) {
                        if (evaluationResult instanceof Long) {
                            var.setValue(evaluationResult);
                        } else {
                            elem.logError("Integer parameter value is not integer.");
                            valid = false;
                        }
                    } else if (var instanceof VariableReal) {
                        if (evaluationResult instanceof Long) {
                            var.setValue(Double.valueOf(((Long) evaluationResult).doubleValue()));
                        } else if (evaluationResult instanceof Double) {
                            var.setValue(evaluationResult);
                        } else {
                            elem.logError("Real parameter value is not real.");
                            valid = false;
                        }
                    } else if (evaluationResult instanceof Boolean) {
                        var.setValue(evaluationResult);
                    } else {
                        elem.logError("Boolean parameter value is not boolean.");
                        valid = false;
                    }
                }
            } else {
                elem.logError("Only constant types may have value specifications.");
                valid = false;
            }

            final String generatedIntegerStr = elem.getStringAttr("generated-integer");

            if (generatedIntegerStr != null) {
                if (var instanceof VariableRandomInteger || var instanceof VariableRandomReal
                        || var instanceof VariableRandomChoice || var instanceof VariableDerived) {

                    try {
                        var.setValue(Long.valueOf(generatedIntegerStr));
                    } catch (final NumberFormatException e) {
                        elem.logError("Invalid 'generated-integer' value: " + generatedIntegerStr);
                        valid = false;
                    }
                } else {
                    elem.logError(
                            "Only random integer, random real, choice or derived parameter can have generated integer" +
                                    " value.");
                    valid = false;
                }
            }

            final String generatedRealStr = elem.getStringAttr("generated-real");

            if (generatedRealStr != null) {
                if (var instanceof VariableRandomReal || var instanceof VariableRandomChoice
                        || var instanceof VariableDerived) {

                    try {
                        var.setValue(Double.valueOf(generatedRealStr));
                    } catch (final NumberFormatException e) {
                        elem.logError("Invalid 'generated-real' value: " + generatedRealStr);
                        valid = false;
                    }
                } else {
                    elem.logError(
                            "Only random real, choice or derived parameter can have generated real value.");
                    valid = false;
                }
            }

            final String generatedBooleanStr = elem.getStringAttr("generated-boolean");

            if (generatedBooleanStr != null) {
                if (var instanceof VariableRandomBoolean || var instanceof VariableRandomChoice
                        || var instanceof VariableDerived) {

                    try {
                        var.setValue(parseBooleanValue(generatedBooleanStr));
                    } catch (final IllegalArgumentException ex) {
                        elem.logError("Invalid 'generated-boolean' value: " + generatedBooleanStr);
                        valid = false;
                    }
                } else {
                    elem.logError(
                            "Only random boolean, choice or derived parameter can have generated boolean value.");
                    valid = false;
                }
            }

            final String generatedString = elem.getStringAttr("generated-string");

            if (generatedString != null) {
                if (var instanceof VariableDerived) {
                    try {
                        var.setValue(generatedString);
                    } catch (final NumberFormatException e) {
                        elem.logError("Invalid 'generated-string' value: " + generatedString);
                        valid = false;
                    }
                } else {
                    elem.logError("Only derived parameter can have generated string value.");
                    valid = false;
                }
            }

            final String decimalFormatStr = elem.getStringAttr("decimal-format");

            if (decimalFormatStr != null) {
                if (var instanceof final AbstractFormattableVariable formattable) {
                    formattable.setFormatPattern(decimalFormatStr);
                } else {
                    elem.logError("Only formattable variables may have decimal-format.");
                    valid = false;
                }
            }

            if (var instanceof VariableSpan) {
                elem.logError("Span parameter must contain span elements.");
                valid = false;
            }

            if (valid) {
                if (evalContext.getVariable(var.name) == null) {
                    for (final Comment comment : comments) {
                        var.addComment(comment.getContent().trim());
                    }
                    evalContext.addVariable(var);
                } else {
                    elem.logError("Parameter '" + var.name + "' is duplicated.");
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
    private static boolean processNonemptyParamElement(final EvalContext evalContext,
                                                       final NonemptyElement elem, final Iterable<Comment> comments,
                                                       final EParserMode mode) {

        boolean valid = true;

        if (mode == EParserMode.NORMAL) {
            elem.logError("Deprecated '<param>' element");
        }

        final String parameterName = elem.getStringAttr("name");
        final String parameterType = elem.getStringAttr("type");
        final String valueTypeStr = elem.getStringAttr("value-type");

        if (parameterName == null) {
            elem.logError("<param> element is missing 'name' attribute.");
            valid = false;
        } else if (parameterName.indexOf('{') != -1 || parameterName.indexOf('}') != -1) {
            elem.logError("Parameter names may not contain '{' or '}'");
            valid = false;
        }

        if (parameterType == null) {
            elem.logError("<param> element is missing 'type' attribute.");
            valid = false;
        }

        EType type = null;
        if (valueTypeStr != null) {
            type = EType.forLabel(valueTypeStr);
        }

        // Create the variable object, and begin populating it.
        AbstractVariable var = null;

        if (VariableInteger.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableInteger(parameterName);
        } else if (VariableReal.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableReal(parameterName);
        } else if (VariableBoolean.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableBoolean(parameterName);
        } else if (VariableSpan.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableSpan(parameterName);
        } else if (VariableRandomInteger.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableRandomInteger(parameterName);
        } else if (VariableRandomReal.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableRandomReal(parameterName);
        } else if (VariableRandomBoolean.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableRandomBoolean(parameterName);
        } else if (VariableRandomChoice.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            // We make some bogus assumption about type until we learn more
            var = new VariableRandomChoice(parameterName, EType.SPAN);
        } else if (VariableDerived.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            // We make some bogus assumption about type until we learn more
            var = new VariableDerived(parameterName, EType.SPAN);
        } else if (VariableInputInteger.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableInputInteger(parameterName);
        } else if (VariableInputReal.TYPE_TAG.equalsIgnoreCase(parameterType)) {
            var = new VariableInputReal(parameterName);
        } else {
            elem.logError("Unrecognized parameter type: " + parameterType);
            valid = false;
        }

        if (var != null) {
            if (var.type == null) {
                var.type = type;
            }

            // PARSE min/max for variables that need it...

            final String minStr = elem.getStringAttr("min");
            final String maxStr = elem.getStringAttr("max");

            if (var instanceof final VariableRandomInteger vRInt) {
                if (minStr != null) {
                    final Formula minFormula =
                            FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                    if (minFormula == null) {
                        elem.logError("Unable to parse 'min' formula.");
                        valid = false;
                    } else {
                        vRInt.setMin(new NumberOrFormula(minFormula));
                    }
                }
                if (maxStr != null) {
                    final Formula maxFormula =
                            FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                    if (maxFormula == null) {
                        elem.logError("Unable to parse 'max' formula.");
                        valid = false;
                    } else {
                        vRInt.setMax(new NumberOrFormula(maxFormula));
                    }
                }

                if (vRInt.getMin() == null || vRInt.getMax() == null) {
                    elem.logError("'random-int' parameters require 'min' and 'max' attributes.");
                    valid = false;
                }
            } else if (var instanceof final VariableRandomReal vRReal) {
                if (minStr != null) {
                    final Formula minFormula =
                            FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                    if (minFormula == null) {
                        elem.logError("Unable to parse 'min' formula.");
                        valid = false;
                    } else {
                        vRReal.setMin(new NumberOrFormula(minFormula));
                    }
                }
                if (maxStr != null) {
                    final Formula maxFormula =
                            FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                    if (maxFormula == null) {
                        elem.logError("Unable to parse 'max' formula.");
                        valid = false;
                    } else {
                        vRReal.setMax(new NumberOrFormula(maxFormula));
                    }
                }

                if (vRReal.getMin() == null || vRReal.getMax() == null) {
                    elem.logError("'random-real' parameters require 'min' and 'max' attributes.");
                    valid = false;
                }
            } else if (var instanceof final VariableDerived vDer) {
                if (minStr != null) {
                    final Formula minFormula =
                            FormulaFactory.parseFormulaString(evalContext, minStr, mode);
                    if (minFormula == null) {
                        elem.logError("Unable to parse 'min' formula.");
                        valid = false;
                    } else {
                        vDer.setMin(new NumberOrFormula(minFormula));
                    }
                }
                if (maxStr != null) {
                    final Formula maxFormula =
                            FormulaFactory.parseFormulaString(evalContext, maxStr, mode);
                    if (maxFormula == null) {
                        elem.logError("Unable to parse 'max' formula.");
                        valid = false;
                    } else {
                        vDer.setMax(new NumberOrFormula(maxFormula));
                    }
                }
            } else {
                // All other types may not have min or max values
                if (minStr != null) {
                    elem.logError("Only random numeric or derived parameters can have minimum.");
                    valid = false;
                }
                if (maxStr != null) {
                    elem.logError("Only random numeric or derived parameters can have maximum.");
                    valid = false;
                }
            }

            final String excludeStr = elem.getStringAttr("exclude");

            if (excludeStr != null) {
                if (var instanceof final IExcludableVariable excludeVar) {
                    final String[] split = excludeStr.split(CoreConstants.COMMA);
                    final List<Formula> formulas = new ArrayList<>(split.length);

                    for (final String toParse : split) {
                        final Formula formula =
                                FormulaFactory.parseFormulaString(evalContext, toParse, mode);

                        if (formula == null) {
                            elem.logError("Unable to parse 'exclude' formula.");
                            valid = false;
                        } else {
                            formulas.add(formula);
                        }
                    }

                    if (formulas.isEmpty()) {
                        elem.logError("Empty exclude value in parameter.");
                        valid = false;
                    } else {
                        final Formula[] excludeFormulae = formulas.toArray(ZERO_LEN_FORMULA_ARR);
                        excludeVar.setExcludes(excludeFormulae);
                    }
                } else {
                    elem.logError(
                            "Only random or derived integer or choice parameter can have excludes.");
                    valid = false;
                }
            }

            final String chooseFromStr = elem.getStringAttr("choose-from");

            if (chooseFromStr != null) {
                if (var instanceof final VariableRandomChoice rcvar) {

                    final String[] split = chooseFromStr.split(CoreConstants.COMMA);
                    final List<Formula> formulas = new ArrayList<>(split.length);

                    for (final String toParse : split) {
                        final Formula formula =
                                FormulaFactory.parseFormulaString(evalContext, toParse, mode);

                        if (formula == null) {
                            elem.logError("Unable to parse 'choose-from' formula.");
                            valid = false;
                        } else {
                            if (type == null) {
                                final EType formulaType = formula.getType(evalContext);

                                if (formulaType != null && formulaType != EType.ERROR) {
                                    type = formulaType;
                                    var.type = formulaType;
                                }
                            }
                            formulas.add(formula);
                        }
                    }

                    if (formulas.isEmpty()) {
                        elem.logError("Empty choose-from value in parameter.");
                        valid = false;
                    } else {
                        final Formula[] chooseFormulae = formulas.toArray(ZERO_LEN_FORMULA_ARR);
                        rcvar.setChooseFromList(chooseFormulae);

                        if (type != null) {
                            rcvar.setType(type);
                        }
                    }
                } else {
                    elem.logError("Only random-choice parameter can have choose-from list.");
                    valid = false;
                }
            }

            final String formulaStr = elem.getStringAttr("formula");

            if (formulaStr != null) {
                if (var instanceof final VariableDerived vDer) {
                    final Formula formula =
                            FormulaFactory.parseFormulaString(evalContext, formulaStr, mode);

                    if (formula == null) {
                        elem.logError("Unable to parse 'formula' formula.");
                        valid = false;
                    } else {
                        vDer.setFormula(formula);

                        if (type == null) {
                            final EType formulaType = formula.getType(evalContext);

                            if (formulaType != null && formulaType != EType.ERROR) {
                                var.type = formulaType;
                            }
                        }
                    }
                } else {
                    elem.logError("Only derived parameter can have formula list.");
                    valid = false;
                }
            }

            final String valueStr = elem.getStringAttr("value");

            if (valueStr == null) {
                if (var instanceof VariableInteger || var instanceof VariableReal
                        || var instanceof VariableBoolean) {
                    elem.logError("Constant parameter with no value.");
                    valid = false;
                }
            } else if (var instanceof VariableInteger || var instanceof VariableReal
                    || var instanceof VariableBoolean || var instanceof VariableInputInteger
                    || var instanceof VariableInputReal) {

                final Formula formula =
                        FormulaFactory.parseFormulaString(evalContext, valueStr, mode);

                if (formula == null) {
                    elem.logError("Constant parameter with invalid value.");
                    valid = false;
                } else if (formula.parameterNames().length > 0) {
                    elem.logError("Constant value may not reference other parameters.");
                    valid = false;
                } else {
                    final Object evaluationResult = formula.evaluate(evalContext);

                    if (evaluationResult instanceof ErrorValue) {
                        elem.logError(
                                "Can't evaluate constant value.\n" + evaluationResult);
                        valid = false;
                    } else if (var instanceof VariableInteger
                            || var instanceof VariableInputInteger) {
                        if (evaluationResult instanceof Long) {
                            var.setValue(evaluationResult);
                        } else {
                            elem.logError("Integer parameter value is not integer.");
                            valid = false;
                        }
                    } else if (var instanceof VariableReal || var instanceof VariableInputReal) {
                        if (evaluationResult instanceof Long) {
                            var.setValue(Double.valueOf(((Long) evaluationResult).doubleValue()));
                        } else if (evaluationResult instanceof Double) {
                            var.setValue(evaluationResult);
                        } else {
                            elem.logError("Real parameter value is not real.");
                            valid = false;
                        }
                    } else if (evaluationResult instanceof Boolean) {
                        var.setValue(evaluationResult);
                    } else {
                        elem.logError("Boolean parameter value is not boolean.");
                        valid = false;
                    }
                }
            } else {
                elem.logError("Only constant types may have value specifications.");
                valid = false;
            }

            final String generatedIntegerStr = elem.getStringAttr("generated-integer");

            if (generatedIntegerStr != null) {
                if (var instanceof VariableRandomInteger || var instanceof VariableRandomReal
                        || var instanceof VariableRandomChoice || var instanceof VariableDerived) {

                    try {
                        var.setValue(Long.valueOf(generatedIntegerStr));
                    } catch (final NumberFormatException e) {
                        elem.logError("Invalid 'generated-integer' value: " + generatedIntegerStr);
                        valid = false;
                    }
                } else {
                    elem.logError(
                            "Only random integer, random real, choice or derived parameter can have generated integer" +
                                    " value.");
                    valid = false;
                }
            }

            final String generatedRealStr = elem.getStringAttr("generated-real");

            if (generatedRealStr != null) {
                if (var instanceof VariableRandomReal || var instanceof VariableRandomChoice
                        || var instanceof VariableDerived) {

                    try {
                        var.setValue(Double.valueOf(generatedRealStr));
                    } catch (final NumberFormatException e) {
                        elem.logError("Invalid 'generated-real' value: " + generatedRealStr);
                        valid = false;
                    }
                } else {
                    elem.logError(
                            "Only random real, choice or derived parameter can have generated real value.");
                    valid = false;
                }
            }

            final String generatedBooleanStr = elem.getStringAttr("generated-boolean");

            if (generatedBooleanStr != null) {
                if (var instanceof VariableRandomBoolean || var instanceof VariableRandomChoice
                        || var instanceof VariableDerived) {

                    try {
                        var.setValue(parseBooleanValue(generatedBooleanStr));
                    } catch (final IllegalArgumentException ex) {
                        elem.logError("Invalid 'generated-boolean' value: " + generatedBooleanStr);
                        valid = false;
                    }
                } else {
                    elem.logError(
                            "Only random boolean, choice or derived parameter can have generated boolean value.");
                    valid = false;
                }
            }

            final String generatedString = elem.getStringAttr("generated-string");

            if (generatedString != null) {
                if (var instanceof VariableDerived) {
                    try {
                        var.setValue(generatedString);
                    } catch (final NumberFormatException e) {
                        elem.logError("Invalid 'generated-string' value: " + generatedString);
                        valid = false;
                    }
                } else {
                    elem.logError("Only derived parameter can have generated string value.");
                    valid = false;
                }
            }

            final String decimalFormatStr = elem.getStringAttr("decimal-format");

            if (decimalFormatStr != null) {
                if (var instanceof final AbstractFormattableVariable formattable) {
                    formattable.setFormatPattern(decimalFormatStr);
                } else {
                    elem.logError("Only formattable variables may have decimal-format.");
                    valid = false;
                }
            }

            if (var instanceof VariableSpan) {
                final DocSimpleSpan span = DocFactory.parseSpan(evalContext, elem, mode);
                if (span != null && span.numChildren() > 0) {
                    var.setValue(span);
                }
            } else if ((var instanceof VariableDerived || var instanceof VariableRandomChoice)) {
                DocSimpleSpan span = DocFactory.parseSpan(evalContext, elem, mode);
                if (span == null) {
                    span = new DocSimpleSpan();
                }
                var.setValue(span);
            }

            if (valid) {
                if (evalContext.getVariable(var.name) == null) {
                    for (final Comment comment : comments) {
                        var.addComment(comment.getContent().trim());
                    }
                    evalContext.addVariable(var);
                } else {
                    elem.logError("Parameter '" + var.name + "' is duplicated.");
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
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean processEmptyVarElement(final EvalContext evalContext,
                                                  final EmptyElement elem, final Iterable<Comment> comments) {

        boolean valid = false;

        final String varName = elem.getStringAttr("name");
        final String typeStr = elem.getStringAttr("type");

        if (varName == null) {
            elem.logError("Missing 'name' attribute on <var> element");
        } else if (typeStr == null) {
            elem.logError("Missing 'type' attribute on <var> element");
        } else if (varName.indexOf('{') != -1 || varName.indexOf('}') != -1) {
            elem.logError("Variable names may not contain '{' or '}'");
        } else {
            AbstractVariable var = null;

            if (VariableBoolean.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableBoolean(elem, varName);
            } else if (VariableInteger.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableInteger(elem, varName);
            } else if (VariableReal.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableReal(elem, varName);
            } else if (VariableRandomBoolean.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomBoolean(elem, varName);
            } else if (VariableRandomInteger.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomInteger(elem, varName);
            } else if (VariableRandomReal.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomReal(elem, varName);
            } else if (VariableRandomPermutation.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomPermutation(elem, varName);
            } else if (VariableRandomSimpleAngle.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomSimpleAngle(elem, varName);

            } else if (VariableInputInteger.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableInputInteger(varName);
            } else if (VariableInputIntegerVector.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableInputIntegerVector(varName);
            } else if (VariableInputReal.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableInputReal(varName);
            } else if (VariableInputString.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableInputString(varName);
            } else {
                elem.logError("Unrecognized empty-element variable type.");
            }

            if (var != null) {
                if (evalContext.getVariable(var.name) == null) {
                    for (final Comment comment : comments) {
                        var.addComment(comment.getContent().trim());
                    }
                    evalContext.addVariable(var);
                    valid = true;
                } else {
                    elem.logError("Variable '" + var.name + "' is duplicated.");
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
    private static boolean processNonemptyVarElement(final EvalContext evalContext,
                                                     final NonemptyElement elem, final Iterable<Comment> comments,
                                                     final EParserMode mode) {

        boolean valid = false;

        final String varName = elem.getStringAttr("name");
        final String typeStr = elem.getStringAttr("type");

        if (varName == null) {
            elem.logError("Missing 'name' attribute on <var> element");
        } else if (typeStr == null) {
            elem.logError("Missing 'type' attribute on <var> element");
        } else if (varName.indexOf('{') != -1 || varName.indexOf('}') != -1) {
            elem.logError("Variable names may not contain '{' or '}'");
        } else {
            AbstractVariable var = null;

            if (VariableBoolean.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableBoolean(elem, varName);
            } else if (VariableInteger.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableInteger(elem, varName);
            } else if (VariableReal.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableReal(elem, varName);
            } else if (VariableSpan.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableSpan(evalContext, elem, varName, mode);
            } else if (VariableRandomBoolean.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomBoolean(elem, varName);
            } else if (VariableRandomInteger.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomInteger(evalContext, elem, varName, mode);
            } else if (VariableRandomReal.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomReal(evalContext, elem, varName, mode);
            } else if (VariableRandomPermutation.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomPermutation(evalContext, elem, varName, mode);
            } else if (VariableRandomChoice.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomChoice(evalContext, elem, varName, mode);
            } else if (VariableRandomSimpleAngle.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableRandomSimpleAngle(evalContext, elem, varName, mode);
            } else if (VariableDerived.TYPE_TAG.contentEquals(typeStr)) {
                var = extractVariableDerived(evalContext, elem, varName, mode);
            } else {
                elem.logError("Unrecognized nonempty-element variable type.");
            }

            if (var != null) {
                if (evalContext.getVariable(var.name) == null) {
                    for (final Comment comment : comments) {
                        var.addComment(comment.getContent().trim());
                    }
                    evalContext.addVariable(var);
                    valid = true;
                } else {
                    elem.logError("Variable '" + var.name + "' is duplicated.");
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
     * @return the parsed variable on success; null on failure
     */
    private static VariableBoolean extractVariableBoolean(final EmptyElement elem,
                                                          final String varName) {

        VariableBoolean result = null;

        final String valueStr = elem.getStringAttr("value");

        if (valueStr == null) {
            elem.logError(
                    "Boolean <var> element missing required 'value' attribute in {" + varName + "}");
        } else if ("true".equalsIgnoreCase(valueStr)) {
            result = new VariableBoolean(varName);
            result.setValue(Boolean.TRUE);
        } else if ("false".equalsIgnoreCase(valueStr)) {
            result = new VariableBoolean(varName);
            result.setValue(Boolean.FALSE);
        } else {
            elem.logError("Invalid boolean value: '" + valueStr + "' in {" + varName + "}");
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
     * @return the parsed variable on success; null on failure
     */
    private static VariableBoolean extractVariableBoolean(final NonemptyElement elem,
                                                          final String varName) {

        VariableBoolean result = null;

        final List<IElement> children = elem.getElementChildrenAsList();
        if (children.isEmpty()) {
            final String valueStr = elem.getStringAttr("value");

            if (valueStr == null) {
                elem.logError("Boolean <var> element missing required 'value' attribute in {"
                        + varName + "}");
            } else if ("true".equalsIgnoreCase(valueStr)) {
                result = new VariableBoolean(varName);
                result.setValue(Boolean.TRUE);
            } else if ("false".equalsIgnoreCase(valueStr)) {
                result = new VariableBoolean(varName);
                result.setValue(Boolean.FALSE);
            } else {
                elem.logError("Invalid boolean value: '" + valueStr + "' in {" + varName + "}");
            }
        } else {
            elem.logError("Boolean variable may not contain child elements.");
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
     * @return the parsed variable on success; null on failure
     */
    private static VariableInteger extractVariableInteger(final EmptyElement elem,
                                                          final String varName) {

        VariableInteger result = null;

        final String valueStr = elem.getStringAttr("value");

        if (valueStr == null) {
            elem.logError(
                    "Integer <var> element missing required 'value' attribute in {" + varName + "}");
        } else {
            String formatStr = elem.getStringAttr("format");
            if (formatStr == null) {
                // TODO: Deprecate this format at some point
                formatStr = elem.getStringAttr("decimal-format");
            }

            try {
                final Long longVal = Long.valueOf(valueStr);
                result = new VariableInteger(varName);
                result.setValue(longVal);
                result.setFormatPattern(formatStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "}");
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
     * @return the parsed variable on success; null on failure
     */
    private static VariableInteger extractVariableInteger(final NonemptyElement elem,
                                                          final String varName) {

        VariableInteger result = null;

        String formatStr = elem.getStringAttr("format");
        if (formatStr == null) {
            // TODO: Deprecate this format at some point
            formatStr = elem.getStringAttr("decimal-format");
        }

        final List<IElement> children = elem.getElementChildrenAsList();
        if (children.isEmpty()) {
            final String valueStr = elem.getStringAttr("value");
            if (valueStr == null) {
                elem.logError("Integer <var> element missing required 'value' attribute in {"
                        + varName + "}");
            } else {
                try {
                    final Long longVal = Long.valueOf(valueStr);
                    result = new VariableInteger(varName);
                    result.setValue(longVal);
                    result.setFormatPattern(formatStr);
                } catch (final NumberFormatException ex) {
                    elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "}");
                }
            }
        } else {
            elem.logError("Integer variable may not contain child elements.");
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
     * @return the parsed variable on success; null on failure
     */
    private static VariableReal extractVariableReal(final EmptyElement elem, final String varName) {

        VariableReal result = null;

        String formatStr = elem.getStringAttr("format");
        if (formatStr == null) {
            // TODO: Deprecate this format at some point
            formatStr = elem.getStringAttr("decimal-format");
        }

        final String valueStr = elem.getStringAttr("value");
        if (valueStr == null) {
            elem.logError(
                    "Real <var> element missing required 'value' attribute in {" + varName + "}");
        } else {
            try {
                final Number numberVal = NumberParser.parse(valueStr);
                result = new VariableReal(varName);
                result.setValue(numberVal);
                result.setFormatPattern(formatStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid real value: '" + valueStr + "' in {" + varName + "}");
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
     * @return the parsed variable on success; null on failure
     */
    private static VariableReal extractVariableReal(final NonemptyElement elem,
                                                    final String varName) {

        VariableReal result = null;

        String formatStr = elem.getStringAttr("format");
        if (formatStr == null) {
            // TODO: Deprecate this format at some point
            formatStr = elem.getStringAttr("decimal-format");
        }

        final List<IElement> children = elem.getElementChildrenAsList();
        if (children.isEmpty()) {
            final String valueStr = elem.getStringAttr("value");

            if (valueStr == null) {
                elem.logError(
                        "Real <var> element missing required 'value' attribute in {" + varName + "}");
            } else {
                try {
                    final Number numberVal = NumberParser.parse(valueStr);
                    result = new VariableReal(varName);
                    result.setValue(numberVal);
                    result.setFormatPattern(formatStr);
                } catch (final NumberFormatException ex) {
                    elem.logError("Invalid real value: '" + valueStr + "' in {" + varName + "}");
                }
            }
        } else {
            elem.logError("Real variable may not contain child elements.");
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
    private static VariableSpan extractVariableSpan(final EvalContext evalContext,
                                                    final NonemptyElement elem, final String varName,
                                                    final EParserMode mode) {

        final VariableSpan result;

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
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomBoolean extractVariableRandomBoolean(final EmptyElement elem,
                                                                      final String varName) {

        VariableRandomBoolean result = null;

        final String valueStr = elem.getStringAttr("value");

        if (valueStr == null) {
            result = new VariableRandomBoolean(varName);
        } else if ("true".equalsIgnoreCase(valueStr)) {
            result = new VariableRandomBoolean(varName);
            result.setValue(Boolean.TRUE);
        } else if ("false".equalsIgnoreCase(valueStr)) {
            result = new VariableRandomBoolean(varName);
            result.setValue(Boolean.FALSE);
        } else {
            elem.logError("Invalid boolean value: '" + valueStr + "' in {" + varName + "}");
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
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomBoolean extractVariableRandomBoolean(final NonemptyElement elem,
                                                                      final String varName) {

        VariableRandomBoolean result = null;

        final List<IElement> children = elem.getElementChildrenAsList();
        if (children.isEmpty()) {
            final String valueStr = elem.getStringAttr("value");

            if (valueStr == null) {
                result = new VariableRandomBoolean(varName);
            } else if ("true".equalsIgnoreCase(valueStr)) {
                result = new VariableRandomBoolean(varName);
                result.setValue(Boolean.TRUE);
            } else if ("false".equalsIgnoreCase(valueStr)) {
                result = new VariableRandomBoolean(varName);
                result.setValue(Boolean.FALSE);
            } else {
                elem.logError(
                        "Invalid random boolean value: '" + valueStr + "' in {" + varName + "}");
            }
        } else {
            elem.logError("Random boolean variable may not contain child elements.");
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
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomInteger extractVariableRandomInteger(final EmptyElement elem,
                                                                      final String varName) {

        boolean valid = true;

        final String valueStr = elem.getStringAttr("value");
        Long value = null;

        if (valueStr != null) {
            try {
                value = Long.valueOf(valueStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        VariableRandomInteger result = null;

        final String minStr = elem.getStringAttr("min");
        final String maxStr = elem.getStringAttr("max");

        if (minStr == null || maxStr == null) {
            elem.logError("Missing required min/max value in random integer var {" + varName + "}");
        } else {
            NumberOrFormula min = null;
            try {
                min = new NumberOrFormula(Long.valueOf(minStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "}");
                valid = false;
            }

            NumberOrFormula max = null;
            try {
                max = new NumberOrFormula(Long.valueOf(maxStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "}");
                valid = false;
            }

            if (valid) {
                String formatStr = elem.getStringAttr("format");
                if (formatStr == null) {
                    // TODO: Deprecate this format at some point
                    formatStr = elem.getStringAttr("decimal-format");
                }

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

        final String valueStr = elem.getStringAttr("value");
        Long value = null;

        if (valueStr != null) {
            try {
                value = Long.valueOf(valueStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        VariableRandomInteger result = null;

        NumberOrFormula min = null;
        NumberOrFormula max = null;

        final String minStr = elem.getStringAttr("min");
        if (minStr != null) {
            try {
                min = new NumberOrFormula(Long.valueOf(minStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        final String maxStr = elem.getStringAttr("max");
        if (maxStr != null) {
            try {
                max = new NumberOrFormula(Long.valueOf(maxStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        if (valid) {
            final Collection<Formula> excludes = new ArrayList<>(5);

            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    if ("min".equals(tag)) {
                        if (min == null) {
                            final Formula minFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (minFormula == null) {
                                elem.logError("Invalid 'min' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                min = new NumberOrFormula(minFormula);
                            }
                        } else {
                            elem.logError("Multiple 'min' values in {" + varName + "}");
                            break;
                        }
                    } else if ("max".equals(tag)) {
                        if (max == null) {
                            final Formula maxFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (maxFormula == null) {
                                elem.logError("Invalid 'max' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                max = new NumberOrFormula(maxFormula);
                            }
                        } else {
                            elem.logError("Multiple 'max' values in {" + varName + "}");
                            break;
                        }
                    } else if ("exclude".equals(tag)) {
                        final Formula exclude =
                                XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                        if (exclude == null) {
                            elem.logError("Invalid 'exclude' formula in {" + varName + "}: "
                                    + nonempty.print(0));
                            valid = false;
                        } else {
                            excludes.add(exclude);
                        }
                    } else {
                        elem.logError("Unsupported '" + tag + "' element in in {" + varName + "}");
                        valid = false;
                    }
                }
            }

            if (valid) {
                String formatStr = elem.getStringAttr("format");
                if (formatStr == null) {
                    // TODO: Deprecate this format at some point
                    formatStr = elem.getStringAttr("decimal-format");
                }

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
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomReal extractVariableRandomReal(final EmptyElement elem,
                                                                final String varName) {

        boolean valid = true;

        final String valueStr = elem.getStringAttr("value");
        Number value = null;

        if (valueStr != null) {
            try {
                value = NumberParser.parse(valueStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid real value: '" + valueStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        VariableRandomReal result = null;

        final String minStr = elem.getStringAttr("min");
        final String maxStr = elem.getStringAttr("max");

        if (minStr == null || maxStr == null) {
            elem.logError("Missing required min/max value in random real var {" + varName + "}");
        } else {
            NumberOrFormula min = null;
            try {
                min = new NumberOrFormula(NumberParser.parse(minStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "}");
                valid = false;
            }

            NumberOrFormula max = null;
            try {
                max = new NumberOrFormula(NumberParser.parse(maxStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "}");
                valid = false;
            }

            if (valid) {
                String formatStr = elem.getStringAttr("format");
                if (formatStr == null) {
                    // TODO: Deprecate this format at some point
                    formatStr = elem.getStringAttr("decimal-format");
                }

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

        final String valueStr = elem.getStringAttr("value");
        Double value = null;

        if (valueStr != null) {
            try {
                value = Double.valueOf(valueStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid real value: '" + valueStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        NumberOrFormula min = null;
        NumberOrFormula max = null;

        final String minStr = elem.getStringAttr("min");
        if (minStr != null) {
            try {
                min = new NumberOrFormula(NumberParser.parse(minStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        final String maxStr = elem.getStringAttr("max");
        if (maxStr != null) {
            try {
                max = new NumberOrFormula(NumberParser.parse(minStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        VariableRandomReal result = null;

        if (valid) {
            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    if ("min".equals(tag)) {
                        if (min == null) {
                            final Formula minFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (minFormula == null) {
                                elem.logError("Invalid 'min' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                min = new NumberOrFormula(minFormula);
                            }
                        } else {
                            elem.logError("Multiple 'min' values in {" + varName + "}");
                            break;
                        }
                    } else if ("max".equals(tag)) {
                        if (max == null) {
                            final Formula maxFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (maxFormula == null) {
                                elem.logError("Invalid 'max' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                max = new NumberOrFormula(maxFormula);
                            }
                        } else {
                            elem.logError("Multiple 'max' values in {" + varName + "}");
                            break;
                        }
                    } else {
                        elem.logError("Unsupported '" + tag + "' element in {" + varName + "}");
                        valid = false;
                    }
                }
            }

            if (valid) {
                String formatStr = elem.getStringAttr("format");
                if (formatStr == null) {
                    // TODO: Deprecate this format at some point
                    formatStr = elem.getStringAttr("decimal-format");
                }

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
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomPermutation
    extractVariableRandomPermutation(final EmptyElement elem, final String varName) {

        boolean valid = true;

        final String valueStr = elem.getStringAttr("value");
        IntegerVectorValue value = null;

        if (valueStr != null) {
            try {
                value = IntegerVectorValue.parse(valueStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid permutation value: '" + valueStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        VariableRandomPermutation result = null;

        final String minStr = elem.getStringAttr("min");
        final String maxStr = elem.getStringAttr("max");

        if (minStr == null || maxStr == null) {
            elem.logError(
                    "Missing required min/max value in random permutation var {" + varName + "}");
        } else {
            NumberOrFormula min = null;
            try {
                min = new NumberOrFormula(Long.valueOf(minStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "}");
                valid = false;
            }

            NumberOrFormula max = null;
            try {
                max = new NumberOrFormula(Long.valueOf(maxStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "}");
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
    private static VariableRandomPermutation extractVariableRandomPermutation(
            final EvalContext evalContext, final NonemptyElement elem, final String varName,
            final EParserMode mode) {

        boolean valid = true;

        final String valueStr = elem.getStringAttr("value");
        IntegerVectorValue value = null;

        if (valueStr != null) {
            try {
                value = IntegerVectorValue.parse(valueStr);
            } catch (final IllegalArgumentException ex) {
                elem.logError("Invalid permutation value: '" + valueStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        NumberOrFormula min = null;
        NumberOrFormula max = null;

        final String minStr = elem.getStringAttr("min");
        if (minStr != null) {
            try {
                min = new NumberOrFormula(Long.valueOf(minStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        final String maxStr = elem.getStringAttr("max");
        if (maxStr != null) {
            try {
                max = new NumberOrFormula(Long.valueOf(maxStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        VariableRandomPermutation result = null;

        if (valid) {
            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    if ("min".equals(tag)) {
                        if (min == null) {
                            final Formula minFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (minFormula == null) {
                                elem.logError("Invalid 'min' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                min = new NumberOrFormula(minFormula);
                            }
                        } else {
                            elem.logError("Multiple 'min' values in {" + varName + "}");
                            break;
                        }
                    } else if ("max".equals(tag)) {
                        if (max == null) {
                            final Formula maxFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (maxFormula == null) {
                                elem.logError("Invalid 'max' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                max = new NumberOrFormula(maxFormula);
                            }
                        } else {
                            elem.logError("Multiple 'max' values in {" + varName + "}");
                            break;
                        }
                    } else {
                        elem.logError("Unsupported '" + tag + "' element in in {" + varName + "}");
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

        Object value = null;

        EType type = EType.ERROR;
        final String valueTypeStr = elem.getStringAttr("value-type");
        if (valueTypeStr != null) {
            type = EType.forLabel(valueTypeStr);
        }

        final String longStr = elem.getStringAttr("long");
        if (longStr == null) {
            final String doubleStr = elem.getStringAttr("double");
            if (doubleStr == null) {
                final String booleanStr = elem.getStringAttr("boolean");
                if (booleanStr == null) {
                    final String irrationalStr = elem.getStringAttr("irrational");
                    if (irrationalStr == null) {
                        final String stringStr = elem.getStringAttr("string");
                        if (stringStr == null) {

                            final String intVectorStr = elem.getStringAttr("int-vector");
                            if (intVectorStr != null) {
                                if (type == EType.ERROR) {
                                    type = EType.INTEGER_VECTOR;
                                }
                                try {
                                    value = IntegerVectorValue.parse(intVectorStr);
                                } catch (final NumberFormatException ex) {
                                    elem.logError("Invalid integer vector value: '" + intVectorStr
                                            + "' in {" + varName + "}");
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
                            elem.logError("Invalid irrational value: '" + irrationalStr + "' in {"
                                    + varName + "}");
                            valid = false;
                        }
                    }
                } else {
                    if (type == EType.ERROR) {
                        type = EType.BOOLEAN;
                    }
                    if ("true".equalsIgnoreCase(booleanStr)) {
                        value = Boolean.TRUE;
                    } else if ("false".equalsIgnoreCase(booleanStr)) {
                        value = Boolean.FALSE;
                    } else {
                        elem.logError(
                                "Invalid boolean value: '" + booleanStr + "' in {" + varName + "}");
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
                    elem.logError("Invalid real value: '" + doubleStr + "' in {" + varName + "}");
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
                elem.logError("Invalid long value: '" + longStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        VariableRandomChoice result = null;

        if (valid) {
            final Collection<Formula> excludes = new ArrayList<>(5);
            final Collection<Formula> choices = new ArrayList<>(5);
            DocSimpleSpan span = null;

            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    if ("exclude".equals(tag)) {
                        final Formula exclude =
                                XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                        if (exclude == null) {
                            elem.logError("Invalid 'exclude' formula in {" + varName + "}: "
                                    + nonempty.print(0));
                            valid = false;
                        } else {
                            excludes.add(exclude);
                        }
                    } else if ("choose-from".equals(tag)) {
                        final Formula choice =
                                XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                        if (choice == null) {
                            elem.logError("Invalid 'choose-from' formula in {" + varName + "}: "
                                    + nonempty.print(0));
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
                                    elem.logError(
                                            "Inconsistent types in 'choose-from' formulas in {"
                                                    + varName + "}");
                                    valid = false;
                                }
                            }
                        }
                    } else if ("span".equals(tag)) {
                        if (type == EType.ERROR) {
                            type = EType.SPAN;
                        }
                        if (span == null) {
                            span = DocFactory.parseSpan(evalContext, nonempty, mode);
                            if (span == null) {
                                elem.logError("Invalid <span> content in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            }
                        } else {
                            elem.logError(
                                    "Multiple 'span' values not allowed in {" + varName + "}");
                            break;
                        }
                    } else {
                        elem.logError("Unsupported '" + tag + "' element in {" + varName + "}");
                        valid = false;
                    }
                }
            }

            if (value != null && span != null) {
                elem.logError("Cannot have both attribute-specified value and span value in {"
                        + varName + "}");
                valid = false;
            }

            if (valid) {
                String formatStr = elem.getStringAttr("format");
                if (formatStr == null) {
                    // TODO: Deprecate this format at some point
                    formatStr = elem.getStringAttr("decimal-format");
                }

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
     * @return {@code true} if successful, {@code false} on any error
     */
    private static VariableRandomSimpleAngle
    extractVariableRandomSimpleAngle(final EmptyElement elem, final String varName) {

        boolean valid = true;

        final String valueStr = elem.getStringAttr("value");
        Long value = null;

        if (valueStr != null) {
            try {
                value = Long.valueOf(valueStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        VariableRandomSimpleAngle result = null;

        final String minStr = elem.getStringAttr("min");
        final String maxStr = elem.getStringAttr("max");

        if (minStr == null || maxStr == null) {
            elem.logError("Missing required min/max value in random integer var {" + varName + "}");
        } else {
            NumberOrFormula min = null;
            try {
                min = new NumberOrFormula(Long.valueOf(minStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "}");
                valid = false;
            }

            NumberOrFormula max = null;
            try {
                max = new NumberOrFormula(Long.valueOf(maxStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "}");
                valid = false;
            }

            final String maxDenomStr = elem.getStringAttr("max-denom");
            NumberOrFormula maxDenom = null;
            try {
                maxDenom = new NumberOrFormula(Long.valueOf(maxDenomStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "}");
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
    private static VariableRandomSimpleAngle extractVariableRandomSimpleAngle(
            final EvalContext evalContext, final NonemptyElement elem, final String varName,
            final EParserMode mode) {

        boolean valid = true;

        final String valueStr = elem.getStringAttr("value");
        Long value = null;

        if (valueStr != null) {
            try {
                value = Long.valueOf(valueStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid integer value: '" + valueStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        NumberOrFormula min = null;
        NumberOrFormula max = null;
        NumberOrFormula maxDenom = null;

        final String minStr = elem.getStringAttr("min");
        if (minStr != null) {
            try {
                min = new NumberOrFormula(Long.valueOf(minStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        final String maxStr = elem.getStringAttr("max");
        if (maxStr != null) {
            try {
                max = new NumberOrFormula(Long.valueOf(maxStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        final String maxDenomStr = elem.getStringAttr("max-denom");
        if (maxDenomStr != null) {
            try {
                maxDenom = new NumberOrFormula(Long.valueOf(maxDenomStr));
            } catch (final NumberFormatException ex) {
                elem.logError(
                        "Invalid max-denom value: '" + maxDenomStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        VariableRandomSimpleAngle result = null;

        if (valid) {
            final Collection<Formula> excludes = new ArrayList<>(5);

            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    if ("min".equals(tag)) {
                        if (min == null) {
                            final Formula minFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (minFormula == null) {
                                elem.logError("Invalid 'min' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                min = new NumberOrFormula(minFormula);
                            }
                        } else {
                            elem.logError("Multiple 'min' values in {" + varName + "}");
                            break;
                        }
                    } else if ("max".equals(tag)) {
                        if (max == null) {
                            final Formula maxFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (maxFormula == null) {
                                elem.logError("Invalid 'max' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                max = new NumberOrFormula(maxFormula);
                            }
                        } else {
                            elem.logError("Multiple 'max' values in {" + varName + "}");
                            break;
                        }
                    } else if ("max-denom".equals(tag)) {
                        if (maxDenom == null) {
                            final Formula maxDenomFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (maxDenomFormula == null) {
                                elem.logError("Invalid 'max-denom' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                maxDenom = new NumberOrFormula(maxDenomFormula);
                            }
                        } else {
                            elem.logError("Multiple 'max-denom' values in {" + varName + "}");
                            break;
                        }
                    } else if ("exclude".equals(tag)) {
                        final Formula exclude =
                                XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                        if (exclude == null) {
                            elem.logError("Invalid 'exclude' formula in {" + varName + "}: "
                                    + nonempty.print(0));
                            valid = false;
                        } else {
                            excludes.add(exclude);
                        }
                    } else {
                        elem.logError("Unsupported '" + tag + "' element in in {" + varName + "}");
                        valid = false;
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
    private static VariableDerived extractVariableDerived(final EvalContext evalContext,
                                                          final NonemptyElement elem, final String varName,
                                                          final EParserMode mode) {

        boolean valid = true;

        Object value = null;

        EType type = EType.ERROR;
        final String valueTypeStr = elem.getStringAttr("value-type");
        if (valueTypeStr != null) {
            type = EType.forLabel(valueTypeStr);
            if (type == null) {
                elem.logError("Invalid value-type: '" + valueTypeStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        final String longStr = elem.getStringAttr("long");
        if (longStr == null) {
            final String doubleStr = elem.getStringAttr("double");
            if (doubleStr == null) {
                final String booleanStr = elem.getStringAttr("boolean");
                if (booleanStr == null) {
                    final String irrationalStr = elem.getStringAttr("irrational");
                    if (irrationalStr == null) {
                        final String stringStr = elem.getStringAttr("string");
                        if (stringStr == null) {
                            final String intVectorStr = elem.getStringAttr("int-vector");
                            if (intVectorStr != null) {
                                if (type == EType.ERROR) {
                                    type = EType.INTEGER_VECTOR;
                                }
                                try {
                                    value = IntegerVectorValue.parse(intVectorStr);
                                } catch (final NumberFormatException ex) {
                                    elem.logError("Invalid integer vector value: '" + intVectorStr
                                            + "' in {" + varName + "}");
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
                            elem.logError("Invalid irrational value: '" + irrationalStr + "' in {"
                                    + varName + "}");
                            valid = false;
                        }
                    }
                } else {
                    if (type == EType.ERROR) {
                        type = EType.BOOLEAN;
                    }
                    if ("true".equalsIgnoreCase(booleanStr)) {
                        value = Boolean.TRUE;
                    } else if ("false".equalsIgnoreCase(booleanStr)) {
                        value = Boolean.FALSE;
                    } else {
                        elem.logError(
                                "Invalid boolean value: '" + booleanStr + "' in {" + varName + "}");
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
                    elem.logError("Invalid real value: '" + doubleStr + "' in {" + varName + "}");
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
                elem.logError("Invalid long value: '" + longStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        VariableDerived result = null;

        NumberOrFormula min = null;
        NumberOrFormula max = null;

        final String minStr = elem.getStringAttr("min");
        if (minStr != null) {
            try {
                min = new NumberOrFormula(Long.valueOf(minStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid min value: '" + minStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        final String maxStr = elem.getStringAttr("max");
        if (maxStr != null) {
            try {
                max = new NumberOrFormula(Long.valueOf(maxStr));
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid max value: '" + maxStr + "' in {" + varName + "}");
                valid = false;
            }
        }

        if (valid) {
            final Collection<Formula> excludes = new ArrayList<>(5);
            DocSimpleSpan span = null;
            Formula formula = null;

            for (final IElement child : elem.getElementChildrenAsList()) {

                if (child instanceof final NonemptyElement nonempty) {
                    final String tag = child.getTagName();

                    if ("min".equals(tag)) {
                        if (min == null) {
                            final Formula minFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (minFormula == null) {
                                elem.logError("Invalid 'min' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                min = new NumberOrFormula(minFormula);
                            }
                        } else {
                            elem.logError("Multiple 'min' values in {" + varName + "}");
                            break;
                        }
                    } else if ("max".equals(tag)) {
                        if (max == null) {
                            final Formula maxFormula =
                                    XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (maxFormula == null) {
                                elem.logError("Invalid 'max' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            } else {
                                max = new NumberOrFormula(maxFormula);
                            }
                        } else {
                            elem.logError("Multiple 'max' values in {" + varName + "}");
                            break;
                        }
                    } else if ("exclude".equals(tag)) {
                        final Formula exclude =
                                XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                        if (exclude == null) {
                            elem.logError("Invalid 'exclude' formula in {" + varName + "}: "
                                    + nonempty.print(0));
                            valid = false;
                        } else {
                            excludes.add(exclude);
                        }
                    } else if ("span".equals(tag)) {
                        if (type == EType.ERROR) {
                            type = EType.SPAN;
                        }
                        if (span == null) {
                            span = DocFactory.parseSpan(evalContext, nonempty, mode);
                            if (span == null) {
                                elem.logError("Invalid <span> content in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            }
                        } else {
                            elem.logError("Multiple 'span' values in {" + varName + "}");
                            break;
                        }
                    } else if ("formula".equals(tag)) {
                        if (formula == null) {
                            formula = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                            if (formula == null) {
                                elem.logError("Invalid 'formula' formula in {" + varName + "}: "
                                        + nonempty.print(0));
                                valid = false;
                            }
                        } else {
                            elem.logError("Multiple 'formula' values in {" + varName + "}");
                            break;
                        }
                    } else {
                        elem.logError("Unsupported '" + tag + "' element in {" + varName + "}");
                        valid = false;
                    }
                }
            }

            if (value != null && span != null) {
                elem.logError("Cannot have both attribute-specified value and span value.");
                valid = false;
            }

            if (valid) {
                String formatStr = elem.getStringAttr("format");
                if (formatStr == null) {
                    // TODO: Deprecate this format at some point
                    formatStr = elem.getStringAttr("decimal-format");
                }

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
                    throw new IllegalArgumentException("Invalid boolean format '" + str + "'");
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
                throw new IllegalArgumentException("Invalid boolean format '" + str + "'");
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
