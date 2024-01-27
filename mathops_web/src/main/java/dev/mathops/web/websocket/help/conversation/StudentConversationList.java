package dev.mathops.web.websocket.help.conversation;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.commons.parser.json.JSONParser;
import dev.mathops.web.websocket.help.StudentKey;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of conversations associated with a single student. This is loaded from the database on startup. Posts should
 * not be updated except by this package, so there is no need to re-query after the initial load.
 *
 * <p>
 * All changes to the data in this class should be synchronized on the owning {@code AllConversations} object.
 */
public final class StudentConversationList {

    /** The owning conversation container. */
    final ConversationsContainer owner;

    /** The student key. */
    final StudentKey studentKey;

    /** List of loaded conversations for the student. */
    private final List<Conversation> conversations;

    /** Listeners registered to receive updates of the conversation list. */
    private final List<IStudentConversationListListener> listeners;

    /** The greatest conversation number in the list. */
    private int greatestConversationNumber;

    /**
     * Constructs a new {@code StudentConversationList}.
     *
     * @param theOwner      the owning conversation container
     * @param theStudentKey the student key
     */
    StudentConversationList(final ConversationsContainer theOwner, final StudentKey theStudentKey) {

        if (theOwner == null || theStudentKey == null) {
            throw new IllegalArgumentException("Owner and student key may not be null");
        }

        this.owner = theOwner;
        this.studentKey = theStudentKey;
        this.listeners = new ArrayList<>(20);
        this.conversations = new ArrayList<>(20);
        this.greatestConversationNumber = 0;
    }

    /**
     * Attempts to parse a student conversation list from its JSON representation.
     *
     * <pre>
     * {
     *   "studentId": "888888888",
     *   "firstName": "First name",
     *   "lastName": "Last name"
     * }
     * </pre>
     *
     * @param owner     the conversation list that will own the parsed conversation
     * @param studentId the student ID (based on the directory containing the metadata)
     * @param json      the JSON
     * @return the parsed conversation list
     * @throws ParsingException if parsing failed
     */
    static StudentConversationList parse(final ConversationsContainer owner, final String studentId,
                                         final String json) throws ParsingException {

        final StudentConversationList result;

        final Object obj = JSONParser.parseJSON(json);

        if (obj instanceof final JSONObject jobj) {
            final Object prop = jobj.getProperty("conversation");
            if (prop instanceof JSONObject) {

                final Object stu = ((JSONObject) prop).getProperty("studentId");
                final Object first = ((JSONObject) prop).getProperty("firstName");
                final Object last = ((JSONObject) prop).getProperty("lastName");
                final Object screen = ((JSONObject) prop).getProperty("screenName");

                if (stu instanceof String && first instanceof String && last instanceof String) {
                    if (stu.equals(studentId)) {
                        final StudentKey key = new StudentKey(studentId, (String) first, (String) last,
                                (String) screen);
                        result = new StudentConversationList(owner, key);
                    } else {
                        throw new ParsingException(0, json.length(),
                                "Metadata has student id " + stu + " but directory name is " + studentId);
                    }
                } else {
                    throw new ParsingException(0, json.length(), "Parsed object was missing a required property");
                }
            } else {
                throw new ParsingException(0, json.length(),
                        "Parsed object's 'conversation' property was not JSONObject");
            }
        } else {
            throw new ParsingException(0, json.length(), "Parsed object was not JSONObject");
        }

        return result;
    }

    /**
     * Gets the number of conversations in the list.
     *
     * @return the number of conversations
     */
    int getNumConversations() {

        synchronized (this.owner) {
            return this.conversations.size();
        }
    }

    /**
     * Gets a specified conversation from the list
     *
     * @param index the index of the conversation to retrieve
     * @return the conversation
     */
    Conversation getConversation(final int index) {

        synchronized (this.owner) {
            return this.conversations.get(index);
        }
    }

    /**
     * Finds the conversation with a specified number.
     *
     * @param number the conversation number
     * @return the matching conversation; {@code null} if none found
     */
    Conversation getConversationByNumber(final int number) {

        Conversation result = null;

        synchronized (this.owner) {
            for (final Conversation test : this.conversations) {
                if (test.conversationNumber == number) {
                    result = test;
                    break;
                }
            }
        }

        return result;
    }

//    /**
//     * Registers a listener to be notified of changes to the conversation list. This triggers sending of the current
//     * state of the conversation list to the listener.
//     *
//     * @param listener the listener to add
//     */
//    public void addListener(final IStudentConversationListListener listener) {
//
//        synchronized (this.owner) {
//            this.listeners.add(listener);
//        }
//    }

//    /**
//     * Removes a listener that was previously registered with {@code addListener}.
//     *
//     * @param listener the listener to remove
//     */
//    public void removeListener(final IStudentConversationListListener listener) {
//
//        synchronized (this.owner) {
//            this.listeners.remove(listener);
//        }
//    }

    /**
     * Ads a conversation to the list without notifying listeners; only intended for use by
     * {@code ConversationDatabase}.
     *
     * @param conv the conversation to add
     */
    void loadConversation(final Conversation conv) {

        synchronized (this.owner) {
            this.conversations.add(conv);

            this.greatestConversationNumber =
                    Math.max(this.greatestConversationNumber, conv.conversationNumber);
        }
    }

    /**
     * Creates a new conversation that has this list as its owner. This allocates a new conversation number for the
     * generated conversation, but does not add the conversation to the list or write it to the database.
     *
     * <p>
     * The process for creating and storing a new conversation is:
     * <ul>
     * <li>Enter a block synchronized on the owning {@code AllConversations} container.
     * <li>Call this method to create the conversation object
     * <li>Call {@code ConversationDatabase.writeConversation} to store its metadata
     * <li>If successful, call {@code addConversation} to add it to this list
     * <li>Leave the synchronized block.
     * </ul>
     *
     * @param theSubject the subject line
     * @return the constructed {@code Conversation}
     */
    Conversation createConversation(final String theSubject) {

        synchronized (this.owner) {
            ++this.greatestConversationNumber;

            return new Conversation(this, this.greatestConversationNumber, theSubject);
        }
    }

//    /**
//     * Adds a conversation to the list, then notifies listeners of the change.
//     *
//     * <p>
//     * This should only be called after the conversation has been successfully stored using
//     * {@code ConversationDatabase.writeConversation}.
//     *
//     * @param theConversation the conversation
//     */
//    public void addConversation(final Conversation theConversation) {
//
//        synchronized (this.owner) {
//            this.conversations.add(theConversation);
//
//            for (final IStudentConversationListListener listener : this.listeners) {
//                listener.conversationAdded(theConversation);
//            }
//        }
//    }

    /**
     * Generates the JSON metadata for this conversation list.
     *
     * <pre>
     * {
     *   "studentId": "888888888",
     *   "firstName": "First name",
     *   "lastName": "Last name"
     * }
     * </pre>
     *
     * @return the JSON metadata
     */
    String toMetadataJson() {

        final HtmlBuilder json = new HtmlBuilder(100);

        json.addln('{');
        json.addln("  \"studentId\": \"", this.studentKey.studentId.replace(CoreConstants.QUOTE, "\\\""), "\",");
        json.addln("  \"firstName\": \"", this.studentKey.firstName.replace(CoreConstants.QUOTE, "\\\""), "\",");
        json.addln("  \"lastName\": \"", this.studentKey.lastName.replace(CoreConstants.QUOTE, "\\\""),
                CoreConstants.QUOTE);
        json.addln('}');

        return json.toString();
    }

    /**
     * Called by a {@code Conversation} object when the number of messages in each tracked state changes.
     *
     * @param conv the conversation whose tracked message count has changed
     */
    void conversationChanged(final Conversation conv) {

        synchronized (this.owner) {
            for (final IStudentConversationListListener listener : this.listeners) {
                listener.conversationChanged(conv);
            }
        }
    }

//    /**
//     * Called by a {@code Conversation} object when its subject has been successfully changed (and the change written to
//     * the database).
//     *
//     * @param conv the conversation whose subject has changed
//     */
//    void subjectChanged(final Conversation conv) {
//
//        synchronized (this.owner) {
//            for (final IStudentConversationListListener listener : this.listeners) {
//                listener.subjectChanged(conv);
//            }
//        }
//    }

    /**
     * Called by a {@code Conversation} object when a new message has been successfully added (and the change written to
     * the database).
     *
     * @param msg the message that was added
     */
    void messageAdded(final ConversationMessage msg) {

        synchronized (this.owner) {
            for (final IStudentConversationListListener listener : this.listeners) {
                listener.messageAdded(msg);
            }
        }
    }

    /**
     * Called by a {@code ConversationMessage} object when the state of the message has been successfully updated (and
     * the change written to the database).
     *
     * @param msg the message that was updated
     */
    void messageStateUpdated(final ConversationMessage msg) {

        synchronized (this.owner) {
            for (final IStudentConversationListListener listener : this.listeners) {
                listener.messageStateUpdated(msg);
            }
        }
    }

    /**
     * Called by a {@code ConversationMessage} object when the when-read time of the message has been successfully
     * updated (and the change written to the database).
     *
     * @param msg the message that was updated
     */
    void messageWhenReadUpdated(final ConversationMessage msg) {

        synchronized (this.owner) {
            for (final IStudentConversationListListener listener : this.listeners) {
                listener.messageWhenReadUpdated(msg);
            }
        }
    }
}
