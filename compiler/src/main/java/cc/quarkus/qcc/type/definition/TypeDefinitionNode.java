package cc.quarkus.qcc.type.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cc.quarkus.qcc.graph.ClassType;
import cc.quarkus.qcc.graph.Type;
import cc.quarkus.qcc.type.ObjectReference;
import cc.quarkus.qcc.type.descriptor.MethodDescriptor;
import cc.quarkus.qcc.type.descriptor.MethodDescriptorParser;
import cc.quarkus.qcc.type.universe.Universe;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class TypeDefinitionNode extends ClassNode implements TypeDefinition {

    volatile ClassType cachedType;

    public TypeDefinitionNode(Universe universe) {
        super(Universe.ASM_VERSION);
        this.universe = universe;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        // eagerly resolver super
        if ( superName != null ) {
            this.universe.findClass(superName, false);
        }
        // eagerly resolver interfaces
        if (interfaces != null ) {
            for (String each : interfaces) {
                this.universe.findClass(each, false);
            }
        }
    }

    @Override
    public MethodDefinitionNode<?> visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodDescriptorParser parser = new MethodDescriptorParser(getUniverse(), this, name, descriptor, (access & Opcodes.ACC_STATIC) != 0);
        MethodDescriptor methodDescriptor = parser.parseMethodDescriptor();
        MethodDefinitionNode<?> visitor = new MethodDefinitionNode<>(this, access, name, methodDescriptor, signature, exceptions);
        this.methods.add(visitor);
        return visitor;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        FieldDefinitionNode<?> visitor = new FieldDefinitionNode<>(this, access, name, descriptor, signature, value);
        this.fields.add(visitor);
        return visitor;
    }

    @Override
    public int getAccess() {
        return this.access;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public TypeDefinition getSuperclass() {
        if (this.superName == null) {
            return null;
        }
        return this.universe.findClass(this.superName);
    }

    @Override
    public List<TypeDefinition> getInterfaces() {
        return this.interfaces.stream()
                .map(this.universe::findClass)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAssignableFrom(TypeDefinition other) {
        if ( other == null ) {
            return false;
        }

        if ( getName().equals(other.getName())) {
            return true;
        }

        if ( isAssignableFrom( other.getSuperclass() ) ) {
            return true;
        }

        for (TypeDefinition each : other.getInterfaces()) {
            if ( isAssignableFrom( each ) ) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Set<MethodDefinition<?>> getMethods() {
        return this.methods.stream()
                .map(e -> (MethodDefinition<?>) e)
                .collect(Collectors.toSet());
    }

    @Override
    public MethodDefinition<?> findMethod(String name, String desc) {
        for (MethodNode each : this.methods) {
            if ( each.name.equals(name) && each.desc.equals(desc)) {
                return (MethodDefinition<?>) each;
            }
        }
        if ( getSuperclass() == null ) {
            throw new RuntimeException("Unresolved method " + name + desc);
        }
        return getSuperclass().findMethod(name, desc);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> MethodDefinition<V> findMethod(MethodDescriptor methodDescriptor) {
        return (MethodDefinition<V>) findMethod(methodDescriptor.getName(), methodDescriptor.getDescriptor());
    }

    public MethodDefinition<?> findMethod(String name, List<Object> actualParameters) {
        List<MethodDefinition<?>> candidates = new ArrayList<>();
        for (MethodNode each : this.methods) {
            MethodDefinition<?> method = (MethodDefinition<?>) each;
            if ( ! method.getName().equals(name)) {
                continue;
            }
            if ( method.getParamTypes().size() != actualParameters.size() ) {
                continue;
            }
            candidates.add(method);
        }

        if ( candidates.isEmpty() ) {
            throw new RuntimeException("Unresolved method " + name + " " + actualParameters);
        }

        // TODO check arg types and winnow down to exact best match.

        if ( candidates.size() == 1 ) {
            return candidates.get(0);
        }
        throw new RuntimeException("Unresolved method " + name + " " + actualParameters);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> FieldDefinition<V> findField(String name) {
        for (FieldNode field : this.fields) {
            if ( field.name.equals(name)) {
                return (FieldDefinition<V>) field;
            }
        }

        throw new RuntimeException("Unresolved field " + name );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> V getStatic(FieldDefinition<V> field) {
        return (V) ((FieldDefinitionNode<V>)field).value;
    }

    @Override
    public <V> V getField(FieldDefinition<V> field, ObjectReference objRef) {
        return objRef.getFieldValue(field);
    }

    @Override
    public <V> void putField(FieldDefinition<V> field, ObjectReference objRef, V val) {
        objRef.setFieldValue(field, val);
    }

    @Override
    public ClassType getType() {
        ClassType cachedType = this.cachedType;
        if (cachedType == null) {
            synchronized (this) {
                cachedType = this.cachedType;
                if (cachedType == null) {
                    this.cachedType = cachedType = Type.classNamed(name);
                }
            }
        }
        return cachedType;
    }

    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof TypeDefinition ) {
            return ((TypeDefinition) obj).getName().equals(this.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Universe getUniverse() {
        return this.universe;
    }

    private final Universe universe;
}
