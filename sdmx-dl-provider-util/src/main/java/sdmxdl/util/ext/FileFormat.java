package sdmxdl.util.ext;

import nbbrd.design.MightBePromoted;
import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@lombok.Value
public class FileFormat<T> {

    @lombok.NonNull
    FileParser<T> parser;

    @lombok.NonNull
    FileFormatter<T> formatter;

    @lombok.NonNull
    String fileExtension;

    public static <T, F extends FileParser<T> & FileFormatter<T>> @NonNull FileFormat<T> of(@NonNull F fileFormat, @NonNull String fileExtension) {
        return new FileFormat<>(fileFormat, fileFormat, fileExtension);
    }

    public static <T> @NonNull FileFormat<T> noOp() {
        return new FileFormat<>(
                noOpParser(),
                noOpFormatter(),
                ""
        );
    }

    public static <T> @NonNull FileFormat<T> gzip(@NonNull FileFormat<T> delegate) {
        return new FileFormat<>(
                gzipParser(delegate.getParser()),
                gzipFormatter(delegate.getFormatter()),
                delegate.getFileExtension() + ".gz"
        );
    }

    @MightBePromoted
    private static <T> @NonNull FileParser<T> noOpParser() {
        return (resource) -> {
            throw new IOException("Cannot parse stream");
        };
    }

    @MightBePromoted
    private static <T> @NonNull FileFormatter<T> noOpFormatter() {
        return (value, resource) -> {
            throw new IOException("Cannot format stream");
        };
    }

    @MightBePromoted
    private static <T> @NonNull FileParser<T> gzipParser(@NonNull FileParser<T> delegate) {
        return (resource) -> {
            try (InputStream gzip = new GZIPInputStream(resource)) {
                return delegate.parseStream(gzip);
            }
        };
    }

    @MightBePromoted
    private static <T> @NonNull FileFormatter<T> gzipFormatter(@NonNull FileFormatter<T> delegate) {
        return (value, resource) -> {
            try (OutputStream gzip = new GZIPOutputStream(resource)) {
                delegate.formatStream(value, gzip);
            }
        };
    }
}
