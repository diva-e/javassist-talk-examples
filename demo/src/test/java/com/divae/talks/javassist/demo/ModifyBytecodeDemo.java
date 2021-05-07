package com.divae.talks.javassist.demo;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ModifyBytecodeDemo {

    @Test
    public void setAndGetValues() throws Exception {

        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.get("com.divae.talks.javassist.demo.ModifyBytecodeDemo$Entity");

        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            ctMethod.instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess f) throws CannotCompileException {
                    try {
                        if (f.isReader() && f.getField().getType().equals(CtClass.intType)) {
                            f.replace("$_ = 42;");
                        }
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        Entity entity = (Entity) ctClass.toClass().getDeclaredConstructor().newInstance();
        entity.setValue(123);

        assertThat(entity.getValue(), is(42));

    }

    public static class Entity {
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

}