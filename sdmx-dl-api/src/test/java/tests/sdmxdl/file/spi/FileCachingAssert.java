package tests.sdmxdl.file.spi;

import lombok.NonNull;
import nbbrd.design.MightBeGenerated;
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import tests.sdmxdl.api.ExtensionPoint;
import tests.sdmxdl.api.TckUtil;
import tests.sdmxdl.ext.CacheAssert;

import java.io.File;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static tests.sdmxdl.api.TckUtil.SCREAMING_SNAKE_CASE;

@lombok.experimental.UtilityClass
public class FileCachingAssert {

    @MightBeGenerated
    private static final ExtensionPoint<FileCaching> EXTENSION_POINT = ExtensionPoint
            .<FileCaching>builder()
            .id(FileCaching::getFileCachingId)
            .idPattern(SCREAMING_SNAKE_CASE)
            .rank(FileCaching::getFileCachingRank)
            .rankLowerBound(FileCaching.UNKNOWN_FILE_CACHING_RANK)
            .properties(FileCaching::getFileCachingProperties)
            .propertiesPrefix(FileCaching.FILE_CACHING_PROPERTY_PREFIX)
            .build();

    @SuppressWarnings("DataFlowIssue")
    public static void assertFileCompliance(@NonNull FileCaching caching) {
        TckUtil.run(s -> EXTENSION_POINT.assertCompliance(s, caching));

        assertThatNullPointerException()
                .isThrownBy(() -> caching.getReaderCache(null, emptyList(), null, null));

        FileSource validSource = FileSource
                .builder()
                .data(new File("hello.xml"))
                .build();

        assertThatNullPointerException()
                .isThrownBy(() -> caching.getReaderCache(validSource, null, null, null));

        assertThat(caching.getReaderCache(validSource, emptyList(), null, null))
                .isNotNull()
                .satisfies(CacheAssert::assertRepositoryCompliance);
    }
}
