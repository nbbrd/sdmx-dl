package internal.sdmxdl.cli.ext;

import nbbrd.console.picocli.csv.CsvOutput;
import nbbrd.design.MightBePromoted;
import nbbrd.io.function.IOConsumer;
import nbbrd.io.picocsv.Picocsv;
import nbbrd.io.text.Formatter;
import nbbrd.io.text.TextFormatter;
import nbbrd.picocsv.Csv;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@lombok.experimental.UtilityClass
public class CsvUtil {

    public static void write(CsvOutput csv, IOConsumer<Csv.Writer> head, IOConsumer<Csv.Writer> body) throws IOException {
        try (Csv.Writer w = csv.newCsvWriter()) {
            if (!csv.isAppending()) head.acceptWithIO(w);
            body.acceptWithIO(w);
        }
    }

    public static <T> Formatter<Iterable<T>> fromIterable(Formatter<T> itemFormatter, char delimiter) {
        return asFormatter(
                elementsFormatter(itemFormatter)
                        .format(Csv.Format.RFC4180.toBuilder().delimiter(delimiter).build())
                        .build()
                        .compose(Iterable::iterator)
        );
    }

    public static <K, V> Formatter<Map<K, V>> fromMap(Formatter<K> keyFormatter, Formatter<V> valueFormatter, char listDelimiter, char entryDelimiter) {
        return asFormatter(
                mapEntriesFormatter(keyFormatter, valueFormatter)
                        .format(Csv.Format.RFC4180.toBuilder().delimiter(entryDelimiter).separator(String.valueOf(listDelimiter)).build())
                        .build()
                        .compose(map -> map.entrySet().iterator())
        );
    }

    public static final Formatter<Map<String, String>> DEFAULT_MAP_FORMATTER = CsvUtil.fromMap(Formatter.onString(), Formatter.onString(), ',', '=');

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

    @MightBePromoted
    private static <T> Formatter<T> asFormatter(TextFormatter<T> textFormatter) {
        return input -> {
            try {
                return input != null ? textFormatter.formatToString(input) : null;
            } catch (IOException ex) {
                return null;
            }
        };
    }
}
