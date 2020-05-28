package cc.quarkus.qcc.graph2;

public interface Value extends Node {
    Value[] NO_VALUES = new Value[0];

    Value ICONST_0 = iconst(0);

    static Value iconst(int operand) {
        // todo: cache
        return new IntConstantValueImpl(operand);
    }
}
