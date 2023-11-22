package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: debugger/disassemble.h, "Z80_command" struct.
 */
final class Z80Com {

    /** WABBITEMU SOURCE: debugger/disassemble.c, "da_opcode" array. */
    static final Z80Com[] DA_OPCODE = {new Z80Com("  nop", 4),
            new Z80Com("  ex %r,%r", 4),
            new Z80Com("  djnz %g", 13),
            new Z80Com("  jr %g", 12),
            new Z80Com("  jr %c,%g", 12),
            new Z80Com("  ld %r,%x", 10),
            new Z80Com("  add %r,%r", 11),
            new Z80Com("  ld (%r),%r", 7),
            new Z80Com("  ld %r,(%r)", 7),
            new Z80Com("  ld (%r),%r", 7),
            new Z80Com("  ld %r,(%r)", 7),
            new Z80Com("  ld (%a),%r", 16),
            new Z80Com("  ld %r,(%a)", 16),
            new Z80Com("  ld (%a),%r", 13),
            new Z80Com("  ld %r,(%a)", 13),
            new Z80Com("  inc %r", 6),
            new Z80Com("  dec %r", 6),
            new Z80Com("  inc %r", 4),
            new Z80Com("  dec %r", 4),
            new Z80Com("  ld %r,%x", 7),
            new Z80Com("  rlca", 4),
            new Z80Com("  rrca", 4),
            new Z80Com("  rla", 4),
            new Z80Com("  rra", 4),
            new Z80Com("  daa", 4),
            new Z80Com("  cpl", 4),
            new Z80Com("  scf", 4),
            new Z80Com("  ccf", 4),
            new Z80Com("  ld %r,%r", 4),
            new Z80Com("  halt", 4),
            new Z80Com("  %s %r", 4),
            new Z80Com("  %s %r,%r", 4),
            new Z80Com("  ret %c", 11),
            new Z80Com("  pop %r", 10),
            new Z80Com("  ret", 10),
            new Z80Com("  exx", 4),
            new Z80Com("  jp (%r)", 4),
            new Z80Com("  ld %r,%r", 6),
            new Z80Com("  jp %c,%a", 10),
            new Z80Com("  jp %a", 10),
            new Z80Com("  out (%x),%r", 11),
            new Z80Com("  in %r,(%x)", 11),
            new Z80Com("  ex (%r),%r", 19),
            new Z80Com("  ex %r,%r", 4),
            new Z80Com("  di", 4),
            new Z80Com("  ei", 4),
            new Z80Com("  call %c,%a", 17),
            new Z80Com("  push %r", 11),
            new Z80Com("  call %a", 17),
            new Z80Com("  %s %x", 4),
            new Z80Com("  %s %r,%x", 4),
            new Z80Com("  rst %xh", 11),
            new Z80Com("  %s %s", -1),
            new Z80Com("  bit %d,%r", 8),
            new Z80Com("  res %d,%r", 8),
            new Z80Com("  set %d,%r", 8),
            new Z80Com("  in %r,(%r)", 12),
            new Z80Com("  out (%r),%r", 12),
            new Z80Com("  sbc %r,%r", 15),
            new Z80Com("  adc %r,%r", 15),
            new Z80Com("  ld (%a),%r", 20),
            new Z80Com("  ld %r,(%a)", 20),
            new Z80Com("  neg", 8),
            new Z80Com("  retn", 14),
            new Z80Com("  reti", 14),
            new Z80Com("  im %s", 8),
            new Z80Com("  ld i,a", 9),
            new Z80Com("  ld r,a", 9),
            new Z80Com("  ld a,i", 9),
            new Z80Com("  ld a,r", 9),
            new Z80Com("  rrd", 18),
            new Z80Com("  rld", 18),
            new Z80Com("  nop", 8),
            new Z80Com("  %s", 21),
            new Z80Com("  %s (%r%h)->%r", 23),
            new Z80Com("  bit %d,(%r%h)->%r", 23),
            new Z80Com("  res %d,(%r%h)->%r", 23),
            new Z80Com("  set %d,(%r%h)->%r", 23),
            new Z80Com("  %s (%s%h)", 23),
            new Z80Com("  bit %d,(%r%h)", 20),
            new Z80Com("  res %d,(%r%h)", 20),
            new Z80Com("  set %d,(%r%h)", 20),
            new Z80Com("  add %r,%r", 15),
            new Z80Com("  ld %r,(%a)", 20),
            new Z80Com("  ld (%a),%r", 20),
            new Z80Com("  inc (%r%h)", 23),
            new Z80Com("  dec (%r%h)", 23),
            new Z80Com("  ld (%r%h),%x", 19),
            new Z80Com("  ld (%r%h),%r", 19),
            new Z80Com("  ld %r,(%r%h)", 19),
            new Z80Com("  %s (%r%h)", 19),
            new Z80Com("  %s %r,(%r%h)", 19),
            new Z80Com("  jp %s", 6),
            new Z80Com("  ld %r,%r", 10),
            new Z80Com("  ex (sp),%s", 23),
            new Z80Com("%l:", -1),
            new Z80Com("  bcall(%l)", -1),
            new Z80Com("  bcall(%a)", -1),
            new Z80Com("  bit %l,(%r+%l)->%r", 23),
            new Z80Com("  res %l,(%r+%l)->%r", 23),
            new Z80Com("  set %l,(%r+%l)->%r", 23),
            new Z80Com("  bit %l,(%r+%l)", 20),
            new Z80Com("  res %l,(%r+%l)", 20),
            new Z80Com("  set %l,(%r+%l)", 20),
            new Z80Com("  bjump(%l)", -1),
            new Z80Com("  bjump(%a)", -1),
            new Z80Com("  ld %r,%x", 10),
            new Z80Com("  ld %r,%r", 7),
            new Z80Com("  inc %r", 11),
            new Z80Com("  dec %r", 11),
            new Z80Com("  bit %d,%r", 12),
            new Z80Com("  res %d,%r", 15),
            new Z80Com("  set %d,%r", 15),};

    /** Formatted string. */
    private String format;

    /** Clocks to complete. */
    private final int clocks;

    /**
     * Constructs a new {@code Z80Com}.
     *
     * @param theFormat the format
     * @param theClocks the clocks
     */
    private Z80Com(final String theFormat, final int theClocks) {

        this.format = theFormat;
        this.clocks = theClocks;
    }

    /**
     * Gets the format.
     *
     * @return the format
     */
    public String getFormat() {

        return this.format;
    }

    /**
     * Sets the format.
     *
     * @param theFormat the format
     */
    public void setFormat(final String theFormat) {

        this.format = theFormat;
    }

    /**
     * Gets the clocks.
     *
     * @return the clocks
     */
    int getClocks() {

        return this.clocks;
    }
}
