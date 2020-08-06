package cc.quarkus.qcc.interpreter;

import cc.quarkus.qcc.context.Context;
import cc.quarkus.qcc.type.definition.DefinedTypeDefinition;
import cc.quarkus.qcc.type.definition.Dictionary;
import io.smallrye.common.function.ExceptionConsumer;
import org.fest.assertions.core.Condition;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;

import static cc.quarkus.qcc.TestUtil.defineInitialClass;
import static cc.quarkus.qcc.TestUtil.initialize;
import static cc.quarkus.qcc.interpreter.CodegenUtils.p;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PrototypeTest {
    @Test
    public void testPrototype() throws Exception {
        withRootDictionary((dictionary) -> {
            String classWithFields = p(ClassWithFields.class);

            defineInitialClass(dictionary, classWithFields);

            DefinedTypeDefinition myClass = dictionary.findClass(classWithFields);

            Prototype proto = PrototypeGenerator.getPrototype(myClass);

            Class protoClass = proto.getPrototypeClass();

            Arrays.stream(ClassWithFields.class.getDeclaredFields()).forEach((field) -> {
                Field protoField = getDeclaredField(protoClass, field.getName());

                assertThat(protoField).isNotNull();
                assertThat(protoField.getType()).is(sizeEquivalent(field.getType()));
                assertThat(protoField.getModifiers()).isEqualTo(field.getModifiers());
            });

            FieldContainer protoObject = proto.construct();

            int instanceReferenceIndex = protoObject.getFieldIndex("instanceReference");
            assertEquals(8, instanceReferenceIndex);
            assertNull(protoObject.getObjectVolatile(instanceReferenceIndex));
            protoObject.setObjectVolatile(instanceReferenceIndex, "foo");
            assertEquals(protoObject.getObjectVolatile(instanceReferenceIndex), "foo");
        });
    }

    private static void withRootDictionary(ExceptionConsumer<Dictionary, Exception> runner) throws Exception {
        Context context = new Context(false);

        context.run(() -> {
            Dictionary dictionary = new Dictionary();
            Dictionary.setRootDictionary(dictionary);

            initialize(dictionary);

            runner.accept(dictionary);
        });
    }

    private static Condition<Class<?>> sizeEquivalent(Class<?> expected) {
        return new Condition<>() {
            @Override
            public boolean matches(Class<?> actual) {
                if (expected.isPrimitive()) {
                    if (expected == boolean.class) {
                        return actual == boolean.class;
                    }
                    if (expected == char.class) {
                        return actual == short.class;
                    }
                    return actual == expected;
                } else {
                    return actual == Object.class;
                }
            }
        };
    }

    private static <T extends Throwable> Field getDeclaredField(Class cls, String name) throws T {
        try {
            return cls.getDeclaredField(name);
        } catch (Throwable t) {
            throwsUnchecked(t);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwsUnchecked(final Throwable e) throws T {
        throw (T) e;
    }
}
