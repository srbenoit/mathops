package dev.mathops.app.adm.management;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawCourse;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A card with assessment-related data.
 */
class AssessCard extends AdmPanelBase implements ActionListener {

    /** An action command. */
    private static final String REFRESH = "REFRESH";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7258055062067929993L;

    /**
     * Constructs a new {@code AssessCard}.
     *
     * @param theCache         the data cache
     */
    AssessCard(final Cache theCache) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_YELLOW);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_YELLOW);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        panel.add(makeHeader("Assessments", false), BorderLayout.NORTH);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Skin.BIG_BUTTON_16_FONT);
        tabs.setBackground(Skin.OFF_WHITE_YELLOW);
        panel.add(tabs, BorderLayout.CENTER);

        List<RawCourse> courses;
        try {
            courses = theCache.getSystemData().getCourses();
        } catch (final SQLException ex) {
            Log.warning(ex);
            courses = new ArrayList<>(0);
        }

        final AssessAssignmentsPanel assignments = new AssessAssignmentsPanel(courses);
        tabs.addTab("Assignments", assignments);

        final AssessExamsPanel exams = new AssessExamsPanel(courses);
        tabs.addTab("Exams", exams);

        final AssessMasteryExamsPanel masteryExams = new AssessMasteryExamsPanel(courses);
        tabs.addTab("Mastery Exams", masteryExams);
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (REFRESH.equals(cmd)) {
            processRefresh();
        }
    }

    /**
     * Sets focus.
     */
    void focus() {

        // No action
    }

    /**
     * Resets the card to accept data for a new loan.
     */
    void reset() {

        // TODO:
    }

    /**
     * Called when the "Refresh" button is pressed.
     */
    private void processRefresh() {

        reset();
    }
}
