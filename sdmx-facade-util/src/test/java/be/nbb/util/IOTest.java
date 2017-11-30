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
package be.nbb.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class IOTest {

    //<editor-fold defaultstate="collapsed" desc="Stream">
    private static <R> IO.Function<Closeable, Stream<R>> streamerOf(R... values) {
        return o -> Stream.of(values);
    }

    @Test
    @SuppressWarnings("null")
    public void testStreamOpen() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> IO.Stream.open(null, streamerOf()));
        assertThatNullPointerException().isThrownBy(() -> IO.Stream.open(() -> null, null));

        IO.Supplier<Closeable> ofOpenError = IO.Supplier.throwing(OpenError::new);
        IO.Supplier<Closeable> ofCloseError = () -> IO.Runnable.throwing(CloseError::new)::runWithIO;
        IO.Function<Closeable, Stream<String>> toReadError = IO.Function.throwing(ReadError::new);

        assertThatThrownBy(() -> IO.Stream.open(ofOpenError, streamerOf()).close())
                .isInstanceOf(OpenError.class)
                .hasNoSuppressedExceptions();

        assertThatThrownBy(() -> IO.Stream.open(ofOpenError, toReadError).close())
                .isInstanceOf(OpenError.class)
                .hasNoSuppressedExceptions();

        assertThatThrownBy(() -> IO.Stream.open(ofCloseError, streamerOf()).close())
                .isInstanceOf(UncheckedIOException.class)
                .hasRootCauseInstanceOf(CloseError.class)
                .hasNoSuppressedExceptions();

        assertThatThrownBy(() -> IO.Stream.open(ofCloseError, toReadError).close())
                .isInstanceOf(ReadError.class)
                .hasSuppressedException(new CloseError());

        assertThat(new AtomicInteger(0)).satisfies(c -> {
            assertThatThrownBy(() -> IO.Stream.open(() -> c::incrementAndGet, toReadError).close())
                    .isInstanceOf(ReadError.class)
                    .hasNoSuppressedExceptions();
            assertThat(c).hasValue(1);
        });

        assertThat(new AtomicInteger(0)).satisfies(c -> {
            assertThatCode(() -> IO.Stream.open(() -> c::incrementAndGet, streamerOf()).close()).doesNotThrowAnyException();
            assertThat(c).hasValue(1);
        });

        assertThat(new AtomicInteger(0)).satisfies(c -> {
            assertThatCode(() -> IO.Stream.open(() -> c::incrementAndGet, streamerOf())).doesNotThrowAnyException();
            assertThat(c).hasValue(0);
        });

        assertThat(IO.Stream.open(IO.Runnable.noOp()::asCloseable, streamerOf())).isEmpty();
        assertThat(IO.Stream.open(IO.Runnable.noOp()::asCloseable, streamerOf("a", "b", "c"))).hasSize(3);
    }

    @Test
    @SuppressWarnings("null")
    public void testStreamGenerateUntilNull() {
        assertThatNullPointerException().isThrownBy(() -> IO.Stream.generateUntilNull(null));

        assertThat(IO.Stream.generateUntilNull(() -> null)).isEmpty();

        Iterator<String> iter = Arrays.asList("A", "B").iterator();
        assertThat(IO.Stream.generateUntilNull(() -> iter.hasNext() ? iter.next() : null)).containsExactly("A", "B");

        assertThatThrownBy(() -> IO.Stream.generateUntilNull(ofError1).count())
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Function">
    private final IO.Function<String, String> toUpperCase = String::toUpperCase;
    private final IO.Function<Object, String> toString = Object::toString;
    private final IO.Function<Object, Object> toError1 = IO.Function.throwing(Error1::new);
    private final IO.Function<Object, Object> toError2 = IO.Function.throwing(Error2::new);

    @Test
    @SuppressWarnings("null")
    public void testFunctionCompose() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> toUpperCase.compose(null));

        assertThat(toUpperCase.compose(toString).applyWithIO(Byte.class)).isEqualTo(Byte.class.toString().toUpperCase());
        assertThatThrownBy(() -> toError1.compose(toString).applyWithIO(Byte.class)).isInstanceOf(Error1.class);
        assertThatThrownBy(() -> toString.compose(toError2).applyWithIO(Byte.class)).isInstanceOf(Error2.class);
        assertThatThrownBy(() -> toError1.compose(toError2).applyWithIO(Byte.class)).isInstanceOf(Error2.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFunctionAndThen() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> toUpperCase.andThen(null));

        assertThat(toString.andThen(toUpperCase).applyWithIO(Byte.class)).isEqualTo(Byte.class.toString().toUpperCase());
        assertThatThrownBy(() -> toError1.andThen(toString).applyWithIO(Byte.class)).isInstanceOf(Error1.class);
        assertThatThrownBy(() -> toString.andThen(toError2).applyWithIO(Byte.class)).isInstanceOf(Error2.class);
        assertThatThrownBy(() -> toError1.andThen(toError2).applyWithIO(Byte.class)).isInstanceOf(Error1.class);
    }

    @Test
    public void testFunctionAsUnchecked() {
        assertThat(toUpperCase.asUnchecked().apply("hello")).isEqualTo("HELLO");
        assertThatThrownBy(() -> toError1.asUnchecked().apply(null))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFunctionUnchecked() {
        assertThatNullPointerException().isThrownBy(() -> IO.Function.unchecked(null));

        assertThat(IO.Function.unchecked(toUpperCase).apply("hello")).isEqualTo("HELLO");
        assertThatThrownBy(() -> IO.Function.unchecked(toError1).apply(null))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFunctionChecked() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> IO.Function.checked(null));

        assertThat(IO.Function.checked(Object::toString).applyWithIO(Byte.class)).isEqualTo(Byte.class.toString());
        assertThatThrownBy(() -> IO.Function.checked(IO.Function.unchecked(toError1)).applyWithIO(null))
                .isInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testFunctionThrowing() {
        assertThatNullPointerException().isThrownBy(() -> IO.Function.throwing(null));

        assertThatThrownBy(() -> IO.Function.throwing(Error1::new).applyWithIO(null)).isInstanceOf(Error1.class);
    }

    @Test
    public void testFunctionIdentity() throws IOException {
        assertThat(IO.Function.identity().applyWithIO(null)).isNull();
        assertThat(IO.Function.identity().applyWithIO(Byte.class)).isEqualTo(Byte.class);
    }

    @Test
    public void testFunctionOf() throws IOException {
        assertThat(IO.Function.of(null).applyWithIO(null)).isNull();
        assertThat(IO.Function.of("").applyWithIO(Byte.class)).isEqualTo("");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Runnable">
    private final IO.Runnable onError1 = IO.Runnable.throwing(Error1::new);

    @Test
    public void testRunnableAsCloseable() {
        assertThat(new AtomicInteger(0)).satisfies(o -> {
            assertThatCode(() -> ((IO.Runnable) o::incrementAndGet).asCloseable().close()).doesNotThrowAnyException();
            assertThat(o).hasValue(1);
        });

        assertThatThrownBy(() -> onError1.asCloseable().close()).isInstanceOf(Error1.class);
    }

    @Test
    public void testRunnableAsUnchecked() {
        assertThat(new AtomicInteger(0)).satisfies(o -> {
            assertThatCode(() -> ((IO.Runnable) o::incrementAndGet).asUnchecked().run()).doesNotThrowAnyException();
            assertThat(o).hasValue(1);
        });

        assertThatThrownBy(() -> onError1.asUnchecked().run())
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testRunnableUnchecked() {
        assertThatNullPointerException().isThrownBy(() -> IO.Runnable.unchecked(null));

        assertThat(new AtomicInteger(0)).satisfies(o -> {
            assertThatCode(() -> IO.Runnable.unchecked(o::incrementAndGet).run()).doesNotThrowAnyException();
            assertThat(o).hasValue(1);
        });

        assertThatThrownBy(() -> IO.Runnable.unchecked(onError1).run())
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testRunnableChecked() {
        assertThatNullPointerException().isThrownBy(() -> IO.Runnable.unchecked(null));

        assertThat(new AtomicInteger(0)).satisfies(o -> {
            assertThatCode(() -> IO.Runnable.checked(IO.Runnable.unchecked(o::incrementAndGet)).runWithIO()).doesNotThrowAnyException();
            assertThat(o).hasValue(1);
        });

        assertThatThrownBy(() -> IO.Runnable.checked(IO.Runnable.unchecked(onError1)).runWithIO()).isInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testRunnableThrowing() {
        assertThatNullPointerException().isThrownBy(() -> IO.Runnable.throwing(null));

        assertThatThrownBy(() -> onError1.runWithIO()).isInstanceOf(Error1.class);
    }

    @Test
    public void testRunnableNoOp() {
        assertThatCode(() -> IO.Runnable.noOp().runWithIO()).doesNotThrowAnyException();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Supplier">
    private final IO.Supplier<?> ofError1 = IO.Supplier.throwing(Error1::new);
    private final IO.Supplier<Integer> ofIncrement = new AtomicInteger()::incrementAndGet;

    @Test
    public void testSupplierAsUnchecked() throws IOException {
        assertThat(ofIncrement.asUnchecked().get()).isEqualTo(ofIncrement.getWithIO() - 1);
        assertThatThrownBy(() -> ofError1.asUnchecked().get())
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testSupplierUnchecked() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> IO.Supplier.unchecked(null));

        assertThat(IO.Supplier.unchecked(ofIncrement).get()).isEqualTo(ofIncrement.getWithIO() - 1);
        assertThatThrownBy(() -> IO.Supplier.unchecked(ofError1).get())
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testSupplierChecked() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> IO.Supplier.checked(null));

        assertThat(IO.Supplier.checked(IO.Supplier.unchecked(ofIncrement)).getWithIO()).isEqualTo(ofIncrement.getWithIO() - 1);
        assertThatThrownBy(() -> IO.Supplier.checked(IO.Supplier.unchecked(ofError1)).getWithIO()).isInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testSupplierThrowing() {
        assertThatNullPointerException().isThrownBy(() -> IO.Supplier.throwing(null));
        assertThatThrownBy(() -> ofError1.getWithIO()).isInstanceOf(Error1.class);
    }

    @Test
    public void testSupplierOf() throws IOException {
        assertThat(IO.Supplier.of(null).getWithIO()).isNull();
        assertThat(IO.Supplier.of("").getWithIO()).isEqualTo("");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Predicate">
    private final IO.Predicate<Object> isNonNull = Objects::nonNull;
    private final IO.Predicate<Object> isNull = Objects::isNull;
    private final IO.Predicate<Object> isNotEmptyString = o -> ((String) o).length() > 0;
    private final IO.Predicate<Object> isError1 = IO.Predicate.throwing(Error1::new);
    private final IO.Predicate<Object> isError2 = IO.Predicate.throwing(Error2::new);

    @Test
    @SuppressWarnings("null")
    public void testPredicateAnd() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> isNonNull.and(null));

        assertThat(isNonNull.and(isNotEmptyString).testWithIO("a")).isTrue();
        assertThat(isNonNull.and(isNotEmptyString).testWithIO("")).isFalse();
        assertThat(isNonNull.and(isNotEmptyString).testWithIO(null)).isFalse();
        assertThatThrownBy(() -> isNonNull.and(isError1).testWithIO("")).isInstanceOf(Error1.class);
        assertThatThrownBy(() -> isError1.and(isNonNull).testWithIO("")).isInstanceOf(Error1.class);
        assertThatThrownBy(() -> isError1.and(isError2).testWithIO("")).isInstanceOf(Error1.class);
    }

    @Test
    public void testPredicateNegate() throws IOException {
        assertThat(isNonNull.negate().testWithIO(null)).isTrue();
        assertThat(isNonNull.negate().testWithIO("")).isFalse();
        assertThatThrownBy(() -> isError1.negate().testWithIO("")).isInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testPredicateOr() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> isNonNull.or(null));

        assertThat(isNull.or(isNotEmptyString).testWithIO("a")).isTrue();
        assertThat(isNull.or(isNotEmptyString).testWithIO("")).isFalse();
        assertThat(isNull.or(isNotEmptyString).testWithIO(null)).isTrue();
        assertThat(isNull.or(isError1).testWithIO(null)).isTrue();
        assertThatThrownBy(() -> isNull.or(isError1).testWithIO("")).isInstanceOf(Error1.class);
        assertThatThrownBy(() -> isError1.or(isNull).testWithIO("")).isInstanceOf(Error1.class);
        assertThatThrownBy(() -> isError1.or(isError2).testWithIO("")).isInstanceOf(Error1.class);
    }

    @Test
    public void testPredicateAsUnchecked() {
        assertThat(IO.Predicate.of(true).asUnchecked().test("")).isTrue();
        assertThatThrownBy(() -> isError1.asUnchecked().test(null))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testPredicateUnchecked() {
        assertThatNullPointerException().isThrownBy(() -> IO.Predicate.unchecked(null));

        assertThat(IO.Predicate.unchecked(IO.Predicate.of(true)).test("")).isTrue();
        assertThatThrownBy(() -> IO.Predicate.unchecked(isError1).test(null))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testPredicateChecked() throws IOException {
        assertThatNullPointerException().isThrownBy(() -> IO.Predicate.checked(null));

        assertThat(IO.Predicate.checked(o -> true).testWithIO("")).isTrue();
        assertThatThrownBy(() -> IO.Predicate.checked(IO.Predicate.unchecked(isError1)).testWithIO(null)).isInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testPredicateThrowing() {
        assertThatNullPointerException().isThrownBy(() -> IO.Predicate.throwing(null));

        assertThatThrownBy(() -> isError1.testWithIO(null)).isInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testPredicateIsEqual() throws IOException {
        assertThat(IO.Predicate.isEqual(null).testWithIO(null)).isTrue();
        assertThat(IO.Predicate.isEqual(null).testWithIO("")).isFalse();
        assertThat(IO.Predicate.isEqual("").testWithIO("")).isTrue();
        assertThat(IO.Predicate.isEqual("").testWithIO(null)).isFalse();
    }

    @Test
    public void testPredicateOf() throws IOException {
        assertThat(IO.Predicate.of(true).testWithIO(null)).isTrue();
        assertThat(IO.Predicate.of(false).testWithIO("")).isFalse();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Consumer">
    private final IO.Consumer<AtomicInteger> withError1 = IO.Consumer.throwing(Error1::new);
    private final IO.Consumer<AtomicInteger> withError2 = IO.Consumer.throwing(Error2::new);
    private final IO.Consumer<AtomicInteger> withIncrement = AtomicInteger::incrementAndGet;

    @Test
    @SuppressWarnings("null")
    public void testConsumerAndThen() {
        assertThatNullPointerException().isThrownBy(() -> withIncrement.andThen(null));

        assertThat(new AtomicInteger(0)).satisfies(o -> {
            assertThatCode(() -> withIncrement.andThen(withIncrement).acceptWithIO(o)).doesNotThrowAnyException();
            assertThat(o.get()).isEqualTo(2);
        });

        assertThat(new AtomicInteger(0)).satisfies(o -> {
            assertThatThrownBy(() -> withError1.andThen(withIncrement).acceptWithIO(o)).isInstanceOf(Error1.class);
            assertThat(o.get()).isEqualTo(0);
        });

        assertThat(new AtomicInteger(0)).satisfies(o -> {
            assertThatThrownBy(() -> withIncrement.andThen(withError1).acceptWithIO(o)).isInstanceOf(Error1.class);
            assertThat(o.get()).isEqualTo(1);
        });

        assertThat(new AtomicInteger(0)).satisfies(o -> {
            assertThatThrownBy(() -> withError1.andThen(withError2).acceptWithIO(o)).isInstanceOf(Error1.class);
            assertThat(o.get()).isEqualTo(0);
        });
    }

    @Test
    public void testConsumerAsUnchecked() {
        assertThatCode(() -> IO.Consumer.noOp().asUnchecked().accept(null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> withError1.asUnchecked().accept(null))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testConsumerUnchecked() {
        assertThatNullPointerException().isThrownBy(() -> IO.Consumer.unchecked(null));

        assertThatCode(() -> IO.Consumer.unchecked(IO.Consumer.noOp()).accept(null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> IO.Consumer.unchecked(withError1).accept(null))
                .isInstanceOf(UncheckedIOException.class)
                .hasCauseExactlyInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testConsumerChecked() {
        assertThatNullPointerException().isThrownBy(() -> IO.Consumer.unchecked(null));

        assertThatCode(() -> IO.Consumer.checked(IO.Consumer.unchecked(IO.Consumer.noOp())).acceptWithIO(null)).doesNotThrowAnyException();
        assertThatThrownBy(() -> IO.Consumer.checked(IO.Consumer.unchecked(withError1)).acceptWithIO(null)).isInstanceOf(Error1.class);
    }

    @Test
    @SuppressWarnings("null")
    public void testConsumerThrowing() {
        assertThatNullPointerException().isThrownBy(() -> IO.Consumer.throwing(null));

        assertThatThrownBy(() -> IO.Consumer.throwing(Error1::new).acceptWithIO(null)).isInstanceOf(Error1.class);
    }

    @Test
    public void testConsumerNoOp() {
        assertThatCode(() -> IO.Consumer.noOp().acceptWithIO(null)).doesNotThrowAnyException();
    }
    //</editor-fold>

    @lombok.AllArgsConstructor
    private static final class X implements Closeable {

        private final List<String> events;

        static X open(List<String> stack) throws IOException {
            X result = new X(stack);
            result.events.add("open");
            return result;
        }

        public Closeable read() throws IOException {
            events.add("read");
            return this;
        }

        @Override
        public void close() throws IOException {
            events.add("close");
        }
    }

    @Test
    public void testParserValueOf() throws IOException {
        IO.Function<Object, X> openError = IO.Function.throwing(OpenError::new);
        IO.Function<X, Object> readError = IO.Function.throwing(ReadError::new);
        IO.Consumer<X> closeError = IO.Consumer.throwing(CloseError::new);

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Object> p = IO.Parser.valueOf(X::open, X::read, X::close);
            assertThatCode(() -> p.parseWithIO((List<String>) c)).doesNotThrowAnyException();
            assertThat(c).containsExactly("open", "read", "close");
        });

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Object> p = IO.Parser.valueOf(openError, X::read, X::close);
            assertThatThrownBy(() -> p.parseWithIO((List<String>) c)).isInstanceOf(OpenError.class);
            assertThat(c).isEmpty();
        });

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Object> p = IO.Parser.valueOf(X::open, readError, X::close);
            assertThatThrownBy(() -> p.parseWithIO((List<String>) c)).isInstanceOf(ReadError.class);
            assertThat(c).containsExactly("open", "close");
        });

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Object> p = IO.Parser.valueOf(X::open, X::read, closeError);
            assertThatThrownBy(() -> p.parseWithIO((List<String>) c)).isInstanceOf(CloseError.class);
            assertThat(c).containsExactly("open", "read");
        });

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Object> p = IO.Parser.valueOf(X::open, readError, closeError);
            assertThatThrownBy(() -> p.parseWithIO((List<String>) c)).isInstanceOf(ReadError.class).hasSuppressedException(new CloseError());
            assertThat(c).containsExactly("open");
        });
    }

    @Test
    public void testParserFlowOf() throws IOException {
        IO.Function<Object, X> openError = IO.Function.throwing(OpenError::new);
        IO.Function<X, Closeable> readError = IO.Function.throwing(ReadError::new);
        IO.Consumer<X> closeError = IO.Consumer.throwing(CloseError::new);

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Closeable> p = IO.Parser.flowOf(X::open, X::read, X::close);
            assertThatCode(() -> {
                try (AutoCloseable auto = p.parseWithIO((List<String>) c)) {
                }
            }).doesNotThrowAnyException();
            assertThat(c).containsExactly("open", "read", "close");
        });

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Closeable> p = IO.Parser.flowOf(X::open, X::read, X::close);
            assertThatThrownBy(() -> {
                try (AutoCloseable auto = p.parseWithIO((List<String>) c)) {
                    throw new Error1();
                }
            }).isInstanceOf(Error1.class);
            assertThat(c).containsExactly("open", "read", "close");
        });

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Closeable> p = IO.Parser.valueOf(openError, X::read, X::close);
            assertThatThrownBy(() -> {
                try (AutoCloseable auto = p.parseWithIO((List<String>) c)) {
                }
            }).isInstanceOf(OpenError.class);
            assertThat(c).isEmpty();
        });

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Closeable> p = IO.Parser.valueOf(X::open, readError, X::close);
            assertThatThrownBy(() -> {
                try (AutoCloseable auto = p.parseWithIO((List<String>) c)) {
                }
            }).isInstanceOf(ReadError.class);
            assertThat(c).containsExactly("open", "close");
        });

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Closeable> p = IO.Parser.valueOf(X::open, X::read, closeError);
            assertThatThrownBy(() -> {
                try (AutoCloseable auto = p.parseWithIO((List<String>) c)) {
                }
            }).isInstanceOf(CloseError.class);
            assertThat(c).containsExactly("open", "read");
        });

        assertThat(new ArrayList<String>()).satisfies(c -> {
            IO.Parser<List<String>, Closeable> p = IO.Parser.valueOf(X::open, readError, closeError);
            assertThatThrownBy(() -> {
                try (AutoCloseable auto = p.parseWithIO((List<String>) c)) {
                }
            }).isInstanceOf(ReadError.class).hasSuppressedException(new CloseError());
            assertThat(c).containsExactly("open");
        });
    }

    private static final class OpenError extends IOException {
    }

    private static final class ReadError extends IOException {
    }

    private static final class CloseError extends IOException {
    }

    private static final class Error1 extends IOException {
    }

    private static final class Error2 extends IOException {
    }
}
