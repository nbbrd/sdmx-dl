package tests.sdmxdl.file.spi;

import lombok.NonNull;
import sdmxdl.file.FileSource;
import sdmxdl.file.spi.FileCaching;
import tests.sdmxdl.ext.CacheAssert;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static sdmxdl.file.spi.FileCaching.FILE_CACHING_PROPERTY_PREFIX;
import static tests.sdmxdl.api.TckUtil.SCREAMING_SNAKE_CASE;
import static tests.sdmxdl.api.TckUtil.startingWith;

@lombok.experimental.UtilityClass
public class FileCachingAssert {

    @SuppressWarnings("DataFlowIssue")
    public static void assertFileCompliance(@NonNull FileCaching caching) {
        assertThat(caching.getFileCachingId())
                .containsPattern(SCREAMING_SNAKE_CASE)
                .isNotBlank();

        assertThat(caching.getFileCachingProperties())
                .are(startingWith(FILE_CACHING_PROPERTY_PREFIX))
                .doesNotHaveDuplicates();

        assertThatNullPointerException()
                .isThrownBy(() -> caching.getReaderCache(null, null, null));

        FileSource validSource = FileSource
                .builder()
                .data(new File("hello.xml"))
                .build();

        assertThat(caching.getReaderCache(validSource, null, null))
                .isNotNull()
                .satisfies(CacheAssert::assertRepositoryCompliance);
    }
}
