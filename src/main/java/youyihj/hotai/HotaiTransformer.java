package youyihj.hotai;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author youyihj
 */
public class HotaiTransformer implements ITransformer<ClassNode> {
    private final Map<String, byte[]> transformedClasses;

    public HotaiTransformer(Map<String, byte[]> transformedClasses) {
        this.transformedClasses = transformedClasses;
    }

    @Override
    public @NotNull ClassNode transform(ClassNode classNode, ITransformerVotingContext iTransformerVotingContext) {
        byte[] bytecode = transformedClasses.get(classNode.name);
        ClassNode result = new ClassNode();
        new ClassReader(bytecode).accept(result, 0);
        HotaiTransformationService.LOGGER.info("Patched class: {}", classNode.name);
        return result;
    }

    @Override
    public @NotNull TransformerVoteResult castVote(ITransformerVotingContext iTransformerVotingContext) {
        return TransformerVoteResult.YES;
    }

    @Override
    public @NotNull Set<Target> targets() {
        return transformedClasses.keySet().stream()
                                 .map(Target::targetClass)
                                 .collect(Collectors.toSet());
    }
}
