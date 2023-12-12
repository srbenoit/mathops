package dev.mathops.session.txn.messages;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.SurveyStatusHandler;

/**
 * A request for the list of survey questions associated with a version of an exam, and the student's current set of
 * answers.
 */
public final class SurveyStatusRequest extends AbstractRequestBase {

    /** The student ID. */
    public final String studentId;

    /** The exam version. */
    public final String version;

    /**
     * Constructs a new {@code SurveyStatusRequest}.
     *
     * @param theStudentId the student ID
     * @param theVersion   the exam version
     */
    public SurveyStatusRequest(final String theStudentId, final String theVersion) {

        super();

        this.studentId = theStudentId;
        this.version = theVersion;
    }

    /**
     * Constructs a new {@code SurveyStatusRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    SurveyStatusRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.machineId = extractField(message, "machine-id");
        this.studentId = extractField(message, "student-id");
        this.version = extractField(message, "version");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "survey-status-request";
    }

    /**
     * Generate the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<survey-status-request>");

        if (this.studentId != null) {
            builder.addln(" <student-id>", this.studentId, "</student-id>");
        }

        if (this.version != null) {
            builder.addln(" <version>", this.version, "</version>");
        }

        printMachineId(builder);
        builder.addln("</survey-status-request>");

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

        return new SurveyStatusHandler(dbProfile);
    }
}
