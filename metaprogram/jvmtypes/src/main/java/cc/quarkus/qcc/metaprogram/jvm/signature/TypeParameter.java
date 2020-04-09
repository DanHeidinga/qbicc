package cc.quarkus.qcc.metaprogram.jvm.signature;

import cc.quarkus.qcc.metaprogram.jvm.signature.ReferenceTypeSignature;

/**
 *
 */
public interface TypeParameter {
    String getSimpleName();

    boolean hasClassBound();

    /**
     * Get the class bound for this type parameter declaration.  Top level bounds are always covariant.
     *
     * @return the covariant bound
     * @throws IllegalArgumentException if there is no bound
     */
    ReferenceTypeSignature getClassBound() throws IllegalArgumentException;

    int getInterfaceBoundCount();

    ReferenceTypeSignature getInterfaceBound(int index) throws IndexOutOfBoundsException;
}
