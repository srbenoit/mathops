package dev.mathops.db.old.logic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.rec.TermWeekRec;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Logic related to the term calendar.  This class draws data from the SEMESTER_CALENDAR, CAMPUS_CALENDAR, and TERM
 * tables.  It has the ability to generate an ordered list of the non-holiday weekdays in the term, and to take a given
 * date and find the next "open" date in the term, if any.
 */
public enum TermCalendarLogic {
    ;

    /**
     * Returns the first class date in the active term.
     *
     * @param cache the data cache
     * @return the first class date
     * @throws SQLException if there is an error accessing the database
     */
    public static LocalDate getFirstClassDate(final Cache cache) throws SQLException {

        LocalDate result = null;
        final SystemData systemData = cache.getSystemData();

        // Gather a list of holidays
        final List<RawCampusCalendar> holidays = systemData.getCampusCalendarsByType(RawCampusCalendar.DT_DESC_HOLIDAY);
        final int size = holidays.size();
        final Collection<LocalDate> holidayDates = new ArrayList<>(size);
        for (final RawCampusCalendar row : holidays) {
            holidayDates.add(row.campusDt);
        }

        final List<TermWeekRec> all = systemData.getTermWeeks();

        for (final TermWeekRec test : all) {
            if (test.weekNbr.intValue() == 1) {
                result = test.startDate;
                if (result.getDayOfWeek() == DayOfWeek.SATURDAY) {
                    result = result.plusDays(2L);
                } else if (result.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    result = result.plusDays(1L);
                }
                while (holidayDates.contains(result)) {
                    result = result.plusDays(1L);
                }
                break;
            }
        }

        return result;
    }

    /**
     * Returns the last class date in the active term.
     *
     * @param cache the data cache
     * @return the last class date
     * @throws SQLException if there is an error accessing the database
     */
    public static LocalDate getLastClassDate(final Cache cache) throws SQLException {

        LocalDate result = null;
        final SystemData systemData = cache.getSystemData();

        // Gather a list of holidays
        final List<RawCampusCalendar> holidays = systemData.getCampusCalendarsByType(RawCampusCalendar.DT_DESC_HOLIDAY);
        final int size = holidays.size();
        final Collection<LocalDate> holidayDates = new ArrayList<>(size);
        for (final RawCampusCalendar row : holidays) {
            holidayDates.add(row.campusDt);
        }

        final List<TermWeekRec> all = systemData.getTermWeeks();
        final int numSemesterRows = all.size();
        if (numSemesterRows > 2) {
            final TermWeekRec lastClassWeek = all.get(numSemesterRows - 2);

            result = lastClassWeek.endDate;
            if (result.getDayOfWeek() == DayOfWeek.SATURDAY) {
                result = result.minusDays(1L);
            }
            while (holidayDates.contains(result)) {
                result = result.minusDays(1L);
            }
        }

        return result;
    }

    /**
     * Generates a list of the dates the center is "open".  That is, all dates between the first class date and the last
     * class date that are weekdays and are not holidays.
     *
     * @param cache the data cache
     * @return the list of "open" days in the term (ordered by date)
     * @throws SQLException if there is an error accessing the database
     */
    public static List<LocalDate> getOpenDates(final Cache cache) throws SQLException {

        final List<LocalDate> result = new ArrayList<>(60);
        final SystemData systemData = cache.getSystemData();

        // Gather a list of holidays
        final List<RawCampusCalendar> holidays = systemData.getCampusCalendarsByType(RawCampusCalendar.DT_DESC_HOLIDAY);
        final int size = holidays.size();
        final Collection<LocalDate> holidayDates = new ArrayList<>(size);
        for (final RawCampusCalendar row : holidays) {
            holidayDates.add(row.campusDt);
        }

        final List<TermWeekRec> all = systemData.getTermWeeks();
        if (!all.isEmpty()) {
            final int last = all.getLast().weekNbr.intValue();

            for (final TermWeekRec row : all) {
                final int weekNbr = row.weekNbr.intValue();
                if (weekNbr == 0 || weekNbr == last) {
                    continue;
                }

                LocalDate date = row.startDate;
                final DayOfWeek day = date.getDayOfWeek();
                if (day == DayOfWeek.SATURDAY) {
                    date = date.plusDays(2L);
                } else if (day == DayOfWeek.SUNDAY) {
                    date = date.plusDays(1L);
                }
                while (!date.isAfter(row.endDate)) {
                    if (!holidayDates.contains(date)) {
                        result.add(date);
                    }
                    final DayOfWeek day2 = date.getDayOfWeek();
                    if (day2 == DayOfWeek.FRIDAY) {
                        date = date.plusDays(3L);
                    } else {
                        date = date.plusDays(1L);
                    }
                }

                if (result.size() > 100) {
                    Log.warning("Found 100 'open' dates in a single term - error!");
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Given a date, finds an "open" date some number of work days after that date.
     *
     * @param cache the data cache
     * @param date  the date
     * @param count the number of times to find the next open days (1 to find the next open day, 2 to find the second
     *              open day after the given date, etc.) - must be positive and at least 1
     * @return the specified "open" date after the given date; {@code null} if the search ran past the end of the term
     *         before finding the requested open date
     * @throws SQLException if there is an error accessing the database
     */
    public static LocalDate nextOpenDay(final Cache cache, final ChronoLocalDate date, final int count)
            throws SQLException {

        final List<LocalDate> openDates = getOpenDates(cache);
        final int size = openDates.size();

        // Find the first "open" date that is after the specified date
        int indexOfFirstOpen = -1;
        for (int i = 0; i < size; ++i) {
            if (openDates.get(i).isAfter(date)) {
                indexOfFirstOpen = i;
                break;
            }
        }

        LocalDate result = null;
        if (indexOfFirstOpen > -1) {
            final int wantedIndex = indexOfFirstOpen + count - 1;
            if (wantedIndex < size) {
                result = openDates.get(wantedIndex);
            }
        }

        return result;
    }
}
