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

import internal.sdmxdl.cli.ext.VerboseOptions;
import picocli.CommandLine;
import sdmxdl.LanguagePriorityList;
import sdmxdl.SdmxManager;
import sdmxdl.format.xml.XmlWebSource;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;

/**
 * @author Philippe Charles
 */
@lombok.Getter
@lombok.Setter
public class WebOptions {

    @CommandLine.Mixin
    private VerboseOptions verboseOptions;

    @CommandLine.Option(
            names = {"-s", "--sources"},
            paramLabel = "<file>",
            descriptionKey = "cli.sdmx.sourcesFile"
    )
    private File sourcesFile;

    @CommandLine.Option(
            names = {"-l", "--languages"},
            paramLabel = "<langs>",
            converter = LangsConverter.class,
            defaultValue = LanguagePriorityList.ANY_KEYWORD,
            descriptionKey = "cli.sdmx.languages"
    )
    private LanguagePriorityList langs;

    @CommandLine.Option(
            names = {"--no-log"},
            defaultValue = "false",
            hidden = true
    )
    private boolean noLog;

    @CommandLine.Option(
            names = {SpecialProperties.DEBUG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean debug;

    @CommandLine.Option(
            names = {SpecialProperties.BATCH_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean batch;

    public SdmxWebManager loadManager() throws IOException {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .languages(langs)
                .eventListener(getEventListener())
                .customSources(parseCustomSources())
                .build();
    }

    private BiConsumer<? super SdmxWebSource, ? super String> getEventListener() {
        BiConsumer<? super SdmxWebSource, ? super String> original = isNoLog() ? SdmxManager.NO_OP_EVENT_LISTENER : new LoggingListener()::onSourceEvent;
        return new VerboseListener(original, verboseOptions)::onSourceEvent;
    }

    private List<SdmxWebSource> parseCustomSources() throws IOException {
        if (sourcesFile != null) {
            if (verboseOptions.isVerbose()) {
                verboseOptions.reportToErrorStream("CFG", "Using source file '" + sourcesFile + "'");
            }
            return XmlWebSource.getParser().parseFile(sourcesFile);
        }
        return Collections.emptyList();
    }

    @lombok.extern.java.Log
    private static class LoggingListener {

        public void onSourceEvent(SdmxWebSource source, String message) {
            if (log.isLoggable(Level.INFO)) {
                log.info(message);
            }
        }
    }

    @lombok.AllArgsConstructor
    private static class VerboseListener {

        @lombok.NonNull
        private final BiConsumer<? super SdmxWebSource, ? super String> main;

        @lombok.NonNull
        private final VerboseOptions verboseOptions;

        public void onSourceEvent(SdmxWebSource source, String message) {
            if (main != SdmxManager.NO_OP_EVENT_LISTENER) {
                main.accept(source, message);
            }
            if (verboseOptions.isVerbose()) {
                verboseOptions.reportToErrorStream("WEB", source.getName() + ": " + message);
            }
        }
    }
}
