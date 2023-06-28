package sdmxdl.provider.ext;

import lombok.NonNull;
import nbbrd.io.FileParser;
import nbbrd.io.function.IOSupplier;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@lombok.RequiredArgsConstructor
final class LockingFileParser<T> implements FileParser<T> {

    private final @NonNull FileParser<T> delegate;

    @Override
    public @NonNull T parseFile(@NonNull File source) throws IOException {
        try (FileInputStream stream = new FileInputStream(source)) {
            try (FileLock lock = stream.getChannel().lock(0, Long.MAX_VALUE, true)) {
                return delegate.parseStream(new UnclosableInputStream(stream));
            }
        }
    }

    @Override
    public @NonNull T parsePath(@NonNull Path source) throws IOException {
        try (FileChannel channel = FileChannel.open(source, StandardOpenOption.READ)) {
            try (FileLock lock = channel.lock(0, Long.MAX_VALUE, true)) {
                return delegate.parseStream(new UnclosableInputStream(Channels.newInputStream(channel)));
            }
        }
    }

    @Override
    public @NonNull T parseResource(@NonNull Class<?> type, @NonNull String name) throws IOException {
        return delegate.parseResource(type, name);
    }

    @Override
    public @NonNull T parseStream(@NonNull IOSupplier<? extends InputStream> source) throws IOException {
        return delegate.parseStream(source);
    }

    @Override
    public @NonNull T parseStream(@NonNull InputStream resource) throws IOException {
        return delegate.parseStream(resource);
    }

    @lombok.AllArgsConstructor
    private static final class UnclosableInputStream extends InputStream {

        @lombok.experimental.Delegate(excludes = Closeable.class)
        private final InputStream delegate;
    }
}
