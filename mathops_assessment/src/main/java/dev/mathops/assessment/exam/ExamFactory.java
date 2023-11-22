package dev.mathops.assessment.exam;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.FormulaFactory;
import dev.mathops.assessment.formula.XmlFormulaFactory;
import dev.mathops.assessment.problem.template.ProblemDummyTemplate;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.CData;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.core.parser.xml.IElement;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;
import dev.mathops.core.parser.xml.XmlContent;
import dev.mathops.core.parser.xml.XmlContentError;
import dev.mathops.core.ui.ColorNames;

import java.awt.Color;
import java.util.List;

/**
 * A factory class to load {@code Exam} objects from XML files. This class offers only static methods to generate
 * {@code Exam} objects from a {@code Reference}, a {@code File}, a {@code URL}, or a {@code String}. All the load
 * methods generate a {@code Exam} object even if there is an error in loading. The {@code Exam} object will contain the
 * list of errors from the load process. This prevents error state from having to be maintained in this class.
 */
public enum ExamFactory {
    ;

    /**
     * Loads the exam from an {@code XmlContent}.
     *
     * @param content the {@code XmlContent} from which to load the exam
     * @param mode    the parser mode
     * @return the loaded {@code Exam} object on success, or {@code null} on failure
     */
    public static ExamObj load(final XmlContent content, final EParserMode mode) {

        ExamObj exam;

        try {
            exam = sourceFileToExam(content, mode);

            final List<XmlContentError> allErrors = content.getAllErrors();
            if (!allErrors.isEmpty()) {
                Log.warning("Errors loading exam:");
                for (final XmlContentError err : allErrors) {
                    Log.warning("    ", err);
                }
            }

            // qualityControl(source, exam);
            // realizationTest(source, exam);
        } catch (final Exception ex) {
            Log.warning(Res.get(Res.EXCEPT_LOADING_EXAM), ex);
            exam = null;
        }

        return exam;
    }

    /**
     * Generate the exam from a {@code String} containing XML source.
     *
     * @param xml  the {@code String} containing the exam source XML
     * @param mode the parser mode
     * @return the loaded {@code Exam} object on success, or an empty {@code Exam} object containing the set of errors
     *         encountered on failure
     */
    public static ExamObj load(final String xml, final EParserMode mode) {

        ExamObj exam;

        try {
            final XmlContent source = new XmlContent(xml, false, false);
            exam = sourceFileToExam(source, mode);

            final List<XmlContentError> allErrors = source.getAllErrors();
            if (!allErrors.isEmpty()) {
                Log.warning("Errors loading exam:");
                for (final XmlContentError err : allErrors) {
                    Log.warning("    ", err);
                }
            }

            // TODO: Use these for testing
            // qualityControl(source, exam);
            // realizationTest(source, exam);
        } catch (final Exception ex) {
            Log.warning(Res.get(Res.EXCEPT_LOADING_EXAM), ex);
            exam = null;
        }

        return exam;
    }

    ///**
    // * Perform quality control checks on exams.
    // *
    // * @param content the XML content
    // * @param exam the exam
    // */
    // private static void qualityControl(final XmlContent content, final ExamObj exam) {
    //
    // // Check 1: Does filename agree with reference?
    //
    // String path = source.getOrigin().replace('/', '.').replace('\\', '.');
    // int end = path.length();
    // if (path.endsWith(".xml")) { 
    // end -= 4;
    // path = path.substring(0, end);
    // }
    // final int mathIndex = path.indexOf("math."); 
    // if (mathIndex != -1) {
    // path = path.substring(mathIndex);
    // }
    //
    // final String ref = exam.getIdentifierReference().getRef();
    //
    // if (!("String".equals(path) || path.equals(ref))) { 
    // Log.warning("PATH = [", source.getOrigin(), 
    // "] REF = [", ref, "]");
    // }
    //
    // // Review and unit exams should not have multiple sections
    //
    // final String ver = exam.getExamVersion();
    //
    // if (ver.endsWith("UE") || ver.endsWith("RE")) {
    // if (exam.getNumSections() > 1) {
    // Log.warning("PATH = [", source.getOrigin(), 
    // "] Exam has multiple sections"); 
    // }
    // }
    //
    // }

    /**
     * Generates an {@code Exam} object from a {@code XmlSource}, which may or may not have loaded successfully. On any
     * errors, return a default {@code Exam} object containing the set of errors.
     *
     * @param content the {@code XmlContent} from which to load the {@code Exam}
     * @param mode    the parser mode
     * @return the loaded {@code Exam} object on success, or an empty {@code Exam} object on failure, in which case the
     *         {@code XmlContent} will contain error messages
     */
    private static ExamObj sourceFileToExam(final XmlContent content, final EParserMode mode) {

        ExamObj exam = createFromSource(content, mode);

        // On failure, create a default TemplateExam containing load errors.
        if (exam == null) {
            exam = new ExamObj();
        }

        return exam;
    }

    /**
     * Generates the {@code Exam} object from the source XML. Any errors encountered are logged in the
     * {@code XmlContent} object.
     *
     * @param content the {@code XmlContent} containing the source XML
     * @param mode    the parser mode
     * @return the loaded {@code Exam}, or {@code null} on any error
     */
    private static ExamObj createFromSource(final XmlContent content, final EParserMode mode) {

        ExamObj exam = null;

        final IElement top = content.getToplevel();

        if (!"exam".equals(top.getTagName())) {
            content.logError(top, "Failed to find top-level <exam> element.");
        } else if (!(top instanceof final NonemptyElement nonempty)) {
            content.logError(top, "Top-level <exam> element must be non-empty.");
        } else {
            boolean valid = true;
            final String examName = nonempty.getStringAttr("name");
            final String course = nonempty.getStringAttr("course");
            final String courseUnit = nonempty.getStringAttr("unit");
            final String examVersion = nonempty.getStringAttr("version");
            final String timeLimitStr = nonempty.getStringAttr("time-limit");
            final String createdStr = nonempty.getStringAttr("created");
            final String realizedStr = nonempty.getStringAttr("realized");
            final String presentedStr = nonempty.getStringAttr("presented");
            final String completedStr = nonempty.getStringAttr("completed");
            final String remoteStr = nonempty.getStringAttr("remote");
            final String proctoredStr = nonempty.getStringAttr("proctored");
            final String serialNumberStr = nonempty.getStringAttr("serial-number");
            final String bgColorStr = nonempty.getStringAttr("bg-color");

            Long allowedSeconds = null;
            Long creationTime = null;
            Long realizationTime = null;
            Long presentationTime = null;
            Long completionTime = null;
            Long serialNumber = null;
            Color bgColor = null;

            if (examName == null) {
                content.logError(top, "Missing 'name' attribute on <exam> element.");
                valid = false;
            }
            if (course == null) {
                content.logError(top, "Missing 'course' attribute on <exam> element.");
                valid = false;
            }
            if (courseUnit == null) {
                content.logError(top, "Missing 'unit' attribute on <exam> element.");
                valid = false;
            }
            if (examVersion == null) {
                content.logError(top, "Missing 'version' attribute on <exam> element.");
                valid = false;
            }
            if (timeLimitStr != null) {
                try {
                    allowedSeconds = Long.valueOf(timeLimitStr);
                } catch (final NumberFormatException ex) {
                    content.logError(top, "Missing 'time-limit' attribute on <exam> element.");
                    valid = false;
                }
            }
            if (createdStr != null) {
                try {
                    creationTime = Long.valueOf(createdStr);
                } catch (final NumberFormatException ex) {
                    content.logError(top, "Invalid 'created' attribute on <exam> element.");
                    valid = false;
                }
            }
            if (realizedStr != null) {
                try {
                    realizationTime = Long.valueOf(realizedStr);
                } catch (final NumberFormatException ex) {
                    content.logError(top, "Invalid 'realized' attribute on <exam> element.");
                    valid = false;
                }
            }
            if (presentedStr != null) {
                try {
                    presentationTime = Long.valueOf(presentedStr);
                } catch (final NumberFormatException ex) {
                    content.logError(top, "Invalid 'presented' attribute on <exam> element.");
                    valid = false;
                }
            }
            if (completedStr != null) {
                try {
                    completionTime = Long.valueOf(completedStr);
                } catch (final NumberFormatException ex) {
                    content.logError(top, "Invalid 'completed' attribute on <exam> element.");
                    valid = false;
                }
            }
            if (serialNumberStr != null) {
                try {
                    serialNumber = Long.valueOf(serialNumberStr);
                } catch (final NumberFormatException ex) {
                    content.logError(top, "Invalid 'serial-number' attribute on <exam> element.");
                    valid = false;
                }
            }
            if (bgColorStr != null) {
                if (ColorNames.isColorNameValid(bgColorStr)) {
                    bgColor = ColorNames.getColor(bgColorStr);
                } else {
                    content.logError(top, "Invalid 'bg-color' attribute value on <exam> element.");
                    valid = false;
                }
            }

            if (valid) {
                exam = new ExamObj();
                exam.examName = examName;
                exam.course = course;
                exam.courseUnit = courseUnit;
                exam.examVersion = examVersion;
                exam.allowedSeconds = allowedSeconds;
                exam.creationTime = creationTime == null ? 0L : creationTime.longValue();
                exam.realizationTime = realizationTime == null ? 0L : realizationTime.longValue();
                exam.presentationTime = presentationTime == null ? 0L : presentationTime.longValue();
                exam.completionTime = completionTime == null ? 0L : completionTime.longValue();
                exam.serialNumber = serialNumber;

                if ("Y".equals(remoteStr)) {
                    exam.remote = true;
                }
                if ("Y".equals(proctoredStr)) {
                    exam.proctored = true;
                }
                if (bgColor != null) {
                    exam.setBackgroundColor(bgColorStr, bgColor);
                }

                for (final IElement child : nonempty.getElementChildrenAsList()) {
                    final String tag = child.getTagName();
                    if (child instanceof final NonemptyElement childElem) {

                        if ("ref-base".equals(tag)) {
                            valid = parseRefBase(childElem, exam);
                        } else if ("reference-root".equals(tag)) {
                            valid = parseReferenceRoot(childElem, exam);
                        } else if ("instructions".equals(tag)) {
                            if (exam.instructions == null) {
                                valid = parseInstructions(childElem, exam, mode);
                            } else {
                                content.logError(top, "Multiple <instructions> elements in <exam> element.");
                                valid = false;
                            }
                        } else if ("exam-section".equals(tag)) {
                            valid = parseSection(childElem, exam);
                        } else if ("subtest".equals(tag)) {
                            valid = parseSubtest(childElem, exam);
                        } else if ("grading-rule".equals(tag)) {
                            valid = parseGradingRule(childElem, exam, mode);
                        } else if ("outcome".equals(tag)) {
                            valid = parseOutcome(exam.getEvalContext(), childElem, exam, mode);
                        } else {
                            content.logError(top, "Unexpected '" + tag + "' element in <exam> element.");
                            valid = false;
                        }

                        if (!valid) {
                            break;
                        }
                    } else {
                        content.logError(top, "Unexpected empty '" + tag + "' element in <exam> element.");
                        valid = false;
                    }
                }

                if (exam.ref == null) {
                    content.logError(top, "Missing <ref-base> child of <exam> element.");
                    valid = false;
                }

                if (exam.refRoot == null) {
                    content.logError(top, "Missing <reference-root> child of <exam> element.");
                    valid = false;
                }

                if (valid) {
                    // Build a selected problem list, if this exam is realized
                    exam.generateProblemList();
                } else {
                    exam = null;
                }
            }
        }

        return exam;
    }

    /**
     * Parses the "ref-base" child element.
     *
     * @param elem the element
     * @param exam the exam to which to add the ref base if found
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseRefBase(final NonemptyElement elem, final ExamObj exam) {

        boolean valid = false;

        final int count = elem.getNumChildren();
        if (count == 1) {
            final INode child = elem.getChild(0);
            if (child instanceof final CData cdata) {
                exam.ref = cdata.content;
                valid = true;
            } else {
                elem.logError("<ref-base> element must have simple text content (found " + child.getClass().getName()
                        + ")");
            }
        } else {
            elem.logError("<ref-base> element must have only text content (found " + count + " child nodes)");
        }

        return valid;
    }

    /**
     * Parses the "reference-root" child element.
     *
     * @param elem the element
     * @param exam the exam to which to add the reference root if found
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseReferenceRoot(final NonemptyElement elem, final ExamObj exam) {

        boolean valid = false;

        final int count = elem.getNumChildren();
        if (count == 1) {
            final INode child = elem.getChild(0);
            if (child instanceof final CData cdata) {
                exam.refRoot = cdata.content;
                valid = true;
            } else {
                elem.logError("<reference-root> element must have simple text content (found "
                        + child.getClass().getName() + ")");
            }
        } else {
            elem.logError("<reference-root> element must have only text content (found " + count + " child nodes)");
        }

        return valid;
    }

    /**
     * Parses the (optional) instructions object in the exam from a source XML element. Any errors encountered are
     * logged in the {@code NonemptyElement} object.
     *
     * @param elem the element
     * @param exam the exam to which to add any found instructions
     * @param mode the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseInstructions(final NonemptyElement elem, final ExamObj exam, final EParserMode mode) {

        boolean valid = false;

        final DocColumn instructions = DocFactory.parseDocColumn(new EvalContext(), elem, mode);

        if (instructions == null) {
            Log.warning("No instructions");
        } else {
            instructions.tag = "instructions";
            exam.instructions = instructions;
            valid = true;
        }

        return valid;
    }

    /**
     * Parses an exam section definition from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param elem the element
     * @param exam the exam to which to add any found sections
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseSection(final NonemptyElement elem, final ExamObj exam) {

        boolean valid = true;

        final String sectionName = elem.getStringAttr("name");
        final String shortName = elem.getStringAttr("short-name");
        final String randomOrderStr = elem.getStringAttr("random-order");
        final String canComeBackStr = elem.getStringAttr("can-come-back");
        final String canRegenerateStr = elem.getStringAttr("can-regenerate");
        final String minMoveonScoreStr = elem.getStringAttr("min-moveon-score");
        final String minMasteryScoreStr = elem.getStringAttr("min-mastery-score");
        final String resourcesStr = elem.getStringAttr("resources");
        final String orderStr = elem.getStringAttr("order");

        if (sectionName == null) {
            elem.logError("Missing 'name' attribute in <exam-section> element.");
            valid = false;
        }

        boolean isRandom = false;
        if (randomOrderStr != null) {
            if ("true".equalsIgnoreCase(randomOrderStr)) {
                isRandom = true;
            } else if (!"false".equalsIgnoreCase(randomOrderStr)) {
                elem.logError("Invalid 'random-order' attribute in <exam-section> element - assuming 'false'.");
            }
        }

        boolean canComeBack = true;
        if (canComeBackStr != null) {
            if ("false".equalsIgnoreCase(canComeBackStr)) {
                canComeBack = false;
            } else if (!"true".equalsIgnoreCase(canComeBackStr)) {
                elem.logError("Invalid 'can-come-back' attribute in <exam-section> element - assuming 'true'.");
            }
        }

        boolean canRegenerate = true;
        if (canRegenerateStr != null) {
            if ("false".equalsIgnoreCase(canRegenerateStr)) {
                canRegenerate = false;
            } else if (!"true".equalsIgnoreCase(canRegenerateStr)) {
                elem.logError("Invalid 'can-regenerate' attribute in <exam-section> element - assuming 'true'.");
            }
        }

        Long minMoveon = null;
        if (minMoveonScoreStr != null) {
            try {
                minMoveon = Long.valueOf(minMoveonScoreStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'min-moveon-score' attribute in <exam-section> element.");
                valid = false;
            }
        }

        Long minMastery = null;
        if (minMasteryScoreStr != null) {
            try {
                minMastery = Long.valueOf(minMasteryScoreStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'min-mastery-score' attribute in <exam-section> element.");
                valid = false;
            }
        }

        final ExamSection section;

        if (valid) {
            section = new ExamSection();
            section.sectionName = sectionName;
            section.shortName = shortName;
            section.randomOrder = isRandom;
            section.canComeBack = canComeBack;
            section.canRegenerate = canRegenerate;
            section.minMoveonScore = minMoveon;
            section.minMasteryScore = minMastery;

            if (resourcesStr != null) {
                final String[] strings = resourcesStr.split(CoreConstants.COMMA);
                for (final String str : strings) {
                    // If the resource name is valid, add it
                    if (!section.addResource(str)) {
                        elem.logError("Invalid 'resource' value in <exam-section> element.");
                        valid = false;
                    }
                }
            }

            if (orderStr != null) {
                final String[] strings = orderStr.split(CoreConstants.COMMA);
                final int count = strings.length;
                final int[] order = new int[count];
                for (int i = 0; i < count; i++) {
                    try {
                        order[i] = Integer.parseInt(strings[i]);
                    } catch (final NumberFormatException e) {
                        elem.logError("Invalid 'order' value in <exam-section> element.");
                        valid = false;
                    }
                }

                section.setProblemOrder(order);
            }

            for (final IElement child : elem.getElementChildrenAsList()) {
                final String tag = child.getTagName();
                if (child instanceof final NonemptyElement childElem) {

                    if ("exam-problem".equals(tag)) {
                        valid = parseProblem(childElem, section, exam);
                    } else {
                        elem.logError("Unexpected '" + tag + "' element in <exam-section> element.");
                        valid = false;
                    }

                    if (!valid) {
                        break;
                    }
                } else {
                    elem.logError("Unexpected empty '" + tag + "' element in <exam-section> element.");
                    valid = false;
                }
            }

            exam.addSection(section);
        }

        return valid;
    }

    /**
     * Parses an exam problem definition from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param elem    the element
     * @param section the {@code ExamSection} to which to add the parsed object
     * @param exam    the {@code ExamObj} that will own the generated problem
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseProblem(final NonemptyElement elem, final ExamSection section, final ExamObj exam) {

        boolean valid = true;

        final String idStr = elem.getStringAttr("id");
        final String problemName = elem.getStringAttr("name");
        final String mandatoryStr = elem.getStringAttr("mandatory");
        final String pointsStr = elem.getStringAttr("points");
        final String selected = elem.getStringAttr("selected");

        if (idStr == null) {
            elem.logError("Missing 'id' attribute in <exam-problem> element.");
            valid = false;
        }

        int problemId = 0;
        if (idStr != null) {
            try {
                problemId = Integer.parseInt(idStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'id' attribute in <exam-problem> element.");
                valid = false;
            }
        }

        boolean isMandatory = false;
        if (mandatoryStr != null) {
            if ("true".equalsIgnoreCase(mandatoryStr)) {
                isMandatory = true;
            } else if (!"false".equalsIgnoreCase(mandatoryStr)) {
                elem.logError("Invalid 'mandatory' attribute in <exam-problem> element - assuming 'false'.");
            }
        }

        double points = -1.0;
        if (pointsStr != null) {
            try {
                points = Double.parseDouble(pointsStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'points' attribute in <exam-problem> element.");
                valid = false;
            }
        }

        final ExamProblem problem;

        if (valid) {
            problem = new ExamProblem(exam);
            problem.problemId = problemId;
            problem.problemName = problemName;
            problem.mandatory = isMandatory;

            if (points >= 0.0) {
                problem.numPoints = Double.valueOf(points);
            }

            if (selected != null) {
                problem.setSelectedRef(selected);
            }

            for (final IElement child : elem.getElementChildrenAsList()) {
                final String tag = child.getTagName();
                if (child instanceof final NonemptyElement childElem) {

                    if ("reference".equals(tag)) {
                        valid = parseReference(childElem, problem);
                    } else {
                        elem.logError("Unexpected '" + tag + "' element in <exam-problem> element.");
                        valid = false;
                    }

                    if (!valid) {
                        break;
                    }
                } else {
                    elem.logError("Unexpected empty '" + tag + "' element in <exam-problem> element.");
                    valid = false;
                }
            }

            section.addProblem(problem);
        }

        return valid;
    }

    /**
     * Parses a problem reference from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param elem    the element
     * @param problem the {@code ExamProblem} to which to add the parsed reference
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseReference(final NonemptyElement elem, final ExamProblem problem) {

        boolean valid = false;

        final int count = elem.getNumChildren();
        if (count == 1) {
            final INode child = elem.getChild(0);
            if (child instanceof final CData cdata) {
                // Store a dummy problem to retain the reference value
                final ProblemDummyTemplate prob = new ProblemDummyTemplate();
                prob.ref = cdata.content;
                problem.addProblem(prob);

                valid = true;
            } else {
                elem.logError("<reference> element must have simple text content (found " + child.getClass().getName()
                        + ")");
            }
        } else {
            elem.logError("<reference> element must have only text content (found " + count + " child nodes)");
        }

        return valid;
    }

    /**
     * Parses an exam subtest definition from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param elem the element
     * @param exam the {@code Exam} to which to add parsed subtests
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseSubtest(final NonemptyElement elem, final ExamObj exam) {

        boolean valid = true;

        final String subtestName = elem.getStringAttr("name");
        final String scoreStr = elem.getStringAttr("score");

        if (subtestName == null) {
            elem.logError("Missing 'name' attribute in <subtest> element.");
            valid = false;
        }

        Double score = null;
        if (scoreStr != null) {
            try {
                score = Double.valueOf(scoreStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'score' attribute in <subtest> element.");
                valid = false;
            }
        }

        if (valid) {
            final ExamSubtest subtest = new ExamSubtest();
            subtest.subtestName = subtestName;

            if (score != null) {
                subtest.score = score;
            }

            for (final IElement child : elem.getElementChildrenAsList()) {
                final String tag = child.getTagName();
                if (child instanceof final EmptyElement childElem) {

                    if ("subtest-problem".equals(tag)) {
                        valid = parseSubtestProblem(childElem, subtest);
                    } else {
                        elem.logError("Unexpected '" + tag + "' element in <subtest> element.");
                        valid = false;
                    }

                    if (!valid) {
                        break;
                    }
                } else {
                    elem.logError("Unexpected non-empty '" + tag + "' element in <subtest> element.");
                    valid = false;
                }
            }

            exam.addSubtest(subtest);
        }

        return valid;
    }

    /**
     * Parses a subtest problem definition from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param elem    the element
     * @param subtest the {@code ExamSubtest} to populate with the parsed data
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseSubtestProblem(final EmptyElement elem, final ExamSubtest subtest) {

        boolean valid = true;

        final String problemIdStr = elem.getStringAttr("problem-id");
        final String weightStr = elem.getStringAttr("weight");

        int problemId = 0;
        if (problemIdStr == null) {
            elem.logError("Missing 'problem-id' attribute in <subtest-problem> element.");
            valid = false;
        } else {
            try {
                problemId = Integer.parseInt(problemIdStr);
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'problem-id' attribute in <subtest-problem> element.");
                valid = false;
            }
        }

        // Get weight name (optional attribute - default is 1.0)
        double weight = 1.0;
        if (weightStr != null) {
            try {
                weight = Double.parseDouble(weightStr);
                if (weight <= 0.0) {
                    elem.logError("Negative 'weight' attribute in <subtest-problem> element.");
                    valid = false;
                }
            } catch (final NumberFormatException ex) {
                elem.logError("Invalid 'weight' attribute in <subtest-problem> element.");
                valid = false;
            }
        }

        if (valid) {
            final ExamSubtestProblem problem = new ExamSubtestProblem();
            problem.problemId = problemId;
            problem.weight = weight;
            subtest.addSubtestProblem(problem);
        }

        return valid;
    }

    /**
     * Parses an exam grading rule from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param elem the element
     * @param exam the {@code Exam} to populate with the parsed data
     * @param mode the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseGradingRule(final NonemptyElement elem, final ExamObj exam, final EParserMode mode) {

        boolean valid = true;

        final String ruleName = elem.getStringAttr("name");
        final String ruleType = elem.getStringAttr("type");
        final String result = elem.getStringAttr("result");

        if (ruleName == null) {
            elem.logError("Missing 'name' attribute in <grading-rule> element.");
            valid = false;
        }

        if (ruleType == null) {
            elem.logError("Missing 'type' attribute in <grading-rule> element.");
            valid = false;
        }

        if (valid) {
            final ExamGradingRule rule = new ExamGradingRule();
            rule.gradingRuleName = ruleName;
            rule.setGradingRuleType(ruleType);

            if ("true".equalsIgnoreCase(result)) {
                rule.result = Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(result)) {
                rule.result = Boolean.FALSE;
            } else {
                rule.result = result;
            }

            for (final IElement child : elem.getElementChildrenAsList()) {
                final String tag = child.getTagName();
                if (child instanceof final NonemptyElement childElem) {

                    if ("pass-if".equals(tag)) {
                        valid = parsePassIfCondition(exam.getEvalContext(), childElem, rule, mode);
                    } else if ("letter-if".equals(tag)) {
                        valid = parseLetterIfCondition(exam.getEvalContext(), childElem, rule, mode);
                    } else {
                        elem.logError("Unexpected '" + tag + "' element in <grading-rule> element.");
                        valid = false;
                    }

                    if (!valid) {
                        break;
                    }
                } else {
                    elem.logError("Unexpected non-empty '" + tag + "' element in <grading-rule> element.");
                    valid = false;
                }
            }

            exam.addGradingRule(rule);
        }

        return valid;
    }

    /**
     * Parses a "pass-if" grading rule condition from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param rule        the {@code ExamGradingRule} to which to add the parsed condition
     * @param mode        the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parsePassIfCondition(final EvalContext evalContext, final NonemptyElement elem,
                                                final ExamGradingRule rule, final EParserMode mode) {

        boolean valid = false;

        if (elem.getNumChildren() == 1) {
            final INode child = elem.getChild(0);
            if (child instanceof final CData cdata) {
                // Parse formula from text
                final Formula form = FormulaFactory.parseFormulaString(evalContext, cdata.content, mode);
                if (form == null) {
                    elem.logError("Invalid formula string in <pass-if> element");
                } else {
                    final ExamGradingCondition condition = new ExamGradingCondition();
                    condition.gradingConditionFormula = form;
                    condition.setGradingConditionType(ExamGradingCondition.PASS_IF);
                    rule.addGradingCondition(condition);
                    valid = true;
                }
            } else if (child instanceof final NonemptyElement nonempty) {
                // Parse formula from XML
                final Formula form = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                if (form == null) {
                    elem.logError("Invalid formula XML in <pass-if> element");
                } else {
                    final ExamGradingCondition condition = new ExamGradingCondition();
                    condition.gradingConditionFormula = form;
                    condition.setGradingConditionType(ExamGradingCondition.PASS_IF);
                    rule.addGradingCondition(condition);
                    valid = true;
                }
            } else {
                elem.logError("<pass-if> element must have <formula> or simple text content");
            }
        } else {
            elem.logError("<pass-if> element must have <formula> or simple text content");
        }

        return valid;
    }

    /**
     * Parses a "letter-if" grading rule condition from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param rule        the {@code ExamGradingRule} to which to add the parsed condition
     * @param mode        the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseLetterIfCondition(final EvalContext evalContext, final NonemptyElement elem,
                                                  final ExamGradingRule rule, final EParserMode mode) {

        boolean valid = false;

        if (elem.getNumChildren() == 1) {
            final String value = elem.getStringAttr("value");

            if (value == null || value.isBlank()) {
                elem.logError("<letter-if> element must have nonempty 'value' attribute");
            } else {
                final INode child = elem.getChild(0);
                if (child instanceof final CData cdata) {
                    // Parse formula from text
                    final Formula form = FormulaFactory.parseFormulaString(evalContext, cdata.content, mode);
                    if (form == null) {
                        elem.logError("Invalid formula string in <letter-if> element");
                    } else {
                        final ExamGradingCondition condition = new ExamGradingCondition();
                        condition.gradingConditionFormula = form;
                        condition.setGradingConditionType(ExamGradingCondition.LETTER_IF);
                        condition.gradingConditionValue = value;
                        rule.addGradingCondition(condition);
                        valid = true;
                    }
                } else if (child instanceof final NonemptyElement nonempty) {
                    // Parse formula from XML
                    final Formula form = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                    if (form == null) {
                        elem.logError("Invalid formula XML in <letter-if> element");
                    } else {
                        final ExamGradingCondition condition = new ExamGradingCondition();
                        condition.gradingConditionFormula = form;
                        condition.setGradingConditionType(ExamGradingCondition.LETTER_IF);
                        condition.gradingConditionValue = value;
                        rule.addGradingCondition(condition);
                        valid = true;
                    }
                } else {
                    elem.logError("<letter-if> element must have <formula> or simple text content");
                }
            }
        } else {
            elem.logError("<letter-if> element must have <formula> or simple text content");
        }

        return valid;
    }

    /**
     * Parses an exam outcome from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param exam        the {@code Exam} to populate with the parsed data
     * @param mode        the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseOutcome(final EvalContext evalContext, final NonemptyElement elem, final ExamObj exam,
                                        final EParserMode mode) {

        boolean valid = true;

        final String conditionStr = elem.getStringAttr("condition");
        final String logDenialStr = elem.getStringAttr("log-denial");

        Formula formula = null;
        if (conditionStr != null) {
            formula = FormulaFactory.parseFormulaString(evalContext, conditionStr, mode);
            if (formula == null) {
                elem.logError("Invalid 'condition' attribute in <outcome> element");
                valid = false;
            }
        }

        boolean logDenaial = true;
        if (logDenialStr != null) {
            if ("false".equalsIgnoreCase(logDenialStr)) {
                logDenaial = false;
            } else if (!"true".equalsIgnoreCase(logDenialStr)) {
                elem.logError("Invalid 'log-denial' attribute in <outcome> element - assuming 'true'.");
            }
        }

        if (valid) {
            final ExamOutcome outcome = new ExamOutcome();
            outcome.logDenial = logDenaial;
            outcome.condition = formula;

            for (final IElement child : elem.getElementChildrenAsList()) {
                final String tag = child.getTagName();

                if (child instanceof final EmptyElement childElem) {
                    if ("indicate-placement".equals(tag)) {
                        valid = parseOutcomeIndicatePlacement(childElem, outcome);
                    } else if ("indicate-credit".equals(tag)) {
                        valid = parseOutcomeIndicateCredit(childElem, outcome);
                    } else if ("indicate-licensed".equals(tag)) {
                        valid = parseOutcomeIndicateLicensed(childElem, outcome);
                    } else {
                        elem.logError("Unexpected empty '" + tag + "' element in <outcome> element.");
                        valid = false;
                    }

                    if (!valid) {
                        break;
                    }

                } else if (child instanceof final NonemptyElement childElem) {

                    if ("condition".equals(tag)) {
                        valid = parseOutcomeCondition(evalContext, childElem, outcome, mode);
                    } else if ("prereq".equals(tag)) {
                        valid = parseOutcomePrereq(evalContext, childElem, outcome, mode);
                    } else if ("valid-if".equals(tag)) {
                        valid = parseOutcomeValidIf(evalContext, childElem, outcome, mode);
                    } else {
                        elem.logError("Unexpected nonempty '" + tag + "' element in <outcome> element.");
                        valid = false;
                    }

                    if (!valid) {
                        break;
                    }
                }
            }

            if (outcome.condition == null) {
                valid = false;
                elem.logError("<outcome> element must have 'condition' attribute or a <condition> child.");
            }

            if (valid) {
                exam.addExamOutcome(outcome);
            }
        }

        return valid;
    }

    /**
     * Parses an 'indicate-placement' outcome action from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param elem    the element
     * @param outcome the {@code ExamOutcome} to which to add the action
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseOutcomeIndicatePlacement(final EmptyElement elem, final ExamOutcome outcome) {

        boolean valid = true;

        final String course = elem.getStringAttr("course");

        if (course == null) {
            elem.logError("Missing 'course' attribute in <indicate-placement> element.");
            valid = false;
        } else {
            final ExamOutcomeAction action = new ExamOutcomeAction();
            action.type = ExamOutcomeAction.INDICATE_PLACEMENT;
            action.course = course;
            outcome.addAction(action);
        }

        return valid;
    }

    /**
     * Parses an 'indicate-credit' outcome action from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param elem    the element
     * @param outcome the {@code ExamOutcome} to which to add the action
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseOutcomeIndicateCredit(final EmptyElement elem, final ExamOutcome outcome) {

        boolean valid = true;

        final String course = elem.getStringAttr("course");

        if (course == null) {
            elem.logError("Missing 'course' attribute in <indicate-credit> element.");
            valid = false;
        } else {
            final ExamOutcomeAction action = new ExamOutcomeAction();
            action.type = ExamOutcomeAction.INDICATE_CREDIT;
            action.course = course;
            outcome.addAction(action);
        }

        return valid;
    }

    /**
     * Parses an 'indicate-licensed' outcome action from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param elem    the element
     * @param outcome the {@code ExamOutcome} to which to add the action
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseOutcomeIndicateLicensed(final IElement elem, final ExamOutcome outcome) {

        final String course = elem.getStringAttr("course");
        final ExamOutcomeAction action = new ExamOutcomeAction();
        action.type = ExamOutcomeAction.INDICATE_LICENSED;
        action.course = course;
        outcome.addAction(action);

        return true;
    }

    /**
     * Parses a grading rule condition from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param outcome     the {@code outcome} to populate with the parsed data
     * @param mode        the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseOutcomeCondition(final EvalContext evalContext, final NonemptyElement elem,
                                                 final ExamOutcome outcome, final EParserMode mode) {

        boolean valid = false;

        if (elem.getNumChildren() == 1) {
            final INode child = elem.getChild(0);
            if (child instanceof final CData cdata) {
                // Parse formula from text
                final Formula form = FormulaFactory.parseFormulaString(evalContext, cdata.content, mode);
                if (form == null) {
                    elem.logError("Invalid formula string in <condition> element");
                } else {
                    outcome.condition = form;
                    valid = true;
                }
            } else if (child instanceof final NonemptyElement nonempty) {
                // Parse formula from XML
                final Formula form = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                if (form == null) {
                    elem.logError("Invalid formula XML in <condition> element");
                } else {
                    outcome.condition = form;
                    valid = true;
                }
            } else {
                elem.logError("<condition> element must have <formula> or simple text content");
            }
        } else {
            elem.logError("<condition> element must have <formula> or simple text content");
        }

        return valid;
    }

    /**
     * Parses a grading rule condition from a source XML element. Any errors encountered are logged in the
     * {@code NonemptyElement} object.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param outcome     the {@code outcome} to populate with the parsed data
     * @param mode        the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseOutcomePrereq(final EvalContext evalContext, final NonemptyElement elem,
                                              final ExamOutcome outcome, final EParserMode mode) {

        boolean valid = false;

        if (elem.getNumChildren() == 1) {
            final INode child = elem.getChild(0);
            if (child instanceof final CData cdata) {
                // Parse formula from text
                final Formula form = FormulaFactory.parseFormulaString(evalContext, cdata.content, mode);
                if (form == null) {
                    elem.logError("Invalid formula string in <prereq> element");
                } else {
                    final ExamOutcomePrereq prereq = new ExamOutcomePrereq();
                    prereq.prerequisiteFormula = form;
                    outcome.addPrereq(prereq);
                    valid = true;
                }
            } else if (child instanceof final NonemptyElement nonempty) {
                // Parse formula from XML
                final Formula form = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                if (form == null) {
                    elem.logError("Invalid formula XML in <prereq> element");
                } else {
                    final ExamOutcomePrereq prereq = new ExamOutcomePrereq();
                    prereq.prerequisiteFormula = form;
                    outcome.addPrereq(prereq);
                    valid = true;
                }
            } else {
                elem.logError("<prereq> element must have <formula> or simple text content");
            }
        } else {
            elem.logError("<prereq> element must have <formula> or simple text content");
        }

        return valid;
    }

    /**
     * Parses a validation to be applied before an outcome can be awarded from a source XML element. Any errors
     * encountered are logged in the {@code NonemptyElement} object.
     *
     * @param evalContext the evaluation context
     * @param elem        the element
     * @param outcome     the {@code ExamOutcome} to populate with the parsed data
     * @param mode        the parser mode
     * @return {@code true} if successful; {@code false} on any error
     */
    private static boolean parseOutcomeValidIf(final EvalContext evalContext, final NonemptyElement elem,
                                               final ExamOutcome outcome, final EParserMode mode) {

        boolean valid = false;

        final String how = elem.getStringAttr("how");

        if (how == null) {
            elem.logError("Missing 'how' attribute in <valid-if> element.");
        } else if (elem.getNumChildren() != 1) {
            elem.logError("<valid-if> element must have <formula> or simple text content");
        } else {
            final INode child = elem.getChild(0);
            if (child instanceof final CData cdata) {
                // Parse formula from text
                final Formula form = FormulaFactory.parseFormulaString(evalContext, cdata.content, mode);
                if (form == null) {
                    elem.logError("Invalid formula string in <valid-if> element");
                } else {
                    final ExamOutcomeValidation validation = new ExamOutcomeValidation();
                    validation.validationFormula = form;
                    validation.howValidated = how;
                    outcome.addValidation(validation);
                    valid = true;
                }
            } else if (child instanceof final NonemptyElement nonempty) {
                // Parse formula from XML
                final Formula form = XmlFormulaFactory.extractFormula(evalContext, nonempty, mode);
                if (form == null) {
                    elem.logError("Invalid formula XML in <valid-if> element");
                } else {
                    final ExamOutcomeValidation validation = new ExamOutcomeValidation();
                    validation.validationFormula = form;
                    validation.howValidated = how;
                    outcome.addValidation(validation);
                    valid = true;
                }
            } else {
                elem.logError("<valid-if> element must have <formula> or simple text content");
            }
        }

        return valid;
    }
}
