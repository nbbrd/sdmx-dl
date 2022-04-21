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
package internal.sdmxdl.provider.ri.file;

import nbbrd.io.xml.Xml;
import sdmxdl.DataStructure;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.format.MediaType;
import sdmxdl.format.xml.*;
import sdmxdl.provider.file.SdmxFileInfo;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class XmlDecoder implements SdmxDecoder {

    @lombok.NonNull
    private final BiConsumer<? super SdmxFileSource, ? super String> eventListener;

    @Override
    public SdmxFileInfo decode(SdmxFileSource source, LanguagePriorityList langs) throws IOException {
        MediaType type = probeDataType(source);
        return SdmxFileInfo.of(type, loadStructure(source, langs, type));
    }

    private MediaType probeDataType(SdmxFileSource source) throws IOException {
        if (eventListener != SdmxManager.NO_OP_EVENT_LISTENER) {
            eventListener.accept(source, "Probing data type from '" + source.getData() + "'");
        }
        return XmlMediaTypeProbe.of()
                .parseFile(source.getData())
                .orElseThrow(() -> new IOException("Cannot probe data type"));
    }

    private DataStructure loadStructure(SdmxFileSource source, LanguagePriorityList langs, MediaType type) throws IOException {
        return XmlFileSource.isValidFile(source.getStructure())
                ? parseStruct(type, langs, source)
                : decodeStruct(type, source);
    }

    private DataStructure parseStruct(MediaType dataType, LanguagePriorityList langs, SdmxFileSource source) throws IOException {
        if (eventListener != SdmxManager.NO_OP_EVENT_LISTENER) {
            eventListener.accept(source, "Parsing structure from '" + source.getStructure() + "' with data type '" + dataType + "'");
        }
        return getStructParser(dataType, langs)
                .parseFile(source.getStructure())
                .stream()
                .findFirst()
                .orElseThrow(IOException::new);
    }

    private Xml.Parser<List<DataStructure>> getStructParser(MediaType dataType, LanguagePriorityList langs) throws IOException {
        if (XmlMediaTypes.GENERIC_DATA_20.equals(dataType) || XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_20.equals(dataType)) {
            return SdmxXmlStreams.struct20(langs);
        } else if (XmlMediaTypes.GENERIC_DATA_21.equals(dataType) || XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_21.equals(dataType)) {
            return SdmxXmlStreams.struct21(langs);
        }
        throw new IOException("Don't know how to handle '" + dataType + "'");
    }

    private DataStructure decodeStruct(MediaType dataType, SdmxFileSource source) throws IOException {
        if (eventListener != SdmxManager.NO_OP_EVENT_LISTENER) {
            eventListener.accept(source, "Decoding structure from '" + source.getData() + "' with data type '" + dataType + "'");
        }
        return getStructDecoder(dataType)
                .parseFile(source.getData());
    }

    private static Xml.Parser<DataStructure> getStructDecoder(MediaType o) throws IOException {
        if (XmlMediaTypes.GENERIC_DATA_20.equals(o)) {
            return DataStructureDecoder.generic20();
        } else if (XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_20.equals(o)) {
            return DataStructureDecoder.compact20();
        } else if (XmlMediaTypes.GENERIC_DATA_21.equals(o)) {
            return DataStructureDecoder.generic21();
        } else if (XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_21.equals(o)) {
            return DataStructureDecoder.compact21();
        }
        throw new IOException("Don't know how to handle '" + o + "'");
    }
}
