package sdmxdl.provider.px.drivers;

import nbbrd.io.picocsv.Picocsv;
import nbbrd.picocsv.Csv;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

final class Websites {

    private Websites() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final Picocsv.Parser<Map<String, URL>> PARSER = Picocsv.Parser.builder(Websites::parseCsv).build();

    private static Map<String, URL> parseCsv(Csv.Reader reader) throws IOException {
        Map<String, URL> result = new HashMap<>();
        while (reader.readLine()) {
            if (!reader.isComment()) {
                if (!reader.readField()) throw new IOException("Invalid format, expecting host");
                String host = reader.toString();
                if (!reader.readField()) throw new IOException("Invalid format, expecting URL");
                URL url = reader.length() > 0 ? new URL(reader.toString()) : null;
                if (reader.readField()) throw new IOException("Invalid format, unexpected field");
                result.put(host, url);
            }
        }
        return result;
    }
}
