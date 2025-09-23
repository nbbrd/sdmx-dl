package sdmxdl.web;

import org.junit.jupiter.api.Test;
import sdmxdl.DatabaseRef;
import sdmxdl.Languages;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static sdmxdl.web.DatabaseRequest.parse;

class DatabaseRequestTest {

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
                .returns("ECB", DatabaseRequest::getSource)
                .returns(Languages.ANY, DatabaseRequest::getLanguages)
                .returns(DatabaseRef.NO_DATABASE, DatabaseRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB"), DatabaseRequest::toURI);

        assertThat(parse(URI.create("sdmx-dl:/ECB?l=en,fr&d=abc")))
                .returns("ECB", DatabaseRequest::getSource)
                .returns(Languages.parse("en,fr"), DatabaseRequest::getLanguages)
                .returns(DatabaseRef.parse("abc"), DatabaseRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB?d=abc&l=en%2Cfr"), DatabaseRequest::toURI);

        assertThat(parse(URI.create("sdmx-dl:/ECB?l=en%2Cfr&d=abc")))
                .returns("ECB", DatabaseRequest::getSource)
                .returns(Languages.parse("en,fr"), DatabaseRequest::getLanguages)
                .returns(DatabaseRef.parse("abc"), DatabaseRequest::getDatabase)
                .returns(URI.create("sdmx-dl:/ECB?d=abc&l=en%2Cfr"), DatabaseRequest::toURI);
    }
}