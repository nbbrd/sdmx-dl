package internal.util.rest;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.*;

public class HttpHeadersBuilderTest {

    @Test
    public void test() {
        assertThat(new HttpHeadersBuilder().build())
                .isEmpty();

        assertThatNullPointerException()
                .isThrownBy(() -> new HttpHeadersBuilder().put(null, "v1"));

        assertThatCode(() -> new HttpHeadersBuilder().put("k1", null))
                .doesNotThrowAnyException();

        assertThat(new HttpHeadersBuilder().put("k1", "v1").put("k2", "v2").build())
                .containsEntry("k1", singletonList("v1"))
                .containsEntry("k2", singletonList("v2"))
                .hasSize(2);

        assertThat(new HttpHeadersBuilder().put("k1", "v1").put("k1", "v2").build())
                .containsEntry("k1", asList("v1", "v2"))
                .hasSize(1);

        assertThat(new HttpHeadersBuilder().put("k1", "v2").put("k1", "v1").build())
                .containsEntry("k1", asList("v2", "v1"))
                .containsKeys("K1", "k1")
                .hasSize(1);

        assertThat(new HttpHeadersBuilder().put("k1", "v2").put("K1", "v1").build())
                .containsEntry("k1", asList("v2", "v1"))
                .containsKeys("K1", "k1")
                .hasSize(1);

        assertThat(new HttpHeadersBuilder().put("K1", "v2").put("k1", "v1").build())
                .containsEntry("K1", asList("v2", "v1"))
                .containsKeys("K1", "k1")
                .hasSize(1);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> new HttpHeadersBuilder().put("K1", "v2").build().put("k2", singletonList("v2")));

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> new HttpHeadersBuilder().put("K1", "v2").build().get("k1").add("v2"));

        assertThat(new HttpHeadersBuilder().put("k1", "v1").put("k2", null).build())
                .containsEntry("k1", singletonList("v1"))
                .hasSize(1);

        assertThat(new HttpHeadersBuilder().put("k1", "v1").put("k2", "").build())
                .containsEntry("k1", singletonList("v1"))
                .hasSize(1);
    }
}