package dev.mathops.session.txn.messages;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawStsurveyqa;
import dev.mathops.db.old.rawrecord.RawSurveyqa;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A reply to a survey status request.
 */
public class SurveyStatusReply extends AbstractReplyBase {

    /** The list of questions associated with an exam. */
    public List<RawSurveyqa> questions;

    /** The list of answers the student has supplied to date. */
    public List<RawStsurveyqa> answers;

    /**
     * Constructs a new {@code SurveyStatusReply}.
     */
    public SurveyStatusReply() {

        super();
    }

    /**
     * Constructs a new {@code SurveyStatusReply}, initializing with data from an XML stream.
     *
     * @param xml The XML stream from which to initialize data
     * @throws IllegalArgumentException If the XML stream is not valid
     */
    SurveyStatusReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");

        String sub = extractField(message, "questions");
        if (sub != null) {
            final String[] fields = extractFieldList(sub, "question");

            if (fields != null) {
                final TermKey term = new TermKey(ETermName.FALL, 2000);
                this.questions = new ArrayList<>(fields.length);

                for (final String field : fields) {
                    Integer questionNumber = null;
                    String value = extractField(field, "number");
                    if (value != null) {
                        try {
                            questionNumber = Integer.valueOf(value);
                        } catch (final NumberFormatException e) {
                            throw new IllegalArgumentException(e.getMessage());
                        }
                    }

                    final String descr = extractField(field, "description");
                    final String type = extractField(field, "type");

                    value = extractField(field, "must-answer");

                    // NOTE: Term and profile ID are not used, so we pick arbitrary term
                    final RawSurveyqa question =
                            new RawSurveyqa(term, CoreConstants.EMPTY, questionNumber, descr, type, null, null, null,
                                    value, null);

                    this.questions.add(question);
                }
            }
        }

        sub = extractField(message, "answers");

        if (sub != null) {
            final String[] fields = extractFieldList(sub, "answer");

            if (fields != null) {
                this.answers = new ArrayList<>(fields.length);

                for (final String field : fields) {
                    final String value = extractField(field, "number");

                    int q = 0;
                    if (value != null) {
                        try {
                            q = Integer.parseInt(value);
                        } catch (final NumberFormatException e) {
                            throw new IllegalArgumentException(e.getMessage());
                        }
                    }

                    final LocalDateTime now = LocalDateTime.now();

                    final RawStsurveyqa answer =
                            new RawStsurveyqa("888888888", "x", now.toLocalDate(), Integer.valueOf(q),
                                    extractField(field, "value"), Integer.valueOf(TemporalUtils.minuteOfDay(now)));

                    this.answers.add(answer);
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

        return "survey-status-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<survey-status-reply>");

        if (this.questions != null) {
            builder.addln(" <questions>");

            for (final RawSurveyqa question : this.questions) {

                builder.addln("  <question>");

                if (question.surveyNbr != null) {
                    builder.addln("   <number>", question.surveyNbr,
                            "</number>");
                }

                if (question.questionDesc != null) {
                    builder.addln("   <description>", question.questionDesc,
                            "</description>");
                }

                if (question.typeQuestion != null) {
                    builder.addln("   <type>", question.typeQuestion,
                            "</type>");
                }

                if (question.mustAnswer != null) {
                    builder.addln("   <must-answer>", question.mustAnswer,
                            "</must-answer>");
                }

                builder.addln("  </question>");
            }

            builder.addln(" </questions>");
        }

        if (this.answers != null) {
            builder.addln(" <answers>");

            for (final RawStsurveyqa answer : this.answers) {
                builder.addln("  <answer>");

                if (answer != null) {
                    if (answer.surveyNbr != null) {
                        builder.addln("   <number>", answer.surveyNbr,
                                "</number>");
                    }
                    if (answer.stuAnswer != null) {
                        builder.addln("   <value>", answer.stuAnswer,
                                "</value>");
                    }
                }

                builder.addln("  </answer>");
            }

            builder.addln(" </answers>");
        }

        printError(builder);
        builder.addln("</survey-status-reply>");

        return builder.toString();
    }
}
