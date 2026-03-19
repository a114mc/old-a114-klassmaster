package me.iris.ambien.obfuscator.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.iris.ambien.obfuscator.Ambien;
import me.iris.ambien.obfuscator.entry.Entrypoint;
import me.iris.ambien.obfuscator.settings.data.Setting;
import me.iris.ambien.obfuscator.settings.data.implementations.BooleanSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringListSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.NumberSetting;
import me.iris.ambien.obfuscator.settings.data.implementations.StringSetting;
import me.iris.ambien.obfuscator.transformers.data.Stability;
import me.iris.ambien.obfuscator.transformers.data.Transformer;
import me.iris.ambien.obfuscator.utilities.string.Namings;
import me.iris.ambien.obfuscator.utilities.string.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsManager {
	public static void create () throws IOException {
        // Create file to write to
        File outFile = new File("settings.json");
        if (outFile.exists()) {
            outFile = new File("settings-" + StringUtil.randomStringIS(3,"abcdefghijlkmnopqrstuvwxyzABCDEFGHIJLKMNOPQRSTUVWXYZ") + ".json");
        }
        createMethodForAPIS(outFile, "./in.jar", "out.jar", new JsonArray(), new JsonArray(), true);
	}

    public static void createMethodForAPIS(
            File outFile,
            String input,
            String output,
            JsonArray libraries, JsonArray exclusionPrefix, boolean useEnabledByDefault
    ) throws IOException {



        // Global object everything will be stored in
        final JsonObject obj = new JsonObject();

        // Add current obfuscator version to json
        obj.addProperty("version", Ambien.VERSION);

        // Add string properties for input jar & output path
        obj.addProperty("input", input);

        // more stuffs coming soon
        // TODO: change it to directory
        obj.addProperty("output", output);
        obj.add("libraries", libraries);
        obj.add("exclusion-prefix", exclusionPrefix);
        obj.addProperty("naming", "iIl");
        obj.addProperty("remove-annotations", true);

        // Array all transformers will be in
        final JsonArray transformersArr = new JsonArray();

        // Add transformer stuff to transformers array
        for (Transformer transformer : Ambien.get.transformerManager.getTransformers()) {
            final JsonObject transformerObj = new JsonObject();
            transformerObj.addProperty("name", transformer.getName());
            // Cache description
            String iris = transformer.getDescription();
            // Add when it was provided
            if(!iris.equals("No description provided.")){
                transformerObj.addProperty("description", iris);
            }

            for (Setting<?> setting : transformer.getSettings()) {
                if (setting instanceof BooleanSetting) {
                    if (setting.getName()
                            .equals("enabled")) {
                        transformerObj.addProperty(
                                setting.getName(), useEnabledByDefault ? transformer.isEnabledByDefault() : transformer.isEnabled()
                        );
                    }
                    else {
                        transformerObj.addProperty(
                                setting.getName(),
                                ((BooleanSetting) setting).isEnabled()
                        );
                    }
                }
                else if (setting instanceof StringSetting) {
                    transformerObj.addProperty(
                            setting.getName(), ((StringSetting) setting).getValue());
                }
                else if (setting instanceof NumberSetting) {
                    transformerObj.addProperty(
                            setting.getName(),
                            ((NumberSetting<?>) setting).getValue()
                    );
                }
                else if (setting instanceof StringListSetting) {
                    transformerObj.add(setting.getName(), new JsonArray());
                }
            }

            // Add transformer object to transformers array
            transformersArr.add(transformerObj);
        }

        // Add transformers array to global object
        obj.add("transformers", transformersArr);

        // Create gson w/ pretty printing
        final Gson gson = new GsonBuilder().setPrettyPrinting()
                .create();

        // Convert json object to a string
        final String jsonStr = gson.toJson(obj);

        // Write to file
        final FileWriter writer = new FileWriter(outFile);
        writer.write(jsonStr);
        writer.flush();
        writer.close();

        // Feedback
        Ambien.logger.info("Created stock configuration file "+ outFile.getAbsolutePath());
    }



	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void load (final String input, final String output, final File file, final boolean experimentalTransformers) {
		// Parse file
		final JsonObject obj;
		try {
			// 不知道为什么
			obj = JsonParser.parseReader(new BufferedReader(new FileReader(file)))
					.getAsJsonObject();
		} catch (FileNotFoundException e) {
			Ambien.logger.error("Configuration file not found!");
			return;
		} catch (IllegalStateException ile) {
			Ambien.logger.error("Failed to load configuration file!!!");

			Ambien.logger.error(
					"Please, check your configuration file at \"" + Entrypoint.ambienArgs.configLocation + "\"");
			Ambien.logger.info("Exit!");
			System.exit(0);
			throw new Error();
		}

		// Check versions
		if (!obj.get("version")
				.getAsString()
				.equals(Ambien.VERSION)) {

			Ambien.logger.warn(
					"Settings file was made for a different version of " +Ambien.name+ " (" + Ambien.VERSION + "), some features may have changed or don't exist anymore.");
		}
		// Set input & output jar strings
		Ambien.get.inputJar = (input == null) ? obj.get("input")
				.getAsString() : input;
		Ambien.get.outputJar = (output == null) ? obj.get("output")
				.getAsString() : output;

		String parsedNaming = Ambien.get.naming = obj.get("naming").getAsString();

		try{
			Ambien.get.theNamingNaming = Namings.findByName(Ambien.get.naming);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Illegal naming "+parsedNaming);
		}
		// Get libraries
		final JsonArray libraries = obj.get("libraries")
				.getAsJsonArray();
		for (int i = 0; i < libraries.size(); i++) {
			final String libraryPath = libraries.get(i)
					.getAsString();
			Ambien.get.libraries.add(libraryPath);
			Ambien.logger.info("Added path for library: {}", libraryPath);
		}

		// Get excluded classes
		final JsonArray exclusionArray = obj.get("exclusion-prefix")
				.getAsJsonArray();
		for (int i = 0; i < exclusionArray.size(); i++) {
			final String exclusion = exclusionArray.get(i)
					.getAsString();
			Ambien.get.excludedClasses.add(exclusion);
			Ambien.logger.info("Added to exclusion list: {}", exclusion);
		}

		// Set remove exclude annotations setting
		Ambien.get.removeExcludeAnnotations = obj.get("remove-annotations")
				.getAsBoolean();

		// Get transformers array
		final JsonArray transformersArr = obj.get("transformers")
				.getAsJsonArray();

		// Enumerate through transformer entries
		for (int i = 0; i < transformersArr.size(); i++) {
			// Get entry object
			final JsonObject transformerObj = transformersArr.get(i)
					.getAsJsonObject();

			// Get transformer from name
			final String transformerName = transformerObj.get("name")
					.getAsString();
			final Transformer transformer = Ambien.get.transformerManager.getTransformer(
					transformerName);
			if (transformer == null) {
				Ambien.logger.warn(
						"Could not find transformer with the name \"{}\".",
						transformerName
				                  );
				continue;
			}

			// Set enabled state
			transformer.setEnabled(transformerObj.get("enabled")
					                       .getAsBoolean());

			// Warn if the transformer is experimental
			if (transformer.isEnabled() && transformer.getStability()
					.equals(Stability.EXPERIMENTAL) && !experimentalTransformers) {
				Ambien.logger.warn(
						"Ignoring enabled transformer \"{}\" because " + Ambien.name +" was ran without the experimental flag.",
						transformer.getName()
				                  );
				continue;
			}

			// Assume rest of the entries are settings of the transformer
			for (Setting<?> setting : transformer.getSettings()) {
				final JsonElement element = transformerObj.get(setting.getName());
				try {
					if (setting instanceof BooleanSetting) {
						((BooleanSetting) setting).setEnabled(
								element.getAsBoolean());
					}
					else if (setting instanceof StringSetting) {
						((StringSetting) setting).setValue(element.getAsString());
					}
					else if (setting instanceof NumberSetting) {
						NumberSetting numberSetting = (NumberSetting) setting;
						if (numberSetting.getValue() instanceof Integer) {
							numberSetting.setValue(element.getAsInt());
						}
						else if (numberSetting.getValue() instanceof Long) {
							numberSetting.setValue(element.getAsLong());
						}
						else if (numberSetting.getValue() instanceof Float) {
							numberSetting.setValue(element.getAsFloat());
						}
						else if (numberSetting.getValue() instanceof Double) {
							numberSetting.setValue(element.getAsDouble());
						}
					}
					else if (setting instanceof StringListSetting) {

						StringListSetting stringListSetting = (StringListSetting) setting;

						// Add options
						final JsonArray array = element.getAsJsonArray();
						for (int j = 0; j < array.size(); j++) {
							// Clear settings for first use
							if (!stringListSetting.isOptionsCleared()) {
								try {
									stringListSetting.getOptions()
											.clear();
								} catch (Exception e) {

								}
								stringListSetting.setOptionsCleared(true);
							}

							// Add option
							stringListSetting.getOptions()
									.add(
                                            array.get(j).getAsString()
                                    );
						}
					}
				} catch (NullPointerException e) {
					Ambien.logger.error(
							"Setting \"{}\" not found for transformer {}",
							setting.getName(), transformerName
					                   );
				}
			}
		}

		// Feedback
		Ambien.logger.info("Loaded configuration file (" + file.getAbsolutePath() + ")");
	}
}
