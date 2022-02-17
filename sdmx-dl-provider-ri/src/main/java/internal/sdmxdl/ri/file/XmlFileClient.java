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
package internal.sdmxdl.ri.file;

import nbbrd.io.xml.Xml;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.SdmxMediaType;
import sdmxdl.file.SdmxFileListener;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.util.file.SdmxFileClient;
import sdmxdl.util.file.SdmxFileInfo;
import sdmxdl.util.parser.ObsFactories;
import sdmxdl.xml.DataCursor;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public class XmlFileClient implements SdmxFileClient {

    @lombok.NonNull
    private final SdmxFileSource source;

    @lombok.NonNull
    private final LanguagePriorityList languages;

    @lombok.NonNull
    private final SdmxDecoder decoder;

    @Nullable
    private final ObsFactory obsFactory;

    @lombok.NonNull
    SdmxFileListener eventListener;

    @Override
    public void testClient() throws IOException {
        if (!source.getData().exists()) {
            throw new FileNotFoundException(source.getData().toString());
        }
        if (source.getStructure() != null && !source.getStructure().exists()) {
            throw new FileNotFoundException(source.getStructure().toString());
        }
    }

    @Override
    public SdmxFileInfo decode() throws IOException {
        return decoder.decode(source, languages);
    }

    @Override
    public Stream<Series> loadData(SdmxFileInfo info, DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        if (eventListener.isEnabled()) {
            eventListener.onFileSourceEvent(source, "Loading data from file '" + source.getData() + "'");
        }
        return getDataSupplier(info.getDataType(), info.getStructure())
                .parseFile(source.getData())
                .toCloseableStream()
                .filter(key::containsKey)
                .map(filter::apply);
    }

    private Xml.Parser<DataCursor> getDataSupplier(String dataType, DataStructure dsd) throws IOException {
        switch (dataType) {
            case SdmxMediaType.GENERIC_DATA_20:
                return SdmxXmlStreams.genericData20(dsd, obsFactory != null ? obsFactory : ObsFactories.SDMX20);
            case SdmxMediaType.STRUCTURE_SPECIFIC_DATA_20:
                return SdmxXmlStreams.compactData20(dsd, obsFactory != null ? obsFactory : ObsFactories.SDMX20);
            case SdmxMediaType.GENERIC_DATA_21:
                return SdmxXmlStreams.genericData21(dsd, obsFactory != null ? obsFactory : ObsFactories.SDMX21);
            case SdmxMediaType.STRUCTURE_SPECIFIC_DATA_21:
                return SdmxXmlStreams.compactData21(dsd, obsFactory != null ? obsFactory : ObsFactories.SDMX21);
            default:
                throw new IOException("Don't known how to handle type '" + dataType + "'");
        }
    }
}
