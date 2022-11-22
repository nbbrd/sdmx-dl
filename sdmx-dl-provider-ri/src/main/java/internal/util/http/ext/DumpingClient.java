package internal.util.http.ext;

import internal.util.http.HttpClient;
import internal.util.http.HttpRequest;
import internal.util.http.HttpResponse;
import lombok.NonNull;
import nbbrd.io.Resource;
import nbbrd.io.net.MediaType;

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
    public @NonNull HttpResponse send(@NonNull HttpRequest request) throws IOException {
        String prefix = "http_" + getUniqueTimeStamp();
        try {
            return new DumpingResponse(folder, delegate.send(request), onDump, prefix);
        } finally {
            dumpRequestBody(request, prefix);
        }
    }

    private void dumpRequestBody(HttpRequest request, String prefix) throws IOException {
        if (request.getBody() != null) {
            Files.createDirectories(folder);
            Path requestDump = folder.resolve(prefix + "_request.tmp");
            onDump.accept(requestDump);
            Files.write(requestDump, request.getBody());
        }
    }

    @lombok.AllArgsConstructor
    private static final class DumpingResponse implements HttpResponse {

        @lombok.NonNull
        private final Path folder;

        @lombok.NonNull
        private final HttpResponse delegate;

        @lombok.NonNull
        private final Consumer<? super Path> onDump;

        @lombok.NonNull
        private final String prefix;

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
            Path responseDump = folder.resolve(prefix + "_response.tmp");
            onDump.accept(responseDump);
            return Files.newOutputStream(responseDump);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    private static synchronized String getUniqueTimeStamp() {
        return String.valueOf(System.nanoTime());
    }
}
