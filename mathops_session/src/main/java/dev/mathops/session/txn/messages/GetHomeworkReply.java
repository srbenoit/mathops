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
 * A network message that provides the client a homework assignment.
 */
public final class GetHomeworkReply extends AbstractReplyBase {

    /** The unrealized homework assignment. */
    public ExamObj homework;

    /** A list of holds currently associated with the student account. */
    public String[] holds;

    /** The minimum score to move on. */
    public Integer minMoveOn;

    /** The minimum mastery score. */
    public Integer minMastery;

    /** The student ID taking the exam. */
    public String studentId;

    /**
     * Constructs a new {@code GetHomeworkReply}.
     */
    public GetHomeworkReply() {

        super();
    }

    /**
     * Constructs a new {@code GetHomeworkReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    GetHomeworkReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");
        this.studentId = extractField(message, "student");

        String sub = extractField(message, "holds");

        if (sub != null) {
            this.holds = extractFieldList(sub, "hold");
        }

        sub = extractField(message, "min-moveon");

        if (sub != null) {
            try {
                this.minMoveOn = Integer.valueOf(sub);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException(Res.get(Res.BAD_MIN_MOVEON));
            }
        }

        sub = extractField(message, "min-mastery");

        if (sub != null) {
            try {
                this.minMastery = Integer.valueOf(sub);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException(Res.get(Res.BAD_MIN_MASTERY));
            }
        }

        if (String.valueOf(xml).contains("<exam") && this.error == null) {
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
            this.homework = ExamFactory.load(examXml, EParserMode.ALLOW_DEPRECATED);
        }

        if (this.homework != null) {

            // Use the problem factory to extract selected problems.
            String probs = extractField(message, "problems");

            if (probs != null) {
                int pos = probs.indexOf("</problem>");

                while (pos != -1) {

                    // Carve off one problem's XML
                    sub = probs.substring(0, pos + 10);
                    probs = probs.substring(pos + 10);

                    try {
                        final XmlContent content = new XmlContent(sub, false, false);
                        final AbstractProblemTemplate current =
                                ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                        addProblemToHomework(current);
                    } catch (final ParsingException ex) {
                        Log.warning(ex);
                        throw new IllegalArgumentException(Res.get(Res.CANT_PARSE_PROBLEM));
                    }

                    pos = probs.indexOf("</problem>");
                }
            } else {
                throw new IllegalArgumentException(Res.get(Res.NO_SELECTED_HW_LIST));
            }
        }
    }

    /**
     * Method to tell the message to free any resources allocated to it. The message will assume no other methods will
     * be called after this one.
     */
    @Override
    public void die() {

        if (this.homework != null) {
            this.homework = null;
        }

        super.die();
    }

    /**
     * Given a problem, locates any {@code ExamProblems} that the problem belongs to and store the problem in the
     * {@code ExamProblems}.
     *
     * @param current the problem to add
     */
    private void addProblemToHomework(final AbstractProblemTemplate current) {

        if (current.id == null) {
            return;
        }

        final String ref = current.id;
        final int numSect = this.homework.getNumSections();

        for (int i = 0; i < numSect; i++) {
            final ExamSection sect = this.homework.getSection(i);
            final int numProb = sect.getNumProblems();

            for (int j = 0; j < numProb; j++) {
                final ExamProblem prob = sect.getProblem(j);

                final int count = prob.getNumProblems();

                for (int k = 0; k < count; k++) {
                    final AbstractProblemTemplate exist = prob.getProblem(k);

                    if (exist != null && ref.equals(exist.id)) {
                        prob.setProblem(k, current);
                    }
                }
            }
        }
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "get-homework-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<get-homework-reply>");

        if (this.holds != null) {
            builder.addln(" <holds>");

            for (final String hold : this.holds) {
                builder.addln("   <hold>", hold, "</hold>");
            }

            builder.addln(" </holds>");
        }

        if (this.minMoveOn != null) {
            builder.add(" <min-moveon>", this.minMoveOn, "</min-moveon>");
        }

        if (this.minMastery != null) {
            builder.add(" <min-mastery>", this.minMastery,
                    "</min-mastery>");
        }

        if (this.studentId != null) {
            builder.add(" <student>", this.studentId, "</student>");
        }

        if (this.homework != null) {

            // Embed the assignment XML in the reply.
            this.homework.appendXml(builder, 1);

            // Now embed each of the assignment's problems XML in the reply.
            builder.addln(" <problems>");

            final int numSect = this.homework.getNumSections();

            for (int i = 0; i < numSect; ++i) {
                final ExamSection sect = this.homework.getSection(i);
                final int numProb = sect.getNumProblems();

                for (int j = 0; j < numProb; ++j) {
                    final ExamProblem prob = sect.getProblem(j);

                    if (prob != null) {
                        final int numRefs = prob.getNumProblems();

                        for (int k = 0; k < numRefs; ++k) {
                            final AbstractProblemTemplate current = prob.getProblem(k);

                            if (current != null) {
                                current.appendXml(builder, 2);
                            }
                        }
                    }
                }
            }

            builder.addln(" </problems>");
        }

        printError(builder);
        builder.addln("</get-homework-reply>");

        return builder.toString();
    }
}
