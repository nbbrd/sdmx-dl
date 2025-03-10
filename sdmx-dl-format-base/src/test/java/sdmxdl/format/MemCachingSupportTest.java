package sdmxdl.format;

import nbbrd.io.text.Parser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import sdmxdl.file.FileSource;
import sdmxdl.web.WebSource;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.format.MemCachingSupport.builder;
import static tests.sdmxdl.file.spi.FileCachingAssert.assertFileCompliance;
import static tests.sdmxdl.web.spi.WebCachingAssert.assertWebCompliance;

public class MemCachingSupportTest {

    @Test
    public void testCompliance() {
        MemCachingSupport x = MemCachingSupport.builder().id("COMPLIANCE").build();
        assertFileCompliance(x);
        assertWebCompliance(x);
    }

    @ParameterizedTest
    @EnumSource(Extractor.class)
    public void testFactories(Extractor extractor) {
        assertThat(builder().id("").build())
                .satisfies(x -> assertThat(extractor.f(x).getMap()).isNotSameAs(extractor.f(x).getMap()).isInstanceOf(HashMap.class));

        assertThat(builder().id("").repositories(ConcurrentHashMap::new).webMonitors(ConcurrentHashMap::new).build())
                .satisfies(x -> assertThat(extractor.f(x).getMap()).isNotSameAs(extractor.f(x).getMap()).isInstanceOf(ConcurrentHashMap.class));

        assertThat(builder().id("").repositoriesOf(new ConcurrentHashMap<>()).webMonitorsOf(new ConcurrentHashMap<>()).build())
                .satisfies(x -> assertThat(extractor.f(x).getMap()).isSameAs(extractor.f(x).getMap()).isInstanceOf(ConcurrentHashMap.class));
    }

    enum Extractor {

        READER {
            @Override
            MemCache<?> f(MemCachingSupport z) {
                return (MemCache<?>) z.getReaderCache(FILE_SOURCE, emptyList(), null, null);
            }
        },
        DRIVER {
            @Override
            MemCache<?> f(MemCachingSupport z) {
                return (MemCache<?>) z.getDriverCache(WEB_SOURCE, emptyList(), null, null);
            }
        },
        MONITOR {
            @Override
            MemCache<?> f(MemCachingSupport z) {
                return (MemCache<?>) z.getMonitorCache(WEB_SOURCE, emptyList(), null, null);
            }
        };

        abstract MemCache<?> f(MemCachingSupport z);
    }

    private final static FileSource FILE_SOURCE = FileSource.builder().data(Paths.get("").toFile()).build();

    private final static WebSource WEB_SOURCE = WebSource.builder().id("").driver("").endpoint(Parser.onURI().parseValue("http://localhost").orElseThrow(RuntimeException::new)).build();
}
