package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.GetReviewExamHandler;

/**
 * A network message to request that a new review exam be realized and stored on a user's account as being in progress.
 */
public final class GetReviewExamRequest extends AbstractRequestBase {

    /** The ID of the student making the request. */
    public final String studentId;

    /** The version number of the exam to start. */
    public final String examVersion;

    /** True if the exam is being taken as homework; false otherwise. */
    private final boolean takeAsHomework;

    /** True if exam is a practice exam. */
    public final boolean isPractice;

    /**
     * Constructs a new {@code GetReviewExamRequest}.
     *
     * @param theStudentId      the ID of the student making the request
     * @param theExamVersion    the version of the exam being requested
     * @param theTakeAsHomework true if the exam is being taken as a homework assignment
     * @param theIsPractice     true if exam is a practice exam
     */
    public GetReviewExamRequest(final String theStudentId, final String theExamVersion,
                                final boolean theTakeAsHomework, final boolean theIsPractice) {

        super();

        this.studentId = theStudentId;
        this.examVersion = theExamVersion;
        this.takeAsHomework = theTakeAsHomework;
        this.isPractice = theIsPractice;
    }

    /**
     * Constructs a new {@code GetReviewExamRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    GetReviewExamRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String tag = xmlTag();
        final String message = extractMessage(xml, tag);

        this.machineId = extractField(message, "machine-id");

        this.studentId = extractField(message, "student-id");
        this.examVersion = extractField(message, "version");
        this.takeAsHomework = "true".equalsIgnoreCase(extractField(message, "homework"));
        this.isPractice = "true".equalsIgnoreCase(extractField(message, "is-practice"));
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "get-review-exam-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<get-review-exam-request>");

        if (this.studentId != null) {
            builder.addln(" <student-id>", this.studentId, "</student-id>");
        }

        if (this.examVersion != null) {
            builder.addln(" <version>", this.examVersion, "</version>");
        }

        if (this.takeAsHomework) {
            builder.addln(" <homework>true</homework>");
        }

        if (this.isPractice) {
            builder.addln(" <is-practice>true</is-practice>");
        }

        printMachineId(builder);
        builder.addln("</get-review-exam-request>");

        return builder.toString();
    }

    /**
     * Generates a handler that can process this message.
     *
     * @return a handler that can process the message
     */
    @Override
    public AbstractHandlerBase createHandler() {

        return new GetReviewExamHandler();
    }
}
