package cc.quarkus.qcc.type.definition;

import cc.quarkus.qcc.graph.ClassType;
import cc.quarkus.qcc.type.definition.element.ConstructorElement;
import cc.quarkus.qcc.type.definition.element.FieldElement;
import cc.quarkus.qcc.type.definition.element.InitializerElement;
import cc.quarkus.qcc.type.definition.element.MethodElement;

/**
 *
 */
public abstract class DelegatingVerifiedTypeDefinition extends DelegatingDefinedTypeDefinition implements VerifiedTypeDefinition {
    protected DelegatingVerifiedTypeDefinition() {}

    protected abstract VerifiedTypeDefinition getDelegate();

    public ClassType getClassType() {
        return getDelegate().getClassType();
    }

    public VerifiedTypeDefinition getSuperClass() {
        return getDelegate().getSuperClass();
    }

    public VerifiedTypeDefinition getInterface(final int index) throws IndexOutOfBoundsException {
        return getDelegate().getInterface(index);
    }

    public VerifiedTypeDefinition verify() {
        return this;
    }

    public ResolvedTypeDefinition resolve() throws ResolutionFailedException {
        return getDelegate().resolve();
    }

    public FieldElement getField(final int index) {
        return getDelegate().getField(index);
    }

    public MethodElement getMethod(final int index) {
        return getDelegate().getMethod(index);
    }

    public ConstructorElement getConstructor(final int index) {
        return getDelegate().getConstructor(index);
    }

    public InitializerElement getInitializer() {
        return getDelegate().getInitializer();
    }
}