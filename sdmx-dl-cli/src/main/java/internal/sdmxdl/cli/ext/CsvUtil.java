package internal.sdmxdl.cli.ext;

import nbbrd.console.picocli.csv.CsvOutput;
import nbbrd.design.MightBePromoted;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.picocsv.Picocsv;
import nbbrd.io.text.Formatter;
import nbbrd.picocsv.Csv;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@lombok.experimental.UtilityClass
public class CsvUtil {

    public static final Formatter<Iterable<String>> DEFAULT_LIST_FORMATTER = fromIterable(Formatter.onString(), ',');

    public static final Formatter<Map<String, String>> DEFAULT_MAP_FORMATTER = CsvUtil.fromMap(Formatter.onString(), Formatter.onString(), ',', '=');

    public static void write(CsvOutput csv, IOConsumer<Csv.Writer> head, IOConsumer<Csv.Writer> body) throws IOException {
        try (Csv.Writer w = csv.newCsvWriter()) {
            if (!csv.isAppending()) head.acceptWithIO(w);
            body.acceptWithIO(w);
        }
    }

    public static <T> Formatter<Iterable<T>> fromIterable(Formatter<T> itemFormatter, char delimiter) {
        return elementsFormatter(itemFormatter)
                .format(Csv.Format.RFC4180.toBuilder().delimiter(delimiter).build())
                .build()
                .compose(Iterable<T>::iterator)
                .asFormatter();
    }

    public static <K, V> Formatter<Map<K, V>> fromMap(Formatter<K> keyFormatter, Formatter<V> valueFormatter, char listDelimiter, char entryDelimiter) {
        return mapEntriesFormatter(keyFormatter, valueFormatter)
                .format(Csv.Format.RFC4180.toBuilder().delimiter(entryDelimiter).separator(String.valueOf(listDelimiter)).build())
                .build()
                .asFormatter()
                .compose(map -> map.entrySet().iterator());
    }

    @MightBePromoted
    private static <T> Picocsv.Formatter.Builder<Iterator<T>> elementsFormatter(Formatter<T> element) {
        return Picocsv.Formatter.builder((iterator, csv) -> formatElements(csv, iterator, element));
    }

    private static <T> void formatElements(Csv.Writer csv, Iterator<T> iterator, Formatter<T> element) throws IOException {
        while (iterator.hasNext()) {
            csv.writeField(element.format(iterator.next()));
        }
    }

    @MightBePromoted
    private static <K, V> Picocsv.Formatter.Builder<Iterator<Map.Entry<K, V>>> mapEntriesFormatter(Formatter<K> key, Formatter<V> value) {
        return Picocsv.Formatter.builder((mapEntries, csv) -> formatMapEntries(csv, mapEntries, key, value));
    }

    private static <K, V> void formatMapEntries(Csv.Writer csv, Iterator<Map.Entry<K, V>> mapEntries, Formatter<K> key, Formatter<V> value) throws IOException {
        if (mapEntries.hasNext()) {
            Map.Entry<K, V> first = mapEntries.next();
            csv.writeField(key.format(first.getKey()));
            csv.writeField(value.format(first.getValue()));
            while (mapEntries.hasNext()) {
                csv.writeEndOfLine();
                Map.Entry<K, V> next = mapEntries.next();
                csv.writeField(key.format(next.getKey()));
                csv.writeField(value.format(next.getValue()));
            }
        }
    }
}
