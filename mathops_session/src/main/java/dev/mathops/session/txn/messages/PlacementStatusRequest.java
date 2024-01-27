package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.PlacementStatusHandler;

/**
 * A request for the student's placement status.
 */
public final class PlacementStatusRequest extends AbstractRequestBase {

    /** The ID of the student requesting the status. */
    public final String studentId;

    /**
     * Constructs a new {@code PlacementStatusRequest}.
     *
     * @param theStudentId the ID of the student requesting the status
     */
    public PlacementStatusRequest(final String theStudentId) {

        super();

        this.studentId = theStudentId;
    }

    /**
     * Constructs a new {@code PlacementStatusRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    PlacementStatusRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.machineId = extractField(message, "machine-id");
        this.studentId = extractField(message, "student-id");
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "placement-status-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return The XML representation.
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<placement-status-request>");

        if (this.studentId != null) {
            builder.addln(" <student-id>", this.studentId, "</student-id>");
        }

        printMachineId(builder);
        builder.addln("</placement-status-request>");

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

        return new PlacementStatusHandler(dbProfile);
    }
}
