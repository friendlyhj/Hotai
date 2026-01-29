package youyihj.hotai;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.badiff.Diff;
import org.badiff.imp.MemoryDiff;
import org.badiff.io.DefaultSerialization;
import youyihj.hotai.processor.BytecodeClassProcessor;
import youyihj.hotai.processor.DiffClassProcessor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * @author youyihj
 */
public class HotaiClassProcessorProvider implements ClassProcessorProvider {
    public static final Logger LOGGER = LogManager.getLogger("Hotai");

    @Override
    public void createProcessors(Context context, Collector collector) {
        var gameDir = FMLLoader.getCurrent().getGameDir();
        LOGGER.info("Reading patching classes");
        var hotaiDir = gameDir.resolve("hotai");
        if (!Files.exists(hotaiDir)) {
            try {
                Files.createDirectories(hotaiDir);
            } catch (IOException e) {
                LOGGER.error("Failed to create hotai dir", e);
            }
        }
        Map<String, byte[]> bytecodeTransformations = new HashMap<>();
        Map<String, Diff> diffTransformations = new HashMap<>();
        Map<String, byte[]> bytecodeTransformationsBeforeMixin = new HashMap<>();
        Map<String, Diff> diffTransformationsBeforeMixin = new HashMap<>();

        var beforeMixinRoot = hotaiDir.resolve("before_mixin");

        try {
            Files.walkFileTree(hotaiDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".class")) {
                        var classBytes = Files.readAllBytes(file);
                        if (file.startsWith(beforeMixinRoot)) {
                            bytecodeTransformationsBeforeMixin.put(getClassName(file, beforeMixinRoot, ".class"), classBytes);
                        } else {
                            bytecodeTransformations.put(getClassName(file, hotaiDir, ".class"), classBytes);
                        }
                    } else if (file.toString().endsWith(".badiff")) {
                        var diff = new MemoryDiff();
                        diff.deserialize(DefaultSerialization.newInstance(), Files.newInputStream(file));
                        if (file.startsWith(beforeMixinRoot)) {
                            diffTransformationsBeforeMixin.put(getClassName(file, beforeMixinRoot, ".badiff"), diff);
                        } else {
                            diffTransformations.put(getClassName(file, hotaiDir, ".badiff"), diff);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to read hotai patches", e);
        }

        if (!bytecodeTransformationsBeforeMixin.isEmpty()) {
            collector.add(new BytecodeClassProcessor(bytecodeTransformationsBeforeMixin, true, beforeMixinRoot));
        }
        if (!diffTransformationsBeforeMixin.isEmpty()) {
            collector.add(new DiffClassProcessor(diffTransformationsBeforeMixin, true));
        }
        if (!bytecodeTransformations.isEmpty()) {
            collector.add(new BytecodeClassProcessor(bytecodeTransformations, false, hotaiDir));
        }
        if (!diffTransformations.isEmpty()) {
            collector.add(new DiffClassProcessor(diffTransformations, false));
        }
    }

    private String getClassName(Path file, Path root, String extension) {
        String fileName = root.relativize(file).toString();
        return fileName.substring(0, fileName.length() - extension.length()).replace(file.getFileSystem().getSeparator(), "/");
    }
}
