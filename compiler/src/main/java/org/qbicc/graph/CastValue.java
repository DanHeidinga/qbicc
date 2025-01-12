package org.qbicc.graph;

import org.qbicc.type.ValueType;

/**
 *
 */
public interface CastValue extends Value {
    Value getInput();

    ValueType getType();

    default int getValueDependencyCount() {
        return 1;
    }

    default Value getValueDependency(int index) throws IndexOutOfBoundsException {
        return index == 0 ? getInput() : Util.throwIndexOutOfBounds(index);
    }
}
