package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawAdminHold;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * A panel that shows student administrative holds.
 */
class StuHoldsPanel extends AdminPanelBase implements ActionListener {

    /** A button action command. */
    private static final String SHOW_CMD = "SHOW";

    /** An action command. */
    private static final String ADD_CMD = "ADD";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7865989911136894122L;

    /** The data cache. */
    private final Cache cache;

    /** The center panel. */
    private final JPanel cardPane;

    /** The card layout. */
    private final CardLayout cards;

    /** The card that shows the list of holds, with an "add" button. */
    private final HoldsCard holdsCard;

    /** A card to add a new hold. */
    private final HoldsAddCard addHoldCard;

    /** The current student data. */
    private StudentData currentStudentData;

    /**
     * Constructs a new {@code AdminHoldsPanel}.
     *
     * @param theCache         the data cache
     * @param theFixed         the fixed data
     */
    StuHoldsPanel(final Cache theCache, final FixedData theFixed) {

        super();
        setBackground(Skin.WHITE);

        this.cache = theCache;

        add(makeHeader("Administrative Holds", false), BorderLayout.NORTH);

        // Center is a "card layout" where one card shows the student's incidents, and the other
        // supports the creation of a new incident. We may add a future card to support updates
        // to an existing incident record, depending on permissions level.

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Skin.WHITE);
        add(this.cardPane, BorderLayout.CENTER);

        // Card to show all incidents on record
        this.holdsCard = new HoldsCard(theCache, theFixed, this);

        // Card to add a new incident
        this.addHoldCard = new HoldsAddCard(theCache, this);

        this.cardPane.add(this.holdsCard, SHOW_CMD);
        this.cardPane.add(this.addHoldCard, ADD_CMD);
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
            this.addHoldCard.setStudentId(null);
        } else {
            this.addHoldCard.setStudentId(data.student.stuId);
            populateDisplay(data);
        }
    }

    /**
     * Clears all displayed fields.
     */
    private void clearDisplay() {

        this.holdsCard.clear();
        this.addHoldCard.reset();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    private void populateDisplay(final StudentData data) {

        this.holdsCard.populateDisplay(data);
        this.addHoldCard.reset();
        this.cards.show(this.cardPane, SHOW_CMD);

        this.addHoldCard.setStudentId(data.student.stuId);
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
     * Creates a new hold record. On success, the "add hold" card is reset and the "list of holds" card is refreshed and
     * re-displayed.
     *
     * @param rec the hold record
     * @return an error message on failure; null on success
     */
    String createRecord(final RawAdminHold rec) {

        String error = null;

        final String sql = "INSERT INTO admin_hold "
                + "(stu_id,hold_id,sev_admin_hold,create_dt) VALUES (?,?,?,?)";

        try (final PreparedStatement ps = this.cache.conn.prepareStatement(sql)) {
            ps.setString(1, rec.stuId);
            ps.setString(2, rec.holdId);
            ps.setString(3, rec.sevAdminHold);
            ps.setDate(4, Date.valueOf(rec.createDt));

            final int numRows = ps.executeUpdate();
            if (numRows == 1) {
                this.cache.conn.commit();
            } else {
                error = "Unable to insert record";
                this.cache.conn.rollback();
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            error = "Unable to insert record: " + ex.getMessage();
        }

        if (error == null) {
            String newSeverity = rec.sevAdminHold;

            if ("F".equals(rec.sevAdminHold)) {
                if (!"F".equals(this.currentStudentData.student.sevAdminHold)) {

                    // TODO: Update the student table
                    final String sql2 = "UPDATE student SET sev_admin_hold='F' "
                            + "WHERE stu_id='" + rec.stuId + "'";

                    try (final Statement s = this.cache.conn.createStatement()) {
                        final int numRows = s.executeUpdate(sql2);
                        if (numRows == 1) {
                            this.cache.conn.commit();
                            newSeverity = "F";
                        } else {
                            error = "Unable to update 'sev_admin_hold' on student record";
                            this.cache.conn.rollback();
                        }
                    } catch (final SQLException ex) {
                        Log.warning(ex);
                        error = "Unable to update 'sev_admin_hold' on student record: "
                                + ex.getMessage();
                    }
                }
            } else if ("N".equals(rec.sevAdminHold)
                    && this.currentStudentData.student.sevAdminHold == null) {

                // TODO: Update the student table
                final String sql2 = "UPDATE student SET sev_admin_hold='N' "
                        + "WHERE stu_id='" + rec.stuId + "'";

                try (final Statement s = this.cache.conn.createStatement()) {
                    final int numRows = s.executeUpdate(sql2);
                    if (numRows == 1) {
                        this.cache.conn.commit();
                        newSeverity = "N";
                    } else {
                        error = "Unable to update 'sev_admin_hold' on student record";
                        this.cache.conn.rollback();
                    }
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    error =
                            "Unable to update 'sev_admin_hold' on student record: " + ex.getMessage();
                }
            }

            if (!Objects.equals(newSeverity, rec.sevAdminHold)) {
                this.currentStudentData.student.sevAdminHold = newSeverity;
            }

            // Add the new record and re-populate the list display
            this.currentStudentData.studentHolds.add(rec);
            this.holdsCard.clear();
            this.holdsCard.populateDisplay(this.currentStudentData);
            this.addHoldCard.reset();
            this.cards.show(this.cardPane, SHOW_CMD);
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
