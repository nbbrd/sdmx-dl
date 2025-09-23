package sdmxdl.web;

import org.junit.jupiter.api.Test;
import sdmxdl.DatabaseRef;
import sdmxdl.FlowRef;
import sdmxdl.Languages;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static sdmxdl.web.FlowRequest.parse;

class FlowRequestTest {

    @Test
    void representableAsURI() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(URI.create("")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(URI.create("boom:/ECB/ECB,EXR,1.0")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/boom")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(URI.create("sdmx-dl:")));

        assertThat(parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0")))
                .returns("ECB", FlowRequest::getSource)
                .returns(FlowRef.parse("ECB,EXR,1.0"), FlowRequest::getFlow)
                .returns(Languages.ANY, FlowRequest::getLanguages)
                .returns(DatabaseRef.NO_DATABASE, FlowRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0"), FlowRequest::toURI);

        assertThat(parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0?l=en,fr&d=abc")))
                .returns("ECB", FlowRequest::getSource)
                .returns(FlowRef.parse("ECB,EXR,1.0"), FlowRequest::getFlow)
                .returns(Languages.parse("en,fr"), FlowRequest::getLanguages)
                .returns(DatabaseRef.parse("abc"), FlowRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?d=abc&l=en%2Cfr"), FlowRequest::toURI);

        assertThat(parse(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?l=en%2Cfr&d=abc")))
                .returns("ECB", FlowRequest::getSource)
                .returns(FlowRef.parse("ECB,EXR,1.0"), FlowRequest::getFlow)
                .returns(Languages.parse("en,fr"), FlowRequest::getLanguages)
                .returns(DatabaseRef.parse("abc"), FlowRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0?d=abc&l=en%2Cfr"), FlowRequest::toURI);
    }
}