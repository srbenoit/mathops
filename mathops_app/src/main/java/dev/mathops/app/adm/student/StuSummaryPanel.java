package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.logic.PlacementLogic;
import dev.mathops.db.old.logic.PlacementStatus;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmilestone;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The "Summary" panel of the admin system.
 */
final class StuSummaryPanel extends AdmPanelBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2833502419921576143L;

    /** The data cache. */
    private final Cache cache;

    /** A list of courses the student has placed out of. */
    private final JLabel placedOutOfList;

    /** A list of courses the student is eligible for. */
    private final JLabel eligibleForList;

    /** Placement attempts remaining. */
    private final JLabel attemptsRemain;

    /** Pane in which to display placement status. */
    private final JPanel placementPane;

    /** Pane in which to display current-term registrations. */
    private final JPanel currentCoursePane;

    /** Pane in which to display active holds. */
    private final JPanel holdsPane;

    /** Pane in which to display accommodations. */
    private final JPanel accommodationsPane;

    /**
     * Constructs a new {@code StudentSummaryPanel}.
     *
     * @param theCache         the data cache
     */
    StuSummaryPanel(final Cache theCache) {

        super();

        this.cache = theCache;

        setBackground(Skin.WHITE);

        final JPanel north = makeOffWhitePanel(new BorderLayout(0, 0));
        north.setBackground(Skin.WHITE);
        add(north, StackedBorderLayout.NORTH);

        north.add(makeHeader("Status Summary", false), BorderLayout.PAGE_START);

        final JPanel center = makeOffWhitePanel(new BorderLayout());
        center.setBackground(Skin.WHITE);
        north.add(center, BorderLayout.CENTER);

        final JPanel south = makeOffWhitePanel(new BorderLayout());
        south.setBackground(Skin.WHITE);
        north.add(south, BorderLayout.PAGE_END);

        south.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.DARK));

        this.placedOutOfList = new JLabel();
        this.placedOutOfList.setBorder(BorderFactory.createEmptyBorder(2, 0, 1, 0));
        this.placedOutOfList.setFont(Skin.BUTTON_13_FONT);
        this.eligibleForList = new JLabel();
        this.eligibleForList.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        this.eligibleForList.setFont(Skin.BUTTON_13_FONT);
        this.attemptsRemain = new JLabel();
        this.attemptsRemain.setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
        this.attemptsRemain.setFont(Skin.BUTTON_13_FONT);

        this.placementPane = new JPanel();
        final LayoutManager placementBox = new BoxLayout(this.placementPane, BoxLayout.PAGE_AXIS);
        this.placementPane.setLayout(placementBox);
        this.placementPane.setBackground(Skin.WHITE);
        this.placementPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 2, 10));

        this.currentCoursePane = new JPanel();
        final LayoutManager courseBox = new BoxLayout(this.currentCoursePane, BoxLayout.PAGE_AXIS);
        this.currentCoursePane.setLayout(courseBox);
        this.currentCoursePane.setBackground(Skin.WHITE);
        this.currentCoursePane.setBorder(BorderFactory.createEmptyBorder(0, 10, 2, 10));

        this.holdsPane = new JPanel();
        final LayoutManager holdsBox = new BoxLayout(this.holdsPane, BoxLayout.PAGE_AXIS);
        this.holdsPane.setLayout(holdsBox);
        this.holdsPane.setBackground(Skin.WHITE);
        this.holdsPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 2, 10));

        this.accommodationsPane = new JPanel();
        final LayoutManager accomBox = new BoxLayout(this.accommodationsPane, BoxLayout.PAGE_AXIS);
        this.accommodationsPane.setLayout(accomBox);
        this.accommodationsPane.setBackground(Skin.WHITE);
        this.accommodationsPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 2, 10));

        final JLabel[] blockTitles = new JLabel[4];

        blockTitles[0] = new JLabel("Placement:");
        blockTitles[1] = new JLabel("Courses:");
        blockTitles[2] = new JLabel("Holds:");
        blockTitles[3] = new JLabel("Accommodations:");

        int maxH = 0;
        int maxW = 0;
        for (final JLabel blockTitle : blockTitles) {
            blockTitle.setFont(Skin.MEDIUM_HEADER_15_FONT);
            final Dimension pref = blockTitle.getPreferredSize();
            maxH = Math.max(maxH, pref.height);
            maxW = Math.max(maxW, pref.width);
        }
        final Dimension lblSize = new Dimension(maxW, maxH);
        for (final JLabel blockTitle : blockTitles) {
            blockTitle.setPreferredSize(lblSize);
        }

        final JPanel placementBlock = makePlacementBlock(blockTitles[0]);
        center.add(placementBlock, BorderLayout.PAGE_START);

        final JPanel coursesBlock = makeCoursesBlock(blockTitles[1]);
        center.add(coursesBlock, BorderLayout.CENTER);

        final JPanel holdsBlock = makeHoldsBlock(blockTitles[2]);
        center.add(holdsBlock, BorderLayout.PAGE_END);

        final JPanel accomBlock = makeAccommodationsBlock(blockTitles[3]);
        south.add(accomBlock, BorderLayout.PAGE_START);
    }

    /**
     * Creates the block that displays placement result.
     *
     * @param blockTitle       the block title label
     * @return the placement results block panel
     */
    private JPanel makePlacementBlock(final JLabel blockTitle) {

        final JPanel block = new JPanel(new BorderLayout());
        block.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Skin.DARK));
        block.setBackground(Skin.WHITE);

        final JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Skin.WHITE);
        block.add(grid, BorderLayout.LINE_START);

        final GridBagConstraints constraints = new GridBagConstraints();

        final JPanel rowHead = new JPanel(new BorderLayout());
        rowHead.setBackground(Skin.LIGHTER_GRAY);
        rowHead.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 10));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 4;
        constraints.fill = GridBagConstraints.BOTH;
        grid.add(rowHead, constraints);

        rowHead.add(blockTitle, BorderLayout.PAGE_START);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridheight = 1;

        final JLabel lbl1 = new JLabel("Placed out of:  ");
        lbl1.setForeground(Skin.LABEL_COLOR3);
        lbl1.setBorder(BorderFactory.createEmptyBorder(2, 10, 1, 1));
        lbl1.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl1.setFont(Skin.BUTTON_BOLD_13_FONT);
        constraints.gridx = 1;
        constraints.gridy = 0;
        grid.add(lbl1, constraints);
        constraints.gridx = 2;
        grid.add(this.placedOutOfList, constraints);

        final JLabel lbl2 = new JLabel("Eligible for:  ");
        lbl2.setForeground(Skin.LABEL_COLOR3);
        lbl2.setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 1));
        lbl2.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl2.setFont(Skin.BUTTON_BOLD_13_FONT);
        constraints.gridx = 1;
        constraints.gridy = 1;
        grid.add(lbl2, constraints);
        constraints.gridx = 2;
        grid.add(this.eligibleForList, constraints);

        final JLabel lbl3 = new JLabel("Attempts available:  ");
        lbl3.setForeground(Skin.LABEL_COLOR3);
        lbl3.setBorder(BorderFactory.createEmptyBorder(1, 10, 2, 1));
        lbl3.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl3.setFont(Skin.BUTTON_BOLD_13_FONT);
        constraints.gridx = 1;
        constraints.gridy = 2;
        grid.add(lbl3, constraints);
        constraints.gridx = 2;
        grid.add(this.attemptsRemain, constraints);

        constraints.gridy = 3;
        constraints.gridx = 1;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.LINE_START;
        grid.add(this.placementPane, constraints);

        return block;
    }

    /**
     * Creates the block that displays course registrations.
     *
     * @param blockTitle the block title label
     * @return the course registrations block panel
     */
    private JPanel makeCoursesBlock(final JLabel blockTitle) {

        final JPanel block = new JPanel(new BorderLayout());
        block.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Skin.DARK));
        block.setBackground(Skin.WHITE);

        final JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Skin.WHITE);
        block.add(grid, BorderLayout.LINE_START);

        final GridBagConstraints constraints = new GridBagConstraints();

        final JPanel rowHead = new JPanel(new BorderLayout());
        rowHead.setBackground(Skin.LIGHTER_GRAY);
        rowHead.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 10));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 3;
        constraints.fill = GridBagConstraints.BOTH;
        grid.add(rowHead, constraints);

        rowHead.add(blockTitle, BorderLayout.PAGE_START);

        constraints.gridx = 1;
        grid.add(this.currentCoursePane, constraints);

        return block;
    }

    /**
     * Creates the block that displays holds.
     *
     * @param blockTitle the block title label
     * @return the course registrations block panel
     */
    private JPanel makeHoldsBlock(final JLabel blockTitle) {

        final JPanel block = new JPanel(new BorderLayout());
        block.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Skin.DARK));
        block.setBackground(Skin.WHITE);

        final JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Skin.WHITE);
        block.add(grid, BorderLayout.LINE_START);

        final GridBagConstraints constraints = new GridBagConstraints();

        final JPanel rowHead = new JPanel(new BorderLayout());
        rowHead.setBackground(Skin.LIGHTER_GRAY);
        rowHead.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 10));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.BOTH;
        grid.add(rowHead, constraints);

        rowHead.add(blockTitle, BorderLayout.PAGE_START);

        constraints.gridx = 1;
        grid.add(this.holdsPane, constraints);

        return block;
    }

    /**
     * Creates the block that displays accommodations.
     *
     * @param blockTitle the block title label
     * @return the course registrations block panel
     */
    private JPanel makeAccommodationsBlock(final JLabel blockTitle) {

        final JPanel block = new JPanel(new BorderLayout());
        block.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Skin.DARK));
        block.setBackground(Skin.WHITE);

        final JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Skin.WHITE);
        block.add(grid, BorderLayout.LINE_START);

        final GridBagConstraints constraints = new GridBagConstraints();

        final JPanel rowHead = new JPanel(new BorderLayout());
        rowHead.setBackground(Skin.LIGHTER_GRAY);
        rowHead.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 10));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.BOTH;
        grid.add(rowHead, constraints);

        rowHead.add(blockTitle, BorderLayout.PAGE_START);

        constraints.gridx = 1;
        grid.add(this.accommodationsPane, constraints);

        return block;
    }

    /**
     * Sets the selected student data.
     *
     * @param data the selected student data
     */
    public void setSelectedStudent(final StudentData data) {

        clearDisplay();

        if (data != null) {
            populateDisplay(data);
        }
    }

    /**
     * Clears all displayed fields.
     */
    void clearDisplay() {

        this.placedOutOfList.setText(CoreConstants.EMPTY);
        this.eligibleForList.setText(CoreConstants.EMPTY);
        this.attemptsRemain.setText(CoreConstants.EMPTY);
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data             the student data
     */
    private void populateDisplay(final StudentData data) {

        populatePlacement(data);
        populateCourses(data);
        populateHolds(data);
        populateAccommodations(data);

        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Populates the "Placement" portion of the display.
     *
     * @param data             the student data
     */
    private void populatePlacement(final StudentData data) {

        final StringBuilder placedOut = new StringBuilder(50);

        int added = 0;
        final int count = data.studentPlacementCredit.size();
        for (int i = 0; i < count; ++i) {
            final String name = nameForCourse(data.studentPlacementCredit.get(i).course);
            if (name != null) {
                if (added > 0) {
                    placedOut.append(", ");
                }
                placedOut.append(name);
                ++added;
            }
        }
        if (added == 0) {
            placedOut.append("(None)");
        }

        this.placedOutOfList.setText(placedOut.toString());

        final StringBuilder eligibleFor = new StringBuilder(50);
        try {
            final PrerequisiteLogic prereq = new PrerequisiteLogic(this.cache, data.student.stuId);

            int numEligible = 0;
            if (prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M117)) {
                eligibleFor.append("MATH 117");
                ++numEligible;
            }
            if (prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M118)) {
                if (numEligible > 0) {
                    eligibleFor.append(", ");
                }
                eligibleFor.append("MATH 118");
                ++numEligible;
            }
            if (prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M124)) {
                if (numEligible > 0) {
                    eligibleFor.append(", ");
                }
                eligibleFor.append("MATH 124");
                ++numEligible;
            }
            if (prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M125)) {
                if (numEligible > 0) {
                    eligibleFor.append(", ");
                }
                eligibleFor.append("MATH 125");
                ++numEligible;
            }
            if (prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M126)) {
                if (numEligible > 0) {
                    eligibleFor.append(", ");
                }
                eligibleFor.append("MATH 126");
                ++numEligible;
            }
            if (prereq.hasSatisfiedPrereqsFor(RawRecordConstants.M117)) {
                if (numEligible > 0) {
                    eligibleFor.append(", ");
                }
                eligibleFor.append("MATH 127");
                ++numEligible;
            }

            if (numEligible == 0) {
                final int numPlacement = data.studentPlacementAttempts.size();

                if (numPlacement == 0) {
                    eligibleFor.append("Math Placement Tool");
                } else if (numPlacement >= 2) {
                    eligibleFor.append("ELM Tutorial");
                } else {
                    eligibleFor.append("Math Placement Tool, ELM Tutorial");
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Error checking prerequisites", ex);
            eligibleFor.append("Error checking prerequisites");
        }

        this.eligibleForList.setText(eligibleFor.toString());

        final StringBuilder remaining = new StringBuilder(50);
        try {
            final PlacementLogic plcLogic = new PlacementLogic(this.cache, data.student.stuId, data.student.aplnTerm,
                    ZonedDateTime.now());

            final PlacementStatus status = plcLogic.status;
            final int totalRemain = status.attemptsRemaining;

            if (totalRemain == 0) {
                remaining.append("All attempts used.");
            } else {
                remaining.append(totalRemain);
                if (status.allowedToUseUnproctored && !status.unproctoredUsed) {
                    remaining.append(" (unproctored attempt available)");
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Error checking placement attempts remaining", ex);
            remaining.append("Error checking placement attempts remaining");
        }

        this.attemptsRemain.setText(remaining.toString());

        this.placementPane.removeAll();

        // Populate ELM Tutorial or Precalculus Tutorial status
        boolean usingElm = false;
        for (final RawStexam exam : data.studentExams) {
            if (RawRecordConstants.M100T.equals(exam.course)) {
                usingElm = true;
                break;
            }
        }

        if (usingElm) {
            final RawStcourse reg = new RawStcourse();
            reg.course = RawRecordConstants.M100T;
            reg.sect = "1";
            reg.paceOrder = Integer.valueOf(1);

            this.placementPane.add(Box.createRigidArea(new Dimension(4, 4)));
            this.placementPane.add(makeCourseRow(reg, data));
        }

        // TODO: Similar displays for Precalculus Tutorials
    }

    /**
     * Populates the "Courses" portion of the display.
     *
     * @param data             the student data
     */
    private void populateCourses(final StudentData data) {

        this.currentCoursePane.removeAll();

        boolean allIncompletes = true;
        final List<RawStcourse> regs = data.studentCoursesPastAndCurrent;
        final Collection<RawStcourse> current = new ArrayList<>(regs.size());

        for (final RawStcourse reg : regs) {
            if ("D".equals(reg.openStatus)) {
                continue;
            }
            if (reg.termKey.equals(data.activeKey)) {
                current.add(reg);
                if (!"Y".equals(reg.iInProgress)) {
                    allIncompletes = false;
                }
            }
        }

        if (current.isEmpty()) {
            final JLabel lbl = new JLabel("No registrations this term.");
            lbl.setFont(Skin.BUTTON_13_FONT);
            this.currentCoursePane.add(lbl);
        } else {
            final StringBuilder header = new StringBuilder(50);

            if (allIncompletes) {
                header.append("Finishing Only Incompletes");
            } else if (data.studentTerm == null) {
                header.append("(No STTERM record)");
            } else {
                header.append(data.studentTerm.pace);
                header.append(" course pace, track ");
                header.append(data.studentTerm.paceTrack);
            }

            if ("Y".equals(data.student.licensed)) {
                header.append(" [Passed User's Exam]");
            } else {
                header.append(" [Still needs to pass User's Exam]");
            }

            final JLabel lbl = new JLabel(header.toString());
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            lbl.setForeground(Skin.LABEL_COLOR3);
            lbl.setFont(Skin.BUTTON_BOLD_13_FONT);
            final JPanel lblBorder = new JPanel(new BorderLayout());
            lblBorder.setBackground(Skin.WHITE);
            lblBorder.add(lbl, BorderLayout.LINE_START);
            this.currentCoursePane.add(lblBorder);

            for (final RawStcourse reg : current) {
                this.currentCoursePane.add(Box.createRigidArea(new Dimension(4, 4)));
                this.currentCoursePane.add(makeCourseRow(reg, data));
            }
            this.currentCoursePane.add(Box.createRigidArea(new Dimension(4, 4)));
        }
    }

    /**
     * Populates the "Holds" portion of the display.
     *
     * @param data             the student data
     */
    private void populateHolds(final StudentData data) {

        this.holdsPane.removeAll();

        if (data.studentHolds.isEmpty()) {
            final JLabel lbl = new JLabel("No holds active.");
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            lbl.setFont(Skin.BUTTON_13_FONT);
            final JPanel left = new JPanel(new BorderLayout());
            left.setBackground(Skin.WHITE);
            left.add(lbl, BorderLayout.LINE_START);
            this.holdsPane.add(left);
        } else {
            final HtmlBuilder msg = new HtmlBuilder(100);
            for (final RawAdminHold hold : data.studentHolds) {
                msg.add("HOLD ");
                msg.add(hold.holdId);
                if ("F".equals(hold.sevAdminHold)) {
                    msg.add(" (Cannot Test)");
                }
                msg.add(":  ");
                msg.add(RawAdminHoldLogic.getStaffMessage(hold.holdId));

                final JLabel lbl = new JLabel(msg.toString());
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                lbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
                lbl.setFont(Skin.BUTTON_BOLD_13_FONT);
                this.holdsPane.add(lbl);

                msg.reset();
            }
        }
    }

    /**
     * Populates the "Accommodations" portion of the display.
     *
     * @param data             the student data
     */
    private void populateAccommodations(final StudentData data) {

        this.accommodationsPane.removeAll();

        final Float factor = data.student.timelimitFactor;

        if (data.paceAppeals.isEmpty() && (factor == null || factor.floatValue() <= 1.0f)) {
            final JLabel lbl = new JLabel("No accommodations.");
            lbl.setFont(Skin.BUTTON_13_FONT);
            final JPanel left = new JPanel(new BorderLayout());
            left.setBackground(Color.WHITE);
            left.add(lbl, BorderLayout.LINE_START);
            this.accommodationsPane.add(left);
        } else {
            final HtmlBuilder msg = new HtmlBuilder(100);
            if (factor != null && factor.floatValue() > 1.0f) {
                msg.add("Exam time limit factor: ");
                msg.add(factor);

                final JLabel lbl = new JLabel(msg.toString());
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                lbl.setFont(Skin.BUTTON_13_FONT);
                final JPanel left = new JPanel(new BorderLayout());
                left.setBackground(Color.WHITE);
                left.add(lbl, BorderLayout.LINE_START);
                this.accommodationsPane.add(left);
                msg.reset();
            }

            for (final RawPaceAppeals appeal : data.paceAppeals) {
                if ("N".equals(appeal.reliefGiven)) {
                    msg.add(TemporalUtils.FMT_MDY.format(appeal.appealDt));
                    msg.add(": ");
                    msg.add(appeal.circumstances);
                    if (appeal.comment != null) {
                        msg.add(' ');
                        msg.add(appeal.comment);
                    }

                    final JLabel lbl = new JLabel(msg.toString());
                    lbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
                    lbl.setFont(Skin.BUTTON_13_FONT);
                    final JPanel left = new JPanel(new BorderLayout());
                    left.setBackground(Color.WHITE);
                    left.add(lbl, BorderLayout.LINE_START);
                    this.accommodationsPane.add(left);
                    msg.reset();
                }
            }
        }
    }

    /**
     * Creates a panel to display current status in a single course.
     *
     * @param reg              the course registration
     * @param data             the student data
     * @return the panel
     */
    private static JPanel makeCourseRow(final RawStcourse reg, final StudentData data) {

        // See of the final exam has been passed
        boolean finalPassed = false;
        for (final RawStexam exam : data.studentExams) {
            if (exam.course.equals(reg.course) && exam.unit.intValue() == 5 && "F".equals(exam.examType)
                    && "Y".equals(exam.passed)) {
                finalPassed = true;
                break;
            }
        }

        // See if student is blocked
        boolean blocked = false;
        if (!finalPassed) {
            LocalDate finalDueDate = null;

            if (data.studentTerm != null && reg.paceOrder != null) {
                final int paceOrder = reg.paceOrder.intValue();
                final int pace = data.studentTerm.pace.intValue();
                final String track = data.studentTerm.paceTrack;
                final int msNbr = pace * 100 + paceOrder * 10 + 5;

                for (final RawMilestone ms : data.milestones) {
                    if (ms.paceTrack.equals(track) && "FE".equals(ms.msType) && ms.msNbr.intValue() == msNbr) {
                        finalDueDate = ms.msDate;
                    }
                }
                for (final RawMilestone ms : data.milestones) {
                    if (ms.paceTrack.equals(track) && "F1".equals(ms.msType) && ms.msNbr.intValue() == msNbr) {
                        if (finalDueDate == null || finalDueDate.isBefore(ms.msDate)) {
                            finalDueDate = ms.msDate;
                        }
                    }
                }
                if (finalDueDate != null) {
                    for (final RawStmilestone sms : data.studentMilestones) {
                        if (sms.paceTrack.equals(track) && "FE".equals(sms.msType) && sms.msNbr.intValue() == msNbr) {
                            finalDueDate = sms.msDate;
                            // Don't break - if there are multiple matching rows (which are sorted by deadline date),
                            // we want to take the latest one
                        }
                    }
                    for (final RawStmilestone sms : data.studentMilestones) {
                        if ((sms.paceTrack.equals(track) && "F1".equals(sms.msType) && sms.msNbr.intValue() == msNbr)
                                && finalDueDate.isBefore(sms.msDate)) {
                            finalDueDate = sms.msDate;
                            // Don't break - if there are multiple matching rows (which are sorted by deadline date),
                            // we want to take the latest one
                        }
                    }
                }
            }

            if (finalDueDate != null && finalDueDate.isBefore(LocalDate.now())) {
                blocked = true;
            }
        }

        final JPanel row = new JPanel(new BorderLayout());

        row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(0, 4, 2, 4)));

        switch (reg.course) {
            case RawRecordConstants.M117, RawRecordConstants.MATH117 -> row.setBackground(Skin.OFF_WHITE_GREEN);
            case RawRecordConstants.M118, RawRecordConstants.MATH118 -> row.setBackground(Skin.OFF_WHITE_CYAN);
            case RawRecordConstants.M124, RawRecordConstants.MATH124 -> row.setBackground(Skin.OFF_WHITE_MAGENTA);
            case RawRecordConstants.M125, RawRecordConstants.MATH125 -> row.setBackground(Skin.OFF_WHITE_BLUE);
            case RawRecordConstants.M126, RawRecordConstants.MATH126 -> row.setBackground(Skin.OFF_WHITE_RED);
            case null, default -> row.setBackground(Skin.OFF_WHITE_BROWN);
        }

        final StringBuilder label = new StringBuilder(50);
        label.append(nameForCourse(reg.course));

        if (!RawRecordConstants.M100T.equals(reg.course)) {
            label.append(", Section ");
            label.append(reg.sect);

            RawCsection csect = null;
            for (final RawCsection test : data.currentTermCourseSections) {
                if (test.course.equals(reg.course) && test.sect.equals(reg.sect)) {
                    csect = test;
                    break;
                }
            }

            if (csect == null) {
                label.append(" (CSECTION row not found!)");
            } else if (csect.instructor != null) {
                label.append(" (");
                label.append(csect.instructor);
                label.append(')');
            }

            if ("Y".equals(reg.iInProgress)) {
                label.append(" ** Incomplete from ");
                label.append(reg.iTermKey);
                label.append(" ** ");
            }

            if (blocked) {
                label.append(" [ *** BLOCKED *** - student must change registration to continue ]");
            } else if (reg.openStatus == null) {
                label.append(" [Not yet started]");
            } else if ("G".equals(reg.openStatus)) {
                label.append(" [Forfeit]");
            } else if ("Y".equals(reg.openStatus)) {
                if ("Y".equals(reg.completed)) {
                    label.append(" [Completed, Still Open]");
                } else {
                    label.append(" [In Progress]");
                }
            } else if ("N".equals(reg.openStatus)) {
                if ("Y".equals(reg.completed)) {
                    label.append(" [Completed, Closed]");
                } else {
                    label.append(" [Not Completed, Closed]");
                }
            }
        }

        final JLabel head = new JLabel(label.toString());
        head.setFont(Skin.BOLD_12_FONT);

        row.add(head, BorderLayout.PAGE_START);

        final boolean isPrecalc = RawRecordConstants.M117.equals(reg.course)
                || RawRecordConstants.M118.equals(reg.course)
                || RawRecordConstants.M124.equals(reg.course)
                || RawRecordConstants.M125.equals(reg.course)
                || RawRecordConstants.M126.equals(reg.course);

        if (isPrecalc) {
            final JPanel grid = new JPanel(new GridBagLayout());
            grid.setBackground(Skin.LIGHTEST);
            grid.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Skin.MEDIUM));

            final GridBagConstraints constraints = new GridBagConstraints();

            constraints.gridy = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;

            constraints.gridx = 0;
            grid.add(makeStepLabel("SR", 3), constraints);
            constraints.gridx = 1;
            grid.add(makeStepLabel("H1", 1), constraints);
            constraints.gridx = 2;
            grid.add(makeStepLabel("H2", 1), constraints);
            constraints.gridx = 3;
            grid.add(makeStepLabel("H3", 1), constraints);
            constraints.gridx = 4;
            grid.add(makeStepLabel("H4", 1), constraints);
            constraints.gridx = 5;
            grid.add(makeStepLabel("Rev. 1", 1), constraints);
            constraints.gridx = 6;
            grid.add(makeStepLabel("Unit 1", 3), constraints);
            constraints.gridx = 7;
            grid.add(makeStepLabel("H1", 1), constraints);
            constraints.gridx = 8;
            grid.add(makeStepLabel("H2", 1), constraints);
            constraints.gridx = 9;
            grid.add(makeStepLabel("H3", 1), constraints);
            constraints.gridx = 10;
            grid.add(makeStepLabel("H4", 1), constraints);
            constraints.gridx = 11;
            grid.add(makeStepLabel("Rev. 2", 1), constraints);
            constraints.gridx = 12;
            grid.add(makeStepLabel("Unit 2", 3), constraints);
            constraints.gridx = 13;
            grid.add(makeStepLabel("H1", 1), constraints);
            constraints.gridx = 14;
            grid.add(makeStepLabel("H2", 1), constraints);
            constraints.gridx = 15;
            grid.add(makeStepLabel("H3", 1), constraints);
            constraints.gridx = 16;
            grid.add(makeStepLabel("H4", 1), constraints);
            constraints.gridx = 17;
            grid.add(makeStepLabel("Rev. 3", 1), constraints);
            constraints.gridx = 18;
            grid.add(makeStepLabel("Unit 3", 3), constraints);
            constraints.gridx = 19;
            grid.add(makeStepLabel("H1", 1), constraints);
            constraints.gridx = 20;
            grid.add(makeStepLabel("H2", 1), constraints);
            constraints.gridx = 21;
            grid.add(makeStepLabel("H3", 1), constraints);
            constraints.gridx = 22;
            grid.add(makeStepLabel("H4", 1), constraints);
            constraints.gridx = 23;
            grid.add(makeStepLabel("Rev. 4", 1), constraints);
            constraints.gridx = 24;
            grid.add(makeStepLabel("Unit 4", 3), constraints);
            constraints.gridx = 25;
            grid.add(makeStepLabel("Final", 0), constraints);

            constraints.gridy = 1;

            constraints.gridx = 0;
            grid.add(makeStepStatus(getExamStatus(data, reg, 0, RawStexam.REVIEW_EXAM), 3),
                    constraints);
            constraints.gridx = 1;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 1, 1), 1), constraints);
            constraints.gridx = 2;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 1, 2), 1), constraints);
            constraints.gridx = 3;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 1, 3), 1), constraints);
            constraints.gridx = 4;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 1, 4), 1), constraints);
            constraints.gridx = 5;
            grid.add(makeStepStatus(getExamStatus(data, reg, 1, RawStexam.REVIEW_EXAM), 1),
                    constraints);
            constraints.gridx = 6;
            grid.add(makeStepStatus(getExamStatus(data, reg, 1, RawStexam.UNIT_EXAM), 3),
                    constraints);
            constraints.gridx = 7;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 2, 1), 1), constraints);
            constraints.gridx = 8;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 2, 2), 1), constraints);
            constraints.gridx = 9;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 2, 3), 1), constraints);
            constraints.gridx = 10;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 2, 4), 1), constraints);
            constraints.gridx = 11;
            grid.add(makeStepStatus(getExamStatus(data, reg, 2, RawStexam.REVIEW_EXAM), 1),
                    constraints);
            constraints.gridx = 12;
            grid.add(makeStepStatus(getExamStatus(data, reg, 2, RawStexam.UNIT_EXAM), 3),
                    constraints);
            constraints.gridx = 13;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 3, 1), 1), constraints);
            constraints.gridx = 14;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 3, 2), 1), constraints);
            constraints.gridx = 15;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 3, 3), 1), constraints);
            constraints.gridx = 16;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 3, 4), 1), constraints);
            constraints.gridx = 17;
            grid.add(makeStepStatus(getExamStatus(data, reg, 3, RawStexam.REVIEW_EXAM), 1),
                    constraints);
            constraints.gridx = 18;
            grid.add(makeStepStatus(getExamStatus(data, reg, 3, RawStexam.UNIT_EXAM), 3),
                    constraints);
            constraints.gridx = 19;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 4, 1), 1), constraints);
            constraints.gridx = 20;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 4, 2), 1), constraints);
            constraints.gridx = 21;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 4, 3), 1), constraints);
            constraints.gridx = 22;
            grid.add(makeStepStatus(getHomeworkStatus(data, reg, 4, 4), 1), constraints);
            constraints.gridx = 23;
            grid.add(makeStepStatus(getExamStatus(data, reg, 4, RawStexam.REVIEW_EXAM), 1),
                    constraints);
            constraints.gridx = 24;
            grid.add(makeStepStatus(getExamStatus(data, reg, 4, RawStexam.UNIT_EXAM), 3),
                    constraints);
            constraints.gridx = 25;
            grid.add(makeStepStatus(getExamStatus(data, reg, 5, RawStexam.FINAL_EXAM), 0),
                    constraints);

            row.add(grid, BorderLayout.CENTER);
        } else if (RawRecordConstants.M100T.equals(reg.course)) {
            final JPanel grid = new JPanel(new GridBagLayout());
            grid.setBackground(Skin.LIGHTEST);
            grid.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Skin.MEDIUM));

            final GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridy = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridx = 0;
            grid.add(makeStepLabel("Rev. 1", 1), gbc);
            gbc.gridx = 1;
            grid.add(makeStepLabel("Rev. 2", 1), gbc);
            gbc.gridx = 2;
            grid.add(makeStepLabel("Rev. 3", 1), gbc);
            gbc.gridx = 3;
            grid.add(makeStepLabel("Rev. 4", 1), gbc);
            gbc.gridx = 4;
            grid.add(makeStepLabel("ELM Exam", 0), gbc);

            gbc.gridy = 1;

            gbc.gridx = 0;
            grid.add(makeStepStatus(getExamStatus(data, reg, 1, RawStexam.REVIEW_EXAM), 1), gbc);
            gbc.gridx = 1;
            grid.add(makeStepStatus(getExamStatus(data, reg, 2, RawStexam.REVIEW_EXAM), 1), gbc);
            gbc.gridx = 2;
            grid.add(makeStepStatus(getExamStatus(data, reg, 3, RawStexam.REVIEW_EXAM), 1), gbc);
            gbc.gridx = 3;
            grid.add(makeStepStatus(getExamStatus(data, reg, 4, RawStexam.REVIEW_EXAM), 1), gbc);
            gbc.gridx = 4;
            grid.add(makeStepStatus(getExamStatus(data, reg, 4, RawStexam.UNIT_EXAM), 0), gbc);

            row.add(grid, BorderLayout.CENTER);

        } else {
            final JLabel errLbl = new JLabel("(Course not managed by this system)");
            row.add(errLbl, BorderLayout.PAGE_END);
        }

        return row;
    }

    /**
     * Creates a label for a single step within the course status bar.
     *
     * @param txt              the label text
     * @param borderRight      the width of border to add on the right
     * @return the label
     */
    private static JLabel makeStepLabel(final String txt, final int borderRight) {

        final JLabel lbl = new JLabel(txt);

        if ("Final".equals(txt) || txt.startsWith("Unit")) {
            lbl.setFont(Skin.BOLD_12_FONT);
            lbl.setOpaque(true);
            lbl.setBackground(Skin.LIGHT);
        } else {
            lbl.setFont(Skin.BODY_12_FONT);
        }

        if (borderRight > 0) {
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, borderRight, Skin.MEDIUM),
                    BorderFactory.createEmptyBorder(1, 3, 1, 3)));
        } else {
            lbl.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
        }

        return lbl;
    }

    /**
     * Gets the status of an exam in the course.
     *
     * @param data     the student data
     * @param reg      the registration data
     * @param unit     the unit number
     * @param examType the exam type
     * @return the status
     */
    private static StepStatus getExamStatus(final StudentData data, final RawStcourse reg, final int unit,
                                            final String examType) {

        // Find the due date
        LocalDate dueDate = null;

        if (data.studentTerm != null && reg.paceOrder != null) {
            final int paceOrder = reg.paceOrder.intValue();
            final int pace = data.studentTerm.pace.intValue();
            final String track = data.studentTerm.paceTrack;
            final int msNbr = pace * 100 + paceOrder * 10 + unit;

            if ("F".equals(examType)) {
                for (final RawMilestone ms : data.milestones) {
                    if (ms.paceTrack.equals(track) && "FE".equals(ms.msType) && ms.msNbr.intValue() == msNbr) {
                        dueDate = ms.msDate;
                    }
                }
                for (final RawStmilestone sms : data.studentMilestones) {
                    if (sms.paceTrack.equals(track) && "FE".equals(sms.msType) && sms.msNbr.intValue() == msNbr) {
                        dueDate = sms.msDate;
                        // Don't break - if there are multiple matching rows (which are sorted by deadline date),
                        // we want to take the latest one
                    }
                }
            } else if ("R".equals(examType)) {
                for (final RawMilestone ms : data.milestones) {
                    if (ms.paceTrack.equals(track) && "RE".equals(ms.msType) && ms.msNbr.intValue() == msNbr) {
                        dueDate = ms.msDate;
                    }
                }
                for (final RawStmilestone sms : data.studentMilestones) {
                    if (sms.paceTrack.equals(track) && "RE".equals(sms.msType) && sms.msNbr.intValue() == msNbr) {
                        dueDate = sms.msDate;
                        // Don't break - if there are multiple matching rows (which are sorted by deadline date),
                        // we want to take the latest one
                    }
                }
            }
        }

        boolean attempted = false;
        boolean passed = false;
        int maxScore = -1;
        LocalDate firstPassed = null;

        for (final RawStexam exam : data.studentExams) {
            if (exam.course.equals(reg.course) && exam.unit.intValue() == unit && exam.examType.equals(examType)) {
                attempted = true;
                if ("Y".equals(exam.passed)) {
                    passed = true;

                    if (firstPassed == null || exam.examDt.isBefore(firstPassed)) {
                        firstPassed = exam.examDt;
                    }
                }
                if (exam.examScore != null) {
                    maxScore = Math.max(maxScore, exam.examScore.intValue());
                }
            }
        }

        boolean late = false;
        final LocalDate today = LocalDate.now();
        if (dueDate != null && dueDate.isBefore(today)) {
            if (passed) {
                late = dueDate.isBefore(firstPassed);
            } else {
                late = true;
            }
        }

        final ECourseStepStatus status;

        if (passed) {
            status = late ? ECourseStepStatus.PASSED_LATE : ECourseStepStatus.PASSED_ON_TIME;
        } else if (attempted) {
            status = late ? ECourseStepStatus.NOT_YET_PASSED_LATE : ECourseStepStatus.NOT_YET_PASSED_ON_TIME;
        } else {
            status = late ? ECourseStepStatus.NOT_YET_ATTEMPTED_LATE : ECourseStepStatus.NOT_YET_ATTEMPTED_ON_TIME;
        }

        return new StepStatus(status, maxScore);
    }

    /**
     * Gets the status of a homework in the course.
     *
     * @param data      the student data
     * @param reg       the registration data
     * @param unit      the unit number
     * @param objective the objective number
     * @return the status
     */
    private static StepStatus getHomeworkStatus(final StudentData data, final RawStcourse reg, final int unit,
                                                final int objective) {

        boolean attempted = false;
        boolean passed = false;

        for (final RawSthomework hw : data.studentHomeworks) {
            if (hw.course.equals(reg.course) && hw.unit.intValue() == unit && hw.objective.intValue() == objective) {
                attempted = true;
                if ("Y".equals(hw.passed)) {
                    passed = true;
                }
            }
        }

        final ECourseStepStatus status = passed ? ECourseStepStatus.PASSED_ON_TIME
                : attempted ? ECourseStepStatus.NOT_YET_PASSED_ON_TIME : ECourseStepStatus.NOT_YET_ATTEMPTED_ON_TIME;

        return new StepStatus(status, -1);
    }

    /**
     * Creates a panel that shows the status of a step.
     *
     * @param status           the status
     * @param borderRight      the width of border to add on the right
     * @return the panel
     */
    private static JPanel makeStepStatus(final StepStatus status, final int borderRight) {

        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));

        final JLabel lbl;

        switch (status.status) {
            case NOT_YET_ATTEMPTED_ON_TIME:
                panel.setBackground(Skin.WHITE);
                lbl = new JLabel(CoreConstants.SPC);
                break;

            case NOT_YET_ATTEMPTED_LATE:
                panel.setBackground(Skin.LT_RED);
                lbl = new JLabel(CoreConstants.SPC);
                break;

            case NOT_YET_PASSED_ON_TIME:
                panel.setBackground(Skin.WHITE);
                if (status.score > 0) {
                    lbl = new JLabel("\u2610 " + status.score);
                } else {
                    lbl = new JLabel("\u2610");
                }
                lbl.setForeground(new Color(100, 100, 100));
                break;

            case NOT_YET_PASSED_LATE:
                panel.setBackground(Skin.LT_RED);
                if (status.score > 0) {
                    lbl = new JLabel("\u2610 " + status.score);
                } else {
                    lbl = new JLabel("\u2610");
                }
                lbl.setForeground(new Color(100, 100, 100));
                break;

            case PASSED_ON_TIME:
                panel.setBackground(Skin.WHITE);
                if (status.score > 0) {
                    lbl = new JLabel("\u2611 " + status.score);
                } else {
                    lbl = new JLabel("\u2611");
                }
                lbl.setForeground(new Color(0, 128, 0));
                break;

            case PASSED_LATE:
                panel.setBackground(Skin.LT_RED);
                if (status.score > 0) {
                    lbl = new JLabel("\u2611 " + status.score);
                } else {
                    lbl = new JLabel("\u2611");
                }
                lbl.setForeground(new Color(0, 128, 0));
                break;

            default:
                panel.setBackground(Skin.LT_RED);
                lbl = new JLabel("?");
                break;
        }
        lbl.setFont(Skin.SYMBOL_16_ONT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lbl, BorderLayout.CENTER);

        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, borderRight, Skin.MEDIUM));

        return panel;
    }

    /**
     * Gets the name associated with a course.
     *
     * @param course the course ID
     * @return the name; null if course ID is not that of a course
     */
    private static String nameForCourse(final String course) {

        String result = null;

        if (RawRecordConstants.M117.equals(course) || RawRecordConstants.MATH117.equals(course)) {
            result = "MATH 117";
        } else if (RawRecordConstants.M118.equals(course) || RawRecordConstants.MATH118.equals(course)) {
            result = "MATH 118";
        } else if (RawRecordConstants.M124.equals(course) || RawRecordConstants.MATH124.equals(course)) {
            result = "MATH 124";
        } else if (RawRecordConstants.M125.equals(course) || RawRecordConstants.MATH125.equals(course)) {
            result = "MATH 125";
        } else if (RawRecordConstants.M126.equals(course) || RawRecordConstants.MATH126.equals(course)) {
            result = "MATH 126";
        } else if ("M 127".equals(course)) {
            result = "MATH 127";
        } else if (RawRecordConstants.M100T.equals(course)) {
            result = "ELM Tutorial";
        }

        return result;
    }

    /** The status of a step in the course. */
    private static class StepStatus {

        /** The status. */
        final ECourseStepStatus status;

        /** The score. */
        final int score;

        /**
         * Constructs a new {@code StepStatus}.
         *
         * @param theStatus the status
         * @param theScore  the score
         */
        StepStatus(final ECourseStepStatus theStatus, final int theScore) {

            this.status = theStatus;
            this.score = theScore;
        }
    }
}
