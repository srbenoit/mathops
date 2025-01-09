package dev.mathops.app.adm.instructor;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.text.builder.HtmlBuilder;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A panel to display the status of a single section.
 */
final class SectionStatusPane extends JPanel {

    /**
     * Constructs a new {@code SectionStatusPane}.
     *
     * @param theCache  the cache
     * @param theCourse the course
     * @param theSect   the section
     */
    SectionStatusPane(final Cache theCache, final String theCourse, final String theSect) {

        super(new StackedBorderLayout());

        setBackground(Skin.WHITE);
        final Border etched = BorderFactory.createEtchedBorder();
        setBorder(etched);

        try {
            final TermRec active = TermLogic.get(theCache).queryActive(theCache);
            if (active == null) {
                final JPanel errorFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 9, 6));
                errorFlow.setBackground(Skin.WHITE);
                final JLabel errorLbl = new JLabel("Unable to look up the active term.");
                errorFlow.add(errorLbl);
                add(errorFlow, StackedBorderLayout.NORTH);
            } else {
                final List<RawStcourse> regs = RawStcourseLogic.queryByTermCourseSection(theCache, active.term,
                        theCourse, theSect, false);
                final Map<String, RawStudent> studentMap = new HashMap<>(regs.size());
                for (final RawStcourse reg : regs) {
                    if (!studentMap.containsKey(reg.stuId)) {
                        final RawStudent stu = RawStudentLogic.query(theCache, reg.stuId, false);
                        if (stu != null) {
                            studentMap.put(reg.stuId, stu);
                        }
                    }
                }
                final List<RawStexam> exams = RawStexamLogic.getExams(theCache, theCourse, false, "R", "U", "F");

                final String courseName = theCourse.replace("M ", " MATH ");
                final String headingText = courseName + ", Section " + theSect;

                buildPanelContent(headingText, regs, studentMap, exams);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            final JPanel errorFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 9, 6));
            errorFlow.setBackground(Skin.WHITE);
            final JLabel errorLbl = new JLabel("Failed to query data: " + ex.getLocalizedMessage());
            errorFlow.add(errorLbl);
            add(errorFlow, StackedBorderLayout.NORTH);
        }
    }

    /**
     * Builds the panel content based on a list of active (non-dropped) registrations.
     *
     * @param headingText the course and section number for the heading
     * @param regs        the list of registrations
     * @param studentMap  a map from student ID to student record
     * @param exams       the list of all exams taken in the course (includes non-passing)
     */
    private void buildPanelContent(final String headingText, final Iterable<RawStcourse> regs,
                                   final Map<String, RawStudent> studentMap, final Iterable<RawStexam> exams) {

        // Classify registrations as "not started", "in progress", "completed", and "forfeit"

        final Collection<RawStcourse> notStarted = new ArrayList<>(10);
        final Collection<RawStcourse> inProgress = new ArrayList<>(10);
        final Collection<RawStcourse> completed = new ArrayList<>(10);
        final Collection<RawStcourse> forfeit = new ArrayList<>(10);

        for (final RawStcourse reg : regs) {
            switch (reg.openStatus) {
                case "G" -> forfeit.add(reg);
                case "N" -> {
                    if ("Y".equals(reg.completed)) {
                        completed.add(reg);
                    } else {
                        forfeit.add(reg);
                    }
                }
                case "Y" -> {
                    if ("Y".equals(reg.completed)) {
                        completed.add(reg);
                    } else {
                        inProgress.add(reg);
                    }
                }
                case null, default -> notStarted.add(reg);
            }
        }

        final int numNotStarted = notStarted.size();
        final int numInProgress = inProgress.size();
        final int numCompleted = completed.size();
        final int numForfeit = forfeit.size();

        final HtmlBuilder builder = new HtmlBuilder(100);
        builder.add(headingText, " (");
        boolean comma = false;
        if (numNotStarted > 0) {
            builder.add(numNotStarted);
            builder.add(" not started");
            comma = true;
        }
        if (numInProgress > 0) {
            if (comma) {
                builder.add(", ");
            }
            builder.add(numInProgress);
            builder.add(" in progress");
            comma = true;
        }
        if (numCompleted > 0) {
            if (comma) {
                builder.add(", ");
            }
            builder.add(numCompleted);
            builder.add(" completed");
            comma = true;
        }
        if (numForfeit > 0) {
            if (comma) {
                builder.add(", ");
            }
            builder.add(numForfeit);
            builder.add(" forfeit");
        }
        if (numNotStarted + numInProgress + numCompleted + numForfeit == 0) {
            builder.add("no students enrolled");
        }
        builder.add(")");

        final String fullText = builder.toString();
        final JLabel heading = AdmPanelBase.makeLabelMedium(fullText);
        final JPanel headingFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
        headingFlow.setBackground(Skin.WHITE);
        headingFlow.add(heading);
        add(headingFlow, StackedBorderLayout.NORTH);

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        // Present each population with its statistics

        if (numNotStarted > 0) {
            final String headerText = "Students who have not yet started the course (" + numNotStarted + ")";
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(headerText);
            root.add(node);
            presentSimplePopulation(node, notStarted, studentMap, true);
        }

        if (numInProgress > 0) {
            final String headerText = "Students who have started but not yet finished (" + numInProgress + ")";
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(headerText);
            root.add(node);
            presentPopulation(node, inProgress, studentMap, exams);
        }

        if (numCompleted > 0) {
            final String headerText = "Students who have completed the course (" + numCompleted + ")";
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(headerText);
            root.add(node);
            presentPopulation(node, completed, studentMap, exams);
        }

        if (numForfeit > 0) {
            final String headerText = "Students who have forfeit the course (" + numForfeit + ")";
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(headerText);
            root.add(node);
            presentSimplePopulation(node, forfeit, studentMap, false);
        }

        final JTree tree = new JTree(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        final JScrollPane treeScroll = new JScrollPane(tree);
        add(treeScroll, StackedBorderLayout.CENTER);
    }

    /**
     * Presents the list of students in a single population (without status details).
     *
     * @param node       the tree node for the population
     * @param regs       the list of registrations
     * @param studentMap a map from student ID to student record
     */
    private static void presentSimplePopulation(final DefaultMutableTreeNode node, final Iterable<RawStcourse> regs,
                                                final Map<String, RawStudent> studentMap,
                                                final boolean includeLicensed) {

        final Map<String, RawStcourse> sortedByLastName = new TreeMap<>();
        for (final RawStcourse reg : regs) {
            final RawStudent stu = studentMap.get(reg.stuId);

            if (stu != null) {
                final String name = stu.lastName + ", " + stu.firstName;
                sortedByLastName.put(name, reg);
            }
        }

        final HtmlBuilder builder = new HtmlBuilder(100);

        for (final Map.Entry<String, RawStcourse> entry : sortedByLastName.entrySet()) {
            final String name = entry.getKey();
            final RawStcourse reg = entry.getValue();
            final RawStudent stu = studentMap.get(reg.stuId);

            if (stu != null) {
                builder.add(name, "  (", reg.stuId, ")");

                if ("Y".equals(reg.iInProgress)) {
                    final String iTerm = reg.iTermKey.longString;

                    if ("Y".equals(reg.iCounted)) {
                        builder.add(" [Incomplete from ", iTerm, "]");
                    } else {
                        final String dueDate = TemporalUtils.FMT_MDY.format(reg.iDeadlineDt);
                        builder.add(" [Incomplete from ", iTerm, " due ", dueDate, "]");
                    }
                }

                if (includeLicensed) {
                    if ("Y".equals(stu.licensed)) {
                        builder.add(" - Passed User's Exam");
                    } else {
                        builder.add(" - Has not passed User's Exam");
                    }
                }

                final String nodeText = builder.toString();
                builder.reset();

                final MutableTreeNode stuNode = new DefaultMutableTreeNode(nodeText);
                node.add(stuNode);
            }
        }
    }

    /**
     * Presents status of a single population with statistics.
     *
     * @param node       the tree node for the population
     * @param regs       the list of registrations
     * @param studentMap a map from student ID to student record
     * @param exams      the list of all exams taken in the course (includes non-passing)
     */
    private static void presentPopulation(final DefaultMutableTreeNode node, final Iterable<RawStcourse> regs,
                                          final Map<String, RawStudent> studentMap, final Iterable<RawStexam> exams) {

        final Map<String, RawStcourse> sortedByLastName = new TreeMap<>();
        for (final RawStcourse reg : regs) {
            final RawStudent stu = studentMap.get(reg.stuId);

            if (stu != null) {
                final String name = stu.lastName + ", " + stu.firstName;
                sortedByLastName.put(name, reg);
            }
        }

        final HtmlBuilder builder = new HtmlBuilder(100);

        for (final Map.Entry<String, RawStcourse> entry : sortedByLastName.entrySet()) {
            final String name = entry.getKey();
            final RawStcourse reg = entry.getValue();
            final RawStudent stu = studentMap.get(reg.stuId);

            if (stu != null) {
                builder.add(name, "  (", reg.stuId, ")");

                if ("Y".equals(reg.iInProgress)) {
                    final String iTerm = reg.iTermKey.longString;

                    if ("Y".equals(reg.iCounted)) {
                        builder.add(" [Incomplete from ", iTerm, "]");
                    } else {
                        final String dueDate = TemporalUtils.FMT_MDY.format(reg.iDeadlineDt);
                        builder.add(" [Incomplete from ", iTerm, " due ", dueDate, "]");
                    }
                }

                if ("F".equals(stu.sevAdminHold)) {
                    builder.add(" - HAS HOLD");
                }
                if (stu.timelimitFactor != null && stu.timelimitFactor.doubleValue() > 1.0) {
                    builder.add(" - ", stu.timelimitFactor, "x Time Limit");
                }
                if (stu.extensionDays != null && stu.extensionDays.intValue() > 0) {
                    builder.add(" - ", stu.extensionDays, "-day Extensions");
                }

                final String nodeText = builder.toString();
                builder.reset();
                final DefaultMutableTreeNode stuNode = new DefaultMutableTreeNode(nodeText);
                node.add(stuNode);

                int numTriesSR = 0;
                int numTriesR1 = 0;
                int numTriesU1 = 0;
                int numTriesR2 = 0;
                int numTriesU2 = 0;
                int numTriesR3 = 0;
                int numTriesU3 = 0;
                int numTriesR4 = 0;
                int numTriesU4 = 0;
                int numTriesFE = 0;

                int maxScoreSR = 0;
                int maxScoreR1 = 0;
                int maxScoreU1 = 0;
                int maxScoreR2 = 0;
                int maxScoreU2 = 0;
                int maxScoreR3 = 0;
                int maxScoreU3 = 0;
                int maxScoreR4 = 0;
                int maxScoreU4 = 0;
                int maxScoreFE = 0;

                boolean passedSR = false;
                boolean passedR1 = false;
                boolean passedU1 = false;
                boolean passedR2 = false;
                boolean passedU2 = false;
                boolean passedR3 = false;
                boolean passedU3 = false;
                boolean passedR4 = false;
                boolean passedU4 = false;
                boolean passedFE = false;

                for (final RawStexam exam : exams) {
                    if (reg.stuId.equals(exam.stuId)) {
                        final boolean passed;

                        if ("Y".equals(exam.passed)) {
                            passed = true;
                        } else if ("N".equals(exam.passed)) {
                            passed = false;
                        } else {
                            continue;
                        }

                        final int unit = exam.unit.intValue();
                        final String type = exam.examType;
                        final int score = exam.examScore.intValue();

                        if ("R".equals(type)) {
                            if (unit == 0) {
                                ++numTriesSR;
                                maxScoreSR = Math.max(maxScoreSR, score);
                                passedSR = passedSR || passed;
                            } else if (unit == 1) {
                                ++numTriesR1;
                                maxScoreR1 = Math.max(maxScoreR1, score);
                                passedR1 = passedR1 || passed;
                            } else if (unit == 2) {
                                ++numTriesR2;
                                maxScoreR2 = Math.max(maxScoreR2, score);
                                passedR2 = passedR2 || passed;
                            } else if (unit == 3) {
                                ++numTriesR3;
                                maxScoreR3 = Math.max(maxScoreR3, score);
                                passedR3 = passedR3 || passed;
                            } else if (unit == 4) {
                                ++numTriesR4;
                                maxScoreR4 = Math.max(maxScoreR4, score);
                                passedR4 = passedR4 || passed;
                            }
                        } else if ("U".equals(type)) {
                            if (unit == 1) {
                                ++numTriesU1;
                                maxScoreU1 = Math.max(maxScoreU1, score);
                                passedU1 = passedU1 || passed;
                            } else if (unit == 2) {
                                ++numTriesU2;
                                maxScoreU2 = Math.max(maxScoreU2, score);
                                passedU2 = passedU2 || passed;
                            } else if (unit == 3) {
                                ++numTriesU3;
                                maxScoreU3 = Math.max(maxScoreU3, score);
                                passedU3 = passedU3 || passed;
                            } else if (unit == 4) {
                                ++numTriesU4;
                                maxScoreU4 = Math.max(maxScoreU4, score);
                                passedU4 = passedU4 || passed;
                            }
                        } else if ("F".equals(type)) {
                            ++numTriesFE;
                            maxScoreFE = Math.max(maxScoreFE, score);
                            passedFE = passedFE || passed;
                        }
                    }
                }

                // Skills Review

                builder.add("Skills Review: ");
                if (numTriesSR > 0) {
                    if (numTriesSR == 1) {
                        builder.add(" 1 attempt");
                    } else {
                        builder.add(numTriesSR);
                        builder.add(" attempts");
                    }

                    builder.add(", best score = ");
                    builder.add(maxScoreSR);

                    builder.add(passedSR ? " (PASSED)" : " (not yet passed)");
                } else {
                    builder.add(" (not attempted)");
                }

                final String srText = builder.toString();
                builder.reset();
                final MutableTreeNode srNode = new DefaultMutableTreeNode(srText);
                stuNode.add(srNode);

                // Unit 1

                builder.add("Unit 1 Review: ");
                if (numTriesR1 > 0) {
                    if (numTriesR1 == 1) {
                        builder.add(" 1 attempt");
                    } else {
                        builder.add(numTriesR1);
                        builder.add(" attempts");
                    }

                    builder.add(", best score = ");
                    builder.add(maxScoreR1);

                    builder.add(passedR1 ? " (PASSED)" : " (not yet passed)");
                } else {
                    builder.add(" (not attempted)");
                }

                final String r1Text = builder.toString();
                builder.reset();
                final MutableTreeNode r1Node = new DefaultMutableTreeNode(r1Text);
                stuNode.add(r1Node);

                builder.add("Unit 1 Exam: ");
                if (numTriesU1 > 0) {
                    if (numTriesU1 == 1) {
                        builder.add(" 1 attempt");
                    } else {
                        builder.add(numTriesU1);
                        builder.add(" attempts");
                    }

                    builder.add(", best score = ");
                    builder.add(maxScoreU1);

                    builder.add(passedU1 ? " (PASSED)" : " (not yet passed)");
                } else {
                    builder.add(" (not attempted)");
                }

                final String u1Text = builder.toString();
                builder.reset();
                final MutableTreeNode u1Node = new DefaultMutableTreeNode(u1Text);
                stuNode.add(u1Node);

                // Unit 2

                builder.add("Unit 2 Review: ");
                if (numTriesR2 > 0) {
                    if (numTriesR2 == 1) {
                        builder.add(" 1 attempt");
                    } else {
                        builder.add(numTriesR2);
                        builder.add(" attempts");
                    }

                    builder.add(", best score = ");
                    builder.add(maxScoreR2);

                    builder.add(passedR2 ? " (PASSED)" : " (not yet passed)");
                } else {
                    builder.add(" (not attempted)");
                }

                final String r2Text = builder.toString();
                builder.reset();
                final MutableTreeNode r2Node = new DefaultMutableTreeNode(r2Text);
                stuNode.add(r2Node);

                builder.add("Unit 2 Exam: ");
                if (numTriesU2 > 0) {
                    if (numTriesU2 == 1) {
                        builder.add(" 1 attempt");
                    } else {
                        builder.add(numTriesU2);
                        builder.add(" attempts");
                    }

                    builder.add(", best score = ");
                    builder.add(maxScoreU2);

                    builder.add(passedU2 ? " (PASSED)" : " (not yet passed)");
                } else {
                    builder.add(" (not attempted)");
                }

                final String u2Text = builder.toString();
                builder.reset();
                final MutableTreeNode u2Node = new DefaultMutableTreeNode(u2Text);
                stuNode.add(u2Node);

                // Unit 3

                builder.add("Unit 3 Review: ");
                if (numTriesR3 > 0) {
                    if (numTriesR3 == 1) {
                        builder.add(" 1 attempt");
                    } else {
                        builder.add(numTriesR3);
                        builder.add(" attempts");
                    }

                    builder.add(", best score = ");
                    builder.add(maxScoreR3);

                    builder.add(passedR3 ? " (PASSED)" : " (not yet passed)");
                } else {
                    builder.add(" (not attempted)");
                }

                final String r3Text = builder.toString();
                builder.reset();
                final MutableTreeNode r3Node = new DefaultMutableTreeNode(r3Text);
                stuNode.add(r3Node);

                builder.add("Unit 3 Exam: ");
                if (numTriesU3 > 0) {
                    if (numTriesU3 == 1) {
                        builder.add(" 1 attempt");
                    } else {
                        builder.add(numTriesU3);
                        builder.add(" attempts");
                    }

                    builder.add(", best score = ");
                    builder.add(maxScoreU3);

                    builder.add(passedU3 ? " (PASSED)" : " (not yet passed)");
                } else {
                    builder.add(" (not attempted)");
                }

                final String u3Text = builder.toString();
                builder.reset();
                final MutableTreeNode u3Node = new DefaultMutableTreeNode(u3Text);
                stuNode.add(u3Node);

                // Unit 4

                builder.add("Unit 4 Review: ");
                if (numTriesR4 > 0) {
                    if (numTriesR4 == 1) {
                        builder.add(" 1 attempt");
                    } else {
                        builder.add(numTriesR4);
                        builder.add(" attempts");
                    }

                    builder.add(", best score = ");
                    builder.add(maxScoreR4);

                    builder.add(passedR4 ? " (PASSED)" : " (not yet passed)");
                } else {
                    builder.add(" (not attempted)");
                }

                final String r4Text = builder.toString();
                builder.reset();
                final MutableTreeNode r4Node = new DefaultMutableTreeNode(r4Text);
                stuNode.add(r4Node);

                builder.add("Unit 4 Exam: ");
                if (numTriesU4 > 0) {
                    if (numTriesU4 == 1) {
                        builder.add(" 1 attempt");
                    } else {
                        builder.add(numTriesU4);
                        builder.add(" attempts");
                    }

                    builder.add(", best score = ");
                    builder.add(maxScoreU4);

                    builder.add(passedU4 ? " (PASSED)" : " (not yet passed)");
                } else {
                    builder.add(" (not attempted)");
                }

                final String u4Text = builder.toString();
                builder.reset();
                final MutableTreeNode u4Node = new DefaultMutableTreeNode(u4Text);
                stuNode.add(u4Node);

                // Final

                builder.add("Final Exam: ");
                if (numTriesFE > 0) {
                    if (numTriesFE == 1) {
                        builder.add(" 1 attempt");
                    } else {
                        builder.add(numTriesFE);
                        builder.add(" attempts");
                    }

                    builder.add(", best score = ");
                    builder.add(maxScoreFE);

                    builder.add(passedFE ? " (PASSED)" : " (not yet passed)");
                } else {
                    builder.add(" (not attempted)");
                }

                final String feText = builder.toString();
                builder.reset();
                final MutableTreeNode feNode = new DefaultMutableTreeNode(feText);
                stuNode.add(feNode);
            }
        }
    }
}
