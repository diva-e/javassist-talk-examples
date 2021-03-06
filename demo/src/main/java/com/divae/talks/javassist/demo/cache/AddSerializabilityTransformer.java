package com.divae.talks.javassist.demo.cache;

import javassist.*;

import java.io.IOException;
import java.io.Serializable;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class AddSerializabilityTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if (className.startsWith("com/divae/talks/javassist/demo/cache/closedsource/")) {
            try {
                ClassPool pool = new ClassPool();
                pool.insertClassPath(new LoaderClassPath(loader));
                CtClass targetClass = pool.get(className.replaceAll("/", "."));
                CtClass serializableInterface = pool.get(Serializable.class.getName());

                if (isValidClass(targetClass) && !targetClass.subtypeOf(serializableInterface)) {

                    System.out.println("Transforming " + className + " to add serializability.");

                    targetClass.addInterface(serializableInterface);
                    CtField field = new CtField(CtClass.longType, "serialVersionUID", targetClass);
                    field.setModifiers(Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
                    targetClass.addField(field, "1L");

                    return targetClass.toBytecode();
                } else {
                    return null;
                }
            } catch (NotFoundException | CannotCompileException | IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean isValidClass(CtClass ctClass) {
        return !ctClass.isInterface() && !ctClass.isAnnotation() && !ctClass.isEnum() && !ctClass.isPrimitive();
    }

}
