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
 * A network message that provides a copy of a previously taken exam.
 */
public final class GetPastExamReply extends AbstractReplyBase {

    /** The loaded exam. */
    public ExamObj exam;

    /**
     * Constructs a new {@code GetPastExamReply}.
     */
    public GetPastExamReply() {

        super();
    }

    /**
     * Constructs a new {@code GetExamReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    GetPastExamReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");

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
            this.exam = ExamFactory.load(examXml, EParserMode.ALLOW_DEPRECATED);
        }

        if (this.exam != null) {
            final int numSect = this.exam.getNumSections();

            // Use the problem factory to extract selected problems.
            String selProb = extractField(message, "selected-problems");

            if (selProb != null) {
                int pos = selProb.indexOf("</problem");

                for (int i = 0; i < numSect; i++) {
                    final ExamSection sect = this.exam.getSection(i);
                    final int numProb = sect.getNumProblems();

                    for (int j = 0; j < numProb; j++) {

                        if (pos == -1) {
                            throw new IllegalArgumentException(Res.fmt(Res.MISSING_PROBLEM,
                                    this.exam.examVersion, Integer.toString(j), Integer.toString(i)));
                        }

                        final ExamProblem prob = sect.getProblem(j);

                        // Carve off one problem's XML
                        final int tagEnd = selProb.indexOf('>', pos + 9);
                        final String sub = selProb.substring(0, tagEnd + 1);
                        selProb = selProb.substring(tagEnd + 1);


                        try {
                            final XmlContent content = new XmlContent(sub, false, false);
                            final AbstractProblemTemplate selected =
                                    ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                            prob.setSelectedProblem(selected);
                        } catch (final ParsingException ex) {
                            Log.warning(ex);
                            throw new IllegalArgumentException(Res.get(Res.CANT_PARSE_PROBLEM));
                        }

                        pos = selProb.indexOf("</problem");
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

        return "get-past-exam-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<get-past-exam-reply>");

        if (this.exam != null) {

            // Embed the exam XML in the reply.
            this.exam.appendXml(builder, 1);

            builder.addln(" <selected-problems>");

            // Now embed each of the selected problems' XML in the reply.
            final int numSect = this.exam.getNumSections();

            for (int i = 0; i < numSect; ++i) {
                final ExamSection sect = this.exam.getSection(i);
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
        builder.addln("</get-past-exam-reply>");

        return builder.toString();
    }
}
