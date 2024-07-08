package dev.mathops.session.txn.handlers;

import dev.mathops.assessment.exam.ExamGradingCondition;
import dev.mathops.assessment.exam.ExamGradingRule;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamOutcome;
import dev.mathops.assessment.exam.ExamOutcomeAction;
import dev.mathops.assessment.exam.ExamOutcomePrereq;
import dev.mathops.assessment.exam.ExamOutcomeValidation;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSubtest;
import dev.mathops.assessment.exam.ExamSubtestProblem;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableBoolean;
import dev.mathops.assessment.variable.VariableInteger;
import dev.mathops.assessment.variable.VariableReal;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.logic.ChallengeExamLogic;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawlogic.RawExamLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawMpeLogLogic;
import dev.mathops.db.old.rawlogic.RawMpecrDeniedLogic;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeqaLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStmpeqaLogic;
import dev.mathops.db.old.rawlogic.RawStqaLogic;
import dev.mathops.db.old.rawlogic.RawStsurveyqaLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawlogic.RawSurveyqaLogic;
import dev.mathops.db.old.rawlogic.RawTestingCenterLogic;
import dev.mathops.db.old.rawlogic.RawUsersLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStchallengeqa;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStmpeqa;
import dev.mathops.db.old.rawrecord.RawStqa;
import dev.mathops.db.old.rawrecord.RawStsurveyqa;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rawrecord.RawSurveyqa;
import dev.mathops.db.old.rawrecord.RawUsers;
import dev.mathops.db.old.rec.MasteryAttemptQaRec;
import dev.mathops.db.old.rec.MasteryAttemptRec;
import dev.mathops.db.old.rec.MasteryExamRec;
import dev.mathops.db.old.reclogic.MasteryAttemptLogic;
import dev.mathops.db.old.reclogic.MasteryAttemptQaLogic;
import dev.mathops.db.old.reclogic.MasteryExamLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.LiveSessionInfo;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.UpdateExamReply;
import dev.mathops.session.txn.messages.UpdateExamRequest;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A handler for requests to update an in-progress exam, including finalization of the exam, resulting in execution of
 * the scoring processing.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final  class UpdateExamHandler extends AbstractHandlerBase {

    /**
     * Constructs a new {@code UpdateExamHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public UpdateExamHandler(final DbProfile theDbProfile) {

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

        String result;

        // Validate the type of request
        if (message instanceof final UpdateExamRequest request) {
            Log.info(request.toXml());

            try {
                result = processRequest(cache, request);
            } catch (final SQLException ex) {
                Log.warning(ex);
                final UpdateExamReply reply = new UpdateExamReply();
                reply.error = "Error processing request";
                result = reply.toXml();
            }
        } else {
            Log.warning("UpdateExamHandler called with ", message.getClass().getName());

            final UpdateExamReply reply = new UpdateExamReply();
            reply.error = "Invalid request type for exam submission request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Processes a request from the client.
     *
     * @param cache   the data cache
     * @param request the {@code UpdateExamRequest} received from the client
     * @return the generated reply XML to send to the client
     * @throws SQLException if there is an error accessing the database
     */
    private String processRequest(final Cache cache, final UpdateExamRequest request) throws SQLException {

        // FIXME: Exam attempt data does not get logged until after database access to look up
        // student ID - should store it somewhere in case DB is down.

        final UpdateExamReply reply = new UpdateExamReply();

        final TermRec active = cache.getSystemData().getActiveTerm();

        if (!loadStudentInfo(cache, request.studentId, reply)) {
            return reply.toXml();
        }

        LogBase.setSessionInfo("TXN", request.studentId);

        final String stuId = getStudent().stuId;

        if ("GUEST".equals(stuId) || "AACTUTOR".equals(stuId)) {

            reply.error = "Guest login exams will not be recorded.";
            return reply.toXml();
        }

        if ("ETEXT".equals(request.studentId)) {
            reply.error = "Practice exams will not be recorded.";
            return reply.toXml();
        }

        Log.info("Writing updated exam state");

        // Write the updated exam state out somewhere permanent
        if (request.realizationTime != null && request.identifierReference != null) {
            new ExamWriter().writeUpdatedExam(stuId, active, request.getAnswers(), false);
        }

        if (request.finalize) {
            if (request.realizationTime != null) {

                long serial = 0L;
                if (request.getAnswers()[0][0] instanceof Long) {
                    serial = ((Long) request.getAnswers()[0][0]).longValue();
                }

                Log.info("Reading presented exam");
                // load saved presented exam for grading
                final ExamObj exam = new ExamWriter().readPresentedExam(stuId, active, serial);

                // grade the exam
                if (exam != null) {
                    exam.importState(request.getAnswers());
                    finalizeExam(cache, exam, request, reply);
                    Log.info("Finished grading ", exam.examVersion, " exam ", exam.serialNumber);
                } else {
                    reply.error = "Found no record of exam being issued.";
                    Log.severe("Found no record of exam being issued.\n" + request);
                }
            } else {
                Log.info("Exam update marked for finalize but has no realization time");
                reply.error = "Exam had no creation timestamp.  Unable to grade";
            }
        } else {
            // Create a record of the student exam information, but with no score or answer data.

            // NOTE: Since this is a practice exam, we have no XML files from which to get an
            // authoritative start time. But we do have the presentation time and update times,
            // both set from the client's clock, and we can compute the difference to see how long
            // the student worked on the exam. We use this to reverse-engineer a start time.
            long duration;

            if (request.presentationTime == null || request.updateTime == null) {
                duration = 0L; // We have no data, so don't show something misleading
            } else {
                duration = request.updateTime.longValue() - request.presentationTime.longValue();

                if (duration < 0L || duration > 24L * 3600L * 1000L) {
                    duration = 0L; // Client's clock is bad - don't record
                }
            }

            final ExamObj exam = new ExamWriter().readPresentedExam(stuId, active, 0L);

            if (exam != null) {
                exam.importState(request.getAnswers());

                final RawExam examObj = RawExamLogic.query(cache, exam.examVersion);

                if (examObj != null) {
                    Long ser = exam.serialNumber;
                    if (ser == null || ser.intValue() == -1) {
                        ser = Long.valueOf(generateSerialNumber(false));
                    }

                    final LocalDateTime finish = TemporalUtils.toLocalDateTime(System.currentTimeMillis());
                    final LocalDateTime start = TemporalUtils.toLocalDateTime(System.currentTimeMillis() - duration);

                    Integer unit = null;
                    try {
                        unit = Integer.valueOf(exam.courseUnit);
                    } catch (final NumberFormatException ex) {
                        Log.warning("Failed to parse unit", ex);
                    }

                    String source = null;
                    if (request.proctored) {
                        if (getMachineId() == null) {
                            // Exam was taken at a remote station, but proctored
                            source = "RM";
                        } else {
                            // Exam was taken in the testing center
                            source = "TC";
                        }
                    }

                    final RawStexam stexam = new RawStexam(ser, exam.examVersion, getStudent().stuId,
                            finish.toLocalDate(), Integer.valueOf(0), Integer.valueOf(0),
                            Integer.valueOf(start.getHour() * 60 + start.getMinute()),
                            Integer.valueOf(finish.getHour() * 60 + finish.getMinute()), "Y", "C", null, exam.course,
                            unit, examObj.examType, "N", source, null);

                    RawStexamLogic.INSTANCE.insert(cache, stexam);
                }
            }
        }

        return reply.toXml();
    }

    /**
     * Finalize the exam record on the server, running all grading processing and applying result to the student's
     * record.
     *
     * @param cache     the data cache
     * @param presented the exam that the student is working on
     * @param req       the exam update request
     * @param rep       the exam update reply
     * @throws SQLException if there is an error accessing the database
     */
    private void finalizeExam(final Cache cache, final ExamObj presented, final UpdateExamRequest req,
                              final UpdateExamReply rep) throws SQLException {

        final String courseId = presented.course;

        final Boolean isTut = cache.getSystemData().isCourseTutorial(courseId);
        if (isTut == null) {
            Log.warning("No data for course '", presented.course);
            return;
        }

        // Store the presentation and completion times in the exam object
        Long serial = null;
        if (req.getAnswers() != null && req.getAnswers().length > 0
                && req.getAnswers()[0].length == 4) {

            if (req.getAnswers()[0][0] instanceof Long) {
                serial = (Long) req.getAnswers()[0][0];
            }

            // If exam has both presentation and completion time, compute the duration as seen by
            // the client, then adjust for the server's clock
            if (req.getAnswers()[0][2] != null && req.getAnswers()[0][3] != null) {
                final long duration = ((Long) req.getAnswers()[0][3]).longValue()
                        - ((Long) req.getAnswers()[0][2]).longValue();

                if (duration >= 0L && duration < 43200L) {
                    presented.presentationTime = System.currentTimeMillis() - duration;
                } else {
                    // Time was not reasonable, so set to 0 time.
                    Log.warning("Client gave exam duration as " + duration);
                    presented.presentationTime = System.currentTimeMillis();
                }
            } else if (req.realizationTime != null) {
                presented.presentationTime = req.realizationTime.longValue();
            } else {
                // Client did not provide duration
                Log.warning("Client did not give exam duration");
                presented.presentationTime = System.currentTimeMillis();
            }

            // The following just sets the completion time...
            presented.finalizeExam();
        }

        // See if the exam has already been inserted
        final Long ser = presented.serialNumber;
        final LocalDateTime start = TemporalUtils.toLocalDateTime(presented.realizationTime);

        final RawStudent student = getStudent();
        if (RawRecordConstants.M100P.equals(presented.course)) {
            final List<RawStmpe> existing = RawStmpeLogic.queryLegalByStudent(cache, student.stuId);

            for (final RawStmpe test : existing) {
                final LocalDateTime st = test.getStartDateTime();

                if (test.serialNbr != null && test.serialNbr.equals(ser) && st != null && st.equals(start)) {
                    Log.warning("Submitted placement exam for student ", student.stuId, ", exam ",
                            presented.examVersion, ": serial=", test.serialNbr, " submitted a second time - ignoring");
                    return;
                }
            }
        } else if (ChallengeExamLogic.M117_CHALLENGE_EXAM_ID.equals(presented.examVersion)
                || ChallengeExamLogic.M118_CHALLENGE_EXAM_ID.equals(presented.examVersion)
                || ChallengeExamLogic.M124_CHALLENGE_EXAM_ID.equals(presented.examVersion)
                || ChallengeExamLogic.M125_CHALLENGE_EXAM_ID.equals(presented.examVersion)
                || ChallengeExamLogic.M126_CHALLENGE_EXAM_ID.equals(presented.examVersion)) {

            final List<RawStchallenge> existing = RawStchallengeLogic.queryByStudentCourse(cache, student.stuId,
                    presented.course);

            for (final RawStchallenge test : existing) {

                final LocalDateTime testStart;
                if (test.examDt == null || test.startTime == null) {
                    testStart = null;
                } else {
                    final int startMin = test.startTime.intValue();
                    testStart = LocalDateTime.of(test.examDt, LocalTime.of(startMin / 60, startMin % 60));
                }

                if (test.serialNbr != null && test.serialNbr.equals(ser) && testStart != null
                        && testStart.equals(start)) {
                    Log.warning("Submitted challenge exam for student ", student.stuId, ", exam ",
                            presented.examVersion, ": serial=", test.serialNbr, " submitted a second time - ignoring");
                    return;
                }
            }
        } else if ("synthetic".equals(presented.ref)) {

            final List<MasteryAttemptRec> existing = MasteryAttemptLogic.get(cache).queryByStudent(cache,
                    student.stuId);

            for (final MasteryAttemptRec test : existing) {
                if (test.serialNbr != null && test.serialNbr.longValue() == ser.longValue()) {
                    Log.warning("Submitted mastery exam for student ", student.stuId, ", exam ", presented.examVersion,
                            ": serial=", test.serialNbr, " submitted a second time - ignoring");
                    return;
                }
            }
        } else {
            final List<RawStexam> existing = RawStexamLogic.getExams(cache, student.stuId, courseId, true);
            for (final RawStexam test : existing) {
                final LocalDateTime started = test.getStartDateTime();

                if (test.serialNbr != null && test.serialNbr.equals(ser) && started != null && started.equals(start)) {

                    Log.warning("Submitted exam for student ", student.stuId, ", exam ", presented.examVersion,
                            ": serial=", test.serialNbr, " submitted a second time - ignoring");
                    return;
                }
            }
        }

        // If serial number is still zero, we need to go looking for a pending exam row and go
        // match the serial number based on realization time

        if (serial == null) {
            final List<RawPendingExam> pendings = RawPendingExamLogic.queryByStudent(cache, req.studentId);

            for (final RawPendingExam obj : pendings) {
                final LocalDateTime realized = TemporalUtils.toLocalDateTime(presented.realizationTime);
                final LocalTime tm = realized.toLocalTime();
                final int min = tm.getHour() * 60 + tm.getMinute();

                if (obj.serialNbr != null && obj.examDt != null && obj.startTime != null &&
                        obj.examDt.equals(realized.toLocalDate()) && obj.startTime.intValue() == min) {
                    Log.warning("Forced to recover serial number from pending exam");
                }
            }
        }

        RawPendingExamLogic.delete(cache, presented.serialNumber, student.stuId);

        if ("synthetic".equals(presented.ref)) {
            processStandardMasteryExam(cache, presented, student, req);
        } else {
            processOldExam(cache, presented, student, isTut, req, rep);
        }

        if (getClient() != null) {
            RawClientPcLogic.updateCurrentStatus(cache, getClient().computerId, RawClientPc.STATUS_EXAM_RESULTS);
        }

    }
    /**
     * Processes finalization of an "OLD" exam.
     *
     * @param cache     the cache
     * @param presented the presented exam
     * @param student   the student record
     * @param req       the request
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    private boolean processStandardMasteryExam(final Cache cache, final ExamObj presented, final RawStudent student,
                                               final UpdateExamRequest req) throws SQLException {

        boolean ok = true;

        final String remoteStr = Boolean.toString(presented.remote);
        Log.info("Grading standards mastery exam for student ", student.stuId, ", exam ", presented.examVersion,
                ": Remote=", remoteStr);

        // We record every "unit" represented in the submitted exam as a separate mastery attempt

        final ZonedDateTime zonedNow = ZonedDateTime.now();
        final ZoneOffset zonedNowOffset = zonedNow.getOffset();

        final long startSecond = presented.presentationTime / 1000L;
        final int startNano = (int)(presented.presentationTime % 1000L) * 1000000;
        final LocalDateTime start = LocalDateTime.ofEpochSecond(startSecond, startNano, zonedNowOffset);

        final long endSecond = presented.completionTime / 1000L;
        final int endNano = (int)(presented.completionTime % 1000L) * 1000000;
        final LocalDateTime finish = LocalDateTime.ofEpochSecond(endSecond, endNano, zonedNowOffset);

        final int ser = presented.serialNumber.intValue();
        final Integer serialInt = Integer.valueOf(ser);

        final Object[][] answers = req.getAnswers();
        int answerId = 1;

        final int numSect = presented.getNumSections();
        for (int i = 0; i < numSect; ++i) {
            final ExamSection sect = presented.getSection(i);
            final int[] unitObjective = parseUnitAndObjective(sect.shortName);

            if (unitObjective != null) {
                final Integer unitInt = Integer.valueOf(unitObjective[0]);
                final Integer objInt = Integer.valueOf(unitObjective[1]);

                final List<MasteryExamRec> masteryExams = MasteryExamLogic.get(cache)
                        .queryActiveByCourseUnitObjective(cache, presented.course, unitInt, objInt);

                final int count = masteryExams.size();
                if (count == 1) {
                    final MasteryExamRec masteryExam = masteryExams.getFirst();

                    final MasteryAttemptRec attempt = new MasteryAttemptRec();
                    attempt.serialNbr = serialInt;
                    attempt.examId = masteryExam.examId;
                    attempt.stuId = student.stuId;
                    attempt.whenStarted = start;
                    attempt.whenFinished = finish;
                    attempt.examSource = "TC";
                    attempt.masteryScore = Integer.valueOf(2);

                    int score = 0;

                    final int numProblems = sect.getNumProblems();
                    for (int j = 0; j < numProblems; ++j) {

                        final ExamProblem eprob = sect.getProblem(j);
                        if (eprob != null) {
                            final AbstractProblemTemplate prob = eprob.getSelectedProblem();
                            if (answers[answerId] == null) {
                                final String courseTargetStr = SimpleBuilder.concat(presented.course, " target ",
                                        unitInt, ".", objInt);
                                Log.warning("Answers array for problem " + answerId + " on mastery attempt for ",
                                        courseTargetStr, " was null");
                            } else {
                                prob.recordAnswer(answers[answerId]);

                                final MasteryAttemptQaRec attemptQa = new MasteryAttemptQaRec();
                                attemptQa.serialNbr = serialInt;
                                attemptQa.examId = masteryExam.examId;
                                attemptQa.questionNbr = Integer.valueOf(j + 1);
                                final boolean correct = prob.isCorrect(answers[answerId]);
                                attemptQa.correct = correct ? "Y" : "N";

                                if (correct) {
                                    ++score;
                                }

                                if (!MasteryAttemptQaLogic.get(cache).insert(cache, attemptQa)) {
                                    final String courseTargetStr = SimpleBuilder.concat(presented.course, " target ",
                                            unitInt, ".", objInt);
                                    Log.warning("Failed to insert mastery attempt QA for ", courseTargetStr, ": ",
                                            attemptQa);
                                    ok = false;
                                }
                            }

                            ++answerId;
                        }
                    }

                    attempt.examScore = Integer.valueOf(score);
                    attempt.passed = score >= 2 ? "Y" : "N";
                    attempt.isFirstPassed = attempt.passed;

                    if (!MasteryAttemptLogic.get(cache).insert(cache, attempt)) {
                        final String courseTargetStr = SimpleBuilder.concat(presented.course, " target ", unitInt, ".",
                                objInt);
                        Log.warning("Failed to insert mastery attempt for ", courseTargetStr, ": ", attempt);
                        ok = false;
                    }
                } else {
                    final String courseTargetStr = SimpleBuilder.concat(presented.course, " target ", unitInt, ".",
                            objInt);
                    Log.warning("Found " + count+ " mastery exams for ", courseTargetStr);
                }
            }
        }

        return ok;
    }

    /**
     * Attempts to parse a unit and objective number from the short name of an exam section.
     * @param shortName the short name
     * @return a 2-integer array with the parsed unit and objective number if successful; null if not
     */
    private int[] parseUnitAndObjective(final String shortName) {

        int[] result = null;

        final int dot = shortName.indexOf('.');
        if (shortName.startsWith("Target ") && dot > 7) {
            try {
                final String unitStr = shortName.substring(7, dot);
                final String objStr = shortName.substring(dot + 1);

                final int unit = Integer.parseInt(unitStr);
                final int obj = Integer.parseInt(objStr);

                result = new int[] {unit, obj};

            } catch (final NumberFormatException ex) {
                Log.warning("Unable to parse unit number from section name: ", shortName, ex);
            }
        } else {
            Log.warning("Unable to determine unit number from section name: ", shortName);
        }

        return result;
    }

    /**
     * Processes finalization of an "OLD" exam.
     *
     * @param cache     the cache
     * @param presented the presented exam
     * @param student   the student record
     * @param isTut     TRUE if the exam is a tutorial exam
     * @param req       the request
     * @param rep       the reply
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    private boolean processOldExam(final Cache cache, final ExamObj presented, final RawStudent student,
                                   final Boolean isTut, final UpdateExamRequest req, final UpdateExamReply rep)
            throws SQLException {

        final EvalContext params = new EvalContext();
        final VariableBoolean param1 = new VariableBoolean("proctored");
        param1.setValue(Boolean.valueOf(req.proctored));
        params.addVariable(param1);

        Log.info("Grading exam for student ", student.stuId, ", exam ", presented.examVersion, ": Proctored=",
                param1.getValue(), ", Remote=" + presented.remote);


        // Get the student's SAT and ACT scores from the database, and store in parameters for use in scoring formulas.
        loadSatActSurvey(cache, params);

        final RawExam exam = RawExamLogic.query(cache, presented.examVersion);
        if (exam == null) {
            Log.warning("Exam not ", presented.examVersion, " not found!");
            return false;
        }

        String exType = exam.examType;
        // FIXME: Make sure 17ELM is recorded as a review exam and not a Q exam
        if ("17ELM".equals(exam.version) || "7TELM".equals(exam.version)) {
            exType = "R";
        }

        final ZonedDateTime now = ZonedDateTime.now();

        // Begin preparing the database object to store exam results
        final StudentExamRec stexam = new StudentExamRec();
        stexam.studentId = student.stuId;
        stexam.examType = exType;
        stexam.course = presented.course;
        try {
            stexam.unit = Integer.valueOf(presented.courseUnit);
        } catch (final NumberFormatException ex) {
            Log.warning("Failed to parse unit", ex);
        }
        stexam.examId = presented.examVersion;
        stexam.proctored = presented.proctored || req.proctored;
        stexam.start = TemporalUtils.toLocalDateTime(presented.realizationTime);
        stexam.finish = TemporalUtils.toLocalDateTime(presented.completionTime);
        if (req.isRecovered()) {
            stexam.recovered = LocalDateTime.now();
        }
        stexam.serialNumber = presented.serialNumber;

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        boolean ok = true;

        if ("CH".equals(exType)) {
            // No action
        } else if (!"Q".equals(exType)) {
            RawStcourse stcourse = RawStcourseLogic.getRegistration(cache, stexam.studentId, stexam.course);

            if (stcourse == null) {
                if (isTut.booleanValue()) {
                    // Create a fake STCOURSE record
                    stcourse = new RawStcourse(active.term, // term
                            stexam.studentId, // stuId
                            stexam.course, // course
                            "1", // sect
                            null, // paceOrder
                            "Y", // openStatus
                            null, // gradingOption,
                            "Y", // completed
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
                            null, // instruction type
                            null, // registration status
                            null, // lastClassRollDt
                            null, // iTermKey
                            null); // iDeadlineDt

                    stcourse.synthetic = true;
                } else {
                    boolean isSpecial = false;

                    // 'TUTOR', 'ADMIN' special student types automatically in section
                    // "001" for 117, 118, 124, 125, 126.
                    if (RawRecordConstants.M117.equals(stexam.course)
                            || RawRecordConstants.M118.equals(stexam.course)
                            || RawRecordConstants.M124.equals(stexam.course)
                            || RawRecordConstants.M125.equals(stexam.course)
                            || RawRecordConstants.M126.equals(stexam.course)
                            || RawRecordConstants.MATH117.equals(stexam.course)
                            || RawRecordConstants.MATH118.equals(stexam.course)
                            || RawRecordConstants.MATH124.equals(stexam.course)
                            || RawRecordConstants.MATH125.equals(stexam.course)
                            || RawRecordConstants.MATH126.equals(stexam.course)) {

                        final List<RawSpecialStus> specials = RawSpecialStusLogic
                                .queryActiveByStudent(cache, student.stuId, now.toLocalDate());

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
                        stcourse = new RawStcourse(active.term, // term
                                stexam.studentId, // stuId
                                stexam.course, // course
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
                                null, // timeputFactor
                                null, // forfeitI
                                "N", // iInProgress
                                null, // iCounted
                                "N", // ctrlTest
                                null, // deferredFDt
                                Integer.valueOf(0), // bypassTimeout
                                null, // instrnType
                                null, // registrationStatus
                                null, // lastClassRollDt
                                null, // iTermKey
                                null); // iDeadlineDt

                        stcourse.synthetic = true;
                    } else {
                        Log.severe("Unable to look up STCOURSE record.\n", req.toString());
                        ok = false;
                    }
                }
            }

            if (ok) {
                final RawCusection cusect = systemData.getCourseUnitSection(stexam.course, stcourse.sect, stexam.unit,
                        active.term);

                if (cusect == null) {
                    ok = false;
                } else if ("R".equals(exType)) {
                    stexam.masteryScore = cusect.reMasteryScore;
                } else if ("U".equals(exType) || "F".equals(exType)) {
                    stexam.masteryScore = cusect.ueMasteryScore;
                }

                // FIXME: Double the mastery score for longer skills review exams
                if (stexam.masteryScore != null && ("17ELM".equals(exam.version) || "7TELM".equals(exam.version))) {

                    // Double scores for the extended exams
                    stexam.masteryScore = Integer.valueOf(stexam.masteryScore.intValue() << 1);
                }

            }
        } else if (RawRecordConstants.M100U.equals(stexam.course)) {
            // FIXME: Hardcode
            final RawCusection result = systemData.getCourseUnitSection(stexam.course, "1", stexam.unit, active.term);
            if (result == null) {
                ok = false;
            } else {
                stexam.masteryScore = result.ueMasteryScore;
            }
        }

        // TODO: Record any resources lent to student (store with exam)

        // Generate the list of survey answers, problem answers, store in exam record
        if (ok) {
            buildSurveyAnswerList(presented, req.getAnswers(), stexam);
            ok = buildAnswerList(presented, req.getAnswers(), stexam);
        }

        // Determine problem and subtest scores, add to the parameter set
        if (ok) {
            ok = computeSubtestScores(presented, stexam, params);
        }

        // Determine grading rule results, and add them to the parameter set for use in outcome
        // processing.
        final boolean[] passed = new boolean[1];
        if (ok) {
            evaluateGradingRules(presented, stexam, params, passed);
            ok = determineOutcomes(cache, presented, stexam, params);
        }

        // We have now assembled the student exam record, so insert into the database.
        if (ok) {
            ok = insertStudentExam(cache, now, stexam, passed[0]);
        }

        if (ok) {
            rep.subtestScores = stexam.subtestScores;
            rep.examGrades = stexam.examGrades;
            rep.missed = stexam.missed;
        }

        return ok;
    }

    /**
     * Retrieve the student"s ACT and SAT scores and relevant survey answers from the database and store them in the
     * parameter set as "student-ACT-math", "student-SAT-math", "hours-preparing", "time-since-last-math", and
     * "highest-math-taken", "resources-used-preparing", and "typical-math-grade". If the values are not populated in
     * the database, the parameters will be added with default values.
     *
     * @param cache  the data cache
     * @param params the parameter set to which to add the parameters
     * @throws SQLException if there is an error accessing the database
     */
    private void loadSatActSurvey(final Cache cache, final EvalContext params) throws SQLException {

        final int act = getStudent().actScore == null ? 0 : getStudent().actScore.intValue();

        final int sat = getStudent().satScore == null ? 0 : getStudent().satScore.intValue();

        final VariableInteger param1 = new VariableInteger("student-ACT-math");
        param1.setValue(Long.valueOf((long) act));
        params.addVariable(param1);

        final VariableInteger param2 = new VariableInteger("student-SAT-math");
        param2.setValue(Long.valueOf((long) sat));
        params.addVariable(param2);

        final List<RawStsurveyqa> answers = RawStsurveyqaLogic.queryLatestByStudentProfile(cache,
                getStudent().stuId, "POOOO");

        int prep = 0;
        int resources = 0;
        int course;
        int taken = 0;
        int since = 6;
        int typical = 9;
        for (final RawStsurveyqa answer : answers) {

            if (answer.surveyNbr == null) {
                continue;
            }

            final int questionNumber = answer.surveyNbr.intValue();
            final String ans = answer.stuAnswer;

            try {
                if (questionNumber == 1) {

                    // Question 1: Hours spent preparing
                    prep = Long.valueOf(ans).intValue();
                } else if (questionNumber == 2) {

                    // Question 2: Resources used
                    resources = Long.valueOf(ans).intValue();
                } else if (questionNumber == 3) {

                    // Question 3: Time since last math course
                    since = Long.valueOf(ans).intValue();
                } else if (questionNumber == 4) {

                    // Question 4: Typical math grade
                    typical = Long.valueOf(ans).intValue();
                } else if (questionNumber >= 5) {

                    // Question 5+: Courses taken (find highest)
                    course = Long.valueOf(ans).intValue();

                    if (course > taken) {
                        taken = course;
                    }
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Failed to parse question answer '", ans, "'");
            }
        }

        final VariableInteger param3 = new VariableInteger("hours-preparing");
        param3.setValue(Long.valueOf((long) prep));
        params.addVariable(param3);

        final VariableInteger param4 = new VariableInteger("time-since-last-math");
        param4.setValue(Long.valueOf((long) since));
        params.addVariable(param4);

        final VariableInteger param5 = new VariableInteger("highest-math-taken");
        param5.setValue(Long.valueOf((long) taken));
        params.addVariable(param5);

        final VariableInteger param6 = new VariableInteger("resources-used-preparing");
        param6.setValue(Long.valueOf((long) resources));
        params.addVariable(param6);

        final VariableInteger param7 = new VariableInteger("typical-math-grade");
        param7.setValue(Long.valueOf((long) typical));
        params.addVariable(param7);
    }

    /**
     * Assemble a list of the student's answers and store them with the exam record that is being prepared for database
     * insertion.
     *
     * @param presented the exam the student is submitting
     * @param answers   the list of the student's answers
     * @param stexam    the exam record that will be inserted into the database
     */
    private static void buildSurveyAnswerList(final ExamObj presented, final Object[][] answers,
                                              final StudentExamRec stexam) {

        final Iterator<ExamSection> sections = presented.sections();

        while (sections.hasNext()) {
            final ExamSection sect = sections.next();

            if (!"survey".equalsIgnoreCase(sect.sectionName)) {
                continue;
            }

            final Iterator<ExamProblem> problems = sect.problems();

            while (problems.hasNext()) {
                final ExamProblem prob = problems.next();

                final StudentSurveyAnswer stanswer = new StudentSurveyAnswer();
                final int id = prob.problemId;

                if (answers[id] != null) {
                    final char[] answerStr = "     ".toCharArray();

                    final int ansLen = answers[id].length;
                    for (int i = 0; i < ansLen; ++i) {

                        if (answers[id][i] instanceof Long) {
                            final int index = ((Long) answers[id][i]).intValue();

                            if (index >= 1 && index <= 5) {
                                answerStr[index - 1] = (char) ('A' + index - 1);
                            }
                        }
                    }

                    stanswer.id = id;
                    stanswer.studentAnswer = String.valueOf(answerStr);

                    final String key = Integer.toString(id / 100) + id / 10 % 10
                            + id % 10;
                    stexam.surveys.put(key, stanswer);
                }
            }
        }
    }

    /**
     * Assemble a list of the student's answers and store them with the exam record that is being prepared for database
     * insertion.
     *
     * @param presented the exam the student is submitting
     * @param answers   the list of the student's answers
     * @param stexam    the exam record that will be inserted into the database
     * @return true if succeeded; false otherwise
     */
    private static boolean buildAnswerList(final ExamObj presented, final Object[][] answers,
                                           final StudentExamRec stexam) {

        final boolean ok = true;

        final Iterator<ExamSubtest> subtests = presented.subtests();

        while (subtests.hasNext()) {
            final ExamSubtest subtest = subtests.next();
            final Iterator<ExamSubtestProblem> problems = subtest.getSubtestProblems();

            while (problems.hasNext()) {
                final ExamSubtestProblem subtestprob = problems.next();
                final int id = subtestprob.problemId;

                final ExamProblem problem = presented.getProblem(id);
                final AbstractProblemTemplate selected =
                        problem == null ? null : problem.getSelectedProblem();

                if (selected != null) {
                    final StudentExamAnswerRec stanswer = new StudentExamAnswerRec();
                    stanswer.id = id;
                    stanswer.subtest = subtest.subtestName;
                    stanswer.treeRef = selected.id;

                    // FIXME: Get actual sub-objective relating to problem
                    stanswer.objective = "0";

                    if (answers[id] != null) {
                        selected.recordAnswer(answers[id]);
                        final char[] answerStr = "     ".toCharArray();

                        final int ansLen = answers[id].length;
                        for (int i = 0; i < ansLen; ++i) {

                            if (answers[id][i] instanceof Long) {
                                final int index = ((Long) answers[id][i]).intValue();

                                if (index >= 1 && index <= 5) {
                                    answerStr[index - 1] = (char) ('A' + index - 1);
                                }
                            }
                        }

                        stanswer.studentAnswer = String.valueOf(answerStr);
                        stanswer.correct = selected.isCorrect(answers[id]);

                        if (stanswer.correct) {
                            stanswer.score = 1.0;
                        } else {
                            stanswer.score = 0.0;
                            stexam.missed.put(Integer.valueOf(id), stanswer.objective);
                        }
                    } else {
                        stexam.missed.put(Integer.valueOf(id), stanswer.objective);
                    }

                    final String key =
                            subtest.subtestName + CoreConstants.DOT + id / 100
                                    + id / 10 % 10 + id % 10;
                    stexam.answers.put(key, stanswer);
                }
            }
        }

        return ok;
    }

    /**
     * Given a particular exam problem and a set of student responses, compute the student's subtest score.
     *
     * @param presented the presented exam
     * @param stexam    the student exam record being populated
     * @param params    the parameter set to which to add the subtest score parameters
     * @return true if succeeded; false otherwise
     */
    private static boolean computeSubtestScores(final ExamObj presented,
                                                final StudentExamRec stexam, final EvalContext params) {

        final boolean ok = true;

        final Iterator<ExamSubtest> subtests = presented.subtests();

        while (subtests.hasNext()) {
            double score = 0.0;
            final ExamSubtest subtest = subtests.next();

            final Iterator<ExamSubtestProblem> problems = subtest.getSubtestProblems();

            while (problems.hasNext()) {
                final ExamSubtestProblem problem = problems.next();
                final int id = problem.problemId;
                final String key = subtest.subtestName + CoreConstants.DOT + id / 100 + id / 10 % 10 + id % 10;
                final StudentExamAnswerRec answer = stexam.answers.get(key);

                if (answer != null && answer.correct) {
                    score += answer.score * problem.weight;
                }
            }

            subtest.score = Double.valueOf(score);

            // Store the subtest score in the exam record
            stexam.subtestScores.put(subtest.subtestName,
                    Integer.valueOf((int) subtest.score.doubleValue()));
            Log.info("  Subtest '", subtest.subtestName,
                    "' score ", subtest.score);

            final VariableReal param = new VariableReal(subtest.subtestName);
            param.setValue(Double.valueOf(score));
            params.addVariable(param);
        }

        return ok;
    }

    /**
     * Evaluate the formulae for grading rules based on subtest scores.
     *
     * @param presented the presented exam
     * @param stexam    the student exam record being populated
     * @param params    the parameter set to which to add the subtest score parameters
     * @param passed    an array of one boolean to be populated with {@code true} if there is a "passed" rule that
     *                  evaluates to true
     */
    private static void evaluateGradingRules(final ExamObj presented, final StudentExamRec stexam,
                                             final EvalContext params, final boolean[] passed) {

        // If we have a "score" subtest, and we have a mastery score in the record, then
        // automatically create a "passed" grading rule. Then, as we go through, if there is
        // another explicit "passed" grading rule, it will override this one.
        if (stexam.masteryScore != null) {
            final Integer score = stexam.subtestScores.get("score");

            if (score != null) {
                final VariableBoolean param = new VariableBoolean("passed");

                if (score.intValue() >= stexam.masteryScore.intValue()) {
                    param.setValue(Boolean.TRUE);
                } else {
                    param.setValue(Boolean.FALSE);
                }

                params.addVariable(param);
                stexam.examGrades.put("passed", param.getValue());

                Log.info("  Passed = '", param.getValue(), "'");
            }
        }

        final Iterator<ExamGradingRule> rules = presented.gradingRules();

        while (rules.hasNext()) {
            final ExamGradingRule rule = rules.next();
            final Iterator<ExamGradingCondition> conditions = rule.getGradingConditions();

            boolean pass = false;

            while (conditions.hasNext()) {
                final ExamGradingCondition condition = conditions.next();
                final Formula formula = condition.gradingConditionFormula;
                final Object result = formula.evaluate(params);

                if (result instanceof ErrorValue) {
                    rule.result = result;
                    Log.severe("Error evaluating grading rule ", rule.gradingRuleName, " [", formula.toString(),
                            "]: ", result.toString(), "\n", presented.toXmlString(0));
                    break;
                } else // Insert TRUE boolean parameter if result is PASS
                    if (ExamGradingRule.PASS_FAIL.equals(rule.getGradingRuleType())
                            && result instanceof final Boolean boolResult) {
                        pass = boolResult.booleanValue();

                        if (pass) {
                            rule.result = Boolean.TRUE;
                            stexam.examGrades.put(rule.gradingRuleName, Boolean.TRUE);

                            final VariableBoolean param = new VariableBoolean(rule.gradingRuleName);
                            param.setValue(Boolean.TRUE);
                            params.addVariable(param);

                            break;
                        }
                    }
            }

            // If no passing indication, record a fail
            if (ExamGradingRule.PASS_FAIL.equals(rule.getGradingRuleType()) && !pass) {
                rule.result = Boolean.FALSE;

                Log.info("  Grading Rule '", rule.gradingRuleName,
                        "' : pass = false");

                stexam.examGrades.put(rule.gradingRuleName, Boolean.FALSE);
                final VariableBoolean param = new VariableBoolean(rule.gradingRuleName);
                param.setValue(Boolean.FALSE);
                params.addVariable(param);
            }
        }

        final AbstractVariable result = params.getVariable("passed");

        if (result != null && result.getValue() instanceof Boolean) {
            passed[0] = ((Boolean) result.getValue()).booleanValue();
        } else {
            passed[0] = false;
        }
    }

    /**
     * Evaluate the formulae for grading rules based on subtest scores.
     *
     * @param cache     the data cache
     * @param presented the presented exam
     * @param stexam    the student exam record being populated
     * @param params    the parameter set to which to add the subtest score parameters
     * @return true if succeeded; false otherwise
     * @throws SQLException if there is an error accessing the database
     */
    private boolean determineOutcomes(final Cache cache, final ExamObj presented, final StudentExamRec stexam,
                                      final EvalContext params) throws SQLException {

        final boolean ok = true;

        final boolean proctored;

        if (getTestingCenter() != null && "Y".equals(getTestingCenter().isProctored)) {
            proctored = true;
        } else {
            proctored = stexam.proctored;
        }

        Log.info("  Proctored = " + proctored);

        final Iterator<ExamOutcome> outcomes = presented.examOutcomes();

        String validBy = null;

        while (outcomes.hasNext()) {
            String whyDeny = null;
            String howValid = null;

            final ExamOutcome outcome = outcomes.next();
            Formula formula = outcome.condition;

            if (formula != null) {
                Object result = formula.evaluate(params);

                if (result instanceof Boolean) {

                    final boolean doOutcome = ((Boolean) result).booleanValue();

                    Log.info("  Outcome '", outcome.condition.toString(), "' : ", result);

                    // See if the outcome should be awarded based on grading rules
                    if (doOutcome) {

                        // Test for needed prerequisites; if any are not satisfied deny the outcome
                        final Iterator<ExamOutcomePrereq> prereqs = outcome.getPrereqs();

                        while (prereqs.hasNext() && whyDeny == null) {
                            final ExamOutcomePrereq prereq = prereqs.next();
                            formula = prereq.prerequisiteFormula;

                            if (formula != null) {
                                result = formula.evaluate(params);

                                if (result instanceof Boolean) {
                                    if (!((Boolean) result).booleanValue()) {
                                        whyDeny = RawMpecrDeniedLogic.DENIED_BY_PREREQ;
                                        Log.info("  Denying outcome due to prereqs");
                                    }
                                } else {
                                    whyDeny = RawMpecrDeniedLogic.DENIED_BY_PREREQ;

                                    Log.severe("Outcome prerequisite evaluated to ", result.toString(), "\n",
                                            presented.toXmlString(0));
                                }
                            } else {
                                Log.severe("Outcome prerequisite has no formula\n", presented.toXmlString(0));
                            }
                        }

                        // Next, we test validation rules
                        if (whyDeny == null) {
                            final Iterator<ExamOutcomeValidation> valids = outcome.getValidations();

                            while (valids.hasNext() && howValid == null) {
                                final ExamOutcomeValidation valid = valids.next();
                                formula = valid.validationFormula;

                                if (formula != null) {
                                    result = formula.evaluate(params);

                                    if (result instanceof Boolean) {

                                        if (((Boolean) result).booleanValue()) {
                                            howValid = valid.howValidated;
                                            validBy = howValid;
                                            Log.info("  Outcome is validated : ", howValid);
                                        }
                                    } else {
                                        Log.severe("Validation formula evaluated to ", result.toString(), "\n",
                                                presented.toXmlString(0));
                                    }
                                } else {
                                    Log.severe("Outcome validation has no formula\n", presented.toXmlString(0));
                                }
                            }

                            if (howValid == null) {
                                whyDeny = RawMpecrDeniedLogic.DENIED_BY_VAL;
                                Log.info("  Outcome is denied by validation rule");
                            }
                        }

                        // Award (or deny) the outcome.
                        final Iterator<ExamOutcomeAction> actions = outcome.getActions();

                        while (actions.hasNext()) {
                            final ExamOutcomeAction action = actions.next();

                            if (ExamOutcomeAction.INDICATE_PLACEMENT.equals(action.type)) {

                                Log.info("  Outcome action: INDICATE_PLACEMENT in ", action.course, " (", whyDeny, ")");

                                // Award or deny placement
                                if (whyDeny == null) {

                                    stexam.earnedPlacement.add(action.course);
                                } else if (RawMpecrDeniedLogic.DENIED_BY_VAL.equals(whyDeny)) {

                                    // FIX PER CONVERSATION WITH KEN JAN 12 2017:
                                    // Go ahead and award the result (with "U" as the how-valid
                                    // field) but keep result in "denied" for record-keeping

                                    stexam.earnedPlacement.add(action.course);
                                    if (outcome.logDenial && !stexam.deniedPlacement.containsKey(action.course)) {

                                        stexam.deniedPlacement.put(action.course, whyDeny);
                                    }
                                    validBy = "U";

                                } else if (outcome.logDenial && !stexam.deniedPlacement.containsKey(action.course)) {

                                    stexam.deniedPlacement.put(action.course, whyDeny);
                                }
                            } else if (ExamOutcomeAction.INDICATE_CREDIT.equals(action.type)) {

                                if (!proctored) {
                                    Log.warning("  * Denying credit because not proctored - ", stexam.examId);
                                    whyDeny = RawMpecrDeniedLogic.DENIED_BY_VAL;
                                }

                                Log.info("  Outcome action: INDICATE_CREDIT in ", action.course, " (", whyDeny, ")");

                                // Award or deny credit
                                if (whyDeny == null) {

                                    stexam.earnedCredit.add(action.course);

                                } else if (RawMpecrDeniedLogic.DENIED_BY_VAL.equals(whyDeny) && proctored) {
                                    stexam.earnedCredit.add(action.course);
                                    if (outcome.logDenial && !stexam.deniedCredit.containsKey(action.course)) {
                                        stexam.deniedCredit.put(action.course, whyDeny);
                                    }
                                    validBy = "U";
                                } else if (outcome.logDenial && !stexam.deniedCredit.containsKey(action.course)) {

                                    stexam.deniedCredit.put(action.course, whyDeny);
                                }
                            } else if (ExamOutcomeAction.INDICATE_LICENSED.equals(action.type)) {

                                Log.info("  Outcome action: INDICATE_LICENSED");

                                if ("N".equals(getStudent().licensed)) {
                                    RawStudentLogic.updateLicensed(cache, getStudent().stuId, "Y");
                                }
                            }
                        }
                    }
                } else if (result instanceof ErrorValue) {
                    Log.warning("Error evaluating outcome formula [", outcome.condition.toString(), "]: ",
                            result.toString(), ", ", presented.toXmlString(0));
                } else {
                    Log.warning("Outcome formula [", outcome.condition.toString(), "] did not evaluate to boolean:",
                            presented.toXmlString(0));
                }
            } else {
                Log.warning("Outcome has no formula:", presented.toXmlString(0));
            }
        }

        if (validBy != null) {
            stexam.howValidated = validBy.charAt(0);
        }

        return ok;
    }

    /**
     * Insert this object into the database.
     *
     * @param cache       the data cache
     * @param now         the date/time to consider as "now"
     * @param stexam      the StudentExam object with exam data to be inserted
     * @param usersPassed {@code true} if the score was passing; {@code false} if not (used only for user's exam)
     * @return true if object inserted, false if an error occurred
     * @throws SQLException if there is an error accessing the database
     */
    private boolean insertStudentExam(final Cache cache, final ZonedDateTime now, final StudentExamRec stexam,
                                      final boolean usersPassed) throws SQLException {

        // FIXME: Here, we apply some logic to choose the table to insert into, since placement and
        // challenge exam scores are stored separately from others.

        Log.info("Inserting record of exam ", stexam.examId, " in course ", stexam.course, " for student ",
                stexam.studentId);

        if (!stexam.surveys.isEmpty()) {
            insertSurveyAnswers(cache, stexam);
        }

        boolean rc;
        if (RawRecordConstants.M100P.equals(stexam.course)) {
            rc = insertPlacement(cache, now, stexam);
        } else if (ChallengeExamLogic.M117_CHALLENGE_EXAM_ID.equals(stexam.examId)
                || ChallengeExamLogic.M118_CHALLENGE_EXAM_ID.equals(stexam.examId)
                || ChallengeExamLogic.M124_CHALLENGE_EXAM_ID.equals(stexam.examId)
                || ChallengeExamLogic.M125_CHALLENGE_EXAM_ID.equals(stexam.examId)
                || ChallengeExamLogic.M126_CHALLENGE_EXAM_ID.equals(stexam.examId)) {
            rc = insertChallenge(cache, now, stexam);
        } else if (RawRecordConstants.M100T.equals(stexam.course)
                || RawRecordConstants.M1170.equals(stexam.course)
                || RawRecordConstants.M1180.equals(stexam.course)
                || RawRecordConstants.M1240.equals(stexam.course)
                || RawRecordConstants.M1250.equals(stexam.course)
                || RawRecordConstants.M1260.equals(stexam.course)) {
            rc = insertExam(cache, stexam);

            if (rc) {
                rc = insertPlacementResults(cache, stexam);
            }
        } else {
            if (RawRecordConstants.M100U.equals(stexam.course)) {
                insertUsersExam(cache, stexam, usersPassed);
            }
            rc = insertExam(cache, stexam);
        }

        return rc;
    }

    /**
     * Insert a placement exam object into the database.
     *
     * @param cache  the data cache
     * @param stexam the StudentExam object with exam data to be inserted
     * @throws SQLException if there is an error accessing the database
     */
    private static void insertSurveyAnswers(final Cache cache, final StudentExamRec stexam) throws SQLException {

        // Get the existing set of answers
        final List<RawStsurveyqa> answers = RawStsurveyqaLogic.queryLatestByStudentProfile(cache, stexam.studentId,
                stexam.examId);

        // Loop through new answers, updating any found to have changed, and inserting any that
        // are not already existing.
        for (final StudentSurveyAnswer survey : stexam.surveys.values()) {
            boolean searching = true;

            final int numAns = answers.size();
            for (int i = 0; i < numAns; ++i) {

                final RawStsurveyqa answer = answers.get(i);
                if (answer.surveyNbr != null && answer.surveyNbr.intValue() == survey.id) {

                    if (answer.stuAnswer == null) {

                        if (survey.studentAnswer != null) {
                            final RawStsurveyqa updated = new RawStsurveyqa(answer.stuId, answer.version,
                                    stexam.finish.toLocalDate(), answer.surveyNbr, survey.studentAnswer,
                                    Integer.valueOf(TemporalUtils.minuteOfDay(stexam.finish)));
                            RawStsurveyqaLogic.INSTANCE.insert(cache, updated);
                            answers.set(i, updated);
                        }
                    } else if (!answer.stuAnswer.equals(survey.studentAnswer)) {

                        final RawStsurveyqa updated = new RawStsurveyqa(answer.stuId, answer.version,
                                stexam.finish.toLocalDate(), answer.surveyNbr, survey.studentAnswer,
                                Integer.valueOf(TemporalUtils.minuteOfDay(stexam.finish)));
                        RawStsurveyqaLogic.INSTANCE.insert(cache, updated);
                        answers.set(i, updated);
                    }

                    searching = false;
                }
            }

            if (searching) {
                final RawStsurveyqa answer = new RawStsurveyqa(stexam.studentId, stexam.examId,
                        stexam.finish.toLocalDate(), Integer.valueOf(survey.id), survey.studentAnswer,
                        Integer.valueOf(TemporalUtils.minuteOfDay(stexam.finish)));
                RawStsurveyqaLogic.INSTANCE.insert(cache, answer);
            }
        }

    }

    /**
     * Insert a placement exam object into the database.
     *
     * @param cache  the data cache
     * @param now    the date/time to consider as "now"
     * @param stexam the StudentExam object with exam data to be inserted
     * @return true if object inserted, false if an error occurred
     * @throws SQLException if there is an error accessing the database
     * @throws SQLException if there is an error accessing the database
     */
    private boolean insertPlacement(final Cache cache, final ZonedDateTime now,
                                    final StudentExamRec stexam) throws SQLException {

        final int[] attempts = RawStmpeLogic.countLegalAttempts(cache, stexam.studentId, stexam.examId);

        // Verify that number of attempts is OK, based on the online/proctored status of this
        // attempt. We also disallow online attempts after any proctored attempts have been made.
        if (getClient() != null && getClient().testingCenterId != null) {
            setTestingCenter(RawTestingCenterLogic.query(cache, getClient().testingCenterId));
        }

        final boolean proctored;

        if (getTestingCenter() != null && "Y".equals(getTestingCenter().isProctored)) {
            proctored = true;
        } else {
            proctored = stexam.proctored;
        }

        boolean deny = false;
        if (proctored) {
            if (attempts[1] >= 2) {
                Log.info("Max proctored attempts already used, denying");
                deny = true;
            }
        } else if (attempts[0] >= 1) {
            Log.info("Max unproctored attempts already used, denying");
            deny = true;
        }

        if (deny) {
            // Attempt was not legal; deny all placement & credit awards. Reason for denial now becomes 'I'.
            stexam.howValidated = ' ';
            stexam.deniedPlacement.replaceAll((s, v) -> "I");
            stexam.deniedCredit.replaceAll((s, v) -> "I");
            for (final String s : stexam.earnedPlacement) {
                stexam.deniedPlacement.put(s, "I");
            }
            for (final String s : stexam.earnedCredit) {
                stexam.deniedCredit.put(s, "I");
            }
            stexam.earnedPlacement.clear();
            stexam.earnedCredit.clear();

            // Since we have detected an illegal placement exam attempt, we place a hold 18
            // on the student account. TODO: Fix hardcode
            RawAdminHold hold = RawAdminHoldLogic.query(cache, stexam.studentId, "18");

            if (hold == null) {
                // No hold, so create a new one

                hold = new RawAdminHold(stexam.studentId, "18", "F", Integer.valueOf(0), LocalDate.now());

                // Now that we've inserted a fatal hold, update the student record to
                // indicate a fatal hold exists. TODO: Fix hardcode
                if (RawAdminHoldLogic.INSTANCE.insert(cache, hold) && !"F".equals(getStudent().sevAdminHold)) {
                    RawStudentLogic.updateHoldSeverity(cache, getStudent().stuId, "F");
                }
            } else {
                // Already a hold 18, but update its date to now
                hold = new RawAdminHold(stexam.studentId, "18", "F", Integer.valueOf(0), LocalDate.now());
                RawAdminHoldLogic.updateAdminHoldDate(cache, hold);
            }
        }

        // Finally, we insert the placement attempt record.
        if (stexam.finish == null) {
            stexam.finish = now.toLocalDateTime();
        }

        final TermRec active = cache.getSystemData().getActiveTerm();

        boolean placed = false;
        final String result;
        if (!stexam.earnedCredit.isEmpty() || !stexam.earnedPlacement.isEmpty()) {

            if (!deny) {
                result = "Y";
                placed = true;
            } else {
                // Illegal attempt, so store attempt number
                result = Integer.toString(attempts[0] + attempts[1] + 1);
            }
        } else {
            result = "N";
        }

        String howValidated = null;
        if (proctored) {
            howValidated = "P";
        } else if (placed && stexam.howValidated != ' ') {
            howValidated = Character.toString(stexam.howValidated);
        }

        final RawStmpe attempt = new RawStmpe(stexam.studentId, stexam.examId, active.academicYear,
                stexam.finish.toLocalDate(), Integer.valueOf(TemporalUtils.minuteOfDay(stexam.start)),
                Integer.valueOf(TemporalUtils.minuteOfDay(stexam.finish)), getStudent().lastName,
                getStudent().firstName, getStudent().middleInitial, null, stexam.serialNumber,
                stexam.subtestScores.get("A"),
                stexam.subtestScores.get("117"),
                stexam.subtestScores.get("118"),
                stexam.subtestScores.get("124"),
                stexam.subtestScores.get("125"),
                stexam.subtestScores.get("126"), result, howValidated);

        final Iterator<StudentExamAnswerRec> answers = stexam.answers.values().iterator();

        boolean ok = true;
        while (ok && answers.hasNext()) {
            final StudentExamAnswerRec ansrec = answers.next();

            final RawStmpeqa answer =
                    new RawStmpeqa(stexam.studentId, stexam.examId, stexam.finish.toLocalDate(),
                            Integer.valueOf(TemporalUtils.minuteOfDay(stexam.finish)),
                            Integer.valueOf(ansrec.id), ansrec.studentAnswer, ansrec.correct ? "Y" : "N",
                            ansrec.subtest, ansrec.treeRef);

            ok = RawStmpeqaLogic.INSTANCE.insert(cache, answer);
        }

        if (ok) {
            final LocalDateTime start = stexam.start;
            final int startTime = start.getHour() * 60 + start.getMinute();

            RawMpeLogLogic.indicateFinished(cache, stexam.studentId, start.toLocalDate(),
                    Integer.valueOf(startTime), stexam.finish.toLocalDate(),
                    stexam.recovered == null ? null : stexam.recovered.toLocalDate());
        }

        ok = insertPlacementResults(cache, stexam);

        // Last thing is to insert the actual STMPE row. We do this last so other jobs can know
        // that if they see a row in this table, the associated data will be present and complete.
        if (ok) {
            ok = RawStmpeLogic.INSTANCE.insert(cache, attempt);

            // Clear the indication that the student has checked their results...
            RawStmathplanLogic.deleteAllForPage(cache, attempt.stuId, "WLCM7");
        }

        return ok;
    }

    /**
     * Insert a placement exam object into the database.
     *
     * @param cache  the data cache
     * @param stexam the StudentExam object with exam data to be inserted
     * @return true if object inserted, false if an error occurred
     * @throws SQLException if there is an error accessing the database
     */
    private boolean insertPlacementResults(final Cache cache, final StudentExamRec stexam) throws SQLException {

        // Indicate all required placements.
        final Iterator<String> iter1 = stexam.earnedPlacement.iterator();

        while (iter1.hasNext()) {
            final String placeIn = iter1.next();

            if (stexam.earnedCredit.contains(placeIn)) {
                // If credit is awarded, we don't award placement too
                continue;
            }

            String source = null;
            if (stexam.proctored) {
                if (getMachineId() == null) {
                    // Exam was taken at a remote station, but proctored
                    source = "RM";
                } else {
                    // Exam was taken in the testing center
                    source = "TC";
                }
            }

            final RawMpeCredit credit = new RawMpeCredit(stexam.studentId, placeIn, "P", stexam.finish.toLocalDate(),
                    null, stexam.serialNumber, stexam.examId, source);

            RawMpeCreditLogic.INSTANCE.apply(cache, credit);
        }

        // Indicate all earned credit.
        for (final String placeIn : stexam.earnedCredit) {
            String source = null;
            if (stexam.proctored) {
                if (getMachineId() == null) {
                    // Exam was taken at a remote station, but proctored
                    source = "RM";
                } else {
                    // Exam was taken in the testing center
                    source = "TC";
                }
            }

            final RawMpeCredit credit = new RawMpeCredit(stexam.studentId, placeIn, "C", stexam.finish.toLocalDate(),
                    null, stexam.serialNumber, stexam.examId, source);

            RawMpeCreditLogic.INSTANCE.apply(cache, credit);
        }

        // Record all ignored credit results
        for (String placeIn : stexam.deniedCredit.keySet()) {
            String source = null;
            if (stexam.proctored) {
                if (getMachineId() == null) {
                    // Exam was taken at a remote station, but proctored
                    source = "RM";
                } else {
                    // Exam was taken in the testing center
                    source = "TC";
                }
            }

            final RawMpecrDenied denied = new RawMpecrDenied(stexam.studentId, placeIn, "C",
                    stexam.finish.toLocalDate(), stexam.deniedCredit.get(placeIn), stexam.serialNumber, stexam.examId,
                    source);

            RawMpecrDeniedLogic.INSTANCE.insert(cache, denied);
        }

        // Record all ignored placement results
        final Iterator<String> iter4 = stexam.deniedPlacement.keySet().iterator();

        while (iter4.hasNext()) {
            final String placeIn = iter4.next();

            String source = null;
            if (stexam.proctored) {
                if (getMachineId() == null) {
                    // Exam was taken at a remote station, but proctored
                    source = "RM";
                } else {
                    // Exam was taken in the testing center
                    source = "TC";
                }
            }

            final RawMpecrDenied denied = new RawMpecrDenied(stexam.studentId, placeIn, //
                    "P", stexam.finish.toLocalDate(),
                    stexam.deniedPlacement.get(placeIn), stexam.serialNumber, stexam.examId, source);

            RawMpecrDeniedLogic.INSTANCE.insert(cache, denied);
        }

        // Send results to BANNER, or store in queue table
        final RawStudent stu = getStudent();

        if (stu == null) {
            RawMpscorequeueLogic.logActivity("Unable to upload placement result for student " + stexam.studentId
                    + ": student record not found");
        } else {
            final DbContext liveCtx = this.dbProfile.getDbContext(ESchemaUse.LIVE);
            final DbConnection liveConn = liveCtx.checkOutConnection();

            try {
                if (RawRecordConstants.M100P.equals(stexam.course)) {
                    RawMpscorequeueLogic.INSTANCE.postPlacementToolResult(cache, liveConn, stu.pidm,
                            new ArrayList<>(stexam.earnedPlacement), stexam.finish);
                } else if (RawRecordConstants.M100T.equals(stexam.course)) {
                    if (stexam.earnedPlacement.contains(RawRecordConstants.M100C)) {
                        RawMpscorequeueLogic.INSTANCE.postELMTutorialResult(cache, liveConn, stu.pidm, stexam.finish);
                    }
                } else {
                    String course = null;
                    if (RawRecordConstants.M1170.equals(stexam.course)) {
                        course = RawRecordConstants.M117;
                    } else if (RawRecordConstants.M1180.equals(stexam.course)) {
                        course = RawRecordConstants.M118;
                    } else if (RawRecordConstants.M1240.equals(stexam.course)) {
                        course = RawRecordConstants.M124;
                    } else if (RawRecordConstants.M1250.equals(stexam.course)) {
                        course = RawRecordConstants.M125;
                    } else if (RawRecordConstants.M1260.equals(stexam.course)) {
                        course = RawRecordConstants.M126;
                    }

                    if (course != null && stexam.earnedPlacement.contains(course)) {
                        RawMpscorequeueLogic.INSTANCE.postPrecalcTutorialResult(cache, liveConn, stu.pidm, course,
                                stexam.finish);
                    }

                    for (final String credit : stexam.earnedCredit) {
                        RawMpscorequeueLogic.INSTANCE.postChallengeCredit(cache, liveConn, stu.pidm, credit,
                                stexam.finish);
                    }
                }
            } finally {
                liveCtx.checkInConnection(liveConn);
            }
        }

        return true;
    }

    /**
     * Insert a challenge exam object into the database.
     *
     * @param cache  the data cache
     * @param now    the date/time to consider as "now"
     * @param stexam the StudentExam object with exam data to be inserted
     * @return true if object inserted, false if an error occurred
     * @throws SQLException if there is an error accessing the database
     */
    private boolean insertChallenge(final Cache cache, final ZonedDateTime now,
                                    final StudentExamRec stexam) throws SQLException {

        final int attempts = RawStchallengeLogic.countLegalAttempts(cache, stexam.studentId, stexam.course);

        // Verify that number of attempts is OK, based on the online/proctored status of this
        // attempt. We also disallow online attempts after any proctored attempts have been made.
        if (getClient() != null && getClient().testingCenterId != null) {
            setTestingCenter(RawTestingCenterLogic.query(cache, getClient().testingCenterId));
        }

        final boolean proctored;
        if (getTestingCenter() != null && "Y".equals(getTestingCenter().isProctored)) {
            proctored = true;
        } else {
            proctored = stexam.proctored;
        }

        boolean deny = false;
        if (!proctored) {
            Log.info("Challenge exam may not be unproctored, denying");
            deny = true;
        } else if (attempts >= 1) {
            Log.info("Challenge exam already taken, denying");
            deny = true;
        }

        if (deny) {
            // Attempt was not legal; deny all placement & credit awards. Reason for denial now becomes 'I'.
            stexam.howValidated = ' ';
            stexam.deniedPlacement.replaceAll((s, v) -> "I");
            stexam.deniedCredit.replaceAll((s, v) -> "I");
            for (final String s : stexam.earnedPlacement) {
                stexam.deniedPlacement.put(s, "I");
            }
            for (final String s : stexam.earnedCredit) {
                stexam.deniedCredit.put(s, "I");
            }

            stexam.earnedPlacement.clear();
            stexam.earnedCredit.clear();

            // Since we have detected an illegal challenge exam attempt, we place a hold 18
            // on the
            // student account. TODO: Fix hardcode
            RawAdminHold hold = RawAdminHoldLogic.query(cache, stexam.studentId, "18");

            if (hold == null) {
                // No hold, so create a new one
                hold = new RawAdminHold(stexam.studentId, "18", "F", Integer.valueOf(0), LocalDate.now());

                // Now that we've inserted a fatal hold, update the student record to
                // indicate a fatal hold exists. TODO: Fix hardcode
                if (RawAdminHoldLogic.INSTANCE.insert(cache, hold) && !"F".equals(getStudent().sevAdminHold)) {
                    RawStudentLogic.updateHoldSeverity(cache, getStudent().stuId, "F");
                }
            } else {
                // Already a hold 18, but update its date to now
                hold = new RawAdminHold(stexam.studentId, "18", "F", Integer.valueOf(0), LocalDate.now());

                RawAdminHoldLogic.updateAdminHoldDate(cache, hold);
            }
        }

        // Finally, we insert the challenge attempt record.
        if (stexam.finish == null) {
            stexam.finish = now.toLocalDateTime();
        }

        final String passed;

        if (stexam.earnedCredit.isEmpty()) {
            passed = "N";
        } else if (deny) {
            // Illegal attempt, so store attempt number
            passed = Integer.toString(attempts + 1);
        } else {
            passed = "Y";
        }

        String howVal = null;
        if (proctored) {
            howVal = "P";
        }

        final TermRec active = cache.getSystemData().getActiveTerm();

        final Integer startTime;
        final Integer finishTime;

        if (stexam.start == null) {
            startTime = null;
        } else {
            startTime = Integer.valueOf(stexam.start.getHour() * 60 + stexam.start.getMinute());
        }

        final LocalDate finishDate;
        if (stexam.finish == null) {
            finishTime = null;
            finishDate = null;
        } else {
            finishTime = Integer.valueOf(stexam.finish.getHour() * 60 + stexam.finish.getMinute());
            finishDate = stexam.finish.toLocalDate();
        }

        final RawStchallenge attempt = new RawStchallenge(stexam.studentId, stexam.course,
                stexam.examId, active.academicYear, finishDate, startTime, finishTime,
                getStudent().lastName, getStudent().firstName, getStudent().middleInitial, null,
                stexam.serialNumber, stexam.subtestScores.get("score"),
                passed, howVal);

        final Iterator<StudentExamAnswerRec> answers = stexam.answers.values().iterator();

        boolean ok = true;
        while (ok && answers.hasNext()) {
            final StudentExamAnswerRec ansrec = answers.next();

            final RawStchallengeqa answer = new RawStchallengeqa(stexam.studentId, stexam.course,
                    stexam.examId, stexam.finish.toLocalDate(), finishTime, Integer.valueOf(ansrec.id),
                    ansrec.studentAnswer, //
                    ansrec.correct ? "Y" : "N");

            ok = RawStchallengeqaLogic.INSTANCE.insert(cache, answer);
        }

        if (ok) {
            ok = insertPlacementResults(cache, stexam);
        }

        // Last thing is to insert the actual STMPE row. We do this last so other jobs can know
        // that if they see a row in this table, the associated data will be present and complete.
        if (ok) {
            ok = RawStchallengeLogic.INSTANCE.insert(cache, attempt);
        }

        return ok;
    }

    /**
     * Insert a standard (non-placement) exam object into the database.
     *
     * @param cache  the data cache
     * @param stexam the StudentExam object with exam data to be inserted
     * @return true if object inserted, false if an error occurred
     * @throws SQLException if there is an error accessing the database
     */
    private boolean insertExam(final Cache cache, final StudentExamRec stexam) throws SQLException {

        final Object passedVal = stexam.examGrades.get("passed");

        final String passed;
        if (stexam.serialNumber != null && stexam.serialNumber.intValue() < 0) {
            passed = "C";
        } else if (passedVal instanceof Boolean) {
            passed = Boolean.TRUE.equals(passedVal) ? "Y" : "N";
        } else {
            passed = "N";
        }

        String source = null;
        if (stexam.proctored) {
            if (getMachineId() == null) {
                // Exam was taken at a remote station, but proctored
                source = "RM";
            } else {
                // Exam was taken in the testing center
                source = "TC";
            }
        }

        final LocalDateTime start = stexam.start;
        final LocalDateTime fin = stexam.finish;
        final int startInt = start.getHour() * 60 + start.getMinute();
        final int finInt = fin.getHour() * 60 + fin.getMinute();

        final RawStexam exam = new RawStexam(stexam.serialNumber, stexam.examId, stexam.studentId,
                fin.toLocalDate(), stexam.subtestScores.get("score"), stexam.masteryScore,
                Integer.valueOf(startInt), Integer.valueOf(finInt), "Y", passed, null,
                stexam.course, stexam.unit, stexam.examType, "N", source, null);

        final boolean ok = RawStexamLogic.INSTANCE.insert(cache, exam);

        if (ok) {
            RawStexamLogic.recalculateFirstPassed(cache, stexam.studentId, stexam.course,
                    stexam.unit, stexam.examType);

            // Loop through answers, inserting records.
            int question = 1;
            for (final StudentExamAnswerRec ansrec : stexam.answers.values()) {
                final RawStqa answer = new RawStqa(stexam.serialNumber, Integer.valueOf(question), Integer.valueOf(1),
                        ansrec.objective, ansrec.studentAnswer, stexam.studentId, stexam.examId, //
                        ansrec.correct ? "Y" : "N", fin.toLocalDate(), ansrec.subtest, Integer.valueOf(finInt));

                RawStqaLogic.INSTANCE.insert(cache, answer);
                ++question;
            }
        }

        if (("F".equals(stexam.examType) || "U".equals(stexam.examType))
                && Boolean.TRUE.equals(passedVal) && stexam.unit != null) {
            checkForCourseCompleted(cache, stexam.studentId, stexam.course);
        }

        return ok;
    }

    /**
     * Insert a users exam object into the database.
     *
     * @param cache  the data cache
     * @param stexam the StudentExam object with exam data to be inserted
     * @param passed {@code true} if the score was passing; {@code false} if not
     * @return true if object inserted, false if an error occurred
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean insertUsersExam(final Cache cache, final StudentExamRec stexam,
                                           final boolean passed) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();

        final RawStudent stu = RawStudentLogic.query(cache, stexam.studentId, false);
        if (stu == null) {
            Log.warning("User's exam for student ", stexam.studentId, ", student not found");
            return false;
        }

        // Look up the calculus section reported
        // FIXME: Hardcode question number
        String calc = CoreConstants.SPC;

        final List<RawStsurveyqa> resp = RawStsurveyqaLogic.queryLatestByStudentProfile(cache, stexam.studentId,
                "UOOOO");

        String answer = null;
        for (final RawStsurveyqa rawStsurveyqa : resp) {
            if (rawStsurveyqa.surveyNbr.intValue() == 6) {
                answer = rawStsurveyqa.stuAnswer;
                break;
            }
        }

        if (answer != null) {
            final List<RawSurveyqa> possible = RawSurveyqaLogic.queryByVersionAndQuestion(cache, "UOOOO",
                    Integer.valueOf(1));

            for (final RawSurveyqa rawSurveyqa : possible) {
                if (answer.equals(rawSurveyqa.answer)) {
                    calc = rawSurveyqa.answerMeaning;
                    RawStudentLogic.updateCourseOrder(cache, stu.stuId, calc);
                    break;
                }
            }
        }

        final RawUsers attempt = new RawUsers(active.term, stexam.studentId, stexam.serialNumber,
                stexam.examId, stexam.finish.toLocalDate(), stexam.subtestScores.get("score"),
                calc, passed ? "Y" : "N");

        return RawUsersLogic.INSTANCE.insert(cache, attempt);
    }

    /**
     * Called when a passing unit or final exam score is received, this tests whether we can mark the course as
     * complete. To be complete, a course must have passing scores on file for all units (including final), and the sum
     * of the highest scores in each unit must be at least the lowest passing score defined for the course section.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @param courseId  the ID of the course to test
     * @throws SQLException if there is an error accessing the database
     */
    private void checkForCourseCompleted(final Cache cache, final String studentId,
                                         final String courseId) throws SQLException {

        Log.info("  Testing for completion of ", courseId, " by student ", getStudent().stuId);

        final LiveSessionInfo live = new LiveSessionInfo(
                CoreConstants.newId(ISessionManager.SESSION_ID_LEN), "None", ERole.STUDENT);
        live.setUserInfo(getStudent().stuId, getStudent().firstName, getStudent().lastName,
                getStudent().getScreenName());

        final ImmutableSessionInfo session = new ImmutableSessionInfo(live);

        // The following call calculates the score and updates COMPLETED if needed
        new StudentCourseStatus(this.dbProfile).gatherData(cache, session, studentId, courseId, false, false);
    }
}
