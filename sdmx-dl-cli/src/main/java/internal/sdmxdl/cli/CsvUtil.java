package internal.sdmxdl.cli;

import nbbrd.console.picocli.csv.CsvOutputOptions;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.text.Formatter;
import nbbrd.picocsv.Csv;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

@lombok.experimental.UtilityClass
public class CsvUtil {

    public void write(CsvOutputOptions csv, IOConsumer<Csv.Writer> head, IOConsumer<Csv.Writer> body) throws IOException {
        try (Csv.Writer w = csv.newCsvWriter()) {
            if (!csv.isAppending()) head.acceptWithIO(w);
            body.acceptWithIO(w);
        }
    }

    public static @NonNull Formatter<URL> onURL() {
        return value -> value != null ? value.toString() : null;
    }

    public <T> Formatter<Iterable<T>> fromIterable(Formatter<T> itemFormatter, char delimiter) {
        Csv.Format format = Csv.Format.RFC4180.toBuilder().delimiter(delimiter).build();
        return list -> formatIterator(list.iterator(), format, itemFormatter);
    }

    public <K, V> Formatter<Map<K, V>> fromMap(Formatter<K> keyFormatter, Formatter<V> valueFormatter, char listDelimiter, char entryDelimiter) {
        Csv.Format listFormat = Csv.Format.RFC4180.toBuilder().delimiter(listDelimiter).build();
        Csv.Format entryFormat = Csv.Format.RFC4180.toBuilder().delimiter(entryDelimiter).build();
        return map -> formatMap(map, listFormat, entryFormat, keyFormatter, valueFormatter);
    }

    private <T> CharSequence formatIterator(Iterator<T> iterator, Csv.Format csvFormat, Formatter<T> itemFormatter) {
        try {
            StringWriter result = new StringWriter();
            try (Csv.Writer w = Csv.Writer.of(result, csvFormat)) {
                while (iterator.hasNext()) {
                    w.writeField(itemFormatter.format(iterator.next()));
                }
            }
            return result.toString();
        } catch (IOException ex) {
            return null;
        }
    }

    private <K, V> CharSequence formatMap(Map<K, V> map, Csv.Format listFormat, Csv.Format entryFormat, Formatter<K> keyFormatter, Formatter<V> valueFormatter) {
        return formatIterator(map.entrySet().iterator(), listFormat, entry -> formatEntry(entry, entryFormat, keyFormatter, valueFormatter));
    }

    private <K, V> CharSequence formatEntry(Map.Entry<K, V> entry, Csv.Format csvFormat, Formatter<K> keyFormatter, Formatter<V> valueFormatter) {
        try {
            StringWriter result = new StringWriter();
            try (Csv.Writer w = Csv.Writer.of(result, csvFormat)) {
                w.writeField(keyFormatter.format(entry.getKey()));
                w.writeField(valueFormatter.format(entry.getValue()));
            }
            return result.toString();
        } catch (IOException ex) {
            return null;
        }
    }
}
