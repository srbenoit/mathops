package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.TimerContext;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * A panel to display details of a timer context.
 */
final class TimerContextPanel extends LoggedPanel implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 879157027890412242L;

    /** The timer version. */
    private final JTextField version;

    /** The frequency. */
    private final JTextField freq;

    /** The number of states. */
    private final JTextField states;

    /** The elapsed time. */
    private final JTextField elapsed;

    /** The last time. */
    private final JTextField lastTime;

    /**
     * Constructs a new {@code TimerContextPanel}.
     */
    TimerContextPanel() {

        super(new BorderLayout(4, 4));

        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);
        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);

        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        setBackground(Debugger.BG_COLOR);

        final JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 0, 0)));
        top.setBackground(Debugger.BG_COLOR);
        final JLabel title = new JLabel("Timer Context Detail");
        title.setFont(Fonts.getSans().deriveFont(Font.BOLD, title.getFont().getSize2D() + 1.0f));
        title.setForeground(new Color(100, 0, 0));
        top.add(title);
        add(top, BorderLayout.PAGE_START);

        final JPanel west = new JPanel(new BorderLayout());
        west.setBackground(Debugger.BG_COLOR);
        add(west, BorderLayout.LINE_START);

        final String[] titles = {"Version", "Frequency", "States", "Elapsed", "Last Time"};
        final int numTitles = titles.length;

        final JPanel north = new JPanel(new GridLayout(numTitles, 1));
        north.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        north.setBackground(Debugger.BG_COLOR);
        west.add(north, BorderLayout.PAGE_START);

        final JLabel[] lbls = new JLabel[numTitles];
        int maxWidth = 0;
        int maxHeight = 0;
        final JPanel[] flows = new JPanel[numTitles];
        for (int i = 0; i < numTitles; ++i) {
            lbls[i] = new JLabel(" " + titles[i]);
            lbls[i].setFont(sans);
            maxWidth = Math.max(maxWidth, lbls[i].getPreferredSize().width);
            maxHeight = Math.max(maxHeight, lbls[i].getPreferredSize().height);
        }
        for (int i = 0; i < numTitles; ++i) {
            lbls[i].setPreferredSize(new Dimension(maxWidth, maxHeight));

            flows[i] = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            flows[i].setBackground(getBackground());
            flows[i].add(lbls[i]);
            north.add(flows[i]);
        }

        this.version = new JTextField(4);
        this.version.setFont(mono);

        this.freq = new JTextField(6);
        this.freq.setFont(mono);

        this.states = new JTextField(14);
        this.states.setFont(mono);

        this.elapsed = new JTextField(14);
        this.elapsed.setFont(mono);

        this.lastTime = new JTextField(14);
        this.lastTime.setFont(mono);

        flows[0].add(this.version);
        flows[1].add(this.freq);
        flows[2].add(this.states);
        flows[3].add(this.elapsed);
        flows[4].add(this.lastTime);

        final JLabel mhz = new JLabel(" MHz");
        mhz.setFont(sans);
        flows[1].add(mhz);

        final JLabel ms1 = new JLabel(" sec");
        ms1.setFont(sans);
        flows[3].add(ms1);

        final JLabel ms2 = new JLabel(" sec");
        ms2.setFont(sans);
        flows[4].add(ms2);
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

        if (this.isShowing()) {
            final TimerContext timer = theCalc.getCPU().getTimerContext();

            this.version.setText(Integer.toString(timer.getTimerVersion()));

            final int freqVal = timer.getFreq();
            this.freq.setText(freqVal / 1000000 + "." + (freqVal / 100000) % 10 + (freqVal / 10000) % 10);

            this.states.setText(Long.toString(timer.getTStates()));
            this.elapsed.setText(Float.toString((float) timer.getElapsed()));
            this.lastTime.setText(Float.toString((float) timer.getLasttime()));
        }
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.version.setEnabled(enable);
        this.freq.setEnabled(enable);
        this.states.setEnabled(enable);
        this.elapsed.setEnabled(enable);
        this.lastTime.setEnabled(enable);
    }
}
