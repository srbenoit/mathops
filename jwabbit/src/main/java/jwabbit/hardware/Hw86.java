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
import jwabbit.hardware.device.D86Port00;
import jwabbit.hardware.device.D86Port02;
import jwabbit.hardware.device.D86Port03;
import jwabbit.hardware.device.D86Port04;
import jwabbit.hardware.device.D86Port05;
import jwabbit.hardware.device.D86Port06;
import jwabbit.hardware.device.D86Port07;
import jwabbit.hardware.device.D86Port10;
import jwabbit.hardware.device.DKeypad;
import jwabbit.hardware.device.DLCDData;

/**
 * Hw86.
 */
public enum Hw86 {
    ;

    /** WABBITEMU SOURCE: hardware/86hw.c, "timer_freq" array. */
    private static final double[] TIMER_FREQ_86 = {1.0 / 800.0, 1.0 / 400.0, 3.0 / 800.0, 1.0 / 200.0};

    /**
     * WABBITEMU SOURCE: hardware/86hw.c, "INT86_init" function.
     *
     * @param cpu the CPU
     * @return the STDINT
     */
    private static STDINT int86Init(final CPU cpu) {

        final STDINT stdint = new STDINT();

        stdint.setFreq(0, TIMER_FREQ_86[0]);
        stdint.setFreq(1, TIMER_FREQ_86[1]);
        stdint.setFreq(2, TIMER_FREQ_86[2]);
        stdint.setFreq(3, TIMER_FREQ_86[3]);

        stdint.setIntactive(0);
        stdint.setTimermax1(stdint.getFreq(3));
        stdint.setLastchk1(cpu.getTimerContext().getElapsed());
        stdint.setOnBackup(0);
        stdint.setOnLatch(false);

        return stdint;
    }

    /**
     * WABBITEMU SOURCE: hardware/86hw.c, "link86_init" function.
     *
     * @return the link
     */
    private static Link link86Init() {

        final Link link = new Link();

        link.setHost(0);
        link.setClient(link.getHostArray());

        return link;
    }

    /**
     * WABBITEMU SOURCE: hardware/86hw.c, "device_init_86" function.
     *
     * @param cpu the CPU
     * @return 0
     */
    public static int deviceInit86(final CPU cpu) {

        cpu.clearDevices();

        final Keypad keyp = Keypad.keypadInit();
        final STDINT stdint = int86Init(cpu);
        final Link link = link86Init();
        final LCD lcd = LCD.lcdInit(cpu, EnumCalcModel.TI_86);

        final PIOContext pio = cpu.getPIOContext();

        final D86Port00 dev00 = new D86Port00(0x00);
        dev00.setActive(true);
        dev00.setLcd(lcd);
        pio.setDevice(0x00, dev00);

        final DKeypad dev01 = new DKeypad(0x01);
        dev01.setActive(true);
        dev01.setKeypad(keyp);
        pio.setDevice(0x01, dev01);

        final D86Port02 dev02 = new D86Port02(0x02);
        dev02.setActive(true);
        dev02.setLcd(lcd);
        pio.setDevice(0x02, dev02);

        final D86Port03 dev03 = new D86Port03(0x03);
        dev03.setActive(true);
        dev03.setStdint(stdint);
        pio.setDevice(0x03, dev03);

        final D86Port04 dev04 = new D86Port04(0x04);
        dev04.setActive(true);
        dev04.setData(0);
        pio.setDevice(0x04, dev04);

        final D86Port05 dev05 = new D86Port05(0x05);
        dev05.setActive(true);
        dev05.setStdint(stdint);
        pio.setDevice(0x05, dev05);

        final IDevice dev06 = new D86Port06(0x06);
        dev06.setActive(true);
        pio.setDevice(0x06, dev06);

        final D86Port07 dev07 = new D86Port07(0x07);
        dev07.setActive(true);
        dev07.setLink(link);
        pio.setDevice(0x07, dev07);

        final D86Port10 dev10 = new D86Port10(0x10);
        dev10.setActive(true);
        dev10.setLcd(lcd);
        pio.setDevice(0x10, dev10);

        final DLCDData dev11 = new DLCDData(0x11);
        lcd.setDataCallback(dev11);
        dev11.setActive(true);
        dev11.setLcd(lcd);
        pio.setDevice(0x11, dev11);

        pio.setLcd(lcd);
        pio.setKeypad(keyp);
        pio.setLink(link);
        pio.setStdint(stdint);
        pio.setSeAux(null);

        pio.setModel(EnumCalcModel.TI_86);

        cpu.appendInterruptDevice(0x03, 1);
        cpu.appendInterruptDevice(0x10, 255);
        cpu.appendInterruptDevice(0x11, 255);

        return 0;
    }

    /**
     * WABBITEMU SOURCE: hardware/86hw.c, "memory_init_86" function.
     *
     * @param mc the memory context
     * @return 0
     */
    public static int memoryInit86(final MemoryContext mc) {

        mc.clear();

        mc.getFlash().setSize(16 * Memory.PAGE_SIZE);
        mc.getFlash().fill(0, 16 * Memory.PAGE_SIZE, 0x00FF);

        mc.getRam().setSize(8 * Memory.PAGE_SIZE);

        mc.getFlash().setVersion(1);

        mc.setBootmapped(false);
        mc.setFlashLocked(true);

        final BankState[] banks = mc.getNormalBanks();
        banks[0].set(mc.getFlash(), 0, 0, false, false, false);
        banks[1].set(mc.getFlash(), 0x0F * Memory.PAGE_SIZE, 0x0F, false, false, false);
        banks[2].set(mc.getFlash(), 0, 0, false, false, false);
        banks[3].set(mc.getRam(), 0, 0, false, true, false);
        banks[4].set(null, 0, 0, false, false, false);

        mc.activateNormalBanks();

        return 0;
    }
}
