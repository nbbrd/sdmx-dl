package sdmxdl.format.csv;

import lombok.NonNull;
import nbbrd.io.picocsv.Picocsv;
import nbbrd.picocsv.Csv;
import sdmxdl.DataSet;
import sdmxdl.DataStructure;
import sdmxdl.Obs;
import sdmxdl.Series;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static sdmxdl.format.csv.SdmxCsvFields.*;

@lombok.Builder(toBuilder = true)
public final class SdmxPicocsvFormatter {

    @lombok.Builder.Default
    private final List<String> fields = Arrays.asList(DATAFLOW, KEY_DIMENSIONS, TIME_DIMENSION, OBS_VALUE, ATTRIBUTES, SERIESKEY);

    @lombok.Singular
    private final Map<String, Function<DataSet, SdmxCsvFieldWriter>> customFactories;

    @lombok.NonNull
    @lombok.Builder.Default
    private final Locale locale = Locale.ROOT;

    @lombok.Builder.Default
    private final boolean ignoreHeader = false;

    public Picocsv.@NonNull Formatter<DataSet> getFormatter(DataStructure dsd) {
        return Picocsv.Formatter.<DataSet>builder((value, writer) -> formatCsv(dsd, value, writer)).build();
    }

    private void formatCsv(DataStructure dsd, DataSet data, Csv.Writer w) throws IOException {
        SdmxCsvFieldWriter[] writers = fields.stream()
                .map(field -> getFieldWriter(dsd, data, field))
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

    private SdmxCsvFieldWriter getFieldWriter(DataStructure dsd, DataSet dataSet, String field) {
        Function<DataSet, SdmxCsvFieldWriter> factory = customFactories.get(field);
        if (factory == null) {
            factory = getDefaultFactory(dsd, field);
        }
        return factory.apply(dataSet);
    }

    private Function<DataSet, SdmxCsvFieldWriter> getDefaultFactory(DataStructure dsd, String field) {
        switch (field) {
            case DATAFLOW:
                return dataSet -> SdmxCsvFieldWriter.onDataflow(DATAFLOW, dataSet.getRef());
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
