package internal.util.http;

import org.checkerframework.checker.index.qual.NonNegative;
import sdmxdl.web.spi.URLConnectionFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.net.ProxySelector;
import java.util.List;
import java.util.function.Supplier;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class HttpContext {

    private static final int NO_TIMEOUT = 0;

    @NonNegative
    @lombok.Builder.Default
    int readTimeout = NO_TIMEOUT;

    @NonNegative
    @lombok.Builder.Default
    int connectTimeout = NO_TIMEOUT;

    @NonNegative
    @lombok.Builder.Default
    int maxRedirects = 20;

    @lombok.NonNull
    @lombok.Builder.Default
    Supplier<ProxySelector> proxySelector = ProxySelector::getDefault;

    @lombok.NonNull
    @lombok.Builder.Default
    Supplier<SSLSocketFactory> sslSocketFactory = HttpsURLConnection::getDefaultSSLSocketFactory;

    @lombok.NonNull
    @lombok.Builder.Default
    Supplier<HostnameVerifier> hostnameVerifier = HttpsURLConnection::getDefaultHostnameVerifier;

    @lombok.NonNull
    @lombok.Builder.Default
    Supplier<URLConnectionFactory> urlConnectionFactory = URLConnectionFactory::getDefault;

    @lombok.NonNull
    @lombok.Builder.Default
    HttpEventListener listener = HttpEventListener.noOp();

    @lombok.Singular
    List<StreamDecoder> decoders;

    @lombok.NonNull
    @lombok.Builder.Default
    HttpAuthenticator authenticator = HttpAuthenticator.noOp();

    @lombok.Builder.Default
    boolean preemptiveAuthentication = false;

    @lombok.Builder.Default
    String userAgent = null;

    public static Builder builder() {
        return new Builder()
                .decoder(StreamDecoder.gzip())
                .decoder(StreamDecoder.deflate());
    }

    public static final class Builder {
        // Fix javadoc compilation
    }
}
