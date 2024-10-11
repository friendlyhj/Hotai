package youyihj.hotai.transformers;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.TargetType;
import org.badiff.MemoryDiffs;
import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import youyihj.hotai.HotaiTransformationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author youyihj
 */
public class BytecodeTransformer extends HotaiTransformer<byte[]> implements ITransformer<ClassNode> {
    private final Path hotaiPath;

    public BytecodeTransformer(Path hotaiPath, Map<String, byte[]> transformations) {
        super(transformations);
        this.hotaiPath = hotaiPath;
    }

    @Override
    protected byte[] transform(byte[] src, byte[] transformation, String className) {
        replaceToDiff(src, transformation, className);
        return transformation;
    }

    private void replaceToDiff(byte[] src, byte[] transformation, String className) {
        MemoryDiff diff = MemoryDiffs.diff(src, transformation);
        Path diffPath = hotaiPath.resolve(className.replace("/", hotaiPath.getFileSystem().getSeparator()).concat(".badiff"));
        Path classPath = hotaiPath.resolve(className.replace("/", hotaiPath.getFileSystem().getSeparator()).concat(".class"));
        try {
            diff.serialize(DefaultSerialization.newInstance(), Files.newOutputStream(diffPath));
            Files.deleteIfExists(classPath);
        } catch (IOException e) {
            HotaiTransformationService.LOGGER.error("Failed to replace to diff for {}", className);
        }
    }
}
