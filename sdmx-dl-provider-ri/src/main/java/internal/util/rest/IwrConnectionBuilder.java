package internal.util.rest;

import com.github.tuupertunut.powershelllibjava.PowerShell;
import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;
import nbbrd.io.text.Parser;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.tuupertunut.powershelllibjava.PowerShell.escapePowerShellString;

final class IwrConnectionBuilder implements DefaultClient.ConnectionBuilder {

    @lombok.Setter
    private URL query;

    @lombok.Setter
    private Proxy proxy;

    @lombok.Setter
    private int readTimeout;

    @lombok.Setter
    private int connectTimeout;

    private SSLSocketFactory sslSocketFactory;

    private HostnameVerifier hostnameVerifier;

    private Map<String, String> headers = new HashMap<>();

    @Override
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    @Override
    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override
    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public DefaultClient.Connection open() throws IOException {
        PowerShell ps = PowerShell.open();
        return new IwrConnection(query, ps).open(headers, readTimeout);
    }

    @lombok.AllArgsConstructor
    static final class IwrConnection implements DefaultClient.Connection {

        @lombok.NonNull
        private final URL query;

        @lombok.NonNull
        private final PowerShell psSession;

        public IwrConnection open(Map<String, String> headers, int timeout) throws IOException {
            String headerString = headers
                    .entrySet()
                    .stream()
                    .map(header -> "; $httpClient.DefaultRequestHeaders.add(" + escapePowerShellString(header.getKey()) + "," + escapePowerShellString(header.getValue()) + ")")
                    .collect(Collectors.joining());

            exec("Add-Type -AssemblyName System.Net.Http"
                            + "; $httpHandler = New-Object System.Net.Http.HttpClientHandler"
                            + "; $httpHandler.AutomaticDecompression = [System.Net.DecompressionMethods]::GZip -bor [System.Net.DecompressionMethods]::Deflate"
                            + "; $httpHandler.AllowAutoRedirect = $false"
//                            + "; $httpHandler.ServerCertificateCustomValidationCallback = { $true }"
                            + "; $httpClient = New-Object System.Net.Http.HttpClient $httpHandler"
//                    + "; $httpClient.Timeout = [TimeSpan]::FromMilliseconds(" + timeout + ")"
                            + headerString
                            + "; $response = $httpClient.GetAsync('" + query + "').GetAwaiter().GetResult()"
            );

            return this;
        }

        @Override
        public int getResponseCode() throws IOException {
            String statusCode = exec("$response.StatusCode.Value__");
            return Parser.onInteger().parseValue(statusCode).orElse(200);
        }

        @Override
        public String getHeaderField(String key) throws IOException {
            return getHeaders().getOrDefault(key, Collections.emptyList()).stream().collect(Collectors.joining(","));
        }

        @Override
        public String getResponseMessage() throws IOException {
            return exec("$response.ReasonPhrase");
        }

        @Override
        public Map<String, List<String>> getHeaders() throws IOException {
            String headerFields = exec("$response.Headers | select @{n='id';e={$_.Key + '=' + $_.Value}}  | Format-Table -HideTableHeaders");
            String[] lines = headerFields.split(System.lineSeparator(), -1);
            Map<String, List<String>> result = new HashMap<>();
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] entry = line.split("=", -1);
                    result.computeIfAbsent(entry[0], o -> new ArrayList<>()).add(entry[1]);
                }
            }
            return result;
        }

        @Override
        public URL getQuery() {
            return query;
        }

        @Override
        public String getContentType() throws IOException {
            String result = exec("$response.Content.Headers.ContentType.ToString()");
            return result.isEmpty() ? null : result;
        }

        @Override
        public String getContentEncoding() {
            return null;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            String content = exec("$response.Content.ReadAsStringAsync().Result");
            return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void close() {
            psSession.close();
        }

        private String exec(String command) throws IOException {
//            System.out.println(">" + command);
            try {
                return psSession.executeCommands(command).trim();
            } catch (PowerShellExecutionException ex) {
                throw new IOException(getErrorMessage(ex));
            }
        }

        private static String getErrorMessage(PowerShellExecutionException ex) {
            String input = ex.getMessage();
            int index = input.indexOf(END_OF_COMMAND);
            return index != -1 ? input.substring(index + END_OF_COMMAND.length()) : input;
        }

        private static final String END_OF_COMMAND = "end-of-command-8Nb77LFv";
    }
}
