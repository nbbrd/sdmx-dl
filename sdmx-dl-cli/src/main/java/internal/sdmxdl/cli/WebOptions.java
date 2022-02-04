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
import sdmxdl.web.SdmxWebListener;
import sdmxdl.web.SdmxWebManager;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.xml.XmlWebSource;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
            names = {"--stackTrace"},
            defaultValue = "false",
            hidden = true
    )
    private boolean stackTrace;

    public SdmxWebManager loadManager() throws IOException {
        return SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .languages(langs)
                .eventListener(getEventListener())
                .customSources(parseCustomSources())
                .build();
    }

    private SdmxWebListener getEventListener() {
        SdmxWebListener original = isNoLog() ? SdmxWebListener.noOp() : SdmxWebListener.getDefault();
        return new VerboseWebListener(original, verboseOptions);
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

    @lombok.AllArgsConstructor
    private static class VerboseWebListener implements SdmxWebListener {

        @lombok.NonNull
        private final SdmxWebListener main;

        @lombok.NonNull
        private final VerboseOptions verboseOptions;

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void onWebSourceEvent(SdmxWebSource source, String message) {
            if (main.isEnabled()) {
                main.onWebSourceEvent(source, message);
            }
            if (verboseOptions.isVerbose()) {
                verboseOptions.reportToErrorStream("WEB", source.getName() + ": " + message);
            }
        }
    }
}
