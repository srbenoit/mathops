package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminMainWindow;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbContext;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Serial;

/**
 * A card to display when the user selects the "Populations" option. This card includes a tabbed pane with ways to
 * select a population.
 */
class CardPopulationDetail extends JPanel implements ActionListener {

    /** An action command. */
    private static final String PICK_CMD = "PICK";

    /** An action command. */
    private static final String POPULATION_CMD = "POP";

    /** An action command. */
    private static final String TABS_CMD = "TABS";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1672732730332605390L;

    /** The fixed data. */
    private final FixedData fixed;

    /** The currently selected student. */
    private final JTextField selectedStudentName;

    /** The currently selected student ID. */
    private final JTextField selectedStudentId;

    /** The center panel. */
    private final JPanel cardPane;

    /** The card layout. */
    private final CardLayout cards;

    /** The tabbed layout with actions once a student is picked. */
    private final JTabbedPane tabs;

    /** The "Info" panel. */
    private final StudentInfoPanel infoPanel;

    /** The "Course" panel. */
    private final StudentCoursesPanel coursePanel;

    /** The "Deadlines" panel. */
    private final StudentDeadlinesPanel deadlinesPanel;

    /** The "Activity" panel. */
    private final StudentActivityPanel activityPanel;

    /** The "Discipline" panel. */
    private final StudentDisciplinePanel disciplinePanel;

    /** The "Holds" panel. */
    private final StudentHoldsPanel holdsPanel;

    /** The "Exams" panel. */
    private final StudentExamsPanel examsPanel;

    /** The "MPT" panel. */
    private final StudentPlacementPanel mptPanel;

    /** The "Math Plan" panel. */
    private final StudentMathPlanPanel mathPlanPanel;

    /** The key of the currently showing card layout. */
    private final String showing;

    /**
     * Constructs a new {@code CardPopulationDetail}.
     *
     * @param theCache         the data cache
     * @param liveContext      the database context used to access live data
     * @param theFixed         the fixed data
     */
    CardPopulationDetail(final Cache theCache, final DbContext liveContext,
                         final FixedData theFixed, final Object theRenderingHint) {

        super(new BorderLayout(5, 5));
        setPreferredSize(AdminMainWindow.PREF_SIZE);

        this.fixed = theFixed;

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder( //
                BorderFactory.createEtchedBorder(), //
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Top - [Pick] button and selected student name/ID

        final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 5));
        top.setBackground(Color.WHITE);
        add(top, BorderLayout.NORTH);

        final JButton pick = new JButton("Pick Student");
        final JPanel pickBox = new JPanel(new BorderLayout());
        pickBox.setBackground(Skin.LT_GREEN);
        pickBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        pickBox.add(pick, BorderLayout.CENTER);

        pick.setFont(Skin.BIG_BUTTON_16_FONT);
        pick.setActionCommand(PICK_CMD);
        pick.setMnemonic(KeyEvent.VK_P);
        pick.addActionListener(this);
        top.add(pickBox);
        top.add(new JLabel(CoreConstants.SPC));

        final JButton population = new JButton("Population");
        final JPanel populationBox = new JPanel(new BorderLayout());
        populationBox.setBackground(Skin.LT_RED);
        populationBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Skin.LIGHT, Skin.MEDIUM),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        populationBox.add(population, BorderLayout.CENTER);

        population.setFont(Skin.BIG_BUTTON_16_FONT);
        population.setActionCommand(POPULATION_CMD);
        population.setMnemonic(KeyEvent.VK_O);
        population.addActionListener(this);
        top.add(populationBox);
        top.add(new JLabel(CoreConstants.SPC));

        final JLabel selectedLbl = new JLabel("Selected Student:");
        selectedLbl.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        selectedLbl.setFont(Skin.MEDIUM_15_FONT);
        selectedLbl.setForeground(Skin.LABEL_COLOR);
        top.add(selectedLbl);

        this.selectedStudentName = new JTextField(16);
        this.selectedStudentName.setBorder(null);
        this.selectedStudentName.setFont(Skin.MEDIUM_HEADER_15_FONT);
        top.add(this.selectedStudentName);

        final JLabel stuIdLbl = new JLabel("Student ID:");
        stuIdLbl.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        stuIdLbl.setFont(Skin.MEDIUM_15_FONT);
        stuIdLbl.setForeground(Skin.LABEL_COLOR);
        top.add(stuIdLbl);

        this.selectedStudentId = new JTextField(9);
        this.selectedStudentId.setBorder(null);
        this.selectedStudentId.setEditable(false);
        this.selectedStudentId.setFont(Skin.MEDIUM_HEADER_15_FONT);
        top.add(this.selectedStudentId);

        // Center - card pane (pick pane card, and card with tabs for data areas)

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Color.WHITE);
        add(this.cardPane, BorderLayout.CENTER);

        // Card 1: pick student

        // Card 2: pick population

        // this.populationPanel = new StudentPopulationPanel(this, theCache, theFixed);
        // this.cardPane.add(this.populationPanel, POPULATION_CMD);

        // Card 3: data area tabs

        this.tabs = new JTabbedPane();
        this.tabs.setBackground(Color.WHITE);
        this.cardPane.add(this.tabs, TABS_CMD);

        this.infoPanel = new StudentInfoPanel(theFixed);
        this.tabs.addTab("Info", this.infoPanel);

        this.coursePanel = new StudentCoursesPanel(theCache.conn);
        this.tabs.addTab("Courses", this.coursePanel);

        this.deadlinesPanel = new StudentDeadlinesPanel(this.fixed);
        this.tabs.addTab("Deadlines", this.deadlinesPanel);

        this.activityPanel = new StudentActivityPanel();
        this.tabs.addTab("Activity", this.activityPanel);

        this.disciplinePanel = new StudentDisciplinePanel(theCache);
        this.tabs.addTab("Discipline", this.disciplinePanel);

        this.holdsPanel = new StudentHoldsPanel(theCache, this.fixed);
        this.tabs.addTab("Holds", this.holdsPanel);

        this.examsPanel =
                new StudentExamsPanel(theCache, liveContext, this.fixed);
        this.tabs.addTab("Exams", this.examsPanel);

        this.mptPanel = new StudentPlacementPanel();
        final JScrollPane scroll = new JScrollPane(this.mptPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getVerticalScrollBar().setBlockIncrement(30);
        this.tabs.addTab("Placement", scroll);

        this.mathPlanPanel = new StudentMathPlanPanel(theRenderingHint);
        this.tabs.addTab("Math Plan", this.mathPlanPanel);

        this.cards.show(this.cardPane, PICK_CMD);
        this.showing = PICK_CMD;
    }

    /**
     * Sets the focus when this panel is activated.
     */
    public void focus() {

        // TODO:
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        Log.info("CardPopulationDetail: ", cmd);
        focus();
    }
}
