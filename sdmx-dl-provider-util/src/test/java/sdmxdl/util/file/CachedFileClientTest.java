package sdmxdl.util.file;

import _test.sdmxdl.util.CachingAssert;
import _test.sdmxdl.util.XCountingFileClient;
import _test.sdmxdl.util.XRepoFileClient;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.function.IOFunction;
import org.junit.Test;
import sdmxdl.DataCursor;
import sdmxdl.DataFilter;
import sdmxdl.Key;
import sdmxdl.samples.RepoSamples;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

import static _test.sdmxdl.util.CachingAssert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static sdmxdl.samples.RepoSamples.GOOD_FLOW_REF;
import static sdmxdl.samples.RepoSamples.REPO;
import static sdmxdl.tck.DataFilterAssert.filters;
import static sdmxdl.tck.KeyAssert.keys;

public class CachedFileClientTest {

    private final String base = "abc";
    private final long ttl = CachedFileClient.DEFAULT_CACHE_TTL.toMillis();

    private CachedFileClient getClient(CachingAssert.Context ctx) {
        SdmxFileClient original = new XRepoFileClient(REPO);
        SdmxFileClient counting = new XCountingFileClient(original, ctx.getCount());
        return new CachedFileClient(counting, ctx.newCache(), base);
    }

    @FunctionalInterface
    private interface Method<T> extends IOFunction<CachedFileClient, T> {

        default Consumer<CachedFileClient> andThen(IOConsumer<T> consumer) {
            return IOConsumer.unchecked(o -> consumer.acceptWithIO(applyWithIO(o)));
        }

        default Consumer<CachedFileClient> asConsumer() {
            return IOConsumer.unchecked(this::applyWithIO);
        }
    }

    @Test
    public void testDecode() throws IOException {
        String decodeKey = "decode://" + base;
        Method<SdmxFileInfo> method = CachedFileClient::decode;

        checkCacheHit(this::getClient, method.asConsumer(), decodeKey, ttl);

        assertThat(method.compose(this::getClient).applyWithIO(new Context()))
                .isEqualTo(XRepoFileClient.infoOf(REPO));
    }

    @Test
    public void testLoadData() throws IOException {
        String loadDataKey = "loadData://" + base;

        for (Key key : keys("all", "M.BE.INDUSTRY", ".BE.INDUSTRY", "A.BE.INDUSTRY")) {
            for (DataFilter filter : filters(DataFilter.Detail.values())) {
                Method<DataCursor> method = client -> client.loadData(client.decode(), GOOD_FLOW_REF, key, filter);

                if (filter.getDetail().isDataRequested()) {
                    checkCacheMiss(this::getClient, method.andThen(Closeable::close), loadDataKey, ttl);
                } else {
                    checkCacheHit(this::getClient, method.andThen(Closeable::close), loadDataKey, ttl);
                }

                try (DataCursor cursor = method.compose(this::getClient).applyWithIO(new Context())) {
                    assertThat(cursor.toStream())
                            .containsExactlyElementsOf(RepoSamples.DATA_SET.getData(key, filter));
                }
            }
        }
    }

    @Test
    public void testCopyAllNoData() throws IOException {
        Context ctx = new Context();
        CachedFileClient client = getClient(ctx);

        SdmxFileInfo info = client.decode();
        IOConsumer<Key> method = key -> client.loadData(info, GOOD_FLOW_REF, key, DataFilter.SERIES_KEYS_ONLY).close();

        ctx.reset();
        method.acceptWithIO(Key.ALL);
        method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
        assertThat(ctx.getCount()).hasValue(1);

        ctx.reset();
        method.acceptWithIO(Key.parse("M.BE.INDUSTRY"));
        method.acceptWithIO(Key.ALL);
        assertThat(ctx.getCount()).hasValue(1);
    }
}
