package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

/**
 * A reply to a testing station status request.
 */
public final class TestingStationStatusReply extends AbstractReplyBase {

    /** The usage setting for the testing station. */
    public String stationUsage;

    /** The testing station status. */
    public Integer status;

    /** The student ID assigned to the station. */
    public String studentId;

    /** The student name assigned to the station. */
    public String studentName;

    /** The course of the test being taken at the station. */
    public String course;

    /** The unit of the test being taken at the station. */
    public Integer unit;

    /** The exam version being taken at the station. */
    public String version;

    /**
     * Constructs a new {@code TestingStationStatusReply}.
     */
    public TestingStationStatusReply() {

        super();
    }

    /**
     * Constructs a new {@code TestingStationStatusReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    TestingStationStatusReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");

        this.stationUsage = extractField(message, "usage");
        this.status = Integer.valueOf(extractField(message, "status"));
        this.studentId = extractField(message, "student-id");
        this.studentName = extractField(message, "student-name");
        this.course = extractField(message, "course");

        final String text = extractField(message, "unit");
        if (text != null && !text.isEmpty()) {
            try {
                this.unit = Integer.valueOf(text);
            } catch (final NumberFormatException ex) {
                Log.warning(Res.get(Res.CANT_PARSE_UNIT), ex);
            }
        }

        this.version = extractField(message, "exam-version");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    public static String xmlTag() {

        return "testing-station-status-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<testing-station-status-reply>");

        if (this.stationUsage != null) {
            builder.addln(" <usage>", this.stationUsage, "</usage>");
        }

        if (this.status != null) {
            builder.addln(" <status>", this.status, "</status>");
        }

        if (this.studentId != null) {
            builder.addln(" <student-id>", this.studentId, "</student-id>");
        }

        if (this.studentName != null) {
            builder.addln(" <student-name>", this.studentName, "</student-name>");
        }

        if (this.course != null) {
            builder.addln(" <course>", this.course, "</course>");
        }

        if (this.unit != null) {
            builder.addln(" <unit>", this.unit, "</unit>");
        }

        if (this.version != null) {
            builder.addln(" <exam-version>", this.version, "</exam-version>");
        }

        printError(builder);
        builder.addln("</testing-station-status-reply>");

        return builder.toString();
    }
}
