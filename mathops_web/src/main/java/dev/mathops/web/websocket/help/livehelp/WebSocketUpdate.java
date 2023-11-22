package dev.mathops.web.websocket.help.livehelp;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for data to be sent to a web socket client to update its state with respect to a live help session.
 *
 * <p>
 * When the web socket is initially attached to a live help session, this will contain the complete state of that
 * session. After the initial connection, it will contain only updates as activity occurs on the live help session.
 */
public final class WebSocketUpdate {

    /** New chat posts. */
    private List<LiveHelpChatPost> chatPosts;

    /** New student notes. */
    private List<LiveHelpStudentNote> studentNotes;

    /**
     * Constructs a new {@code WebSocketUpdate}.
     */
    WebSocketUpdate() {

        // No action
    }

    /**
     * Adds new chat post to the update object.
     *
     * @param thePost the post to add
     */
    void addChatPost(final LiveHelpChatPost thePost) {

        if (this.chatPosts == null) {
            this.chatPosts = new ArrayList<>(10);
        }
        this.chatPosts.add(thePost);
    }

    /**
     * Adds new student note to the update object.
     *
     * @param theNote the note to add
     */
    void addStudentNote(final LiveHelpStudentNote theNote) {

        if (this.studentNotes == null) {
            this.studentNotes = new ArrayList<>(10);
        }
        this.studentNotes.add(theNote);
    }

}
