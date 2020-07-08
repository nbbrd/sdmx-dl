package _test.sdmxdl.ri;

import internal.util.rest.RestClient;
import nbbrd.io.function.IORunnable;
import nbbrd.io.function.IOSupplier;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;

@lombok.Builder(builderClassName = "Builder")
public final class RestClientResponseMock implements RestClient.Response {

    @lombok.NonNull
    private final String contentType;

    @lombok.NonNull
    private final IOSupplier<InputStream> body;

    @lombok.NonNull
    private final IORunnable onClose;

    public static Builder builder() {
        return new Builder()
                .contentType("")
                .body(IOSupplier.of(null))
                .onClose(IORunnable.noOp());
    }

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
