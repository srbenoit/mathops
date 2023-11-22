package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.core.WideAddr;
import jwabbit.iface.Calc;
import jwabbit.iface.IEventCallback;
import jwabbit.iface.Label;
import jwabbit.utilities.BCall;

/**
 * EventCreateBCallLabels.
 */
class EventCreateBCallLabels implements IEventCallback {

    /**
     * Constructs a new {@code EventCreateBCallLabels}.
     */
    EventCreateBCallLabels() {

        // No action
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "create_bcall_labels" function.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    @Override
    public final void exec(final Calc calc, final CalcUI theCalcUI) {

        if (calc == null || theCalcUI == null || calc.getModel().ordinal() < EnumCalcModel.TI_83P.ordinal()
                || !theCalcUI.isTIOSDebug()) {
            return;
        }

        calc.voidLabels();

        final BCall[] bcalls = BCall.getBcalls(calc.getModel());
        int ptr = 0;

        int labelIndex = 0;
        while (labelIndex < calc.getNumLabels() && calc.getLabel(labelIndex).getName() != null) {
            ++labelIndex;
        }

        if (bcalls != null) {
            while (bcalls[ptr] != null && bcalls[ptr].getAddress() != -1) {
                final int page;
                if ((bcalls[ptr].getAddress() & (1 << 15)) != 0) {
                    // it's on the boot page
                    page = calc.getCPU().getMemoryContext().getFlash().getPages() - 1;
                } else if ((bcalls[ptr].getAddress() & (1 << 14)) != 0) {
                    // it's on the bcall page
                    page = switch (calc.getModel()) {
                        case TI_84PCSE -> 5;
                        default -> calc.getCPU().getMemoryContext().getFlash().getPages() - 5;
                    };
                } else {
                    continue;
                }

                final int bcallAddress = bcalls[ptr].getAddress() & 0x3FFF;
                WideAddr waddr = new WideAddr(page, bcallAddress, false);

                final int realAddress = calc.getCPU().getMemoryContext().wmemRead16(waddr);

                waddr = new WideAddr(page, bcallAddress + 2, false);
                final int realPage = calc.getCPU().getMemoryContext().wmemRead(waddr);

                // exclude the _
                final String name = bcalls[ptr].getName().substring(1);

                final Label label = calc.getLabel(labelIndex);

                label.setName(name);
                label.setAddr(realAddress);
                label.setPage(realPage);
                label.setRam(false);

                ++labelIndex;
                ++ptr;
            }
        }
    }
}
