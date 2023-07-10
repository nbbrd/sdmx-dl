package sdmxdl.format;

import nbbrd.io.text.Parser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.web.SdmxWebSource;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.format.MemCachingSupport.builder;

public class MemCachingSupportTest {

    @ParameterizedTest
    @EnumSource(Extractor.class)
    public void testFactories(Extractor extractor) {
        assertThat(builder().id("").build())
                .satisfies(x -> assertThat(extractor.f(x).getRepositories()).isNotSameAs(extractor.f(x).getRepositories()).isInstanceOf(HashMap.class))
                .satisfies(x -> assertThat(extractor.f(x).getWebMonitors()).isNotSameAs(extractor.f(x).getWebMonitors()).isInstanceOf(HashMap.class));

        assertThat(builder().id("").repositories(ConcurrentHashMap::new).webMonitors(ConcurrentSkipListMap::new).build())
                .satisfies(x -> assertThat(extractor.f(x).getRepositories()).isNotSameAs(extractor.f(x).getRepositories()).isInstanceOf(ConcurrentHashMap.class))
                .satisfies(x -> assertThat(extractor.f(x).getWebMonitors()).isNotSameAs(extractor.f(x).getWebMonitors()).isInstanceOf(ConcurrentSkipListMap.class));

        assertThat(builder().id("").repositoriesOf(new ConcurrentHashMap<>()).webMonitorsOf(new ConcurrentSkipListMap<>()).build())
                .satisfies(x -> assertThat(extractor.f(x).getRepositories()).isSameAs(extractor.f(x).getRepositories()).isInstanceOf(ConcurrentHashMap.class))
                .satisfies(x -> assertThat(extractor.f(x).getWebMonitors()).isSameAs(extractor.f(x).getWebMonitors()).isInstanceOf(ConcurrentSkipListMap.class));
    }

    enum Extractor {

        FILE {
            @Override
            MemCache f(MemCachingSupport z) {
                return (MemCache) z.getFileCache(FILE_SOURCE, null, null);
            }
        }, WEB {
            @Override
            MemCache f(MemCachingSupport z) {
                return (MemCache) z.getWebCache(WEB_SOURCE, null, null);
            }
        };

        abstract MemCache f(MemCachingSupport z);
    }

    private final static SdmxFileSource FILE_SOURCE = SdmxFileSource.builder().data(new File("")).build();

    private final static SdmxWebSource WEB_SOURCE = SdmxWebSource.builder().id("").driver("").endpoint(Parser.onURI().parseValue("http://localhost").orElseThrow(RuntimeException::new)).build();
}
