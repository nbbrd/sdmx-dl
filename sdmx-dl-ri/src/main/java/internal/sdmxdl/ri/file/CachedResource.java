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

/**
 * @author Philippe Charles
 */
public final class CachedResource extends SdmxDecoderResource {

    // TODO: replace ttl with file last modification time
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofMinutes(5);

    private final SdmxCache cache;
    private final TypedId<SdmxDecoder.Info> idOfDecode;
    private final TypedId<DataSet> idOfLoadData;

    public CachedResource(SdmxFileSource source, LanguagePriorityList languages, SdmxDecoder decoder, ObsFactory obsFactory, SdmxCache cache) {
        super(source, languages, decoder, obsFactory);
        this.cache = cache;
        String base = getBase(source, languages);
        this.idOfDecode = TypedId.of("decode://" + base,
                repo -> SdmxDecoder.Info.of(repo.getName(), repo.getStructures().stream().findFirst().orElse(null)),
                info -> SdmxRepository.builder().name(info.getDataType()).structure(info.getStructure()).build()
        );
        this.idOfLoadData = TypedId.of("loadData://" + base,
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
        return idOfDecode.load(cache, super::decode, o -> DEFAULT_CACHE_TTL);
    }

    @Override
    public DataCursor loadData(SdmxDecoder.Info entry, DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        return filter.isSeriesKeyOnly()
                ? idOfLoadData.load(cache, () -> copyOf(entry, flowRef, key, filter), o -> DEFAULT_CACHE_TTL).getDataCursor()
                : super.loadData(entry, flowRef, key, filter);
    }

    private DataSet copyOf(SdmxDecoder.Info entry, DataflowRef flowRef, Key key, DataFilter filter) throws IOException {
        try (DataCursor cursor = super.loadData(entry, flowRef, key, filter)) {
            return DataSet.builder().ref(flowRef).copyOf(cursor).build();
        }
    }
}
