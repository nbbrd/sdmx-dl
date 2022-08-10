package internal.util.http;

import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class HttpResponseExceptionTest {

    @Test
    public void test() {
        assertThat(new HttpResponseException(100, null))
                .hasMessageContaining("100")
                .hasMessageContaining("null")
                .satisfies(o -> {
                    assertThat(o.getResponseCode()).isEqualTo(100);
                    assertThat(o.getResponseMessage()).isNull();
                    assertThat(o.getHeaderFields()).isEmpty();
                });

        assertThat(new HttpResponseException(100, "hello world"))
                .hasMessageContaining("100")
                .hasMessageContaining("hello world")
                .satisfies(o -> {
                    assertThat(o.getResponseCode()).isEqualTo(100);
                    assertThat(o.getResponseMessage()).isEqualTo("hello world");
                    assertThat(o.getHeaderFields()).isEmpty();
                });

        assertThatNullPointerException()
                .isThrownBy(() -> new HttpResponseException(100, "", null));

        assertThat(new HttpResponseException(100, "hello world", singletonMap("key", singletonList("value"))))
                .hasMessageContaining("100")
                .hasMessageContaining("hello world")
                .satisfies(o -> {
                    assertThat(o.getResponseCode()).isEqualTo(100);
                    assertThat(o.getResponseMessage()).isEqualTo("hello world");
                    assertThat(o.getHeaderFields()).containsEntry("key", singletonList("value")).hasSize(1);
                });
    }
}
