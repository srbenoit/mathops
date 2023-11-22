package dev.mathops.web.websocket.help.conversation;

import dev.mathops.web.websocket.help.StudentKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A container for all conversations, organized by student. This is loaded from the database on startup. Posts should
 * not be updated except by this package, so there is no need to re-query after the initial load.
 */
public final class ConversationsContainer {

    /** Listeners registered to receive updates of the conversation container. */
    private final List<IConversationsContainerListener> listeners;

    /** Map from student key to the list of loaded conversations for that student. */
    private final Map<StudentKey, StudentConversationList> conversations;

    /**
     * Constructs a new {@code ConversationsContainer}.
     */
    ConversationsContainer() {

        this.listeners = new ArrayList<>(4);
        this.conversations = new TreeMap<>();
    }

    /**
     * Registers a listener to be notified when student conversation lists are added or removed.
     *
     * @param listener the listener to add
     */
    /* default */ void addListener(final IConversationsContainerListener listener) {

        synchronized (this) {
            this.listeners.add(listener);
        }
    }

    /**
     * Removes a listener that was previously registered with {@code addListener}.
     *
     * @param listener the listener to remove
     */
    /* default */ void removeListener(final IConversationsContainerListener listener) {

        synchronized (this) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Retrieves the set of student keys for which conversation lists exist.
     *
     * @return the sorted set of student keys
     */
    List<StudentKey> getStudentKeys() {

        synchronized (this) {
            return new ArrayList<>(this.conversations.keySet());
        }
    }

    /**
     * Searches for a student key with a specified student ID.
     *
     * @param studentId the student ID
     * @return the student key, if found; {@code null} if not
     */
    /* default */ StudentKey getStudentKey(final String studentId) {

        StudentKey result = null;

        synchronized (this) {
            for (final StudentKey test : this.conversations.keySet()) {
                if (test.studentId.equals(studentId)) {
                    result = test;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Gets the conversation list for a single student.
     *
     * @param key the student key
     * @return the list; {@code null} if none found
     */
    /* default */ StudentConversationList getStudentConversationList(final StudentKey key) {

        synchronized (this) {
            return this.conversations.get(key);
        }
    }

    /**
     * Ads a student conversation list without notifying listeners - only intended for use by
     * {@code ConversationDatabase}.
     *
     * @param theList the student conversation list to add
     */
    void loadStudentConversationList(final StudentConversationList theList) {

        synchronized (this) {
            this.conversations.put(theList.studentKey, theList);
        }
    }

    /**
     * Adds a student conversation list and notifies listeners of the change.
     *
     * <p>
     * This should only be called after the list has been successfully stored using
     * {@code ConversationDatabase.writeStudentMessageList}.
     *
     * @param theList the student conversation list to add
     */
    /* default */ void addStudentConversationList(final StudentConversationList theList) {

        synchronized (this) {
            this.conversations.put(theList.studentKey, theList);

            for (final IConversationsContainerListener listener : this.listeners) {
                listener.studentConversationListAdded(theList);
            }
        }
    }
}
