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
package sdmxdl.provider.ri.readers;

import lombok.NonNull;
import nbbrd.io.net.MediaType;
import nbbrd.io.xml.Xml;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.Structure;
import sdmxdl.EventListener;
import sdmxdl.Languages;
import sdmxdl.Series;
import sdmxdl.file.FileSource;
import sdmxdl.format.DataCursor;
import sdmxdl.format.ObsParser;
import sdmxdl.format.xml.SdmxXmlStreams;
import sdmxdl.format.xml.XmlMediaTypes;
import sdmxdl.provider.DataRef;
import sdmxdl.provider.HasMarker;
import sdmxdl.provider.Marker;
import sdmxdl.provider.file.FileClient;
import sdmxdl.provider.file.FileInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Philippe Charles
 */
@lombok.AllArgsConstructor
public class XmlFileClient implements FileClient {

    @lombok.NonNull
    private final FileSource source;

    @lombok.NonNull
    private final Languages languages;

    @lombok.NonNull
    private final Decoder decoder;

    @Nullable
    private final Supplier<ObsParser> obsFactory;

    private final @Nullable EventListener<? super FileSource> listener;

    @Override
    public @NonNull Marker getMarker() {
        return HasMarker.of(source);
    }

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
        if (listener != null) {
            listener.accept(source, MARKER, "Loading data from file '" + source.getData() + "'");
        }
        return dataRef.getQuery().execute(
                getDataSupplier(info.getDataType(), info.getStructure())
                        .parseFile(source.getData())
                        .asCloseableStream()
        );
    }

    private Xml.Parser<DataCursor> getDataSupplier(MediaType dataType, Structure dsd) throws IOException {
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

    private static final String MARKER = "XML_FILE_CLIENT";
}
