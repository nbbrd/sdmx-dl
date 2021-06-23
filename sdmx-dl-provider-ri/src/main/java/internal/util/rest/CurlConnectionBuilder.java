package internal.util.rest;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@lombok.RequiredArgsConstructor
public class CurlConnectionBuilder implements DefaultClient.ConnectionBuilder {

    private final boolean insecure;

    private URL query;
    private final Map<String, String> headers = new HashMap<>();
    private int readTimeout;
    private int connectTimeout;

    @Override
    public void setQuery(URL query) {
        this.query = query;
    }

    @Override
    public void setProxy(Proxy proxy) {
    }

    @Override
    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }

    @Override
    public void setConnectTimeout(int timeout) {
        this.connectTimeout = timeout;
    }

    @Override
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
    }

    @Override
    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    }

    @Override
    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public DefaultClient.Connection open() throws IOException {

        Path body = Files.createTempFile("body", ".tmp");

        List<String> command = getCommand(body);

        try (BufferedReader reader = ProcessReader.newReader(new ProcessBuilder(command).start())) {
            return new CurlConnection(query, body, CurlMeta.parse(reader));
        } catch (EndOfProcessException ex) {
            switch (ex.getExitValue()) {
                case 28:
                    throw new IOException("Read timed out");
                default:
                    throw ex;
            }
        }
    }

    private List<String> getCommand(Path body) {
        List<String> command = new ArrayList<>();
        command.add("curl");
        command.add(query.toString());
        command.add("-o");
        command.add(body.toString());
        command.add("-s");
        command.add("-D");
        command.add("-");
        command.add("--connect-timeout");
        command.add(Integer.toString(connectTimeout / 1000));
        command.add("-m");
        command.add(Integer.toString(readTimeout / 1000));

        if (insecure) {
            command.add("-k");
        }

        headers.forEach((k, v) -> {
            command.add("-H");
            command.add(k + ": " + v);
        });

        return command;
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
    }
}
