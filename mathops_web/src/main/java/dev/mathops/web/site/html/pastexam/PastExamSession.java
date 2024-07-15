package dev.mathops.web.site.html.pastexam;

import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
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
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.xml.XmlEscaper;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.GetReviewExamReply;
import dev.mathops.web.site.html.HtmlSessionBase;

import jakarta.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * A user session used to take review exams online. It takes as arguments a session ID, student name, and assignment ID
 * and presents the exam to the student.
 */
public final class PastExamSession extends HtmlSessionBase {

    /** The timeout duration (1 hour), in milliseconds. */
    private static final long TIMEOUT = 60L * 60L * 1000L;

    /** The XML filename. */
    public final String xmlFilename;

    /** The state of the past exam. */
    private EPastExamState state;

    /** Error message, used when exam cannot be loaded. */
    private String error;

    /** The currently active item. */
    private int currentItem;

    /** Timestamp when exam will time out. */
    private long timeout;

    /**
     * Constructs a new {@code PastExamSession}. This is called when the user clicks a button to view a past exam. It
     * stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the site profile
     * @param theSessionId     the session ID
     * @param theExamId        the exam ID
     * @param theXmlFilename   the XML filename
     * @param theStudentId     the student ID
     * @param theRedirectOnEnd the URL to which to redirect at the end of the exam
     * @throws SQLException if there is an error accessing the database
     */
    public PastExamSession(final Cache cache, final WebSiteProfile theSiteProfile,
                           final String theSessionId, final String theExamId, final String theXmlFilename,
                           final String theStudentId, final String theRedirectOnEnd) throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExamId, theRedirectOnEnd);

        if (theXmlFilename == null) {
            throw new IllegalArgumentException("XML Filename may not be null");
        }

        this.xmlFilename = theXmlFilename;
        this.state = EPastExamState.INITIAL;
        this.error = null;
        this.currentItem = -1;
        this.timeout = System.currentTimeMillis() + TIMEOUT;
    }

    /**
     * Constructs a new {@code PastExamSession}. This is called when the user clicks a button to view a past exam. It
     * stores data but does not generate the HTML until the page is actually generated.
     *
     * @param cache            the data cache
     * @param theSiteProfile   the site profile
     * @param theSessionId     the session ID
     * @param theXmlFilename   the XML filename
     * @param theStudentId     the student ID
     * @param theRedirectOnEnd the URL to which to redirect at the end of the exam
     * @param theState         the session state
     * @param theError         the grading error
     * @param theCurrentItem   the current item
     * @param theTimeout       the timeout
     * @param theExam          the exam
     * @throws SQLException if there is an error accessing the database
     */
    PastExamSession(final Cache cache, final WebSiteProfile theSiteProfile,
                    final String theSessionId, final String theXmlFilename, final String theStudentId,
                    final String theRedirectOnEnd, final EPastExamState theState, final String theError,
                    final int theCurrentItem, final long theTimeout, final ExamObj theExam)
            throws SQLException {

        super(cache, theSiteProfile, theSessionId, theStudentId, theExam.examVersion,
                theRedirectOnEnd);

        if (theXmlFilename == null) {
            throw new IllegalArgumentException("XML Filename may not be null");
        }

        this.xmlFilename = theXmlFilename;
        this.state = theState;
        this.error = theError;
        this.currentItem = theCurrentItem;
        this.timeout = theTimeout;
        setExam(theExam);
    }

    /**
     * Gets the exam state.
     *
     * @return the exam state
     */
    public EPastExamState getState() {

        return this.state;
    }

    /**
     * Gets the current item.
     *
     * @return the current item
     */
    public int getItem() {

        return this.currentItem;
    }

    /**
     * Gets the time remaining in the exam.
     *
     * @return the time remaining, in milliseconds (0 if the exam has not been started)
     */
    public long getTimeRemaining() {

        return this.timeout == 0L ? 0L : this.timeout - System.currentTimeMillis();
    }

    /**
     * Tests whether this session is timed out.
     *
     * @return {@code true} if timed out
     */
    public boolean isTimedOut() {

        return System.currentTimeMillis() >= this.timeout;
    }

    /**
     * Generates HTML for the exam based on its current state.
     *
     * @param now the date/time to consider "now"
     * @param xml the relative path of the XML file
     * @param upd the relative path of the update file
     * @param htm the {@code HtmlBuilder} to which to append
     */
    public void generateHtml(final ZonedDateTime now, final String xml, final String upd, final HtmlBuilder htm) {

        this.timeout = System.currentTimeMillis() + TIMEOUT;

        switch (this.state) {
            case INITIAL:
                doInitial(now, xml, upd, htm);
                break;

            case CANT_LOAD_EXAM:
                appendError(htm);
                break;

            case INSTRUCTIONS:
                appendInstructionsHtml(htm);
                break;

            case ITEM_NN:
                appendExamHtml(htm);
                break;

            default:
                appendHeader(htm);
                htm.addln("<div style='text-align:center; color:navy;'>").add("Unsupported state.").eDiv();
                appendFooter(htm, "close", "Close", null, null, null, null);
                htm.eDiv(); // outer DIV from header
                break;
        }
    }

    /**
     * Processes a request for the page while in the INITIAL state, which generates the assignment, then sends its
     * HTML.
     *
     * @param now the date/time to consider "now"
     * @param xml the relative path of the XML file
     * @param upd the relative path of the update file
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void doInitial(final ZonedDateTime now, final String xml, final String upd,
                           final HtmlBuilder htm) {

        // Verify presence of all required files
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

            if (xmlPath.exists()) {
                if (updPath.exists()) {
                    if (loadExam(xmlPath)) {
                        if (loadUpdates(updPath, getExam())) {

                            populateScores();

                            if (getExam().instructions == null) {
                                this.currentItem = 0;
                                this.state = EPastExamState.ITEM_NN;
                            } else {
                                this.state = EPastExamState.INSTRUCTIONS;
                            }
                        } else {
                            this.error = "Unable to load exam answers.";
                            this.state = EPastExamState.CANT_LOAD_EXAM;
                            Log.warning(this.error);
                        }
                    } else {
                        this.error = "Unable to load exam record.";
                        this.state = EPastExamState.CANT_LOAD_EXAM;
                        Log.warning(this.error);
                    }
                } else {
                    this.error = "Requested exam answers not found.";
                    this.state = EPastExamState.CANT_LOAD_EXAM;
                    Log.warning(this.error);
                    Log.warning("  Missing UPD path is ", updPath);
                }
            } else {
                this.error = "Requested exam not found.";
                this.state = EPastExamState.CANT_LOAD_EXAM;
                Log.warning(this.error);
                Log.warning("  Missing XML path is ", xmlPath);
            }
        } else {
            this.error = "No student exam data found on server.";
            this.state = EPastExamState.CANT_LOAD_EXAM;
            Log.warning(this.error);
        }

        generateHtml(now, xml, upd, htm);
    }

    /**
     * Populates scores on items in the exam.
     */
    private void populateScores() {

        final int numSect = getExam().getNumSections();
        for (int sectIndex = 0; sectIndex < numSect; ++sectIndex) {

            final ExamSection examSect = getExam().getSection(sectIndex);

            final int numProb = examSect.getNumProblems();
            for (int p = 0; p < numProb; ++p) {

                final ExamProblem examProb = examSect.getProblem(p);

                final AbstractProblemTemplate ap = examProb.getSelectedProblem();
                if (ap == null) {
                    Log.warning("Selected problem is null");
                } else if (ap.isAnswered() && ap.isCorrect(ap.getAnswer()) && ap.score == 0.0) {
                    ap.score = 1.0;
                }
            }
        }
    }

    /**
     * Read the exam XML file.
     *
     * @param xmlFile the XML file to read
     * @return true if successful; false otherwise
     */
    private boolean loadExam(final File xmlFile) {

//        Log.info("Loading completed exam");

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
                setExam(new GetReviewExamReply(data).presentedExam);
                if (getExam() == null) {
                    Log.warning("Unable to load ", xmlFile.getAbsolutePath());
                }
            } else if (str.startsWith("<get-exam-reply>")) {
                setExam(new GetExamReply(data).presentedExam);
                if (getExam() == null) {
                    Log.warning("Unable to load ", xmlFile.getAbsolutePath());
                }
            } else {
                Log.warning("Unrecognized past exam type: ",
                        str.substring(0, Math.min(100, str.length())));
                setExam(null);
            }
        } catch (final IllegalArgumentException ex) {
            Log.warning("Unable to load ", xmlFile.getAbsolutePath(), ex);
            setExam(null);
        }

        return getExam() != null;
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
     * Appends the HTML for the exam instructions.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendError(final HtmlBuilder htm) {

        appendHeader(htm);
        startMain(htm);

        htm.sP("center").add(this.error).eP();

        endMain(htm);
        appendFooter(htm, "close", "Close", null, null, null, null);
        htm.eDiv(); // outer DIV from header
    }

    /**
     * Appends the HTML for the exam instructions.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendInstructionsHtml(final HtmlBuilder htm) {

        appendHeader(htm);
        appendNav(htm);
        startMain(htm);

        if (getExam().instructionsHtml == null && getExam().instructions != null) {
            ExamObjConverter.populateExamHtml(getExam(), new int[]{1});
        }

        if (getExam().instructionsHtml != null) {
            htm.addln(getExam().instructionsHtml);
        }

        endMain(htm);
        appendFooter(htm, "close", "Close", null, null, "nav_0", "Review question 1");
        htm.eDiv(); // outer DIV from header
    }

    /**
     * Appends the HTML for the exam, showing the current item.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendExamHtml(final HtmlBuilder htm) {

        appendHeader(htm);
        appendNav(htm);
        startMain(htm);

        final ExamSection sect = getExam().getSection(0);
        final ExamProblem ep = sect.getPresentedProblem(this.currentItem);
        if (ep == null) {
            htm.addln("No presented problem.");
        } else {
            final AbstractProblemTemplate p = ep.getSelectedProblem();
            if (p == null) {
                htm.addln("No selected problem.");
            } else {
                if (p.questionHtml == null) {
                    ProblemConverter.populateProblemHtml(p, new int[]{1});
                }
                htm.addln(p.insertAnswers(p.solutionHtml));
            }
        }

        final String prevCmd = this.currentItem == 0 ? null : "nav_" + (this.currentItem - 1);
        final String nextCmd = this.currentItem >= (sect.getNumProblems() - 1) ? null : "nav_" + (this.currentItem + 1);

        endMain(htm);
        appendFooter(htm, "close", "Close",
                prevCmd, "Review Question " + (this.currentItem), //
                nextCmd, "Review Question " + (this.currentItem + 2));
        htm.eDiv(); // outer DIV from header
    }

    /**
     * Appends the header.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHeader(final HtmlBuilder htm) {

        htm.sDiv(null, "style='display:flex; flex-flow:row wrap; margin:0 6px 12px 6px;'");

        htm.addln("<div style='flex: 1 100%; order:1; display:block; ",
                "background-color:AliceBlue; border:1px solid SteelBlue; margin:1px;'>");

        htm.add("<h1 style='text-align:center; font-family:sans-serif; font-size:18pt; ",
                "font-weight:bold; color:#36648b; text-shadow:2px 1px #ccc; margin:0; padding:4pt;'>");

        if (getExam() != null) {
            htm.add(getExam().examName);
        }

        htm.eH(1);

        htm.eDiv();
    }

    /**
     * Starts the "main" section.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void startMain(final HtmlBuilder htm) {

        htm.addln("<main style='flex:1 1 73%; order:3; margin:1px; padding:2px; border:1px solid SteelBlue;'>");

        htm.addln("<div style='padding:8px; min-height:100%; border:1px solid #b3b3b3; ",
                "background:#8f8f8; font-family:serif; font-size:",
                Float.toString(AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE), "px;'>");
    }

    /**
     * Ends the "main" section.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endMain(final HtmlBuilder htm) {

        htm.eDiv().addln("</main>");
    }

    /**
     * Appends the navigation section.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendNav(final HtmlBuilder htm) {

        htm.addln("<script>");
        htm.addln("function invokeAct(action) {");
        htm.addln("  document.getElementById(\"past_exam_act\").value = action;");
        htm.addln("  document.getElementById(\"past_exam_form\").submit();");
        htm.addln("}");
        htm.addln("</script>");

        // htm.addln("<nav style='grid-area:navigation; display:block;
        // background-color:AliceBlue;",
        // "border:1px solid SteelBlue; margin:1px; padding:6pt; font-size:14pt;'>");

        htm.addln("<nav style='flex:1 1 22%; order:2; display:block; background-color:AliceBlue; ",
                "border:1px solid SteelBlue; margin:1px; padding:6pt; font-size:14pt;'>");

        if (this.state == EPastExamState.INSTRUCTIONS) {
            htm.addln("<div style='background:#7FFF7F;'>");
        } else {
            htm.sDiv();
        }

        htm.add("<a style='font-family:serif;' href='javascript:invokeAct(\"instruct\");'>Instructions</a>");
        htm.eDiv();

        final ExamSection sect = getExam().getSection(0);
        if (sect.shortName == null) {
            htm.addln("<h2 style='padding:6pt 0 3pt 0;color:SteelBlue'>", sect.sectionName).eH(2);
        } else {
            htm.addln("<h2 style='padding:6pt 0 3pt 0;color:SteelBlue'>", sect.shortName).eH(2);
        }

        final int numProblems = sect.getNumProblems();

        for (int p = 0; p < numProblems; ++p) {
            final ExamProblem ep = sect.getPresentedProblem(p);

            if (this.currentItem == p && this.state == EPastExamState.ITEM_NN) {
                htm.addln("<div style='background:#7FFF7F;'>");
            } else {
                htm.sDiv();
            }

            if (this.state == EPastExamState.ITEM_NN || this.state == EPastExamState.INSTRUCTIONS) {
                // When interacting or instructions, mark the ones that were correct
                if (ep.getSelectedProblem().score == (double) 0) {
                    htm.add("<img src='images/redx.png'> ");
                } else {
                    htm.add("<img src='images/check.png'> ");
                }
            }

            htm.add("<a style='font-family:serif;' href='javascript:invokeAct(\"nav_", Integer.toString(p), "\");'>");
            if (ep.problemName == null) {
                htm.add(Integer.valueOf(p + 1));
            } else {
                htm.add(ep.problemName);
            }
            htm.addln("</a>");
            htm.eDiv();
        }

        htm.addln("</nav>");
    }

    /**
     * Called when a POST is received on the page hosting the review exam.
     *
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @return a URL to which to redirect; {@code null} to present the generated HTML
     */
    public String processPost(final ImmutableSessionInfo session, final ServletRequest req, final HtmlBuilder htm) {

        String redirect = null;

        switch (this.state) {
            case INSTRUCTIONS:
                redirect = processPostInstructions(session, req, htm);
                break;

            case ITEM_NN:
                redirect = processPostInteracting(session, req, htm);
                break;

            case CANT_LOAD_EXAM:
                redirect = processPostError(session, req, htm);
                break;

            case INITIAL:
            default:
                generateHtml(session.getNow(), null, null, htm);
                break;
        }

        return redirect;
    }

    /**
     * Called when a POST is received while in the INSTRUCTIONS state.
     *
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @return a string to which to redirect; null if no redirection should occur
     */
    private String processPostInstructions(final ImmutableSessionInfo session, final ServletRequest req,
                                           final HtmlBuilder htm) {

        String redirect = null;

        // Log.info("req.getParameter(\"close\") = " + req.getParameter("close"));

        if (req.getParameter("close") != null) {
            final PastExamSessionStore store = PastExamSessionStore.getInstance();
            store.removePastExamSession(session.loginSessionId, this.xmlFilename);

            setExam(null);

            redirect = this.redirectOnEnd;
        } else {
            final String act = req.getParameter("action");

            // Navigation ...
            final ExamSection sect = getExam().getSection(0);
            final int numProblems = sect.getNumProblems();
            for (int i = 0; i < numProblems; ++i) {
                if (("nav_" + i).equals(act)) {
                    this.currentItem = i;
                    this.state = EPastExamState.ITEM_NN;
                    break;
                }
            }

            generateHtml(session.getNow(), null, null, htm);
        }

        return redirect;
    }

    /**
     * Called when a POST is received while in the INTERACTING state.
     *
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @return a string to which to redirect; null if no redirection should occur
     */
    private String processPostInteracting(final ImmutableSessionInfo session, final ServletRequest req,
                                          final HtmlBuilder htm) {

        String redirect = null;

        // Log.info("req.getParameter(\"close\") = " + req.getParameter("close"));

        if (req.getParameter("close") != null) {
            final PastExamSessionStore store = PastExamSessionStore.getInstance();
            store.removePastExamSession(session.loginSessionId, this.xmlFilename);

            setExam(null);

            redirect = this.redirectOnEnd;
        } else {
            final String act = req.getParameter("action");

            if ("instruct".equals(act)) {
                this.state = EPastExamState.INSTRUCTIONS;
            } else {
                // Navigation ...
                final ExamSection sect = getExam().getSection(0);
                final int numProblems = sect.getNumProblems();
                for (int i = 0; i < numProblems; ++i) {
                    if (("nav_" + i).equals(act)) {
                        this.currentItem = i;
                        break;
                    }
                }
            }

            generateHtml(session.getNow(), null, null, htm);
        }

        return redirect;
    }

    /**
     * Called when a POST is received while in the CANT_LOAD_EXAM state.
     *
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @return a string to which to redirect; null if no redirection should occur
     */
    private String processPostError(final ImmutableSessionInfo session,
                                    final ServletRequest req, final HtmlBuilder htm) {

        String redirect = null;

        // Log.info("req.getParameter(\"close\") = " + req.getParameter("close"));

        if (req.getParameter("close") != null) {
            final PastExamSessionStore store = PastExamSessionStore.getInstance();
            store.removePastExamSession(session.loginSessionId, this.xmlFilename);

            if (getExam() != null) {
                setExam(null);
            }

            redirect = this.redirectOnEnd;
        } else {
            generateHtml(session.getNow(), null, null, htm);
        }

        return redirect;
    }

    /**
     * Appends the XML representation of this session to an {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    void appendXml(final HtmlBuilder xml) {

        if (getExam() != null) {
            xml.addln("<past-exam-session>");
            xml.addln(" <host>", getSiteProfile().host, "</host>");
            xml.addln(" <path>", getSiteProfile().path, "</path>");
            xml.addln(" <session>", this.sessionId, "</session>");
            xml.addln(" <xml>", this.xmlFilename, "</xml>");
            xml.addln(" <student>", this.studentId, "</student>");
            xml.addln(" <state>", this.state.name(), "</state>");
            if (this.error != null) {
                xml.addln(" <error>", this.error, "</error>");
            }
            xml.addln(" <cur-item>", Integer.toString(this.currentItem), "</cur-item>");
            xml.addln(" <redirect>", XmlEscaper.escape(this.redirectOnEnd), "</redirect>");
            xml.addln(" <timeout>", Long.toString(this.timeout), "</timeout>");
            getExam().appendXml(xml, 1);

            final int numSect = getExam().getNumSections();
            for (int i = 0; i < numSect; ++i) {
                final ExamSection sect = getExam().getSection(i);
                final int numProb = sect.getNumProblems();
                for (int j = 0; j < numProb; ++j) {
                    final ExamProblem prob = sect.getProblem(j);
                    if (prob != null) {
                        final AbstractProblemTemplate selected = prob.getSelectedProblem();
                        if (selected != null) {
                            xml.addln(" <selected-problem sect='", Integer.toString(i), "' prob='",
                                    Integer.toString(j), "'>");
                            selected.appendXml(xml, 2);
                            xml.addln(" </selected-problem>");
                        }
                    }
                }
            }
            xml.addln("</past-exam-session>");
        }
    }

    /**
     * Performs a forced abort of a past exam session.
     *
     * @param session the login session requesting the forced abort
     */
    public void forceAbort(final ImmutableSessionInfo session) {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            appendExamLog("Forced abort requested");
            final PastExamSessionStore store = PastExamSessionStore.getInstance();
            store.removePastExamSession(this.sessionId, this.xmlFilename);

            if (getExam() != null) {
                setExam(null);
            }
        } else {
            appendExamLog("Forced abort requested, but requester is not ADMINISTRATOR");
        }
    }
}
