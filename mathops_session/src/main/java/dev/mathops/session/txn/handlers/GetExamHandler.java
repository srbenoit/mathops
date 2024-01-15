package dev.mathops.session.txn.handlers;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocNonwrappingSpan;
import dev.mathops.assessment.document.template.DocParagraph;
import dev.mathops.assessment.document.template.DocText;
import dev.mathops.assessment.document.template.DocWrappingSpan;
import dev.mathops.assessment.exam.ExamGradingRule;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamOutcome;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSubtest;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemAutoCorrectTemplate;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.log.LogBase;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.logic.ChallengeExamLogic;
import dev.mathops.db.old.logic.ChallengeExamStatus;
import dev.mathops.db.old.logic.PlacementLogic;
import dev.mathops.db.old.logic.PlacementStatus;
import dev.mathops.db.old.logic.StandardsMasteryLogic;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawExamLogic;
import dev.mathops.db.old.rawlogic.RawMpeLogLogic;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStqaLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawMpeLog;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStqa;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rawrecord.RawTestingCenter;
import dev.mathops.db.old.rec.MasteryExamRec;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.sitelogic.servlet.ExamEligibilityTester;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.AvailableExam;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.GetExamRequest;

import javax.print.Doc;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A handler for requests to start an exam received by the server. Requests include a login session ID and the exam
 * version being requested, and replies include the realized exam, or a list of errors or holds that prevented the exam
 * from being realized.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class GetExamHandler extends AbstractHandlerBase {

    /** The time allowed for each standard in a standards-based course. */
    private static final long TEN_MIN = 600L;

    /**
     * Construct a new {@code GetExamHandler}.
     *
     * @param theDbProfile the database profile under which the handler is being accessed
     */
    public GetExamHandler(final DbProfile theDbProfile) {

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
        if (message instanceof final GetExamRequest request) {
            try {
                result = processRequest(cache, request);
            } catch (final SQLException ex) {
                final GetExamReply reply = new GetExamReply();
                reply.error = "Error accessing the database: " + ex.getMessage();
                result = reply.toXml();
            }
        } else {
            Log.info("GetExamHandler called with ", message.getClass().getName());

            final GetExamReply reply = new GetExamReply();
            reply.error = "Invalid request type for get exam request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache   the data cache
     * @param request the {@code GetExamRequest} received from the client
     * @return the generated reply XML to send to the client
     * @throws SQLException if there is an error accessing the database
     */
    private String processRequest(final Cache cache, final GetExamRequest request) throws SQLException {

        final GetExamReply reply = new GetExamReply();

        boolean ok = loadStudentInfo(cache, request.studentId, reply);

        if (ok) {
            LogBase.setSessionInfo("TXN", request.studentId);

            // Look up the exam and store it in an AvailableExam object.
            final AvailableExam avail = new AvailableExam();

            if (request.examVersion != null) {
                avail.exam = RawExamLogic.query(cache, request.examVersion);
                if (avail.exam == null) {
                    reply.error = "No exam found with the requested version";
                    ok = false;
                }
            } else if (request.examCourse != null && request.examUnit != null && request.examType != null) {

                avail.exam = RawExamLogic.queryActiveByCourseUnitType(cache, request.examCourse, request.examUnit,
                        request.examType);
                if (avail.exam == null) {
                    reply.error = "Failed to query for exam for course, unit, and type.";
                    ok = false;
                }
            } else {
                Log.info("Invalid parameters supplied in GetExamRequest");
                ok = false;
            }

            final ZonedDateTime now = ZonedDateTime.now();

            if (ok) {
                final RawStudent student = getStudent();
                final List<RawAdminHold> holds = RawAdminHoldLogic.queryByStudent(cache, student.stuId);

                // We need to verify the exam and fill in the remaining fields in AvailableExam
                final HtmlBuilder reasons = new HtmlBuilder(100);
                reply.status = GetExamReply.SUCCESS;

                final RawTestingCenter testingCenter = getTestingCenter();
                boolean isProctored = "Y".equals(testingCenter.isProctored);

                // FIXME: Hardcode - add proctored field to request
                if ("PPPPP".equals(request.examVersion) || "MPTTC".equals(request.examVersion)) {
                    isProctored = true;
                }

                String section = null;
                boolean eligible;
                StandardsMasteryLogic standardsMasteryLogic = null;

                if ("Q".equals(avail.exam.examType)) {

                    // NOTE: This includes user's exams
                    if ("M 100U".equals(avail.exam.course)) {
                        eligible = true;

                        if (student.timelimitFactor != null) {
                            avail.timelimitFactor = student.timelimitFactor;
                        }
                    } else {
                        try {
                            final PlacementLogic logic = new PlacementLogic(cache, student.stuId,
                                    student.aplnTerm, now);
                            final PlacementStatus status = logic.status;
                            final Set<String> availablePlacement = isProctored ? status.availableLocalProctoredIds
                                    : status.availableUnproctoredIds;

                            if (availablePlacement.isEmpty()) {
                                eligible = false;
                                reasons.add(isProctored ? status.whyProctoredUnavailable
                                        : status.whyUnproctoredUnavailable);
                            } else {
                                eligible = true;

                                if (student.timelimitFactor != null) {
                                    avail.timelimitFactor = student.timelimitFactor;
                                }
                            }
                        } catch (final SQLException ex) {
                            Log.warning(ex);
                            reasons.add("Error querying placement status.");
                            eligible = false;
                        }
                    }
                } else if ("CH".equals(avail.exam.examType)) {

                    final ChallengeExamLogic logic = new ChallengeExamLogic(cache, student.stuId);
                    final ChallengeExamStatus status = logic.getStatus(avail.exam.course);

                    if (status.availableExamId == null) {
                        eligible = false;
                        reasons.add(status.reasonUnavailable);
                    } else {
                        eligible = true;

                        if (student.timelimitFactor != null) {
                            avail.timelimitFactor = student.timelimitFactor;
                        }
                    }
                } else if ("MA".equals(avail.exam.examType)) {

                    standardsMasteryLogic = new StandardsMasteryLogic(cache, request.studentId, avail.exam.course);
                    final int numAvailableToMaster = standardsMasteryLogic.countAvailableStandards();

                    if (numAvailableToMaster == 0) {
                        eligible = false;
                        reasons.add("Not eligible to master any learning targets");
                    } else {
                        eligible = true;
                        if (student.timelimitFactor != null) {
                            avail.timelimitFactor = student.timelimitFactor;
                        }
                    }

                } else {
                    final ExamEligibilityTester examtest = new ExamEligibilityTester(student.stuId);

                    eligible = examtest.isExamEligible(cache, now, avail, reasons, holds, request.checkEligibility);

                    if (examtest.getCourseSection() != null) {
                        section = examtest.getCourseSection().sect;
                    }
                }

                if (eligible) {
                    // Generate a serial number for the exam
                    final long serial = generateSerialNumber(false);

                    final TermRec term = TermLogic.get(cache).queryActive(cache);
                    if (Objects.nonNull(term)) {

                        final String treeRef = avail.exam.treeRef;
                        if ("synthetic".equals(treeRef) && standardsMasteryLogic != null) {
                            buildSyntheticExam(avail.exam, standardsMasteryLogic, serial, reply, term);
                        } else {
                            buildPresentedExam(cache, treeRef, serial, reply, term, isProctored);
                        }

                        final ExamObj exam = reply.presentedExam;

                        Log.info("Built presented exam ", exam.examVersion);

                        final DocColumn newInstr = new DocColumn();
                        newInstr.tag = "instructions";

                        DocParagraph para = new DocParagraph();
                        para.setColorName("navy");

                        DocText text = new DocText("Instructions:");
                        para.add(text);
                        newInstr.add(para);

                        // Alter the exam instructions based on section number
                        if (section != null && !section.isEmpty()
                                && (section.charAt(0) == '8' || section.charAt(0) == '4')) {

                            // Instructions for Distance Math courses
                            para = new DocParagraph();
                            para.add(new DocText("This exam consists of " + exam.getNumProblems()
                                    + " questions. Your score will be based on the number of "
                                    + "questions answered correctly. There is at least one "
                                    + "correct response to each question. To correctly answer a "
                                    + "question on this exam, you must choose "));

                            final DocWrappingSpan all = new DocWrappingSpan();
                            all.tag = "span";
                            all.setFontStyle(Integer.valueOf(Font.BOLD | Font.ITALIC));
                            all.add(new DocText("ALL"));
                            para.add(all);

                            para.add(new DocText(" correct responses to that question."));
                            newInstr.add(para);

                            exam.instructions = newInstr;

                        } else {
                            final String type = avail.exam.examType;

                            if ("U".equals(type) || "F".equals(type)) {

                                // Instructions for all Resident course unit and final exams
                                para = new DocParagraph();
                                para.add(new DocText("This exam has a time limit.  The time remaining to "
                                        + "complete the exam is displayed at the top right hand "
                                        + "corner of your computer screen."));
                                newInstr.add(para);

                                newInstr.add(new DocParagraph());

                                para = new DocParagraph();

                                final DocNonwrappingSpan note = new DocNonwrappingSpan();
                                note.tag = "span";
                                note.outlines = 8;
                                note.add(new DocText("PLEASE NOTE"));
                                para.add(note);

                                text = new DocText(": all exams taken in the Precalculus");
                                para.add(text);

                                text = new DocText(" Center must be submitted by the posted "
                                        + "closing time, even if your time limit has not expired.");
                                para.add(text);
                                newInstr.add(para);

                                newInstr.add(new DocParagraph());
                                newInstr.add(new DocParagraph());

                                para = new DocParagraph();
                                para.add(new DocText("This exam consists of " + exam.getNumProblems()
                                        + " questions. Your score will be based on the number of questions answered "
                                        + "correctly. There is at least one correct response to each question. To "
                                        + "correctly answer a question on this exam, you must choose "));

                                final DocWrappingSpan all = new DocWrappingSpan();
                                all.tag = "span";
                                all.setFontStyle(Integer.valueOf(Font.BOLD | Font.ITALIC));
                                all.add(new DocText("ALL"));
                                para.add(all);

                                para.add(new DocText(" correct responses to that question."));
                                newInstr.add(para);

                                newInstr.add(new DocParagraph());
                                newInstr.add(new DocParagraph());

                                para = new DocParagraph();

                                final DocNonwrappingSpan warn = new DocNonwrappingSpan();
                                warn.tag = "span";
                                warn.setFontStyle(Integer.valueOf(Font.BOLD));
                                warn.outlines = 8;
                                warn.add(new DocText("WARNING"));
                                para.add(warn);

                                para.add(new DocText(": You are "));

                                final DocNonwrappingSpan not = new DocNonwrappingSpan();
                                not.tag = "span";
                                not.setFontStyle(Integer.valueOf(Font.BOLD | Font.ITALIC));
                                not.add(new DocText("NOT"));
                                para.add(not);

                                para.add(new DocText(" permitted to use reference materials of any kind on this "
                                        + "exam. If you are found to be in possession of reference materials while "
                                        + "working on this exam, even if unintentional, you will be charged with "
                                        + "academic misconduct."));
                                newInstr.add(para);

                                exam.instructions = newInstr;
                            }
                        }

                        if (RawRecordConstants.M100P.equals(avail.exam.course)) {

                            // Log the fact that a placement exam was begun
                            final LocalDateTime start = TemporalUtils.toLocalDateTime(
                                    Instant.ofEpochMilli(reply.presentedExam.realizationTime));
                            final int startTime = start.getHour() * 60 + start.getMinute();

                            final RawMpeLog mpelog = new RawMpeLog(student.stuId, term.academicYear,
                                    avail.exam.course, avail.exam.version, start.toLocalDate(), null, null,
                                    Long.valueOf(serial), Integer.valueOf(startTime), null);

                            RawMpeLogLogic.INSTANCE.insert(cache, mpelog);
                        }

                        // Apply time limit factor adjustment
                        if (avail.timelimitFactor != null && reply.presentedExam.allowedSeconds != null) {
                            final double secs = (double) reply.presentedExam.allowedSeconds.intValue()
                                    * avail.timelimitFactor.doubleValue();
                            reply.presentedExam.allowedSeconds = Long.valueOf(Math.round(secs));
                        }

                        if (holds != null && !holds.isEmpty()) {
                            final int count = holds.size();
                            reply.holds = new String[count];

                            for (int i = 0; i < count; ++i) {
                                reply.holds[i] = RawAdminHoldLogic.getStudentMessage(holds.get(i).holdId);
                            }
                        }

                        // Store the pending exam row if record is from testing center machine
                        if (request.machineId != null || RawRecordConstants.M100P.equals(avail.exam.course)) {

                            final LocalDateTime realized = TemporalUtils.toLocalDateTime(
                                    Instant.ofEpochMilli(reply.presentedExam.realizationTime));
                            final LocalTime tm = realized.toLocalTime();
                            final int min = tm.getHour() * 60 + tm.getMinute();

                            final RawPendingExam pending = new RawPendingExam(Long.valueOf(serial),
                                    reply.presentedExam.examVersion, student.stuId, realized.toLocalDate(),
                                    null, Integer.valueOf(min), null, null, null, null, avail.exam.course,
                                    avail.exam.unit, avail.exam.examType, avail.timelimitFactor, "STU");

                            RawPendingExamLogic.INSTANCE.insert(cache, pending);
                        }
                    }
                } else {
                    reply.error = reasons.toString();

                    if (reply.error.isEmpty()) {
                        reply.error = null;
                    }

                    Log.info("Exam not eligible: " + reply.error);
                }
            }
        } else {
            reply.error = "Unable to load session information";
        }

        return reply.toXml();
    }

    /**
     * Builds a synthetic "ExamObj" object for mastery of course standards based on the set of standards for which the
     * student is currently eligible.  The exam will have one section for each unit in which the student is eligible
     * to try to master at least one standard.
     *
     * @param examRec  the exam record
     * @param logic    the standards mastery logic
     * @param serial   the serial number to associate with the exam
     * @param reply    the reply message to populate with the realized exam or the error status
     * @param term     the active term
     */
    private void buildSyntheticExam(final RawExam examRec, final StandardsMasteryLogic logic, final long serial,
                                    final GetExamReply reply, final TermRec term) {

        final List<MasteryExamRec> eligibleToMaster = logic.gatherEligibleStandards();

        // Ordering of mastery exams is based on course, unt, objective, then type.  All our exams should be for the
        // same course, so this will sort by unit and objective
        eligibleToMaster.sort(null);

        final ExamObj exam = new ExamObj();
        exam.ref = "synthetic";
        exam.refRoot = "math";
        exam.examName = "Learning Target Mastery";
        exam.course = examRec.course;
        exam.courseUnit = "0";
        exam.examVersion = examRec.version;

        exam.instructions = new DocColumn();
        exam.instructions.tag = "instructions";

        final DocParagraph instrPara1 = new DocParagraph();
        exam.instructions.add(instrPara1);
        instrPara1.setColorName("navy");
        instrPara1.add(new DocText("Instructions:"));
        final DocParagraph instrPara2 = new DocParagraph();
        exam.instructions.add(instrPara2);
        instrPara2.add(new DocText("This proctored exam allows you to demonstrate mastery of one or more Learning "
                + "Targets. Each learning target has two questions.  You have to answer both questions correctly to "
                + "master that target.  You may use as many attempts as you need to do this - once you have answered "
                + "one of the questions correctly twice, you will not have to answer it again on future attempts."));

        // Time limit is 10 minutes per standard
        final int numToMaster = eligibleToMaster.size();
        exam.allowedSeconds = Long.valueOf((long) numToMaster * TEN_MIN);

        ExamSection currentSection = null;
        Integer currentUnit = null;
        Integer currentObj = null;

        int id = 1;

        for (final MasteryExamRec masteryExam : eligibleToMaster) {
            final String examId = masteryExam.examId;

            // See which questions have already been answered correctly twice
            final int alreadyPassed = logic.whichQuestionsPassedTwice(examId);
            if (alreadyPassed == 2) {
                Log.warning("Mastery exam for ", examRec.course, " Target ", masteryExam.unit, ".",
                        masteryExam.objective,
                        " is available to master, but both questions have been answered correctly twice.");
            } else {
                // Load up the exam object for the mastery exam
                final ExamObj unitMasteryExam = InstructionalCache.getExam(masteryExam.treeRef);
                if (unitMasteryExam == null) {
                    Log.warning("Unable to find Mastery exam for ", examRec.course, " Target ", masteryExam.unit, ".",
                            masteryExam.objective, " with ref ", masteryExam.treeRef);
                } else {
                    if (!(masteryExam.unit.equals(currentUnit) && masteryExam.objective.equals(currentObj))) {
                        // Start a new section for this unit
                        currentSection = new ExamSection();
                        currentUnit = masteryExam.unit;
                        currentObj = masteryExam.objective;
                        currentSection.sectionName = "Learning Target " + masteryExam.unit + "."
                                + masteryExam.objective;
                        currentSection.shortName = "Target " +masteryExam.unit + "." + masteryExam.objective;
                        exam.addSection(currentSection);
                    }

                    final ExamSection unitMasterySection = unitMasteryExam.getSection(0);
                    if (unitMasterySection.getNumProblems() == 2) {

                        // Add questions to the open section
                        if ((alreadyPassed & 0x01) == 0x00) {
                            final ExamProblem problem1 = unitMasterySection.getProblem(0);
                            final ExamProblem problem1Copy = problem1.deepCopy(exam);
                            problem1Copy.problemId = id;
                            ++id;
                            problem1Copy.problemName = unitMasterySection.sectionName + " Question 1";
                            currentSection.addProblem(problem1Copy);
                        } else {
                            final ExamProblem eprob = new ExamProblem(exam);
                            eprob.problemId = id;
                            ++id;
                            final ProblemAutoCorrectTemplate prb = new ProblemAutoCorrectTemplate();
                            eprob.addProblem(prb);
                            eprob.problemName = unitMasterySection.sectionName + " Question 1";
                            currentSection.addProblem(eprob);
                        }

                        if ((alreadyPassed & 0x02) == 0x00) {
                            final ExamProblem problem2 = unitMasterySection.getProblem(1);
                            final ExamProblem problem2Copy = problem2.deepCopy(exam);
                            problem2Copy.problemId = id;
                            ++id;
                            problem2Copy.problemName = unitMasterySection.sectionName + " Question 2";
                            currentSection.addProblem(problem2Copy);
                        } else {
                            final ExamProblem eprob = new ExamProblem(exam);
                            eprob.problemId = id;
                            ++id;
                            final ProblemAutoCorrectTemplate prb = new ProblemAutoCorrectTemplate();
                            eprob.addProblem(prb);
                            eprob.problemName = unitMasterySection.sectionName + " Question 2";
                            currentSection.addProblem(eprob);
                        }
                    } else {
                        Log.warning("Mastery exam for ", examRec.course, " Unit ", masteryExam.unit,
                                " does not have 2 problems");
                    }
                }
            }
        }

        // Now we must add the exam's problems so it can be realized.
        final int numSect = exam.getNumSections();

        for (int onSect = 0; onSect < numSect; ++onSect) {
            final ExamSection esect = exam.getSection(onSect);
            final int numProb = esect.getNumProblems();

            for (int onProb = 0; onProb < numProb; ++onProb) {

                final ExamProblem eprob = esect.getProblem(onProb);
                final int num = eprob.getNumProblems();

                for (int i = 0; i < num; ++i) {
                    AbstractProblemTemplate prb = eprob.getProblem(i);

                    if (prb == null || prb.ref == null) {
                        Log.warning("Exam " + exam.ref + " section " + onSect + " problem " + onProb + " choice "
                                + i + " getProblem() returned " + prb);
                    } else if (!(prb instanceof ProblemAutoCorrectTemplate)) {
                        prb = InstructionalCache.getProblem(prb.ref);

                        if (prb != null) {
                            eprob.setProblem(i, prb);
                        }
                    }
                }
            }
        }

        if (exam.realize("Y".equals(getTestingCenter().isRemote), true, serial)) {
            reply.presentedExam = exam;
            reply.status = GetExamReply.SUCCESS;
            reply.studentId = getStudent().stuId;

            if (!new ExamWriter().writePresentedExam(getStudent().stuId, term, reply.presentedExam,
                    reply.toXml())) {
                Log.warning("Unable to cache exam " + exam.ref);
                reply.presentedExam = null;
                reply.status = GetExamReply.CANNOT_REALIZE_EXAM;
            }
        } else {
            Log.warning("Unable to realize " + exam.ref);
            reply.status = GetExamReply.CANNOT_REALIZE_EXAM;
        }

        if (reply.status == GetExamReply.SUCCESS) {
            reply.presentedExam = exam;
        }
    }

    /**
     * Attempt to construct a realized exam and install it in the reply message. On errors, the reply message errors
     * field will be set to the cause of the error.
     *
     * @param cache       the data cache
     * @param ref         the reference to the exam to be loaded
     * @param serial      the serial number to associate with the exam
     * @param reply       the reply message to populate with the realized exam or the error status
     * @param term        the term under which to file the presented exam
     * @param isProctored {@code true} if the exam is proctored; {@code false} if not
     */
    private void buildPresentedExam(final Cache cache, final String ref, final long serial,
                                    final GetExamReply reply, final TermRec term, final boolean isProctored) {

        final ExamObj exam = InstructionalCache.getExam(ref);

        if (exam == null) {
            reply.status = GetExamReply.CANNOT_LOAD_EXAM_TEMPLATE;
            Log.warning("Unable to load template for " + ref);
        } else if (exam.ref == null) {
            reply.status = GetExamReply.CANNOT_LOAD_EXAM_TEMPLATE;
            Log.warning("Errors loading exam template");
        } else {
            final Collection<Integer> autoPassItems = new ArrayList<>(10);

            // See which items the student has already gotten correct twice on this exam version
            final RawStudent student = getStudent();
            try {
                final List<RawStexam> stexams = RawStexamLogic.getExamsByVersion(cache, student.stuId, exam.examVersion,
                        false);

                final String examCourse = exam.course;
                if (stexams.size() > 1 && (RawRecordConstants.M117.equals(examCourse)
                        || RawRecordConstants.M118.equals(examCourse)
                        || RawRecordConstants.M124.equals(examCourse)
                        || RawRecordConstants.M125.equals(examCourse)
                        || RawRecordConstants.M126.equals(examCourse))) {

                    final List<RawStqa> answers = RawStqaLogic.queryByStudent(cache, student.stuId);

                    // Map from question number to count of correct answers
                    final Map<Integer, Integer> correctCount = new HashMap<>(20);

                    for (final RawStexam stexam : stexams) {
                        final Long sernum = stexam.serialNbr;

                        if (sernum != null) {
                            for (final RawStqa qa : answers) {
                                if (sernum.equals(qa.serialNbr) && "Y".equals(qa.ansCorrect)) {

                                    final Integer questionNbr = qa.questionNbr;
                                    final Integer count = correctCount.get(questionNbr);
                                    final Integer newCount;
                                    if (count == null) {
                                        newCount = Integer.valueOf(1);
                                    } else {
                                        newCount = Integer.valueOf(count.intValue() + 1);
                                    }

                                    correctCount.put(questionNbr, newCount);
                                }
                            }
                        }
                    }

                    for (final Map.Entry<Integer, Integer> entry : correctCount.entrySet()) {
                        if (entry.getValue().intValue() >= 2) {
                            // Student has answered this question correctly twice before - replace
                            // that item with an "automatically correct" item.

                            final Integer question = entry.getKey();
                            autoPassItems.add(question);
                        }
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to look up exam history", ex);
            }

            // Now we must add the exam's problems so it can be realized.
            final int numSect = exam.getNumSections();

            for (int onSect = 0; onSect < numSect; ++onSect) {
                final ExamSection esect = exam.getSection(onSect);
                final int numProb = esect.getNumProblems();

                for (int onProb = 0; onProb < numProb; ++onProb) {

                    final ExamProblem eprob = esect.getProblem(onProb);
                    final int num = eprob.getNumProblems();

                    if (autoPassItems.contains(Integer.valueOf(eprob.problemId))) {
                        final ProblemAutoCorrectTemplate prb = new ProblemAutoCorrectTemplate();
                        for (int i = 0; i < num; ++i) {
                            eprob.setProblem(i, prb);
                        }
                    } else {
                        for (int i = 0; i < num; ++i) {
                            AbstractProblemTemplate prb = eprob.getProblem(i);

                            if (prb == null || prb.ref == null) {
                                Log.warning("Exam " + ref + " section " + onSect + " problem " + onProb + " choice "
                                        + i + " getProblem() returned " + prb);
                            } else {
                                prb = InstructionalCache.getProblem(prb.ref);

                                if (prb != null) {
                                    eprob.setProblem(i, prb);
                                }
                            }
                        }
                    }
                }
            }

            if (exam.realize("Y".equals(getTestingCenter().isRemote), isProctored,
                    serial)) {
                reply.presentedExam = exam;
                reply.status = GetExamReply.SUCCESS;
                reply.studentId = student.stuId;

                if (!new ExamWriter().writePresentedExam(student.stuId, term, reply.presentedExam,
                        reply.toXml())) {
                    Log.warning("Unable to cache exam " + ref);
                    reply.presentedExam = null;
                    reply.status = GetExamReply.CANNOT_REALIZE_EXAM;
                }
            } else {
                Log.warning("Unable to realize " + ref);
                reply.status = GetExamReply.CANNOT_REALIZE_EXAM;
            }
        }

        // TODO: Pre-populate "Survey" section of exam with existing answers.

        if (reply.status == GetExamReply.SUCCESS) {
            reply.presentedExam = exam;
        }
    }
}
