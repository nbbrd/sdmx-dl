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
package tests.sdmxdl.format.xml;

import nbbrd.io.Resource;
import tests.sdmxdl.api.ByteSource;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxXmlSources {

    private static ByteSource of(String name) {
        return () -> Resource.newInputStream(SdmxXmlSources.class, name);
    }

    public static final ByteSource ECB_DATAFLOWS = of("ecb/EcbDataflows.xml");
    public static final ByteSource ECB_DATA_STRUCTURE = of("ecb/EcbDataStructure.xml");
    public static final ByteSource ECB_DATA = of("ecb/EcbDataKeys.xml");

    public static final ByteSource NBB_DATAFLOWS = of("nbb/Dataflows.xml");
    public static final ByteSource NBB_DATA_STRUCTURE = of("nbb/DataflowStructure.xml");
    public static final ByteSource NBB_DATA = of("nbb/TimeSeries.xml");

    public static final ByteSource OTHER_GENERIC20 = of("other/sdmx-generic20-sample.xml");
    public static final ByteSource OTHER_COMPACT20 = of("other/sdmx-compact20-sample.xml");
    public static final ByteSource OTHER_GENERIC21 = of("other/sdmx-GenericData21.xml");
    public static final ByteSource OTHER_COMPACT21 = of("other/sdmx-compactData21.xml");
    public static final ByteSource OTHER_FLOWS20 = of("other/sdmx-Dataflows20.xml");
}
