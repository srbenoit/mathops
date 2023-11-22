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
import jwabbit.hardware.device.D82Port00;
import jwabbit.hardware.device.D83Port00;
import jwabbit.hardware.device.D83Port02;
import jwabbit.hardware.device.D83Port03;
import jwabbit.hardware.device.D83Port04;
import jwabbit.hardware.device.D83Port14;
import jwabbit.hardware.device.DKeypad;
import jwabbit.hardware.device.DLCDCommand;
import jwabbit.hardware.device.DLCDData;
import jwabbit.log.LoggedObject;

/**
 * Hw83.
 */
public final class Hw83 {

    /** WABBITEMU SOURCE: hardware/83hw.c, "timer_freq83" array. */
    private static final double[] TIMER_FREQ_83 = {1.0 / 600.0, 1.0 / 257.14, 1.0 / 163.63, 1.0 / 120.0};

    /** WABBITEMU SOURCE: hardware/83phw.c, "SWAP_BANK" macro. */
    private static final int SWAP_BANK = 0xFF;

    /** WABBITEMU SOURCE: hardware/83phw.c, "ROM0_8" macro. */
    private static final int ROM0_8 = 0xFE;

    /** WABBITEMU SOURCE: hardware/83phw.c, "ROM1_8" macro. */
    private static final int ROM1_8 = 0xFD;

    /** WABBITEMU SOURCE: hardware/83phw.c, "ROM1_9" macro. */
    private static final int ROM1_9 = 0xFC;

    /** WABBITEMU SOURCE: hardware/83phw.c, "ROM0" macro. */
    private static final int ROM0 = 0x00;

    /** WABBITEMU SOURCE: hardware/83phw.c, "RAM0" macro. */
    private static final int RAM0 = 0x08;

    /** WABBITEMU SOURCE: hardware/83phw.c, "RAM1" macro. */
    private static final int RAM1 = 0x09;

    /** WABBITEMU SOURCE: hardware/83phw.c, "banks83" array. */
    private static final int[][] BANKS_83 =
            {{ROM0, SWAP_BANK, ROM0_8, RAM0}, {ROM0, SWAP_BANK, ROM1_8, RAM0},
                    {ROM0, SWAP_BANK, RAM0, RAM0}, {ROM0, SWAP_BANK, RAM1, RAM0},

                    {ROM0, SWAP_BANK, ROM0_8, RAM0}, {ROM0, SWAP_BANK, ROM1_8, RAM0},
                    {ROM0, SWAP_BANK, RAM0, RAM0}, {ROM0, SWAP_BANK, RAM1, RAM0},

                    {ROM0, ROM0_8, SWAP_BANK, ROM0_8}, {ROM0, ROM0_8, SWAP_BANK, ROM1_9},
                    {ROM0, ROM0_8, SWAP_BANK, RAM0}, {ROM0, ROM0_8, SWAP_BANK, RAM1},

                    {ROM0, RAM0, RAM1, ROM0_8}, {ROM0, RAM0, RAM1, ROM1_9}, {ROM0, RAM0, RAM1, RAM0},
                    {ROM0, RAM0, RAM1, RAM1}};

    /**
     * Constructs a new {@code Hw83}.
     */
    public Hw83() {

        super();
    }

    /**
     * WABBITEMU SOURCE: hardware/83hw.c, "setpage83" array.
     *
     * @param cpu the CPU
     */
    public static void setpage83(final CPU cpu) {

        final STDINT stdint = cpu.getPIOContext().getStdint();
        final MemoryContext memc = cpu.getMemoryContext();
        final BankState[] banks = memc.getBanks();

        final int xy = (stdint.getXy() & 0x10) >> 4;
        final int ram = (stdint.getMem() & 0x40) >> 6;
        final int page = (stdint.getMem() & 0x07) + ((stdint.getXy() & 0x10) >> 0x01);
        final int rpage = page % memc.getRam().getPages();
        final int fpage = page % memc.getFlash().getPages();
        final int mem = ((stdint.getMem() & 0x08) >> 3) + ((stdint.getMem() & 0x80) >> 6) + (ram << 2)
                + (memc.isBootmapped() ? 8 : 0);

        for (int i = 0; i < 4; ++i) {
            final BankState bank = banks[i];

            switch (BANKS_83[mem][i]) {
                case ROM0:
                    bank.setMem(memc.getFlash());
                    bank.setAddr(0);
                    bank.setPage(0x00);
                    bank.setReadOnly(false);
                    bank.setRam(false);
                    bank.setNoExec(false);
                    break;
                case RAM0:
                    bank.setMem(memc.getRam());
                    bank.setAddr(0);
                    bank.setPage(0x00);
                    bank.setReadOnly(false);
                    bank.setRam(true);
                    bank.setNoExec(false);
                    break;
                case RAM1:
                    bank.setMem(memc.getRam());
                    bank.setAddr(Memory.PAGE_SIZE);
                    bank.setPage(0x01);
                    bank.setReadOnly(false);
                    bank.setRam(true);
                    bank.setNoExec(false);
                    break;
                case ROM0_8:
                    bank.setMem(memc.getFlash());
                    if (xy == 0) {
                        bank.setAddr(0);
                        bank.setPage(0x00);
                    } else {
                        bank.setAddr(0x08 * Memory.PAGE_SIZE);
                        bank.setPage(0x08);
                    }
                    bank.setReadOnly(false);
                    bank.setRam(false);
                    bank.setNoExec(false);
                    break;
                case ROM1_8:
                    bank.setMem(memc.getFlash());
                    if (xy == 0) {
                        bank.setAddr(Memory.PAGE_SIZE);
                        bank.setPage(0x01);
                    } else {
                        bank.setAddr(0x08 * Memory.PAGE_SIZE);
                        bank.setPage(0x08);
                    }
                    bank.setReadOnly(false);
                    bank.setRam(false);
                    bank.setNoExec(false);
                    break;
                case ROM1_9:
                    bank.setMem(memc.getFlash());
                    if (xy == 0) {
                        bank.setAddr(Memory.PAGE_SIZE);
                        bank.setPage(0x01);
                    } else {
                        bank.setAddr(0x09 * Memory.PAGE_SIZE);
                        bank.setPage(0x09);
                    }
                    bank.setReadOnly(false);
                    bank.setRam(false);
                    bank.setNoExec(false);
                    break;
                case SWAP_BANK:
                    if (ram == 0) {
                        bank.setMem(memc.getFlash());
                        bank.setAddr(fpage * Memory.PAGE_SIZE);
                        bank.setPage(fpage);
                        bank.setRam(false);
                    } else {
                        bank.setMem(memc.getRam());
                        bank.setAddr(rpage * Memory.PAGE_SIZE);
                        bank.setPage(rpage);
                        bank.setRam(true);
                    }
                    bank.setReadOnly(false);
                    bank.setNoExec(false);
                    break;
                default:
                    LoggedObject.LOG.warning("Unhandled case");
                    break;
            }
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/83hw.c, "INT83_init" function.
     *
     * @param cpu the CPU
     * @return the STDINT
     */
    private static STDINT int83Init(final CPU cpu) {

        final STDINT stdint = new STDINT();

        stdint.setFreq(0, TIMER_FREQ_83[0]);
        stdint.setFreq(1, TIMER_FREQ_83[1]);
        stdint.setFreq(2, TIMER_FREQ_83[2]);
        stdint.setFreq(3, TIMER_FREQ_83[3]);

        stdint.setIntactive(0);
        stdint.setTimermax1(stdint.getFreq(3));
        stdint.setLastchk1(cpu.getTimerContext().getElapsed());
        stdint.setTimermax2(stdint.getFreq(3) / 2.0);
        stdint.setLastchk2(cpu.getTimerContext().getElapsed() + stdint.getFreq(3) / 4.0);

        stdint.setMem(0);
        stdint.setXy(0);

        return stdint;
    }

    /**
     * WABBITEMU SOURCE: hardware/83hw.c, "link83_init" function.
     *
     * @return the Link
     */
    private static Link link83Init() {

        final Link link = new Link();

        link.setHost(0);
        link.setClient(link.getHostArray());

        return link;
    }

    /**
     * WABBITEMU SOURCE: hardware/83hw.c, "device_init_83" function.
     *
     * @param cpu   the CPU
     * @param bad82 true if this is an 82
     * @return 0
     */
    public static int deviceInit83(final CPU cpu, final boolean bad82) {

        cpu.clearDevices();

        final Link link = link83Init();
        final Keypad keyp = Keypad.keypadInit();
        final STDINT stdint = int83Init(cpu);
        final LCD lcd = LCD.lcdInit(cpu, EnumCalcModel.TI_83);

        final PIOContext pio = cpu.getPIOContext();

        if (bad82) {
            final D82Port00 dev00 = new D82Port00(0x00);
            dev00.setActive(true);
            dev00.setLink(link);
            pio.setDevice(0x00, dev00);
        } else {
            final D83Port00 dev00 = new D83Port00(0x00);
            dev00.setActive(true);
            dev00.setLink(link);
            pio.setDevice(0x00, dev00);
        }

        final DKeypad dev01 = new DKeypad(0x01);
        dev01.setActive(true);
        dev01.setKeypad(keyp);
        pio.setDevice(0x01, dev01);

        final D83Port02 dev02 = new D83Port02(0x02);
        dev02.setActive(true);
        dev02.setStdint(stdint);
        pio.setDevice(0x02, dev02);

        final D83Port03 dev03 = new D83Port03(0x03);
        dev03.setActive(true);
        dev03.setStdint(stdint);
        pio.setDevice(0x03, dev03);

        final D83Port04 dev04 = new D83Port04(0x04);
        dev04.setActive(true);
        dev04.setStdint(stdint);
        pio.setDevice(0x04, dev04);

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

        final IDevice dev14 = new D83Port14(0x14);
        dev14.setActive(true);
        pio.setDevice(0x14, dev14);

        pio.setLcd(lcd);
        pio.setKeypad(keyp);
        pio.setLink(link);
        pio.setStdint(stdint);
        pio.setSeAux(null);
        pio.setModel(EnumCalcModel.TI_83);

        cpu.appendInterruptDevice(0x00, 1);
        cpu.appendInterruptDevice(0x03, 8);
        cpu.appendInterruptDevice(0x11, 138);

        return 0;
    }

    /**
     * WABBITEMU SOURCE: hardware/83hw.c, "memory_init_83" function.
     *
     * @param mc the memory context
     * @return 0
     */
    public static int memoryInit83(final MemoryContext mc) {

        mc.clear();

        mc.getFlash().setSize(16 * Memory.PAGE_SIZE);
        mc.getFlash().fill(0, 16 * Memory.PAGE_SIZE, 0xFF);

        mc.getRam().setSize(2 * Memory.PAGE_SIZE);

        mc.getFlash().setVersion(0);
        mc.setBootmapped(false);
        mc.setFlashLocked(true);

        final BankState[] banks = mc.getNormalBanks();
        banks[0].set(mc.getFlash(), 0, 0, false, false, false);
        banks[1].set(mc.getFlash(), 0, 0, false, false, false);
        banks[2].set(mc.getRam(), Memory.PAGE_SIZE, 1, false, true, false);
        banks[3].set(mc.getRam(), 0, 0, false, true, false);
        banks[4].set(null, 0, 0, false, false, false);

        mc.activateNormalBanks();

        return 0;
    }
}
