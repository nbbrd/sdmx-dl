package internal.util.rest;

import internal.util.rest.HttpRest.Client;
import internal.util.rest.HttpRest.Response;
import nbbrd.io.Resource;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@lombok.AllArgsConstructor
final class DumpingClient implements Client {

    @lombok.NonNull
    private final Path folder;

    @lombok.NonNull
    private final Client delegate;

    @lombok.NonNull
    private final Consumer<? super Path> onDump;

    @Override
    public @NonNull Response requestGET(@NonNull URL query, @NonNull List<MediaType> mediaTypes, @NonNull String langs) throws IOException {
        return new DumpingResponse(folder, delegate.requestGET(query, mediaTypes, langs), onDump);
    }

    @lombok.AllArgsConstructor
    private static final class DumpingResponse implements Response {

        @lombok.NonNull
        private final Path folder;

        @lombok.NonNull
        private final Response delegate;

        @lombok.NonNull
        private final Consumer<? super Path> onDump;

        @Override
        public @NonNull MediaType getContentType() throws IOException {
            return delegate.getContentType();
        }

        @Override
        public @NonNull InputStream getBody() throws IOException {
            InputStream inputStream = delegate.getBody();
            try {
                OutputStream outputStream = getDumpStream();
                return new TeeInputStream(inputStream, outputStream);
            } catch (IOException ex) {
                Resource.ensureClosed(ex, inputStream);
                throw ex;
            }
        }

        private OutputStream getDumpStream() throws IOException {
            Files.createDirectories(folder);
            Path dump = Files.createTempFile(folder, "body", ".tmp");
            onDump.accept(dump);
            return Files.newOutputStream(dump);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
