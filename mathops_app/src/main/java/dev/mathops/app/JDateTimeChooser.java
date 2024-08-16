package dev.mathops.app;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;

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
public final class JDateTimeChooser extends JPanel implements ActionListener, MouseListener, JMonthCalendar.Listener {

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
    private LocalDate currentDate;

    /** The month calendar. */
    private final JMonthCalendar monthCalendar;

    /** The month calendar window. */
    private final JWindow monthCalendarWindow;

    /**
     * Constructs a new {@code JDateChooser} with a specified starting value.
     *
     * @param theCurrentDate the current date (can be null)
     * @param holidays       an optional list of holidays
     * @param theFont        the font
     */
    public JDateTimeChooser(final LocalDate theCurrentDate, final List<LocalDate> holidays, final Font theFont) {

        super(new BorderLayout());

        this.listeners = new ArrayList<>(2);

        this.dateField = new JTextField();
        final Insets insets = this.dateField.getInsets();
        final Border border = this.dateField.getBorder();
        setBorder(border);

        final Border padding = BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right);
        this.dateField.setBorder(padding);
        this.dateField.setFont(theFont);
        add(this.dateField, BorderLayout.CENTER);
        this.dateField.setActionCommand(DATE_TYPED_CMD);
        this.dateField.addActionListener(this);

        this.dateDropdownArrow = new BasicArrowButton(SwingConstants.SOUTH);
        this.dateDropdownArrow.addMouseListener(this);
        add(this.dateDropdownArrow, BorderLayout.LINE_END);

        this.currentDate = theCurrentDate;
        if (theCurrentDate != null) {
            final String dateStr = TemporalUtils.FMT_MDY.format(theCurrentDate);
            this.dateField.setText(dateStr);
        }

        final LocalDate today = LocalDate.now();
        final YearMonth thisMonth = YearMonth.from(today);
        this.monthCalendar = new JMonthCalendar(thisMonth, today, holidays, theCurrentDate, theFont, this);

        this.monthCalendarWindow = new JWindow();
        this.monthCalendarWindow.add(this.monthCalendar);
        this.monthCalendarWindow.pack();
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public LocalDate getCurrentDate() {

        return this.currentDate;
    }

    /**
     * Sets the date.
     *
     * @param newDate the new date
     */
    public void setCurrentDate(final LocalDate newDate) {

        this.currentDate = newDate;

        if (newDate == null) {
            this.dateField.setText(CoreConstants.EMPTY);
        } else {
            this.dateField.setText(TemporalUtils.FMT_MDY.format(newDate));
        }

        this.monthCalendar.setSelectedDate(newDate);
        fireActionEvent();
    }

    /**
     * Sets the font to use when displaying this control.
     *
     * @param font the desired {@code Font} for this component
     */
    public void setFont(final Font font) {

        super.setFont(font);

        if (this.dateField != null) {
            this.dateField.setFont(font);
        }
        if (this.monthCalendar != null) {
            this.monthCalendar.setFont(font);

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
     * Sets the enabled state of this control.
     *
     * @param enabled true if this component should be enabled, false otherwise
     */
    public void setEnabled(final boolean enabled) {

        super.setEnabled(enabled);

        this.dateField.setEnabled(enabled);
        this.dateDropdownArrow.setEnabled(enabled);
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
    private void fireActionEvent() {

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
            Log.info("Date typed");
            final String dateText = this.dateField.getText();
            final LocalDate parsed = interpretDate(dateText);

            if (parsed != null) {
                final String newText = TemporalUtils.FMT_MDY.format(parsed);
                if (!newText.equals(dateText)) {
                    this.dateField.setText(newText);
                }
                fireActionEvent();
                this.currentDate = parsed;
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

    /**
     * Called when the mouse is clicked in the component.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        // No action
    }

    /**
     * Called when the mouse is pressed in the component.
     *
     * @param e the mouse event
     */
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

    /**
     * Called when the mouse is released after being pressed in the component.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        // No action
    }

    /**
     * Called when the mouse enters the component.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        // No action
    }

    /**
     * Called when the mouse exits the component.
     *
     * @param e the mouse event
     */
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

        setCurrentDate(date);
        this.monthCalendarWindow.setVisible(false);
        fireActionEvent();
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

            final JDateTimeChooser chooser = new JDateTimeChooser(selected, holidays, Skin.BODY_12_FONT);
            chooser.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
            chooser.setForeground(Color.BLUE);
            chooser.setActionCommand("FOO");
            final Dimension prefSize = chooser.getPreferredSize();
            final Dimension minSize = new Dimension(150, prefSize.height);
            chooser.setPreferredSize(minSize);
            flow.add(chooser, BorderLayout.CENTER);

            final JLabel result = new JLabel(" ");
            content.add(result, BorderLayout.PAGE_END);

            chooser.addActionListener(e -> {
                final String cmd = e.getActionCommand();
                if ("FOO".equals(cmd)) {
                    final LocalDate parsed = chooser.getCurrentDate();

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
