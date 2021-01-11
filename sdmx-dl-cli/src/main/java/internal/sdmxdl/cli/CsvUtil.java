package internal.sdmxdl.cli;

import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.function.IOConsumer;
import nbbrd.picocsv.Csv;

import java.io.IOException;

@lombok.experimental.UtilityClass
public class CsvUtil {

    public void write(CsvOutputOptions csv, IOConsumer<Csv.Writer> head, IOConsumer<Csv.Writer> body) throws IOException {
        try (Csv.Writer w = csv.newCsvWriter()) {
            if (!csv.isAppending()) head.acceptWithIO(w);
            body.acceptWithIO(w);
        }
    }
}
