package dev.mathops.web.host.placement.placement;

import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.logic.mathplan.EEligibility;
import dev.mathops.db.logic.mathplan.Major;
import dev.mathops.db.logic.mathplan.Majors;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.MathPlanConstants;
import dev.mathops.db.logic.mathplan.MathPlanStudentData;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStudent;
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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates the page that presents majors, organized alphabetically.
 */
enum PagePlanMajors2 {
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

        final Profile profile = site.site.profile;
        final MathPlanLogic logic = new MathPlanLogic(profile);

        final String stuId = session.getEffectiveUserId();
        final ZonedDateTime now = session.getNow();

        final RawStudent student = RawStudentLogic.query(cache, stuId, false);

        final MathPlanStudentData data = new MathPlanStudentData(cache, student, logic, now, session.loginSessionTag,
                session.actAsUserId == null);

        final HtmlBuilder htm = new HtmlBuilder(8192);
        final String title = site.getTitle();
        Page.startNofooterPage(htm, title, session, true, Page.NO_BARS, null, false, false);

        MPPage.emitMathPlanHeader(htm);

        MathPlacementSite.emitLoggedInAs2(htm, session);
        htm.sDiv("inset2");

        htm.sDiv("shaded2left");
        htm.sP().add("Let us know what majors you think you might choose.  You can change ",
                "your selections later if you change your mind.").eP();

        htm.sP().add("Choosing a major here does not declare it as your actual major - you can ",
                "try out different majors here and see how they affect your Math Plan.").eP();

        htm.sP().add("If you don't know what majors you might be interested in yet, you can choose ",
                "<em>Exploratory Studies</em> and move on.").eP();
        htm.eDiv();

        htm.div("vgap");

        htm.sDiv("folders");

        htm.add("<nav class='folderstabs'>");
        htm.sDiv("tab unsel", "id='first'").add("<a href='plan_majors1.html'>group by area of study</a>").eDiv();
        htm.sDiv("tab selected", "id='last'").add("sort majors alphabetically").eDiv();
        htm.add("</nav>");

        htm.sDiv("folder-content");
        final Map<Integer, RawStmathplan> majorProfileResponses = data.getMajorProfileResponses();
        emitMajorsSelectionForm(htm, majorProfileResponses, data, session);
        htm.eDiv();

        htm.eDiv(); // folders

        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Emits the selection of possible majors, with any current selections checked.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param curResponses the current student responses (empty if not yet responded)
     * @param data         the student data
     * @param session      the session
     */
    private static void emitMajorsSelectionForm(final HtmlBuilder htm, final Map<Integer, RawStmathplan> curResponses,
                                                final MathPlanStudentData data, final ImmutableSessionInfo session) {

        final boolean disable = session.actAsUserId != null;

        final Major declaredMajor = Majors.INSTANCE.getMajor(data.student.programCode);

        htm.addln("<script>");
        htm.addln(" function toggleCategory(input,id) {");
        htm.addln("  if (input.checked) {");
        htm.addln("    document.getElementById(id).style.display='block';");
        htm.addln("  } else {");
        htm.addln("    document.getElementById(id).style.display='none';");
        htm.addln("  }");
        htm.addln(" }");
        htm.addln(" function toggleConc(input,id) {");
        htm.addln("  var elms = document.getElementsByClassName(id);");
        htm.addln("  if (input.checked) {");
        htm.addln("   document.getElementById(id).style.display='block';");
        htm.addln("   for (i=0;i<elms.length;i++) {");
        htm.addln("    if (elms[i].classList.contains(\"autocheck\")) {");
        htm.addln("     elms[i].checked = true;");
        htm.addln("    }");
        htm.addln("   }");
        htm.addln("  } else {");
        htm.addln("   document.getElementById(id).style.display='none';");
        htm.addln("   for (i=0;i<elms.length;i++) {");
        htm.addln("    elms[i].checked = false;");
        htm.addln("   }");
        htm.addln("  }");
        htm.addln(" }");
        htm.addln("</script>");

        htm.add("<form id='moi-form' action='plan_majors2.html' method='post'>");

        final List<Major> allMajors = Majors.INSTANCE.getMajors();

        // Gather the set of selected majors and concentrations
        final Collection<Major> selectedMajors = new HashSet<>(10);
        if (declaredMajor != null) {
            selectedMajors.add(declaredMajor);
        }
        for (final Major major : allMajors) {
            final RawStmathplan curResp = curResponses.get(Integer.valueOf(major.questionNumbers[0]));
            if (curResp != null && "Y".equals(curResp.stuAnswer)) {
                selectedMajors.add(major);
            }
        }

        final Set<EEligibility> eligibility = EnumSet.noneOf(EEligibility.class);
        final int numSelected = emitMajors(htm, allMajors, declaredMajor, selectedMajors, eligibility, disable);

        final boolean showUpdate = numSelected > 0 && !curResponses.isEmpty();

        htm.div("vgap");

        htm.sDiv("center");

        if (disable) {
            htm.add("<a class='btn' href='plan_record.html'>", Res.get(Res.SECURE_NEXT_STEP_BTN), "</a>");
        } else {
            htm.add("<a class='btn' href='javascript:;' onclick='document.getElementById(\"moi-form\").submit();'>",
                    Res.get(showUpdate ? Res.SECURE_UPDATE_MAJORS_BTN : Res.SECURE_SUBMIT_MAJORS_BTN), "</a>");
        }

        htm.eDiv();
        htm.addln("</form>");
    }

    /**
     * Emits a block of majors belonging to one category.
     *
     * @param htm            the {@code HtmlBuilder} to which to append
     * @param allMajors      all majors and their math requirements
     * @param declaredMajor  the student's declared major, if any
     * @param selectedMajors the set of all selected majors
     * @param eligibility    a set to which to add the cumulative eligibility recommendations of all selected majors
     * @param disable        true to disable inputs
     * @return the number of items selected in the category
     */
    private static int emitMajors(final HtmlBuilder htm, final List<Major> allMajors, final Major declaredMajor,
                                  final Collection<Major> selectedMajors, final Set<? super EEligibility> eligibility,
                                  final boolean disable) {

        int numSelected = 0;

        eligibility.clear();

        // First, count the number of majors checked to see if we should check the top-level (and
        // accumulate math requirements at the same time)
        for (final Major entry : allMajors) {
            if (selectedMajors.contains(entry)) {
                eligibility.addAll(entry.idealEligibility);
                ++numSelected;
            }
        }
        eligibility.remove(EEligibility.AUCC);
        if (eligibility.isEmpty()) {
            eligibility.add(EEligibility.AUCC);
        }

        htm.sDiv("columns");
        boolean foundDeclared = false;
        char letter = 0;
        for (final Major major : allMajors) {
            if (major.questionNumbers[0] > 9000) {
                // Don't emit all the specific Exploratory Studies tracks
                continue;
            }

            final String mname = major.programName;

            if (!mname.isEmpty() && mname.charAt(0) != letter) {
                letter = mname.charAt(0);
                htm.sDiv("alph-letter").add(letter).eDiv();
            }

            final String pcode = major.programCodes.getFirst();
            final String classname = pcode + "-conc";

            // Emit the major
            htm.add("<div class='major'>");
            htm.add("<input type='checkbox'");

            boolean selected = selectedMajors.contains(major);
            if (selected) {
                htm.add(" checked='checked'");
            }
            if (disable) {
                htm.add(" disabled='disabled'");
            }

            htm.add("name='", pcode, "' id='", pcode, "' onchange=\"toggleConc(this,'", classname, "');\"/>");
            htm.add("<label for='", pcode, "'>");
            htm.add(mname);
            if (major.catalogPageUrl != null) {
                htm.add("<span style='white-space:nowrap;'>&nbsp;<a target='_blank' href='", major.catalogPageUrl,
                        "'><img style='position:relative;top:-1px' src='/images/welcome/catalog3.png'/></a></span>");
            }
            if (major.equals(declaredMajor)) {
                htm.add("<strong class='red'> *</strong>");
                foundDeclared = true;
            }
            htm.addln("</label>");

            htm.eDiv();
        }
        htm.eDiv();

        if (foundDeclared) {
            htm.sDiv("indent3");
            htm.add("<em class='columnsize'> <strong class='red'>*</strong> ", Res.get(Res.SECURE_CUR_MAJOR), "</em>");
            htm.eDiv();
        }

        return numSelected;
    }

    /**
     * Called when a POST is received to the page.
     *
     * @param cache       the data cache
     * @param site        the owning site
     * @param siteProfile the site profile
     * @param req         the request
     * @param resp        the response
     * @param session     the session
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final MathPlacementSite site, final Site siteProfile,
                       final ServletRequest req, final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws SQLException {

        final MathPlanLogic logic = new MathPlanLogic(site.site.profile);

        final String stuId = session.getEffectiveUserId();
        final RawStudent student = RawStudentLogic.query(cache, stuId, false);

        final ZonedDateTime now = session.getNow();
        final MathPlanStudentData data = new MathPlanStudentData(cache, student, logic, now, session.loginSessionTag,
                session.actAsUserId == null);

        // Only perform updates if data present AND this is not an adviser using "Act As"
        if (session.actAsUserId == null) {
            final Map<String, String[]> params = req.getParameterMap();

            // Build the list of new responses
            final List<Integer> questions = new ArrayList<>(params.size());
            final List<String> answers = new ArrayList<>(1);

            for (final Major major : Majors.INSTANCE.getMajors()) {
                final List<String> programCodes = major.programCodes;

                for (final String key : params.keySet()) {
                    final String test = key.substring(key.indexOf('.') + 1);
                    if (programCodes.contains(test)) {
                        questions.add(Integer.valueOf(major.questionNumbers[0]));
                        answers.add("Y");
                    }
                }
            }

            if (!questions.isEmpty()) {
                RawStmathplanLogic.deleteAllForPage(cache, student.stuId, MathPlanConstants.MAJORS_PROFILE);
                logic.storeMathPlanResponses(cache, data.student, MathPlanConstants.MAJORS_PROFILE, questions, answers,
                        now, session.loginSessionTag);
            }
        }

        // Redirect to this page, so we show it with a "GET" (and page reloads won't repost)
        resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        final String path = siteProfile.path;
        resp.setHeader("Location", path + (path.endsWith(Contexts.ROOT_PATH)
                ? "plan_record.html" : "/plan_record.html"));
    }
}
