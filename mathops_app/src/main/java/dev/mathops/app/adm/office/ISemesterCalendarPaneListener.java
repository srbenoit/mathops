package dev.mathops.app.adm.office;

import java.time.LocalDate;

/**
 * A listener that will be notified when the user clicks on a date on the calendar.
 */
public interface ISemesterCalendarPaneListener {

    /**
     * Called when the user selects a date.
     *
     * @param date the selected date
     */
    void dateSelected(LocalDate date);
}
