package dev.mathops.session.txn.handlers;

import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawSthwqaLogic;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawSthwqa;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.SubmitHomeworkReply;
import dev.mathops.session.txn.messages.SubmitHomeworkRequest;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;

/**
 * A handler for requests to submit homework assignments.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class SubmitHomeworkHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code SubmitHomeworkHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public SubmitHomeworkHandler(final DbProfile theDbProfile) {

        super(theDbProfile);
    }

    /**
     * Processes a message from the client.
     *
     * @param cache   the data cache
     * @param message the message received from the client
     * @return the reply to be sent to the client, or null if the connection should be closed
     */
    @Override
    public String process(final Cache cache, final AbstractRequestBase message) {

        setMachineId(message);
        touch(cache);

        final String result;

        // Validate the type of request
        if (message instanceof final SubmitHomeworkRequest request) {
            result = processRequest(cache, request);
        } else {
            Log.info("SubmitHomeworkHandler called with ", message.getClass().getName());

            final SubmitHomeworkReply reply = new SubmitHomeworkReply();
            reply.error = "Invalid request type for homework submission request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache   the data cache
     * @param request the {@code SubmitHomeworkRequest} received from the client
     * @return the generated reply XML to send to the client
     */
    private String processRequest(final Cache cache, final SubmitHomeworkRequest request) {

        final SubmitHomeworkReply reply = new SubmitHomeworkReply();

        try {
            final boolean ok = loadStudentInfo(cache, request.studentId, reply);

            if (ok) {
                LogBase.setSessionInfo("TXN", request.studentId);

                if (request.homework == null) {
                    Log.warning("Homework submission with no attached homework");
                    reply.error = "No homework assignment included in submission";
                } else if ("GUEST".equals(request.studentId) || "AACTUTOR".equals(request.studentId)) {
                    reply.result = "Guest login homework will not be recorded.";
                } else if ("ETEXT".equals(request.studentId)) {
                    reply.result = "Practice homeworks will not be recorded.";
                } else {
                    finalizeHomework(request, reply);
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            reply.result = "There was an error connecting to the database.";
        }

        return reply.toXml();
    }

    /**
     * Finalize the homework record on the server, running all grading processing and applying result to the student's
     * record.
     *
     * @param req   the homework submission request
     * @param rep   the homework submission reply
     * @return true if finalization succeeded; false otherwise
     */
    private boolean finalizeHomework(final SubmitHomeworkRequest req, final SubmitHomeworkReply rep) {

        // Client generated the timestamps, and their clock may be off, but we assume the duration is accurate. We
        // then set the finish time to the current server time, and the start time based on the duration submitted.
        long dur = req.homework.completionTime - req.homework.presentationTime;

        // Guard against wild values if one timestamp was null.
        if (dur > 86400000L || dur < 0L) {
            dur = 0L;
        }

        final ZonedDateTime now = ZonedDateTime.now();
        final long timesamp = now.toInstant().toEpochMilli();

        req.homework.completionTime = timesamp;
        req.homework.presentationTime = timesamp - dur;

        // TODO: Update finish times on individual problems, if needed

        final StudentData studentData = getStudentData();
        final String studentId = studentData.getStudentId();
        final SystemData systemData = studentData.getSystemData();

        boolean ok = false;
        try {
            // From the homework version, look up the course, unit in the homework table, then use that to fetch the
            // course/unit/section data for the student. This gives us the minimum move-on and mastery scores.
            final TermRec activeTerm = systemData.getActiveTerm();
            if (activeTerm == null) {
                rep.error = "Unable to lookup active term to submit homework.";
                return false;
            }

            final String ver = req.homework.examVersion;

            final AssignmentRec hw = systemData.getActiveAssignment(ver);
            if (hw == null) {
                rep.error = "Assignment has been removed from the course!";
                return false;
            }

            RawStcourse stcourse = studentData.getActiveRegistration(hw.courseId);

            if (stcourse == null) {
                boolean isSpecial = false;

                // 'TUTOR', 'ADMIN' special student types automatically in section "001" for 117, 118, 124, 125, 126.
                if (RawRecordConstants.M117.equals(hw.courseId) || RawRecordConstants.M118.equals(hw.courseId) || RawRecordConstants.M124.equals(hw.courseId) || RawRecordConstants.M125.equals(hw.courseId) || RawRecordConstants.M126.equals(hw.courseId) || RawRecordConstants.MATH117.equals(hw.courseId) || RawRecordConstants.MATH118.equals(hw.courseId) || RawRecordConstants.MATH124.equals(hw.courseId) || RawRecordConstants.MATH125.equals(hw.courseId) || RawRecordConstants.MATH126.equals(hw.courseId)) {

                    final List<RawSpecialStus> specials = studentData.getActiveSpecialCategories(now.toLocalDate());

                    for (final RawSpecialStus special : specials) {
                        final String type = special.stuType;

                        if ("TUTOR".equals(type) || "M384".equals(type) || "ADMIN".equals(type)) {
                            isSpecial = true;
                            break;
                        }
                    }
                }

                if (isSpecial) {
                    // Create a fake STCOURSE record
                    stcourse = new RawStcourse(activeTerm.term, // term
                            studentId, // stuId
                            hw.courseId, // course
                            "001", // sect
                            null, // paceOrder
                            "Y", // openStatus
                            null, // gradingOption,
                            "N", // completed
                            null, // score
                            null, // courseGrade
                            "Y", // prereqSatis
                            "N", // initClassRoll
                            "N", // stuProvided
                            "N", // finalClassRoll
                            null, // examPlaced
                            null, // zeroUnit
                            null, // timeoutFactor
                            null, // forfeitI
                            "N", // iInProgress
                            null, // iCounted
                            "N", // ctrlTest
                            null, // deferredFDt
                            Integer.valueOf(0), // bypassTimeout
                            null, // instrnType
                            null, // registrationStatus
                            null, // lastClassRollDate
                            null, // iTermKey
                            null); // iDeadlineDt

                    stcourse.synthetic = true;
                } else {
                    rep.error = "You are not registered in this course!";
                    return false;
                }
            }

            if (!activeTerm.term.equals(stcourse.termKey)) {
                final TermRec incTerm = systemData.getTerm(stcourse.termKey);
                if (incTerm != null) {
                    // FIXME: do something with this...
                }
            }

            final List<RawCusection> cusects = systemData.getCourseUnitSections(stcourse.course, stcourse.sect,
                    activeTerm.term);

            RawCusection cusect = null;
            for (final RawCusection rawCusection : cusects) {
                if (rawCusection.unit != null && rawCusection.unit.equals(hw.unit)) {
                    cusect = rawCusection;
                    break;
                }
            }
            if (cusect == null) {
                rep.error = "Course section information not found!";
                return false;
            }

            int minMoveOn = 0;
            if (cusect.hwMoveonScore != null) {
                minMoveOn = cusect.hwMoveonScore.intValue();

                if (minMoveOn == -1) {
                    minMoveOn = req.homework.getNumProblems();
                }
            }

            int minMastery = 0;
            if (cusect.hwMasteryScore != null) {
                minMastery = cusect.hwMasteryScore.intValue();

                if (minMastery == -1) {
                    minMastery = req.homework.getNumProblems();
                }
            }

            // Compare the scores to the minimum mastery and move-on scores
            if (req.score >= minMastery) {

                // Record a "passing" homework record
                ok = recordMasteredHomework(studentData, req, rep, hw, stcourse);
            } else if (req.score >= minMoveOn) {

                // Record a "not-passing" homework record
                ok = recordNonMasteredHomework(studentData, req, rep, hw, stcourse);
            } else {
                // No entry in the database.
                rep.result = "Your score was not sufficient to move on.";
            }
        } catch (final Exception ex) {
            Log.warning("Exception while submitting assignment for student '", studentId, ex);
            ok = false;
        }

        return ok;
    }

    /**
     * Record a passing (mastered) homework assignment in the database. This creates a new STHOMEWORK record. If this is
     * the first passed homework in this unit, its passed field in set to "Y". Otherwise, the passed field is set to
     * "2", "3", "4", etc.
     *
     * @param studentData        the student data object
     * @param req      the homework submission request
     * @param rep      the homework submission reply
     * @param hw       the homework assignment being submitted
     * @param stcourse the student course registration information
     * @return true if insertion succeeded; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean recordMasteredHomework(final StudentData studentData, final SubmitHomeworkRequest req,
                                           final SubmitHomeworkReply rep, final AssignmentRec hw,
                                           final RawStcourse stcourse) throws SQLException {

        // We must first find any existing PASSED homework for this course, unit and objective, and
        // determine what to set this new record's PASSED field to.

        final List<RawSthomework> exist = studentData.getStudentHomeworkForCourseUnit(hw.courseId, hw.unit, true,
                hw.assignmentType);

        int max = 0;
        boolean foundY = false;
        for (final RawSthomework rawSthomework : exist) {

            // Ignore if not for this objective
            if (!rawSthomework.objective.equals(hw.objective)) {
                continue;
            }

            int which = 0;
            if ("Y".equals(rawSthomework.passed)) {
                which = 1;
                foundY = true;
            } else if (Character.isDigit(rawSthomework.passed.charAt(0))) {
                try {
                    which = Long.valueOf(rawSthomework.passed).intValue();
                } catch (final NumberFormatException ex) {
                    Log.warning("Failed to parse integer");
                }
            }

            if (which > max) {
                max = which;
            }

            // If we didn't find a "Y" row, reset max to 0, since we want to put this row in
            // as "Y", even if there are numeric rows.
            if (!foundY) {
                max = 0;
            }
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(req.homework.completionTime);

        final String passed;
        if ("LB".equals(hw.assignmentType) || (max == 0)) {
            passed = "Y";
        } else {
            passed = Integer.toString(max + 1);
        }

        final LocalDateTime start = TemporalUtils.toLocalDateTime(req.homework.presentationTime);
        final LocalDateTime end = TemporalUtils.toLocalDateTime(req.homework.completionTime);

        final int startTime = TemporalUtils.minuteOfDay(start);
        final int endTime = TemporalUtils.minuteOfDay(end);

        final String studentId = studentData.getStudentId();
        final RawSthomework sthw = new RawSthomework(Long.valueOf(generateSerialNumber(false)),
                req.homework.examVersion, studentId, end.toLocalDate(), Integer.valueOf(req.score),
                Integer.valueOf(startTime), Integer.valueOf(endTime), "Y", passed, hw.assignmentType, hw.courseId,
                stcourse.sect, hw.unit, hw.objective, "N", null, null);

        final Cache cache = studentData.getCache();
        RawSthomeworkLogic.INSTANCE.insert(cache, sthw);
        final boolean ok = recordQuestionAnswers(cache, studentId, req, hw, sthw.serialNbr);

        if (ok) {
            if ("LB".equals(hw.assignmentType)) {
                rep.result = "Lab has been recorded.";
            } else if (max == 0) {
                rep.result = "Assignment has been recorded.";
            } else {
                rep.result = "Assignment was previously completed.";
            }
        } else {
            rep.error = "Failed to record assignment.";
        }

        studentData.forgetStudentHomeworks();
        studentData.forgetStudentHomeworkAnswers();

        return ok;
    }

    /**
     * Record a non-mastered homework assignment in the database. This creates a new STHOMEWORK record with the passed
     * field set to "N".
     *
     * @param studentData        the student data object
     * @param req      the homework submission request
     * @param rep      the homework submission reply
     * @param hw       the homework assignment being submitted
     * @param stcourse the student course registration information
     * @return true if insertion succeeded; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean recordNonMasteredHomework(final StudentData studentData, final SubmitHomeworkRequest req,
                                              final SubmitHomeworkReply rep, final AssignmentRec hw,
                                              final RawStcourse stcourse) throws SQLException {

        final LocalDateTime start = TemporalUtils.toLocalDateTime(req.homework.presentationTime);
        final LocalDateTime end = TemporalUtils.toLocalDateTime(req.homework.completionTime);

        final int startTime = TemporalUtils.minuteOfDay(start);
        final int endTime = TemporalUtils.minuteOfDay(end);

        final String studentId = studentData.getStudentId();

        final RawSthomework sthw = new RawSthomework(Long.valueOf(generateSerialNumber(false)),
                req.homework.examVersion, studentId, end.toLocalDate(), Integer.valueOf(req.score),
                Integer.valueOf(startTime), Integer.valueOf(endTime), "Y", "N", hw.assignmentType, hw.courseId,
                stcourse.sect, hw.unit, hw.objective, "N", null, null);

        final Cache cache = studentData.getCache();
        RawSthomeworkLogic.INSTANCE.insert(cache, sthw);
        final boolean ok = recordQuestionAnswers(cache, studentId, req, hw, sthw.serialNbr);

        if (ok) {
            rep.result = "Assignment accepted.";
        } else {
            rep.error = "Failed to record assignment.";
        }

        studentData.forgetStudentHomeworks();
        studentData.forgetStudentHomeworkAnswers();

        return ok;
    }

    /**
     * Write the series of STHWQA records to the database to record the student's answers on the homework assignment.
     *
     * @param cache the data cache
     * @param studentId the student ID
     * @param req          the homework submission request (with student answers)
     * @param hw           the homework assignment being submitted
     * @param serialNumber the serial number of the homework submission
     * @return true if successful; false on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean recordQuestionAnswers(final Cache cache, final String studentId, final SubmitHomeworkRequest req,
                                          final AssignmentRec hw, final Long serialNumber) throws SQLException {

        final int numAns = req.answers.length;

        // answers[0] is time stamps, so we start at 1
        for (int i = 1; i < numAns; ++i) {

            final ExamProblem prob = req.homework.getProblem(i);
            if (prob == null) {
                continue;
            }

            final AbstractProblemTemplate selected = prob.getSelectedProblem();
            if (selected == null) {
                continue;
            }

            // construct the answer string
            final char[] ans = "     ".toCharArray();

            if (req.answers[i] != null) {
                final int ansLen = req.answers[i].length;

                for (int j = 0; j < ansLen; ++j) {

                    if (req.answers[i][j] instanceof Long) {
                        final int index = ((Long) req.answers[i][j]).intValue();

                        if (index >= 1 && index <= 5) {
                            ans[index - 1] = (char) ('A' + index - 1);
                        }
                    }
                }
            }

            final String obj = hw.objective == null ? null : hw.objective.toString();
            final LocalDateTime fin = TemporalUtils.toLocalDateTime(req.homework.completionTime);
            final int finTime = TemporalUtils.minuteOfDay(fin);

            final RawSthwqa sthwqa = new RawSthwqa(serialNumber, Integer.valueOf(i), Integer.valueOf(1), obj,
                    new String(ans), studentId, hw.assignmentId, selected.isCorrect(req.answers[i]) ? "Y" : "N",
                    fin.toLocalDate(), Integer.valueOf(finTime));

            if (!RawSthwqaLogic.INSTANCE.insert(cache, sthwqa)) {
                Log.warning("There was an error recording an assignment score.");
                break;
            }
        }

        return true;
    }
}
