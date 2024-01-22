package sdmxdl.ext;

import internal.sdmxdl.NoOpFileFormat;
import lombok.NonNull;
import nbbrd.design.StaticFactoryMethod;
import nbbrd.design.ThreadSafe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

@ThreadSafe
public interface FileFormat<T> {

    @NonNull T parsePath(@NonNull Path source) throws IOException;

    @NonNull T parseStream(@NonNull InputStream resource) throws IOException;

    void formatPath(@NonNull T value, @NonNull Path target) throws IOException;

    void formatStream(@NonNull T value, @NonNull OutputStream resource) throws IOException;

    @NonNull String getFileExtension();

    @StaticFactoryMethod
    static <T> @NonNull FileFormat<T> noOp() {
        return new NoOpFileFormat<>();
    }
}
