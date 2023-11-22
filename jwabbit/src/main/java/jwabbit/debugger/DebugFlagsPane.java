package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.JWCoreConstants;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * A panel to show or update CPU register values.
 */
final class DebugFlagsPane extends CollapsePane implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6559765397549659301L;

    /** Register value color. */
    private static final Color TITLE_COLOR = Color.BLACK;

    /** The checkbox fields. */
    private final JCheckBox[] fields;

    /**
     * Constructs a new {@code DebugFlagsPane}.
     */
    DebugFlagsPane() {

        super("Flags", new JPanel(new GridLayout(2, 3)));

        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);

        final JPanel center = getCenter();

        final String[] titles = {"z", "c", "s", "p/v", "hc", "n"};
        final int numTitles = titles.length;

        this.fields = new JCheckBox[numTitles];
        for (int i = 0; i < numTitles; ++i) {
            this.fields[i] = new JCheckBox(titles[i]);
            this.fields[i].setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 0));
            this.fields[i].setForeground(TITLE_COLOR);
            this.fields[i].setBackground(getBackground());
            this.fields[i].setFont(sans);
            center.add(this.fields[i]);
        }
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

        final int val = theCalc.getCPU().getF();

        this.fields[0].setSelected((val & JWCoreConstants.ZERO_MASK) != 0);
        this.fields[1].setSelected((val & JWCoreConstants.CARRY_MASK) != 0);
        this.fields[2].setSelected((val & JWCoreConstants.SIGN_MASK) != 0);
        this.fields[3].setSelected((val & JWCoreConstants.PV_MASK) != 0);
        this.fields[4].setSelected((val & JWCoreConstants.HC_MASK) != 0);
        this.fields[5].setSelected((val & JWCoreConstants.N_MASK) != 0);
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        for (final JCheckBox field : this.fields) {
            field.setEnabled(enable);
        }
    }
}
