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
package internal.sdmxdl.provider.ri.file;

import lombok.NonNull;
import nbbrd.io.xml.Xml;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataStructure;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.Series;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.format.DataCursor;
import sdmxdl.format.MediaType;
import sdmxdl.format.ObsParser;
import sdmxdl.format.xml.XmlMediaTypes;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.file.FileClient;
import sdmxdl.provider.file.FileInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public class XmlFileClient implements FileClient {

    @lombok.NonNull
    private final SdmxFileSource source;

    @lombok.NonNull
    private final LanguagePriorityList languages;

    @lombok.NonNull
    private final SdmxDecoder decoder;

    @Nullable
    private final Supplier<ObsParser> obsFactory;

    @lombok.NonNull
    BiConsumer<? super SdmxFileSource, ? super String> eventListener;

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
    public @NonNull FileInfo decode() throws IOException {
        return decoder.decode(source, languages);
    }

    @Override
    public @NonNull Stream<Series> loadData(@NonNull FileInfo info, @NonNull DataRef dataRef) throws IOException {
        if (eventListener != SdmxManager.NO_OP_EVENT_LISTENER) {
            eventListener.accept(source, "Loading data from file '" + source.getData() + "'");
        }
        return dataRef.getQuery().execute(
                getDataSupplier(info.getDataType(), info.getStructure())
                        .parseFile(source.getData())
                        .asCloseableStream()
        );
    }

    private Xml.Parser<DataCursor> getDataSupplier(MediaType dataType, DataStructure dsd) throws IOException {
        if (XmlMediaTypes.GENERIC_DATA_20.equals(dataType)) {
            return SdmxXmlStreams.genericData20(dsd, obsFactory != null ? obsFactory : ObsParser::newDefault);
        } else if (XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_20.equals(dataType)) {
            return SdmxXmlStreams.compactData20(dsd, obsFactory != null ? obsFactory : ObsParser::newDefault);
        } else if (XmlMediaTypes.GENERIC_DATA_21.equals(dataType)) {
            return SdmxXmlStreams.genericData21(dsd, obsFactory != null ? obsFactory : ObsParser::newDefault);
        } else if (XmlMediaTypes.STRUCTURE_SPECIFIC_DATA_21.equals(dataType)) {
            return SdmxXmlStreams.compactData21(dsd, obsFactory != null ? obsFactory : ObsParser::newDefault);
        }
        throw new IOException("Don't known how to handle type '" + dataType + "'");
    }
}
