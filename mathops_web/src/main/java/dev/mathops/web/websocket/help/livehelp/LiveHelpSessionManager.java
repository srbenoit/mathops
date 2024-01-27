package dev.mathops.web.websocket.help.livehelp;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A singleton container for all active live-help sessions.
 */
public final class LiveHelpSessionManager {

    /** Inactivity duration that deletes a "sleeping" session. */
    private static final long DELETE_TIME_OUT = (long) (30 * 60 * 1000);

    /** The single instance, lazily created. */
    private static LiveHelpSessionManager instance;

    /** Map from live session ID to live session. */
    private final Map<String, LiveHelpSession> sessions;

    /** The timestamp of the last periodic scan. */
    private long lastPeriodicScan;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private LiveHelpSessionManager() {

        this.sessions = new HashMap<>(30);
    }

    /**
     * Gets the single instance of the manager, creating it if it has not already been created.
     *
     * @return the instance
     */
    public static LiveHelpSessionManager getInstance() {

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            if (instance == null) {
                instance = new LiveHelpSessionManager();
            }

            return instance;
        }
    }

    /**
     * Adds an active live help session. Called when a staff member accepts a help request.
     *
     * @param theSession the session to add
     */
    public void addSession(final LiveHelpSession theSession) {

        theSession.touch();

        final String key = theSession.sessionId;

        synchronized (this) {
            if (this.sessions.put(key, theSession) != null) {
                Log.warning("Adding live help session ", key,
                        " that already existed.");
            }
        }
    }

    /**
     * Removes a live help session. Called when a session is terminated by either party.
     *
     * @param theSession the session to remove
     */
    public void removeSession(final LiveHelpSession theSession) {

        final String key = theSession.sessionId;

        synchronized (this) {
            if (this.sessions.remove(key, theSession)) {
                Log.info("Removed live help session ", key, CoreConstants.DOT);
            } else {
                Log.warning("Attempt to remove live help session ", key,
                        ", but session did not exist.");
            }
        }
    }

    /**
     * Gets a live help session based on its session ID.
     *
     * @param theSessionId the session ID
     * @return the session, if found; {@code null} if not
     */
    LiveHelpSession getSession(final String theSessionId) {

        synchronized (this) {
            return this.sessions.get(theSessionId);
        }
    }

    /**
     * Scans for a live help session owned by a tutor ID, returning it if found.
     *
     * @param theTutorId the tutor ID
     * @return the session accepted by that tutor if found; {@code null} if not
     */
    public LiveHelpSession getSessionByTutorId(final String theTutorId) {

        LiveHelpSession result = null;

        synchronized (this) {
            Log.info("Scanning for sessions with tutor ID ", theTutorId);

            for (final LiveHelpSession test : this.sessions.values()) {
                Log.info("Testing " + test.getAcceptingTutor().studentId);

                if (theTutorId.equals(test.getAcceptingTutor().studentId)) {
                    Log.info("  FOUND");
                    result = test;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Retrieves a collection of all active sessions. The returned list is a copy - modifying this list does not affect
     * the sessions under management by this object.
     *
     * @return the list of active sessions (in no particular order)
     */
    public List<LiveHelpSession> getAllSessions() {

        return new ArrayList<>(this.sessions.values());
    }

    /**
     * Gets the timestamp of the most recent periodic scan.
     *
     * @return the most recent scan timestamp
     */
    public long getLastPeriodicScan() {

        synchronized (this) {
            return this.lastPeriodicScan;
        }
    }

    /**
     * Scans the list of sessions and removes any that have timed out. This method should be called periodically.
     */
    public void periodicScan() {

        final long now = System.currentTimeMillis();
        final long deleteThreshold = now - DELETE_TIME_OUT;

        synchronized (this) {
            // Test for sleeping sessions that have timed out
            final Iterator<Map.Entry<String, LiveHelpSession>> iter =
                    this.sessions.entrySet().iterator();

            while (iter.hasNext()) {
                final Map.Entry<String, LiveHelpSession> entry = iter.next();
                final long lastActivity = entry.getValue().getLastActivity();
                if (lastActivity < deleteThreshold) {
                    Log.warning("Deleting live help session ", entry.getKey(),
                            " due to timeout");
                    iter.remove();
                }
            }

            this.lastPeriodicScan = now;
        }
    }
}
