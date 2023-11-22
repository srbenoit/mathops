package dev.mathops.assessment.exam;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * An exam constructed from a selection of problems, and assembled into a device to be used for assessment of student
 * performance. Exams can be made up of several sections. The sections will be presented to the student in order, but
 * the questions within each section may be randomly ordered if the section specifies they are to be as such. Each
 * section is composed of a number of problems, each with a textual label (the problem number), and a specified number
 * of points, which may be fractional. Each problem can then refer to any number of problem definitions. When the exam
 * is realized, exactly one of these references will be selected at random for the problem. Exams definitions are stored
 * as XML files/streams, and may reside on a file system, a database, or in a network repository. Their format is
 * specified by {@code exam.dtd}. An XSL transform is available as {@code exam.xsl}, and an accompanying style sheet is
 * available as {@code exam-style.css} allowing the XML files to be viewed conveniently in a browser.
 */
public final class ExamObj extends AbstractXmlObject {

    /** A commonly used value. */
    private static final Long LONG_ZERO = Long.valueOf(0L);

    /** A commonly used value. */
    private static final Integer INT_ZERO = Integer.valueOf(0);

    /** A set of parameters for algorithmically generating problems. */
    private EvalContext evalContext;

    /**
     * The exam's unique position in the organizational tree, in which all instructional material is maintained.
     */
    public String ref;

    /** The root that all references made within the exam are relative to. */
    public String refRoot;

    /** The title of the exam. */
    public String examName;

    /** The course name or number with which the exam is associated. */
    public String course;

    /** The unit within the course with which the exam is associated. */
    public String courseUnit;

    /** The version of the exam. */
    public String examVersion;

    /** The exam instructions. */
    public DocColumn instructions;

    /** The permitted time to work on the exam, in seconds. */
    public Long allowedSeconds;

    /** An ordered list of {@code ExamSection} objects in the exam. */
    private final List<ExamSection> examSections;

    /** An ordered list of {@code ExamSubtest} objects. */
    private final List<ExamSubtest> examSubtests;

    /** An ordered list of {@code ExamGradingRule} objects. */
    private final List<ExamGradingRule> examGradingRules;

    /** An ordered list of {@code ExamOutcome} objects. */
    private final List<ExamOutcome> examOutcomes;

    /** The index of the currently selected section. */
    private Integer currentSection;

    /** The index of the currently selected problem (within a section). */
    private Integer currentProblem;

    /** The time/date when the exam object was created. */
   long creationTime;

    /** The time/date when the exam object was most recently realized. */
    public long realizationTime;

    /** The time/date when the exam object was most recently presented. */
    public long presentationTime;

    /** The time/date when the exam object was most recently completed. */
    public long completionTime;

    /** True if the exam is being taken remotely. */
    public boolean remote;

    /** True if the exam is being taken in a proctored setting. */
    public boolean proctored;

    /** A list of problems indexed on ID value from the ExamProblem object. */
    private ExamProblem[] problemById;

    /** The background color to use when drawing the exam. */
    public Color backgroundColor;

    /** The name of the background color to use when drawing the exam. */
    private String backgroundColorName;

    /** The serial number assigned to the realized exam. */
    public Long serialNumber;

    /** The HTML representation of the exam instructions. */
    public String instructionsHtml;

    /**
     * Constructs a new, empty {@code ExamObj}.
     */
    public ExamObj() {
        super();

        this.creationTime = System.currentTimeMillis();

        this.evalContext = new EvalContext();

        this.examSections = new ArrayList<>(1);
        this.examSubtests = new ArrayList<>(1);
        this.examGradingRules = new ArrayList<>(1);
        this.examOutcomes = new ArrayList<>(1);
    }

    /**
     * Makes a clone of the exam. The clone is a deep copy such that any changes to the clone or its contained objects
     * will not change the original object (references are copied only when the underlying object is immutable,
     * otherwise contained objects are cloned). The exceptions to this is that the creation timestamp on the new exam is
     * set to the time when the clone is constructed, and the realization, presentation, and completion timestamps of
     * the clone are set to zero. The clone also does not carry over the current section and problem value nor the
     * serial number, or exam listeners.
     *
     * @return a copy of the original object
     */
    public ExamObj deepCopy() {

        final ExamObj copy = new ExamObj();

        copy.evalContext = this.evalContext.deepCopy();
        copy.ref = this.ref;
        copy.refRoot = this.refRoot;
        copy.examName = this.examName;
        copy.course = this.course;
        copy.courseUnit = this.courseUnit;
        copy.examVersion = this.examVersion;

        if (this.instructions != null) {
            copy.instructions = this.instructions.deepCopy();
        }

        copy.allowedSeconds = this.allowedSeconds;

        for (final ExamSection section : this.examSections) {
            copy.examSections.add(section.deepCopy(copy));
        }

        for (final ExamSubtest subtest : this.examSubtests) {
            copy.examSubtests.add(subtest.deepCopy());
        }

        for (final ExamGradingRule rule : this.examGradingRules) {
            copy.examGradingRules.add(rule.deepCopy());
        }

        for (final ExamOutcome outcome : this.examOutcomes) {
            copy.examOutcomes.add(outcome.deepCopy());
        }

        copy.remote = this.remote;
        copy.proctored = this.proctored;
        copy.backgroundColor = this.backgroundColor;
        copy.backgroundColorName = this.backgroundColorName;

        copy.generateProblemList();

        return copy;
    }

    /**
     * Gets the set of parameters used to realize the problem.
     *
     * @return the parameter set, or {@code null} if no parameters defined
     */
    public EvalContext getEvalContext() {

        return this.evalContext;
    }

    /**
     * Gets the number of sections in the exam.
     *
     * @return the number of sections
     */
    public int getNumSections() {

        return this.examSections.size();
    }

    /**
     * Gets the total number of problems in the exam.
     *
     * @return the number of problems
     */
    public int getNumProblems() {

        int count = 0;

        for (final ExamSection sect : this.examSections) {
            count += sect.getNumProblems();
        }

        return count;
    }

    /**
     * Gets a single {@code ExamSection} from the exam.
     *
     * @param index the index of the section to get
     * @return the requested exam section
     */
    public ExamSection getSection(final int index) {

        return this.examSections.get(index);
    }

    /**
     * Gets an iterator over the sections of the exam. Each element in the iteration will be an {@code ExamSection}.
     *
     * @return the section iterator
     */
    public Iterator<ExamSection> sections() {

        return this.examSections.iterator();
    }

    /**
     * Adds a section to the exam.
     *
     * @param section the {@code ExamSection} to add
     */
    public void addSection(final ExamSection section) {

        this.examSections.add(section);
    }

    /**
     * Gets an iterator over the subtests of the exam. Each element in the iteration will be an {@code ExamSubtest}.
     *
     * @return the subtest iterator
     */
    public Iterator<ExamSubtest> subtests() {

        return this.examSubtests.iterator();
    }

    /**
     * Adds a subtest to the exam.
     *
     * @param subtest the {@code ExamSubtest} to add
     */
    void addSubtest(final ExamSubtest subtest) {

        this.examSubtests.add(subtest);
    }

    /**
     * Gets an iterator over the grading rules of the exam. Each element in the iteration will be an
     * {@code ExamGradingRule}.
     *
     * @return the grading rules iterator
     */
    public Iterator<ExamGradingRule> gradingRules() {

        return this.examGradingRules.iterator();
    }

    /**
     * Adds a grading rule to the exam.
     *
     * @param gradingRule the {@code ExamGradingRule} to add
     */
    void addGradingRule(final ExamGradingRule gradingRule) {

        this.examGradingRules.add(gradingRule);
    }

    /**
     * Gets an iterator over the outcomes of the exam. Each element in the iteration will be an {@code ExamOutcome}.
     *
     * @return the outcomes iterator
     */
    public Iterator<ExamOutcome> examOutcomes() {

        return this.examOutcomes.iterator();
    }

    /**
     * Adds an outcome to the exam.
     *
     * @param outcome the {@code ExamOutcome} to add
     */
    void addExamOutcome(final ExamOutcome outcome) {

        this.examOutcomes.add(outcome);
    }

    /**
     * Sets the currently selected problem.
     *
     * @param section the index of the section containing the selected problem ({@code null} if none)
     * @param problem the index of the selected problem ({@code null} if none)
     */
    public void setCurrentProblem(final Integer section, final Integer problem) {

        this.currentSection = section;
        this.currentProblem = problem;
    }

    /**
     * Tests whether a particular problem is currently selected.
     *
     * @param section the index of the section to test
     * @param problem the index of the problem to test
     * @return {@code true} if the indicated problem is selected; {@code false} otherwise
     */
    public boolean isCurrentProblem(final int section, final int problem) {

        return (this.currentSection != null) && (this.currentSection.intValue() == section)
                && (this.currentProblem != null) && (this.currentProblem.intValue() == problem);
    }

//    /**
//     * Gets the currently selected section.
//     *
//     * @return the index of the section containing the selected problem ({@code null} if none)
//     */
//    public Integer getCurrentSection() {
//
//        return this.currentSection;
//    }

//    /**
//     * Gets the currently selected problem.
//     *
//     * @return the index of the selected problem within its section ({@code null} if none)
//     */
//    public Integer getCurrentProblem() {
//
//        return this.currentProblem;
//    }

//    /**
//     * Gets the index of the currently selected problem.
//     *
//     * @return the currently selected problem
//     */
//    public Integer getOnSection() {
//
//        return this.currentSection;
//    }

//    /**
//     * Gets the index of the currently selected problem.
//     *
//     * @return the currently selected problem
//     */
//    public Integer getOnProblem() {
//
//        return this.currentProblem;
//    }

    /**
     * Sets the completion time on the exam to the current date/time. This should be done by the server when the exam is
     * submitted, to avoid differences in client clock setting from affecting computed durations.
     */
    public void finalizeExam() {

        this.completionTime = System.currentTimeMillis();
    }

//    /**
//     * Gets the maximum problem ID in the exam.
//     *
//     * @return the maximum problem ID
//     */
//    public int getMaxId() {
//
//        // If not yet realized, return 0
//        if (this.problemById == null) {
//            return 0;
//        }
//
//        return this.problemById.length - 1;
//    }

    /**
     * Gets the problem for a given ID.
     *
     * @param id the problem ID
     * @return the problem
     */
    public ExamProblem getProblem(final int id) {

        return this.problemById[id];
    }

    /**
     * Sets the exam's background color.
     *
     * @param name  the background color name
     * @param color the background color
     */
    public void setBackgroundColor(final String name, final Color color) {

        this.backgroundColorName = name;
        this.backgroundColor = color;
    }

    /**
     * Realizes this exam, substituting any computed parameter values for parameter tags in source XML, generating the
     * instructions Doc object, and realizing each exam section, subtest, grading rule and outcome.
     *
     * @param isRemote    {@code true} if the exam is being taken remotely
     * @param isProctored {@code true} if the exam is being taken in an unproctored setting
     * @param serial      the serial number for the realized exam
     * @return {@code true} if realization succeeds; {@code false} otherwise
     */
    public boolean realize(final boolean isRemote, final boolean isProctored, final long serial) {

        boolean result = true;

        this.remote = isRemote;
        this.proctored = isProctored;
        this.serialNumber = Long.valueOf(serial);
        this.realizationTime = System.currentTimeMillis();

        // Realize each section
        for (final ExamSection sect : this.examSections) {
            if (sect != null) {
                if (!sect.realize(this.evalContext)) {
                    Log.warning("Failed to realize section");
                    result = false;
                    break;
                }
            } else {
                Log.warning("Section was null while realizing exam");
                result = false;
                break;
            }
        }

        if (result) {
            for (final ExamSubtest subtest : this.examSubtests) {
                if (subtest != null) {
                    if (!subtest.realize(this.evalContext)) {
                        Log.warning("Failed to realize subtest");
                        result = false;
                        break;
                    }
                } else {
                    Log.warning("Subtest was null while realizing exam");
                    result = false;
                    break;
                }
            }
        }

        if (result) {
            for (final ExamGradingRule rule : this.examGradingRules) {
                if (rule != null) {
                    if (!rule.realize(this.evalContext)) {
                        Log.warning("Failed to realize grading rule");
                        result = false;
                        break;
                    }
                } else {
                    Log.warning("Grading rule was null while realizing exam");
                    result = false;
                    break;
                }
            }
        }

        if (result) {
            for (final ExamOutcome outcome : this.examOutcomes) {
                if (outcome != null) {
                    if (!outcome.realize(this.evalContext)) {
                        Log.warning("Failed to realize outcome");
                        result = false;
                        break;
                    }
                } else {
                    Log.warning("Outcome was null while realizing exam");
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Builds the array of selected problems in index order.
     */
    void generateProblemList() {

        int max = 0;

        final int numSections = getNumSections();

        // Determine the maximum ID value in the list of problems
        for (int i = 0; i < numSections; i++) {
            final ExamSection section = getSection(i);

            final int numProblems = section.getNumProblems();
            for (int j = 0; j < numProblems; j++) {
                final ExamProblem problem = section.getProblem(j);

                if (problem.problemId > max) {
                    max = problem.problemId;
                }
            }
        }

        // Allocate an array to be indexed on ID (need max + 1 elements since arrays start at
        // index 0)
        this.problemById = new ExamProblem[max + 1];

        // Populate the array (note that the array may be sparse - there need not be a problem at
        // each index.)
        for (int i = 0; i < numSections; i++) {
            final ExamSection section = getSection(i);

            final int numProblems = section.getNumProblems();
            for (int j = 0; j < numProblems; j++) {
                final ExamProblem problem = section.getProblem(j);

                if (problem != null) {
                    this.problemById[problem.problemId] = problem;
                }
            }
        }
    }

    /**
     * Appends the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void appendXml(final HtmlBuilder xml, final int indent) {

        final String ind = makeIndent(indent);

        xml.add(ind, "<exam");
        writeAttribute(xml, "name", this.examName);
        writeAttribute(xml, "course", this.course);
        writeAttribute(xml, "unit", this.courseUnit);
        writeAttribute(xml, "version", this.examVersion);
        writeAttribute(xml, "time-limit", this.allowedSeconds);

        if (this.creationTime > 0L) {
            writeAttribute(xml, "created", Long.toString(this.creationTime));
        }

        if (this.realizationTime > 0L) {
            writeAttribute(xml, "realized", Long.toString(this.realizationTime));
        }

        if (this.presentationTime > 0L) {
            writeAttribute(xml, "presented", Long.toString(this.presentationTime));
        }

        if (this.completionTime > 0L) {
            writeAttribute(xml, "completed", Long.toString(this.completionTime));
        }

        if (this.serialNumber != null) {
            writeAttribute(xml, "serial-number", this.serialNumber);
        }

        if (this.remote) {
            xml.add(" remote='Y'");
        }

        if (this.proctored) {
            xml.add(" proctored='Y'");
        }

        writeAttribute(xml, "current-section", this.currentSection);
        writeAttribute(xml, "current-problem", this.currentProblem);

        if (this.backgroundColorName != null) {
            writeAttribute(xml, "bg-color", this.backgroundColorName);
        }

        xml.addln('>');

        if (this.ref != null) {
            xml.addln(ind, " <ref-base>", this.ref, "</ref-base>");
        }

        if (this.refRoot != null) {
            xml.addln(ind, " <reference-root>", this.refRoot,
                    "</reference-root>");
        }

        if (this.instructions != null) {
            this.instructions.toXml(xml, indent + 1);
        }

        for (final ExamSection section : this.examSections) {
            section.appendXml(xml, indent);
        }

        for (final ExamSubtest subtest : this.examSubtests) {
            subtest.appendXml(xml, indent);
        }

        for (final ExamGradingRule rule : this.examGradingRules) {
            rule.appendXml(xml, indent);
        }

        for (final ExamOutcome outcome : this.examOutcomes) {
            outcome.appendXml(xml, indent);
        }

        xml.add(ind);
        xml.addln("</exam>");
    }

    /**
     * Generates the current state of the exam as an array of object arrays. The [0] element is an array of two Integer
     * objects, with [0][0] storing the section (0-based) index of the currently selected section, and [0][1] storing
     * the (0-based) index of the currently selected problem. Then the [1] through [N] elements are the student
     * responses to problems 1 through N, where answers are stored in the order they are presented in the exam. A null
     * object array indicates an unanswered problem.
     *
     * @return the current state of the exam
     */
    public Object[][] exportState() {

        final Object[][] state = new Object[this.problemById.length][];

        // Allocate storage for current selection, start time
        state[0] = new Object[]{this.serialNumber, LONG_ZERO, null, null};

        if (this.presentationTime > 0L) {
            state[0][2] = Long.valueOf(this.presentationTime);
        }
        if (this.completionTime > 0L) {
            state[0][3] = Long.valueOf(this.completionTime);
        }

        // Populate the problem states, and current selection
        final int count = this.problemById.length;
        for (int i = 1; i < count; ++i) {

            if (this.problemById[i] != null) {
                final AbstractProblemTemplate prob = this.problemById[i].getSelectedProblem();

                if (prob != null) {
                    state[i] = prob.getAnswer();
                }
            }
        }

        return state;
    }

    /**
     * Imports an exam state and configure the exam to match.
     *
     * @param state the exam state to import
     * @return {@code true} if successful; {@code false} if unsuccessful
     */
    public boolean importState(final Object[][] state) {

        // Verify state contains correct number of responses
        if ((this.problemById.length != state.length) || (state[0].length != 4)) {
            Log.warning("State to be imported is not expected size");

            return false;
        }

        // Extract presentation time
        if ((state[0][2] != null) && (state[0][2] instanceof Long)) {
            this.presentationTime = ((Long) state[0][2]).longValue();
        }

        // Extract completion time
        if ((state[0][3] != null) && (state[0][3] instanceof Long)) {
            this.completionTime = ((Long) state[0][3]).longValue();
        }

        // Extract serial number
        if ((state[0][0] != null) && (state[0][0] instanceof Long)) {
            this.serialNumber = (Long) state[0][0];
        }

        this.currentSection = INT_ZERO;
        this.currentProblem = INT_ZERO;

        // Populate the problem states
        final int len = this.problemById.length;
        for (int i = 1; i < len; ++i) {

            if (this.problemById[i] != null) {
                final AbstractProblemTemplate prob = this.problemById[i].getSelectedProblem();

                if (prob != null) {
                    if (state[i] != null) {
                        prob.recordAnswer(state[i]);
                    }
                } else {
                    Log.warning("No problem " + i + " selected!");
                }
            } else {
                Log.warning("No problem " + i);
            }
        }

        return true;
    }

    /**
     * Generates a LaTeX file with the contents of the randomized exam. This file can be printed for students needing a
     * paper copy, can be converted to a PDF for viewing, or can be translated to Braille for blind students.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file (the value should be updated if the method writes any files)
     * @param overwriteAll a 1-boolean array whose only entry contains {@code true} if the user has selected "overwrite
     *                     all"; {@code false} to ask the user each time (this method can update this value to
     *                     {@code true} if it is {@code false} and the user is asked "Overwrite? [YES] [ALL] [NO]" and
     *                     chooses [ALL])
     * @return the LaTeX representation of the exam
     */
    public String examToLaTeX(final File dir, final int[] fileIndex, final boolean[] overwriteAll) {

        final HtmlBuilder builder = new HtmlBuilder(1000);

        doLaTeXHeader(builder);

        // Exam title
        builder.addln("\\title{", this.examName, "}");
        builder.addln("\\maketitle");
        builder.addln();

        final char[] mode = {'T', ' '};

        // Instructions, if applicable
        if (this.instructions != null) {
            this.instructions.toLaTeX(dir, fileIndex, overwriteAll, builder, false, mode,
                    this.evalContext);
        }
        builder.addln("\\clearpage");
        builder.addln(CoreConstants.EMPTY);

        // Go through each section in turn
        for (final ExamSection section : this.examSections) {

            // Print section headings only if more than 1 section
            if (this.examSections.size() > 1) {
                builder.addln("\\[\\rule{0pt}{16pt}\\]");
                builder.addln("\\section*{", section.sectionName, "}");
            }

            final int len = section.getNumProblems();

            for (int i = 0; i < len; i++) {
                final ExamProblem eprob = section.getPresentedProblem(i);
                final AbstractProblemTemplate prob = eprob.getSelectedProblem();

                // Prepend the problem name (likely a number) in boldface
                builder.addln("\\[\\rule{0pt}{16pt}\\]");
                builder.addln(CoreConstants.EMPTY);
                builder.addln("\\begin{samepage}");
                builder.addln("\\noindent \\begin{bfseries}", eprob.problemName,
                        ".\\end{bfseries}");
                prob.toLaTeX(dir, fileIndex, overwriteAll, builder, false, mode, this.evalContext);
                builder.addln("\\end{samepage}");
            }
        }

        doLaTeXFooter(builder);

        return builder.toString();
    }

    /**
     * Generates a LaTeX file with the answer key for the randomized exam. This file can be printed for students needing
     * a paper copy, can be converted to a PDF for viewing, or can be translated to Braille for blind students.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file (the value should be updated if the method writes any files)
     * @param overwriteAll a 1-boolean array whose only entry contains {@code true} if the user has selected "overwrite
     *                     all"; {@code false} to ask the user each time (this method can update this value to
     *                     {@code true} if it is {@code false} and the user is asked "Overwrite? [YES] [ALL] [NO]" and
     *                     chooses [ALL])
     * @return the LaTeX representation of the answer key
     */
    public String answersToLaTeX(final File dir, final int[] fileIndex,
                                 final boolean[] overwriteAll) {

        final HtmlBuilder builder = new HtmlBuilder(1000);
        final char[] mode = {'T', ' '};

        doLaTeXHeader(builder);

        // Answer key title
        builder.add("\\title{");
        builder.add(this.examName);
        builder.addln(" Answer Key}");
        builder.addln("\\maketitle");
        builder.addln(CoreConstants.EMPTY);

        // Go through each section in turn
        for (final ExamSection section : this.examSections) {

            // Print section headings only if more than 1 section
            if (this.examSections.size() > 1) {
                builder.add("\\section*{");
                builder.add(section.sectionName);
                builder.addln("}");
            }

            final int len = section.getNumProblems();

            for (int i = 0; i < len; i++) {
                final ExamProblem eprob = section.getPresentedProblem(i);
                final AbstractProblemTemplate prob = eprob.getSelectedProblem();

                // Prepend the problem name (likely a number) in boldface
                builder.addln("\\[\\rule{0pt}{16pt}\\]");
                builder.addln("\\begin{samepage}");
                builder.add("\\noindent \\begin{bfseries}");
                builder.add(eprob.problemName);
                builder.addln(".\\end{bfseries}");
                prob.toLaTeX(dir, fileIndex, overwriteAll, builder, true, mode, this.evalContext);
                builder.addln("\\end{samepage}");
            }
        }

        doLaTeXFooter(builder);

        return builder.toString();
    }

    /**
     * Generates a LaTeX file with the detailed solutions key for the randomized exam. This file can be printed for
     * students needing a paper copy, can be converted to a PDF for viewing, or can be translated to Braille for blind
     * students.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file (the value should be updated if the method writes any files)
     * @param overwriteAll a 1-boolean array whose only entry contains {@code true} if the user has selected "overwrite
     *                     all"; {@code false} to ask the user each time (this method can update this value to
     *                     {@code true} if it is {@code false} and the user is asked "Overwrite? [YES] [ALL] [NO]" and
     *                     chooses [ALL])
     * @return the LaTeX representation of the annotated solutions
     */
    public String solutionsToLaTeX(final File dir, final int[] fileIndex,
                                   final boolean[] overwriteAll) {

        final HtmlBuilder builder = new HtmlBuilder(1000);
        final char[] mode = {'T', ' '};

        doLaTeXHeader(builder);

        // Answer key title
        builder.add("\\title{");
        builder.add(this.examName);
        builder.addln(" Solutions}");
        builder.addln("\\maketitle");
        builder.addln(CoreConstants.EMPTY);

        // Go through each section in turn
        for (final ExamSection section : this.examSections) {

            // Print section headings only if more than 1 section
            if (this.examSections.size() > 1) {
                builder.add("\\section*{");
                builder.add(section.sectionName);
                builder.addln("}");
            }

            final int len = section.getNumProblems();

            for (int i = 0; i < len; i++) {
                final ExamProblem eprob = section.getPresentedProblem(i);
                final AbstractProblemTemplate prob = eprob.getSelectedProblem();

                // Prepend the problem name (likely a number) in boldface
                builder.addln("\\[\\rule{0pt}{16pt}\\]");
                builder.addln("\\begin{samepage}");
                builder.add("\\noindent \\begin{bfseries}");
                builder.add(eprob.problemName);
                builder.addln(".\\end{bfseries}");
                prob.toLaTeX(dir, fileIndex, overwriteAll, builder, true, mode, this.evalContext);

                if (prob.solution != null) {
                    prob.solution.toLaTeX(dir, fileIndex, overwriteAll, builder, true, mode,
                            this.evalContext);
                }

                builder.addln("\\end{samepage}");
            }
        }

        doLaTeXFooter(builder);

        return builder.toString();
    }

    /**
     * Generates the header for the top of an output LaTeX file.
     *
     * @param builder the {@code HtmlBuilder} to which to append the header
     */
    private static void doLaTeXHeader(final HtmlBuilder builder) {

        builder.add("\\documentclass[14pt]{amsart}", CoreConstants.CRLF);
        builder.add("\\usepackage{extsizes}", CoreConstants.CRLF);
        builder.add("\\usepackage{amssymb,latexsym,xy,graphicx}", CoreConstants.CRLF);
        builder.add("\\pagestyle{empty}", CoreConstants.CRLF);
        builder.add("\\setlength{\\topmargin}{-.25in}", CoreConstants.CRLF);
        builder.add("\\setlength{\\textheight}{9in}", CoreConstants.CRLF);
        builder.add("\\oddsidemargin 0pt", CoreConstants.CRLF);
        builder.add("\\evensidemargin 0pt", CoreConstants.CRLF);
        builder.add("\\setlength{\\textwidth}{6.5in}", CoreConstants.CRLF, CoreConstants.CRLF);

        builder.add("\\newlength{\\digitwidth}", CoreConstants.CRLF);
        builder.add("\\settowidth{\\digitwidth}{0}", CoreConstants.CRLF);

        builder.add("\\begin{document}", CoreConstants.CRLF);
        builder.add("\\xyoption{frame}", CoreConstants.CRLF, CoreConstants.CRLF);
    }

    /**
     * Generates the footer for the top of an output LaTeX file.
     *
     * @param builder the {@code HtmlBuilder} to which to append the footer
     */
    private static void doLaTeXFooter(final HtmlBuilder builder) {

        builder.addln("\\end{document}");
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return EqualityTests.objectHashCode(this.evalContext)
                + EqualityTests.objectHashCode(this.ref)
                + EqualityTests.objectHashCode(this.refRoot)
                + EqualityTests.objectHashCode(this.examName)
                + EqualityTests.objectHashCode(this.course)
                + EqualityTests.objectHashCode(this.courseUnit)
                + EqualityTests.objectHashCode(this.examVersion)
                + EqualityTests.objectHashCode(this.instructions)
                + EqualityTests.objectHashCode(this.allowedSeconds)
                + EqualityTests.objectHashCode(this.examSections)
                + EqualityTests.objectHashCode(this.examSubtests)
                + EqualityTests.objectHashCode(this.examGradingRules)
                + EqualityTests.objectHashCode(this.examOutcomes)
                + EqualityTests.objectHashCode(this.backgroundColorName)
                + EqualityTests.objectHashCode(this.backgroundColor)
                + EqualityTests.objectHashCode(this.serialNumber);
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param obj the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final ExamObj problem) {
            equal = Objects.equals(this.evalContext, problem.evalContext)
                    && Objects.equals(this.ref, problem.ref)
                    && Objects.equals(this.refRoot, problem.refRoot)
                    && Objects.equals(this.examName, problem.examName)
                    && Objects.equals(this.course, problem.course)
                    && Objects.equals(this.courseUnit, problem.courseUnit)
                    && Objects.equals(this.examVersion, problem.examVersion)
                    && Objects.equals(this.instructions, problem.instructions)
                    && Objects.equals(this.allowedSeconds, problem.allowedSeconds)
                    && Objects.equals(this.examSections, problem.examSections)
                    && Objects.equals(this.examSubtests, problem.examSubtests)
                    && Objects.equals(this.examGradingRules, problem.examGradingRules)
                    && Objects.equals(this.examOutcomes, problem.examOutcomes)
                    && Objects.equals(this.backgroundColorName, problem.backgroundColorName)
                    && Objects.equals(this.backgroundColor, problem.backgroundColor)
                    && Objects.equals(this.serialNumber, problem.serialNumber);
        } else {
            equal = false;
        }

        return equal;
    }
}
