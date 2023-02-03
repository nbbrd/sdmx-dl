package internal.sdmxdl.provider.ri.web;

import internal.util.http.HttpClient;
import internal.util.http.HttpResponseException;
import internal.util.http.ext.DumpingClientTest;
import nbbrd.io.xml.Xml;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.format.ObsParser;
import sdmxdl.provider.Marker;
import tests.sdmxdl.api.ByteSource;
import tests.sdmxdl.format.xml.SdmxXmlSources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static tests.sdmxdl.api.RepoSamples.*;

public class RiRestClientTest {

    @Test
    public void testGetFlow() throws IOException {
        assertThatExceptionOfType(IOException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_NOT_FOUND)).getFlow(BAD_FLOW_REF))
                .withMessageContaining(BAD_FLOW_REF.toString());

        assertThatExceptionOfType(HttpResponseException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_FORBIDDEN)).getFlow(BAD_FLOW_REF))
                .withMessageContaining(String.valueOf(HTTP_FORBIDDEN));

        DataflowRef ref = DataflowRef.of("ECB", "AME", "1.0");
        assertThat(of(onResponseStream(SdmxXmlSources.ECB_DATAFLOWS)).getFlow(ref)).satisfies(dsd -> {
                    assertThat(dsd.getRef()).isEqualTo(ref);
                }
        );
    }

    @Test
    public void testGetStructure() throws IOException {
        assertThatExceptionOfType(IOException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_NOT_FOUND)).getStructure(BAD_STRUCT_REF))
                .withMessageContaining(BAD_STRUCT_REF.toString());

        assertThatExceptionOfType(HttpResponseException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_FORBIDDEN)).getStructure(BAD_STRUCT_REF))
                .withMessageContaining(String.valueOf(HTTP_FORBIDDEN));

        DataStructureRef ref = DataStructureRef.of("ECB", "ECB_AME1", "1.0");
        assertThat(of(onResponseStream(SdmxXmlSources.ECB_DATA_STRUCTURE)).getStructure(ref)).satisfies(dsd -> {
                    assertThat(dsd.getRef()).isEqualTo(ref);
                    assertThat(dsd.getDimensions()).hasSize(7);
                }
        );
    }

    @Test
    public void testGetCodelist() throws IOException {
        assertThatExceptionOfType(IOException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_NOT_FOUND)).getCodelist(BAD_CODELIST_REF))
                .withMessageContaining(BAD_CODELIST_REF.toString());

        assertThatExceptionOfType(HttpResponseException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_FORBIDDEN)).getCodelist(BAD_CODELIST_REF))
                .withMessageContaining(String.valueOf(HTTP_FORBIDDEN));

        CodelistRef ref = CodelistRef.of("ECB", "CL_AME_AGG_METHOD", "1.0");
        assertThat(of(onResponseStream(SdmxXmlSources.ECB_DATA_STRUCTURE)).getCodelist(ref))
                .isEqualTo(Codelist
                        .builder()
                        .ref(ref)
                        .code("0", "Standard aggregation")
                        .code("1", "Weighted mean by GDP, weights in current euro")
                        .code("2", "Weighted mean by GDP, weights in current PPS")
                        .code("3", "Weighted mean by private consumption in euro")
                        .code("4", "Weighted mean by private consumption in PPS")
                        .build()
                );
    }

    private static RiRestClient of(HttpClient executor) throws MalformedURLException {
        return new RiRestClient(
                Marker.of("abc"),
                new URL("http://localhost"),
                LanguagePriorityList.ANY,
                ObsParser::newDefault,
                executor,
                new Sdmx21RestQueries(false),
                new Sdmx21RestParsers(),
                true
        );
    }

    private static HttpClient onResponseError(int responseCode) {
        return (httpRequest) -> {
            throw new HttpResponseException(responseCode, "");
        };
    }

    private static HttpClient onResponseStream(ByteSource byteSource) {
        return (httpRequest) -> DumpingClientTest.MockedResponse
                .builder()
                .body(byteSource::openStream)
                .mediaType(() -> Xml.APPLICATION_XML_UTF_8)
                .build();
    }
}
