package dev.mathops.session.txn.messages;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.GetHomeworkHandler;

/**
 * A network message to request a particular homework assignment.
 */
public final class GetHomeworkRequest extends AbstractRequestBase {

    /** The ID of the student requesting the homework. */
    public final String studentId;

    /** The version number of the homework to retrieve. */
    public final String homeworkVersion;

    /** True if homework is a practice problem set. */
    public final boolean isPractice;

    /**
     * Constructs a new {@code GetHomeworkRequest}.
     *
     * @param theStudentId       the ID of the student requesting the homework
     * @param theHomeworkVersion the version number of the homework assignment requested
     * @param theIsPractice      true if homework is a practice problem set
     */
    public GetHomeworkRequest(final String theStudentId, final String theHomeworkVersion,
                              final boolean theIsPractice) {

        super();

        this.studentId = theStudentId;
        this.homeworkVersion = theHomeworkVersion;
        this.isPractice = theIsPractice;
    }

    /**
     * Constructs a new {@code GetHomeworkRequest}, initializing with data from an XML stream.
     *
     * @param xml The XML stream from which to initialize data.
     * @throws IllegalArgumentException If the XML stream is not valid.
     */
    GetHomeworkRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.machineId = extractField(message, "machine-id");

        this.studentId = extractField(message, "student-id");
        this.homeworkVersion = extractField(message, "homework");
        this.isPractice = "true".equalsIgnoreCase(
                extractField(message, "is-practice"));
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "get-homework-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<get-homework-request>");

        if (this.studentId != null) {
            builder.addln(" <student-id>", this.studentId, "</student-id>");
        }

        if (this.homeworkVersion != null) {
            builder.addln(" <homework>", this.homeworkVersion,
                    "</homework>");
        }

        if (this.isPractice) {
            builder.addln(" <is-practice>true</is-practice>");
        }

        printMachineId(builder);
        builder.addln("</get-homework-request>");

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

        return new GetHomeworkHandler(dbProfile);
    }
}
