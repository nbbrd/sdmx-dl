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
package sdmxdl.xml;

import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;
import sdmxdl.ext.SdmxMediaType;
import sdmxdl.xml.stream.StaxUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static internal.sdmxdl.xml.Sdmxml.*;

/**
 * @author Philippe Charles
 */
public final class SdmxmlDataTypeProbe {

    public static Xml.Parser<String> of() {
        return Stax.StreamParser.valueOf(SdmxmlDataTypeProbe::probeDataType);
    }

    private static final String UNSUPPORTED_TYPE = null;
    private static final String UNKNOWN_TYPE = null;
    private static final String DATASET_TAG = "DataSet";
    private static final String SERIES_TAG = "Series";
    private static final String SERIES_KEY_TAG = "SeriesKey";

    private static String probeDataType(XMLStreamReader reader) throws XMLStreamException {
        if (StaxUtil.isNotNamespaceAware(reader)) {
            throw new XMLStreamException("Cannot probe data type");
        }

        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    URI uri = URI.create(reader.getNamespaceURI());
                    if (MESSAGE_V10.is(uri)) {
                        return UNSUPPORTED_TYPE;
                    } else if (MESSAGE_V20.is(uri)) {
                        return parse20(reader);
                    } else if (MESSAGE_V21.is(uri)) {
                        return parse21(reader);
                    } else {
                        return UNKNOWN_TYPE;
                    }
            }
        }
        return null;
    }

    private static String parse21(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getLocalName().equals(DATASET_TAG)) {
                        return parseDataSet21(reader);
                    }
                    break;
            }
        }
        return UNKNOWN_TYPE;
    }

    private static String parseDataSet21(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getLocalName().equals(SERIES_TAG)) {
                        return hasSeriesKeyTag(reader) ? SdmxMediaType.GENERIC_DATA_21 : SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21;
                    }
                    break;
            }
        }
        return UNKNOWN_TYPE;
    }

    private static String parse20(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getLocalName().equals(DATASET_TAG)) {
                        return parseDataSet20(reader);
                    }
                    break;
            }
        }
        return UNKNOWN_TYPE;
    }

    private static String parseDataSet20(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getLocalName().equals(SERIES_TAG)) {
                        return hasSeriesKeyTag(reader) ? SdmxMediaType.GENERIC_DATA_20 : SdmxMediaType.STRUCTURE_SPECIFIC_DATA_20;
                    }
                    break;
            }
        }
        return UNKNOWN_TYPE;
    }

    private static boolean hasSeriesKeyTag(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getLocalName().equals(SERIES_KEY_TAG)) {
                        return true;
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getLocalName().equals(SERIES_TAG)) {
                        return false;
                    }
            }
        }
        return false;
    }
}
