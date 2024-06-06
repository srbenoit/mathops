package dev.mathops.app.adm.management;

import dev.mathops.app.adm.AdminMainWindow;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.logic.Cache;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The "Management" pane.
 */
public class ManagementTabPane extends JPanel implements ActionListener {

    /** A button action command. */
    private static final String GENERAL_CMD = "GENERAL";

    /** A button action command. */
    private static final String TERMS_CMD = "TERMS";

    /** A button action command. */
    private static final String COURSES_CMD = "COURSES";

    /** A button action command. */
    private static final String PACING_CMD = "PACING";

    /** A button action command. */
    private static final String ASSESSMENT_CMD = "ASSESSMENT";

    /** A button action command. */
    private static final String RESOURCES_CMD = "RESOURCES";

    /** A button action command. */
    private static final String PLACEMENT_CMD = "PLACEMENT";

    /** A button action command. */
    private static final String PEOPLE_CMD = "PEOPLE";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3608158472388701801L;

    /** The center panel. */
    private final JPanel cardPane;

    /** The card layout. */
    private final CardLayout cards;

    /** The "general" card. */
    private final GeneralCard generalCard;

    /** The "term" card. */
    private final TermCard termCard;

    /** The "course" card. */
    private final CourseCard courseCard;

    /** The "pacing" card. */
    private final PacingCard pacingCard;

    /** The "assess" card. */
    private final AssessCard assessCard;

    /** The "resource" card. */
    private final ResourceCard resourceCard;

    /** The "placement" card. */
    private final PlacementCard placementCard;

    /** The "people" card. */
    private final PeopleCard peopleCard;

    /**
     * Constructs a new {@code ManagementTabPane}.
     *
     * @param theCache         the data cache
     */
    public ManagementTabPane(final Cache theCache, final Object theRenderingHint) {

        super(new BorderLayout(5, 5));
        setPreferredSize(AdminMainWindow.PREF_SIZE);

        setBackground(Skin.OFF_WHITE_GRAY);
        setBorder(BorderFactory.createCompoundBorder( //
                BorderFactory.createEtchedBorder(), //
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JPanel menu = new JPanel();
        menu.setBackground(Skin.OFF_WHITE_GRAY);
        menu.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 5));
        add(menu, BorderLayout.NORTH);

        // [ General ]
        // admin_hold, hold_type, msg_lookup, dont_submit, parameters, high_schools, which_db,
        // zip_code
        menu.add(makeTopButton("General", GENERAL_CMD, Skin.LT_GREEN));
        menu.add(new JLabel(CoreConstants.SPC));

        // [ Terms ]
        // campus_calendar, remote_mpe, semester_calendar, term
        menu.add(makeTopButton("Term", TERMS_CMD, Skin.LT_RED));
        menu.add(new JLabel(CoreConstants.SPC));

        // [ Courses ]
        // course, crsection, csection, cunit, cuobjective, cusection, grading_std, prereq,
        // surveyq, stsurveyqa
        menu.add(makeTopButton("Course", COURSES_CMD, Skin.LT_CYAN));
        menu.add(new JLabel(CoreConstants.SPC));

        // [ Pacing ]
        // milestone, pace_track_rule, pacing_rules, pacing_structure
        menu.add(makeTopButton("Pacing", PACING_CMD, Skin.LT_MAGENTA));
        menu.add(new JLabel(CoreConstants.SPC));

        // [ Assessment ]
        // exam, examqa, homework, client_pc, pending_exam, testing_centers
        menu.add(makeTopButton("Assess", ASSESSMENT_CMD, Skin.LT_YELLOW));
        menu.add(new JLabel(CoreConstants.SPC));

        // [ Resources ]
        // calcs, resource, stresource, etext, etext_course, etext_key, stetext
        menu.add(makeTopButton("Resource", RESOURCES_CMD, Skin.LT_BLUE));
        menu.add(new JLabel(CoreConstants.SPC));

        // [ Placement/Challenge ]
        // challenge_fee, mpe, mpe_credit, mpe_log, mpecr_denied, plc_fee, stchallenge,
        // stchallengeqa, stmpe, stmpeqa
        menu.add(makeTopButton("Placement", PLACEMENT_CMD, Skin.LT_LIME));
        menu.add(new JLabel(CoreConstants.SPC));

        // [ People ]
        // applicant, discipline, dup_registr, except_stu, fft_trns, final_croll, grade_roll,
        // logins, pace_appeals, special_stus, stcourse, stcuobjective, stexam,
        // sthomework, sthwqa, stmilestone, stpace_summary, stqa, stterm, student, stvisit,
        // user_clearance
        menu.add(makeTopButton("People", PEOPLE_CMD, Skin.LT_BROWN));
        menu.add(new JLabel(CoreConstants.SPC));

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Color.WHITE);
        add(this.cardPane, BorderLayout.CENTER);

        // Card 1: General
        this.generalCard = new GeneralCard();
        this.cardPane.add(this.generalCard, GENERAL_CMD);

        // Card 2: Terms
        this.termCard = new TermCard(theCache, theRenderingHint);
        this.cardPane.add(this.termCard, TERMS_CMD);

        // Card 3: Courses
        this.courseCard = new CourseCard();
        this.cardPane.add(this.courseCard, COURSES_CMD);

        // Card 4: Pacing
        this.pacingCard = new PacingCard();
        this.cardPane.add(this.pacingCard, PACING_CMD);

        // Card 5: Assess
        this.assessCard = new AssessCard(theCache);
        this.cardPane.add(this.assessCard, ASSESSMENT_CMD);

        // Card 6: Resource
        this.resourceCard = new ResourceCard();
        this.cardPane.add(this.resourceCard, RESOURCES_CMD);

        // Card 7: Placement
        this.placementCard = new PlacementCard(theCache);
        this.cardPane.add(this.placementCard, PLACEMENT_CMD);

        // Card 8: People
        this.peopleCard = new PeopleCard(theCache);
        this.cardPane.add(this.peopleCard, PEOPLE_CMD);

        this.cards.show(this.cardPane, GENERAL_CMD);
    }

    /**
     * Creates a button for the top of the pane.
     *
     * @param title            the button title
     * @param command          the action command
     * @param background       the background color
     * @return the button panel
     */
    private JPanel makeTopButton(final String title, final String command, final Color background) {

        final JButton btn = new JButton(title);
        btn.setActionCommand(command);
        btn.addActionListener(this);
        btn.setFont(Skin.BIG_BUTTON_16_FONT);

        final JPanel menuBox = new JPanel(new BorderLayout());
        menuBox.setBackground(background);
        menuBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        menuBox.add(btn, BorderLayout.CENTER);

        return menuBox;
    }

    /**
     * Sets the focus when this panel is activated.
     */
    public void focus() {

        // No action
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (GENERAL_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, GENERAL_CMD);
            this.generalCard.reset();
        } else if (TERMS_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, TERMS_CMD);
            this.termCard.reset();
        } else if (COURSES_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, COURSES_CMD);
            this.courseCard.reset();
        } else if (PACING_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, PACING_CMD);
            this.pacingCard.reset();
        } else if (ASSESSMENT_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, ASSESSMENT_CMD);
            this.assessCard.reset();
        } else if (RESOURCES_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, RESOURCES_CMD);
            this.resourceCard.reset();
        } else if (PLACEMENT_CMD.equals(cmd)) {
            this.placementCard.refresh();
            this.cards.show(this.cardPane, PLACEMENT_CMD);
        } else if (PEOPLE_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, PEOPLE_CMD);
            this.peopleCard.reset();
        }
    }
}
