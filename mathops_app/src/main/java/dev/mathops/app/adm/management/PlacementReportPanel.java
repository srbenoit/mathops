package dev.mathops.app.adm.management;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawMpecrDeniedLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStsurveyqaLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStsurveyqa;
import dev.mathops.db.old.rawrecord.RawStudent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A panel that presents a report of recent placement activity, like placement tool completions, credit denied, ELM and
 * precalculus tutorial results submitted, and an audit of recent placement- related "test scores" in Banner.
 */
final class PlacementReportPanel extends AdmPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2417731406942097215L;

    /** An action command. */
    private static final String VIEW_MPT_TODAY = "VIEW_MPT_TODAY";

    /** An action command. */
    private static final String VIEW_MPT_YESTERDAY = "VIEW_MPT_YESTERDAY";

    /** An action command. */
    private static final String VIEW_MPT_PASTNDAYS = "VIEW_MPT_PAST3DAYS";

    /** An action command. */
    private static final String VIEW_MPT_PAST7DAYS = "VIEW_MPT_PAST7DAYS";

    /** An action command. */
    private static final String VIEW_ELM_TODAY = "VIEW_ELM_TODAY";

    /** An action command. */
    private static final String VIEW_ELM_YESTERDAY = "VIEW_ELM_YESTERDAY";

    /** An action command. */
    private static final String VIEW_ELM_PASTNDAYS = "VIEW_ELM_PAST3DAYS";

    /** An action command. */
    private static final String VIEW_ELM_PAST7DAYS = "VIEW_ELM_PAST7DAYS";

    /** An action command. */
    private static final String VIEW_PRE_TODAY = "VIEW_PRE_TODAY";

    /** An action command. */
    private static final String VIEW_PRE_YESTERDAY = "VIEW_PRE_YESTERDAY";

    /** An action command. */
    private static final String VIEW_PRE_PASTNDAYS = "VIEW_PRE_PAST3DAYS";

    /** An action command. */
    private static final String VIEW_PRE_PAST7DAYS = "VIEW_PRE_PAST7DAYS";

    /** The data cache. */
    private final Cache cache;

    /** A panel in the center to which report contents will be added. */
    private final JPanel center;

    /** Field to receive number of MPT attempts today. */
    private final JTextField mptToday;

    /** Field to receive number of MPT attempts yesterday. */
    private final JTextField mptYesterday;

    /** Field to receive number of days for which to query MPT history. */
    private final JTextField mptPastNDays;

    /** Field to receive number of ELM Tutorial attempts today. */
    private final JTextField elmToday;

    /** Field to receive number of ELM Tutorial attempts yesterday. */
    private final JTextField elmYesterday;

    /** Field to receive number of days for which to query ELM history. */
    private final JTextField elmPastNDays;

    /** Field to receive number of Precalculus Tutorial attempts today. */
    private final JTextField preToday;

    /** Field to receive number of Precalculus Tutorial attempts yesterday. */
    private final JTextField preYesterday;

    /** Field to receive number of days for which to query Precalculus Tutorial history. */
    private final JTextField prePastNDays;

    /** The current date. */
    private LocalDateTime today;

    /** The queried MPT submission history. */
    private final List<List<RawStmpe>> mpeHistory;

    /** The queried ELM submission history. */
    private final List<List<RawStexam>> elmHistory;

    /** The queried Precalculus Tutorial submission history. */
    private final List<List<RawStexam>> preHistory;

    /**
     * Constructs a new {@code PlacementReportPanel}.
     *
     * @param theCache         the data cache
     */
    PlacementReportPanel(final Cache theCache) {

        super();
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;
        this.mpeHistory = new ArrayList<>(10);
        this.elmHistory = new ArrayList<>(10);
        this.preHistory = new ArrayList<>(10);

        this.mptToday = new JTextField(5);
        this.mptToday.setEditable(false);
        this.mptToday.setBackground(Skin.LIGHTEST);
        this.mptYesterday = new JTextField(5);
        this.mptYesterday.setEditable(false);
        this.mptYesterday.setBackground(Skin.LIGHTEST);
        this.mptPastNDays = new JTextField(5);

        this.elmToday = new JTextField(5);
        this.elmToday.setEditable(false);
        this.elmToday.setBackground(Skin.LIGHTEST);
        this.elmYesterday = new JTextField(5);
        this.elmYesterday.setEditable(false);
        this.elmYesterday.setBackground(Skin.LIGHTEST);
        this.elmPastNDays = new JTextField(5);

        this.preToday = new JTextField(5);
        this.preToday.setEditable(false);
        this.preToday.setBackground(Skin.LIGHTEST);
        this.preYesterday = new JTextField(5);
        this.preYesterday.setEditable(false);
        this.preYesterday.setBackground(Skin.LIGHTEST);
        this.prePastNDays = new JTextField(5);

        // Left-hand side is a menu of reports with some short summary data

        final JPanel col1 = makeOffWhitePanel(new BorderLayout(0, 0));
        col1.setBackground(Skin.LIGHTEST);
        add(col1, StackedBorderLayout.WEST);

        col1.add(makeHeader("Placement-Related Reports", false), BorderLayout.PAGE_START);

        final JPanel col1Center = makeOffWhitePanel(new BorderLayout(0, 0));
        col1Center.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        col1Center.setBackground(Skin.WHITE);
        col1.add(col1Center, BorderLayout.CENTER);

        final JPanel col1N = makeOffWhitePanel(new BorderLayout(0, 0));
        col1N.setBackground(Skin.LIGHTEST);
        final JPanel col1NN = makeOffWhitePanel(new BorderLayout(0, 0));
        col1NN.setBackground(Skin.LIGHTEST);
        final JPanel col1NC = makeOffWhitePanel(new BorderLayout(0, 0));
        col1NC.setBackground(Skin.LIGHTEST);
        final JPanel col1NNC = makeOffWhitePanel(new BorderLayout(0, 0));
        col1NNC.setBackground(Skin.LIGHTEST);
        final JPanel col1NNS = makeOffWhitePanel(new BorderLayout(0, 0));
        col1NNS.setBackground(Skin.LIGHTEST);
        final JPanel col1NCN = makeOffWhitePanel(new BorderLayout(0, 0));
        col1NCN.setBackground(Skin.LIGHTEST);
        final JPanel col1NCC = makeOffWhitePanel(new BorderLayout(0, 0));
        col1NCC.setBackground(Skin.LIGHTEST);

        col1Center.add(col1N, BorderLayout.PAGE_START);
        col1N.add(col1NN, BorderLayout.PAGE_START);
        col1N.add(col1NC, BorderLayout.CENTER);

        col1NN.add(col1NNC, BorderLayout.CENTER);
        col1NN.add(col1NNS, BorderLayout.PAGE_END);
        col1NC.add(col1NCN, BorderLayout.PAGE_START);
        col1NC.add(col1NCC, BorderLayout.CENTER);

        col1NNC.add(buildPlacementToolSubmissionsSummary(), BorderLayout.PAGE_START);
        col1NNC.add(buildELMTutorialSubmissionsSummary(), BorderLayout.CENTER);
        col1NNC.add(buildPrecalcTutorialSubmissionsSummary(), BorderLayout.PAGE_END);

        col1NNS.add(buildCreditDeniedSummary(), BorderLayout.PAGE_START);
        col1NNS.add(buildChallengeExamSummary(), BorderLayout.CENTER);
        col1NNS.add(buildMathPlanActivitySummary(), BorderLayout.PAGE_END);

        col1NCN.add(buildQueuedTestScoreReport(), BorderLayout.PAGE_START);
        col1NCN.add(buildBannerTestScoreAudit(), BorderLayout.CENTER);
        col1NCN.add(buildPlacementHistortReport(), BorderLayout.PAGE_END);

        col1NCC.add(buildAutomatedReportControl(), BorderLayout.PAGE_START);

        this.center = makeOffWhitePanel(new BorderLayout(0, 0));
        this.center.setBorder(BorderFactory.createEtchedBorder());
        this.center.setBackground(Skin.LIGHTEST);
        add(this.center, StackedBorderLayout.CENTER);
    }

    /**
     * Creates the summary panel for Placement Tool submissions.
     *
     * @return the panel
     */
    private JPanel buildPlacementToolSubmissionsSummary() {

        final JPanel panel = makeOffWhitePanel(null);
        final LayoutManager box = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
        panel.setLayout(box);

        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));

        final JPanel flow0 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        flow0.setBackground(Skin.OFF_WHITE_BLUE);
        flow0.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Skin.WHITE));
        final JLabel header =
                makeBoldLabel("Placement Tool Submissions");
        flow0.add(header);
        panel.add(flow0);

        final JLabel[] labels = {
                makeLabel("Today:"),
                makeLabel("Yesterday:")};

        int maxW = 0;
        int maxH = 0;
        for (final JLabel label : labels) {
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            final Dimension size = label.getPreferredSize();
            maxW = Math.max(maxW, size.width);
            maxH = Math.max(maxH, size.height);
        }
        final Dimension newSize = new Dimension(maxW, maxH);
        for (final JLabel label : labels) {
            label.setPreferredSize(newSize);
        }

        final JButton btn1 = new JButton("View...");
        btn1.setActionCommand(VIEW_MPT_TODAY);
        btn1.addActionListener(this);

        final JButton btn2 = new JButton("View...");
        btn2.setActionCommand(VIEW_MPT_YESTERDAY);
        btn2.addActionListener(this);

        final JButton btn3 = new JButton("View...");
        btn3.setActionCommand(VIEW_MPT_PASTNDAYS);
        btn3.addActionListener(this);

        final JButton btn4 = new JButton("View...");
        btn4.setActionCommand(VIEW_MPT_PAST7DAYS);
        btn4.addActionListener(this);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 1));
        flow1.setBackground(Skin.WHITE);
        flow1.add(labels[0]);
        flow1.add(this.mptToday);
        flow1.add(btn1);
        panel.add(flow1);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 1));
        flow2.setBackground(Skin.WHITE);
        flow2.add(labels[1]);
        flow2.add(this.mptYesterday);
        flow2.add(btn2);
        panel.add(flow2);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 1));
        flow3.setBackground(Skin.WHITE);
        final JLabel pastLabel = new JLabel("Past");
        flow3.add(pastLabel);
        flow3.add(this.mptPastNDays);
        final JLabel daysLabel = new JLabel("Days");
        flow3.add(daysLabel);
        flow3.add(btn3);
        panel.add(flow3);

        return panel;
    }

    /**
     * Creates the summary panel for ELM Tutorial submissions.
     *
     * @return the panel
     */
    private JPanel buildELMTutorialSubmissionsSummary() {

        final JPanel panel = makeOffWhitePanel(null);
        final LayoutManager box = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
        panel.setLayout(box);

        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));

        final JPanel flow0 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        flow0.setBackground(Skin.OFF_WHITE_BLUE);
        flow0.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Skin.WHITE));
        final JLabel header =
                makeBoldLabel("ELM Tutorial Submissions");
        flow0.add(header);
        panel.add(flow0);

        final JLabel[] labels = {
                makeLabel("Today:"),
                makeLabel("Yesterday:")};

        int maxW = 0;
        int maxH = 0;
        for (final JLabel label : labels) {
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            final Dimension size = label.getPreferredSize();
            maxW = Math.max(maxW, size.width);
            maxH = Math.max(maxH, size.height);
        }
        final Dimension newSize = new Dimension(maxW, maxH);
        for (final JLabel label : labels) {
            label.setPreferredSize(newSize);
        }

        final JButton btn1 = new JButton("View...");
        btn1.setActionCommand(VIEW_ELM_TODAY);
        btn1.addActionListener(this);

        final JButton btn2 = new JButton("View...");
        btn2.setActionCommand(VIEW_ELM_YESTERDAY);
        btn2.addActionListener(this);

        final JButton btn3 = new JButton("View...");
        btn3.setActionCommand(VIEW_ELM_PASTNDAYS);
        btn3.addActionListener(this);

        final JButton btn4 = new JButton("View...");
        btn4.setActionCommand(VIEW_ELM_PAST7DAYS);
        btn4.addActionListener(this);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 1));
        flow1.setBackground(Skin.WHITE);
        flow1.add(labels[0]);
        flow1.add(this.elmToday);
        flow1.add(btn1);
        panel.add(flow1);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 1));
        flow2.setBackground(Skin.WHITE);
        flow2.add(labels[1]);
        flow2.add(this.elmYesterday);
        flow2.add(btn2);
        panel.add(flow2);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 1));
        flow3.setBackground(Skin.WHITE);
        final JLabel pastLabel = new JLabel("Past");
        flow3.add(pastLabel);
        flow3.add(this.elmPastNDays);
        final JLabel daysLabel = new JLabel("Days");
        flow3.add(daysLabel);
        flow3.add(btn3);
        panel.add(flow3);

        return panel;
    }

    /**
     * Creates the summary panel for Precalculus Tutorial submissions.
     *
     * @return the panel
     */
    private JPanel buildPrecalcTutorialSubmissionsSummary() {

        final JPanel panel = makeOffWhitePanel(null);
        final LayoutManager box = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
        panel.setLayout(box);

        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));

        final JPanel flow0 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        flow0.setBackground(Skin.OFF_WHITE_BLUE);
        flow0.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Skin.WHITE));
        final JLabel header =
                makeBoldLabel("Precalculus Tutorial Submissions");
        flow0.add(header);
        panel.add(flow0);

        final JLabel[] labels = {
                makeLabel("Today:"),
                makeLabel("Yesterday:")};

        int maxW = 0;
        int maxH = 0;
        for (final JLabel label : labels) {
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            final Dimension size = label.getPreferredSize();
            maxW = Math.max(maxW, size.width);
            maxH = Math.max(maxH, size.height);
        }
        final Dimension newSize = new Dimension(maxW, maxH);
        for (final JLabel label : labels) {
            label.setPreferredSize(newSize);
        }

        final JButton btn1 = new JButton("View...");
        btn1.setActionCommand(VIEW_PRE_TODAY);
        btn1.addActionListener(this);

        final JButton btn2 = new JButton("View...");
        btn2.setActionCommand(VIEW_PRE_YESTERDAY);
        btn2.addActionListener(this);

        final JButton btn3 = new JButton("View...");
        btn3.setActionCommand(VIEW_PRE_PASTNDAYS);
        btn3.addActionListener(this);

        final JButton btn4 = new JButton("View...");
        btn4.setActionCommand(VIEW_PRE_PAST7DAYS);
        btn4.addActionListener(this);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 1));
        flow1.setBackground(Skin.WHITE);
        flow1.add(labels[0]);
        flow1.add(this.preToday);
        flow1.add(btn1);
        panel.add(flow1);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 1));
        flow2.setBackground(Skin.WHITE);
        flow2.add(labels[1]);
        flow2.add(this.preYesterday);
        flow2.add(btn2);
        panel.add(flow2);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 1));
        flow3.setBackground(Skin.WHITE);
        final JLabel pastLabel = new JLabel("Past");
        flow3.add(pastLabel);
        flow3.add(this.prePastNDays);
        final JLabel daysLabel = new JLabel("Days");
        flow3.add(daysLabel);
        flow3.add(btn3);
        panel.add(flow3);

        return panel;
    }

    /**
     * Creates the summary panel for denied placement credit.
     *
     * @return the panel
     */
    private static JPanel buildCreditDeniedSummary() {

        final JPanel panel = makeOffWhitePanel(new BorderLayout(0, 0));
        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));

        final JLabel lbl = makeBoldLabel("Placement Credit Denied");
        panel.add(lbl, BorderLayout.PAGE_START);

        return panel;
    }

    /**
     * Creates the summary panel for challenge exam activity.
     *
     * @return the panel
     */
    private static JPanel buildChallengeExamSummary() {

        final JPanel panel = makeOffWhitePanel(new BorderLayout(0, 0));
        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));

        final JLabel lbl = makeBoldLabel("Challenge Exam Activity");
        panel.add(lbl, BorderLayout.PAGE_START);

        return panel;
    }

    /**
     * Creates the summary panel for Math Plan activity.
     *
     * @return the panel
     */
    private static JPanel buildMathPlanActivitySummary() {

        final JPanel panel = makeOffWhitePanel(new BorderLayout(0, 0));
        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));

        final JLabel lbl = makeBoldLabel("Math Plan Activity");
        panel.add(lbl, BorderLayout.PAGE_START);

        return panel;
    }

    /**
     * Creates the summary panel for queued Banner test scores.
     *
     * @return the panel
     */
    private static JPanel buildQueuedTestScoreReport() {

        final JPanel panel = makeOffWhitePanel(new BorderLayout(0, 0));
        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));

        final JLabel lbl = makeBoldLabel("Queued Banner Test Scores");
        panel.add(lbl, BorderLayout.PAGE_START);

        return panel;
    }

    /**
     * Creates the summary panel for an audit of Banner test scores.
     *
     * @return the panel
     */
    private static JPanel buildBannerTestScoreAudit() {

        final JPanel panel = makeOffWhitePanel(new BorderLayout(0, 0));
        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));

        final JLabel lbl = makeBoldLabel("Audit Banner Test Scores");
        panel.add(lbl, BorderLayout.PAGE_START);

        return panel;
    }

    /**
     * Creates the summary panel to generate a history report of all placement activity.
     *
     * @return the panel
     */
    private static JPanel buildPlacementHistortReport() {

        final JPanel panel = makeOffWhitePanel(new BorderLayout(0, 0));
        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));

        final JLabel lbl = makeBoldLabel("Placement History Report");
        panel.add(lbl, BorderLayout.PAGE_START);

        return panel;
    }

    /**
     * Creates the summary panel to manage automated placement-related reporting.
     *
     * @return the panel
     */
    private static JPanel buildAutomatedReportControl() {

        final JPanel panel = makeOffWhitePanel(new BorderLayout(0, 0));
        panel.setBackground(Skin.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Skin.MEDIUM));

        final JLabel lbl = makeBoldLabel("Automated Placement Reports");
        panel.add(lbl, BorderLayout.PAGE_START);

        return panel;
    }

    /**
     * Refreshes the billing status display.
     */
    public void refreshStatus() {

        this.today = LocalDateTime.now();
        this.mpeHistory.clear();
        this.elmHistory.clear();

        try {
            RawStmpeLogic.getHistory(this.cache, this.mpeHistory, 8, this.today.toLocalDate());
            final int size = this.mpeHistory.size();

            if (size > 0) {
                this.mptToday.setText(Integer.toString(this.mpeHistory.get(size - 1).size()));
            } else {
                this.mptToday.setText("Error");
            }

            if (size > 1) {
                this.mptYesterday.setText(Integer.toString(this.mpeHistory.get(size - 2).size()));
            } else {
                this.mptYesterday.setText("Error");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            this.mptToday.setText("Error");
            this.mptYesterday.setText("Error");
        }

        try {
            RawStexamLogic.getHistory(this.cache, this.elmHistory, 8, this.today.toLocalDate(), "M 100T");

            // Filter out non-Unit exams
            for (final List<RawStexam> list : this.elmHistory) {
                list.removeIf(row -> !RawStexam.UNIT_EXAM.equals(row.examType));
            }

            final int size = this.elmHistory.size();

            if (size > 0) {
                this.elmToday.setText(Integer.toString(this.elmHistory.get(size - 1).size()));
            } else {
                this.elmToday.setText("Error");
            }

            if (size > 1) {
                this.elmYesterday.setText(Integer.toString(this.elmHistory.get(size - 2).size()));
            } else {
                this.elmYesterday.setText("Error");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            this.elmToday.setText("Error");
            this.elmYesterday.setText("Error");
        }

        try {
            RawStexamLogic.getHistory(this.cache, this.preHistory, 8, this.today.toLocalDate(),
                    "M 1170", "M 1180", "M 1240", "M 1250", "M 1260");

            // Filter out non-Unit exams
            for (final List<RawStexam> list : this.preHistory) {
                list.removeIf(row -> !RawStexam.UNIT_EXAM.equals(row.examType));
            }

            final int size = this.preHistory.size();

            if (size > 0) {
                this.preToday.setText(Integer.toString(this.preHistory.get(size - 1).size()));
            } else {
                this.preToday.setText("Error");
            }

            if (size > 1) {
                this.preYesterday.setText(Integer.toString(this.preHistory.get(size - 2).size()));
            } else {
                this.preYesterday.setText("Error");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            this.preToday.setText("Error");
            this.preYesterday.setText("Error");
        }

    }

    /**
     * Called when a button is pressed in the panel.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (VIEW_MPT_TODAY.equals(cmd)) {
            generateViewMptTodayReport();
        } else if (VIEW_MPT_YESTERDAY.equals(cmd)) {
            generateViewMptYeterdayReport();
        } else if (VIEW_MPT_PASTNDAYS.equals(cmd)) {
            generateViewMptPastNDaysReport();
        } else if (VIEW_ELM_TODAY.equals(cmd)) {
            generateViewElmTodayReport();
        } else if (VIEW_ELM_YESTERDAY.equals(cmd)) {
            generateViewElmYeterdayReport();
        } else if (VIEW_ELM_PASTNDAYS.equals(cmd)) {
            generateViewElmPastNDaysReport();
        } else if (VIEW_PRE_TODAY.equals(cmd)) {
            generateViewPreTodayReport();
        } else if (VIEW_PRE_YESTERDAY.equals(cmd)) {
            generateViewPreYeterdayReport();
        } else if (VIEW_PRE_PASTNDAYS.equals(cmd)) {
            generateViewPrePastNDaysReport();
        }
    }

    /**
     * Generates a report of Math Placement Tool submissions today and installs it in the center panel.
     */
    private void generateViewMptTodayReport() {

        this.center.removeAll();
        final HtmlBuilder builder = new HtmlBuilder(100);

        final int size = this.mpeHistory.size();

        if (size > 0) {
            try {
                builder.addln("Today's Math Placement Tool attempts as of ",
                        TemporalUtils.FMT_MDY_AT_HM_A.format(this.today)).addln();
                generateStmpeReport(this.mpeHistory.get(size - 1), builder);
            } catch (final SQLException ex) {
                Log.warning(ex);
                builder.add("Error creating report: ", ex.getMessage());
            }
        } else {
            builder.add("Error querying 'stmpe' table.");
        }

        final JTextArea area = new JTextArea(builder.toString());
        area.setFont(Skin.MONO_12_FONT);
        this.center.add(new JScrollPane(area));
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Generates a report of Math Placement Tool submissions yesterday and installs it in the center panel.
     */
    private void generateViewMptYeterdayReport() {

        this.center.removeAll();
        final HtmlBuilder builder = new HtmlBuilder(100);

        final int size = this.mpeHistory.size();

        if (size > 1) {
            final LocalDate yesterday = this.today.toLocalDate().minusDays(1L);

            try {
                builder.addln("Math Placement Tool attempts for ", TemporalUtils.FMT_MDY.format(yesterday)).addln();
                generateStmpeReport(this.mpeHistory.get(size - 2), builder);
                repaint();
            } catch (final SQLException ex) {
                Log.warning(ex);
                builder.add("Error creating report: ", ex.getMessage());
            }
        } else {
            builder.add("Error querying 'stmpe' table.");
        }

        final JTextArea area = new JTextArea(builder.toString());
        area.setFont(Skin.MONO_12_FONT);
        this.center.add(new JScrollPane(area));
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Generates a report of Math Placement Tool submissions today and installs it in the center panel.
     */
    private void generateViewMptPastNDaysReport() {

        this.center.removeAll();
        final HtmlBuilder builder = new HtmlBuilder(100);

        final int size = this.mpeHistory.size();

        if (size > 4) {
            final LocalDate d3 = this.today.toLocalDate().minusDays(1L);
            final LocalDate d2 = d3.minusDays(1L);
            final LocalDate d1 = d2.minusDays(1L);

            try {
                builder.addln("Math Placement Tool attempts for ", TemporalUtils.FMT_MDY.format(d1)).addln();
                generateStmpeReport(this.mpeHistory.get(size - 4), builder);
                builder.addln();
                builder.addln("Math Placement Tool attempts for ", TemporalUtils.FMT_MDY.format(d2)).addln();
                generateStmpeReport(this.mpeHistory.get(size - 3), builder);
                builder.addln();
                builder.addln("Math Placement Tool attempts for ", TemporalUtils.FMT_MDY.format(d3)).addln();
                generateStmpeReport(this.mpeHistory.get(size - 2), builder);
            } catch (final SQLException ex) {
                Log.warning(ex);
                builder.add("Error creating report: ", ex.getMessage());
            }
        } else {
            builder.add("Error querying 'stmpe' table.");
        }

        final JTextArea area = new JTextArea(builder.toString());
        area.setFont(Skin.MONO_12_FONT);
        this.center.add(new JScrollPane(area));
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Generates a report of ELM Exam submissions today and installs it in the center panel.
     */
    private void generateViewElmTodayReport() {

        this.center.removeAll();
        final HtmlBuilder builder = new HtmlBuilder(100);

        final int size = this.elmHistory.size();

        if (size > 0) {
            try {
                builder.addln("Today's ELM Exam attempts as of ",
                        TemporalUtils.FMT_MDY_AT_HM_A.format(this.today)).addln();
                generateStexamReport(this.elmHistory.get(size - 1), builder);
            } catch (final SQLException ex) {
                Log.warning(ex);
                builder.add("Error creating report: ", ex.getMessage());
            }
        } else {
            builder.add("Error querying 'stexam' table.");
        }

        final JTextArea area = new JTextArea(builder.toString());
        area.setFont(Skin.MONO_12_FONT);
        this.center.add(new JScrollPane(area));
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Generates a report of ELM Exam submissions yesterday and installs it in the center panel.
     */
    private void generateViewElmYeterdayReport() {

        this.center.removeAll();
        final HtmlBuilder builder = new HtmlBuilder(100);

        final int size = this.elmHistory.size();

        if (size > 1) {
            final LocalDate yesterday = this.today.toLocalDate().minusDays(1L);

            try {
                builder.addln("ELM Exam attempts for ", TemporalUtils.FMT_MDY.format(yesterday)).addln();
                generateStexamReport(this.elmHistory.get(size - 2), builder);
            } catch (final SQLException ex) {
                Log.warning(ex);
                builder.add("Error creating report: ", ex.getMessage());
            }
        } else {
            builder.add("Error querying 'stexam' table.");
        }

        final JTextArea area = new JTextArea(builder.toString());
        area.setFont(Skin.MONO_12_FONT);
        this.center.add(new JScrollPane(area));
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Generates a report of ELM Exam submissions today and installs it in the center panel.
     */
    private void generateViewElmPastNDaysReport() {

        this.center.removeAll();
        final HtmlBuilder builder = new HtmlBuilder(100);

        final int size = this.elmHistory.size();

        if (size > 4) {
            final LocalDate d3 = this.today.toLocalDate().minusDays(1L);
            final LocalDate d2 = d3.minusDays(1L);
            final LocalDate d1 = d2.minusDays(1L);

            try {
                builder.addln("ELM Exam attempts for ", TemporalUtils.FMT_MDY.format(d1)).addln();
                generateStexamReport(this.elmHistory.get(size - 4), builder);
                builder.addln();
                builder.addln("ELM Exam attempts for ", TemporalUtils.FMT_MDY.format(d2)).addln();
                generateStexamReport(this.elmHistory.get(size - 3), builder);
                builder.addln();
                builder.addln("ELM Exam attempts for ", TemporalUtils.FMT_MDY.format(d3)).addln();
                generateStexamReport(this.elmHistory.get(size - 2), builder);
            } catch (final SQLException ex) {
                Log.warning(ex);
                builder.add("Error creating report: ", ex.getMessage());
            }
        } else {
            builder.add("Error querying 'stexam' table.");
        }

        final JTextArea area = new JTextArea(builder.toString());
        area.setFont(Skin.MONO_12_FONT);
        this.center.add(new JScrollPane(area));
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Generates a report of Precalculus Tutorial Exam submissions today and installs it in the center panel.
     */
    private void generateViewPreTodayReport() {

        this.center.removeAll();
        final HtmlBuilder builder = new HtmlBuilder(100);

        final int size = this.preHistory.size();

        if (size > 0) {
            try {
                builder.addln("Today's Precalculus Tutorial Exam attempts as of ",
                        TemporalUtils.FMT_MDY_AT_HM_A.format(this.today)).addln();
                generateStexamReport(this.preHistory.get(size - 1), builder);
            } catch (final SQLException ex) {
                Log.warning(ex);
                builder.add("Error creating report: ", ex.getMessage());
            }
        } else {
            builder.add("Error querying 'stexam' table.");
        }

        final JTextArea area = new JTextArea(builder.toString());
        area.setFont(Skin.MONO_12_FONT);
        this.center.add(new JScrollPane(area));
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Generates a report of Precalculus Tutorial Exam submissions yesterday and installs it in the center panel.
     */
    private void generateViewPreYeterdayReport() {

        this.center.removeAll();
        final HtmlBuilder builder = new HtmlBuilder(100);

        final int size = this.preHistory.size();

        if (size > 1) {
            final LocalDate yesterday = this.today.toLocalDate().minusDays(1L);

            try {
                builder.addln("Precalculus Tutorial Exam attempts for ",
                        TemporalUtils.FMT_MDY.format(yesterday)).addln();
                generateStexamReport(this.preHistory.get(size - 2), builder);
            } catch (final SQLException ex) {
                Log.warning(ex);
                builder.add("Error creating report: ", ex.getMessage());
            }
        } else {
            builder.add("Error querying 'stexam' table.");
        }

        final JTextArea area = new JTextArea(builder.toString());
        area.setFont(Skin.MONO_12_FONT);
        this.center.add(new JScrollPane(area));
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Generates a report of Precalculus Tutorial Exam submissions today and installs it in the center panel.
     */
    private void generateViewPrePastNDaysReport() {

        this.center.removeAll();
        final HtmlBuilder builder = new HtmlBuilder(100);

        final int size = this.preHistory.size();

        if (size > 4) {
            final LocalDate d3 = this.today.toLocalDate().minusDays(1L);
            final LocalDate d2 = d3.minusDays(1L);
            final LocalDate d1 = d2.minusDays(1L);

            try {
                builder.addln("Precalculus Tutorial Exam attempts for ", TemporalUtils.FMT_MDY.format(d1)).addln();
                generateStexamReport(this.preHistory.get(size - 4), builder);
                builder.addln();
                builder.addln("Precalculus Tutorial Exam attempts for ", TemporalUtils.FMT_MDY.format(d2)).addln();
                generateStexamReport(this.preHistory.get(size - 3), builder);
                builder.addln();
                builder.addln("Precalculus Tutorial Exam attempts for ", TemporalUtils.FMT_MDY.format(d3)).addln();
                generateStexamReport(this.preHistory.get(size - 2), builder);
            } catch (final SQLException ex) {
                Log.warning(ex);
                builder.add("Error creating report: ", ex.getMessage());
            }
        } else {
            builder.add("Error querying 'stexam' table.");
        }

        final JTextArea area = new JTextArea(builder.toString());
        area.setFont(Skin.MONO_12_FONT);
        this.center.add(new JScrollPane(area));
        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Generates a report of placement tool submissions.
     *
     * @param exams   the list of submission records
     * @param builder the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error querying the database
     */
    private void generateStmpeReport(final Collection<RawStmpe> exams, final HtmlBuilder builder)
            throws SQLException {

        final int numExams = exams.size();

        if (exams.isEmpty()) {
            builder.addln("No exams found.");
        } else {
            if (exams.size() == 1) {
                builder.addln("One exam found.");
            } else {
                builder.addln(Integer.toString(numExams), " exams found.");
            }
            builder.addln();

            builder.addln("STUDENT ID NAME                          ACT  SAT  Placed  Validated  Version");
            builder.addln("---------- ----                          ---  ---  ------  ---------  -------");

            for (final RawStmpe exam : exams) {
                final RawStudent stu = RawStudentLogic.query(this.cache, exam.stuId, false);
                final List<RawMpeCredit> credit =
                        RawMpeCreditLogic.queryByExam(this.cache, exam.serialNbr);
                final List<RawMpecrDenied> denied =
                        RawMpecrDeniedLogic.queryByExam(this.cache, exam.serialNbr);
                final List<RawStsurveyqa> survey = RawStsurveyqaLogic
                        .queryLatestByStudentProfile(this.cache, exam.stuId, exam.version);

                builder.add(exam.stuId).add("  ");
                String name = exam.lastName + ", " + exam.firstName;
                if (name.length() > 29) {
                    name = name.substring(0, 29);
                }
                builder.add(name);
                for (int i = name.length(); i < 29; ++i) {
                    builder.add(' ');
                }
                builder.add(' ');

                if (stu == null) {
                    builder.add("???  ???    ");
                } else {
                    if (stu.actScore == null) {
                        builder.add("     ");
                    } else {
                        final String actStr = stu.actScore.toString();
                        builder.add(actStr);
                        for (int i = actStr.length(); i < 5; ++i) {
                            builder.add(' ');
                        }
                    }
                    if (stu.satScore == null) {
                        builder.add("       ");
                    } else {
                        final String satStr = stu.satScore.toString();
                        builder.add(satStr);
                        for (int i = satStr.length(); i < 7; ++i) {
                            builder.add(' ');
                        }
                    }
                }

                builder.add(exam.placed).add("         ");
                if (exam.howValidated == null) {
                    builder.add("        ");
                } else {
                    builder.add(exam.howValidated).add("       ");
                }

                builder.addln(exam.version);

                builder.addln("  Scores:  A=", exam.stsA, ", 117=", exam.sts117, ", 118=", exam.sts118, ", 124=",
                        exam.sts124, ", 125=", exam.sts125, ", 126=", exam.sts126);

                if (credit.isEmpty()) {
                    builder.addln("  Credit:  None");
                } else {
                    Collections.sort(credit);
                    builder.add("  Credit:  ");
                    for (final RawMpeCredit row : credit) {
                        builder.add(row.course).add('-').add(row.examPlaced);
                    }
                    builder.addln();
                }

                if (denied.isEmpty()) {
                    builder.addln("  Denied:  None");
                } else {
                    Collections.sort(denied);
                    builder.add("  Denied:  ");
                    for (final RawMpecrDenied row : denied) {
                        builder.add(row.course).add('-').add(row.examPlaced).add('-').add(row.whyDenied);
                    }
                    builder.addln();
                }

                if (survey.isEmpty()) {
                    builder.addln("  Survey:  None");
                } else {
                    builder.add("  Survey:  ");

                    for (final RawStsurveyqa row : survey) {
                        if (row.surveyNbr.intValue() == 1) {
                            final String ans = row.stuAnswer;
                            if (ans != null) {
                                builder.add("        Time spent preparing: ");

                                switch (ans) {
                                    case "1" -> builder.add("None at all");
                                    case "2" -> builder.add("Less than 2 hours");
                                    case "3" -> builder.add("2-5 hours");
                                    case "4" -> builder.add("5-10 hours");
                                    case "5" -> builder.add("More than 10 hours");
                                    default -> builder.add(ans);
                                }

                                builder.addln().add("           ");
                                break;
                            }
                        }
                    }

                    for (final RawStsurveyqa row : survey) {
                        if (row.surveyNbr.intValue() == 2) {
                            final String ans = row.stuAnswer;
                            if (ans != null) {
                                builder.add("   Resources used to prepare: ");

                                switch (ans) {
                                    case "0" -> builder.add("Did not prepare");
                                    case "1" -> builder.add("Past course materials");
                                    case "2" -> builder.add("Textbooks");
                                    case "3" -> builder.add("Past course materials, Textbooks");
                                    case "4" -> builder.add("Tutoring");
                                    case "5" -> builder.add("Past course materials, Tutoring");
                                    case "6" -> builder.add("Textbooks, Tutoring");
                                    case "7" -> builder.add("Past course materials, Textbooks, Tutoring");
                                    case "8" -> builder.add("Web study guide");
                                    case "9" -> builder.add("Web study guide, Past course materials");
                                    case "10" -> builder.add("Web study guide, Textbooks");
                                    case "11" -> builder.add("Web study guide, Past course materials, Textbooks");
                                    case "12" -> builder.add("Web study guide, Tutoring");
                                    case "13" -> builder.add("Web study guide, Past course materials, Tutoring");
                                    case "14" -> builder.add("Web study guide, Textbooks, Tutoring");
                                    case "15" -> builder.add(
                                            "Web study guide, Past course materials, Textbooks, Tutoring");
                                    default -> builder.add(ans);
                                }

                                builder.addln().add("           ");
                                break;
                            }
                        }
                    }

                    for (final RawStsurveyqa row : survey) {
                        if (row.surveyNbr.intValue() == 1) {
                            final String ans = row.stuAnswer;
                            if (ans != null) {
                                builder.add(" Time since last math course: ");

                                switch (ans) {
                                    case "1" -> builder.add("Currently enrolled");
                                    case "2" -> builder.add("Less than 3 months");
                                    case "3" -> builder.add("3-9 months");
                                    case "4" -> builder.add("9 months - 2 years");
                                    case "5" -> builder.add("2-5 years");
                                    case "6" -> builder.add("More than 5 years");
                                    default -> builder.add(ans);
                                }

                                builder.addln().add("           ");
                                break;
                            }
                        }
                    }

                    builder.add("        Highest course taken: ");

                    int max = 0;
                    for (final RawStsurveyqa row : survey) {
                        final String ans = row.stuAnswer;

                        if (row.surveyNbr.intValue() >= 5) {
                            try {
                                max = Math.max(max, Integer.parseInt(ans));
                            } catch (final NumberFormatException ex) {
                                Log.warning(ex);
                            }
                        }
                    }

                    if (max == 0) {
                        builder.addln("did not take math");
                    } else if (max == 1) {
                        builder.addln("High School Other");
                    } else if (max == 2) {
                        builder.addln("College Other");
                    } else if (max == 3) {
                        builder.addln("College Math for Liberal Arts");
                    } else if (max == 4) {
                        builder.addln("High School Algebra I");
                    } else if (max == 5) {
                        builder.addln("High School Integrated Math 1");
                    } else if (max == 6) {
                        builder.addln("High School Geometry");
                    } else if (max == 7) {
                        builder.addln("High School Integrated Math 2");
                    } else if (max == 8) {
                        builder.addln("College Elementary Algebra");
                    } else if (max == 9) {
                        builder.addln("High School Algebra II");
                    } else if (max == 10) {
                        builder.addln("High School Integrated Math 3");
                    } else if (max == 11) {
                        builder.addln("College Intermediate Algebra");
                    } else if (max == 12) {
                        builder.addln("College Algebra");
                    } else if (max == 13) {
                        builder.addln("High School Pre-Calculus/Trig");
                    } else if (max == 14) {
                        builder.addln("College Pre-Calculus/Trig");
                    } else if (max == 15) {
                        builder.addln("High School Calculus");
                    } else if (max == 16) {
                        builder.addln("College Calculus");
                    } else {
                        builder.addln(Integer.toString(max));
                    }
                }

                builder.addln();
            }
        }
    }

    /**
     * Generates a report of ELM or Precalculus tutorial exam submissions.
     *
     * @param exams   the list of submission records
     * @param builder the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error querying the database
     */
    private void generateStexamReport(final Collection<RawStexam> exams, final HtmlBuilder builder)
            throws SQLException {

        final int numExams = exams.size();

        if (exams.isEmpty()) {
            builder.addln("No exams found.");
        } else {
            if (exams.size() == 1) {
                builder.addln("One exam found.");
            } else {
                builder.addln(Integer.toString(numExams), " exams found.");
            }
            builder.addln();

            builder.addln(
                    "STUDENT ID NAME                          COURSE  UNIT  SCORE  MASTERY  PASSED  VERSION");
            builder.addln(
                    "---------- ----                          ------  ----  -----  -------  ------  -------");

            for (final RawStexam exam : exams) {
                final RawStudent stu = RawStudentLogic.query(this.cache, exam.stuId, false);

                builder.add(exam.stuId).add("  ");
                String name = stu == null ? CoreConstants.EMPTY : stu.lastName + ", " + stu.firstName;
                if (name.length() > 29) {
                    name = name.substring(0, 29);
                }
                builder.add(name);
                for (int i = name.length(); i < 29; ++i) {
                    builder.add(' ');
                }
                builder.add(' ');

                final String courseStr = exam.course;
                builder.add(courseStr);
                for (int i = courseStr.length(); i < 8; ++i) {
                    builder.add(' ');
                }

                final String unitStr = exam.unit.toString();
                builder.add(unitStr);
                for (int i = unitStr.length(); i < 6; ++i) {
                    builder.add(' ');
                }

                final String scoreStr = exam.examScore.toString();
                builder.add(scoreStr);
                for (int i = scoreStr.length(); i < 7; ++i) {
                    builder.add(' ');
                }

                if (exam.masteryScore == null) {
                    builder.add("         ");
                } else {
                    final String masteryStr = exam.masteryScore.toString();
                    builder.add(masteryStr);
                    for (int i = masteryStr.length(); i < 11; ++i) {
                        builder.add(' ');
                    }
                }

                builder.add(exam.passed).add("     ");
                builder.addln(exam.version);
            }
        }
    }
}
