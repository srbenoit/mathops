package dev.mathops.app;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A Swing component that displays a single month of a calendar, and supports sending the date on which a user selects
 * to a controller.
 *
 * <p>
 * The user can scroll forward and backward through months or years.  The calendar can display defined holidays and the
 * "current day" in different styles.
 */
final class JMonthCalendar extends JPanel implements ActionListener {

    /** An action command. */
    private static final String PRIOR_MONTH_CMD = "PRIOR_MONTH";

    /** An action command. */
    private static final String NEXT_MONTH_CMD = "NEXT_MONTH";

    /** The year/month to display. */
    private YearMonth yearMonth;

    /** The current date. */
    private final LocalDate currentDate;

    /** A list of holidays. */
    private final List<LocalDate> holidays;

    /** The selected date. */
    private LocalDate selectedDate;

    /** The month name label. */
    private final JLabel monthNameLabel;

    /** The year label. */
    private final JLabel yearLabel;

    /** The weekday label. */
    private final JLabel[] weekdayLabels;

    /** The grid panel that holds date buttons. */
    private final JPanel body;

    /** The date buttons [week index 0 - 5][day index 0(Sun) - 6(Sat)]. */
    private final JButton[][] dateButtons;

    /** The dates associated with each date button. */
    private final LocalDate[][] dates;

    /** The listener to notify when a date is selected. */
    private final Listener listener;

    /**
     * Constructs a new {@code JMonthCalendar}.
     *
     * @param theYearMonth    the year and month to display
     * @param theCurrentDate  the current date ({@code null} if the current date should not be displayed)
     * @param theHolidays     a list of dates to display as "holiday"
     * @param theSelectedDate a date to display as "selected"
     * @param theFont         the font
     * @param theListener     a listener to be notified when a date is selected
     */
    JMonthCalendar(final YearMonth theYearMonth, final LocalDate theCurrentDate, final List<LocalDate> theHolidays,
                   final LocalDate theSelectedDate, final Font theFont, final Listener theListener) {

        super(new StackedBorderLayout());

        setFont(theFont);

        final MatteBorder outline = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.lightGray);
        setBorder(outline);

        this.yearMonth = theYearMonth;
        this.currentDate = theCurrentDate;
        this.holidays = new ArrayList<>(theHolidays);
        this.selectedDate = theSelectedDate;
        this.listener = theListener;

        final Locale locale = Locale.getDefault();

        // Top row has the month and year, with left/right buttons to scroll

        final JPanel topRow = new JPanel(new StackedBorderLayout());

        final JButton priorMonth = new BasicArrowButton(SwingConstants.WEST);
        priorMonth.setEnabled(true);
        priorMonth.setActionCommand(PRIOR_MONTH_CMD);
        priorMonth.addActionListener(this);

        final JButton nextMonth = new BasicArrowButton(SwingConstants.EAST);
        nextMonth.setEnabled(true);
        nextMonth.setActionCommand(NEXT_MONTH_CMD);
        nextMonth.addActionListener(this);

        final JPanel topRowFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));

        final float size = theFont.getSize2D();
        final Font titleFont = theFont.deriveFont(size + 2.0f);

        this.monthNameLabel = new JLabel(CoreConstants.SPC);
        this.monthNameLabel.setFont(titleFont);
        topRowFlow.add(this.monthNameLabel);

        this.yearLabel = new JLabel(CoreConstants.SPC);
        this.yearLabel.setForeground(Color.GRAY);
        this.yearLabel.setFont(titleFont);
        topRowFlow.add(this.yearLabel);

        topRow.add(priorMonth, StackedBorderLayout.WEST);
        topRow.add(nextMonth, StackedBorderLayout.EAST);
        topRow.add(topRowFlow, StackedBorderLayout.CENTER);
        add(topRow, StackedBorderLayout.NORTH);

        // Body is a grid 7 units wide and 7 units tall (top row is names of weekdays)
        this.body = new JPanel(new GridLayout(7, 7, 2, 2));
        add(this.body, StackedBorderLayout.NORTH);

        final String sunStr = DayOfWeek.SUNDAY.getDisplayName(TextStyle.SHORT, locale);
        final String monStr = DayOfWeek.MONDAY.getDisplayName(TextStyle.SHORT, locale);
        final String tueStr = DayOfWeek.TUESDAY.getDisplayName(TextStyle.SHORT, locale);
        final String wedStr = DayOfWeek.WEDNESDAY.getDisplayName(TextStyle.SHORT, locale);
        final String thuStr = DayOfWeek.THURSDAY.getDisplayName(TextStyle.SHORT, locale);
        final String friStr = DayOfWeek.FRIDAY.getDisplayName(TextStyle.SHORT, locale);
        final String satStr = DayOfWeek.SATURDAY.getDisplayName(TextStyle.SHORT, locale);

        this.weekdayLabels = new JLabel[7];
        this.weekdayLabels[0] = new JLabel(sunStr);
        this.weekdayLabels[1] = new JLabel(monStr);
        this.weekdayLabels[2] = new JLabel(tueStr);
        this.weekdayLabels[3] = new JLabel(wedStr);
        this.weekdayLabels[4] = new JLabel(thuStr);
        this.weekdayLabels[5] = new JLabel(friStr);
        this.weekdayLabels[6] = new JLabel(satStr);

        final Font weekdayFont = theFont.deriveFont(size - 1.0f);
        for (final JLabel lbl : this.weekdayLabels) {
            lbl.setFont(weekdayFont);
            lbl.setForeground(Color.GRAY);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            this.body.add(lbl);
        }

        // Next 6 rows are the weeks - we will create a grid of general buttons and then set their text, colors,
        // and enabled state on "update()" to reflect the current month

        this.dateButtons = new JButton[6][7];
        this.dates = new LocalDate[6][7];

        final Border buttonPad = BorderFactory.createEmptyBorder(2, 8, 2, 8);

        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 7; ++j) {
                final String cmd = i + "." + j;
                final JButton btn = new JButton(CoreConstants.SPC);
                btn.setFont(theFont);
                btn.setEnabled(true);
                btn.setActionCommand(cmd);
                btn.addActionListener(this);
                btn.setForeground(Color.BLACK);
                btn.setBorder(buttonPad);
                btn.setBackground(j == 0 || j == 6 ? getBackground() : Color.WHITE);
                this.body.add(btn);
                this.dateButtons[i][j] = btn;
            }
        }

        update();
    }

    /**
     * Sets this component's font.
     *
     * @param font the desired {@code Font} for this component
     */
    public void setFont(final Font font) {

        super.setFont(font);

        final float size = font.getSize2D();
        final Font titleFont = font.deriveFont(size * 1.15f);
        final Font weekdayFont = font.deriveFont(size * 0.8f);

        if (this.monthNameLabel != null) {
            this.monthNameLabel.setFont(titleFont);
        }
        if (this.yearLabel != null) {
            this.yearLabel.setFont(titleFont);
        }

        if (this.weekdayLabels != null) {
            for (final JLabel lbl : weekdayLabels) {
                lbl.setFont(weekdayFont);
            }
        }

        if (this.dateButtons != null) {
            for (final JButton[] row : dateButtons) {
                for (final JButton button : row) {
                    button.setFont(font);
                }
            }
        }

        if (this.body != null) {
            this.body.invalidate();
            this.body.revalidate();
            this.body.setSize(this.body.getPreferredSize());
        }
    }

    /**
     * Sets the "selected" date.
     *
     * @param newSelectedDate the new selected date
     */
    void setSelectedDate(final LocalDate newSelectedDate) {

        this.selectedDate = newSelectedDate;
        update();
    }

    /**
     * Updates the calendar based on current member variables.
     */
    public void update() {

        final Locale locale = Locale.getDefault();

        final int year = this.yearMonth.getYear();
        final Month month = this.yearMonth.getMonth();

        final String monthNameString = month.getDisplayName(TextStyle.FULL_STANDALONE, locale);
        this.monthNameLabel.setText(monthNameString);

        final String yearString = Integer.toString(year);
        this.yearLabel.setText(yearString);

        final Color yellow = ColorNames.getColor("yellow");
        final Color azure2 = ColorNames.getColor("azure2");
        final Color indianRed = ColorNames.getColor("IndianRed");
        final Color background = getBackground();

        // Populate the dates grid

        // Fill in numbers of dates in the prior month
        LocalDate past = LocalDate.of(year, month, 1).minusDays(1L);

        while (past.getDayOfWeek() != DayOfWeek.SATURDAY) {

            final DayOfWeek day = past.getDayOfWeek();
            final int dayOfMonth = past.getDayOfMonth();
            final String dateStr = Integer.toString(dayOfMonth);
            JButton btn = null;

            if (day == DayOfWeek.SUNDAY) {
                btn = this.dateButtons[0][0];
                this.dates[0][0] = past;
            } else if (day == DayOfWeek.MONDAY) {
                btn = this.dateButtons[0][1];
                this.dates[0][1] = past;
            } else if (day == DayOfWeek.TUESDAY) {
                btn = this.dateButtons[0][2];
                this.dates[0][2] = past;
            } else if (day == DayOfWeek.WEDNESDAY) {
                btn = this.dateButtons[0][3];
                this.dates[0][3] = past;
            } else if (day == DayOfWeek.THURSDAY) {
                btn = this.dateButtons[0][4];
                this.dates[0][4] = past;
            } else if (day == DayOfWeek.FRIDAY) {
                btn = this.dateButtons[0][5];
                this.dates[0][5] = past;
            }
            if (btn != null) {
                if (past.equals(this.selectedDate)) {
                    btn.setBackground(yellow);
                } else if (past.equals(this.currentDate)) {
                    btn.setBackground(azure2);
                } else {
                    btn.setBackground(background);
                }

                final Color rosyBrown = ColorNames.getColor("RosyBrown");
                final boolean pastIsHoliday = this.holidays.contains(past);
                btn.setForeground(pastIsHoliday ? indianRed : Color.GRAY);
                btn.setText(dateStr);
            }

            past = past.minusDays(1L);
        }

        // Fill in numbers of dates in the current month
        int currentRow = 0;
        LocalDate current = LocalDate.of(year, month, 1);
        while (current.getMonth() == month) {
            final DayOfWeek day = current.getDayOfWeek();
            final int dayOfMonth = current.getDayOfMonth();
            final String dateStr = Integer.toString(dayOfMonth);
            JButton btn = null;

            if (day == DayOfWeek.SUNDAY) {
                btn = this.dateButtons[currentRow][0];
                this.dates[currentRow][0] = current;
            } else if (day == DayOfWeek.MONDAY) {
                btn = this.dateButtons[currentRow][1];
                this.dates[currentRow][1] = current;
            } else if (day == DayOfWeek.TUESDAY) {
                btn = this.dateButtons[currentRow][2];
                this.dates[currentRow][2] = current;
            } else if (day == DayOfWeek.WEDNESDAY) {
                btn = this.dateButtons[currentRow][3];
                this.dates[currentRow][3] = current;
            } else if (day == DayOfWeek.THURSDAY) {
                btn = this.dateButtons[currentRow][4];
                this.dates[currentRow][4] = current;
            } else if (day == DayOfWeek.FRIDAY) {
                btn = this.dateButtons[currentRow][5];
                this.dates[currentRow][5] = current;
            } else if (day == DayOfWeek.SATURDAY) {
                btn = this.dateButtons[currentRow][6];
                this.dates[currentRow][6] = current;
                ++currentRow;
            }

            if (btn != null) {
                if (current.equals(this.selectedDate)) {
                    btn.setBackground(yellow);
                } else if (current.equals(this.currentDate)) {
                    btn.setBackground(azure2);
                } else {
                    btn.setBackground(day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY ? background : Color.WHITE);
                }
                final boolean currentIsHoliday = this.holidays.contains(current);
                btn.setForeground(currentIsHoliday ? Color.RED : Color.BLACK);

                btn.setText(dateStr);
            }

            current = current.plusDays(1L);
        }

        // Fill in numbers of dates in the next month
        while (currentRow < 6) {

            final DayOfWeek day = current.getDayOfWeek();
            final int dayOfMonth = current.getDayOfMonth();
            final String dateStr = Integer.toString(dayOfMonth);
            JButton btn = null;

            if (day == DayOfWeek.SUNDAY) {
                btn = this.dateButtons[currentRow][0];
                this.dates[currentRow][0] = current;
            } else if (day == DayOfWeek.MONDAY) {
                btn = this.dateButtons[currentRow][1];
                this.dates[currentRow][1] = current;
            } else if (day == DayOfWeek.TUESDAY) {
                btn = this.dateButtons[currentRow][2];
                this.dates[currentRow][2] = current;
            } else if (day == DayOfWeek.WEDNESDAY) {
                btn = this.dateButtons[currentRow][3];
                this.dates[currentRow][3] = current;
            } else if (day == DayOfWeek.THURSDAY) {
                btn = this.dateButtons[currentRow][4];
                this.dates[currentRow][4] = current;
            } else if (day == DayOfWeek.FRIDAY) {
                btn = this.dateButtons[currentRow][5];
                this.dates[currentRow][5] = current;
            } else if (day == DayOfWeek.SATURDAY) {
                btn = this.dateButtons[currentRow][6];
                this.dates[currentRow][6] = current;
                ++currentRow;
            }

            if (btn != null) {
                if (current.equals(this.selectedDate)) {
                    btn.setBackground(yellow);
                } else if (current.equals(this.currentDate)) {
                    btn.setBackground(azure2);
                } else {
                    btn.setBackground(background);
                }

                final boolean currentIsHoliday = this.holidays.contains(current);
                btn.setForeground(currentIsHoliday ? indianRed : Color.GRAY);
                btn.setText(dateStr);
            }

            current = current.plusDays(1L);
        }

        repaint();
    }

    /**
     * Called when an action is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (PRIOR_MONTH_CMD.equals(cmd)) {
            this.yearMonth = this.yearMonth.minusMonths(1L);
            update();
        } else if (NEXT_MONTH_CMD.equals(cmd)) {
            this.yearMonth = this.yearMonth.plusMonths(1L);
            update();
        } else {
            final int dot = cmd.indexOf('.');
            if (dot == 1) {
                final String rowStr = cmd.substring(0, 1);
                final String colStr = cmd.substring(2);

                try {
                    final int row = Integer.parseInt(rowStr);
                    final int col = Integer.parseInt(colStr);

                    if (row < 6 && col >= 0 && col < 7) {
                        this.selectedDate = this.dates[row][col];
                        update();
                        if (this.selectedDate != null && this.listener != null) {
                            this.listener.dateSelected(this.selectedDate);
                        }
                    } else {
                        Log.warning("Unrecognized command: ", cmd);
                    }
                } catch (final NumberFormatException ex) {
                    Log.warning("Unrecognized command: ", cmd);
                }
            } else {
                Log.warning("Unrecognized command: ", cmd);
            }
        }
    }

    /**
     * The interface for a listener to notify when a date is selected.
     */
    @FunctionalInterface
    public interface Listener {

        /**
         * Called when a date is selected.
         *
         * @param date the selected date
         */
        void dateSelected(LocalDate date);
    }
}
