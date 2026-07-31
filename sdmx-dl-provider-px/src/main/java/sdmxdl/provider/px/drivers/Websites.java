package sdmxdl.provider.px.drivers;

import nbbrd.io.picocsv.Picocsv;
import nbbrd.picocsv.Csv;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

final class Websites {

    private Websites() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final Picocsv.Parser<Map<String, Website>> PARSER =
            Picocsv.Parser
                    .builder(Websites::parseCsv)
                    .options(Csv.ReaderOptions.DEFAULT
                            .toBuilder()
                            .lenientSeparator(true)
                            .build())
                    .build();

    @lombok.Value
    static class Website {
        URL url;
        String listing;
    }

    private static Map<String, Website> parseCsv(Csv.Reader reader) throws IOException {
        Map<String, Website> result = new HashMap<>();
        while (reader.readLine()) {
            if (!reader.isComment()) {
                if (!reader.readField()) throw new IOException("Invalid format, expecting host");
                String host = reader.toString();
                if (!reader.readField()) throw new IOException("Invalid format, expecting URL");
                URL url = reader.length() > 0 ? URI.create(reader.toString()).toURL() : null;
                String listing = null;
                if (reader.readField()) {
                    listing = reader.length() > 0 ? reader.toString() : null;
                    if (reader.readField()) throw new IOException("Invalid format, unexpected field");
                }
                result.put(host, new Website(url, listing));
            }
        }
        return result;
    }
}
