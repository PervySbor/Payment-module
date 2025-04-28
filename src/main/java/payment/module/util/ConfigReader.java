package payment.module.util;

import payment.module.exceptions.ConfigFileNotFoundException;
import payment.module.exceptions.FailedToReadJsonValueException;
import payment.module.exceptions.FatalException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

public class ConfigReader {

    private static final Path config;

    static {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL filePath = loader.getResource("config.json");
            if (filePath == null) {
                throw new ConfigFileNotFoundException("");
            }
            config = Path.of(filePath.toURI());
            if (!Files.exists(config)) {
                throw new ConfigFileNotFoundException("");
            }
        } catch (URISyntaxException | ConfigFileNotFoundException e) {
            LogManager.logException(e, Level.CONFIG);
            throw (RuntimeException) new FatalException("configuration failed").initCause(e);
        }
    }

    public static String getStringValue(String property)
    {
        try {
            String json = Files.readString(config);
            return JsonManager.getStringValue(json, property);
        } catch (IOException | FailedToReadJsonValueException e) {
            LogManager.logException(e, Level.CONFIG);
            throw (RuntimeException) new FatalException("configuration failed").initCause(e);
        }
    }

    public static List<String> getListValue(String property){
        try {
            String json = Files.readString(config);
            return JsonManager.getListValue(json, property);
        } catch ( IOException | FailedToReadJsonValueException e) {
            LogManager.logException(e, Level.CONFIG);
            throw (RuntimeException) new FatalException("configuration failed").initCause(e);
        }
    }
}
