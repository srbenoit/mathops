package dev.mathops.session.txn.messages;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.XmlContent;

/**
 * A network message that provides the client a new realized exam.
 */
public class GetExamReply extends AbstractReplyBase {

    /** Result code indicating exam was started successfully. */
    public static final int SUCCESS = 0;

    /** Result code indicating exam template could not be loaded. */
    public static final int CANNOT_LOAD_EXAM_TEMPLATE = 1;

    /** Result code indicating exam could not be realized from template. */
    public static final int CANNOT_REALIZE_EXAM = 2;

    /** Result code indicating presented exam could not be created. */
    public static final int CANNOT_PRESENT_EXAM = 3;

    /** The result of attempting to create the realized exam. */
    public int status;

    /** The generated realized exam, or null on any failure to generate. */
    public ExamObj presentedExam;

    /** A list of holds currently associated with the student account. */
    public String[] holds;

    /** The student ID taking the exam. */
    public String studentId;

    /**
     * Constructs a new {@code GetExamReply}.
     */
    public GetExamReply() {

        super();
    }

    /**
     * Constructs a new {@code GetExamReply}, initializing with data from an XML stream.
     *
     * @param xml The XML stream from which to initialize data.
     * @throws IllegalArgumentException If the XML stream is not valid.
     */
    public GetExamReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");
        this.studentId = extractField(message, "student");

        String sub = extractField(message, "holds");

        if (sub != null) {
            this.holds = extractFieldList(sub, "hold");
        }

        if (this.error == null) {
            final int start = message.indexOf("<exam ");
            if (start == -1) {
                throw new IllegalArgumentException("GetReviewExamReply did not contain exam");
            }

            final int end = message.indexOf("</exam>", start + 6);
            if (end == -1) {
                throw new IllegalArgumentException(
                        "GetReviewExamReply: can't find end tag of <exam>");
            }

            // Use the exam factory to extract the exam.
            final String examXml = message.substring(start, end + 7);
            this.presentedExam = ExamFactory.load(examXml, EParserMode.ALLOW_DEPRECATED);
        }

        if (this.presentedExam != null) {
            Log.info("GetExamReply contained an exam");

            // Use the problem factory to extract selected problems.
            String selProb = extractField(message, "selected-problems");

            if (selProb != null) {
                int pos = selProb.indexOf("</problem>");
                final int numSect = this.presentedExam.getNumSections();

                for (int i = 0; i < numSect; i++) {
                    final ExamSection sect = this.presentedExam.getSection(i);

                    final int numProb = sect.getNumProblems();

                    for (int j = 0; j < numProb; j++) {

                        if (pos == -1) {
                            throw new IllegalArgumentException(
                                    Res.fmt(Res.MISSING_PROBLEM, this.presentedExam.examVersion,
                                            Integer.toString(j), Integer.toString(i)));
                        }

                        final ExamProblem prob = sect.getProblem(j);

                        // Carve off one problem's XML
                        sub = selProb.substring(0, pos + 10);
                        selProb = selProb.substring(pos + 10);

                        try {
                            final XmlContent content = new XmlContent(sub, false, false);
                            final AbstractProblemTemplate selected =
                                    ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                            Log.info("Problem " + prob.problemName + ", found selected ", selected.ref);
                            prob.setSelectedProblem(selected);
                        } catch (final ParsingException ex) {
                            Log.warning(ex);
                            throw new IllegalArgumentException(Res.get(Res.CANT_PARSE_PROBLEM));
                        }

                        pos = selProb.indexOf("</problem>");
                    }
                }
            } else {
                throw new IllegalArgumentException(Res.get(Res.NO_SELECTED_LIST));
            }
        }
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "get-exam-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<get-exam-reply>");

        if (this.studentId != null) {
            builder.addln(" <student>", this.studentId, "</student>");
        }

        if (this.holds != null) {
            builder.addln(" <holds>");

            for (final String hold : this.holds) {
                builder.addln("   <hold>", hold, "</hold>");
            }

            builder.addln(" </holds>");
        }

        if (this.presentedExam != null) {

            // Embed the exam XML in the reply.
            this.presentedExam.appendXml(builder, 1);

            builder.addln(" <selected-problems>");

            // Now embed each of the selected problems' XML in the reply.
            final int numSect = this.presentedExam.getNumSections();

            for (int i = 0; i < numSect; ++i) {

                final ExamSection sect = this.presentedExam.getSection(i);
                final int numProb = sect.getNumProblems();

                for (int j = 0; j < numProb; ++j) {
                    final ExamProblem prob = sect.getPresentedProblem(j);

                    if (prob != null) {
                        final AbstractProblemTemplate selected = prob.getSelectedProblem();

                        if (selected != null) {
                            selected.appendXml(builder, 2);
                        }
                    }
                }
            }

            builder.addln(" </selected-problems>");
        }

        printError(builder);
        builder.addln("</get-exam-reply>");

        return builder.toString();
    }
}
