package internal.util.http.ext;

import internal.util.http.HttpClient;
import internal.util.http.HttpRequest;
import internal.util.http.HttpResponse;
import lombok.NonNull;
import nbbrd.io.Resource;
import sdmxdl.format.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

@lombok.AllArgsConstructor
public final class DumpingClient implements HttpClient {

    @lombok.NonNull
    private final Path folder;

    @lombok.NonNull
    private final HttpClient delegate;

    @lombok.NonNull
    private final Consumer<? super Path> onDump;

    @Override
    public @NonNull HttpResponse requestGET(@NonNull HttpRequest request) throws IOException {
        return new DumpingResponse(folder, delegate.requestGET(request), onDump);
    }

    @lombok.AllArgsConstructor
    private static final class DumpingResponse implements HttpResponse {

        @lombok.NonNull
        private final Path folder;

        @lombok.NonNull
        private final HttpResponse delegate;

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
