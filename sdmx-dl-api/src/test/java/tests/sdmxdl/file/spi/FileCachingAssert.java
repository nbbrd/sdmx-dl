package tests.sdmxdl.file.spi;

import internal.sdmxdl.file.spi.FileCachingLoader;
import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;
import tests.sdmxdl.ext.CacheAssert;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@lombok.experimental.UtilityClass
public class FileCachingAssert {

    @MightBeGenerated
    private static final ExtensionPoint<FileCaching> EXTENSION_POINT = ExtensionPoint
            .<FileCaching>builder()
            .id(FileCaching::getFileCachingId)
            .idPattern(FileCachingLoader.ID_PATTERN)
            .rank(FileCaching::getFileCachingRank)
            .rankLowerBound(FileCaching.UNKNOWN_FILE_CACHING_RANK)
            .properties(FileCaching::getFileCachingPropertyNames)
            .propertiesPrefix(FileCaching.FILE_CACHING_PROPERTY_PREFIX)
            .build();

    @SuppressWarnings("DataFlowIssue")
    public static void assertFileCompliance(@NonNull FileCaching caching) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, caching));

        assertThatNullPointerException()
                .isThrownBy(() -> caching.getReaderCache(null, null, null));

        FileSource validSource = FileSource
                .builder()
                .data(Paths.get("hello.xml").toFile())
                .build();

        assertThat(caching.getReaderCache(validSource, null, null))
                .isNotNull()
                .satisfies(CacheAssert::assertRepositoryCompliance);
    }
}
