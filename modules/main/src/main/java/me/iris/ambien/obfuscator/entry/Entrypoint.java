package me.iris.ambien.obfuscator.entry;

import com.beust.jcommander.JCommander;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.settings.SettingsManager;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.utilities.LicenseInformation;
import me.iris.ambien.obfuscator.utilities.string.StringUtil;

import java.io.File;

public class Entrypoint {

    /**
     * Probably error-free
     */
    public static Args ambienArgs;


    /**
     * This is the program entry point
     */
    public static void main(String[] args) {

	    //<editor-fold desc="ASCII art">
	    System.out.println(
                "a114-klassmaster"
        );
	    //</editor-fold>



        // Get current time
        final long startingTime = System.currentTimeMillis();

	    //<editor-fold desc="Argument parsing">
	    // Parse arguments
        final Args _main_ambienArgs = new Args();
        ambienArgs = _main_ambienArgs;

        final JCommander commander = JCommander.newBuilder().addObject(_main_ambienArgs).build();
        commander.parse(args);

        // Print help
        if (_main_ambienArgs.help || args.length == 0) {
            commander.usage();
            return;
        }

        // Debugging
        Ambien.logger.info("Parsed arguments.");
	    //</editor-fold>

        try {
            // Initialize transformers & check version
            Ambien.get.initializeTransformers();

            // Get transformer info
            if (_main_ambienArgs.about != null) {
                // Get the transformer
                final Transformer transformer = Ambien.get.transformerManager.getTransformer(_main_ambienArgs.about);

                // Make sure it exists
                if (transformer == null) {
                    Ambien.logger.info("No transformer found with the name \"{}\"", _main_ambienArgs.about);
                    return;
                }

	            //<editor-fold desc="About transformer">
	            Ambien.logger.info("Info for transformer \"{}\"", transformer.getName());
                Ambien.logger.info("Category: {}", transformer.getCategory().toString());
                Ambien.logger.info("Stability: {}", transformer.getStability().toString());
                Ambien.logger.info("Description: {}", transformer.getDescription());
	            //</editor-fold>
                return;
            }

            if(_main_ambienArgs.showLicInfo){
                System.out.println("Powered by open-source software");
                LicenseInformation.software_pair.forEach((String name, String license) -> {
                    System.out.println(name + ":" + license);
                });
                return;
            }

            // Create settings file if one wasn't provided or load provided settings from arg
            if (_main_ambienArgs.createConfig) {
                SettingsManager.create();
                return;
            }
            SettingsManager.load(ambienArgs.inputLocation,
                                 ambienArgs.outputLocation,
                                 new File(ambienArgs.configLocation),
                                 ambienArgs.experimentalTransformers
                                );
            // Gather information about the input jar
            Ambien.get.preTransform();

            // Transform & export jar
            try {
                Ambien.get.transformAndExport(_main_ambienArgs.experimentalTransformers);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            // Debugging
            Ambien.logger.debug("finished obfuscation in {}ms", (System.currentTimeMillis() - startingTime));
        } catch (Throwable t) {

	        //<editor-fold desc="System information stdout">
	        final String javaVersion = System.getProperty("java.version");
            final String javaVendor = System.getProperty("java.vendor");

            // print environment info
            Ambien.logger.error("Exception thrown: {}", t.getMessage());
            Ambien.logger.error("Java version: {}", javaVersion);
            Ambien.logger.error("Vendor: {}", javaVendor);
            Ambien.logger.error("Args: {}", StringUtil.build(args));
	        //</editor-fold>

            // check for basic fixes
            if (_main_ambienArgs.experimentalTransformers)
                Ambien.logger.info("This exception may have been caused by an " +
                                   "experimental transformer. (Run me without the " +
                                   "experimental arg)");
            if (!javaVersion.startsWith("1.8")) {
                Ambien.logger.info("It is recommended to use Java 8, your java version " + javaVersion + " was un-tested");
            }
            //noinspection CallToPrintStackTrace
            t.printStackTrace();
        }
    }
}
