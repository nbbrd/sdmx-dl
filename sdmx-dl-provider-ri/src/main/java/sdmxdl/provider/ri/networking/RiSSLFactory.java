package sdmxdl.provider.ri.networking;

import lombok.AccessLevel;
import lombok.NonNull;
import nl.altindag.ssl.util.TrustManagerUtils;
import sdmxdl.provider.Slow;
import sdmxdl.web.spi.SSLFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import java.util.Optional;
import java.util.function.Supplier;

import static sdmxdl.provider.Suppliers.memoize;

@lombok.Builder
final class RiSSLFactory implements SSLFactory {

    @lombok.Builder.Default
    private final boolean noDefaultTrustMaterial = false;

    @lombok.Builder.Default
    private final boolean noSystemTrustMaterial = false;

    @lombok.Getter(value = AccessLevel.PACKAGE, lazy = true)
    private final @NonNull nl.altindag.ssl.SSLFactory lazyDelegate = initLazyDelegate();

    @Slow
    private nl.altindag.ssl.SSLFactory initLazyDelegate() {
        nl.altindag.ssl.SSLFactory.Builder result = nl.altindag.ssl.SSLFactory.builder();
        if (!noDefaultTrustMaterial) result.withTrustMaterial(MEMOIZED_DEFAULT_TRUST_MATERIAL.get());
        if (!noSystemTrustMaterial) MEMOIZED_SYSTEM_TRUST_MATERIAL.get().ifPresent(result::withTrustMaterial);
        if (noDefaultTrustMaterial && noSystemTrustMaterial) result.withDummyTrustMaterial();
        return result.build();
    }

    @Override
    public @NonNull SSLSocketFactory getSSLSocketFactory() {
        return getLazyDelegate().getSslSocketFactory();
    }

    @Override
    public @NonNull HostnameVerifier getHostnameVerifier() {
        return getLazyDelegate().getHostnameVerifier();
    }

    @Slow
    private static final Supplier<X509ExtendedTrustManager> MEMOIZED_DEFAULT_TRUST_MATERIAL
            = memoize(TrustManagerUtils::createTrustManagerWithJdkTrustedCertificates);

    @Slow
    private static final Supplier<Optional<X509ExtendedTrustManager>> MEMOIZED_SYSTEM_TRUST_MATERIAL
            = memoize(TrustManagerUtils::createTrustManagerWithSystemTrustedCertificates);
}
