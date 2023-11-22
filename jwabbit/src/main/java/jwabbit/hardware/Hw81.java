package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.BankState;
import jwabbit.core.CPU;
import jwabbit.core.EnumCalcModel;
import jwabbit.core.IDevice;
import jwabbit.core.Memory;
import jwabbit.core.MemoryContext;
import jwabbit.core.PIOContext;
import jwabbit.hardware.device.D81Port00;
import jwabbit.hardware.device.D81Port02;
import jwabbit.hardware.device.D81Port03;
import jwabbit.hardware.device.D81Port04;
import jwabbit.hardware.device.D81Port05;
import jwabbit.hardware.device.D81Port06;
import jwabbit.hardware.device.D81Port10;
import jwabbit.hardware.device.DKeypad;
import jwabbit.hardware.device.DLCDData;

/**
 * Hw81.
 */
public enum Hw81 {
    ;

    /** WABBITEMU SOURCE: hardware/81hw.c, "timer_freq81" array. */
    private static final double[] TIMER_FREQ_81 = {1.0 / 800.0, 1.0 / 400.0, 3.0 / 800.0, 1.0 / 200.0};

    /**
     * WABBITEMU SOURCE: hardware/81hw.c, "INT81_init" function.
     *
     * @param cpu the CPU
     * @return the STDINT
     */
    private static STDINT int81Init(final CPU cpu) {

        final STDINT stdint = new STDINT();

        stdint.setFreq(0, TIMER_FREQ_81[0]);
        stdint.setFreq(1, TIMER_FREQ_81[1]);
        stdint.setFreq(2, TIMER_FREQ_81[2]);
        stdint.setFreq(3, TIMER_FREQ_81[3]);

        stdint.setIntactive(0);
        stdint.setTimermax1(stdint.getFreq(3));
        stdint.setLastchk1(cpu.getTimerContext().getElapsed());
        stdint.setOnBackup(0);
        stdint.setOnLatch(false);

        return stdint;
    }

    /**
     * WABBITEMU SOURCE: hardware/81hw.c, "memory_init_81" function.
     *
     * @param mc the memory context
     * @return 0
     */
    public static int memoryInit81(final MemoryContext mc) {

        mc.clear();

        mc.getFlash().setVersion(1);
        mc.getFlash().setSize(2 * Memory.PAGE_SIZE);
        mc.getFlash().fill(0, 2 * Memory.PAGE_SIZE, 0xFF);

        mc.getRam().setSize(2 * Memory.PAGE_SIZE);

        mc.setBootmapped(false);
        mc.setFlashLocked(true);

        final BankState[] banks = mc.getNormalBanks();
        banks[0].set(mc.getFlash(), 0, 0, false, false, false);
        banks[1].set(mc.getFlash(), Memory.PAGE_SIZE, 1, false, false, false);
        banks[2].set(mc.getFlash(), Memory.PAGE_SIZE, 1, false, false, false);
        banks[3].set(mc.getRam(), 0, 0, false, true, false);
        banks[4].set(null, 0, 0, false, false, false);

        mc.activateNormalBanks();

        return 0;
    }

    /**
     * WABBITEMU SOURCE: hardware/81hw.c, "device_init_81" function.
     *
     * @param cpu the CPU
     * @return 0
     */
    public static int deviceInit81(final CPU cpu) {

        cpu.clearDevices();

        final PIOContext pio = cpu.getPIOContext();

        final LCD lcd = LCD.lcdInit(cpu, EnumCalcModel.TI_81);
        final Keypad keyp = Keypad.keypadInit();
        final STDINT stdint = int81Init(cpu);

        final IDevice dev00 = new D81Port00(0x00);
        pio.setDevice(0x00, dev00);
        dev00.setActive(true);

        final DKeypad dev01 = new DKeypad(0x01);
        dev01.setKeypad(keyp);
        pio.setDevice(0x01, dev01);
        dev01.setActive(true);

        final D81Port02 dev02 = new D81Port02(0x02);
        dev02.setLcd(lcd);
        pio.setDevice(0x02, dev02);
        dev02.setActive(true);

        final D81Port03 dev03 = new D81Port03(0x03);
        dev03.setStdint(stdint);
        pio.setDevice(0x03, dev03);
        dev03.setActive(true);

        final IDevice dev04 = new D81Port04(0x04);
        pio.setDevice(0x04, dev04);
        dev04.setActive(true);

        final IDevice dev05 = new D81Port05(0x05);
        pio.setDevice(0x05, dev05);
        dev05.setActive(true);

        final IDevice dev06 = new D81Port06(0x06);
        pio.setDevice(0x06, dev06);
        dev06.setActive(true);

        final D81Port10 dev10 = new D81Port10(0x10);
        dev10.setLcd(lcd);
        pio.setDevice(0x10, dev10);
        dev10.setActive(true);

        final DLCDData dev11 = new DLCDData(0x11);
        lcd.setDataCallback(dev11);
        dev11.setLcd(lcd);
        pio.setDevice(0x11, dev11);
        dev11.setActive(true);

        pio.setLcd(lcd);
        pio.setKeypad(keyp);
        pio.setLink(null);
        pio.setStdint(stdint);
        pio.setSeAux(null);
        pio.setModel(EnumCalcModel.TI_81);

        cpu.appendInterruptDevice(0x03, 1);
        cpu.appendInterruptDevice(0x10, 255);
        cpu.appendInterruptDevice(0x11, 255);

        return 0;
    }
}
