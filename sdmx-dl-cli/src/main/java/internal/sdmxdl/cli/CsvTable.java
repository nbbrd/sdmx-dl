package internal.sdmxdl.cli;

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

    public void write(CsvOutputOptions csv, Stream<T> list) throws IOException {
        write(csv, list.iterator());
    }

    public void write(CsvOutputOptions csv, Iterable<T> list) throws IOException {
        write(csv, list.iterator());
    }

    public void write(CsvOutputOptions csv, Iterator<T> list) throws IOException {
        CsvUtil.write(csv, w -> writeNames(w), w -> {
            while (list.hasNext()) {
                writeValues(w, list.next());
            }
        });
    }

    void writeNames(Csv.Writer writer) throws IOException {
        for (CsvColumn<T> column : columns) {
            column.writeName(writer);
        }
        writer.writeEndOfLine();
    }

    private void writeValues(Csv.Writer writer, T item) throws IOException {
        for (CsvColumn<T> column : columns) {
            column.writeValue(writer, item);
        }
        writer.writeEndOfLine();
    }
}
