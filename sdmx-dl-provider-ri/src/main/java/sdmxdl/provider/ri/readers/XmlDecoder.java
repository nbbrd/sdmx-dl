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
package sdmxdl.provider.ri.readers;

import lombok.NonNull;
import nbbrd.io.net.MediaType;
import nbbrd.io.xml.Xml;
import org.jspecify.annotations.Nullable;
import sdmxdl.EventListener;
import sdmxdl.Languages;
import sdmxdl.Structure;
import sdmxdl.file.FileSource;
import sdmxdl.format.xml.*;
import sdmxdl.provider.file.FileInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class XmlDecoder implements Decoder {

    private final @Nullable EventListener listener;

    @Override
    public @NonNull FileInfo decode(@NonNull FileSource source, @NonNull Languages langs) throws IOException {
        MediaType type = probeDataType(source);
        return FileInfo.of(type, loadStructure(source, langs, type));
    }

    private MediaType probeDataType(FileSource source) throws IOException {
        if (listener != null) {
            listener.accept(MARKER, "Probing data type from '" + source.getData() + "'");
        }
        return XmlMediaTypeProbe.of()
                .parseFile(source.getData())
                .orElseThrow(() -> new IOException("Cannot probe data type"));
    }

    private Structure loadStructure(FileSource source, Languages langs, MediaType type) throws IOException {
        return XmlFileSource.isValidFile(source.getStructure())
                ? parseStruct(type, langs, source.getStructure())
                : decodeStruct(type, source);
    }

    private Structure parseStruct(MediaType dataType, Languages langs, File structure) throws IOException {
        if (listener != null) {
            listener.accept(MARKER, "Parsing structure from '" + structure + "' with data type '" + dataType + "'");
        }
        return getStructParser(dataType, langs)
                .parseFile(structure)
                .stream()
                .findFirst()
                .orElseThrow(IOException::new);
    }

    private Xml.Parser<List<Structure>> getStructParser(MediaType dataType, Languages langs) throws IOException {
        if (XmlMediaTypes.GENERIC_DATA_20.equals(dataType) || XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_20.equals(dataType)) {
            return SdmxXmlStreams.struct20(langs);
        } else if (XmlMediaTypes.GENERIC_DATA_21.equals(dataType) || XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_21.equals(dataType)) {
            return SdmxXmlStreams.struct21(langs);
        }
        throw new IOException("Don't know how to handle '" + dataType + "'");
    }

    private Structure decodeStruct(MediaType dataType, FileSource source) throws IOException {
        if (listener != null) {
            listener.accept(MARKER, "Decoding structure from '" + source.getData() + "' with data type '" + dataType + "'");
        }
        return getStructDecoder(dataType)
                .parseFile(source.getData());
    }

    private static Xml.Parser<Structure> getStructDecoder(MediaType o) throws IOException {
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

    private static final String MARKER = "XML_DECODER";
}
