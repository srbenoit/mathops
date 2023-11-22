package dev.mathops.web.websocket.help.conversation;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.json.JSONObject;
import dev.mathops.core.parser.json.JSONParser;
import dev.mathops.web.websocket.help.HelpManager;
import dev.mathops.web.websocket.help.StudentKey;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * State for an individual conversation.
 *
 * <p>
 * A conversation is associated with a single student, but may involve several learning assistants. It consists of a
 * sequence of messages, each of which is from either the student or a course assistant.
 *
 * <p>
 * A student may have several conversations, and conversations remain active throughout the current semester. They are
 * reset (but archived) as each semester starts, and learning assistants may be able to view conversations for a student
 * from past semesters.
 *
 * <p>
 * Any participant can edit or delete their own messages. If a message is deleted after being responded to, a "deleted
 * message" placeholder will remain in its place.
 */
public final class Conversation implements Comparable<Conversation> {

    /** The owning conversations list. */
    final StudentConversationList owner;

    /** The conversation number (unique within student's conversations). */
    final int conversationNumber;

    /** The subject line of the conversation (subject to change). */
    private final String subject;

    /** The messages. */
    private final List<ConversationMessage> messages;

    /** The last message number used. */
    private int lastMessageNumber;

    /** The number of messages not in the "deleted" state. */
    private int numUndeleted;

    /** The number of messages by learning assistants not yet read by student. */
    private int numUnreadByStudent;

    /** The number of messages by the student unread by staff. */
    private int numUnreadByStaff;

    /**
     * Constructs a new {@code Conversation}.
     *
     * @param theOwner           the owning conversation list
     * @param theConversationNbr the conversation number
     * @param theSubject         the initial subject
     */
    Conversation(final StudentConversationList theOwner, final int theConversationNbr, final String theSubject) {

        if (theOwner == null) {
            throw new IllegalArgumentException("Owner may not be null");
        }

        this.owner = theOwner;
        this.conversationNumber = theConversationNbr;
        this.subject = theSubject == null ? CoreConstants.EMPTY : theSubject;
        this.messages = new ArrayList<>(10);
    }

    /**
     * Attempts to parse a conversation from its JSON representation.
     *
     * @param owner              the conversation list that will own the parsed conversation
     * @param conversationNumber the conversation number
     * @param json               the JSON
     * @return the parsed conversation
     * @throws ParsingException if parsing failed
     */
    static Conversation parse(final StudentConversationList owner, final int conversationNumber,
                              final String json) throws ParsingException {

        final Conversation result;

        final Object obj = JSONParser.parseJSON(json);

        if (obj instanceof final JSONObject jobj) {

            final Object prop = jobj.getProperty("conversation");
            if (prop instanceof JSONObject) {

                final Object sub = ((JSONObject) prop).getProperty("subject");

                if (sub instanceof String) {
                    result = new Conversation(owner, conversationNumber, (String) sub);
                } else {
                    throw new ParsingException(0, json.length(), "Parsed object was missing required property");
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
     * Gets the subject.
     *
     * @return the subject
     */
    public String getSubject() {

        synchronized (this.owner.owner) {
            return this.subject;
        }
    }

//    /**
//     * Attempts to update the subject of the conversation. This call does the following:
//     *
//     * <ul>
//     * <li>Tests whether the subject is actually changing. If not, stop here.
//     * <li>Updates the subject field in this conversation.
//     * <li>Asks the conversation database to write the conversation metadata.
//     * <li>If write was successful, notifies the owning list of the change, returns true
//     * <li>If write was not successful, restores the old subject, returns false
//     * </ul>
//     *
//     * @param theSubject the new subject
//     * @return true if successful; false if not
//     */
//     public boolean setSubject(final String theSubject) {
//
//     boolean ok = false;
//
//     synchronized (this.owner.owner) {
//     final String actual = theSubject == null ? CoreConstants.EMPTY : theSubject;
//
//     if (!actual.equals(this.subject)) {
//     final String old = this.subject;
//     this.subject = actual;
//
//     if (HelpManager.getInstance().conversationDatabase.writeConversation(this)) {
//     this.owner.subjectChanged(this);
//     ok = true;
//     } else {
//     this.subject = old;
//     ok = false;
//     }
//     }
//     }
//
//     return ok;
//     }

    /**
     * Gets the total number of messages (including deleted messages).
     *
     * @return the total number of messages
     */
    int getTotalMessages() {

        synchronized (this.owner.owner) {
            return this.messages.size();
        }
    }

    /**
     * Retrieves a specific message from a conversation.
     *
     * @param index the index of the message
     * @return the message
     */
    ConversationMessage getMessage(final int index) {

        synchronized (this.owner.owner) {
            return this.messages.get(index);
        }
    }

    /**
     * Finds the message with a specified number.
     *
     * @param number the message number
     * @return the matching message; {@code null} if none found
     */
    ConversationMessage getMessageByNumber(final int number) {

        ConversationMessage result = null;

        synchronized (this.owner) {
            for (final ConversationMessage test : this.messages) {
                if (test.messageNumber == number) {
                    result = test;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Gets the number of undeleted messages.
     *
     * @return the number of undeleted messages
     */
    int getNumUndeleted() {

        synchronized (this.owner.owner) {
            return this.numUndeleted;
        }
    }

    /**
     * Gets the number of messages posted by staff not yet read by student.
     *
     * @return the number of unread messages
     */
    int getNumUnreadByStudent() {

        synchronized (this.owner.owner) {
            return this.numUnreadByStudent;
        }
    }

    /**
     * Gets the number of messages posted by students not yet read by staff.
     *
     * @return the number of unread messages
     */
    int getNumUnreadByStaff() {

        synchronized (this.owner.owner) {
            return this.numUnreadByStaff;
        }
    }

    /**
     * Ads a message to the conversation without notifying listeners; only intended for use by
     * {@code ConversationDatabase}
     *
     * @param msg the conversation to add
     */
    void loadMessage(final ConversationMessage msg) {

        final EMessageState state = msg.getState();

        synchronized (this.owner.owner) {
            this.messages.add(msg);

            if (state == EMessageState.UNREAD_BY_STUDENT) {
                ++this.numUnreadByStudent;
            } else if (state == EMessageState.UNREAD_BY_STAFF) {
                ++this.numUnreadByStaff;
            }
            if (state != EMessageState.DELETED) {
                ++this.numUndeleted;
            }
        }
    }

    /**
     * Creates a new message that has this conversation as its owner. This method does the following (all within a block
     * synchronized on the owning container
     *
     * <ul>
     * <li>Allocates a new message number for the message.
     * <li>Creates the message object.
     * <li>Calls {@code ConversationDatabase.writeMessage} to attempt to write the message to
     * the database
     * <li>If write was successful, the message is added to the conversation, the owning list is
     * notified of the change, and true is returned
     * <li>If write was not successful, the message is discarded, the allocated message number is
     * released, and false is returned
     * </ul>
     *
     * @param theAuthor  the author
     * @param theState   the initial state of the message
     * @param theContent the initial content of the message
     * @return the constructed {@code ConversationMessage} on success; {@code null} on failure
     */
    ConversationMessage addMessage(final StudentKey theAuthor, final EMessageState theState, final String theContent) {

        ConversationMessage msg;

        synchronized (this.owner.owner) {
            ++this.lastMessageNumber;

            msg = new ConversationMessage(this, this.lastMessageNumber, LocalDateTime.now(), theAuthor, theState,
                    theContent);

            if (HelpManager.getInstance().conversationDatabase.writeMessage(msg)) {

                this.messages.add(msg);

                final EMessageState state = msg.getState();

                if (state == EMessageState.UNREAD_BY_STUDENT) {
                    ++this.numUnreadByStudent;
                }
                if (state == EMessageState.UNREAD_BY_STAFF) {
                    ++this.numUnreadByStudent;
                }
                if (state != EMessageState.DELETED) {
                    ++this.numUndeleted;
                }

                this.owner.messageAdded(msg);
            } else {
                --this.lastMessageNumber;
                msg = null;
            }

        }

        return msg;
    }

    /**
     * Called when the state of a message has been changed. Attempts to write the state to the database. If successful
     * the message counts are updated based on the old and new states, and the owning list is notified of the change.
     *
     * @param theMessage the message whose state changed
     * @param oldState   the old state
     * @return true if update succeeded; false otherwise
     */
    boolean updateState(final ConversationMessage theMessage, final EMessageState oldState) {

        final EMessageState newState = theMessage.getState();

        final boolean ok;

        synchronized (this.owner.owner) {
            ok = HelpManager.getInstance().conversationDatabase.writeMessageMetadata(theMessage);

            if (ok) {
                boolean delta = false;

                if (oldState == EMessageState.UNREAD_BY_STUDENT && newState != EMessageState.UNREAD_BY_STUDENT) {
                    --this.numUnreadByStudent;
                    delta = true;
                } else if (oldState != EMessageState.UNREAD_BY_STUDENT && newState == EMessageState.UNREAD_BY_STUDENT) {
                    ++this.numUnreadByStudent;
                    delta = true;
                }

                if (oldState == EMessageState.UNREAD_BY_STAFF && newState != EMessageState.UNREAD_BY_STAFF) {
                    --this.numUnreadByStaff;
                    delta = true;
                } else if (oldState != EMessageState.UNREAD_BY_STAFF && newState == EMessageState.UNREAD_BY_STAFF) {
                    ++this.numUnreadByStaff;
                    delta = true;
                }

                if (oldState == EMessageState.DELETED && newState != EMessageState.DELETED) {
                    ++this.numUndeleted;
                    delta = true;
                } else if (oldState != EMessageState.DELETED && newState == EMessageState.DELETED) {
                    --this.numUndeleted;
                    delta = true;
                }

                this.owner.messageStateUpdated(theMessage);

                if (delta) {
                    // The conversation-related data that gets sent to clients has changed, so
                    // notify listeners, so they can send
                    this.owner.conversationChanged(this);
                }
            }
        }

        return ok;
    }

    /**
     * Called when the when-read date/time of a message has been changed. Attempts to write the state to the database.
     * If successful, the owning list is notified of the change.
     *
     * @param theMessage the message whose when-read timestamp changed
     * @return true if update succeeded; false otherwise
     */
    boolean updateWhenRead(final ConversationMessage theMessage) {

        synchronized (this.owner.owner) {
            final boolean ok = HelpManager.getInstance().conversationDatabase.writeMessageMetadata(theMessage);

            if (ok) {
                this.owner.messageWhenReadUpdated(theMessage);
            }

            return ok;
        }
    }

    /**
     * Generates the JSON metadata for this conversation.
     *
     * <pre>
     * {
     *   "subject": "The Subject Line, with quotes escaped"
     * }
     * </pre>
     *
     * @return the JSON metadata
     */
    String toMetadataJson() {

        final HtmlBuilder json = new HtmlBuilder(100);

        synchronized (this.owner.owner) {
            json.addln('{');
            json.addln("  \"subject\": \"", this.subject.replace(CoreConstants.QUOTE, "\\\""), CoreConstants.QUOTE);
            json.addln('}');
        }

        return json.toString();
    }

    /**
     * Compares two conversations for order. Conversations are ordered first by student name (last then first), then by
     * conversation number.
     *
     * @return -1, 0, or 1 as this object is less than, equal to, or greater than {@code o}
     */
    @Override
    public int compareTo(final Conversation o) {

        synchronized (this.owner.owner) {
            int result = this.owner.studentKey.compareTo(o.owner.studentKey);

            if (result == 0) {
                result = Integer.compare(this.conversationNumber, o.conversationNumber);
            }

            return result;
        }
    }
}
