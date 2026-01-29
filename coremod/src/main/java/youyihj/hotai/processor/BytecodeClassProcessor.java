package youyihj.hotai.processor;

import org.badiff.MemoryDiffs;
import org.badiff.io.DefaultSerialization;
import youyihj.hotai.HotaiClassProcessorProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author youyihj
 */
public final class BytecodeClassProcessor extends HotaiClassProcessor<byte[]> {
    private final Path hotaiPath;

    public BytecodeClassProcessor(Map<String, byte[]> transformations, boolean beforeMixin, Path hotaiPath) {
        super(transformations, beforeMixin);
        this.hotaiPath = hotaiPath;
    }

    @Override
    protected byte[] transform(byte[] classBytes, byte[] transformation, String className) {
        replaceToDiff(classBytes, transformation, className);
        return transformation;
    }

    @Override
    protected String path() {
        return "bytecode";
    }

    private void replaceToDiff(byte[] src, byte[] transformation, String className) {
        var diff = MemoryDiffs.diff(src, transformation);
        var diffPath = hotaiPath.resolve(className.replace("/", hotaiPath.getFileSystem().getSeparator()).concat(".badiff"));
        var classPath = hotaiPath.resolve(className.replace("/", hotaiPath.getFileSystem().getSeparator()).concat(".class"));
        try {
            diff.serialize(DefaultSerialization.newInstance(), Files.newOutputStream(diffPath));
            Files.deleteIfExists(classPath);
        } catch (IOException e) {
            HotaiClassProcessorProvider.LOGGER.error("Failed to replace to diff for {}", className);
        }
    }
}
