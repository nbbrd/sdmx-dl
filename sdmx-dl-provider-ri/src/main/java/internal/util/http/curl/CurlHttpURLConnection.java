package internal.util.http.curl;

import internal.util.http.HttpHeadersBuilder;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.sys.EndOfProcessException;
import nbbrd.io.sys.ProcessReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static internal.util.http.HttpConstants.hasProxy;
import static internal.util.http.curl.Curl.*;

@lombok.extern.java.Log
final class CurlHttpURLConnection extends HttpURLConnection {

    @lombok.NonNull
    private final Proxy proxy;

    private final boolean insecure;

    private Map<String, List<String>> headerFields = Collections.emptyMap();

    private Path body = null;

    @VisibleForTesting
    CurlHttpURLConnection(URL url, Proxy proxy, boolean insecure) {
        super(url);
        this.proxy = proxy;
        this.insecure = insecure;
    }

    @Override
    public boolean usingProxy() {
        return hasProxy(proxy);
    }

    @Override
    public void connect() throws IOException {
        Path output = Files.createTempFile("body", ".tmp");
        String[] request = createCurlCommand(output);
        CurlHead responseHead = executeCurlCommand(request);
        this.responseCode = responseHead.getCode();
        this.responseMessage = responseHead.getMessage();
        this.headerFields = responseHead.getHeaders();
        this.body = output;
    }

    @Override
    public void disconnect() {
        if (body != null && Files.exists(body)) {
            try {
                Files.delete(body);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public String getHeaderField(String name) {
        return HttpHeadersBuilder.lastValueOrNull(headerFields, name);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(body);
    }

    @VisibleForTesting
    String[] createCurlCommand(Path output) {
        return new CurlCommandBuilder()
                .url(getURL())
                .http1_1()
                .silent()
                .insecure(insecure)
                .proxy(proxy)
                .output(output)
                .dumpHeader("-")
                .connectTimeout(getConnectTimeout() / 1000f)
                .maxTime(getReadTimeout() / 1000f)
                .headers(getRequestProperties())
                .build();
    }

    private CurlHead executeCurlCommand(String[] command) throws IOException {
        if (log.isLoggable(Level.FINE)) {
            log.fine(Arrays.toString(command));
        }
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
        if (hasProxy(proxy)) {
            result = "Unable to tunnel through proxy. " + result;
        }
        return result;
    }
}
