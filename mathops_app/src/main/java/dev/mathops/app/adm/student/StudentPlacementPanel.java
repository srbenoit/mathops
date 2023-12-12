package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.db.old.rawrecord.RawStmpe;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.io.Serial;

/**
 * A panel that shows student placement status.
 */
/* default */ class StudentPlacementPanel extends AdminPanelBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7889549799220741800L;

    /** The "Placement Attempts" header. */
    private final JLabel placementHeader;

    /** The "Challenge Attempts" header. */
    private final JLabel challengeHeader;

    /** A table that shows all placement attempts. */
    private final JTablePlacementAttempts placementAttemptsTable;

    /** Scroll pane for placement attempts. */
    private final JScrollPane placementScroll;

    /** A table that shows all challenge attempts. */
    private final JTableChallengeAttempts challengeAttemptsTable;

    /** Scroll pane for challenge attempts. */
    private final JScrollPane challengeScroll;

    /** A table that shows all earned placement results. */
    private final JTableEarnedPlacement earedPlacementTable;

    /** Scroll pane for earned placement results. */
    private final JScrollPane earnedScroll;

    /** A table that shows all tutorial-based placement outcomes. */
    private final JTableTutorials tutorialsTable;

    /** Scroll pane for earned placement results. */
    private final JScrollPane tutorialsScroll;

    /** A table that shows all transfer credit. */
    private final JTableTransferCredit transferTable;

    /** Scroll pane for transfer credit results. */
    private final JScrollPane transferScroll;

    /** An error message. */
    private final JLabel error;

    /**
     * Constructs a new {@code StudentPlacementPanel}.
     *
     */
    StudentPlacementPanel() {

        super();
        setBackground(Skin.WHITE);

        final JPanel north = makeOffWhitePanel(new BorderLayout(5, 5));
        north.setBackground(Skin.WHITE);
        north.setLayout(new BoxLayout(north, BoxLayout.PAGE_AXIS));
        add(north, BorderLayout.NORTH);

        final JPanel pane1 = makeOffWhitePanel(new BorderLayout(0, 0));
        pane1.setBackground(Skin.WHITE);
        pane1.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        north.add(pane1);

        final JPanel pane2 = makeOffWhitePanel(new BorderLayout(0, 0));
        pane2.setBackground(Skin.WHITE);
        pane2.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        north.add(pane2);

        final JPanel pane3 = makeOffWhitePanel(new BorderLayout(0, 0));
        pane3.setBackground(Skin.WHITE);
        pane3.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        north.add(pane3);

        final JPanel pane4 = makeOffWhitePanel(new BorderLayout(0, 0));
        pane4.setBackground(Skin.WHITE);
        pane4.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        north.add(pane4);

        final JPanel pane5 = makeOffWhitePanel(new BorderLayout(0, 0));
        pane5.setBackground(Skin.WHITE);
        north.add(pane5);

        this.placementHeader = makeHeader("Placement Tool Attempts", false);
        pane1.add(this.placementHeader, BorderLayout.NORTH);

        this.placementAttemptsTable = new JTablePlacementAttempts();
        this.placementAttemptsTable.setFillsViewportHeight(true);

        this.placementScroll = new JScrollPane(this.placementAttemptsTable);
        this.placementScroll.setPreferredSize(
                this.placementAttemptsTable.getPreferredScrollSize(this.placementScroll, 3));

        pane1.add(this.placementScroll, BorderLayout.WEST);

        this.challengeHeader = makeHeader("Challenge Exam Attempts", true);
        pane2.add(this.challengeHeader, BorderLayout.NORTH);

        this.challengeAttemptsTable = new JTableChallengeAttempts();
        this.challengeAttemptsTable.setFillsViewportHeight(true);

        this.challengeScroll = new JScrollPane(this.challengeAttemptsTable);
        this.challengeScroll.setPreferredSize(
                this.challengeAttemptsTable.getPreferredScrollSize(this.challengeScroll, 3));

        pane2.add(this.challengeScroll, BorderLayout.WEST);

        pane3.add(makeHeader("Earned Placement Results", true),
                BorderLayout.NORTH);

        this.earedPlacementTable = new JTableEarnedPlacement();
        this.earedPlacementTable.setFillsViewportHeight(true);

        this.earnedScroll = new JScrollPane(this.earedPlacementTable);
        this.earnedScroll.setPreferredSize(
                this.earedPlacementTable.getPreferredScrollSize(this.challengeScroll, 3));

        pane3.add(this.earnedScroll, BorderLayout.WEST);

        pane4.add(makeHeader("Placement from Tutorials", true),
                BorderLayout.NORTH);

        this.tutorialsTable = new JTableTutorials();
        this.tutorialsTable.setFillsViewportHeight(true);

        this.tutorialsScroll = new JScrollPane(this.tutorialsTable);
        this.tutorialsScroll
                .setPreferredSize(this.tutorialsTable.getPreferredScrollSize(this.tutorialsScroll, 3));

        pane4.add(this.tutorialsScroll, BorderLayout.WEST);

        pane5.add(makeHeader("Transfer Credit", true), BorderLayout.NORTH);

        this.transferTable = new JTableTransferCredit();
        this.transferTable.setFillsViewportHeight(true);

        this.transferScroll = new JScrollPane(this.transferTable);
        this.transferScroll
                .setPreferredSize(this.transferTable.getPreferredScrollSize(this.transferScroll, 3));

        pane5.add(this.transferScroll, BorderLayout.WEST);

        this.error = makeError();
        add(this.error, BorderLayout.SOUTH);
    }

    /**
     * Sets the selected student data.
     *
     * @param data the selected student data
     */
    public void setSelectedStudent(final StudentData data) {

        this.error.setText(CoreConstants.SPC);
        clearDisplay();

        if (data != null) {
            populateDisplay(data);

            this.placementScroll.setPreferredSize(
                    this.placementAttemptsTable.getPreferredScrollSize(this.placementScroll, 2));
            this.challengeScroll.setPreferredSize(
                    this.challengeAttemptsTable.getPreferredScrollSize(this.challengeScroll, 2));
            this.earnedScroll.setPreferredSize(
                    this.earedPlacementTable.getPreferredScrollSize(this.earnedScroll, 2));
            this.tutorialsScroll.setPreferredSize(
                    this.tutorialsTable.getPreferredScrollSize(this.tutorialsScroll, 2));
            this.transferScroll.setPreferredSize(
                    this.transferTable.getPreferredScrollSize(this.transferScroll, 2));
        }
    }

    /**
     * Clears all displayed fields.
     */
    private void clearDisplay() {

        this.placementHeader.setText("Placement Tool Attempts");
        this.challengeHeader.setText("Challenge Exam Attempts");

        this.placementAttemptsTable.clear();
        this.challengeAttemptsTable.clear();
        this.earedPlacementTable.clear();
        this.tutorialsTable.clear();
        this.transferTable.clear();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        int remain = 2;
        for (final RawStmpe rec : data.studentPlacementAttempts) {
            if (remain > 0 && !"POOOO".equals(rec.version)) {
                --remain;
            }
        }

        if (!data.studentPlacementAttempts.isEmpty()) {
            if (data.placementFee == null || data.placementFee.billDt == null) {
                this.placementHeader.setText(
                        "Placement Tool Attempts (not yet billed, " + remain + " attempts remain)");
            } else {
                this.placementHeader.setText("Placement Tool Attempts (billed on "
                        + TemporalUtils.FMT_MDY.format(data.placementFee.billDt) + ", " + remain
                        + " attempts remain)");
            }
        }

        if (!data.studentChallengeAttempts.isEmpty()) {
            if (data.challengeFees == null || data.challengeFees.isEmpty()) {
                this.challengeHeader.setText("Challenge Exam Attempts (not yet billed)");
            } else {
                this.challengeHeader.setText(
                        "Challenge Exam Attempts (" + data.challengeFees.size() + " attempts billed)");
            }
        }

        this.placementAttemptsTable.addData(data.studentPlacementAttempts,
                data.studentPlacementAttempts.size() + remain);
        this.challengeAttemptsTable.addData(data.studentChallengeAttempts, 2);
        this.earedPlacementTable.addData(data.studentPlacementCredit, 5);
        this.tutorialsTable.addData(data.studentExams, 2);
        this.transferTable.addData(data.studentTransferCredit, 2);

    }
}
