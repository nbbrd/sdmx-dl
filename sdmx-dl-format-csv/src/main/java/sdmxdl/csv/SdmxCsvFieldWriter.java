package sdmxdl.csv;

import nbbrd.io.function.IOConsumer;
import nbbrd.io.text.Formatter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import sdmxdl.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public interface SdmxCsvFieldWriter {

    void writeHead(@NonNull IOConsumer<CharSequence> output) throws IOException;

    void writeBody(@NonNull Series series, @NonNull Obs obs, @NonNull IOConsumer<CharSequence> output) throws IOException;

    @FunctionalInterface
    interface SingleField {

        @Nullable CharSequence apply(@NonNull Series series, @NonNull Obs obs);
    }

    static @NonNull SdmxCsvFieldWriter single(@NonNull String label, @NonNull SingleField value) {
        Objects.requireNonNull(label);
        Objects.requireNonNull(value);
        return new SdmxCsvFieldWriter() {
            @Override
            public void writeHead(IOConsumer<CharSequence> output) throws IOException {
                output.acceptWithIO(label);
            }

            @Override
            public void writeBody(Series series, Obs obs, IOConsumer<CharSequence> output) throws IOException {
                output.acceptWithIO(value.apply(series, obs));
            }
        };
    }

    @FunctionalInterface
    interface MultiField {

        @NonNull CharSequence apply(@NonNull Series series, @NonNull Obs obs, int i);
    }

    static @NonNull SdmxCsvFieldWriter multi(@NonNull List<String> labels, @NonNull MultiField values) {
        Objects.requireNonNull(labels);
        Objects.requireNonNull(values);
        return new SdmxCsvFieldWriter() {
            @Override
            public void writeHead(IOConsumer<CharSequence> output) throws IOException {
                for (String label : labels) {
                    output.acceptWithIO(label);
                }
            }

            @Override
            public void writeBody(Series series, Obs obs, IOConsumer<CharSequence> output) throws IOException {
                for (int i = 0; i < labels.size(); i++) {
                    output.acceptWithIO(values.apply(series, obs, i));
                }
            }
        };
    }

    static @NonNull SdmxCsvFieldWriter onDataflow(@NonNull String label, @NonNull DataflowRef ref) {
        Objects.requireNonNull(ref);
        String dataflow = SdmxCsvFields.getDataflowRefFormatter().formatAsString(ref);
        return single(label, (series, obs) -> dataflow);
    }

    static @NonNull SdmxCsvFieldWriter onKeyDimensions(@NonNull DataStructure dsd) {
        return onKeyDimensions(dsd.getDimensions()
                .stream()
                .map(Dimension::getId)
                .collect(Collectors.toList()));
    }

    static @NonNull SdmxCsvFieldWriter onKeyDimensions(@NonNull List<String> labels) {
        return multi(labels, (series, obs, i) -> series.getKey().get(i));
    }

    static @NonNull SdmxCsvFieldWriter onTimeDimension(@NonNull DataStructure dsd, @NonNull Formatter<LocalDateTime> formatter) {
        // FIXME: dsd#getTimeDimensionId() might be null !
        return onTimeDimension(dsd.getTimeDimensionId(), formatter);
    }

    static @NonNull SdmxCsvFieldWriter onTimeDimension(@NonNull String label, @NonNull Formatter<LocalDateTime> formatter) {
        Objects.requireNonNull(formatter);
        return single(label, (series, obs) -> formatter.format(obs.getPeriod()));
    }

    static @NonNull SdmxCsvFieldWriter onObsValue(@NonNull String label, @NonNull Formatter<Number> formatter) {
        Objects.requireNonNull(formatter);
        return single(label, (series, obs) -> formatter.format(obs.getValue()));
    }

    static @NonNull SdmxCsvFieldWriter onAttributes(@NonNull DataStructure dsd) {
        return onAttributes(dsd.getAttributes()
                .stream()
                .sorted(Comparator.comparing(Attribute::getId))
                .map(Attribute::getId)
                .collect(Collectors.toList()));
    }

    static @NonNull SdmxCsvFieldWriter onAttributes(@NonNull List<String> labels) {
        return multi(labels, (series, obs, i) -> {
            String label = labels.get(i);
            String value = series.getMeta().get(label);
            return value != null ? value : obs.getMeta().get(label);
        });
    }

    static @NonNull SdmxCsvFieldWriter onCompactObsAttributes(@NonNull String label, @NonNull Formatter<Map<String, String>> formatter) {
        Objects.requireNonNull(formatter);
        return single(label, (series, obs) -> formatter.format(obs.getMeta()));
    }

    static @NonNull SdmxCsvFieldWriter onSeriesKey(@NonNull String label) {
        return single(label, (series, obs) -> series.getKey().toString());
    }

    static @NonNull SdmxCsvFieldWriter onConstant(@NonNull String label, @NonNull String value) {
        Objects.requireNonNull(value);
        return single(label, (series, obs) -> value);
    }
}
