package dev.mathops.session.txn.messages;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * A network message to provide client machine setup results.
 */
public final class MachineSetupReply extends AbstractReplyBase {

    /** Result code indicating authentication succeeded. */
    public static final int SUCCESS = 0;

    /** Result code indicating the server encountered an error processing. */
    public static final int FAILURE = 1;

    /** Result code indicating system is temporarily disabled. */
    public static final int SYSTEM_MAINTENANCE = 2;

    /** TRhe result of the setup attempt. */
    public final int resultCode;

    /** The server-generated unique machine ID. */
    public final String machineId;

    /**
     * Constructs a new {@code MachineSetupReply}.
     */
    public MachineSetupReply() {

        super();

        this.resultCode = -1;
        this.machineId = null;
    }

    /**
     * Constructs a new {@code MachineSetupReply}.
     *
     * @param theResultCode the result of the authentication attempt
     * @param theMachineId  the server-generated unique machine ID
     */
    public MachineSetupReply(final int theResultCode, final String theMachineId) {

        super();

        this.resultCode = theResultCode;
        this.machineId = theMachineId;
    }

    /**
     * Constructs a new {@code MachineSetupReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    public MachineSetupReply(final char[] xml) throws IllegalArgumentException {

        super();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");

        final String field = extractField(message, "result");

        final int res;
        try {
            res = Long.valueOf(field).intValue();
            if (res < 0 || res > 2) {
                throw new IllegalArgumentException(Res.fmt(Res.BAD_SETUP_RESULT, field));
            }
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(Res.fmt(Res.NONINT_SETUP_RESULT, field));
        }
        this.resultCode = res;

        this.machineId = extractField(message, "machine-id");
    }

    /**
     * Get the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return The XML tag.
     */
    static String xmlTag() {

        return "machine-setup-reply";
    }

    /**
     * Generate the XML representation of the message.
     *
     * @return The XML representation.
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<machine-setup-reply>");

        builder.addln(" <result>", Integer.toString(this.resultCode), "</result>");

        if (this.machineId != null) {
            builder.addln(" <machine-id>", this.machineId, "</machine-id>");
        }

        printError(builder);

        builder.addln("</machine-setup-reply>");

        return builder.toString();
    }
}
