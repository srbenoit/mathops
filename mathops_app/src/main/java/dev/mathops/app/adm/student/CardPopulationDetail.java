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
final class CardPopulationDetail extends JPanel implements ActionListener {

    /** An action command. */
    private static final String PICK_CMD = "PICK";

    /** An action command. */
    private static final String POPULATION_CMD = "POP";

    /** An action command. */
    private static final String TABS_CMD = "TABS";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1672732730332605390L;

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

        // The fixed data.

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder( BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // Top - [Pick] button and selected student name/ID

        final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 5));
        top.setBackground(Color.WHITE);
        add(top, BorderLayout.NORTH);

        final JButton pick = new JButton("Pick Student");
        final JPanel pickBox = new JPanel(new BorderLayout());
        pickBox.setBackground(Skin.LT_GREEN);
        pickBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
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

        final JTextField selectedStudentName = new JTextField(16);
        selectedStudentName.setBorder(null);
        selectedStudentName.setFont(Skin.MEDIUM_HEADER_15_FONT);
        top.add(selectedStudentName);

        final JLabel stuIdLbl = new JLabel("Student ID:");
        stuIdLbl.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 0));
        stuIdLbl.setFont(Skin.MEDIUM_15_FONT);
        stuIdLbl.setForeground(Skin.LABEL_COLOR);
        top.add(stuIdLbl);

        final JTextField selectedStudentId = new JTextField(9);
        selectedStudentId.setBorder(null);
        selectedStudentId.setEditable(false);
        selectedStudentId.setFont(Skin.MEDIUM_HEADER_15_FONT);
        top.add(selectedStudentId);

        // Center - card pane (pick pane card, and card with tabs for data areas)

        final CardLayout cards = new CardLayout();
        final JPanel cardPane = new JPanel(cards);
        cardPane.setBackground(Color.WHITE);
        add(cardPane, BorderLayout.CENTER);

        // Card 1: pick student

        // Card 2: pick population

        // this.populationPanel = new StudentPopulationPanel(this, theCache, theFixed);
        // this.cardPane.add(this.populationPanel, POPULATION_CMD);

        // Card 3: data area tabs

        final JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.WHITE);
        cardPane.add(tabs, TABS_CMD);

        final StudentInfoPanel infoPanel = new StudentInfoPanel(theFixed);
        tabs.addTab("Info", infoPanel);

        final StudentCoursesPanel coursePanel = new StudentCoursesPanel(theCache.conn);
        tabs.addTab("Courses", coursePanel);

        final StudentDeadlinesPanel deadlinesPanel = new StudentDeadlinesPanel(theFixed);
        tabs.addTab("Deadlines", deadlinesPanel);

        final StudentActivityPanel activityPanel = new StudentActivityPanel();
        tabs.addTab("Activity", activityPanel);

        final StudentDisciplinePanel disciplinePanel = new StudentDisciplinePanel(theCache);
        tabs.addTab("Discipline", disciplinePanel);

        final StudentHoldsPanel holdsPanel = new StudentHoldsPanel(theCache, theFixed);
        tabs.addTab("Holds", holdsPanel);

        final StudentExamsPanel examsPanel = new StudentExamsPanel(theCache, liveContext, theFixed);
        tabs.addTab("Exams", examsPanel);

        final StudentPlacementPanel mptPanel = new StudentPlacementPanel();
        final JScrollPane scroll = new JScrollPane(mptPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        scroll.getVerticalScrollBar().setBlockIncrement(30);
        tabs.addTab("Placement", scroll);

        final StudentMathPlanPanel mathPlanPanel = new StudentMathPlanPanel(theRenderingHint);
        tabs.addTab("Math Plan", mathPlanPanel);

        cards.show(cardPane, PICK_CMD);
    }

    /**
     * Sets the focus when this panel is activated.
     */
    void focus() {

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
