package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.gui.fonts.Fonts;
import jwabbit.hardware.Keypad;
import jwabbit.iface.Calc;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * A panel to show keyboard status.
 */
final class DebugKeyboardPane extends CollapsePane implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6713021665713938562L;

    /** Register name color. */
    private static final Color REGNAME_COLOR = new Color(0x55, 0xAA, 0x55);

    /** Register value color. */
    private static final Color REGVAL_COLOR = Color.BLACK;

    /** The text fields. */
    private final JTextField[] fields;

    /** The on key pressed check box. */
    private final JCheckBox onPress;

    /**
     * Constructs a new {@code DebugKeyboardPane}.
     */
    DebugKeyboardPane() {

        super("Keyboard", new JPanel(new GridLayout(8, 1)));

        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);
        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);

        final JPanel center = getCenter();

        final String[] titles = {"Group FE  ", "Group FD  ", "Group FB  ", "Group F7  ", "Group EF  ", "Group DF  ",
                "Group BF  "};
        final int numTitles = titles.length;

        final JLabel[] lbls = new JLabel[numTitles];
        this.fields = new JTextField[numTitles];
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < numTitles; ++i) {
            lbls[i] = new JLabel(" " + titles[i]);
            lbls[i].setForeground(REGNAME_COLOR);
            lbls[i].setFont(sans);
            maxWidth = Math.max(maxWidth, lbls[i].getPreferredSize().width);
            maxHeight = Math.max(maxHeight, lbls[i].getPreferredSize().height);

            this.fields[i] = new JTextField(10);
            this.fields[i].setForeground(REGVAL_COLOR);
            this.fields[i].setBackground(getBackground());
            this.fields[i].setBorder(null);
            this.fields[i].setFont(mono);
        }
        for (int i = 0; i < numTitles; ++i) {
            lbls[i].setPreferredSize(new Dimension(maxWidth, maxHeight));
        }

        for (int i = 0; i < numTitles; ++i) {
            final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            flow.setBackground(center.getBackground());
            flow.add(lbls[i]);
            flow.add(this.fields[i]);
            center.add(flow);
        }

        this.onPress = new JCheckBox("On Key Press");
        this.onPress.setBackground(center.getBackground());
        this.onPress.setFont(sans);
        this.onPress.setBorder(BorderFactory.createEmptyBorder());
        center.add(this.onPress);
    }

    /**
     * Called from the calculator thread to allow a client to retrieve data values from a running or stopped calculator
     * without fear of thread conflicts. The receiver should try to minimize time in the function, but will have
     * exclusive access to the calculator data while this method executes.
     *
     * @param theCalc the calculator
     */
    @Override
    public void calcState(final Calc theCalc) {

        // Called from the AWT event thread while the calculator thread is suspended

        final Keypad keypad = theCalc.getCPU().getPIOContext().getKeypad();

        final StringBuilder builder = new StringBuilder(8);
        for (int group = 0; group < 7; ++group) {
            for (int bit = 0; bit < 8; ++bit) {
                if (keypad.getKey(group, bit) == 0) {
                    builder.append('0');
                } else {
                    builder.append('1');
                }
            }
            this.fields[group].setText(builder.toString());
            builder.setLength(0);
        }

        this.onPress.setSelected(keypad.getOnLastPressed() != 0L);
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        for (final JTextField field : this.fields) {
            field.setEnabled(enable);
        }
        this.onPress.setEnabled(enable);
    }
}
