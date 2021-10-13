package sdmxdl.util.file;

import _test.sdmxdl.util.CachingAssert;
import _test.sdmxdl.util.XCountingFileClient;
import _test.sdmxdl.util.XRepoFileClient;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.function.IOFunction;
import org.assertj.core.api.HamcrestCondition;
import org.junit.Test;
import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.Key;
import sdmxdl.Series;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.stream.Collectors;

import static _test.sdmxdl.util.CachingAssert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static sdmxdl.samples.RepoSamples.*;
import static sdmxdl.tck.DataFilterAssert.filters;
import static sdmxdl.tck.KeyAssert.keys;

public class CachedFileClientTest {

    private final String base = "abc";
    private final Duration ttl = CachedFileClient.DEFAULT_CACHE_TTL;

    private CachedFileClient getClient(CachingAssert.Context ctx) {
        SdmxFileClient original = new XRepoFileClient(REPO);
        SdmxFileClient counting = new XCountingFileClient(original, ctx.getCount());
        return new CachedFileClient(counting, ctx.newCache(), base);
    }

    @FunctionalInterface
    private interface Method<T> extends IOFunction<CachedFileClient, T> {
    }

    @Test
    public void testDecode() throws IOException {
        String decodeKey = "decode://" + base;
        Method<SdmxFileInfo> x = CachedFileClient::decode;

        checkCacheHit(this::getClient, x, new HamcrestCondition<>(equalTo(XRepoFileClient.infoOf(REPO))), decodeKey, ttl);
    }

    @Test
    public void testLoadData() throws IOException {
        String loadDataKey = "loadData://" + base;

        for (Key key : keys("all", "M.BE.INDUSTRY", ".BE.INDUSTRY", "A.BE.INDUSTRY")) {
            for (DataFilter filter : filters(DataFilter.Detail.values())) {
                Method<Collection<Series>> x = client -> {
                    try (DataCursor cursor = client.loadData(client.decode(), GOOD_FLOW_REF, key, filter)) {
                        return cursor.toStream().collect(Collectors.toList());
                    }
                };

                HamcrestCondition<Collection<Series>> validator = new HamcrestCondition<>(equalTo(DATA_SET.getData(key, filter)));

                if (filter.getDetail().isDataRequested()) {
                    checkCacheMiss(this::getClient, x, validator, loadDataKey, ttl);
                } else {
                    checkCacheHit(this::getClient, x, validator, loadDataKey, ttl);
                }
            }
        }
    }

    @Test
    public void testCopyAllNoData() throws IOException {
        Context ctx = new Context();
        CachedFileClient client = getClient(ctx);

        SdmxFileInfo info = client.decode();
        IOConsumer<Key> x = key -> client.loadData(info, GOOD_FLOW_REF, key, DataFilter.SERIES_KEYS_ONLY).close();

        ctx.reset();
        x.acceptWithIO(Key.ALL);
        x.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
        assertThat(ctx.getCount()).hasValue(1);

        ctx.reset();
        x.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
        x.acceptWithIO(Key.ALL);
        assertThat(ctx.getCount()).hasValue(1);
    }
}
