package _test.sdmxdl.ri;

import internal.util.http.HttpResponse;
import lombok.NonNull;
import nbbrd.io.function.IORunnable;
import nbbrd.io.function.IOSupplier;
import nbbrd.io.net.MediaType;

import java.io.IOException;
import java.io.InputStream;

@lombok.Builder(toBuilder = true)
public final class RestClientResponseMock implements HttpResponse {

    @lombok.NonNull
    @lombok.Builder.Default
    private final MediaType contentType = MediaType.ANY_TYPE;

    @lombok.NonNull
    @lombok.Builder.Default
    private final IOSupplier<InputStream> body = IOSupplier.of(null);

    @lombok.NonNull
    @lombok.Builder.Default
    private final IORunnable onClose = IORunnable.noOp();

    @Override
    public @NonNull MediaType getContentType() {
        return contentType;
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
