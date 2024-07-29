package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.SurveySubmitHandler;

/**
 * A submission of answers to survey questions.
 */
public final class SurveySubmitRequest extends AbstractRequestBase {

    /** The student ID. */
    public final String studentId;

    /** The exam version. */
    public final String version;

    /** The list of answers the student is submitting. */
    public final String[] answers;

    /**
     * Constructs a new {@code SurveySubmitRequest}.
     *
     * @param theStudentId the student ID
     * @param theVersion   the exam version
     * @param theAnswers   the list of student answers
     */
    public SurveySubmitRequest(final String theStudentId, final String theVersion, final String[] theAnswers) {

        super();

        this.studentId = theStudentId;
        this.version = theVersion;
        this.answers = theAnswers;
    }

    /**
     * Constructs a new {@code SurveySubmitRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    SurveySubmitRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String tag = xmlTag();
        final String message = extractMessage(xml, tag);

        this.machineId = extractField(message, "machine-id");
        this.studentId = extractField(message, "student-id");
        this.version = extractField(message, "version");

        final String sub = extractMessage(message.toCharArray(), "answers");
        this.answers = extractFieldList(sub, "answer");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "survey-submit-request";
    }

    /**
     * Generate the XML representation of the message.
     *
     * @return The XML representation.
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<survey-submit-request>");

        if (this.studentId != null) {
            builder.addln(" <student-id>", this.studentId, "</student-id>");
        }

        if (this.version != null) {
            builder.addln(" <version>", this.version, "</version>");
        }

        if (this.answers != null) {
            builder.addln(" <answers>");

            for (final String answer : this.answers) {
                builder.add("  <answer>");
                if (answer != null) {
                    builder.add(answer);
                }
                builder.addln("</answer>");
            }

            builder.addln(" </answers>");
        }

        printMachineId(builder);
        builder.addln("</survey-submit-request>");

        return builder.toString();
    }

    /**
     * Generates a handler that can process this message.
     *
     * @return a handler that can process the message
     */
    @Override
    public AbstractHandlerBase createHandler() {

        return new SurveySubmitHandler();
    }
}
