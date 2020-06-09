package cc.quarkus.qcc.type.universe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import cc.quarkus.qcc.spi.ClassFinder;
import cc.quarkus.qcc.type.definition.LazyTypeDefinition;
import cc.quarkus.qcc.type.definition.TypeDefinition;
import cc.quarkus.qcc.type.definition.TypeDefinitionNode;
import cc.quarkus.qcc.type.definition.UnresolvableClassDefinition;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;

public class Universe {

    public static final int ASM_VERSION = Opcodes.ASM7;

    private static final AtomicReference<Universe> INSTANCE = new AtomicReference<>();

    public static Universe instance() {
        return INSTANCE.get();
    }

    public Universe(ClassFinder classFinder) {
        this(classFinder, ForkJoinPool.commonPool());
    }

    public Universe(ClassFinder classFinder, ForkJoinPool pool) {
        this.classFinder = classFinder;
        this.pool = pool;
        INSTANCE.set(this);
    }

    public TypeDefinition findClass(String name) {
        return findClass(name, false);
    }

    public TypeDefinition findClass(String name, boolean resolve) {
        TypeDefinition typeDefinition = this.objectTypes.computeIfAbsent(name, (k) -> new LazyTypeDefinition(this, name));
        if (resolve && typeDefinition instanceof LazyTypeDefinition) {
            ((LazyTypeDefinition) typeDefinition).getDelegate();
        }
        return typeDefinition;
    }

    public TypeDefinition defineClass(String name, ByteBuffer buffer) {
        ClassReader reader = null;
        try {
            reader = new ClassReader(new ByteBufferInputStream(buffer));
        } catch (IOException e) {
            return new UnresolvableClassDefinition(name);
        }
        TypeDefinitionNode node = new TypeDefinitionNode(this);
        reader.accept(node, 0);
        return node;
    }

    public void await(long timeout, TimeUnit unit) {
        this.pool.awaitQuiescence(timeout, unit);
    }

    public ClassFinder getClassFinder() {
        return this.classFinder;
    }

    public ForkJoinPool getPool() {
        return this.pool;
    }

    private final ClassFinder classFinder;

    private final ForkJoinPool pool;

    private final ConcurrentHashMap<String, TypeDefinition> objectTypes = new ConcurrentHashMap<>();

}
