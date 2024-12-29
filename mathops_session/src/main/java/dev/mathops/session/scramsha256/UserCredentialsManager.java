package dev.mathops.session.scramsha256;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.HexEncoder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawLoginsLogic;
import dev.mathops.db.old.rawrecord.RawLogins;
import dev.mathops.text.parser.Base64;
import dev.mathops.text.parser.ParsingException;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A single-instance manager for user credentials.
 */
final class UserCredentialsManager {

    /** The number of iterations. */
    static final int ITERATIONS = 4096;

    /** The single instance. */
    private static UserCredentialsManager instance;

    /** Map from normalized username to credentials. */
    private final Map<String, UserCredentials> credentials;

    /**
     * Private constructor to prevent direct instantiation.
     *
     * @param cache the data cache
     */
    private UserCredentialsManager(final Cache cache) {

        this.credentials = new HashMap<>(10);

        try {
            final List<RawLogins> allLogins = RawLoginsLogic.INSTANCE.queryAll(cache);

            for (final RawLogins record : allLogins) {
                if ("ADM".equals(record.userType)) {

                    final String saltBase64 = record.salt;
                    final String storedKeyHex = record.storedKey;
                    final String serverKeyHex = record.serverKey;

                    if (saltBase64 != null && storedKeyHex != null && serverKeyHex != null) {
                        try {
                            final byte[] salt = Base64.decode(saltBase64);
                            final byte[] storedKey = HexEncoder.decode(storedKeyHex);
                            final byte[] serverKey = HexEncoder.decode(serverKeyHex);

                            if (salt.length == 24 && storedKey.length == 32 && serverKey.length == 32) {
                                final UserCredentials cred = new UserCredentials(record.userType,
                                        record.userName, salt, storedKey, serverKey, ITERATIONS);

                                this.credentials.put(new String(cred.normalizedUsername, StandardCharsets.UTF_8), cred);
                            }
                        } catch (final ParsingException ex) {
                            Log.warning("Failed to decode salt from Base64 '", saltBase64, "', stored key from hex '"
                                    , storedKeyHex, "', or server key from hex ", serverKeyHex, "'", ex);
                        } catch (final IllegalArgumentException ex) {
                            Log.warning("Failed to create user credentials", ex);
                        }
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query logins table", ex);
        }
    }

    /**
     * Gets the single instance.
     *
     * @param cache the data cache
     * @return the instance
     */
    static UserCredentialsManager getInstance(final Cache cache) {

        // Called only from WebServiceSite, and only from within a block synchronized on a single object, so no
        // need for synchronization here.

        if (instance == null) {
            instance = new UserCredentialsManager(cache);
        }

        return instance;
    }

    /**
     * Looks up credentials for a username.
     *
     * @param normalizedUsername the normalized username
     * @return the credentials if found; null if not
     */
    UserCredentials getCredentials(final String normalizedUsername) {

        return this.credentials.get(normalizedUsername);
    }

    /**
     * Test method.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();
        final DbProfile dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);

        if (dbProfile == null) {
            Log.warning("Unable to create production context.");
        } else {
            final DbContext primaryCtx = dbProfile.getDbContext(ESchemaUse.PRIMARY);

            if (primaryCtx == null) {
                Log.warning("Unable to create PRIMARY database context.");
            } else {
                try {
                    final DbConnection conn = primaryCtx.checkOutConnection();
                    final Cache cache = new Cache(dbProfile, conn);
                    try {
                        final Random rnd = new Random(System.currentTimeMillis());

                        // Client gathers username and password, sends client-first to server
                        final String clientUsername = "benoit";
                        final String clientPassword = "testPassword";
                        final ClientFirstMessage clientClientFirst =
                                new ClientFirstMessage(clientUsername, rnd);

                        // Server reads client-first, responds with server-first
                        final ClientFirstMessage serverClientFirst =
                                new ClientFirstMessage(clientClientFirst.hex);
                        if (!serverClientFirst.hex.equals(clientClientFirst.hex)) {
                            Log.warning("Client-first hex mismatch");
                        }

                        final UserCredentials cred =
                                getInstance(cache).credentials
                                        .get(new String(serverClientFirst.normalizedUsername, StandardCharsets.UTF_8));
                        final ServerFirstMessage serverServerFirst =
                                new ServerFirstMessage(serverClientFirst, cred, rnd);

                        // Client reads server-first, responds with client-final
                        final ServerFirstMessage clientServerFirst =
                                new ServerFirstMessage(serverServerFirst.hex, clientClientFirst);
                        if (!clientServerFirst.hex.equals(serverServerFirst.hex)) {
                            Log.warning("Server-first hex mismatch");
                        }

                        final ClientFinalMessage clientClientFinal = new ClientFinalMessage(
                                clientPassword, clientClientFirst, clientServerFirst);

                        // Server reads client-final, responds with server-final
                        final ClientFinalMessage serverClientFinal = new ClientFinalMessage(
                                clientClientFinal.hex, serverClientFirst, serverServerFirst, cred);
                        final String token = CoreConstants.newId(30);
                        final ServerFinalMessage serverServerFinal =
                                new ServerFinalMessage(serverClientFinal, cred, token);

                        // Client reads server-final, obtains token
                        final ServerFinalMessage clientServerFinal =
                                new ServerFinalMessage(serverServerFinal.hex);

                        Log.info("Negotiated token: ", clientServerFinal.token);

                    } finally {
                        primaryCtx.checkInConnection(conn);
                    }
                } catch (final SQLException ex) {
                    Log.warning("EXCEPTION: " + ex.getMessage());
                }
            }
        }
    }
}
