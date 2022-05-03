package internal.http.curl;

import lombok.NonNull;
import nbbrd.design.BuilderPattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@lombok.experimental.UtilityClass
class Curl {

    public static final int CURL_UNSUPPORTED_PROTOCOL = 1;
    public static final int CURL_COULD_NOT_RESOLVE_HOST = 6;
    public static final int CURL_OPERATION_TIMEOUT = 28;
    public static final int CURL_FAILURE_RECEIVING = 56;

    @lombok.Value
    @lombok.Builder
    public static class CurlHead {

        @lombok.Builder.Default
        int code = -1;

        @lombok.Builder.Default
        String message = null;

        @lombok.Singular
        Map<String, List<String>> headers;

        public static CurlHead parseResponse(BufferedReader reader) throws IOException {
            CurlHead.Builder result = new Builder();
            parseStatusLine(reader, result);
            parseHeaders(reader, result);
            return result.build();
        }

        private static char SP = 32;

        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages#status_line
        private static void parseStatusLine(BufferedReader reader, CurlHead.Builder result) throws IOException {
            String statusLine = reader.readLine();
            if (statusLine == null) {
                return;
            }
            int codeStart = statusLine.indexOf(SP);
            if (codeStart == -1) {
                return;
            }
            int codeEnd = statusLine.indexOf(SP, codeStart + 1);
            if (codeEnd == -1) {
                result.code(Integer.parseInt(statusLine.substring(codeStart + 1)));
            } else {
                result.code(Integer.parseInt(statusLine.substring(codeStart + 1, codeEnd))).message(statusLine.substring(codeEnd + 1));
            }
        }

        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Messages#headers_2
        private static void parseHeaders(BufferedReader reader, CurlHead.Builder result) throws IOException {
            Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                int index = line.indexOf(":");
                if (index != -1) {
                    String key = line.substring(0, index);
                    String value = line.substring(index + 1).trim();
                    if (value != null && !value.isEmpty()) {
                        headers.computeIfAbsent(key, ignore -> new ArrayList<>()).add(value);
                    }
                }
            }
            result.headers(headers);
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

        public static CurlVersion parseText(BufferedReader reader) throws IOException {
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

    // https://curl.se/docs/manpage.html
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

        public CurlCommandBuilder connectTimeout(float seconds) {
            return push("--connect-timeout").push(fixNumericalParameter(seconds));
        }

        public CurlCommandBuilder maxTime(float seconds) {
            return push("-m").push(fixNumericalParameter(seconds));
        }

        @CurlMinVersion("7.70.0")
        public CurlCommandBuilder sslRevokeBestEffort(boolean sslRevokeBestEffort) {
            return sslRevokeBestEffort ? push("--ssl-revoke-best-effort") : this;
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
            headers.forEach((key, values) -> values.forEach(value -> header(key, value)));
            return this;
        }

        public CurlCommandBuilder version() {
            return push("-V");
        }

        @CurlMinVersion("7.33.0")
        public CurlCommandBuilder http1_1() {
            return push("--http1.1");
        }

        public String[] build() {
            return items.toArray(new String[0]);
        }

        // some old versions don't accept decimal values!
        private String fixNumericalParameter(float seconds) {
            return Integer.toString((int) seconds);
        }
    }

    private @interface CurlMinVersion {
        String value();
    }

    static boolean hasProxy(@NonNull Proxy proxy) {
        return !proxy.equals(Proxy.NO_PROXY);
    }
}