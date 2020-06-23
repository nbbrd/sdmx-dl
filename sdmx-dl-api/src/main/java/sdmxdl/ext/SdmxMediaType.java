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
package sdmxdl.ext;

/**
 * Represents an SDMX Media Type (also known as a MIME Type or Content Type)
 * used in HTTP content negotiation.
 * <p>
 * https://raw.githubusercontent.com/airosa/test-sdmx-ws/master/v2_1/rest/src/sdmx-rest.wadl<br>
 * http://sdw-wsrest.ecb.europa.eu/documentation/index.jsp#negotiation<br>
 * https://github.com/sdmx-twg/sdmx-rest/wiki/HTTP-content-negotiation<br>
 * https://github.com/sdmx-twg/sdmx-rest/blob/master/v2_1/ws/rest/docs/4_6_conneg.md<br>
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class SdmxMediaType {

    public static final String GENERIC_DATA_20
            = "application/vnd.sdmx.genericdata+xml;version=2.0";

    public static final String GENERIC_DATA_21
            = "application/vnd.sdmx.genericdata+xml;version=2.1";

    public static final String STRUCTURE_SPECIFIC_DATA_20
            = "application/vnd.sdmx.structurespecificdata+xml;version=2.0";

    public static final String STRUCTURE_SPECIFIC_DATA_21
            = "application/vnd.sdmx.structurespecificdata+xml;version=2.1";

    public static final String GENERIC_TS_DATA_21
            = "application/vnd.sdmx.generictimeseriesdata+xml;version=2.1";

    public static final String STRUCTURE_SPECIFIC_TS_DATA_21
            = "application/vnd.sdmx.structurespecifictimeseriesdata+xml;version=2.1";

    public static final String GENERIC_METADATA_21
            = "application/vnd.sdmx.genericmetadata+xml;version=2.1";

    public static final String STRUCTURE_SPECIFIC_METADATA_21
            = "application/vnd.sdmx.structurespecificmetadata+xml;version=2.1";

    public static final String STRUCTURE_21
            = "application/vnd.sdmx.structure+xml;version=2.1";

    public static final String ERROR_21
            = "application/vnd.sdmx.error+xml;version=2.1";

    public static final String JSON_DATA
            = "application/vnd.sdmx.data+json;version=1.0.0";

    public static final String CSV_DATA
            = "application/vnd.ecb.data+csv;version=1.0.0";

    public static final String XML = "application/xml";
    public static final String JSON = "application/json";
    public static final String CSV = "text/csv";
}
