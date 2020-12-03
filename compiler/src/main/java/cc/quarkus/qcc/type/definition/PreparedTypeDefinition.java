package cc.quarkus.qcc.type.definition;

/**
 *
 */
public interface PreparedTypeDefinition extends ResolvedTypeDefinition {
    default PreparedTypeDefinition validate() {
        return this;
    }

    default PreparedTypeDefinition resolve() {
        return this;
    }

    default PreparedTypeDefinition prepare() {
        return this;
    }

    PreparedTypeDefinition getSuperClass();

    PreparedTypeDefinition getInterface(int index) throws IndexOutOfBoundsException;

    FieldContainer getStaticFields();

    InitializedTypeDefinition initialize() throws InitializationFailedException;
}