package _test.sdmxdl.util;

import nbbrd.io.function.IOFunction;
import org.assertj.core.api.Condition;
import sdmxdl.DataRepository;
import sdmxdl.format.MemCache;
import sdmxdl.web.MonitorReports;
import tests.sdmxdl.ext.FakeClock;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@lombok.experimental.UtilityClass
public class CachingAssert {

    public static String idOf(Object... items) {
        return Stream.of(items).map(Object::toString).collect(Collectors.joining());
    }

    @lombok.Value
    public static class Context {

        @lombok.NonNull
        AtomicInteger count = new AtomicInteger(0);

        @lombok.NonNull
        Map<String, DataRepository> map = new HashMap<>();

        @lombok.NonNull
        Map<String, MonitorReports> monitors = new HashMap<>();

        @lombok.NonNull
        FakeClock clock = new FakeClock().set(0);

        public void reset() {
            count.set(0);
            map.clear();
            clock.set(0);
        }

        public MemCache<DataRepository> newCache() {
            return MemCache.<DataRepository>builder().map(map).clock(clock).build();
        }
    }

    public static <T, V> void checkCacheHit(Function<Context, T> factory, IOFunction<T, V> method, Condition<? super V> validator, String key, Duration ttl) throws IOException {
        Context ctx = new Context();

        T target = factory.apply(ctx);

        ctx.reset();

        // first call
        assertThat(method.applyWithIO(target)).satisfies(validator);
        State state1 = State.of(ctx, key);
        assertThat(state1.count).isGreaterThan(0);
        assertThat(state1.value).isNotNull();

        // subsequent call
        assertThat(method.applyWithIO(target)).satisfies(validator);
        State state2 = State.of(ctx, key);
        assertThat(state2.count).isEqualTo(state1.count);
        assertThat(state2.value).isSameAs(state1.value);

        // expired content
        ctx.getClock().plus(ttl.toMillis());
        assertThat(method.applyWithIO(target)).satisfies(validator);
        State state3 = State.of(ctx, key);
        assertThat(state3.count).isGreaterThan(state2.count);
        assertThat(state3.value).isNotSameAs(state2.value);

        // cleared content
        ctx.getMap().clear();
        assertThat(method.applyWithIO(target)).satisfies(validator);
        State state4 = State.of(ctx, key);
        assertThat(state4.count).isGreaterThan(state3.count);
        assertThat(state4.value).isNotSameAs(state3.value);
    }

    public static <T, V> void checkCacheMiss(Function<Context, T> factory, IOFunction<T, V> method, Condition<? super V> validator, String key, Duration ttl) throws IOException {
        Context ctx = new Context();

        T target = factory.apply(ctx);

        ctx.reset();

        // first call
        assertThat(method.applyWithIO(target)).satisfies(validator);
        State state1 = State.of(ctx, key);
        assertThat(state1.count).isGreaterThan(0);
        assertThat(state1.value).isNull();

        // subsequent call
        assertThat(method.applyWithIO(target)).satisfies(validator);
        State state2 = State.of(ctx, key);
        assertThat(state2.count).isGreaterThan(state1.count);
        assertThat(state2.value).isNull();

        // expired content
        ctx.getClock().plus(ttl.toMillis());
        assertThat(method.applyWithIO(target)).satisfies(validator);
        State state3 = State.of(ctx, key);
        assertThat(state3.count).isGreaterThan(state2.count);
        assertThat(state3.value).isNull();

        // cleared content
        ctx.getMap().clear();
        assertThat(method.applyWithIO(target)).satisfies(validator);
        State state4 = State.of(ctx, key);
        assertThat(state4.count).isGreaterThan(state3.count);
        assertThat(state4.value).isNull();
    }

    @lombok.Value
    private static class State {

        int count;

        DataRepository value;

        static State of(Context context, String key) {
            return new State(context.getCount().get(), context.getMap().get(key));
        }
    }
}
