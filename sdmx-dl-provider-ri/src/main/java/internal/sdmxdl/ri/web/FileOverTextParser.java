package internal.sdmxdl.ri.web;

import nbbrd.design.MightBePromoted;
import nbbrd.design.VisibleForTesting;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOSupplier;
import nbbrd.io.text.TextParser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Function;

@MightBePromoted
@VisibleForTesting
@lombok.AllArgsConstructor
final class FileOverTextParser<T> implements FileParser<T> {

    @lombok.NonNull
    private final TextParser<T> delegate;

    @lombok.NonNull
    private final Charset charset;

    @Override
    public @NonNull T parseFile(@NonNull File source) throws IOException {
        return delegate.parseFile(source, charset);
    }

    @Override
    public @NonNull T parsePath(@NonNull Path source) throws IOException {
        return delegate.parsePath(source, charset);
    }

    @Override
    public @NonNull T parseResource(@NonNull Class<?> type, @NonNull String name) throws IOException {
        return delegate.parseResource(type, name, charset);
    }

    @Override
    public @NonNull T parseStream(IOSupplier<? extends InputStream> source) throws IOException {
        return delegate.parseStream(source, charset);
    }

    @Override
    public @NonNull T parseStream(@NonNull InputStream inputStream) throws IOException {
        return delegate.parseStream(inputStream, charset);
    }

    @Override
    public @NonNull <V> FileParser<V> andThen(@NonNull Function<? super T, ? extends V> after) {
        return new FileOverTextParser<>(delegate.andThen(after), charset);
    }
}
