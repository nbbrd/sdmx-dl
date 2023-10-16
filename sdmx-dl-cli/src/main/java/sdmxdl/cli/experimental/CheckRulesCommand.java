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
import sdmxdl.cli.protobuf.*;
import sdmxdl.testing.WebRequest;
import sdmxdl.testing.WebResponse;
import sdmxdl.testing.WebRuleLoader;
import sdmxdl.testing.xml.XmlSourceQuery;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

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

        RulesSummaries result = RulesSummaries
                .newBuilder()
                .addAllSummaries(
                        web.applyParallel(requestsBySource.values().stream())
                                .flatMap(list -> list.stream().map(request -> WebResponse.of(request, manager)))
                                .map(CheckRulesCommand::summaryOf)
                                .sorted(Comparator.comparing(RulesSummary::getDigest))
                                .collect(Collectors.toList()))
                .build();

        output.dumpAll(result);

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


    private static RulesTarget targetOf(WebRequest request) {
        return RulesTarget
                .newBuilder()
                .setSource(request.getSource())
                .setFlow(request.getFlowRef().toString())
                .setKey(request.getQuery().getKey().toString())
                .build();
    }

    private static RulesConfig configOf(WebSource source) {
        try {
            RulesConfig.Builder result = RulesConfig.newBuilder();
            result.setDriver(source.getDriver());
            result.setProtocol(source.getEndpoint().toURL().getProtocol());
            result.setProperties(DEFAULT_MAP_FORMATTER.formatAsString(source.getProperties()));
            return result.build();
        } catch (MalformedURLException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static RulesExpect expectOf(WebRequest request) {
        return RulesExpect
                .newBuilder()
                .setFlowCount(request.getFlowCount().toString())
                .setDimCount(request.getDimensionCount().toString())
                .setSeriesCount(request.getSeriesCount().toString())
                .setObsCount(request.getObsCount().toString())
                .build();
    }

    private static RulesActual actualOf(WebResponse r) {
        return RulesActual
                .newBuilder()
                .setFlowCount(ofNullable(r.getFlows()).map(Collection::size).orElse(-1))
                .setDimCount(ofNullable(r.getStructure()).map(dsd -> dsd.getDimensions().size()).orElse(-1))
                .setSeriesCount(ofNullable(r.getData()).map(Collection::size).orElse(-1))
                .setObsCount(ofNullable(r.getData()).map(WebResponse::getObsCount).orElse(-1))
                .build();
    }

    private static RulesSummary summaryOf(WebResponse r) {
        return RulesSummary
                .newBuilder()
                .setDigest(r.getRequest().getDigest())
                .setTarget(targetOf(r.getRequest()))
                .setConfig(configOf(r.getSource()))
                .setExpect(expectOf(r.getRequest()))
                .setActual(actualOf(r))
                .setErrors(ofNullable(r.getError()).orElse(""))
                .addAllIssues(checkAll(r))
                .build();
    }

    private static List<String> checkAll(WebResponse response) {
        return WebRuleLoader.get()
                .stream()
                .map(rule -> !rule.getValidator().isValid(response) ? rule.getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
