package com.iwillow.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TraceClassVisitor extends ClassVisitor {

    public TraceClassVisitor(int api) {
        super(api);
    }

    public TraceClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        return new TraceMethodVisitor(Opcodes.ASM5, mv, access, name, desc);
    }
}
