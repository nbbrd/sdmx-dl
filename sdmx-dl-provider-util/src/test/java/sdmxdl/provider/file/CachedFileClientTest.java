package sdmxdl.provider.file;

import _test.sdmxdl.util.CachingAssert;
import _test.sdmxdl.util.XCountingFileClient;
import _test.sdmxdl.util.XRepoFileClient;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.function.IOFunction;
import org.assertj.core.api.HamcrestCondition;
import org.junit.jupiter.api.Test;
import sdmxdl.DataDetail;
import sdmxdl.DataQuery;
import sdmxdl.Key;
import sdmxdl.Series;
import sdmxdl.provider.DataRef;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collection;
import java.util.stream.Stream;

import static _test.sdmxdl.util.CachingAssert.*;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static tests.sdmxdl.api.KeyAssert.keys;
import static tests.sdmxdl.api.RepoSamples.*;

public class CachedFileClientTest {

    private final URI base = URI.create("cache:file");
    private final Duration ttl = CachedFileClient.DEFAULT_CACHE_TTL;

    private CachedFileClient getClient(CachingAssert.Context ctx) {
        FileClient original = new XRepoFileClient(REPO);
        FileClient counting = new XCountingFileClient(original, ctx.getCount());
        return new CachedFileClient(counting, ctx.newCache(), base);
    }

    @FunctionalInterface
    private interface Method<T> extends IOFunction<CachedFileClient, T> {
    }

    @Test
    public void testDecode() throws IOException {
        String decodeKey = base + "/decode";
        Method<FileInfo> x = CachedFileClient::decode;

        checkCacheHit(this::getClient, x, new HamcrestCondition<>(equalTo(XRepoFileClient.infoOf(REPO))), decodeKey, ttl);
    }

    @Test
    public void testLoadData() throws IOException {
        String loadDataKey = base + "/loadData";

        for (Key key : keys("all", "M.BE.INDUSTRY", ".BE.INDUSTRY", "A.BE.INDUSTRY")) {
            for (DataDetail detail : DataDetail.values()) {
                DataRef ref = DataRef.of(FLOW_REF, DataQuery.of(key, detail));

                Method<Collection<Series>> x = client -> {
                    try (Stream<Series> stream = client.loadData(client.decode(), ref)) {
                        return stream.collect(toList());
                    }
                };

                HamcrestCondition<Collection<Series>> validator = new HamcrestCondition<>(equalTo(DATA_SET.getDataStream(ref.getQuery()).collect(toList())));

                if (detail.isDataRequested()) {
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

        FileInfo info = client.decode();
        IOConsumer<Key> x = key -> client.loadData(info, DataRef.of(FLOW_REF, DataQuery.of(key, DataDetail.SERIES_KEYS_ONLY))).close();

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
