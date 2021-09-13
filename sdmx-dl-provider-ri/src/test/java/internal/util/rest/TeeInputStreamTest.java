package internal.util.rest;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class TeeInputStreamTest {

    @Test
    public void test() throws IOException {
        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (TeeInputStream x = new TeeInputStream(input, output)) {
            assertThat(x.available()).isEqualTo(bytes.length);
            assertThat(x.markSupported()).isFalse();
            x.skip(2);
            x.read();
        }
        assertThat(output.toByteArray()).isEqualTo(bytes);
    }
}