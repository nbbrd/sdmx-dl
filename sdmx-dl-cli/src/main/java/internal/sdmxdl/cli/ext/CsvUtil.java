package internal.sdmxdl.cli.ext;

import nbbrd.console.picocli.csv.CsvOutput;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.text.Formatter;
import nbbrd.picocsv.Csv;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

@lombok.experimental.UtilityClass
public class CsvUtil {

    public void write(CsvOutput csv, IOConsumer<Csv.Writer> head, IOConsumer<Csv.Writer> body) throws IOException {
        try (Csv.Writer w = csv.newCsvWriter()) {
            if (!csv.isAppending()) head.acceptWithIO(w);
            body.acceptWithIO(w);
        }
    }

    public <T> Formatter<Iterable<T>> fromIterable(Formatter<T> itemFormatter, char delimiter) {
        Csv.Format format = Csv.Format.RFC4180.toBuilder().delimiter(delimiter).build();
        return list -> list != null ? formatIterator(list.iterator(), format, itemFormatter) : null;
    }

    public <K, V> Formatter<Map<K, V>> fromMap(Formatter<K> keyFormatter, Formatter<V> valueFormatter, char listDelimiter, char entryDelimiter) {
        Csv.Format csvFormat = Csv.Format.RFC4180.toBuilder().delimiter(entryDelimiter).separator(String.valueOf(listDelimiter)).build();
        return map -> map != null ? formatMap(map, csvFormat, keyFormatter, valueFormatter) : null;
    }

    private <T> CharSequence formatIterator(Iterator<T> iterator, Csv.Format csvFormat, Formatter<T> itemFormatter) {
        try {
            StringWriter result = new StringWriter();
            try (Csv.Writer w = Csv.Writer.of(csvFormat, Csv.WriterOptions.DEFAULT, result, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
                while (iterator.hasNext()) {
                    w.writeField(itemFormatter.format(iterator.next()));
                }
            }
            return result.toString();
        } catch (IOException ex) {
            return null;
        }
    }

    private <K, V> CharSequence formatMap(Map<K, V> map, Csv.Format csvFormat, Formatter<K> keyFormatter, Formatter<V> valueFormatter) {
        try {
            StringWriter result = new StringWriter();
            try (Csv.Writer w = Csv.Writer.of(csvFormat, Csv.WriterOptions.DEFAULT, result, Csv.DEFAULT_CHAR_BUFFER_SIZE)) {
                Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
                if (iterator.hasNext()) {
                    Map.Entry<K, V> first = iterator.next();
                    w.writeField(keyFormatter.format(first.getKey()));
                    w.writeField(valueFormatter.format(first.getValue()));
                }
                while (iterator.hasNext()) {
                    Map.Entry<K, V> next = iterator.next();
                    w.writeEndOfLine();
                    w.writeField(keyFormatter.format(next.getKey()));
                    w.writeField(valueFormatter.format(next.getValue()));
                }
            }
            return result.toString();
        } catch (IOException ex) {
            return null;
        }
    }
}
