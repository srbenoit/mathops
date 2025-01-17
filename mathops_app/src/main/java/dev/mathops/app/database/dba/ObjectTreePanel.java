package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * A panel that displays database objects (schemas, tables, views, roles, tablespaces).  Selecting one of these objects
 * notifies the owning {@code MainWindow}, which can update window contents accordingly.
 */
final class ObjectTreePanel extends JPanel implements TreeSelectionListener {

    private static final String[] LEGACY_TABLE_NAMES = {"admin_hold", "applicant", "bogus_mapping", "calcs",
            "campus_calendar", "challenge_fee", "client_pc", "cohort", "course", "crsection", "csection", "cunit",
            "cuobjective", "cusection", "ddcode", "dddomain", "ddelement", "ddelement_report", "ddelement_screen",
            "ddreport", "ddscreen", "ddtable", "ddtable_element", "delphi", "delpli_check", "discipline", "dont_submit",
            "dup_registr", "etext", "etext_course", "etext_key", "exam", "examqa", "except_stu", "fcr (view)",
            "fcrstu (view)", "fcr_student", "ffr_trns", "final_croll", "grade_roll", "grading_std", "high_schools",
            "hold_type", "homework", "index_descriptions", "index_frequency", "logins", "mastery_attempt",
            "mastery_attempt_qa", "mastery_exam", "mdstudent", "milestone", "milestone_appeal", "mpe", "mpe_credit",
            "mpe_log", "mpecr_denied", "mpscorequeue", "msg", "msg_lookup", "newstu", "next_campus_calendar",
            "next_csection", "next_milestone", "next_remote_mpe", "next_semester_calendar", "pace_appeals",
            "pace_track_rule", "pacing_rules", "pacing_structure", "parameters", "pending_exam", "plc_fee", "prereq",
            "prev_extensions", "prev_milestone_appeal", "prev_stlmiss", "prev_stlock", "prev_stmilestone",
            "prev_stterm", "remote_mpe", "report_perms", "resource", "semester_calendar", "special_stus", "stc",
            "stchallenge", "stchallengeqa", "stcourse", "stcunit", "stcuobjective", "std_milestone", "stetext",
            "stexam", "sthomework", "sthwqa", "stmathplan", "stmdscores", "stmilestone", "stmpe", "stmpeqa", "stmsg",
            "stpace_summary", "stqa", "stresource", "stsurveyqa", "stterm", "stu_course_mastery", "stu_std_mastery",
            "stu_unit_mastery", "student", "stuid_tables", "stvisit", "surveyqa", "sysmenuitems", "sysmenus", "term",
            "testing_centers", "tree_path", "user_clearance", "users", "which_db", "zip_code"};

    /** The owning {@code MainWindow}. */
    private final MainWindow owner;

    /** The node that holds schemas. */
    private final DefaultMutableTreeNode schemas;

    /** The tree. */
    private final JTree tree;

    /**
     * Constructs a new {@code ObjectTreePanel}.
     *
     * @param theOwner the owning {@code MainWindow}
     */
    ObjectTreePanel(final MainWindow theOwner) {

        super(new StackedBorderLayout());

        final Border etchedBorder = BorderFactory.createEtchedBorder();
        setBorder(etchedBorder);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int minWidth = Math.max(250, screen.width / 12);
        final int w = Math.min(screen.width, minWidth);
        final int minHeight = Math.max(700, screen.height / 2);
        final int h = Math.min(screen.height, minHeight);
        setPreferredSize(new Dimension(w, h));

        this.owner = theOwner;

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        this.schemas = new DefaultMutableTreeNode("Schemas");

        root.add(this.schemas);

        final DefaultTreeModel treeModel = new DefaultTreeModel(root);

        this.tree = new JTree(treeModel);
        this.tree.setRootVisible(false);
        this.tree.setShowsRootHandles(true);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        final JScrollPane treeScroll = new JScrollPane(this.tree);
        add(treeScroll, StackedBorderLayout.CENTER);

    }

    /**
     * Initializes the window.  This is separated from the constructor because it leaks references to this object which
     * is not completely constructed during the constructor.
     */
    void init() {

        populateTree();
        this.tree.expandRow(0);
        this.tree.addTreeSelectionListener(this);
    }

    /**
     * Populates the tree.
     */
    private void populateTree() {

        final DefaultMutableTreeNode analytics = new DefaultMutableTreeNode("analytics");
        this.schemas.add(analytics);

        final DefaultMutableTreeNode external = new DefaultMutableTreeNode("extern");
        this.schemas.add(external);

        final DefaultMutableTreeNode legacy = new DefaultMutableTreeNode("legacy");
        this.schemas.add(legacy);

        for (final String name : LEGACY_TABLE_NAMES) {
            final MutableTreeNode tableNode = new DefaultMutableTreeNode(name);
            legacy.add(tableNode);
        }

        final DefaultMutableTreeNode main = new DefaultMutableTreeNode("main");
        this.schemas.add(main);

        final DefaultMutableTreeNode mathops = new DefaultMutableTreeNode("mathops");
        this.schemas.add(mathops);

        final DefaultMutableTreeNode term = new DefaultMutableTreeNode("term");
        this.schemas.add(term);
    }

    /**
     * Called when the tree selection changes.
     *
     * @param e the event that characterizes the change
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        final TreePath selection = this.tree.getSelectionPath();
        if (selection == null) {
            this.owner.clearDisplay();
        } else {
            final Object[] path = selection.getPath();

            if (path.length == 3) {
                final String schemaName = path[2].toString();
                this.owner.schemaSelected(schemaName);
            } else if (path.length == 4) {
                final String schemaName = path[2].toString();
                final String name = path[3].toString();

                if (name.endsWith(" (view)")) {
                    final int len = name.length();
                    final String viewName = name.substring(0, len - 7);
                    this.owner.viewSelected(schemaName, viewName);
                } else {
                    this.owner.tableSelected(schemaName, name);
                }
            } else {
                this.owner.clearDisplay();
            }
        }
    }
}
