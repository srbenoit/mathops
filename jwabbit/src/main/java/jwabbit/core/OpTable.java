package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.alu.AdcANum8;
import jwabbit.core.alu.AdcAReg8;
import jwabbit.core.alu.AdcHLReg16;
import jwabbit.core.alu.AddANum8;
import jwabbit.core.alu.AddAReg8;
import jwabbit.core.alu.AddHLReg16;
import jwabbit.core.alu.AndNum8;
import jwabbit.core.alu.AndReg8;
import jwabbit.core.alu.Bit;
import jwabbit.core.alu.CpNum8;
import jwabbit.core.alu.CpReg8;
import jwabbit.core.alu.Cpd;
import jwabbit.core.alu.Cpdr;
import jwabbit.core.alu.Cpi;
import jwabbit.core.alu.Cpir;
import jwabbit.core.alu.Cpl;
import jwabbit.core.alu.Daa;
import jwabbit.core.alu.DecReg16;
import jwabbit.core.alu.DecReg8;
import jwabbit.core.alu.IncReg16;
import jwabbit.core.alu.IncReg8;
import jwabbit.core.alu.Neg;
import jwabbit.core.alu.OrNum8;
import jwabbit.core.alu.OrReg8;
import jwabbit.core.alu.Res;
import jwabbit.core.alu.RlReg;
import jwabbit.core.alu.Rla;
import jwabbit.core.alu.RlcReg;
import jwabbit.core.alu.Rlca;
import jwabbit.core.alu.Rld;
import jwabbit.core.alu.RrReg;
import jwabbit.core.alu.Rra;
import jwabbit.core.alu.RrcReg;
import jwabbit.core.alu.Rrca;
import jwabbit.core.alu.Rrd;
import jwabbit.core.alu.SbcANum8;
import jwabbit.core.alu.SbcAReg8;
import jwabbit.core.alu.SbcHLReg16;
import jwabbit.core.alu.Set;
import jwabbit.core.alu.SlaReg;
import jwabbit.core.alu.SllReg;
import jwabbit.core.alu.SraReg;
import jwabbit.core.alu.SrlReg;
import jwabbit.core.alu.SubANum8;
import jwabbit.core.alu.SubAReg8;
import jwabbit.core.alu.XorNum8;
import jwabbit.core.alu.XorReg8;
import jwabbit.core.control.Call;
import jwabbit.core.control.CallCondition;
import jwabbit.core.control.Ccf;
import jwabbit.core.control.Di;
import jwabbit.core.control.Djnz;
import jwabbit.core.control.EdNop;
import jwabbit.core.control.Ei;
import jwabbit.core.control.ExAFAFp;
import jwabbit.core.control.ExDEHL;
import jwabbit.core.control.ExSPHL;
import jwabbit.core.control.Exx;
import jwabbit.core.control.Halt;
import jwabbit.core.control.IM0;
import jwabbit.core.control.IM1;
import jwabbit.core.control.IM2;
import jwabbit.core.control.In;
import jwabbit.core.control.InRegC;
import jwabbit.core.control.Ind;
import jwabbit.core.control.Indr;
import jwabbit.core.control.Ini;
import jwabbit.core.control.Inir;
import jwabbit.core.control.Jp;
import jwabbit.core.control.JpCondition;
import jwabbit.core.control.JpHL;
import jwabbit.core.control.Jr;
import jwabbit.core.control.JrCondition;
import jwabbit.core.control.LdABC;
import jwabbit.core.control.LdADE;
import jwabbit.core.control.LdAI;
import jwabbit.core.control.LdAMem16;
import jwabbit.core.control.LdAR;
import jwabbit.core.control.LdBCA;
import jwabbit.core.control.LdBCNum16;
import jwabbit.core.control.LdDEA;
import jwabbit.core.control.LdDENum16;
import jwabbit.core.control.LdHLNum16;
import jwabbit.core.control.LdHLfMem16;
import jwabbit.core.control.LdIA;
import jwabbit.core.control.LdMem16A;
import jwabbit.core.control.LdMem16HLf;
import jwabbit.core.control.LdMem16Reg16;
import jwabbit.core.control.LdRA;
import jwabbit.core.control.LdRNum8;
import jwabbit.core.control.LdRR;
import jwabbit.core.control.LdReg16Mem16;
import jwabbit.core.control.LdSPHL;
import jwabbit.core.control.LdSPNum16;
import jwabbit.core.control.Ldd;
import jwabbit.core.control.Lddr;
import jwabbit.core.control.Ldi;
import jwabbit.core.control.Ldir;
import jwabbit.core.control.Nop;
import jwabbit.core.control.Otdr;
import jwabbit.core.control.Otir;
import jwabbit.core.control.Out;
import jwabbit.core.control.OutReg;
import jwabbit.core.control.Outd;
import jwabbit.core.control.Outi;
import jwabbit.core.control.PopReg16;
import jwabbit.core.control.PushReg16;
import jwabbit.core.control.Ret;
import jwabbit.core.control.RetCondition;
import jwabbit.core.control.RetI;
import jwabbit.core.control.RetN;
import jwabbit.core.control.Rst;
import jwabbit.core.control.Scf;
import jwabbit.core.indexcb.BitInd;
import jwabbit.core.indexcb.ResInd;
import jwabbit.core.indexcb.RlInd;
import jwabbit.core.indexcb.RlcInd;
import jwabbit.core.indexcb.RrInd;
import jwabbit.core.indexcb.RrcInd;
import jwabbit.core.indexcb.SetInd;
import jwabbit.core.indexcb.SlaInd;
import jwabbit.core.indexcb.SllInd;
import jwabbit.core.indexcb.SraInd;
import jwabbit.core.indexcb.SrlInd;

/**
 * Opcode table.
 */
enum OpTable {
    ;

    /** Opcode table (all functions taking a CPU argument, returning void). */
    static final IOpcode[] opcode = {

            // 0
            new Nop(), //
            new LdBCNum16(), //
            new LdBCA(), //
            new IncReg16(), //
            new IncReg8(), //
            new DecReg8(), //
            new LdRNum8(), //
            new Rlca(), //

            new ExAFAFp(), //
            new AddHLReg16(), //
            new LdABC(), //
            new DecReg16(), //
            new IncReg8(), //
            new DecReg8(), //
            new LdRNum8(), //
            new Rrca(), //

            // 10
            new Djnz(), //
            new LdDENum16(), //
            new LdDEA(), //
            new IncReg16(), //
            new IncReg8(), //
            new DecReg8(), //
            new LdRNum8(), //
            new Rla(), //

            new Jr(), //
            new AddHLReg16(), //
            new LdADE(), //
            new DecReg16(), //
            new IncReg8(), //
            new DecReg8(), //
            new LdRNum8(), //
            new Rra(), //

            // 20
            new JrCondition(), //
            new LdHLNum16(), //
            new LdMem16HLf(), //
            new IncReg16(), //
            new IncReg8(), //
            new DecReg8(), //
            new LdRNum8(), //
            new Daa(), //

            new JrCondition(), //
            new AddHLReg16(), //
            new LdHLfMem16(), //
            new DecReg16(), //
            new IncReg8(), //
            new DecReg8(), //
            new LdRNum8(), //
            new Cpl(), //

            // 30
            new JrCondition(), //
            new LdSPNum16(), //
            new LdMem16A(), //
            new IncReg16(), //
            new IncReg8(), //
            new DecReg8(), //
            new LdRNum8(), //
            new Scf(), //

            new JrCondition(), //
            new AddHLReg16(), //
            new LdAMem16(), //
            new DecReg16(), //
            new IncReg8(), //
            new DecReg8(), //
            new LdRNum8(), //
            new Ccf(), //

            // 40
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //

            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //

            // 50
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //

            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //

            // 60
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //

            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //

            // 70
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new Halt(), //
            new LdRR(), //

            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //
            new LdRR(), //

            // 80
            new AddAReg8(), //
            new AddAReg8(), //
            new AddAReg8(), //
            new AddAReg8(), //
            new AddAReg8(), //
            new AddAReg8(), //
            new AddAReg8(), //
            new AddAReg8(), //

            new AdcAReg8(), //
            new AdcAReg8(), //
            new AdcAReg8(), //
            new AdcAReg8(), //
            new AdcAReg8(), //
            new AdcAReg8(), //
            new AdcAReg8(), //
            new AdcAReg8(), //

            // 90
            new SubAReg8(), //
            new SubAReg8(), //
            new SubAReg8(), //
            new SubAReg8(), //
            new SubAReg8(), //
            new SubAReg8(), //
            new SubAReg8(), //
            new SubAReg8(), //

            new SbcAReg8(), //
            new SbcAReg8(), //
            new SbcAReg8(), //
            new SbcAReg8(), //
            new SbcAReg8(), //
            new SbcAReg8(), //
            new SbcAReg8(), //
            new SbcAReg8(), //

            // A0
            new AndReg8(), //
            new AndReg8(), //
            new AndReg8(), //
            new AndReg8(), //
            new AndReg8(), //
            new AndReg8(), //
            new AndReg8(), //
            new AndReg8(), //

            new XorReg8(), //
            new XorReg8(), //
            new XorReg8(), //
            new XorReg8(), //
            new XorReg8(), //
            new XorReg8(), //
            new XorReg8(), //
            new XorReg8(), //

            // B0
            new OrReg8(), //
            new OrReg8(), //
            new OrReg8(), //
            new OrReg8(), //
            new OrReg8(), //
            new OrReg8(), //
            new OrReg8(), //
            new OrReg8(), //

            new CpReg8(), //
            new CpReg8(), //
            new CpReg8(), //
            new CpReg8(), //
            new CpReg8(), //
            new CpReg8(), //
            new CpReg8(), //
            new CpReg8(), //

            // C0
            new RetCondition(), //
            new PopReg16(), //
            new JpCondition(), //
            new Jp(), //
            new CallCondition(), //
            new PushReg16(), //
            new AddANum8(), //
            new Rst(), //

            new RetCondition(), //
            new Ret(), //
            new JpCondition(), //
            new CPUCBOpcodeRun(), //
            new CallCondition(), //
            new Call(), //
            new AdcANum8(), //
            new Rst(), //

            // D0
            new RetCondition(), //
            new PopReg16(), //
            new JpCondition(), //
            new Out(), //
            new CallCondition(), //
            new PushReg16(), //
            new SubANum8(), //
            new Rst(), //

            new RetCondition(), //
            new Exx(), //
            new JpCondition(), //
            new In(), //
            new CallCondition(), //
            new CPUIXYOpcodeRun(), //
            new SbcANum8(), //
            new Rst(), //

            // E0
            new RetCondition(), //
            new PopReg16(), //
            new JpCondition(), //
            new ExSPHL(), //
            new CallCondition(), //
            new PushReg16(), //
            new AndNum8(), //
            new Rst(),

            new RetCondition(), //
            new JpHL(), //
            new JpCondition(), //
            new ExDEHL(), //
            new CallCondition(), //
            new CPUEDOpcodeRun(), //
            new XorNum8(), //
            new Rst(), //

            // F0
            new RetCondition(), //
            new PopReg16(), //
            new JpCondition(), //
            new Di(), //
            new CallCondition(), //
            new PushReg16(), //
            new OrNum8(), //
            new Rst(), //

            new RetCondition(), //
            new LdSPHL(), //
            new JpCondition(), //
            new Ei(), //
            new CallCondition(), //
            new CPUIXYOpcodeRun(), //
            new CpNum8(), //
            new Rst(), //
    };

    /** CB opcodes (all functions taking a CPU argument, returning void). */
    static final IOpcode[] cbTab = {

            // 00
            new RlcReg(), //
            new RlcReg(), //
            new RlcReg(), //
            new RlcReg(), //
            new RlcReg(), //
            new RlcReg(), //
            new RlcReg(), //
            new RlcReg(), //

            new RrcReg(), //
            new RrcReg(), //
            new RrcReg(), //
            new RrcReg(), //
            new RrcReg(), //
            new RrcReg(), //
            new RrcReg(), //
            new RrcReg(), //

            // 10
            new RlReg(), //
            new RlReg(), //
            new RlReg(), //
            new RlReg(), //
            new RlReg(), //
            new RlReg(), //
            new RlReg(), //
            new RlReg(), //

            new RrReg(), //
            new RrReg(), //
            new RrReg(), //
            new RrReg(), //
            new RrReg(), //
            new RrReg(), //
            new RrReg(), //
            new RrReg(), //

            // 20
            new SlaReg(), //
            new SlaReg(), //
            new SlaReg(), //
            new SlaReg(), //
            new SlaReg(), //
            new SlaReg(), //
            new SlaReg(), //
            new SlaReg(), //

            new SraReg(), //
            new SraReg(), //
            new SraReg(), //
            new SraReg(), //
            new SraReg(), //
            new SraReg(), //
            new SraReg(), //
            new SraReg(), //

            // 30
            new SllReg(), //
            new SllReg(), //
            new SllReg(), //
            new SllReg(), //
            new SllReg(), //
            new SllReg(), //
            new SllReg(), //
            new SllReg(), //

            new SrlReg(), //
            new SrlReg(), //
            new SrlReg(), //
            new SrlReg(), //
            new SrlReg(), //
            new SrlReg(), //
            new SrlReg(), //
            new SrlReg(), //

            // 40
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //

            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //

            // 50
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //

            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //

            // 60
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //

            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //

            // 70
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //

            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //
            new Bit(), //

            // 80
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //

            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //

            // 90
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //

            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //

            // A0
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //

            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //

            // B0
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //

            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //
            new Res(), //

            // C0
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //

            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //

            // D0
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //

            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //

            // E0
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //

            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //

            // F0
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //

            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
            new Set(), //
    };

    /**
     * Index register cb opcodes (all functions taking a CPU argument and byte index, returning void).
     */
    static final IIndexOpcode[] icbOpcode = {

            // 00
            new RlcInd(), //
            new RlcInd(), //
            new RlcInd(), //
            new RlcInd(), //
            new RlcInd(), //
            new RlcInd(), //
            new RlcInd(), //
            new RlcInd(),

            new RrcInd(), //
            new RrcInd(), //
            new RrcInd(), //
            new RrcInd(), //
            new RrcInd(), //
            new RrcInd(), //
            new RrcInd(), //
            new RrcInd(),

            // 10
            new RlInd(), //
            new RlInd(), //
            new RlInd(), //
            new RlInd(), //
            new RlInd(), //
            new RlInd(), //
            new RlInd(), //
            new RlInd(),

            new RrInd(), //
            new RrInd(), //
            new RrInd(), //
            new RrInd(), //
            new RrInd(), //
            new RrInd(), //
            new RrInd(), //
            new RrInd(),

            // 20
            new SlaInd(), //
            new SlaInd(), //
            new SlaInd(), //
            new SlaInd(), //
            new SlaInd(), //
            new SlaInd(), //
            new SlaInd(), //
            new SlaInd(),

            new SraInd(), //
            new SraInd(), //
            new SraInd(), //
            new SraInd(), //
            new SraInd(), //
            new SraInd(), //
            new SraInd(), //
            new SraInd(),

            // 30
            new SllInd(), //
            new SllInd(), //
            new SllInd(), //
            new SllInd(), //
            new SllInd(), //
            new SllInd(), //
            new SllInd(), //
            new SllInd(),

            new SrlInd(), //
            new SrlInd(), //
            new SrlInd(), //
            new SrlInd(), //
            new SrlInd(), //
            new SrlInd(), //
            new SrlInd(), //
            new SrlInd(),

            // 40
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(),

            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(),

            // 50
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(),

            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(),

            // 60
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(),

            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(),

            // 70
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(),

            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(), //
            new BitInd(),

            // 80
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(),

            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(),

            // 90
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(),

            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(),

            // A0
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(),

            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(),

            // B0
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(),

            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(), //
            new ResInd(),

            // C0
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(),

            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(),

            // D0
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(),

            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(),

            // E0
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(),

            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(),

            // F0
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(),

            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(), //
            new SetInd(),};

    /** ED opcodes (all functions taking a CPU argument, returning void). */
    static final IOpcode[] edTab = {

            // 00
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // 10
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // 20
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // 30
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // 40
            new InRegC(), //
            new OutReg(), //
            new SbcHLReg16(), //
            new LdMem16Reg16(), //
            new Neg(), //
            new RetN(), //
            new IM0(), //
            new LdIA(),

            new InRegC(), //
            new OutReg(), //
            new AdcHLReg16(), //
            new LdReg16Mem16(), //
            new Neg(), //
            new RetI(), //
            new IM0(), //
            new LdRA(),

            // 50
            new InRegC(), //
            new OutReg(), //
            new SbcHLReg16(), //
            new LdMem16Reg16(), //
            new Neg(), //
            new RetN(), //
            new IM1(), //
            new LdAI(),

            new InRegC(), //
            new OutReg(), //
            new AdcHLReg16(), //
            new LdReg16Mem16(), //
            new Neg(), //
            new RetI(), //
            new IM2(), //
            new LdAR(),

            // 60
            new InRegC(), //
            new OutReg(), //
            new SbcHLReg16(), //
            new LdMem16Reg16(), //
            new Neg(), //
            new RetN(), //
            new IM0(), //
            new Rrd(),

            new InRegC(), //
            new OutReg(), //
            new AdcHLReg16(), //
            new LdReg16Mem16(), //
            new Neg(), //
            new RetI(), //
            new IM0(), //
            new Rld(),

            // 70
            new InRegC(), //
            new OutReg(), //
            new SbcHLReg16(), //
            new LdMem16Reg16(), //
            new Neg(), //
            new RetN(), //
            new IM1(), //
            new EdNop(),

            new InRegC(), //
            new OutReg(), //
            new AdcHLReg16(), //
            new LdReg16Mem16(), //
            new Neg(), //
            new RetI(), //
            new IM2(), //
            new EdNop(),

            // 80
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // 90
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // A0
            new Ldi(), //
            new Cpi(), //
            new Ini(), //
            new Outi(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new Ldd(), //
            new Cpd(), //
            new Ind(), //
            new Outd(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // B0
            new Ldir(), //
            new Cpir(), //
            new Inir(), //
            new Otir(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new Lddr(), //
            new Cpdr(), //
            new Indr(), //
            new Otdr(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // C0
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // D0
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // E0
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            // F0
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),

            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(), //
            new EdNop(),};

}
