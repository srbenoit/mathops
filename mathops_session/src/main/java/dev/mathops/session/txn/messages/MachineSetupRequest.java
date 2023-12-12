package dev.mathops.session.txn.messages;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.parser.xml.XmlEscaper;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.MachineSetupHandler;

import java.util.Map;
import java.util.TreeMap;

/**
 * A network message that adds a new machine to the database. The new machine will be assigned a unique ID, and may be
 * assigned to a testing center. The server's SSL certificate will be sent back to the client in the reply. It contains
 * the data entered in a request form by the user setting up the new machine. This form should contain the following
 * fields, with the names given here:
 *
 * <pre>
 *   testing-center-id (Integer)
 *   description (String)
 *   station-number (String)
 * </pre>
 */
public final class MachineSetupRequest extends AbstractRequestBase {

    /** Constant indicating machine is on the Internet. */
    public static final int PUBLIC_INTERNET = 0;

    /** The testing center this machine is in. */
    public final int testingCenterId;

    /** The station number if the machine is not on the Internet. */
    public final String stationNumber;

    /** A description of the machine. */
    public final String description;

    /** The client machine's system properties. */
    public final Map<String, String> systemProperties;

    /**
     * Constructs a new {@code MachineSetupRequest}.
     *
     * @param theTestingCenterId  the testing center this machine is in
     * @param theStationNumber    the station number if the machine is not on the Internet
     * @param theDescription      the description of the machine
     * @param theSystemProperties the client machine's system properties
     */
    public MachineSetupRequest(final int theTestingCenterId, final String theStationNumber,
                               final String theDescription, final Map<String, String> theSystemProperties) {

        super();

        this.testingCenterId = theTestingCenterId;
        this.stationNumber = theStationNumber;
        this.description = theDescription;
        this.systemProperties = theSystemProperties;
    }

    /**
     * Construct a new {@code MachineSetupRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    MachineSetupRequest(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.machineId = extractField(message, "machine-id");

        String sub = extractField(message, "testing-center");

        try {
            this.testingCenterId = Integer.parseInt(sub);

            if (this.testingCenterId < 0) {
                throw new IllegalArgumentException(Res.fmt(Res.BAD_CENTER_ID, sub));
            }
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(Res.fmt(Res.NONINT_CENTER_ID, sub));
        }

        sub = extractField(message, "station-number");

        if ((sub != null) && (!sub.isEmpty())) {
            this.stationNumber = XmlEscaper.unescape(sub);
        } else {
            this.stationNumber = null;
        }

        sub = extractField(message, "description");

        if ((sub != null) && (!sub.isEmpty())) {
            this.description = XmlEscaper.unescape(sub);
        } else {
            this.description = null;
        }

        sub = extractField(message, "system-properties");

        if (sub != null && !sub.isEmpty()) {
            this.systemProperties = new TreeMap<>();

            final String[] list = extractFieldList(sub, "property");

            if (list != null) {
                for (final String s : list) {
                    final int pos = s.indexOf('=');

                    if (pos != -1) {
                        this.systemProperties.put(XmlEscaper.unescape(s.substring(0, pos)),
                                XmlEscaper.unescape(s.substring(pos + 1)));
                    }
                }
            }
        } else {
            this.systemProperties = null;
        }
    }

    /**
     * Method to tell the message to free any resources allocated to it. The message will assume no other methods will
     * be called after this one.
     */
    @Override
    public void die() {

        this.systemProperties.clear();
        super.die();
    }

    /**
     * Get the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "machine-setup-request";
    }

    /**
     * Generate the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<machine-setup-request>");

        builder.addln(" <testing-center>",
                Integer.toString(this.testingCenterId), "</testing-center>");

        if (this.stationNumber != null) {
            builder.addln(" <station-number>", XmlEscaper.escape(this.stationNumber),
                    "</station-number>");
        }

        if (this.description != null) {
            builder.addln(" <description>", XmlEscaper.escape(this.description),
                    "</description>");
        }

        if (this.systemProperties != null) {
            builder.addln(" <system-properties>");

            for (final Map.Entry<String, String> entry : this.systemProperties.entrySet()) {
                builder.addln("  <property>", XmlEscaper.escape(entry.getKey()), "=", XmlEscaper.escape(entry.getValue()),
                        "</property>");
            }

            builder.addln(" </system-properties>");
        }

        printMachineId(builder);
        builder.addln("</machine-setup-request>");

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

        return new MachineSetupHandler(dbProfile);
    }
}
