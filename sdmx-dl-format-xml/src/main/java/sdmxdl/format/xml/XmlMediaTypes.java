package sdmxdl.format.xml;

import sdmxdl.format.MediaType;

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
public class XmlMediaTypes {

    public static final MediaType GENERIC_DATA_20
            = MediaType.parse("application/vnd.sdmx.genericdata+xml;version=2.0");

    public static final MediaType GENERIC_DATA_21
            = MediaType.parse("application/vnd.sdmx.genericdata+xml;version=2.1");

    public static final MediaType STRUCTURE_SPECIFIC_DATA_20
            = MediaType.parse("application/vnd.sdmx.structurespecificdata+xml;version=2.0");

    public static final MediaType STRUCTURE_SPECIFIC_DATA_21
            = MediaType.parse("application/vnd.sdmx.structurespecificdata+xml;version=2.1");

    public static final MediaType GENERIC_TS_DATA_21
            = MediaType.parse("application/vnd.sdmx.generictimeseriesdata+xml;version=2.1");

    public static final MediaType STRUCTURE_SPECIFIC_TS_DATA_21
            = MediaType.parse("application/vnd.sdmx.structurespecifictimeseriesdata+xml;version=2.1");

    public static final MediaType GENERIC_METADATA_21
            = MediaType.parse("application/vnd.sdmx.genericmetadata+xml;version=2.1");

    public static final MediaType STRUCTURE_SPECIFIC_METADATA_21
            = MediaType.parse("application/vnd.sdmx.structurespecificmetadata+xml;version=2.1");

    public static final MediaType STRUCTURE_21
            = MediaType.parse("application/vnd.sdmx.structure+xml;version=2.1");

    public static final MediaType ERROR_21
            = MediaType.parse("application/vnd.sdmx.error+xml;version=2.1");
}
