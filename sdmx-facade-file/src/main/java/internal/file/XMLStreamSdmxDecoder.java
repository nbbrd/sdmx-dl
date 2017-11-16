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
package internal.file;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.LanguagePriorityList;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLInputFactory;
import static internal.file.SdmxDecoder.DataType.COMPACT20;
import static internal.file.SdmxDecoder.DataType.COMPACT21;
import static internal.file.SdmxDecoder.DataType.GENERIC20;
import static internal.file.SdmxDecoder.DataType.GENERIC21;
import be.nbb.sdmx.facade.file.SdmxFileSet;
import be.nbb.sdmx.facade.xml.stream.SdmxXmlStreams;
import be.nbb.sdmx.facade.xml.stream.XMLStream;
import java.util.List;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class XMLStreamSdmxDecoder implements SdmxDecoder {

    private final XMLInputFactory factory;

    @Override
    public Info decode(SdmxFileSet files, LanguagePriorityList langs) throws IOException {
        DataType dataType = probeDataType(files.getData());
        return Info.of(dataType, files.getStructure() != null
                ? parseStruct(dataType, langs, files.getStructure())
                : decodeStruct(dataType, files.getData()));
    }

    private DataType probeDataType(File data) throws IOException {
        return DataTypeProbe.of().parseFile(factory, data, StandardCharsets.UTF_8);
    }

    private DataStructure parseStruct(DataType dataType, LanguagePriorityList langs, File structure) throws IOException {
        return getStructParser(dataType, langs).parseFile(factory, structure, StandardCharsets.UTF_8).get(0);
    }

    private XMLStream<List<DataStructure>> getStructParser(DataType o, LanguagePriorityList langs) throws IOException {
        switch (o) {
            case GENERIC20:
            case COMPACT20:
                return SdmxXmlStreams.struct20(langs);
            case GENERIC21:
            case COMPACT21:
                return SdmxXmlStreams.struct21(langs);
            default:
                throw new IOException("Don't know how to handle '" + o + "'");
        }
    }

    private DataStructure decodeStruct(DataType dataType, File data) throws IOException {
        if (factory.isPropertySupported(XMLInputFactory.IS_NAMESPACE_AWARE)
                && (Boolean) factory.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE)) {
            factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
            try {
                return getStructDecoder(dataType).parseFile(factory, data, StandardCharsets.UTF_8);
            } finally {
                factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
            }
        } else {
            return getStructDecoder(dataType).parseFile(factory, data, StandardCharsets.UTF_8);
        }
    }

    private static XMLStream<DataStructure> getStructDecoder(SdmxDecoder.DataType o) throws IOException {
        switch (o) {
            case GENERIC20:
                return DataStructureDecoder.generic20();
            case COMPACT20:
                return DataStructureDecoder.compact20();
            case GENERIC21:
                return DataStructureDecoder.generic21();
            case COMPACT21:
                return DataStructureDecoder.compact21();
            default:
                throw new IOException("Don't know how to handle '" + o + "'");
        }
    }
}
