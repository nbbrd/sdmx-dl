package sdmxdl.format;

import nbbrd.io.text.Parser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.format.MemCachingSupport.builder;

public class MemCachingSupportTest {

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
                return (MemCache<?>) z.getReaderCache(FILE_SOURCE, null, null);
            }
        },
        DRIVER {
            @Override
            MemCache<?> f(MemCachingSupport z) {
                return (MemCache<?>) z.getDriverCache(WEB_SOURCE, null, null);
            }
        },
        MONITOR {
            @Override
            MemCache<?> f(MemCachingSupport z) {
                return (MemCache<?>) z.getMonitorCache(WEB_SOURCE, null, null);
            }
        };

        abstract MemCache<?> f(MemCachingSupport z);
    }

    private final static SdmxFileSource FILE_SOURCE = SdmxFileSource.builder().data(new File("")).build();

    private final static SdmxWebSource WEB_SOURCE = SdmxWebSource.builder().id("").driver("").endpoint(Parser.onURI().parseValue("http://localhost").orElseThrow(RuntimeException::new)).build();
}
