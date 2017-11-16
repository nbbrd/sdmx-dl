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
package be.nbb.sdmx.facade.xml.stream;

import be.nbb.sdmx.facade.util.IO;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Stax {

    @FunctionalInterface
    public interface Supplier<T> {

        T getWithStream() throws XMLStreamException;
    }

    @FunctionalInterface
    public interface Function<T, R> {

        R applyWithStream(T t) throws XMLStreamException;
    }

    @FunctionalInterface
    public interface Parser<T> {

        @Nonnull
        default T parseFile(@Nonnull XMLInputFactory xf, @Nonnull File file, @Nonnull Charset cs) throws IOException {
            return parseStream(xf, () -> new FileInputStream(file), cs);
        }

        @Nonnull
        default T parseFile(@Nonnull XMLInputFactory xf, @Nonnull Path path, @Nonnull Charset cs) throws IOException {
            try {
                return parseStream(xf, () -> new FileInputStream(path.toFile()), cs);
            } catch (UnsupportedOperationException ex) {
                return parseReader(xf, () -> Files.newBufferedReader(path, cs));
            }
        }

        @Nonnull
        default T parseStream(@Nonnull XMLInputFactory xf, @Nonnull IO.Supplier<? extends InputStream> source, @Nonnull Charset cs) throws IOException {
            InputStream stream = source.getWithIO();
            return parse(() -> xf.createXMLStreamReader(stream, cs.name()), stream);
        }

        @Nonnull
        default T parseReader(@Nonnull XMLInputFactory xf, @Nonnull IO.Supplier<? extends Reader> source) throws IOException {
            Reader reader = source.getWithIO();
            return parse(() -> xf.createXMLStreamReader(reader), reader);
        }

        @Nonnull
        default T parse(@Nonnull Supplier<? extends XMLStreamReader> supplier, @Nonnull Closeable onClose) throws IOException {
            try {
                XMLStreamReader xml = supplier.getWithStream();
                return parse(xml, () -> closeBoth(xml, onClose));
            } catch (XMLStreamException ex) {
                ensureClosed(ex, onClose);
                throw new XMLStreamIOException("Failed to create XMLStreamReader", ex);
            }
        }

        @Nonnull
        T parse(@Nonnull XMLStreamReader reader, @Nonnull Closeable onClose) throws IOException;

        @Nonnull
        static <R> Parser<R> of(@Nonnull Function<XMLStreamReader, R> func) {
            return (reader, onClose) -> {
                try (Closeable c = onClose) {
                    return func.applyWithStream(reader);
                } catch (XMLStreamException ex) {
                    throw new XMLStreamIOException(ex);
                }
            };
        }
    }

    public static final class XMLStreamIOException extends IOException {

        public XMLStreamIOException(XMLStreamException ex) {
            super(ex);
        }

        public XMLStreamIOException(String message, XMLStreamException ex) {
            super(message, ex);
        }
    }

    void closeBoth(XMLStreamReader reader, Closeable onClose) throws IOException {
        try {
            reader.close();
        } catch (XMLStreamException ex) {
            ensureClosed(ex, onClose);
            throw new Stax.XMLStreamIOException("Failed to close xml stream reader", ex);
        }
        onClose.close();
    }

    void ensureClosed(XMLStreamException ex, Closeable onClose) throws IOException {
        try {
            onClose.close();
        } catch (IOException suppressed) {
            ex.addSuppressed(suppressed);
        }
    }
}
