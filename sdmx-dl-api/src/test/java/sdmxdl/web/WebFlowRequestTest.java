package sdmxdl.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.net.URI;
import org.junit.jupiter.api.Test;
import sdmxdl.MetaRequest;

class WebFlowRequestTest {

    @Test
    void flowRequest() {
        assertThatIllegalArgumentException().isThrownBy(() -> WebFlowRequest.parse(URI.create("")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> WebFlowRequest.parse(URI.create("boom:/ECB/ECB,EXR,1.0")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> WebFlowRequest.parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/boom")));

        assertThatIllegalArgumentException().isThrownBy(() -> WebFlowRequest.parse(URI.create("sdmx-dl:")));

        assertThat(WebFlowRequest.parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0")))
                .returns(MetaRequest.builder().flowOf("ECB,EXR,1.0").build(), WebFlowRequest::getRequest)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0"), WebFlowRequest::toURI)
                .hasToString("sdmx-dl:/ECB/ECB%2CEXR%2C1.0");

        assertThat(WebFlowRequest.parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0?l=en,fr&d=abc")))
                .returns(
                        MetaRequest.builder()
                                .flowOf("ECB,EXR,1.0")
                                .languagesOf("en,fr")
                                .databaseOf("abc")
                                .build(),
                        WebFlowRequest::getRequest)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?d=abc&l=en%2Cfr"), WebFlowRequest::toURI)
                .hasToString("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?d=abc&l=en%2Cfr");

        assertThat(WebFlowRequest.parse(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?l=en%2Cfr&d=abc")))
                .returns(
                        MetaRequest.builder()
                                .flowOf("ECB,EXR,1.0")
                                .languagesOf("en,fr")
                                .databaseOf("abc")
                                .build(),
                        WebFlowRequest::getRequest)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?d=abc&l=en%2Cfr"), WebFlowRequest::toURI)
                .hasToString("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?d=abc&l=en%2Cfr");
    }
}
