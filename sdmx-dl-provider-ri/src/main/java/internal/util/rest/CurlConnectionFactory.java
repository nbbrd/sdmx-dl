package internal.util.rest;

import nbbrd.design.BuilderPattern;
import nbbrd.io.sys.EndOfProcessException;
import nbbrd.io.sys.ProcessReader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@lombok.RequiredArgsConstructor
final class CurlConnectionFactory implements DefaultClient.ConnectionFactory {

    private static final int COULD_NOT_RESOLVE_HOST = 6;
    private static final int OPERATION_TIMEOUT = 28;

    private final boolean insecure;

    @Override
    public DefaultClient.Connection open(URL query, Proxy proxy, int readTimeout, int connectTimeout,
                                         SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier,
                                         Map<String, List<String>> headers) throws IOException {
        Path body = Files.createTempFile("body", ".tmp");

        String[] command = newCurlCommand()
                .url(query)
                .output(body)
                .silent()
                .dumpHeader("-")
                .connectTimeout(connectTimeout / 1000)
                .maxTime(readTimeout / 1000)
                .headers(headers)
                .build();

        try (BufferedReader reader = ProcessReader.newReader(command)) {
            return new CurlConnection(query, body, CurlMeta.parse(reader));
        } catch (EndOfProcessException ex) {
            switch (ex.getExitValue()) {
                case COULD_NOT_RESOLVE_HOST:
                    throw new UnknownHostException(query.getHost());
                case OPERATION_TIMEOUT:
                    throw new IOException("Read timed out");
                default:
                    throw ex;
            }
        }
    }

    private CurlCommandBuilder newCurlCommand() {
        CurlCommandBuilder result = new CurlCommandBuilder();
        if (insecure) {
            result.insecure();
        }
        return result;
    }

    @lombok.AllArgsConstructor
    private static final class CurlConnection implements DefaultClient.Connection {

        @lombok.Getter
        @lombok.NonNull
        private final URL query;

        @lombok.NonNull
        private final Path body;

        @lombok.NonNull
        private final CurlMeta meta;

        @Override
        public int getStatusCode() {
            return meta.getCode();
        }

        @Override
        public @Nullable String getStatusMessage() {
            return meta.getMessage();
        }

        @Override
        public @NonNull Optional<String> getHeaderFirstValue(@NonNull String name) {
            return HttpHeadersBuilder.firstValue(getHeaders(), name);
        }

        @Override
        public @NonNull Map<String, List<String>> getHeaders() {
            return meta.getHeaders();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return Files.newInputStream(body);
        }

        @Override
        public void close() throws IOException {
            Files.delete(body);
        }
    }

    @lombok.Getter
    @lombok.Builder
    static final class CurlMeta {

        @lombok.Builder.Default
        int code = DefaultClient.Connection.NOT_VALID_CODE;

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

    @BuilderPattern(String[].class)
    private static final class CurlCommandBuilder {

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

        public String[] build() {
            return items.toArray(new String[items.size()]);
        }
    }
}
