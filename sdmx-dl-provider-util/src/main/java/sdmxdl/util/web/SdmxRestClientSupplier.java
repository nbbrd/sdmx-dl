package sdmxdl.util.web;

import lombok.NonNull;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;

@FunctionalInterface
public interface SdmxRestClientSupplier {

    @NonNull SdmxRestClient get(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException;
}
