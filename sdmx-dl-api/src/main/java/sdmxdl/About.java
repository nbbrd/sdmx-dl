package sdmxdl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public final class About {

    public static final String NAME = "sdmx-dl";

    public static final String VERSION = loadVersion();

    private static String loadVersion() {
        Properties properties = new Properties();
        try (InputStream stream = About.class.getResourceAsStream("/META-INF/maven/com.github.nbbrd.sdmx-dl/sdmx-dl-api/pom.properties")) {
            if (stream != null) {
                properties.load(stream);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return properties.getProperty("version", "unknown");
    }
}