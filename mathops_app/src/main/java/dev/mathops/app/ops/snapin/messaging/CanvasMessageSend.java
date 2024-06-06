package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.json.JSONObject;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.app.canvas.ApiResult;
import dev.mathops.app.canvas.CanvasApi;
import dev.mathops.app.canvas.data.UserInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that can send a composed message to a student through Canvas.
 */
public final class CanvasMessageSend {

    /** If true, messages are not actually sent. */
    private static final boolean DEBUG = false;

    /** The Canvas API. */
    private final CanvasApi api;

    /** The data cache. */
    private final Cache cache;

    /** The course ID. */
    private final Long courseId;

    /**
     * Constructs a new {@code CanvasMessageSend}.
     *
     * @param theCache       the data cache
     * @param theCanvasHost  the hostname of the Canvas installation
     * @param theAccessToken the access token
     * @param theCourseId    the Canvas course ID
     */
    public CanvasMessageSend(final Cache theCache, final String theCanvasHost, final String theAccessToken,
                             final Long theCourseId) {

        this.api = new CanvasApi(theCanvasHost, theAccessToken);

        this.cache = theCache;

        final UserInfo userInfo = this.api.fetchUser();
        if (userInfo == null) {
            throw new IllegalArgumentException("Unable to log in and check user ID.");
        }
        final String name = userInfo.getDisplayName();
        Log.info("Connected to Canvas course ", theCourseId, " as ", name);

        this.courseId = theCourseId;
    }

    /**
     * Sends a new message to a student. This starts a new conversation and adds a message to that conversation.
     *
     * @param label         a label for the message, like "3 of 10"
     * @param theRecipients an array of recipient CSU IDs
     * @param theSubject    the subject
     * @param theBody       the body
     * @return true if the message was sent; false if not
     */
    public boolean sendMessage(final String label, final Collection<String> theRecipients,
                               final String theSubject, final String theBody) {

        boolean result = false;
        try {
            final Map<String, List<String>> parameters = new HashMap<>(10);

            final List<String> recipients = new ArrayList<>(theRecipients.size());
            for (final String csuId : theRecipients) {

                final RawStudent student = RawStudentLogic.query(this.cache, csuId, false);

                if (student == null) {
                    Log.info("Unable to find Canvas ID for student ", csuId);
                } else {
                    final String canvasId = student.canvasId;
                    if (canvasId == null) {
                        Log.info("No Canvas ID configured for student ", csuId);
                    } else {
                        recipients.add(canvasId);
                    }
                }
            }

            if (recipients.isEmpty()) {
                Log.warning("No recipients - canceling message");
            } else {
                final List<String> subject = new ArrayList<>(1);
                subject.add(theSubject);

                final List<String> body = new ArrayList<>(1);
                body.add(theBody);

                final List<String> group = new ArrayList<>(1);
                group.add("false");

                final List<String> contextCode = new ArrayList<>(1);
                contextCode.add("course_" + this.courseId);

                parameters.put("recipients[]", recipients);
                parameters.put("subject", subject);
                parameters.put("body", body);
                parameters.put("group_conversation", group);
                parameters.put("context_code", contextCode);

                if (DEBUG) {
                    Log.info("SEND: course = " + this.courseId);
                    Log.info("SEND: recipients[] = " + recipients);
                    Log.info("SEND: subject = " + theSubject);
                    Log.info("SEND: body = " + theBody.substring(0, 50));
                } else {
                    Log.info("Sending message ", label, " to student ", recipients, " in course ", this.courseId);

                    final ApiResult result1 = this.api.apiCall("conversations", "POST", parameters);

                    if (result1.response != null) {
                        Log.fine("Response:", result1.response.toJSONFriendly(0));
                    } else if (result1.arrayResponse != null) {
                        Log.fine("Array Response:");
                        for (final JSONObject element : result1.arrayResponse) {
                            Log.fine(element.toJSONFriendly(0));
                        }
                    } else {
                        Log.warning("ERROR: " + result1.error);
                    }
                }

                result = true;
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to access database", ex);
        }

        return result;
    }
}
