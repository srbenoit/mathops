package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.challenge.ChallengeExamLogic;
import dev.mathops.db.schema.legacy.impl.RawClientPcLogic;
import dev.mathops.db.schema.legacy.impl.RawPaceAppealsLogic;
import dev.mathops.db.schema.legacy.impl.RawStudentLogic;
import dev.mathops.db.schema.legacy.rec.RawClientPc;
import dev.mathops.db.schema.legacy.rec.RawCunit;
import dev.mathops.db.schema.legacy.rec.RawPaceAppeals;
import dev.mathops.db.schema.legacy.rec.RawStudent;
import dev.mathops.db.schema.main.rec.TermRec;
import dev.mathops.db.schema.RawRecordConstants;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A modal pop-up dialog opened when the user has picked an exam to issue from the office. Inputs are the student ID,
 * the exam ID, and the "check eligibility" flag.
 *
 * <p>
 * This dialog asks whether to issue in the regular center or in quiet testing, reminds the user of any accommodations
 * the student has, and allows an office calculator be issued as well.
 */
final class StartExamDialog extends JDialog implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -8435645118378278131L;

    /** An action command. */
    private static final String CMD_ISSUE = "ISSUE";

    /** An action command. */
    private static final String CMD_CANCEL = "CANCEL";

    /** The data cache. */
    private final Cache cache;

    /** The student ID. */
    private final String studentId;

    /** The course ID. */
    private final String courseId;

    /** The unit. */
    private final int unit;

    /** The exam ID. */
    private final String examId;

    /** The check eligibility flag. */
    private final boolean checkEligibility;

    /** The "issue" button. */
    private final JButton issue;

    /** Checkbox to issue in quiet testing. */
    private final JCheckBox quietTesting;

    /**
     * Constructs a new {@code StartExamDialog}.
     *
     * @param theCache            the data cache
     * @param frame               the owning frame
     * @param theStudentId        the student ID
     * @param theCourseId         the course ID
     * @param theUnit             the unit
     * @param theExamId           the exam ID
     * @param theCheckEligibility true if the testing station should check eligibility; false if not
     */
    StartExamDialog(final Cache theCache, final Frame frame, final String theStudentId, final String theCourseId,
                    final int theUnit, final String theExamId, final boolean theCheckEligibility) {

        super(frame, "Issue Exam", ModalityType.APPLICATION_MODAL);

        this.cache = theCache;
        this.studentId = theStudentId;
        this.courseId = theCourseId;
        this.unit = theUnit;
        this.examId = theExamId;
        this.checkEligibility = theCheckEligibility;

        this.issue = new JButton("Issue Exam");
        this.quietTesting = new JCheckBox("Issue in Quiet Testing area");

        final JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Skin.WHITE);
        setContentPane(content);
        content.setPreferredSize(new Dimension(500, 250));

        content.add(makeHeader(theCourseId, theUnit), BorderLayout.NORTH);
        final JPanel middle = new JPanel(new BorderLayout());
        middle.setBackground(Skin.WHITE);
        content.add(middle, BorderLayout.CENTER);
        middle.add(makeCenter(), BorderLayout.NORTH);
        content.add(makeButtonBar(), BorderLayout.SOUTH);

        pack();

        final Rectangle outer = frame.getBounds();
        final Dimension size = getSize();
        setLocation(outer.x + (outer.width - size.width) / 2, outer.y + (outer.height - size.height) / 2);
    }

    /**
     * Creates a header panel that shows the name of the exam being started.
     *
     * @param course the course
     * @param unit   the unit
     * @return the panel
     */
    private static JPanel makeHeader(final String course, final int unit) {

        final JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        header.setBackground(Skin.LIGHTEST);
        final MatteBorder border = BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM);
        header.setBorder(border);

        final JLabel lbl = new JLabel("Issue");
        lbl.setFont(Skin.BIG_HEADER_18_FONT);
        lbl.setForeground(Skin.LABEL_COLOR);
        header.add(lbl);

        final StringBuilder sb = new StringBuilder(50);

        switch (course) {
            case RawRecordConstants.M117 -> {
                sb.append("MATH 117 Unit ");
                sb.append(unit);
                sb.append(" Exam");
            }
            case RawRecordConstants.M118 -> {
                sb.append("MATH 118 Unit ");
                sb.append(unit);
                sb.append(" Exam");
            }
            case RawRecordConstants.M124 -> {
                sb.append("MATH 124 Unit ");
                sb.append(unit);
                sb.append(" Exam");
            }
            case RawRecordConstants.M125 -> {
                sb.append("MATH 125 Unit ");
                sb.append(unit);
                sb.append(" Exam");
            }
            case RawRecordConstants.M126 -> {
                sb.append("MATH 126 Unit ");
                sb.append(unit);
                sb.append(" Exam");
            }
            case ChallengeExamLogic.M117_CHALLENGE_EXAM_ID -> sb.append("MATH 117 Challenge Exam");
            case ChallengeExamLogic.M118_CHALLENGE_EXAM_ID -> sb.append("MATH 118 Challenge Exam");
            case ChallengeExamLogic.M124_CHALLENGE_EXAM_ID -> sb.append("MATH 124 Challenge Exam");
            case ChallengeExamLogic.M125_CHALLENGE_EXAM_ID -> sb.append("MATH 125 Challenge Exam");
            case ChallengeExamLogic.M126_CHALLENGE_EXAM_ID -> sb.append("MATH 126 Challenge Exam");
            case RawRecordConstants.M100T -> sb.append("ELM Exam");
            case RawRecordConstants.M1170 -> sb.append("Algebra I (117) Tutorial Exam");
            case RawRecordConstants.M1180 -> sb.append("Algebra II (118) Tutorial Exam");
            case RawRecordConstants.M1240 -> sb.append("Functions (124) Tutorial Exam");
            case RawRecordConstants.M1250 -> sb.append("Trig I (125) Tutorial Exam");
            case RawRecordConstants.M1260 -> sb.append("Trig II (126) Tutorial Exam");
            case RawRecordConstants.M100U -> sb.append("User's Exam");
            case RawRecordConstants.M100P -> sb.append("Math Placement Tool");
            case null, default -> {
                sb.append(course);
                sb.append(" Unit ");
                sb.append(unit);
            }
        }

        final JLabel lbl2 = new JLabel(sb.toString());
        lbl2.setFont(Skin.BIG_HEADER_18_FONT);
        lbl2.setForeground(Skin.LABEL_COLOR3);
        header.add(lbl2);

        return header;
    }

    /**
     * Creates the center panel, whose contents may depend on the exam.
     *
     * <p>
     * The top of the panel will display any accommodations the student has. The center of the panel will allow the
     * issuance of a calculator with the exam. The bottom panel will show the time limit for the exam (after any
     * time-limit factor adjustment), and the time remaining before the testing center closes, and will show a warning
     * if the student does not have full time available.
     *
     * @return the panel
     */
    private JPanel makeCenter() {

        final JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Skin.WHITE);
        center.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        List<RawPaceAppeals> appeals;
        try {
            appeals = RawPaceAppealsLogic.queryByStudent(this.cache, this.studentId);

            if (!appeals.isEmpty()) {
                appeals.removeIf(rec -> "Y".equals(rec.reliefGiven));
            }
        } catch (final SQLException ex) {
            Log.warning("Unable to query for accommodations.", ex);
            appeals = new ArrayList<>(0);
        }

        if (!appeals.isEmpty()) {
            final JPanel appealPane = new JPanel(new BorderLayout());
            appealPane.setBackground(Skin.WHITE);

            final JLabel appealLbl = new JLabel("Student Accommodations:");
            appealLbl.setFont(Skin.SUB_HEADER_16_FONT);
            appealLbl.setForeground(Skin.LABEL_COLOR2);
            appealLbl.setHorizontalAlignment(SwingConstants.LEFT);
            appealPane.add(appealLbl, BorderLayout.NORTH);

            final StringBuilder msg = new StringBuilder(1000);

            for (final RawPaceAppeals appeal : appeals) {

                msg.append("\u2022 ");
                msg.append(appeal.circumstances);
                if (appeal.comment != null) {
                    msg.append(' ');
                    msg.append(appeal.comment);
                }
                msg.append(CoreConstants.CRLF);
            }

            final JTextArea area = new JTextArea(msg.toString());
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
            area.setBackground(Skin.LIGHTER_YELLOW);

            appealPane.add(new JScrollPane(area), BorderLayout.CENTER);

            center.add(appealPane, BorderLayout.NORTH);
        }

        // Bottom: Time limit information (ignore for User's exam)
        if (!RawRecordConstants.M100U.equals(this.courseId)) {

            final SystemData systemData = this.cache.getSystemData();

            int timelimit = 0;
            try {
                final TermRec activeTerm = systemData.getActiveTerm();
                final Integer unitObj = Integer.valueOf(this.unit);
                final RawCunit cunit = systemData.getCourseUnit(this.courseId, unitObj, activeTerm.term);

                if (cunit != null && cunit.unitTimelimit != null) {
                    timelimit = cunit.unitTimelimit.intValue();

                    final RawStudent stu = RawStudentLogic.query(this.cache, this.studentId, false);
                    if (stu != null && stu.timelimitFactor != null) {
                        timelimit = Math.round((float) timelimit * stu.timelimitFactor.floatValue());
                    }
                }

            } catch (final SQLException ex) {
                Log.warning("Unable to query for course unit.", ex);
            }

            if (timelimit > 0) {
                final LocalDateTime now = LocalDateTime.now();
                final LocalDateTime end = now.plusMinutes(timelimit + 2);

                final JPanel timerPane = new JPanel(new BorderLayout());
                timerPane.setBackground(Skin.WHITE);
                timerPane.setBorder(BorderFactory.createEmptyBorder(6, 0, 4, 0));

                final JPanel allowedFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
                allowedFlow.setBackground(Skin.WHITE);
                timerPane.add(allowedFlow, BorderLayout.NORTH);

                final JLabel timerLbl = new JLabel("Time Allowed:");
                timerLbl.setFont(Skin.MEDIUM_HEADER_15_FONT);
                timerLbl.setForeground(Skin.LABEL_COLOR2);
                allowedFlow.add(timerLbl);

                final StringBuilder sb1 = new StringBuilder(50);
                if (timelimit > 120) {
                    final int numHours = timelimit / 60;
                    sb1.append(numHours);
                    timelimit -= numHours * 60;
                    if (timelimit == 0) {
                        sb1.append(" Hours");
                    } else {
                        sb1.append(" Hours, ");
                        sb1.append(timelimit);
                        sb1.append(" Minutes");
                    }
                }

                final JLabel allowedLbl = new JLabel(sb1.toString());
                allowedLbl.setFont(Skin.BIG_BUTTON_16_FONT);
                allowedLbl.setForeground(Skin.LABEL_COLOR3);
                allowedFlow.add(allowedLbl);

                final JPanel endTimeFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
                endTimeFlow.setBackground(Skin.WHITE);
                timerPane.add(endTimeFlow, BorderLayout.SOUTH);

                final JLabel endTimeLbl = new JLabel("Approximate End Time:");
                endTimeLbl.setFont(Skin.MEDIUM_HEADER_15_FONT);
                endTimeLbl.setForeground(Skin.LABEL_COLOR2);
                endTimeFlow.add(endTimeLbl);

                final JLabel endLbl = new JLabel(TemporalUtils.FMT_HM_A.format(end));
                endLbl.setFont(Skin.BIG_BUTTON_16_FONT);
                endLbl.setForeground(Skin.LABEL_COLOR3);
                endTimeFlow.add(endLbl);

                center.add(timerPane, BorderLayout.SOUTH);
            }
        }

        // Pad to allow a calculator to be issued

        final JPanel lendPane = new JPanel(new BorderLayout(8, 8));
        lendPane.setBackground(Skin.WHITE);
        lendPane.setBorder(BorderFactory.createEmptyBorder(16, 10, 10, 16));
        center.add(lendPane, BorderLayout.CENTER);

        this.quietTesting.setFont(Skin.BIG_BUTTON_16_FONT);
        lendPane.add(this.quietTesting, BorderLayout.NORTH);

        return center;
    }

    /**
     * Creates a panel with [Cancel] and [Issue] buttons.
     *
     * @return the panel
     */
    private JPanel makeButtonBar() {

        final JPanel result = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));

        result.setBackground(Skin.LIGHTEST);
        final MatteBorder matteBorder = BorderFactory.createMatteBorder(1, 0, 0, 0, Skin.MEDIUM);
        result.setBorder(matteBorder);

        final JButton cancel = new JButton("Cancel");
        cancel.setFont(Skin.BIG_BUTTON_16_FONT);
        cancel.setActionCommand(CMD_CANCEL);
        cancel.addActionListener(this);

        this.issue.setFont(Skin.BIG_BUTTON_16_FONT);
        this.issue.setActionCommand(CMD_ISSUE);
        this.issue.addActionListener(this);

        result.add(cancel);
        result.add(this.issue);

        return result;
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action command
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CMD_ISSUE.equals(cmd)) {
            // Identify testing center
            final String tcId = this.quietTesting.isSelected() ? "4" : "1";

            final SystemData systemData = this.cache.getSystemData();

            // Select a computer
            RawClientPc selected = null;
            List<RawClientPc> pcs;
            try {
                pcs = systemData.getClientPcsByTestingCenter(tcId);
            } catch (final SQLException ex) {
                Log.warning("Failed to query available PCs", ex);
                pcs = new ArrayList<>(0);
            }
            for (final RawClientPc pc : pcs) {
                if (Integer.valueOf(4).equals(pc.currentStatus)) {
                    selected = pc;
                    break;
                }
            }

            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Unable to find an open computer.");
            } else {
                try {
                    final Integer unitObj = Integer.valueOf(this.unit);
                    if (RawClientPcLogic.updateAllCurrent(this.cache, selected.computerId,
                            this.checkEligibility ? RawClientPc.STATUS_AWAIT_STUDENT : RawClientPc.STATUS_LOGIN_NOCHECK,
                            this.studentId, this.courseId, unitObj, this.examId)) {

                        setVisible(false);
                        dispose();

                        final String[] messages = new String[2];
                        messages[0] = "Exam was issued to station " + selected.stationNbr;
                        messages[1] = "Please have student log in right away.";
                        JOptionPane.showMessageDialog(this, messages);
                    } else {
                        JOptionPane.showMessageDialog(this, "Unable to issue exam.");
                    }
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    JOptionPane.showMessageDialog(this, "Database error issuing exam.");
                }
            }
        } else if (CMD_CANCEL.equals(cmd)) {
            setVisible(false);
            dispose();
        }
    }
}
