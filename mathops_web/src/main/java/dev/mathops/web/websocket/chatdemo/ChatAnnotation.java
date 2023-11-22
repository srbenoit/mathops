package dev.mathops.web.websocket.chatdemo;

import dev.mathops.core.log.Log;
import oracle.jdbc.proxy.annotation.OnError;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chat WebSocket example.
 */
@ServerEndpoint("/ws/chat")
final class ChatAnnotation {

    /** Guest prefix. */
    private static final String GUEST_PREFIX = "Guest";

    /** Incrementing connection ID. */
    private static final AtomicInteger CONNECTION_IDS = new AtomicInteger(0);

    /** Set of connections. */
    private static final Collection<ChatAnnotation> CONNECTIONS = new CopyOnWriteArraySet<>();

    /** User's nickname. */
    private final String nickname;

    /** Session associated with connection. */
    private Session session;

    /**
     * Constructs a new {@code ChatAnnotation}.
     */
    ChatAnnotation() {

        this.nickname = GUEST_PREFIX + CONNECTION_IDS.getAndIncrement();
    }

    /**
     * Called when the socket is opened.
     *
     * @param theSession the session
     */
    @OnOpen
    public void start(final Session theSession) {

        Log.info("Chat websocket opened");

        this.session = theSession;
        CONNECTIONS.add(this);

        final String message = String.format("* %s %s", this.nickname, "has joined.");
        broadcast(message);
    }

    /**
     * Called when the socket is closed.
     */
    @OnClose
    public void end() {

        Log.info("Chat websocket closed");

        CONNECTIONS.remove(this);
        final String message = String.format("* %s %s", this.nickname, "has disconnected.");
        broadcast(message);
    }

    /**
     * Called when a text message arrives.
     *
     * @param message the message
     */
    @OnMessage
    public void incoming(final String message) {

        Log.info("Chat websocket received message");

        // Never trust the client
        final String filteredMessage = String.format("%s: %s", this.nickname, HTMLFilter.filter(message));

        broadcast(filteredMessage);
    }

    /**
     * Called when there is an error on the connection.
     *
     * @param t the error
     */
    @OnError
    public void onError(final Throwable t) {

        Log.warning("Chat Error: " + t.toString(), t);
    }

    /**
     * Broadcasts a message to all participants.
     *
     * @param msg the message
     */
    private static void broadcast(final String msg) {

        for (final ChatAnnotation client : CONNECTIONS) {
            try {
                synchronized (client) {
                    client.session.getBasicRemote().sendText(msg);
                }
            } catch (final IOException e) {
                Log.info("Chat Error: Failed to send message to client", e);
                CONNECTIONS.remove(client);
                try {
                    client.session.close();
                } catch (final IOException e1) {
                    // Ignore
                }
                final String message = String.format("* %s %s", client.nickname, "has been disconnected.");
                broadcast(message);
            }
        }
    }
}
