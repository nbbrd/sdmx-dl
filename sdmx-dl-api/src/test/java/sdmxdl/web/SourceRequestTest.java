package sdmxdl.web;

import org.junit.jupiter.api.Test;
import sdmxdl.Languages;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static sdmxdl.web.SourceRequest.parse;

class SourceRequestTest {

    @Test
    void representableAsURI() {
        assertThatIllegalArgumentException()
                .isThrownBy(()->parse(URI.create("")));

        assertThatIllegalArgumentException()
                .isThrownBy(()->parse(URI.create("boom:/ECB")));

        assertThatIllegalArgumentException()
                .isThrownBy(()->parse(URI.create("sdmx-dl:/ECB/boom")));

        assertThatIllegalArgumentException()
                .isThrownBy(()->parse(URI.create("sdmx-dl:")));

        assertThat(parse(URI.create("sdmx-dl:/ECB")))
                .returns("ECB", SourceRequest::getSource)
                .returns(Languages.ANY, SourceRequest::getLanguages)
                .returns(URI.create("sdmx-dl:/ECB"), SourceRequest::toURI);

        assertThat(parse(URI.create("sdmx-dl:/ECB?l=en,fr")))
                .returns("ECB", SourceRequest::getSource)
                .returns(Languages.parse("en,fr"), SourceRequest::getLanguages)
                .returns(URI.create("sdmx-dl:/ECB?l=en%2Cfr"), SourceRequest::toURI);

        assertThat(parse(URI.create("sdmx-dl:/ECB?l=en%2Cfr")))
                .returns("ECB", SourceRequest::getSource)
                .returns(Languages.parse("en,fr"), SourceRequest::getLanguages)
                .returns(URI.create("sdmx-dl:/ECB?l=en%2Cfr"), SourceRequest::toURI);
    }
}