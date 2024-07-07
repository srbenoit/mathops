package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminMainWindow;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbContext;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The top-most panel within the "Students" tab.
 *
 * <p>
 * This panel has buttons to choose between 4 possible display cards
 * <ul>
 * <li>CardStudentPick - to allow the user to pick a single student
 * <li>CardPopulations - to allow the user to pick a population
 * <li>CardStudentDetail - details on the currently picked student
 * <li>CardPopulationDetail - details on the currently picked population
 * </ul>
 */
public final class TopPanelStudent extends JPanel implements ActionListener {

    /** An action command. */
    private static final String PICK_CMD = "PICK";

    /** An action command. */
    private static final String CURSTU_CMD = "CURSTU";

    /** An action command. */
    private static final String POPULATION_CMD = "POP";

    /** An action command. */
    private static final String CURPOP_CMD = "CURPOPS";

    /** An action command. */
    private static final String VIEWLOG_CMD = "VIEWLOG";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1672732730332605390L;

    /** The center panel. */
    private final JPanel cardPane;

    /** The card layout. */
    private final CardLayout cards;

    /** The "Pick Student" card. */
    private final CardPickStudent cardPickStudent;

    /** The "Populations" card. */
    private final CardPopulations cardPopulations;

    /** The "Student Detail" card. */
    private final CardStudentDetail cardStudentDetail;

    /** The "Population Detail" card. */
    private final CardPopulationDetail cardPopulationDetail;

    /** The button to view the current student detail. */
    private final JButton stuDetailButton;

    ///** The button to view the current population detail. */
    // private final JButton popDetailButton;

    /** The key of the currently showing card. */
    private String showing;

    /**
     * Constructs a new {@code TopPanelStudent}.
     *
     * @param theCache         the data cache
     * @param liveContext      the database context used to access live data
     * @param theFixed         the fixed data
     */
    public TopPanelStudent(final Cache theCache, final Object theRenderingHint, final DbContext liveContext,
                           final FixedData theFixed) {

        super(new BorderLayout(5, 5));
        setPreferredSize(AdminMainWindow.PREF_SIZE);

        setBackground(Skin.OFF_WHITE_GRAY);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JButton[] btn = new JButton[1];

        final JPanel buttonRow = new JPanel(new StackedBorderLayout(5, 5));
        buttonRow.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        buttonRow.setBackground(Skin.OFF_WHITE_GRAY);
        add(buttonRow, BorderLayout.PAGE_START);

        buttonRow.add(makeTopButton("Pick Student", PICK_CMD, Skin.LT_GREEN, btn),
                StackedBorderLayout.WEST);

        buttonRow.add(makeTopButton("Student Detail", CURSTU_CMD, Skin.LT_RED, btn),
                StackedBorderLayout.WEST);

        this.stuDetailButton = btn[0];
        this.stuDetailButton.setEnabled(false);

        // buttonRow.add(new JLabel(" "));

        // buttonRow.add(makeTopButton("Pick Population", POPULATION_CMD,
        // Skin.LT_CYAN,theRenderingHint, btn), StackedBorderLayout.WEST);
        //
        // buttonRow.add(makeTopButton("Population Detail", CURPOP_CMD, Skin.LT_MAGENTA,
        // theRenderingHint,btn), StackedBorderLayout.WEST);
        // this.popDetailButton = btn[0];
        // this.popDetailButton.setEnabled(false);

        final JButton viewLog = new JButton("View Log");
        viewLog.setActionCommand(VIEWLOG_CMD);
        viewLog.addActionListener(this);
        buttonRow.add(viewLog, StackedBorderLayout.EAST);

        // Center - card pane

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Skin.OFF_WHITE_GRAY);
        add(this.cardPane, BorderLayout.CENTER);

        // Card 1: pick student

        this.cardPickStudent = new CardPickStudent(this, theCache, theFixed);
        this.cardPane.add(this.cardPickStudent, PICK_CMD);

        this.cardStudentDetail = new CardStudentDetail(theCache, liveContext, theFixed);
        this.cardPane.add(this.cardStudentDetail, CURSTU_CMD);

        this.cardPopulations = new CardPopulations(theCache);
        this.cardPane.add(this.cardPopulations, POPULATION_CMD);

        this.cardPopulationDetail = new CardPopulationDetail(theCache, liveContext, theFixed);
        this.cardPane.add(this.cardPopulationDetail, CURPOP_CMD);

        this.cards.show(this.cardPane, PICK_CMD);
        this.showing = PICK_CMD;
    }

    /**
     * Creates a button for the top of the pane.
     *
     * @param title            the button title
     * @param command          the action command
     * @param background       the background color
     * @param btn              an array to populate with the created button
     * @return the button panel
     */
    private JPanel makeTopButton(final String title, final String command, final Color background,
                                 final JButton[] btn) {

        btn[0] = new JButton(title);
        btn[0].setActionCommand(command);
        btn[0].addActionListener(this);
        btn[0].setFont(Skin.BIG_BUTTON_16_FONT);

        final JPanel menuBox = new JPanel(new BorderLayout());
        menuBox.setBackground(background);
        menuBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        menuBox.add(btn[0], BorderLayout.CENTER);

        return menuBox;
    }

    /**
     * Sets the focus when this panel is activated.
     */
    public void focus() {

        if (PICK_CMD.equals(this.showing)) {
            this.cardPickStudent.focus();
        } else if (CURSTU_CMD.equals(this.showing)) {
            this.cardStudentDetail.focus();
        } else if (POPULATION_CMD.equals(this.showing)) {
            this.cardPopulations.focus();
        } else if (CURPOP_CMD.equals(this.showing)) {
            this.cardPopulationDetail.focus();
        }
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (PICK_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, PICK_CMD);
            this.showing = PICK_CMD;
            this.cardPickStudent.focus();
        } else if (CURSTU_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, CURSTU_CMD);
            this.showing = CURSTU_CMD;
            this.cardStudentDetail.focus();
        } else if (POPULATION_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, POPULATION_CMD);
            this.showing = POPULATION_CMD;
            this.cardPopulations.focus();
        } else if (CURPOP_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, CURPOP_CMD);
            this.showing = CURPOP_CMD;
            this.cardPopulationDetail.focus();
        } else if (VIEWLOG_CMD.equals(cmd)) {
            new LogWindow(Log.getWriter().errorMessagesAsString()).setVisible(true);
        }
    }

    /**
     * Sets the picked student.
     *
     * @param cache the data cache
     * @param data  the student data
     */
    void setStudent(final Cache cache, final StudentData data) {

        this.cardStudentDetail.setStudent(cache, data);
        this.stuDetailButton.setEnabled(data != null);

        if (data != null) {
            Log.info("Setting student: ", data.student.stuId);

            this.cards.show(this.cardPane, CURSTU_CMD);
            this.showing = CURSTU_CMD;
            this.cardStudentDetail.focus();
        }
    }
}
