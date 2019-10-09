package com.iwillow.asm;


import com.iwilliow.annotation.Trace;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

public class TraceMethodVisitor extends AdviceAdapter {
    private boolean inject = false;
    private String methodName;

    protected TraceMethodVisitor(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
        methodName = name;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (Type.getDescriptor(Trace.class).equals(desc)) {
            inject = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    protected void onMethodEnter() {
        if (inject) {
            log("method " + methodName + " enter");
            // TimeCache.addStartTime("xxxx", System.currentTimeMillis());
            mv.visitLdcInsn(methodName);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/iwilliow/app/android/plugin/TimeCache", "addStartTime", "(Ljava/lang/String;J)V", false);
        }
    }


    @Override
    protected void onMethodExit(int opcode) {
        if (inject) {
            // TimeCache.addEndTime("xxxx", System.currentTimeMillis());
            mv.visitLdcInsn(methodName);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/iwilliow/app/android/plugin/TimeCache", "addEndTime", "(Ljava/lang/String;J)V", false);

            // TimeCache.startCost("xxxx");
            mv.visitLdcInsn(methodName);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "com/iwilliow/app/android/plugin/TimeCache", "startCost", "(Ljava/lang/String;)V", false);
            log("method " + methodName + " exit");
        }
    }

    private static void log(String msg) {
        System.out.println(msg);
    }
}
