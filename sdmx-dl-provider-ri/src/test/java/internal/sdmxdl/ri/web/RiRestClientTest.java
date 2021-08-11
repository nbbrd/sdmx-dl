package internal.sdmxdl.ri.web;

import internal.util.rest.HttpRest;
import org.junit.Test;
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxException;
import sdmxdl.util.parser.ObsFactories;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static sdmxdl.samples.RepoSamples.BAD_FLOW_REF;
import static sdmxdl.samples.RepoSamples.BAD_STRUCT_REF;

public class RiRestClientTest {

    @Test
    public void testGetFlow() {
        assertThatExceptionOfType(SdmxException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_NOT_FOUND)).getFlow(BAD_FLOW_REF))
                .withMessageContaining(BAD_FLOW_REF.toString());

        assertThatExceptionOfType(HttpRest.ResponseError.class)
                .isThrownBy(() -> of(onResponseError(HTTP_FORBIDDEN)).getFlow(BAD_FLOW_REF))
                .withMessageContaining(String.valueOf(HTTP_FORBIDDEN));
    }

    @Test
    public void testGetStructure() {
        assertThatExceptionOfType(SdmxException.class)
                .isThrownBy(() -> of(onResponseError(HTTP_NOT_FOUND)).getStructure(BAD_STRUCT_REF))
                .withMessageContaining(BAD_STRUCT_REF.toString());

        assertThatExceptionOfType(HttpRest.ResponseError.class)
                .isThrownBy(() -> of(onResponseError(HTTP_FORBIDDEN)).getStructure(BAD_STRUCT_REF))
                .withMessageContaining(String.valueOf(HTTP_FORBIDDEN));
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
}
