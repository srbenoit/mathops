package dev.mathops.web.host.testing.adminsys.genadmin.logic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdmSubtopic;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdminTopic;
import dev.mathops.web.host.testing.adminsys.genadmin.GenAdminPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A page to test logic related to prerequisites.
 */
public enum PageLogicPrerequisites {
    ;

    /**
     * Generates the logic testing page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = startPage(cache, site, session);

        endPage(cache, site, req, resp, htm);
    }

    /**
     * Generates the logic testing page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = startPage(cache, site, session);

        final String stuId = req.getParameter("stu_id");
        if (stuId != null && !stuId.isEmpty()) {
            htm.hr().div("vgap");

            if ("ALL".equals(stuId)) {
                scanAllStudents(cache, htm);
            } else {
                scanOneStudent(cache, htm, stuId);
            }
        }

        endPage(cache, site, req, resp, htm);
    }

    /**
     * Performs a scan of all students who have a registration in the active term, printing summary statistics and all
     * warnings encountered.  This will skip students whose only registrations in the active term have been dropped.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void scanAllStudents(final Cache cache, final HtmlBuilder htm) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();
        if (active == null) {
            htm.sH(4).add("Unable to query the active term.").eH(4);
        } else {
            // Compile a list of IDs for all students who have active registrations this term
            final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, active.term, true, false);
            final int numTotalRegs = allRegs.size();
            final Set<String> studentIds = new HashSet<>(numTotalRegs * 2 / 3);
            for (final RawStcourse stc : allRegs) {
                studentIds.add(stc.stuId);
            }

            htm.addln("<ul>");
            int numIssues = 0;

            // Scan those students, emit warnings as encountered, and gather summary statistics
            for (final String stuId : studentIds) {

                boolean okFor117 = false;
                boolean provisionalFor117 = false;
                boolean okFor118 = false;
                boolean okFor124 = false;
                boolean okFor125 = false;
                boolean okFor126 = false;

                final List<RawStcourse> completed = RawStcourseLogic.getAllPriorCompleted(cache, stuId);
                for (final RawStcourse row : completed) {
                    if ("M 117".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                    } else if ("M 118".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                        okFor124 = true;
                        okFor125 = true;
                    } else if ("M 124".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                        okFor124 = true;
                    } else if ("M 125".equals(row.course) || "M 126".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                        okFor125 = true;
                        okFor126 = true;
                    } else if ("M 120".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                        okFor124 = true;
                        okFor125 = true;
                    } else if ("M 127".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                        okFor124 = true;
                        okFor125 = true;
                        okFor126 = true;
                    }
                }

                final List<RawFfrTrns> transfers = RawFfrTrnsLogic.queryByStudent(cache, stuId);
                for (final RawFfrTrns row : transfers) {
                    if ("M 002".equals(row.course) || "M 055".equals(row.course) || "M 093".equals(row.course)
                        || "M 099".equals(row.course)) {
                        okFor117 = true;
                    } else if ("M 117".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                    } else if ("M 118".equals(row.course) || "M 124".equals(row.course) || "M 120".equals(row.course)
                               || "M 141".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                        okFor124 = true;
                        okFor125 = true;
                    } else if ("M 125".equals(row.course) || "M 126".equals(row.course) || "M 127".equals(row.course)
                               || "M 155".equals(row.course) || "M 156".equals(row.course) || "M 157".equals(row.course)
                               || "M 159".equals(row.course) || "M 160".equals(row.course)
                               || "M 161".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                        okFor124 = true;
                        okFor125 = true;
                        okFor126 = true;
                    }
                }

                final List<RawMpeCredit> mpe = RawMpeCreditLogic.queryByStudent(cache, stuId);
                for (final RawMpeCredit row : mpe) {
                    if ("M 100A".equals(row.course) || "M 100C".equals(row.course)) {
                        okFor117 = true;
                    } else if ("M 117".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                    } else if ("M 118".equals(row.course) || "M 124".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                        okFor124 = true;
                        okFor125 = true;
                    } else if ("M 125".equals(row.course) || "M 126".equals(row.course)) {
                        okFor117 = true;
                        okFor118 = true;
                        okFor124 = true;
                        okFor125 = true;
                        okFor126 = true;
                    }
                }

                final List<RawStcourse> history = RawStcourseLogic.getHistory(cache, stuId);

                for (final RawStcourse row : history) {
                    if ("Y".equals(row.prereqSatis)) {
                        if ("M 117".equals(row.course) && !okFor117) {
                            okFor117 = true;
                        } else if ("M 118".equals(row.course) && !okFor118) {
                            okFor118 = true;
                        } else if ("M 124".equals(row.course) && !okFor124) {
                            okFor124 = true;
                        } else if ("M 125".equals(row.course) && !okFor125) {
                            okFor125 = true;
                        } else if ("M 126".equals(row.course) && !okFor126) {
                            okFor126 = true;
                        }
                    }
                }

                if (!okFor117) {
                    for (final RawStcourse row : history) {
                        if (row.termKey.equals(active.term)) {
                            if (RawRecordConstants.M117.equals(row.course)
                                && ("801".equals(row.sect) || "809".equals(row.sect))
                                && ("Y".equals(row.openStatus) || row.openStatus == null)) {
                                provisionalFor117 = true;
                                break;
                            }
                        }
                    }
                }

                final PrerequisiteLogic prereq = new PrerequisiteLogic(cache, stuId);

                if (prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M117)) {
                    if (!okFor117 && !provisionalFor117) {
                        htm.add("<li style='color:red'>*** WARNING: Logic says student ", stuId,
                                " eligible for MATH 117");
                    }
                } else if (okFor117) {
                    htm.add("<li style='color:red'>*** WARNING: Logic says student ", stuId,
                            " not eligible for MATH 117");
                }

                if (prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M118)) {
                    if (!okFor118) {
                        htm.add("<li style='color:red'>*** WARNING: Logic says student ", stuId,
                                " eligible for MATH 118");
                    }
                } else if (okFor118) {
                    htm.add("<li style='color:red'>*** WARNING: Logic says student ", stuId,
                            " not eligible for MATH 118");
                }

                if (prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M124)) {
                    if (!okFor124) {
                        htm.add("<li style='color:red'>*** WARNING: Logic says student ", stuId,
                                " eligible for MATH 124");
                    }
                } else if (okFor124) {
                    htm.add("<li style='color:red'>*** WARNING: Logic says student ", stuId,
                            " not eligible for MATH 124");
                }

                if (prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M125)) {
                    if (!okFor125) {
                        htm.add("<li style='color:red'>*** WARNING: Logic says student ", stuId,
                                " eligible for MATH 125");
                    }
                } else if (okFor125) {
                    htm.add("<li style='color:red'>*** WARNING: Logic says student ", stuId,
                            " not eligible for MATH 125");
                }

                if (prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M126)) {
                    if (!okFor126) {
                        htm.add("<li style='color:red'>*** WARNING: Logic says student ", stuId,
                                " eligible for MATH 126");
                    }
                } else if (okFor126) {
                    htm.add("<li style='color:red'>*** WARNING: Logic says student ", stuId,
                            " not eligible for MATH 126");
                }

                final List<RawStcourse> current = RawStcourseLogic.getPaced(cache, stuId);
                for (final RawStcourse row : current) {

                    if (RawRecordConstants.M117.equals(row.course) || RawRecordConstants.MATH117.equals(row.course)) {
                        if (okFor117) {
                            if (!"Y".equals(row.prereqSatis)) {
                                htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " for student ", stuId,
                                        " (prereq_satis was ", row.prereqSatis, " , should be 'Y') - fixed</li>");
                                RawStcourseLogic.updatePrereqSatisfied(cache, row.stuId, row.course, row.sect,
                                        row.termKey, "Y");
                                ++numIssues;
                            }
                        } else if ("Y".equals(row.prereqSatis)) {
                            htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " for student ", stuId,
                                    " (prereq_satis was 'Y', should be null) - not changed</li>");
//                            RawStcourseLogic.updatePrereqSatisfied(cache, row.stuId, row.course, row.sect,
//                                    row.termKey, null);
                            ++numIssues;
                        }
                    } else if (RawRecordConstants.M118.equals(row.course)
                               || RawRecordConstants.MATH118.equals(row.course)) {
                        if (okFor118) {
                            if (!"Y".equals(row.prereqSatis)) {
                                htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " for student ", stuId,
                                        " (prereq_satis was ", row.prereqSatis, ", should be 'Y') - fixed</li>");
                                RawStcourseLogic.updatePrereqSatisfied(cache, row.stuId, row.course, row.sect,
                                        row.termKey, "Y");
                                ++numIssues;
                            }
                        } else if ("Y".equals(row.prereqSatis)) {
                            htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " for student ", stuId,
                                    " (prereq_satis was 'Y', should be null) - not changed</li>");
//                            RawStcourseLogic.updatePrereqSatisfied(cache, row.stuId, row.course, row.sect,
//                                    row.termKey, null);
                            ++numIssues;
                        }
                    } else if (RawRecordConstants.M124.equals(row.course)
                               || RawRecordConstants.MATH124.equals(row.course)) {
                        if (okFor124) {
                            if (!"Y".equals(row.prereqSatis)) {
                                htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " for student ", stuId,
                                        " (prereq_satis was ", row.prereqSatis, " should be 'Y') - fixed</li>");
                                RawStcourseLogic.updatePrereqSatisfied(cache, row.stuId, row.course, row.sect,
                                        row.termKey, "Y");
                                ++numIssues;
                            }
                        } else if ("Y".equals(row.prereqSatis)) {
                            htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " for student ", stuId,
                                    " (prereq_satis was 'Y', should be null) - not changed</li>");
//                            RawStcourseLogic.updatePrereqSatisfied(cache, row.stuId, row.course, row.sect,
//                                    row.termKey, null);
                            ++numIssues;
                        }
                    } else if (RawRecordConstants.M125.equals(row.course)
                               || RawRecordConstants.MATH125.equals(row.course)) {
                        if (okFor125) {
                            if (!"Y".equals(row.prereqSatis)) {
                                htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " for student ", stuId,
                                        " (prereq_satis was ", row.prereqSatis, ", should be 'Y') - fixed</li>");
                                RawStcourseLogic.updatePrereqSatisfied(cache, row.stuId, row.course, row.sect,
                                        row.termKey, "Y");
                                ++numIssues;
                            }
                        } else if ("Y".equals(row.prereqSatis)) {
                            htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " for student ", stuId,
                                    " (prereq_satis was 'Y', should be null) - not changed</li>");
//                            RawStcourseLogic.updatePrereqSatisfied(cache, row.stuId, row.course, row.sect,
//                                    row.termKey, null);
                            ++numIssues;
                        }
                    } else if (RawRecordConstants.M126.equals(row.course)
                               || RawRecordConstants.MATH126.equals(row.course)) {
                        if (okFor126) {
                            if (!"Y".equals(row.prereqSatis)) {
                                htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " for student ", stuId,
                                        " (prereq_satis was ", row.prereqSatis, ", should be 'Y') - fixed</li>");
                                RawStcourseLogic.updatePrereqSatisfied(cache, row.stuId, row.course, row.sect,
                                        row.termKey, "Y");
                                ++numIssues;
                            }
                        } else if ("Y".equals(row.prereqSatis)) {
                            htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " for student ", stuId,
                                    " (prereq_satis was 'Y', should be null) - not changed</li>");
//                            RawStcourseLogic.updatePrereqSatisfied(cache, row.stuId, row.course, row.sect,
//                                    row.termKey, null);
                            ++numIssues;
                        }
                    }
                }

                // Try not to make this method blast the production server's load up to 100%
                try {
                    Thread.sleep(5L);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            htm.addln("</ul>");

            final int numRegs = allRegs.size();
            final int numStus = studentIds.size();
            final String numRegsStr = Integer.toString(numRegs);
            final String numStusStr = Integer.toString(numStus);

            htm.sP().add("Scanned ", numRegsStr, " registrations for ", numStusStr, " students.");

            if (numIssues == 0) {
                htm.sP().add("No prerequisite issues found");
            } else {
                final String numIssuesStr = Integer.toString(numIssues);
                htm.sP().add("Found ", numIssuesStr, " prerequisite issues.");
            }
        }
    }

    /**
     * Performs a scan of a single student, printing that student's registration status, calculated pace and pace track,
     * and information from STTERM, if found.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param stuId the student ID
     * @throws SQLException if there is an error accessing the database
     */
    private static void scanOneStudent(final Cache cache, final HtmlBuilder htm, final String stuId) throws SQLException {

        final RawStudent student = RawStudentLogic.query(cache, stuId, false);
        if (student == null) {
            htm.sH(4).add("Student ", stuId, " not found in database.").eH(4);
        } else {
            final String screenName = student.getScreenName();

            boolean okFor117 = false;
            boolean okFor118 = false;
            boolean okFor124 = false;
            boolean okFor125 = false;
            boolean okFor126 = false;

            htm.sH(4).add("Work record for student ", stuId, " (", screenName, ")").eH(4);

            htm.addln("<ul>");

            final List<RawStcourse> completed = RawStcourseLogic.getAllPriorCompleted(cache, stuId);
            for (final RawStcourse row : completed) {
                htm.addln("<li>Completed ", row.course, " in ", row.termKey.longString, " with grade of ",
                        row.courseGrade, "</li>");
                if ("M 117".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                } else if ("M 118".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor124 = true;
                    okFor125 = true;
                } else if ("M 124".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor124 = true;
                } else if ("M 125".equals(row.course) || "M 126".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor125 = true;
                    okFor126 = true;
                } else if ("M 120".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor124 = true;
                    okFor125 = true;
                } else if ("M 127".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor124 = true;
                    okFor125 = true;
                    okFor126 = true;
                }
            }

            final List<RawFfrTrns> transfers = RawFfrTrnsLogic.queryByStudent(cache, stuId);
            for (final RawFfrTrns row : transfers) {
                htm.addln("<li>Transfer credit for ", row.course, "</li>");
                if ("M 002".equals(row.course) || "M 055".equals(row.course) || "M 093".equals(row.course)
                    || "M 099".equals(row.course)) {
                    okFor117 = true;
                } else if ("M 117".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                } else if ("M 118".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor124 = true;
                    okFor125 = true;
                } else if ("M 124".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor124 = true;
                } else if ("M 125".equals(row.course) || "M 126".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor125 = true;
                    okFor126 = true;
                } else if ("M 120".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor124 = true;
                    okFor125 = true;
                } else if ("M 127".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor124 = true;
                    okFor125 = true;
                    okFor126 = true;
                }
            }

            final List<RawMpeCredit> mpe = RawMpeCreditLogic.queryByStudent(cache, stuId);
            for (final RawMpeCredit row : mpe) {
                htm.addln("<li>Placement credit for ", row.course, "</li>");
                if ("M 100A".equals(row.course) || "M 100C".equals(row.course)) {
                    okFor117 = true;
                } else if ("M 117".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                } else if ("M 118".equals(row.course) || "M 124".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor124 = true;
                    okFor125 = true;
                } else if ("M 125".equals(row.course) || "M 126".equals(row.course)) {
                    okFor117 = true;
                    okFor118 = true;
                    okFor124 = true;
                    okFor125 = true;
                    okFor126 = true;
                }
            }

            final List<RawStcourse> history = RawStcourseLogic.getHistory(cache, stuId);
            final TermRec active = cache.getSystemData().getActiveTerm();

            for (final RawStcourse row : history) {
                if (row.termKey.equals(active.term)) {
                    continue;
                }

                if ("Y".equals(row.prereqSatis)) {
                    if ("M 117".equals(row.course) && !okFor117) {
                        htm.addln("<li>Registration in MATH 117 from ", row.termKey.longString,
                                " had prereq_satis = 'Y'; assuming eligible for MATH 117</li>");
                        okFor117 = true;
                    } else if ("M 118".equals(row.course) && !okFor118) {
                        htm.addln("<li>Registration in MATH 118 from ", row.termKey.longString,
                                " had prereq_satis = 'Y'; assuming eligible for MATH 118</li>");
                        okFor118 = true;
                    } else if ("M 124".equals(row.course) && !okFor124) {
                        htm.addln("<li>Registration in MATH 124 from ", row.termKey.longString,
                                " had prereq_satis = 'Y'; assuming eligible for MATH 124</li>");
                        okFor124 = true;
                    } else if ("M 125".equals(row.course) && !okFor125) {
                        htm.addln("<li>Registration in MATH 125 from ", row.termKey.longString,
                                " had prereq_satis = 'Y'; assuming eligible for MATH 125</li>");
                        okFor125 = true;
                    } else if ("M 126".equals(row.course) && !okFor126) {
                        htm.addln("<li>Registration in MATH 126 from ", row.termKey.longString,
                                " had prereq_satis = 'Y'; assuming eligible for MATH 126</li>");
                        okFor126 = true;
                    }
                }
            }

            final List<RawStcourse> current = RawStcourseLogic.getPaced(cache, stuId);

            if (!okFor117) {
                for (final RawStcourse test : current) {
                    if (RawRecordConstants.M117.equals(test.course)
                        && ("801".equals(test.sect) || "809".equals(test.sect))) {
                        htm.addln("<li>Current-Term Registration in distance section of MATH 117, ",
                                "clearing prerequisite but using ELM as Skills Review.</li>");
                        okFor117 = true;
                        break;
                    }
                }
            }

            htm.addln("</ul>");

            htm.sH(4).add("Prerequisite status for student ", stuId, " (", screenName, ")").eH(4);

            final PrerequisiteLogic prereq = new PrerequisiteLogic(cache, stuId);

            htm.addln("<ul>");
            if (prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M117)) {
                if (okFor117) {
                    htm.add("<li>");
                } else {
                    htm.add("<li style='color:red'>*** WARNING: ");
                }
                if (prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.M117)) {
                    htm.addln("Eligible for MATH 117 (by transfer credit)</li>");
                } else {
                    htm.addln("Eligible for MATH 117</li>");
                }
            } else if (okFor117) {
                htm.addln("<li style='color:red'>*** WARNING: Logic says prerequisite not cleared for MATH 117</li>");
            }

            if (prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M118)) {
                if (okFor118) {
                    htm.add("<li>");
                } else {
                    htm.add("<li style='color:red'>*** WARNING: ");
                }
                if (prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.M118)) {
                    htm.addln("Eligible for MATH 118 (by transfer credit)</li>");
                } else {
                    htm.addln("Eligible for MATH 118</li>");
                }
            } else if (okFor118) {
                htm.addln("<li style='color:red'>*** WARNING: Logic says prerequisite not cleared for MATH 118</li>");
            }

            if (prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M124)) {
                if (okFor124) {
                    htm.add("<li>");
                } else {
                    htm.add("<li style='color:red'>*** WARNING: ");
                }
                if (prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.M124)) {
                    htm.addln("Eligible for MATH 124 (by transfer credit)</li>");
                } else {
                    htm.addln("Eligible for MATH 124</li>");
                }
            } else if (okFor124) {
                htm.addln("<li style='color:red'>*** WARNING: Logic says prerequisite not cleared for MATH 124</li>");
            }

            if (prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M125)) {
                if (okFor125) {
                    htm.add("<li>");
                } else {
                    htm.add("<li style='color:red'>*** WARNING: ");
                }
                if (prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.M125)) {
                    htm.addln("Eligible for MATH 125 (by transfer credit)</li>");
                } else {
                    htm.addln("Eligible for MATH 125</li>");
                }
            } else if (okFor125) {
                htm.addln("<li style='color:red'>*** WARNING: Logic says prerequisite not cleared for MATH 125</li>");
            }

            if (prereq.hasSatisfiedPrerequisitesFor(RawRecordConstants.M126)) {
                if (okFor126) {
                    htm.add("<li>");
                } else {
                    htm.add("<li style='color:red'>*** WARNING: ");
                }
                if (prereq.hasSatisfiedPrerequisitesByTransferFor(RawRecordConstants.M126)) {
                    htm.addln("Eligible for MATH 126 (by transfer credit)</li>");
                } else {
                    htm.addln("Eligible for MATH 126</li>");
                }
            } else if (okFor126) {
                htm.addln("<li style='color:red'>*** WARNING: Logic says prerequisite not cleared for MATH 126</li>");
            }

            htm.addln("</ul>");

            htm.sH(4).add("Current-term registrations for student ", stuId, " (", screenName, ")").eH(4);
            htm.addln("<ul>");
            for (final RawStcourse row : current) {

                if (RawRecordConstants.M117.equals(row.course) || RawRecordConstants.MATH117.equals(row.course)) {
                    if (okFor117) {
                        if ("Y".equals(row.prereqSatis)) {
                            htm.addln("<li>", row.course, " (prereq_satis = 'Y')</li>");
                        } else {
                            htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " (prereq_satis = ",
                                    row.prereqSatis, ")</li>");
                        }
                    } else if ("Y".equals(row.prereqSatis)) {
                        htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " (prereq_satis = 'Y')</li>");
                    } else {
                        htm.addln("<li>", row.course, " (prereq_satis = ", row.prereqSatis, ")</li>");
                    }
                }

                if (RawRecordConstants.M118.equals(row.course) || RawRecordConstants.MATH118.equals(row.course)) {
                    if (okFor118) {
                        if ("Y".equals(row.prereqSatis)) {
                            htm.addln("<li>", row.course, " (prereq_satis = 'Y')</li>");
                        } else {
                            htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " (prereq_satis = ",
                                    row.prereqSatis, ")</li>");
                        }
                    } else if ("Y".equals(row.prereqSatis)) {
                        htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " (prereq_satis = 'Y')</li>");
                    } else {
                        htm.addln("<li>", row.course, " (prereq_satis = ", row.prereqSatis, ")</li>");
                    }
                }

                if (RawRecordConstants.M124.equals(row.course) || RawRecordConstants.MATH124.equals(row.course)) {
                    if (okFor124) {
                        if ("Y".equals(row.prereqSatis)) {
                            htm.addln("<li>", row.course, " (prereq_satis = 'Y')</li>");
                        } else {
                            htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " (prereq_satis = ",
                                    row.prereqSatis, ")</li>");
                        }
                    } else if ("Y".equals(row.prereqSatis)) {
                        htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " (prereq_satis = 'Y')</li>");
                    } else {
                        htm.addln("<li>", row.course, " (prereq_satis = ", row.prereqSatis, ")</li>");
                    }
                }

                if (RawRecordConstants.M125.equals(row.course) || RawRecordConstants.MATH125.equals(row.course)) {
                    if (okFor125) {
                        if ("Y".equals(row.prereqSatis)) {
                            htm.addln("<li>", row.course, " (prereq_satis = 'Y')</li>");
                        } else {
                            htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " (prereq_satis = ",
                                    row.prereqSatis, ")</li>");
                        }
                    } else if ("Y".equals(row.prereqSatis)) {
                        htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " (prereq_satis = 'Y')</li>");
                    } else {
                        htm.addln("<li>", row.course, " (prereq_satis = ", row.prereqSatis, ")</li>");
                    }
                }

                if (RawRecordConstants.M126.equals(row.course) || RawRecordConstants.MATH126.equals(row.course)) {
                    if (okFor126) {
                        if ("Y".equals(row.prereqSatis)) {
                            htm.addln("<li>", row.course, " (prereq_satis = 'Y')</li>");
                        } else {
                            htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " (prereq_satis = ",
                                    row.prereqSatis, ")</li>");
                        }
                    } else if ("Y".equals(row.prereqSatis)) {
                        htm.addln("<li style='color:red;'>*** WARNING: ", row.course, " (prereq_satis = 'Y')</li>");
                    } else {
                        htm.addln("<li>", row.course, " (prereq_satis = ", row.prereqSatis, ")</li>");
                    }
                }
            }
            htm.addln("</ul>");
        }
    }

    /**
     * Starts a page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param session the login session
     * @return an {@code HtmlBuilder} with the started page content
     * @throws SQLException if there is an error accessing the database
     */
    private static HtmlBuilder startPage(final Cache cache, final AdminSite site, final ImmutableSessionInfo session)
            throws SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.LOGIC_TESTING, htm);
        htm.sH(1).add("Logic Testing").eH(1);

        PageLogicTesting.emitNavMenu(htm, EAdmSubtopic.LOGIC_PREREQUISITES);
        htm.hr().div("vgap");

        htm.sP().add("This page tests logic contained in the <code>PrerequisiteLogic</code> class in the ",
                "<code>dev.mathops.db.old.logic</code> package.").eP();
        htm.hr().div("vgap0");

        htm.sDiv("indent");
        htm.addln("<form action='logic_prerequisites.html' method='POST'>");
        htm.addln("  Student ID: <input id='stu_id' name='stu_id' type='text' size='7'/>");
        htm.addln("  <input type='submit' value='Examine this student's prerequisite status...'/>");
        htm.addln("</form>");
        htm.eDiv();
        htm.div("vgap0");

        htm.sDiv("indent");
        htm.addln("<form action='logic_prerequisites.html' method='POST'>");
        htm.addln("  <input type='hidden' id='stu_id' name='stu_id' value='ALL'/>");
        htm.addln("  <input type='submit' value='Scan prerequisites for all active registrations...'/>");
        htm.addln("</form>");
        htm.eDiv();

        return htm;
    }

    /**
     * Ends a page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @param htm   the {@code HtmlBuilder} with the page content to send
     * @throws SQLException if there is an error accessing the database
     */
    private static void endPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                final HttpServletResponse resp, final HtmlBuilder htm)
            throws IOException, SQLException {

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
