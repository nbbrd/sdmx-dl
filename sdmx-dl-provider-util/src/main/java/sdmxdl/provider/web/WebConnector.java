package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.Connection;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;

@FunctionalInterface
public interface WebConnector {

    @NonNull Connection connect(@NonNull SdmxWebSource source, @NonNull WebContext context) throws IOException;
}
