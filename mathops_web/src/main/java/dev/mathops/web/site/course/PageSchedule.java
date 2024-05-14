package dev.mathops.web.site.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.EExamStructure;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawPacingRulesLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCunit;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPacingRules;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteDataActivity;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;
import dev.mathops.session.sitelogic.data.SiteDataCourse;
import dev.mathops.session.sitelogic.data.SiteDataMilestone;
import dev.mathops.session.sitelogic.data.SiteDataRegistration;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import dev.mathops.web.site.course.data.CourseData;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the content of the web page that displays the student's progress status in a single course. If the course
 * requires weekly progress, this page displays the weekly progress requirement and the student's progress level.
 * Otherwise, it displays a table of recommended dated by which each element of the course should be completed.
 */
enum PageSchedule {
    ;

    /**
     * Generates the page that shows the student's term schedule and deadlines.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @param logic   the site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null,
                false, true);

        htm.sDiv("menupanelu");
        CourseMenu.buildMenu(cache, site, session, logic, htm);
        htm.sDiv("panelu");

        doScheduleContent(cache, logic, htm);

        htm.eDiv(); // panelu
        htm.eDiv(); // menupanelu

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Constructs the course schedule content.
     *
     * @param cache the data cache
     * @param logic the course site logic
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    static void doScheduleContent(final Cache cache, final CourseSiteLogic logic,
                                  final HtmlBuilder htm) throws SQLException {

        // Build list of registrations relevant to this context
        final List<RawCourse> webSiteCourses = logic.data.contextData.getCourses();

        final SiteDataRegistration regData = logic.data.registrationData;

        final List<RawStcourse> tempList = new ArrayList<>(6);

        final List<RawStcourse> tempAllReg = regData.getRegistrations();
        for (final RawStcourse reg : tempAllReg) {

            // Ignore placement-credit registrations
            // FIXME remove once getRegistrationData omits AP credit records

            // Ignore forfeit registrations
            // Ignore synthetic registrations
            if ("OT".equals(reg.instrnType) || "G".equals(reg.openStatus) || reg.synthetic) {
                continue;
            }

            for (final RawCourse wsCourse : webSiteCourses) {
                if (reg.course.equals(wsCourse.course)) {
                    tempList.add(reg);
                    break;
                }
            }
        }

        final List<RawStcourse> allReg = new ArrayList<>(tempList);
        tempList.clear();

        final List<RawStcourse> tempPaceReg = regData.getPaceRegistrations();
        if (tempPaceReg != null) {
            for (final RawStcourse reg : tempPaceReg) {

                // Ignore placement-credit registrations

                // Ignore forfeit registrations
                // Ignore synthetic registrations
                if ("OT".equals(reg.instrnType) || "G".equals(reg.openStatus) || reg.synthetic) {
                    continue;
                }

                for (final RawCourse wsCourse : webSiteCourses) {
                    if (reg.course.equals(wsCourse.course)) {
                        tempList.add(reg);
                        break;
                    }
                }
            }
        }

        final List<RawStcourse> paceReg = new ArrayList<>(tempList);
        tempList.clear();

        if (sortPaceOrder(paceReg)) {
            presentSchedule(cache, logic, htm, allReg, paceReg);
        } else {
            presentCourseOrderPage(cache, logic, htm, allReg, paceReg);
        }
    }

    /**
     * Scans the list of registrations ensuring that all registrations which will be included in the pace have a pace
     * order assigned, that the order sequence has no gaps, and that completed courses come before open courses come
     * before unopened courses, with incompletes sorted before current term courses.
     *
     * @param paceReg those registrations which contribute to pace
     * @return {@code true} if registrations all have pace order and are sorted without gaps or duplicates;
     *         {@code false} otherwise
     */
    static boolean sortPaceOrder(final List<RawStcourse> paceReg) {

        boolean allInOrder = true;

        // Verify all have a pace order specified
        for (final RawStcourse reg : paceReg) {
            if (reg.paceOrder == null) {
                Log.warning("Registration for " + reg.stuId + " in " + reg.course
                        + " with no pace order assigned - resetting pace orders");
                allInOrder = false;
                break;
            }
        }

        // Verify that all pace orders from 1 to N are present (guarantees no duplicates or gaps)
        int which = 1;
        while (allInOrder && which <= paceReg.size()) {
            allInOrder = false;
            for (final RawStcourse reg : paceReg) {
                if (reg.paceOrder.intValue() == which) {
                    allInOrder = true;
                    break;
                }
            }

            if (!allInOrder) {
                Log.warning("No course with pace order " + which + " found for ", paceReg.get(0).stuId,
                        " (pace " + paceReg.size() + ") - resetting pace orders");
            }

            ++which;
        }

        if (allInOrder) {
            // Sort them in pace order
            RawStcourse hold;

            for (int i = 0; i < paceReg.size(); ++i) {
                if (paceReg.get(i).paceOrder.intValue() == i + 1) {
                    // Already in its proper place
                    continue;
                }
                // Find the one that belongs at [i] and swap it into place
                for (int j = i + 1; j < paceReg.size(); ++j) {
                    if (paceReg.get(j).paceOrder.intValue() == i + 1) {
                        hold = paceReg.get(i);
                        paceReg.set(i, paceReg.get(j));
                        paceReg.set(j, hold);
                        break;
                    }
                }
                // Now, [i] contains the correct row.
            }
        }

        // FIXME: This appears bogus.

        // ESchedulePhase phase = ESchedulePhase.inc_completed;
        // for (final RawStcourse reg : paceReg) {
        //
        // if ("Y".equals(reg.iInProgress)) {
        // if ("Y".equals(reg.completed)
        // || "G".equals(reg.openStatus)) {
        // // Completed incomplete - must be within inc_completed phase
        // if (phase != ESchedulePhase.inc_completed) {
        // allInOrder = false;
        // break;
        // }
        // } else if ("Y".equals(reg.openStatus)) {
        // // Open incomplete - must be within inc_open phase or earlier
        // if (phase == ESchedulePhase.inc_completed) {
        // phase = ESchedulePhase.inc_open;
        // } else if (phase != ESchedulePhase.inc_open) {
        // allInOrder = false;
        // break;
        // }
        // } else // Not yet open incomplete - must be within inc_unopened or earlier phase
        // if (phase == ESchedulePhase.inc_completed || phase == ESchedulePhase.inc_open) {
        // phase = ESchedulePhase.inc_unopened;
        // } else if (phase != ESchedulePhase.inc_unopened) {
        // allInOrder = false;
        // break;
        // }
        // } else if ("Y".equals(reg.completed)
        // || "N".equals(reg.openStatus)) {
        // // Completed regular course - must be within completed or earlier phase
        // if (phase == ESchedulePhase.inc_completed || phase == ESchedulePhase.inc_open
        // || phase == ESchedulePhase.inc_unopened) {
        // phase = ESchedulePhase.completed;
        // } else if (phase != ESchedulePhase.completed) {
        // Log.warning("Registration for " + reg.stuId + " in " + reg.course
        // + " was completed but follows open course in pace order");
        // allInOrder = false;
        // break;
        // }
        // } else if ("Y".equals(reg.openStatus)) {
        // // Open regular course - must be within open phase or earlier
        // if (phase == ESchedulePhase.inc_completed || phase == ESchedulePhase.inc_open
        // || phase == ESchedulePhase.inc_unopened || phase == ESchedulePhase.completed) {
        // phase = ESchedulePhase.open;
        // } else if (phase != ESchedulePhase.open) {
        // allInOrder = false;
        // break;
        // }
        // }
        // }

        return allInOrder;
    }

    /**
     * Generates the page that shows the student's term schedule and deadlines.
     *
     * @param cache   the data cache
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @param allReg  all student registrations (includes those not in pace)
     * @param paceReg the student registrations that contribute toward pace
     * @throws SQLException if there is an error accessing the database
     */
    private static void presentSchedule(final Cache cache, final CourseSiteLogic logic,
                                        final HtmlBuilder htm, final Collection<RawStcourse> allReg,
                                        final Collection<RawStcourse> paceReg)
            throws SQLException {

        htm.sH(2).add("My Exam Deadlines").eH(2);

        presentOpeningText(htm, allReg, paceReg);

        if (allReg.size() > paceReg.size()) {
            presentNonPaceCourses(logic, htm, allReg, paceReg);
        }

        if (!paceReg.isEmpty()) {
            final boolean allMidterms = presentPaceCourses(cache, logic, htm, paceReg);

            final TermRec active = TermLogic.get(cache).queryActive(cache);
            htm.addln("<ul class='boxlist'>");

            if (!allMidterms) {
                htm.addln("<li class='boxlist'>");
                htm.addln(" You may retake Unit Exams and Final Exams through the last regular ",
                        "class day of the ", active.term.longString, " term to improve ",
                        "your score in any course in which the Final Exam is passed by the deadline ",
                        "date.");
                htm.addln("</li>");
            }

            // If we're still before the term withdrawal deadline, show a message
            final LocalDate wDeadline = active.withdrawDeadline;
            if (paceReg.size() > 1 && wDeadline != null
                    && !wDeadline.isBefore(logic.data.now.toLocalDate())) {
                htm.addln("<li class='boxlist'>");
                htm.addln(" If you fail to complete a course by its deadline, you may withdraw ",
                        "from one or more courses, which will adjust the deadline dates for the ",
                        "courses that remain.");
                htm.addln("</li>");
            }
        }
    }

    /**
     * Presents the opening text that describes the number of non-pace incompletes and the number of paced courses the
     * student must complete.
     *
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @param allReg  the list of all registrations
     * @param paceReg the list of registrations that contribute toward pace
     */
    private static void presentOpeningText(final HtmlBuilder htm, final Collection<RawStcourse> allReg,
                                           final Collection<RawStcourse> paceReg) {

        htm.sDiv("indent22");
        final int numNonPace = allReg.size() - paceReg.size();

        if (numNonPace == 0) {
            // All courses are paced
            int numReal = 0;
            for (final RawStcourse test : paceReg) {
                if (!test.synthetic) {
                    ++numReal;
                }
            }

            if (numReal == 0) {
                htm.sP().add("<strong>You are not registered in any Precalculus courses this semester.").eP();
            } else {
                htm.sP().add("You must <strong>PASS</strong> each exam listed below by its deadline date.").eP();
            }
        } else {
            // There is at least one non-paced course - count incompletes
            int numInc = 0;
            for (final RawStcourse reg : allReg) {
                if (reg.iDeadlineDt != null) {
                    ++numInc;
                }
            }

            // Show messages relating to non-paced courses
            if (numInc == 0) {
                htm.addln("<p>You have <strong>",
                        allReg.size() == 1 ? "1 course" : allReg.size() + " courses",
                        "</strong> to finish this semester.</p>");
            } else if (numInc == 1) {
                if (allReg.size() == 1) {
                    htm.addln("<p>You have <strong>1 incomplete course</strong> to finish ",
                            "this semester.</p>");
                } else {
                    htm.addln("<p>You have <strong>1 incomplete course</strong> and <strong>",
                            allReg.size() == 2 ? "1 course" : allReg.size() - 1 + " courses",
                            "</strong> to finish this semester.</p>");
                }
            } else if (allReg.size() == numInc) {
                htm.addln("<p>You have <strong>", Integer.toString(numInc),
                        " incomplete courses</strong> to finish this semester.</p>");
            } else {
                htm.addln("<p>You have <strong>", Integer.toString(numInc),
                        " incomplete courses</strong> and <strong>",
                        allReg.size() == numInc + 1 ? "1 course"
                                : allReg.size() - numInc + " courses",
                        "</strong> to finish this semester.</p>");
            }

            // Show messages relating to paced courses, if any
            if (!paceReg.isEmpty()) {
                int numReal = 0;
                for (final RawStcourse test : paceReg) {
                    if (!test.synthetic) {
                        ++numReal;
                    }
                }

                htm.addln("<p>In addition, you must complete <strong>");
                if (numReal == 1) {
                    htm.addln("1 Precalculus course");
                } else {
                    htm.addln(Integer.toString(numReal), " Precalculus courses");
                }
                htm.addln("</strong> according to the schedule shown below.</p>");

                htm.addln("<p>You must <strong>PASS</strong> each exam listed below by its ",
                        "deadline date.</p>");
            }
        }

        htm.eDiv().hr(); // indent22
    }

    /**
     * Presents information on the deadline dates of incomplete courses that do not contribute toward the student's
     * pace.
     *
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @param allReg  the list of all registrations
     * @param paceReg the list of registrations that contribute toward pace
     */
    private static void presentNonPaceCourses(final CourseSiteLogic logic, final HtmlBuilder htm,
                                              final Iterable<RawStcourse> allReg,
                                              final Iterable<RawStcourse> paceReg) {

        // Get the list of non-pace courses
        final RawStcourse[] nonPace;
        int numInc = 0;

        final List<RawStcourse> tempList = new ArrayList<>(6);

        for (final RawStcourse reg : allReg) {

            if ("OT".equals(reg.instrnType)) {
                continue;
            }

            boolean hit = false;
            for (final RawStcourse pace : paceReg) {
                if (reg.course.equals(pace.course) && reg.sect.equals(pace.sect)) {
                    hit = true;
                    break;
                }
            }

            if (!hit) {
                tempList.add(reg);
                if (reg.iDeadlineDt != null) {
                    ++numInc;
                }
            }
        }
        nonPace = tempList.toArray(new RawStcourse[0]);

        if (numInc > 0) {
            htm.sH(3).add("Incomplete ", numInc == 1 ? "course" : "courses").eH(3);

            htm.sDiv("indent22");

            for (final RawStcourse reg : nonPace) {
                final LocalDate deadline = reg.iDeadlineDt;

                if (deadline == null) {
                    continue;
                }

                final TermKey incTerm = reg.iTermKey;

                final SiteDataCfgCourse courseData = logic.data.courseData.getCourse(reg.course, reg.sect);
                if (courseData == null) {
                    htm.addln(" <strong>", reg.course, "</strong> - unable to query course status.");

                    final Map<String, Map<String, SiteDataCfgCourse>> courses =  logic.data.courseData.getCourses();
                    for (final Map.Entry<String, Map<String, SiteDataCfgCourse>> e1 : courses.entrySet()) {
                        final String courseId = e1.getKey();
                        for (final Map.Entry<String, SiteDataCfgCourse> e2 : e1.getValue().entrySet()) {
                            final String sect = e2.getKey();
                            Log.info("  Found ", courseId, " sect ", sect);
                        }
                    }
                } else {
                    final RawCourse course = courseData.course;

                    htm.sP();
                    htm.addln(" <strong>", course.courseLabel, "</strong> (an incomplete from the ", incTerm.name.fullName,
                            ", ", incTerm.year, " semester) must be completed by <strong>",
                            TemporalUtils.FMT_WMDY.format(deadline), "</strong>.");

                    final LocalDate today = logic.data.now.toLocalDate();
                    final LocalDate plus1 = today.plusDays(1L);
                    final LocalDate plus2 = today.plusDays(2L);
                    final LocalDate plus3 = today.plusDays(3L);

                    if ((!today.isAfter(deadline) && !plus3.isBefore(deadline))) {
                        htm.add(" <span class='redred'>(<strong>DEADLINE IS ");
                        if (deadline.isEqual(today)) {
                            htm.add("TODAY");
                        } else if (deadline.isEqual(plus1)) {
                            htm.add("TOMORROW");
                        } else if (deadline.isEqual(plus2)) {
                            htm.add("2 DAYS FROM TODAY");
                        } else if (deadline.isEqual(plus3)) {
                            htm.add("3 DAYS FROM TODAY)");
                        }
                        htm.addln("</strong>)</span>");
                    }
                }
                htm.eP();
            }
            htm.eDiv().hr(); // indent22

            // TODO: Show schedule with deadline dates and sources of points for Incomplete course
        }

        final int numNonPace = nonPace.length - numInc;

        if (numNonPace > 0) {
            htm.sH(3);
            if (numNonPace == 1) {
                htm.add("Course which is not part of your deadline schedule");
            } else {
                htm.add("Courses which are not part of your deadline schedule");
            }
            htm.eH(3);

            htm.sDiv("indent22");
            for (final RawStcourse reg : nonPace) {
                final LocalDate deadline = reg.iDeadlineDt;

                if (deadline != null) {
                    continue;
                }

                final RawCourse course = logic.data.courseData.getCourse(reg.course, reg.sect).course;

                htm.sP().add("<strong>", course.courseLabel, "</strong>").eP();
            }
            htm.eDiv().hr(); // indent22
        }
    }

    /**
     * Presents information on the deadline dates, status, and scoring penalties of pace courses.
     *
     * @param cache   the data cache
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @param paceReg the list of registrations that contribute toward pace
     * @return {@code true} if all registrations are based on midterms rather than unit and final exams; {@code false}
     *         if at least one course uses unit and final exams
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean presentPaceCourses(final Cache cache, final CourseSiteLogic logic,
                                              final HtmlBuilder htm, final Iterable<RawStcourse> paceReg)
            throws SQLException {

        final TermRec active = TermLogic.get(cache).queryActive(cache);
        htm.sH(3).add("Courses Scheduled for the ", active.term.longString, " Semester").eH(3);

        htm.sDiv("indent22");

        final SiteDataCourse courseData = logic.data.courseData;
        for (final RawStcourse reg : paceReg) {

            final String courseId = reg.course;
            final String sectNum = reg.sect;
            final SiteDataCfgCourse cfgCourse = courseData.getCourse(courseId, sectNum);
            final RawCourse course = cfgCourse.course;
            final RawCsection sect = cfgCourse.courseSection;
            final RawPacingStructure pacingStructure = cfgCourse.pacingStructure;

            final Integer[] unitNums = courseData.getUnitsForCourse(courseId);
            final int numUnits = unitNums.length;

            final RawCusection[] csUnits = new RawCusection[numUnits];
            for (int j = 0; j < numUnits; ++j) {
                csUnits[j] = courseData.getCourseUnit(courseId, unitNums[j]).courseSectionUnit;
            }

            if (course == null || sect == null || pacingStructure == null) {
                htm.sP();
                htm.addln(" <span class='redred'>");
                htm.addln("  ", courseId, " has a configuration error.");
                htm.addln("  Please contact the Precalculus Center at ",
                        "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>",
                        "precalc_math@colostate.edu</a> to resolve this problem.");
                htm.addln(" </span>");
                htm.eP();
            } else {
                presentPaceCourse(cache, logic, htm, reg, course, sect, csUnits, pacingStructure);
            }
        }
        htm.eDiv(); // indent22

        return true;
    }

    /**
     * Presents information on a single pace course.
     *
     * @param cache           the data cache
     * @param logic           the site logic
     * @param htm             the {@code HtmlBuilder} to which to append the HTML
     * @param reg             the registration record
     * @param course          the course record
     * @param courseSect      the course record
     * @param csUnits         the set of units records
     * @param pacingStructure the pacing structure record
     * @throws SQLException if there is an error accessing the database
     */
    private static void presentPaceCourse(final Cache cache, final CourseSiteLogic logic,
                                          final HtmlBuilder htm, final RawStcourse reg, final RawCourse course,
                                          final RawCsection courseSect, final RawCusection[] csUnits,
                                          final RawPacingStructure pacingStructure) throws SQLException {

        final String courseId = reg.course;
        final String sectionNum = reg.sect;

        boolean doFinal = false;

        final EExamStructure exStruct = RawCsectionLogic.getExamStructure(courseSect);

        if (exStruct == EExamStructure.UNIT_FINAL) {
            doFinal = true;
        }

        htm.sP();

        htm.sDiv("left").add("<img src='/images/stock-jump-to-32.png'/> &nbsp;").eDiv();

        htm.sDiv("larger", "style='height:32px;line-height:24px;'");
        htm.addln(" <strong>", course.courseLabel, ": ", course.courseName, ", <span class='gray'>Section ", sectionNum,
                "</span></strong>");

        if (reg.iDeadlineDt != null) {
            htm.addln(" (Incomplete)");
        }
        htm.eDiv(); // larger

        htm.div("clear");

        boolean started = false;

        final SiteDataActivity actData = logic.data.activityData;
        final SiteDataCourse courseData = logic.data.courseData;
        final SiteDataMilestone msData = logic.data.milestoneData;

        // Print all pace deadlines, including student override dates
        final TermRec term = TermLogic.get(cache).queryActive(cache);

        final List<RawMilestone> allMilestones = msData.getMilestones(term.term);
        final List<RawStmilestone> stMilestones = msData.getStudentMilestones(term.term);
        final LocalDate today = logic.data.now.toLocalDate();

        final int paceOrder = reg.paceOrder == null ? -1 : reg.paceOrder.intValue();

        LocalDate finalDeadline = null;
        LocalDate lastTry = null;
        int lastTriesTaken = 0;
        int lastTriesAllowed = 0;

        if (allMilestones != null && stMilestones != null) {
            for (final RawMilestone milestoneRec : allMilestones) {
                final String type = milestoneRec.msType;

                if ("US".equals(type) || "SR".equals(type)
                        || "H1".equals(type) || "H2".equals(type)
                        || "H3".equals(type) || "H4".equals(type)
                        || "H5".equals(type) || "UE".equals(type)) {
                    continue;
                }

                final int msNumber = milestoneRec.msNbr.intValue();

                // Skip any pace records for other courses
                if (msNumber / 10 % 10 != paceOrder) {
                    continue;
                }

                // Get the milestone date, overriding as needed for student pace record
                LocalDate milestoneDate = milestoneRec.msDate;
                Integer milestoneAttempts = milestoneRec.nbrAtmptsAllow;
                for (final RawStmilestone stpaceRec : stMilestones) {
                    if (stpaceRec.msNbr.equals(milestoneRec.msNbr) && stpaceRec.msType.equals(milestoneRec.msType)) {

                        milestoneDate = stpaceRec.msDate;
                        milestoneAttempts = stpaceRec.nbrAtmptsAllow;
                    }
                }

                final Integer unit = Integer.valueOf(msNumber % 10);

                // Get the deadline and see if it has been overridden for the student
                final LocalDate deadline = milestoneDate;

                final String examType;
                final Integer ontime;

                RawCusection unitModel = null;
                for (final RawCusection csUnit : csUnits) {
                    if (csUnit != null && csUnit.unit.intValue() == unit.intValue()) {
                        unitModel = csUnit;
                        break;
                    }
                }

                if (unitModel == null) {
                    continue;
                }

                if ("FE".equals(type)) {
                    if (!doFinal) {
                        continue;
                    }
                    examType = "F";
                    ontime = null;

                    finalDeadline = deadline;

                } else if ("F1".equals(type)) {

                    if (!doFinal) {
                        continue;
                    }

                    RawStexam firstPassing = null;
                    // FIXME: Hardcoded unit number 4
                    final List<RawStexam> stuExams =
                            actData.getStudentExams(courseId, Integer.valueOf(4));
                    for (final RawStexam stexam : stuExams) {
                        if ("U".equals(stexam.examType) && "Y".equals(stexam.isFirstPassed)) {
                            firstPassing = stexam;
                            break;
                        }
                    }

                    if (firstPassing != null && finalDeadline != null) {

                        final LocalDate finishedOnDay = firstPassing.examDt;

                        if (!finishedOnDay.isAfter(finalDeadline)) {

                            // Student is eligible for last-try - see if they have already used it!
                            lastTry = milestoneDate;
                            lastTriesAllowed = milestoneAttempts == null ? 1 : milestoneAttempts.intValue();

                            final List<RawStexam> exams =
                                    actData.getStudentExams(courseId, Integer.valueOf(5));

                            // As before, count attempts finished in the first 5 minutes of a day
                            // as if they happened on the day before
                            for (final RawStexam exam : exams) {
                                if ("F".equals(exam.examType) && exam.examDt.isAfter(finalDeadline)) {
                                    ++lastTriesTaken;
                                }
                            }
                        }
                    }
                    // Emit no text for this type of milestone
                    continue;
                } else if ("RE".equals(type)) {
                    examType = "R";
                    ontime = unitModel.rePointsOntime;
                } else {
                    examType = "U";
                    ontime = null;
                }

                final boolean hasPointPenalty = ontime != null && ontime.intValue() > 0;

                final List<RawExam> unitExams = courseData.getCourseUnit(courseId, unit).getExams();
                RawExam theExam = null;
                for (final RawExam exam : unitExams) {
                    if (exam.examType.equals(examType)) {
                        theExam = exam;
                        break;
                    }
                }

                if (theExam != null) {
                    final String examTitle = theExam.buttonLabel;

                    final List<RawStexam> unitStExams = actData.getStudentExams(courseId, unit);
                    RawStexam firstPassing = null;
                    int highestPassing = -1;
                    int highestRaw = -1;
                    for (final RawStexam test : unitStExams) {
                        if (examType.equals(test.examType)) {
                            if ("Y".equals(test.isFirstPassed)) {
                                firstPassing = test;
                            }
                            final int score = test.examScore.intValue();
                            if ("Y".equals(test.passed)) {
                                highestPassing = Math.max(highestPassing, score);
                            }
                            highestRaw = Math.max(highestRaw, score);
                        }
                    }

                    final HtmlBuilder xml = new HtmlBuilder(50);
                    if (highestPassing > -1) {
                        if (firstPassing == null || !hasPointPenalty) {
                            xml.add("<span class='green'>Passed</span>");
                        } else {
                            final LocalDate finishedOnDay = firstPassing.examDt;

                            if (finishedOnDay.isAfter(deadline)) {
                                xml.add("<span class='green'>Passed</span> <span class='orange'>(on ",
                                        TemporalUtils.FMT_MD.format(finishedOnDay), ", LATE)</span>");
                            } else {
                                xml.add("<span class='green'>Passed (on ", TemporalUtils.FMT_MD.format(finishedOnDay),
                                        ", ON TIME)</span>");
                            }
                        }
                    } else {
                        xml.add("<span class='orange'>");
                        if (highestRaw > -1) {
                            xml.add("Not yet passed");
                        } else {
                            xml.add("Not yet attempted");
                        }

                        if (deadline.isEqual(today)) {
                            xml.add(" (<strong>DEADLINE IS TODAY</strong>)");
                        } else if (deadline.isEqual(today.plusDays(1L))) {
                            xml.add(" (<strong>DEADLINE IS TOMORROW</strong>)");
                        } else if (deadline.isEqual(today.plusDays(2L))) {
                            xml.add(" (<strong>DEADLINE IS 2 DAYS FROM TODAY</strong>)");
                        } else if (deadline.isEqual(today.plusDays(3L))) {
                            xml.add(" (<strong>DEADLINE IS 3 DAYS FROM TODAY</strong>)");
                        }

                        xml.add("</span>");
                    }

                    if (!started) {
                        htm.addln(" <div class='indent3'>");
                        htm.addln(" <table class='pacetable'>");
                        htm.addln("  <tr>");
                        htm.addln("   <th class='paceh'>Exam:</th>");
                        htm.addln("   <th class='paceh'>Deadline:</th>");
                        htm.addln("   <th class='paceh'>Status:</th>");
                        htm.addln("  </tr>");
                        started = true;
                    }

                    // TODO: Do we want messaging to the student when they have earned the extended
                    // final exam deadline? If so, test "if (extendedFinal != null)" and include
                    // the messaging in the table.

                    htm.addln("  <tr>");
                    htm.addln("   <td class='paced'>", examTitle, "</td>");
                    htm.add("   <td class='paced'>");
                    if (!"UE".equals(type)) {
                        if ((highestPassing > -1) || deadline.isAfter(today.plusDays(7L))) {
                            // Already passed, so no need to bold and color the deadline date
                            htm.add(TemporalUtils.FMT_WMDY.format(deadline));
                        } else if (deadline.isAfter(today)) {
                            htm.add("<span class='orange'><strong>", TemporalUtils.FMT_WMDY.format(deadline),
                                    "<strong></span>");
                        } else {
                            htm.add("<span class='redred'><strong>", TemporalUtils.FMT_WMDY.format(deadline),
                                    "<strong></span>");
                        }
                    }
                    htm.addln("</td>");
                    htm.addln("   <td class='paced'>", xml.toString(), "</td>");
                    htm.addln("  </tr>");
                }
            }

            if (started) {
                htm.addln(" </table>");
                htm.addln(" </div>");
            }
        }
        htm.eP();

        // After the table of deadlines, show the scoring penalties and business logic...

        int gwPenalty = 0;
        int rePenalty = 0;
        int uePenalty = 0;
        int fePenalty = 0;

        // Find the maximum point penalties for missing deadlines
        for (final RawCusection unit : csUnits) {

            if (unit == null) {
                continue;
            }

            final Integer rOntime = unit.rePointsOntime;
            final int irOntime = rOntime == null ? 0 : rOntime.intValue();

            final RawCunit cunit = courseData.getCourseUnit(courseId, Integer.valueOf(unit.unit.intValue())).courseUnit;
            if (cunit == null) {
                continue;
            }

            if ("SR".equals(cunit.unitType)) {
                gwPenalty = Math.max(gwPenalty, 0);
            } else if ("FIN".equals(cunit.unitType)) {
                fePenalty = Math.max(fePenalty, 0);
            } else {
                uePenalty = Math.max(uePenalty, 0);
                rePenalty = Math.max(rePenalty, irOntime);
            }
        }

        htm.sDiv("inednt22");
        htm.addln("<ul class='boxlist'>");

        // See if final exam has been passed, and if so, present a message

        if (rePenalty > 0) {
            final String penaltyStr =
                    rePenalty == 1 ? "1 point" : rePenalty + " points";

            htm.addln("<li class='boxlist'>");
            htm.addln(" Each <b>Review Exam</b> earns ", penaltyStr,
                    " if passed by 11:59 PM (<span class='red'>Mountain time zone</span>) on their ",
                    "deadline date. If a <b>Review Exam</b> is not passed by this time, you receive ",
                    "no points for the <b>Review Exam</b>.");

            final List<RawPacingRules> rsRules = RawPacingRulesLogic.queryByTermAndPacingStructure(
                    cache, TermLogic.get(cache).queryActive(cache).term,
                    pacingStructure.pacingStructure);

            boolean unitRequiresReview = false;
            for (final RawPacingRules rule : rsRules) {
                if ("UE".equals(rule.activityType)
                        && RawPacingRulesLogic.UR_MSTR.equals(rule.requirement)) {
                    unitRequiresReview = true;
                    break;
                }
            }

            if (unitRequiresReview) {
                htm.addln(" However, it must still be passed before you can take the corresponding <b>Unit Exam</b>.");
            }
            htm.addln("</li>");
        }

        final List<RawStexam> stExams = actData.getStudentExams(courseId, Integer.valueOf(5));

        RawStexam passedFinal = null;
        for (final RawStexam stexam : stExams) {
            if ("F".equals(stexam.examType) && "Y".equals(stexam.isFirstPassed)) {
                passedFinal = stexam;
                break;
            }
        }

        if (passedFinal != null) {
            htm.addln("<li class='boxlist'><span class='redred'>",
                    "<span class='lightgold' ",
                    "style='padding-top:2px;padding-bottom:2px;'>");
            htm.addln(" You are now eligible to retest on any proctored exam through the last ",
                    "day of classes to improve your point total in this course.");
            htm.addln("</span></span></li>");
        }

        if (doFinal && passedFinal == null) {
            if (fePenalty > 0) {
                final String penaltyStr = fePenalty == 1 ? "1 point" : fePenalty + " points";

                htm.addln("<li class='boxlist'>");
                htm.addln(" If the <b>Final Exam</b> is not passed by its deadline date, a ",
                        "late penalty of ", penaltyStr, "will be applied to your score on that ",
                        "exam, but the exam must still be passed to complete the course.");
                htm.addln("</li>");
            }

            if (lastTry == null) {
                final boolean earnedBonus =
                        paceOrder == 1 && RawSpecialStusLogic.isSpecialType(cache,
                                logic.sessionInfo.getEffectiveUserId(),
                                logic.sessionInfo.getNow().toLocalDate(), "UBONUS");

                final String count1 =
                        earnedBonus ? "<b>TWO</b> more opportunities" : "<b>ONE</b> more opportunity";
                final String count2 = earnedBonus ? "those attempts" : "that one attempt";

                htm.addln("<li class='boxlist'>");
                htm.addln(" If you become eligible for, but do not pass the <b>Final Exam</b> by ",
                        "its deadline date, you will be given ", count1, " to take the <b>Final ",
                        "Exam</b> on <b>THE VERY NEXT DAY THE PRECALCULUS CENTER IS OPEN</b>. If you ",
                        "do not pass on ", count2, ", you cannot complete the course.");
                htm.addln("</li>");
            } else if (!today.isAfter(lastTry)) {

                htm.addln("<li class='boxlist'><span class='redred'><strong>");
                final String lastTryText;
                if (lastTriesAllowed == 1) {
                    lastTryText = "ONE additional attempt";
                } else {
                    lastTryText = Integer.valueOf(lastTriesAllowed) + " additional attempts";
                }

                if (lastTriesTaken == 0) {
                    htm.addln(" Because you were eligible for the <b>Final Exam</b> by its due date, ",
                            "you have a total of ", lastTryText, " on the <b>Final Exam</b> by ",
                            TemporalUtils.FMT_MDY.format(lastTry));
                } else if (lastTriesTaken < lastTriesAllowed) {
                    htm.addln(" Because you were eligible for the <b>Final Exam</b> by its due date, you were given ");

                    if (lastTriesAllowed == 1) {
                        htm.addln(lastTryText);
                    } else {
                        htm.addln("a total of ", lastTryText);
                    }

                    htm.addln(" on the <b>Final Exam</b> by ", TemporalUtils.FMT_MDY.format(lastTry),
                            ". You have ALREADY USED ");
                    if (lastTriesTaken == 1) {
                        htm.addln("one attempt,");
                    } else {
                        htm.addln(Long.valueOf((long) lastTriesTaken), " attempts,");
                    }
                    htm.addln(" and have ", Integer.valueOf(lastTriesAllowed - lastTriesTaken), " remaining.");
                } else {
                    htm.addln(" You have used the ", lastTryText,
                            " on the <b>Final Exam</b> for which you were eligible.");
                }
                htm.addln("</strong></li>");
            }
        }

        htm.addln("</ul>");
        htm.eDiv().hr(); // indent22
    }

    /**
     * Generates a page that allows a user to place the set of courses which contribute towards their pace in order so
     * they can be assigned correct deadline dates. The order of any course that has been started is fixed.
     *
     * @param cache   the data cache
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @param allReg  all student registrations (includes those not in pace)
     * @param paceReg the list of courses that will contribute toward pace
     * @throws SQLException if there is an error accessing the database
     */
    private static void presentCourseOrderPage(final Cache cache, final CourseSiteLogic logic,
                                               final HtmlBuilder htm, final List<RawStcourse> allReg,
                                               final List<RawStcourse> paceReg)
            throws SQLException {

        // Clear existing course order values as something is wrong
        final int count = paceReg.size();
        for (int i = 0; i < count; ++i) {
            final RawStcourse reg = paceReg.get(i);

            if (reg.paceOrder != null) {
                Log.info("presentCourseOrderPage setting " + reg.course + " pace order to null for " + reg.stuId);
                SiteDataRegistration.updatePaceOrder(cache, reg, null);
                paceReg.set(i, reg);
            }
        }

        final Map<String, RawStcourse> tempMap = new HashMap<>(10);

        // First in the list are counted incompletes from prior terms - those which are
        // completed first, then those which are not yet completed, ordered first by deadline
        // date, then by course ID
        int order = orderIncompletes(cache, 1, paceReg, tempMap);

        // Next, add any non-incomplete courses that have been completed, or which have not yet
        // been completed but are open. Ideally, this order would be based on the order in
        // which work was done in the courses.
        order = orderCompletedOpen(cache, order, paceReg, tempMap);

        // Store those remaining courses in tempMap
        findRemaining(paceReg, tempMap);

        // If only one course remains unordered, there's no choice
        if (tempMap.size() == 1) {
            final RawStcourse reg = tempMap.values().iterator().next();
            final int index = paceReg.indexOf(reg);

            SiteDataRegistration.updatePaceOrder(cache, reg, Integer.valueOf(order));
            paceReg.set(index, reg);
            sortPaceOrder(paceReg);
            presentSchedule(cache, logic, htm, allReg, paceReg);
        } else {
            orderTwoOrMore(cache, logic, htm, allReg, paceReg, order, tempMap);
        }
    }

    /**
     * Generates a page that allows a user to place the set of courses which contribute towards their pace in order when
     * there are two or more courses whose order is not determined.
     *
     * @param cache   the data cache
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @param allReg  all student registrations (includes those not in pace)
     * @param paceReg the list of courses that will contribute toward pace
     * @param order   the next order to use in the sequence
     * @param tempMap a map from course to registration
     * @throws SQLException if there is an error accessing the database
     */
    private static void orderTwoOrMore(final Cache cache, final CourseSiteLogic logic,
                                       final HtmlBuilder htm, final Collection<RawStcourse> allReg,
                                       final List<RawStcourse> paceReg, final int order,
                                       final Map<String, RawStcourse> tempMap) throws SQLException {

        // If only one has prerequisites satisfied, it must be next
        int count = 0;
        RawStcourse last = null;
        for (final RawStcourse test : tempMap.values()) {
            if (isPrereqSatisfied(test)) {
                ++count;
                last = test;
            }
        }

        int next = order;
        int numToOrder = tempMap.size();
        if (count == 1 && last != null) {
            // Only one has prerequisite satisfied, so make it next

            final int index = paceReg.indexOf(last);
            if (index >= 0) {
                Log.info("orderTwoOrMore setting " + last.course + " to pace order " + next + " for " + last.stuId);
                SiteDataRegistration.updatePaceOrder(cache, last, Integer.valueOf(next));
                paceReg.set(index, last);
            }

            ++next;
            tempMap.remove(last.course);
            --numToOrder;
        }

        // if this leaves only one, no choices remain
        if (numToOrder == 1) {
            final RawStcourse reg = tempMap.values().iterator().next();

            final int index = paceReg.indexOf(last);
            if (index >= 0) {
                SiteDataRegistration.updatePaceOrder(cache, reg, Integer.valueOf(next));
                paceReg.set(index, reg);
            }

            sortPaceOrder(paceReg);
            presentSchedule(cache, logic, htm, allReg, paceReg);
        } else {
            // At this point, there are multiple courses to order, and either zero or more than one
            // have prerequisites satisfied.
            orderN(cache, logic, htm, allReg, paceReg, next, tempMap);
        }
    }

    /**
     * Tests whether an object's prerequisite satisfied field indicates a true value.
     *
     * @param model the model to test
     * @return {@code true} if the prerequisite is satisfied; {@code false} if not
     */
    private static boolean isPrereqSatisfied(final RawStcourse model) {

        final String satis = model.prereqSatis;

        return "Y".equals(satis) || "P".equals(satis);
    }

    /**
     * Gathers and orders all incompletes in the pace registrations list, in order of deadline date. Must be called from
     * within a block synchronized on {@code tempMap}.
     *
     * @param cache      the data cache
     * @param startOrder the next pace order to use in the sequence
     * @param paceReg    the list of registrations that take part in the pace
     * @param tempMap    a map from course to registration
     * @return the next pace order to use in the sequence after this group
     * @throws SQLException if there is an error accessing the database
     */
    private static int orderIncompletes(final Cache cache, final int startOrder,
                                        final List<RawStcourse> paceReg, final Map<String, RawStcourse> tempMap)
            throws SQLException {

        int next = startOrder;

        // TODO: Sort by deadline date first, then by course ID number

        // First, put any incompletes that are now complete in place
        tempMap.clear();
        for (final RawStcourse reg : paceReg) {
            if (reg.iDeadlineDt != null && "Y".equals(reg.completed)) {
                tempMap.put(reg.course, reg);
            }
        }

        next = setOrders(cache, next, tempMap, paceReg);

        // Next, any incompletes that are not yet complete
        tempMap.clear();
        for (final RawStcourse reg : paceReg) {
            if (reg.iDeadlineDt != null && !"Y".equals(reg.completed)) {
                tempMap.put(reg.course, reg);
            }
        }

        return setOrders(cache, next, tempMap, paceReg);
    }

    /**
     * Gathers and orders all completed courses and all courses that are already open. Must be called from within a
     * block synchronized on {@code tempMap}.
     *
     * @param cache      the data cache
     * @param startOrder the next pace order to use in the sequence
     * @param paceReg    the list of registrations that take part in the pace
     * @param tempMap    a map from course to registration
     * @return the next pace order to use in the sequence after this group
     * @throws SQLException if there is an error accessing the database
     */
    private static int orderCompletedOpen(final Cache cache, final int startOrder,
                                          final List<RawStcourse> paceReg, final Map<String, RawStcourse> tempMap)
            throws SQLException {

        int next = startOrder;

        // First, put any non-incompletes that have been completed or marked 'N'
        tempMap.clear();
        for (final RawStcourse reg : paceReg) {
            if (reg.iDeadlineDt == null && ("Y".equals(reg.completed) || "N".equals(reg.openStatus))) {
                tempMap.put(reg.course, reg);
            }
        }

        next = setOrders(cache, next, tempMap, paceReg);

        // Next, any non-incompletes that are opened
        tempMap.clear();
        for (final RawStcourse reg : paceReg) {
            if (reg.iDeadlineDt == null && !"Y".equals(reg.completed) && "Y".equals(reg.openStatus)) {
                tempMap.put(reg.course, reg);
            }
        }

        return setOrders(cache, next, tempMap, paceReg);
    }

    /**
     * Stores those registrations whose pace order has not yet been set in {@code tempMap}. Must be called from within a
     * block synchronized on {@code tempMap}.
     *
     * @param paceReg the list of registrations that take part in the pace
     * @param tempMap a map from course to registration
     */
    private static void findRemaining(final Iterable<RawStcourse> paceReg,
                                      final Map<? super String, ? super RawStcourse> tempMap) {

        tempMap.clear();
        for (final RawStcourse reg : paceReg) {
            if (reg.paceOrder == null) {
                tempMap.put(reg.course, reg);
            }
        }
    }

    /**
     * Assigns pace orders to all records in the {@code tempMap} map, in the order given by the IDs list. Must be called
     * from within a block synchronized on {@code tempMap}.
     *
     * @param cache      the data cache
     * @param startOrder the next pace order to use in the sequence
     * @param tempMap    a map from course to registration
     * @param paceReg    all student registrations contributing toward pace
     * @return the next pace order to use in the sequence after this group
     * @throws SQLException if there is an error accessing the database
     */
    private static int setOrders(final Cache cache, final int startOrder,
                                 final Map<String, RawStcourse> tempMap, final List<RawStcourse> paceReg)
            throws SQLException {

        int next = startOrder;

        for (final RawStcourse stcourse : tempMap.values()) {
            Log.info("Setting ", stcourse.course, " to order ", Integer.toString(next));
            updatePaceOrder(cache, stcourse, Integer.valueOf(next), paceReg);
            ++next;
        }

        return next;
    }

    /**
     * Updates the pace order of a record and replaces the existing record with the updated record in a list of
     * registrations.
     *
     * @param cache    the data cache
     * @param reg      the registration to update
     * @param newOrder the new pace order
     * @param paceRegs the list to update with the newly updated record
     * @throws SQLException if there is an error accessing the database
     */
    private static void updatePaceOrder(final Cache cache, final RawStcourse reg,
                                        final Integer newOrder, final List<? super RawStcourse> paceRegs) throws SQLException {

        if (reg != null) {
            final int index = paceRegs.indexOf(reg);
            if (index >= 0) {
                Log.info("presentCourseOrderPage setting " + reg.course + " pace order to " + newOrder + " for "
                        + reg.stuId);
                SiteDataRegistration.updatePaceOrder(cache, reg, newOrder);
                paceRegs.set(index, reg);
            }
        }
    }

    /**
     * Generates a page that allows a user to place the set of courses which contribute towards their pace in order when
     * there are two or more courses whose order is not determined and there is not just one with prerequisites
     * satisfied. On input, {@code tempMap} holds the list of courses which remain to be ordered.
     *
     * @param cache   the data cache
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @param allReg  all student registrations (includes those not in pace)
     * @param paceReg the list of courses that will contribute toward pace
     * @param order   the next order to use in the sequence
     * @param tempMap a map from course to registration
     * @throws SQLException if there is an error accessing the database
     */
    private static void orderN(final Cache cache, final CourseSiteLogic logic, final HtmlBuilder htm,
                               final Collection<RawStcourse> allReg, final List<RawStcourse> paceReg,
                               final int order, final Map<String, RawStcourse> tempMap) throws SQLException {

        // FIXME: This is very specific to the courses listed in IDS
        boolean has117 = false;
        boolean has118 = false;
        boolean has124 = false;
        boolean has125 = false;
        boolean has126 = false;
        boolean has124Pre = false;
        boolean has125Pre = false;
        boolean has126Pre = false;

        // See which courses are present and which have prerequisites met
        for (final Map.Entry<String, RawStcourse> entry : tempMap.entrySet()) {
            final String key = entry.getKey();

            if (RawRecordConstants.M117.equals(key)) {
                has117 = true;
            } else if (RawRecordConstants.M118.equals(key)) {
                has118 = true;
            } else if (RawRecordConstants.M124.equals(key)) {
                has124 = true;
                has124Pre = isPrereqSatisfied(entry.getValue());
            } else if (RawRecordConstants.M125.equals(key)) {
                has125 = true;
                has125Pre = isPrereqSatisfied(entry.getValue());
            } else if (RawRecordConstants.M126.equals(key)) {
                has126 = true;
                has126Pre = isPrereqSatisfied(entry.getValue());
            }
        }

        // Assume all courses with a known pace order will be completed first, which could satisfy
        // some prerequisites of those that remain
        for (final RawStcourse reg : allReg) {
            if (reg.paceOrder == null) {
                continue;
            }
            final String courseId = reg.course;
            if (RawRecordConstants.M118.equals(courseId)) {
                if (has124) {
                    has124Pre = true;
                }
                if (has125) {
                    has125Pre = true;
                }
            } else if (RawRecordConstants.M125.equals(courseId) && has126) {
                has126Pre = true;
            }
        }

        // At this point, at least one prerequisite must be set by prior coursework

        // FIXME: The following is shockingly ugly. Don't look, or shower immediately after.

        if (has117) {
            if (has118) {
                if (has124) {
                    if (has125) {
                        if (has126) {
                            // Has 117, 118, 124, 125, 126
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M126));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M124));
                        } else {
                            // Has 117, 118, 124, 125
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M125));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M124));
                        }
                        endChoosePage(htm);
                    } else if (has126) {
                        // Has 117, 118, 124, 126
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M117),
                                tempMap.get(RawRecordConstants.M118),
                                tempMap.get(RawRecordConstants.M124),
                                tempMap.get(RawRecordConstants.M126));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M117),
                                tempMap.get(RawRecordConstants.M118),
                                tempMap.get(RawRecordConstants.M126),
                                tempMap.get(RawRecordConstants.M124));
                        endChoosePage(htm);
                    } else // Has 117, 118, 124
                        if (has124Pre) {
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M124));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M118));
                            endChoosePage(htm);
                        } else {
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                                    Integer.valueOf(order), paceReg);
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M118),
                                    Integer.valueOf(order + 1), paceReg);
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M124),
                                    Integer.valueOf(order + 2), paceReg);
                            presentSchedule(cache, logic, htm, allReg, paceReg);
                        }
                } else if (has125) {
                    if (has126) {
                        // Has 117, 118, 125, 126
                        if (has125Pre) {
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M126));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M118));
                            endChoosePage(htm);
                        } else {
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                                    Integer.valueOf(order), paceReg);
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M118),
                                    Integer.valueOf(order + 1), paceReg);
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M125),
                                    Integer.valueOf(order + 2), paceReg);
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                                    Integer.valueOf(order + 3), paceReg);
                            presentSchedule(cache, logic, htm, allReg, paceReg);
                        }
                    } else // Has 117, 118, 125
                        if (has125Pre) {
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M125));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M118));
                            endChoosePage(htm);
                        } else {
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                                    Integer.valueOf(order), paceReg);
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M118),
                                    Integer.valueOf(order + 1), paceReg);
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M125),
                                    Integer.valueOf(order + 2), paceReg);
                            presentSchedule(cache, logic, htm, allReg, paceReg);
                        }
                } else if (has126) {
                    // Has 117, 118, 126
                    if (has126Pre) {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M117),
                                tempMap.get(RawRecordConstants.M118),
                                tempMap.get(RawRecordConstants.M126));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M126),
                                tempMap.get(RawRecordConstants.M117),
                                tempMap.get(RawRecordConstants.M118));
                        endChoosePage(htm);
                    } else {
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M118),
                                Integer.valueOf(order + 1), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                                Integer.valueOf(order + 2), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
                } else {
                    // Has 117, 118
                    updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                            Integer.valueOf(order), paceReg);
                    updatePaceOrder(cache, tempMap.get(RawRecordConstants.M118),
                            Integer.valueOf(order + 1), paceReg);
                    presentSchedule(cache, logic, htm, allReg, paceReg);
                }
            } else if (has124) {
                if (has125) {
                    if (has126) {
                        // Has 117, 124, 125, 126
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M117),
                                tempMap.get(RawRecordConstants.M124),
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M117),
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126),
                                tempMap.get(RawRecordConstants.M124));
                        emitForm(logic, htm, "3", order,
                                tempMap.get(RawRecordConstants.M117),
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M124),
                                tempMap.get(RawRecordConstants.M126));
                        endChoosePage(htm);
                    } else // Has 117, 124, 125
                        if (has125Pre) {
                            if (has124Pre) {
                                // Must have all three prerequisites
                                startChoosePage(logic, htm, paceReg, order);
                                emitForm(logic, htm, "1", order,
                                        tempMap.get(RawRecordConstants.M117),
                                        tempMap.get(RawRecordConstants.M124),
                                        tempMap.get(RawRecordConstants.M125));
                                emitForm(logic, htm, "2", order,
                                        tempMap.get(RawRecordConstants.M117),
                                        tempMap.get(RawRecordConstants.M125),
                                        tempMap.get(RawRecordConstants.M124));
                                emitForm(logic, htm, "3", order,
                                        tempMap.get(RawRecordConstants.M124),
                                        tempMap.get(RawRecordConstants.M117),
                                        tempMap.get(RawRecordConstants.M125));
                                emitForm(logic, htm, "4", order,
                                        tempMap.get(RawRecordConstants.M125),
                                        tempMap.get(RawRecordConstants.M117),
                                        tempMap.get(RawRecordConstants.M124));
                                emitForm(logic, htm, "5", order,
                                        tempMap.get(RawRecordConstants.M124),
                                        tempMap.get(RawRecordConstants.M125),
                                        tempMap.get(RawRecordConstants.M117));
                                emitForm(logic, htm, "6", order,
                                        tempMap.get(RawRecordConstants.M125),
                                        tempMap.get(RawRecordConstants.M124),
                                        tempMap.get(RawRecordConstants.M117));
                            } else {
                                // Has prerequisites for 117 and 125 but not 124
                                startChoosePage(logic, htm, paceReg, order);
                                emitForm(logic, htm, "1", order,
                                        tempMap.get(RawRecordConstants.M117),
                                        tempMap.get(RawRecordConstants.M124),
                                        tempMap.get(RawRecordConstants.M125));
                                emitForm(logic, htm, "2", order,
                                        tempMap.get(RawRecordConstants.M117),
                                        tempMap.get(RawRecordConstants.M125),
                                        tempMap.get(RawRecordConstants.M124));
                            }
                            endChoosePage(htm);
                        } else {
                            // Must have 117 and 124 prerequisites, but not 125
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                                    Integer.valueOf(order), paceReg);
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M124),
                                    Integer.valueOf(order + 1), paceReg);
                            updatePaceOrder(cache, tempMap.get(RawRecordConstants.M125),
                                    Integer.valueOf(order + 2), paceReg);
                            presentSchedule(cache, logic, htm, allReg, paceReg);
                        }
                } else if (has126) {
                    // Has 117, 124, 126
                    if (has126Pre) {
                        if (has124Pre) {
                            // Must have all three prerequisites
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M126));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M124));
                            emitForm(logic, htm, "3", order,
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M126));
                            emitForm(logic, htm, "4", order,
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M124));
                            emitForm(logic, htm, "5", order,
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M117));
                            emitForm(logic, htm, "6", order,
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M117));
                        } else {
                            // Has prerequisites for 117 and 126 but not 124
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M126));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M117),
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M124));
                        }
                        endChoosePage(htm);
                    } else {
                        // Must have 117 and 124 prerequisites, but not 126
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M124),
                                Integer.valueOf(order + 1), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                                Integer.valueOf(order + 2), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
                } else // Has 117, 124
                    if (has124Pre) {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M117), tempMap.get(RawRecordConstants.M124));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M124), tempMap.get(RawRecordConstants.M117));
                        endChoosePage(htm);
                    } else {
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M124),
                                Integer.valueOf(order + 1), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
            } else if (has125) {
                if (has126) {
                    // Has 117, 125, 126
                    if (has126Pre || has125Pre) {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M117),
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126),
                                tempMap.get(RawRecordConstants.M117));
                        endChoosePage(htm);
                    } else {
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M125),
                                Integer.valueOf(order + 1), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                                Integer.valueOf(order + 2), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
                } else // Has 117, 125
                    if (has125Pre) {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M117), tempMap.get(RawRecordConstants.M125));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M125), tempMap.get(RawRecordConstants.M117));
                        endChoosePage(htm);
                    } else {
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M125),
                                Integer.valueOf(order + 1), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
            } else if (has126) {
                // Has 117, 126
                if (has126Pre) {
                    startChoosePage(logic, htm, paceReg, order);
                    emitForm(logic, htm, "1", order,
                            tempMap.get(RawRecordConstants.M117), tempMap.get(RawRecordConstants.M126));
                    emitForm(logic, htm, "2", order,
                            tempMap.get(RawRecordConstants.M126), tempMap.get(RawRecordConstants.M117));
                    endChoosePage(htm);
                } else {
                    updatePaceOrder(cache, tempMap.get(RawRecordConstants.M117),
                            Integer.valueOf(order), paceReg);
                    updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                            Integer.valueOf(order + 1), paceReg);
                    presentSchedule(cache, logic, htm, allReg, paceReg);
                }
            } else {
                Log.warning("Error: Only M 117 found in OrderN");
                presentSchedule(cache, logic, htm, allReg, paceReg);
            }
        } else if (has118) {
            if (has124) {
                if (has125) {
                    if (has126) {
                        // Has 118, 124, 125, 126
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M118),
                                tempMap.get(RawRecordConstants.M124),
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M118),
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126),
                                tempMap.get(RawRecordConstants.M124));
                        emitForm(logic, htm, "3", order,
                                tempMap.get(RawRecordConstants.M118),
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M124),
                                tempMap.get(RawRecordConstants.M126));
                    } else // Has 118, 124, 125
                        if (has125Pre && has124Pre) {
                            // Must have all three prerequisites
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M125));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M124));
                            emitForm(logic, htm, "3", order,
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M125));
                            emitForm(logic, htm, "4", order,
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M124));
                            emitForm(logic, htm, "5", order,
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M118));
                            emitForm(logic, htm, "6", order,
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M118));
                        } else {
                            // Must have 118 only
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M125));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M125),
                                    tempMap.get(RawRecordConstants.M124));
                        }
                    endChoosePage(htm);
                } else if (has126) {
                    // Has 118, 124, 126
                    if (has126Pre) {
                        if (has124Pre) {
                            // Must have all three prerequisites
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M126));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M124));
                            emitForm(logic, htm, "3", order,
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M126));
                            emitForm(logic, htm, "4", order,
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M124));
                            emitForm(logic, htm, "5", order,
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M118));
                            emitForm(logic, htm, "6", order,
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M118));
                        } else {
                            // Has prerequisites for 118 and 126 but not 124
                            startChoosePage(logic, htm, paceReg, order);
                            emitForm(logic, htm, "1", order,
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M124),
                                    tempMap.get(RawRecordConstants.M126));
                            emitForm(logic, htm, "2", order,
                                    tempMap.get(RawRecordConstants.M118),
                                    tempMap.get(RawRecordConstants.M126),
                                    tempMap.get(RawRecordConstants.M124));
                        }
                        endChoosePage(htm);
                    } else {
                        // Must have 118 and 124 prerequisites, but not 126
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M118),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M124),
                                Integer.valueOf(order + 1), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                                Integer.valueOf(order + 2), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
                } else // Has 118, 124
                    if (has124Pre) {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M118), tempMap.get(RawRecordConstants.M124));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M124), tempMap.get(RawRecordConstants.M118));
                        endChoosePage(htm);
                    } else {
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M118),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M124),
                                Integer.valueOf(order + 1), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
            } else if (has125) {
                if (has126) {
                    // Has 118, 125, 126
                    if (has126Pre || has125Pre) {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M118),
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126),
                                tempMap.get(RawRecordConstants.M118));
                        emitForm(logic, htm, "3", order,
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M118),
                                tempMap.get(RawRecordConstants.M126));
                        endChoosePage(htm);
                    } else {
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M118),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M125),
                                Integer.valueOf(order + 1), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                                Integer.valueOf(order + 2), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
                } else // Has 118, 125
                    if (has125Pre) {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M118), tempMap.get(RawRecordConstants.M125));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M125), tempMap.get(RawRecordConstants.M118));
                        endChoosePage(htm);
                    } else {
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M118),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M125),
                                Integer.valueOf(order + 1), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
            } else if (has126) {
                // Has 118, 126
                if (has126Pre) {
                    startChoosePage(logic, htm, paceReg, order);
                    emitForm(logic, htm, "1", order,
                            tempMap.get(RawRecordConstants.M118), tempMap.get(RawRecordConstants.M126));
                    emitForm(logic, htm, "2", order,
                            tempMap.get(RawRecordConstants.M126), tempMap.get(RawRecordConstants.M118));
                    endChoosePage(htm);
                } else {
                    updatePaceOrder(cache, tempMap.get(RawRecordConstants.M118),
                            Integer.valueOf(order), paceReg);
                    updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                            Integer.valueOf(order + 1), paceReg);
                    presentSchedule(cache, logic, htm, allReg, paceReg);
                }
            } else {
                Log.warning("Error: Only M 118 found in OrderN");
                presentSchedule(cache, logic, htm, allReg, paceReg);
            }
        } else if (has124) {
            if (has125) {
                if (has126) {
                    // Has 124, 125, 126
                    if (has124Pre && has125Pre) {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M124),
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126),
                                tempMap.get(RawRecordConstants.M124));
                        emitForm(logic, htm, "3", order,
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M124),
                                tempMap.get(RawRecordConstants.M126));
                    } else {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M126),
                                tempMap.get(RawRecordConstants.M124));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M125),
                                tempMap.get(RawRecordConstants.M124),
                                tempMap.get(RawRecordConstants.M126));
                    }
                    endChoosePage(htm);
                } else // Has 124, 125
                    if (has124Pre && has125Pre) {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M124), tempMap.get(RawRecordConstants.M125));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M125), tempMap.get(RawRecordConstants.M124));
                        endChoosePage(htm);
                    } else {
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M124),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M125),
                                Integer.valueOf(order + 1), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
            } else if (has126) {
                // Has 124, 126
                if (has126Pre) {
                    if (has124Pre) {
                        startChoosePage(logic, htm, paceReg, order);
                        emitForm(logic, htm, "1", order,
                                tempMap.get(RawRecordConstants.M124),
                                tempMap.get(RawRecordConstants.M126));
                        emitForm(logic, htm, "2", order,
                                tempMap.get(RawRecordConstants.M126),
                                tempMap.get(RawRecordConstants.M124));
                        endChoosePage(htm);
                    } else {
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                                Integer.valueOf(order), paceReg);
                        updatePaceOrder(cache, tempMap.get(RawRecordConstants.M124),
                                Integer.valueOf(order + 1), paceReg);
                        presentSchedule(cache, logic, htm, allReg, paceReg);
                    }
                } else {
                    updatePaceOrder(cache, tempMap.get(RawRecordConstants.M124),
                            Integer.valueOf(order), paceReg);
                    updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                            Integer.valueOf(order + 1), paceReg);
                    presentSchedule(cache, logic, htm, allReg, paceReg);
                }
            } else {
                Log.warning("Error: Only M 124 found in OrderN");
                presentSchedule(cache, logic, htm, allReg, paceReg);
            }
        } else {
            if (has125) {
                if (has126) {
                    // Has 125, 126
                    updatePaceOrder(cache, tempMap.get(RawRecordConstants.M125),
                            Integer.valueOf(order), paceReg);
                    updatePaceOrder(cache, tempMap.get(RawRecordConstants.M126),
                            Integer.valueOf(order + 1), paceReg);
                } else {
                    Log.warning("Error: Only M 125 found in OrderN");
                }
            } else if (has126) {
                Log.warning("Error: Only M 126 found in OrderN");
            } else {
                Log.warning("Error No Precalculus courses found in OrderN");
            }
            presentSchedule(cache, logic, htm, allReg, paceReg);
        }
    }

    /**
     * Emits the start of the page to choose from a set of options for course order.
     *
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param paceReg the list of registrations that contribute to pace
     * @param order   the first unassigned course order
     */
    private static void startChoosePage(final CourseSiteLogic logic, final HtmlBuilder htm,
                                        final Iterable<RawStcourse> paceReg, final int order) {

        htm.addln("<h3 class='bar'>Choose the Ordering of your Courses</h3>");

        htm.addln("<div class='indent22'>");

        // At this point, 'order' is the first unoccupied index
        if (order > 1) {
            htm.addln("<p>");
            htm.addln(" <strong>You must complete these courses first:</strong>");
            emitKnownCourses(logic, htm, paceReg);
            htm.addln("</p>");
        }

        // Display options that the student may choose from
        htm.addln("<p>");
        htm.addln(" <strong>Please choose from the following options for the order in which ",
                "you would like to complete your ", order > 1 ? "remaining " : CoreConstants.EMPTY,
                "courses:</strong>");
        htm.addln("</p>");
    }

    /**
     * Emits the end of the page to choose from a set of options for course order.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endChoosePage(final HtmlBuilder htm) {

        htm.addln("</div>");
    }

    /**
     * Emits the list of courses with known pace orders.
     *
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param paceReg the list of registrations that contribute to pace
     * @return the first unassigned course order
     */
    private static int emitKnownCourses(final CourseSiteLogic logic, final HtmlBuilder htm,
                                        final Iterable<RawStcourse> paceReg) {

        final SiteDataCourse courseData = logic.data.courseData;

        int next = 1;
        for (; ; ) {
            boolean found = false;
            for (final RawStcourse reg : paceReg) {
                final Integer curOrder = reg.paceOrder;
                if (curOrder != null && curOrder.intValue() == next) {

                    htm.addln("<li class='boxlist'>");
                    htm.add(" Course ", curOrder, ": ");

                    final String courseId = reg.course;

                    final SiteDataCfgCourse cfgCourse = courseData.getCourse(courseId, reg.sect);

                    if (cfgCourse == null) {
                        htm.addln("(not configured properly)", "<br/>");
                    } else {
                        final RawCourse course = cfgCourse.course;
                        if (course == null) {
                            htm.addln("(not configured properly)", "<br/>");
                        } else {
                            htm.addln(courseId, ", ", course.courseName, "<br/>");
                        }
                    }
                    htm.addln("</li>");

                    found = true;
                    ++next;
                    break;
                }
            }
            if (!found) {
                break;
            }
        }

        return next;
    }

    /**
     * Emits a form to choose a particular course ordering.
     *
     * @param logic     the site logic
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param optionNum the option number
     * @param start     the starting order number
     * @param regs      the list of course registrations
     */
    private static void emitForm(final CourseSiteLogic logic, final HtmlBuilder htm,
                                 final String optionNum, final int start, final RawStcourse... regs) {

        htm.addln("<p>");
        htm.addln("<strong>Option ", optionNum, ":</strong>");
        htm.addln("<ul class='boxlist'>");
        emitList(logic, htm, start, regs);
        htm.addln("</ul>");

        htm.addln("<div class='indent22'>");
        htm.addln("<form method='post' action='set_course_schedule.html'>");
        final int numRegs = regs.length;
        for (int i = 0; i < numRegs; ++i) {
            htm.add("<input type='hidden' name='order", Integer.toString(start + i), "' value='", regs[i].course,
                    "'/>");
        }
        htm.addln("<input type='submit' value='Choose Option ", optionNum, "'/>");
        htm.addln("</form>");
        htm.addln("</div>");

        htm.addln("</p>");
    }

    /**
     * Emits the list of courses without pace order assigned.
     *
     * @param logic the site logic
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param next  the first unassigned course order
     * @param regs  the list of registrations to emit (in the order provided)
     */
    private static void emitList(final CourseSiteLogic logic, final HtmlBuilder htm, final int next,
                                 final RawStcourse... regs) {

        final SiteDataCourse courseData = logic.data.courseData;
        int curOrder = next;

        for (final RawStcourse reg : regs) {
            htm.addln("<li class='boxlist'>");
            htm.add(" Course ", Integer.toString(curOrder), ": ");

            final String courseId = reg.course;

            final SiteDataCfgCourse cfgCourse = courseData.getCourse(courseId, reg.sect);

            if (cfgCourse == null) {
                htm.addln("(not configured properly)", "<br/>");
            } else {
                final RawCourse course = cfgCourse.course;
                if (course == null) {
                    htm.addln("(not configured properly)", "<br/>");
                } else {
                    htm.addln(courseId, ", ", course.courseName, "<br/>");
                }
            }
            htm.addln("</li>");
            curOrder++;
        }
    }

    /**
     * Processes a form submission to set the course order.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @param logic the site logic
     * @throws IOException  if there is an error processing the request
     * @throws SQLException if there is an error accessing the database
     */
    static void doSetCourseOrder(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                                 final CourseSiteLogic logic) throws IOException, SQLException {

        for (int i = 0; i < 10; ++i) {
            final String whichCourse = req.getParameter("order" + i);

            if (whichCourse != null) {
                final List<RawStcourse> allReg = logic.data.registrationData.getRegistrations();

                final int size = allReg.size();
                for (int j = 0; j < size; ++j) {
                    final RawStcourse reg = allReg.get(j);

                    if (whichCourse.equals(reg.course)) {
                        Log.info("Setting " + reg.course + " to pace order " + i + " for "
                                + reg.stuId + " based on schedule page form submission");
                        SiteDataRegistration.updatePaceOrder(cache, reg, Integer.valueOf(i));
                        allReg.set(j, reg);
                        break;
                    }
                }
            }
        }

        resp.sendRedirect("schedule.html");
    }
}
