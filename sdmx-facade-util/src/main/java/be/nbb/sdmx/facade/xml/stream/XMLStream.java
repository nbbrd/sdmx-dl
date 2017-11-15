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
 * @param <T>
 */
public interface XMLStream<T> {

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
    default T parseStream(@Nonnull XMLInputFactory xf, @Nonnull IO.Supplier<? extends InputStream> supplier, @Nonnull Charset cs) throws IOException {
        InputStream stream = supplier.getWithIO();

        try {
            XMLStreamReader xml = xf.createXMLStreamReader(stream, cs.name());
            return parse(xml, () -> XMLStreamUtil.closeBoth(xml, stream));
        } catch (XMLStreamException ex) {
            XMLStreamUtil.ensureClosed(ex, stream);
            throw new IOException("Failed to create XMLStreamReader from a stream", ex);
        }
    }

    @Nonnull
    default T parseReader(@Nonnull XMLInputFactory xf, @Nonnull IO.Supplier<? extends Reader> supplier) throws IOException {
        Reader reader = supplier.getWithIO();

        try {
            XMLStreamReader xml = xf.createXMLStreamReader(reader);
            return parse(xml, () -> XMLStreamUtil.closeBoth(xml, reader));
        } catch (XMLStreamException ex) {
            XMLStreamUtil.ensureClosed(ex, reader);
            throw new IOException("Failed to create XMLStreamReader from a reader", ex);
        }
    }

    @Nonnull
    T parse(@Nonnull XMLStreamReader reader, @Nonnull Closeable onClose) throws IOException;

    @Nonnull
    static <T> XMLStream<T> of(@Nonnull XFunc<T> func) {
        return (reader, onClose) -> {
            try (Closeable c = onClose) {
                return func.apply(reader);
            } catch (XMLStreamException ex) {
                throw new IOException(ex);
            }
        };
    }
}
