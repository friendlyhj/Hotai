package youyihj.hotai;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author youyihj
 */
public class HotaiTransformationService implements ITransformationService {
    private final Map<String, byte[]> transformedClasses = new ConcurrentHashMap<>();
    public static final Logger LOGGER = LoggerFactory.getLogger("Hotai");


    @Override
    public @NotNull String name() {
        return "hotai";
    }

    @Override
    public void initialize(IEnvironment environment) {
        LOGGER.info("Reading patched classes");
        Optional<Path> gamePath = environment.getProperty(IEnvironment.Keys.GAMEDIR.get());
        gamePath.ifPresentOrElse(it -> {
            Path hotaiPath = it.resolve("hotai");
            if (!Files.exists(hotaiPath)) {
                try {
                    Files.createDirectory(hotaiPath);
                } catch (IOException e) {
                    LOGGER.error("Failed to create hotai dir", e);
                }
            }
            try {
                Files.walkFileTree(hotaiPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toString().endsWith(".class")) {
                            String fileName = hotaiPath.relativize(file).toString();
                            String className = fileName.substring(0, fileName.length() - ".class".length()).replace(file.getFileSystem().getSeparator(), "/");
                            transformedClasses.put(className, Files.readAllBytes(file));
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                LOGGER.error("Could not read class files", e);
            }
        }, () -> LOGGER.error("Could not find game path"));
    }

    @Override
    public void onLoad(IEnvironment environment, Set<String> otherServices) throws IncompatibleEnvironmentException {

    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return List.of(new HotaiTransformer(transformedClasses));
    }
}
