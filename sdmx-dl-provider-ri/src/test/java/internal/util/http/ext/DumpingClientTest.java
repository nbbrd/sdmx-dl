package internal.util.http.ext;

import internal.util.http.HttpClient;
import internal.util.http.HttpRequest;
import internal.util.http.HttpResponse;
import lombok.NonNull;
import nbbrd.io.function.IORunnable;
import nbbrd.io.function.IOSupplier;
import nbbrd.io.text.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.format.MediaType;
import wiremock.com.google.common.io.ByteStreams;
import wiremock.org.apache.commons.io.input.ReaderInputStream;
import wiremock.org.apache.hc.core5.http.io.entity.EmptyInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.*;

public class DumpingClientTest {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void testFactories(@TempDir Path temp) {
        IOSupplier<InputStream> empty = () -> EmptyInputStream.INSTANCE;

        Deque<Path> stack = new LinkedList<>();
        DumpingClient x = new DumpingClient(temp, MockedClient.ofBody(empty), stack::add);

        assertThatNullPointerException()
                .isThrownBy(() -> x.send(null));

        assertThat(stack).isEmpty();
    }

    @Test
    public void testEmptyClient(@TempDir Path temp) throws IOException {
        IOSupplier<InputStream> empty = () -> EmptyInputStream.INSTANCE;

        Deque<Path> stack = new LinkedList<>();
        DumpingClient x = new DumpingClient(temp, MockedClient.ofBody(empty), stack::add);

        try (HttpResponse r = x.send(request)) {
            assertThat(r.getContentType())
                    .isEqualTo(MediaType.ANY_TYPE);

            try (InputStream stream = r.getBody()) {
                assertThat(stream).isEmpty();
            }

            assertThat(stack)
                    .hasSize(1)
                    .element(0, PATH)
                    .exists()
                    .isEmptyFile();
        }
    }

    @Test
    public void testNonEmptyClient(@TempDir Path temp) throws IOException {
        IOSupplier<InputStream> nonEmpty = () -> new ReaderInputStream(new StringReader("hello"), StandardCharsets.UTF_8);

        Deque<Path> stack = new LinkedList<>();
        DumpingClient x = new DumpingClient(temp, MockedClient.ofBody(nonEmpty), stack::add);

        try (HttpResponse r = x.send(request)) {
            assertThat(r.getContentType())
                    .isEqualTo(MediaType.ANY_TYPE);

            try (InputStream stream = r.getBody()) {
                assertThat(stream).hasContent("hello");
            }

            assertThat(stack)
                    .hasSize(1)
                    .element(0, PATH)
                    .exists()
                    .hasContent("hello");
        }
    }

    @Test
    public void testFailingOnGetBody(@TempDir Path temp) throws IOException {
        IOSupplier<InputStream> failingOnGetBody = () -> {
            throw new IOException("boom");
        };

        Deque<Path> stack = new LinkedList<>();
        DumpingClient x = new DumpingClient(temp, MockedClient.ofBody(failingOnGetBody), stack::add);

        try (HttpResponse r = x.send(request)) {
            assertThat(r.getContentType())
                    .isEqualTo(MediaType.ANY_TYPE);

            assertThatIOException().isThrownBy(() -> {
                try (InputStream stream = r.getBody()) {
                    ByteStreams.toByteArray(stream);
                }
            });

            assertThat(stack)
                    .isEmpty();
        }
    }

    @Test
    public void testFailingOnRead(@TempDir Path temp) throws IOException {
        IOSupplier<InputStream> failingOnRead = () -> new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }
        };

        Deque<Path> stack = new LinkedList<>();
        DumpingClient x = new DumpingClient(temp, MockedClient.ofBody(failingOnRead), stack::add);

        try (HttpResponse r = x.send(request)) {
            assertThat(r.getContentType())
                    .isEqualTo(MediaType.ANY_TYPE);

            assertThatIOException().isThrownBy(() -> {
                try (InputStream stream = r.getBody()) {
                    ByteStreams.toByteArray(stream);
                }
            });

            assertThat(stack)
                    .hasSize(1)
                    .element(0, PATH)
                    .exists()
                    .isEmptyFile();
        }
    }

    private final HttpRequest request = HttpRequest
            .builder()
            .query(Parser.onURL().parseValue("http://localhost").orElseThrow(RuntimeException::new))
            .build();

    @lombok.AllArgsConstructor(staticName = "of")
    private static final class MockedClient implements HttpClient {

        public static MockedClient ofBody(IOSupplier<InputStream> body) {
            return of(() -> MockedResponse.ofBody(body));
        }

        @lombok.NonNull
        private final IOSupplier<MockedResponse> response;

        @Override
        public @NonNull HttpResponse send(@NonNull HttpRequest httpRequest) throws IOException {
            return response.getWithIO();
        }
    }

    @lombok.Builder
    public static final class MockedResponse implements HttpResponse {

        public static MockedResponse ofBody(IOSupplier<InputStream> body) {
            return builder().body(body).build();
        }

        @lombok.Builder.Default
        private final IOSupplier<MediaType> mediaType = IOSupplier.of(MediaType.ANY_TYPE);

        @lombok.Builder.Default
        private final IOSupplier<InputStream> body = IOSupplier.of(EmptyInputStream.INSTANCE);

        @lombok.Builder.Default
        private final IORunnable onClose = IORunnable.noOp();

        @Override
        public @NonNull MediaType getContentType() throws IOException {
            return mediaType.getWithIO();
        }

        @Override
        public @NonNull InputStream getBody() throws IOException {
            return body.getWithIO();
        }

        @Override
        public void close() throws IOException {
            onClose.runWithIO();
        }
    }
}
