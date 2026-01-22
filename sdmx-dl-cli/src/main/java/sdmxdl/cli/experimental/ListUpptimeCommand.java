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

import dev.failsafe.Failsafe;
import dev.failsafe.Fallback;
import dev.failsafe.RetryPolicy;
import internal.sdmxdl.cli.WebNetOptions;
import internal.sdmxdl.cli.ext.Anchor;
import nbbrd.console.picocli.text.TextOutputOptions;
import picocli.CommandLine;
import sdmxdl.Languages;
import sdmxdl.SourceRequest;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static java.lang.System.lineSeparator;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "upptime", description = "Create Upptime config file", hidden = true)
@SuppressWarnings("FieldMayBeFinal")
public final class ListUpptimeCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebNetOptions web;

    @CommandLine.ArgGroup(validate = false, headingKey = "debug")
    private TextOutputOptions output = new TextOutputOptions();

    @Override
    public Void call() throws Exception {
        try (Writer writer = output.newCharWriter()) {
            for (UpptimeSite site : getSiteList(web.loadManager(), web.getLangs(), msg -> web.getVerboseOptions().reportToErrorStream(Anchor.WEB, msg))) {
                writer.write("  - name: " + site.getName() + lineSeparator());
                writer.write("    url: " + (site.getUri() != null ? site.getUri().toString() : "N/A") + lineSeparator());
            }
        }
        return null;
    }

    private static List<UpptimeSite> getSiteList(SdmxWebManager manager, Languages languages, Consumer<String> report) {
        return manager.getSources()
                .values()
                .parallelStream()
                .filter(source -> !source.isAlias() && isUpptimeMonitor(source))
                .map(source -> getSite(manager, languages, source, report))
                .sorted(comparing(UpptimeSite::getName))
                .collect(toList());
    }

    private static boolean isUpptimeMonitor(WebSource source) {
        return source.getMonitor() != null && "upptime".equals(source.getMonitor().getScheme());
    }

    private static UpptimeSite getSite(SdmxWebManager manager, Languages languages, WebSource source, Consumer<String> report) {
        return Failsafe
                .with(Fallback.of(() -> UpptimeSite.failed(source)))
                .compose(RetryPolicy.<UpptimeSite>builder()
                        .handle(IOException.class)
                        .withMaxRetries(3)
                        .withDelay(Duration.ofSeconds(1))
                        .onRetry(event -> report.accept("Retrying " + source.getId() + " due to " + event.getLastException().getMessage()))
                        .build())
                .get(() -> getUpptimeSite(manager, languages, source));
    }

    private static UpptimeSite getUpptimeSite(SdmxWebManager manager, Languages languages, WebSource source) throws IOException {
        return manager
                .using(source)
                .testConnection(SourceRequest.builder().languages(languages).build())
                .map(value -> UpptimeSite.ok(source, value))
                .orElseGet(() -> UpptimeSite.missing(source));
    }

    @lombok.Value
    private static class UpptimeSite {

        String name;

        URI uri;

        static UpptimeSite ok(WebSource source, URI uri) {
            return new UpptimeSite(source.getId(), uri);
        }

        static UpptimeSite failed(WebSource source) {
            return new UpptimeSite(source.getId(), null);
        }

        static UpptimeSite missing(WebSource source) {
            return new UpptimeSite(source.getId(), null);
        }
    }
}

