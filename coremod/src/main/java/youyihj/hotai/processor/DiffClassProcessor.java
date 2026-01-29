package youyihj.hotai.processor;

import org.badiff.Diff;
import org.badiff.MemoryDiffs;

import java.util.Map;

/**
 * @author youyihj
 */
public final class DiffClassProcessor extends HotaiClassProcessor<Diff> {
    public DiffClassProcessor(Map<String, Diff> transformations, boolean beforeMixin) {
        super(transformations, beforeMixin);
    }

    @Override
    protected byte[] transform(byte[] src, Diff transformation, String className) {
        return MemoryDiffs.apply(src, transformation);
    }

    @Override
    protected String path() {
        return "diff";
    }
}
