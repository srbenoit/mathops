package dev.mathops.app.sim;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.app.sim.rooms.RoomSet;
import dev.mathops.app.sim.rooms.RoomSetsDlg;
import dev.mathops.app.sim.rooms.RoomSetsListModel;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * A registration simulation designed to determine, for a given set of classrooms and offered courses, and a specified
 * set of student preferences for courses, how many students can be accommodated.
 *
 * <p>
 * A registration simulation begins with a variable-size population of students, grouped by preferences regarding
 * classes they want to take (for example, 10% of students have preference set 1, 8% have preference set 2, etc.), and a
 * list of offered classes.  It first performs a registration cycle to determine the number of seats of each course that
 * should be offered to meet projected demand.
 *
 * <p>
 * The simulation next considers the set of classrooms and hour blocks available, and the number of classroom blocks
 * each class (or its lab) needed.  It determines possible section sizes and classroom assignments and tries to optimize
 * classroom capacity usage and minimize needed sections.  During this phase, the size of the population is increased
 * until classroom capacity is exceeded, giving an absolute upper bound on student population.  A "realistic" student
 * population size is then selected as some percentage of this absolute bound.
 *
 * <p>
 * Finally, it simulates the registration process some number of times to get lists of student schedules. Then for each
 * set of schedules, and each possible ordering of hour blocks within days, it computes the "best" time for instructors
 * to hold office hours (when the instructor is not teaching, and the maximum number of students in their courses are
 * on-campus but not in class, and a "desirability" score based on (1) how many days a week students need to travel to
 * campus, (2) how many hours they need to spend on campus each day to take classes, (3) how many days a week
 * instructors need to travel to campus, (4) how many hours instructors need to spend on campus each day, and (5) how
 * many instructors are needed.
 *
 * <p>
 * At the end of the process, the simulation emits the number of sections of each course needed, room assignments, and
 * the two or three "best" schedule layouts (those with the highest desirability scores).
 *
 * <p>
 * Each semester, the group preference matrices can be updated to refine estimations of demand, and could even include
 * statistical parameters like mean and standard deviation to support stochastic modeling of student registration
 * choices.
 */
public final class SpurSimulation extends WindowAdapter implements Runnable, ActionListener {

    /** An accent color. */
    public static final Color ACCENT_COLOR = new Color(0, 140, 200);

    /** An action command. */
    private static final String MANAGE_CLASSROOM_PROFILES_CMD = "A";

    /** An action command. */
    private static final String MANAGE_SEMESTER_PROFILES_CMD = "B";

    /** An action command. */
    private static final String MANAGE_COURSE_PROFILES_CMD = "C";

    /** An action command. */
    private static final String MANAGE_STUDENT_PROFILES_CMD = "D";

    /** An action command. */
    private static final String MANAGE_SCORING_PROFILES_CMD = "E";

    /** An action command. */
    private static final String RUN_SIM_CMD = "GO";

    /** The simulation data object. */
    private final SpurSimulationData data;

    /** The frame. */
    private JFrame frame;

    /** A dropdown from which to choose a room set to use for a simulation. */
    private JComboBox<RoomSet> roomSetChooser;

    /** A dropdown from which to choose a profile for semester schedule. */
    private JComboBox<String> semesterScheduleProfiles;

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

    /** A dialog to manage classroom spaces. */
    private RoomSetsDlg classroomDialog = null;

    /**
     * Private constructor to prevent instantiation.
     *
     * @param dataDir the directory in which configuration data is stored
     */
    private SpurSimulation(final File dataDir) {

        this.data = new SpurSimulationData(dataDir);
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        this.frame = new JFrame("Spur Registration and Class Schedule Simulation");
        this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new StackedBorderLayout(10, 10));
        content.setPreferredSize(new Dimension(1024, 768));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.frame.setContentPane(content);

        final JPanel north = new JPanel(new StackedBorderLayout(10, 10));
        content.add(north, StackedBorderLayout.NORTH);

        final JPanel northwest = new JPanel(new StackedBorderLayout());
        northwest.setBorder(BorderFactory.createEtchedBorder());
        north.add(northwest, StackedBorderLayout.WEST);

        final JLabel[] nwLabels = new JLabel[5];
        nwLabels[0] = new JLabel("Classroom space configuration:");
        nwLabels[1] = new JLabel("Semester schedule configuration:");
        nwLabels[2] = new JLabel("Course offerings configuration:");
        nwLabels[3] = new JLabel("Student population settings:");
        nwLabels[4] = new JLabel("Quality scoring configuration:");
        UIUtilities.makeLabelsSameSizeRightAligned(nwLabels);

        final JButton manageClassroom = new JButton("Manage...");
        manageClassroom.setActionCommand(MANAGE_CLASSROOM_PROFILES_CMD);
        manageClassroom.addActionListener(this);

        final JButton manageSemester = new JButton("Manage...");
        manageSemester.setActionCommand(MANAGE_SEMESTER_PROFILES_CMD);
        manageSemester.addActionListener(this);

        final JButton manageCourses = new JButton("Manage...");
        manageCourses.setActionCommand(MANAGE_COURSE_PROFILES_CMD);
        manageCourses.addActionListener(this);

        final JButton manageStudents = new JButton("Manage...");
        manageStudents.setActionCommand(MANAGE_STUDENT_PROFILES_CMD);
        manageStudents.addActionListener(this);

        final JButton manageScoring = new JButton("Manage...");
        manageScoring.setActionCommand(MANAGE_SCORING_PROFILES_CMD);
        manageScoring.addActionListener(this);

        final RoomSetsListModel roomSetListModel = this.data.getRoomSetListModel();
        this.roomSetChooser = new JComboBox<>(roomSetListModel);
        final Dimension pref = this.roomSetChooser.getPreferredSize();
        final Dimension newPref = new Dimension(350, pref.height);
        this.roomSetChooser.setPreferredSize(newPref);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow1.add(nwLabels[0]);
        flow1.add(this.roomSetChooser);
        flow1.add(manageClassroom);
        northwest.add(flow1, StackedBorderLayout.NORTH);

        this.semesterScheduleProfiles = new JComboBox<>();
        this.semesterScheduleProfiles.setPreferredSize(newPref);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow2.add(nwLabels[1]);
        flow2.add(this.semesterScheduleProfiles);
        flow2.add(manageSemester);
        northwest.add(flow2, StackedBorderLayout.NORTH);

        this.classOfferingProfiles = new JComboBox<>();
        this.classOfferingProfiles.setPreferredSize(newPref);
        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow3.add(nwLabels[2]);
        flow3.add(this.classOfferingProfiles);
        flow3.add(manageCourses);
        northwest.add(flow3, StackedBorderLayout.NORTH);

        this.studentPopulationProfiles = new JComboBox<>();
        this.studentPopulationProfiles.setPreferredSize(newPref);
        final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow4.add(nwLabels[3]);
        flow4.add(this.studentPopulationProfiles);
        flow4.add(manageStudents);
        northwest.add(flow4, StackedBorderLayout.NORTH);

        this.qualityScoringProfile = new JComboBox<>();
        this.qualityScoringProfile.setPreferredSize(newPref);
        final JPanel flow5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow5.add(nwLabels[4]);
        flow5.add(this.qualityScoringProfile);
        flow5.add(manageScoring);
        northwest.add(flow5, StackedBorderLayout.NORTH);

        final JPanel northcenter = new JPanel(new StackedBorderLayout());
        northcenter.setBorder(BorderFactory.createEtchedBorder());
        north.add(northcenter, StackedBorderLayout.WEST);

        final JButton runSim = new JButton("Run Simulation...");
        runSim.setActionCommand(RUN_SIM_CMD);
        runSim.addActionListener(this);
        final JPanel flow6 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        flow6.add(runSim);
        northcenter.add(flow6, StackedBorderLayout.NORTH);

        this.progressStatus = new JLabel(CoreConstants.SPC);
        content.add(this.progressStatus, StackedBorderLayout.SOUTH);
        this.progressBar = new JProgressBar(0, 1000);
        content.add(this.progressBar, StackedBorderLayout.SOUTH);

        final JPanel display = new JPanel();
        display.setBorder(BorderFactory.createLoweredBevelBorder());
        content.add(display, StackedBorderLayout.CENTER);

        this.frame.addWindowListener(this);

        UIUtilities.packAndCenter(this.frame);
        this.frame.setVisible(true);
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
            if (this.classroomDialog == null) {
                this.classroomDialog = new RoomSetsDlg(this.data);
                final Dimension size = this.frame.getSize();
                final Point pos = this.frame.getLocation();
                final Dimension dlgSize = this.classroomDialog.getSize();
                final int x = pos.x + (size.width - dlgSize.width) / 2;
                final int y = pos.y + (size.height - dlgSize.height) / 2;
                this.classroomDialog.setLocation(x, y);
            }
            this.classroomDialog.show();
        } else if (MANAGE_SEMESTER_PROFILES_CMD.equals(cmd)) {

        } else if (MANAGE_COURSE_PROFILES_CMD.equals(cmd)) {

        } else if (MANAGE_STUDENT_PROFILES_CMD.equals(cmd)) {

        } else if (MANAGE_SCORING_PROFILES_CMD.equals(cmd)) {

        }
    }

    /**
     * Invoked when a window has been closed.
     */
    public void windowClosed(final WindowEvent e) {

        if (this.classroomDialog != null) {
            this.classroomDialog.close();
            this.classroomDialog = null;
        }
    }

    /**
     * Main method to run the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();

        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);

        final File dataDir = new File("/opt/sim");

        if (dataDir.exists() || dataDir.mkdirs()) {
            SwingUtilities.invokeLater(new SpurSimulation(dataDir));
        } else {
            JOptionPane.showMessageDialog(null, "Unable to create data directory", "Spur Simulation",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

