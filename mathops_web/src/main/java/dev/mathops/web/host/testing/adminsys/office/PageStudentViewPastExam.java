package dev.mathops.web.host.testing.adminsys.office;

import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.htmlgen.ExamObjConverter;
import dev.mathops.assessment.htmlgen.ProblemConverter;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleSelectionTemplate;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.GetReviewExamReply;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Pages that displays a past exam taken by a student.
 */
enum PageStudentViewPastExam {
    ;

    /**
     * Shows a single past exam for a student.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String stu = req.getParameter("stu");
        final String ser = req.getParameter("ser");
        final String course = req.getParameter("course");
        final String exam = req.getParameter("exam");
        final String xml = req.getParameter("xml");
        final String upd = req.getParameter("upd");

        if (AbstractSite.isParamInvalid(stu) || AbstractSite.isParamInvalid(ser)
                || AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(exam)
                || AbstractSite.isParamInvalid(xml) || AbstractSite.isParamInvalid(upd)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  studentId='", stu, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final RawStudent student = RawStudentLogic.query(cache, stu, false);

            if (student == null) {
                PageHome.doGet(cache, site, req, resp, session, "Student not found.");
            } else {
                doStudentExamsPage(cache, site, req, resp, session, student, ser, course, xml, upd);
            }
        }
    }

    /**
     * Shows the student activity page for a provided student.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param student the student for which to present information
     * @param ser     the serial number of the exam
     * @param course  the course
     * @param xml     the path to the XML file
     * @param upd     the path to the update file
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void doStudentExamsPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                           final HttpServletResponse resp, final ImmutableSessionInfo session,
                                           final RawStudent student, final String ser, final String course,
                                           final String xml, final String upd) throws IOException, SQLException {

        final HtmlBuilder htm = OfficePage.startOfficePage(cache, site, session, true);

        htm.sP("studentname").add("<strong>", student.getScreenName(), "</strong> &nbsp; <strong><code>",
                student.stuId, "</code></strong>").eP();

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {

            htm.sDiv("narrowstack");
            htm.addln("<form method='get' action='student_info.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Registrations</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_schedule.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Schedule</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_activity.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Activity</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_exams.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='navlit'>Exams</button>");
            htm.addln("</form>");

            htm.addln("<form method='get' action='student_placement.html'>");
            htm.addln("<input type='hidden' name='stu' value='", student.stuId, "'/>");
            htm.addln("<button class='nav'>Placement</button>");
            htm.addln("</form>");
            htm.eDiv(); // narrowstack

            htm.sDiv("detail");
            emitStudentPastExam(cache, htm, student, ser, course, xml, upd);
            htm.eDiv(); // detail
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Emits the student's past exam record.
     *
     * @param cache   the data cache
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param student the student
     * @param ser     the serial number of the exam
     * @param course  the course
     * @param xml     the path to the XML file
     * @param upd     the path to the update file
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStudentPastExam(final Cache cache, final HtmlBuilder htm, final RawStudent student,
                                            final String ser, final String course, final String xml,
                                            final String upd) throws SQLException {

        RawStexam examRec = null;

        final List<RawStexam> allExams = RawStexamLogic.getExams(cache, student.stuId, course, false);
        for (final RawStexam test : allExams) {
            if (test.serialNbr.toString().equals(ser)) {
                examRec = test;
                break;
            }
        }

        if (examRec == null) {
            // It could be a placement or challenge exam...
            RawStmpe placementRec = null;

            final List<RawStmpe> allPlacements = RawStmpeLogic.queryLegalByStudent(cache, student.stuId);
            for (final RawStmpe test : allPlacements) {
                if (test.serialNbr.toString().equals(ser)) {
                    placementRec = test;
                    break;
                }
            }

            if (placementRec == null) {
                RawStchallenge challengeRec = null;

                final List<RawStchallenge> allChallenges = RawStchallengeLogic.queryByStudent(cache, student.stuId);
                for (final RawStchallenge test : allChallenges) {
                    if (test.serialNbr.toString().equals(ser)) {
                        challengeRec = test;
                        break;
                    }
                }

                if (challengeRec == null) {
                    htm.sH(3).add("Database Record of Exam:").eH(3);

                    htm.sP("error").add("(No record found)").eP();
                    htm.sP("error").add("Stu = ", student.stuId, ", course = ", course, ", serial = ", ser).eP();
                } else {
                    emitStudentChallenge(htm, challengeRec, xml, upd);
                }
            } else {
                emitStudentPlacement(htm, placementRec, xml, upd);
            }
        } else {
            emitStudentExam(htm, examRec, xml, upd);
        }
    }

    /**
     * Emits a past exam display for a "StudentExam" record (used for all but placement or challenge).
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param examRec the exam record
     * @param xml     the location of the exam XML file
     * @param upd     the location of the exam update file
     */
    private static void emitStudentExam(final HtmlBuilder htm, final RawStexam examRec, final String xml,
                                        final String upd) {

        htm.sH(3).add("Database Record of Exam:").eH(3);

        final LocalDateTime sta = examRec.getStartDateTime();
        final LocalDateTime fin = examRec.getFinishDateTime();

        if (sta != null && fin != null) {
            final long totalSec = fin.toEpochSecond(ZoneOffset.UTC) - sta.toEpochSecond(ZoneOffset.UTC);
            final long min = totalSec / 60L;
            final long sec = totalSec % 60L;

            htm.sTable("vreport", "style='margin-top:5px;border-collapse:separate;border-spacing:0;min-width:688px;'");

            htm.sTr().sTh().add("Course / Unit").eTh().sTd().add(examRec.course, ", Unit ", examRec.unit).eTd().eTr();
            htm.sTr().sTh().add("Exam").eTh().sTd()
                    .add(examRec.version, " (", RawExam.getExamTypeName(examRec.examType), ")").eTd().eTr();
            htm.sTr().sTh().add("Serial No.").eTh().sTd().add(examRec.serialNbr).eTd().eTr();
            htm.sTr().sTh().add("Date").eTh().sTd().add(TemporalUtils.FMT_WMD.format(examRec.examDt)).eTd().eTr();
            htm.sTr().sTh().add("Time").eTh().sTd()
                    .add("From ", TemporalUtils.FMT_HM_A.format(sta.toLocalTime()), " to ",
                            TemporalUtils.FMT_HM_A.format(fin.toLocalTime()), " (duration ", Long.toString(min),
                            CoreConstants.COLON, sec < 10L ? "0" : CoreConstants.EMPTY,
                            Long.toString(sec), ")")
                    .eTd().eTr();
            htm.sTr().sTh().add("Score").eTh().sTd().add(examRec.examScore).eTd().eTr();
            htm.sTr().sTh().add("Passed").eTh().sTd().add(examRec.passed).eTd().eTr();
            htm.sTr().sTh().add("First Passed").eTh().sTd().add(examRec.isFirstPassed).eTd().eTr();
            htm.eTable();

            htm.sH(3).add("Exam Details:").eH(3);

            final File basePath1 = new File("/imp/data");
            final File basePath2 = new File("/impback");

            if (basePath1.isDirectory() || basePath2.isDirectory()) {

                File xmlPath = new File(basePath1, xml);
                File updPath = new File(basePath1, upd);

                if (!xmlPath.exists() || !updPath.exists()) {
                    xmlPath = new File(basePath2, xml);
                    updPath = new File(basePath2, upd);

                    if (!xmlPath.exists() || !updPath.exists()) {
                        xmlPath = new File(basePath1, xml);
                        updPath = new File(basePath1, upd);
                    }
                }
                final ExamObj exam = loadExam(xmlPath);

                if (exam == null) {
                    htm.sP("error").add("(Unable to load exam XML file)").eP();
                } else {
                    if (!loadUpdates(updPath, exam)) {
                        htm.sP("error").add("Unable to load submitted answers file - exam not submitted?").eP();
                    }

                    emitExam(htm, exam);
                }
            } else {
                htm.sP("error").add("(Unable to loacate exam directory)").eP();
            }
        }
    }

    /**
     * Emits a past exam display for a "StudentPlacementAttempt" record.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param placementRec the exam record
     * @param xml          the location of the exam XML file
     * @param upd          the location of the exam update file
     */
    private static void emitStudentPlacement(final HtmlBuilder htm, final RawStmpe placementRec,
                                             final String xml, final String upd) {

        htm.sH(3).add("Database Record of Exam:").eH(3);

        final LocalDateTime start = placementRec.getStartDateTime();
        final LocalDateTime fin = placementRec.getFinishDateTime();

        if (start != null && fin != null) {
            final long totalSec = fin.toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC);
            final long min = totalSec / 60L;
            final long sec = totalSec % 60L;

            htm.sTable("vreport", "style='margin-top:5px;border-collapse:separate;border-spacing:0;min-width:688px;'");

            htm.sTr().sTh().add("Exam ID").eTh().sTd().add(placementRec.version, ")").eTd().eTr();
            htm.sTr().sTh().add("Serial No.").eTh().sTd().add(placementRec.serialNbr).eTd().eTr();
            htm.sTr().sTh().add("Date").eTh().sTd().add(TemporalUtils.FMT_WMD.format(placementRec.examDt)).eTd().eTr();
            htm.sTr().sTh().add("Time").eTh().sTd().add(TemporalUtils.FMT_HMS_A.format(start.toLocalTime()), " to ",
                            TemporalUtils.FMT_HMS_A.format(fin.toLocalTime()), " (", Long.toString(min),
                            CoreConstants.COLON, sec < 10L ? "0" : CoreConstants.EMPTY, Long.toString(sec), ")")
                    .eTd().eTr();
            htm.sTr().sTh().add("Placed").eTh().sTd().add(placementRec.placed).eTd().eTr();
            htm.sTr().sTh().add("How Validated").eTh().sTd().add(placementRec.howValidated).eTd().eTr();
            htm.sTr().sTh().add("Subtest A").eTh().sTd().add(placementRec.stsA).eTd().eTr();
            htm.sTr().sTh().add("Subtest 117").eTh().sTd().add(placementRec.sts117).eTd().eTr();
            htm.sTr().sTh().add("Subtest 118").eTh().sTd().add(placementRec.sts118).eTd().eTr();
            htm.sTr().sTh().add("Subtest 124").eTh().sTd().add(placementRec.sts124).eTd().eTr();
            htm.sTr().sTh().add("Subtest 125").eTh().sTd().add(placementRec.sts125).eTd().eTr();
            htm.sTr().sTh().add("Subtest 126").eTh().sTd().add(placementRec.sts126).eTd().eTr();

            htm.eTable();

            htm.sH(3).add("Exam Details:").eH(3);

            final File basePath1 = new File("/imp/data");
            final File basePath2 = new File("/impback");

            if (basePath1.isDirectory() || basePath2.isDirectory()) {

                File xmlPath = new File(basePath1, xml);
                File updPath = new File(basePath1, upd);

                if (!xmlPath.exists() || !updPath.exists()) {
                    xmlPath = new File(basePath2, xml);
                    updPath = new File(basePath2, upd);

                    if (!xmlPath.exists() || !updPath.exists()) {
                        xmlPath = new File(basePath1, xml);
                        updPath = new File(basePath1, upd);
                    }
                }
                final ExamObj exam = loadExam(xmlPath);

                if (exam == null) {
                    htm.sP("error").add("(Unable to load exam XML file)").eP();
                } else {
                    if (!loadUpdates(updPath, exam)) {
                        htm.sP("error").add("Unable to load submitted answers file - exam not submitted?").eP();
                    }

                    emitExam(htm, exam);
                }
            } else {
                htm.sP("error").add("(Unable to loacate exam directory)").eP();
            }
        }
    }

    /**
     * Emits a past exam display for a "StudentChallengeAttempt" record.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param challengeRec the exam record
     * @param xml          the location of the exam XML file
     * @param upd          the location of the exam update file
     */
    private static void emitStudentChallenge(final HtmlBuilder htm, final RawStchallenge challengeRec,
                                             final String xml, final String upd) {

        htm.sH(3).add("Database Record of Exam:").eH(3);

        long totalSec = 0L;
        LocalTime start = null;
        LocalTime end = null;

        if (challengeRec.startTime != null && challengeRec.finishTime != null) {
            final int startMin = challengeRec.startTime.intValue();
            final int finishMin = challengeRec.finishTime.intValue();

            totalSec = (long) (finishMin - startMin) * 60L;

            start = LocalTime.of(startMin / 60, startMin % 60);
            end = LocalTime.of(finishMin / 60, finishMin % 60);
        }

        final long min = totalSec / 60L;

        htm.sTable("vreport", "style='margin-top:5px;border-collapse:separate;border-spacing:0;min-width:688px;'");

        htm.sTr().sTh().add("Exam ID").eTh().sTd().add(challengeRec.version).eTd().eTr();
        htm.sTr().sTh().add("Serial No.").eTh().sTd().add(challengeRec.serialNbr).eTd().eTr();
        htm.sTr().sTh().add("Date").eTh().sTd()
                .add(TemporalUtils.FMT_WMD.format(challengeRec.examDt)).eTd().eTr();
        htm.sTr().sTh().add("Time").eTh().sTd()
                .add(start == null ? "?" : TemporalUtils.FMT_HMS_A.format(start),
                        " to ", end == null ? "?" : TemporalUtils.FMT_HMS_A.format(end),
                        " (", Long.toString(min), ":00)")
                .eTd().eTr();
        htm.sTr().sTh().add("Score").eTh().sTd().add(challengeRec.score).eTd().eTr();
        htm.sTr().sTh().add("Passed").eTh().sTd().add(challengeRec.passed).eTd().eTr();

        htm.eTable();

        htm.sH(3).add("Exam Details:").eH(3);

        final File basePath1 = new File("/imp/data");
        final File basePath2 = new File("/impback");

        if (basePath1.isDirectory() || basePath2.isDirectory()) {

            File xmlPath = new File(basePath1, xml);
            File updPath = new File(basePath1, upd);

            if (!xmlPath.exists() || !updPath.exists()) {
                xmlPath = new File(basePath2, xml);
                updPath = new File(basePath2, upd);

                if (!xmlPath.exists() || !updPath.exists()) {
                    xmlPath = new File(basePath1, xml);
                    updPath = new File(basePath1, upd);
                }
            }

            final ExamObj exam = loadExam(xmlPath);

            if (exam == null) {
                htm.sP("error").add("(Unable to load exam XML file)").eP();
            } else {
                if (!loadUpdates(updPath, exam)) {
                    htm.sP("error").add("Unable to load submitted answers file - exam not submitted?").eP();
                }

                emitExam(htm, exam);
            }
        } else {
            htm.sP("error").add("(Unable to loacate exam directory)").eP();
        }

    }

    /**
     * Read the exam XML file.
     *
     * @param xmlFile the XML file to read
     * @return the loaded exam if successful; null otherwise
     */
    private static ExamObj loadExam(final File xmlFile) {

        ExamObj exam = null;

        // Read the file and convert to a string and character array
        final byte[] buffer = new byte[1024];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (final InputStream in = new FileInputStream(xmlFile)) {
            if (xmlFile.getName().endsWith(".Z")) {
                try (final InputStream zin = new GZIPInputStream(in)) {
                    for (int len = zin.read(buffer); len > 0; len = zin.read(buffer)) {
                        baos.write(buffer, 0, len);
                    }
                }
            } else {
                for (int len = in.read(buffer); len > 0; len = in.read(buffer)) {
                    baos.write(buffer, 0, len);
                }
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        final String str = baos.toString(StandardCharsets.UTF_8);
        final char[] data = str.toCharArray();

        // Convert the character array into the proper request type
        try {
            if (str.startsWith("<get-review-exam-reply>")) {
                exam = new GetReviewExamReply(data).presentedExam;
                if (exam == null) {
                    Log.warning("Unable to load ", xmlFile.getAbsolutePath());
                }
            } else if (str.startsWith("<get-exam-reply>")) {
                exam = new GetExamReply(data).presentedExam;
                if (exam == null) {
                    Log.warning("Unable to load ", xmlFile.getAbsolutePath());
                }
            } else {
                Log.warning("Unrecognized past exam type: ", str.substring(0, Math.min(100, str.length())));
            }
        } catch (final IllegalArgumentException ex) {
            Log.warning("Unable to load ", xmlFile.getAbsolutePath(), ex);
        }

        return exam;
    }

    /**
     * Read the updates file, rebuild the list of student answers, and apply them to the exam.
     *
     * @param updatesFile the updates file to read
     * @param exam        the exam to which to apply the updates
     * @return true if successful; false otherwise
     */
    private static boolean loadUpdates(final File updatesFile, final ExamObj exam) {

        // Log.info("Loading updates");

        boolean ok = false;

        // Read the file and convert to String
        final byte[] buffer = new byte[256];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (updatesFile.exists()) {
            try (final InputStream in = new FileInputStream(updatesFile)) {

                if (updatesFile.getName().endsWith(".Z")) {
                    try (final InputStream zin = new GZIPInputStream(in)) {
                        for (int len = zin.read(buffer); len > 0; len = zin.read(buffer)) {
                            baos.write(buffer, 0, len);
                        }
                    }
                } else {
                    for (int len = in.read(buffer); len > 0; len = in.read(buffer)) {
                        baos.write(buffer, 0, len);
                    }
                }
            } catch (final IOException ex) {
                Log.warning("Failed to read updates file", ex);
            }
        }

        final String str = baos.toString(StandardCharsets.UTF_8);

        // Convert to a series of lines, trimming away line numbers
        final List<String> data = new ArrayList<>(30);
        int num = 1;
        try (final BufferedReader br = new BufferedReader(new StringReader(str))) {
            String line = br.readLine();

            while (line != null) {
                final String test = num + ": ";

                if (!line.startsWith(test)) {
                    num = -1;
                    break;
                }

                data.add(line.substring(test.length()));
                line = br.readLine();
                ++num;
            }

            ok = num > 1;
        } catch (final IOException ex) {
            Log.warning("Failed to scan updates file", ex);
        }

        // Convert first line if updates file into "state" of exam
        if (ok) {
            final int numAns = data.size();
            final Object[][] ans = new Object[numAns][];
            ans[0] = new Object[4];

            String[] split = data.getFirst().split(CoreConstants.COMMA);

            if (split.length == 4) {
                ans[0][0] = Long.valueOf(0L);
                ans[0][1] = ans[0][0];
                ans[0][2] = ans[0][0];
                ans[0][3] = ans[0][0];
            } else {
                Log.warning("Updates file has invalid state line: ", data.getFirst());
                ok = false;
            }

            if (ok) {

                // First line can be ignored, but convert every other line into an answer in an answer array.
                for (num = 1; ok && num < numAns; ++num) {
                    final String test = data.get(num);

                    if ("(no answer)".equals(test)) {
                        continue;
                    }

                    // Break on comma boundaries
                    split = test.split(CoreConstants.COMMA);
                    final int splitLen = split.length;
                    if (splitLen == 0) {
                        continue;
                    }

                    // Now the tricky part - find out what data type the answers are and convert
                    // them back to answer objects

                    // Get the exam problem so we have some clue
                    final ExamProblem eprob = exam.getProblem(num);
                    final AbstractProblemTemplate prob = eprob.getSelectedProblem();

                    if (prob instanceof ProblemEmbeddedInputTemplate) {

                        if (split[0].contains("{") && split[0].contains("}")) {

                            // Type is set of embedded input parameters - we
                            // just store these in the answer list as strings
                            ans[num] = new String[splitLen];

                            for (int i = 0; i < splitLen; ++i) {
                                split[i] = split[i].trim();
                            }

                            ans[num] = split;
                        } else {
                            Log.warning("Embedded input answer " + num + " is not parameter list");
                            ok = false;
                        }
                    } else if (prob instanceof ProblemMultipleChoiceTemplate) {

                        if (splitLen == 1) {
                            final Long[] ints = new Long[1];

                            try {
                                ints[0] = Long.valueOf(split[0].trim());
                                ans[num] = ints;
                            } catch (final NumberFormatException e) {
                                Log.warning("Multiple choice answer " + num + " not integer" + split[0]);
                                ok = false;
                            }
                        } else {
                            Log.warning("Multiple choice answer " + num + " not length 1");
                            ok = false;
                        }
                    } else if (prob instanceof ProblemMultipleSelectionTemplate) {
                        final Long[] ints = new Long[splitLen];

                        try {
                            for (int i = 0; i < splitLen; ++i) {
                                ints[i] = Long.valueOf(split[i].trim());
                            }

                            ans[num] = ints;
                        } catch (final NumberFormatException e) {
                            Log.warning("Multiple selection answer " + num + " not integer");
                            ok = false;
                        }
                    } else if (prob instanceof ProblemNumericTemplate) {

                        // NOTE: answer could be like "$1,234.56", so split could break things
                        ans[num] = new String[]{test};
                    }

                    if (prob != null) {
                        prob.recordAnswer(ans[num]);

                        if (prob.isCorrect(ans[num])) {
                            prob.score = 1.0;
                        } else {
                            prob.score = 0.0;
                        }
                    }
                }
            }

            // If answers were loaded, apply them to the exam
            if (ok) {
                exam.importState(ans);
            }
        }

        return ok;
    }

    /**
     * Emits a representation of the completed exam.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param exam the exam object
     */
    private static void emitExam(final HtmlBuilder htm, final ExamObj exam) {

        htm.sP("indent").add("<strong>", exam.examName, "</strong> [", exam.examVersion, "] (", exam.course, ", Unit ",
                exam.courseUnit, ")").eP();

        if (exam.allowedSeconds != null) {
            final long min = exam.allowedSeconds.longValue() / 60L;
            htm.sP("indent").add("Time allowed: ", exam.allowedSeconds + " sec. (", Long.toString(min), " min)").eP();
        }

        final int[] id = {1};
        ExamObjConverter.populateExamHtml(exam, id);

        if (exam.instructionsHtml != null) {
            htm.hr().sDiv("indent").add(exam.instructionsHtml).eDiv();
        }

        htm.hr();
        final int numSect = exam.getNumSections();
        for (int i = 0; i < numSect; ++i) {
            final ExamSection sect = exam.getSection(i);
            if (numSect > 1) {
                htm.sP("indent").add("Section ", Integer.toString(i + 1), ": <strong>", sect.sectionName, "</strong>")
                        .eP();
            }

            final int numProb = sect.getNumProblems();
            for (int j = 0; j < numProb; ++j) {
                final ExamProblem prob = sect.getPresentedProblem(j);

                final AbstractProblemTemplate selected = prob.getSelectedProblem();
                boolean correct = false;
                if (selected != null) {
                    correct = selected.isCorrect(selected.getAnswer());
                }

                htm.sP("indent").add("<strong>", prob.problemName, "</strong>",
                        (correct ? " (correct)" : " (incorrect)")).eP();

                if (selected != null) {
                    ProblemConverter.populateProblemHtml(selected, id);
                    htm.sDiv("indent2").add(selected.insertAnswers(selected.answerHtml)).eDiv();
                }
                htm.hr();
            }
        }
    }
}
