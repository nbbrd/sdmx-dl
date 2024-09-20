package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.CatalogRef;
import sdmxdl.FlowRef;
import sdmxdl.Languages;

import java.util.List;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class DataSourceRef {

    @NonNull
    String source;

    @lombok.Builder.Default
    @NonNull
    CatalogRef catalog = CatalogRef.NO_CATALOG;

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
}
