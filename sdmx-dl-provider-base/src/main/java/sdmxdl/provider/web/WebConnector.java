package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.Connection;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;

@FunctionalInterface
public interface WebConnector {

    @NonNull
    Connection connect(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException;
}
