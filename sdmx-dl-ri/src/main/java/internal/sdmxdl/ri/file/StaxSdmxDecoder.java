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
package internal.sdmxdl.ri.file;

import nbbrd.io.xml.Xml;
import sdmxdl.DataStructure;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxMediaType;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.xml.SdmxmlDataTypeProbe;
import sdmxdl.xml.XmlFileSource;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.io.IOException;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class StaxSdmxDecoder implements SdmxDecoder {

    @lombok.NonNull
    private final SdmxFileListener eventListener;

    @Override
    public Info decode(SdmxFileSource source, LanguagePriorityList langs) throws IOException {
        String type = probeDataType(source);
        return Info.of(type, loadStructure(source, langs, type));
    }

    private String probeDataType(SdmxFileSource source) throws IOException {
        if (eventListener.isEnabled()) {
            eventListener.onFileSourceEvent(source, "Probing data type from '" + source.getData() + "'");
        }
        return SdmxmlDataTypeProbe.of()
                .parseFile(source.getData());
    }

    private DataStructure loadStructure(SdmxFileSource source, LanguagePriorityList langs, String type) throws IOException {
        return XmlFileSource.isValidFile(source.getStructure())
                ? parseStruct(type, langs, source)
                : decodeStruct(type, source);
    }

    private DataStructure parseStruct(String dataType, LanguagePriorityList langs, SdmxFileSource source) throws IOException {
        if (eventListener.isEnabled()) {
            eventListener.onFileSourceEvent(source, "Parsing structure from '" + source.getStructure() + "' with data type '" + dataType + "'");
        }
        return getStructParser(dataType, langs)
                .parseFile(source.getStructure())
                .stream()
                .findFirst()
                .orElseThrow(IOException::new);
    }

    private Xml.Parser<List<DataStructure>> getStructParser(String dataType, LanguagePriorityList langs) throws IOException {
        switch (dataType) {
            case SdmxMediaType.GENERIC_DATA_20:
            case SdmxMediaType.STRUCTURE_SPECIFIC_DATA_20:
                return SdmxXmlStreams.struct20(langs);
            case SdmxMediaType.GENERIC_DATA_21:
            case SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21:
                return SdmxXmlStreams.struct21(langs);
            default:
                throw new IOException("Don't know how to handle '" + dataType + "'");
        }
    }

    private DataStructure decodeStruct(String dataType, SdmxFileSource source) throws IOException {
        if (eventListener.isEnabled()) {
            eventListener.onFileSourceEvent(source, "Decoding structure from '" + source.getData() + "' with data type '" + dataType + "'");
        }
        return getStructDecoder(dataType)
                .parseFile(source.getData());
    }

    private static Xml.Parser<DataStructure> getStructDecoder(String o) throws IOException {
        switch (o) {
            case SdmxMediaType.GENERIC_DATA_20:
                return DataStructureDecoder.generic20();
            case SdmxMediaType.STRUCTURE_SPECIFIC_DATA_20:
                return DataStructureDecoder.compact20();
            case SdmxMediaType.GENERIC_DATA_21:
                return DataStructureDecoder.generic21();
            case SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21:
                return DataStructureDecoder.compact21();
            default:
                throw new IOException("Don't know how to handle '" + o + "'");
        }
    }
}
