package sdmxdl.util.file;

import _test.sdmxdl.util.XCacheAssertions;
import _test.sdmxdl.util.XCountingFileClient;
import _test.sdmxdl.util.XRepoFileClient;
import org.junit.Test;
import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.Key;
import sdmxdl.repo.DataSet;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.tck.ext.FakeClock;
import sdmxdl.util.ext.ExpiringRepository;
import sdmxdl.util.ext.MapCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.samples.RepoSamples.*;

public class CachedFileClientTest {

    @Test
    public void testDecode() throws IOException {
        XCacheAssertions.Factory factory = (count, map, clock) -> getClient(REPO, count, map, clock)::decode;
        XCacheAssertions.checkCache(factory, "decode://", CachedFileClient.DEFAULT_CACHE_TTL.toMillis());
    }

    @Test
    public void testLoadData() throws IOException {
        XCacheAssertions.Factory factory = (count, map, clock) -> {
            CachedFileClient client = getClient(REPO, count, map, clock);
            SdmxFileInfo info = client.decode();
            return () -> client.loadData(info, GOOD_FLOW_REF, Key.ALL, DataFilter.SERIES_KEYS_ONLY);
        };
        XCacheAssertions.checkCache(factory, "loadData://", CachedFileClient.DEFAULT_CACHE_TTL.toMillis());

        ConcurrentMap<String, ExpiringRepository> map = new ConcurrentHashMap<>();

        SdmxRepository noData = SdmxRepository.builder().dataSet(dataSetOf(DATA_SET, Key.ALL, DataFilter.NO_DATA)).build();
        ExpiringRepository expectedEntry = ExpiringRepository.of(0, CachedFileClient.DEFAULT_CACHE_TTL.toMillis(), noData);

        CachedFileClient client = new CachedFileClient(new XRepoFileClient(REPO), MapCache.of(map, new FakeClock().set(0)), "");

        for (Key key : new Key[]{Key.ALL, Key.of("M", "BE", "INDUSTRY"), Key.of("", "BE", "INDUSTRY"), Key.of("A", "BE", "INDUSTRY")}) {
            try (DataCursor cursor = client.loadData(client.decode(), GOOD_FLOW_REF, key, DataFilter.SERIES_KEYS_ONLY)) {
                while (cursor.nextSeries()) {
                    assertThat(cursor.getSeriesKey()).matches(key::contains);
                }
            }
            assertThat(map).containsEntry("loadData://", expectedEntry);
        }
    }

    private static CachedFileClient getClient(SdmxRepository repo, AtomicInteger count, ConcurrentMap<String, ExpiringRepository> map, FakeClock clock) {
        return new CachedFileClient(new XCountingFileClient(new XRepoFileClient(repo), count), MapCache.of(map, clock), "");
    }

    private static DataSet dataSetOf(DataSet dataSet, Key key, DataFilter filter) throws IOException {
        try (DataCursor cursor = dataSet.getDataCursor(key, filter)) {
            return DataSet.builder().ref(dataSet.getRef()).copyOf(cursor).build();
        }
    }
}
