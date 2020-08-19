package cc.quarkus.qcc.type.definition.element;

import cc.quarkus.qcc.graph.Type;
import cc.quarkus.qcc.type.definition.ResolutionFailedException;
import cc.quarkus.qcc.type.definition.classfile.ClassFile;

/**
 *
 */
public interface FieldElement extends AnnotatedElement, NamedElement {
    FieldElement[] NO_FIELDS = new FieldElement[0];

    Type getType() throws ResolutionFailedException;

    default boolean isVolatile() {
        return hasAllModifiersOf(ClassFile.ACC_VOLATILE);
    }

    interface TypeResolver {
        Type resolveFieldType(long argument) throws ResolutionFailedException;

        // todo: generic/annotated type
    }

    static Builder builder() {
        return new FieldElementImpl.Builder();
    }

    interface Builder extends AnnotatedElement.Builder, NamedElement.Builder {
        void setTypeResolver(TypeResolver resolver, long argument);

        FieldElement build();
    }
}