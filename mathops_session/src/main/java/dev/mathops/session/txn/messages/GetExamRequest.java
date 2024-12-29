package dev.mathops.session.txn.messages;

import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.GetExamHandler;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * A network message to request that a new exam be realized and stored on a user's account as being in progress.
 */
public final class GetExamRequest extends AbstractRequestBase {

    /** The ID of the student making the request. */
    public final String studentId;

    /** The version number of the exam to start. */
    public final String examVersion;

    /** The course number of the exam to start. */
    public String examCourse = null;

    /** The course number of the exam to start. */
    public Integer examUnit = null;

    /** The type of exam to fetch. */
    public String examType = null;

    /** True if the exam is being taken as homework; false otherwise. */
    private final boolean takeAsHomework;

    /** Flag controlling whether coupon checks are done. */
    public boolean checkCoupons = true;

    /** Flag controlling whether eligibility checks are done. */
    public boolean checkEligibility = true;

    /**
     * Constructs a new {@code GetExamRequest}.
     *
     * @param theStudentId      the student ID
     * @param theExamVersion    the version of the exam being requested
     * @param theTakeAsHomework true if the exam is being taken as a homework assignment
     */
    public GetExamRequest(final String theStudentId, final String theExamVersion, final boolean theTakeAsHomework) {

        super();

        this.studentId = theStudentId;
        this.examVersion = theExamVersion;
        this.takeAsHomework = theTakeAsHomework;
    }

    /**
     * Constructs a new {@code GetExamRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    GetExamRequest(final char[] xml) throws IllegalArgumentException {
        super();

        final String tag = xmlTag();
        final String message = extractMessage(xml, tag);

        this.machineId = extractField(message, "machine-id");

        this.studentId = extractField(message, "student-id");
        this.examVersion = extractField(message, "version");
        this.examCourse = extractField(message, "course");

        final String unitValue = extractField(message, "unit");
        if (unitValue != null) {
            try {
                this.examUnit = Integer.valueOf(unitValue);
            } catch (final NumberFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        final String homeworkValue = extractField(message, "homework");
        this.takeAsHomework = "TRUE".equalsIgnoreCase(homeworkValue);

        final String couponsValue = extractField(message, "check-coupons");
        this.checkCoupons = !("FALSE".equalsIgnoreCase(couponsValue));

        final String eligibilityValue = extractField(message, "check-eligibility");
        this.checkEligibility = !("FALSE".equalsIgnoreCase(eligibilityValue));
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "get-exam-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<get-exam-request>");

        if (this.studentId != null) {
            builder.addln(" <student-id>", this.studentId, "</student-id>");
        }

        if (this.examVersion != null) {
            builder.addln(" <version>", this.examVersion, "</version>");
        }

        if (this.examCourse != null) {
            builder.addln(" <course>", this.examCourse, "</course>");
        }

        if (this.examUnit != null) {
            builder.addln(" <unit>", this.examUnit, "</unit>");
        }

        if (this.takeAsHomework) {
            builder.addln(" <homework>true</homework>");
        }

        if (!this.checkCoupons) {
            builder.addln(" <check-coupons>false</check-coupons>");
        }

        if (!this.checkEligibility) {
            builder.addln(" <check-eligibility>false</check-eligibility>");
        }

        printMachineId(builder);
        builder.addln("</get-exam-request>");

        return builder.toString();
    }

    /**
     * Generates a handler that can process this message.
     *
     * @return a handler that can process the message
     */
    @Override
    public AbstractHandlerBase createHandler() {

        return new GetExamHandler();
    }
}
