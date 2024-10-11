package youyihj.hotai.transformers;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import youyihj.hotai.HotaiTransformationService;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author youyihj
 */
public abstract class HotaiTransformer<T> implements ITransformer<ClassNode> {
    private final Map<String, T> transformations;

    public HotaiTransformer(Map<String, T> transformations) {
        this.transformations = transformations;
    }

    @Override
    public final ClassNode transform(ClassNode classNode, ITransformerVotingContext iTransformerVotingContext) {
        ClassNode dest = new ClassNode();
        ClassWriter src = new ClassWriter(0);
        classNode.accept(src);
        new ClassReader(transform(src.toByteArray(), transformations.get(classNode.name), classNode.name)).accept(dest, 0);
        HotaiTransformationService.LOGGER.info("Patched class: {}", classNode.name);
        return dest;
    }

    @Override
    public final @NotNull TransformerVoteResult castVote(ITransformerVotingContext iTransformerVotingContext) {
        return TransformerVoteResult.YES;
    }

    @Override
    public final @NotNull Set<Target<ClassNode>> targets() {
        return transformations.keySet().stream().map(Target::targetClass).collect(Collectors.toSet());
    }

    protected abstract byte[] transform(byte[] src, T transformation, String className);

    @Override
    public @NotNull TargetType<ClassNode> getTargetType() {
        return TargetType.CLASS;
    }
}
