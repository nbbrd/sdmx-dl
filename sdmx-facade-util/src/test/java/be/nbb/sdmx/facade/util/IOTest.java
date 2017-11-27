/*
 * Copyright 2017 National Bank of Belgium
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
package be.nbb.sdmx.facade.util;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class IOTest {

    @Test
    @SuppressWarnings("null")
    public void testStream() {
        assertThatNullPointerException().isThrownBy(() -> IO.stream(null, emptyStream()));
        assertThatNullPointerException().isThrownBy(() -> IO.stream(() -> null, null));

        IO.Supplier<Closeable> factoryError = IO.Supplier.throwing(FactoryError::new);
        IO.Supplier<Closeable> closeError = () -> IO.Runnable.throwing(CloseError::new)::runWithIO;
        IO.Function<Closeable, Stream<String>> streamError = IO.Function.throwing(StreamError::new);

        assertThatThrownBy(() -> IO.stream(factoryError, emptyStream()).close())
                .isInstanceOf(FactoryError.class)
                .hasNoSuppressedExceptions();

        assertThatThrownBy(() -> IO.stream(factoryError, streamError).close())
                .isInstanceOf(FactoryError.class)
                .hasNoSuppressedExceptions();

        assertThatThrownBy(() -> IO.stream(closeError, emptyStream()).close())
                .isInstanceOf(UncheckedIOException.class)
                .hasRootCauseInstanceOf(CloseError.class)
                .hasNoSuppressedExceptions();

        assertThatThrownBy(() -> IO.stream(closeError, streamError).close())
                .isInstanceOf(StreamError.class)
                .hasSuppressedException(new CloseError());

        assertThat(new AtomicBoolean(false)).satisfies(c -> {
            assertThatThrownBy(() -> IO.stream(closeable(c), streamError).close())
                    .isInstanceOf(StreamError.class)
                    .hasNoSuppressedExceptions();
            assertThat(c).isTrue();
        });

        assertThat(new AtomicBoolean(false)).satisfies(c -> {
            assertThatCode(() -> IO.stream(closeable(c), emptyStream()).close()).doesNotThrowAnyException();
            assertThat(c).isTrue();
        });

        assertThat(new AtomicBoolean(false)).satisfies(c -> {
            assertThatCode(() -> IO.stream(closeable(c), emptyStream())).doesNotThrowAnyException();
            assertThat(c).isFalse();
        });
    }

    @Test
    @SuppressWarnings("null")
    public void testStreamNonnull() {
        assertThatNullPointerException().isThrownBy(() -> IO.streamNonnull(null));

        assertThat(IO.streamNonnull(() -> null)).isEmpty();

        Iterator<String> iter = Arrays.asList("A", "B").iterator();
        assertThat(IO.streamNonnull(() -> iter.hasNext() ? iter.next() : null)).containsExactly("A", "B");

        assertThatThrownBy(() -> IO.streamNonnull(IO.Supplier.throwing(FileNotFoundException::new)).count())
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(FileNotFoundException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testShortcuts() {
        assertThatNullPointerException().isThrownBy(() -> IO.asUnchecked((IO.Runnable) null));
        assertThatNullPointerException().isThrownBy(() -> IO.asUnchecked((IO.Function) null));
        assertThatNullPointerException().isThrownBy(() -> IO.asUnchecked((IO.Predicate) null));
        assertThatNullPointerException().isThrownBy(() -> IO.asUnchecked((IO.Consumer) null));
    }

    @Test
    @SuppressWarnings("null")
    public void testFunctionThrowing() {
        assertThatNullPointerException().isThrownBy(() -> IO.Function.throwing(null));
        assertThatThrownBy(() -> IO.Function.throwing(FileNotFoundException::new).applyWithIO(null))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testFunctionAsUnchecked() {
        assertThatCode(() -> IO.Function.of("").asUnchecked().apply("")).doesNotThrowAnyException();

        assertThatThrownBy(() -> IO.Function.throwing(FileNotFoundException::new).asUnchecked().apply(null))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(FileNotFoundException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testRunnableThrowing() {
        assertThatNullPointerException().isThrownBy(() -> IO.Runnable.throwing(null));
        assertThatThrownBy(() -> IO.Runnable.throwing(FileNotFoundException::new).runWithIO())
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testRunnableAsUnchecked() {
        assertThatCode(() -> IO.Runnable.noOp().asUnchecked().run()).doesNotThrowAnyException();

        assertThatThrownBy(() -> IO.Runnable.throwing(FileNotFoundException::new).asUnchecked().run())
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(FileNotFoundException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testSupplierThrowing() {
        assertThatNullPointerException().isThrownBy(() -> IO.Supplier.throwing(null));
        assertThatThrownBy(() -> IO.Supplier.throwing(FileNotFoundException::new).getWithIO())
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testSupplierAsUnchecked() {
        assertThatCode(() -> IO.Supplier.of("").asUnchecked().get()).doesNotThrowAnyException();

        assertThatThrownBy(() -> IO.Supplier.throwing(FileNotFoundException::new).asUnchecked().get())
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(FileNotFoundException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testPredicateThrowing() {
        assertThatNullPointerException().isThrownBy(() -> IO.Predicate.throwing(null));
        assertThatThrownBy(() -> IO.Predicate.throwing(FileNotFoundException::new).testWithIO(null))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testPredicateAsUnchecked() {
        assertThatCode(() -> IO.Predicate.of(true).asUnchecked().test("")).doesNotThrowAnyException();

        assertThatThrownBy(() -> IO.Predicate.throwing(FileNotFoundException::new).asUnchecked().test(null))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(FileNotFoundException.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testConsumerThrowing() {
        assertThatNullPointerException().isThrownBy(() -> IO.Consumer.throwing(null));
        assertThatThrownBy(() -> IO.Consumer.throwing(FileNotFoundException::new).acceptWithIO(null))
                .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    public void testConsumerAsUnchecked() {
        assertThatCode(() -> IO.Consumer.noOp().asUnchecked().accept("")).doesNotThrowAnyException();

        assertThatThrownBy(() -> IO.Consumer.throwing(FileNotFoundException::new).asUnchecked().accept(null))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(FileNotFoundException.class);
    }

    private static <T, R> IO.Function<T, Stream<R>> emptyStream() {
        return IO.Function.of(Stream.empty());
    }

    private static IO.Supplier<Closeable> closeable(AtomicBoolean o) {
        return IO.Supplier.of(() -> o.set(true));
    }

    private static final class FactoryError extends IOException {
    }

    private static final class CloseError extends IOException {
    }

    private static final class StreamError extends IOException {
    }
}
