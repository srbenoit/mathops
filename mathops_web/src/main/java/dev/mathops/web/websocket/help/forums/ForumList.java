package dev.mathops.web.websocket.help.forums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The list of available forums. This is loaded from the database on startup. Posts should not be updated except by this
 * package, so there is no need to re-query after the initial load.
 */
public final class ForumList {

    /** Listeners registered to receive updates of the forum list. */
    private final List<IForumListListener> listeners;

    /** The loaded forums. */
    private final List<Forum> forums;

    /** An unmodifiable view of the loaded forums. */
    private final List<Forum> unmodifiable;

    /**
     * Constructs a new {@code ForumList}.
     */
    public ForumList() {

        this.listeners = new ArrayList<>(20);
        this.forums = new ArrayList<>(20);
        this.unmodifiable = Collections.unmodifiableList(this.forums);
    }

    /**
     * Registers a listener to be notified of changes to the forum list. This triggers sending the current state of
     * the forum list to the listener.
     *
     * @param listener the listener to add
     */
    public void addListener(final IForumListListener listener) {

        synchronized (this) {
            this.listeners.add(listener);
        }
    }

    /**
     * Removes a listener that was previously registered with {@code addListener}.
     *
     * @param listener the listener to remove
     */
    void removeListener(final IForumListListener listener) {

        synchronized (this) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Adds a forum to the list.
     *
     * @param theForum the forum to add
     */
    void addForum(final Forum theForum) {

        synchronized (this) {
            this.forums.add(theForum);
            for (final IForumListListener listener : this.listeners) {
                listener.forumAdded(theForum);
            }
        }
    }

    /**
     * Gets an unmodifiable view of the list of forums.
     *
     * @return the view
     */
    public List<Forum> getForums() {

        return this.unmodifiable;
    }

//    /**
//     * Called when the title of a forum has changed.
//     *
//     * @param forum the forum
//     */
//    public void titleChanged(final Forum forum) {
//
//        synchronized (this) {
//            for (final IForumListListener listener : this.listeners) {
//                listener.titleChanged(forum);
//            }
//        }
//    }

    /**
     * Called when a post is added to a forum.
     *
     * @param thePost the post that was added
     */
    void postAdded(final ForumPost thePost) {

        synchronized (this) {
            for (final IForumListListener listener : this.listeners) {
                listener.postAdded(thePost);
            }
        }
    }

    /**
     * Called when the content of a post has been updated.
     *
     * @param thePost the post whose content changed
     */
    void postContentUpdated(final ForumPost thePost) {

        synchronized (this) {
            for (final IForumListListener listener : this.listeners) {
                listener.postContentUpdated(thePost);
            }
        }
    }

    /**
     * Called when the state and/or when-read timestamp of a post has been updated.
     *
     * @param thePost the post whose state or when-read timestamp changed
     */
    void postUpdated(final ForumPost thePost) {

        synchronized (this) {
            for (final IForumListListener listener : this.listeners) {
                listener.postUpdated(thePost);
            }
        }
    }
}
