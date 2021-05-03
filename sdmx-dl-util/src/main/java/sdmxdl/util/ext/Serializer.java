package sdmxdl.util.ext;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public interface Serializer {

    @NonNull
    ExpiringRepository load(@NonNull InputStream stream) throws IOException;

    void store(@NonNull OutputStream stream, @NonNull ExpiringRepository entry) throws IOException;

    @NonNull
    static Serializer noOp() {
        return new Serializer() {
            @Override
            public ExpiringRepository load(@NonNull InputStream stream) {
                return null;
            }

            @Override
            public void store(@NonNull OutputStream stream, ExpiringRepository entry) {
            }
        };
    }

    @NonNull
    static Serializer gzip(@NonNull Serializer delegate) {
        return new Serializer() {
            @Override
            public ExpiringRepository load(@NonNull InputStream stream) throws IOException {
                try (InputStream gzip = new GZIPInputStream(stream)) {
                    return delegate.load(gzip);
                }
            }

            @Override
            public void store(@NonNull OutputStream stream, ExpiringRepository entry) throws IOException {
                try (OutputStream gzip = new GZIPOutputStream(stream)) {
                    delegate.store(gzip, entry);
                }
            }
        };
    }
}
