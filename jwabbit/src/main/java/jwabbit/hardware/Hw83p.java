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
import jwabbit.hardware.device.D83pPort00;
import jwabbit.hardware.device.D83pPort02;
import jwabbit.hardware.device.D83pPort03;
import jwabbit.hardware.device.D83pPort04;
import jwabbit.hardware.device.D83pPort05;
import jwabbit.hardware.device.D83pPort06;
import jwabbit.hardware.device.D83pPort07;
import jwabbit.hardware.device.D83pPort14;
import jwabbit.hardware.device.D83pPort16;
import jwabbit.hardware.device.DKeypad;
import jwabbit.hardware.device.DLCDCommand;
import jwabbit.hardware.device.DLCDData;

/**
 * Hw83p.
 */
public final class Hw83p {

    /** WABBITEMU SOURCE: hardware/83phw.c, "timer_freq83p" array. */
    private static final double[] TIMER_FREQ_83P = {1.0 / 560.0, 1.0 / 248.0, 1.0 / 170.0, 1.0 / 118.0};

    /**
     * Constructs a new {@code Hw83p}.
     */
    public Hw83p() {

        super();
    }

    /**
     * WABBITEMU SOURCE: hardware/83phw.h, "LinkRead" macro.
     *
     * @param cpu the CPU
     * @return the link value
     */
    public static int linkRead(final CPU cpu) {

        return ((cpu.getPIOContext().getLink().getHost() & 0x03)
                | (cpu.getPIOContext().getLink().getClient()[0] & 0x03)) ^ 3;
    }

    /**
     * WABBITEMU SOURCE: hardware/83phw.c, "INT83P_init" function.
     *
     * @param cpu the CPU
     * @return the STDINT
     */
    private static STDINT int83PInit(final CPU cpu) {

        final STDINT stdint = new STDINT();

        stdint.setFreq(0, TIMER_FREQ_83P[0]);
        stdint.setFreq(1, TIMER_FREQ_83P[1]);
        stdint.setFreq(2, TIMER_FREQ_83P[2]);
        stdint.setFreq(3, TIMER_FREQ_83P[3]);

        stdint.setIntactive(0);
        stdint.setTimermax1(stdint.getFreq(3));
        stdint.setLastchk1(cpu.getTimerContext().getElapsed());
        stdint.setTimermax2(stdint.getFreq(3) / 2.0);
        stdint.setLastchk2(cpu.getTimerContext().getElapsed() + stdint.getFreq(3) / 4.0);
        stdint.setOnBackup(0);
        stdint.setOnLatch(false);

        return stdint;
    }

    /**
     * SOURCE: hardware/83phw.c, "link83p_init" function.
     *
     * @return the link
     */
    private static Link link83pInit() {

        final Link link = new Link();

        link.setHost(0);
        link.setClient(link.getHostArray());

        return link;
    }

    /**
     * SOURCE: hardware/83phw.c, "device_init_83p" function.
     *
     * @param cpu the CPU
     * @return 0
     */
    public static int deviceInit83p(final CPU cpu) {

        cpu.clearDevices();

        final LinkAssist assist = new LinkAssist();
        assist.setLinkEnable(0);

        final Link link = link83pInit();
        final Keypad keyp = Keypad.keypadInit();
        final STDINT stdint = int83PInit(cpu);
        final LCD lcd = LCD.lcdInit(cpu, EnumCalcModel.TI_83P);

        final PIOContext pio = cpu.getPIOContext();

        final D83pPort00 dev00 = new D83pPort00(0x00);
        dev00.setActive(true);
        dev00.setLinkAssist(assist);
        pio.setDevice(0x00, dev00);

        final DKeypad dev01 = new DKeypad(0x01);
        dev01.setActive(true);
        dev01.setKeypad(keyp);
        pio.setDevice(0x01, dev01);

        final IDevice dev02 = new D83pPort02(0x02);
        dev02.setActive(true);
        pio.setDevice(0x02, dev02);

        final D83pPort03 dev03 = new D83pPort03(0x03);
        dev03.setActive(true);
        dev03.setStdint(stdint);
        pio.setDevice(0x03, dev03);

        final D83pPort04 dev04 = new D83pPort04(0x04);
        dev04.setActive(true);
        dev04.setStdint(stdint);
        pio.setDevice(0x04, dev04);

        final D83pPort05 dev05 = new D83pPort05(0x05);
        dev05.setActive(true);
        dev05.setLinkAssist(assist);
        pio.setDevice(0x05, dev05);

        final IDevice dev06 = new D83pPort06(0x06);
        dev06.setActive(true);
        pio.setDevice(0x06, dev06);

        final IDevice dev07 = new D83pPort07(0x07);
        dev07.setActive(true);
        pio.setDevice(0x07, dev07);

        final DLCDCommand dev10 = new DLCDCommand(0x10);
        lcd.setCommandCallback(dev10);
        dev10.setActive(true);
        dev10.setLcd(lcd);
        pio.setDevice(0x10, dev10);

        final DLCDData dev11 = new DLCDData(0x11);
        lcd.setDataCallback(dev11);
        dev11.setActive(true);
        dev11.setLcd(lcd);
        pio.setDevice(0x11, dev11);

        final IDevice dev14 = new D83pPort14(0x14);
        dev14.setActive(true);
        pio.setDevice(0x14, dev14);

        final IDevice dev16 = new D83pPort16(0x16);
        dev16.setActive(true);
        dev16.setProtected(true);
        pio.setDevice(0x16, dev16);

        // shadows
        final IDevice dev21 = new D83pPort02(0x21);
        dev21.setActive(true);
        pio.setDevice(0x21, dev21);

        final D83pPort03 dev26 = new D83pPort03(0x26);
        dev26.setActive(true);
        dev26.setStdint(stdint);
        pio.setDevice(0x26, dev26);

        final IDevice dev27 = new D83pPort07(0x27);
        dev27.setActive(true);
        pio.setDevice(0x27, dev27);

        pio.setLcd(lcd);
        pio.setKeypad(keyp);
        pio.setLink(link);
        pio.setStdint(stdint);
        pio.setSeAux(null);
        pio.setLinkAssist(assist);

        pio.setModel(EnumCalcModel.TI_83P);

        cpu.appendInterruptDevice(0x00, 1);
        cpu.appendInterruptDevice(0x03, 8);
        cpu.appendInterruptDevice(0x11, 128);

        return 0;
    }

    /**
     * SOURCE: hardware/83phw.c, "memory_init_83p" function.
     *
     * @param mc the memory context
     * @return 0
     */
    public static int memoryInit83p(final MemoryContext mc) {

        mc.clear();

        mc.setProtectedPageSet(0);

        mc.getFlash().setSize(32 * Memory.PAGE_SIZE);
        mc.getFlash().fill(0, 32 * Memory.PAGE_SIZE, 0xFF);
        mc.getFlash().setVersion(1);

        mc.getRam().setSize(2 * Memory.PAGE_SIZE);

        mc.setBootmapped(false);
        mc.setFlashLocked(true);

        final BankState[] banks = mc.getNormalBanks();
        banks[0].set(mc.getFlash(), 0, 0, false, false, false);
        banks[1].set(mc.getFlash(), 0x1f * Memory.PAGE_SIZE, 0x1f, false, false, false);
        banks[2].set(mc.getFlash(), 0x1f * Memory.PAGE_SIZE, 0x1f, false, false, false);
        banks[3].set(mc.getRam(), 0, 0, false, true, false);
        banks[4].set(null, 0, 0, false, false, false);

        mc.updateBootmapPages();
        mc.activateNormalBanks();

        return 0;
    }
}
