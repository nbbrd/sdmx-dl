package sdmxdl.util.ext;

import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.repo.SdmxRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public interface Serializer {

    @NonNull
    SdmxRepository load(@NonNull InputStream stream) throws IOException;

    void store(@NonNull OutputStream stream, @NonNull SdmxRepository entry) throws IOException;

    static @NonNull Serializer of(@NonNull FileParser<SdmxRepository> parser, @NonNull FileFormatter<SdmxRepository> formatter) {
        Objects.requireNonNull(parser);
        Objects.requireNonNull(formatter);
        return new Serializer() {
            @Override
            public @NonNull SdmxRepository load(@NonNull InputStream stream) throws IOException {
                return parser.parseStream(stream);
            }

            @Override
            public void store(@NonNull OutputStream stream, @NonNull SdmxRepository entry) throws IOException {
                formatter.formatStream(entry, stream);
            }
        };
    }

    @NonNull
    static Serializer noOp() {
        return new Serializer() {
            @Override
            public SdmxRepository load(@NonNull InputStream stream) {
                return SdmxRepository.builder().build();
            }

            @Override
            public void store(@NonNull OutputStream stream, SdmxRepository entry) {
            }
        };
    }

    @NonNull
    static Serializer gzip(@NonNull Serializer delegate) {
        return new Serializer() {
            @Override
            public SdmxRepository load(@NonNull InputStream stream) throws IOException {
                try (InputStream gzip = new GZIPInputStream(stream)) {
                    return delegate.load(gzip);
                }
            }

            @Override
            public void store(@NonNull OutputStream stream, SdmxRepository entry) throws IOException {
                try (OutputStream gzip = new GZIPOutputStream(stream)) {
                    delegate.store(gzip, entry);
                }
            }
        };
    }
}
