package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.Serial;

/**
 * The debugger main panel.
 */
final class MainPanel extends LoggedPanel implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3968265144619140261L;

    /** The refreshable panes. */
    private final ICalcStateListener[] panes;

    /** The debug summary panel. */
    private final DebugPanel debugPanel;

    /** The CPU list panel. */
    private final CPUPanel cpuPanel;

    /** The timer context panel. */
    private final TimerContextPanel timerPanel;

    /** The memory context panel. */
    private final MemoryContextPanel memcPanel;

    /** The PIO context panel. */
    private final PIOContextPanel pioPanel;

    /**
     * Constructs a new {@code MainPanel}.
     *
     * @param theHandler the action handler
     */
    MainPanel(final ActionHandler theHandler) {

        super(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));
        setBackground(Debugger.BG_COLOR);

        final Font font = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);

        final JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tabs.setBackground(Debugger.BG_COLOR);
        tabs.setFont(font);
        this.add(tabs, BorderLayout.CENTER);

        this.debugPanel = new DebugPanel(theHandler);
        this.cpuPanel = new CPUPanel();
        this.timerPanel = new TimerContextPanel();
        this.memcPanel = new MemoryContextPanel();
        this.pioPanel = new PIOContextPanel();

        this.panes = new ICalcStateListener[5];
        this.panes[0] = this.debugPanel;
        this.panes[1] = this.cpuPanel;
        this.panes[2] = this.timerPanel;
        this.panes[3] = this.memcPanel;
        this.panes[4] = this.pioPanel;

        tabs.addTab("Summary", this.debugPanel);
        tabs.addTab("CPU", this.cpuPanel);
        tabs.addTab("Timer Context", this.timerPanel);
        tabs.addTab("Mem Context", this.memcPanel);
        tabs.addTab("PIO Context", this.pioPanel);
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

        for (final ICalcStateListener pane : this.panes) {
            pane.calcState(theCalc);
        }
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        this.debugPanel.enableControls(enable);
        this.cpuPanel.enableControls(enable);
        this.timerPanel.enableControls(enable);
        this.memcPanel.enableControls(enable);
        this.pioPanel.enableControls(enable);
    }
}
