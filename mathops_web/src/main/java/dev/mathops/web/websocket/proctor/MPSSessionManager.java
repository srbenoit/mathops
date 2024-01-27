package dev.mathops.web.websocket.proctor;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.DbConnection;
import dev.mathops.web.cron.Cron;
import dev.mathops.web.cron.ICronJob;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Manager for MPS proctoring sessions.
 */
public final class MPSSessionManager implements ICronJob {

    /** The single instance. */
    private static MPSSessionManager instance;

    /** A map from proctoring session ID to the proctoring session. */
    private final Map<String, MPSSession> sessionsById;

    /** A map from student ID to the proctoring session. */
    private final Map<String, MPSSession> sessionsByStudentId;

    /**
     * Constructs a new {@code MPSSessionManager}.
     */
    private MPSSessionManager() {

        this.sessionsById = new HashMap<>(20);
        this.sessionsByStudentId = new HashMap<>(20);

        DbConnection.registerDrivers();

        Cron.getInstance().registerJob(this);
    }

    /**
     * Executes the job. Called every 10 seconds by the ScheduledExecutorService that is started by the context listener
     * when the servlet container starts (and stopped when the servlet container shuts down).
     *
     * <p>
     * This can serve as a heartbeat for processes that require periodic processing (like testing session timeouts or
     * sending push data on web sockets), or can be used by jobs to test whether the next run time has arrived, in which
     * case the job is executed.
     */
    @Override
    public void exec() {

        final long now = System.currentTimeMillis();

        synchronized (this) {
            final Iterator<Map.Entry<String, MPSSession>> iter = this.sessionsById.entrySet().iterator();

            while (iter.hasNext()) {
                final Map.Entry<String, MPSSession> entry = iter.next();
                final MPSSession session = entry.getValue();

                if (session.timeout < now) {
                    Log.info("Proctoring session ", session.proctoringSessionId, " for student ", session.student.stuId,
                            " timing out");

                    this.sessionsByStudentId.remove(session.student.stuId);
                    iter.remove();
                }
            }
        }
    }

    /**
     * Gets the single instance of the manager, creating it if it has not yet been created.
     *
     * @return the instance
     */
    public static MPSSessionManager getInstance() {

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            if (instance == null) {
                instance = new MPSSessionManager();
            }

            return instance;
        }
    }

//    /**
//     * Gets a copy of the map from session ID to session.
//     *
//     * @return a copy of the map
//     */
//    public Map<String, MPSSession> getSessionsById() {
//
//        synchronized (this) {
//            return new HashMap<>(this.sessionsById);
//        }
//    }

//    /**
//     * Gets the proctoring session with a specified session ID.
//     *
//     * @param psid the session ID
//     * @return the proctoring session; null if none matched
//     */
//     public MPSSession getSession(final String psid) {
//
//     synchronized (this) {
//     return this.sessionsById.get(psid);
//     }
//     }

//    /**
//     * Gets a copy of the list of all sessions.
//     *
//     * @return a copy of the current session list
//     */
//    public List<MPSSession> getAllSessions() {
//
//        synchronized (this) {
//            Log.info("*** Getting all sessions: ",
//                    Integer.toString(this.sessionsById.size()), "/",
//                    Integer.toString(this.sessionsByStudentId.size())); // $NON-NLS-2
//
//            return new ArrayList<>(this.sessionsByStudentId.values());
//        }
//    }

    /**
     * Gets the proctoring session with a specified student ID.
     *
     * @param studentId the student ID
     * @return the proctoring session; null if none matched
     */
    public MPSSession getSessionForStudent(final String studentId) {

        synchronized (this) {
            final MPSSession sess = this.sessionsByStudentId.get(studentId);

            Log.info("*** Get proctoring session for ", studentId, " returning (", sess, ")");

            return sess;
        }
    }

    /**
     * Adds a new session.
     *
     * @param theSession the session to add
     */
    void addSession(final MPSSession theSession) {

        if (theSession == null || theSession.proctoringSessionId == null || theSession.student == null) {
            throw new IllegalArgumentException("Session may not be null or have null student or session ID");
        }

        Log.info("*** Adding proctoring session for ", theSession.student.getScreenName(), " (",
                theSession.student.stuId, ")");

        synchronized (this) {
            this.sessionsById.put(theSession.proctoringSessionId, theSession);
            this.sessionsByStudentId.put(theSession.student.stuId, theSession);
        }
    }

    /**
     * Adds a new session.
     *
     * @param theSession the session to add
     */
    void endSession(final MPSSession theSession) {

        if (theSession == null) {
            throw new IllegalArgumentException("Sesion may not be null");
        }

        Log.info("*** Removing proctoring session for ", theSession.student.getScreenName(), " (",
                theSession.student.stuId, ")");

        synchronized (this) {
            this.sessionsById.remove(theSession.proctoringSessionId);
            this.sessionsByStudentId.remove(theSession.student.stuId);
        }
    }
}
