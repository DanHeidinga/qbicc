package cc.quarkus.qcc.object;

import cc.quarkus.qcc.graph.literal.SymbolLiteral;
import cc.quarkus.qcc.type.FunctionType;
import cc.quarkus.qcc.type.definition.MethodBody;
import cc.quarkus.qcc.type.definition.element.BasicElement;

/**
 * A function definition.
 */
public final class Function extends SectionObject {
    private volatile MethodBody body;

    Function(final BasicElement originalElement, final String name, final SymbolLiteral literal) {
        super(originalElement, name, literal);
    }

    public FunctionType getType() {
        return (FunctionType) super.getType();
    }

    public MethodBody getBody() {
        return body;
    }

    public void replaceBody(final MethodBody body) {
        this.body = body;
    }
}