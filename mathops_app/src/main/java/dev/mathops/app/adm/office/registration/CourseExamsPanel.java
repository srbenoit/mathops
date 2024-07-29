package dev.mathops.app.adm.office.registration;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.UserData;
import dev.mathops.app.adm.IZTableCommandListener;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.DbContext;

import javax.swing.JPanel;
import java.awt.CardLayout;
import java.io.Serial;

/**
 * A panel that shows student exams.
 */
public final class CourseExamsPanel extends AdmPanelBase implements IZTableCommandListener<ExamListRow> {

    /** An action command. */
    private static final String LIST_CMD = "LIST";

    /** A button action command. */
    private static final String DETAIL_CMD = "DETAIL";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5026351810311131356L;

    /** The center panel. */
    private final JPanel cardPane;

    /** The card layout. */
    private final CardLayout cards;

    /** The card that shows the list of holds, with an "add" button. */
    private final ExamListCard examsCard;

    /** A card to add a new hold. */
    private final ExamDetailsCard examDetailsCard;

    /**
     * Constructs a new {@code AdminExamsPanel}.
     *
     * @param theCache    the data cache
     * @param liveContext the database context used to access live data
     * @param theFixed    the fixed data
     */
    public CourseExamsPanel(final Cache theCache, final DbContext liveContext, final UserData theFixed) {

        super();
        setBackground(Skin.WHITE);

        add(makeHeader("Exams", false), StackedBorderLayout.NORTH);

        // Center is a "card layout" where one card shows the student's incidents, and the other
        // supports the creation of a new incident. We may add a future card to support updates
        // to an existing incident record, depending on permissions level.

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Skin.WHITE);
        add(this.cardPane, StackedBorderLayout.CENTER);

        // Card to show all exams on record, with a button to open details for each exam
        this.examsCard = new ExamListCard(theCache, this);

        // Card to show details of one exam
        final boolean allowUpdate = theFixed.getClearanceLevel("EXM_CHANS") != null;
        this.examDetailsCard = new ExamDetailsCard(this, theCache, liveContext, allowUpdate);

        this.cardPane.add(this.examsCard, LIST_CMD);
        this.cardPane.add(this.examDetailsCard, DETAIL_CMD);
        this.cards.show(this.cardPane, LIST_CMD);
    }

    /**
     * Sets the selected student.
     *
     * @param data the selected student data
     */
    public void setSelectedStudent(final StudentData data) {

        this.examDetailsCard.reset();

        if (data == null) {
            this.examsCard.clear();
        } else {
            this.examsCard.populateDisplay(data);
        }

        this.cards.show(this.cardPane, LIST_CMD);
    }

    /**
     * Clears all displayed fields.
     */
    public void clearDisplay() {

        // No action
    }

    ///**
    // * Determines the week number
    // *
    // * @param date the date
    // * @param weeks the list of term weeks
    // * @return 0 if the date is before all term week records,the week number of the matching record,
    // *         or one larger than the largest week number if the date is beyond all term weeks
    // */
    // public static int determineWeek(final LocalDate date, final List<TermWeek> weeks) {
    //
    // int result;
    //
    // if (date.isBefore(weeks.get(0).startDate)) {
    // result = 0;
    // } else {
    // final TermWeek last = weeks.get(weeks.size() - 1);
    // result = last.weekInTerm.intValue() + 1;
    //
    // for (final TermWeek test : weeks) {
    // if (!date.isAfter(test.endDate)) {
    // result = test.weekInTerm.intValue();
    // break;
    // }
    // }
    // }
    //
    // return result;
    // }

    /**
     * Called when a button is pressed within a row of a table.
     *
     * @param rowIndex the index of the row (where 0 is the first row below the header)
     * @param rowData  the record corresponding to the row
     * @param cmd      the action command associated with the button
     */
    @Override
    public void commandOnRow(final int rowIndex, final ExamListRow rowData, final String cmd) {

        this.examDetailsCard.setCurrent(rowData);
        this.cards.show(this.cardPane, DETAIL_CMD);
    }

    /**
     * Closes the "details" panel and returns to the list of exams.
     */
    void closeDetails() {

        this.examDetailsCard.reset();
        this.cards.show(this.cardPane, LIST_CMD);
    }
}
