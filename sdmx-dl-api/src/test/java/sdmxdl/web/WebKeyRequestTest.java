package sdmxdl.web;

import org.junit.jupiter.api.Test;
import sdmxdl.KeyRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class WebKeyRequestTest {

    @Test
    void keyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> WebKeyRequest.parse(URI.create("")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> WebKeyRequest.parse(URI.create("boom:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> WebKeyRequest.parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A/boom")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> WebKeyRequest.parse(URI.create("sdmx-dl:")));

        assertThat(WebKeyRequest.parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A")))
                .returns(KeyRequest
                        .builder()
                        .flowOf("ECB,EXR,1.0")
                        .keyOf("M.CHF.EUR.SP00.A")
                        .build(), WebKeyRequest::getRequest)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A"), WebKeyRequest::toURI)
                .hasToString("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A");

        assertThat(WebKeyRequest.parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A?l=en,fr&d=abc")))
                .returns(KeyRequest
                        .builder()
                        .flowOf("ECB,EXR,1.0")
                        .languagesOf("en,fr")
                        .databaseOf("abc")
                        .keyOf("M.CHF.EUR.SP00.A")
                        .build(), WebKeyRequest::getRequest)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?d=abc&l=en%2Cfr"), WebKeyRequest::toURI)
                .hasToString("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?d=abc&l=en%2Cfr");

        assertThat(WebKeyRequest.parse(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?l=en%2Cfr&d=abc")))
                .returns(KeyRequest
                        .builder()
                        .flowOf("ECB,EXR,1.0")
                        .languagesOf("en,fr")
                        .databaseOf("abc")
                        .keyOf("M.CHF.EUR.SP00.A")
                        .build(), WebKeyRequest::getRequest)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?d=abc&l=en%2Cfr"), WebKeyRequest::toURI)
                .hasToString("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?d=abc&l=en%2Cfr");
    }
}