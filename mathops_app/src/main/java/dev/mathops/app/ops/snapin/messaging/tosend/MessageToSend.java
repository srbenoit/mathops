package dev.mathops.app.ops.snapin.messaging.tosend;

import dev.mathops.app.ops.snapin.messaging.EMilestone;
import dev.mathops.app.ops.snapin.messaging.EMsg;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;

/**
 * A message that is due to be sent.
 */
public final class MessageToSend {

    /** The messaging context. */
    public final MessagingContext context;

    /** The course status in the current course. */
    public final MessagingCourseStatus status;

    /** The course index. */
    final Integer courseIndex;

    /** The touch point. */
    public final EMilestone milestone;

    /** The message code. */
    public final EMsg msgCode;

    /** The message subject. */
    public String subject;

    /** The message body. */
    public String body;

    /**
     * Constructs a new {@code MessageToSend}.
     *
     * @param theContext     the messaging context
     * @param theStatus      the course status in the current course
     * @param theCourseIndex the course index
     * @param theMilestone   the touch point
     * @param theMsgCode     the message code
     * @param theSubject     the message subject
     * @param theBody        the message body
     */
    public MessageToSend(final MessagingContext theContext, final MessagingCourseStatus theStatus,
                         final Integer theCourseIndex, final EMilestone theMilestone, final EMsg theMsgCode,
                         final String theSubject, final String theBody) {

        if (theContext == null) {
            throw new IllegalArgumentException("Message context may not be null");
        }
        if (theStatus == null) {
            throw new IllegalArgumentException("Message status may not be null");
        }
        if (theCourseIndex == null) {
            throw new IllegalArgumentException("Course index may not be null");
        }
        if (theMsgCode == null) {
            throw new IllegalArgumentException("Message code may not be null");
        }

        this.context = theContext;
        this.status = theStatus;
        this.courseIndex = theCourseIndex;
        this.milestone = theMilestone;
        this.msgCode = theMsgCode;
        this.subject = theSubject;
        this.body = theBody;
    }

    /**
     * Generates the String representation of the row.
     *
     * @return the string representation, which is the student screen name
     */
    @Override
    public String toString() {

        return this.context.student.getScreenName();
    }
}
