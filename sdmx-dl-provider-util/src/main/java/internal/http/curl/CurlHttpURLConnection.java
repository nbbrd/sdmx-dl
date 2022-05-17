package internal.http.curl;

import lombok.NonNull;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.sys.EndOfProcessException;
import nbbrd.io.sys.OS;
import nbbrd.io.sys.ProcessReader;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static internal.http.curl.Curl.*;

public final class CurlHttpURLConnection extends HttpURLConnection {

    public static @NonNull CurlHttpURLConnection of(@NonNull URL url, @NonNull Proxy proxy) throws IOException {
        return new CurlHttpURLConnection(url, proxy, false, Files.createTempDirectory("curl"));
    }

    public static @NonNull CurlHttpURLConnection insecureForTestOnly(@NonNull URL url, @NonNull Proxy proxy) throws IOException {
        return new CurlHttpURLConnection(url, proxy, true, Files.createTempDirectory("curl"));
    }

    @lombok.NonNull
    private final Proxy proxy;

    private final boolean insecure;

    private final Path tempDir;

    private Map<String, List<String>> headerFields = Collections.emptyMap();

    private CurlHttpURLConnection(URL url, Proxy proxy, boolean insecure, Path tempDir) {
        super(url);
        this.proxy = proxy;
        this.insecure = insecure;
        this.tempDir = tempDir;
    }

    @Override
    public boolean usingProxy() {
        return Curl.hasProxy(proxy);
    }

    @Override
    public void connect() throws IOException {
        String[] request = createCurlCommand();
        CurlHead responseHead = executeCurlCommand(request);
        this.responseCode = responseHead.getStatus().getCode();
        this.responseMessage = responseHead.getStatus().getMessage();
        this.headerFields = responseHead.getHeaders();
    }

    @Override
    public void disconnect() {
        if (Files.exists(tempDir)) {
            try {
                try (Stream<Path> files = Files.walk(tempDir).sorted(Comparator.reverseOrder())) {
                    files.forEach(IOConsumer.unchecked(Files::delete));
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public String getHeaderField(String name) {
        return lastValueOrNull(headerFields, name);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(getInput());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(getOutput());
    }

    @VisibleForTesting
    Path getInput() {
        return tempDir.resolve("input.tmp");
    }

    @VisibleForTesting
    Path getOutput() {
        return tempDir.resolve("output.tmp");
    }

    @VisibleForTesting
    boolean isSchannel() {
        return OS.NAME.equals(OS.Name.WINDOWS);
    }

    @VisibleForTesting
    String[] createCurlCommand() {
        return new CurlCommandBuilder()
                .request(getRequestMethod())
                .url(getURL())
                .http1_1()
                .silent(true)
                .sslRevokeBestEffort(isSchannel())
                .insecure(insecure)
                .proxy(proxy)
                .output(getInput())
                .dumpHeader("-")
                .connectTimeout(getConnectTimeout() / 1000f)
                .maxTime(getReadTimeout() / 1000f)
                .headers(getRequestProperties())
                .dataBinary(getDoOutput() ? getOutput() : null)
                .build();
    }

    private CurlHead executeCurlCommand(String[] command) throws IOException {
        try (BufferedReader reader = ProcessReader.newReader(command)) {
            return CurlHead.parseResponse(reader);
        } catch (EndOfProcessException ex) {
            switch (ex.getExitValue()) {
                case CURL_UNSUPPORTED_PROTOCOL:
                    throw new IOException("Unsupported protocol '" + getURL().getProtocol() + "'");
                case CURL_COULD_NOT_RESOLVE_HOST:
                    throw new UnknownHostException(getURL().getHost());
                case CURL_OPERATION_TIMEOUT:
                    throw new IOException("Read timed out");
                case CURL_FAILURE_RECEIVING:
                    throw new IOException(getFailureReceivingNetworkDataMessage(proxy));
                default:
                    throw ex;
            }
        }
    }

    private static String getFailureReceivingNetworkDataMessage(Proxy proxy) {
        String result = "Failure in receiving network data.";
        if (Curl.hasProxy(proxy)) {
            result = "Unable to tunnel through proxy. " + result;
        }
        return result;
    }

    private static @Nullable String lastValueOrNull(@NonNull Map<String, List<String>> headers, @NonNull String name) {
        List<String> header = headers.get(name);
        return header != null && !header.isEmpty() ? header.get(header.size() - 1) : null;
    }
}
