package org.qbicc.graph;

import org.qbicc.graph.literal.NullLiteral;
import org.qbicc.type.ClassObjectType;
import org.qbicc.type.ReferenceType;
import org.qbicc.type.definition.element.ExecutableElement;

/**
 * A {@code new} allocation operation.
 */
public final class New extends AbstractValue implements OrderedNode {
    private final Node dependency;
    private final ClassObjectType type;

    New(final Node callSite, final ExecutableElement element, final int line, final int bci, Node dependency, final ClassObjectType type) {
        super(callSite, element, line, bci);
        this.dependency = dependency;
        this.type = type;
    }

    public Node getDependency() {
        return dependency;
    }

    public ReferenceType getType() {
        return type.getReference();
    }

    public ClassObjectType getClassObjectType() {
        return type;
    }

    public <T, R> R accept(final ValueVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }

    int calcHashCode() {
        // every allocation is unique
        return System.identityHashCode(this);
    }

    public boolean equals(final Object other) {
        return this == other;
    }

    @Override
    public boolean isDefNe(Value other) {
        return other instanceof NullLiteral;
    }
}
