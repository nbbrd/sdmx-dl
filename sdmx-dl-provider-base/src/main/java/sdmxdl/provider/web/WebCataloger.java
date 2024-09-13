package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.Catalog;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@FunctionalInterface
public interface WebCataloger {

    @NonNull
    List<Catalog> getCatalogs(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException;

    static @NonNull WebCataloger noOp() {
        return (source, languages, context) -> Collections.emptyList();
    }
}
