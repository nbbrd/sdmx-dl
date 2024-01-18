package internal.sdmxdl.format;

import lombok.NonNull;
import sdmxdl.format.spi.FileFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public final class NoOpFileFormat<T> implements FileFormat<T> {

    @Override
    public boolean isParsingSupported() {
        return false;
    }

    @Override
    public @NonNull T parsePath(@NonNull Path source) throws IOException {
        throw new IOException("Cannot parse file");
    }

    @Override
    public @NonNull T parseStream(@NonNull InputStream resource) throws IOException {
        throw new IOException("Cannot parse stream");
    }

    @Override
    public boolean isFormattingSupported() {
        return false;
    }

    @Override
    public void formatPath(@NonNull T value, @NonNull Path target) throws IOException {
        throw new IOException("Cannot format file");
    }

    @Override
    public void formatStream(@NonNull T value, @NonNull OutputStream resource) throws IOException {
        throw new IOException("Cannot format stream");
    }

    @Override
    public @NonNull String getFileExtension() {
        return ".null";
    }
}
