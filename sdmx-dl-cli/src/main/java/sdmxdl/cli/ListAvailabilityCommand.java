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
        Availability availability = getAvailability();
        getTable(availability.getDimension()).write(csv, availability.getCodes().entrySet());
        return null;
    }

    private CsvTable<Map.Entry<String, String>> getTable(String dimensionId) {
        return CsvTable.<Map.Entry<String, String>>builder()
                .columnOf("Code", Map.Entry::getKey)
                .columnOf("Label", Map.Entry::getValue)
                .columnOf("Dimension", ignore -> dimensionId)
                .build();
    }

    private Availability getAvailability() throws IOException {
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
