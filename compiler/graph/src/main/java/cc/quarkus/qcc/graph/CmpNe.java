package cc.quarkus.qcc.graph;

/**
 *
 */
public final class CmpNe extends AbstractCmp implements CommutativeBinaryValue {
    CmpNe(final Value v1, final Value v2) {
        super(v1, v2);
    }

    public <T, R> R accept(final ValueVisitor<T, R> visitor, final T param) {
        return visitor.visit(param, this);
    }
}