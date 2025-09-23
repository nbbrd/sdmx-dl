package sdmxdl.web;

import org.junit.jupiter.api.Test;
import sdmxdl.DatabaseRef;
import sdmxdl.FlowRef;
import sdmxdl.Key;
import sdmxdl.Languages;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static sdmxdl.web.KeyRequest.parse;

class KeyRequestTest {

    @Test
    void representableAsURI() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(URI.create("")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(URI.create("boom:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A/boom")));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> parse(URI.create("sdmx-dl:")));

        assertThat(parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A")))
                .returns("ECB", KeyRequest::getSource)
                .returns(FlowRef.parse("ECB,EXR,1.0"), KeyRequest::getFlow)
                .returns(Key.parse("M.CHF.EUR.SP00.A"), KeyRequest::getKey)
                .returns(Languages.ANY, KeyRequest::getLanguages)
                .returns(DatabaseRef.NO_DATABASE, KeyRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A"), KeyRequest::toURI);

        assertThat(parse(URI.create("sdmx-dl:/ECB/ECB,EXR,1.0/M.CHF.EUR.SP00.A?l=en,fr&d=abc")))
                .returns("ECB", KeyRequest::getSource)
                .returns(FlowRef.parse("ECB,EXR,1.0"), KeyRequest::getFlow)
                .returns(Key.parse("M.CHF.EUR.SP00.A"), KeyRequest::getKey)
                .returns(Languages.parse("en,fr"), KeyRequest::getLanguages)
                .returns(DatabaseRef.parse("abc"), KeyRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?d=abc&l=en%2Cfr"), KeyRequest::toURI);

        assertThat(parse(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?l=en%2Cfr&d=abc")))
                .returns("ECB", KeyRequest::getSource)
                .returns(FlowRef.parse("ECB,EXR,1.0"), KeyRequest::getFlow)
                .returns(Key.parse("M.CHF.EUR.SP00.A"), KeyRequest::getKey)
                .returns(Languages.parse("en,fr"), KeyRequest::getLanguages)
                .returns(DatabaseRef.parse("abc"), KeyRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB/ECB%2CEXR%2C1.0/M.CHF.EUR.SP00.A?d=abc&l=en%2Cfr"), KeyRequest::toURI);
    }
}