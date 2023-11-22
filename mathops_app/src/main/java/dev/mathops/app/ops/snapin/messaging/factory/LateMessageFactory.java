package dev.mathops.app.ops.snapin.messaging.factory;

import dev.mathops.db.Cache;
import dev.mathops.app.ops.snapin.messaging.MessagingContext;
import dev.mathops.app.ops.snapin.messaging.MessagingCourseStatus;
import dev.mathops.app.ops.snapin.messaging.tosend.MessageToSend;

/**
 * A factory class that can generate messages to students who are "late" (urgency >= 1)
 */
public enum LateMessageFactory {
    ;

    /**
     * Generates an appropriate message. The "urgency" computed for the student governs how frequently this method is
     * called - the generated message is based on the student's situation and what messages may have been sent
     * previously.
     *
     * @param cache   the data cache
     * @param context the messaging context
     * @param status  the student's status in their current course
     * @return a report row if a message is to be sent to the student; null if not
     */
    public static MessageToSend generate(final Cache cache, final MessagingContext context,
                                         final MessagingCourseStatus status) {

        MessageToSend result = null;

        if (!status.metPrereq) {
            // Student has not met prerequisite for their first course
            result = LateMessagePrereqFactory.generate(cache, context, status);
        } else if (context.currentRegIndex == 0 && !status.started) {
            // Student has not started their first course
            result = LateMessageStartFirstFactory.generate(context, status);
        } else if (!status.passedUsers) {
            // Student has not passed User's Exam
            result = LateMessageUsersExamFactory.generate(context, status);
        } else if (!status.passedSR) {
            // Student has not passed Skills Review exam in current course
            result = LateMessageSkillsReviewFactory.generate(context, status);

            //

        } else if (!status.passedHw11()) {
            // Student has not passed Homework 1.1 in current course
            result = LateMessageHW11Factory.generate(context, status);
        } else if (!status.passedHw12()) {
            // Student has not passed Homework 1.2 in current course
            result = LateMessageHW12Factory.generate(context, status);
        } else if (!status.passedHw13()) {
            // Student has not passed Homework 1.3 in current course
            result = LateMessageHW13Factory.generate(context, status);
        } else if (!status.passedHw14()) {
            // Student has not passed Homework 1.4 in current course
            result = LateMessageHW14Factory.generate(context, status);
        } else if (!status.passedHw15()) {
            // Student has not passed Homework 1.5 in current course
            result = LateMessageHW15Factory.generate(context, status);
        } else if (!status.passedRE1) {
            // Student has not passed Review Exam 1 in current course
            result = LateMessageRE1Factory.generate(context, status);
        } else if (!status.passedUE1) {
            // Student has not passed Unit Exam 1 in current course
            result = LateMessageUE1Factory.generate(context, status);
        } else if (!status.passedHw21()) {
            // Student has not passed Homework 2.1 in current course
            result = LateMessageHW21Factory.generate(context, status);
        } else if (!status.passedHw22()) {
            // Student has not passed Homework 2.2 in current course
            result = LateMessageHW22Factory.generate(context, status);
        } else if (!status.passedHw23()) {
            // Student has not passed Homework 2.3 in current course
            result = LateMessageHW23Factory.generate(context, status);
        } else if (!status.passedHw24()) {
            // Student has not passed Homework 2.4 in current course
            result = LateMessageHW24Factory.generate(context, status);
        } else if (!status.passedHw25()) {
            // Student has not passed Homework 2.5 in current course
            result = LateMessageHW25Factory.generate(context, status);
        } else if (!status.passedRE2) {
            // Student has not passed Review Exam 2 in current course
            result = LateMessageRE2Factory.generate(context, status);
        } else if (!status.passedUE2) {
            // Student has not passed Unit Exam 2 in current course
            result = LateMessageUE2Factory.generate(context, status);
        } else if (!status.passedHw31()) {
            // Student has not passed Homework 3.1 in current course
            result = LateMessageHW31Factory.generate(context, status);
        } else if (!status.passedHw32()) {
            // Student has not passed Homework 3.2 in current course
            result = LateMessageHW32Factory.generate(context, status);
        } else if (!status.passedHw33()) {
            // Student has not passed Homework 3.3 in current course
            result = LateMessageHW33Factory.generate(context, status);
        } else if (!status.passedHw34()) {
            // Student has not passed Homework 3.4 in current course
            result = LateMessageHW34Factory.generate(context, status);
        } else if (!status.passedHw35()) {
            // Student has not passed Homework 3.5 in current course
            result = LateMessageHW35Factory.generate(context, status);
        } else if (!status.passedRE3) {
            // Student has not passed Review Exam 3 in current course
            result = LateMessageRE3Factory.generate(context, status);
        } else if (!status.passedUE3) {
            // Student has not passed Unit Exam 3 in current course
            result = LateMessageUE3Factory.generate(context, status);
        } else if (!status.passedHw41()) {
            // Student has not passed Homework 4.1 in current course
            result = LateMessageHW41Factory.generate(context, status);
        } else if (!status.passedHw42()) {
            // Student has not passed Homework 4.2 in current course
            result = LateMessageHW42Factory.generate(context, status);
        } else if (!status.passedHw43()) {
            // Student has not passed Homework 4.3 in current course
            result = LateMessageHW43Factory.generate(context, status);
        } else if (!status.passedHw44()) {
            // Student has not passed Homework 4.4 in current course
            result = LateMessageHW44Factory.generate(context, status);
        } else if (!status.passedHw45()) {
            // Student has not passed Homework 4.5 in current course
            result = LateMessageHW45Factory.generate(context, status);
        } else if (!status.passedRE4) {
            // Student has not passed Review Exam 4 in current course
            result = LateMessageRE4Factory.generate(context, status);
        } else if (!status.passedUE4) {
            // Student has not passed Unit Exam 4 in current course
            result = LateMessageUE4Factory.generate(context, status);
        } else if (!status.passedFIN) {
            // Has not passed final
            result = LateMessageFINFactory.generate(context, status);
        } else if (status.totalScore < 54) {
            // Passed final, does not yet have 54 points (urgency will be 3)
            result = LateMessageNeeds54Factory.generate(context, status);
        }

        return result;
    }
}
