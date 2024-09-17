package sdmxdl.desktop;

import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.FlowRef;
import sdmxdl.Languages;
import sdmxdl.Options;

import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataSourceRef {

    @NonNull
    String source;

    @lombok.Builder.Default
    @Nullable
    String catalog = null;

    @lombok.Builder.Default
    @NonNull
    String flow = "";

    @lombok.Singular
    @NonNull
    List<String> dimensions;

    @lombok.Builder.Default
    @NonNull
    Languages languages = Sdmxdl.INSTANCE.getLanguages();

    public FlowRef toFlowRef() {
        return FlowRef.parse(flow);
    }

    public Options toOptions() {
        return Options.builder().languages(languages).catalogId(catalog).build();
    }
}
