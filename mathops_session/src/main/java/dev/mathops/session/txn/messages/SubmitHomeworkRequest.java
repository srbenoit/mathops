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
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.SubmitHomeworkHandler;

/**
 * A network message to submit a completed homework assignment.
 */
public final class SubmitHomeworkRequest extends AbstractRequestBase {

    /** The homework being submitted. */
    public ExamObj homework;

    /** An array of answers. */
    public Object[][] answers;

    /** The score on the assignment. */
    public int score;

    /** The student ID submitting the homework. */
    public final String studentId;

    /**
     * Constructs a new {@code SubmitHomeworkRequest}.
     *
     * @param theHomework  the homework being submitted
     * @param theAnswers   the list of answers as they currently exist
     * @param theScore     the score on the assignment
     * @param theStudentId the student ID submitting the homework
     */
    public SubmitHomeworkRequest(final ExamObj theHomework, final Object[][] theAnswers,
                                 final int theScore, final String theStudentId) {

        super();

        this.homework = theHomework;
        this.answers = theAnswers;
        this.score = theScore;
        this.studentId = theStudentId;
    }

    /**
     * Constructs a new {@code SubmitHomeworkRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    SubmitHomeworkRequest(final char[] xml) throws IllegalArgumentException {

        super();

        int count = 0;

        final String message = extractMessage(xml, xmlTag());

        this.machineId = extractField(message, "machine-id");
        this.studentId = extractField(message, "student");

        // Use the exam factory to extract the homework.
        String sub = extractField(message, "homework");
        this.homework = ExamFactory.load(sub, EParserMode.ALLOW_DEPRECATED);

        // Use the problem factory to extract selected problems.
        String submsg = extractField(message, "selected-problems");

        if (submsg != null) {
            int pos = submsg.indexOf("</problem>");
            final int numSect = this.homework.getNumSections();

            for (int i = 0; i < numSect; i++) {
                final ExamSection sect = this.homework.getSection(i);
                final int numProb = sect.getNumProblems();

                for (int j = 0; j < numProb; j++) {

                    if (pos == -1) {
                        throw new IllegalArgumentException(
                                Res.fmt(Res.NOT_ALL_SEL_HW_INCLUDED, this.homework.ref));
                    }

                    final ExamProblem prob = sect.getProblem(j);

                    // Carve off one problem's XML
                    sub = submsg.substring(0, pos + 10);
                    submsg = submsg.substring(pos + 10);

                    try {
                        final XmlContent content = new XmlContent(sub, false, false);
                        final AbstractProblemTemplate selected =
                                ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                        prob.setSelectedProblem(selected);
                    } catch (final ParsingException ex) {
                        Log.warning(ex);
                        throw new IllegalArgumentException(
                                Res.fmt(Res.CANT_PARSE_SEL_HW, this.homework.ref));
                    }

                    pos = submsg.indexOf("</problem>");
                }
            }
        } else {
            throw new IllegalArgumentException(Res.fmt(Res.NO_SEL_HW_LIST, this.homework.ref));
        }

        String value = extractField(message, "num-answers");

        if (value != null) {
            try {
                count = Long.valueOf(value).intValue();
            } catch (final NumberFormatException e) { /* Empty */
            }
        }

        if (count != 0) {
            this.answers = new Object[count + 1][];
            this.answers[0] = new Object[4];
            this.answers[0][0] = null;
            this.answers[0][1] = null;
            this.answers[0][2] = null;

            sub = extractField(message, "answers");

            if (sub != null) {
                final String[] list = extractFieldList(sub, "answer");
                if (list != null) {
                    if (count > list.length) {
                        count = list.length;
                    }

                    for (int i = 0; i < count; i++) {
                        if (!list[i].isEmpty()) {
                            this.answers[i + 1] = extractValueList(list[i]);
                        }
                    }
                }
            }
        }

        value = extractField(message, "score");

        if (value != null) {
            try {
                this.score = Long.valueOf(value).intValue();
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException(Res.fmt(Res.BAD_SUBMIT_HW_SCORE, value));
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
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "submit-homework-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<submit-homework-request>");

        if (this.studentId != null) {
            builder.addln(" <student>", this.studentId, "</student>");
        }

        if (this.homework != null) {
            builder.addln(" <homework>", this.homework.toXmlString(2), "</homework>");

            builder.addln(" <selected-problems>");

            // Now embed each of the selected problems' XML in the reply.
            final int numSect = this.homework.getNumSections();

            for (int i = 0; i < numSect; ++i) {
                final ExamSection sect = this.homework.getSection(i);

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

        if (this.answers != null) {
            final int numAns = this.answers.length;

            if (numAns > 0 && this.answers[0].length == 4) {

                if (this.answers[0][0] != null) {
                    builder.addln(" <cur-section>", this.answers[0][0], "</cur-section>");
                }

                if (this.answers[0][1] != null) {
                    builder.addln(" <cur-problem>", this.answers[0][1], "</cur-problem>");
                }

                if (this.answers[0][2] != null) {
                    builder.addln(" <presented>", this.answers[0][2], "</presented>");
                }
            }

            if (numAns > 1) {
                builder.addln(" <num-answers>", Integer.toString(numAns - 1), "</num-answers>");

                builder.addln(" <answers>");

                // Include one answer line per problem, even if unanswered
                for (int i = 1; i < numAns; ++i) {
                    builder.add("  <answer>");

                    if (this.answers[i] != null) {

                        final int innerLen = this.answers[i].length;
                        for (int j = 0; j < innerLen; ++j) {

                            switch (this.answers[i][j]) {
                                case null -> {
                                    continue;
                                }
                                case final Long l -> builder.addln("<long>", this.answers[i][j], "</long>");
                                case final Double v -> builder.addln("<double>", this.answers[i][j], "</double>");
                                case final String s -> builder.addln("<string>", this.answers[i][j], "</string>");
                                default -> Log.warning(Res.fmt(Res.BAD_ANSWER_OBJ,
                                        this.answers[i][j].getClass().getName()));
                            }

                        }
                    }

                    builder.addln("</answer>");
                }

                builder.addln(" </answers>");
            }
        }

        builder.addln(" <score>", Integer.toString(this.score),
                "</score>");

        printMachineId(builder);
        builder.addln("</submit-homework-request>");

        return builder.toString();
    }

    /**
     * Generates a handler that can process this message.
     *
     * @param dbProfile the database profile in which the handler will operate
     * @return a handler that can process the message
     */
    @Override
    public AbstractHandlerBase createHandler(final DbProfile dbProfile) {

        return new SubmitHomeworkHandler(dbProfile);
    }
}
