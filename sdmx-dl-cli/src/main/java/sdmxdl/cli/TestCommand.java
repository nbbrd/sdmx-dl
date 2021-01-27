/*
 * Copyright 2018 National Bank of Belgium
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
package sdmxdl.cli;

import internal.sdmxdl.cli.DebugOutputOptions;
import internal.sdmxdl.cli.WebSourcesOptions;
import internal.sdmxdl.cli.ext.ProxyOptions;
import picocli.CommandLine;
import sdmxdl.testing.WebReport;
import sdmxdl.testing.WebRequest;
import sdmxdl.testing.WebResponse;
import sdmxdl.testing.xml.XmlSourceQuery;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "test",
        hidden = true
)
@SuppressWarnings("FieldMayBeFinal")
public final class TestCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourcesOptions web;

    @CommandLine.Option(
            names = {"--requests"},
            paramLabel = "<file>"
    )
    private File requests;

    @CommandLine.ArgGroup
    private DebugOutputOptions output = new DebugOutputOptions();

    @Override
    public Void call() throws Exception {
        SdmxWebManager manager = web.loadManager();
        ProxyOptions.warmupProxySelector(manager.getProxySelector());

        List<WebRequest> requests = getRequests();
        List<Summary> result = (web.isNoParallel() ? requests.stream() : requests.parallelStream())
                .filter(getSourceFilter())
                .map(request -> WebResponse.of(request, manager))
                .map(WebReport::of)
                .map(Summary::of)
                .collect(Collectors.toList());

        output.dumpAll(Summary.class, result);

        return null;
    }

    private List<WebRequest> getRequests() throws IOException {
        return requests != null
                ? XmlSourceQuery.getParser().parseFile(requests)
                : XmlSourceQuery.getDefaultRequests();
    }

    private Predicate<WebRequest> getSourceFilter() {
        return web.isAllSources()
                ? request -> true
                : request -> web.getSources().contains(request.getSource());
    }

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    private static class WebConfig {

        @lombok.NonNull
        String driver;

        //        @Nullable
        String dialect;

        @lombok.NonNull
        URL endpoint;

        @lombok.Singular
        Map<String, String> properties;

        public static WebConfig of(SdmxWebSource source) {
            return WebConfig
                    .builder()
                    .driver(source.getDriver())
                    .dialect(source.getDialect())
                    .endpoint(source.getEndpoint())
                    .properties(source.getProperties())
                    .build();
        }
    }

    @lombok.Value
    @lombok.Builder(toBuilder = true)
    private static class Summary {

        @lombok.NonNull
        WebRequest request;

        @lombok.NonNull
        WebConfig config;

        int flowCount;

        boolean flow;

        boolean struct;

        int seriesCount;

        int obsCount;

        @lombok.NonNull
        String error;

        @lombok.NonNull
        List<String> problems;

        public static Summary of(WebReport r) {
            return Summary
                    .builder()
                    .request(r.getResponse().getRequest())
                    .config(WebConfig.of(r.getResponse().getSource()))
                    .flowCount(r.getResponse().hasFlows() ? r.getResponse().getFlows().size() : -1)
                    .flow(r.getResponse().hasFlow())
                    .struct(r.getResponse().hasStructure())
                    .seriesCount(r.getResponse().hasData() ? r.getResponse().getData().size() : -1)
                    .obsCount(r.getResponse().hasData() ? r.getResponse().getData().stream().mapToInt(series -> series.getObs().size()).sum() : -1)
                    .error(r.getResponse().hasError() ? r.getResponse().getError() : "")
                    .problems(r.getProblems())
                    .build();
        }
    }
}
