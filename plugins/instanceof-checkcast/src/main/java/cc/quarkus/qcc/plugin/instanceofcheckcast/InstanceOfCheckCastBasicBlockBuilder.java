package cc.quarkus.qcc.plugin.instanceofcheckcast;

import java.util.List;

import cc.quarkus.qcc.context.CompilationContext;
import cc.quarkus.qcc.graph.BasicBlockBuilder;
import cc.quarkus.qcc.graph.DelegatingBasicBlockBuilder;
import cc.quarkus.qcc.graph.literal.LiteralFactory;
import cc.quarkus.qcc.graph.Value;
import cc.quarkus.qcc.graph.literal.ZeroInitializerLiteral;
import cc.quarkus.qcc.object.Function;
import cc.quarkus.qcc.type.definition.ClassContext;
import cc.quarkus.qcc.type.definition.DefinedTypeDefinition;
import cc.quarkus.qcc.type.definition.element.MethodElement;
import cc.quarkus.qcc.type.definition.ValidatedTypeDefinition;
import cc.quarkus.qcc.type.ReferenceType;
import cc.quarkus.qcc.type.ValueType;

/**
 * A BasicBlockBuilder which replaces instanceof/checkcast operations with calls to
 * RuntimeHelper APIs.
 */
public class InstanceOfCheckCastBasicBlockBuilder extends DelegatingBasicBlockBuilder {
    private final CompilationContext ctxt;

    static final boolean PLUGIN_DISABLED = true;

    public InstanceOfCheckCastBasicBlockBuilder(final CompilationContext ctxt, final BasicBlockBuilder delegate) {
        super(delegate);
        this.ctxt = ctxt;
    }

    public Value narrow(Value value, ValueType toType) {
        if (toType instanceof ReferenceType) {
            ReferenceType refExpectedType = (ReferenceType) toType;
            ValueType actualType = value.getType();
            if (actualType instanceof ReferenceType) {
                if (((ReferenceType) actualType).instanceOf(refExpectedType)) {
                    // the reference type matches statically
                    return value;
                }
            }
        }
        return super.narrow(value, toType);
    }

    public Value instanceOf(final Value input, final ValueType expectedType) {
        // "null" instanceof <X> is always false
        if (input instanceof ZeroInitializerLiteral) {
            return ctxt.getLiteralFactory().literalOf(false);
        }
        // statically true instanceof checks are equal to x != null
        LiteralFactory lf = ctxt.getLiteralFactory();
        if (expectedType instanceof ReferenceType) {
            ReferenceType refExpectedType = (ReferenceType) expectedType;
            ValueType actualType = input.getType();
            if (actualType instanceof ReferenceType) {
                if (((ReferenceType) actualType).instanceOf(refExpectedType)) {
                    // the reference type matches statically
                    return super.isNe(input, lf.zeroInitializerLiteralOfType(actualType));
                }
            }
        }

        if (PLUGIN_DISABLED) {
            ctxt.warning(getLocation(), "instanceof not supported yet");
            return ctxt.getLiteralFactory().literalOf(true);
        }
        // This code is not yet enabled.  Committing in this state so it's available
        // and so the plugin is included in the list of plugins.


        ctxt.info("Lowering instanceof:" + expectedType.getClass());
        // Value result = super.instanceOf(input, expectedType);
        // convert InstanceOf into a new FunctionCall()
        // RuntimeHelpers.fast_instanceof(CurrentThread, Value, ValueType) {
        //  cheap checks for class depth and then probe supers[]
        //  for array cases, etc, call RuntimeHelpers.slow_instanceOf(CurrentThread, Value, ValueType)
        // and let the optimizer inline the 'fast_instanceof' call and hope the rest is removed
        // mark the slow path as @noinline
        // DelegatingBasicBlockBuilder.getLocation() to get the bci & line
        MethodElement methodElement = ctxt.getVMHelperMethod("fast_instanceof");
        ctxt.registerEntryPoint(methodElement);
        Function function = ctxt.getExactFunction(methodElement);
        List<Value> args = List.of(input, lf.literalOfType(expectedType));
        return super.callFunction(lf.literalOfSymbol(function.getName(), function.getType()), args);
    }

    // TODO: Find equivalent checkcast methods to implement here as well
}
