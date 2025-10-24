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

import internal.sdmxdl.cli.ext.Anchor;
import internal.sdmxdl.cli.ext.VerboseOptions;
import nbbrd.design.ReturnNew;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import sdmxdl.ErrorListener;
import sdmxdl.EventListener;
import sdmxdl.Languages;
import sdmxdl.provider.ri.registry.RiRegistry;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.WebSource;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;
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
            defaultValue = "${env:SDMXDL_REGISTRY_SOURCESFILE}",
            paramLabel = "<file>",
            descriptionKey = "cli.sdmx.sourcesFile"
    )
    private File sourcesFile;

    @CommandLine.Option(
            names = {"-l", "--languages"},
            paramLabel = "<langs>",
            converter = LangsConverter.class,
            defaultValue = Languages.ANY_KEYWORD,
            descriptionKey = "cli.sdmx.languages"
    )
    private Languages langs;

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

    @CommandLine.Option(
            names = {SpecialProperties.NO_CONFIG_OPTION},
            defaultValue = "false",
            hidden = true
    )
    private boolean noConfig;

    @ReturnNew
    public SdmxWebManager loadManager() throws IOException {
        System.setProperty(
                RiRegistry.SOURCES_FILE_PROPERTY.getKey(),
                hasSourceFile() ? sourcesFile.toString() : RiRegistry.NO_SOURCES_FILE.toString()
        );

        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .onEvent(getEventListener())
                .onError(getErrorListener())
                .onRegistryEvent((marker, message) -> {
                    if (verboseOptions.isVerbose())
                        verboseOptions.reportToErrorStream(Anchor.CFG, marker + ": " + message);
                })
                .onRegistryError((marker, message, error) -> {
                    if (verboseOptions.isVerbose())
                        verboseOptions.reportToErrorStream(Anchor.CFG, marker + ": " + message, error);
                })
                .build();
    }

    private boolean hasSourceFile() {
        return !isNoConfig() && sourcesFile != null && sourcesFile.exists() && sourcesFile.isFile();
    }

    private Function<? super WebSource, EventListener> getEventListener() {
        Function<? super WebSource, EventListener> original = isNoLog() ? null : source -> new LoggingListener(source)::onSourceEvent;
        VerboseEventListener result = new VerboseEventListener(original, verboseOptions);
        return source -> (marker, message) -> result.onSourceEvent(source, marker, message);
    }

    private Function<? super WebSource, ErrorListener> getErrorListener() {
        Function<? super WebSource, ErrorListener> original = isNoLog() ? null : source -> new LoggingListener(source)::onSourceError;
        VerboseErrorListener result = new VerboseErrorListener(original, verboseOptions);
        return source -> (marker, message, ex) -> result.onSourceError(source, marker, message, ex);
    }

    @lombok.extern.java.Log
    @lombok.AllArgsConstructor
    private static class LoggingListener {

        private final WebSource source;

        public void onSourceEvent(String marker, CharSequence message) {
            if (log.isLoggable(Level.INFO)) {
                log.info(message.toString());
            }
        }

        public void onSourceError(String marker, CharSequence message, IOException error) {
            if (log.isLoggable(Level.INFO)) {
                log.log(Level.INFO, message.toString(), error);
            }
        }
    }

    @lombok.AllArgsConstructor
    private static class VerboseEventListener {

        private final @Nullable Function<? super WebSource, EventListener> main;

        @lombok.NonNull
        private final VerboseOptions verboseOptions;

        public void onSourceEvent(WebSource source, String marker, CharSequence message) {
            if (main != null) {
                main.apply(source).accept(marker, message);
            }
            if (verboseOptions.isVerbose()) {
                verboseOptions.reportToErrorStream(Anchor.WEB, source.getId() + ": " + message);
            }
        }
    }

    @lombok.AllArgsConstructor
    private static class VerboseErrorListener {

        private final @Nullable Function<? super WebSource, ErrorListener> main;

        @lombok.NonNull
        private final VerboseOptions verboseOptions;

        public void onSourceError(WebSource source, String marker, CharSequence message, IOException error) {
            if (main != null) {
                main.apply(source).accept(marker, message, error);
            }
            if (verboseOptions.isVerbose()) {
                verboseOptions.reportToErrorStream(Anchor.WEB, source.getId() + ": " + message, error);
            }
        }
    }
}
