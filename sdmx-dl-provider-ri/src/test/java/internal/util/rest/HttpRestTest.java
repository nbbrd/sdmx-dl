package internal.util.rest;

import nbbrd.io.function.IORunnable;
import nbbrd.io.function.IOSupplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;
import wiremock.com.google.common.io.ByteStreams;
import wiremock.org.apache.http.impl.io.EmptyInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class HttpRestTest {

    @Test
    public void testDumpingClient() throws IOException {
        URL url = new URL("http://localhost");
        List<MediaType> mediaTypes = Collections.emptyList();
        String langs = "";

        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
        Deque<Path> stack = new LinkedList<>();

        assertThat(new HttpRest.DumpingClient(MockedClient.of(), stack::add))
                .satisfies(o -> {
                    assertThatNullPointerException()
                            .isThrownBy(() -> o.requestGET(null, mediaTypes, langs));

                    assertThatNullPointerException()
                            .isThrownBy(() -> o.requestGET(url, null, langs));

                    assertThatNullPointerException()
                            .isThrownBy(() -> o.requestGET(url, mediaTypes, null));
                });

        HttpRest.DumpingClient empty = new HttpRest.DumpingClient(MockedClient.of(), stack::add);
        try (HttpRest.Response r = empty.requestGET(url, mediaTypes, langs)) {
            assertThat(r.getContentType()).isEqualTo(MediaType.ANY_TYPE);
            assertThat(r.getBody()).isEmpty();
            assertThat(stack.removeLast()).exists().isEmptyFile();
        }

        IOSupplier<MockedResponse> nonEmpty = () -> MockedResponse
                .builder()
                .body(() -> new ByteArrayInputStream(bytes))
                .build();
        try (HttpRest.Response r = new HttpRest.DumpingClient(MockedClient.of(nonEmpty), stack::add).requestGET(url, mediaTypes, langs)) {
            assertThat(r.getBody()).hasBinaryContent(bytes);
            assertThat(stack.removeLast()).exists().hasBinaryContent(bytes);
        }

        IOSupplier<MockedResponse> failing1 = () -> MockedResponse
                .builder()
                .body(() -> {
                    throw new IOException("boom");
                })
                .build();
        try (HttpRest.Response r = new HttpRest.DumpingClient(MockedClient.of(failing1), stack::add).requestGET(url, mediaTypes, langs)) {
            assertThatIOException().isThrownBy(() -> ByteStreams.toByteArray(r.getBody()));
            assertThat(stack).isEmpty();
        }

        IOSupplier<MockedResponse> failing2 = () -> MockedResponse
                .builder()
                .body(() -> new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw new IOException("boom");
                    }
                })
                .build();
        try (HttpRest.Response r = new HttpRest.DumpingClient(MockedClient.of(failing2), stack::add).requestGET(url, mediaTypes, langs)) {
            assertThatIOException().isThrownBy(() -> ByteStreams.toByteArray(r.getBody()));
            assertThat(stack.removeLast()).exists().isEmptyFile();
        }
    }

    @lombok.AllArgsConstructor(staticName = "of")
    private static final class MockedClient implements HttpRest.Client {

        public static MockedClient of() {
            return of(() -> MockedResponse.builder().build());
        }

        @lombok.NonNull
        private final IOSupplier<MockedResponse> response;

        @Override
        public HttpRest.@NonNull Response requestGET(@NonNull URL query, @NonNull List<MediaType> mediaTypes, @NonNull String langs) throws IOException {
            Objects.requireNonNull(query);
            Objects.requireNonNull(mediaTypes);
            Objects.requireNonNull(langs);
            return response.getWithIO();
        }
    }

    @lombok.Builder
    private static final class MockedResponse implements HttpRest.Response {

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
