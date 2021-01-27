package internal.sdmxdl.cli.ext;

import nbbrd.io.text.Formatter;
import nbbrd.picocsv.Csv;

import java.io.IOException;

@lombok.Value
public class CsvColumn<T> {

    @lombok.NonNull
    String name;

    @lombok.NonNull
    Formatter<T> formatter;

    void writeName(Csv.Writer writer) throws IOException {
        writer.writeField(name);
    }

    void writeValue(Csv.Writer writer, T item) throws IOException {
        writer.writeField(formatter.format(item));
    }
}
