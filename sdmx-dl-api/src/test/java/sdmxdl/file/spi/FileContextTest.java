package sdmxdl.file.spi;

import org.junit.jupiter.api.Test;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.file.FileSource;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class FileContextTest {

    private static final FileSource SOURCE = FileSource.builder().data(Paths.get("data.xml").toFile()).build();

    @Test
    public void testDefaults() {
        FileContext context = FileContext.builder().build();

        assertThat(context.getCaching()).isEqualTo(FileCaching.noOp());
        assertThat(context.getOnEvent()).isNull();
        assertThat(context.getOnError()).isNull();
    }

    @Test
    public void testGetEventListener() {
        assertThat(FileContext.builder().build().getEventListener(SOURCE))
                .isNull();

        EventListener listener = (marker, message) -> {
        };
        assertThat(FileContext.builder().onEvent(x -> listener).build().getEventListener(SOURCE))
                .isSameAs(listener);
    }

    @Test
    public void testGetErrorListener() {
        assertThat(FileContext.builder().build().getErrorListener(SOURCE))
                .isNull();

        ErrorListener listener = (marker, message, error) -> {
        };
        assertThat(FileContext.builder().onError(x -> listener).build().getErrorListener(SOURCE))
                .isSameAs(listener);
    }

    @Test
    public void testGetReaderCache() {
        assertThat(FileContext.builder().build().getReaderCache(SOURCE)).isNotNull();
    }
}

