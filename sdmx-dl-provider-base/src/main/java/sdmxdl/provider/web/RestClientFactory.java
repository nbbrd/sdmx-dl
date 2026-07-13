package sdmxdl.provider.web;

import lombok.NonNull;
import nbbrd.io.text.BaseProperty;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.util.List;

public interface RestClientFactory {

    @NonNull
    List<BaseProperty> getRestClientProperties();

    @NonNull
    RestClient createRestClient(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context);
}
