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

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
interface GenericDataParser {

    void parseValueElement(@Nonnull XMLStreamReader r, @Nonnull BiConsumer<String, String> c) throws XMLStreamException;

    void parseTimeElement(@Nonnull XMLStreamReader r, @Nonnull Consumer<String> c) throws XMLStreamException;

    @Nonnull
    String getTimeELement();

    @Nonnull
    static GenericDataParser sdmx20() {
        return new GenericDataParser() {
            @Override
            public void parseValueElement(XMLStreamReader r, BiConsumer<String, String> c) throws XMLStreamException {
                c.accept(r.getAttributeValue(null, "concept"), r.getAttributeValue(null, "value"));
            }

            @Override
            public void parseTimeElement(XMLStreamReader r, Consumer<String> c) throws XMLStreamException {
                c.accept(r.getElementText());
            }

            @Override
            public String getTimeELement() {
                return "Time";
            }
        };
    }

    @Nonnull
    static GenericDataParser sdmx21() {
        return new GenericDataParser() {
            @Override
            public void parseValueElement(XMLStreamReader r, BiConsumer<String, String> c) throws XMLStreamException {
                c.accept(r.getAttributeValue(null, "id"), r.getAttributeValue(null, "value"));
            }

            @Override
            public void parseTimeElement(XMLStreamReader r, Consumer<String> c) throws XMLStreamException {
                c.accept(r.getAttributeValue(null, "value"));
            }

            @Override
            public String getTimeELement() {
                return "ObsDimension";
            }
        };
    }
}
