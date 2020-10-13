package cc.quarkus.qcc.graph;

/**
 * A field read value.
 */
public interface FieldRead extends Value, FieldOperation {
    default Type getType() {
        return getFieldElement().getType();
    }
}