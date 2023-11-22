package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.MD5;
import jwabbit.log.LoggedObject;

/**
 * The MD5 device for the TI 83pse hardware.
 */
public final class DMD5Ports extends AbstractDevice {

    /** The MD5 processor. */
    private MD5 md5;

    /**
     * Constructs a new {@code DeviceMD5Ports}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public DMD5Ports(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.md5 = null;
    }

    /**
     * Sets the MD5 processor.
     *
     * @param theMd5 the MD5 processor
     */
    public void setMD5(final MD5 theMd5) {

        this.md5 = theMd5;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "md5ports" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            switch (getDevIndex()) {
                case 0x18:
                case 0x19:
                case 0x1A:
                case 0x1B:
                    cpu.setBus(0);
                    break;
                case 0x1C:
                case 0x1D:
                case 0x1E:
                case 0x1F:
                    cpu.setBus((int) ((calcMD5() >> ((getDevIndex() - 0x1C) << 3)) & 0xFFL));
                    break;
                default:
                    LoggedObject.LOG.warning("Unhandled case");
                    break;
            }
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            switch (getDevIndex()) {
                case 0x18:
                case 0x19:
                case 0x1A:
                case 0x1B:
                case 0x1C:
                case 0x1D:
                    this.md5.setReg(getDevIndex() - 0x18,
                            (this.md5.getReg(getDevIndex() - 0x18) >> 8) | ((long) cpu.getBus() << 24));
                    break;
                case 0x1E:
                    this.md5.setS(cpu.getBus() & 0x1F);
                    break;
                case 0x1F:
                    this.md5.setMode(cpu.getBus() & 0x03);
                    break;
                default:
                    LoggedObject.LOG.warning("Unhandled case");
                    break;
            }
            cpu.setOutput(false);
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "calc_md5" function.
     *
     * @return the hash
     */
    private long calcMD5() {

        final long a = this.md5.getA();
        final long b = this.md5.getB();
        final long c = this.md5.getC();
        final long d = this.md5.getD();
        final long x = this.md5.getX();
        final long ac = this.md5.getAC();
        long reg = 0L;

        switch (this.md5.getMode()) {
            case 0:
                reg = (b & c) | ((~b) & d);
                break;
            case 1:
                reg = (b & d) | (c & (~d));
                break;
            case 2:
                reg = b ^ c ^ d;
                break;
            case 3:
                reg = c ^ (b | (~d));
                break;
            default:
                LoggedObject.LOG.warning("Unhandled MD5 mode");
                break;
        }
        reg += a + x + ac;
        reg &= 0x0FFFFFFFFL;
        final int s = this.md5.getS() & 0x1f;
        reg = (reg << s) | (reg >> (32 - s));
        reg += b;

        return reg;
    }
}
