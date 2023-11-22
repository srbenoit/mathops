package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.Calc;
import jwabbit.iface.EnumGifDispState;
import jwabbit.iface.IEventCallback;

import javax.swing.JFrame;
import java.awt.Container;

/**
 * Event GUI draw.
 */
class EventGuiDraw implements IEventCallback {

    /** WABBITEMU SOURCE: gui/gui.c, "gui_draw" function, "skip" static variable. */
    private int guiDrawSkip;

    /**
     * Constructs a new {@code EventGuiDraw}.
     */
    EventGuiDraw() {

        super();
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "gui_draw" function.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    @Override
    public final void exec(final Calc calc, final CalcUI theCalcUI) {

        if (theCalcUI.getDetachedLCDFrame() != null) {
            theCalcUI.getDetachedLCDFrame().repaint();
        }

        if (theCalcUI.getGifDispState() != EnumGifDispState.GDS_IDLE) {
            if (this.guiDrawSkip == 0) {
                Gui.gifAnimAdvance = true;
                final JFrame frame = theCalcUI.getMainFrame();
                if (frame != null) {
                    final Container content = frame.getContentPane();
                    content.repaint();
                }
            }

            this.guiDrawSkip = (this.guiDrawSkip + 1) % 4;
        }
    }
}
