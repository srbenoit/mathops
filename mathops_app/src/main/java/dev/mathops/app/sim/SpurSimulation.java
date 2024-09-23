package dev.mathops.app.sim;

import com.formdev.flatlaf.FlatDarkLaf;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A simulation application to model registrations and enrollment on the Spur (or any) campus.
 */
public final class SpurSimulation implements Runnable, ActionListener {

    /** An action command. */
    private static final String MANAGE_CLASSROOM_PROFILES_CMD = "A";

    /** An action command. */
    private static final String MANAGE_COURSE_PROFILES_CMD = "B";

    /** An action command. */
    private static final String MANAGE_STUDENT_PROFILES_CMD = "C";

    /** An action command. */
    private static final String MANAGE_SCORING_PROFILES_CMD = "D";

    /** An action command. */
    private static final String RUN_SIM_CMD = "GO";

    /** A dropdown from which to choose a profile for classroom/lab space setup. */
    private JComboBox<String> classroomAndLabProfiles;

    /** A dropdown from which to choose a profile for class offerings. */
    private JComboBox<String> classOfferingProfiles;

    /** A dropdown from which to choose a profile for the student population. */
    private JComboBox<String> studentPopulationProfiles;

    /** A dropdown from which to choose a profile for generating quality scores for a registration outcome. */
    private JComboBox<String> qualityScoringProfile;

    /** A progress bar. */
    private JProgressBar progressBar;

    /** A progress status display. */
    private JLabel progressStatus;

    /**
     * Private constructor to prevent instantiation.
     */
    private SpurSimulation() {

        // TODO: Load configuration settings from XML files.
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        final JFrame frame = new JFrame("Spur Registration and Class Schedule Simulation");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new StackedBorderLayout(10, 10));
        content.setPreferredSize(new Dimension(1024, 768));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.setContentPane(content);

        final JPanel north = new JPanel(new StackedBorderLayout(10, 10));
        content.add(north, StackedBorderLayout.NORTH);

        final JPanel northwest = new JPanel(new StackedBorderLayout());
        northwest.setBorder(BorderFactory.createEtchedBorder());
        north.add(northwest, StackedBorderLayout.WEST);

        final JLabel[] nwLabels = new JLabel[4];
        nwLabels[0] = new JLabel("Classroom and lab space configuration:");
        nwLabels[1] = new JLabel("Course offerings configuration:");
        nwLabels[2] = new JLabel("Student population settings:");
        nwLabels[3] = new JLabel("Quality scoring configuration:");
        UIUtilities.makeLabelsSameSizeRightAligned(nwLabels);

        final JButton manageClassroom = new JButton("Manage...");
        manageClassroom.setActionCommand(MANAGE_CLASSROOM_PROFILES_CMD);
        manageClassroom.addActionListener(this);

        final JButton manageCourses = new JButton("Manage...");
        manageCourses.setActionCommand(MANAGE_COURSE_PROFILES_CMD);
        manageCourses.addActionListener(this);

        final JButton manageStudents = new JButton("Manage...");
        manageStudents.setActionCommand(MANAGE_STUDENT_PROFILES_CMD);
        manageStudents.addActionListener(this);

        final JButton manageScoring = new JButton("Manage...");
        manageScoring.setActionCommand(MANAGE_SCORING_PROFILES_CMD);
        manageScoring.addActionListener(this);

        this.classroomAndLabProfiles = new JComboBox<>();
        final Dimension pref = this.classroomAndLabProfiles.getPreferredSize();
        final Dimension newPref = new Dimension(350, pref.height);
        this.classroomAndLabProfiles.setPreferredSize(newPref);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow1.add(nwLabels[0]);
        flow1.add(this.classroomAndLabProfiles);
        flow1.add(manageClassroom);
        northwest.add(flow1, StackedBorderLayout.NORTH);

        this.classOfferingProfiles = new JComboBox<>();
        this.classOfferingProfiles.setPreferredSize(newPref);
        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow2.add(nwLabels[1]);
        flow2.add(this.classOfferingProfiles);
        flow2.add(manageCourses);
        northwest.add(flow2, StackedBorderLayout.NORTH);

        this.studentPopulationProfiles = new JComboBox<>();
        this.studentPopulationProfiles.setPreferredSize(newPref);
        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow3.add(nwLabels[2]);
        flow3.add(this.studentPopulationProfiles);
        flow3.add(manageStudents);
        northwest.add(flow3, StackedBorderLayout.NORTH);

        this.qualityScoringProfile = new JComboBox<>();
        this.qualityScoringProfile.setPreferredSize(newPref);
        final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow4.add(nwLabels[3]);
        flow4.add(this.qualityScoringProfile);
        flow4.add(manageScoring);
        northwest.add(flow4, StackedBorderLayout.NORTH);

        final JPanel northcenter = new JPanel(new StackedBorderLayout());
        northcenter.setBorder(BorderFactory.createEtchedBorder());
        north.add(northcenter, StackedBorderLayout.WEST);

        final JButton runSim = new JButton("Run Simulation...");
        runSim.setActionCommand(RUN_SIM_CMD);
        runSim.addActionListener(this);
        final JPanel flow5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow5.add(runSim);
        northcenter.add(flow5, StackedBorderLayout.NORTH);

        this.progressStatus = new JLabel(CoreConstants.SPC);
        content.add(this.progressStatus, StackedBorderLayout.SOUTH);
        this.progressBar = new JProgressBar(0, 1000);
        content.add(this.progressBar, StackedBorderLayout.SOUTH);

        final JPanel display = new JPanel();
        display.setBorder(BorderFactory.createLoweredBevelBorder());
        content.add(display, StackedBorderLayout.CENTER);

        UIUtilities.packAndCenter(frame);
        frame.setVisible(true);
    }

    /**
     * Called when a button is activated.
     *
     * @param evt the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {

        final String cmd = evt.getActionCommand();

        if (RUN_SIM_CMD.equals(cmd)) {

        } else if (MANAGE_CLASSROOM_PROFILES_CMD.equals(cmd)) {

        } else if (MANAGE_COURSE_PROFILES_CMD.equals(cmd)) {

        } else if (MANAGE_STUDENT_PROFILES_CMD.equals(cmd)) {

        } else if (MANAGE_SCORING_PROFILES_CMD.equals(cmd)) {

        }
    }

    /**
     * Main method to run the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(new SpurSimulation());
    }
}

