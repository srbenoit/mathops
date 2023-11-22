package dev.mathops.web.websocket.help.queue;

import dev.mathops.core.log.Log;
import dev.mathops.core.log.LogBase;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.web.websocket.help.HelpManager;
import dev.mathops.web.websocket.help.IHelpManagerListener;
import dev.mathops.web.websocket.help.StudentKey;
import oracle.jdbc.proxy.annotation.OnError;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * WebSocket service to provide live help queue data.
 *
 * <p>
 * One instance of this class is created per client connection.
 *
 * <p>
 * On new connection, the session is "unauthorized". The first message the client should send on opening the connection
 * is a string with "Session:[login-session-id]". Once that is received and validated, the session will to go the
 * "authorized" state, and will begin considering other types of request.
 */
@ServerEndpoint("/ws/helpqueue")
public final class HelpQueueWebSocket
        implements IHelpManagerListener {

    /** The web socket session. */
    private Session session;

    /** The session (null until a session message is received and verified). */
    private ImmutableSessionInfo loginSession;

    /** A student key derived from the login session. */
    private StudentKey student;

    /** The live help queue entry for this session, if any. */
    private LiveHelpQueueEntry entry;

    /**
     * Flag indicating GetQueue has been processed, meaning periodic updates to queue data will be pushed on each help
     * manager heartbeat.
     */
    private boolean getQueue;

    /**
     * Flag indicating GetStats has been processed, meaning periodic updates to statistics data will be pushed on each
     * help manager heartbeat.
     */
    private boolean getStats;

    /**
     * Flag indicating GetLog has been processed, meaning periodic updates to log data will be pushed on each help
     * manager heartbeat.
     */
    private boolean getLog;

    /**
     * Constructs a new {@code HelpQueueWebSocket}.
     */
    public HelpQueueWebSocket() {

        this.getQueue = false;
    }

    /**
     * Called when the socket is opened.
     *
     * @param theSession the session
     * @param conf       the endpoint configuration
     */
    @OnOpen
    public void open(final Session theSession,
                     final EndpointConfig conf) {

        Log.info("Help Queue websocket opened");

        this.session = theSession;

        HelpManager.getInstance().addListener(this);
    }

    /**
     * Called when a message arrives.
     *
     * @param message the message
     */
    @OnMessage
    public void incoming(final String message) {

        Log.info("Help Queue websocket received message: ", message);

        if (message.startsWith("Session:")) {
            processSessionMessage(message);
        } else if (this.loginSession == null) {
            Log.warning("Invalid message received on Help Queue websocket: "
                    + message);
        } else {
            LogBase.setSessionInfo(this.loginSession.loginSessionId,
                    this.loginSession.getEffectiveUserId());

            if (message.startsWith("Help:")) {
                processHelpMessage(message);
            } else if ("Touch".equals(message)) {
                processTouchMessage();
            } else if ("GetHours".equals(message)) {
                processGetHoursMessage();
            } else if ("GetQueue".equals(message)) {
                processGetQueueMessage();
            } else if ("GetStats".equals(message)) {
                processGetStatsMessage();
            } else if ("GetLog".equals(message)) {
                processGetLogMessage();
            } else {
                // TODO:
            }

            LogBase.setSessionInfo(null, null);
        }
    }

    /**
     * Called when there is an error on the connection.
     *
     * @param t the error
     */
    @OnError
    public void onError(final Throwable t) {

        HelpManager.getInstance().removeListener(this);

        Log.warning("Help Queue web socket error", t);

        try {
            if (this.session != null) {
                this.session.close();
            }
        } catch (final IOException e1) {
            // Ignore
        } finally {
            if (this.entry != null) {
                HelpManager.getInstance().queue.delete(this.entry.id);
                this.entry = null;
            }

            this.session = null;

            if (this.loginSession != null) {

                if (this.loginSession.getEffectiveRole().canActAs(ERole.TUTOR)) {
                    HelpManager.getInstance().queue
                            .removeTutor(this.loginSession.getEffectiveUserId());
                }

                this.loginSession = null;
                this.student = null;
            }
        }
    }

    /**
     * Called when the socket is closed.
     */
    @OnClose
    public void end() {

        HelpManager.getInstance().removeListener(this);

        Log.info("Help Queue websocket closed");

        if (this.entry != null) {
            HelpManager.getInstance().queue.delete(this.entry.id);
            this.entry = null;
        }

        this.session = null;

        if (this.loginSession != null) {

            if (this.loginSession.getEffectiveRole().canActAs(ERole.TUTOR)) {
                HelpManager.getInstance().queue.removeTutor(this.loginSession.getEffectiveUserId());
            }

            this.loginSession = null;
            this.student = null;
        }
    }

    /**
     * Called when the help manager sends a heart-beat (every 10 seconds).
     */
    @Override
    public void heartbeat() {

        if (this.getQueue) {
            processGetQueueMessage();
        }
        if (this.getStats) {
            processGetStatsMessage();
        }
        if (this.getLog) {
            processGetLogMessage();
        }
    }

    /**
     * Instructs the web socket to send a notification to the client that their request has been accepted, so that the
     * client can redirect to the live help site.
     *
     * @param helpSessionId the newly created help session ID (the client will include this in the request for the live
     *                      help session page)
     */
    public void notifyOfAccept(final String helpSessionId) {

        final String msg = "StartHelp:" + helpSessionId;
        Log.info(msg);
        send(msg);
    }

    /**
     * Called on the web socket of an online tutor when the queue changes, to provide quick updates of the tutor's
     * display.
     */
    public void notifyOfQueueChange() {

        this.getQueue = this.loginSession.getEffectiveRole().canActAs(ERole.TUTOR);

        if (this.getQueue) {
            final String json = HelpManager.getInstance().queue.toJSON();
            // Log.fine(json);
            send(json);
        }
    }

    /**
     * Processes a message starting with "Session:", which should be followed immediately by the login session ID.
     *
     * <p>
     * This should be the first message a client sends on connection, to associate a login session ID with the web
     * socket session. No other messages will be processed until this message has been received and the login session ID
     * verified.
     *
     * @param message the message
     */
    private void processSessionMessage(final String message) {

        final String id = message.substring(8);
        final SessionResult result = SessionManager.getInstance().validate(id);
        if (result.session == null) {
            final String msg = result.error == null ? "SessionError:Invalid session ID"
                    : "SessionError:" + result.error;
            Log.warning(msg);
            send(msg);
        } else {
            this.loginSession = result.session;
            this.student = new StudentKey(this.loginSession);

            Log.info("Help Queue connection from ", this.student.studentId,
                    " (", this.student.screenName, ")");

            HelpManager.getInstance().log
                    .log("Help Queue connection from " + this.student.screenName + " ("
                            + this.loginSession.getEffectiveRole().name() + ")");

            if (this.loginSession.getEffectiveRole().canActAs(ERole.TUTOR)) {
                final LiveHelpOnlineTutor tutor =
                        new LiveHelpOnlineTutor(new StudentKey(this.loginSession));
                tutor.setWebSocket(this);
                HelpManager.getInstance().queue.addTutor(tutor);
            }
        }
    }

    /**
     * Processes a message starting with "Help:", which should be followed immediately by one of the following:
     * <ul>
     * <li>c=[course-id]
     * <li>c=[course-id]&u=[unit]
     * <li>c=[course-id]&u=[unit]&o=[obj]
     * <li>h=[hw-session-id]
     * <li>p=[past-exam-session-id]
     * <li>m=[media-id]
     * </ul>
     *
     * <p>
     * This should be the first message a client sends on connection, to associate a login session
     * ID with the web socket session. No other messages will be processed until this message has
     * been received and the login session ID verified.
     *
     * @param message the message
     */
    private void processHelpMessage(final String message) {

        LiveHelpQueueEntry newEntry = null;

        final String query = message.substring(5);

        if (query.startsWith("c=")) {
            final String[] list = query.split("&");
            if (list.length == 1) {
                final String courseId = query.substring(2);
                newEntry = LiveHelpQueueEntry.forCourse(this.student, courseId, -1, -1,
                        System.currentTimeMillis(), this);
            } else if (list.length == 2) {
                if (list[1].startsWith("u=")) {
                    final String courseId = list[0].substring(2);
                    try {
                        final int unit = Integer.parseInt(list[1].substring(2));
                        newEntry = LiveHelpQueueEntry.forCourse(this.student, courseId, unit, -1,
                                System.currentTimeMillis(), this);
                    } catch (final NumberFormatException ex) {
                        Log.warning("Help queue: Invalid help context: ",
                                message, ex);
                        send("HelpError:Invalid help context");
                    }
                } else {
                    Log.warning("Help queue: Invalid help context: ", message);
                    send("HelpError:Invalid help context");
                }
            } else if (list.length == 3) {
                if (list[1].startsWith("u=")
                        && list[2].startsWith("o=")) {
                    final String courseId = list[0].substring(2);
                    try {
                        final int unit = Integer.parseInt(list[1].substring(2));
                        final int obj = Integer.parseInt(list[2].substring(2));
                        newEntry = LiveHelpQueueEntry.forCourse(this.student, courseId, unit, obj,
                                System.currentTimeMillis(), this);
                    } catch (final NumberFormatException ex) {
                        Log.warning("Help queue: Invalid help context: ",
                                message, ex);
                        send("HelpError:Invalid help context");
                    }

                } else {
                    Log.warning("Help queue: Invalid help context: ", message);
                    send("HelpError:Invalid help context");
                }
            }
        } else if (query.startsWith("h=")) {
            final String hwId = query.substring(2);
            newEntry = LiveHelpQueueEntry.forHomework(this.student, hwId,
                    System.currentTimeMillis(), this);
        } else if (query.startsWith("p=")) {
            final String pastId = query.substring(2);
            newEntry = LiveHelpQueueEntry.forPastExam(this.student, pastId,
                    System.currentTimeMillis(), this);
        } else if (query.startsWith("m=")) {
            final String mediaId = query.substring(2);
            newEntry = LiveHelpQueueEntry.forMedia(this.student, mediaId,
                    System.currentTimeMillis(), this);
        } else {
            Log.warning("Help queue: Invalid help context: ", message);
            send("HelpError:Invalid help context");
        }

        if (newEntry != null) {
            final LiveHelpQueue queue = HelpManager.getInstance().queue;

            if (this.entry != null) {
                queue.delete(this.entry.id);
            }
            this.entry = newEntry;

            queue.enqueue(newEntry);
        }
    }

    /**
     * Processes a "Touch" message, which updates the timeout on the request.
     */
    private void processTouchMessage() {

        if (this.entry != null) {
            this.entry.touch();
        }
    }

    /**
     * Processes a "GetHours" message, which requests a download of the weekly hours.
     */
    private void processGetHoursMessage() {

        final String json = HelpManager.getInstance().queue.hours.toJSON();
        send(json);
    }

    /**
     * Processes a "GetQueue" message, which requests a download of the current queue in JSON format. This request will
     * be ignored if the login session associated with the connection does not support the TUTOR role.
     */
    private void processGetQueueMessage() {

        this.getQueue = this.loginSession.getEffectiveRole().canActAs(ERole.TUTOR);

        if (this.getQueue) {
            final String json = HelpManager.getInstance().queue.toJSON();
            send(json);
        }
    }

    /**
     * Processes a "GetStats" message, which requests a download of the current system statistics in JSON format. This
     * request will be ignored if the login session associated with the connection does not support the ADMINISTRATOR
     * role.
     */
    private void processGetStatsMessage() {

        this.getStats = this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR);

        if (this.getStats) {
            // final String json = HelpManager.getInstance().getQueue().toJSON();
            // Log.fine(json);
            // send(json);
        }
    }

    /**
     * Processes a "GetLog" message, which requests a download of the current system statistics in JSON format. This
     * request will be ignored if the login session associated with the connection does not support the ADMINISTRATOR
     * role.
     */
    private void processGetLogMessage() {

        this.getLog = this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR);

        if (this.getLog) {
            final String json = HelpManager.getInstance().log.toJSON();
            send(json);
        }

    }

    /**
     * Attempts to send a message to the remote endpoint. On error, the web socket is closed.
     *
     * @param msg the message
     */
    private void send(final String msg) {

        try {
            this.session.getBasicRemote().sendText(msg);
        } catch (final IOException e) {
            Log.warning("Help Queue error: Failed to send message to client", e);

            try {
                if (this.session != null) {
                    this.session.close();
                }
            } catch (final IOException e1) {
                // Ignore
            } finally {
                this.session = null;

                if (this.entry != null) {
                    HelpManager.getInstance().queue.delete(this.entry.id);
                    this.entry = null;
                }
            }
        }
    }

    /**
     * Notifies an attached administrator that a new tutor has been added.
     *
     * @param theTutor the tutor
     */
    void notifyAdminOfAddedTutor(final LiveHelpOnlineTutor theTutor) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }

    /**
     * Notifies an attached administrator that a tutor has been removed.
     *
     * @param theTutor the tutor
     */
    void notifyAdminOfRemovedTutor(final LiveHelpOnlineTutor theTutor) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }

    /**
     * Notifies an attached administrator that a new administrator has been added.
     *
     * @param theAdministrator the administrator
     */
    void notifyAdminOfAddedAdministrator(
            final LiveHelpOnlineAdministrator theAdministrator) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }

    /**
     * Notifies an attached administrator that an administrator has been removed.
     *
     * @param theAdministrator the administrator
     */
    void notifyAdminOfRemovedAdministrator(
            final LiveHelpOnlineAdministrator theAdministrator) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }

    /**
     * Notifies an attached administrator that a help request has been deactivated due to a timeout.
     *
     * @param theEntry the queue entry that has been deactivated
     */
    void notifyAdminOfDeactivatedRequest(
            final LiveHelpQueueEntry theEntry) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }

    /**
     * Notifies an attached administrator that a help request has been deleted (because it has been canceled by the
     * user).
     *
     * @param theEntry the queue entry that has been deactivated
     */
    void notifyAdminOfDeletedRequest(
            final LiveHelpQueueEntry theEntry) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }

    /**
     * Notifies an attached administrator that an inactive help request has timed out and has been removed.
     *
     * @param theEntry the queue entry that has been removed
     */
    void notifyAdminOfTimedOutInactiveRequest(
            final LiveHelpQueueEntry theEntry) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }

    /**
     * Notifies an attached administrator that an active help request has timed out and has been moved to inactive
     * status.
     *
     * @param theEntry the queue entry that has been moved to inactive status
     */
    void notifyAdminOfTimedOutActiveRequest(
            final LiveHelpQueueEntry theEntry) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }

    /**
     * Notifies an attached administrator that an active help request has received a "touch" to update its timeout.
     *
     * @param theEntry the queue entry that has been touched
     */
    void notifyAdminOfTouchOnActive(final LiveHelpQueueEntry theEntry) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }

    /**
     * Notifies an attached administrator that an inactive help request has received a "touch" to update its timeout and
     * return it to active status.
     *
     * @param theEntry the queue entry that has been touched
     */
    void notifyAdminOfTouchOnInactive(
            final LiveHelpQueueEntry theEntry) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }

    /**
     * Notifies an attached administrator that a help request has been accepted.
     *
     * @param theEntry       the queue entry that has been accepted
     * @param newAvgWaitTime the new average wait time
     */
    void notifyAdminOfRequstAccepted(final LiveHelpQueueEntry theEntry,
                                     final long newAvgWaitTime) {

        if (this.loginSession.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            // TODO:
        }
    }
}
