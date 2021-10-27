package internal.sdmxdl.ri.web;

import internal.util.rest.DumpingClientTest;
import internal.util.rest.HttpRest;
import internal.util.rest.MediaType;
import org.junit.jupiter.api.Test;
import sdmxdl.*;
import sdmxdl.ext.SdmxException;
import sdmxdl.ext.SdmxMediaType;
import sdmxdl.samples.ByteSource;
import sdmxdl.samples.SdmxSource;
import sdmxdl.util.parser.ObsFactories;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static sdmxdl.samples.RepoSamples.*;

public class RiRestClientTest {

    @Test
    public void testGetFlow() throws IOException {
        assertThatExceptionOfType(SdmxException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_NOT_FOUND)).getFlow(BAD_FLOW_REF))
                .withMessageContaining(BAD_FLOW_REF.toString());

        assertThatExceptionOfType(HttpRest.ResponseError.class)
                .isThrownBy(() -> of(onResponseError(HTTP_FORBIDDEN)).getFlow(BAD_FLOW_REF))
                .withMessageContaining(String.valueOf(HTTP_FORBIDDEN));

        DataflowRef ref = DataflowRef.of("ECB", "AME", "1.0");
        assertThat(of(onResponseStream(SdmxSource.ECB_DATAFLOWS)).getFlow(ref)).satisfies(dsd -> {
                    assertThat(dsd.getRef()).isEqualTo(ref);
                }
        );
    }

    @Test
    public void testGetStructure() throws IOException {
        assertThatExceptionOfType(SdmxException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_NOT_FOUND)).getStructure(BAD_STRUCT_REF))
                .withMessageContaining(BAD_STRUCT_REF.toString());

        assertThatExceptionOfType(HttpRest.ResponseError.class)
                .isThrownBy(() -> of(onResponseError(HTTP_FORBIDDEN)).getStructure(BAD_STRUCT_REF))
                .withMessageContaining(String.valueOf(HTTP_FORBIDDEN));

        DataStructureRef ref = DataStructureRef.of("ECB", "ECB_AME1", "1.0");
        assertThat(of(onResponseStream(SdmxSource.ECB_DATA_STRUCTURE)).getStructure(ref)).satisfies(dsd -> {
                    assertThat(dsd.getRef()).isEqualTo(ref);
                    assertThat(dsd.getDimensions()).hasSize(7);
                }
        );
    }

    @Test
    public void testGetCodelist() throws IOException {
        assertThatExceptionOfType(SdmxException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_NOT_FOUND)).getCodelist(BAD_CODELIST_REF))
                .withMessageContaining(BAD_CODELIST_REF.toString());

        assertThatExceptionOfType(HttpRest.ResponseError.class)
                .isThrownBy(() -> of(onResponseError(HTTP_FORBIDDEN)).getCodelist(BAD_CODELIST_REF))
                .withMessageContaining(String.valueOf(HTTP_FORBIDDEN));

        CodelistRef ref = CodelistRef.of("ECB", "CL_AME_AGG_METHOD", "1.0");
        assertThat(of(onResponseStream(SdmxSource.ECB_DATA_STRUCTURE)).getCodelist(ref))
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

    private static RiRestClient of(HttpRest.Client executor) throws MalformedURLException {
        return new RiRestClient(
                "abc",
                new URL("http://localhost"),
                LanguagePriorityList.ANY,
                ObsFactories.SDMX21,
                executor,
                new Sdmx21RestQueries(false),
                new Sdmx21RestParsers(),
                true
        );
    }

    private static HttpRest.Client onResponseError(int responseCode) {
        return (query, mediaTypes, langs) -> {
            throw new HttpRest.ResponseError(responseCode, "", Collections.emptyMap());
        };
    }

    private static HttpRest.Client onResponseStream(ByteSource byteSource) {
        return (url, mediaTypes, langs) -> DumpingClientTest.MockedResponse
                .builder()
                .body(byteSource::openStream)
                .mediaType(() -> MediaType.parse(SdmxMediaType.GENERIC_XML))
                .build();
    }
}
