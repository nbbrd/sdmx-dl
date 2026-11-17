/*
 * Copyright 2020 National Bank of Belgium
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

import internal.sdmxdl.cli.HiddenSortOptions;
import internal.sdmxdl.cli.WebKeyOptions;
import internal.sdmxdl.cli.ext.CsvTable;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import org.jspecify.annotations.Nullable;
import picocli.CommandLine;
import sdmxdl.*;

/**
 * @author Philippe Charles
 */
@CommandLine.Command(name = "availability")
public final class ListAvailabilityCommand implements Callable<Void> {

    @CommandLine.Mixin
    private WebKeyOptions web;

    @CommandLine.Mixin
    private final RFC4180OutputOptions csv = new RFC4180OutputOptions();

    @CommandLine.Mixin
    private HiddenSortOptions sortOptions;

    @CommandLine.Parameters(
            index = "3",
            arity = "0..1",
            paramLabel = "<dimension>",
            defaultValue = AvailabilityRequest.FIRST_WILDCARD_DIMENSION,
            descriptionKey = "cli.sdmx.availabilityDimension")
    private String dimension;

    @Override
    public Void call() throws Exception {
        getTable().write(csv, getRows(getAvailability()));
        return null;
    }

    private CsvTable<AvailableCode> getTable() {
        return CsvTable.builderOf(AvailableCode.class)
                .columnOf("Code", AvailableCode::getCode)
                .columnOf("Label", AvailableCode::getLabel)
                .columnOf("Dimension", AvailableCode::getDimension)
                .build();
    }

    private static List<AvailableCode> getRows(List<Availability> availabilities) {
        List<AvailableCode> result = new ArrayList<>();
        for (Availability availability : availabilities) {
            availability
                    .getCodes()
                    .forEach((code, label) -> result.add(new AvailableCode(code, label, availability.getDimension())));
        }
        return result;
    }

    @lombok.Value
    private static class AvailableCode {
        String code;

        @Nullable String label;

        String dimension;
    }

    private List<Availability> getAvailability() throws IOException {
        return web.loadManager()
                .usingName(web.getSource())
                .listAvailability(AvailabilityRequest.builder()
                        .languages(web.getLangs())
                        .database(web.getDatabase())
                        .flow(web.getFlow())
                        .key(web.getKey())
                        .dimension(dimension)
                        .build());
    }
}
