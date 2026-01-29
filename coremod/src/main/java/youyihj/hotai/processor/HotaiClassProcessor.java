package youyihj.hotai.processor;

import net.neoforged.neoforgespi.transformation.ClassProcessor;
import net.neoforged.neoforgespi.transformation.ClassProcessorIds;
import net.neoforged.neoforgespi.transformation.ProcessorName;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import youyihj.hotai.HotaiClassProcessorProvider;

import java.util.Map;
import java.util.Set;

/**
 * @author youyihj
 */
public abstract sealed class HotaiClassProcessor<T> implements ClassProcessor permits DiffClassProcessor, BytecodeClassProcessor {
    private final Map<String, T> transformations;
    private final boolean beforeMixin;

    public HotaiClassProcessor(Map<String, T> transformations, boolean beforeMixin) {
        this.transformations = transformations;
        this.beforeMixin = beforeMixin;
    }

    @Override
    public Set<ProcessorName> runsBefore() {
        return beforeMixin ? Set.of(ClassProcessorIds.MIXIN) : Set.of();
    }

    @Override
    public Set<ProcessorName> runsAfter() {
        return beforeMixin ? Set.of(ClassProcessorIds.COMPUTING_FRAMES) : Set.of(ClassProcessorIds.MIXIN);
    }

    @Override
    public boolean handlesClass(SelectionContext context) {
        return transformations.containsKey(context.type().getInternalName());
    }

    @Override
    public ComputeFlags processClass(TransformationContext context) {
        HotaiClassProcessorProvider.LOGGER.info("Patching class: {}", context.type().getInternalName());

        var srcNode = context.node();
        var src = new ClassWriter(0);
        srcNode.accept(src);
        var transformed = transform(src.toByteArray(), transformations.get(srcNode.name), srcNode.name);
        var classReader = new ClassReader(transformed);
        var destNode = new ClassNode();
        classReader.accept(destNode, 0);

        srcNode.version = destNode.version;
        srcNode.access = destNode.access;
        srcNode.name = destNode.name;
        srcNode.signature = destNode.signature;
        srcNode.superName = destNode.superName;
        srcNode.interfaces = destNode.interfaces;
        srcNode.sourceFile = srcNode.sourceFile + "-hotai_modified";
        srcNode.sourceDebug = destNode.sourceDebug;
        srcNode.module = destNode.module;
        srcNode.outerClass = destNode.outerClass;
        srcNode.outerMethod = destNode.outerMethod;
        srcNode.outerMethodDesc = destNode.outerMethodDesc;
        srcNode.visibleAnnotations = destNode.visibleAnnotations;
        srcNode.invisibleAnnotations = destNode.invisibleAnnotations;
        srcNode.visibleTypeAnnotations = destNode.visibleTypeAnnotations;
        srcNode.invisibleTypeAnnotations = destNode.invisibleTypeAnnotations;
        srcNode.attrs = destNode.attrs;
        srcNode.innerClasses = destNode.innerClasses;
        srcNode.nestHostClass = destNode.nestHostClass;
        srcNode.nestMembers = destNode.nestMembers;
        srcNode.permittedSubclasses = destNode.permittedSubclasses;
        srcNode.recordComponents = destNode.recordComponents;
        srcNode.fields = destNode.fields;
        srcNode.methods = destNode.methods;

        return ComputeFlags.SIMPLE_REWRITE;
    }

    @Override
    public ProcessorName name() {
        return new ProcessorName("hotai", path() + (beforeMixin ? "_before_mixin" : ""));
    }

    protected abstract byte[] transform(byte[] src, T transformation, String className);

    protected abstract String path();
}
