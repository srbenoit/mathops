package dev.mathops.app.assessment.qualitycontrol;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.template.AbstractDocContainer;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.document.template.AbstractDocPrimitiveContainer;
import dev.mathops.assessment.document.template.AbstractDocSpanBase;
import dev.mathops.assessment.document.template.DocImage;
import dev.mathops.assessment.document.template.DocRadical;
import dev.mathops.assessment.document.template.DocRelativeOffset;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.document.template.DocText;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.problem.template.AbstractProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemAcceptNumberTemplate;
import dev.mathops.assessment.problem.template.ProblemChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemDummyTemplate;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleSelectionTemplate;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.IExcludableVariable;
import dev.mathops.assessment.variable.IRangedVariable;
import dev.mathops.assessment.variable.VariableDerived;
import dev.mathops.assessment.variable.VariableRandomChoice;
import dev.mathops.assessment.variable.VariableRandomSimpleAngle;
import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.XmlContent;
import dev.mathops.text.parser.xml.XmlContentError;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The actual quality control checks that are run.
 */
enum QualityControlChecks {
    ;

    /** The number of times each problem will be realized. */
    private static final int NUM_REALIZATIONS = 3;

    /**
     * Performs quality checks on a problem.
     *
     * @param report      the report being constructed
     * @param problemFile the problem file to scan
     * @param problem     the problem to check
     * @return the number of errors/warnings found
     */
    static int problemQualityChecks(final HtmlBuilder report, final File problemFile,
                                    final AbstractProblemTemplate problem) {

        return problemTest1(report, problemFile, problem)
               + problemTest2(report, problem)
               + problemTest3(report, problem)
               + problemTest4(report, problem)
               + problemTest5(report, problem)
               + problemTest6(report, problem)
               + problemTest7(report, problem)
               + problemTest8(report, problem)
               + problemTest9(report, problem)
               + problemTest10(report, problem);

        // TODO: all branches in "test" or "switch" formulas result in compatible value types.
        //  In particular, if any return SPAN values, all should.

        // TODO: SPAN variables that do not depend on any parameters; candidates for direct substitution in source file

        // TODO: font size on fractions other than 85%

        // TODO: font name usage

        // TODO: Graph formula whose domain is graph's window (unneeded minx/maxx)

        // TODO: Check for "grouping" usage in formulas

        // TODO: missing specification of domain variable in graph formula

        // TODO: empty paragraphs used as vertical spacing.

        // TODO: variable names that aren't valid Java identifiers.

        // TODO: &nbsp; used for horizontal spacing.

        // TODO: variables with constant values.

        // TODO: exact comparison of real values using '='.

        // TODO: real constants that are very close to multiples of PI

        // TODO: expressions with a number raised to power 2 - replace with multiplication by itself

        // TODO: deeply nested Tests might be candidate for SWITCH (if conditions all equate one variable to an integer
        //  value)

        // TODO: "] U [" or ") U (" or similar - suggest {\cup}

        // TODO: embedded input problem without answer

        // TODO: any problem without solution

        // TODO: use of unrecognized attributes or child elements or unrecognized attributes

        // TODO: constructions like "integer 1 divided by function sine of x" - suggest csc(x)

        // TODO: nested sums within sums (or any other N-ary operator) that can be simplified as N-ary

        // TODO: constructions like unary minus of a positive constant - use negative constant

        // TODO: constructions like adding or subtracting zero, multiplying or dividing by 1

        // TODO: constraints on multiple choice/selection on number of choices shown or min/max correct that are
        //  unnecessary based on the number of choices defined

        // TODO: child elements that replace attributes with an expression, but where that expression is a constant
    }

    /**
     * Check 1 for Problems: Does relative filename agree with reference?
     *
     * @param report      the report being constructed
     * @param problemFile the problem file to scan
     * @param problem     the problem to check
     * @return the number of errors/warnings found
     */
    private static int problemTest1(final HtmlBuilder report, final File problemFile,
                                    final AbstractProblemTemplate problem) {

        int count = 0;

        String absPath = problemFile.getAbsolutePath().replace('/', '.').replace('\\', '.');
        final int lastDot = absPath.lastIndexOf('.');
        if (lastDot != -1) {
            absPath = absPath.substring(0, lastDot);
        }
        final int mathIndex = absPath.indexOf("math.");
        final String relPath = mathIndex == -1 ? absPath : absPath.substring(mathIndex);

        if (!relPath.equals(problem.id)) {
            logOrange(report, "WARNING: &lt;ref-base&gt; was ", problem.id, " but relative path was ", relPath);
            ++count;
        }

        return count;
    }

    /**
     * Check 2 for Problems: Are there unused parameters in the problem?
     *
     * @param report  the report being constructed
     * @param problem the problem to check
     * @return the number of errors/warnings found
     */
    private static int problemTest2(final HtmlBuilder report, final AbstractProblemTemplate problem) {

        int count = 0;

        final Set<String> referenced = new HashSet<>(10);
        for (final AbstractVariable test : problem.evalContext.getVariables()) {

            if (test instanceof final VariableDerived der) {
                accumulateReferences(der.getFormula(), referenced);
            }

            if (test instanceof final IRangedVariable ranged) {
                accumulateReferences(ranged.getMin(), referenced);
                accumulateReferences(ranged.getMax(), referenced);
            }

            if (test instanceof final IExcludableVariable excludable) {
                accumulateReferences(excludable.getExcludes(), referenced);
            }

            if (test instanceof final VariableRandomChoice rndChoice) {
                accumulateReferences(rndChoice.getChooseFromList(), referenced);
            }

            if (test instanceof final VariableRandomSimpleAngle rndAngle) {
                accumulateReferences(rndAngle.getMaxDenom(), referenced);
            }

            if (test.getValue() instanceof final AbstractDocObjectTemplate docobj) {
                accumulateReferences(docobj, referenced);
            }
        }
        accumulateReferences(problem.question, referenced);
        accumulateReferences(problem.solution, referenced);

        if (problem instanceof final AbstractProblemMultipleChoiceTemplate mcb) {
            accumulateReferences(mcb.numChoices, referenced);
            accumulateReferences(mcb.randomOrderChoices, referenced);

            for (final ProblemChoiceTemplate choice : mcb.getChoices()) {
                accumulateReferences(choice.correct, referenced);
                accumulateReferences(choice.doc, referenced);
            }
        }

        if (problem instanceof final ProblemMultipleSelectionTemplate ms) {
            accumulateReferences(ms.maxCorrect, referenced);
            accumulateReferences(ms.minCorrect, referenced);

        } else if (problem instanceof final ProblemNumericTemplate num) {
            final ProblemAcceptNumberTemplate accept = num.acceptNumber;

            if (accept != null) {
                accumulateReferences(accept.correctAnswer, referenced);
                accumulateReferences(accept.varianceFormula, referenced);
            }
        } else if (problem instanceof final ProblemEmbeddedInputTemplate emb) {
            accumulateReferences(emb.correctness, referenced);
            accumulateReferences(emb.correctAnswer, referenced);
        }

        for (final AbstractVariable var : problem.evalContext.getVariables()) {
            if (referenced.contains(var.name)) {
                continue;
            }

            if (var instanceof final VariableDerived der
                && (der.getMin() != null || der.getMax() != null || der.getExcludes() != null)) {
                continue;
            }

            logOrange(report, "WARNING: Variable {", var.name, "} is never used.");
            ++count;
        }

        return count;
    }

    /**
     * Check 3 for Problems: Pre-realize, serialize, parse, serialize, result should match first serialize.
     *
     * @param report  the report being constructed
     * @param problem the problem to check
     * @return the number of errors/warnings found
     */
    private static int problemTest3(final HtmlBuilder report, final AbstractProblemTemplate problem) {

        int count = 0;

        final String initialXml = problem.toXmlString(0);

        try {
            final XmlContent source = new XmlContent(initialXml, false, true);
            for (final XmlContentError error : source.getAllErrors()) {
                logRed(report, "Parse error: ", error.msg);
                ++count;
            }

            final AbstractProblemTemplate parsed = ProblemTemplateFactory.load(source, EParserMode.NORMAL);

            final String finalXml = parsed.toXmlString(0);

            if (!finalXml.equals(initialXml)) {
                logRed(report, "ERROR: serialization of parsed pre-realize problem differs.");
                logDiff(report, finalXml, initialXml);
                ++count;
            }
        } catch (final ParsingException ex) {
            final String msg = ex.getMessage();
            logRed(report, "Parse exception: ", msg);
            ++count;
        }

        return count;
    }

    /**
     * Check 4 for Problems: Can problem be realized 20 times?
     *
     * @param report  the report being constructed
     * @param problem the problem to check
     * @return the number of errors/warnings found
     */
    private static int problemTest4(final HtmlBuilder report, final AbstractProblemTemplate problem) {

        int count = 0;

        for (int i = 0; i < NUM_REALIZATIONS; ++i) {
            if (!problem.realize(problem.evalContext)) {
                logRed(report, "ERROR: Failed to realize problem.");
                ++count;
            }
        }

        return count;
    }

    /**
     * Check 5 for Problems: Post-realize, serialize, parse, serialize, result should match first serialize.
     *
     * @param report  the report being constructed
     * @param problem the problem to check
     * @return the number of errors/warnings found
     */
    private static int problemTest5(final HtmlBuilder report, final AbstractProblemTemplate problem) {

        int count = 0;

        if (problem.realize(problem.evalContext)) {

            final String initialXml = problem.toXmlString(0);

            try {
                final XmlContent source = new XmlContent(initialXml, false, true);
                for (final XmlContentError error : source.getAllErrors()) {
                    logRed(report, "Parsing error: ", error.msg);
                    ++count;
                }

                final AbstractProblemTemplate parsed = ProblemTemplateFactory.load(source, EParserMode.NORMAL);

                // Log.info("Parsed " + parsed.ref + " completed is " + parsed.completionTime);

                if (parsed instanceof ProblemDummyTemplate) {
                    logRed(report, "ERROR: Failed to parsed serialized post-realize problem.");
                    ++count;
                } else {
                    final String finalXml = parsed.toXmlString(0);

                    if (!finalXml.equals(initialXml)) {
                        logRed(report, "ERROR: serialization of parsed post-realize problem differs.");
                        logDiff(report, finalXml, initialXml);
                        ++count;
                    }
                }
            } catch (final ParsingException ex) {
                final String msg = ex.getMessage();
                logRed(report, "Parsing exception: ", msg);
                ++count;
            }
        } else {
            logRed(report, "ERROR: Failed to realize problem.");
            ++count;
        }

        return count;
    }

    /**
     * Check 6 for Problems: Check for derived variable with no value type specified.
     *
     * @param report  the report being constructed
     * @param problem the problem to check
     * @return the number of errors/warnings found
     */
    private static int problemTest6(final HtmlBuilder report, final AbstractProblemTemplate problem) {

        int count = 0;

        for (final AbstractVariable var : problem.evalContext.getVariables()) {
            if (var instanceof final VariableDerived derived && derived.getVariableType() == null) {
                logOrange(report, "WARNING: Variable {", var.name, "} does not specify value type.");
                ++count;
            }
        }

        return count;
    }

    /**
     * Check 7 for Problems: Test for font size on super/sub/root/over/under.
     *
     * @param report  the report being constructed
     * @param problem the problem to check
     * @return the number of errors/warnings found
     */
    private static int problemTest7(final HtmlBuilder report, final AbstractProblemTemplate problem) {

        int count = 0;

        if (problem.question != null) {
            count = scan7(report, problem.question);
        }

        if (problem.solution != null) {
            count += scan7(report, problem.solution);
        }

        if (problem instanceof final AbstractProblemMultipleChoiceTemplate mc) {
            for (final ProblemChoiceTemplate choice : mc.getChoices()) {
                count += scan7(report, choice.doc);
            }
        } else if (problem instanceof final ProblemEmbeddedInputTemplate embedded && embedded.correctAnswer != null) {
            count += scan7(report, embedded.correctAnswer);
        }

        final List<AbstractDocSpanBase> spans = new ArrayList<>(10);
        for (final AbstractVariable var : problem.evalContext.getVariables()) {

            final Object value = var.getValue();
            if (value instanceof final DocSimpleSpan spanVal) {
                count += scan7(report, spanVal);
            }

            if (var instanceof final VariableRandomChoice rc) {
                if (rc.getChooseFromList() != null) {
                    for (final Formula formula : rc.getChooseFromList()) {
                        formula.accumulateSpans(spans);
                    }
                }
            }

            if (var instanceof final VariableDerived der) {
                final Formula formula = der.getFormula();
                formula.accumulateSpans(spans);
            }
        }

        for (final AbstractDocSpanBase span : spans) {
            count += scan7(report, span);
        }

        return count;
    }

    /**
     * Scans a document column for font size on super/sub/root/over/under.
     *
     * @param report    the report being constructed
     * @param container the document column to check
     * @return the number of errors/warnings found
     */
    private static int scan7(final HtmlBuilder report, final AbstractDocContainer container) {

        int count = 0;

        for (final AbstractDocObjectTemplate obj : container.getChildren()) {

            if (obj instanceof final DocRelativeOffset relOffset) {
                final AbstractDocObjectTemplate sup = relOffset.getSuperscript();

                if (sup != null) {
                    final int percent = Math.round(sup.getFontScale() * 100.0f);
                    if (percent != 75) {
                        logOrange(report, "WARNING: Font scale of " + percent + "% on Superscript in REL-OFFSET");
                        ++count;
                    }
                }

                final AbstractDocObjectTemplate sub = relOffset.getSubscript();
                if (sub != null) {
                    final int percent = Math.round(sub.getFontScale() * 100.0f);
                    if (percent != 75) {
                        logOrange(report, "WARNING: Font scale of " + percent + "% on Subscript in REL-OFFSET");
                        ++count;
                    }
                }

                final AbstractDocObjectTemplate over = relOffset.getOver();
                if (over != null) {
                    final int percent = Math.round(over.getFontScale() * 100.0f);
                    if (percent != 75) {
                        logOrange(report, "WARNING: Font scale of " + percent + "% on Over in REL-OFFSET");
                        ++count;
                    }
                }

                final AbstractDocObjectTemplate under = relOffset.getUnder();
                if (under != null) {
                    final int percent = Math.round(under.getFontScale() * 100.0f);
                    if (percent != 75) {
                        logOrange(report, "WARNING: Font scale of " + percent + "% on Under in REL-OFFSET");
                        ++count;
                    }
                }

            } else if (obj instanceof final AbstractDocContainer inner) {
                count += scan7(report, inner);
            }
        }

        return count;
    }

    /**
     * Check 8 for Problems: Check for empty "root" element on radicals.
     *
     * @param report  the report being constructed
     * @param problem the problem to check
     * @return the number of errors/warnings found
     */
    private static int problemTest8(final HtmlBuilder report, final AbstractProblemTemplate problem) {

        int count = 0;

        if (problem.question != null) {
            count = scan8(report, problem.question);
        }

        if (problem.solution != null) {
            count += scan8(report, problem.solution);
        }

        if (problem instanceof final AbstractProblemMultipleChoiceTemplate mc) {
            for (final ProblemChoiceTemplate choice : mc.getChoices()) {
                count += scan8(report, choice.doc);
            }
        } else if (problem instanceof final ProblemEmbeddedInputTemplate embedded && embedded.correctAnswer != null) {
            count += scan8(report, embedded.correctAnswer);
        }

        final List<AbstractDocSpanBase> spans = new ArrayList<>(10);
        for (final AbstractVariable var : problem.evalContext.getVariables()) {

            final Object value = var.getValue();
            if (value instanceof final DocSimpleSpan spanVal) {
                count += scan8(report, spanVal);
            }

            if (var instanceof final VariableRandomChoice rc) {
                if (rc.getChooseFromList() != null) {
                    for (final Formula formula : rc.getChooseFromList()) {
                        formula.accumulateSpans(spans);
                    }
                }
            }

            if (var instanceof final VariableDerived der) {
                final Formula formula = der.getFormula();
                formula.accumulateSpans(spans);
            }
        }

        for (final AbstractDocSpanBase span : spans) {
            count += scan8(report, span);
        }

        return count;
    }

    /**
     * Scans a document container for empty {root} elements.
     *
     * @param report    the report being constructed
     * @param container the document column to check
     * @return the number of errors/warnings found
     */
    private static int scan8(final HtmlBuilder report, final AbstractDocContainer container) {

        int count = 0;

        for (final AbstractDocObjectTemplate obj : container.getChildren()) {

            if (obj instanceof final DocRadical radical) {

                final AbstractDocObjectTemplate base = radical.getBase();
                if (base instanceof final AbstractDocContainer rootContainer) {
                    count += scan8(report, rootContainer);
                }

                final AbstractDocObjectTemplate root = radical.getRoot();
                if (root instanceof final AbstractDocContainer rootContainer) {

                    if (rootContainer.getChildren().isEmpty()) {
                        logOrange(report, "WARNING: Empty ROOT in RADICAL");
                        ++count;
                    } else {
                        count += scan8(report, rootContainer);
                    }
                }

            } else if (obj instanceof final AbstractDocContainer inner) {
                count += scan8(report, inner);
            }
        }

        return count;
    }

    /**
     * Check 9 for Problems: Check for '-' in spans (when not followed by something common like "axis" or "coordinate"
     * or "like").
     *
     * @param report  the report being constructed
     * @param problem the problem to check
     * @return the number of errors/warnings found
     */
    private static int problemTest9(final HtmlBuilder report, final AbstractProblemTemplate problem) {

        int count = 0;

        for (int i = 0; i < NUM_REALIZATIONS; ++i) {
            if (problem.realize(problem.evalContext)) {

                if (problem.question != null) {
                    count += scan9(report, problem.question);
                }

                if (problem.solution != null) {
                    count += scan9(report, problem.solution);
                }

                if (problem instanceof final AbstractProblemMultipleChoiceTemplate mc) {
                    for (final ProblemChoiceTemplate choice : mc.getChoices()) {
                        count += scan9(report, choice.doc);
                    }
                } else if (problem instanceof final ProblemEmbeddedInputTemplate embedded && embedded.correctAnswer != null) {
                    count += scan9(report, embedded.correctAnswer);
                }

                final List<AbstractDocSpanBase> spans = new ArrayList<>(10);
                for (final AbstractVariable var : problem.evalContext.getVariables()) {

                    final Object value = var.getValue();
                    if (value instanceof final DocSimpleSpan spanVal) {
                        count += scan9(report, spanVal);
                    }

                    if (var instanceof final VariableRandomChoice rc) {
                        if (rc.getChooseFromList() != null) {
                            for (final Formula formula : rc.getChooseFromList()) {
                                formula.accumulateSpans(spans);
                            }
                        }
                    }

                    if (var instanceof final VariableDerived der) {
                        final Formula formula = der.getFormula();
                        formula.accumulateSpans(spans);
                    }
                }

                for (final AbstractDocSpanBase span : spans) {
                    count += scan9(report, span);
                }
            } else {
                logRed(report, "ERROR: Failed to realize problem.");
                ++count;
            }
        }

        return count;
    }

    /**
     * Scans a document container for spans with '-' characters that might be used as minus signs.
     *
     * @param report    the report being constructed
     * @param container the document column to check
     * @return the number of errors/warnings found
     */
    private static int scan9(final HtmlBuilder report, final AbstractDocContainer container) {

        int count = 0;

        for (final AbstractDocObjectTemplate obj : container.getChildren()) {

            if (obj instanceof final DocText text) {
                count += textCheck9(report, text.getText());
            } else if (obj instanceof final AbstractDocContainer inner) {
                count += scan9(report, inner);
            }
        }

        return count;
    }

    /**
     * Checks a text string for '-' characters that might be used as minus signs.
     *
     * @param report the report being constructed
     * @param txt    the txt column to check
     * @return the number of errors/warnings found
     */
    private static int textCheck9(final HtmlBuilder report, final String txt) {

        int count = 0;

        int index = txt.indexOf('-');
        while (index >= 0) {
            final String follows = txt.substring(index + 1);

            boolean error = true;

            if (follows.startsWith("axis")
                || follows.startsWith("axes")
                || follows.startsWith("coordinate")
                || follows.startsWith("like")
                || follows.startsWith("clockwise")
                || follows.startsWith("terminal")
                || follows.startsWith("hand")
                || follows.startsWith("of")
                || follows.startsWith("living")
                || follows.startsWith("to")
                || follows.startsWith("one")
                || follows.startsWith("angle")
                || follows.startsWith("triang")
                || follows.startsWith("term")
                || follows.startsWith("spring")
                || follows.startsWith("mass")
                || follows.startsWith("aligned")
                || follows.startsWith("value")
                || follows.startsWith("inch")
                || follows.startsWith("foot")
                || follows.startsWith("meter")
                || follows.startsWith("second")
                || follows.startsWith("mile")
                || follows.startsWith("example")
                || follows.startsWith("labeled")
                || follows.startsWith("Benz")
                || follows.startsWith("over")
                || follows.startsWith("east")
                || follows.startsWith("west")
                || follows.startsWith("house")
                || follows.startsWith("45-90")
                || follows.startsWith("60-90")
                || follows.startsWith("campus")
                || follows.startsWith("week")
                || follows.startsWith("paced")
                || follows.startsWith("point")
                || follows.startsWith("solving")
                || follows.startsWith("issued")
                || follows.startsWith("person")
                || follows.startsWith("in")
                || follows.startsWith("root")
                || follows.startsWith("half")
                || follows.startsWith("even")
                || follows.startsWith("odd")
                || follows.startsWith("plane")
                || follows.startsWith("side")
                || follows.startsWith("slope")
                || follows.startsWith("intercept")
                || follows.startsWith("to-end")
                || follows.startsWith("end")
                || follows.startsWith("South")
                || follows.startsWith("West")
                || follows.startsWith("circle")
                || follows.startsWith("function")
                || follows.startsWith("to-sum")
                || follows.startsWith("sum")
                || follows.startsWith("to-product")
                || follows.startsWith("product")
                || follows.startsWith("radius")
                || follows.startsWith("max")
                || follows.startsWith("min")
                || follows.startsWith("than")
                || follows.startsWith("or")
                || follows.startsWith("equal")
                || follows.startsWith("base")
                || follows.startsWith("ten")
                || follows.startsWith("squared")
                || follows.startsWith("van")
                || follows.startsWith("wagon")
                || follows.startsWith("shift")
                || follows.startsWith("letter")
                || follows.startsWith("line")
                || follows.startsWith("degree")
                || follows.startsWith("looking")
                || follows.startsWith("turn")
                || follows.startsWith("th")) {
                error = false;
            } else {
                if (index >= 1) {
                    final String sub1 = txt.substring(index - 1, index);
                    if ("n".equals(sub1) && follows.startsWith("th")) {
                        error = false;
                    } else if ("x".equals(sub1) && follows.isEmpty()) {
                        error = false;
                    }
                }
                if (index >= 2) {
                    final String sub2 = txt.substring(index - 2, index);
                    if ("+/".equals(sub2) || "TI".equals(sub2)) {
                        error = false;
                    }
                }
                if (index >= 3) {
                    final String sub3 = txt.substring(index - 3, index);
                    if ("sub".equalsIgnoreCase(sub3) || "non".equalsIgnoreCase(sub3)
                        || "SOH".equalsIgnoreCase(sub3) || "CAH".equalsIgnoreCase(sub3)) {
                        error = false;
                    }
                }
                if (index >= 5) {
                    final String sub5 = txt.substring(index - 5, index);
                    if ("45-45".equalsIgnoreCase(sub5) || "30-60".equalsIgnoreCase(sub5)) {
                        error = false;
                    }
                }
                if (index >= 6) {
                    final String sub6 = txt.substring(index - 6, index);
                    if ("Carbon".equalsIgnoreCase(sub6)) {
                        error = false;
                    }
                }
                if (index >= 7) {
                    final String sub7 = txt.substring(index - 7, index);
                    if ("Uranium".equalsIgnoreCase(sub7) || "Fermium".equalsIgnoreCase(sub7)) {
                        error = false;
                    }
                }
                if (index >= 8) {
                    final String sub8 = txt.substring(index - 8, index);
                    if ("Hydrogen".equalsIgnoreCase(sub8)) {
                        error = false;
                    }
                }
                if (index >= 9) {
                    final String sub9 = txt.substring(index - 9, index);
                    if ("Americium".equalsIgnoreCase(sub9) || "Palladium".equalsIgnoreCase(sub9)) {
                        error = false;
                    }
                }
            }

            if (error) {
                final int len = txt.length();
                final int min = Math.max(0, index - 10);
                final int max = Math.min(len, index + 10);
                final String pre = min == 0 ? CoreConstants.EMPTY : "...";
                final String post = max == len ? CoreConstants.EMPTY : "...";
                final String surround = txt.substring(min, max);

                logOrange(report, "WARNING: Possible use of '-' as a minus sign: [", pre, surround, post, "]");
                ++count;
            }

            index = txt.indexOf('-', index + 1);
        }

        return count;
    }

    /**
     * Check 10 for Problems: Look for drawings, graphs, or images that do not have 'alt' text.
     *
     * @param report  the report being constructed
     * @param problem the problem to check
     * @return the number of errors/warnings found
     */
    private static int problemTest10(final HtmlBuilder report, final AbstractProblemTemplate problem) {

        int count = 0;

        if (problem.question != null) {
            count = scan10(report, problem.question);
        }

        if (problem.solution != null) {
            count += scan10(report, problem.solution);
        }

        if (problem instanceof final AbstractProblemMultipleChoiceTemplate mc) {
            for (final ProblemChoiceTemplate choice : mc.getChoices()) {
                count += scan10(report, choice.doc);
            }
        } else if (problem instanceof final ProblemEmbeddedInputTemplate embedded && embedded.correctAnswer != null) {
            count += scan10(report, embedded.correctAnswer);
        }

        final List<AbstractDocSpanBase> spans = new ArrayList<>(10);
        for (final AbstractVariable var : problem.evalContext.getVariables()) {

            final Object value = var.getValue();
            if (value instanceof final DocSimpleSpan spanVal) {
                count += scan10(report, spanVal);
            }

            if (var instanceof final VariableRandomChoice rc) {
                if (rc.getChooseFromList() != null) {
                    for (final Formula formula : rc.getChooseFromList()) {
                        formula.accumulateSpans(spans);
                    }
                }
            }

            if (var instanceof final VariableDerived der) {
                final Formula formula = der.getFormula();
                formula.accumulateSpans(spans);
            }
        }

        for (final AbstractDocSpanBase span : spans) {
            count += scan10(report, span);
        }

        return count;
    }

    /**
     * Scans a document object container recursively for drawing or graph objects, and reports any that do not have an
     * 'alt' value.
     *
     * @param report    the report being constructed
     * @param container the container to check
     * @return the number of errors/warnings found
     */
    private static int scan10(final HtmlBuilder report, final AbstractDocContainer container) {

        int count = 0;

        for (final AbstractDocObjectTemplate obj : container.getChildren()) {

            if (obj instanceof final AbstractDocPrimitiveContainer drawing) {
                if (drawing.getAltText() == null) {
                    final Class<? extends AbstractDocObjectTemplate> cls = obj.getClass();
                    final String typeName = cls.getSimpleName();
                    logOrange(report, "WARNING: Empty ALT in ", typeName);
                    ++count;
                }
            } else if (obj instanceof final DocImage img) {
                if (img.getAltText() == null) {
                    final Class<? extends AbstractDocObjectTemplate> cls = obj.getClass();
                    final String typeName = cls.getSimpleName();
                    logOrange(report, "WARNING: Empty ALT in ", typeName);
                    ++count;
                }
            } else if (obj instanceof final AbstractDocContainer inner) {
                count += scan10(report, inner);
            }
        }

        return count;
    }

    /**
     * Performs quality checks on an exam.
     *
     * @param report   the report being constructed
     * @param examFile the exam file to scan
     * @param exam     the exam object to check
     * @return the number of errors/warnings found
     */
    static int examQualityChecks(final HtmlBuilder report, final File examFile, final ExamObj exam) {

        return examTest1(report, examFile, exam);

        // TODO: Check that all referenced problems exist
    }

    /**
     * Performs quality checks on a homework set.
     *
     * @param report   the report being constructed
     * @param examFile the exam file to scan
     * @param exam     the exam object to check
     * @return the number of errors/warnings found
     */
    static int homeworkQualityChecks(final HtmlBuilder report, final File examFile, final ExamObj exam) {

        return examTest1(report, examFile, exam);

        // TODO: Check that all referenced problems exist
    }

    /**
     * Check 1 for Exam: Does relative filename agree with reference?
     *
     * @param report   the report being constructed
     * @param examFile the exam file to scan
     * @param exam     the exam to check
     * @return the number of errors/warnings found
     */
    private static int examTest1(final HtmlBuilder report, final File examFile, final ExamObj exam) {

        int count = 0;

        String absPath = examFile.getAbsolutePath().replace('/', '.').replace('\\', '.');
        final int lastDot = absPath.lastIndexOf('.');
        if (lastDot != -1) {
            absPath = absPath.substring(0, lastDot);
        }
        final int mathIndex = absPath.indexOf("math.");
        final String relPath = mathIndex == -1 ? absPath : absPath.substring(mathIndex);

        if (!relPath.equals(exam.ref)) {
            logOrange(report, "WARNING: &lt;ref-base&gt; was ", exam.ref, " but relative path was ", relPath);
            ++count;
        }

        return count;
    }

    /**
     * Accumulates all parameter names referenced by a number orformula (if it is not null).
     *
     * @param numberOrFormula the number or formula
     * @param referenced      the set to which to add all referenced parameter names
     */
    private static void accumulateReferences(final NumberOrFormula numberOrFormula,
                                             final Collection<? super String> referenced) {

        if (numberOrFormula != null && numberOrFormula.getFormula() != null) {
            referenced.addAll(numberOrFormula.getFormula().params.keySet());
        }
    }

    /**
     * Accumulates all parameter names referenced by a formula (if it is not null).
     *
     * @param formula    the formula
     * @param referenced the set to which to add all referenced parameter names
     */
    private static void accumulateReferences(final Formula formula, final Collection<? super String> referenced) {

        if (formula != null) {
            referenced.addAll(formula.params.keySet());
        }
    }

    /**
     * Accumulates all parameter names referenced by an array of formulas (if it is not null).
     *
     * @param formulas   the formula array
     * @param referenced the set to which to add all referenced parameter names
     */
    private static void accumulateReferences(final Formula[] formulas,
                                             final Collection<? super String> referenced) {

        if (formulas != null) {
            for (final Formula formula : formulas) {
                if (formula != null) {
                    referenced.addAll(formula.params.keySet());
                }
            }
        }
    }

    /**
     * Accumulates all parameter names referenced by a document object (if it is not null).
     *
     * @param doc        the document object
     * @param referenced the set to which to add all referenced parameter names
     */
    private static void accumulateReferences(final AbstractDocObjectTemplate doc, final Set<String> referenced) {

        if (doc != null) {
            doc.accumulateParameterNames(referenced);
        }
    }

    /**
     * Logs a message in "red" color.
     *
     * @param report the report to which to log
     * @param msg    the message string
     */
    private static void logRed(final HtmlBuilder report, final Object... msg) {

        final String text = SimpleBuilder.concat(msg);
        report.sSpan(null, "style='color:red;'").add(text).eSpan().br().addln();
    }

    /**
     * Logs a message in "orange" color.
     *
     * @param report the report to which to log
     * @param msg    the message string
     */
    private static void logOrange(final HtmlBuilder report, final Object... msg) {

        final String text = SimpleBuilder.concat(msg);
        report.sSpan(null, "style='color:orange;'").add(text).eSpan().br().addln();
    }

    /**
     * Print the difference between two strings to a log.
     *
     * @param report the report to which to log
     * @param after  the "after" string
     * @param before the "before" string
     */
    private static void logDiff(final HtmlBuilder report, final String after, final String before) {

        report.addln("<div color='blue' border-top='1px solid blue' border-bottom='1px solid blue'>");
        if (after.length() != before.length()) {
            report.addln("Length from ", Integer.toString(before.length()), " to ", Integer.toString(after.length()),
                    "<br/>");
        }

        final int len = Math.min(before.length(), after.length());

        for (int i = 0; i < len; ++i) {
            if (before.charAt(i) != after.charAt(i)) {
                final int start = Math.max(0, i - 40);

                report.addln("After:<br/><pre>");
                int max = i + 100;
                if (max > after.length()) {
                    max = after.length();
                }
                final String afterRaw = after.substring(start, max);
                final String afterLt = afterRaw.replace("<", "&lt;");
                final String afterGt = afterLt.replace("<", "&lt;");
                report.addln(afterGt);

                report.addln("</pre><br/>Before:<br/><pre>");
                max = i + 100;
                if (max > before.length()) {
                    max = before.length();
                }

                final String beforeRaw = before.substring(start, max);
                final String beforeLt = beforeRaw.replace("<", "&lt;");
                final String beforeGr = beforeLt.replace("<", "&lt;");
                report.addln(beforeGr);

                report.addln("</pre>");
                break;
            }
        }
        report.addln("</div>");
    }
}
