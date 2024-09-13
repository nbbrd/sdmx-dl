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

import internal.sdmxdl.cli.SortOptions;
import internal.sdmxdl.cli.WebKeyOptions;
import internal.sdmxdl.cli.ext.CsvUtil;
import internal.sdmxdl.cli.ext.RFC4180OutputOptions;
import nbbrd.picocsv.Csv;
import picocli.CommandLine;
import sdmxdl.Connection;
import sdmxdl.Options;
import sdmxdl.ext.SdmxCubeUtil;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

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
    private SortOptions sort;

    @CommandLine.Parameters(
            index = "3",
            paramLabel = "<index>",
            descriptionKey = "cli.sdmx.dimensionIndex"
    )
    private int dimensionIndex;

    @Override
    public Void call() throws Exception {
        CsvUtil.write(csv, this::writeHead, this::writeBody);
        return null;
    }

    private void writeHead(Csv.Writer w) throws IOException {
        w.writeField("Code");
        w.writeEndOfLine();
    }

    private void writeBody(Csv.Writer w) throws IOException {
        try (Connection conn = web.loadManager().getConnection(web.getSource(), Options.of(web.getLangs()))) {
            try (Stream<String> children = SdmxCubeUtil.getChildren(conn, web.getFlow(), web.getKey(), dimensionIndex)) {
                Iterator<String> iterator = sort.applySort(children, Comparator.naturalOrder()).iterator();
                while (iterator.hasNext()) {
                    w.writeField(iterator.next());
                    w.writeEndOfLine();
                }
            }
        }
    }
}
