package internal.util.rest;

import com.github.tuupertunut.powershelllibjava.PowerShell;
import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;
import nbbrd.design.LombokWorkaround;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tuupertunut.powershelllibjava.PowerShell.escapePowerShellString;

final class IwrConnectionFactory implements DefaultClient.ConnectionFactory {

    @Override
    public DefaultClient.Connection open(URL query, Proxy proxy, int readTimeout, int connectTimeout,
                                         SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier,
                                         Map<String, List<String>> headers) throws IOException {
        PowerShell ps = PowerShell.open();
        exec(ps, getCommand(query, headers));
        return new IwrConnection(query, ps, IwrMeta.parse(ps));
    }

    private String getCommand(URL query, Map<String, List<String>> headers) {
        String headerString = HttpHeadersBuilder.keyValues(headers)
                .map(header -> "; $httpClient.DefaultRequestHeaders.add(" + escapePowerShellString(header.getKey()) + "," + escapePowerShellString(header.getValue()) + ")")
                .collect(Collectors.joining());

        return "Add-Type -AssemblyName System.Net.Http"
                + "; $httpHandler = New-Object System.Net.Http.HttpClientHandler"
                + "; $httpHandler.AutomaticDecompression = [System.Net.DecompressionMethods]::GZip -bor [System.Net.DecompressionMethods]::Deflate"
                + "; $httpHandler.AllowAutoRedirect = $false"
//                            + "; $httpHandler.ServerCertificateCustomValidationCallback = { $true }"
                + "; $httpClient = New-Object System.Net.Http.HttpClient $httpHandler"
//                    + "; $httpClient.Timeout = [TimeSpan]::FromMilliseconds(" + timeout + ")"
                + headerString
                + "; $response = $httpClient.GetAsync('" + query + "').GetAwaiter().GetResult()";
    }

    @lombok.AllArgsConstructor
    static final class IwrConnection implements DefaultClient.Connection {

        @lombok.Getter
        @lombok.NonNull
        private final URL query;

        @lombok.NonNull
        private final PowerShell psSession;

        @lombok.NonNull
        private final IwrMeta meta;

        @Override
        public int getStatusCode() {
            return meta.getStatusCode();
        }

        @Override
        public @Nullable String getStatusMessage() throws IOException {
            return exec(psSession, "$response.ReasonPhrase");
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
            String content = exec(psSession, "$response.Content.ReadAsStringAsync().Result");
            return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void close() {
            psSession.close();
        }
    }

    private static String exec(PowerShell ps, String command) throws IOException {
//            System.out.println(">" + command);
        try {
            return ps.executeCommands(command).trim();
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

    @lombok.Getter
    @lombok.Builder
    static final class IwrMeta {

        @lombok.Builder.Default
        int statusCode = DefaultClient.Connection.NOT_VALID_CODE;

        @lombok.Builder.Default
        Map<String, List<String>> headers = Collections.emptyMap();

        public static IwrMeta parse(PowerShell ps) throws IOException {
            IwrMeta.Builder result = builder();
            parseCode(ps, result);
            parseHeaders(ps, result);
            return result.build();
        }

        private static void parseCode(PowerShell ps, IwrMeta.Builder result) throws IOException {
            Parser.onInteger().parseValue(exec(ps, "$response.StatusCode.Value__")).ifPresent(result::statusCode);
        }

        private static void parseHeaders(PowerShell ps, IwrMeta.Builder result) throws IOException {
            if (!isValidCode(result)) {
                return;
            }
            String headerFields = exec(ps, "$response.Headers | select @{n='id';e={$_.Key + '=' + $_.Value}}  | Format-Table -HideTableHeaders");
            HttpHeadersBuilder headers = new HttpHeadersBuilder();
            Stream.of(headerFields.split(System.lineSeparator(), -1))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .forEach(line -> {
                        int index = line.indexOf('=');
                        if (index != -1) {
                            headers.put(line.substring(0, index), line.substring(index + 1));
                        }
                    });
            headers.put(DefaultClient.CONTENT_TYPE_HEADER, exec(ps, "$response.Content.Headers.ContentType.ToString()"));
            result.headers(headers.build());
        }

        private static boolean isValidCode(Builder result) {
            return result.statusCode$value / 100 == 2;
        }

        public static final class Builder {
            // fix error when generating Javadoc
        }
    }
}
