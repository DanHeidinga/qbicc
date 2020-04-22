package cc.quarkus.qcc.machine.llvm;

import java.io.BufferedWriter;
import java.io.IOException;

import cc.quarkus.qcc.machine.llvm.impl.LLVM;
import cc.quarkus.qcc.machine.llvm.op.Assignment;

/**
 *
 */
public interface Module {
    // todo: metadata goes at the end for definitions
    FunctionDefinition define(String name);

    // todo: metadata goes after `declare` for declarations
    Function declare(String name);

    Assignment assign(Value type, Value value);

    Global global(Value type);

    Global constant(Value type);

    void writeTo(BufferedWriter output) throws IOException;

    static Module newModule() {
        return LLVM.newModule();
    }
}