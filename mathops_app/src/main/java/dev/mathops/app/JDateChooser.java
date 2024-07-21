package dev.mathops.app;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import jwabbit.CoreConstants;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
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
public final class JDateChooser extends JPanel implements ActionListener, MouseListener {

    /** The action command to open a dropdown calendar picker. */
    private static final String DATE_TYPED_CMD = "DATE_TYPED_CMD";

    /** The text field into which the user can type, or in which the selected date is displayed. */
    private final JTextField dateField;

    /** The list of action listeners. */
    private final List<ActionListener> listeners;

    /** The action command. */
    private String actionCommand = CoreConstants.EMPTY;

    /** The current date. */
    private LocalDate date;

    private JPopupMenu popup;

    /**
     * Constructs a new {@code JDateChooser}with a specified starting value.
     *
     * @param currentValue the current value (can be null)
     */
    public JDateChooser(final LocalDate currentValue) {

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

        final BasicArrowButton arrow = new BasicArrowButton(SwingConstants.SOUTH);
        arrow.addMouseListener(this);
        add(arrow, BorderLayout.LINE_END);

        this.date = currentValue;
        if (currentValue != null) {
            final String dateStr = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(currentValue);
            this.dateField.setText(dateStr);
        }

        this.popup = new JPopupMenu();

        final JMenuItem menuItem = new JMenuItem("A popup menu item");
        menuItem.addActionListener(this);
        this.popup.add(menuItem);

    }

    /**
     * Constructs a new {@code JDateChooser) with today's date as the current value.
     */
    public JDateChooser() {

        this(LocalDate.now());
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
                final String newText = TemporalUtils.FMT_MDY_COMPACT_FIXED.format(parsed);
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
                newDate = TemporalUtils.FMT_MDY_COMPACT_FIXED.parse(dateString);
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

        final Point where = getLocation();
        final Dimension size = getSize();
        final Dimension fieldSize = this.dateField.getSize();

        Log.info("Location = ", where, ", size = ", size);

        this.popup.show(e.getComponent(), -fieldSize.width, where.y + size.height - 1);
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
     * Main method to test the control.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new JFrame("Test");
            final JPanel content = new JPanel(new BorderLayout());
            content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            frame.setContentPane(content);

            final JPanel flow = new JPanel(new BorderLayout());
            content.add(flow, BorderLayout.PAGE_START);
            flow.add(new JLabel("Date: "), BorderLayout.LINE_START);

            final JDateChooser chooser = new JDateChooser();
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
