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
package internal.sdmxdl.ri.web;

import internal.util.rest.HttpRest;
import sdmxdl.*;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.SdmxExceptions;
import sdmxdl.xml.stream.SdmxXmlStreams;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static sdmxdl.ext.SdmxMediaType.*;

/**
 * @author Philippe Charles
 */
public class Sdmx21RestClient extends RiRestClient {

    protected final boolean detailSupported;
    protected final ObsFactory dataFactory;

    public Sdmx21RestClient(String name, URL endpoint, LanguagePriorityList langs, HttpRest.Client executor,
                            boolean detailSupported, boolean trailingSlashRequired, ObsFactory dataFactory) {
        super(name, endpoint, langs, executor, dataFactory, new Sdmx21RestQueries(trailingSlashRequired));
        this.detailSupported = detailSupported;
        this.dataFactory = dataFactory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected List<Dataflow> getFlows(URL url) throws IOException {
        return SdmxXmlStreams
                .flow21(langs)
                .parseStream(calling(url, XML));
    }

    @Override
    protected Dataflow getFlow(URL url, DataflowRef ref) throws IOException {
        return SdmxXmlStreams
                .flow21(langs)
                .parseStream(calling(url, XML))
                .stream()
                .filter(ref::containsRef)
                .findFirst()
                .orElseThrow(() -> SdmxExceptions.missingFlow(name, ref));
    }

    @Override
    protected DataStructure getStructure(URL url, DataStructureRef ref) throws IOException {
        return SdmxXmlStreams
                .struct21(langs)
                .parseStream(calling(url, STRUCTURE_21))
                .stream()
                .filter(ref::equalsRef)
                .findFirst()
                .orElseThrow(() -> SdmxExceptions.missingStructure(name, ref));
    }

    @Override
    protected DataCursor getData(DataStructure dsd, URL url) throws IOException {
        return SdmxXmlStreams
                .compactData21(dsd, dataFactory)
                .parseStream(calling(url, STRUCTURE_SPECIFIC_DATA_21));
    }

    @Override
    public boolean isDetailSupported() {
        return detailSupported;
    }

    @Override
    public DataStructureRef peekStructureRef(DataflowRef flowRef) {
        return null;
    }
}
