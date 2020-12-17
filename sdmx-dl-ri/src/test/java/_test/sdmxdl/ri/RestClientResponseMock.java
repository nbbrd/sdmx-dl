package _test.sdmxdl.ri;

import internal.util.rest.HttpRest;
import nbbrd.io.function.IORunnable;
import nbbrd.io.function.IOSupplier;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;

@lombok.Builder(toBuilder = true)
public final class RestClientResponseMock implements HttpRest.Response {

    @lombok.NonNull
    @lombok.Builder.Default
    private final String contentType = "";

    @lombok.NonNull
    @lombok.Builder.Default
    private final IOSupplier<InputStream> body = IOSupplier.of(null);

    @lombok.NonNull
    @lombok.Builder.Default
    private final IORunnable onClose = IORunnable.noOp();

    @Override
    public @NonNull String getContentType() throws IOException {
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
