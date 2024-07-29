package dev.mathops.session.txn.handlers;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.document.template.DocColumn;
import dev.mathops.assessment.document.template.DocNonwrappingSpan;
import dev.mathops.assessment.document.template.DocParagraph;
import dev.mathops.assessment.document.template.DocText;
import dev.mathops.assessment.document.template.DocWrappingSpan;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.PlacementLogic;
import dev.mathops.db.old.logic.PlacementStatus;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.sitelogic.servlet.ReviewExamEligibilityTester;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.AvailableExam;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.GetReviewExamReply;
import dev.mathops.session.txn.messages.GetReviewExamRequest;

import java.awt.Font;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A handler for requests to start a unit review exam received by the server. Requests include a login session ID and
 * the exam version being requested, and replies include the realized exam, or a list of errors or holds that prevented
 * the exam from being realized.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class GetReviewExamHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code GetReviewExamHandler}.
     */
    public GetReviewExamHandler() {

        super();
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
        if (message instanceof final GetReviewExamRequest request) {
            try {
                result = processRequest(cache, request);
            } catch (final SQLException ex) {
                final GetReviewExamReply reply = new GetReviewExamReply();
                reply.error = "Unable to access the database: " + ex.getMessage();
                result = reply.toXml();
            }
        } else {
            Log.info("GetReviewExamHandler called with ", message.getClass().getName());

            final GetReviewExamReply reply = new GetReviewExamReply();
            reply.error = "Invalid request type for get review exam request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param cache   the data cache
     * @param request the {@code GetReviewExamRequest} received from the client
     * @return the generated reply XML to send to the client
     * @throws SQLException if there is an error accessing the database
     */
    private String processRequest(final Cache cache, final GetReviewExamRequest request) throws SQLException {

        final GetReviewExamReply reply = new GetReviewExamReply();

        boolean ok = loadStudentInfo(cache, request.studentId, reply);

        if (ok) {
            LogBase.setSessionInfo("TXN", request.studentId);

            final SystemData systemData = cache.getSystemData();

            // Look up the exam and store it in an AvailableExam object.
            final AvailableExam avail = new AvailableExam();

            if (request.examVersion == null) {
                Log.info("Invalid parameters supplied in GetReviewExamRequest");
                reply.error = "Invalid request to server for review exam.";
                ok = false;
            } else {
                avail.exam = systemData.getActiveExam(request.examVersion);

                if (avail.exam == null) {
                    reply.error = "No exam found with the requested version";
                    ok = false;
                } else {
                    final String type = avail.exam.examType;

                    if (!"R".equals(type) && !"Q".equals(type)) {
                        Log.info("Exam version '", request.examVersion, "' is not a supported exam type.");
                        reply.error = "Requested exam is not a review/placement exam.";
                        ok = false;
                    }
                }
            }

            if (ok) {
                reply.status = GetExamReply.SUCCESS;
                final StudentData studentData = getStudentData();
                reply.studentId = studentData.getStudentId();

                final HtmlBuilder reasons = new HtmlBuilder(100);
                final List<RawAdminHold> holds = new ArrayList<>(1);

                final ZonedDateTime now = ZonedDateTime.now();

                boolean eligible;
                if (request.isPractice) {
                    eligible = true;
                    // No mastery score in reply.
                } else {
                    final String type = avail.exam.examType;

                    if ("R".equals(type)) {

                        // We need to verify the exam and fill in the remaining fields in AvailableExam
                        final ReviewExamEligibilityTester examtest = new ReviewExamEligibilityTester(reply.studentId);

                        eligible = examtest.isExamEligible(cache, now, avail, reasons, holds);
                        reply.masteryScore = examtest.getMasteryScore(type);
                    } else if ("Q".equals(type)) {

                        // FIXME:
                        if ("17ELM".equals(avail.exam.version) || "7TELM".equals(avail.exam.version)) {
                            // This exam had to be marked as EnumExamType.QUALIFYING_VAL to avoid a
                            // key duplication, but is treated as a review exam

                            final ReviewExamEligibilityTester examtest = new ReviewExamEligibilityTester(
                                    reply.studentId);

                            eligible = examtest.isExamEligible(cache, now, avail, reasons, holds);

                            // Adjust mastery scores since ELM exam is twice as long
                            if (examtest.getMasteryScore(type) != null) {
                                if (examtest.getMasteryScore(type).intValue() == 8) {
                                    reply.masteryScore = Integer.valueOf(16);
                                } else if (examtest.getMasteryScore(type).intValue() == 7) {
                                    reply.masteryScore = Integer.valueOf(14);
                                } else if (examtest.getMasteryScore(type).intValue() == 6) {
                                    reply.masteryScore = Integer.valueOf(12);
                                } else {
                                    reply.masteryScore = examtest.getMasteryScore(type);
                                }
                            }
                        } else {
                            try {
                                final RawStudent student = studentData.getStudentRecord();
                                final PlacementStatus placementStat = new PlacementLogic(cache, reply.studentId,
                                        student.aplnTerm, now).status;

                                eligible = placementStat.attemptsRemaining > 0;
                            } catch (final SQLException ex) {
                                Log.warning(ex);
                                eligible = false;
                            }
                            reply.masteryScore = null;
                        }
                    } else {
                        eligible = false;
                    }
                }

                if (eligible) {

                    // Generate a serial number for the exam
                    final long serial = generateSerialNumber(request.isPractice);
                    final TermRec active = cache.getSystemData().getActiveTerm();

                    buildPresentedExam(avail.exam.treeRef, serial, reply, active);

                    final ExamObj exam = reply.presentedExam;

                    // Replace the instructions on review exams (leave qualifier exams alone)
                    final String type = avail.exam.examType;

                    if ("R".equals(type)) {

                        final String singular = "M 101".equals(avail.exam.course) ? "quiz" : "exam";
                        final String plural = "M 101".equals(avail.exam.course) ? "quizzes" : "exams";

                        final DocColumn newInstr = new DocColumn();
                        newInstr.tag = "instructions";

                        DocParagraph para = new DocParagraph();
                        para.setColorName("navy");
                        para.add(new DocText("Instructions:"));
                        newInstr.add(para);

                        para = new DocParagraph();
                        para.add(new DocText("This " + singular + " consists of "
                                + exam.getNumProblems()
                                + " questions. Your score will be based on the number of questions answered "
                                + "correctly. There is at least one correct response to each question. To "
                                + "correctly answer a question on this " + singular + ", you must choose "));

                        final DocWrappingSpan all = new DocWrappingSpan();
                        all.tag = "span";
                        all.setFontStyle(Integer.valueOf(Font.BOLD | Font.ITALIC));
                        all.add(new DocText("ALL"));
                        para.add(all);

                        para.add(new DocText(" correct responses to that question."));
                        newInstr.add(para);

                        newInstr.add(new DocParagraph());
                        newInstr.add(new DocParagraph());

                        final boolean hasProctored;

                        if (RawRecordConstants.M100T.equals(avail.exam.course)) {
                            hasProctored = avail.exam.unit.intValue() > 2;
                        } else {
                            hasProctored = true;
                        }

                        if (hasProctored) {
                            para = new DocParagraph();
                            para.setColorName("navy");

                            final DocNonwrappingSpan warn = new DocNonwrappingSpan();
                            warn.tag = "span";
                            warn.setFontStyle(Integer.valueOf(Font.BOLD | Font.ITALIC));
                            warn.add(new DocText("Reminder concerning " + plural
                                    + " taken in the Precalculus Center:"));
                            para.add(warn);
                            newInstr.add(para);

                            para = new DocParagraph();
                            para.add(new DocText("You are "));

                            final DocNonwrappingSpan not = new DocNonwrappingSpan();
                            not.tag = "span";
                            not.setFontStyle(Integer.valueOf(Font.BOLD | Font.ITALIC));
                            not.add(new DocText("NOT"));
                            para.add(not);

                            para.add(new DocText(" permitted to use reference materials of any kind during a proctored "
                                    + singular + ". Possession of reference materials, even if unintentional, will "
                                    + "result in an academic penalty and a University Disciplinary Hearing."));
                            newInstr.add(para);
                        }

                        exam.instructions = newInstr;
                    }

                    if (!holds.isEmpty()) {
                        final int numHolds = holds.size();
                        reply.holds = new String[numHolds];

                        for (int i = 0; i < numHolds; ++i) {
                            final RawAdminHold hold = holds.get(i);
                            reply.holds[i] = RawAdminHoldLogic.getStudentMessage(hold.holdId);
                        }
                    }
                } else {
                    reply.error = reasons.toString();

                    if (reply.error.isEmpty()) {
                        reply.error = null;
                    }

                    Log.info("Review exam not eligible: ", reply.error);
                }
            }
        }

        return reply.toXml();
    }

    /**
     * Attempt to construct a realized exam and install it in the reply message. On errors, the reply message errors
     * field will be set to the cause of the error.
     *
     * @param ref    the reference to the exam to be loaded
     * @param serial the serial number to associate with the exam
     * @param reply  the reply message to populate with the realized exam or the error status
     * @param term   the term under which to file the presented exam
     */
    private void buildPresentedExam(final String ref, final long serial,
                                    final GetReviewExamReply reply, final TermRec term) {

        final ExamObj exam = InstructionalCache.getExam(ref);

        if (exam == null) {
            reply.status = GetExamReply.CANNOT_LOAD_EXAM_TEMPLATE;
            Log.warning("Unable to load template for ", ref);
        } else if (exam.ref == null) {
            reply.status = GetExamReply.CANNOT_LOAD_EXAM_TEMPLATE;
            Log.warning("Errors loading exam template");
        } else {
            // add the exam's problems, so it can be realized.
            final int numSect = exam.getNumSections();

            for (int onSect = 0; onSect < numSect; ++onSect) {
                final ExamSection esect = exam.getSection(onSect);
                final int numProb = esect.getNumProblems();

                for (int onProb = 0; onProb < numProb; ++onProb) {
                    final ExamProblem eprob = esect.getProblem(onProb);
                    final int num = eprob.getNumProblems();

                    for (int i = 0; i < num; ++i) {
                        AbstractProblemTemplate prb = eprob.getProblem(i);

                        if (prb == null || prb.id == null) {
                            Log.warning("Problem " + (onProb + 1) + CoreConstants.DOT + (i + 1) + " in section "
                                    + (onSect + 1) + " in exam ", exam.ref, " invalid");
                        } else {
                            prb = InstructionalCache.getProblem(prb.id);

                            if (prb != null) {
                                eprob.setProblem(i, prb);
                            }
                        }
                    }
                }
            }

            if (exam.realize("Y".equals(getTestingCenter().isRemote), "Y".equals(getTestingCenter().isProctored),
                    serial)) {

                reply.presentedExam = exam;
                reply.status = GetExamReply.SUCCESS;
                reply.studentId = getStudentData().getStudentId();

                final String xml = reply.toXml();
                if (!new ExamWriter().writePresentedExam(reply.studentId, term, reply.presentedExam, xml)) {
                    Log.warning("Unable to cache ", ref);
                    reply.status = GetExamReply.CANNOT_REALIZE_EXAM;
                }
            } else {
                reply.status = GetExamReply.CANNOT_REALIZE_EXAM;
                Log.warning("Unable to realize ", ref);
            }
        }

        // TODO: Pre-populate "Survey" section of exam with existing answers.

        reply.presentedExam = exam;
    }
}
