package dev.mathops.app.assessment;

import dev.mathops.app.DirectoryFilter;
import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.FactoryBase;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.problem.template.AbstractProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemAcceptNumberTemplate;
import dev.mathops.assessment.problem.template.ProblemChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleSelectionTemplate;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.IExcludableVariable;
import dev.mathops.assessment.variable.IRangedVariable;
import dev.mathops.assessment.variable.VariableDerived;
import dev.mathops.assessment.variable.VariableRandomChoice;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EPath;
import dev.mathops.commons.PathList;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.parser.xml.XmlContentError;

import javax.swing.JFileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A class that sweeps through a given directory looking for problem and exam XML files, and testing each one for
 * validity.
 */
final class InstructionTester {

    /** Number of repetitions for generating objects. */
    private static final int REPS = 1;

    /** Flag to control tests of pre-realize serialization. */
    private static final boolean TEST_PRE_REALIZE_SERIALIZATION = true;

    /** Flag to control tests of realization. */
    private static final boolean TEST_REALIZE = true;

    /** The GUI for the test process. */
    private InstructionTesterGUI gui;

    /** The total number of files being scanned. */
    private int total;

    /** The index of the file currently being scanned. */
    private int which;

    /**
     * Construct a new {@code InstructionTester}.
     */
    private InstructionTester() {

        // No action
    }

    /**
     * Run method to execute the test process.
     */
    private void run() {

        // Get the base directory at which to begin scanning
        final File base = getBaseDir();
        if (base != null) {
            // Count the number of files to be processed
            final int[] counts = new int[3];
            this.total = count(counts, base);

            // Recursively scan, testing as we go.
            this.gui = new InstructionTesterGUI();
            this.gui.create();
            this.gui.logError("Tests will generate each item " + REPS + " times.");
            this.gui.logError("Scanning " + base.getAbsolutePath());
            this.gui.logError("Found " + counts[0] + " problems, " + counts[1] + " homeworks, and " + counts[2]
                    + " exams.");
            scan(base);

            this.gui.indicateFinished();

            while (!this.gui.getCancelled()) {
                try {
                    Thread.sleep(100L);
                } catch (final InterruptedException ex) {
                    Log.warning(ex);
                }
            }

            this.gui.destroy();
        }
    }

    /**
     * Ask the user what the base directory is for instructional data, using some known defaults if they exist.
     *
     * @return the file selected by the user, or null if dialog was canceled
     */
    private static File getBaseDir() {

        // Set a default if it is found...
        final PathList paths = PathList.getInstance();
        File file = paths.get(EPath.CUR_DATA_PATH);
        if (file != null) {
            file = new File(file, "instruction");
        }

        final JFileChooser jfc = new JFileChooser();
        if ((file != null) && (file.exists())) {
            jfc.setCurrentDirectory(file);
        } else {
            file = paths.get(EPath.SOURCE_1_PATH);

            if (file != null) {
                file = new File(file, "instruction");
            }

            if ((file != null) && (file.exists())) {
                jfc.setCurrentDirectory(file);
            } else {
                file = paths.get(EPath.SOURCE_2_PATH);

                if (file != null) {
                    file = new File(file, "instruction");
                }

                if ((file != null) && (file.exists())) {
                    jfc.setCurrentDirectory(file);
                } else {
                    file = paths.get(EPath.SOURCE_3_PATH);

                    if (file != null) {
                        file = new File(file, "instruction");
                    }

                    if ((file != null) && (file.exists())) {
                        jfc.setCurrentDirectory(file);
                    }
                }
            }
        }

        jfc.setFileFilter(new DirectoryFilter());
        jfc.setDialogTitle("Instructional Data Path");
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.setMultiSelectionEnabled(false);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = jfc.getSelectedFile();
        } else {
            file = null;
        }

        return file;
    }

    /**
     * Descend a directory recursively, counting the number of xml files we find.
     *
     * @param counts a 3-integer array with individual counts of problems, homework, and exams
     * @param dir    the directory to scan
     * @return the number of matching XML files within the directory
     */
    private static int count(final int[] counts, final File dir) {

        final String path = dir.getAbsolutePath();
        final File[] list = dir.listFiles();
        int theTotal = 0;

        if (list != null) {
            final boolean isProblem = path.contains("/problems/")
                    || path.endsWith("/problems")
                    || path.contains("\\problems\\")
                    || path.endsWith("\\problems");
            final boolean isExam = path.contains("/exams/")
                    || path.endsWith("/exams")
                    || path.contains("\\exams\\")
                    || path.endsWith("\\exams");
            final boolean isHomework = path.contains("/homework/")
                    || path.endsWith("/homework")
                    || path.contains("\\homework\\")
                    || path.endsWith("\\homework");

            for (final File file : list) {

                if (file.isDirectory()) {
                    theTotal += count(counts, file);
                }

                if (file.getName().toLowerCase(Locale.ROOT).endsWith(".xml")) {

                    if (isProblem) {
                        ++counts[0];
                        ++theTotal;
                    } else if (isHomework) {
                        ++counts[1];
                        ++theTotal;
                    } else if (isExam) {
                        ++counts[2];
                        ++theTotal;
                    }
                }
            }
        }

        return theTotal;
    }

    /**
     * Scan a directory recursively, descending into subdirectories and testing each XML file we find.
     *
     * @param dir the directory to scan
     */
    private void scan(final File dir) {

        try {
            Thread.sleep(5L);
        } catch (final InterruptedException e) {
            Log.warning(e);
        }

        if (this.gui.getCancelled()) {
            return;
        }

        final String path = dir.getAbsolutePath();
        final File[] list = dir.listFiles();

        if (list != null) {
            // Sort the file list alphabetically
            Arrays.sort(list);

            final boolean isProblem = path.contains("/problems/")
                    || path.endsWith("/problems")
                    || path.contains("\\problems\\")
                    || path.endsWith("\\problems");
            final boolean isExam = path.contains("/exams/")
                    || path.endsWith("/exams")
                    || path.contains("\\exams\\")
                    || path.endsWith("\\exams");
            final boolean isHomework = path.contains("/homework/")
                    || path.endsWith("/homework")
                    || path.contains("\\homework\\")
                    || path.endsWith("\\homework");

            for (final File file : list) {

                if (file.isDirectory()) {
                    scan(file);
                }

                if (file.getName().toLowerCase(Locale.ROOT).endsWith(".xml")) {

                    final String lbl;
                    if (isProblem) {
                        this.which++;
                        lbl = "Scanning file " + this.which + " / " + this.total;
                        this.gui.setTopProgressLabel(lbl);
                        this.gui.setTopProgressValue(this.which, this.total);
                        this.gui.repaint();
                        testProblem(file);
                    } else if (isExam) {
                        this.which++;
                        lbl = "Scanning file " + this.which + " / " + this.total;
                        this.gui.setTopProgressLabel(lbl);
                        this.gui.setTopProgressValue(this.which, this.total);
                        this.gui.repaint();
                        testExam(file);
                    } else if (isHomework) {
                        this.which++;
                        lbl = "Scanning file " + this.which + " / " + this.total;
                        this.gui.setTopProgressLabel(lbl);
                        this.gui.setTopProgressValue(this.which, this.total);
                        this.gui.repaint();
                        testHomework(file);
                    }
                }

                if (this.gui.getCancelled()) {
                    break;
                }
            }
        }
    }

    /**
     * Test that an XML file contains a working problem.
     *
     * @param file the file to test
     */
    private void testProblem(final File file) {

        this.gui.setBottomProgressLabel("Problem " + file.getAbsolutePath());
        this.gui.setBottomProgressValue(0, REPS);
        this.gui.repaint();

        Log.info(file.getAbsolutePath());

        // Load the problem file
        try {
            XmlContent content = FactoryBase.getSourceContent(file);

            final AbstractProblemTemplate prob =
                    ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

            List<XmlContentError> allErrors = gatherErrors(content);
            if (!allErrors.isEmpty()) {
                this.gui.logError("Errors in problem file: " + file.getAbsolutePath());
                for (final XmlContentError error : allErrors) {
                    this.gui.logError("    " + error);
                }
                return;
            }

            qualityControl(file, prob);

            if (TEST_PRE_REALIZE_SERIALIZATION) {
                //
                // Test serialization of unrealized problem
                //

                final String preXml = prob.toXmlString(0);
                final AbstractProblemTemplate parsedPre =
                        ProblemTemplateFactory.load(preXml, EParserMode.ALLOW_DEPRECATED);

                if (parsedPre == null) {
                    this.gui.logError("Can't parse serialized problem: " + file.getAbsolutePath());
                    this.gui.logError(Log.errorMessagesAsString());
                    return;
                }

                allErrors = gatherErrors(content);
                if (!allErrors.isEmpty()) {
                    this.gui.logError(
                            "Errors in parsed pre-realize problem XML: " + file.getAbsolutePath());
                    for (final XmlContentError error : allErrors) {
                        this.gui.logError("    " + error);
                    }
                    return;
                }

                final String postXml = parsedPre.toXmlString(0);
                if (!postXml.equals(preXml)) {
                    logDiff(prob.id, postXml, preXml);
                    this.gui.logError("Problem not identical after pre-realize transfer "
                            + file.getAbsolutePath());
                    this.gui.logError(Log.errorMessagesAsString());
                }
            }

            if (TEST_REALIZE) {
                //
                // Realize it REPS times
                //

                // Log.info(" Realizing...");

                for (int i = 0; i < REPS; i++) {

                    if (this.gui.getCancelled()) {
                        return;
                    }

                    this.gui.setBottomProgressValue(i + 1, REPS);
                    this.gui.repaint();

                    final long start = System.currentTimeMillis();

                    if (prob.realize(prob.evalContext)) {
                        final long end = System.currentTimeMillis();
                        final double duration = (double) (end - start) / 1000.0;

                        if (duration > 3.0) {
                            this.gui.logError("Problem generation took " + duration
                                    + " sec.: " + file.getAbsolutePath());
                        }

                        // Simulate a network transfer
                        final String before = prob.toXmlString(0);
                        try {
                            content = new XmlContent(before, false, false);

                            final AbstractProblemTemplate prob2 =
                                    ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                            final String after = prob2.toXmlString(0);

                            if (!after.equals(before)) {
                                logDiff(prob.id, after, before);
                                this.gui.logError("Problem not identical after transfer "
                                        + file.getAbsolutePath());
                                this.gui.logError(Log.errorMessagesAsString());

                                System.exit(0);
                            }
                        } catch (final ParsingException ex) {
                            Log.warning(ex);
                            this.gui.logError(
                                    "Cannot parse XML generated by problem " + file.getAbsolutePath());
                            this.gui.logError(Log.errorMessagesAsString());
                        }
                    } else {
                        allErrors = gatherErrors(content);
                        if (allErrors.isEmpty()) {
                            this.gui.logError("Problem generation failed but gave no error: "
                                    + file.getAbsolutePath());
                        } else {
                            this.gui.logError("Can't generate problem: ");
                            for (final XmlContentError error : allErrors) {
                                this.gui.logError("    " + error);
                            }
                            return;
                        }
                    }

                    // Look for parameters with names that might cause problems
                    final EvalContext set = prob.evalContext;

                    if (set.getVariable("x") != null) {
                        this.gui.logError("WARNING: " + file.getAbsolutePath()
                                + " contains parameter {x} (may conflict with graphs or user formula entry)");
                    }

                    if (set.getVariable("y") != null) {
                        this.gui.logError("WARNING: " + file.getAbsolutePath()
                                + " contains parameter {y} (may conflict with graphs or user formula entry)");
                    }

                    if (set.getVariable("z") != null) {
                        this.gui.logError("WARNING: " + file.getAbsolutePath()
                                + " contains parameter {z} (may conflict with graphs or user formula entry)");
                    }

                    if (set.getVariable("t") != null) {
                        this.gui.logError("WARNING: " + file.getAbsolutePath()
                                + " contains parameter {t} (may conflict with graphs or user formula entry)");
                    }
                }
            }
        } catch (final ParsingException ex) {
            Log.warning(ex.getMessage());
            this.gui.logError("Cannot parse problem XML: " + file.getAbsolutePath());
        }

        this.gui.setBottomProgressLabel(CoreConstants.EMPTY);
        this.gui.setBottomProgressValue(0, REPS);
        this.gui.repaint();
    }

    /**
     * Test that an XML file contains a valid exam.
     *
     * @param file the file to test
     */
    private void testExam(final File file) {

        try {
            final XmlContent content = FactoryBase.getSourceContent(file);

            this.gui.setBottomProgressLabel("Exam " + file.getAbsolutePath());
            this.gui.setBottomProgressValue(0, REPS);
            this.gui.repaint();

            //
            // Load the exam
            //

            final ExamObj exam = ExamFactory.load(content, EParserMode.ALLOW_DEPRECATED);

            if (exam == null) {
                this.gui.logError("Can't load exam file: " + file.getAbsolutePath());
                this.gui.logError(Log.errorMessagesAsString());
                return;
            }

            List<XmlContentError> allErrors = gatherErrors(content);
            if (!allErrors.isEmpty()) {
                this.gui.logError("Errors in exam file: " + file.getAbsolutePath());
                for (final XmlContentError error : allErrors) {
                    this.gui.logError("    " + error);
                }
                return;
            }

            if (TEST_PRE_REALIZE_SERIALIZATION) {
                //
                // Test serialization of unrealized exam
                //

                final String preXml = exam.toXmlString(0);
                final ExamObj parsedPre = ExamFactory.load(preXml, EParserMode.ALLOW_DEPRECATED);

                if (parsedPre == null) {
                    this.gui.logError("Can't parse serialized exam: " + file.getAbsolutePath());
                    this.gui.logError(Log.errorMessagesAsString());
                    return;
                }

                allErrors = gatherErrors(content);
                if (!allErrors.isEmpty()) {
                    this.gui.logError(
                            "Errors in parsed pre-realize exam XML: " + file.getAbsolutePath());
                    for (final XmlContentError error : allErrors) {
                        this.gui.logError("    " + error);
                    }
                    return;
                }

                final String postXml = parsedPre.toXmlString(0);
                if (!postXml.equals(preXml)) {
                    logDiff(exam.ref, postXml, preXml);
                    this.gui.logError(
                            "Exam not identical after pre-realize transfer " + file.getAbsolutePath());
                    this.gui.logError(Log.errorMessagesAsString());
                }
            }

            if (TEST_REALIZE) {
                //
                // Realize it REPS times
                //

                for (int i = 0; i < REPS; i++) {

                    try {
                        Thread.sleep(5L);
                    } catch (final InterruptedException e) {
                        Log.warning(e);
                    }

                    if (this.gui.getCancelled()) {
                        return;
                    }

                    this.gui.setBottomProgressValue(i + 1, REPS);
                    this.gui.repaint();

                    if (!exam.realize(false, false, 100L)) {
                        this.gui.logError("Exam generation failed but gave no error: " + file.getAbsolutePath());
                    }

                    // TODO: Test serialization of realized
                }
            }
        } catch (final ParsingException ex) {
            Log.warning(ex);
            this.gui.logError("Cannot parse problem XML: " + file.getAbsolutePath());
        }

        this.gui.setBottomProgressLabel(CoreConstants.EMPTY);
        this.gui.setBottomProgressValue(0, REPS);
        this.gui.repaint();
    }

    /**
     * Test that an XML file contains a valid homework set.
     *
     * @param file the file to test
     */
    private void testHomework(final File file) {

        try {
            final XmlContent content = FactoryBase.getSourceContent(file);

            this.gui.setBottomProgressLabel("Homework" + file.getAbsolutePath());
            this.gui.setBottomProgressValue(0, REPS);
            this.gui.repaint();

            //
            // Load the exam
            //

            final ExamObj exam = ExamFactory.load(content, EParserMode.ALLOW_DEPRECATED);

            if (exam == null) {
                this.gui.logError("Can't load homework file: " + file.getAbsolutePath());
                this.gui.logError(Log.errorMessagesAsString());
                return;
            }

            List<XmlContentError> allErrors = gatherErrors(content);
            if (!allErrors.isEmpty()) {
                this.gui.logError("Errors in homework file: " + file.getAbsolutePath());
                for (final XmlContentError error : allErrors) {
                    this.gui.logError("    " + error);
                }
                return;
            }

            if (TEST_PRE_REALIZE_SERIALIZATION) {
                //
                // Test serialization of unrealized exam
                //

                final String preXml = exam.toXmlString(0);
                final ExamObj parsedPre = ExamFactory.load(preXml, EParserMode.ALLOW_DEPRECATED);

                if (parsedPre == null) {
                    this.gui.logError("Can't parse serialized homework: " + file.getAbsolutePath());
                    this.gui.logError(Log.errorMessagesAsString());
                    return;
                }

                allErrors = gatherErrors(content);
                if (!allErrors.isEmpty()) {
                    this.gui.logError("Errors in parsed pre-realize homework XML: " + file.getAbsolutePath());
                    for (final XmlContentError error : allErrors) {
                        this.gui.logError("    " + error);
                    }
                    return;
                }

                final String postXml = parsedPre.toXmlString(0);
                if (!postXml.equals(preXml)) {
                    logDiff(exam.ref, postXml, preXml);
                    this.gui.logError("Homework not identical after pre-realize transfer " + file.getAbsolutePath());
                    this.gui.logError(Log.errorMessagesAsString());
                }
            }

            if (TEST_REALIZE) {
                //
                // Realize it REPS times
                //

                for (int i = 0; i < REPS; i++) {

                    try {
                        Thread.sleep(5L);
                    } catch (final InterruptedException e) {
                        Log.warning(e);
                    }

                    if (this.gui.getCancelled()) {
                        return;
                    }

                    this.gui.setBottomProgressValue(i + 1, REPS);
                    this.gui.repaint();

                    if (!exam.realize(false, false, 100L)) {
                        this.gui.logError("Homework generation failed but gave no error: " + file.getAbsolutePath());
                    }

                    // TODO: Test serialization of realized
                }
            }
        } catch (final ParsingException ex) {
            Log.warning(ex);
            this.gui.logError("Cannot parse problem XML: " + file.getAbsolutePath());
        }

        this.gui.setBottomProgressLabel(CoreConstants.EMPTY);
        this.gui.setBottomProgressValue(0, REPS);
        this.gui.repaint();
    }

    /**
     * Gathers all errors from an {@code XmlContent} object.
     *
     * @param content the {@code XmlContent} object
     * @return the list of all errors
     */
    private static List<XmlContentError> gatherErrors(final XmlContent content) {

        final List<XmlContentError> allErrors = new ArrayList<>(10);
        final List<XmlContentError> mainErrors = content.getErrors();
        if (mainErrors != null) {
            allErrors.addAll(mainErrors);
        }

        final IElement top = content.getToplevel();
        accumulateErrors(top, allErrors);

        return allErrors;
    }

    /**
     * Recursively accumulates errors from a node and its descendants.
     *
     * @param node   the node
     * @param target the list to which to add accumulated errors
     */
    private static void accumulateErrors(final INode node, final List<? super XmlContentError> target) {

        final List<XmlContentError> nodeErrors = node.getErrors();
        if (nodeErrors != null && !nodeErrors.isEmpty()) {
            if (node instanceof final IElement elem) {
                final String tag = elem.getTagName();
                final String prefix = "In <" + tag + ">: ";
                for (final XmlContentError error : nodeErrors) {
                    target.add(new XmlContentError(error.span, prefix + error.msg));
                }
            } else {
                final String prefix = "In text: ";
                for (final XmlContentError error : nodeErrors) {
                    target.add(new XmlContentError(error.span, prefix + error.msg));
                }
            }
        }

        if (node instanceof final NonemptyElement elem) {
            for (final INode child : elem.getChildrenAsList()) {
                accumulateErrors(child, target);
            }
        }
    }

    /**
     * Print the difference between two strings to a log.
     *
     * @param identifier the identifier of the problem being serialized
     * @param after      the "after" string
     * @param before     the "before" string
     */
    private static void logDiff(final String identifier, final String after, final String before) {

        final HtmlBuilder builder = new HtmlBuilder(3 * before.length());

        if (after.length() != before.length()) {
            builder.addln(identifier, ": Length from ", Integer.toString(before.length()), " to ",
                    Integer.toString(after.length()));
        }
        final int len = Math.min(before.length(), after.length());

        for (int i = 0; i < len; ++i) {

            if (before.charAt(i) != after.charAt(i)) {
                builder.addln("After:");
                int max = i + 100;

                if (max > after.length()) {
                    max = after.length();
                }

                final int start = Math.max(0, i - 40);
                builder.addln(after.substring(start, max));

                builder.addln("Before:");
                max = i + 100;

                if (max > before.length()) {
                    max = before.length();
                }

                builder.addln(before.substring(start, max));

                break;
            }
        }

        Log.info(builder.toString());
    }

    /**
     * Perform quality control checks on problems.
     *
     * @param file    the file from which the problem was loaded
     * @param problem the problem
     */
    private static void qualityControl(final File file, final AbstractProblemTemplate problem) {

        // Check 1: Does filename agree with reference?

        final String origin = file.getAbsolutePath();
        String path = origin.replace('/', '.').replace('\\', '.');
        int end = path.length();
        if (path.endsWith(".xml")) {
            end -= 4;
            path = path.substring(0, end);
        }
        final int mathIndex = path.indexOf("math.");
        if (mathIndex != -1) {
            path = path.substring(mathIndex);
        }

        if ((!"String".equals(path) && !path.equals(problem.id))) {
            Log.warning("PATH = [", origin,
                    "] REF = [", problem.id, "]");
        }

        // Check 2: Are there unused parameters in the problem?

        final Set<String> referenced = new HashSet<>(10);
        for (final AbstractVariable test : problem.evalContext.getVariables()) {

            if (test instanceof final VariableDerived der) {
                accumulateReferences(der.getFormula(), referenced);
            }

            if (test instanceof final IRangedVariable ranged) {
                accumulateReferences(ranged.getMin(), referenced);
                accumulateReferences(ranged.getMax(), referenced);
            }

            if (test instanceof IExcludableVariable) {
                accumulateReferences(((IExcludableVariable) test).getExcludes(), referenced);
            }

            if (test instanceof VariableRandomChoice) {
                accumulateReferences(((VariableRandomChoice) test).getChooseFromList(), referenced);
            }

            if (test.getValue() instanceof AbstractDocObjectTemplate) {
                accumulateReferences((AbstractDocObjectTemplate) (test.getValue()), referenced);
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
            final String name = var.name;

            if (referenced.contains(name)) {
                continue;
            }

            if (var instanceof final VariableDerived der) {
                // A derived variable might be unreferenced if it has a min/max value or a list of excludes

                if (der.getMin() != null || der.getMax() != null || der.getExcludes() != null) {
                    continue;
                }
            }

            Log.warning("PARAMETER {", name, "} NOT REFERENCED IN [", origin, "]");

            // TODO: Test for SPAN variables that do not depend on any parameters - candidates
            // for direct substitution in source files
        }
    }

    /**
     * Accumulates all parameter names referenced by a number or formula (if it is not null).
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
    private static void accumulateReferences(final Formula[] formulas, final Collection<? super String> referenced) {

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
     * Main method to run the test application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        new InstructionTester().run();
    }
}
