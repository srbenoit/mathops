package dev.mathops.app.ui;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.ui.UIUtilities;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A Swing control to choose a date.  The user can type a date into the field with some standard formats, or can pop
 * open a calendar from which to choose the date.  When a date is selected or entered, an action event is fired to all
 * registered listeners.
 */
public final class JDateTimeChooser extends JPanel implements ActionListener, ChangeListener {

    /** The action command fired when the date chooser's value changes. */
    private static final String DATE_CHOOSER_CMD = "DATE_CHOOSER";

    /** The action command fired when the AM/PM selection changes. */
    private static final String AM_PM_CHANGED_CMD = "AM_PM_CHANGED";

    /** A commonly used integer. */
    private static final Integer ZERO = Integer.valueOf(0);

    /** The panel with the date (stored so we can update background color if needed). */
    private final JPanel dateRow;

    /** The panel with the time (stored so we can update background color if needed). */
    private final JPanel timeRow;

    /** The date chooser, with associated month calendar. */
    private final JDateChooser dateChooser;

    /** The model for the hour spinner. */
    private final SpinnerNumberModel hourModel;

    /** The hour spinner. */
    private final JSpinner hourSpinner;

    /** The model for the minute spinner. */
    private final SpinnerNumberModel minuteModel;

    /** The minute spinner. */
    private final JSpinner minuteSpinner;

    /** The model for the second spinner. */
    private final SpinnerNumberModel secondModel;

    /** The second spinner. */
    private final JSpinner secondSpinner;

    /** The Am/PM selector. */
    private final JComboBox<String> amPm;

    /** A colon label (stored so we can update its font if needed). */
    private final JLabel colon1;

    /** A colon label (stored so we can update its font if needed). */
    private final JLabel colon2;

    /** The list of action listeners. */
    private final List<ActionListener> listeners;

    /** The action command. */
    private String actionCommand = CoreConstants.EMPTY;

    /** The current date/time. */
    private LocalDateTime currentDateTime;

    /**
     * Constructs a new {@code JDateTimeChooser} with a specified starting value.
     *
     * @param theCurrentDateTime the current date/time (can be null)
     * @param holidays           an optional list of holidays
     * @param theFont            the font
     * @param orientation        the orientation (SwingConstants.HORIZONTAL to show the time to the right of the date;
     *                           SwingConstants.VERTICAL to show the time below the date.
     */
    public JDateTimeChooser(final LocalDateTime theCurrentDateTime, final List<LocalDate> holidays,
                            final Font theFont, final int orientation) {

        super(new BorderLayout(4, 4));

        final LocalDate date = theCurrentDateTime == null ? null : theCurrentDateTime.toLocalDate();
        final LocalTime time = theCurrentDateTime == null ? null : theCurrentDateTime.toLocalTime();
        this.currentDateTime = theCurrentDateTime;

        this.listeners = new ArrayList<>(2);

        this.dateChooser = new JDateChooser(date, holidays, theFont);
        this.dateChooser.setActionCommand(DATE_CHOOSER_CMD);
        this.dateChooser.addActionListener(this);

        this.hourModel = new SpinnerNumberModel(0, 0, 11, 1);
        this.hourSpinner = new JSpinner(this.hourModel);
        this.hourSpinner.setFont(theFont);

        final Dimension wholeSize = this.hourSpinner.getPreferredSize();
        final int size = wholeSize.height;
        final Dimension newSpinnerSize = new Dimension(size * 9 / 4, size);

        this.hourSpinner.setPreferredSize(newSpinnerSize);
        this.hourSpinner.addChangeListener(this);

        this.minuteModel = new SpinnerNumberModel(0, 0, 59, 1);
        this.minuteSpinner = new JSpinner(this.minuteModel);
        this.minuteSpinner.setFont(theFont);
        this.minuteSpinner.setPreferredSize(newSpinnerSize);
        this.minuteSpinner.addChangeListener(this);

        this.secondModel = new SpinnerNumberModel(0, 0, 59, 1);
        this.secondSpinner = new JSpinner(this.secondModel);
        this.secondSpinner.setFont(theFont);
        this.secondSpinner.setPreferredSize(newSpinnerSize);
        this.secondSpinner.addChangeListener(this);

        final String[] choices = {"AM", "PM"};
        this.amPm = new JComboBox<>(choices);
        this.amPm.setFont(theFont);
        this.amPm.setActionCommand(AM_PM_CHANGED_CMD);
        this.amPm.addActionListener(this);

        final Dimension newAmPmSize = new Dimension(size * 10 / 4, size);
        this.amPm.setPreferredSize(newAmPmSize);

        if (time != null) {
            final int curHour = time.getHour() % 12;
            final Integer hourObj = Integer.valueOf(curHour);
            this.hourModel.setValue(hourObj);

            final int curMinute = time.getMinute();
            final Integer minuteObj = Integer.valueOf(curMinute);
            this.minuteSpinner.setValue(minuteObj);

            final int curSecond = time.getSecond();
            final Integer secondObj = Integer.valueOf(curSecond);
            this.secondModel.setValue(secondObj);

            final int curAmPm = time.getHour() > 11 ? 1 : 0;
            this.amPm.setSelectedIndex(curAmPm);
        }

        this.colon1 = new JLabel(":");
        this.colon1.setFont(theFont);
        this.colon2 = new JLabel(":");
        this.colon2.setFont(theFont);

        this.dateRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 0));
        this.dateRow.add(this.dateChooser);

        this.timeRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 0));
        this.timeRow.add(this.hourSpinner);
        this.timeRow.add(this.colon1);
        this.timeRow.add(this.minuteSpinner);
        this.timeRow.add(this.colon2);
        this.timeRow.add(this.secondSpinner);
        this.timeRow.add(this.amPm);

        add(this.dateRow, BorderLayout.CENTER);

        if (orientation == SwingConstants.VERTICAL) {
            add(this.timeRow, BorderLayout.PAGE_END);
        } else {
            add(this.timeRow, BorderLayout.LINE_END);
        }
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public LocalDateTime getCurrentDateTime() {

        return this.currentDateTime;
    }

    /**
     * Sets the date/time.
     *
     * @param newDateTime the new date/time
     */
    public void setCurrentDateTime(final LocalDateTime newDateTime) {

        final LocalDate date = newDateTime == null ? null : newDateTime.toLocalDate();
        final LocalTime time = newDateTime == null ? null : newDateTime.toLocalTime();

        this.currentDateTime = newDateTime;

        this.dateChooser.setCurrentDate(date);

        if (time == null) {
            this.hourSpinner.setValue(ZERO);
            this.minuteSpinner.setValue(ZERO);
            this.secondSpinner.setValue(ZERO);
            this.amPm.setSelectedIndex(0);
        } else {
            final int hour = time.getHour() % 12;
            final Integer hourNum = Integer.valueOf(hour);
            this.hourSpinner.setValue(hourNum);

            final int min = time.getMinute();
            final Integer minNum = Integer.valueOf(min);
            this.minuteSpinner.setValue(minNum);

            final int sec = time.getSecond();
            final Integer secNum = Integer.valueOf(sec);
            this.secondSpinner.setValue(secNum);

            final int amPmIndex = time.getHour() / 12;
            this.amPm.setSelectedIndex(amPmIndex);
        }

        fireActionEvent();
    }

    /**
     * Sets the font to use when displaying this control.
     *
     * @param font the desired {@code Font} for this component
     */
    public void setFont(final Font font) {

        super.setFont(font);

        if (this.dateChooser != null) {
            this.dateChooser.setFont(font);
        }
        if (this.hourSpinner != null) {
            this.hourSpinner.setFont(font);
        }
        if (this.minuteSpinner != null) {
            this.minuteSpinner.setFont(font);
        }
        if (this.secondSpinner != null) {
            this.secondSpinner.setFont(font);
        }
        if (this.amPm != null) {
            this.amPm.setFont(font);
        }
        if (this.colon1 != null) {
            this.colon1.setFont(font);
        }
        if (this.colon2 != null) {
            this.colon2.setFont(font);
        }

        invalidate();
        revalidate();
    }

    /**
     * Sets the control's background color.
     *
     * @param bg the desired background color
     */
    @Override
    public void setBackground(final Color bg) {

        super.setBackground(bg);

        if (this.dateRow != null) {
            this.dateRow.setBackground(bg);
        }
        if (this.timeRow != null) {
            this.timeRow.setBackground(bg);
        }
    }

    /**
     * Sets the enabled state of this control.
     *
     * @param enabled true if this component should be enabled, false otherwise
     */
    public void setEnabled(final boolean enabled) {

        super.setEnabled(enabled);

        this.dateChooser.setEnabled(enabled);
        this.hourSpinner.setEnabled(enabled);
        this.minuteSpinner.setEnabled(enabled);
        this.secondSpinner.setEnabled(enabled);
        this.amPm.setEnabled(enabled);
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

        final String cmd = this.actionCommand;

        synchronized (this.listeners) {
            if (!this.listeners.isEmpty()) {

                final ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, cmd);

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

        if (DATE_CHOOSER_CMD.equals(cmd)) {
            // The date chooser's value has changed
            this.currentDateTime = interpretFields();
            fireActionEvent();
        } else if (AM_PM_CHANGED_CMD.equals(cmd)) {
            // The AM/PM selector has changed
            this.currentDateTime = interpretFields();
            fireActionEvent();
        }
    }

    /**
     * Called when one of the spinners changes state.
     *
     * @param e the change event
     */
    @Override
    public void stateChanged(final ChangeEvent e) {

        this.currentDateTime = interpretFields();
        fireActionEvent();
    }

    /**
     * Interprets field values and constructs a {@code LocalDateTime}.
     *
     * @return the constructed  {@code LocalDateTime}
     */
    private LocalDateTime interpretFields() {

        final LocalDateTime result;

        final LocalDate date = this.dateChooser.getCurrentDate();

        if (date == null) {
            result = null;
        } else {
            final Number hourValue = this.hourModel.getNumber();
            final Number minuteValue = this.minuteModel.getNumber();
            final Number secondValue = this.secondModel.getNumber();

            if (hourValue == null || minuteValue == null || secondValue == null) {
                result = null;
            } else {
                final int hour = hourValue.intValue() + this.amPm.getSelectedIndex() * 12;
                final int minute = minuteValue.intValue();
                final int second = secondValue.intValue();

                final LocalTime time = LocalTime.of(hour, minute, second);
                result = LocalDateTime.of(date, time);
            }
        }

        return result;
    }

    /**
     * Main method to test the control.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();

        final List<LocalDate> holidays = new ArrayList<>(10);
        holidays.add(LocalDate.of(2024, 5, 27));
        holidays.add(LocalDate.of(2024, 6, 19));
        holidays.add(LocalDate.of(2024, 7, 4));
        holidays.add(LocalDate.of(2024, 9, 2));
        final LocalDateTime selected = LocalDateTime.of(2024, 7, 25, 13, 30, 45);

        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new JFrame("Test");
            final JPanel content = new JPanel(new BorderLayout());
            content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            frame.setContentPane(content);

            final JPanel flow = new JPanel(new BorderLayout());
            final JPanel left = new JPanel(new BorderLayout());
            flow.add(left, BorderLayout.LINE_START);

            content.add(flow, BorderLayout.PAGE_START);
            left.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            left.add(new JLabel("Date/Time: "), BorderLayout.PAGE_START);

            final JDateTimeChooser chooser = new JDateTimeChooser(selected, holidays, Skin.BODY_12_FONT,
                    SwingConstants.VERTICAL);
//            chooser.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 17));
            chooser.setActionCommand("FOO");
            flow.add(chooser, BorderLayout.CENTER);

            final JLabel result = new JLabel(" ");
            content.add(result, BorderLayout.PAGE_END);

            chooser.addActionListener(e -> {
                final String cmd = e.getActionCommand();
                if ("FOO".equals(cmd)) {
                    final LocalDateTime parsed = chooser.getCurrentDateTime();

                    if (parsed == null) {
                        result.setText("(No date/time entered)");
                    } else {
                        final String dateStr = TemporalUtils.FMT_MDY_AT_HMS_A.format(parsed);
                        result.setText(dateStr);
                    }
                }
            });

            UIUtilities.packAndCenter(frame);
            frame.setVisible(true);
        });
    }
}
