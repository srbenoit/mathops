package dev.mathops.web.websocket.help.livehelp;

import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
import dev.mathops.web.websocket.help.StudentKey;
import oracle.jdbc.proxy.annotation.OnError;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * WebSocket service to provide live help session communications. This class does not do video and audio streaming -
 * that is handled by a separate a TURN server.
 *
 * <p>
 * One instance of this class is created per client connection.
 *
 * <p>
 * On new connection, the session is "unauthorized". The first message the client should send on opening the connection
 * is a string with "Session:[login-session-id]&[help-session-id]". Once that is received and validated, the session
 * will to go the "authorized" state.
 */
@ServerEndpoint("/ws/livehelp")
public final class LiveHelpWebSocket {

    /** The web socket session. */
    private Session session;

    /** The login session (null until a session message is received and verified). */
    private ImmutableSessionInfo loginSession;

    /** The help session (null until a session message is received and verified). */
    private LiveHelpSession helpSession;

    /**
     * Constructs a new {@code LiveHelpWebSocket}.
     */
    LiveHelpWebSocket() {

        // No action
    }

    /**
     * Gets the login session associated with this web socket.
     *
     * @return the login session
     */
    ImmutableSessionInfo getLoginSession() {

        synchronized (this) {
            return this.loginSession;
        }
    }

    /**
     * Gets the help session associated with this web socket.
     *
     * @return the help session
     */
    public LiveHelpSession getHelpSession() {

        synchronized (this) {
            return this.helpSession;
        }
    }

    /**
     * Called when the socket is opened.
     *
     * @param theSession the session
     * @param conf       the endpoint configuration
     */
    @OnOpen
    public void open(final Session theSession, final EndpointConfig conf) {

        Log.info("Live help session websocket opened");

        this.session = theSession;
    }

    /**
     * Called when a message arrives.
     *
     * @param message the message
     */
    @OnMessage
    public void incoming(final String message) {

        Log.info("Live help session websocket received message: ", message);

        if (message.startsWith("Session:")) {
            processSessionMessage(message);
        } else {
            synchronized (this) {
                if (this.loginSession != null) {

                    if (message.startsWith("Chat:")) {
                        processChatMessage(message);
                    } else {
                        Log.warning("Bad message received on Live help session websocket: " + message);
                    }
                }
            }
        }
    }

    /**
     * Called when there is an error on the connection.
     *
     * @param t the error
     */
    @OnError
    public void onError(final Throwable t) {

        Log.warning("Live help session web socket error", t);

        synchronized (this) {
            this.loginSession = null;
            this.helpSession = null;
        }

        try {
            if (this.session != null) {
                this.session.close();
            }
        } catch (final IOException e1) {
            // Ignore
        } finally {
            this.session = null;
        }
    }

    /**
     * Called when the socket is closed.
     */
    @OnClose
    public void end() {

        Log.info("Live help session websocket closed");

        synchronized (this) {
            this.loginSession = null;
            this.helpSession = null;
        }

        this.session = null;
    }

    /**
     * Processes a message starting with "Session:", which should be followed immediately by the login session ID, an
     * ampersand, and the help session ID.
     *
     * <p>
     * This should be the first message a client sends on connection, to associate a login and help session ID with the
     * web socket session. No other messages will be processed until this message has been received and the login and
     * help session IDs verified.
     *
     * @param message the message
     */
    private void processSessionMessage(final String message) {

        final int amper = message.indexOf('&');
        if (amper == -1) {
            final String msg = "SessionError:No help session ID provided";
            Log.warning(msg);
            send(msg);
        } else {
            final String loginId = message.substring(8, amper);

            final SessionResult loginResult = SessionManager.getInstance().validate(loginId);

            if (loginResult.session == null) {
                final String msg = loginResult.error == null ? "SessionError:Invalid login session ID"
                        : "SessionError:" + loginResult.error;
                Log.warning(msg);
                send(msg);
            } else {
                final String helpId = message.substring(amper + 1);
                final LiveHelpSession helpResult = LiveHelpSessionManager.getInstance().getSession(helpId);

                if (helpResult == null) {
                    final String msg = loginResult.error == null ? "SessionError:Invalid help session ID"
                            : "SessionError:" + loginResult.error;
                    Log.warning(msg);
                    send(msg);
                } else {
                    synchronized (this) {
                        this.loginSession = loginResult.session;
                        this.helpSession = helpResult;

                        Log.info("Live help connection from ", this.loginSession.getEffectiveUserId(), " (",
                                this.loginSession.getEffectiveScreenName(), ")");
                    }
                }
            }
        }
    }

    /**
     * Processes a message starting with "Chat:", which should be followed immediately by a JSON-encoded chat post.
     *
     * <pre>
     * {
     *   "id": "...",
     *   "type": "T|M|H|I",
     *   "content": "..."
     * }
     * </pre>
     *
     * <p>
     * If this message is received before a validated Session:message, it is ignored.
     *
     * @param message the message
     */
    private void processChatMessage(final String message) {

        final String json = message.substring(5);

        try {
            final Object obj = JSONParser.parseJSON(json);
            if (obj instanceof final JSONObject jobj) {

                final String id = jobj.getStringProperty("id");
                final EChatPostType type = EChatPostType.forCode(jobj.getStringProperty("type"));
                final String content = jobj.getStringProperty("content");

                if (id == null || type == null || content == null) {
                    Log.warning("Parsed JSON chat post message missing requird fields: ", message);
                } else {
                    final LiveHelpChatPost post = new LiveHelpChatPost(id, type,
                            this.loginSession.getEffectiveRole() == ERole.STUDENT,
                            new StudentKey(this.loginSession), content);
                    this.helpSession.addChatPost(post);
                }
            } else {
                Log.warning("Parsed JSON chat post message is not JSONObject: ", obj.getClass().getName());
            }
        } catch (final ParsingException ex) {
            Log.warning("Failed to parse JSON chat post message: ", message, ex);
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
            Log.warning("Live help session error: Failed to send message to client", e);

            try {
                if (this.session != null) {
                    this.session.close();
                }
            } catch (final IOException e1) {
                // Ignore
            } finally {
                this.session = null;
            }
        }
    }

    /**
     * Sends an update of the session state to an attached client.
     *
     * @param theUpdate the update to send
     * @return true if sending succeeded
     */
    static boolean sendUpdate(final WebSocketUpdate theUpdate) {

        // TODO:

        return true;
    }
}
