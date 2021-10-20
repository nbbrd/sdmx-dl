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
package internal.util.rest;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.*;

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
        assertThat(MediaType.parse("text/plain; charset=utf-8;  version=2.1")).hasToString("text/plain; charset=utf-8; version=2.1");
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

    @Test
    public void testEquals() {
        assertThat(ANY_TYPE).isEqualTo(ANY_TYPE);
        assertThat(MediaType.parse("text/html; charset=utf-8")).isEqualTo(MediaType.parse("text/html;charset=utf-8"));
    }

    @Test
    public void testGetters() {
        assertThat(ANY_TYPE.getType()).isEqualTo("*");
        assertThat(ANY_TYPE.getSubtype()).isEqualTo("*");
        assertThat(ANY_TYPE.getParameters()).isEmpty();

        assertThat(XML_UTF_8.getType()).isEqualTo("text");
        assertThat(XML_UTF_8.getSubtype()).isEqualTo("xml");
        assertThat(XML_UTF_8.getParameters()).hasSize(1).containsEntry("charset", Collections.singletonList("utf-8"));
    }

    @Test
    public void testGetCharset() {
        assertThat(ANY_TYPE.getCharset()).isEmpty();
        assertThat(XML_UTF_8.getCharset()).contains(StandardCharsets.UTF_8);
        assertThat(MediaType.parse("text/xml; charset=abc").getCharset()).isEmpty();
    }

    @Test
    public void testWithCharset() {
        assertThatNullPointerException().isThrownBy(() -> ANY_TYPE.withCharset(null));
        assertThat(ANY_TYPE.withCharset(StandardCharsets.UTF_8).getCharset()).contains(StandardCharsets.UTF_8);
        assertThat(XML_UTF_8.withCharset(StandardCharsets.UTF_8).getCharset()).contains(StandardCharsets.UTF_8);
        assertThat(XML_UTF_8.withCharset(StandardCharsets.US_ASCII).getCharset()).contains(StandardCharsets.US_ASCII);
    }
}