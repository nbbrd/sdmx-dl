package sdmxdl.util.web;

import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import java.io.IOException;

@FunctionalInterface
public interface SdmxWebClientSupplier {

    @NonNull
    SdmxWebClient get(
            @NonNull SdmxWebSource source,
            @NonNull SdmxWebContext context
    ) throws IOException;
}
