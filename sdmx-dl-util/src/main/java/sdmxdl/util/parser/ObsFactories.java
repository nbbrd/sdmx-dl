/*
 * Copyright 2017 National Bank of Belgium
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
package sdmxdl.util.parser;

import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.DataStructure;
import sdmxdl.ext.ObsFactory;
import sdmxdl.ext.ObsParser;
import sdmxdl.ext.spi.SdmxDialect;
import sdmxdl.file.SdmxFileSource;
import sdmxdl.file.spi.SdmxFileContext;
import sdmxdl.web.SdmxWebSource;
import sdmxdl.web.spi.SdmxWebContext;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Philippe Charles
 */
public enum ObsFactories implements ObsFactory {

    SDMX20 {
        @Override
        public @NonNull ObsParser getObsParser(@NonNull DataStructure dsd) {
            Objects.requireNonNull(dsd);
            return new DefaultObsParser(
                    FreqFactory.sdmx20(dsd),
                    PeriodParsers::onStandardFreq,
                    Parser.onDouble()
            );
        }
    },
    SDMX21 {
        @Override
        public @NonNull ObsParser getObsParser(@NonNull DataStructure dsd) {
            Objects.requireNonNull(dsd);
            return new DefaultObsParser(
                    FreqFactory.sdmx21(dsd),
                    PeriodParsers::onStandardFreq,
                    Parser.onDouble()
            );
        }
    };

    private static Optional<ObsFactory> lookupObsFactory(List<SdmxDialect> dialects, String name) {
        return dialects
                .stream()
                .filter(dialect -> dialect.getName().equals(name))
                .findFirst()
                .map(SdmxDialect::getObsFactory);
    }

    @Nullable
    public static ObsFactory getObsFactory(@NonNull SdmxFileContext context, @NonNull SdmxFileSource source) throws IOException {
        String dialectName = source.getDialect();
        if (dialectName == null) {
            return null;
        }
        return lookupObsFactory(context.getDialects(), dialectName)
                .orElseThrow(() -> new IOException("Failed to find a suitable dialect for '" + source + "'"));
    }

    @NonNull
    public static ObsFactory getObsFactory(@NonNull SdmxWebContext context, @NonNull SdmxWebSource source, @NonNull String defaultDialect) throws IOException {
        String dialectName = source.getDialect() != null ? source.getDialect() : defaultDialect;
        return lookupObsFactory(context.getDialects(), dialectName)
                .orElseThrow(() -> new IOException("Failed to find a suitable dialect for '" + source + "'"));
    }
}
