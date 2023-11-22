package dev.mathops.web.websocket.help.livehelp;

import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.websocket.help.StudentKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A single session, consisting of a chat conversation, whiteboard, video feed, etc.
 */
public final class LiveHelpSession {

    /** The length of a live help session ID. */
    public static final int SESSION_ID_LEN = 16;

    /** The session ID. */
    public final String sessionId;

    /** The initiating student. */
    public final StudentKey initiatingStudent;

    /** The accepting tutor. */
    private StudentKey acceptingTutor;

    /** The timestamp of last activity, used to time-out inactive sessions. */
    private long lastActivity;

    /** The list of web socket sessions attached to this live help session. */
    private final List<LiveHelpWebSocket> webSockets;

    /** The chat posts in the session. */
    private final List<LiveHelpChatPost> chatPosts;

    // /** The history of all commands sent to the whiteboard. */
    // private WhiteboardHistory whiteboardHistory;

    // /** The current whiteboard state. */
    // private WhiteboardState whiteboardState;

    // /** The student and context information (visible to staff only). */
    // private LiveHelpStudentInfo studentContextInfo;

    /** The cumulative student notes (visible to staff only). */
    private final List<LiveHelpStudentNote> studentNotes;

    /** The set of participating login sessions. */
    private final Set<ImmutableSessionInfo> participants;

    /**
     * Constructs a new {@code LiveHelpSession}.
     *
     * @param theSessionId         the session id
     * @param theInitiatingStudent the information of the initiating student
     */
    public LiveHelpSession(final String theSessionId, final StudentKey theInitiatingStudent) {

        if (theSessionId == null) {
            throw new IllegalArgumentException("Session ID and initiating student ID may not be null");
        }

        this.sessionId = theSessionId;
        this.initiatingStudent = theInitiatingStudent;
        this.lastActivity = System.currentTimeMillis();
        this.webSockets = new ArrayList<>(4);
        this.chatPosts = new ArrayList<>(30);
        this.studentNotes = new ArrayList<>(4);
        this.participants = new HashSet<>(4);
    }

    /**
     * Sets the information and name of the tutor who is accepting the queued request.
     *
     * @param theTutor the tutor information
     */
    public void setAcceptingTutor(final StudentKey theTutor) {

        synchronized (this) {
            this.acceptingTutor = theTutor;
        }
    }

    /**
     * Gets the information of the tutor who is accepting the queued request.
     *
     * @return the tutor information
     */
    StudentKey getAcceptingTutor() {

        synchronized (this) {
            return this.acceptingTutor;
        }
    }

    /**
     * Updates the last activity timestamp to the current timestamp.
     */
    public void touch() {

        synchronized (this) {
            this.lastActivity = System.currentTimeMillis();
        }
    }

    /**
     * Gets the timestamp of the last activity detected on this session.
     *
     * @return the timestamp of the last activity
     */
    long getLastActivity() {

        synchronized (this) {
            return this.lastActivity;
        }
    }

    /**
     * Attaches a web socket session to the live help session.
     *
     * @param theSession the web socket session to attach
     * @return a state update to send to the attaching web socket session to bring it up to date
     */
    public WebSocketUpdate attachSession(final LiveHelpWebSocket theSession) {

        final WebSocketUpdate result = new WebSocketUpdate();

        synchronized (this) {
            // TODO: Populate result with all current state

            this.webSockets.add(theSession);
        }

        return result;
    }

    /**
     * Detaches a web socket session from the live help session.
     *
     * @param theSession the web socket session to detach
     */
    public void dettachSession(final LiveHelpWebSocket theSession) {

        synchronized (this) {
            this.webSockets.remove(theSession);
        }
    }

    /**
     * Sends an update to all attached web sockets. If an attempt to send an update to a web socket fails, that socket
     * is removed, and will have to be re-attached to get a fresh state download.
     *
     * @param update    the update
     * @param staffOnly true to send to staff only
     */
    private void broadcastUpdate(final WebSocketUpdate update, final boolean staffOnly) {

        final Iterator<LiveHelpWebSocket> iter = this.webSockets.iterator();

        while (iter.hasNext()) {
            final LiveHelpWebSocket socket = iter.next();
            final boolean success;

            if (staffOnly) {
                final ImmutableSessionInfo login = socket.getLoginSession();
                if (login != null && login.getEffectiveRole().canActAs(ERole.TUTOR)) {
                    success = LiveHelpWebSocket.sendUpdate(update);
                } else {
                    success = true;
                }
            } else {
                success = LiveHelpWebSocket.sendUpdate(update);
            }

            if (!success) {
                // The failure to write should have already closed the web socket
                iter.remove();
            }
        }
    }

    // TODO: All changes to help session state should generate notifications to all attached
    // web sockets with the update. If sending an update fails to a web socket, that web socket
    // should be detached so it can re-attach and get a complete state snapshot.

    /**
     * Adds new chat post to the session, sending notification of the update to all attached web sockets.
     *
     * @param thePost the post to add
     */
    void addChatPost(final LiveHelpChatPost thePost) {

        synchronized (this) {
            this.chatPosts.add(thePost);

            final WebSocketUpdate update = new WebSocketUpdate();
            update.addChatPost(thePost);
            broadcastUpdate(update, false);
        }
    }

    /**
     * Gets a copy of the list of chat posts in this session.
     *
     * @return the list
     */
    public List<LiveHelpChatPost> getChatPosts() {

        synchronized (this) {
            return new ArrayList<>(this.chatPosts);
        }
    }

    // TODO: Add a student note

    /**
     * Adds new student note to the session, sending notification of the update to all attached web sockets assigned to
     * staff.
     *
     * @param theNote the note to add
     */
    public void addStudentNote(final LiveHelpStudentNote theNote) {

        synchronized (this) {
            this.studentNotes.add(theNote);

            final WebSocketUpdate update = new WebSocketUpdate();
            update.addStudentNote(theNote);
            broadcastUpdate(update, true);
        }
    }

    /**
     * Adds a participant to a live help session.
     *
     * @param theParticipant the participant to add
     */
    public void addParticipant(final ImmutableSessionInfo theParticipant) {

        synchronized (this) {
            this.participants.add(theParticipant);
        }
    }

    /**
     * Removes a participant from a live help session.
     *
     * @param theParticipant the participant to remove
     */
    public void removeParticipant(final ImmutableSessionInfo theParticipant) {

        synchronized (this) {
            this.participants.remove(theParticipant);
        }
    }

    /**
     * Tests whether a login session is a participant in a live help session.
     *
     * @param theParticipant the participant to add
     * @return true if the login session is a participant
     */
    public boolean isParticipant(final ImmutableSessionInfo theParticipant) {

        synchronized (this) {
            return this.participants.contains(theParticipant);
        }
    }
}
