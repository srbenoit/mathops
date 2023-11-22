package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.Calc;
import jwabbit.iface.IEventCallback;

/**
 * Event handle AVI audio frame.
 */
class EventHandleAviAudioFrame implements IEventCallback {

    /**
     * Constructs a new {@code EventHandleAviAudioFrame}.
     */
    EventHandleAviAudioFrame() {

        super();
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "handle_avi_audio_frame" function.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    @Override
    public void exec(final Calc calc, final CalcUI theCalcUI) {

//        if (!theCalcUI.isRecording()) {
//            return;
//        }

        // TODO: Get current sound data and add to AVI stream
    }
}
