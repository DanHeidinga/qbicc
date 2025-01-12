package org.qbicc.graph;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.qbicc.type.ValueType;
import org.qbicc.type.definition.classfile.ClassFile;
import org.qbicc.type.definition.element.ExecutableElement;
import org.qbicc.type.definition.element.MethodElement;

/**
 * An invoke instruction which returns a value.
 */
public final class StaticInvocationValue extends AbstractValue implements MethodInvocation, Triable, OrderedNode {
    private final Node dependency;
    private final MethodElement target;
    private final ValueType type;
    private final List<Value> arguments;

    StaticInvocationValue(final Node callSite, final ExecutableElement element, final int line, final int bci, final Node dependency, final MethodElement target, final ValueType type, final List<Value> arguments) {
        super(callSite, element, line, bci);
        this.dependency = dependency;
        this.target = target;
        this.type = type;
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

    public ValueType getType() {
        return type;
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

    public <T, R> R accept(final ValueVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }

    public <T, R> R accept(final TriableVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }

    int calcHashCode() {
        return Objects.hash(dependency, target, arguments);
    }

    public boolean equals(final Object other) {
        return other instanceof StaticInvocationValue && equals((StaticInvocationValue) other);
    }

    public boolean equals(final StaticInvocationValue other) {
        return this == other || other != null
            && dependency.equals(other.dependency)
            && target.equals(other.target)
            && arguments.equals(other.arguments);
    }
}
