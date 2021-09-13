/*
 * Copyright 2018 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package internal.util.rest;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * @author Philippe Charles
 */
public class RestQueryBuilderTest {

    @Test
    @SuppressWarnings("null")
    public void test() throws IOException {
        assertThatNullPointerException()
                .isThrownBy(() -> RestQueryBuilder.of(null));

        URL endpoint = new URL("http://localhost");

        assertThatNullPointerException()
                .isThrownBy(() -> RestQueryBuilder.of(endpoint).path((String) null));

        assertThatNullPointerException()
                .isThrownBy(() -> RestQueryBuilder.of(endpoint).path((List<String>) null));

        assertThatNullPointerException()
                .isThrownBy(() -> RestQueryBuilder.of(endpoint).param(null, ""));

        assertThatNullPointerException()
                .isThrownBy(() -> RestQueryBuilder.of(endpoint).param("", null));

        assertThat(RestQueryBuilder.of(endpoint).build())
                .hasToString("http://localhost");

        assertThat(RestQueryBuilder.of(endpoint).param("p1", "v1").param("p&=2", "v&=2").build())
                .hasToString("http://localhost?p1=v1&p%26%3D2=v%26%3D2");

        assertThat(RestQueryBuilder.of(endpoint).path("hello").path("worl/d").build())
                .hasToString("http://localhost/hello/worl%2Fd");

        assertThat(RestQueryBuilder.of(endpoint).path("hello").path("worl/d").param("p1", "v1").param("p&=2", "v&=2").build())
                .hasToString("http://localhost/hello/worl%2Fd?p1=v1&p%26%3D2=v%26%3D2");

        assertThat(RestQueryBuilder.of(endpoint).trailingSlash(true).build())
                .hasToString("http://localhost/");

        assertThat(RestQueryBuilder.of(endpoint).trailingSlash(true).param("p1", "v1").param("p&=2", "v&=2").build())
                .hasToString("http://localhost/?p1=v1&p%26%3D2=v%26%3D2");

        assertThat(RestQueryBuilder.of(endpoint).trailingSlash(true).path("hello").path("worl/d").build())
                .hasToString("http://localhost/hello/worl%2Fd/");

        assertThat(RestQueryBuilder.of(endpoint).trailingSlash(true).path("hello").path("worl/d").param("p1", "v1").param("p&=2", "v&=2").build())
                .hasToString("http://localhost/hello/worl%2Fd/?p1=v1&p%26%3D2=v%26%3D2");
    }
}
