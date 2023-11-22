package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.CPU;
import jwabbit.core.TimerContext;
import jwabbit.gui.fonts.Fonts;
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
 * A panel to show or update CPU register values.
 */
final class DebugCpuPane extends CollapsePane implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6559765397549659301L;

    /** Name color. */
    private static final Color NAME_COLOR = new Color(0x55, 0xAA, 0x55);

    /** Value color. */
    private static final Color VAL_COLOR = Color.BLACK;

    /** The halt checkbox. */
    private final JCheckBox halt;

    /** The frequency field. */
    private final JTextField freq;

    /** The bus field. */
    private final JTextField bus;

    /**
     * Constructs a new {@code DebugCpuPane}.
     */
    DebugCpuPane() {

        super("CPU Status", new JPanel(new GridLayout(3, 1)));

        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);
        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);

        final JPanel center = getCenter();

        this.halt = new JCheckBox("Halt");
        this.halt.setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 0));
        this.halt.setForeground(VAL_COLOR);
        this.halt.setBackground(getBackground());
        this.halt.setFont(sans);
        center.add(this.halt);

        final String[] titles = {"Freq", "Bus"};
        final int numTitles = titles.length;

        final JLabel[] lbls = new JLabel[numTitles];
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < numTitles; ++i) {
            lbls[i] = new JLabel(" " + titles[i]);
            lbls[i].setForeground(NAME_COLOR);
            lbls[i].setFont(sans);
            maxWidth = Math.max(maxWidth, lbls[i].getPreferredSize().width);
            maxHeight = Math.max(maxHeight, lbls[i].getPreferredSize().height);
        }
        for (int i = 0; i < numTitles; ++i) {
            lbls[i].setPreferredSize(new Dimension(maxWidth, maxHeight));
        }

        this.freq = new JTextField(6);
        this.freq.setForeground(VAL_COLOR);
        this.freq.setBackground(getBackground());
        this.freq.setBorder(null);
        this.freq.setFont(mono);

        this.bus = new JTextField(4);
        this.bus.setForeground(VAL_COLOR);
        this.bus.setBackground(getBackground());
        this.bus.setBorder(null);
        this.bus.setFont(mono);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        flow1.setBackground(center.getBackground());
        flow1.add(lbls[0]);
        flow1.add(this.freq);
        center.add(flow1);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        flow2.setBackground(center.getBackground());
        flow2.add(lbls[1]);
        flow2.add(this.bus);
        center.add(flow2);
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

        final CPU cpu = theCalc.getCPU();
        this.halt.setSelected(cpu.isHalt());

        final TimerContext timer = cpu.getTimerContext();
        final int intval = timer.getFreq();
        this.freq.setText(intval / 1000000 + "." + (intval / 100000) % 10 + (intval / 10000) % 10);

        this.bus.setText(Debugger.toHex2(cpu.getBus()));
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.halt.setEnabled(enable);
        this.freq.setEnabled(enable);
        this.bus.setEnabled(enable);
    }
}
