package dev.mathops.app;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import jwabbit.CoreConstants;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

/**
 * A Swing control to choose a date.  The user can type a date into the field with some standard formats, or can pop
 * open a calendar from which to choose the date.  When a date is selected or entered, an action event is fired to all
 * registered listeners.
 */
public final class JDateChooser extends JPanel implements ActionListener, MouseListener, JMonthCalendar.Listener {

    /** The action command to open a dropdown calendar picker. */
    private static final String DATE_TYPED_CMD = "DATE_TYPED_CMD";

    /** The text field into which the user can type, or in which the selected date is displayed. */
    private final JTextField dateField;

    /** The button to drop down the calendar display. */
    private final BasicArrowButton dateDropdownArrow;

    /** The list of action listeners. */
    private final List<ActionListener> listeners;

    /** The action command. */
    private String actionCommand = CoreConstants.EMPTY;

    /** The current date. */
    private LocalDate date;

    /** The month calendar. */
    private final JMonthCalendar monthCalendar;

    /** The month calendar window. */
    private final JWindow monthCalendarWindow;

    /**
     * Constructs a new {@code JDateChooser} with a specified starting value.
     *
     * @param currentValue the current value (can be null)
     * @param holidays     an optional list of holidays
     */
    public JDateChooser(final LocalDate currentValue, final List<LocalDate> holidays) {

        super(new BorderLayout());

        this.listeners = new ArrayList<>(2);

        this.dateField = new JTextField();
        final Insets insets = this.dateField.getInsets();
        final Border border = this.dateField.getBorder();
        setBorder(border);

        final Border padding = BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right);
        this.dateField.setBorder(padding);
        add(this.dateField, BorderLayout.CENTER);
        this.dateField.setActionCommand(DATE_TYPED_CMD);
        this.dateField.addActionListener(this);

        this.dateDropdownArrow = new BasicArrowButton(SwingConstants.SOUTH);
        this.dateDropdownArrow.addMouseListener(this);
        add(this.dateDropdownArrow, BorderLayout.LINE_END);

        this.date = currentValue;
        if (currentValue != null) {
            final String dateStr = TemporalUtils.FMT_MDY.format(currentValue);
            this.dateField.setText(dateStr);
        }

        final LocalDate today = LocalDate.now();
        final YearMonth thisMonth = YearMonth.from(today);
        this.monthCalendar = new JMonthCalendar(thisMonth, today, holidays, currentValue, this);

        this.monthCalendarWindow = new JWindow();
        this.monthCalendarWindow.add(this.monthCalendar);
        this.monthCalendarWindow.pack();
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public LocalDate getDate() {

        return this.date;
    }

    /**
     * Sets the date.
     *
     * @param newDate the new date
     */
    public void setDate(final LocalDate newDate) {

        this.date = newDate;

        if (newDate == null) {
            this.dateField.setText("");
        } else {
            this.dateField.setText(TemporalUtils.FMT_MDY.format(newDate));
        }
    }

    /**
     * Sets the font to use when displaying this control.
     *
     * @param newFont the desired {@code Font} for this component
     */
    public void setFont(final Font newFont) {

        super.setFont(newFont);

        if (this.dateField != null) {
            this.dateField.setFont(newFont);
        }
        if (this.monthCalendar != null) {
            this.monthCalendar.setFont(newFont);

            if (this.dateField != null) {
                final Dimension calendarSize = this.monthCalendar.getPreferredSize();
                final Dimension fieldSize = this.dateField.getPreferredSize();
                final Dimension buttonSize = this.dateDropdownArrow.getPreferredSize();

                final int w = calendarSize.width - buttonSize.width;
                final Dimension newFieldSize = new Dimension(w, fieldSize.height);
                this.dateField.setPreferredSize(newFieldSize);
            }
        }

        invalidate();
        revalidate();
    }

    /**
     * Sets the action command to include with actions fired by this component.
     *
     * @param theActionCommand the new action command
     */
    public void setActionCommand(final String theActionCommand) {

        this.actionCommand = theActionCommand;
    }

    /**
     * Adds an action listener.
     *
     * @param listener the action listener
     */
    public void addActionListener(final ActionListener listener) {

        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
    }

    /**
     * Adds an action listener.
     *
     * @param listener the action listener
     */
    public void removeActionListener(final ActionListener listener) {

        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Fires an action event to all registered listeners.
     */
    public void fireActionEvent() {

        synchronized (this.listeners) {
            if (!this.listeners.isEmpty()) {

                final ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, this.actionCommand);

                for (final ActionListener listener : this.listeners) {
                    listener.actionPerformed(evt);
                }
            }
        }
    }

    /**
     * Called when an action is performed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (DATE_TYPED_CMD.equals(cmd)) {
            final String dateText = this.dateField.getText();
            final LocalDate parsed = interpretDate(dateText);

            if (parsed != null) {
                final String newText = TemporalUtils.FMT_MDY.format(parsed);
                if (!newText.equals(dateText)) {
                    this.dateField.setText(newText);
                    fireActionEvent();
                }
            }
        }
    }

    /**
     * Attempts to interpret a date string
     *
     * @param dateString the date string
     * @return the parsed date; null if unable to interpret
     */
    public static LocalDate interpretDate(final String dateString) {

        LocalDate date = null;
        TemporalAccessor newDate = null;

        try {
            newDate = TemporalUtils.FMT_MDY_COMPACT.parse(dateString);
        } catch (final DateTimeParseException ex2) {
            try {
                newDate = TemporalUtils.FMT_MDY.parse(dateString);
            } catch (final DateTimeParseException ex3) {
                if (dateString.length() == 6) {
                    // Try MMDDYY, like 123199
                    try {
                        final int value = Integer.parseInt(dateString);
                        final int month = value / 10000;
                        final int day = (value / 100) % 100;
                        final int year2 = value % 100;
                        final int year = year2 < 50 ? 2000 + year2 : 1900 + year2;

                        newDate = LocalDate.of(year, month, day);
                    } catch (final NumberFormatException | DateTimeException ex) {
                        // No action
                    }
                }
            }
        }

        if (newDate == null) {
            Log.warning("Failed to interpret '", dateString, "' as date");
        } else {
            final int day = newDate.get(ChronoField.DAY_OF_MONTH);
            final int month = newDate.get(ChronoField.MONTH_OF_YEAR);
            final int year = newDate.get(ChronoField.YEAR);
            date = LocalDate.of(year, month, day);
        }

        return date;
    }

    @Override
    public void mouseClicked(final MouseEvent e) {

        // No action
    }

    @Override
    public void mousePressed(final MouseEvent e) {

        final Dimension fieldSize = this.dateField.getSize();
        final Point fieldLocation = this.dateField.getLocationOnScreen();

        final int x = fieldLocation.x - 1;
        final int y = fieldLocation.y + fieldSize.height;

        if (this.monthCalendarWindow.isVisible()) {
            this.monthCalendarWindow.setVisible(false);
        } else {
            this.monthCalendarWindow.setLocation(x, y);
            this.monthCalendarWindow.setVisible(true);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {

        // No action
    }

    private void maybeShowPopup(final MouseEvent e) {

        // No action
    }

    @Override
    public void mouseEntered(final MouseEvent e) {

        // No action
    }

    @Override
    public void mouseExited(final MouseEvent e) {

        // No action
    }

    /**
     * Called when a date is selected.
     *
     * @param date the selected date
     */
    @Override
    public void dateSelected(final LocalDate date) {

        final String dateStr = TemporalUtils.FMT_MDY.format(date);
        this.dateField.setText(dateStr);

        this.monthCalendarWindow.setVisible(false);
    }

    /**
     * Main method to test the control.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();

        final LocalDate today = LocalDate.now();
        final YearMonth yearMonth = YearMonth.from(today);
        final List<LocalDate> holidays = new ArrayList<>(10);
        holidays.add(LocalDate.of(2024, 5, 27));
        holidays.add(LocalDate.of(2024, 6, 19));
        holidays.add(LocalDate.of(2024, 7, 4));
        holidays.add(LocalDate.of(2024, 9, 2));
        final LocalDate selected = LocalDate.of(2024, 7, 25);

        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new JFrame("Test");
            final JPanel content = new JPanel(new BorderLayout());
            content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            frame.setContentPane(content);

            final JPanel flow = new JPanel(new BorderLayout());
            content.add(flow, BorderLayout.PAGE_START);
            flow.add(new JLabel("Date: "), BorderLayout.LINE_START);

            final JDateChooser chooser = new JDateChooser(selected, holidays);
            chooser.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
            chooser.setForeground(Color.BLUE);
            chooser.setActionCommand("FOO");
            final Dimension prefSize = chooser.getPreferredSize();
            final Dimension minSize = new Dimension(150, prefSize.height);
            chooser.setPreferredSize(minSize);
            flow.add(chooser, BorderLayout.CENTER);


//            final JMonthCalendar month = new JMonthCalendar(yearMonth, today, holidays, selected, chooser);
//            monthFlow.add(month);
//            content.add(monthFlow, BorderLayout.CENTER);

            final JLabel result = new JLabel(" ");
            content.add(result, BorderLayout.PAGE_END);

            chooser.addActionListener(e -> {
                final String cmd = e.getActionCommand();
                if ("FOO".equals(cmd)) {
                    final LocalDate parsed = chooser.getDate();

                    if (parsed == null) {
                        result.setText("(No date entered)");
                    } else {
                        final String dateStr = TemporalUtils.FMT_MDY.format(parsed);
                        result.setText(dateStr);
                    }
                }
            });

            UIUtilities.packAndCenter(frame);
            frame.setVisible(true);
        });
    }
}
