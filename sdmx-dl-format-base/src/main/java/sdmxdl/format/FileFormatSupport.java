package sdmxdl.format;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import sdmxdl.HasPersistence;
import sdmxdl.ext.FileFormat;
import sdmxdl.format.design.ServiceSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import static nbbrd.io.FileFormatter.*;
import static nbbrd.io.FileParser.*;

@ServiceSupport
@lombok.Builder
public final class FileFormatSupport<T extends HasPersistence> implements FileFormat<T> {

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileParser<T> parser = noOpParser();

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileFormatter<T> formatter = noOpFormatter();

    @lombok.NonNull
    private final String extension;

    @Override
    public @NonNull T parsePath(@NonNull Path source) throws IOException {
        return parser.parsePath(source);
    }

    @Override
    public @NonNull T parseStream(@NonNull InputStream resource) throws IOException {
        return parser.parseStream(resource);
    }

    @Override
    public void formatPath(@NonNull T value, @NonNull Path target) throws IOException {
        formatter.formatPath(value, target);
    }

    @Override
    public void formatStream(@NonNull T value, @NonNull OutputStream resource) throws IOException {
        formatter.formatStream(value, resource);
    }

    @Override
    public @NonNull String getFileExtension() {
        return extension;
    }

    public static @NonNull <T extends HasPersistence> Builder<T> builder(@NonNull Class<T> ignore) {
        return new Builder<T>();
    }

    @StaticFactoryMethod
    public static <T extends HasPersistence> @NonNull FileFormatSupport<T> wrap(@NonNull FileFormat<T> delegate) {
        return delegate instanceof FileFormatSupport
                ? (FileFormatSupport<T>) delegate
                : new FileFormatSupport<>(
                onParsingStream(delegate::parseStream),
                onFormattingStream(delegate::formatStream),
                delegate.getFileExtension()
        );
    }

    @StaticFactoryMethod
    public static <T extends HasPersistence> @NonNull FileFormatSupport<T> gzip(@NonNull FileFormatSupport<T> delegate) {
        return new FileFormatSupport<>(
                onParsingGzip(delegate.parser),
                onFormattingGzip(delegate.formatter),
                delegate.getFileExtension() + ".gz"
        );
    }

    @StaticFactoryMethod
    public static <T extends HasPersistence> @NonNull FileFormatSupport<T> lock(@NonNull FileFormatSupport<T> delegate) {
        return new FileFormatSupport<>(
                onParsingLock(delegate.parser),
                onFormattingLock(delegate.formatter),
                delegate.getFileExtension()
        );
    }

    @MightBePromoted
    private static <T> @NonNull FileParser<T> noOpParser() {
        return onParsingStream((resource) -> {
            throw new IOException("Cannot parse stream");
        });
    }

    @MightBePromoted
    private static <T> @NonNull FileFormatter<T> noOpFormatter() {
        return onFormattingStream((value, resource) -> {
            throw new IOException("Cannot format stream");
        });
    }
}
