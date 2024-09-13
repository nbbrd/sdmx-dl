package sdmxdl.desktop;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.FlowRef;
import sdmxdl.Languages;
import sdmxdl.Options;

import java.util.List;

@lombok.Value
public class DataSourceRef {

    @NonNull String source;

    @Nullable
    String catalog;

    @NonNull FlowRef flow;

    @NonNull List<String> dimensions;

    @NonNull Languages languages;

    public Options toOptions() {
        return Options.builder().languages(languages).catalogId(catalog).build();
    }
}
