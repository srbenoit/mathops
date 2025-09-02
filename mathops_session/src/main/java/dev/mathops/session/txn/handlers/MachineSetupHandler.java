package dev.mathops.session.txn.handlers;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.schema.legacy.impl.RawClientPcLogic;
import dev.mathops.db.schema.legacy.rec.RawClientPc;
import dev.mathops.db.schema.legacy.rec.RawTestingCenter;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.MachineSetupReply;
import dev.mathops.session.txn.messages.MachineSetupRequest;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;

/**
 * A handler for machine authorization requests.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class MachineSetupHandler extends AbstractHandlerBase {

    /** The length of machine IDs (must match database field size). */
    private static final int MACHINE_ID_LEN = 40;

    /** A random generator used to create unique machine IDs. */
    private static final Object SYNCH = new Object();

    /** A random generator used to create unique machine IDs. */
    private static Random rand = null;

    /**
     * Construct a new {@code MachineSetupHandler}.
     */
    public MachineSetupHandler() {

        super();
    }

    /**
     * Processes a message from the client.
     *
     * @param cache   the data cache
     * @param message the message received from the client
     * @return the reply to be sent to the client, or null if the connection should be closed
     */
    @Override
    public String process(final Cache cache, final AbstractRequestBase message) {

        setMachineId(message);

        String result;

        if (message instanceof final MachineSetupRequest request) {

            try {
                result = processRequest(cache, request);
            } catch (final SQLException ex) {
                Log.warning(ex);
                final MachineSetupReply reply = new MachineSetupReply();
                reply.error = "Error processing request";
                result = reply.toXml();
            }
        } else {
            final String clsName = message.getClass().getName();
            Log.info("MachineSetupHandler called with ", clsName);

            final MachineSetupReply reply = new MachineSetupReply();
            reply.error = "Invalid request type for machine setup request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache   the data cache
     * @param request the {@code MachineSetupRequest} received from the client
     * @return the generated reply XML to send to the client
     * @throws SQLException if there is an error accessing the database
     */
    private static String processRequest(final Cache cache, final MachineSetupRequest request)
            throws SQLException {

        int code;
        String error = null;
        final String computerId = makeMachineId();
        final String testingCenterId;
        String stationNumber = null;
        String description = null;

        final SystemData systemData = cache.getSystemData();

        if (request.testingCenterId == 0) {
            // Public Internet
            testingCenterId = "0";
            description = request.description;
            code = MachineSetupReply.SUCCESS;
        } else {
            testingCenterId = Integer.toString(request.testingCenterId);
            stationNumber = request.stationNumber;

            // Verify testing center exists
            final RawTestingCenter center = systemData.getTestingCenter(testingCenterId);

            if (center == null) {
                Log.info("Testing center " + testingCenterId + " was not found");
                code = MachineSetupReply.FAILURE;
                error = "Selected testing center is invalid.";
            } else {
                code = MachineSetupReply.SUCCESS;
                description = request.description;
            }
        }

        final LocalDateTime now = LocalDateTime.now();

        // Construct a new client PC record, with a newly generated name
        final RawClientPc obj = new RawClientPc(computerId, testingCenterId, stationNumber,
                description, null, null, RawClientPc.USAGE_ONLINE, RawClientPc.STATUS_UNINITIALIZED,
                now, now, null, RawClientPc.POWER_OFF, null, null, null, null, null, null);

        if (!RawClientPcLogic.insert(cache, obj)) {
            code = MachineSetupReply.FAILURE;
            error = "Unable to create client_pc record.";
        }

        final MachineSetupReply reply = new MachineSetupReply(code, computerId);
        reply.error = error;

        return reply.toXml();
    }

    /**
     * Generate a new, unique machine ID.
     *
     * @return the generated machine ID
     */
    private static String makeMachineId() {

        final byte[] data = new byte[MACHINE_ID_LEN];

        synchronized (SYNCH) {
            if (rand == null) {
                final long seed = System.currentTimeMillis();
                rand = new Random(seed);
            }
            rand.nextBytes(data);
        }

        for (int i = 0; i < MACHINE_ID_LEN; ++i) {

            // Force each byte into range A-Z
            data[i] = (byte) (((int) data[i] & 0x7f) % 26 + 'A');
        }

        return new String(data, StandardCharsets.UTF_8);
    }
}
