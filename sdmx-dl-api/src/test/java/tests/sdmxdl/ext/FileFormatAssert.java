package tests.sdmxdl.ext;

import org.assertj.core.api.SoftAssertions;
import sdmxdl.ext.FileFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@lombok.experimental.UtilityClass
public class FileFormatAssert {

    @SuppressWarnings("DataFlowIssue")
    public <T> void assertCompliance(SoftAssertions s, FileFormat<T> fileFormat, T data, boolean supported) throws IOException {
        s.assertThat(fileFormat.getFileExtension())
                .isNotNull();

        Path tmpFile = Files.createTempFile("store", "load");
        try {
            s.assertThatNullPointerException()
                    .isThrownBy(() -> fileFormat.formatPath(null, tmpFile));
            s.assertThatNullPointerException()
                    .isThrownBy(() -> fileFormat.formatPath(data, null));
            s.assertThatNullPointerException()
                    .isThrownBy(() -> fileFormat.parsePath(null));

            s.assertThatNullPointerException()
                    .isThrownBy(() -> fileFormat.formatStream(null, new ByteArrayOutputStream()));
            s.assertThatNullPointerException()
                    .isThrownBy(() -> fileFormat.formatStream(data, null));
            s.assertThatNullPointerException()
                    .isThrownBy(() -> fileFormat.parseStream(null));

            if (supported) {
                s.assertThat(storeLoadPath(fileFormat, data, tmpFile))
                        .isEqualTo(data)
                        .isNotSameAs(data);

                s.assertThat(storeLoadStream(fileFormat, data))
                        .isEqualTo(data)
                        .isNotSameAs(data);
            } else {
                s.assertThatIOException()
                        .isThrownBy(() -> storeLoadPath(fileFormat, data, tmpFile));

                s.assertThatIOException()
                        .isThrownBy(() -> storeLoadStream(fileFormat, data));
            }
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }

    private static <T> T storeLoadPath(FileFormat<T> fileFormat, T data, Path tmpFile) throws IOException {
        fileFormat.formatPath(data, tmpFile);
        return fileFormat.parsePath(tmpFile);
    }

    private static <T> T storeLoadStream(FileFormat<T> fileFormat, T data) throws IOException {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            fileFormat.formatStream(data, output);
            try (ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray())) {
                return fileFormat.parseStream(input);
            }
        }
    }
}
