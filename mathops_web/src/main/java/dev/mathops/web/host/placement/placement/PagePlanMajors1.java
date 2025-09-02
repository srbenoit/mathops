package dev.mathops.web.host.placement.placement;

import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Contexts;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.mathplan.MathPlanConstants;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.StudentMathPlan;
import dev.mathops.db.logic.mathplan.majors.Major;
import dev.mathops.db.logic.mathplan.majors.Majors;
import dev.mathops.db.logic.mathplan.majors.MajorsCurrent;
import dev.mathops.db.logic.mathplan.types.EMajorTrack;
import dev.mathops.db.schema.legacy.impl.RawStmathplanLogic;
import dev.mathops.db.schema.legacy.rec.RawStmathplan;
import dev.mathops.db.schema.legacy.rec.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Generates the page that presents majors, organized by area of study.
 */
enum PagePlanMajors1 {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final StudentData studentData = cache.getStudent(stuId);
        final RawStudent student = studentData.getStudentRecord();

        final StudentMathPlan plan = MathPlanLogic.queryPlan(cache, stuId);
        final Map<Integer, RawStmathplan> majorsResponses = plan.stuStatus.majorsResponses;

        final Major declaredMajor = Majors.getMajorByProgramCode(student.programCode);
        final List<Major> majors = MajorsCurrent.INSTANCE.getMajors();

        final Map<Integer, Major> majorMap = new HashMap<>(300);
        for (final Major major : majors) {
            for (final int q : major.questionNumbers) {
                final Integer qObj = Integer.valueOf(q);
                majorMap.put(qObj, major);
            }
        }

        // Gather the set of selected majors and concentrations
        final Collection<Major> selectedMajors = new HashSet<>(10);
        if (declaredMajor != null) {
            selectedMajors.add(declaredMajor);
        }
        for (final Major major : majors) {
            final Integer key = Integer.valueOf(major.questionNumbers[0]);
            final RawStmathplan curResp = majorsResponses.get(key);
            if (curResp != null && "Y".equals(curResp.stuAnswer)) {
                selectedMajors.add(major);
            }
        }

        final boolean disable = session.actAsUserId != null;

        final HtmlBuilder htm = new HtmlBuilder(8192);
        final String title = site.getTitle();
        Page.startNofooterPage(htm, title, session, true, Page.NO_BARS, null, false, false);

        MPPage.emitMathPlanHeader(htm);

        MathPlacementSite.emitLoggedInAs2(htm, session);

        htm.addln("<script>");
        htm.addln(" function toggleMajor(input,id) {");
        htm.addln("  var checked = input.checked;");
        htm.addln("  var elms = document.getElementsByName(id);");
        htm.addln("  for (i=0;i<elms.length;i++) {");
        htm.addln("   elms[i].checked = checked;");
        htm.addln("  }");
        htm.addln(" }");
        htm.addln("</script>");

        htm.add("<form id='moi-form' action='plan_majors1.html' method='post'>");

        htm.sDiv("inset2");

        htm.sDiv("shaded2left");

        htm.sP().add("Let us know what majors you think you might choose.  You can change your selections later ",
                "if you change your mind.").eP();

        htm.sP().add("Choosing a major here does not declare it as your actual major - you can try out different ",
                "majors here and see how they affect your Math Plan.").eP();

        final Major exploratory = majorMap.get(Integer.valueOf(9000));
        final String progCode = exploratory.programCodes.getFirst();

        htm.sDiv("indent center");

        htm.add("<input type='checkbox'");
        if (selectedMajors.contains(exploratory)) {
            htm.add(" checked='checked'");
        }
        if (disable) {
            htm.add(" disabled='disabled'");
        }
        htm.add("name='", progCode, "' id='", progCode, "' onchange=\"toggleMajor(this,'", progCode, "');\"/>");

        htm.add("<label for='", progCode, "'>");
        htm.add("I don't know yet what majors I'm interested in - assume <em>Exploratory Studies</em> ");
        if (exploratory.catalogPageUrl != null) {
            htm.add("<span style='white-space:nowrap;'>&nbsp;<a target='_blank' href='", exploratory.catalogPageUrl,
                    "'><img style='position:relative;top:-1px' src='/images/welcome/catalog3.png'/></a></span>");
        }
        htm.addln("</label>");
        htm.eDiv(); // center

        htm.eDiv();

        htm.div("vgap");

        htm.sDiv("folders");

        htm.add("<nav class='folderstabs'>");
        htm.sDiv("tab selected", "id='first'")
                .add("group by area of study")
                .eDiv();
        htm.sDiv("tab unsel", "id='last'")
                .add("<a href='plan_majors2.html'>sort majors alphabetically</a>")
                .eDiv();
        htm.add("</nav>");

        htm.sDiv("folder-content");
        emitMajorsSelectionForm(htm, majorsResponses, student, session);
        htm.eDiv();

        htm.eDiv(); // folders

        htm.eDiv(); // inset2
        htm.addln("</form>");

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }

    private static final String AHD = "AHD";
    private static final String ET = "ET";
    private static final String ENR = "ENR";
    private static final String GSS = "GSS";
    private static final String HLF = "HLF";
    private static final String LPA = "LPA";
    private static final String MSE = "MSE";
    private static final String OME = "OME";

    /**
     * Emits the selection of possible majors, with any current selections checked.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param curResponses the current student responses (empty if not yet responded)
     * @param student      the student record
     * @param session      the session
     */
    private static void emitMajorsSelectionForm(final HtmlBuilder htm, final Map<Integer, RawStmathplan> curResponses,
                                                final RawStudent student, final ImmutableSessionInfo session) {

        final boolean disable = session.actAsUserId != null;
        final Major declaredMajor = Majors.getMajorByProgramCode(student.programCode);

        final List<Major> majors = MajorsCurrent.INSTANCE.getMajors();

//        final Map<Integer, Major> majorMap = new HashMap<>(300);
//        for (final Major major : majors) {
//            for (final int q : major.questionNumbers) {
//                final Integer qObj = Integer.valueOf(q);
//                majorMap.put(qObj, major);
//            }
//        }

        // Gather the set of selected majors and concentrations
        final Collection<Major> selectedMajors = new HashSet<>(10);
        if (declaredMajor != null) {
            selectedMajors.add(declaredMajor);
        }
        for (final Major major : majors) {
            final Integer key = Integer.valueOf(major.questionNumbers[0]);
            final RawStmathplan curResp = curResponses.get(key);
            if (curResp != null && "Y".equals(curResp.stuAnswer)) {
                selectedMajors.add(major);
            }
        }

        int numSelected = 0;

        for (final EMajorTrack track : EMajorTrack.values()) {
            htm.addln("<details>");
            htm.addln("<summary class='study-area'>", track.label, "</summary>");
            htm.sDiv("indent");
            for (final Major major : majors) {
                if (major.majorTracks.contains(track)) {
                    numSelected += emitMajor(htm, track, major, selectedMajors, disable, declaredMajor);
                }
            }
            htm.eDiv(); // indent
            htm.addln("</details>");
        }

        final boolean showUpdate = numSelected > 0 && !curResponses.isEmpty();

        htm.div("vgap");
        htm.sDiv("center");

        if (disable) {
            final String label = Res.get(Res.SECURE_NEXT_STEP_BTN);
            htm.add("<a class='btn' href='plan_record.html'>", label, "</a>");
        } else {
            final String label = Res.get(showUpdate ? Res.SECURE_UPDATE_MAJORS_BTN : Res.SECURE_SUBMIT_MAJORS_BTN);
            htm.add("<a class='btn' href='javascript:;' ",
                    "onclick='document.getElementById(\"moi-form\").submit();'>", label, "</a>");
        }

        final String msg = Res.get(Res.EXPLORE_MAJORS_BTN);
        htm.addln(
                "<a class='btn' href='https://www.math.colostate.edu/placement/Math_Requirements.pdf' target='_blank'>",
                msg, "</a>");

        htm.eDiv();
    }

    /**
     * Emits a single major.
     *
     * @param htm            the {@code HtmlBuilder} to which to append
     * @param track          the major track
     * @param major          the major
     * @param selectedMajors the set of majors the student has selected on this question
     * @param disable        true if the form is disabled (when "Acting as" another user)
     * @param declaredMajor  the student's declared major
     * @return 1 if the major is selected; 0 if not
     */
    private static int emitMajor(final HtmlBuilder htm, final EMajorTrack track, final Major major,
                                 final Collection<Major> selectedMajors, final boolean disable,
                                 final Major declaredMajor) {

        final String progCode = major.programCodes.getFirst();

        htm.add("<div class='major'>");
        htm.add("<input type='checkbox'");

        // Now display any concentrations associated with the major in a DIV
        final boolean selected = selectedMajors.contains(major);
        if (selected) {
            htm.add(" checked='checked'");
        }
        if (disable) {
            htm.add(" disabled='disabled'");
        }

        htm.add("name='", progCode, "' id='", progCode, track.key, "' onchange=\"toggleMajor(this,'", progCode,
                "');\"/>");
        htm.add("<label for='", progCode, "'>");

        final String mname = major.programName;

        final int lastMSpace = mname.lastIndexOf(' ');
        if (lastMSpace == -1) {
            htm.add("<span style='white-space:nowrap;'>", mname);
        } else {
            final String firstPart = mname.substring(0, lastMSpace + 1);
            final String lastPart = mname.substring(lastMSpace + 1);
            htm.add(firstPart, "<span style='white-space:nowrap;'>", lastPart);
        }

        if (major.catalogPageUrl != null) {
            htm.add("<span style='white-space:nowrap;'>&nbsp;<a target='_blank' href='", major.catalogPageUrl,
                    "'><img style='position:relative;top:-1px' src='/images/welcome/catalog3.png'/></a></span>");
        }

        if (major.equals(declaredMajor)) {
            htm.add("<strong class='red'> *</strong>");
        }

        htm.add("</span>");
        htm.addln("</label>");

        htm.eDiv();

        return selected ? 1 : 0;
    }

    /**
     * Called when a POST is received to the page.
     *
     * @param cache       the data cache
     * @param siteProfile the site profile
     * @param req         the request
     * @param resp        the response
     * @param session     the session
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final Site siteProfile, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session) throws SQLException {

        final String stuId = session.getEffectiveUserId();
        final StudentData studentData = cache.getStudent(stuId);
        final RawStudent student = studentData.getStudentRecord();

        final ZonedDateTime now = session.getNow();

        // Only perform updates if data present AND this is not an adviser using "Act As"
        if (session.actAsUserId == null) {

            final Map<String, String[]> params = req.getParameterMap();

            // Build the list of new responses
            final int numParams = params.size();
            final List<Integer> questions = new ArrayList<>(numParams);
            final List<String> answers = new ArrayList<>(1);

            final List<Major> allMajors = MajorsCurrent.INSTANCE.getMajors();
            for (final Major major : allMajors) {
                final List<String> programCodes = major.programCodes;

                for (final String key : params.keySet()) {
                    final int dot = key.indexOf('.');
                    final String test = key.substring(dot + 1);
                    if (programCodes.contains(test)) {
                        final Integer firstNumber = Integer.valueOf(major.questionNumbers[0]);
                        questions.add(firstNumber);
                        answers.add("Y");
                    }
                }
            }

            if (!questions.isEmpty()) {
                RawStmathplanLogic.deleteAllForPage(cache, student.stuId, MathPlanConstants.MAJORS_PROFILE);
                MathPlanLogic.storeMathPlanResponses(cache, student, MathPlanConstants.MAJORS_PROFILE, questions,
                        answers, now, session.loginSessionTag);
            }
        }

        // Redirect to this page, so we show it with a "GET" (and page reloads won't repost)
        resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        final String path = siteProfile.path;
        final boolean hasTrailingSlash = path.endsWith(Contexts.ROOT_PATH);
        resp.setHeader("Location", path + (hasTrailingSlash ? "plan_record.html" : "/plan_record.html"));
    }
}
