package dev.mathops.assessment.problem.template;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.EType;
import dev.mathops.assessment.NumberParser;
import dev.mathops.assessment.document.template.AbstractDocInput;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.ConstBooleanValue;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.formula.XmlFormulaFactory;
import dev.mathops.assessment.problem.ECalculatorType;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableDerived;
import dev.mathops.assessment.variable.VariableFactory;
import dev.mathops.assessment.variable.VariableInputInteger;
import dev.mathops.assessment.variable.VariableInputReal;
import dev.mathops.assessment.variable.VariableRandomChoice;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.CharSpan;
import dev.mathops.text.parser.ICharSpan;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.CData;
import dev.mathops.text.parser.xml.IElement;
import dev.mathops.text.parser.xml.INode;
import dev.mathops.text.parser.xml.NonemptyElement;
import dev.mathops.text.parser.xml.XmlContent;
import dev.mathops.text.parser.xml.XmlContentError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A factory class to load {@code Problem} objects from XML files. This class offers only static methods to generate
 * {@code Problem} objects from a {@code Reference}, a {@code File}, a {@code URL}, or a {@code String}. All the load
 * methods generate a {@code Problem} object even if there is an error in loading. The {@code Problem} object will
 * contain the list of errors from the load process. This prevents error state from having to be maintained in this
 * class.
 */
public enum ProblemTemplateFactory {
    ;

    /** Am empty array used when allocating arrays of objects. */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * Loads the problem from an {@code XmlSource}.
     *
     * @param content the {@code XmlContent} from which to load the problem
     * @param mode    the parser mode
     * @return the loaded {@code AbstractProblem} object on success (actually a subclass of {@code AbstractProblem}
     *         based on the type specified in the XML), or an empty {@code AbstractProblem} object containing the set of
     *         errors encountered on failure
     */
    public static AbstractProblemTemplate load(final XmlContent content, final EParserMode mode) {

        // Log.info(CoreConstants.CRLF, content.toString());

        AbstractProblemTemplate problem = createFromSource(content, mode);

        // On failure, create a default Problem containing load errors
        if (problem == null) {
            problem = new ProblemDummyTemplate();

            Log.warning("Failed to parse problem:");
            Log.fine(content);
            for (final XmlContentError err : content.getAllErrors()) {
                Log.warning("    ", err);
            }
        }

        return problem;
    }

    /**
     * Generate a problem from a {@code String} containing XML source.
     *
     * @param xml  the {@code String} containing the problem source XML
     * @param mode the parser mode
     * @return the loaded {@code AbstractProblem} object on success, or an empty {@code ProblemDummy} object containing
     *         the set of errors encountered on failure
     */
    public static AbstractProblemTemplate load(final String xml, final EParserMode mode) {

        AbstractProblemTemplate problem;

        try {
            final XmlContent source = new XmlContent(xml, false, true);
            problem = createFromSource(source, mode);

            if (problem == null) {
                problem = new ProblemDummyTemplate();

                Log.warning("Failed to parse problem.");
                Log.fine(xml);
            }

            for (final XmlContentError err : source.getAllErrors()) {
                Log.warning("    ", err);
            }

        } catch (final ParsingException ex) {
            problem = new ProblemDummyTemplate();
            Log.warning(ex);
        }

        return problem;
    }

    /**
     * Sets the type value on all "derived" and "random choice" variables.
     *
     * @param problem the problem
     * @param errors  a list to which to add error messages
     * @return true if all variable types can be determined
     */
    public static boolean setVariableTypes(final AbstractProblemTemplate problem,
                                           final Collection<? super String> errors) {

        boolean ok = true;

        // We realize 10 times since a variable of "REAL" type could generate a Long value for some
        // parameter choices, and we want to catch if it returns Double sometimes

        for (int i = 0; i < 10; ++i) {
            if (problem.evalContext.generate(problem.id)) {
                for (final AbstractVariable var : problem.evalContext.getVariables()) {

                    if (var instanceof VariableDerived) {
                        if (var.hasValue()) {
                            final Object value = var.getValue();

                            EType newType = null;
                            switch (value) {
                                case final Boolean b -> newType = EType.BOOLEAN;
                                case final Long l -> newType = EType.INTEGER;
                                case final Double v -> newType = EType.REAL;
                                case final DocSimpleSpan docSimpleSpan -> newType = EType.SPAN;
                                case final ErrorValue errorValue -> {
                                    newType = EType.ERROR;
                                    errors.add("Variable {" + var.name + "} generated ErrorValue");
                                }
                                case null, default -> errors.add("Unexpected value type for {" + var.name + "}: "
                                                                 + value.getClass().getSimpleName());
                            }

                            if (newType != null) {
                                if (var.type == null || var.type == EType.ERROR) {
                                    var.type = newType;
                                } else if (newType == EType.REAL && var.type == EType.INTEGER) {
                                    // Log.info("NOTE: Variable {", var.name, "} returned Long then Double");
                                    var.type = EType.REAL;
                                }
                            }
                        } else {
                            errors.add("Variable {" + var.name + "} had no value after generate");
                        }
                    }

                    if (var instanceof final VariableRandomChoice choiceVar) {

                        boolean allNumeric = true;
                        boolean hasReal = false;
                        final Formula[] choices = choiceVar.getChooseFromList();
                        if (choices != null) {
                            for (final Formula formula : choices) {
                                final EType choiceType = formula.getType(problem.evalContext);

                                if (choiceType == EType.REAL) {
                                    hasReal = true;
                                } else if (choiceType != EType.INTEGER) {
                                    allNumeric = false;
                                }
                            }
                        }

                        if (allNumeric) {
                            final EType newType = hasReal ? EType.REAL : EType.INTEGER;

                            if (var.type == null || var.type == EType.ERROR) {
                                var.type = newType;
                            } else if (newType == EType.REAL && var.type == EType.INTEGER) {
                                // Log.info("NOTE: Variable {", var.name, "} returned Long then Double");
                                var.type = EType.REAL;
                            }
                        } else if (var.hasValue()) {
                            final Object value = var.getValue();

                            EType newType = null;
                            switch (value) {
                                case final Boolean b -> newType = EType.BOOLEAN;
                                case final Long l -> newType = EType.INTEGER;
                                case final Double v -> newType = EType.REAL;
                                case final DocSimpleSpan docSimpleSpan -> newType = EType.SPAN;
                                case final ErrorValue errorValue -> {
                                    newType = EType.ERROR;
                                    errors.add("Variable {" + var.name + "} generated ErrorValue");
                                }
                                case null, default -> errors.add("Unexpected value type for {" + var.name + "}: "
                                                                 + value.getClass().getSimpleName());
                            }

                            if (newType != null) {
                                if (var.type == null || var.type == EType.ERROR) {
                                    var.type = newType;
                                } else if (newType == EType.REAL && var.type == EType.INTEGER) {
                                    // Log.info("NOTE: Variable {", var.name, "} returned Long then Double");
                                    var.type = EType.REAL;
                                }
                            }
                        } else {
                            errors.add("Variable {" + var.name + "} had no value after generate");
                        }
                    }
                }
            } else {
                errors.add("Unable to generate loaded problem");
                ok = false;
            }
        }

        for (final AbstractVariable var : problem.evalContext.getVariables()) {

            if (var instanceof VariableInputInteger || var instanceof VariableInputReal) {
                continue;
            }

            if (var.type == null) {
                errors.add("Type of {" + var.name + "} was null");
                ok = false;
            } else if (var.type == EType.ERROR) {
                errors.add("Type of " + var.getClass().getSimpleName() + " {" + var.name
                        + "} was ERROR with value " + var.getValue());
                ok = false;
            }
            var.clearDerivedValues();
        }

        return ok;
    }

    /**
     * Generates the {@code Problem} object from the source XML. Any errors encountered are logged in the
     * {@code XmlSource} object.
     *
     * @param content the {@code XmlContent} containing the source XML
     * @param mode    the parser mode
     * @return the loaded {@code Problem}, or null on any error
     */
    private static AbstractProblemTemplate createFromSource(final XmlContent content, final EParserMode mode) {

        AbstractProblemTemplate problem = null;

        final IElement top = content.getTopLevel();

        if (top instanceof final NonemptyElement nonempty) {
            final String tagName = top.getTagName();

            switch (tagName) {
                case "problem" -> problem = parseFromProblemElement(nonempty, mode);
                case "problem-multiple-choice" -> problem = parseFromProblemMultipleChoiceElement(nonempty, mode);
                case "problem-multiple-selection" -> problem = parseFromProblemMultipleSelectionElement(nonempty, mode);
                case "problem-numeric" -> problem = parseFromProblemNumericElement(nonempty, mode);
                case "problem-embedded-input" -> problem = parseFromProblemEmbeddedInputElement(nonempty, mode);
                case "problem-auto-correct" -> problem = parseAutocorrectProblem();
                case null, default -> content.logError(top, "Unrecognized top-level element: " + tagName);
            }
        } else {
            final ICharSpan source = Objects.requireNonNullElseGet(top, () -> new CharSpan(0, 0, 1, 1));
            content.logError(source, "Problem must be defined in a nonempty top-level element.");
        }

        return problem;
    }

    /**
     * Generates the Problem object from a nonempty "problem" element. Any errors encountered are logged in the
     * element.
     *
     * @param nonempty the {@code NonemptyElement} from which to parse
     * @param mode     the parser mode
     * @return the loaded {@code Problem}, or null on any error
     */
    public static AbstractProblemTemplate parseFromProblemElement(final NonemptyElement nonempty,
                                                                  final EParserMode mode) {

        AbstractProblemTemplate problem = null;

        final String tagName = nonempty.getTagName();

        if ("problem".equals(tagName)) {

            final String problemType = nonempty.getStringAttr("type");

            if (problemType == null) {
                nonempty.logError("&lt;problem&gt; element missing required 'type' attribute..");
            } else if ("numeric".equalsIgnoreCase(problemType)) {
                if (mode.reportDeprecated) {
                    nonempty.logError("Deprecated &lt;problem type='numeric'&gt; tag; use &lt;problem-numeric&gt;");
                }
                problem = parseNumericProblem(nonempty, mode);
            } else if ("multiplechoice".equalsIgnoreCase(problemType)) {
                if (mode.reportDeprecated) {
                    nonempty.logError("Deprecated &lt;problem type='multiplechoice'&gt; tag; "
                            + "use &lt;problem-multiple-choice&gt;");
                }
                problem = parseMultipleChoiceProblem(nonempty, mode);
            } else if ("multipleselection".equalsIgnoreCase(problemType)) {
                if (mode.reportDeprecated) {
                    nonempty.logError("Deprecated &lt;problem type='multipleselection'&gt; tag; "
                            + "use &lt; problem-multiple-selection&gt;");
                }
                problem = parseMultipleSelectionProblem(nonempty, mode);
            } else if ("embeddedinput".equalsIgnoreCase(problemType)) {
                if (mode.reportDeprecated) {
                    nonempty.logError(
                            "Deprecated &lt;problem type='embeddedinput'&gt; tag; use &lt; problem-embedded-input&gt;");
                }
                problem = parseEmbeddedInputProblem(nonempty, mode);
            } else if ("autocorrect".equalsIgnoreCase(problemType)) {
                if (mode.reportDeprecated) {
                    nonempty.logError(
                            "Deprecated &lt;problem type='autocorrect'&gt; tag; use &lt;problem-auto-correct&gt;");
                }
                problem = parseAutocorrectProblem();
            } else {
                nonempty.logError("Invalid 'type' attribute on &lt;problem&gt; element: " + problemType);
            }

            if (problem != null &&
                    (!parseCalculatorAttr(problem, nonempty) || !parseCompletedAttr(problem, nonempty))) {
                problem = null;
            }
        } else {
            nonempty.logError("Attempt to extract problem from '" + tagName
                    + "' element (expected &lt;problem&gt; element)");
        }

        return problem;
    }

    /**
     * Generates a ProblemMultipleChoiceTemplate object from a nonempty "problem-multiple-choice" element. Any errors
     * encountered are logged in the element.
     *
     * @param nonempty the {@code NonemptyElement} from which to parse
     * @param mode     the parser mode
     * @return the loaded {@code Problem}, or null on any error
     */
    public static ProblemMultipleChoiceTemplate parseFromProblemMultipleChoiceElement(final NonemptyElement nonempty,
                                                                                      final EParserMode mode) {

        final ProblemMultipleChoiceTemplate parsed = parseMultipleChoiceProblem(nonempty, mode);

        return parsed == null || (parseCalculatorAttr(parsed, nonempty) && parseCompletedAttr(parsed, nonempty))
                ? parsed : null;
    }

    /**
     * Generates a ProblemMultipleSelectionTemplate object from a nonempty "problem-multiple-selection" element. Any
     * errors encountered are logged in the element.
     *
     * @param nonempty the {@code NonemptyElement} from which to parse
     * @param mode     the parser mode
     * @return the loaded {@code Problem}, or null on any error
     */
    public static ProblemMultipleSelectionTemplate parseFromProblemMultipleSelectionElement(
            final NonemptyElement nonempty, final EParserMode mode) {

        final ProblemMultipleSelectionTemplate parsed = parseMultipleSelectionProblem(nonempty, mode);

        return parsed == null || (parseCalculatorAttr(parsed, nonempty) && parseCompletedAttr(parsed, nonempty))
                ? parsed : null;
    }

    /**
     * Generates a ProblemNumericTemplate object from a nonempty "problem-numeric" element. Any errors encountered are
     * logged in the element.
     *
     * @param nonempty the {@code NonemptyElement} from which to parse
     * @param mode     the parser mode
     * @return the loaded {@code Problem}, or null on any error
     */
    public static ProblemNumericTemplate parseFromProblemNumericElement(final NonemptyElement nonempty,
                                                                        final EParserMode mode) {

        final ProblemNumericTemplate parsed = parseNumericProblem(nonempty, mode);

        return parsed == null || (parseCalculatorAttr(parsed, nonempty) && parseCompletedAttr(parsed, nonempty))
                ? parsed : null;
    }

    /**
     * Generates a ProblemEmbeddedInputTemplate object from a nonempty "problem-embedded-input" element. Any errors
     * encountered are logged in the element.
     *
     * @param nonempty the {@code NonemptyElement} from which to parse
     * @param mode     the parser mode
     * @return the loaded {@code Problem}, or null on any error
     */
    public static ProblemEmbeddedInputTemplate parseFromProblemEmbeddedInputElement(final NonemptyElement nonempty,
                                                                                    final EParserMode mode) {

        final ProblemEmbeddedInputTemplate parsed = parseEmbeddedInputProblem(nonempty, mode);

        return parsed == null || (parseCalculatorAttr(parsed, nonempty) && parseCompletedAttr(parsed, nonempty))
                ? parsed : null;
    }

    /**
     * Parses and interprets the "calculator" attribute on a problem element.
     *
     * @param problem  the loaded problem
     * @param nonempty the {@code NonemptyElement} from which to parse
     * @return true if successful; false if an error occurred
     */
    private static boolean parseCalculatorAttr(final AbstractProblemTemplate problem,
                                               final NonemptyElement nonempty) {

        boolean ok = true;

        final String calculatorStr = nonempty.getStringAttr("calculator");

        if (calculatorStr == null) {
            problem.calculator = ECalculatorType.FULL_CALC;
        } else {
            problem.calculator = ECalculatorType.forLabel(calculatorStr);
            if (problem.calculator == null) {
                nonempty.logError("Calculator attribute on problem tag must be one of 'none', "
                        + "'basic', 'scientific', 'graphing', or 'full'.");
                ok = false;
            }
        }

        return ok;
    }

    /**
     * Parses and interprets the "completed" attribute on a problem element.
     *
     * @param problem  the loaded problem
     * @param nonempty the {@code NonemptyElement} from which to parse
     * @return true if successful; false if an error occurred
     */
    private static boolean parseCompletedAttr(final AbstractProblemTemplate problem,
                                              final NonemptyElement nonempty) {

        boolean ok = true;

        final String completedStr = nonempty.getStringAttr("completed");

        if (completedStr != null) {
            try {
                problem.completionTime = Long.parseLong(completedStr);
            } catch (final NumberFormatException ex) {
                nonempty.logError("Completed attribute on problem tag must be valid timestamp");
                ok = false;
            }
        }

        return ok;
    }

    /**
     * Generates a {@code ProblemNumeric} object from the source XML. Any errors encountered are logged in the
     * {@code XmlSource} object.
     *
     * @param elem the element
     * @param mode the parser mode
     * @return the {@code ProblemNumeric}, or null on any error
     */
    private static ProblemNumericTemplate parseNumericProblem(final NonemptyElement elem, final EParserMode mode) {

        final ProblemNumericTemplate problem = new ProblemNumericTemplate();

        return parseCommonElements(elem, problem, mode) && parseStudentStringAnswer(elem, problem)
                && parseAcceptNumber(elem, problem, mode) ? problem : null;
    }

    /**
     * Generates a {@code ProblemMultipleChoice} object from the source XML. Any errors encountered are logged in the
     * {@code XmlSource} object.
     *
     * @param elem the element
     * @param mode the parser mode
     * @return the {@code ProblemMultipleChoice}, or {@code null} on any error
     */
    private static ProblemMultipleChoiceTemplate parseMultipleChoiceProblem(final NonemptyElement elem,
                                                                            final EParserMode mode) {

        final ProblemMultipleChoiceTemplate problem = new ProblemMultipleChoiceTemplate();

        return parseCommonElements(elem, problem, mode)
                && parseNumChoices(elem, problem, mode)
                && parseRandomOrder(elem, problem, mode)
                && parseChoiceOrder(elem, problem)
                && parseStudentChoices(elem, problem)
                && parseChoices(elem, problem, mode) ? problem : null;
    }

    /**
     * Generates a {@code ProblemMultipleSelection} object from the source XML. Any errors encountered are logged in the
     * {@code XmlSource} object.
     *
     * @param elem the element
     * @param mode the parser mode
     * @return the {@code ProblemMultipleSelection}, or {@code null} on any error
     */
    private static ProblemMultipleSelectionTemplate parseMultipleSelectionProblem(final NonemptyElement elem,
                                                                                  final EParserMode mode) {

        final ProblemMultipleSelectionTemplate problem = new ProblemMultipleSelectionTemplate();

        return parseCommonElements(elem, problem, mode)
                && parseNumChoices(elem, problem, mode)
                && parseRandomOrder(elem, problem, mode)
                && parseMinCorrect(elem, problem, mode)
                && parseMaxCorrect(elem, problem, mode)
                && parseChoiceOrder(elem, problem)
                && parseStudentChoices(elem, problem)
                && parseChoices(elem, problem, mode) ? problem : null;
    }

    /**
     * Generates a {@code ProblemNumeric} object from the source XML. Any errors encountered are logged in the
     * {@code XmlSource} object.
     *
     * @param elem the element
     * @param mode the parser mode
     * @return the {@code ProblemNumeric}, or null on any error
     */
    private static ProblemEmbeddedInputTemplate parseEmbeddedInputProblem(final NonemptyElement elem,
                                                                          final EParserMode mode) {

        ProblemEmbeddedInputTemplate problem = new ProblemEmbeddedInputTemplate();

        if (!parseCommonElements(elem, problem, mode) || !parseCorrect(elem, problem, mode)
                || !parseAnswer(elem, problem, mode)) {
            problem = null;
        } else if (problem.completionTime != 0L) {
            // Make sure any student answers that came along are posted.
            final Serializable[] inputs = problem.question.getInputValues();
            if (inputs != null && inputs.length > 0) {
                problem.recordAnswer(inputs);
            }
        }

        return problem;
    }

    /**
     * Generates a {@code ProblemAutoCorrect} object from the source XML. Any errors encountered are logged in the
     * {@code XmlSource} object.
     *
     * @return the {@code ProblemMultipleSelection}, or {@code null} on any error
     */
    public static ProblemAutoCorrectTemplate parseAutocorrectProblem() {

        return new ProblemAutoCorrectTemplate(2);
    }

    /**
     * Parses elements common to all {@code Problem} classes from source XML. Any errors encountered are logged in the
     * {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code Problem} to populate with the parsed data
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseCommonElements(final NonemptyElement elem, final AbstractProblemTemplate problem,
                                               final EParserMode mode) {

        return parseProblemId(elem, problem, mode)
                && VariableFactory.parseVars(problem.evalContext, elem, mode)
                && parseQuestion(elem, problem, mode)
                && parseSolution(elem, problem, mode)
                && parseStudentResponse(elem, problem)
                && parseScore(elem, problem);
    }

    /**
     * Parses the 'ref-base' element from source XML. Any errors encountered are logged in the {@code XmlSource}
     * object.
     *
     * @param elem    the element
     * @param problem the {@code Problem} to populate with the parsed data
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseProblemId(final NonemptyElement elem, final AbstractProblemTemplate problem,
                                          final EParserMode mode) {

        problem.id = elem.getStringAttr("id");

        if (problem.id == null) {
            problem.id = getRefElement("ref-base", elem, true);
            if (mode.reportDeprecated) {
                elem.logError("Deprecated <ref-base> element; use id attribute on problem element>.");
            }
        }

        return problem.id != null;
    }

    /**
     * Parses the 'question' element from source XML. Any errors encountered are logged in the {@code XmlSource}
     * object.
     *
     * @param elem    the element
     * @param problem the {@code Problem} to populate with the parsed data
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseQuestion(final NonemptyElement elem, final AbstractProblemTemplate problem,
                                         final EParserMode mode) {

        boolean valid = true;

        for (final IElement child : elem.getElementChildrenAsList()) {
            if (child instanceof final NonemptyElement nonempty && "question".equals(child.getTagName())) {

                final DocColumn question = DocFactory.parseDocColumn(problem.evalContext, nonempty, mode);
                if (question == null) {
                    valid = false;
                } else if (problem.question != null) {
                    elem.logError("Multiple &lt;question&gt; elements found.");
                    valid = false;
                } else {
                    problem.question = question;

                    // Bind all inputs in the question to the problem's evaluation context.

                    final EvalContext ec = problem.evalContext;
                    for (final AbstractDocInput input : question.getInputs()) {
                        input.bind(ec);
                    }

                    final Set<String> varNames = new HashSet<>(20);
                    question.accumulateParameterNames(varNames);
                    for (final String varName : varNames) {
                        if (ec.getVariable(varName) == null) {
                            nonempty.logError("Document references nonexistent variable {" + varName + "}.");
                        }
                    }
                }
            }
        }

        return valid;
    }

    /**
     * Parses the 'solution' element from source XML. Any errors encountered are logged in the {@code XmlSource}
     * object.
     *
     * @param elem    the element
     * @param problem the {@code Problem} to populate with the parsed data
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseSolution(final NonemptyElement elem, final AbstractProblemTemplate problem,
                                         final EParserMode mode) {

        boolean valid = true;

        for (final IElement child : elem.getElementChildrenAsList()) {
            final String tagName = child.getTagName();
            if (child instanceof final NonemptyElement nonempty && "solution".equals(tagName)) {

                final DocColumn solution = DocFactory.parseDocColumn(problem.evalContext, nonempty, mode);
                if (solution == null) {
                    valid = false;
                } else if (problem.solution != null) {
                    elem.logError("Multiple &lt;solution&gt; elements found.");
                    valid = false;
                } else {
                    problem.solution = solution;

                    final Set<String> varNames = new HashSet<>(20);
                    solution.accumulateParameterNames(varNames);
                    for (final String varName : varNames) {
                        if (problem.evalContext.getVariable(varName) == null) {
                            elem.logError("Document references nonexistent variable {" + varName + "}.");
                        }
                    }
                }
            }
        }

        return valid;
    }

    /**
     * Parses the 'student-response' element from source XML. Any errors encountered are logged in the {@code XmlSource}
     * object.
     *
     * @param elem    the element
     * @param problem the {@code Problem} to populate with the parsed data
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseStudentResponse(final NonemptyElement elem, final AbstractProblemTemplate problem) {

        for (final IElement child : elem.getElementChildrenAsList()) {
            final String tagName = child.getTagName();
            if (child instanceof final NonemptyElement nonempty && "student-response".equals(tagName)) {

                final Collection<Object> values = new ArrayList<>(10);

                for (final IElement grandchild : nonempty.getElementChildrenAsList()) {

                    if (grandchild instanceof final NonemptyElement value && value.getNumChildren() == 1
                            && value.getChild(0) instanceof final CData cdata) {

                        final String content = cdata.content;
                        final String valueTag = value.getTagName();

                        switch (valueTag) {
                            case "long" -> {
                                try {
                                    values.add(Long.valueOf(content));
                                } catch (final NumberFormatException ex) {
                                    Log.warning(ex);
                                    elem.logError("Invalid content of &lt;long&gt; element");
                                }
                            }
                            case "double" -> {
                                try {
                                    values.add(Double.valueOf(content));
                                } catch (final NumberFormatException ex) {
                                    Log.warning(ex);
                                    elem.logError("Invalid content of &lt;double&gt; element");
                                }
                            }
                            case "string" -> values.add(content);
                            case null, default -> elem.logError("Unrecognized response type: " + value.getTagName());
                        }
                    }
                }

                problem.setStudentResponse(values.toArray(EMPTY_OBJECT_ARRAY));
            }
        }

        return true;
    }

    /**
     * Parses the 'score' element from source XML. Any errors encountered are logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code Problem} to populate with the parsed data
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseScore(final NonemptyElement elem, final AbstractProblemTemplate problem) {

        for (final IElement child : elem.getElementChildrenAsList()) {
            final String tagName = child.getTagName();
            if (child instanceof final NonemptyElement nonempty && "score".equals(tagName)
                    && nonempty.getNumChildren() == 1 && nonempty.getChild(0) instanceof final CData cdata) {

                final String content = cdata.content;
                try {
                    problem.score = Double.parseDouble(content);
                } catch (final NumberFormatException ex) {
                    Log.warning(ex);
                    elem.logError("Invalid content of <score> element");
                }
            }
        }

        return true;
    }

    /**
     * Parses the 'student-string-answer' attribute of a {@code ProblemNumeric} from a source XML element. Any errors
     * encountered are logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemNumeric} to populate with the result
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseStudentStringAnswer(final IElement elem, final ProblemNumericTemplate problem) {

        problem.stringAnswer = elem.getStringAttr("student-string-answer");

        return true;
    }

    /**
     * Parses the 'accept-number' element from source XML. Any errors encountered are logged in the {@code XmlSource}
     * object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemNumeric} to populate with the result
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseAcceptNumber(final NonemptyElement elem, final ProblemNumericTemplate problem,
                                             final EParserMode mode) {

        boolean valid = true;
        int count = 0;

        ProblemAcceptNumberTemplate acceptNumber = null;

        for (final IElement child : elem.getElementChildrenAsList()) {

            final String tagName = child.getTagName();
            if ("accept-number".equals(tagName)) {
                ++count;
                if (count > 1) {
                    elem.logError("Multiple &lt;accept-number&gt; elements.");
                    valid = false;
                } else {
                    acceptNumber = new ProblemAcceptNumberTemplate();

                    final String typeStr = child.getStringAttr("type");
                    if (typeStr == null) {
                        elem.logError("Missing 'type' attribute on accept-number.");
                        valid = false;
                    } else if ("integer".equalsIgnoreCase(typeStr)) {
                        acceptNumber.forceInteger = true;
                    } else if (!"real".equalsIgnoreCase(typeStr)) {
                        elem.logError("Invalid 'type' attribute on accept-number.");
                        valid = false;
                    }

                    Number varianceConstant = null;
                    Formula varianceFormula = null;

                    if (!acceptNumber.forceInteger) {
                        final String varianceStr = child.getStringAttr("variance");
                        if (varianceStr == null) {
                            if (child instanceof final NonemptyElement nonempty) {

                                if (nonempty.getNumChildren() == 1) {
                                    final INode innerChild = nonempty.getChild(0);

                                    if (innerChild instanceof final CData cdata) {
                                        if (mode.reportDeprecated) {
                                            elem.logError("Deprecated 'variance' text formula on accept-number");
                                        }

                                        varianceFormula = FormulaFactory.parseFormulaString(problem.evalContext,
                                                cdata.content,
                                                mode);
                                    } else if (innerChild instanceof final NonemptyElement varInner) {
                                        final String innerTag = varInner.getTagName();
                                        if ("variance".equals(innerTag)) {
                                            varianceFormula = XmlFormulaFactory.extractFormula(problem.evalContext,
                                                    varInner, mode);
                                        } else {
                                            elem.logError("Unsupported '" + innerTag + "' tag in accept-number.");
                                        }
                                    } else {
                                        elem.logError("Unsupported child in accept-number.");
                                    }
                                }

                                if (varianceFormula == null) {
                                    elem.logError("Invalid &lt;variance&gt; child element on problem.");
                                    valid = false;
                                }
                            }
                        } else {
                            try {
                                varianceConstant = NumberParser.parse(varianceStr);
                            } catch (final NumberFormatException ex) {
                                elem.logError("Invalid 'variance' attribute on accept-number.");
                                valid = false;
                            }
                        }
                    }

                    Formula correct = null;

                    final String correctStr = child.getStringAttr("correct-answer");
                    if (correctStr == null && child instanceof final NonemptyElement nonemptychild) {
                        for (final IElement grandchild : nonemptychild.getElementChildrenAsList()) {
                            final String tagName1 = grandchild.getTagName();
                            if (grandchild instanceof final NonemptyElement inner
                                    && "correct-answer".equals(tagName1)) {

                                if (inner.getNumChildren() == 1 && inner.getChild(0) instanceof final CData cdata) {
                                    if (mode.reportDeprecated) {
                                        elem.logError("Deprecated 'correct-answer' text formula on accept-number");
                                    }

                                    correct = FormulaFactory.parseFormulaString(problem.evalContext, cdata.content,
                                            mode);
                                    if (correct == null) {
                                        elem.logError("Invalid <correct-answer> child element on problem.");
                                        valid = false;
                                    }
                                } else {
                                    correct = XmlFormulaFactory.extractFormula(problem.evalContext, inner, mode);
                                    if (correct == null) {
                                        elem.logError("Invalid &lt;correct-answer&gt; child element on problem.");
                                        valid = false;
                                    }
                                }
                            }
                        }
                    } else {
                        correct = FormulaFactory.parseFormulaString(problem.evalContext, correctStr, mode);
                        if (correct == null) {
                            elem.logError("Invalid 'correct-answer' attribute on accept-number.");
                            valid = false;
                        }
                    }

                    if (valid) {
                        acceptNumber.varianceConstant = varianceConstant;
                        acceptNumber.varianceFormula = varianceFormula;
                        acceptNumber.correctAnswer = correct;
                    }
                }
            }
        }

        if (valid) {
            problem.acceptNumber = acceptNumber;
        }

        return valid;
    }

    /**
     * Parses the 'num-choices' attribute on a multiple choice problem from source XML. Any errors encountered are
     * logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemMultipleChoice} to populate with the result
     * @param mode    the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseNumChoices(final NonemptyElement elem,
                                           final AbstractProblemMultipleChoiceTemplate problem,
                                           final EParserMode mode) {

        boolean valid = true;

        // FIXME: formulas should not be in attributes - but constants can

        final String numChoicesStr = elem.getStringAttr("num-choices");

        if (numChoicesStr == null) {
            for (final IElement child : elem.getElementChildrenAsList()) {
                final String tagName = child.getTagName();
                if (child instanceof final NonemptyElement nonempty && "num-choices".equals(tagName)) {

                    if (nonempty.getNumChildren() == 1 && nonempty.getChild(0) instanceof final CData cdata) {

                        problem.numChoices = FormulaFactory.parseFormulaString(problem.evalContext, cdata.content,
                                mode);
                    } else {
                        problem.numChoices = XmlFormulaFactory.extractFormula(problem.evalContext, nonempty, mode);
                    }
                    if (problem.numChoices == null) {
                        elem.logError("Invalid &lt;num-choices&gt; child element on problem.");
                        valid = false;
                    }
                }
            }

        } else {
            problem.numChoices = FormulaFactory.parseFormulaString(problem.evalContext, numChoicesStr, mode);
            if (problem.numChoices == null) {
                elem.logError("Invalid 'num-choices' attribute on accept-number.");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Parses the 'random-order' attribute on a multiple choice problem from source XML. Any errors encountered are
     * logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemMultipleChoice} to populate with the result
     * @param mode    the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseRandomOrder(final NonemptyElement elem,
                                            final AbstractProblemMultipleChoiceTemplate problem,
                                            final EParserMode mode) {

        boolean valid = true;

        final String numChoicesStr = elem.getStringAttr("random-order");

        if (numChoicesStr == null) {

            for (final IElement child : elem.getElementChildrenAsList()) {
                final String tagName = child.getTagName();
                if (child instanceof final NonemptyElement nonempty && "random-order".equals(tagName)) {

                    if (nonempty.getNumChildren() == 1 && nonempty.getChild(0) instanceof final CData cdata) {

                        problem.randomOrderChoices = FormulaFactory.parseFormulaString(problem.evalContext,
                                cdata.content, mode);
                    } else {
                        problem.randomOrderChoices = XmlFormulaFactory.extractFormula(problem.evalContext, nonempty,
                                mode);
                    }
                    if (problem.randomOrderChoices == null) {
                        elem.logError("Invalid &lt;random-order&gt; child element on problem.");
                        valid = false;
                    }
                }
            }

        } else {
            problem.randomOrderChoices = FormulaFactory.parseFormulaString(problem.evalContext, numChoicesStr, mode);
            if (problem.randomOrderChoices == null) {
                elem.logError("Invalid 'random-order' attribute on accept-number.");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Parses the 'maxCorrect' attribute on a multiple selection problem from source XML. Any errors encountered are
     * logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemMultipleSelection} to populate with the result
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseChoiceOrder(final NonemptyElement elem,
                                            final AbstractProblemMultipleChoiceTemplate problem) {

        boolean valid = true;

        final String choiceOrderStr = elem.getStringAttr("choice-order");

        if (choiceOrderStr != null) {
            final String[] list = choiceOrderStr.split(CoreConstants.COMMA);

            final int[] order = new int[list.length];
            final int listLen = list.length;
            for (int i = 0; i < listLen; ++i) {
                try {
                    order[i] = Long.valueOf(list[i]).intValue();
                } catch (final NumberFormatException ex) {
                    elem.logError("Choice order must be a list of integers.");
                    valid = false;
                }
            }

            if (valid) {
                problem.choiceOrder = order;
            }
        }

        return valid;
    }

    /**
     * Parses the attributes storing the choice(s) selected by the student from the source XML. Any errors encountered
     * are logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemNumeric} to populate with the result
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseStudentChoices(final NonemptyElement elem, final ProblemTemplateInt problem) {

        boolean valid = true;

        final String choiceOrderStr = elem.getStringAttr("student-choices");

        if (choiceOrderStr != null) {
            final String[] list = choiceOrderStr.split(CoreConstants.COMMA);
            final Object[] answer = new Object[list.length];
            final int ansLen = answer.length;
            try {
                for (int i = 0; i < ansLen; ++i) {
                    answer[i] = Long.valueOf(list[i]);
                }
                problem.recordAnswer(answer);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'student-choices' attribute on problem.");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Parses the 'choice' element from source XML. Any errors encountered are logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemMultipleChoice} to populate with the result
     * @param mode    the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseChoices(final NonemptyElement elem, final AbstractProblemMultipleChoiceTemplate problem,
                                        final EParserMode mode) {

        final List<ProblemChoiceTemplate> choices = parseChoiceValues(elem, problem, mode);

        if (choices != null) {
            for (final ProblemChoiceTemplate choice : choices) {
                problem.addChoice(choice);
            }
        }

        return choices != null;
    }

    /**
     * Parses the 'choice' element from source XML. Any errors encountered are logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the problem
     * @param mode    the parser mode
     * @return the list of parsed choices, or {@code null} on any error
     */
    private static List<ProblemChoiceTemplate> parseChoiceValues(final NonemptyElement elem,
                                                                 final AbstractProblemMultipleChoiceTemplate problem,
                                                                 final EParserMode mode) {

        // FIXME: This has a Formula in attribute - need to wrap the children content in a
        //  <doc> tag and allow the "correct" formula to be a child element

        List<ProblemChoiceTemplate> choices = new ArrayList<>(5);

        for (final IElement child : elem.getElementChildrenAsList()) {
            final String tagName = child.getTagName();
            if (child instanceof final NonemptyElement nonempty && "choice".equals(tagName)) {

                // Old format: <p> elements are children
                // New format: <correct> formula as child, <content> child with <p> elements
                boolean newFormat = false;
                for (final IElement grandchild : nonempty.getElementChildrenAsList()) {
                    final String tag = grandchild.getTagName();
                    if ("correct".equals(tag) || "content".equals(tag)) {
                        newFormat = true;
                    }
                }

                if (!newFormat) {
                    if (mode.reportDeprecated) {
                        nonempty.logError("Deprecated format for &lt;choice&gt;");
                    }
                }

                final String idStr = nonempty.getStringAttr("id");
                final String positionStr = nonempty.getStringAttr("position");

                boolean valid = true;
                int choiceId = 0;
                Formula correct = null;
                DocColumn doc = null;
                int pos = 0;

                if (idStr == null) {
                    nonempty.logError("Missing 'id' attribute.");
                    valid = false;
                } else {
                    try {
                        choiceId = Integer.parseInt(idStr);
                    } catch (final NumberFormatException ex) {
                        nonempty.logError("'id' attribute on choice tag is not an integer.");
                        valid = false;
                    }
                }

                if (positionStr != null) {
                    try {
                        pos = Integer.parseInt(positionStr);
                    } catch (final NumberFormatException ex) {
                        nonempty.logError("'position' attribute on choice tag is not an integer.");
                        valid = false;
                    }
                }

                if (valid) {
                    if (newFormat) {
                        final String correctStr = nonempty.getStringAttr("correct");

                        if ("TRUE".equalsIgnoreCase(correctStr)) {
                            correct = new Formula(new ConstBooleanValue(Boolean.TRUE));
                        } else if ("FALSE".equalsIgnoreCase(correctStr)) {
                            correct = new Formula(new ConstBooleanValue(Boolean.FALSE));
                        } else if (correctStr != null) {
                            correct = FormulaFactory.parseFormulaString(problem.evalContext, correctStr, mode);
                            if (correct == null) {
                                elem.logError("Invalid &lt;correct&gt; attribute value in &lt;choice&gt;.");
                                valid = false;
                            } else if (mode.reportDeprecated) {
                                nonempty.logError("Non-constant correctness formula defined as attribute on choice; "
                                        + "should be in <correct> child element");
                            }
                        }

                        for (final IElement grandchild : nonempty.getElementChildrenAsList()) {
                            final String tag = grandchild.getTagName();

                            if (grandchild instanceof final NonemptyElement nonempty2) {
                                if ("correct".equals(tag)) {

                                    if (mode.reportDeprecated && correct != null) {
                                        nonempty.logError("Correctness defined both as attribute and child element in" +
                                                " &lt;choice&gt;");
                                    }
                                    correct = XmlFormulaFactory.extractFormula(problem.evalContext, nonempty2, mode);

                                    if (correct == null) {
                                        elem.logError("Invalid &lt;correct&gt; child element in &lt;choice&gt;.");
                                        valid = false;
                                    }
                                } else if ("content".equals(tag)) {
                                    doc = DocFactory.parseDocColumn(problem.evalContext, nonempty2, mode);

                                    if (doc == null) {
                                        valid = false;
                                    } else {
                                        final Set<String> varNames = new HashSet<>(20);
                                        doc.accumulateParameterNames(varNames);
                                        for (final String varName : varNames) {
                                            if (problem.evalContext.getVariable(varName) == null) {
                                                elem.logError("Document references nonexistent variable {" + varName
                                                        + "}.");
                                            }
                                        }
                                    }
                                } else {
                                    nonempty.logError("Unexpected &lt;" + tag + "&gt; element in &lt;choice&gt;");
                                    valid = false;
                                }
                            } else {
                                nonempty.logError("Unexpected empty &lt;" + tag + "&gt; element in &lt;choice&gt;");
                                valid = false;
                            }
                        }

                    } else {
                        final String correctStr = nonempty.getStringAttr("correct");

                        if (correctStr != null) {
                            correct = FormulaFactory.parseFormulaString(problem.evalContext, correctStr, mode);
                            if (correct == null) {
                                nonempty.logError("Unable to parse 'correct' formula.");
                                valid = false;
                            }
                        }

                        doc = DocFactory.parseDocColumn(problem.evalContext, nonempty, mode);
                        if (doc == null) {
                            valid = false;
                        } else {
                            final Set<String> varNames = new HashSet<>(20);
                            doc.accumulateParameterNames(varNames);
                            for (final String varName : varNames) {
                                if (problem.evalContext.getVariable(varName) == null) {
                                    elem.logError("Document references nonexistent variable {" + varName + "}.");
                                }
                            }
                        }
                    }
                }

                if (valid) {
                    final ProblemChoiceTemplate choice = new ProblemChoiceTemplate();
                    choice.choiceId = choiceId;
                    choice.correct = correct;
                    choice.pos = pos;
                    choice.doc = doc;

                    choices.add(choice);
                }
            }
        }

        if (choices.isEmpty()) {
            elem.logError("No choices defined in the problem.");
            choices = null;
        }

        return choices;
    }

    /**
     * Parses the 'min-correct' attribute on a multiple selection problem from source XML. Any errors encountered are
     * logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemMultipleSelection} to populate with the result
     * @param mode    the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseMinCorrect(final NonemptyElement elem, final ProblemMultipleSelectionTemplate problem,
                                           final EParserMode mode) {

        boolean valid = true;

        final String minCorrectStr = elem.getStringAttr("min-correct");

        if (minCorrectStr == null) {
            for (final IElement child : elem.getElementChildrenAsList()) {
                final String tagName = child.getTagName();
                if (child instanceof final NonemptyElement nonempty && "min-correct".equals(tagName)) {

                    if (nonempty.getNumChildren() == 1 && nonempty.getChild(0) instanceof final CData cdata) {

                        problem.minCorrect = FormulaFactory.parseFormulaString(problem.evalContext, cdata.content,
                                mode);
                    } else {
                        problem.minCorrect = XmlFormulaFactory.extractFormula(problem.evalContext, nonempty, mode);
                    }
                    if (problem.minCorrect == null) {
                        elem.logError("Invalid &lt;min-correct&gt; child element on problem.");
                        valid = false;
                    }
                }
            }
        } else {
            problem.minCorrect = FormulaFactory.parseFormulaString(problem.evalContext, minCorrectStr, mode);
            if (problem.minCorrect == null) {
                elem.logError("Invalid 'min-correct' attribute on problem.");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Parses the 'max-correct' attribute on a multiple selection problem from source XML. Any errors encountered are
     * logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemMultipleSelection} to populate with the result
     * @param mode    the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseMaxCorrect(final NonemptyElement elem, final ProblemMultipleSelectionTemplate problem,
                                           final EParserMode mode) {

        boolean valid = true;

        final String maxCorrectStr = elem.getStringAttr("max-correct");

        if (maxCorrectStr == null) {
            for (final IElement child : elem.getElementChildrenAsList()) {
                final String tagName = child.getTagName();
                if (child instanceof final NonemptyElement nonempty && "max-correct".equals(tagName)) {

                    if (nonempty.getNumChildren() == 1 && nonempty.getChild(0) instanceof final CData cdata) {

                        problem.maxCorrect = FormulaFactory.parseFormulaString(problem.evalContext, cdata.content,
                                mode);
                    } else {
                        problem.maxCorrect = XmlFormulaFactory.extractFormula(problem.evalContext, nonempty, mode);
                    }
                    if (problem.maxCorrect == null) {
                        elem.logError("Invalid &lt;max-correct&gt; child element on problem.");
                        valid = false;
                    }
                }
            }
        } else {
            problem.maxCorrect = FormulaFactory.parseFormulaString(problem.evalContext, maxCorrectStr, mode);
            if (problem.maxCorrect == null) {
                elem.logError("Invalid 'max-correct' attribute on problem.");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Parses the 'correct' formula attribute on an embedded input problem from source XML. Any errors encountered are
     * logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemEmbeddedInput} to populate with the result
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseCorrect(final NonemptyElement elem, final ProblemEmbeddedInputTemplate problem,
                                        final EParserMode mode) {

        boolean valid = true;

        final String correctStr = elem.getStringAttr("correct");

        if (correctStr == null) {
            for (final IElement child : elem.getElementChildrenAsList()) {
                final String tagName = child.getTagName();
                if (child instanceof final NonemptyElement nonempty && "correct".equals(tagName)) {

                    if (nonempty.getNumChildren() == 1 && nonempty.getChild(0) instanceof final CData cdata) {
                        problem.correctness = FormulaFactory.parseFormulaString(problem.evalContext, cdata.content,
                                mode);
                    } else {
                        problem.correctness = XmlFormulaFactory.extractFormula(problem.evalContext, nonempty, mode);
                    }
                    if (problem.correctness == null) {
                        elem.logError("Invalid &lt;correct&gt; child element on problem.");
                        valid = false;
                    }
                }
            }
        } else {
            if (mode == EParserMode.NORMAL) {
                elem.logError("Deprecated 'correct' attribute on embedded input problem");
            }
            problem.correctness = FormulaFactory.parseFormulaString(problem.evalContext, correctStr, mode);
            if (problem.correctness == null) {
                elem.logError("Invalid 'correct' attribute on problem.");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Parses the 'answer' element from source XML. Any errors encountered are logged in the {@code XmlSource} object.
     *
     * @param elem    the element
     * @param problem the {@code ProblemEmbeddedInput} to populate with the parsed data
     * @param mode    the parser mode
     * @return {@code true} if successful, {@code false} on any error
     */
    private static boolean parseAnswer(final NonemptyElement elem, final ProblemEmbeddedInputTemplate problem,
                                       final EParserMode mode) {

        boolean valid = true;

        for (final IElement child : elem.getElementChildrenAsList()) {
            final String tagName = child.getTagName();
            if (child instanceof final NonemptyElement nonempty && "answer".equals(tagName)) {

                final DocColumn answer = DocFactory.parseDocColumn(problem.evalContext, nonempty, mode);
                if (answer == null) {
                    valid = false;
                } else if (problem.correctAnswer != null) {
                    elem.logError("Multiple &lt;answer&gt; elements found.");
                    valid = false;
                } else {
                    problem.correctAnswer = answer;

                    final Set<String> varNames = new HashSet<>(20);
                    answer.accumulateParameterNames(varNames);
                    for (final String varName : varNames) {
                        if (problem.evalContext.getVariable(varName) == null) {
                            elem.logError("Document references nonexistent variable {" + varName + "}.");
                        }
                    }
                }
            }
        }

        return valid;
    }

    /**
     * Parse a named reference element from source XML. Any errors encountered are logged in the {@code XmlSource}
     * object.
     *
     * @param name        The name of the tag to locate and extract
     * @param elem        the element
     * @param isMandatory True if it is an error not to find the tag.
     * @return the parsed reference if successful; null on any error.
     */
    private static String getRefElement(final String name, final NonemptyElement elem, final boolean isMandatory) {

        NonemptyElement found = null;
        int count = 0;
        for (final IElement child : elem.getElementChildrenAsList()) {
            final String tagName = child.getTagName();
            if (child instanceof final NonemptyElement nonempty && name.equals(tagName)) {
                found = nonempty;
                ++count;
            }
        }

        String tagValue = null;

        if (found == null) {
            if (isMandatory) {
                elem.logError("'" + name + "' reference is missing");
            }
        } else if (count > 1) {
            elem.logError("element contains multiple '" + name + "' child elements.");
        } else {
            final List<INode> children = found.getChildrenAsList();
            if (children.size() == 1 && children.getFirst() instanceof final CData cdata) {
                tagValue = cdata.content;

                if (tagValue.isBlank()) {
                    elem.logError("Reference is empty.");
                    tagValue = null;
                } else {
                    // See that it contains valid characters for references
                    // [a-z][A-Z][0-9][space]._-+=:~
                    final String validCharacters =
                            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._-+=:~ ";

                    final int tagValueLen = tagValue.length();
                    for (int i = 0; i < tagValueLen; ++i) {
                        final char ch = tagValue.charAt(i);
                        if (validCharacters.indexOf(ch) == -1) {
                            elem.logError("Invalid character in reference: " + tagValue
                                    + "\n(valid are [a-z] [A-Z] [0-9] [spc] . _ - + = : ~)");
                            tagValue = null;
                            break;
                        }
                    }
                }
            } else {
                elem.logError("'" + name + "' child must contain only text data");
            }
        }

        return tagValue;
    }
}
