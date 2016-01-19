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
package be.nbb.sdmx.facade.connectors;

import be.nbb.sdmx.facade.DataStructure;
import be.nbb.sdmx.facade.ResourceRef;
import be.nbb.sdmx.facade.FlowRef;
import be.nbb.sdmx.facade.SdmxConnection;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.bancaditalia.oss.sdmx.api.Codelist;
import it.bancaditalia.oss.sdmx.api.DSDIdentifier;
import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.Dimension;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Philippe Charles
 */
@UtilityClass
final class Util {

    public static be.nbb.sdmx.facade.Dataflow toDataflow(Dataflow dataflow) {
        return new be.nbb.sdmx.facade.Dataflow(FlowRef.parse(dataflow.getFullIdentifier()), toDataStructureRef(dataflow.getDsdIdentifier()), dataflow.getDescription());
    }

    public static be.nbb.sdmx.facade.ResourceRef toDataStructureRef(DSDIdentifier input) {
        return new ResourceRef(input.getAgency(), input.getId(), input.getVersion());
    }

    static be.nbb.sdmx.facade.Codelist toCodelist(Codelist input) {
        return new be.nbb.sdmx.facade.Codelist(new ResourceRef(input.getAgency(), input.getId(), input.getVersion()), input.getCodes());
    }

    public static DataStructure toDataStructure(DataFlowStructure dfs) {
        Set<be.nbb.sdmx.facade.Dimension> dimensions = new HashSet<>();
        for (Dimension o : dfs.getDimensions()) {
            dimensions.add(new be.nbb.sdmx.facade.Dimension(o.getId(), o.getPosition(), toCodelist(o.getCodeList()), o.getName()));
        }
        return new DataStructure(new ResourceRef(dfs.getAgency(), dfs.getId(), dfs.getVersion()), dimensions, dfs.getName(), dfs.getTimeDimension(), dfs.getMeasure());
    }

    public interface ClientSupplier {

        @Nonnull
        GenericSDMXClient getClient(@Nonnull URL endpoint, @Nonnull Properties info) throws MalformedURLException;
    }

    private final static Cache<String, Object> CACHE = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    @Nonnull
    public SdmxConnection getConnection(@Nonnull String url, @Nonnull Properties info, @Nonnull ClientSupplier supplier) throws IOException {
        try {
            URL endpoint = new URL(url);
            GenericSDMXClient client = supplier.getClient(endpoint, info);
            return new CachedSdmxConnection(client, endpoint.getHost(), CACHE);
        } catch (MalformedURLException ex) {
            throw new IOException(ex);
        }
    }

    public static final String SUPPORTS_COMPRESSION_PROPERTY = "supportsCompression";
    public static final String NEEDS_CREDENTIALS_PROPERTY = "needsCredentials";
    public static final String NEEDS_URL_ENCODING_PROPERTY = "needsURLEncoding";
    public static final String SERIES_KEYS_ONLY_SUPPORTED_PROPERTY = "seriesKeysOnlySupported";

    public static boolean get(Properties p, String name, boolean defaultValue) {
        String value = p.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}
