package sdmxdl.provider.web;

import lombok.NonNull;
import sdmxdl.LanguagePriorityList;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;

@FunctionalInterface
public interface RestClientSupplier {

    @NonNull RestClient get(@NonNull SdmxWebSource source, @NonNull LanguagePriorityList languages, @NonNull WebContext context) throws IOException;
}
