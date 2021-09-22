package _test.sdmxdl.util;

import sdmxdl.repo.SdmxRepository;
import sdmxdl.tck.ext.FakeClock;
import sdmxdl.util.ext.MapCache;
import sdmxdl.web.SdmxWebMonitorReports;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@lombok.experimental.UtilityClass
public class CachingAssert {

    public static String idOf(Object... items) {
        return Stream.of(items).map(Object::toString).collect(Collectors.joining());
    }

    public static Clock clock(long value) {
        return Clock.fixed(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }

    @lombok.Value
    public static class Context {

        @lombok.NonNull
        AtomicInteger count = new AtomicInteger(0);

        @lombok.NonNull
        ConcurrentMap<String, SdmxRepository> map = new ConcurrentHashMap<>();

        @lombok.NonNull
        ConcurrentMap<String, SdmxWebMonitorReports> monitors = new ConcurrentHashMap<>();

        @lombok.NonNull
        FakeClock clock = new FakeClock().set(0);

        public void reset() {
            count.set(0);
            map.clear();
            clock.set(0);
        }

        public MapCache newCache() {
            return MapCache.of(map, monitors, clock);
        }
    }

    public static <T> void checkCacheHit(Function<Context, T> type, Consumer<T> method, String key, long ttl) {
        Context ctx = new Context();

        T target = type.apply(ctx);

        ctx.reset();

        // first call
        method.accept(target);
        State state1 = State.of(ctx, key);
        assertThat(state1.count).isGreaterThan(0);
        assertThat(state1.value).isNotNull();

        // subsequent call
        method.accept(target);
        State state2 = State.of(ctx, key);
        assertThat(state2.count).isEqualTo(state1.count);
        assertThat(state2.value).isSameAs(state1.value);

        // expired content
        ctx.getClock().plus(ttl);
        method.accept(target);
        State state3 = State.of(ctx, key);
        assertThat(state3.count).isGreaterThan(state2.count);
        assertThat(state3.value).isNotSameAs(state2.value);

        // cleared content
        ctx.getMap().clear();
        method.accept(target);
        State state4 = State.of(ctx, key);
        assertThat(state4.count).isGreaterThan(state3.count);
        assertThat(state4.value).isNotSameAs(state3.value);
    }

    public static <T> void checkCacheMiss(Function<Context, T> type, Consumer<T> method, String key, long ttl) {
        Context ctx = new Context();

        T target = type.apply(ctx);

        ctx.reset();

        // first call
        method.accept(target);
        State state1 = State.of(ctx, key);
        assertThat(state1.count).isGreaterThan(0);
        assertThat(state1.value).isNull();

        // subsequent call
        method.accept(target);
        State state2 = State.of(ctx, key);
        assertThat(state2.count).isGreaterThan(state1.count);
        assertThat(state2.value).isNull();

        // expired content
        ctx.getClock().plus(ttl);
        method.accept(target);
        State state3 = State.of(ctx, key);
        assertThat(state3.count).isGreaterThan(state2.count);
        assertThat(state3.value).isNull();

        // cleared content
        ctx.getMap().clear();
        method.accept(target);
        State state4 = State.of(ctx, key);
        assertThat(state4.count).isGreaterThan(state3.count);
        assertThat(state4.value).isNull();
    }

    @lombok.Value
    private static class State {

        int count;

        SdmxRepository value;

        static State of(Context context, String key) {
            return new State(context.getCount().get(), context.getMap().get(key));
        }
    }
}
