package sdmxdl.util.web;

import lombok.AccessLevel;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.ext.SdmxException;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.web.SdmxWebConnection;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;
import sdmxdl.web.spi.SdmxWebDriver;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@lombok.Builder(toBuilder = true)
public final class SdmxRepoDriverSupport implements SdmxWebDriver {

    @lombok.Getter
    @lombok.NonNull
    private final String name;

    @lombok.Getter
    @lombok.Builder.Default
    private final int rank = SdmxWebDriver.UNKNOWN;

    @lombok.Getter
    @lombok.Builder.Default
    private final boolean available = true;

    @lombok.Singular
    private final List<SdmxRepository> repos;

    @lombok.Getter(value = AccessLevel.PRIVATE, lazy = true)
    private final Validator<SdmxWebSource> sourceValidator = SdmxValidators.onDriverName(name);

    @Override
    public @NonNull SdmxWebConnection connect(@NonNull SdmxWebSource source, @NonNull SdmxWebContext context) throws IOException {
        Objects.requireNonNull(source);
        Objects.requireNonNull(context);
        getSourceValidator().checkValidity(source);

        return repos.stream()
                .filter(repo -> repo.getName().equals(source.getName()))
                .map(repo -> new SdmxRepoConnection(name, repo.asConnection()))
                .findFirst()
                .orElseThrow(() -> SdmxException.missingSource(source.toString(), SdmxWebSource.class));
    }

    @Override
    public @NonNull Collection<SdmxWebSource> getDefaultSources() {
        return repos
                .stream()
                .map(repo -> SdmxWebSource.builder().name(repo.getName()).driver(name).endpoint(URI.create(repo.getName())).build())
                .collect(Collectors.toSet());
    }

    @Override
    public @NonNull Collection<String> getSupportedProperties() {
        return Collections.emptyList();
    }
}
