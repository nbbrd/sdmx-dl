package sdmxdl.provider.web;

import lombok.NonNull;
import nbbrd.io.text.BaseProperty;
import sdmxdl.Connection;
import sdmxdl.Languages;
import sdmxdl.web.WebSource;
import sdmxdl.web.spi.WebContext;

import java.io.IOException;
import java.util.List;

public interface ConnectionFactory {

    @NonNull
    List<BaseProperty> getConnectionProperties();

    @NonNull
    Connection connect(@NonNull WebSource source, @NonNull Languages languages, @NonNull WebContext context) throws IOException;
}
