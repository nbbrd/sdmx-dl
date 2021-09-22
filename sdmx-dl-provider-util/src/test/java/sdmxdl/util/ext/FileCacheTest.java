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
package sdmxdl.util.ext;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Test;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.tck.ext.FakeClock;
import sdmxdl.tck.ext.SdmxCacheAssert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Philippe Charles
 */
public class FileCacheTest {

    @Test
    public void testCompliance() {
        SdmxCacheAssert.assertCompliance(FileCache.builder().build());
    }

    @Test
    public void testGetSet() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            FakeClock clock = new FakeClock();
            clock.set(1000);
            FakeSerializer serializer = new FakeSerializer();

            FileCache cache = FileCache
                    .builder()
                    .root(fs.getPath("/").resolve("testfolder"))
                    .fileNameGenerator(UnaryOperator.identity())
                    .serializer(serializer)
                    .clock(clock)
                    .build();

            assertThat(cache.getRoot())
                    .doesNotExist();
            assertThat(cache.getRepository("KEY1"))
                    .as("Empty directory should return null")
                    .isNull();

            SdmxRepository r1 = SdmxRepository
                    .builder()
                    .name("r1")
                    .ttl(clock.instant(), Duration.ofMillis(10))
                    .build();
            cache.putRepository("KEY1", r1);
            assertThat(cache.getFile("KEY1"))
                    .exists()
                    .hasContent("r1");

            assertThat(cache.getRepository("KEY2"))
                    .as("Non-existing key should return null")
                    .isNull();

            clock.plus(9);
            assertThat(cache.getRepository("KEY1"))
                    .as("Non-expired key should return value")
                    .isEqualTo(r1);

            clock.plus(1);
            assertThat(cache.getRepository("KEY1"))
                    .as("Expired key should return null")
                    .isNull();
            assertThat(cache.getFile("KEY1"))
                    .as("Expired key should be deleted")
                    .doesNotExist();

            SdmxRepository r1b = r1
                    .toBuilder()
                    .ttl(clock.instant(), Duration.ofMillis(10))
                    .build();
            SdmxRepository r2 = SdmxRepository
                    .builder()
                    .name("r2")
                    .ttl(clock.instant(), Duration.ofMillis(10))
                    .build();
            cache.putRepository("KEY1", r1b);
            cache.putRepository("KEY1", r2);
            assertThat(cache.getFile("KEY1"))
                    .exists()
                    .hasContent("r2");
            assertThat(cache.getRepository("KEY1"))
                    .as("Updated key should return updated value")
                    .isEqualTo(r2);
        }
    }

    private static final class FakeSerializer implements Serializer<SdmxRepository> {

        @lombok.Getter
        private final Map<String, SdmxRepository> content = new HashMap<>();

        @Override
        public @NonNull SdmxRepository parseStream(@NonNull InputStream stream) throws IOException {
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                String name = new BufferedReader(reader).lines().collect(Collectors.joining(""));
                return content.get(name);
            }
        }

        @Override
        public void formatStream(@NonNull SdmxRepository entry, @NonNull OutputStream stream) throws IOException {
            try (Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                writer.write(entry.getName());
                content.put(entry.getName(), entry);
            }
        }
    }
}
