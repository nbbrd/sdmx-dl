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
package sdmxdl.cli.experimental;

import internal.sdmxdl.cli.DebugOutputOptions;
import internal.sdmxdl.cli.WebSourcesOptions;
import picocli.CommandLine;
import sdmxdl.DataflowRef;
import sdmxdl.Key;
import sdmxdl.testing.IntRange;
import sdmxdl.testing.WebRequest;
import sdmxdl.testing.WebResponse;
import sdmxdl.testing.WebRuleLoader;
import sdmxdl.testing.xml.XmlSourceQuery;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static internal.sdmxdl.cli.ext.CsvUtil.DEFAULT_MAP_FORMATTER;
import static java.util.Optional.ofNullable;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(
        name = "rules",
        hidden = true
)
@SuppressWarnings("FieldMayBeFinal")
public final class CheckRulesCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebSourcesOptions web;

    @CommandLine.Option(
            names = {"--requests"},
            paramLabel = "<file>"
    )
    private File requests;

    @CommandLine.ArgGroup(validate = false, headingKey = "debug")
    private DebugOutputOptions output = new DebugOutputOptions();

    @Override
    public Void call() throws Exception {
        SdmxWebManager manager = web.loadManager();

        Map<String, List<WebRequest>> requestsBySource = getRequests()
                .stream()
                .filter(getSourceFilter())
                .collect(Collectors.groupingBy(WebRequest::getSource));

        List<Summary> result = web.applyParallel(requestsBySource.values().stream())
                .flatMap(list -> list.stream().map(request -> WebResponse.of(request, manager)))
                .map(Summary::of)
                .sorted(Comparator.comparing(o -> o.digest))
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

    @lombok.AllArgsConstructor
    private static class Target {

        String source;
        DataflowRef flow;
        Key key;

        static Target of(WebRequest request) {
            return new Target(
                    request.getSource(),
                    request.getFlowRef(),
                    request.getQuery().getKey()
            );
        }
    }

    @lombok.AllArgsConstructor
    private static class Config {

        String driver;
        String dialect;
        String protocol;
        String properties;

        static Config of(SdmxWebSource source) {
            try {
                return new Config(
                        source.getDriver(),
                        source.getDialect(),
                        source.getEndpoint().toURL().getProtocol(),
                        DEFAULT_MAP_FORMATTER.formatAsString(source.getProperties())
                );
            } catch (MalformedURLException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @lombok.AllArgsConstructor
    private static class Expect {

        IntRange flowCount;
        IntRange dimCount;
        IntRange seriesCount;
        IntRange obsCount;

        static Expect of(WebRequest request) {
            return new Expect(
                    request.getFlowCount(),
                    request.getDimensionCount(),
                    request.getSeriesCount(),
                    request.getObsCount()
            );
        }
    }

    @lombok.AllArgsConstructor
    private static class Actual {

        int flowCount;
        int dimCount;
        int seriesCount;
        int obsCount;

        static Actual of(WebResponse r) {
            return new Actual(
                    ofNullable(r.getFlows()).map(Collection::size).orElse(-1),
                    ofNullable(r.getStructure()).map(dsd -> dsd.getDimensions().size()).orElse(-1),
                    ofNullable(r.getData()).map(Collection::size).orElse(-1),
                    ofNullable(r.getData()).map(WebResponse::getObsCount).orElse(-1)
            );
        }
    }

    @lombok.AllArgsConstructor
    private static class Summary {

        String digest;
        Target target;
        Config config;
        Expect expect;
        Actual actual;
        String errors;
        List<String> issues;

        static Summary of(WebResponse r) {
            return new Summary(
                    r.getRequest().getDigest(),
                    Target.of(r.getRequest()),
                    Config.of(r.getSource()),
                    Expect.of(r.getRequest()),
                    Actual.of(r),
                    ofNullable(r.getError()).orElse(""),
                    checkAll(r)
            );
        }
    }

    private static List<String> checkAll(WebResponse response) {
        return WebRuleLoader.get()
                .stream()
                .map(rule -> !rule.getValidator().isValid(response) ? rule.getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
