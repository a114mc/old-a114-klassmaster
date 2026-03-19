package me.iris.ambien.obfuscator;

import lombok.Getter;
import me.iris.ambien.obfuscator.settings.SettingsManager;
import me.iris.ambien.obfuscator.transformers.ExclusionManager;
import me.iris.ambien.obfuscator.transformers.TransformerManager;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.utilities.string.Namings;
import me.iris.ambien.obfuscator.wrappers.JarWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Ambien {

    public static final String name = "Bruhfuscator";
    public static final Ambien INSTANCE = new Ambien();

    // GLOBALS
    public static final Ambien get = INSTANCE;
    public static final Logger logger = LoggerFactory.getLogger(name);
    // why I forgot to update this
    public static final String VERSION = "26.01.17.0";
/*
    public static String VERSION;
    static {
        VERSION = Version.getVersion();
    }
*/
    public static final String CLASSIFIER = "dev";

    public final List<String> excludedClasses = new ArrayList<>();
    public final List<String> libraries = new ArrayList<>();
//    public final List<String> excludedPrefixes = new ArrayList<>();

    // MANAGERS
    public TransformerManager transformerManager;
    public static ExclusionManager exclusionManager;
    // SETTINGS
    public String inputJar,
            outputJar;
    public String naming;
    public Namings theNamingNaming;
    public boolean removeExcludeAnnotations;

    @Getter
    private JarWrapper jarWrapper;

    public void initializeTransformers() {
        // Check for new versions
        logger.info(name + " | " + VERSION + ":" + CLASSIFIER);

        // Initialize transformers
        logger.info("Initializing transformer manager...");
        transformerManager = new TransformerManager();
        logger.info("Initialized transformer manager!");
    }

    public void preTransform() throws IOException {
        // Parse input jar
        logger.info("Pre transform...");
        File f;
        try{
            f = new File(inputJar);
        } catch (NullPointerException npe) {
            logger.error("Jar path was null?");
            try {
                SettingsManager.create();
                logger.error("Default configuration was created. Edit it and run me again!");
            } catch (IOException ignored2) {
                logger.error("Failed to create default configuration file!");
            }
            System.exit(0);
            return;
        }

        jarWrapper = new JarWrapper().from(f);

        // Initialize exclusion manager
        logger.info("Initializing exclusion manager...");
        exclusionManager = new ExclusionManager(jarWrapper);

        // Import libraries
        if (!libraries.isEmpty()) {
            logger.info("Importing libraries...");
            for (final String lib : libraries) {
                if (lib.endsWith("*")) {
                    Ambien.logger.info("Found wildcard at library path, loading all .jar inside...");
                    Path dir = Paths.get(lib.substring(0, lib.length() - 1));
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.jar")) {
                        for (Path jar : stream) {
                            jarWrapper = jarWrapper.importLibrary(jar.toString());
                            logger.info("Imported library: {}", jar.getFileName());
                        }
                    } catch (IOException e) {
                        logger.error("Failed to import libraries from {}", lib, e);
                    }
                    continue;
                }

                jarWrapper = jarWrapper.importLibrary(lib);
                logger.info("Imported library: {}", lib);
            }
        } else {
            logger.info("Skipped library import phase due to library list was empty.");
        }

    }

    public void transformAndExport(final boolean experimentalTransformers) throws IOException {
        logger.info("Transforming jar...");
        final JarWrapper transformedWrapper = transform(jarWrapper, experimentalTransformers);

        if (transformedWrapper == null) {
            logger.error("JarWrapper is null, cannot export jar.");
            return;
        }

        if (removeExcludeAnnotations) {
            exclusionManager.removeAmbienAnnotations(transformedWrapper);
        }
        try {
            logger.info("Exporting jar \"" + transformedWrapper.to() +"\"");
        } catch (NullPointerException e) {
            System.err.println("transformedWrapper was null?");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private JarWrapper transform (JarWrapper wrapper, boolean experimentalTransformers) {
        for (Transformer transformer : transformerManager.getTransformers()) {
            if (!transformer.isEnabled()) continue;
            if (!experimentalTransformers && transformer.getStability().equals(Stability.EXPERIMENTAL))
                continue;
            logger.info("Executing transformer: {}", transformer.getName());
            transformer.transform(wrapper);
        }

        return wrapper;
    }
}
