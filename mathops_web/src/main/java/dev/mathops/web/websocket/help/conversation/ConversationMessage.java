package dev.mathops.web.websocket.help.conversation;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.commons.parser.json.JSONParser;
import dev.mathops.web.websocket.help.HelpManager;
import dev.mathops.web.websocket.help.StudentKey;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * A conversation message.
 *
 * <p>
 * All changes to the message data MUST be synchronized on the conversation list that owns the conversation, and MUST
 * notify the owning conversation of the change.
 */
public final class ConversationMessage implements Comparable<ConversationMessage> {

    /** The conversation to which the post belongs. */
    public final Conversation conversation;

    /** The fixed message number within that conversation, monotonically increasing over time. */
    final int messageNumber;

    /** The date/time when the message was created. */
    final LocalDateTime whenCreated;

    /** The author */
    public final StudentKey author;

    /** The message state. */
    private EMessageState state;

    /** The date/time when the message was read. */
    private LocalDateTime whenRead;

    /** The message content - null until lazily loaded. */
    private String content;

    /**
     * Constructs a new {@code ConversationMessage}.
     *
     * @param theConversation  the conversation to which this message belongs
     * @param theMessageNumber the message number within the conversation
     * @param theWhenCreated   the date/time when the message was created
     * @param theAuthor        the ID of user who posted message
     * @param theState         the initial message state
     * @param theContent       the initial message content
     */
    ConversationMessage(final Conversation theConversation, final int theMessageNumber,
                        final LocalDateTime theWhenCreated, final StudentKey theAuthor,
                        final EMessageState theState, final String theContent) {

        if (theConversation == null || theWhenCreated == null || theAuthor == null) {
            throw new IllegalArgumentException("Invalid arguments when creating conversation message");
        }

        this.conversation = theConversation;
        this.messageNumber = theMessageNumber;
        this.whenCreated = theWhenCreated;
        this.author = theAuthor;
        this.state = theState;
        this.content = theContent;
    }

    /**
     * Attempts to parse a conversation message from its JSON representation.
     *
     * @param owner         the conversation that will own the parsed message
     * @param messageNumber the message number
     * @param json          the JSON
     * @return the parsed conversation
     * @throws ParsingException if parsing failed
     */
    public static ConversationMessage parse(final Conversation owner, final int messageNumber,
                                            final String json) throws ParsingException {

        final ConversationMessage result;

        final Object obj = JSONParser.parseJSON(json);

        if (obj instanceof final JSONObject jobj) {

            final Object prop = jobj.getProperty("message");
            if (prop instanceof final JSONObject jprop) {
                final Object create = jprop.getProperty("whenCreated");

                if (create instanceof final String createStr) {
                    final Object stuId = jprop.getProperty("byStudentId");

                    if (stuId instanceof String) {
                        final Object firstName = jprop.getProperty("byFirstName");
                        final Object lastName = jprop.getProperty("byLastName");
                        final Object screenName = jprop.getProperty("byScreenName");

                        if (firstName instanceof String && lastName instanceof String && screenName instanceof String) {
                            final Object state = jprop.getProperty("state");
                            if (state instanceof String) {
                                try {
                                    final StudentKey author = new StudentKey((String) stuId,
                                            (String) firstName, (String) lastName, (String) screenName);

                                    result = new ConversationMessage(owner, messageNumber,
                                            LocalDateTime.parse(createStr), author,
                                            EMessageState.forCode((String) state), null);

                                    final Object read = jprop.getProperty("whenRead");
                                    if (read instanceof final String readStr) {
                                        try {
                                            result.setWhenRead(LocalDateTime.parse(readStr));
                                        } catch (final DateTimeParseException ex) {
                                            throw new ParsingException(0, json.length(),
                                                    "Failed to parse 'whenRead' property", ex);
                                        }
                                    } else {
                                        throw new ParsingException(0, json.length(),
                                                "Parsed object 'whenRead' property was not String");
                                    }
                                } catch (final DateTimeParseException ex) {
                                    throw new ParsingException(0, json.length(),
                                            "Failed to parse 'whenCreated' property", ex);
                                }
                            } else {
                                throw new ParsingException(0, json.length(),
                                        "Parsed object 'state' property was not String");
                            }
                        } else {
                            throw new ParsingException(0, json.length(),
                                    "Parsed object's 'byFirstName', 'byLastName', "
                                            + "'byScreenName' property was not String");
                        }
                    } else {
                        throw new ParsingException(0, json.length(),
                                "Parsed object's 'byStudentId' property was not String");
                    }
                } else {
                    throw new ParsingException(0, json.length(),
                            "Parsed object's 'whenCreated' property was not String");
                }
            } else {
                throw new ParsingException(0, json.length(),
                        "Parsed object's 'message' property was not JSONObject");
            }
        } else {
            throw new ParsingException(0, json.length(), "Parsed object was not JSONObject");
        }

        return result;
    }

    /**
     * Gets the message state.
     *
     * @return the state
     */
    public EMessageState getState() {

        synchronized (this.conversation.owner.owner) {
            return this.state;
        }
    }

    /**
     * Sets the message state.
     *
     * @param theState the new state
     * @return true if update succeeded; false if not
     */
    public boolean setState(final EMessageState theState) {

        if (theState == null) {
            throw new IllegalArgumentException("Message state may not be null");
        }

        final boolean ok;

        synchronized (this.conversation.owner.owner) {
            final EMessageState oldState = this.state;
            this.state = theState;
            ok = this.conversation.updateState(this, oldState);
            if (!ok) {
                this.state = oldState;
            }
        }

        return ok;
    }

    /**
     * Gets the (server-local) date/time when the post was first read.
     *
     * @return the read date/time (null if not yet read)
     */
    LocalDateTime getWhenRead() {

        synchronized (this.conversation.owner.owner) {
            return this.whenRead;
        }
    }

    /**
     * Sets the (server-local) date/time when the message was first read.
     *
     * @param theWhenRead the new read date/time
     * @return true if update succeeded; false if not
     */
    boolean setWhenRead(final LocalDateTime theWhenRead) {

        final boolean ok;

        synchronized (this.conversation.owner.owner) {
            final LocalDateTime oldWhenRead = this.whenRead;
            this.whenRead = theWhenRead;
            ok = this.conversation.updateWhenRead(this);
            if (!ok) {
                this.whenRead = oldWhenRead;
            }
        }

        return ok;
    }

    /**
     * Gets the message content.
     *
     * @return the content
     */
    public String getContent() {

        synchronized (this.conversation.owner.owner) {
            if (this.content == null) {
                HelpManager.getInstance().conversationDatabase.loadMessageContent(this);
            }

            return this.content;
        }
    }

    /**
     * Loads the message content. Intended for use only by {@code ConversationDatabase}.
     *
     * @param theContent the new content
     */
    void loadContent(final String theContent) {

        synchronized (this.conversation.owner.owner) {
            this.content = theContent;
        }
    }

    /**
     * Generates the JSON metadata for this message.
     *
     * <pre>
     * {"message": {
     *   "whenCreated": "ISO-8601 date/time string",
     *   "byStudentId": "student ID",
     *   "byStudentName": "screen name",
     *   "state": "state code",
     *   "whenRead": "ISO-8601 date/time string, absent if null"
     * }}
     * </pre>
     *
     * @return the JSON metadata
     */
    String toMetadataJson() {

        final HtmlBuilder json = new HtmlBuilder(100);

        json.addln("{\"message\": {");
        json.addln("  \"whenCreated\": \"", this.whenCreated.toString(), "\",");
        json.addln("  \"byStudentId\": \"", this.author.studentId, "\",");
        json.addln("  \"byStudentName\": \"", this.author.screenName.replace(CoreConstants.QUOTE, "\\\""), "\",");
        json.add("  \"state\": \"", this.state.code, CoreConstants.QUOTE);
        if (this.whenRead == null) {
            json.addln();
        } else {
            json.addln(",").addln("  \"whenRead\": \"", this.whenRead.toString(), CoreConstants.QUOTE);
        }
        json.addln("}}");

        return json.toString();
    }

    /**
     * Compares two conversation messages for order. Conversation messages are ordered by message number.
     *
     * @return -1, 0, or 1 as this object is less than, equal to, or greater than {@code o}
     */
    @Override
    public int compareTo(final ConversationMessage o) {

        return Integer.compare(this.messageNumber, o.messageNumber);
    }
}
