package dev.mathops.web.websocket.help;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.cron.Cron;
import dev.mathops.web.cron.ICronJob;
import dev.mathops.web.websocket.help.conversation.ConversationDatabase;
import dev.mathops.web.websocket.help.conversation.ConversationsContainer;
import dev.mathops.web.websocket.help.forums.ForumDatabase;
import dev.mathops.web.websocket.help.forums.ForumList;
import dev.mathops.web.websocket.help.livehelp.LiveHelpSession;
import dev.mathops.web.websocket.help.livehelp.LiveHelpSessionManager;
import dev.mathops.web.websocket.help.queue.LiveHelpLog;
import dev.mathops.web.websocket.help.queue.LiveHelpQueue;
import dev.mathops.web.websocket.help.queue.LiveHelpQueueEntry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A single-instance manager for the help subsystem.
 *
 * <p>
 * This manager owns all {@code LiveHelpCalendar} and {@code LiveHelpAssistant} database objects. It caches these
 * objects on startup, and any changes to these objects should be done through this manager, or they will not be
 * recognized until the next system start.
 */
public final class HelpManager implements ICronJob {

    /** The single instance (lazily instantiated). */
    private static HelpManager instance;

    /** The registered listeners. */
    private final List<IHelpManagerListener> listeners;

    /** The queue of requests awaiting assignment to a learning assistant. */
    public final LiveHelpQueue queue;

    /** The log. */
    public final LiveHelpLog log;

    /** The live help session manager. */
    private final LiveHelpSessionManager liveSessions;

    /** The list of Forums for asynchronous, public posts. */
    public final ForumList forums;

    /** The conversations database manager. */
    public final ConversationDatabase conversationDatabase;

    /** The container for all Conversations for asynchronous, private posts. */
    public final ConversationsContainer conversations;

    /**
     * Constructs a new {@code HelpManager}.
     */
    private HelpManager() {

        this.listeners = new ArrayList<>(10);

        this.queue = new LiveHelpQueue();
        this.log = new LiveHelpLog();

        this.liveSessions = LiveHelpSessionManager.getInstance();

        this.conversationDatabase = new ConversationDatabase();
        this.conversations = this.conversationDatabase.load();

        this.forums = new ForumList();

        ForumDatabase.load(this.forums);

        // TODO: Cache all LiveHelpCalendar and LiveHelpAssistant records using the
        // "LiveHelpManager" context to select a backing database.

        Cron.getInstance().registerJob(this);

        this.log.log("Help Manager started at "
                + TemporalUtils.FMT_MDY_AT_HMS_A.format(LocalDateTime.now()));
    }

    /**
     * Gets the single instance of the {@code LiveHelpManager}.
     *
     * @return the instance
     */
    public static HelpManager getInstance() {

        synchronized (CoreConstants.INSTANCE_SYNCH) {

            if (instance == null) {
                instance = new HelpManager();
            }

            return instance;
        }
    }

    /**
     * Adds a listener.
     *
     * @param theListener the listener to add
     */
    public void addListener(final IHelpManagerListener theListener) {

        synchronized (this.listeners) {
            this.listeners.add(theListener);
        }
    }

    /**
     * Removes a listener.
     *
     * @param theListener the listener to remove
     */
    public void removeListener(final IHelpManagerListener theListener) {

        synchronized (this.listeners) {
            this.listeners.remove(theListener);
        }
    }

    /**
     * Attempts to accept the next queued help request and assign it to a TUTOR-capable login session.
     *
     * <p>
     * If the supplied login session already has an open live help session in progress, that session is returned (a
     * tutor could run two concurrent live help sessions, but each would have to be through a separate login session).
     *
     * @param session the login session to which assign the request
     * @return the live help session creates for the accepted request; {@code null} if no active request is available in
     *         the queue
     */
    public LiveHelpSession acceptRequest(final ImmutableSessionInfo session) {

        final String tutorId = session.getEffectiveUserId();

        // If "session" already has an open live help session, just return that one.
        LiveHelpSession lhsess = LiveHelpSessionManager.getInstance().getSessionByTutorId(tutorId);

        if (lhsess == null) {
            final LiveHelpQueueEntry entry = this.queue.pop();

            if (entry != null) {
                Log.info("Live Help request from '", entry.student.screenName,
                        "' accepted by ", session.getEffectiveScreenName());

                final String sid = CoreConstants.newId(LiveHelpSession.SESSION_ID_LEN);

                lhsess = new LiveHelpSession(sid, entry.student);
                lhsess.setAcceptingTutor(new StudentKey(session));

                this.liveSessions.addSession(lhsess);

                // Notify the student socket that the connection is open, so the studnt's browse
                // can redirect to the session
                entry.studentWebSocket.notifyOfAccept(sid);
            }
        }

        return lhsess;
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

        this.queue.checkTimeouts();

        synchronized (this.listeners) {
            for (final IHelpManagerListener listener : this.listeners) {
                listener.heartbeat();
            }
        }
    }
}
