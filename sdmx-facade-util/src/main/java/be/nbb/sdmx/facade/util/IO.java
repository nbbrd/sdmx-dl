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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class IO {

    @FunctionalInterface
    public interface Supplier<T> {

        T getWithIO() throws IOException;
    }

    @FunctionalInterface
    public interface Function<T, R> {

        R applyWithIO(T t) throws IOException;
    }

    @Nonnull
    public <T extends Closeable, R> Stream<R> stream(IO.Supplier<T> supplier, IO.Function<T, Stream<R>> stream) throws IOException {
        T resource = supplier.getWithIO();
        try {
            return stream.applyWithIO(resource).onClose(asUncheckedRunnable(resource));
        } catch (Error | RuntimeException e) {
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
    public <T> Stream<T> streamNonnull(@Nonnull IO.Supplier<T> nextSupplier) {
        Iterator<T> iter = new Iterator<T>() {
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
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    private Runnable asUncheckedRunnable(Closeable c) {
        return () -> {
            try {
                c.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }
}
