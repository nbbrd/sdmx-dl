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
package internal.sdmxdl.file.xml;

import sdmxdl.DataStructure;
import sdmxdl.LanguagePriorityList;
import java.io.File;
import java.io.IOException;

import sdmxdl.file.SdmxFileSet;
import sdmxdl.xml.stream.SdmxXmlStreams;
import java.util.List;
import internal.sdmxdl.file.SdmxDecoder;
import internal.sdmxdl.file.SdmxFileUtil;
import nbbrd.io.xml.Xml;

/**
 *
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public final class StaxSdmxDecoder implements SdmxDecoder {

    @Override
    public Info decode(SdmxFileSet files, LanguagePriorityList langs) throws IOException {
        DataType type = probeDataType(files.getData());
        File structure = files.getStructure();
        return Info.of(type, SdmxFileUtil.isValidFile(structure)
                ? parseStruct(type, langs, structure)
                : decodeStruct(type, files.getData()));
    }

    private DataType probeDataType(File data) throws IOException {
        return DataTypeProbe.of().parseFile(data);
    }

    private DataStructure parseStruct(DataType dataType, LanguagePriorityList langs, File structure) throws IOException {
        return getStructParser(dataType, langs).parseFile(structure).get(0);
    }

    private Xml.Parser<List<DataStructure>> getStructParser(DataType o, LanguagePriorityList langs) throws IOException {
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
        return getStructDecoder(dataType).parseFile(data);
    }

    private static Xml.Parser<DataStructure> getStructDecoder(SdmxDecoder.DataType o) throws IOException {
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
