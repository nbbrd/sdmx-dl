package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.DataRequest;
import sdmxdl.Key;
import sdmxdl.web.WebFlowRequest;
import sdmxdl.web.WebKeyRequest;

@lombok.Value
@lombok.Builder
public class DataSetRef {

    @NonNull DataSourceRef dataSourceRef;

    @NonNull Key key;

    int dimensionIndex;

    public WebKeyRequest toWebKeyRequest() {
        WebFlowRequest webFlowRequest = dataSourceRef.toWebFlowRequest();
        return WebKeyRequest.builder()
                .source(webFlowRequest.getSource())
                .request(DataRequest.builder()
                        .database(webFlowRequest.getRequest().getDatabase())
                        .flow(webFlowRequest.getRequest().getFlow())
                        .languages(webFlowRequest.getRequest().getLanguages())
                        .key(key)
                        .build())
                .build();
    }
}
