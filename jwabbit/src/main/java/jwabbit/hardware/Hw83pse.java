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
import jwabbit.core.JWCoreConstants;
import jwabbit.core.Memory;
import jwabbit.core.MemoryContext;
import jwabbit.core.PIOContext;
import jwabbit.hardware.device.D83pseClockEnable;
import jwabbit.hardware.device.D83pseClockRead;
import jwabbit.hardware.device.D83pseClockSet;
import jwabbit.hardware.device.D83pseDelayPort;
import jwabbit.hardware.device.D83psePort00;
import jwabbit.hardware.device.D83psePort02;
import jwabbit.hardware.device.D83psePort03;
import jwabbit.hardware.device.D83psePort04;
import jwabbit.hardware.device.D83psePort05;
import jwabbit.hardware.device.D83psePort06;
import jwabbit.hardware.device.D83psePort07;
import jwabbit.hardware.device.D83psePort08;
import jwabbit.hardware.device.D83psePort09;
import jwabbit.hardware.device.D83psePort0A;
import jwabbit.hardware.device.D83psePort0D;
import jwabbit.hardware.device.D83psePort0E;
import jwabbit.hardware.device.D83psePort0F;
import jwabbit.hardware.device.D83psePort10;
import jwabbit.hardware.device.D83psePort11;
import jwabbit.hardware.device.D83psePort14;
import jwabbit.hardware.device.D83psePort15;
import jwabbit.hardware.device.D83psePort20;
import jwabbit.hardware.device.D83psePort21;
import jwabbit.hardware.device.D83psePort22;
import jwabbit.hardware.device.D83psePort23;
import jwabbit.hardware.device.D83psePort24;
import jwabbit.hardware.device.D83psePort25;
import jwabbit.hardware.device.D83psePort26;
import jwabbit.hardware.device.D83psePort30;
import jwabbit.hardware.device.D83psePort31;
import jwabbit.hardware.device.D83psePort32;
import jwabbit.hardware.device.D83psePort3A;
import jwabbit.hardware.device.D83psePort4A;
import jwabbit.hardware.device.D83psePort4C;
import jwabbit.hardware.device.D83psePort4D;
import jwabbit.hardware.device.D83psePort54;
import jwabbit.hardware.device.D83psePort55;
import jwabbit.hardware.device.D83psePort56;
import jwabbit.hardware.device.D83psePort57;
import jwabbit.hardware.device.D83psePort5B;
import jwabbit.hardware.device.D83psePort80;
import jwabbit.hardware.device.D83psePortChunkRemap;
import jwabbit.hardware.device.DColorLCDCommand;
import jwabbit.hardware.device.DColorLCDData;
import jwabbit.hardware.device.DKeypad;
import jwabbit.hardware.device.DLCDCommand;
import jwabbit.hardware.device.DLCDData;
import jwabbit.hardware.device.DMD5Ports;

/**
 * Hardware for 83PSE.
 */
public final class Hw83pse {

    /** WABBITEMU SOURCE: hardware/83psehw.h, "USB_MASK" enum. */
    public static final int VBUS_HIGH_MASK = 0x40;

    /** WABBITEMU SOURCE: hardware/83psehw.c, "timer_freq83pse" array. */
    private static final double[] TIMER_FREQ_83_PSE = {1.0 / 512.0, 1.0 / 227.0, 1.0 / 158.0, 1.0 / 108.0};

    /**
     * Constructs a new {@code Hw83pse}.
     */
    public Hw83pse() {

        super();
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.h, "LinkRead" macro.
     *
     * @param cpu the CPU
     * @return the link value
     */
    public static int linkRead(final CPU cpu) {

        return ((cpu.getPIOContext().getLink().getHost() & 0x03)
                | (cpu.getPIOContext().getLink().getClient()[0] & 0x03)) ^ 3;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "UpdateDelays" function.
     *
     * @param cpu   the CPU
     * @param delay the delay object
     */
    public static void updateDelays(final CPU cpu, final Delay delay) {

        final int enable = delay.getReg(cpu.getCPUSpeed());
        final int select = delay.getReg(0x2E - 0x29);

        final MemoryContext mc = cpu.getMemoryContext();

        mc.setReadOPFlashTStates(((enable & 1) != 0 && (select & 0x01) != 0) ? 1 : 0);
        mc.setReadNOPFlashTStates(((enable & 1) != 0 && (select & 0x02) != 0) ? 1 : 0);
        mc.setWriteFlashTStates(((enable & 1) != 0 && (select & 0x04) != 0) ? 1 : 0);
        mc.setReadOPRamTStates(((enable & 2) != 0 && (select & 0x10) != 0) ? 1 : 0);
        mc.setReadNOPRamTStates(((enable & 2) != 0 && (select & 0x20) != 0) ? 1 : 0);
        mc.setWriteRamTStates(((enable & 2) != 0 && (select & 0x40) != 0) ? 1 : 0);
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "mod_timer" function.
     *
     * @param cpu  the CPU
     * @param xtal the XTAL
     */
    public static void modTimer(final CPU cpu, final XTAL xtal) {

        final int a = ((xtal.getTimer(0).getClock() & 0xC0) >> 6) & 0x03;
        final int b = ((xtal.getTimer(1).getClock() & 0xC0) >> 6) & 0x03;
        final int c = ((xtal.getTimer(2).getClock() & 0xC0) >> 6) & 0x03;

        switch (a | b | c) {
            case 0:
                cpu.modifyInterruptDevice(0x32, 0);
                break;
            case 1:
                // cpu.modifyInterruptDevice(0x32, 8);
                cpu.modifyInterruptDevice(0x32, 1);
                break;
            case 2:
            case 3:
                cpu.modifyInterruptDevice(0x32, 1);
                break;
            default:
                break;
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "INT83PSE_init" function.
     *
     * @param cpu the CPU
     * @return the initialized STDINT
     */
    private static STDINT int83PSEinit(final CPU cpu) {

        final STDINT stdint = new STDINT();

        stdint.setFreq(0, TIMER_FREQ_83_PSE[0]);
        stdint.setFreq(1, TIMER_FREQ_83_PSE[1]);
        stdint.setFreq(2, TIMER_FREQ_83_PSE[2]);
        stdint.setFreq(3, TIMER_FREQ_83_PSE[3]);

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "link83pse_init" function.
     *
     * @return the initialized Link
     */
    private static Link link83pseInit() {

        final Link link = new Link();

        link.setHost(0);
        link.setClient(link.getHostArray());

        return link;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "SE_AUX_init" function.
     *
     * @return the initialized SE_AUX
     */
    private static SEAux seAuxInit() {

        final SEAux seAux = new SEAux();

        seAux.getXtal().getTimer(0).setDivisor(1.0);
        seAux.getXtal().getTimer(1).setDivisor(1.0);
        seAux.getXtal().getTimer(2).setDivisor(1.0);
        seAux.getLinka().setLinkEnable(0x80);
        seAux.getUsb().setUSBEvents(0x50L);
        seAux.getUsb().setUSBLineState(0xA5L);
        seAux.getUsb().setLineInterrupt(false);
        seAux.getUsb().setProtocolInterrupt(false);
        seAux.setGpio(0);

        return seAux;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "device_init_83pse" function.
     *
     * @param cpu   the CPU
     * @param model the model
     * @return 0
     */
    public static int deviceInit83pse(final CPU cpu, final EnumCalcModel model) {

        cpu.clearDevices();

        final Link link = link83pseInit();
        final Keypad keyp = Keypad.keypadInit();
        final STDINT stdint = int83PSEinit(cpu);
        final SEAux seAux = seAuxInit();

        final AbstractLCDBase lcd;
        if (model == EnumCalcModel.TI_84PCSE) {
            final ColorLCD colorlcd = ColorLCD.colorLcdInit();
            lcd = colorlcd;

            final DColorLCDCommand command = new DColorLCDCommand(0x10);
            command.setLcd(colorlcd);
            lcd.setCommandCallback(command);

            final DColorLCDData data = new DColorLCDData(0x11);
            data.setLcd(colorlcd);
            lcd.setDataCallback(data);
        } else {
            lcd = LCD.lcdInit(cpu, EnumCalcModel.TI_83PSE);

            final DLCDCommand command = new DLCDCommand(0x10);
            command.setLcd(lcd);
            lcd.setCommandCallback(command);

            final DLCDData data = new DLCDData(0x11);
            data.setLcd(lcd);
            lcd.setDataCallback(data);
        }

        final PIOContext pio = cpu.getPIOContext();

        final D83psePort00 dev00 = new D83psePort00(0x00);
        dev00.setActive(true);
        dev00.setLink(link);
        pio.setDevice(0x00, dev00);

        final DKeypad dev01 = new DKeypad(0x01);
        dev01.setActive(true);
        dev01.setKeypad(keyp);
        pio.setDevice(0x01, dev01);

        final IDevice dev02 = new D83psePort02(0x02);
        dev02.setActive(true);
        pio.setDevice(0x02, dev02);

        final D83psePort03 dev03 = new D83psePort03(0x03);
        dev03.setActive(true);
        dev03.setStdint(stdint);
        pio.setDevice(0x03, dev03);

        final D83psePort04 dev04 = new D83psePort04(0x04);
        dev04.setActive(true);
        dev04.setStdint(stdint);
        pio.setDevice(0x04, dev04);

        final IDevice dev05 = new D83psePort05(0x05);
        dev05.setActive(true);
        pio.setDevice(0x05, dev05);

        final IDevice dev06 = new D83psePort06(0x06);
        dev06.setActive(true);
        pio.setDevice(0x06, dev06);

        final IDevice dev07 = new D83psePort07(0x07);
        dev07.setActive(true);
        pio.setDevice(0x07, dev07);

        final D83psePort08 dev08 = new D83psePort08(0x08);
        dev08.setActive(true);
        dev08.setLinkAssist(seAux.getLinka());
        pio.setDevice(0x08, dev08);

        final D83psePort09 dev09 = new D83psePort09(0x09);
        dev09.setActive(true);
        dev09.setLinkAssist(seAux.getLinka());
        pio.setDevice(0x09, dev09);

        final D83psePort0A dev0A = new D83psePort0A(0x0A);
        dev0A.setActive(true);
        dev0A.setLinkAssist(seAux.getLinka());
        pio.setDevice(0x0A, dev0A);

        final D83psePort0D dev0D = new D83psePort0D(0x0D);
        dev0D.setActive(true);
        dev0D.setLinkAssist(seAux.getLinka());
        pio.setDevice(0x0D, dev0D);

        final IDevice dev0E = new D83psePort0E(0x0E);
        dev0E.setActive(true);
        pio.setDevice(0x0E, dev0E);

        final IDevice dev0F = new D83psePort0F(0x0F);
        dev0F.setActive(true);
        pio.setDevice(0x0F, dev0F);

        final D83psePort10 dev10 = new D83psePort10(0x10);
        dev10.setActive(true);
        dev10.setLcd(lcd);
        pio.setDevice(0x10, dev10);

        final D83psePort11 dev11 = new D83psePort11(0x11);
        dev11.setActive(true);
        dev11.setLcd(lcd);
        pio.setDevice(0x11, dev11);

        final IDevice dev14 = new D83psePort14(0x14);
        dev14.setActive(true);
        pio.setDevice(0x14, dev14);

        final IDevice dev15 = new D83psePort15(0x15);
        dev15.setActive(true);
        pio.setDevice(0x15, dev15);

        for (int i = 0x18; i <= 0x1F; ++i) {
            final DMD5Ports devI = new DMD5Ports(i);
            devI.setActive(true);
            devI.setMD5(seAux.getMd5());
            pio.setDevice(i, devI);
        }

        final IDevice dev20 = new D83psePort20(0x20);
        dev20.setActive(true);
        pio.setDevice(0x20, dev20);

        final IDevice dev21 = new D83psePort21(0x21);
        dev21.setActive(true);
        dev21.setProtected(true);
        pio.setDevice(0x21, dev21);

        final IDevice dev22 = new D83psePort22(0x22);
        dev22.setActive(true);
        dev22.setProtected(true);
        pio.setDevice(0x22, dev22);

        final IDevice dev23 = new D83psePort23(0x23);
        dev23.setActive(true);
        dev23.setProtected(true);
        pio.setDevice(0x23, dev23);

        final IDevice dev24 = new D83psePort24(0x24);
        dev24.setActive(true);
        dev24.setProtected(true);
        pio.setDevice(0x24, dev24);

        final IDevice dev25 = new D83psePort25(0x25);
        dev25.setActive(true);
        dev25.setProtected(true);
        pio.setDevice(0x25, dev25);

        final IDevice dev26 = new D83psePort26(0x26);
        dev26.setActive(true);
        dev26.setProtected(true);
        pio.setDevice(0x26, dev26);

        final D83psePortChunkRemap dev27 = new D83psePortChunkRemap(0x27);
        dev27.setActive(true);
        dev27.setData(cpu.getMemoryContext().getPort27RemapCount());
        pio.setDevice(0x27, dev27);

        final D83psePortChunkRemap dev28 = new D83psePortChunkRemap(0x28);
        dev28.setActive(true);
        dev28.setData(cpu.getMemoryContext().getPort28RemapCount());
        pio.setDevice(0x28, dev28);

        for (int i = 0x29; i <= 0x2F; ++i) {
            final D83pseDelayPort devI = new D83pseDelayPort(i);
            devI.setActive(true);
            devI.setDelay(seAux.getDelay());
            pio.setDevice(i, devI);
        }

        final D83psePort30 dev30 = new D83psePort30(0x30);
        dev30.setActive(true);
        dev30.setXtal(seAux.getXtal());
        pio.setDevice(0x30, dev30);

        final D83psePort31 dev31 = new D83psePort31(0x31);
        dev31.setActive(true);
        dev31.setXtal(seAux.getXtal());
        pio.setDevice(0x31, dev31);

        final D83psePort32 dev32 = new D83psePort32(0x32);
        dev32.setActive(true);
        dev32.setXtal(seAux.getXtal());
        pio.setDevice(0x32, dev32);

        final D83psePort30 dev33 = new D83psePort30(0x33);
        dev33.setActive(true);
        dev33.setXtal(seAux.getXtal());
        pio.setDevice(0x33, dev33);

        final D83psePort31 dev34 = new D83psePort31(0x34);
        dev34.setActive(true);
        dev34.setXtal(seAux.getXtal());
        pio.setDevice(0x34, dev34);

        final D83psePort32 dev35 = new D83psePort32(0x35);
        dev35.setActive(true);
        dev35.setXtal(seAux.getXtal());
        pio.setDevice(0x35, dev35);

        final D83psePort30 dev36 = new D83psePort30(0x36);
        dev36.setActive(true);
        dev36.setXtal(seAux.getXtal());
        pio.setDevice(0x36, dev36);

        final D83psePort31 dev37 = new D83psePort31(0x37);
        dev37.setActive(true);
        dev37.setXtal(seAux.getXtal());
        pio.setDevice(0x37, dev37);

        final D83psePort32 dev38 = new D83psePort32(0x38);
        dev38.setActive(true);
        dev38.setXtal(seAux.getXtal());
        pio.setDevice(0x38, dev38);

        final D83psePort3A dev3A = new D83psePort3A(0x3A);
        dev3A.setActive(true);
        dev3A.setSeAux(seAux);
        pio.setDevice(0x3A, dev3A);

        final D83pseClockEnable dev40 = new D83pseClockEnable(0x40);
        dev40.setActive(true);
        dev40.setClock(seAux.getClock());
        pio.setDevice(0x40, dev40);

        final D83pseClockSet dev41 = new D83pseClockSet(0x41);
        dev41.setActive(true);
        dev41.setClock(seAux.getClock());
        pio.setDevice(0x41, dev41);

        final D83pseClockSet dev42 = new D83pseClockSet(0x42);
        dev42.setActive(true);
        dev42.setClock(seAux.getClock());
        pio.setDevice(0x42, dev42);

        final D83pseClockSet dev43 = new D83pseClockSet(0x43);
        dev43.setActive(true);
        dev43.setClock(seAux.getClock());
        pio.setDevice(0x43, dev43);

        final D83pseClockSet dev44 = new D83pseClockSet(0x44);
        dev44.setActive(true);
        dev44.setClock(seAux.getClock());
        pio.setDevice(0x44, dev44);

        final D83pseClockRead dev45 = new D83pseClockRead(0x45);
        dev45.setActive(true);
        dev45.setClock(seAux.getClock());
        pio.setDevice(0x45, dev45);

        final D83pseClockRead dev46 = new D83pseClockRead(0x46);
        dev46.setActive(true);
        dev46.setClock(seAux.getClock());
        pio.setDevice(0x46, dev46);

        final D83pseClockRead dev47 = new D83pseClockRead(0x47);
        dev47.setActive(true);
        dev47.setClock(seAux.getClock());
        pio.setDevice(0x47, dev47);

        final D83pseClockRead dev48 = new D83pseClockRead(0x48);
        dev48.setActive(true);
        dev48.setClock(seAux.getClock());
        pio.setDevice(0x48, dev48);

        final D83psePort4A dev4A = new D83psePort4A(0x4A);
        dev4A.setActive(true);
        dev4A.setUsb(seAux.getUsb());
        pio.setDevice(0x4A, dev4A);

        final D83psePort4C dev4C = new D83psePort4C(0x4C);
        dev4C.setActive(true);
        dev4C.setUsb(seAux.getUsb());
        pio.setDevice(0x4C, dev4C);

        final D83psePort4D dev4D = new D83psePort4D(0x4D);
        dev4D.setActive(true);
        dev4D.setUsb(seAux.getUsb());
        pio.setDevice(0x4D, dev4D);

        final D83psePort54 dev54 = new D83psePort54(0x54);
        dev54.setActive(true);
        dev54.setUsb(seAux.getUsb());
        pio.setDevice(0x54, dev54);

        final D83psePort55 dev55 = new D83psePort55(0x55);
        dev55.setActive(true);
        dev55.setUsb(seAux.getUsb());
        pio.setDevice(0x55, dev55);

        final D83psePort56 dev56 = new D83psePort56(0x56);
        dev56.setActive(true);
        dev56.setUsb(seAux.getUsb());
        pio.setDevice(0x56, dev56);

        final D83psePort57 dev57 = new D83psePort57(0x57);
        dev57.setActive(true);
        dev57.setUsb(seAux.getUsb());
        pio.setDevice(0x57, dev57);

        final D83psePort5B dev5B = new D83psePort5B(0x5B);
        dev5B.setActive(true);
        dev5B.setUsb(seAux.getUsb());
        pio.setDevice(0x5B, dev5B);

        final D83psePort80 dev80 = new D83psePort80(0x80);
        dev80.setActive(true);
        dev80.setUsb(seAux.getUsb());
        pio.setDevice(0x80, dev80);

        pio.setLcd(lcd);
        pio.setKeypad(keyp);
        pio.setLink(link);
        pio.setStdint(stdint);
        pio.setSeAux(seAux);
        pio.setModel(model);

        cpu.appendInterruptDevice(0x00, 1);
        cpu.appendInterruptDevice(0x03, 8);
        cpu.appendInterruptDevice(0x11, 128);
        cpu.appendInterruptDevice(0x09, 3);
        cpu.appendInterruptDevice(0x32, 8);

        return 0;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "memory_init_83pse" function.
     *
     * @param mc the memory context
     * @return 0
     */
    public static int memoryInit83pse(final MemoryContext mc) {

        mc.clear();

        mc.getFlash().setSize(128 * Memory.PAGE_SIZE);
        mc.getFlash().fill(0, 128 * Memory.PAGE_SIZE, 0xFF);
        mc.getRam().setSize(8 * Memory.PAGE_SIZE);

        mc.getFlash().setVersion(2);
        mc.getFlash().setUpper(0x60);
        mc.getFlash().setLower(0x10);

        mc.getRam().setLower(0);
        mc.getRam().setUpper(0x3FF);

        mc.setBootmapped(false);
        mc.setFlashLocked(true);
        mc.setProtMode(JWCoreConstants.MODE0);

        final BankState[] banks = mc.getNormalBanks();
        banks[0].set(mc.getFlash(), 0x7f * Memory.PAGE_SIZE, 0x7f, false, false, false);
        banks[1].set(mc.getFlash(), 0, 0, false, false, false);
        banks[2].set(mc.getFlash(), 0, 0, false, false, false);
        banks[3].set(mc.getRam(), 0, 0, false, true, false);
        banks[4].set(null, 0, 0, false, false, false);

        mc.updateBootmapPages();
        mc.activateNormalBanks();

        return 0;
    }

    /**
     * SOURCE: hardware/83psehw.c, "memory_init_84p" function.
     *
     * @param mc the memory context
     * @return 0
     */
    public static int memoryInit84p(final MemoryContext mc) {

        mc.clear();

        mc.getFlash().setSize(64 * Memory.PAGE_SIZE);
        mc.getFlash().fill(0, 64 * Memory.PAGE_SIZE, 0xFF);

        mc.getRam().setSize(8 * Memory.PAGE_SIZE);

        mc.getFlash().setVersion(3);
        mc.getFlash().setLower(0x30);
        mc.getFlash().setLower(0x10);

        mc.setBootmapped(false);
        mc.setFlashLocked(true);
        mc.setProtMode(JWCoreConstants.MODE0);

        final BankState[] banks = mc.getNormalBanks();
        banks[0].set(mc.getFlash(), 0x3f * Memory.PAGE_SIZE, 0x3f, false, false, false);
        banks[1].set(mc.getFlash(), 0, 0, false, false, false);
        banks[2].set(mc.getFlash(), 0, 0, false, false, false);
        banks[3].set(mc.getRam(), 0, 0, false, true, false);
        banks[4].set(null, 0, 0, false, false, false);

        mc.updateBootmapPages();
        mc.activateNormalBanks();

        return 0;
    }

    /**
     * SOURCE: hardware/83psehw.c, "memory_init_84pcse" function.
     *
     * @param mc the memory context
     * @return 0
     */
    public static int memoryInit84pcse(final MemoryContext mc) {

        mc.clear();

        mc.getFlash().setSize(256 * Memory.PAGE_SIZE);
        mc.getFlash().fill(0, 256 * Memory.PAGE_SIZE, 0xFF);

        mc.getRam().setSize(8 * Memory.PAGE_SIZE);

        mc.getFlash().setVersion(2);
        mc.getFlash().setLower(0xC0);
        mc.getFlash().setLower(0x10);

        mc.getRam().setLower(0);
        mc.getRam().setUpper(0x3FF);

        mc.setBootmapped(false);
        mc.setFlashLocked(true);
        mc.setProtMode(JWCoreConstants.MODE0);

        final BankState[] banks = mc.getNormalBanks();
        banks[0].set(mc.getFlash(), 0xff * Memory.PAGE_SIZE, 0xff, false, false, false);
        banks[1].set(mc.getFlash(), 0, 0, false, false, false);
        banks[2].set(mc.getFlash(), 0, 0, false, false, false);
        banks[3].set(mc.getRam(), 0, 0, false, true, false);
        banks[4].set(null, 0, 0, false, false, false);

        mc.updateBootmapPages();
        mc.activateNormalBanks();

        return 0;
    }
}
