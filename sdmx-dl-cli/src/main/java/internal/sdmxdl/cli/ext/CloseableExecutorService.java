package internal.sdmxdl.cli.ext;

import lombok.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

// https://stackoverflow.com/a/72267126
@lombok.AllArgsConstructor
public final class CloseableExecutorService implements ExecutorService, AutoCloseable {

    @lombok.experimental.Delegate
    private final @NonNull ExecutorService delegate;

    @Override
    public void close() {
        // copy paste from JDK 19 EA
        boolean terminated = isTerminated();
        if (!terminated) {
            shutdown();
            boolean interrupted = false;
            while (!terminated) {
                try {
                    terminated = awaitTermination(1L, TimeUnit.DAYS);
                } catch (InterruptedException e) {
                    if (!interrupted) {
                        shutdownNow();
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
