package internal.sdmxdl.desktop.util;

import lombok.NonNull;
import nbbrd.design.swing.OnAnyThread;
import nbbrd.design.swing.OnEDT;
import nbbrd.io.function.IOFunction;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

@lombok.Value
@lombok.Builder(toBuilder = true)
public class AsyncSupport<K, V> {

    public static <K, V> @NonNull AsyncSupport<K, V> of(@NonNull IOFunction<K, V> supplier) {
        return AsyncSupport.<K, V>builder().supplier(supplier).build();
    }

    @NonNull
    @lombok.Builder.Default
    IOFunction<K, V> supplier = IOFunction.of(null);

    @NonNull
    @lombok.Builder.Default
    Executor executor = Executors.newCachedThreadPool(AsyncSupport::newLowPriorityDaemonThread);

    @NonNull
    @lombok.Builder.Default
    Executor dispatcher = SwingUtilities::invokeLater;

    @NonNull
    @lombok.Builder.Default
    Map<K, V> cache = new HashMap<>();

    @NonNull
    @lombok.Builder.Default
    Set<K> pending = new HashSet<>();

    @NonNull
    @lombok.Builder.Default
    BiConsumer<K, ? super String> onExecutorMessage = AsyncSupport::doNothing;

    @NonNull
    @lombok.Builder.Default
    BiConsumer<K, ? super IOException> onExecutorError = AsyncSupport::doNothing;

    @OnEDT
    public @Nullable V getOrNull(@NonNull K key) {
        return getOrLoadValue(key, AsyncSupport::doNothing);
    }

    @OnEDT
    public @NonNull V getOrDefault(@NonNull K key, @NonNull V fallback) {
        V result = getOrLoadValue(key, AsyncSupport::doNothing);
        return result != null ? result : fallback;
    }

    @OnEDT
    public @Nullable V getOrNull(@NonNull K key, @NonNull Runnable onUpdate) {
        return getOrLoadValue(key, onUpdate);
    }

    @OnEDT
    public @NonNull V getOrDefault(@NonNull K key, @NonNull Runnable onUpdate, @NonNull V fallback) {
        V result = getOrLoadValue(key, onUpdate);
        return result != null ? result : fallback;
    }

    @OnEDT
    private @Nullable V getOrLoadValue(@NonNull K key, @NonNull Runnable onUpdate) {
        if (pending.contains(key)) {
            return null;
        }
        V result = cache.get(key);
        if (result != null) {
            return result;
        } else {
            pending.add(key);
            executor.execute(() -> asyncLoadIntoCache(key, onUpdate));
            return null;
        }
    }

    @OnEDT
    private void updateCacheAndNotify(K key, V value, Runnable onUpdate) {
        pending.remove(key);
        cache.put(key, value);
        onUpdate.run();
    }

    @OnAnyThread
    private void asyncLoadIntoCache(K key, Runnable onUpdate) {
        V image = asyncLoadOrNull(key);
        if (image != null) {
            dispatcher.execute(() -> updateCacheAndNotify(key, image, onUpdate));
        }
    }

    @OnAnyThread
    private V asyncLoadOrNull(K key) {
        try {
            long start = System.currentTimeMillis();
            V result = supplier.applyWithIO(key);
            long stop = System.currentTimeMillis();
            if (result != null) {
                onExecutorMessage.accept(key, String.format(Locale.ROOT, "Loaded in %sms", stop - start));
                return result;
            } else {
                onExecutorMessage.accept(key, "Missing");
                return null;
            }
        } catch (IOException ex) {
            onExecutorError.accept(key, ex);
            return null;
        } catch (RuntimeException ex) {
            onExecutorError.accept(key, new IOException("Unexpected " + ex.getClass().getName() + ": " + ex.getMessage(), ex));
            return null;
        }
    }

    private static Thread newLowPriorityDaemonThread(Runnable runnable) {
        Thread result = new Thread(runnable);
        result.setDaemon(true);
        result.setPriority(Thread.MIN_PRIORITY);
        return result;
    }

    private static void doNothing() {
    }

    private static <K, V> void doNothing(K k, V v) {
    }
}
