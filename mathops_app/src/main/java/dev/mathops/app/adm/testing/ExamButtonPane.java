package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A pane that shows a button for an exam along with a status message.
 */
final class ExamButtonPane extends JPanel {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 3076946554619779035L;

    /** The button. */
    private final JButton button;

    /** Padded panel to hold button. */
    private final JPanel padded;

    /** A label that shows the status. */
    private final JLabel status;

    /**
     * Constructs a new {@code ExamButtonPane}.
     *
     * @param buttonTitle      the button title
     * @param listener         the action listener to register with the button
     * @param cmd              the action command to assign to the button
     */
    ExamButtonPane(final String buttonTitle, final ActionListener listener, final String cmd) {

        super(new BorderLayout());

        setBackground(Skin.LIGHTEST);
        final MatteBorder outline = BorderFactory.createMatteBorder(0, 1, 1, 1, Skin.MEDIUM);
        final Border margin = BorderFactory.createEmptyBorder(6, 4, 6, 4);
        final CompoundBorder border = BorderFactory.createCompoundBorder(outline, margin);
        setBorder(border);

        this.button = new JButton(buttonTitle);
        this.button.setFont(Skin.BIG_BUTTON_16_FONT);
        this.button.addActionListener(listener);
        this.button.setActionCommand(cmd);
        this.button.setEnabled(false);

        this.padded = new JPanel(new BorderLayout());
        this.padded.setBackground(Skin.LIGHTEST);
        final Border padding = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        this.padded.setBorder(padding);
        add(this.padded, BorderLayout.CENTER);

        this.padded.add(this.button, BorderLayout.CENTER);

        this.status = new JLabel(CoreConstants.SPC);
        this.status.setHorizontalAlignment(SwingConstants.CENTER);
        this.status.setFont(Skin.BODY_12_FONT);
        this.status.setForeground(Skin.LABEL_COLOR3);
        add(this.status, BorderLayout.PAGE_END);

        final Dimension sz = getPreferredSize();
        final int actualwidth = Math.max(120, sz.width);
        final Dimension pref = new Dimension(actualwidth, sz.height);
        setPreferredSize(pref);
    }

    /**
     * Enables the button.
     */
    @Override
    public void enable() {

        this.button.setEnabled(true);
        setBackground(Skin.OFF_WHITE_GREEN);
        this.padded.setBackground(Skin.OFF_WHITE_GREEN);
    }

    /**
     * Disables the button.
     */
    @Override
    public void disable() {

        this.button.setEnabled(false);
        setBackground(Skin.LIGHTEST);
        this.padded.setBackground(Skin.LIGHTEST);
    }

    /**
     * Updates the status text.
     *
     * @param text the new status text
     */
    void setStatusText(final String text) {

        this.status.setText(text);
    }

    /**
     * Sets the "tool tip" text.
     *
     * @param text the new tool-tip text
     */
    void setTooltip(final String text) {

        if (text == null) {
            this.button.setToolTipText(null);
        } else {
            final boolean blank = text.isBlank();
            this.button.setToolTipText(blank ? null : text);
        }
    }

    /**
     * Indicates (through a darkened background) that the student is not registered in the course.
     */
    void reset() {

        setBackground(Skin.LIGHTEST);
        this.padded.setBackground(Skin.LIGHTEST);
        this.status.setText(CoreConstants.SPC);
    }

    /**
     * Indicates (through a darkened background) that the student is not registered in the course.
     */
    void indicateNotRegistered() {

        setBackground(Skin.LIGHT);
        this.padded.setBackground(Skin.LIGHT);
    }
}
