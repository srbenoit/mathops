package dev.mathops.session.txn.messages;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.ExamStartResultHandler;

/**
 * A network message to indicate that the launch of an exam failed.
 */
public final class ExamStartResultRequest extends AbstractRequestBase {

    /** Result code indicating exam was successfully started. */
    public static final int EXAM_STARTED = 1;

    /** Result code indicating exam could not be retrieved by the client. */
    public static final int CANT_GET_EXAM = 2;

    /** Result code indicating exam XML could not be parsed by client. */
    public static final int CANT_PARSE_EXAM = 3;

    /** The result of trying to start the exam. */
    public Long result;

    /** The version number of the exam to start. */
    private final String examVersion;

    /** The course number of the exam to start. */
    private String examCourse;

    /** The course number of the exam to start. */
    private Long examUnit;

    /** The serial number of the exam being started. */
    public Long serialNumber;

    /**
     * Constructs a new {@code ExamStartResultRequest}.
     *
     * @param theResult       the result of attempting to start the exam
     * @param theExamVersion  the version of the exam being requested
     * @param theSerialNumber the serial number of the exam being started
     */
    public ExamStartResultRequest(final int theResult, final String theExamVersion,
                                  final Long theSerialNumber) {

        super();

        this.result = Long.valueOf((long) theResult);
        this.examVersion = theExamVersion;
        this.serialNumber = theSerialNumber;
    }

    /**
     * Constructs a new {@code ExamStartResultRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    ExamStartResultRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.machineId = extractField(message, "machine-id");

        String value = extractField(message, "result");
        try {
            if (value != null) {
                this.result = Long.valueOf(value);
            }

            this.examVersion = extractField(message, "version");
            this.examCourse = extractField(message, "course");
            value = extractField(message, "unit");

            if (value != null) {
                this.examUnit = Long.valueOf(value);
            }

            value = extractField(message, "serial-number");

            if (value != null) {
                this.serialNumber = Long.valueOf(value);
            }
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "exam-start-result-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<exam-start-result-request>");

        if (this.result != null) {
            builder.addln(" <result>", this.result, "</result>");
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

        if (this.serialNumber != null) {
            builder.addln(" <serial-number>", this.serialNumber,
                    "</serial-number>");
        }

        printMachineId(builder);
        builder.addln("</exam-start-result-request>");

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

        return new ExamStartResultHandler(dbProfile);
    }
}
