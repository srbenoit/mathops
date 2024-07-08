package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawDisciplineLogic;
import dev.mathops.db.old.rawrecord.RawDiscipline;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;

/**
 * A panel that shows a students disciplinary history.
 */
class StuDisciplinePanel extends AdminPanelBase implements ActionListener {

    /** An action command. */
    static final String ADD_CMD = "ADD";

    /** A button action command. */
    private static final String SHOW_CMD = "SHOW";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8629221992069383568L;

    /** The data cache. */
    private final Cache cache;

    /** The center panel. */
    private final JPanel cardPane;

    /** The card layout. */
    private final CardLayout cards;

    /** The card that shows the list of incidents, with an "add" button. */
    private final DisciplineIncidentsCard incidentListCard;

    /** A card to add a new incident. */
    private final DisciplineAddIncidentCard addIncidentCard;

    /** The current student data. */
    private StudentData currentStudentData;

    /**
     * Constructs a new {@code AdminDisciplinePanel}.
     *
     * @param theCache         the database connection
     */
    StuDisciplinePanel(final Cache theCache) {

        super();
        setBackground(Skin.WHITE);

        this.cache = theCache;

        add(makeHeader("Academic Misconduct", false), BorderLayout.NORTH);

        // Center is a "card layout" where one card shows the student's incidents, and the other
        // supports the creation of a new incident. We may add a future card to support updates
        // to an existing incident record, depending on permissions level.

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Skin.WHITE);
        add(this.cardPane, BorderLayout.CENTER);

        // Card to show all incidents on record
        this.incidentListCard = new DisciplineIncidentsCard(this);

        // Card to add a new incident
        this.addIncidentCard = new DisciplineAddIncidentCard(this);

        //

        this.cardPane.add(this.incidentListCard, SHOW_CMD);
        this.cardPane.add(this.addIncidentCard, ADD_CMD);
        this.cards.show(this.cardPane, SHOW_CMD);
    }

    /**
     * Sets the selected student data.
     *
     * @param data the selected student data
     */
    public void setSelectedStudent(final StudentData data) {

        clearDisplay();

        this.currentStudentData = data;

        if (data == null) {
            this.addIncidentCard.setStudentId(null);
        } else {
            this.addIncidentCard.setStudentId(data.student.stuId);
            populateDisplay(data);
        }
    }

    /**
     * Clears all displayed fields.
     */
    private void clearDisplay() {

        this.incidentListCard.clear();
        this.addIncidentCard.reset();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        this.incidentListCard.populateDisplay(data);
        this.addIncidentCard.reset();
        this.cards.show(this.cardPane, SHOW_CMD);

        this.addIncidentCard.setStudentId(data.student.stuId);
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (ADD_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, ADD_CMD);
        }
    }

    /**
     * Creates a new discipline record. On success, the "add incident" card is reset and the "list of incidents" card is
     * refreshed and re-displayed.
     *
     * @param rec the discipline record
     * @return an error message on failure; null on success
     */
    String createRecord(final RawDiscipline rec) {

        String error = null;

        try {
            RawDisciplineLogic.INSTANCE.insert(this.cache, rec);

            // Add the new record and re-populate the list display
            this.currentStudentData.studentDisciplines.add(rec);
            this.incidentListCard.clear();
            this.incidentListCard.populateDisplay(this.currentStudentData);
            this.addIncidentCard.reset();
            this.cards.show(this.cardPane, SHOW_CMD);
        } catch (final SQLException ex) {
            error = ex.getMessage();
        }

        return error;
    }

    /**
     * Cancels the "add" action, returning to the incidents list card.
     */
    void cancelAdd() {

        this.cards.show(this.cardPane, SHOW_CMD);
    }
}
