package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.reverseinfo.AdcANum8ReverseInfo;
import jwabbit.core.reverseinfo.AdcAReg8ReverseInfo;
import jwabbit.core.reverseinfo.AdcHLReg16ReverseInfo;
import jwabbit.core.reverseinfo.AddANum8ReverseInfo;
import jwabbit.core.reverseinfo.AddAReg8ReverseInfo;
import jwabbit.core.reverseinfo.AddHLReg16ReverseInfo;
import jwabbit.core.reverseinfo.AndNum8ReverseInfo;
import jwabbit.core.reverseinfo.AndReg8ReverseInfo;
import jwabbit.core.reverseinfo.CallConditionReverseInfo;
import jwabbit.core.reverseinfo.CallReverseInfo;
import jwabbit.core.reverseinfo.CplReverseInfo;
import jwabbit.core.reverseinfo.DaaReverseInfo;
import jwabbit.core.reverseinfo.DiReverseInfo;
import jwabbit.core.reverseinfo.DjnzReverseInfo;
import jwabbit.core.reverseinfo.EiReverseInfo;
import jwabbit.core.reverseinfo.HaltReverseInfo;
import jwabbit.core.reverseinfo.IM0ReverseInfo;
import jwabbit.core.reverseinfo.IM1ReverseInfo;
import jwabbit.core.reverseinfo.IM2ReverseInfo;
import jwabbit.core.reverseinfo.InReverseInfo;
import jwabbit.core.reverseinfo.IndReverseInfo;
import jwabbit.core.reverseinfo.IndrReverseInfo;
import jwabbit.core.reverseinfo.IniReverseInfo;
import jwabbit.core.reverseinfo.InirReverseInfo;
import jwabbit.core.reverseinfo.JpConditionReverseInfo;
import jwabbit.core.reverseinfo.JpHLReverseInfo;
import jwabbit.core.reverseinfo.JpReverseInfo;
import jwabbit.core.reverseinfo.JrConditionReverseInfo;
import jwabbit.core.reverseinfo.JrReverseInfo;
import jwabbit.core.reverseinfo.LdABCReverseInfo;
import jwabbit.core.reverseinfo.LdADEReverseInfo;
import jwabbit.core.reverseinfo.LdAIReverseInfo;
import jwabbit.core.reverseinfo.LdAMem16ReverseInfo;
import jwabbit.core.reverseinfo.LdARReverseInfo;
import jwabbit.core.reverseinfo.LdBCAReverseInfo;
import jwabbit.core.reverseinfo.LdBCNum16ReverseInfo;
import jwabbit.core.reverseinfo.LdDEAReverseInfo;
import jwabbit.core.reverseinfo.LdDENum16ReverseInfo;
import jwabbit.core.reverseinfo.LdHLNum16ReverseInfo;
import jwabbit.core.reverseinfo.LdHLfMem16ReverseInfo;
import jwabbit.core.reverseinfo.LdIAReverseInfo;
import jwabbit.core.reverseinfo.LdMem16AReverseInfo;
import jwabbit.core.reverseinfo.LdMem16HLfReverseInfo;
import jwabbit.core.reverseinfo.LdMem16Reg16ReverseInfo;
import jwabbit.core.reverseinfo.LdRAReverseInfo;
import jwabbit.core.reverseinfo.LdRNum8ReverseInfo;
import jwabbit.core.reverseinfo.LdRRReverseInfo;
import jwabbit.core.reverseinfo.LdReg16Mem16ReverseInfo;
import jwabbit.core.reverseinfo.LdSPHLReverseInfo;
import jwabbit.core.reverseinfo.LdSPNum16ReverseInfo;
import jwabbit.core.reverseinfo.LddReverseInfo;
import jwabbit.core.reverseinfo.LddrReverseInfo;
import jwabbit.core.reverseinfo.LdiReverseInfo;
import jwabbit.core.reverseinfo.LdirReverseInfo;
import jwabbit.core.reverseinfo.NopIndReverseInfo;
import jwabbit.core.reverseinfo.NopReverseInfo;
import jwabbit.core.reverseinfo.OrNum8ReverseInfo;
import jwabbit.core.reverseinfo.OrReg8ReverseInfo;
import jwabbit.core.reverseinfo.PopReg16ReverseInfo;
import jwabbit.core.reverseinfo.PushReg16ReverseInfo;
import jwabbit.core.reverseinfo.ResIndReverseInfo;
import jwabbit.core.reverseinfo.ResReverseInfo;
import jwabbit.core.reverseinfo.RetConditionReverseInfo;
import jwabbit.core.reverseinfo.RetReverseInfo;
import jwabbit.core.reverseinfo.RetiReverseInfo;
import jwabbit.core.reverseinfo.RetnReverseInfo;
import jwabbit.core.reverseinfo.RlIndReverseInfo;
import jwabbit.core.reverseinfo.RlRegReverseInfo;
import jwabbit.core.reverseinfo.RlaReverseInfo;
import jwabbit.core.reverseinfo.RlcIndReverseInfo;
import jwabbit.core.reverseinfo.RlcRegReverseInfo;
import jwabbit.core.reverseinfo.RlcaReverseInfo;
import jwabbit.core.reverseinfo.RldReverseInfo;
import jwabbit.core.reverseinfo.RrIndReverseInfo;
import jwabbit.core.reverseinfo.RrRegReverseInfo;
import jwabbit.core.reverseinfo.RraReverseInfo;
import jwabbit.core.reverseinfo.RrcIndReverseInfo;
import jwabbit.core.reverseinfo.RrcRegReverseInfo;
import jwabbit.core.reverseinfo.RrcaReverseInfo;
import jwabbit.core.reverseinfo.RrdReverseInfo;
import jwabbit.core.reverseinfo.RstReverseInfo;
import jwabbit.core.reverseinfo.SbcANum8ReverseInfo;
import jwabbit.core.reverseinfo.SbcAReg8ReverseInfo;
import jwabbit.core.reverseinfo.SbcHLReg16ReverseInfo;
import jwabbit.core.reverseinfo.SetIndReverseInfo;
import jwabbit.core.reverseinfo.SetReverseInfo;
import jwabbit.core.reverseinfo.SlaIndReverseInfo;
import jwabbit.core.reverseinfo.SlaRegReverseInfo;
import jwabbit.core.reverseinfo.SllIndReverseInfo;
import jwabbit.core.reverseinfo.SllRegReverseInfo;
import jwabbit.core.reverseinfo.SraIndReverseInfo;
import jwabbit.core.reverseinfo.SraRegReverseInfo;
import jwabbit.core.reverseinfo.SrlIndReverseInfo;
import jwabbit.core.reverseinfo.SrlRegReverseInfo;
import jwabbit.core.reverseinfo.SubANum8ReverseInfo;
import jwabbit.core.reverseinfo.SubAReg8ReverseInfo;
import jwabbit.core.reverseinfo.XorNum8ReverseInfo;
import jwabbit.core.reverseinfo.XorReg8ReverseInfo;

/**
 * Table of reverse info objects corresponding to opcodes.
 */
enum ReverseInfoTable {
    ;

    /** Reverse info objects for each opcode. */
    static final IRevOpcode[] opcodeReverseInfo = {

            // 00
            new NopReverseInfo(), //
            new LdBCNum16ReverseInfo(), //
            new LdBCAReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new LdRNum8ReverseInfo(), //
            new RlcaReverseInfo(), //

            new NopReverseInfo(), //
            new AddHLReg16ReverseInfo(), //
            new LdABCReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new LdRNum8ReverseInfo(), //
            new RrcaReverseInfo(), //

            // 10
            new DjnzReverseInfo(), //
            new LdDENum16ReverseInfo(), //
            new LdDEAReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new LdRNum8ReverseInfo(), //
            new RlaReverseInfo(), //

            new JrReverseInfo(), //
            new AddHLReg16ReverseInfo(), //
            new LdADEReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new LdRNum8ReverseInfo(), //
            new RraReverseInfo(), //

            // 20
            new JrConditionReverseInfo(), //
            new LdHLNum16ReverseInfo(), //
            new LdMem16HLfReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new LdRNum8ReverseInfo(), //
            new DaaReverseInfo(), //

            new JrConditionReverseInfo(), //
            new AddHLReg16ReverseInfo(), //
            new LdHLfMem16ReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new LdRNum8ReverseInfo(), //
            new CplReverseInfo(), //

            // 30
            new JrConditionReverseInfo(), //
            new LdSPNum16ReverseInfo(), //
            new LdMem16AReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new LdRNum8ReverseInfo(), //
            new NopReverseInfo(), //

            new JrConditionReverseInfo(), //
            new AddHLReg16ReverseInfo(), //
            new LdAMem16ReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new LdRNum8ReverseInfo(), //
            new NopReverseInfo(), //

            // 40
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //

            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //

            // 50
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //

            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //

            // 60
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //

            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //

            // 70
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new HaltReverseInfo(), //
            new LdRRReverseInfo(), //

            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //
            new LdRRReverseInfo(), //

            // 80
            new AddAReg8ReverseInfo(), //
            new AddAReg8ReverseInfo(), //
            new AddAReg8ReverseInfo(), //
            new AddAReg8ReverseInfo(), //
            new AddAReg8ReverseInfo(), //
            new AddAReg8ReverseInfo(), //
            new AddAReg8ReverseInfo(), //
            new AddAReg8ReverseInfo(), //

            new AdcAReg8ReverseInfo(), //
            new AdcAReg8ReverseInfo(), //
            new AdcAReg8ReverseInfo(), //
            new AdcAReg8ReverseInfo(), //
            new AdcAReg8ReverseInfo(), //
            new AdcAReg8ReverseInfo(), //
            new AdcAReg8ReverseInfo(), //
            new AdcAReg8ReverseInfo(), //

            // 90
            new SubAReg8ReverseInfo(), //
            new SubAReg8ReverseInfo(), //
            new SubAReg8ReverseInfo(), //
            new SubAReg8ReverseInfo(), //
            new SubAReg8ReverseInfo(), //
            new SubAReg8ReverseInfo(), //
            new SubAReg8ReverseInfo(), //
            new SubAReg8ReverseInfo(), //

            new SbcAReg8ReverseInfo(), //
            new SbcAReg8ReverseInfo(), //
            new SbcAReg8ReverseInfo(), //
            new SbcAReg8ReverseInfo(), //
            new SbcAReg8ReverseInfo(), //
            new SbcAReg8ReverseInfo(), //
            new SbcAReg8ReverseInfo(), //
            new SbcAReg8ReverseInfo(), //

            // A0
            new AndReg8ReverseInfo(), //
            new AndReg8ReverseInfo(), //
            new AndReg8ReverseInfo(), //
            new AndReg8ReverseInfo(), //
            new AndReg8ReverseInfo(), //
            new AndReg8ReverseInfo(), //
            new AndReg8ReverseInfo(), //
            new AndReg8ReverseInfo(), //

            new XorReg8ReverseInfo(), //
            new XorReg8ReverseInfo(), //
            new XorReg8ReverseInfo(), //
            new XorReg8ReverseInfo(), //
            new XorReg8ReverseInfo(), //
            new XorReg8ReverseInfo(), //
            new XorReg8ReverseInfo(), //
            new XorReg8ReverseInfo(), //

            // B0
            new OrReg8ReverseInfo(), //
            new OrReg8ReverseInfo(), //
            new OrReg8ReverseInfo(), //
            new OrReg8ReverseInfo(), //
            new OrReg8ReverseInfo(), //
            new OrReg8ReverseInfo(), //
            new OrReg8ReverseInfo(), //
            new OrReg8ReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // C0
            new RetConditionReverseInfo(), //
            new PopReg16ReverseInfo(), //
            new JpConditionReverseInfo(), //
            new JpReverseInfo(), //
            new CallConditionReverseInfo(), //
            new PushReg16ReverseInfo(), //
            new AddANum8ReverseInfo(), //
            new RstReverseInfo(), //

            new RetConditionReverseInfo(), //
            new RetReverseInfo(), //
            new JpConditionReverseInfo(), //
            new CPUCbOpcodeRunReverseInfo(), //
            new CallConditionReverseInfo(), //
            new CallReverseInfo(), //
            new AdcANum8ReverseInfo(), //
            new RstReverseInfo(), //

            // D0
            new RetConditionReverseInfo(), //
            new PopReg16ReverseInfo(), //
            new JpConditionReverseInfo(), //
            new NopReverseInfo(), //
            new CallConditionReverseInfo(), //
            new PushReg16ReverseInfo(), //
            new SubANum8ReverseInfo(), //
            new RstReverseInfo(), //

            new RetConditionReverseInfo(), //
            new NopReverseInfo(), //
            new JpConditionReverseInfo(), //
            new InReverseInfo(), //
            new CallConditionReverseInfo(), //
            new NopReverseInfo(), //
            new SbcANum8ReverseInfo(), //
            new RstReverseInfo(), //

            // E0
            new RetConditionReverseInfo(), //
            new PopReg16ReverseInfo(), //
            new JpConditionReverseInfo(), //
            new NopReverseInfo(), //
            new CallConditionReverseInfo(), //
            new PushReg16ReverseInfo(), //
            new AndNum8ReverseInfo(), //
            new RstReverseInfo(), //

            new RetConditionReverseInfo(), //
            new JpHLReverseInfo(), //
            new JpConditionReverseInfo(), //
            new NopReverseInfo(), //
            new CallConditionReverseInfo(), //
            new CPUEdOpcodeRunReverseInfo(), //
            new XorNum8ReverseInfo(), //
            new RstReverseInfo(), //

            // F0
            new RetConditionReverseInfo(), //
            new PopReg16ReverseInfo(), //
            new JpConditionReverseInfo(), //
            new DiReverseInfo(), //
            new CallConditionReverseInfo(), //
            new PushReg16ReverseInfo(), //
            new OrNum8ReverseInfo(), //
            new RstReverseInfo(), //

            new RetConditionReverseInfo(), //
            new LdSPHLReverseInfo(), //
            new JpConditionReverseInfo(), //
            new EiReverseInfo(), //
            new CallConditionReverseInfo(), //
            new NopReverseInfo(), // FD prefix
            new NopReverseInfo(), //
            new RstReverseInfo(), //
    };

    /** Reverse info objects for each CB opcode. */
    private static final IRevOpcode[] cbTabReverseInfo = {

            // 00
            new RlcRegReverseInfo(), //
            new RlcRegReverseInfo(), //
            new RlcRegReverseInfo(), //
            new RlcRegReverseInfo(), //
            new RlcRegReverseInfo(), //
            new RlcRegReverseInfo(), //
            new RlcRegReverseInfo(), //
            new RlcRegReverseInfo(), //

            new RrcRegReverseInfo(), //
            new RrcRegReverseInfo(), //
            new RrcRegReverseInfo(), //
            new RrcRegReverseInfo(), //
            new RrcRegReverseInfo(), //
            new RrcRegReverseInfo(), //
            new RrcRegReverseInfo(), //
            new RrcRegReverseInfo(), //

            // 10
            new RlRegReverseInfo(), //
            new RlRegReverseInfo(), //
            new RlRegReverseInfo(), //
            new RlRegReverseInfo(), //
            new RlRegReverseInfo(), //
            new RlRegReverseInfo(), //
            new RlRegReverseInfo(), //
            new RlRegReverseInfo(), //

            new RrRegReverseInfo(), //
            new RrRegReverseInfo(), //
            new RrRegReverseInfo(), //
            new RrRegReverseInfo(), //
            new RrRegReverseInfo(), //
            new RrRegReverseInfo(), //
            new RrRegReverseInfo(), //
            new RrRegReverseInfo(), //

            // 20
            new SlaRegReverseInfo(), //
            new SlaRegReverseInfo(), //
            new SlaRegReverseInfo(), //
            new SlaRegReverseInfo(), //
            new SlaRegReverseInfo(), //
            new SlaRegReverseInfo(), //
            new SlaRegReverseInfo(), //
            new SlaRegReverseInfo(), //

            new SraRegReverseInfo(), //
            new SraRegReverseInfo(), //
            new SraRegReverseInfo(), //
            new SraRegReverseInfo(), //
            new SraRegReverseInfo(), //
            new SraRegReverseInfo(), //
            new SraRegReverseInfo(), //
            new SraRegReverseInfo(), //

            // 30
            new SllRegReverseInfo(), //
            new SllRegReverseInfo(), //
            new SllRegReverseInfo(), //
            new SllRegReverseInfo(), //
            new SllRegReverseInfo(), //
            new SllRegReverseInfo(), //
            new SllRegReverseInfo(), //
            new SllRegReverseInfo(), //

            new SrlRegReverseInfo(), //
            new SrlRegReverseInfo(), //
            new SrlRegReverseInfo(), //
            new SrlRegReverseInfo(), //
            new SrlRegReverseInfo(), //
            new SrlRegReverseInfo(), //
            new SrlRegReverseInfo(), //
            new SrlRegReverseInfo(), //

            // 40
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // 50
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // 60
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // 70
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // 80
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //

            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //

            // 90
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //

            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //

            // A0
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //

            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //

            // B0
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //

            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //
            new ResReverseInfo(), //

            // C0
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //

            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //

            // D0
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //

            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //

            // E0
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //

            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //

            // F0
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //

            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
            new SetReverseInfo(), //
    };

    /** Reverse info objects for each index CB opcode. */
    private static final IRevIndexOpcode[] icbOpcodeReverseInfo = {

            // 00
            new RlcIndReverseInfo(), //
            new RlcIndReverseInfo(), //
            new RlcIndReverseInfo(), //
            new RlcIndReverseInfo(), //
            new RlcIndReverseInfo(), //
            new RlcIndReverseInfo(), //
            new RlcIndReverseInfo(), //
            new RlcIndReverseInfo(), //

            new RrcIndReverseInfo(), //
            new RrcIndReverseInfo(), //
            new RrcIndReverseInfo(), //
            new RrcIndReverseInfo(), //
            new RrcIndReverseInfo(), //
            new RrcIndReverseInfo(), //
            new RrcIndReverseInfo(), //
            new RrcIndReverseInfo(), //

            // 10
            new RlIndReverseInfo(), //
            new RlIndReverseInfo(), //
            new RlIndReverseInfo(), //
            new RlIndReverseInfo(), //
            new RlIndReverseInfo(), //
            new RlIndReverseInfo(), //
            new RlIndReverseInfo(), //
            new RlIndReverseInfo(), //

            new RrIndReverseInfo(), //
            new RrIndReverseInfo(), //
            new RrIndReverseInfo(), //
            new RrIndReverseInfo(), //
            new RrIndReverseInfo(), //
            new RrIndReverseInfo(), //
            new RrIndReverseInfo(), //
            new RrIndReverseInfo(), //

            // 20
            new SlaIndReverseInfo(), //
            new SlaIndReverseInfo(), //
            new SlaIndReverseInfo(), //
            new SlaIndReverseInfo(), //
            new SlaIndReverseInfo(), //
            new SlaIndReverseInfo(), //
            new SlaIndReverseInfo(), //
            new SlaIndReverseInfo(), //

            new SraIndReverseInfo(), //
            new SraIndReverseInfo(), //
            new SraIndReverseInfo(), //
            new SraIndReverseInfo(), //
            new SraIndReverseInfo(), //
            new SraIndReverseInfo(), //
            new SraIndReverseInfo(), //
            new SraIndReverseInfo(), //

            // 30
            new SllIndReverseInfo(), //
            new SllIndReverseInfo(), //
            new SllIndReverseInfo(), //
            new SllIndReverseInfo(), //
            new SllIndReverseInfo(), //
            new SllIndReverseInfo(), //
            new SllIndReverseInfo(), //
            new SllIndReverseInfo(), //

            new SrlIndReverseInfo(), //
            new SrlIndReverseInfo(), //
            new SrlIndReverseInfo(), //
            new SrlIndReverseInfo(), //
            new SrlIndReverseInfo(), //
            new SrlIndReverseInfo(), //
            new SrlIndReverseInfo(), //
            new SrlIndReverseInfo(), //

            // 40
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //

            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //

            // 50
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //

            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //

            // 60
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //

            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //

            // 70
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //

            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //
            new NopIndReverseInfo(), //

            // 80
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //

            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //

            // 90
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //

            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //

            // A0
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //

            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //

            // B0
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //

            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //
            new ResIndReverseInfo(), //

            // C0
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //

            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //

            // D0
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //

            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //

            // E0
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //

            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //

            // F0
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //

            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
            new SetIndReverseInfo(), //
    };

    /** Reverse info objects for each ED opcode. */
    private static final IRevOpcode[] edTabReverseInfo = {

            // 00
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // 10
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // 20
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // 30
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // 40
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new SbcHLReg16ReverseInfo(), //
            new LdMem16Reg16ReverseInfo(), //
            new NopReverseInfo(), //
            new RetnReverseInfo(), //
            new IM0ReverseInfo(), //
            new LdIAReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new AdcHLReg16ReverseInfo(), //
            new LdReg16Mem16ReverseInfo(), //
            new NopReverseInfo(), //
            new RetiReverseInfo(), //
            new IM0ReverseInfo(), //
            new LdRAReverseInfo(), //

            // 50
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new SbcHLReg16ReverseInfo(), //
            new LdMem16Reg16ReverseInfo(), //
            new NopReverseInfo(), //
            new RetnReverseInfo(), //
            new IM1ReverseInfo(), //
            new LdAIReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new AdcHLReg16ReverseInfo(), //
            new LdReg16Mem16ReverseInfo(), //
            new NopReverseInfo(), //
            new RetiReverseInfo(), //
            new IM2ReverseInfo(), //
            new LdARReverseInfo(), //

            // 60
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new SbcHLReg16ReverseInfo(), //
            new LdMem16Reg16ReverseInfo(), //
            new NopReverseInfo(), //
            new RetnReverseInfo(), //
            new IM0ReverseInfo(), //
            new RrdReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new AdcHLReg16ReverseInfo(), //
            new LdReg16Mem16ReverseInfo(), //
            new NopReverseInfo(), //
            new RetiReverseInfo(), //
            new IM0ReverseInfo(), //
            new RldReverseInfo(), //

            // 70
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new SbcHLReg16ReverseInfo(), //
            new LdMem16Reg16ReverseInfo(), //
            new NopReverseInfo(), //
            new RetnReverseInfo(), //
            new IM1ReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new AdcHLReg16ReverseInfo(), //
            new LdReg16Mem16ReverseInfo(), //
            new NopReverseInfo(), //
            new RetiReverseInfo(), //
            new IM2ReverseInfo(), //
            new NopReverseInfo(), //

            // 80
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // 90
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // A0
            new LdiReverseInfo(), //
            new NopReverseInfo(), //
            new IniReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new LddReverseInfo(), //
            new NopReverseInfo(), //
            new IndReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // B0
            new LdirReverseInfo(), //
            new NopReverseInfo(), //
            new InirReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new LddrReverseInfo(), //
            new NopReverseInfo(), //
            new IndrReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // C0
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // D0
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // E0
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            // F0
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //

            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
            new NopReverseInfo(), //
    };

    /**
     * The CPU_CB_opcode_run_reverse_info "opcode".
     */
    private static final class CPUCbOpcodeRunReverseInfo implements IRevOpcode {

        /**
         * Private constructor to prevent instantiation.
         */
        CPUCbOpcodeRunReverseInfo() {

            // No action
        }

        /**
         * Opcode CPU_CB_opcode_run_reverse_info.
         *
         * <p>
         * WABBITEMU SOURCE: core/core.c: "CPU_CB_opcode_run_reverse_info" function.
         */
        @Override
        public void exec(final CPU cpu) {

            final MemoryContext mem = cpu.getMemoryContext();

            if (cpu.getPrefix() == 0) {
                final int temp = mem.memRead(cpu.getPC());

                if (cbTabReverseInfo[temp] != null) {
                    cbTabReverseInfo[temp].exec(cpu);
                }
            } else {
                final byte offset = (byte) mem.memRead(cpu.getPC());
                final int temp = mem.memRead(cpu.getPC() + 1);

                if (icbOpcodeReverseInfo[temp] != null) {
                    icbOpcodeReverseInfo[temp].exec(cpu, offset);
                }
            }
        }
    }

    /**
     * The CPU_ED_opcode_run_reverse_info "opcode".
     */
    private static final class CPUEdOpcodeRunReverseInfo implements IRevOpcode {

        /**
         * Private constructor to prevent instantiation.
         */
        CPUEdOpcodeRunReverseInfo() {

            // No action
        }

        /**
         * Opcode CPU_ED_opcode_run_reverse_info.
         *
         * <p>
         * WABBITEMU SOURCE: core/core.c: "CPU_ED_opcode_run_reverse_info" function.
         */
        @Override
        public void exec(final CPU cpu) {

            if (edTabReverseInfo[cpu.getBus()] != null) {
                edTabReverseInfo[cpu.getBus()].exec(cpu);
            }
        }
    }
}
