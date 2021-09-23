package internal.util.http.curl;

import internal.util.http.HttpHeadersBuilder;
import nbbrd.design.BuilderPattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static internal.util.http.HttpConstants.hasProxy;

@lombok.experimental.UtilityClass
class Curl {

    public static final int CURL_UNSUPPORTED_PROTOCOL = 1;
    public static final int CURL_COULD_NOT_RESOLVE_HOST = 6;
    public static final int CURL_OPERATION_TIMEOUT = 28;
    public static final int CURL_FAILURE_RECEIVING = 56;

    @lombok.Value
    @lombok.Builder
    public static class CurlMeta {

        @lombok.Builder.Default
        int code = -1;

        @lombok.Builder.Default
        String message = null;

        @lombok.Builder.Default
        Map<String, List<String>> headers = Collections.emptyMap();

        public static CurlMeta parse(BufferedReader reader) throws IOException {
            CurlMeta.Builder result = new Builder();
            parseStatus(reader, result);
            parseHeaders(reader, result);
            return result.build();
        }

        private static void parseStatus(BufferedReader reader, CurlMeta.Builder result) throws IOException {
            String line = reader.readLine();
            if (line == null) {
                return;
            }
            int codeStart = line.indexOf(' ');
            if (codeStart == -1) {
                return;
            }
            int codeEnd = line.indexOf(' ', codeStart + 1);
            if (codeEnd == -1) {
                result.code(Integer.parseInt(line.substring(codeStart + 1)));
            } else {
                result.code(Integer.parseInt(line.substring(codeStart + 1, codeEnd))).message(line.substring(codeEnd + 1));
            }
        }

        private static void parseHeaders(BufferedReader reader, CurlMeta.Builder result) throws IOException {
            HttpHeadersBuilder headers = new HttpHeadersBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                int index = line.indexOf(":");
                if (index != -1) {
                    headers.put(line.substring(0, index), line.substring(index + 1).trim());
                }
            }
            result.headers(headers.build());
        }

        public static final class Builder {
            // fix error when generating Javadoc
        }
    }

    @lombok.Value
    @lombok.Builder
    public static class CurlVersion {

        @lombok.Singular
        List<String> lines;

        public static CurlVersion parse(BufferedReader reader) throws IOException {
            CurlVersion.Builder result = new CurlVersion.Builder();
            try {
                reader.lines().forEach(result::line);
            } catch (UncheckedIOException ex) {
                throw ex.getCause();
            }
            return result.build();
        }

        public static final class Builder {
            // fix error when generating Javadoc
        }
    }

    @BuilderPattern(String[].class)
    public static final class CurlCommandBuilder {

        private final List<String> items;

        public CurlCommandBuilder() {
            this.items = new ArrayList<>();
            items.add("curl");
        }

        private CurlCommandBuilder push(String item) {
            items.add(item);
            return this;
        }

        public CurlCommandBuilder url(URL url) {
            return push(url.toString());
        }

        public CurlCommandBuilder proxy(Proxy proxy) {
            if (hasProxy(proxy)) {
                InetSocketAddress address = (InetSocketAddress) proxy.address();
                push("-x").push(address.getHostString() + ":" + address.getPort());
            }
            return this;
        }

        public CurlCommandBuilder output(Path file) {
            return push("-o").push(file.toString());
        }

        public CurlCommandBuilder silent() {
            return push("-s");
        }

        public CurlCommandBuilder dumpHeader(String filename) {
            return push("-D").push(filename);
        }

        public CurlCommandBuilder connectTimeout(int seconds) {
            return push("--connect-timeout").push(Integer.toString(seconds));
        }

        public CurlCommandBuilder maxTime(int seconds) {
            return push("-m").push(Integer.toString(seconds));
        }

        public CurlCommandBuilder insecure(boolean insecure) {
            return insecure ? insecure() : this;
        }

        public CurlCommandBuilder insecure() {
            return push("-k");
        }

        public CurlCommandBuilder header(String key, String value) {
            return push("-H").push(key + ": " + value);
        }

        public CurlCommandBuilder headers(Map<String, List<String>> headers) {
            HttpHeadersBuilder.keyValues(headers)
                    .forEach(header -> header(header.getKey(), header.getValue()));
            return this;
        }

        public CurlCommandBuilder version() {
            return push("-V");
        }

        public String[] build() {
            return items.toArray(new String[0]);
        }
    }
}
