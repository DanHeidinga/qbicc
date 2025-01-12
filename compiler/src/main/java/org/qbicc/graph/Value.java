package org.qbicc.graph;

import org.qbicc.type.FloatType;
import org.qbicc.type.ValueType;

public interface Value extends Node {

    ValueType getType();

    <T, R> R accept(ValueVisitor<T, R> visitor, T param);

    // static

    Value[] NO_VALUES = new Value[0];

    default boolean isDefEq(Value other) {
        return equals(other) && isDefNotNaN() && other.isDefNotNaN();
    }

    default boolean isDefNe(Value other) {
        return false;
    }

    default boolean isDefLt(Value other) {
        return false;
    }

    default boolean isDefGt(Value other) {
        return false;
    }

    default boolean isDefLe(Value other) {
        return equals(other) && isDefNotNaN() && other.isDefNotNaN();
    }

    default boolean isDefGe(Value other) {
        return equals(other) && isDefNotNaN() && other.isDefNotNaN();
    }

    default boolean isDefNaN() {
        return false;
    }

    default boolean isDefNotNaN() {
        // only floats can be NaN
        return ! (getType() instanceof FloatType);
    }
}
