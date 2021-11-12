package _test.sdmxdl.ri;

import nbbrd.io.function.IOFunction;
import nbbrd.io.text.TextParser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

@lombok.experimental.UtilityClass
public final class TextParsers {

    public static <T> TextParser<T> of(IOFunction<Reader, T> delegate) {
        return new TextParser<T>() {
            @Override
            public @NonNull T parseReader(@NonNull Reader resource) throws IOException {
                return delegate.applyWithIO(resource);
            }

            @Override
            public @NonNull T parseStream(@NonNull InputStream resource, @NonNull Charset encoding) throws IOException {
                return parseReader(new InputStreamReader(resource, encoding));
            }
        };
    }
}
