package dev.mathops.web.host.placement.placement;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.mathplan.MathPlanConstants;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.NextSteps;
import dev.mathops.db.logic.mathplan.StudentMathPlan;
import dev.mathops.db.logic.mathplan.types.ENextStep;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawrecord.RawMpscorequeue;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.host.placement.tutorial.precalc.EEligibility;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates the page that shows next steps with affirmations and plans to take MPE.
 */
enum PagePlanNext {
    ;

    /** A commonly used integer. */
    private static final Integer ONE = Integer.valueOf(1);

    /** A commonly used integer. */
    private static final Integer TWO = Integer.valueOf(2);

    /** A class. */
    private static final String CENTER = "center";

    /** A string used when describing "ideal" eligibility. */
    private static final String IT_IS_IDEAL = "it is ideal if you are eligible for ";

    /** A string used when describing "ideal" eligibility. */
    private static final String OR = " or ";

    /** A string used when describing "ideal" eligibility. */
    private static final String AND = " and ";

    /** A string used when describing "ideal" eligibility. */
    private static final String C = ", ";

    /** A string used when describing "ideal" eligibility. */
    private static final String C_AND = ", and ";

    /** A string used when describing "ideal" eligibility. */
    private static final String C_OR_FOR = ", or for ";

    /** A string used when describing "ideal" eligibility. */
    private static final String C_AND_ALSO_FOR = ", and also for ";

    /** A course name for describing "ideal" eligibility. */
    private static final String MATH_117 = "<b>MATH 117</b> (College Algebra in Context I)";

    /** A course name for describing "ideal" eligibility. */
    private static final String MATH_118 = "<b>MATH 118</b> (College Algebra in Context II)";

    /** A course name for describing "ideal" eligibility. */
    private static final String MATH_124 = "<b>MATH 124</b> (Logarithmic and Exponential Functions)";

    /** A course name for describing "ideal" eligibility. */
    private static final String MATH_125 = "<b>MATH 125</b> (Numerical Trigonometry)";

    /** A course name for describing "ideal" eligibility. */
    private static final String MATH_126 = "<b>MATH 126</b> (Analytic Trigonometry)";

    /** A course name for describing "ideal" eligibility. */
    private static final String MATH_120 = "<b>MATH 120</b> (College Algebra)";

    /** A course name for describing "ideal" eligibility. */
    private static final String MATH_155 = "<b>MATH 155</b> (Calculus for Biological Scientists I)";

    /** A course name for describing "ideal" eligibility. */
    private static final String MATH_156 = "<b>MATH 156</b> (Mathematics for Computational Science I)";

    /** A course name for describing "ideal" eligibility. */
    private static final String MATH_160 = "<b>MATH 160</b> (Calculus for Physical Scientists I)";

    /** A course name for describing next steps. */
    private static final String YOU_SHOULD_COMPLETE_A =
            "You should complete the \"Algebra\" section of the Math Placement Tool to try to ";

    /** A course name for describing next steps. */
    private static final String YOU_SHOULD_COMPLETE_AT = "You should complete the \"Algebra\" and \"Trigonometry\" "
                                                         + "sections of the Math Placement Tool to try to ";

    /** A course name for describing next steps. */
    private static final String YOU_SHOULD_COMPLETE_AL = "You should complete the \"Algebra\" and \"Logarithmic &amp; "
                                                         + "Exponential Functions\" sections of the Math Placement "
                                                         + "Tool to try to ";

    /** A course name for describing next steps. */
    private static final String YOU_SHOULD_COMPLETE_3 = "You should complete the \"Algebra\", \"Trigonometry\", and "
                                                        + "\"Logarithmic &amp; Exponential Functions\" sections of "
                                                        + "the Math Placement Tool to try to ";

    /** A course name for describing next steps. */
    private static final String IF_NOT_PLACED = "If you do not place out of ";

    /** A course name for describing next steps. */
    private static final String YOU_CAN_DO_TUTORIAL =
            " on the Math Placement Tool, you can do so by completing one or more tutorials.";

    /** A string to start a "strong" span. */
    private static final String SSTRONG = "<strong>";

    /** A string to end a "strong" span. */
    private static final String ESTRONG = "</strong>";

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

        final HtmlBuilder htm = new HtmlBuilder(8192);
        final String title = site.getTitle();
        Page.startNofooterPage(htm, title, session, true, Page.NO_BARS, null, false, false);
        MPPage.emitMathPlanHeader(htm);

        MathPlacementSite.emitLoggedInAs2(htm, session);
        htm.sDiv("inset2");

        final String stuId = session.getEffectiveUserId();
        final StudentData studentData = cache.getStudent(stuId);
        final RawStudent student = studentData.getStudentRecord();

        final Map<Integer, RawStmathplan> existing =
                studentData.getLatestMathPlanResponsesByPage(MathPlanConstants.ONLY_RECOM_PROFILE);

        if (existing.containsKey(ONE)) {
            showPlan(cache, session, htm, studentData);
        } else {
            PagePlanView.doGet(cache, site, req, resp, session);
        }

        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Displays the student's next steps.
     *
     * @param cache       the data cache
     * @param session     the session
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param studentData the student data object
     * @throws SQLException if there is an error accessing the database
     */
    private static void showPlan(final Cache cache, final ImmutableSessionInfo session,
                                 final HtmlBuilder htm, final StudentData studentData) throws SQLException {

        htm.sDiv("shaded2left");

        final String screenName = session.getEffectiveScreenName();
        final String stuId = session.getEffectiveUserId();
        final StudentMathPlan plan = MathPlanLogic.queryPlan(cache, stuId);

        final Map<Integer, RawStmathplan> intentions =
                studentData.getLatestMathPlanResponsesByPage(MathPlanConstants.INTENTIONS_PROFILE);

        htm.sDiv("left welcome", "style='margin-bottom:0;'");
        if (screenName == null) {
            htm.add("Your <span class='hidebelow700'>Personalized</span> Math Plan:");
        } else {
            htm.add("<span class='hidebelow700'>Personalized</span> Math Plan for ", screenName);
        }
        htm.eDiv();
        htm.sDiv("right welcome", "style='margin-bottom:0;'");
        final LocalDate today = LocalDate.now();
        final String todayStr = TemporalUtils.FMT_MDY.format(today);
        htm.add(todayStr);
        htm.eDiv();
        htm.div("clear");
        htm.hr();

        htm.sP().add("Based on your math plan, these are your next steps:").eP();

        final boolean needsPlacement = showNextSteps(cache, htm, plan);
        htm.div("vgap2");

        htm.sDiv("advice");
        htm.addln("<form action='plan_next.html' method='post'>");

        htm.add("Read and affirm each statement to complete your Math Plan...");

        final boolean check1 = intentions.containsKey(ONE);
        htm.sP().add("<input type='checkbox' name='affirm1' id='affirm1'");
        if (check1) {
            htm.add(" checked");
        }

        final boolean check2 = intentions.containsKey(TWO);
        htm.add(" onclick='affirmed();'> &nbsp; <label for='affirm1'>",
                "I understand that this plan is only a recommendation.  The math requirements for each degree ",
                "program can change over time, and should be verified with the University Catalog.</label>").eP();

        if (needsPlacement) {
            htm.sP().add("<input type='checkbox' name='affirm2' id='affirm2'");
            if (check2) {
                htm.add(" checked");
            }
            htm.add(" onclick='affirmed();'> &nbsp; <label for='affirm2'>",
                    "I plan to complete the Math Placement Tool.</label>").eP();
        } else {
            htm.sP().add("<input type='hidden' name='affirm2' id='affirm2' value='Y'/>");
        }

        htm.sDiv(CENTER);
        htm.addln("<button type='submit' id='affirmsubmit' class='btn'");
        if (!check1) {
            htm.add(" disabled");
        }
        htm.add(">Affirm</button>");
        htm.eDiv();

        htm.addln("<script>");
        htm.addln(" function affirmed() {");
        htm.addln("  document.getElementById('affirmsubmit').disabled =");
        htm.addln("  !document.getElementById('affirm1').checked;");
        htm.addln(" }");
        htm.addln("</script>");
        htm.addln("</form>");
        htm.eDiv();
        htm.div("vgap");

        htm.eDiv();
    }

    /**
     * Shows the next steps for the student, or informs the student that they are eligible for the course(s) they
     * currently need.
     *
     * @param cache       the data cache
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param studentData the student data
     * @param plan        the student math plan
     * @return true if the student needs to complete placement, false if not
     * @throws SQLException if there is an error accessing the database
     */
    static boolean showNextSteps(final Cache cache, final HtmlBuilder htm, final StudentData studentData,
                                 final StudentMathPlan plan) throws SQLException {

        TermKey active = null;
        try {
            final SystemData systemData = cache.getSystemData();
            final TermRec activeTerm = systemData.getActiveTerm();
            if (activeTerm != null) {
                active = activeTerm.term;
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        final Set<EEligibility> eligibility = plan.recommendedEligibility;
        final NextSteps nextStep = plan.nextSteps;

        final RawStudent student = studentData.getStudentRecord();
        final TermKey termKey = student.aplnTerm;
        final ETermName applicationTerm = termKey == null ? null : termKey.name;
        final String termName = applicationTerm == null ? null : applicationTerm.fullName;
        final boolean isIncoming = testForIncoming(active, termKey);

        final Map<Integer, RawStmathplan> profileResponses =
                studentData.getLatestMathPlanResponsesByPage(MathPlanConstants.MAJORS_PROFILE);
        final String basedOn = profileResponses.size() == 1 ? "Based on the major you selected, "
                : "Based on the list of majors you selected, ";
        final String inTerm = " in " + termName + ".";

        htm.div("vgap");
        htm.sDiv("indent");

        // Show the student's "ideal placement" based on their selected major(s)
        if (eligibility.contains(EEligibility.M_160)) {
            if (eligibility.contains(EEligibility.M_156)) {
                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_156, OR, MATH_160, inTerm).eP();
            } else if (eligibility.contains(EEligibility.M_155)) {
                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_155, OR, MATH_160, inTerm).eP();
            } else {
                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_160, inTerm).eP();
            }
        } else if (eligibility.contains(EEligibility.M_156)) {
            if (eligibility.contains(EEligibility.M_155)) {
                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_156, OR, MATH_155, inTerm).eP();
            } else {
                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_156, inTerm).eP();
            }
        } else if (eligibility.contains(EEligibility.M_155)) {
            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_155, inTerm).eP();
        } else if (eligibility.contains(EEligibility.M_126)) {
            if (eligibility.contains(EEligibility.M_125)) {
                if (eligibility.contains(EEligibility.M_124)) {
                    if (eligibility.contains(EEligibility.M_118)) {
                        if (eligibility.contains(EEligibility.M_117)) {
                            if (eligibility.contains(EEligibility.M_120)) {
                                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C, MATH_124, C_OR_FOR,
                                        MATH_120, C_AND_ALSO_FOR, MATH_125, AND, MATH_126, inTerm).eP();
                            } else {
                                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C, MATH_124, C, MATH_125,
                                        C_AND, MATH_126, inTerm).eP();
                            }
                        } else if (eligibility.contains(EEligibility.M_120)) {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, C, MATH_124, C_OR_FOR, MATH_120,
                                    C_AND_ALSO_FOR, MATH_125, AND, MATH_126, inTerm).eP();
                        } else {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, C, MATH_124, C, MATH_125, C_AND, MATH_126,
                                    inTerm).eP();
                        }
                    } else if (eligibility.contains(EEligibility.M_117)) {
                        if (eligibility.contains(EEligibility.M_120)) {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_124, C_OR_FOR, MATH_120,
                                    C_AND_ALSO_FOR, MATH_125, AND, MATH_126, inTerm).eP();
                        } else {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_124, C, MATH_125, C_AND, MATH_126,
                                    inTerm).eP();
                        }
                    } else if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_124, C_OR_FOR, MATH_120, C_AND_ALSO_FOR, MATH_125,
                                AND, MATH_126, inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_124, C, MATH_125, C_AND, MATH_126, inTerm).eP();
                    }
                } else {
                    // 126 and 125 but not 124
                    if (eligibility.contains(EEligibility.M_118)) {
                        if (eligibility.contains(EEligibility.M_117)) {
                            if (eligibility.contains(EEligibility.M_120)) {
                                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C_OR_FOR, MATH_120,
                                        C_AND_ALSO_FOR, MATH_125, AND, MATH_126, inTerm).eP();
                            } else {
                                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C, MATH_125, C_AND, MATH_126,
                                        inTerm).eP();
                            }
                        } else if (eligibility.contains(EEligibility.M_120)) {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, OR, MATH_120, C_AND_ALSO_FOR, MATH_125, AND,
                                    MATH_126, inTerm).eP();
                        } else {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, C, MATH_125, C_AND, MATH_126, inTerm).eP();
                        }
                    } else if (eligibility.contains(EEligibility.M_117)) {
                        if (eligibility.contains(EEligibility.M_120)) {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, OR, MATH_120, C_AND_ALSO_FOR, MATH_125, AND,
                                    MATH_126, inTerm).eP();
                        } else {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_125, C_AND, MATH_126, inTerm).eP();
                        }
                    } else if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_120, C, MATH_125, C_AND, MATH_126, inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_125, AND, MATH_126, inTerm).eP();
                    }
                }
                // BELOW HERE NEEDS 126 BUT DOES NOT NAME 125
            } else if (eligibility.contains(EEligibility.M_124)) {
                if (eligibility.contains(EEligibility.M_118)) {
                    if (eligibility.contains(EEligibility.M_117)) {
                        if (eligibility.contains(EEligibility.M_120)) {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C_AND, MATH_124, C_OR_FOR,
                                    MATH_120, C_AND_ALSO_FOR, MATH_126, inTerm).eP();
                        } else {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C, MATH_124, C_AND, MATH_126,
                                    inTerm).eP();
                        }
                    } else if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, AND, MATH_124, C_OR_FOR, MATH_120, C_AND_ALSO_FOR,
                                MATH_126, inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, C, MATH_124, C_AND, MATH_126, inTerm).eP();
                    }
                } else if (eligibility.contains(EEligibility.M_117)) {
                    if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, AND, MATH_124, C_OR_FOR, MATH_120, C_AND_ALSO_FOR,
                                MATH_126, inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_124, C_AND, MATH_126, inTerm).eP();
                    }
                } else if (eligibility.contains(EEligibility.M_120)) {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_124, OR, MATH_120, C_AND_ALSO_FOR, MATH_126, inTerm).eP();
                } else {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_124, AND, MATH_126, inTerm).eP();
                }
            } else {
                // 126 but not 124
                if (eligibility.contains(EEligibility.M_118)) {
                    if (eligibility.contains(EEligibility.M_117)) {
                        if (eligibility.contains(EEligibility.M_120)) {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, AND, MATH_118, C_OR_FOR, MATH_120,
                                    C_AND_ALSO_FOR, MATH_126, inTerm).eP();
                        } else {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C_AND, MATH_126, inTerm).eP();
                        }
                    } else if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, OR, MATH_120, ", and also ", MATH_126,
                                inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, AND, MATH_126, inTerm).eP();
                    }
                } else if (eligibility.contains(EEligibility.M_117)) {
                    if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, OR, MATH_120, C_AND_ALSO_FOR, MATH_126,
                                inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, AND, MATH_126, inTerm).eP();
                    }
                } else if (eligibility.contains(EEligibility.M_120)) {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_120, AND, MATH_126, inTerm).eP();
                } else {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_126, inTerm).eP();
                }
            }
            // BELOW HERE DOES NOT NEED 126
        } else if (eligibility.contains(EEligibility.M_125)) {
            if (eligibility.contains(EEligibility.M_124)) {
                if (eligibility.contains(EEligibility.M_118)) {
                    if (eligibility.contains(EEligibility.M_117)) {
                        if (eligibility.contains(EEligibility.M_120)) {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C_AND, MATH_124, C_OR_FOR,
                                    MATH_120, C_AND_ALSO_FOR, MATH_125, inTerm).eP();
                        } else {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C, MATH_124, C_AND, MATH_125,
                                    inTerm).eP();
                        }
                    } else if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, AND, MATH_124, C_OR_FOR, MATH_120, C_AND_ALSO_FOR,
                                MATH_125, inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, C, MATH_124, C_AND, MATH_125, inTerm).eP();
                    }
                } else if (eligibility.contains(EEligibility.M_117)) {
                    if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, AND, MATH_124, C_OR_FOR, MATH_120, C_AND_ALSO_FOR,
                                MATH_125, inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_124, C_AND, MATH_125, inTerm).eP();
                    }
                } else if (eligibility.contains(EEligibility.M_120)) {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_124, OR, MATH_120, C_AND_ALSO_FOR, MATH_125, inTerm).eP();
                } else {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_124, AND, MATH_125, inTerm).eP();
                }
            } else {
                // 125 but not 124
                if (eligibility.contains(EEligibility.M_118)) {
                    if (eligibility.contains(EEligibility.M_117)) {
                        if (eligibility.contains(EEligibility.M_120)) {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, AND, MATH_118, C_OR_FOR, MATH_120,
                                    C_AND_ALSO_FOR, MATH_125, inTerm).eP();
                        } else {
                            htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C_AND, MATH_125, inTerm).eP();
                        }
                    } else if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, OR, MATH_120, C_AND_ALSO_FOR, MATH_125,
                                inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, AND, MATH_125, inTerm).eP();
                    }
                } else if (eligibility.contains(EEligibility.M_117)) {
                    if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, OR, MATH_120, C_AND_ALSO_FOR, MATH_125,
                                inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, AND, MATH_125, inTerm).eP();
                    }
                } else if (eligibility.contains(EEligibility.M_120)) {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_120, AND, MATH_125, inTerm).eP();
                } else {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_125, inTerm).eP();
                }
            }
            // BELOW HERE DOES NOT NEED 126 or 125
        } else if (eligibility.contains(EEligibility.M_124)) {
            if (eligibility.contains(EEligibility.M_118)) {
                if (eligibility.contains(EEligibility.M_117)) {
                    if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C_AND, MATH_124, C_OR_FOR, MATH_120,
                                inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, C, MATH_118, C_AND, MATH_124, inTerm).eP();
                    }
                } else if (eligibility.contains(EEligibility.M_120)) {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, AND, MATH_124, C_OR_FOR, MATH_120, inTerm).eP();
                } else {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, AND, MATH_124, inTerm).eP();
                }
            } else if (eligibility.contains(EEligibility.M_117)) {
                if (eligibility.contains(EEligibility.M_120)) {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, AND, MATH_124, C_OR_FOR, MATH_120, inTerm).eP();
                } else {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, AND, MATH_124, inTerm).eP();
                }
            } else if (eligibility.contains(EEligibility.M_120)) {
                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_124, OR, MATH_120, inTerm).eP();
            } else {
                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_124, inTerm).eP();
            }
            // BELOW HERE DOES NOT NEED 124, 125, or 126
        } else {
            if (eligibility.contains(EEligibility.M_118)) {
                if (eligibility.contains(EEligibility.M_117)) {
                    if (eligibility.contains(EEligibility.M_120)) {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, AND, MATH_118, C_OR_FOR, MATH_120, inTerm).eP();
                    } else {
                        htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, AND, MATH_118, inTerm).eP();
                    }
                } else if (eligibility.contains(EEligibility.M_120)) {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, OR, MATH_120, inTerm).eP();
                } else {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_118, inTerm).eP();
                }
            } else if (eligibility.contains(EEligibility.M_117)) {
                if (eligibility.contains(EEligibility.M_120)) {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, OR, MATH_120, inTerm).eP();
                } else {
                    htm.sP().add(basedOn, IT_IS_IDEAL, MATH_117, inTerm).eP();
                }
            } else if (eligibility.contains(EEligibility.M_120)) {
                htm.sP().add(basedOn, IT_IS_IDEAL, MATH_120, inTerm).eP();
            } else {
                htm.sP().add(basedOn, "you just need to complete the All-University Core Curriculum requirement of 3 ",
                        "credits of Quantitative Reasoning.").eP();

                final double completed = data.getCreditsOfCoreCompleted();

                if (completed >= 3.0) {
                    htm.sP().add("You have already satisfied this core requirement.").eP();
                } else if (completed > 0.0) {
                    final long floor = (long) Math.floor(completed);
                    final String str = Long.toString(floor);
                    htm.sP().add("You have already completed ", str, " of these credits.").eP();
                }
            }
        }

        htm.eDiv();

        htm.div("vgap");
        htm.sDiv("advice");
        final boolean needsPlacement = showNextSteps(htm, plan, nextStep, eligibility, termName, isIncoming);
        htm.eDiv(); // advice

        return needsPlacement;
    }

    /**
     * Tests whether a student is "incoming".
     *
     * @param active  the active term
     * @param termKey the student's application term
     * @return trye if the student is "incoming" and can access the Precalculus tutorials
     */
    private static boolean testForIncoming(final TermKey active, final TermKey termKey) {

        boolean isIncoming = false;
        if (active != null && termKey != null && termKey.name == ETermName.FALL
            && (active.name == ETermName.SUMMER || active.name == ETermName.FALL)) {
            isIncoming = active.year.equals(termKey.year);
        }

        return isIncoming;
    }

    /**
     * Shows the student's next step(s), or a message telling them nothing more is needed.
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param plan        the student math plan
     * @param nextStep    the next step
     * @param eligibility the set of courses for which the student is ideally eligible
     * @param termName    the name of the student's first term
     * @param isIncoming  true if the student is "incoming" and can use Precalculus Tutorials
     */
    private static boolean showNextSteps(final HtmlBuilder htm, final StudentMathPlan plan,
                                         final ENextStep nextStep, final Set<EEligibility> eligibility,
                                         final String termName, final boolean isIncoming) {

        boolean needsPlacement = true;

        final String existing = buildExistingEligibility(plan, eligibility, termName);

        switch (nextStep) {
            case MSG_PLACEMENT_NOT_NEEDED, MSG_ALREADY_ELIGIBLE -> {
                htm.sP(CENTER);
                htm.addln("<img class='check' src='/images/welcome/check.png' alt=''/>");
                if (eligibility.contains(EEligibility.AUCC)) {
                    htm.add(SSTRONG,
                            "You are eligible to register for a Mathematics course appropriate for your program.",
                            ESTRONG);
                } else {
                    emitExistingIfNotBlank(htm, existing);
                }
                htm.eP(); // center
                needsPlacement = false;
            }

            case MSG_PLACE_INTO_117 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A, "become eligible for MATH 117 or MATH 120.", ESTRONG).eP();
                htm.sP().add(
                        "If you do not place into MATH 117 or MATH 120 on the Math Placement Tool, you can become ",
                        "eligible for those courses by completing the Entry Level Math Tutorial.").eP();
            }
            case MSG_PLACE_OUT_117 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A, "place out of MATH 117.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_INTO_118 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A,
                        "place out of MATH 117 and become eligible for MATH 118.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_118 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A, "place out of MATH 118.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_117_118 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A, "place out of MATH 117 and MATH 118.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117 and MATH 118", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_INTO_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_A,
                        "place out of MATH 117 and MATh 118 and become eligible for MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117 and MATh 118", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT, "place out of MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_118_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT, "place out of MATH 118 and MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118 and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_117_118_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT,
                        "place out of MATH 117, MATH 118, and MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_INTO_155 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3, "become eligible for MATH 155.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, MATH 124, and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT, "place out of MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT, "place out of MATH 125 and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 125 and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_118_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT,
                        "place out of MATH 118, MATH 125, and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118, MATH 125, and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_117_118_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AT,
                        "place out of MATH 117, MATH 118, MATH 125, and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, MATH 125, and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_124 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AL, "place out of MATH 124.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 124", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_118_124 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AL, "place out of MATH 118 and MATH 124.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118 and MATH 124", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_117_118_124 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_AL,
                        "place out of MATh 117, MATH 118, and MATH 124.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, and MATH 124", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_124_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3, "place out of MATH 124 and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 124 and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_124_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3,
                        "place out of MATH 124, MATH 125, and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 124, MATH 125, and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_118_124_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3,
                        "place out of MATH 118, MATH 124, MATH 125, and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118, MATH 124, MATH 125, and MATH 126", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
            case MSG_PLACE_OUT_117_118_124_125_126 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3,
                        "place out of MATH 117, MATH 118, MATH 124, MATH 125, and MATH 126.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, MATH 124, MATH 125, and MATH 126",
                            YOU_CAN_DO_TUTORIAL).eP();
                }
            }

            case MSG_PLACE_OUT_118_124_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3,
                        "place out of MATH 118, MATH 124, and MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 118, MATH 124, and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }

            case MSG_PLACE_OUT_117_118_124_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3,
                        "place out of MATH 117, MATH 118, MATH 124, and MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 117, MATH 118, MATH 124, and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }

            case MSG_PLACE_OUT_124_125 -> {
                emitExistingIfNotBlank(htm, existing);
                htm.sP().add(SSTRONG, YOU_SHOULD_COMPLETE_3, "place out of MATH 124 and MATH 125.", ESTRONG).eP();
                if (isIncoming) {
                    htm.sP().add(IF_NOT_PLACED, "MATH 124 and MATH 125", YOU_CAN_DO_TUTORIAL).eP();
                }
            }
        }

        return needsPlacement;
    }

    /**
     * Emits a paragraph with the "existing eligibility and credit" string if that string is not blank.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param existing the "existing eligibility" string
     */
    private static void emitExistingIfNotBlank(final HtmlBuilder htm, final String existing) {

        if (!existing.isBlank()) {
            htm.sP().add(existing).eP();
        }
    }

    /**
     * Displays the set of courses for which the student is already eligible or already has credit.
     *
     * @param plan        the student math plan
     * @param eligibility the set of courses for which the student is ideally eligible
     * @param termName    the name of the student's first term
     * @return the eligibility string; an empty string if the student is not eligible for any courses and has no credit
     */
    private static String buildExistingEligibility(final StudentMathPlan plan, final Set<EEligibility> eligibility,
                                                   final String termName) {

        final HtmlBuilder htm = new HtmlBuilder(50);

        // Display the relevant courses for which the student is eligible
        final Set<String> clearedFor = plan.getCanRegisterFor();
        final Set<String> doesNotHave = plan.getCanRegisterForAndDoesNotHave();

        final List<String> isEligibleFor = new ArrayList<>(10);
        final List<String> alreadyHas = new ArrayList<>(10);
        final boolean okFor120 = clearedFor.contains(RawRecordConstants.M117);

        for (final EEligibility e : eligibility) {
            if (e == EEligibility.M_117) {
                if (clearedFor.contains(RawRecordConstants.M117)) {
                    if (doesNotHave.contains(RawRecordConstants.M117)) {
                        isEligibleFor.add("MATH 117");
                    } else {
                        alreadyHas.add("MATH 117");
                    }
                }
            } else if (e == EEligibility.M_118) {
                if (clearedFor.contains(RawRecordConstants.M118)) {
                    if (doesNotHave.contains(RawRecordConstants.M118)) {
                        isEligibleFor.add("MATH 118");
                    } else {
                        alreadyHas.add("MATH 118");
                    }
                }
            } else if (e == EEligibility.M_124) {
                if (clearedFor.contains(RawRecordConstants.M124)) {
                    if (doesNotHave.contains(RawRecordConstants.M124)) {
                        isEligibleFor.add("MATH 124");
                    } else {
                        alreadyHas.add("MATH 124");
                    }
                }
            } else if (e == EEligibility.M_125) {
                if (clearedFor.contains(RawRecordConstants.M125)) {
                    if (doesNotHave.contains(RawRecordConstants.M125)) {
                        isEligibleFor.add("MATH 125");
                    } else {
                        alreadyHas.add("MATH 125");
                    }
                }
            } else if (e == EEligibility.M_126) {
                if (clearedFor.contains(RawRecordConstants.M126)) {
                    if (doesNotHave.contains(RawRecordConstants.M126)) {
                        isEligibleFor.add("MATH 126");
                    } else {
                        alreadyHas.add("MATH 126");
                    }
                }
            } else if (e == EEligibility.M_141) {
                if (clearedFor.contains(RawRecordConstants.M141)) {
                    if (doesNotHave.contains(RawRecordConstants.M141)) {
                        isEligibleFor.add("MATH 141");
                    } else {
                        alreadyHas.add("MATH 141");
                    }
                }
            } else if (e == EEligibility.M_155) {
                if (clearedFor.contains(RawRecordConstants.M155)) {
                    if (doesNotHave.contains(RawRecordConstants.M155)) {
                        isEligibleFor.add("MATH 155");
                    } else {
                        alreadyHas.add("MATH 155");
                    }
                }
            } else if (e == EEligibility.M_156) {
                if (clearedFor.contains(RawRecordConstants.M156)) {
                    if (doesNotHave.contains(RawRecordConstants.M156)) {
                        isEligibleFor.add("MATH 156");
                    } else {
                        alreadyHas.add("MATH 156");
                    }
                }
            } else if (e == EEligibility.M_160) {
                if (clearedFor.contains(RawRecordConstants.M160)) {
                    if (doesNotHave.contains(RawRecordConstants.M160)) {
                        isEligibleFor.add("MATH 160");
                    } else {
                        alreadyHas.add("MATH 160");
                    }
                }
            }
        }

        final int numEligible = isEligibleFor.size();
        if (numEligible > 0) {
            htm.add(SSTRONG, "You are eligible to register for ");
            final String first = isEligibleFor.getFirst();
            if (numEligible == 1) {
                htm.add(first);
            } else if (numEligible == 2) {
                final String second = isEligibleFor.get(1);
                htm.add(first, AND, second);
            } else {
                htm.add(first);
                for (int i = 1; i < numEligible - 1; ++i) {
                    final String item = isEligibleFor.get(i);
                    htm.add(C, item);
                }
                final String last = isEligibleFor.get(numEligible - 1);
                htm.add(C_AND, last);
            }

            if (isShowOr120(eligibility, okFor120, isEligibleFor)) {
                htm.add(" (or MATH 120)");
            }
            htm.add(" in ", termName, ". ", ESTRONG);
        }

        final int numAlready = alreadyHas.size();
        if (!alreadyHas.isEmpty()) {
            htm.add("You already have credit for ");
            final String frist = alreadyHas.get(0);
            if (numAlready == 1) {
                htm.add(frist);
            } else if (numAlready == 2) {
                final String second = alreadyHas.get(1);
                htm.add(frist, AND, second);
            } else {
                htm.add(frist);
                for (int i = 1; i < numAlready - 1; ++i) {
                    final String item = alreadyHas.get(i);
                    htm.add(C, item);
                }
                final String last = alreadyHas.get(numAlready - 1);
                htm.add(C_AND, last);
            }
            htm.add(".");
        }

        return htm.toString();
    }

    /**
     * Tests whether to show " (or MATH 120)" after a list of courses.
     *
     * @param eligibility   the set of courses for which the student is ideally eligible
     * @param okFor120      true if the student is OK for 120
     * @param isEligibleFor the list of courses for which the student is already eligible
     * @return true if " (or MATH 120)" should be shown
     */
    private static boolean isShowOr120(final Collection<EEligibility> eligibility, final boolean okFor120,
                                       final Collection<String> isEligibleFor) {

        return eligibility.contains(EEligibility.M_120) && okFor120 &&
               (isEligibleFor.contains(RawRecordConstants.MATH117)
                || isEligibleFor.contains(RawRecordConstants.MATH118)
                || isEligibleFor.contains(RawRecordConstants.MATH124));
    }

    /**
     * Called when a POST is received to the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final Profile profile = site.site.profile;

        final boolean aff1 = req.getParameter("affirm1") != null;
        final boolean aff2 = req.getParameter("affirm2") != null;

        // Only perform updates if this is not an adviser using "Act As"
        if (session.actAsUserId == null) {
            final String stuId = session.getEffectiveUserId();
            final StudentData studentData = cache.getStudent(stuId);
            final RawStudent student = studentData.getStudentRecord();

            final ZonedDateTime sessionNow = session.getNow();

            RawStmathplanLogic.deleteAllForPage(cache, stuId, MathPlanConstants.INTENTIONS_PROFILE);

            final List<Integer> questions = new ArrayList<>(2);
            final List<String> answers = new ArrayList<>(2);

            questions.add(ONE);
            answers.add(aff1 ? "Y" : "N");

            questions.add(TWO);
            answers.add(aff2 ? "Y" : "N");

            MathPlanLogic.storeMathPlanResponses(cache, student, MathPlanConstants.INTENTIONS_PROFILE, questions,
                    answers, sessionNow, session.loginSessionTag);

            // Store MPL test score in Banner SOATEST (1 if no placement needed, 2 if placement needed). This is
            // based on a response with version='WLCM5'.  If there is a row with survey_nbr=2 and stu_answer='Y', that
            // indicates placement is needed.  If there is a row with survey_nbr=1 and stu_answer='Y', that indicates
            // the math plan has been completed and placement is not needed. The MPL test score is '1' if placement
            // is not needed, and '2' if placement is needed.

            if (aff1) {
                final String desiredMPLTestScore = aff2 ? "2" : "1";

                final Login liveCtx = profile.getLogin(ESchema.LIVE);
                final DbConnection liveConn = liveCtx.checkOutConnection();
                try {
                    // Query the test score, see if this update represents a change, and only insert a new test score
                    // row if the result has changed...  People may do the math plan several times with the same
                    // outcome, and we don't need to insert the same result each time.
                    final List<RawMpscorequeue> existing = RawMpscorequeueLogic.querySORTESTByStudent(liveConn,
                            student.pidm);

                    RawMpscorequeue mostRecent = null;
                    for (final RawMpscorequeue test : existing) {

                        // Log.info("Found '", test.testCode, "' test score of '", test.testScore, "' for student ",
                        //         data.student.stuId, " with PIDM ", data.student.pidm);

                        if ("MPL".equals(test.testCode)) {
                            if (mostRecent == null || mostRecent.testDate.isBefore(test.testDate)) {
                                mostRecent = test;
                            }
                        }
                    }

                    if (mostRecent == null || !desiredMPLTestScore.equals(mostRecent.testScore)) {
                        final LocalDateTime now = LocalDateTime.now();
                        final RawMpscorequeue newRow = new RawMpscorequeue(student.pidm, "MPL", now,
                                desiredMPLTestScore);

                        Log.info("Inserting MPL test score of ", desiredMPLTestScore, " for student ", stuId,
                                " with PIDM ", student.pidm);

                        if (!RawMpscorequeueLogic.insertSORTEST(liveConn, newRow)) {
                            Log.warning("Failed to insert 'MPL' test score for ", student.stuId);
                        }
                    }
                } finally {
                    liveCtx.checkInConnection(liveConn);
                }
            }
        }

        resp.sendRedirect("plan_start.html");
    }
}
