package org.qbicc.graph;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.qbicc.type.definition.classfile.ClassFile;
import org.qbicc.type.definition.element.ExecutableElement;
import org.qbicc.type.definition.element.MethodElement;

/**
 * An invocation on an object instance.
 */
public final class InstanceInvocation extends AbstractNode implements InstanceOperation, MethodInvocation, DispatchInvocation, Triable, Action, OrderedNode {
    private final Node dependency;
    private final DispatchInvocation.Kind kind;
    private final Value instance;
    private final MethodElement target;
    private final List<Value> arguments;

    InstanceInvocation(final Node callSite, final ExecutableElement element, final int line, final int bci, final Node dependency, final Kind kind, final Value instance, final MethodElement target, final List<Value> arguments) {
        super(callSite, element, line, bci);
        this.dependency = dependency;
        this.kind = kind;
        this.instance = instance;
        this.target = target;
        this.arguments = arguments;
    }

    public MethodElement getInvocationTarget() {
        return target;
    }

    public int getArgumentCount() {
        return arguments.size();
    }

    public Value getArgument(final int index) {
        return arguments.get(index);
    }

    public List<Value> getArguments() {
        return arguments;
    }

    public DispatchInvocation.Kind getKind() {
        return kind;
    }

    public Value getInstance() {
        return instance;
    }

    @Override
    public Node getDependency() {
        if (hasDependency()) {
            return dependency;
        }
        throw new NoSuchElementException();
    }

    public boolean hasDependency() {
        return target.hasNoModifiersOf(ClassFile.I_ACC_NO_SIDE_EFFECTS);
    }

    public int getValueDependencyCount() {
        return MethodInvocation.super.getValueDependencyCount() + 1;
    }

    public Value getValueDependency(int index) throws IndexOutOfBoundsException {
        return index == 0 ? getInstance() : MethodInvocation.super.getValueDependency(index - 1);
    }

    public <T, R> R accept(final ActionVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }

    public <T, R> R accept(final TriableVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }

    int calcHashCode() {
        return Objects.hash(dependency, kind, instance, target, arguments);
    }

    public boolean equals(final Object other) {
        return other instanceof InstanceInvocation && equals((InstanceInvocation) other);
    }

    public boolean equals(final InstanceInvocation other) {
        return this == other || other != null
            && dependency.equals(other.dependency)
            && kind.equals(other.kind)
            && instance.equals(other.instance)
            && target.equals(other.target)
            && arguments.equals(other.arguments);
    }
}
