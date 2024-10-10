package youyihj.hotai.transformers;

import cpw.mods.modlauncher.api.ITransformer;
import org.badiff.Diff;
import org.badiff.MemoryDiffs;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;

/**
 * @author youyihj
 */
public class DiffTransformer extends HotaiTransformer<Diff> implements ITransformer<ClassNode> {

    public DiffTransformer(Map<String, Diff> transformations) {
        super(transformations);
    }

    @Override
    protected byte[] transform(byte[] src, Diff transformation, String className) {
        return MemoryDiffs.apply(src, transformation);
    }
}
