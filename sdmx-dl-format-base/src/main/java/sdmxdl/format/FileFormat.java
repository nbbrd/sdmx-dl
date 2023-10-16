package sdmxdl.format;

import lombok.NonNull;
import nbbrd.design.MightBePromoted;
import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;

import java.io.IOException;

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
                FileParser.onParsingGzip(delegate.getParser()),
                FileFormatter.onFormattingGzip(delegate.getFormatter()),
                delegate.getFileExtension() + ".gz"
        );
    }

    public static <T> @NonNull FileFormat<T> lock(@NonNull FileFormat<T> delegate) {
        return new FileFormat<>(
                FileParser.onParsingLock(delegate.getParser()),
                FileFormatter.onFormattingLock(delegate.getFormatter()),
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
