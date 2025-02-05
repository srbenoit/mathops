package dev.mathops.web.site.placement.main;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Site;
import dev.mathops.db.old.logic.mathplan.MathPlanLogic;
import dev.mathops.db.old.logic.mathplan.data.Major;
import dev.mathops.db.old.logic.mathplan.data.MajorMathRequirement;
import dev.mathops.db.old.logic.mathplan.data.MathPlanConstants;
import dev.mathops.db.old.logic.mathplan.data.MathPlanStudentData;
import dev.mathops.db.old.rawrecord.RawStmathplan;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Generates the page that presents majors, organized by math requirement.
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

        final Profile profile = site.site.profile;
        final MathPlanLogic logic = new MathPlanLogic(profile);

        final String stuId = session.getEffectiveUserId();
        final ZonedDateTime now = session.getNow();
        final MathPlanStudentData data = logic.getStudentData(cache, stuId, now, session.loginSessionTag,
                session.actAsUserId == null);

        final HtmlBuilder htm = new HtmlBuilder(8192);
        final String title = site.getTitle();
        Page.startNofooterPage(htm, title, session, true, Page.NO_BARS, null, false, false);

        MPPage.emitMathPlanHeader(htm);

        if (data == null) {
            MPPage.emitNoStudentDataError(htm);
        } else {
            MathPlacementSite.emitLoggedInAs2(htm, session);
            htm.sDiv("inset2");

            htm.sDiv("shaded2left");
            htm.sP().add("Let us know what majors you think you might choose.  If you're not sure ",
                    "yet what you want to major in, take your best guess.  You can change ",
                    "your selections later if you change your mind.").eP();

            htm.sP().add("Choosing a major here does not declare it as your actual major - you can ",
                    "try out different majors here and see how they affect your Math Plan.").eP();
            htm.eDiv(); // shaded2left

            htm.div("vgap");

            htm.sDiv("folders");

            htm.add("<nav class='folderstabs'>");
            htm.sDiv("tab selected", "id='first'")
                    .add("sort majors by math <span class='hidebelow700'>required</span>",
                            "<span class='hideabove600'><span class='hidebelow400'>required</span></span>")
                    .eDiv();
            htm.sDiv("tab unsel", "id='last'")
                    .add("<a href='plan_majors2.html'>",
                            "sort majors alphabetically</a>")
                    .eDiv();
            htm.add("</nav>");

            htm.sDiv("folder-content");
            final Map<Integer, RawStmathplan> majorProfileResponses = data.getMajorProfileResponses();
            emitMajorsSelectionForm(htm, majorProfileResponses, data, session, logic);
            htm.eDiv();

            htm.eDiv(); // folders

            htm.eDiv(); // inset2
        }

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
     * @param logic        the site logic
     */
    private static void emitMajorsSelectionForm(final HtmlBuilder htm, final Map<Integer, RawStmathplan> curResponses,
                                                final MathPlanStudentData data, final ImmutableSessionInfo session,
                                                final MathPlanLogic logic) {

        final boolean disable = session.actAsUserId != null;

        final Major declaredMajor = logic.getMajor(data.student.programCode);

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

        htm.add("<form id='moi-form' action='plan_majors1.html' method='post'>");

        final Map<Major, MajorMathRequirement> allMajors = logic.getMajors();
        final List<Major> auccMajors = logic.getMajorsRequiringAUCC();
        final List<Major> precalcMajors = logic.getMajorsRequiringPrecalc();
        final List<Major> calc1Majors = logic.getMajorsRequiringCalc1();
        final List<Major> beyondMajors = logic.getMajorsRequiringBeyondCalc1();

        // Gather the set of selected majors and concentrations
        final Collection<Major> selectedMajors = new HashSet<>(10);
        if (declaredMajor != null) {
            selectedMajors.add(declaredMajor);
        }
        for (final Major major : allMajors.keySet()) {

            final RawStmathplan curResp = curResponses.get(major.questionNumber);
            if (curResp != null && "Y".equals(curResp.stuAnswer)) {
                selectedMajors.add(major);
            }
        }

        final Collection<MajorMathRequirement> requirements = new ArrayList<>(10);

        final int numSelected = emitMajorsCategory(htm, auccMajors, "AUCC",
                "Majors that allow me to choose three credits from any "
                        + "core curriculum Mathematics or Statistics courses",
                allMajors, declaredMajor, requirements, selectedMajors, disable)
                + emitMajorsCategory(htm, precalcMajors, "PRECALC",
                "Majors that require some Algebra and/or Trigonometry", allMajors, declaredMajor,
                requirements, selectedMajors, disable)
                + emitMajorsCategory(htm, calc1Majors, "CALC1",
                "Majors that require courses beyond Algebra or Trigonometry", allMajors,
                declaredMajor, requirements, selectedMajors, disable)
                + emitMajorsCategory(htm, beyondMajors, "BEYONDCALC",
                "Majors that require a Calculus sequence", allMajors, declaredMajor, requirements,
                selectedMajors, disable);

        final boolean showUpdate = numSelected > 0 && !curResponses.isEmpty();

        htm.div("vgap");

        htm.sDiv("center");

        if (disable) {
            htm.add("<a class='btn' href='plan_record.html'>",
                    Res.get(Res.SECURE_NEXT_STEP_BTN), "</a>");
        } else {
            htm.add("<a class='btn' href='javascript:;' ",
                    "onclick='document.getElementById(\"moi-form\").submit();'>",
                    Res.get(showUpdate ? Res.SECURE_UPDATE_MAJORS_BTN : Res.SECURE_SUBMIT_MAJORS_BTN),
                    "</a>");
        }

        htm.addln("<a class='btn' href='https://www.math.colostate.edu/placement",
                "/Math_Requirements.pdf' target='_blank'>",
                Res.get(Res.EXPLORE_MAJORS_BTN), "</a>");

        htm.eDiv();
        htm.addln("</form>");
    }

    /**
     * Emits a block of majors belonging to one category.
     *
     * @param htm            the {@code HtmlBuilder} to which to append
     * @param majors         the list of majors to display (sorted)
     * @param categoryId     a unique ID for each category
     * @param categoryLabel  the category label
     * @param allMajors      all majors and their math requirements
     * @param declaredMajor  the student's declared major, if any
     * @param requirements   an accumulator for requirements of selected majors
     * @param selectedMajors the set of all selected majors
     * @param disable        true to disable inputs
     * @return the number of items selected in the category
     */
    private static int emitMajorsCategory(final HtmlBuilder htm, final Iterable<Major> majors,
                                          final String categoryId, final String categoryLabel,
                                          final Map<Major, MajorMathRequirement> allMajors, final Major declaredMajor,
                                          final Collection<? super MajorMathRequirement> requirements,
                                          final Collection<Major> selectedMajors, final boolean disable) {

        int numSelected = 0;

        // First, count the number of majors checked to see if we should check the top-level (and
        // accumulate math requirements at the same time)
        for (final Major major : majors) {
            if (selectedMajors.contains(major)) {
                requirements.add(allMajors.get(major));
                ++numSelected;
            }
        }

        htm.sP("indent linksize");
        htm.add("<input type='checkbox' id='ck_", categoryId, "'");
        if (numSelected > 0) {
            htm.add(" checked='checked'");
        }
        if (disable) {
            htm.add(" disabled='disabled'");
        }
        htm.addln(" onchange=\"toggleCategory(this,'", categoryId, "');\"/><label for='ck_", categoryId, "'>",
                categoryLabel, "</label>").eP();

        htm.add("<div class='indent2 columns' id='", categoryId, "'");
        if (numSelected == 0) {
            htm.add(" style='display:none;'");
        } else {
            htm.add(" style='display:block;'");
        }
        htm.addln(">");

        boolean foundDeclared = false;
        for (final Major major : majors) {
            if (major.bogus) {
                continue;
            }

            // Get just majors (not concentrations) first
            if (major.concentrationName != null) {
                continue;
            }

            final String pcode = categoryId + CoreConstants.DOT + major.programCode;
            final String classname = pcode + "-conc";

            // Emit the major
            htm.add("<div class='major'>");
            htm.add("<input type='checkbox'");

            if (Boolean.FALSE.equals(major.checkable)) {
                htm.add(" class='expandcollapse'");
            }

            // Now display any concentrations associated with the major in a DIV
            boolean selected = selectedMajors.contains(major);
            if (!selected) {
                // See if a concentration under the major is selected...
                for (final Major conc : majors) {
                    if (selectedMajors.contains(conc) && conc.concentrationName != null
                            && conc.majorName.equals(major.majorName)) {
                        selected = true;
                        break;
                    }
                }
            }
            if (selected) {
                htm.add(" checked='checked'");
            }
            if (disable) {
                htm.add(" disabled='disabled'");
            }

            htm.add("name='", pcode, "' id='", pcode, "' onchange=\"toggleConc(this,'", classname, "');\"/>");
            htm.add("<label for='", pcode, "'>");

            final String mname = major.majorName;

            final int lastMSpace = mname.lastIndexOf(' ');
            if (lastMSpace == -1) {
                htm.add("<span style='white-space:nowrap;'>");
                htm.add(mname);
            } else {
                htm.add(mname.substring(0, lastMSpace + 1));
                htm.add("<span style='white-space:nowrap;'>");
                htm.add(mname.substring(lastMSpace + 1));
            }

            if (major.catalogUrl != null) {
                htm.add("<span style='white-space:nowrap;'>&nbsp;<a target='_blank' href='", major.catalogUrl,
                        "'><img style='position:relative;top:-1px' src='/images/welcome/catalog3.png'/></a></span>");
            }

            if (major.equals(declaredMajor)) {
                htm.add("<strong class='red'> *</strong>");
                foundDeclared = true;
            }

            htm.add("</span>");
            htm.addln("</label>");

            if (selected) {
                htm.addln("<div id='", classname, "' style='margin-left:20px;display:block;'>");
            } else {
                htm.addln("<div id='", classname, "' style='padding-left:20px;display:none;'>");
            }

            for (final Major conc : majors) {
                if (conc.bogus) {
                    continue;
                }

                if (conc.concentrationName != null && major.majorName.equals(conc.majorName)) {

                    final String ccode = categoryId + CoreConstants.DOT + conc.programCode;

                    htm.sDiv();
                    htm.add("<input type='checkbox' name='", ccode, "' id='", ccode, "'");

                    htm.add(" class='");
                    if (Boolean.TRUE.equals(conc.autoCheck)) {
                        htm.add(classname, " autocheck");
                    } else {
                        htm.add(classname);
                    }
                    htm.add("'");

                    if (selectedMajors.contains(conc)) {
                        htm.add(" checked='checked'");
                    }
                    if (disable) {
                        htm.add(" disabled='disabled'");
                    }

                    htm.add("/>");
                    htm.add("<label for='", ccode, "'>");

                    // Play game to get icon to not wrap to next line
                    final String cname = conc.concentrationName;
                    final int lastCSpace = cname.lastIndexOf(' ');
                    if (lastCSpace == -1) {
                        htm.add("<span style='white-space:nowrap;'>");
                        htm.add(cname);
                    } else {
                        htm.add(cname.substring(0, lastCSpace + 1));
                        htm.add("<span style='white-space:nowrap;'>");
                        htm.add(cname.substring(lastCSpace + 1));
                    }
                    if (conc.catalogUrl != null) {
                        htm.add("&nbsp;<a target='_blank' href='", conc.catalogUrl, "'>",
                                "<img style='position:relative;top:-1px' src='/images/welcome/catalog3.png'/></a>");
                    }
                    if (conc.equals(declaredMajor)) {
                        htm.add("<strong class='red'> *</strong>");
                        foundDeclared = true;
                    }
                    htm.add("</span>");
                    htm.addln("</label>");
                    htm.eDiv();
                }
            }

            htm.eDiv();
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
    static void doPost(final Cache cache, final MathPlacementSite site,
                       final Site siteProfile, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session) throws SQLException {

        final MathPlanLogic logic = new MathPlanLogic(site.site.profile);

        final String stuId = session.getEffectiveUserId();
        final MathPlanStudentData data = stuId == null ? null : logic.getStudentData(cache, stuId, session.getNow(),
                session.loginSessionTag, session.actAsUserId == null);

        // Only perform updates if data present AND this is not an adviser using "Act As"
        if (data != null && session.actAsUserId == null) {

            final Map<String, String[]> params = req.getParameterMap();

            // Build the list of new responses
            final List<Integer> questions = new ArrayList<>(params.size());
            final List<String> answers = new ArrayList<>(1);

            for (final Major major : logic.getMajors().keySet()) {
                final String programCode = major.programCode;

                for (final String key : params.keySet()) {
                    final String test = key.substring(key.indexOf('.') + 1);
                    if (test.equals(programCode)) {
                        questions.add(major.questionNumber);
                        answers.add("Y");
                    }
                }
            }

            if (!questions.isEmpty()) {
                logic.deleteMathPlanResponses(cache, data.student, MathPlanConstants.MAJORS_PROFILE, session.getNow(),
                        session.loginSessionTag);
                logic.storeMathPlanResponses(cache, data.student, MathPlanConstants.MAJORS_PROFILE, questions, answers,
                        session.getNow(), session.loginSessionTag);
            }
        }

        // Redirect to this page, so we show it with a "GET" (and page reloads won't repost)
        resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        final String path = siteProfile.path;
        resp.setHeader("Location", path + (path.endsWith(Contexts.ROOT_PATH)
                ? "plan_record.html" : "/plan_record.html"));
    }
}
