/*
 * Copyright 2016 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package sdmxdl.provider.caching;

import static java.util.function.UnaryOperator.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static tests.sdmxdl.ext.CacheAssert.assertMonitorCompliance;
import static tests.sdmxdl.ext.CacheAssert.assertRepositoryCompliance;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nbbrd.io.FileFormatter;
import nbbrd.io.FileParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sdmxdl.DataRepository;
import sdmxdl.format.FileFormatSupport;
import sdmxdl.web.MonitorReports;
import tests.sdmxdl.ext.FakeClock;

/**
 * @author Philippe Charles
 */
public class DiskCacheTest {

    @Test
    public void testCompliance() {
        assertMonitorCompliance(DiskCache.<MonitorReports>builder().id("test").build());
        assertRepositoryCompliance(
                DiskCache.<DataRepository>builder().id("test").build());
    }

    @Test
    public void testGetSet(@TempDir Path temp) {
        FakeClock clock = new FakeClock();
        clock.set(1000);

        Path root = temp.resolve("testfolder");
        List<IOException> exceptions = new ArrayList<>();

        DiskCache<DataRepository> cache = DiskCache.<DataRepository>builder()
                .id("test")
                .root(root)
                .nameGenerator(identity())
                .format(newFakeFileFormat())
                .clock(clock)
                .onError((id, message, exception) -> exceptions.add(exception))
                .build();

        assertThat(root).doesNotExist();
        assertThat(cache.get("KEY1")).as("Empty directory should return null").isNull();

        DataRepository r1 = DataRepository.builder()
                .name("r1")
                .ttl(clock.instant(), Duration.ofMillis(10))
                .build();
        cache.put("KEY1", r1);
        assertThat(cache.getFile("KEY1")).exists().hasContent("r1");

        assertThat(cache.get("KEY2")).as("Non-existing key should return null").isNull();

        clock.plus(9);
        assertThat(cache.get("KEY1")).as("Non-expired key should return value").isEqualTo(r1);

        clock.plus(1);
        assertThat(cache.get("KEY1")).as("Expired key should return null").isNull();
        assertThat(cache.getFile("KEY1")).as("Expired key should be deleted").doesNotExist();

        DataRepository r1b =
                r1.toBuilder().ttl(clock.instant(), Duration.ofMillis(10)).build();
        DataRepository r2 = DataRepository.builder()
                .name("r2")
                .ttl(clock.instant(), Duration.ofMillis(10))
                .build();
        cache.put("KEY1", r1b);
        cache.put("KEY1", r2);
        assertThat(cache.getFile("KEY1")).exists().hasContent("r2");
        assertThat(cache.get("KEY1"))
                .as("Updated key should return updated value")
                .isEqualTo(r2);

        assertThat(exceptions).isEmpty();
    }

    @Test
    public void testFailedWriteKeepsPreviousValue(@TempDir Path root) throws IOException {
        List<IOException> exceptions = new ArrayList<>();

        DiskCache<DataRepository> cache = DiskCache.<DataRepository>builder()
                .id("test")
                .root(root)
                .nameGenerator(identity())
                .format(newFakeFileFormat())
                .onError((id, message, exception) -> exceptions.add(exception))
                .build();

        cache.put("KEY1", DataRepository.builder().name("r1").build());
        cache.put("KEY1", DataRepository.builder().name(FAILING_NAME).build());

        assertThat(cache.getFile("KEY1"))
                .as("Failed write should not alter previous value")
                .hasContent("r1");
        try (Stream<Path> files = Files.list(root)) {
            assertThat(files)
                    .as("Failed write should not leave temporary files")
                    .containsExactly(cache.getFile("KEY1"));
        }
        assertThat(exceptions).hasSize(1);
    }

    @Test
    public void testCorruptedFileIsDeleted(@TempDir Path root) throws IOException {
        List<IOException> exceptions = new ArrayList<>();

        DiskCache<DataRepository> cache = DiskCache.<DataRepository>builder()
                .id("test")
                .root(root)
                .nameGenerator(identity())
                .format(newFakeFileFormat())
                .onError((id, message, exception) -> exceptions.add(exception))
                .build();

        Files.write(cache.getFile("KEY1"), FAILING_NAME.getBytes(StandardCharsets.UTF_8));

        assertThat(cache.get("KEY1")).isNull();
        assertThat(cache.getFile("KEY1")).as("Corrupted file should be deleted").doesNotExist();
        assertThat(exceptions).hasSize(1);
    }

    private static final String FAILING_NAME = "corrupted";

    private static FileFormatSupport<DataRepository> newFakeFileFormat() {
        Map<String, DataRepository> content = new HashMap<>();
        return FileFormatSupport.builder(DataRepository.class)
                .parser(FileParser.onParsingStream(stream -> parseFake(content, stream)))
                .formatter(FileFormatter.onFormattingStream((value, stream) -> formatFake(content, stream, value)))
                .extension(".dat")
                .build();
    }

    private static DataRepository parseFake(Map<String, DataRepository> content, InputStream stream)
            throws IOException {
        Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        String name = new BufferedReader(reader).lines().collect(Collectors.joining(""));
        if (FAILING_NAME.equals(name)) throw new IOException("Corrupted content");
        return content.get(name);
    }

    private static void formatFake(Map<String, DataRepository> content, OutputStream stream, DataRepository value)
            throws IOException {
        Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        if (FAILING_NAME.equals(value.getName())) {
            writer.write("partial");
            writer.flush();
            throw new IOException("Interrupted write");
        }
        writer.write(value.getName());
        writer.flush();
        content.put(value.getName(), value);
    }
}
