package cc.quarkus.qcc.graph.node;

import java.util.Collections;
import java.util.List;

import cc.quarkus.qcc.graph.type.ConcreteType;
import cc.quarkus.qcc.graph.type.ControlType;
import cc.quarkus.qcc.graph.type.ControlValue;
import cc.quarkus.qcc.graph.type.InvokeValue;
import cc.quarkus.qcc.interpret.Context;

public class NormalControlProjection extends AbstractNode<ControlType, ControlValue>{
    protected NormalControlProjection(InvokeNode in) {
        super(in, ControlType.INSTANCE);
    }

    @Override
    public InvokeNode getControl() {
        return (InvokeNode) super.getControl();
    }

    @Override
    public ControlValue getValue(Context context) {
        InvokeValue input = context.get(getControl());
        if ( input.getThrowValue() == null ) {
            return new ControlValue();
        }
        return null;
    }

    @Override
    public List<Node<?, ?>> getPredecessors() {
        return Collections.singletonList(getControl());
    }

    @Override
    public String label() {
        return "<proj> normal control";
    }
}
