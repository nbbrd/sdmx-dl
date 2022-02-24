package sdmxdl.util.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;

@FunctionalInterface
public interface SdmxRestClientSupplier {

    @NonNull SdmxRestClient get(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException;
}
