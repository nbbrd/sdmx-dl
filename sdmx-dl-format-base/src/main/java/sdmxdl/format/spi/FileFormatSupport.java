package sdmxdl.format.spi;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import sdmxdl.format.ServiceSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

@ServiceSupport
@lombok.Builder
public final class FileFormatSupport<T> implements FileFormat<T> {

    @lombok.Builder.Default
    private final boolean parsing = false;

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileParser<T> parser = noOpParser();

    @lombok.Builder.Default
    private final boolean formatting = false;

    @lombok.NonNull
    @lombok.Builder.Default
    private final FileFormatter<T> formatter = noOpFormatter();

    @lombok.NonNull
    private final String extension;

    @Override
    public boolean isParsingSupported() {
        return parsing;
    }

    @Override
    public @NonNull T parsePath(@NonNull Path source) throws IOException {
        return parser.parsePath(source);
    }

    @Override
    public @NonNull T parseStream(@NonNull InputStream resource) throws IOException {
        return parser.parseStream(resource);
    }

    @Override
    public boolean isFormattingSupported() {
        return formatting;
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

    public static @NonNull <T> Builder<T> builder(@NonNull Class<T> ignore) {
        return new Builder<T>();
    }

    @StaticFactoryMethod
    public static <T> @NonNull FileFormatSupport<T> wrap(@NonNull FileFormat<T> delegate) {
        return delegate instanceof FileFormatSupport
                ? (FileFormatSupport<T>) delegate
                : new FileFormatSupport<>(
                delegate.isParsingSupported(), FileParser.onParsingStream(delegate::parseStream),
                delegate.isFormattingSupported(), FileFormatter.onFormattingStream(delegate::formatStream),
                delegate.getFileExtension()
        );
    }

    @StaticFactoryMethod
    public static <T> @NonNull FileFormatSupport<T> gzip(@NonNull FileFormatSupport<T> delegate) {
        return new FileFormatSupport<>(
                delegate.isParsingSupported(), FileParser.onParsingGzip(delegate.parser),
                delegate.isFormattingSupported(), FileFormatter.onFormattingGzip(delegate.formatter),
                delegate.getFileExtension() + ".gz"
        );
    }

    @StaticFactoryMethod
    public static <T> @NonNull FileFormatSupport<T> lock(@NonNull FileFormatSupport<T> delegate) {
        return new FileFormatSupport<>(
                delegate.isParsingSupported(), FileParser.onParsingLock(delegate.parser),
                delegate.isFormattingSupported(), FileFormatter.onFormattingLock(delegate.formatter),
                delegate.getFileExtension()
        );
    }

    @MightBePromoted
    private static <T> @NonNull FileParser<T> noOpParser() {
        return FileParser.onParsingStream((resource) -> {
            throw new IOException("Cannot parse stream");
        });
    }

    @MightBePromoted
    private static <T> @NonNull FileFormatter<T> noOpFormatter() {
        return FileFormatter.onFormattingStream((value, resource) -> {
            throw new IOException("Cannot format stream");
        });
    }
}
