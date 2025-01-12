package org.qbicc.interpreter.impl;

import static org.qbicc.interpreter.impl.CodegenUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.qbicc.type.BooleanType;
import org.qbicc.type.ClassObjectType;
import org.qbicc.type.FloatType;
import org.qbicc.type.IntegerType;
import org.qbicc.type.ObjectType;
import org.qbicc.type.PhysicalObjectType;
import org.qbicc.type.ReferenceType;
import org.qbicc.type.Type;
import org.qbicc.type.WordType;
import org.qbicc.type.definition.DefinedTypeDefinition;
import org.qbicc.type.definition.FieldContainer;
import org.qbicc.type.definition.LoadedTypeDefinition;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class PrototypeGenerator {
    private static Map<DefinedTypeDefinition, Prototype> prototypes = new HashMap<>();
    private static ClassDefiningClassLoader cdcl = new ClassDefiningClassLoader();

    public static Prototype getPrototype(DefinedTypeDefinition definition) {
        return prototypes.computeIfAbsent(definition, PrototypeGenerator::generate);
    }

    private static Prototype generate(DefinedTypeDefinition defined) {
        LoadedTypeDefinition verified = defined.load();
        ObjectType classType = verified.getType();
        String className = verified.getInternalName();

        // prepend qbicc so they're isolated from containing VM
        className = "qbicc/" + className;

        ClassObjectType superType = classType instanceof PhysicalObjectType ? ((PhysicalObjectType) classType).getSuperClassType() : null;
        String superName;

        if (superType == null) {
            superName = "java/lang/Object";
        } else {
            Prototype superProto = generate(verified.getSuperClass());
            superName = superProto.getClassName();
        }

        ClassWriter proto = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        proto.visit(Opcodes.V9, verified.getModifiers(), p(className), null, p(superName), arrayOf(p(FieldContainer.class)));

        verified.eachField(
                (field) -> proto.visitField(
                        field.getModifiers(),
                        field.getName(),
                        ci(javaTypeFromFieldType(field.getType())),
                        null, null));

        proto.visitEnd();

        byte[] bytecode = proto.toByteArray();

        return new PrototypeImpl(className, bytecode, cdcl);
    }

    private static class ClassDefiningClassLoader extends ClassLoader {
        public Class<?> defineAndResolveClass(String name, byte[] b, int off, int len) {
            Class<?> cls = super.defineClass(name, b, off, len);
            resolveClass(cls);
            return cls;
        }
    }

    public static class PrototypeImpl implements Prototype {
        private final String className;
        private final byte[] bytecode;
        private final ClassDefiningClassLoader cdcl;
        private final Class<? extends FieldContainer>  cls;

        PrototypeImpl(String className, byte[] bytecode, ClassDefiningClassLoader cdcl) {
            this.className = className;
            this.bytecode = bytecode;
            this.cdcl = cdcl;

            this.cls = initializeClass(cdcl, className, bytecode);
        }

        private static Class<? extends FieldContainer> initializeClass(ClassDefiningClassLoader cdcl, String name, byte[] bytecode) {
            Class<?> cls = cdcl.defineAndResolveClass(name.replaceAll("/", "."), bytecode, 0, bytecode.length);

            return (Class<FieldContainer>) cls;
        }

        @Override
        public byte[] getBytecode() {
            return bytecode;
        }

        @Override
        public String getClassName() {
            return className;
        }

        @Override
        public Class<? extends FieldContainer> getPrototypeClass() {
            return cls;
        }

        @Override
        public FieldContainer construct() {
            try {
                return cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("BUG: could not instantiate", e);
            }
        }
    }

    private static Class javaTypeFromFieldType(Type type) {
        Class fieldType;

        if (type instanceof ReferenceType) {
            // reference type
            fieldType = Object.class;
        } else if (type instanceof WordType) {
            // primitive type
            fieldType = wordTypeToClass((WordType) type);
        } else {
            throw new IllegalStateException("unhandled type: " + type);
        }

        return fieldType;
    }

    private static Class wordTypeToClass(WordType wordType) {
        if (wordType instanceof BooleanType) {
            return boolean.class;
        } else if (wordType instanceof IntegerType) {
            switch ((int) wordType.getSize()) {
                case 1:
                    return byte.class;
                case 2:
                    return short.class;
                case 4:
                    return int.class;
                case 8:
                    return long.class;
            }
        } else if (wordType instanceof FloatType) {
            switch ((int) wordType.getSize()) {
                case 4:
                    return float.class;
                case 8:
                    return double.class;
            }
        }
        throw new IllegalArgumentException("unknown word type: " + wordType);
    }
}
