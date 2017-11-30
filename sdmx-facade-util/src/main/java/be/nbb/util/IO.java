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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class IO {

    @FunctionalInterface
    public interface Runnable {

        void runWithIO() throws IOException;

        @Nonnull
        default java.lang.Runnable asUnchecked() {
            return () -> {
                try {
                    runWithIO();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }

        @Nonnull
        static Runnable throwing(@Nonnull java.util.function.Supplier<? extends IOException> ex) {
            Objects.requireNonNull(ex);
            return () -> {
                throw ex.get();
            };
        }

        @Nonnull
        static Runnable noOp() {
            return () -> {
            };
        }
    }

    @FunctionalInterface
    public interface Supplier<T> {

        T getWithIO() throws IOException;

        @Nonnull
        default java.util.function.Supplier<T> asUnchecked() {
            return () -> {
                try {
                    return getWithIO();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }

        @Nonnull
        static <T> Supplier<T> throwing(@Nonnull java.util.function.Supplier<? extends IOException> ex) {
            Objects.requireNonNull(ex);
            return () -> {
                throw ex.get();
            };
        }

        @Nonnull
        @SuppressWarnings("null")
        static <T> Supplier<T> of(@Nullable T t) {
            return () -> t;
        }
    }

    @FunctionalInterface
    public interface Function<T, R> {

        R applyWithIO(T t) throws IOException;

        @Nonnull
        default java.util.function.Function<T, R> asUnchecked() {
            return (T t) -> {
                try {
                    return applyWithIO(t);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }

        @Nonnull
        static <T, R> Function<T, R> throwing(@Nonnull java.util.function.Supplier<? extends IOException> ex) {
            Objects.requireNonNull(ex);
            return o -> {
                throw ex.get();
            };
        }

        @Nonnull
        @SuppressWarnings("null")
        static <T, R> Function<T, R> of(@Nullable R r) {
            return o -> r;
        }
    }

    @FunctionalInterface
    public interface Predicate<T> {

        boolean testWithIO(T t) throws IOException;

        @Nonnull
        default java.util.function.Predicate<T> asUnchecked() {
            return (T t) -> {
                try {
                    return testWithIO(t);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }

        @Nonnull
        static <T> Predicate<T> throwing(@Nonnull java.util.function.Supplier<? extends IOException> ex) {
            Objects.requireNonNull(ex);
            return o -> {
                throw ex.get();
            };
        }

        @Nonnull
        static <T> Predicate<T> of(boolean r) {
            return o -> r;
        }
    }

    @FunctionalInterface
    public interface Consumer<T> {

        void acceptWithIO(T t) throws IOException;

        @Nonnull
        default java.util.function.Consumer<T> asUnchecked() {
            return (T t) -> {
                try {
                    acceptWithIO(t);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            };
        }

        @Nonnull
        static <T> Consumer<T> throwing(@Nonnull java.util.function.Supplier<? extends IOException> ex) {
            Objects.requireNonNull(ex);
            return o -> {
                throw ex.get();
            };
        }

        @Nonnull
        static <T> Consumer<T> noOp() {
            return o -> {
            };
        }
    }

    @FunctionalInterface
    public interface Parser<T, R> {

        R parseWithIO(@Nonnull Supplier<? extends T> input) throws IOException;
    }

    @Nonnull
    public <T extends Closeable, R> Stream<R> stream(@Nonnull Supplier<T> supplier, @Nonnull Function<T, Stream<R>> stream) throws IOException {
        T resource = supplier.getWithIO();
        try {
            return stream.applyWithIO(resource).onClose(asUnchecked(resource::close));
        } catch (Error | RuntimeException | IOException e) {
            try {
                resource.close();
            } catch (IOException ex) {
                try {
                    e.addSuppressed(ex);
                } catch (Throwable ignore) {
                }
            }
            throw e;
        }
    }

    @Nonnull
    public <T> Stream<T> streamNonnull(@Nonnull Supplier<T> nextSupplier) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(asIterator(nextSupplier), Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    @Nonnull
    private <T> Iterator<T> asIterator(@Nonnull Supplier<T> nextSupplier) {
        Objects.requireNonNull(nextSupplier);
        return new Iterator<T>() {
            T nextElement = null;

            @Override
            public boolean hasNext() {
                if (nextElement != null) {
                    return true;
                } else {
                    try {
                        nextElement = nextSupplier.getWithIO();
                        return (nextElement != null);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            @Override
            public T next() {
                if (nextElement != null || hasNext()) {
                    T line = nextElement;
                    nextElement = null;
                    return line;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    @Nonnull
    public java.lang.Runnable asUnchecked(@Nonnull Runnable o) {
        return o.asUnchecked();
    }

    @Nonnull
    public <T, R> java.util.function.Function<T, R> asUnchecked(@Nonnull Function<T, R> o) {
        return o.asUnchecked();
    }

    @Nonnull
    public <T> java.util.function.Predicate<T> asUnchecked(@Nonnull Predicate<T> o) {
        return o.asUnchecked();
    }

    @Nonnull
    public <T> java.util.function.Consumer<T> asUnchecked(@Nonnull Consumer<T> o) {
        return o.asUnchecked();
    }
}
