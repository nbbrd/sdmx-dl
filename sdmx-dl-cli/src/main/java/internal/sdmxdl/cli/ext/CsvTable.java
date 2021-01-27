package internal.sdmxdl.cli.ext;

import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.text.Formatter;
import nbbrd.picocsv.Csv;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@lombok.Value
@lombok.Builder
public class CsvTable<T> {

    @lombok.Singular
    List<CsvColumn<T>> columns;

    public static <T> Builder<T> builderOf(Class<T> type) {
        return builder();
    }

    public static class Builder<T> {

        public <C> Builder<T> columnOf(String name, Function<? super T, ? extends C> extractor, Formatter<C> formatter) {
            return column(new CsvColumn<>(name, formatter.compose(extractor)));
        }
    }

    public void write(CsvOutputOptions csv, Stream<T> rows) throws IOException {
        write(csv, rows.iterator());
    }

    public void write(CsvOutputOptions csv, Iterable<T> rows) throws IOException {
        write(csv, rows.iterator());
    }

    public void write(CsvOutputOptions csv, Iterator<T> rows) throws IOException {
        CsvUtil.write(csv, w -> writeNames(w), w -> {
            while (rows.hasNext()) {
                writeValues(w, rows.next());
            }
        });
    }

    void writeNames(Csv.Writer writer) throws IOException {
        for (CsvColumn<T> column : columns) {
            column.writeName(writer);
        }
        writer.writeEndOfLine();
    }

    private void writeValues(Csv.Writer writer, T row) throws IOException {
        for (CsvColumn<T> column : columns) {
            column.writeValue(writer, row);
        }
        writer.writeEndOfLine();
    }
}
