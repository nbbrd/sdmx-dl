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
package internal.sdmxdl.cli;

import picocli.CommandLine;
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.xml.XmlWebSource;

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebOptions {

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.verbose"
    )
    private boolean verbose;

    @CommandLine.Option(
            names = {"-s", "--sources"},
            paramLabel = "<file>",
            descriptionKey = "sdmxdl.cli.sources"
    )
    private File sourcesFile;

    public SdmxWebManager getManager() throws IOException {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .eventListener(getEventListener())
                .customSources(getCustomSources())
                .build();
    }

    private SdmxWebListener getEventListener() {
        return verbose
                ? new VerboseWebListener(SdmxWebListener.getDefault())
                : SdmxWebListener.getDefault();
    }

    private List<SdmxWebSource> getCustomSources() throws IOException {
        return sourcesFile != null
                ? XmlWebSource.getParser().parseFile(sourcesFile)
                : Collections.emptyList();
    }

    protected void reportIOException(String message, IOException ex) {
        if (verbose) {
            reportToErrorStream("IO", message + " - " + ex.getMessage());
        }
    }

    @lombok.AllArgsConstructor
    private static class VerboseWebListener implements SdmxWebListener {

        @lombok.NonNull
        private final SdmxWebListener main;

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void onSourceEvent(SdmxWebSource source, String message) {
            if (main.isEnabled()) {
                main.onSourceEvent(source, message);
            }
            reportToErrorStream(source.getName(), message);
        }
    }

    private static void reportToErrorStream(String anchor, String message) {
        System.err.println(anchor + ": " + message);
    }

    public static boolean isAllSources(List<String> sourceNames) {
        return sourceNames.size() == 1 && isAllSources(sourceNames.get(0));
    }

    private static boolean isAllSources(String name) {
        return "all".equals(name);
    }

    public static void warmupProxySelector(ProxySelector proxySelector) {
        try {
            proxySelector.select(new URI("http://localhost"));
        } catch (URISyntaxException ex) {
        }
    }
}
