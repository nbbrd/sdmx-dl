package internal.sdmxdl.desktop;

import org.junit.jupiter.api.Test;
import sdmxdl.*;

import java.net.URI;

import static internal.sdmxdl.desktop.SdmxUri.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SdmxUriTest {

    @Test
    void flowRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> toFlowRequest(URI.create("")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toFlowRequest(URI.create("boom:/ECB/ECB,EXR,1.0")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toFlowRequest(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/boom")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toFlowRequest(URI.create("sdmx-dl:")));

        assertThat(toFlowRequest(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0")))
                .returns(FlowRef.parse("ECB,EXR,1.0"), FlowRequest::getFlow)
                .returns(Languages.ANY, FlowRequest::getLanguages)
                .returns(DatabaseRef.NO_DATABASE, FlowRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0"), o -> fromFlowRequest("ECB", o));

        assertThat(toFlowRequest(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0?l=en,fr&d=abc")))
                .returns(FlowRef.parse("ECB,EXR,1.0"), FlowRequest::getFlow)
                .returns(Languages.parse("en,fr"), FlowRequest::getLanguages)
                .returns(DatabaseRef.parse("abc"), FlowRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?d=abc&l=en%2Cfr"), o -> fromFlowRequest("ECB", o));

        assertThat(toFlowRequest(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?l=en%2Cfr&d=abc")))
                .returns(FlowRef.parse("ECB,EXR,1.0"), FlowRequest::getFlow)
                .returns(Languages.parse("en,fr"), FlowRequest::getLanguages)
                .returns(DatabaseRef.parse("abc"), FlowRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?d=abc&l=en%2Cfr"), o -> fromFlowRequest("ECB", o));
    }

    @Test
    void keyRequest() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> toKeyRequest(URI.create("")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toKeyRequest(URI.create("boom:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toKeyRequest(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A/boom")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> toKeyRequest(URI.create("sdmx-dl:")));

        assertThat(toKeyRequest(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A")))
                .returns(FlowRef.parse("ECB,EXR,1.0"), KeyRequest::getFlow)
                .returns(Key.parse("M.CHF.EUR.SP00.A"), KeyRequest::getKey)
                .returns(Languages.ANY, KeyRequest::getLanguages)
                .returns(DatabaseRef.NO_DATABASE, KeyRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A"), o -> fromKeyRequest("ECB", o));

        assertThat(toKeyRequest(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A?l=en,fr&d=abc")))
                .returns(FlowRef.parse("ECB,EXR,1.0"), KeyRequest::getFlow)
                .returns(Key.parse("M.CHF.EUR.SP00.A"), KeyRequest::getKey)
                .returns(Languages.parse("en,fr"), KeyRequest::getLanguages)
                .returns(DatabaseRef.parse("abc"), KeyRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?d=abc&l=en%2Cfr"), o -> fromKeyRequest("ECB", o));

        assertThat(toKeyRequest(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?l=en%2Cfr&d=abc")))
                .returns(FlowRef.parse("ECB,EXR,1.0"), KeyRequest::getFlow)
                .returns(Key.parse("M.CHF.EUR.SP00.A"), KeyRequest::getKey)
                .returns(Languages.parse("en,fr"), KeyRequest::getLanguages)
                .returns(DatabaseRef.parse("abc"), KeyRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?d=abc&l=en%2Cfr"), o -> fromKeyRequest("ECB", o));
    }
}