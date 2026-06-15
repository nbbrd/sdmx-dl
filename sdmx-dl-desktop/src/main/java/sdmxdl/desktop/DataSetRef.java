package sdmxdl.desktop;

import lombok.NonNull;
import sdmxdl.Key;
import sdmxdl.KeyRequest;
import sdmxdl.web.WebFlowRequest;
import sdmxdl.web.WebKeyRequest;

@lombok.Value
@lombok.Builder
public class DataSetRef {

    @NonNull
    DataSourceRef dataSourceRef;

    @NonNull
    Key key;

    int dimensionIndex;

    public WebKeyRequest toWebKeyRequest() {
        WebFlowRequest webFlowRequest = dataSourceRef.toWebFlowRequest();
        return WebKeyRequest
                .builder()
                .source(webFlowRequest.getSource())
                .request(KeyRequest
                        .builderOf(webFlowRequest.getRequest())
                        .key(key)
                        .build())
                .build();
    }
}
