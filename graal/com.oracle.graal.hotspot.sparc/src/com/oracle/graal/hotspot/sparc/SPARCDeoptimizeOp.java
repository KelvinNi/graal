/*
 * Copyright (c) 2012, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.hotspot.sparc;

import static com.oracle.graal.hotspot.HotSpotHostBackend.UNCOMMON_TRAP_HANDLER;
import jdk.vm.ci.meta.AllocatableValue;
import jdk.vm.ci.meta.LIRKind;
import jdk.vm.ci.meta.PlatformKind;
import jdk.vm.ci.sparc.SPARC;

import com.oracle.graal.asm.sparc.SPARCMacroAssembler;
import com.oracle.graal.lir.LIRFrameState;
import com.oracle.graal.lir.LIRInstructionClass;
import com.oracle.graal.lir.Opcode;
import com.oracle.graal.lir.asm.CompilationResultBuilder;
import com.oracle.graal.lir.sparc.SPARCBlockEndOp;
import com.oracle.graal.lir.sparc.SPARCCall;

@Opcode("DEOPT")
final class SPARCDeoptimizeOp extends SPARCBlockEndOp {
    public static final LIRInstructionClass<SPARCDeoptimizeOp> TYPE = LIRInstructionClass.create(SPARCDeoptimizeOp.class);
    public static final SizeEstimate SIZE = SizeEstimate.create(1);
    @Temp AllocatableValue pcRegister;
    @State private LIRFrameState info;

    SPARCDeoptimizeOp(LIRFrameState info, PlatformKind wordKind) {
        super(TYPE, SIZE);
        this.info = info;
        pcRegister = SPARC.o7.asValue(LIRKind.value(wordKind));
    }

    @Override
    public void emitCode(CompilationResultBuilder crb, SPARCMacroAssembler masm) {
        // TODO the patched call address looks odd (and is invalid) compared to other runtime calls:
        // 0xffffffff749bb5fc: call 0xffffffff415a720c ; {runtime_call}
        // [Exception Handler]
        // 0xffffffff749bb604: call 0xffffffff749bb220 ; {runtime_call}
        // 0xffffffff749bb608: nop
        // [Deopt Handler Code]
        // 0xffffffff749bb60c: call 0xffffffff748da540 ; {runtime_call}
        // 0xffffffff749bb610: nop
        SPARCCall.directCall(crb, masm, crb.foreignCalls.lookupForeignCall(UNCOMMON_TRAP_HANDLER), null, info);
    }
}