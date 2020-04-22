package cc.quarkus.qcc.machine.llvm.impl;

final class FunctionDeclarationImpl extends AbstractFunction {
    FunctionDeclarationImpl(final String name) {
        super(name);
    }

    String keyWord() {
        return "declare";
    }
}