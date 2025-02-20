package dev.mathops.app.adm.office;

import dev.mathops.app.adm.MainWindow;
import dev.mathops.app.adm.UserData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The top-most panel within the "Office" tab.
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
public final class TopPanelOffice extends JPanel implements ActionListener {

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
     * Constructs a new {@code TopPanelOffice}.
     *
     * @param theCache the data cache
     * @param theFixed the fixed data
     */
    public TopPanelOffice(final Cache theCache, final UserData theFixed) {

        super(new BorderLayout(5, 5));

        setPreferredSize(MainWindow.PREF_SIZE);

        setBackground(Skin.OFF_WHITE_GRAY);
        final Border etched = BorderFactory.createEtchedBorder();
        final Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        final CompoundBorder border = BorderFactory.createCompoundBorder(etched, padding);
        setBorder(border);

        final JButton[] btn = new JButton[1];

        final JPanel buttonRow = new JPanel(new StackedBorderLayout(5, 5));
        final Border topBottomPad = BorderFactory.createEmptyBorder(5, 0, 5, 0);
        buttonRow.setBorder(topBottomPad);
        buttonRow.setBackground(Skin.OFF_WHITE_GRAY);
        add(buttonRow, BorderLayout.PAGE_START);

        final String pickStudentLbl = Res.get(Res.PICK_STUDENT);
        final JPanel pickButton = makeTopButton(pickStudentLbl, PICK_CMD, Skin.LT_GREEN, btn);
        buttonRow.add(pickButton, StackedBorderLayout.WEST);

        final String studentDetailLbl = Res.get(Res.STUDENT_DETAIL);
        final JPanel detailButton = makeTopButton(studentDetailLbl, CURSTU_CMD, Skin.LT_RED, btn);
        buttonRow.add(detailButton, StackedBorderLayout.WEST);

        this.stuDetailButton = btn[0];
        this.stuDetailButton.setEnabled(false);

        //buttonRow.add(new JLabel(" "));

        //final String pickPopLbl = Res.get(Res.PICK_POPULATION);
        //buttonRow.add(makeTopButton(pickPopLbl, POPULATION_CMD, btn), StackedBorderLayout.WEST);

        //final String popDetailLbl = Res.get(Res.POPULATION_DETAIL);
        //buttonRow.add(makeTopButton(popDetailLbl, CURPOP_CMD, Skin.LT_MAGENTA, btn), StackedBorderLayout.WEST);

        //this.popDetailButton = btn[0];
        //this.popDetailButton.setEnabled(false);

        final String viewLogLbl = Res.get(Res.VIEW_LOG);
        final JButton viewLog = new JButton(viewLogLbl);
        viewLog.setFont(Skin.BUTTON_13_FONT);
        viewLog.setActionCommand(VIEWLOG_CMD);
        viewLog.addActionListener(this);
        buttonRow.add(viewLog, StackedBorderLayout.EAST);

        // Center - card pane

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Skin.OFF_WHITE_GRAY);
        add(this.cardPane, BorderLayout.CENTER);

        this.cardPickStudent = new CardPickStudent(this, theCache, theFixed);
        this.cardPane.add(this.cardPickStudent, PICK_CMD);

        this.cardStudentDetail = new CardStudentDetail(theCache, theFixed);
        this.cardPane.add(this.cardStudentDetail, CURSTU_CMD);

        this.cardPopulations = new CardPopulations(theCache);
        this.cardPane.add(this.cardPopulations, POPULATION_CMD);

        this.cardPopulationDetail = new CardPopulationDetail(theCache, theFixed);
        this.cardPane.add(this.cardPopulationDetail, CURPOP_CMD);

        this.cards.show(this.cardPane, PICK_CMD);
        this.showing = PICK_CMD;
    }

    /**
     * Creates a button for the top of the pane.
     *
     * @param title      the button title
     * @param command    the action command
     * @param background the background color
     * @param btn        an array to populate with the created button
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

        final Border bevel = BorderFactory.createLoweredBevelBorder();
        final Border padding = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        final CompoundBorder border = BorderFactory.createCompoundBorder(bevel, padding);
        menuBox.setBorder(border);
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
            final String message = Log.getWriter().errorMessagesAsString();
            new LogWindow(message).setVisible(true);
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

    /**
     * Clears the display - this makes sure any open dialogs are closed so the app can close.
     */
    public void clearDisplay() {

        if (this.cardPickStudent != null) {
            this.cardPickStudent.clearDisplay();
        }
        if (this.cardPopulations != null) {
            this.cardPopulations.clearDisplay();
        }
        if (this.cardStudentDetail != null) {
            this.cardStudentDetail.clearDisplay();
        }
        if (this.cardPopulationDetail != null) {
            this.cardPopulationDetail.clearDisplay();
        }
    }
}
