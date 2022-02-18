package sdmxdl.csv;

import nbbrd.io.text.TextBuffers;
import nbbrd.io.text.TextFormatter;
import nbbrd.picocsv.Csv;
import org.checkerframework.checker.nullness.qual.NonNull;
import sdmxdl.DataStructure;
import sdmxdl.Obs;
import sdmxdl.Series;
import sdmxdl.DataSet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static nbbrd.io.text.TextResource.newBufferedWriter;
import static sdmxdl.csv.SdmxCsvFields.*;

@lombok.Builder(toBuilder = true)
public final class SdmxPicocsvFormatter implements TextFormatter<DataSet> {

    @lombok.NonNull
    private final DataStructure dsd;

    @lombok.Builder.Default
    private final List<String> fields = Arrays.asList(DATAFLOW, KEY_DIMENSIONS, TIME_DIMENSION, OBS_VALUE, ATTRIBUTES, SERIESKEY);

    @lombok.Singular
    private final Map<String, Function<DataSet, SdmxCsvFieldWriter>> customFactories;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Csv.Format format = Csv.Format.RFC4180;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Csv.WriterOptions options = Csv.WriterOptions.DEFAULT;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Locale locale = Locale.ROOT;

    @lombok.Builder.Default
    private final boolean ignoreHeader = false;

    @Override
    public void formatWriter(DataSet data, Writer charWriter) throws IOException {
        try (Csv.Writer csv = newCsvWriter(charWriter, TextBuffers.UNKNOWN)) {
            formatCsv(data, csv);
        }
    }

    @Override
    public void formatStream(DataSet data, OutputStream stream, Charset charset) throws IOException {
        CharsetEncoder encoder = charset.newEncoder();
        try (Csv.Writer csv = newCsvWriter(newBufferedWriter(stream, encoder), TextBuffers.of(stream, encoder))) {
            formatCsv(data, csv);
        }
    }

    public void formatCsv(@NonNull DataSet data, Csv.@NonNull Writer w) throws IOException {
        SdmxCsvFieldWriter[] writers = fields.stream()
                .map(field -> getFieldWriter(data, field))
                .toArray(SdmxCsvFieldWriter[]::new);

        if (!ignoreHeader) {
            for (SdmxCsvFieldWriter writer : writers) {
                writer.writeHead(w::writeField);
            }
            w.writeEndOfLine();
        }

        for (Series series : data.getData()) {
            for (Obs obs : series.getObs()) {
                for (SdmxCsvFieldWriter writer : writers) {
                    writer.writeBody(series, obs, w::writeField);
                }
                w.writeEndOfLine();
            }
        }
    }

    private Csv.Writer newCsvWriter(Writer charWriter, TextBuffers buffers) throws IOException {
        return Csv.Writer.of(format, options, charWriter, buffers.getCharBufferSize());
    }

    private SdmxCsvFieldWriter getFieldWriter(DataSet dataSet, String field) {
        Function<DataSet, SdmxCsvFieldWriter> factory = customFactories.get(field);
        if (factory == null) {
            factory = getDefaultFactory(field);
        }
        return factory.apply(dataSet);
    }

    private Function<DataSet, SdmxCsvFieldWriter> getDefaultFactory(String field) {
        switch (field) {
            case DATAFLOW:
                return dataSet -> SdmxCsvFieldWriter.onDataflow(DATAFLOW, dataSet.getRef().getFlowRef());
            case KEY_DIMENSIONS:
                return dataSet -> SdmxCsvFieldWriter.onKeyDimensions(dsd);
            case TIME_DIMENSION:
                return dataSet -> SdmxCsvFieldWriter.onTimeDimension(dsd, getPeriodFormatter(dataSet.getData()));
            case OBS_VALUE:
                return dataSet -> SdmxCsvFieldWriter.onObsValue(OBS_VALUE, getValueFormatter(locale));
            case ATTRIBUTES:
                return dataSet -> SdmxCsvFieldWriter.onAttributes(dsd);
            case SERIESKEY:
                return dataSet -> SdmxCsvFieldWriter.onSeriesKey(SERIESKEY);
            default:
                return dataSet -> SdmxCsvFieldWriter.onConstant(field, "");
        }
    }
}
