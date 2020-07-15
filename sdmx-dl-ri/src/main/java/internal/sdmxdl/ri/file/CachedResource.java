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

import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.SdmxCache;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.repo.DataSet;
import sdmxdl.repo.SdmxRepository;
import sdmxdl.util.TypedId;
import sdmxdl.xml.XmlFileSource;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
public final class CachedResource extends SdmxDecoderResource {

    // TODO: replace ttl with file last modification time
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(5);

    private final SdmxCache cache;
    private final TypedId<SdmxDecoder.Info> decodeKey;
    private final TypedId<DataSet> loadDataKey;

    public CachedResource(SdmxFileSource source, LanguagePriorityList languages, SdmxDecoder decoder, Optional<ObsFactory> dataFactory, SdmxCache cache) {
        super(source, languages, decoder, dataFactory);
        this.cache = cache;
        String base = getBase(source, languages);
        this.decodeKey = TypedId.of("decode://" + base,
                repo -> SdmxDecoder.Info.of(repo.getName(), repo.getStructures().stream().findFirst().orElse(null)),
                info -> SdmxRepository.builder().name(info.getDataType()).structure(info.getStructure()).build()
        );
        this.loadDataKey = TypedId.of("loadData://" + base,
                repo -> repo.getDataSets().stream().findFirst().orElse(null),
                data -> SdmxRepository.builder().dataSet(data).build()
        );
    }

    private static String getBase(SdmxFileSource source, LanguagePriorityList languages) {
        try {
            return XmlFileSource.getFormatter().formatToString(source) + languages.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public SdmxDecoder.Info decode() throws IOException {
        SdmxDecoder.Info result = decodeKey.load(cache);
        if (result == null) {
            result = super.decode();
            decodeKey.store(cache, result, DEFAULT_CACHE_TTL);
        }
        return result;
    }

    @Override
    public DataCursor loadData(SdmxDecoder.Info entry, DataflowRef flowRef, Key key, boolean serieskeysonly) throws IOException {
        if (serieskeysonly) {
            DataSet result = loadDataKey.load(cache);
            if (result == null) {
                result = copyOfKeysAndMeta(entry, flowRef, key);
                loadDataKey.store(cache, result, DEFAULT_CACHE_TTL);
            }
            return result.getDataCursor(key, NO_DATA_FILTER);
        }
        return super.loadData(entry, flowRef, key, serieskeysonly);
    }

    private DataSet copyOfKeysAndMeta(SdmxDecoder.Info entry, DataflowRef flowRef, Key key) throws IOException {
        try (DataCursor c = super.loadData(entry, flowRef, key, true)) {
            return DataSet
                    .builder()
                    .ref(flowRef)
                    .copyOf(c, NO_DATA_FILTER)
                    .build();
        }
    }

    private static final DataFilter NO_DATA_FILTER = DataFilter.builder().detail(DataFilter.Detail.NO_DATA).build();
}
