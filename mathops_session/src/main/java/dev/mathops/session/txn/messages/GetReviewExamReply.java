package dev.mathops.session.txn.messages;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.XmlContent;

/**
 * A network message that provides the client a new realized review exam.
 */
public final class GetReviewExamReply extends GetExamReply {

    /** The mastery score for the exam based on course-unit-section. */
    public Integer masteryScore;

    /**
     * Constructs a new {@code GetReviewExamReply}.
     */
    public GetReviewExamReply() {

        super();
    }

    /**
     * Constructs a new {@code GetReviewExamReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    public GetReviewExamReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, "get-review-exam-reply");

        this.error = extractField(message, "error");
        this.studentId = extractField(message, "student");

        String sub = extractField(message, "holds");

        if (sub != null) {
            this.holds = extractFieldList(sub, "hold");
        }

        sub = extractField(message, "mastery-score");
        if (sub != null && !sub.isEmpty()) {
            try {
                this.masteryScore = Integer.valueOf(sub);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException(Res.get(Res.BAD_MASTERY));
            }
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
            final int numSect = this.presentedExam.getNumSections();

            // Use the problem factory to extract selected problems.
            String selProb = extractField(message, "selected-problems");

            if (selProb != null) {
                int pos = selProb.indexOf("</problem");

                for (int i = 0; i < numSect; i++) {
                    final ExamSection sect = this.presentedExam.getSection(i);
                    final int numProb = sect.getNumProblems();

                    for (int j = 0; j < numProb; ++j) {

                        if (pos == -1) {
                            throw new IllegalArgumentException(Res.get(Res.NOT_ALL_SEL_IN_XML));
                        }

                        final ExamProblem prob = sect.getProblem(j);

                        // Carve off one problem's XML
                        final int tagEnd = selProb.indexOf('>', pos + 9);
                        sub = selProb.substring(0, tagEnd + 1);
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
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<get-review-exam-reply>");

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

        if (this.masteryScore != null) {
            builder.addln(" <mastery-score>", this.masteryScore,
                    "</mastery-score>");
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

                        if (selected == null) {
                            Log.warning(Res.fmt(Res.SELECTED_WAS_NULL, Integer.toString(j),
                                    Integer.toString(i)));
                        } else {
                            selected.appendXml(builder, 2);
                        }
                    }
                }
            }

            builder.addln(" </selected-problems>");
        }

        printError(builder);
        builder.addln("</get-review-exam-reply>");

        return builder.toString();
    }
}
