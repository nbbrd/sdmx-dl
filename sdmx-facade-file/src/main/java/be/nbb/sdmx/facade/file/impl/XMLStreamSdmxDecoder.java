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
package be.nbb.sdmx.facade.file.impl;

import be.nbb.sdmx.facade.DataStructure;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLInputFactory;
import be.nbb.sdmx.facade.file.SdmxDecoder;
import static be.nbb.sdmx.facade.file.SdmxDecoder.DataType.COMPACT20;
import static be.nbb.sdmx.facade.file.SdmxDecoder.DataType.COMPACT21;
import static be.nbb.sdmx.facade.file.SdmxDecoder.DataType.GENERIC20;
import static be.nbb.sdmx.facade.file.SdmxDecoder.DataType.GENERIC21;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import java.io.Reader;
import java.nio.file.Files;

/**
 *
 * @author Philippe Charles
 */
public final class XMLStreamSdmxDecoder implements SdmxDecoder {

    private final XMLInputFactory factory;
    private final String preferredLang;

    public XMLStreamSdmxDecoder(XMLInputFactory factory, String preferredLang) {
        this.factory = factory;
        this.preferredLang = preferredLang;
    }

    @Override
    public Info decode(File data, File structure) throws IOException {
        DataType dataType = probeDataType(data);
        return Info.of(dataType, structure != null
                ? parseDataStructure(dataType, structure)
                : decodeDataStructure(dataType, data));
    }

    private DataType probeDataType(File data) throws IOException {
        try (Reader stream = Files.newBufferedReader(data.toPath())) {
            return DataTypeProbe.probeDataType(factory, stream);
        }
    }

    private DataStructure parseDataStructure(DataType dataType, File structure) throws IOException {
        switch (dataType) {
            case GENERIC20:
            case COMPACT20:
                try (Reader stream = Files.newBufferedReader(structure.toPath(), StandardCharsets.UTF_8)) {
                    return SdmxXmlStreams.struct20(factory, stream, preferredLang).get(0);
                }
            case GENERIC21:
            case COMPACT21:
                try (Reader stream = Files.newBufferedReader(structure.toPath(), StandardCharsets.UTF_8)) {
                    return SdmxXmlStreams.struct21(factory, stream, preferredLang).get(0);
                }
            default:
                throw new IOException("Don't know how to handle '" + dataType + "'");
        }
    }

    private DataStructure decodeDataStructure(DataType dataType, File data) throws IOException {
        try (Reader stream = Files.newBufferedReader(data.toPath())) {
            return DataStructureDecoder.decodeDataStructure(dataType, factory, stream);
        }
    }
}
