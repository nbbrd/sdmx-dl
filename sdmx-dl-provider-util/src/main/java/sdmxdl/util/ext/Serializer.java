package sdmxdl.util.ext;

import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public interface Serializer<T> extends FileParser<T>, FileFormatter<T> {

    static <T> @NonNull Serializer<T> of(@NonNull FileParser<T> parser, @NonNull FileFormatter<T> formatter) {
        Objects.requireNonNull(parser);
        Objects.requireNonNull(formatter);
        return new Serializer<T>() {
            @Override
            public @NonNull T parseStream(@NonNull InputStream stream) throws IOException {
                return parser.parseStream(stream);
            }

            @Override
            public void formatStream(@NonNull T entry, @NonNull OutputStream stream) throws IOException {
                formatter.formatStream(entry, stream);
            }
        };
    }

    @NonNull
    static <T> Serializer<T> noOp() {
        return new Serializer<T>() {
            @Override
            public T parseStream(@NonNull InputStream stream) throws IOException {
                throw new IOException("Cannot parse stream");
            }

            @Override
            public void formatStream(T entry, @NonNull OutputStream stream) {
            }
        };
    }

    @NonNull
    static <T> Serializer<T> gzip(@NonNull Serializer<T> delegate) {
        return new Serializer<T>() {
            @Override
            public T parseStream(@NonNull InputStream stream) throws IOException {
                try (InputStream gzip = new GZIPInputStream(stream)) {
                    return delegate.parseStream(gzip);
                }
            }

            @Override
            public void formatStream(T entry, @NonNull OutputStream stream) throws IOException {
                try (OutputStream gzip = new GZIPOutputStream(stream)) {
                    delegate.formatStream(entry, gzip);
                }
            }
        };
    }
}
