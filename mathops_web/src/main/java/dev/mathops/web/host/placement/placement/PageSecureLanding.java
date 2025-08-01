package dev.mathops.web.host.placement.placement;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.StudentMathPlan;
import dev.mathops.db.logic.mathplan.types.EMathPlanStatus;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * The landing page after successful login.
 */
enum PageSecureLanding {
    ;

    /**
     * Processes a GET request of this form.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's session (guaranteed not to be null)
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();

        final EMathPlanStatus planStatus = MathPlanLogic.getStatus(cache, stuId);
        final List<RawStmpe> tries = RawStmpeLogic.queryLegalByStudent(cache, stuId);

        final boolean planCompleted = planStatus == EMathPlanStatus.PLAN_COMPLETED_PLACEMENT_NEEDED
                                      || planStatus == EMathPlanStatus.PLAN_COMPLETED_PLACEMENT_NOT_NEEDED;

        final boolean hasReviewed = planStatus == EMathPlanStatus.REVIEWED_EXISTING
                                    || planStatus == EMathPlanStatus.PLAN_COMPLETED_PLACEMENT_NEEDED
                                    || planStatus == EMathPlanStatus.PLAN_COMPLETED_PLACEMENT_NOT_NEEDED;

        final boolean placementRequired = planStatus != EMathPlanStatus.PLAN_COMPLETED_PLACEMENT_NOT_NEEDED;

        // 1 if not completed, 2 if completed
        final boolean placementCompleted = !tries.isEmpty();
        final boolean attemptsRemain = tries.size() < 2;

        final HtmlBuilder htm = MPPage.startPage1(site, session);

        htm.sDiv("inset2");

        emitStep1(cache, session, htm, planCompleted);
        emitStep2(htm, planCompleted, hasReviewed, placementRequired);
        emitStep3(htm, planCompleted, hasReviewed, placementCompleted, placementRequired, attemptsRemain);

        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Emits the box for "Step 1" (Create a math plan), with current status and any actions needed.
     *
     * @param cache         the data cache
     * @param session       the user's session (guaranteed not to be null)
     * @param htm           the {@code HtmlBuilder} to which to append
     * @param planCompleted true if the student has completed the Math Plan
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitStep1(final Cache cache, final ImmutableSessionInfo session, final HtmlBuilder htm,
                                  final boolean planCompleted) throws SQLException {

        htm.sDiv("shaded2left");

        htm.sDiv("left", "style='position:relative;top:8px;margin-bottom:8px;'").sH(2);
        if (planCompleted) {
            htm.add("<img src='/www/images/square-check-regular.svg' "
                    + "style='height:24px;position:relative;top:-2px;'/>");
        } else {
            htm.add("<img src='/www/images/square-regular.svg' style='height:24px;position:relative;top:-2px;'/>");
        }
        htm.add(" Step 1: &nbsp;").eH(2).eDiv();
        htm.sP("question")
                .add("Create a personalized Math Plan to see whether you should complete the Math Placement Tool.")
                .eP();
        htm.div("clear");

        if (planCompleted) {
            // Show brief summary of plan outcome
            final String stuId = session.getEffectiveUserId();

            final StudentMathPlan plan = MathPlanLogic.queryPlan(cache, stuId);
            if (plan != null) {
                PagePlanView.showBriefPlan(cache, session, htm, plan);
            }

            // Show button to change plan
            htm.div("vgap");
            htm.sDiv("center");
            htm.sDiv().add("<a class='btn' href='plan_start.html'>Make changes to my Math Plan</a>").eDiv();
        } else {
            // Show button to start plan
            htm.sDiv("center");
            htm.sP().add("You have not yet completed your Math Plan.").eP();
            htm.sDiv().add("<a class='btn' href='plan_start.html'>Create my Math Plan</a>").eDiv();
        }
        htm.eDiv(); // center

        htm.eDiv();
        htm.div("vgap0");
    }

    /**
     * Emits the box for "Step 2" (Review and practice), with current status and any actions needed.
     *
     * @param htm               the {@code HtmlBuilder} to which to append
     * @param planCompleted     true if the student has completed the Math Plan
     * @param hasReviewed       true if the student has access review materials
     * @param placementRequired true if the math plan indicated placement is required
     */
    private static void emitStep2(final HtmlBuilder htm, final boolean planCompleted,
                                  final boolean hasReviewed, final boolean placementRequired) {

        if (planCompleted) {
            htm.sDiv("shaded2left");
        } else {
            htm.sDiv("shadedleft");
        }

        htm.sDiv("left", "style='position:relative;top:8px;margin-bottom:8px;'").sH(2);
        if (hasReviewed) {
            htm.add("<img src='/www/images/square-check-regular.svg' style='height:24px;position:relative;top:-2px;" +
                    "'/>");
        } else if (placementRequired) {
            htm.add("<img src='/www/images/square-regular.svg' style='height:24px;position:relative;top:-2px;'/>");
        } else {
            htm.add("<img src='/www/images/square-dim.svg' style='height:24px;position:relative;top:-2px;'/>");
        }
        htm.add(" Step 2: &nbsp;").eH(2).eDiv();
        if (placementRequired) {
            htm.sP("question").add("Use our review materials to practice before completing the Math Placement Tool.")
                    .eP();
        } else {
            htm.sP("question").add("If you would like to complete the Math Placement Tool, use our review materials ",
                    "to practice first.").eP();
        }
        htm.div("clear");

        htm.sDiv("center");
        htm.sDiv().add("<a class='btn' href='review.html'>Access interactive <span class='hideabove400'><br></span>",
                "review materials<br>and practice questions</a>").eDiv();
        htm.eDiv(); // center

        htm.eDiv(); // shaded2left or shadedleft
        htm.div("vgap0");
    }

    /**
     * Emits the box for "Step 3" (Math Placement Tool), with current status and any actions needed.
     *
     * @param htm                the {@code HtmlBuilder} to which to append
     * @param planCompleted      true if the student has completed the Math Plan
     * @param hasReviewed        true if the student has access review materials
     * @param placementCompleted true if the student has completed placement
     * @param placementRequired  true if the math plan indicated placement is required
     * @param attemptsRemain     true if any attempts remain; false if all are used
     */
    private static void emitStep3(final HtmlBuilder htm, final boolean planCompleted,
                                  final boolean hasReviewed, final boolean placementCompleted,
                                  final boolean placementRequired, final boolean attemptsRemain) {

        if (planCompleted && hasReviewed) {
            htm.sDiv("shaded2left");
        } else {
            htm.sDiv("shadedleft");
        }

        htm.sDiv("left", "style='position:relative;top:8px;margin-bottom:8px;'").sH(2);
        if (placementCompleted) {
            htm.add("<img src='/www/images/square-check-regular.svg' style='height:24px;position:relative;top:-2px;" +
                    "'/>");
        } else if (placementRequired) {
            htm.add("<img src='/www/images/square-regular.svg' style='height:24px;position:relative;top:-2px;'/>");
        } else {
            htm.add("<img src='/www/images/square-dim.svg' style='height:24px;position:relative;top:-2px;'/>");
        }
        htm.add(" Step 3: &nbsp;").eH(2).eDiv();
        htm.sP("question").add("Go to the Math Placement Tool website to learn about testing options, access your ",
                        "results, and check if further action is necessary.")
                .eP();
        htm.div("clear");

        htm.sDiv("center");
        if (hasReviewed) {
            htm.sDiv().add("<a class='btn' href='tool.html'>");
            if (placementCompleted) {
                if (attemptsRemain) {
                    htm.add("Check Placement Results <span class='hideabove400'><br></span>and Next Steps<br>",
                            "or Retake the Placement Tool");
                } else {
                    htm.add("Check Placement Results <span class='hideabove400'><br></span>and Next Steps");
                }
            } else {
                htm.add("Complete the <span class='hideabove400'><br></span>Math Placement Tool");
            }
            htm.add("</a>").eDiv();
        } else if (placementCompleted) {
            htm.sDiv().add("<a class='btn' href='tool.html'>");
            if (attemptsRemain) {
                htm.add("Check Placement Results <span class='hideabove400'><br></span>and Next Steps<br>",
                        "or Retake the Placement Tool");
            } else {
                htm.add("Check Placement Results <span class='hideabove400'><br></span>and Next Steps");
            }
            htm.add("</a>").eDiv();
        } else {
            htm.sP().add("You have not yet accessed Review Materials.").eP();
            htm.sDiv().add("<a class='btndim'>Complete the <span class='hideabove400'><br></span>",
                    "Math Placement Tool</a>").eDiv();
        }
        htm.eDiv(); // center

        htm.eDiv(); // shaded2left or shadedleft
        htm.div("vgap0");
    }
}
