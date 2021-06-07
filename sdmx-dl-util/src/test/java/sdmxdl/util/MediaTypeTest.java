/*
 * Copyright 2017 National Bank of Belgium
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
package sdmxdl.util;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Philippe Charles
 */
public class MediaTypeTest {

    private static Map<String, Collection<String>> singleton(String key, String value) {
        return Collections.singletonMap(key, Collections.singletonList(value));
    }

    private static final Map<String, Collection<String>> UTF_8 = singleton("charset", "utf-8");

    static final MediaType ANY_TYPE = new MediaType(MediaType.WILDCARD, MediaType.WILDCARD, emptyMap());
    static final MediaType ANY_TEXT_TYPE = new MediaType("text", MediaType.WILDCARD, emptyMap());
    static final MediaType ANY_IMAGE_TYPE = new MediaType("image", MediaType.WILDCARD, emptyMap());
    static final MediaType PLAIN_TEXT_UTF_8 = new MediaType("text", "plain", UTF_8);
    static final MediaType HTML_UTF_8 = new MediaType("text", "html", UTF_8);

    static final MediaType XML_UTF_8 = new MediaType("text", "xml", UTF_8);
    static final MediaType JSON_UTF_8 = new MediaType("application", "json", UTF_8);

    @Test
    @SuppressWarnings("null")
    public void testParse() {
        assertThat(MediaType.parse("text/plain; charset=utf-8")).hasToString("text/plain; charset=utf-8");
        assertThat(MediaType.parse("TEXT/PLAIN; CHARSET=utf-8")).hasToString("text/plain; charset=utf-8");
        assertThat(MediaType.parse("text/plain")).hasToString("text/plain");

        assertThatThrownBy(() -> MediaType.parse(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> MediaType.parse("text/plain ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MediaType.parse(" text/plain")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MediaType.parse("text/plain;")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MediaType.parse("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MediaType.parse("/plain")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MediaType.parse("text/")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testIsCompatible() {
        assertThat(PLAIN_TEXT_UTF_8).satisfies(o -> {
            assertThat(o.isCompatible(PLAIN_TEXT_UTF_8)).isTrue();
            assertThat(o.isCompatible(HTML_UTF_8)).isFalse();
            assertThat(o.isCompatible(ANY_TYPE)).isTrue();
            assertThat(o.isCompatible(ANY_TEXT_TYPE)).isTrue();
            assertThat(o.isCompatible(ANY_IMAGE_TYPE)).isFalse();
        });
    }

    @Test
    public void testWithoutParameters() {
        assertThat(XML_UTF_8.withoutParameters()).hasToString("text/xml");
    }

    @Test
    public void testToString() {
        assertThat(ANY_TYPE).hasToString("*/*");
        assertThat(ANY_TEXT_TYPE).hasToString("text/*");
        assertThat(ANY_IMAGE_TYPE).hasToString("image/*");
        assertThat(PLAIN_TEXT_UTF_8).hasToString("text/plain; charset=utf-8");
        assertThat(HTML_UTF_8).hasToString("text/html; charset=utf-8");

        assertThat(XML_UTF_8).hasToString("text/xml; charset=utf-8");
        assertThat(JSON_UTF_8).hasToString("application/json; charset=utf-8");
    }
}