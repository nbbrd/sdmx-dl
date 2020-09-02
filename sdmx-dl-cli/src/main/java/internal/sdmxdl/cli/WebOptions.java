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
import sdmxdl.LanguagePriorityList;
import sdmxdl.ext.SdmxCache;
import sdmxdl.kryo.KryoSerialization;
import sdmxdl.sys.SdmxSystemUtil;
import sdmxdl.util.ext.FileCache;
import sdmxdl.util.ext.Serializer;
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
@lombok.Data
@SuppressWarnings("FieldMayBeFinal")
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

    @CommandLine.Option(
            names = {"-l", "--languages"},
            paramLabel = "<langs>",
            converter = LangsConverter.class,
            defaultValue = "*",
            descriptionKey = "sdmxdl.cli.languages"
    )
    private LanguagePriorityList langs;

    @CommandLine.Option(
            names = {"--no-cache"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.noCache"
    )
    private boolean noCache;

    @CommandLine.Option(
            names = {"--no-sys-proxy"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.noSysProxy"
    )
    private boolean noSysProxy;

    @CommandLine.Option(
            names = {"--no-sys-ssl"},
            defaultValue = "false",
            descriptionKey = "sdmxdl.cli.noSysSsl"
    )
    private boolean noSysSsl;

    public SdmxWebManager getManager() throws IOException {
        SdmxWebManager.Builder result = SdmxWebManager.ofServiceLoader()
                .toBuilder()
                .languages(langs)
                .cache(getCache())
                .eventListener(getEventListener())
                .customSources(getCustomSources());

        if (!isNoSysProxy()) {
            SdmxSystemUtil.configureProxy(result, this::reportIOException);
        }

        if (!isNoSysSsl()) {
            SdmxSystemUtil.configureSsl(result, this::reportIOException);
        }

        return result.build();
    }

    private SdmxCache getCache() {
        return noCache
                ? SdmxCache.noOp()
                : FileCache
                .builder()
                .serializer(Serializer.gzip(new KryoSerialization()))
                .onIOException(this::reportIOException)
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
            System.out.println("IO: " + message + " - " + ex.getMessage());
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
            System.out.println(source.getName() + ": " + message);
        }
    }
}
