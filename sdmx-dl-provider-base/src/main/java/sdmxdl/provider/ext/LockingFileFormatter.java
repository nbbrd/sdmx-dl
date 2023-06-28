package sdmxdl.provider.ext;

import lombok.NonNull;
import nbbrd.io.FileFormatter;
import nbbrd.io.function.IOSupplier;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@lombok.RequiredArgsConstructor
final class LockingFileFormatter<T> implements FileFormatter<T> {

    private final @NonNull FileFormatter<T> delegate;

    @Override
    public void formatFile(@NonNull T value, @NonNull File target) throws IOException {
        try (FileOutputStream stream = new FileOutputStream(target)) {
            try (FileLock lock = stream.getChannel().lock()) {
                delegate.formatStream(value, new UnclosableOutputStream(stream));
            }
        }
    }

    @Override
    public void formatPath(@NonNull T value, @NonNull Path target) throws IOException {
        try (FileChannel channel = FileChannel.open(target, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            try (FileLock lock = channel.lock()) {
                delegate.formatStream(value, new UnclosableOutputStream(Channels.newOutputStream(channel)));
            }
        }
    }

    @Override
    public void formatStream(@NonNull T value, @NonNull IOSupplier<? extends OutputStream> target) throws IOException {
        delegate.formatStream(value, target);
    }

    @Override
    public void formatStream(@NonNull T value, @NonNull OutputStream resource) throws IOException {
        delegate.formatStream(value, resource);
    }

    @lombok.AllArgsConstructor
    private static final class UnclosableOutputStream extends OutputStream {

        @lombok.experimental.Delegate(excludes = Closeable.class)
        private final OutputStream delegate;

        @Override
        public void close() throws IOException {
            flush();
            super.close();
        }
    }
}
