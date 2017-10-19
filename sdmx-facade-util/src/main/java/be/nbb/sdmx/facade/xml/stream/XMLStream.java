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

import java.io.IOException;
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
    default T get(@Nonnull XMLInputFactory xf, @Nonnull Path path, @Nonnull Charset cs) throws IOException {
        return get(xf, Files.newBufferedReader(path, cs));
    }

    @Nonnull
    default T get(@Nonnull XMLInputFactory xf, @Nonnull Reader stream) throws IOException {
        XMLStreamReader reader;

        try {
            reader = xf.createXMLStreamReader(stream);
        } catch (XMLStreamException ex) {
            throw new IOException("Failed to create XML reader", ex);
        }

        return get(reader);
    }

    @Nonnull
    T get(@Nonnull XMLStreamReader reader) throws IOException;
}
