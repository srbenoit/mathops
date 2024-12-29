package dev.mathops.web.websocket.help.conversation;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;
import dev.mathops.web.websocket.help.HelpManager;
import dev.mathops.web.websocket.help.StudentKey;
import oracle.jdbc.proxy.annotation.OnError;

import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * WebSocket service to provide help conversations.
 *
 * <p>
 * One instance of this class is created per client connection.
 *
 * <p>
 * On new connection, the session is "unauthorized". The first message the client should send on opening the connection
 * is a string with "Session:[session-id-from-cookie]". Once that is received, the session will to go the "authorized"
 * state, and the {@code ImmutableSessionInfo} object will be stored.
 *
 * <p>
 * On an initial connection (on receipt of a valid "Session:..." message), the entire conversation list is sent in JSON
 * format (but not the metadata on individual messages). Also, any changes to the conversation list, like the addition
 * of new conversations, triggers sends of the update to the client.
 *
 * <p>
 * If the client sends a "Student:[student-id]" message, that indicates a desire to view the conversations for a
 * student, and triggers the sending of all message metadata for the student.
 *
 * <p>
 * If the client sends a "Message:[student-id].[message-number]" message, that indicates a desire to view the metadata
 * and content of a message, which is then sent.
 *
 * <p>
 * The client can post a new message to a conversation by sending a "NewMessage" message.
 *
 * <p>
 * The client can post an update to a message by sending a "UpdateMessage" message.
 */
@ServerEndpoint("/ws/helpconversations")
public final class ConversationWebSocket
        implements IConversationsContainerListener, IStudentConversationListListener {

    /** The web socket session. */
    private Session session;

    /** The session (null until a session message is received and verified). */
    private ImmutableSessionInfo loginSession;

    /** The list of student keys for which the client has expressed interest in details. */
    private final Set<StudentKey> studentKeys;

    /** The list of conversations for which the client has expressed interest in details. */
    private final Set<Conversation> conversations;

    /**
     * Constructs a new {@code ConversationWebSocket}.
     */
    public ConversationWebSocket() {

        this.studentKeys = new HashSet<>(10);
        this.conversations = new HashSet<>(10);
    }

    /**
     * Called when the socket is opened.
     *
     * @param theSession the session
     * @param conf       the endpoint configuration
     */
    @OnOpen
    public void open(final Session theSession, final EndpointConfig conf) {

        Log.info("Help Conversations websocket opened");

        this.session = theSession;
    }

    /**
     * Called when a message arrives.
     *
     * @param message the message
     */
    @OnMessage
    public void incoming(final String message) {

        Log.info("Help Conversations websocket received message: ", message);

        if (message.startsWith("Session:")) {
            processSessionMessage(message);
        } else if (this.loginSession != null) {

            if (message.startsWith("OpenStudent:")) {
                processOpenStudent(message);
            } else if (message.startsWith("CloseStudent:")) {
                processCloseStudent(message);
            } else if (message.startsWith("OpenConv:")) {
                processOpenConv(message);
            } else if (message.startsWith("CloseConv:")) {
                processCloseConv(message);
            } else if (message.startsWith("GetMessage:")) {
                processGetMessage(message);
            } else if (message.startsWith("PostMessage:")) {
                processPostMessage(message);
            } else if (message.startsWith("UpdMessage:")) {
                processUpdMessage(message);
            } else {
                Log.warning(//
                        "Unrecognized message received on Help Conversations websocket: "
                                + message);
            }
        } else {
            Log.warning("Invalid message received on Help Conversations websocket: "
                    + message);
        }
    }

    /**
     * Called when there is an error on the connection.
     *
     * @param t the error
     */
    @OnError
    public void onError(final Throwable t) {

        Log.warning("Help Conversations web socket error", t);

        HelpManager.getInstance().conversations.removeListener(this);

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

        Log.info("Help Conversations websocket closed");

        HelpManager.getInstance().conversations.removeListener(this);
        this.session = null;
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
     * Receipt of this message triggers the sending of the current conversation state, and registers the connection to
     * receive future updates to conversation list state.
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

                Log.info("Help Conversations connection from ",
                        this.loginSession.getEffectiveUserId(), " (",
                        this.loginSession.getEffectiveScreenName(), ")");

                HelpManager.getInstance().conversations.addListener(this);
                sendCompleteConversationsList();
            } else {
                final String msg = "SessionError:Not Authorized";
                Log.warning(msg);
                send(msg);
            }
        }
    }

    /**
     * Processes a message starting with "OpenStudent:", which should be followed immediately by a student ID.
     *
     * <p>
     * This requests that the server add the student to the list of students of interest.
     *
     * @param message the message
     */
    private void processOpenStudent(final String message) {

        final String stuId = message.substring(12);

        final ConversationsContainer container = HelpManager.getInstance().conversations;
        final StudentKey key = container.getStudentKey(stuId);

        if (key == null) {
            Log.warning("Request to open unknown student: ", stuId);
        } else if (this.studentKeys.contains(key)) {
            Log.warning("Request to open student that is already open: ", stuId);
        } else {
            this.studentKeys.add(key);
            sendStudentConversations(key);
        }
    }

    /**
     * Processes a message starting with "CloseStudent:", which should be followed immediately by a student ID.
     *
     * <p>
     * This requests that the server remove the student from the list of students of interest.
     *
     * @param message the message
     */
    private void processCloseStudent(final String message) {

        final String stuId = message.substring(12);

        final ConversationsContainer container = HelpManager.getInstance().conversations;
        final StudentKey key = container.getStudentKey(stuId);

        if (key == null) {
            Log.warning("Request to close unknown student: ", stuId);
        } else if (!this.studentKeys.remove(key)) {
            Log.warning("Request to close student that was not open: ", stuId);
        }
    }

    /**
     * Processes a message starting with "OpenConv:", which should be followed immediately by a student ID, a '.'
     * character, and a conversation number.
     *
     * <p>
     * This requests that the server add the conversation to the list of conversations of interest.
     *
     * @param message the message
     */
    private void processOpenConv(final String message) {

        final int dot = message.lastIndexOf('.');
        if (dot == -1) {
            Log.warning("Invalid OpenConv request: ", message);
        } else {
            try {
                final int cnum = Integer.parseInt(message.substring(dot + 1));
                final String stu = message.substring(9, dot);

                final ConversationsContainer container = HelpManager.getInstance().conversations;
                final StudentKey key = container.getStudentKey(stu);

                if (key == null) {
                    Log.warning(//
                            "Request to open conversation for unknown student ", stu);
                } else {
                    final StudentConversationList list = container.getStudentConversationList(key);
                    final Conversation conv = list.getConversationByNumber(cnum);

                    if (conv == null) {
                        Log.warning("Request to open unknown conversation "
                                + cnum + " for student ", stu);
                    } else if (this.conversations.contains(conv)) {
                        Log.warning("Request to open conversation that is already ",
                                "open for student ",
                                stu);
                    } else {
                        this.conversations.add(conv);
                        sendConversationMessages(conv);
                    }
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid OpenConv request: ", message, ex);
            }
        }
    }

    /**
     * Processes a message starting with "CloseConv:", which should be followed immediately by a student ID, a "."
     * character, and a conversation number.
     *
     * <p>
     * This requests that the server remove the conversation from the list of conversations of interest.
     *
     * @param message the message
     */
    private void processCloseConv(final String message) {

        final int dot = message.lastIndexOf('.');
        if (dot == -1) {
            Log.warning("Invalid CloseConv request: ", message);
        } else {
            try {
                final int cnum = Integer.parseInt(message.substring(dot + 1));
                final String stu = message.substring(10, dot);

                boolean searching = true;

                final Iterator<Conversation> iter = this.conversations.iterator();
                while (iter.hasNext()) {
                    final Conversation test = iter.next();
                    if (test.conversationNumber == cnum
                            && test.owner.studentKey.studentId.equals(stu)) {
                        iter.remove();
                        searching = false;
                        break;
                    }
                }

                if (searching) {
                    Log.warning("Request to close conversation that is ",
                            "not open for student : ", stu);
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid CloseConv request: ", message, ex);
            }
        }
    }

    /**
     * Processes a message starting with "GetMessage:", which should be followed immediately by a student ID, a "."
     * character, a conversation number, a ":" character, and a message number.
     *
     * <p>
     * This requests that the server send the message (with content) to the client.
     *
     * @param message the message
     */
    private void processGetMessage(final String message) {

        final int dot = message.lastIndexOf('.');
        final int colon = message.lastIndexOf(':');
        if (dot == -1 || colon == -1 || colon < dot) {
            Log.warning("Invalid GetMessage request: ", message);
        } else {
            try {
                final int cnum = Integer.parseInt(message.substring(dot + 1, colon));
                final int mnum = Integer.parseInt(message.substring(colon + 1));
                final String stu = message.substring(9, dot);

                final ConversationsContainer container = HelpManager.getInstance().conversations;
                final StudentKey key = container.getStudentKey(stu);

                if (key == null) {
                    Log.warning(//
                            "Request to open conversation for unknown student ", stu);
                } else {
                    final StudentConversationList list = container.getStudentConversationList(key);
                    final Conversation conv = list.getConversationByNumber(cnum);

                    if (conv == null) {
                        Log.warning("Request to open unknown conversation "
                                + cnum + " for student ", stu);
                    } else {
                        final ConversationMessage msg = conv.getMessageByNumber(mnum);

                        if (msg == null) {
                            Log.warning("Request to open unknown message " + mnum
                                    + " in conversation " + cnum
                                    + " for student ", stu);
                        } else {
                            sendMessage(msg);
                        }
                    }
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid GetMessage request: ", message, ex);
            }
        }
    }

    /**
     * Processes a message starting with "PostMessage:", which should be followed immediately by a JSON message data
     * object of the form below.
     *
     * <pre>
     * {
     *   "studentId": "...",
     *   "firstName": "...",
     *   "lastName": "...",
     *   "convNbr": ##,       &lt;-- Exactly one of convNbr and subject provided
     *   "subject": "...",    &lt;-- (provide subject to start a new conversation)
     *   "authorId": "...",
     *   "authorName": "...",
     *   "state": "[code]",
     *   "content": "..."
     * }
     * </pre>
     *
     * <p>
     * This requests that the server create a new message (within the appropriate student conversation list and
     * conversation, creating those objects as needed). This may result in update messages being sent to the client if
     * the student/conversation are currently "of interest".
     *
     * @param message the message
     */
    private static void processPostMessage(final String message) {

        final String json = message.substring(12);

        try {
            final Object obj = JSONParser.parseJSON(json);

            if (obj instanceof final JSONObject jobj) {

                final Object studentId = jobj.getProperty("studentId");
                final Object firstName = jobj.getProperty("firstName");
                final Object lastName = jobj.getProperty("lastName");
                final Object screenName = jobj.getProperty("screnName");
                final Object convNbr = jobj.getProperty("convNbr");
                final Object subject = jobj.getProperty("subject");
                final Object authorId = jobj.getProperty("authorId");
                final Object authorFirst = jobj.getProperty("authorFirst");
                final Object authorLast = jobj.getProperty("authorLast");
                final Object authorScreen = jobj.getProperty("authorScreen");
                final Object state = jobj.getProperty("state");
                final Object content = jobj.getProperty("content");

                if (studentId instanceof String && firstName instanceof String
                        && lastName instanceof String && (convNbr == null || convNbr instanceof Double)
                        && (subject == null || subject instanceof String) && authorId instanceof String
                        && authorFirst instanceof String && authorLast instanceof String
                        && authorScreen instanceof String && state instanceof String
                        && content instanceof String) {

                    final StudentKey author = new StudentKey((String) authorId,
                            (String) authorFirst, (String) authorLast, (String) authorScreen);

                    processPostMessage((String) studentId, (String) firstName, (String) lastName,
                            (String) screenName, (Double) convNbr, (String) subject, author,
                            (String) state, (String) content);
                } else {
                    Log.warning("PostMessage JSON was massing required property: ", message);
                }
            } else {
                Log.warning("PostMessage JSON was ", obj.getClass().getName(), " rather than JSONObject'");
            }
        } catch (final ParsingException ex) {
            Log.warning("Failed to parse PostMessage JSON '", message, "'", ex);
        }
    }

    /**
     * Processes a decoded "PostMessage:" message.
     *
     * @param studentId  the student id
     * @param firstName  the student first name
     * @param lastName   the student last name
     * @param screenName the student screen name
     * @param convNbr    the conversation number
     * @param subject    the subject
     * @param author     the author
     * @param state      the state
     * @param content    the content (HTML with no external references)
     */
    private static void processPostMessage(final String studentId, final String firstName,
                                           final String lastName, final String screenName, final Double convNbr,
                                           final String subject,
                                           final StudentKey author, final String state, final String content) {

        final HelpManager help = HelpManager.getInstance();

        final ConversationsContainer container = help.conversations;
        StudentKey key = container.getStudentKey(studentId);

        if (key == null) {
            key = new StudentKey(studentId, firstName, lastName, screenName);
        }

        StudentConversationList list = container.getStudentConversationList(key);

        if (list == null) {
            list = new StudentConversationList(container, key);
            if (help.conversationDatabase.writeStudentMessageList(list)) {
                container.addStudentConversationList(list);
            } else {
                list = null;
            }
        }

        if (list != null) {
            Conversation conv = null;

            if (convNbr == null) {
                if (subject == null) {
                    Log.warning("PostMessage with neither conversation ID or subject: ", studentId);
                } else {
                    conv = list.createConversation(subject);
                }
            } else {
                conv = list.getConversationByNumber(convNbr.intValue());
            }

            if (conv == null) {
                Log.warning("Unable to find conversation for PostMessage: ", studentId);
            } else {
                final EMessageState st = EMessageState.forCode(state);
                if (st == null) {
                    Log.warning("Invalid state in PostMessage: ", state);
                } else {
                    conv.addMessage(author, st, content);
                }
            }
        }
    }

    /**
     * Processes a message starting with "UpdMessage:", which should be followed immediately by a JSON message data
     * object of the form below.
     *
     * <pre>
     * {
     *   "studentId": "...",
     *   "convNbr": ##,
     *   "msgNbr": ##,
     *   "state": "[code]",
     *   "whenRead" : "..."
     * }
     * </pre>
     *
     * <p>
     * This requests that the server update the metadata for a message. This may result in update messages being sent to
     * the client if the student/conversation are currently "of interest".
     *
     * @param message the message
     */
    private static void processUpdMessage(final String message) {

        final String json = message.substring(11);

        try {
            final Object obj = JSONParser.parseJSON(json);

            if (obj instanceof final JSONObject jobj) {

                final Object studentId = jobj.getProperty("studentId");
                final Object convNbr = jobj.getProperty("convNbr");
                final Object msgNbr = jobj.getProperty("msgNbr");
                final Object state = jobj.getProperty("state");
                final Object whenRead = jobj.getProperty("whenRead");

                if (studentId instanceof String && convNbr instanceof Double && msgNbr instanceof Double
                        && state instanceof String && (whenRead == null || whenRead instanceof String)) {

                    processUpdMessage((String) studentId, ((Double) convNbr).intValue(),
                            ((Double) msgNbr).intValue(), (String) state, (String) whenRead);
                } else {
                    Log.warning("PostMessage JSON was massing required property: ", message);
                }

            } else {
                Log.warning("UpdMessage JSON was ", obj.getClass().getName(), " rather than JSONObject'");
            }
        } catch (final ParsingException ex) {
            Log.warning("Failed to parse UpdMessage JSON '", message, "'", ex);
        }
    }

    /**
     * Processes a decoded "UpdMessage:" message.
     *
     * @param studentId the student id
     * @param convNbr   the conversation number
     * @param msgNbr    the message number
     * @param state     the state
     * @param whenRead  the date/time the message was read, null if not yet read
     */
    private static void processUpdMessage(final String studentId, final int convNbr,
                                          final int msgNbr, final String state, final String whenRead) {

        final HelpManager help = HelpManager.getInstance();

        final ConversationsContainer container = help.conversations;
        final StudentKey key = container.getStudentKey(studentId);

        if (key == null) {
            Log.warning("Request to update nonexistent message:", studentId, CoreConstants.SLASH,
                    Integer.toString(convNbr), CoreConstants.SLASH, Integer.toString(msgNbr));
        } else {
            final StudentConversationList list = container.getStudentConversationList(key);

            if (list == null) {
                Log.warning("Request to update nonexistent message:", studentId, CoreConstants.SLASH,
                        Integer.toString(convNbr), CoreConstants.SLASH, Integer.toString(msgNbr));
            } else {
                final Conversation conv = list.getConversationByNumber(convNbr);

                if (conv == null) {
                    Log.warning("Request to update nonexistent message:", studentId, CoreConstants.SLASH,
                            Integer.toString(convNbr), CoreConstants.SLASH, Integer.toString(msgNbr));
                } else {
                    final ConversationMessage msg = conv.getMessageByNumber(msgNbr);

                    if (msg == null) {
                        Log.warning("Request to update nonexistent message:", studentId, CoreConstants.SLASH,
                                Integer.toString(convNbr), CoreConstants.SLASH, Integer.toString(msgNbr));
                    } else {
                        final EMessageState st = EMessageState.forCode(state);

                        if (st == null) {
                            Log.warning("Invalid state in update message:", state);
                        } else {
                            try {
                                final LocalDateTime when = whenRead == null ? null : LocalDateTime.parse(whenRead);

                                synchronized (container) {
                                    msg.setState(st);
                                    msg.setWhenRead(when);
                                }
                            } catch (final DateTimeParseException ex) {
                                Log.warning("Invalid when-read date/time in update message:", whenRead, ex);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Attempts to send a message to the remote endpoint. On error, the web socket is closed.
     *
     * @param msg the message
     */
    private void send(final String msg) {

        Log.info("Help Conversations web socket sending: ", msg);

        try {
            this.session.getBasicRemote().sendText(msg);
        } catch (final IOException e) {
            Log.warning("Help Conversations error: Failed to send message to client", e);

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
     * Called when a student conversation list has been added to the container.
     *
     * @param theList the list that was added
     */
    @Override
    public void studentConversationListAdded(final StudentConversationList theList) {

        final HtmlBuilder json = new HtmlBuilder(100);
        json.addln("{\"addStuConvList\": {");

        final ConversationsContainer container = HelpManager.getInstance().conversations;

        synchronized (container) {
            final StudentKey key = theList.studentKey;

            int total = 0;
            int totalUnread = 0;
            final int num = theList.getNumConversations();
            for (int i = 0; i < num; ++i) {
                final Conversation conversation = theList.getConversation(i);
                total += conversation.getNumUndeleted();
                totalUnread += conversation.getNumUnreadByStaff();
            }

            json.addln("{\"studentId\": \"", key.studentId,
                    "\", \"firstName\": \"", key.firstName,
                    "\", \"lastName\": \"", key.lastName,
                    "\", \"numConv\": ", Integer.toString(num),
                    ", \"numUndeleted\": ", Integer.toString(total),
                    ", \"numUnread\": ", Integer.toString(totalUnread),
                    "}");
        }

        json.addln("}}");

        send(json.toString());
    }

    /**
     * Called when a conversation has been added to the list.
     *
     * @param theConversation the conversation that was added
     */
    @Override
    public void conversationAdded(final Conversation theConversation) {

        final StudentKey key = theConversation.owner.studentKey;

        final HtmlBuilder json = new HtmlBuilder(100);

        json.addln("{\"convAdded\": {");
        json.addln("  \"studentId\": \"", key.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"firstName\": \"", key.firstName.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"lastName\": \"", key.lastName.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"convNbr\": ", Integer.toString(theConversation.conversationNumber),
                ", \"subject\": \"", theConversation.getSubject().replace(CoreConstants.QUOTE, "\\\""),
                "\", \"msgCount\": ", Integer.toString(theConversation.getNumUndeleted()),
                ", \"nbrUnreadByStaff\": ", Integer.toString(theConversation.getNumUnreadByStaff()),
                ", \"nbrUnreadByStu\": ", Integer.toString(theConversation.getNumUnreadByStudent()));
        json.addln("}}");

        send(json.toString());
    }

    /**
     * Called when the counts of tracked messages changes in a conversation.
     *
     * @param conversation the conversation
     */
    @Override
    public void conversationChanged(final Conversation conversation) {

        final HtmlBuilder json = new HtmlBuilder(100);
        json.addln("{\"stuConvListUpdated\": {");

        final ConversationsContainer container = HelpManager.getInstance().conversations;
        final StudentConversationList list = conversation.owner;

        synchronized (container) {
            final StudentKey key = list.studentKey;

            int total = 0;
            int totalUnread = 0;
            final int num = list.getNumConversations();
            for (int i = 0; i < num; ++i) {
                final Conversation test = list.getConversation(i);
                total += test.getNumUndeleted();
                totalUnread += test.getNumUnreadByStaff();
            }

            json.addln("{\"studentId\": \"", key.studentId,
                    "\", \"numConv\": ", Integer.toString(num),
                    ", \"numUndeleted\": ", Integer.toString(total),
                    ", \"numUnread\": ", Integer.toString(totalUnread),
                    "}");
        }

        json.addln("}}");

        send(json.toString());

        // Also send the update to the conversation
        json.reset();
        final StudentKey key = conversation.owner.studentKey;

        json.addln("{\"convUpdated\": {");
        json.addln("  \"studentId\": \"", key.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"convNbr\": ", Integer.toString(conversation.conversationNumber),
                ", \"subject\": \"", conversation.getSubject().replace(CoreConstants.QUOTE, "\\\""),
                "\", \"msgCount\": ", Integer.toString(conversation.getNumUndeleted()),
                ", \"nbrUnreadByStaff\": ", Integer.toString(conversation.getNumUnreadByStaff()),
                ", \"nbrUnreadByStu\": ", Integer.toString(conversation.getNumUnreadByStudent()));
        json.addln("}}");

        send(json.toString());
    }

    /**
     * Called when the subject of a conversation is changed.
     *
     * @param conversation the conversation
     */
    @Override
    public void subjectChanged(final Conversation conversation) {

        final StudentKey key = conversation.owner.studentKey;

        final HtmlBuilder json = new HtmlBuilder(100);

        json.addln("{\"convUpdated\": {");
        json.addln("  \"studentId\": \"", key.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"convNbr\": ", Integer.toString(conversation.conversationNumber),
                ", \"subject\": \"", conversation.getSubject().replace(CoreConstants.QUOTE, "\\\""),
                "\", \"msgCount\": ", Integer.toString(conversation.getNumUndeleted()),
                ", \"nbrUnreadByStaff\": ", Integer.toString(conversation.getNumUnreadByStaff()),
                ", \"nbrUnreadByStu\": ", Integer.toString(conversation.getNumUnreadByStudent()));
        json.addln("}}");

        send(json.toString());
    }

    /**
     * Called when a message is added to a conversation.
     *
     * @param theMessage the message that was added
     */
    @Override
    public void messageAdded(final ConversationMessage theMessage) {

        final Conversation conv = theMessage.conversation;
        final StudentKey key = conv.owner.studentKey;

        final HtmlBuilder json = new HtmlBuilder(100);

        json.addln("{\"msgAdded\": {");
        json.addln("  \"studentId\": \"", key.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"convNbr\": ", Integer.toString(conv.conversationNumber),
                "\", \"msgNbr\": ", Integer.toString(theMessage.messageNumber),
                ", \"created\": \"", theMessage.whenCreated.toString(),
                "\", \"authorId\": \"", theMessage.author.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"authorName\": \"", theMessage.author.screenName.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"state\": \"", theMessage.getState().code,
                CoreConstants.QUOTE);
        json.addln("}}");

        send(json.toString());
    }

    /**
     * Called when the state of a message has been updated. Should be called from within a block synchronized on this
     * object.
     *
     * @param theMessage the message whose state changed
     */
    @Override
    public void messageStateUpdated(final ConversationMessage theMessage) {

        final Conversation conv = theMessage.conversation;
        final StudentKey key = conv.owner.studentKey;

        final HtmlBuilder json = new HtmlBuilder(100);

        json.addln("{\"msgStateUpdated\": {");
        json.addln("  \"studentId\": \"", key.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"convNbr\": ", Integer.toString(conv.conversationNumber),
                "\", \"msgNbr\": ", Integer.toString(theMessage.messageNumber),
                "\", \"state\": \"", theMessage.getState().code, CoreConstants.QUOTE);
        json.addln("}}");

        send(json.toString());
    }

    /**
     * Called when the "when read" timestamp of a message has been updated. Should be called from within a block
     * synchronized on this object.
     *
     * @param theMessage the message whose "when read" timestamp changed
     */
    @Override
    public void messageWhenReadUpdated(final ConversationMessage theMessage) {

        final Conversation conv = theMessage.conversation;
        final StudentKey key = conv.owner.studentKey;

        final HtmlBuilder json = new HtmlBuilder(100);

        json.addln("{\"msgWhenReadUpdated\": {");
        json.addln("  \"studentId\": \"", key.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"convNbr\": ", Integer.toString(conv.conversationNumber),
                "\", \"msgNbr\": ", Integer.toString(theMessage.messageNumber));
        if (theMessage.getWhenRead() != null) {
            json.addln(", \"read\": \"", theMessage.getWhenRead().toString(), CoreConstants.QUOTE);
        }
        json.addln("}}");

        send(json.toString());
    }

    /**
     * Sends a complete list of all students with conversations to the client.
     */
    private void sendCompleteConversationsList() {

        final HtmlBuilder json = new HtmlBuilder(100);
        json.addln("{\"allConvLists\": [");

        final ConversationsContainer container = HelpManager.getInstance().conversations;

        synchronized (container) {
            final List<StudentKey> keys = container.getStudentKeys();

            boolean comma = false;
            for (final StudentKey key : keys) {
                final StudentConversationList list = container.getStudentConversationList(key);

                int total = 0;
                int totalUnread = 0;
                final int num = list.getNumConversations();
                for (int i = 0; i < num; ++i) {
                    final Conversation conversation = list.getConversation(i);
                    total += conversation.getNumUndeleted();
                    totalUnread += conversation.getNumUnreadByStaff();
                }

                if (comma) {
                    json.add(CoreConstants.COMMA_CHAR);
                }

                json.addln("{\"studentId\": \"", key.studentId.replace(CoreConstants.QUOTE, "\\\""),
                        "\", \"firstName\": \"", key.firstName.replace(CoreConstants.QUOTE, "\\\""),
                        "\", \"lastName\": \"", key.lastName.replace(CoreConstants.QUOTE, "\\\""),
                        "\", \"numConv\": ", Integer.toString(num),
                        ", \"numUndeleted\": ", Integer.toString(total),
                        ", \"numUnread\": ", Integer.toString(totalUnread),
                        "}");

                comma = true;
            }
        }

        json.addln("]}");

        send(json.toString());
    }

    /**
     * Sends a list of all conversations for a student to the client.
     *
     * @param key the key
     */
    private void sendStudentConversations(final StudentKey key) {

        final HtmlBuilder json = new HtmlBuilder(100);
        json.addln("{\"stuConvList\": {");
        json.addln("  \"studentId\": \"", key.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"conversations\": [");

        final ConversationsContainer container = HelpManager.getInstance().conversations;

        synchronized (container) {
            boolean comma = false;
            final StudentConversationList list = container.getStudentConversationList(key);
            final int count = list.getNumConversations();

            for (int i = 0; i < count; ++i) {
                if (comma) {
                    json.add(CoreConstants.COMMA_CHAR);
                }

                final Conversation conversation = list.getConversation(i);
                json.addln("{\"convNbr\": ", Integer.toString(conversation.conversationNumber), //
                        ", \"subject\": \"", conversation.getSubject().replace(CoreConstants.QUOTE, "\\\""),
                        "\", \"msgCount\": ", Integer.toString(conversation.getNumUndeleted()),
                        ", \"nbrUnreadByStaff\": ", Integer.toString(conversation.getNumUnreadByStaff()), //
                        ", \"nbrUnreadByStu\": ", Integer.toString(conversation.getNumUnreadByStudent()), "}");

                comma = true;
            }
        }

        json.addln("]}}");

        send(json.toString());
    }

    /**
     * Sends a list of all messages in a conversation.
     *
     * @param conv the conversation
     */
    private void sendConversationMessages(final Conversation conv) {

        final StudentKey key = conv.owner.studentKey;

        final HtmlBuilder json = new HtmlBuilder(100);
        json.addln("{\"convMsgList\": {");
        json.addln("  \"studentId\": \"", key.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"convNbr\": ", Integer.toString(conv.conversationNumber), //
                ", \"messages\": [");

        final ConversationsContainer container = HelpManager.getInstance().conversations;

        synchronized (container) {
            boolean comma = false;
            final int count = conv.getTotalMessages();

            for (int i = 0; i < count; ++i) {
                if (comma) {
                    json.add(CoreConstants.COMMA_CHAR);
                }

                final ConversationMessage message = conv.getMessage(i);

                json.add("{\"msgNbr\": ", Integer.toString(message.messageNumber),
                        ", \"created\": \"", message.whenCreated.toString(),
                        "\", \"authorId\": \"", message.author.studentId.replace(CoreConstants.QUOTE, "\\\""),
                        "\", \"authorName\": \"", message.author.screenName.replace(CoreConstants.QUOTE, "\\\""),
                        "\", \"state\": \"", message.getState().code, CoreConstants.QUOTE);

                if (message.getWhenRead() != null) {
                    json.addln(", \"read\": \"", message.getWhenRead().toString(), CoreConstants.QUOTE);
                }
                comma = true;
            }
        }

        json.addln("]}}");

        send(json.toString());
    }

    /**
     * Sends a single conversation message to the client.
     *
     * @param msg the message to send
     */
    private void sendMessage(final ConversationMessage msg) {

        final Conversation conv = msg.conversation;
        final StudentKey key = conv.owner.studentKey;

        final HtmlBuilder json = new HtmlBuilder(100);
        json.addln("{\"convMsg\": {");
        json.addln("  \"studentId\": \"", key.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"convNbr\": ", Integer.toString(conv.conversationNumber),
                "\", \"msgNbr\": ", Integer.toString(msg.messageNumber),
                ", \"created\": \"", msg.whenCreated.toString(),
                "\", \"authorId\": \"", msg.author.studentId.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"authorName\": \"", msg.author.screenName.replace(CoreConstants.QUOTE, "\\\""),
                "\", \"state\": \"", msg.getState().code, CoreConstants.QUOTE);

        if (msg.getWhenRead() != null) {
            json.addln(", \"read\": \"", msg.getWhenRead().toString(), CoreConstants.QUOTE);
        }

        if (msg.getContent() != null) {
            json.addln(", \"content\": \"", msg.getContent().replace(CoreConstants.QUOTE, "\\\""), CoreConstants.QUOTE);
        }

        json.addln("}}");

        send(json.toString());
    }
}
