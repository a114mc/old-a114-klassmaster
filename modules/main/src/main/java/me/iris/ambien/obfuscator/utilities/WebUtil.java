package me.iris.ambien.obfuscator.utilities;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

@UtilityClass
public class WebUtil {
    /**
     * Requests a JSON object from the given URL.
     *
     * @param urlStr The URL to request the JSON object from.
     * @return The JSON object parsed from the response.
     * @throws IOException If an I/O error occurs while reading from the URL.
     */
    public JsonObject requestJsonObject(final String urlStr) throws IOException {
        // Open stream to url
        final URL url = new URL(urlStr);
        final InputStream stream = url.openStream();

        // Read content from url & parse as json
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        final JsonElement element = JsonParser.parseReader(reader);

        // Close streams
        stream.close();
        reader.close();

        // Return as json object
        return element.getAsJsonObject();
    }
}
