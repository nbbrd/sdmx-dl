package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;

@FunctionalInterface
public interface RestClientSupplier {

    @NonNull RestClient get(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException;
}
