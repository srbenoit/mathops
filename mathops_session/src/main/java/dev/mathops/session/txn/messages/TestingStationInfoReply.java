package dev.mathops.session.txn.messages;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * A reply to a testing station info request.
 */
public final class TestingStationInfoReply extends AbstractReplyBase {

    /** The name of the testing center, null if station is invalid. */
    public String testingCenterName;

    /** The number of the testing station, null if station is invalid. */
    public String stationNumber;

    /** The usage setting for the testing station. */
    public String stationUsage;

    /** The testing station status. */
    public Integer status;

    /**
     * Constructs a new {@code TestingStationInfoReply}.
     */
    public TestingStationInfoReply() {

        super();
    }

    /**
     * Constructs a new {@code TestingStationInfoReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    TestingStationInfoReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");
        this.testingCenterName = extractField(message, "center-name");
        this.stationNumber = extractField(message, "station-number");
        this.stationUsage = extractField(message, "usage");
        this.status = Integer.valueOf(extractField(message, "status"));
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    public static String xmlTag() {

        return "testing-station-info-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<testing-station-info-reply>");

        if (this.testingCenterName != null) {
            builder.addln(" <center-name>", this.testingCenterName,
                    "</center-name>");
        }

        if (this.stationNumber != null) {
            builder.addln(" <station-number>", this.stationNumber,
                    "</station-number>");
        }

        if (this.stationUsage != null) {
            builder.addln(" <usage>", this.stationUsage, "</usage>");
        }

        if (this.status != null) {
            builder.addln(" <status>", this.status, "</status>");
        }

        printError(builder);
        builder.addln("</testing-station-info-reply>");

        return builder.toString();
    }
}
