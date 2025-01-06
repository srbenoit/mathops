package dev.mathops.app.adm.office.registration;

import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.office.ISemesterCalendarPaneListener;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * A panel that displays the semester calendar, based on the rows in the "semester_calendar" and "campus_calendar"
 * tables.
 */
public final class SemesterCalendarPane extends JPanel implements MouseListener {

    /** Gap between months. */
    private static final int MONTH_GAP = 3;

    /** Padding under each day box */
    private static final int DAY_PAD = 2;

    /** The list of semester calendar rows. */
    private final List<RawSemesterCalendar> semesterCalendars;

    /** The list of holiday dates. */
    private final List<LocalDate> holidays;

    /** The calculated column width. */
    private int colWidth = 0;

    /** A listener to notify when the user clicks on a date. */
    private ISemesterCalendarPaneListener listener = null;

    /** A rectangle and associated date for mouse click processing. */
    private final List<DateRect> dateRects;

    /**
     * Constructs a new {@code SemesterCalendarPane}.
     *
     * @param cache the cache
     */
    public SemesterCalendarPane(final Cache cache) {

        super();

        setBackground(Skin.LIGHTEST);

        List<RawSemesterCalendar> theSemesterCalendars;
        List<LocalDate> theHolidays;

        try {
            theSemesterCalendars = cache.getSystemData().getSemesterCalendars();
            final List<RawCampusCalendar> campusCalendarRows =
                    cache.getSystemData().getCampusCalendarsByType(RawCampusCalendar.DT_DESC_HOLIDAY);
            final int size = campusCalendarRows.size();
            theHolidays = new ArrayList<>(size);
            for (final RawCampusCalendar row : campusCalendarRows) {
                theHolidays.add(row.campusDt);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query calendar data.");
            theSemesterCalendars = new ArrayList<>(0);
            theHolidays = new ArrayList<>(0);
        }

        this.semesterCalendars = theSemesterCalendars;
        this.holidays = theHolidays;
        this.dateRects = new ArrayList<>(100);

        addMouseListener(this);
    }

    /**
     * Called after the panel has been added to a container, so we can get its graphics context and determine the
     * preferred size.
     */
    public void initialize() {

        // Count up the number of distinct months and week rows are included...
        final EnumSet<Month> months = EnumSet.noneOf(Month.class);
        DayOfWeek day = null;

        int numWeeks = 0;

        // Find the span of dates the semester covers
        final LocalDate start = getFirstDate();
        final LocalDate end = getLastDate();

        // Gather the set of months represented
        Month month = start.getMonth();
        final Month endMonth = end.getMonth();
        while (month != endMonth) {
            months.add(month);
            month = month.plus(1);
        }
        months.add(endMonth);

        // Count the number of calendar weeks covered
        // FIXME: flawed - does not count 2 when week splits between months
        LocalDate date = start;
        while (!date.isAfter(end)) {
            if (day == null || (day == DayOfWeek.SATURDAY && date.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                ++numWeeks;
            }
            day = date.getDayOfWeek();
            date = date.plusDays(1);
        }

        final int numMonths = months.size();

        final Font font = Skin.MEDIUM_13_FONT;
        final Graphics g = getGraphics();
        final FontMetrics metrics = g.getFontMetrics(font);

        final int lineHeight = metrics.getAscent() + metrics.getDescent();

        // For each month, we have a top line, bottom line, and one-line month heading
        // For each week, we have a top line and a two-line entry per day.
        final int calHeight = numMonths * (lineHeight + 2) + (numMonths - 1) * MONTH_GAP
                + numWeeks * (lineHeight + DAY_PAD);

        this.colWidth = metrics.stringWidth("Sun");
        final int calWidth = 1 + this.colWidth * 7;

        setPreferredSize(new Dimension(calWidth, calHeight));
    }

    /**
     * Draws the component.
     *
     * @param g the {@code Graphics} to which to draw
     */
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        if (g instanceof final Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        final int calWidth = 1 + this.colWidth * 7;

        final Font font = Skin.MEDIUM_13_FONT;
        final FontMetrics metrics = g.getFontMetrics(font);

        final int ascent = metrics.getAscent();
        final int descent = metrics.getDescent();
        final int lineHeight = ascent + descent;
        final Locale locale = Locale.getDefault();

        // Find the span of dates the semester covers
        final LocalDate start = getFirstDate();
        final LocalDate end = getLastDate();
        final LocalDate today = LocalDate.now();

        Month priorMonth = null;
        int y = 0;

        final List<DateRect> newDateRects = new ArrayList<>(100);

        LocalDate current = start;
        while (!current.isAfter(end)) {
            final DayOfWeek day = current.getDayOfWeek();

            // Print the month header if this is a new month.
            final Month month = current.getMonth();
            if (month != priorMonth) {
                if (priorMonth != null) {
                    // If the new month is not starting on a Sunday, move down to finish the week
                    if (day != DayOfWeek.SUNDAY) {
                        y += lineHeight + DAY_PAD;
                    }
                    y += MONTH_GAP;
                }

                g.setColor(Skin.OFF_WHITE_BLUE);
                g.fillRect(0, y + 1, calWidth, lineHeight);

                g.setColor(Skin.MEDIUM);
                g.drawRect(0, y + 1, calWidth - 1, lineHeight);

                g.setColor(Color.BLACK);
                final String monthName = month.getDisplayName(TextStyle.FULL, locale);
                g.drawString(monthName, 4, y + ascent);
                y += lineHeight + 1;

                priorMonth = month;
            }

            // Draw each day.
            final Color labelColor;
            Color bgColor;

            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                bgColor = Skin.LIGHT;
                labelColor = Skin.MEDIUM;
            } else if (this.holidays.contains(current)) {
                bgColor = Skin.LIGHT;
                labelColor = Skin.LABEL_COLOR3;
            } else {
                bgColor = Skin.WHITE;
                labelColor = Color.BLACK;
            }
            if (current.equals(today)) {
                bgColor = Skin.LT_YELLOW;
            }

            final int currentDay = current.getDayOfMonth();
            final String label = Integer.toString(currentDay);
            final Rectangle bounds = drawDay(g, day, y, lineHeight, ascent, label, labelColor, bgColor);
            newDateRects.add(new DateRect(current, bounds));

            if (day == DayOfWeek.SATURDAY) {
                y += lineHeight + DAY_PAD;
            }

            current = current.plusDays(1);
        }

        this.dateRects.clear();
        this.dateRects.addAll(newDateRects);

        final Dimension pref = getPreferredSize();

        if (pref.width != calWidth || pref.height != y) {
            setPreferredSize(new Dimension(calWidth, y));
        }
    }

    /**
     * Gets the first that should be included in the calendar.
     *
     * @return the first date
     */
    private LocalDate getFirstDate() {

        LocalDate start;
        RawSemesterCalendar firstWeek = this.semesterCalendars.getFirst();
        if (firstWeek.weekNbr.intValue() == 0) {
            start = this.semesterCalendars.get(1).startDt;
        } else {
            start = firstWeek.startDt;
        }

        if (start.getDayOfWeek() == DayOfWeek.SATURDAY) {
            start = start.plusDays(1L);
        }

        return start;
    }

    /**
     * Gets the last date that should be included in the calendar.
     *
     * @return the last date
     */
    private LocalDate getLastDate() {

        LocalDate end = this.semesterCalendars.getLast().endDt;
        if (end.getDayOfWeek() == DayOfWeek.SUNDAY) {
            end = end.minusDays(1L);
        }

        return end;
    }

    /**
     * Draws a box for a day.
     *
     * @param g          the {@code Graphics} to which to dra3w
     * @param day        the day of week
     * @param y          the y coordinate
     * @param lineHeight the font line height
     * @param ascent     the ascent
     * @param label      the label
     * @param labelColor the label color
     * @param bgColor    the background color
     * @return the bounds to associate with the date for mouse click processing
     */
    private Rectangle drawDay(final Graphics g, final DayOfWeek day, final int y, final int lineHeight, final int ascent,
                              final String label, final Color labelColor, final Color bgColor) {

        final int x = getX(day);

        g.setColor(bgColor);
        g.fillRect(x, y, this.colWidth, lineHeight + DAY_PAD);

        g.setColor(labelColor);
        g.drawString(label, x + 2, y + ascent);

        g.setColor(Skin.MEDIUM);
        g.drawRect(x, y, this.colWidth, lineHeight + DAY_PAD);

        return new Rectangle( x + 1, y + 1, this.colWidth - 2, lineHeight + DAY_PAD - 2);
    }

    /**
     * Calculates the x coordinate for a day of the week.
     *
     * @param day the day of the week
     * @return the x coordinate
     */
    private int getX(final DayOfWeek day) {

        final int x;

        if (day == DayOfWeek.SUNDAY) {
            x = 0;
        } else if (day == DayOfWeek.MONDAY) {
            x = this.colWidth;
        } else if (day == DayOfWeek.TUESDAY) {
            x = 2 * this.colWidth;
        } else if (day == DayOfWeek.WEDNESDAY) {
            x = 3 * this.colWidth;
        } else if (day == DayOfWeek.THURSDAY) {
            x = 4 * this.colWidth;
        } else if (day == DayOfWeek.FRIDAY) {
            x = 5 * this.colWidth;
        } else {
            x = 6 * this.colWidth;
        }

        return x;
    }

    /**
     * Sets the listener.
     * @param theListener the new listener
     */
    public void setListener(final ISemesterCalendarPaneListener theListener) {
        
        this.listener = theListener;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {

        if (this.listener != null) {
            final int mouseX = e.getX();
            final int mouseY = e.getY();

            for (final DateRect test : this.dateRects) {
                if (test.rect().contains(mouseX, mouseY)) {
                    final LocalDate date = test.date();
                    this.listener.dateSelected(date);
                    break;
                }
            }
        }
    }

    @Override
    public void mousePressed(final MouseEvent e) {

        // No action
    }

    @Override
    public void mouseReleased(final MouseEvent e) {

        // No action
    }

    @Override
    public void mouseEntered(final MouseEvent e) {

        if (this.listener != null) {
            final Cursor cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
            setCursor(cursor);
        }
    }

    @Override
    public void mouseExited(final MouseEvent e) {

        if (this.listener != null) {
            final Cursor cursor = Cursor.getDefaultCursor();
            setCursor(cursor);
        }
    }

    /** A record that represents a date and corresponding rectangle. */
    private record DateRect(LocalDate date, Rectangle rect) {}
}
