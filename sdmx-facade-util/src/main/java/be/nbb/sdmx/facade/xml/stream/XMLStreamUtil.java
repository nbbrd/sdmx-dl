/*
 * Copyright 2015 National Bank of Belgium
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

import java.util.function.Supplier;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class XMLStreamUtil {

    enum Status {
        HALT, CONTINUE, SUSPEND;
    }

    interface Func {

        Status visitTag(boolean start, String localName) throws XMLStreamException;
    }

    static boolean nextWhile(XMLStreamReader reader, Func func) throws XMLStreamException {
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                switch (func.visitTag(true, reader.getLocalName())) {
                    case CONTINUE:
                        break;
                    case HALT:
                        return false;
                    case SUSPEND:
                        return true;
                }
            } else if (event == XMLStreamReader.END_ELEMENT) {
                switch (func.visitTag(false, reader.getLocalName())) {
                    case CONTINUE:
                        break;
                    case HALT:
                        return false;
                    case SUSPEND:
                        return true;
                }
            }
        }
        return false;
    }

    static boolean nextTags(XMLStreamReader reader, String tag) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    return true;
                case XMLStreamReader.END_ELEMENT:
                    if (tag.equals(reader.getLocalName())) {
                        return false;
                    }
                    break;
            }
        }
        return false;
    }

    static boolean nextTag(XMLStreamReader reader, String end, String start) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case XMLStreamReader.START_ELEMENT:
                    if (start.equals(reader.getLocalName())) {
                        return true;
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    if (end.equals(reader.getLocalName())) {
                        return false;
                    }
                    break;
            }
        }
        return false;
    }

    static void check(boolean expression, XMLStreamReader reader, String message, Object... args) throws XMLStreamException {
        check(expression, reader::getLocation, message, args);
    }

    static void check(boolean expression, Supplier<Location> location, String message, Object... args) throws XMLStreamException {
        if (!expression) {
            throw new XMLStreamException(String.format(message, args), location.get());
        }
    }

    static int toInt(String input, int defaultValue) {
        if (input != null) {
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
            }
        }
        return defaultValue;
    }
}
