package dev.mathops.web.websocket.help.forums;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.field.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.websocket.help.HelpManager;
import oracle.jdbc.proxy.annotation.OnError;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * WebSocket service to provide help forums.
 *
 * <p>
 * One instance of this class is created per client connection.
 *
 * <p>
 * On new connection, the session is "unauthorized". The first message the client should send on opening the connection
 * is a string with "Session:[session-id-from-cookie]". Once that is received, the session will to go the "authorized"
 * state, and the {@code ImmutableSessionInfo} object will be stored.
 */
@ServerEndpoint("/ws/helpforums")
public final class HelpForaWebSocket
        implements IForumListListener {

    /** The web socket session. */
    private Session session;

    /** The session (null until a session message is received and verified). */
    private ImmutableSessionInfo loginSession;

    /**
     * Constructs a new {@code HelpForaWebSocket}.
     */
    public HelpForaWebSocket() {

        // No action
    }

    /**
     * Called when the socket is opened.
     *
     * @param theSession the session
     * @param conf       the endpoint configuration
     */
    @OnOpen
    public void open(final Session theSession, final EndpointConfig conf) {

        Log.info("HelpFora websocket opened");

        this.session = theSession;
    }

    /**
     * Called when a message arrives.
     *
     * @param message the message
     */
    @OnMessage
    public void incoming(final String message) {

        Log.info("HelpFora websocket received message: ", message);

        if (message.startsWith("Session:")) {
            processSessionMessage(message);
        } else if (this.loginSession != null) {

            // TODO:
        } else {
            Log.warning("Invalid message received on HelpFora websocket: " + message);
        }
    }

    /**
     * Called when there is an error on the connection.
     *
     * @param t the error
     */
    @OnError
    public void onError(final Throwable t) {

        Log.warning("HelpFora web socket error", t);

        HelpManager.getInstance().forums.removeListener(this);

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

        Log.info("HelpFora websocket closed");

        HelpManager.getInstance().forums.removeListener(this);
        this.session = null;
    }

    /**
     * Sends a complete list of all forums to the client.
     */
    private void sendCompleteForumsList() {

        // Send the forum list in JSON format...
        final HtmlBuilder json = new HtmlBuilder(100);

        json.add("{\"fora\": [");

        boolean comma = false;
        for (final Forum state : HelpManager.getInstance().forums.getForums()) {

            if (comma) {
                json.add(CoreConstants.COMMA_CHAR);
            }
            json.add("{\"title\": \"", state.title,
                    "\", \"totalPosts\": ", Integer.toString(state.getNumUndeleted()),
                    ", \"totalUnread\": ", Integer.toString(state.getNumUnread()),
                    "}");
            comma = true;
        }

        json.add("]}");

        send(json.toString());
    }

    /**
     * Processes a message starting with "Session:", which should be followed immediately by the login session ID.
     *
     * <p>
     * This should be the first message a client sends on connection, to associate a login session ID with the web
     * socket session. No other messages will be processed until this message has been received and the login session ID
     * verified.
     *
     * <p>
     * Receipt of this message triggers sending the current fora state, and registers the connection to receive
     * future updates to fora list state.
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

            if (this.loginSession.getEffectiveRole().canActAs(ERole.TUTOR)) {

                Log.info("HelpFora connection from ",
                        this.loginSession.getEffectiveUserId(), " (",
                        this.loginSession.getEffectiveScreenName(), ")");

                HelpManager.getInstance().forums.addListener(this);
                sendCompleteForumsList();
            } else {
                final String msg = "SessionError:Not Authorized";
                Log.warning(msg);
                send(msg);
            }
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
            Log.warning("HelpFora error: Failed to send message to client", e);

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
     * Called when a forum has been added to the list.
     *
     * @param theForum the forum that was added
     */
    @Override
    public void forumAdded(final Forum theForum) {

        // TODO:
    }

    /**
     * Called when the title of a forum is changed.
     *
     * @param forum the forum
     */
    @Override
    public void titleChanged(final Forum forum) {

        // TODO:
    }

    /**
     * Called when a post is added to a forum.
     *
     * @param thePost the post that was added
     */
    @Override
    public void postAdded(final ForumPost thePost) {

        // TODO:
    }

    /**
     * Called when the content of a post has been updated. Should be called from within a block synchronized on this
     * object.
     *
     * @param thePost the post whose content changed
     */
    @Override
    public void postContentUpdated(final ForumPost thePost) {

        // TODO:
    }

    /**
     * Called when the state of a post has been updated. Should be called from within a block synchronized on this
     * object.
     *
     * @param thePost the post whose state changed
     */
    @Override
    public void postUpdated(final ForumPost thePost) {

        // TODO:
    }

    /**
     * Called when the "when read" timestamp of a post has been updated. Should be called from within a block
     * synchronized on this object.
     *
     * @param thePost the post whose "when read" timestamp changed
     */
    @Override
    public void postWhenReadUpdated(final ForumPost thePost) {

        // TODO:
    }
}
