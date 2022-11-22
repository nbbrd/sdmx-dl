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
package sdmxdl.format.xml;

import internal.sdmxdl.format.xml.XMLStreamUtil;
import nbbrd.io.net.MediaType;
import nbbrd.io.xml.Stax;
import nbbrd.io.xml.Xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.util.Optional;

import static internal.sdmxdl.format.xml.Sdmxml.*;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * @author Philippe Charles
 */
@SuppressWarnings("SwitchStatementWithTooFewBranches")
public final class XmlMediaTypeProbe {

    public static Xml.Parser<Optional<MediaType>> of() {
        return Stax.StreamParser.valueOf(XmlMediaTypeProbe::probeDataType);
    }

    private static final MediaType UNSUPPORTED_TYPE = null;
    private static final MediaType UNKNOWN_TYPE = null;

    private static final String DATASET_TAG = "DataSet";
    private static final String SERIES_TAG = "Series";
    private static final String SERIES_KEY_TAG = "SeriesKey";

    private static Optional<MediaType> probeDataType(XMLStreamReader reader) throws XMLStreamException {
        return Optional.ofNullable(probeDataTypeOrNull(reader));
    }

    private static MediaType probeDataTypeOrNull(XMLStreamReader reader) throws XMLStreamException {
        if (XMLStreamUtil.isNotNamespaceAware(reader)) {
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

    private static MediaType parse21(XMLStreamReader reader) throws XMLStreamException {
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

    private static MediaType parseDataSet21(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getLocalName().equals(SERIES_TAG)) {
                        return hasSeriesKeyTag(reader) ? XmlMediaTypes.GENERIC_DATA_21 : XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_21;
                    }
                    break;
            }
        }
        return UNKNOWN_TYPE;
    }

    private static MediaType parse20(XMLStreamReader reader) throws XMLStreamException {
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

    private static MediaType parseDataSet20(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext()) {
            switch (reader.next()) {
                case START_ELEMENT:
                    if (reader.getLocalName().equals(SERIES_TAG)) {
                        return hasSeriesKeyTag(reader) ? XmlMediaTypes.GENERIC_DATA_20 : XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_20;
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
